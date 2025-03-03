package com.idea.guli.ware.controller;

import com.idea.common.exception.BizCodeEnume;
import com.idea.common.exception.NoStockException;
import com.idea.common.to.SkuHasStockVo;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.R;
import com.idea.guli.ware.entity.WareSkuEntity;
import com.idea.guli.ware.service.WareSkuService;
import com.idea.guli.ware.vo.LockStockResult;
import com.idea.guli.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 18:02:12
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 为订单锁定库存
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try{
            boolean b = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(),BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 查询sku是否有库存
     * @return
     */
    @PostMapping(value = "/hasStock")
    public R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds) {

        //skuId stock
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(vos);

    }


    /**
     * 列表
     */
    @RequestMapping("/list")
//@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
