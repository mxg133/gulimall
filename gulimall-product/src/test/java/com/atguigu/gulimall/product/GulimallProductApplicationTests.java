package com.atguigu.gulimall.product;

import com.aliyun.oss.*;
import com.aliyun.oss.model.PutObjectRequest;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Resource
    OSSClient ossClient;
    @Test
    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//// 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
//        String accessKeyId = "LTAI4G6sWhsMNcDrwdR7mKck";
//        String accessKeySecret = "ODyHrcm0AuMLobxHWl4IWM2vsqnh65";
//
//// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 创建PutObjectRequest对象。
//        PutObjectRequest putObjectRequest = new PutObjectRequest("gulimall-hello-3", "b.jpg", new File("/Users/mxg/Downloads/b.jpg"));

// 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
// ObjectMetadata metadata = new ObjectMetadata();
// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
// metadata.setObjectAcl(CannedAccessControlList.Private);
// putObjectRequest.setMetadata(metadata);

// 上传文件。
        ossClient.putObject("gulimall-hello-3", "b.jpg", new FileInputStream("/Users/mxg/Downloads/b.jpg"));


// 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成....");
    }

    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");
//
////        brandEntity.setName("华为");
//////        brandService.save(brandEntity);
////        System.out.println("保存OK！");
//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(item->{
            System.out.println(item);
        });
    }

}
