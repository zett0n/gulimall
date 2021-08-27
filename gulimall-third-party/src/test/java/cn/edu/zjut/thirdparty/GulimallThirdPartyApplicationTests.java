package cn.edu.zjut.thirdparty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.aliyun.oss.OSSClient;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        // String endpoint = "oss-cn-shanghai.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        // String accessKeyId = "LTAI4G4W1RA4JXz2QhoDwHhi";
        // String accessKeySecret = "R99lmDOJumF2x43ZBKT259Qpe70Oxw";

        // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\d6733\\Pictures\\img\\花与女生-视觉中国1-裁剪.jpg");
        this.ossClient.putObject("zett0n-gulimall", "花与女生-视觉中国1-裁剪.jpg", inputStream);

        // 关闭OSSClient。
        this.ossClient.shutdown();
        System.out.println("上传成功.");
    }

}
