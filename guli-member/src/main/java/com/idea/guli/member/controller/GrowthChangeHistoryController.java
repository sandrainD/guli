package com.idea.guli.member.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idea.guli.member.entity.GrowthChangeHistoryEntity;
import com.idea.guli.member.service.GrowthChangeHistoryService;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.R;



/**
 * 成长值变化历史记录
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:20:48
 */
@RestController
@RequestMapping("member/growthchangehistory")
public class GrowthChangeHistoryController {
    @Autowired
    private GrowthChangeHistoryService growthChangeHistoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//@RequiresPermissions("member:growthchangehistory:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = growthChangeHistoryService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/akebi")
    public R akebiDead(){
        return R.ok().put("akebi","\n" +
                "You are right, but Akebi-GC is a plug-in of a new open world adventure game independently developed by Taiga. The plug-in mainly solves a new open world adventure game \"Genshin Impact\" independently developed by Mihayou. Here, the person selected by Taiga will be awarded \"Akebi\" to guide the power of science and technology. You will play a mysterious role called \"hacker\". You will meet the noble people with different personalities and unique abilities in the free opening, defeat the strong enemy with them, and find the lost mother - at the same time, Gradually explore the truth of \"Akebi\".");
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:growthchangehistory:info")
    public R info(@PathVariable("id") Long id){
		GrowthChangeHistoryEntity growthChangeHistory = growthChangeHistoryService.getById(id);

        return R.ok().put("growthChangeHistory", growthChangeHistory);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:growthchangehistory:save")
    public R save(@RequestBody GrowthChangeHistoryEntity growthChangeHistory){
		growthChangeHistoryService.save(growthChangeHistory);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:growthchangehistory:update")
    public R update(@RequestBody GrowthChangeHistoryEntity growthChangeHistory){
		growthChangeHistoryService.updateById(growthChangeHistory);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:growthchangehistory:delete")
    public R delete(@RequestBody Long[] ids){
		growthChangeHistoryService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
