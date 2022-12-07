package com.stx.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stx.reggie.common.CustomExcetion;
import com.stx.reggie.dto.SetmealDto;
import com.stx.reggie.entity.Setmeal;
import com.stx.reggie.entity.SetmealDish;
import com.stx.reggie.mapper.SetmealMapper;
import com.stx.reggie.service.SetmealDishService;
import com.stx.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealSerivceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void deleteWithDish(List<Long> id) {

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,id);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);
        int count = count(setmealLambdaQueryWrapper);
        if (count>0){
            throw new CustomExcetion("此套餐正在售卖，无法删除！！！");
        }
        removeByIds(id);
        LambdaQueryWrapper<SetmealDish> setmealDish = new LambdaQueryWrapper<>();
        setmealDish.in(SetmealDish::getSetmealId,id);
        setmealDishService.remove(setmealDish);
    }

    @Override
    public void updateWithDish(int st,Long[] id) {

        for (int i = 0;i<id.length;i++){
            Long ids = id[i];
            LambdaUpdateWrapper<Setmeal> dishLambdaWrapper = new LambdaUpdateWrapper<Setmeal>();
            dishLambdaWrapper.eq(Setmeal::getId,ids).set(Setmeal::getStatus,st);
            update(dishLambdaWrapper);
        }


    }
}
