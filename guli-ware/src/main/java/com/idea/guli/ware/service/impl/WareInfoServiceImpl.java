package com.idea.guli.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.Query;
import com.idea.common.utils.R;
import com.idea.guli.ware.dao.WareInfoDao;
import com.idea.guli.ware.entity.WareInfoEntity;
import com.idea.guli.ware.feign.MemberFeignService;
import com.idea.guli.ware.service.WareInfoService;
import com.idea.guli.ware.vo.FareVo;
import com.idea.guli.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }
    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = (MemberAddressVo) r.getData2("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if (data != null) {
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 2, phone.length());
            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(substring));
            return fareVo;
        }
        return null;
    }

}