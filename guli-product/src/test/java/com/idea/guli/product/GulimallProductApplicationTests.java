package com.idea.guli.product;

import com.idea.guli.product.dao.AttrGroupDao;
import com.idea.guli.product.dao.SkuSaleAttrValueDao;
import com.idea.guli.product.service.BrandService;
import com.idea.guli.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }

//    @Test
//    public void test1() {
//        List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(13L);
//        saleAttrBySpuId.forEach(System.out::println);
//    }
//
//    @Test
//    public void test() {
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(130L, 225L);
//        attrGroupWithAttrsBySpuId.forEach(System.out::println);
//    }


    @Test
    public void testStringRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        ops.set("hello","world_" + UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");
        System.out.println("之前保存的数据:"+hello);
    }

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225l);

        log.info("完整路径catelogPath={}", Arrays.asList(catelogPath));
    }

//    @Test
//    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        String accessKeyId = "";
//        String accessKeySecret = "";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("C:\\Users\\Jerry\\Desktop\\1.png");
//
//        ossClient.putObject("gulimall-clouds", "1.png", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//
//        System.out.println("上传成功...");
//    }


    @Test
    public void contextLoads() {
        System.out.println(Integer.MAX_VALUE);

    }

}
