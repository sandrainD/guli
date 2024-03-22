package com.idea.guli.product.service.impl;

import com.idea.guli.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.Query;

import com.idea.guli.product.dao.CategoryDao;
import com.idea.guli.product.entity.CategoryEntity;
import com.idea.guli.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2、组装成父子的属性结构
        //(1)找出一级分类
        Stream<CategoryEntity> categoryEntityStream = categoryEntities.stream();
//        List<CategoryEntity> level1Menus = categoryEntityStream.filter((CategoryEntity categoryEntity) -> {
//                    return categoryEntity.getParentCid() == 0;
//                }
//        ).collect(Collectors.toList());
        List<CategoryEntity> level1Menus = categoryEntityStream.filter(categoryEntity ->{
                return categoryEntity.getParentCid() == 0;
        }).map(menu->{
                    menu.setChildren(getChildrens(menu,categoryEntities));
                    return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort())-(menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }


    //递归查找所有菜单的子菜单
    private  List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> allCategoryEntities){
        List<CategoryEntity> children = allCategoryEntities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity->{
            //找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,allCategoryEntities));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //找到菜单后进行排序然后进行收集返回
            return (menu1.getSort()==null?0:menu1.getSort())-(menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前要删除的菜单是否被其他地方引用
        baseMapper.deleteBatchIds(asList);
    }

    //[2,,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList();
        List<Long> paretPath = findParetPath(catelogId, paths);
        Collections.reverse(paretPath);

        return paretPath.toArray(new Long[paretPath.size()]);
    }

    /***
     * 级联更新所有的数据
     * @param category
     * 由于需要级联更新，所以需要开启事务，利于出错时回滚
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    private List<Long> findParetPath(Long catelogId,List<Long> paths){
        //1`收集当前节点的id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParetPath(byId.getParentCid(),paths);
        }
        return paths;
    }
}