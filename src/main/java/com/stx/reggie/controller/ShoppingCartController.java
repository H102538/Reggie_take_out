package com.stx.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stx.reggie.common.BaseContext;
import com.stx.reggie.common.R;
import com.stx.reggie.entity.ShoppingCart;
import com.stx.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
     public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询套餐或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);

        Long dishId = shoppingCart.getDishId();
        Long setmaalId = shoppingCart.getSetmealId();
        if(null!=dishId){
            //加入的是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加的是套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setmaalId);
        }

        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        if (one!=null){
            //如果存在数量加1
            Integer number = one.getNumber();
            one.setNumber(number + 1);
            shoppingCartService.updateById(one);
        }else {
            //如果不存在加入购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //重新赋值
            one = shoppingCart;
        }
        //返回给前端
        return R.success(one);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){

        LambdaUpdateWrapper<ShoppingCart> shoppingCartLambda =new LambdaUpdateWrapper<>();
        shoppingCartLambda.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(shoppingCartLambda);
        return R.success("已清空购物车！！");
    }


    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        ShoppingCart shoppingCartssss = null;
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        if (shoppingCart.getDishId()!=null){
            //减少的是菜品数量
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            ShoppingCart serviceOne = shoppingCartService.getOne(lambdaQueryWrapper);
            Integer number = serviceOne.getNumber();
            if (number>1){
//                大于一条之间不善
                Integer number1 = serviceOne.getNumber();
                serviceOne.setNumber(number1 - 1);
                shoppingCartService.updateById(serviceOne);
                return R.success(serviceOne);
            }else {
                //1条数据直接删除
                shoppingCartService.removeById(serviceOne);

                return R.success(shoppingCartssss);
            }

        }else {
            //减少的是套餐数量
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(lambdaQueryWrapper);
            Integer number = shoppingCart1.getNumber();
            if (number>1){
//                大于一条之间不善
                Integer number1 = shoppingCart1.getNumber();
                shoppingCart1.setNumber(number1 - 1);
                shoppingCartService.updateById(shoppingCart1);
                return R.success(shoppingCart1);
            }else {
                //1条数据直接删除
                shoppingCartService.removeById(shoppingCart1);
                return R.success(shoppingCartssss);
            }
        }


    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
       Long userId = BaseContext.getCurrentId();
       LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
       lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
       lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);

        return R.success(shoppingCarts);
    }
}
