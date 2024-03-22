package com.idea.guli.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.idea.common.utils.R;
import com.idea.guli.product.entity.AttrGroupEntity;
import com.idea.guli.product.entity.BrandEntity;
import com.idea.guli.product.service.AttrGroupService;
import com.idea.guli.product.service.BrandService;
import com.idea.guli.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
@Slf4j
@SpringBootTest
public class GuliProductApplicationTests {
    @Autowired
    public BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    AttrGroupService attrGroupService;
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("mi");
        brandEntity.setName("小米");

        brandService.save(brandEntity);
        System.out.println("do save success");
    }
    @Test
    public void  testFindPath(){
        Long[] catelogPath= categoryService.findCatelogPath(225L);
        log.info("完整路径"+ Arrays.asList(catelogPath));
    }
    @Test
    //@RequiresPermissions("product:attrgroup:info")
    public void info(){
        AttrGroupEntity attrGroup = attrGroupService.getById(4);
        Long attrGroupId1 = attrGroup.getCatelogId();
        log.info("查询的分组id："+attrGroupId1);
        Long[] path=categoryService.findCatelogPath(attrGroupId1);
        log.info("完整路径："+Arrays.asList(path));
        attrGroup.setCatelogPath(path);
        R attrGroup1 = R.ok().put("attrGroup", attrGroup);
        log.info(String.valueOf(attrGroup1));

    }
    @Test
    void contextLoads1() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("miser");
        brandService.updateById(brandEntity);
        System.out.println("do update success");
    }
    @Test
    void contextLoads2() {
        List<BrandEntity> brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        brand_id.forEach((item)->{
            System.out.println(item);
        });
        System.out.println("do update success");
    }


}
