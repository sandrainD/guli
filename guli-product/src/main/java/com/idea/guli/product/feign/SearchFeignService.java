package com.idea.guli.product.feign;

import com.idea.common.to.es.SkuEsModel;
import com.idea.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-06 17:12
 **/

@FeignClient("guli-search")
public interface SearchFeignService {

    @PostMapping(value = "/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);

}
