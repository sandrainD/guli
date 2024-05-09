package com.idea.guli.product.feign;

import com.idea.common.to.SkuHasStockVo;
import com.idea.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-06 15:50
 **/

@FeignClient("guli-ware")
public interface WareFeignService {

    /**
     *
     * @param skuIds
     * @return
     */
    @PostMapping(value = "/ware/waresku/hasStock")
    R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds);

}
