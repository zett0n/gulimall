package cn.edu.zjut.thirdparty.controller;

import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.thirdparty.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class OssController {

    @Autowired
    private OssService ossService;

    /**
     * 获取对象存储服务端签名并返回给前端，前端直接传文件给 OSS
     */
    @GetMapping("/oss/policy")
    public R policy() {
        Map<String, String> respMap = this.ossService.generatePolicy();

        return R.ok().put("data", respMap);
    }
}
