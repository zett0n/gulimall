package cn.edu.zjut.product.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@Configuration
// 开启事务注解
@EnableTransactionManagement
@MapperScan("cn.edu.zjut.product.dao")
public class MybatisPlusConfig {

    // 引入分页插件旧版
    // @Bean
    // public PaginationInterceptor paginationInterceptor() {
    // PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
    // // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求 默认false
    // // paginationInterceptor.setOverflow(false);
    // // 设置最大单页限制数量，默认 500 条，-1 不受限制
    // // paginationInterceptor.setLimit(500);
    // // 开启 count 的 join 优化,只针对部分 left join
    // paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
    // return paginationInterceptor;
    // }

    // 引入分页插件(最新版)
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }
}