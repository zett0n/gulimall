package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.CategoryDao;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.CategoryBrandRelationService;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.Catalog2VO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private final CategoryBrandRelationService categoryBrandRelationService;

    private final StringRedisTemplate stringRedisTemplate;

    /*
     * 不使用 RedisTemplate 而用 StringRedisTemplate 原因：
     * 存入 redis 中的 value 使用 JSON 字符串的形式兼容性更好
     * 如果使用 <String, Object> 方式，会采用 Java 序列化机制，如果其他微服务使用非 Java 语言无法读取数据
     */
    private final ValueOperations<String, String> ops;

    // TODO Autowird 空指针的解决办法与思考
    // 这里三个成员变量如果都用 Autowired，ops 初始化时还未执行 stringRedisTemplate 的自动注入就会报空指针
    public CategoryServiceImpl(CategoryBrandRelationService categoryBrandRelationService, StringRedisTemplate stringRedisTemplate) {
        this.categoryBrandRelationService = categoryBrandRelationService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.ops = this.stringRedisTemplate.opsForValue();
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }


    @Override
    public List<CategoryEntity> listWithTree() {
        // TODO stream api 复习
        // 1、查出所有分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 2、组装成父子的树形结构
        return categoryEntities.stream()
                // 过滤找出一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == DefaultConstant.NO_PARENT_CATEGORY)
                // 处理，给一级菜单递归设置子菜单
                .peek(menu -> menu.setChildren(findChildren(menu, categoryEntities)))
                // 因为按sort属性排序，所以 menu-> .. 将需要排序的字段 sort 映射给 Comparator 排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? DefaultConstant.SORT_DEFAULT_VALUE : menu.getSort())))
                .collect(Collectors.toList());
    }


    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> findChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek(categoryEntity -> categoryEntity.setChildren(findChildren(categoryEntity, all)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? DefaultConstant.SORT_DEFAULT_VALUE : menu.getSort())))
                .collect(Collectors.toList());
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查要删除的菜单是否被别的地方引用
        this.baseMapper.deleteBatchIds(asList);
    }


    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> catelogPath = findParentPath(catelogId, new ArrayList<>());
        Collections.reverse(catelogPath);
        return catelogPath.toArray(new Long[0]);
    }


    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);

        Long parentCid = this.getById(catelogId).getParentCid();
        if (parentCid != DefaultConstant.NO_PARENT_CATEGORY) {
            findParentPath(parentCid, path);
        }
        return path;
    }


    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        // 更新时需要同步更新 pms_category_brand_relation 等表中的冗余信息
        if (StringUtils.isNotEmpty(category.getName())) {
            this.categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO 更新其他关联
        }
    }


    /**
     * 查询一级分类
     */
    @Override
    public List<CategoryEntity> getRootCategories() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1L));
    }


    /**
     * 从数据库中查询所有目录，封装为 Map
     * 只需查一次数据库，后续通过 stream 封装，避免了循环查库
     * <p>
     * 【问题】所有目录作为热点数据，数据库查询压力过大
     * 【解决】引入缓存
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJSONFromDB() {
        log.debug("查询数据库...");

        // 查询目录所有信息
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 获取一级分类
        List<CategoryEntity> level1Categories = filterByParentCid(categoryEntities, 0L);

        // map 用于封装返回结果
        Map<String, List<Catalog2VO>> map = new HashMap<>();

        level1Categories.forEach(l1 -> {
            List<Catalog2VO> catalog2VOs = null;

            // 对于每个一级分类，查询它的二级分类
            List<CategoryEntity> level2Categories = filterByParentCid(categoryEntities, l1.getCatId());

            if (!level2Categories.isEmpty()) {
                catalog2VOs = level2Categories.stream()
                        .map(l2 -> {
                            Catalog2VO catalog2VO = new Catalog2VO();
                            catalog2VO.setCatalog1Id(l1.getCatId().toString())
                                    .setId(l2.getCatId().toString())
                                    .setName(l2.getName());

                            List<Catalog2VO.Catalog3VO> catalog3VOs = null;

                            // 对于每个二级分类，查询它的三级分类
                            List<CategoryEntity> level3Categories = filterByParentCid(categoryEntities, l2.getCatId());
                            if (!level3Categories.isEmpty()) {
                                catalog3VOs = level3Categories.stream()
                                        .map(l3 -> {
                                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO();
                                            catalog3VO.setCatalog2Id(l2.getCatId().toString())
                                                    .setId(l3.getCatId().toString())
                                                    .setName(l3.getName());

                                            return catalog3VO;
                                        })
                                        .collect(Collectors.toList());
                            }
                            catalog2VO.setCatalog3List(catalog3VOs);
                            return catalog2VO;
                        })
                        .collect(Collectors.toList());
            }
            map.put(l1.getCatId().toString(), catalog2VOs);
        });
        return map;
    }


    /**
     * 按父分类 id 过滤出对应的子分类
     */
    private List<CategoryEntity> filterByParentCid(List<CategoryEntity> categoryEntities, Long parentCid) {
        return categoryEntities.stream()
                .filter(item -> Objects.equals(item.getParentCid(), parentCid))
                .collect(Collectors.toList());
    }

    /**
     * 从缓存中获取数据，未找到则从数据库中查询
     * 应对一般并发没有问题
     * <p>
     * 【高并发下缓存失效问题】
     * 缓存穿透：查询一个 null 数据，请求穿透 Redis 直接访问了数据库         解决方案：缓存空数据
     * 缓存雪崩：大量的 key 同时过期，瞬间数据库压力过大                    解决方案：加随机过期时间
     * 缓存击穿：大量并发进来同时查询一个正好过期的数据，瞬间数据库压力过大     解决方案：加锁
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJSONFromRedis() {
        // 尝试从 redis 中获取数据
        String catalogJSON = this.ops.get("catalogJSON");

        if (StringUtils.isEmpty(catalogJSON)) {
            // redis 中未找到，从数据库中查询
            Map<String, List<Catalog2VO>> catalogJSONFromDB = getCatalogJSONFromDB();

            // 存入 redis，指定随机过期时间（24h ~ 25h 随机的任一秒）
            catalogJSON = JSON.toJSONString(catalogJSONFromDB);
            this.ops.set("catalogJSON", catalogJSON,
                    DefaultConstant.REDIS_BASIC_TTL + new Random().nextInt(DefaultConstant.REDIS_EXTRA_TTL_UPPER_LIMIT),
                    TimeUnit.SECONDS);

            return catalogJSONFromDB;
        }
        // redis 中找到数据，JSON 转 Map 并返回
        return ConvertJSONToMap(catalogJSON);
    }


    /**
     * 通过 JVM 本地锁解决缓存击穿问题
     * 在分布式模式下，使用本地锁只能锁本机 JVM 的线程
     * 当然，高并发场景下每台机器（一个 JVM）同时访问一次数据库，压力不大
     */
    public Map<String, List<Catalog2VO>> getCatalogJSONFromRedisWithLocalLock() {
        // 尝试从 redis 中获取数据
        String catalogJSON = this.ops.get("catalogJSON");

        if (StringUtils.isEmpty(catalogJSON)) {
            // redis 中未找到，从数据库中查询
            // 加锁防止缓存击穿
            synchronized (this) {
                // 获得锁后，再次检查 redis 是否有数据(double check)
                catalogJSON = this.ops.get("catalogJSON");

                // double check
                if (StringUtils.isEmpty(catalogJSON)) {
                    // 仍然没有，查询数据库
                    Map<String, List<Catalog2VO>> catalogJSONFromDB = getCatalogJSONFromDB();

                    // 存入 redis，指定随机过期时间（24h ~ 25h 随机的任一秒）
                    catalogJSON = JSON.toJSONString(catalogJSONFromDB);
                    this.ops.set("catalogJSON", catalogJSON,
                            DefaultConstant.REDIS_BASIC_TTL + new Random().nextInt(DefaultConstant.REDIS_EXTRA_TTL_UPPER_LIMIT),
                            TimeUnit.SECONDS);

                    // 返回结果并释放锁
                    return catalogJSONFromDB;
                }
            }
        }
        // redis 中找到数据，JSON 转 Map 并返回
        return ConvertJSONToMap(catalogJSON);
    }


    /**
     * 通过 Redis 分布式锁解决缓存击穿问题
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJSONFromRedisWithRedisLock() {
        // token 用于在删锁时判断是否是锁当前 JVM 的锁
        String token = UUID.randomUUID().toString();

        // 设置 redis 分布式锁，加锁和设置 ttl 为原子操作
        Boolean lock = this.ops.setIfAbsent("lock", token, DefaultConstant.REDIS_LOCK_TOKEN_TTL, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lock)) {
            log.debug("获取分布式锁成功！");

            // 加锁成功
            Map<String, List<Catalog2VO>> catalogJSONFromRedis;

            // try finally 保证业务奔溃就删锁，而不是等到 ttl
            try {
                // 执行业务...
                catalogJSONFromRedis = getCatalogJSONFromRedis();
            } finally {
                // lua 脚本原子删锁
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";

                // public <T> T execute(RedisScript<T> script, List<K> keys, Object... args)
                this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                        Collections.singletonList("lock"),
                        token
                );
            }
            return catalogJSONFromRedis;
        } else {
            log.debug("获取分布式锁失败，重试...");

            // 加锁失败，休眠 100ms 自旋重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJSONFromRedisWithRedisLock();
        }
    }


    // JSON 转 Map
    private Map<String, List<Catalog2VO>> ConvertJSONToMap(String catalogJSON) {
        TypeReference<Map<String, List<Catalog2VO>>> typeReference = new TypeReference<Map<String, List<Catalog2VO>>>() {
        };
        return JSON.parseObject(catalogJSON, typeReference);
    }
}