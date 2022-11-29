package com.stx.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stx.reggie.common.CustomExcetion;
import com.stx.reggie.entity.Category;
import com.stx.reggie.entity.Dish;
import com.stx.reggie.entity.Setmeal;
import com.stx.reggie.mapper.CategoryMapper;
import com.stx.reggie.service.CategoryService;
import com.stx.reggie.service.DishService;
import com.stx.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceimpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishLambdaQueryWrapper);
        //判断当前分类是否关联菜品
        if (count > 0) {
            //已经关联，抛出异常
           throw new CustomExcetion("当前分类已关联菜品，不能删除！！");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        //判断当前分类是否关联菜品
        if (count1 > 0) {
            //已经关联，抛出异常
            throw new CustomExcetion("当前分类已关联套餐，不能删除！！");
        }
        super.removeById(id);
    }
}
