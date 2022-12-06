package com.stx.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stx.reggie.common.CustomExcetion;
import com.stx.reggie.common.R;
import com.stx.reggie.dto.DishDto;
import com.stx.reggie.entity.Category;
import com.stx.reggie.entity.Dish;
import com.stx.reggie.entity.DishFlavor;
import com.stx.reggie.service.CategoryService;
import com.stx.reggie.service.DishFlavorService;
import com.stx.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品操作
 * @author Hasee
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
       log.info("dishDto:{}",dishDto);
       dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！！！");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page:{},pageName{},name{}",page,pageSize,name);
        Page<Dish> page1 = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishLambdaQueryWrapper.like(name!=null,Dish::getName,name);

        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(page1,dishLambdaQueryWrapper);

        BeanUtils.copyProperties(page1,dishDtoPage,"records");

        List<Dish> records = page1.getRecords();
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId(); //分类id
            Category category = categoryService.getById(categoryId); //通过分类id查分类名称
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
    @GetMapping("/{id}")
    public R<DishDto> getByIdWithF(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

        //清除redis中的缓存，
        String key = "dish_" + dishDto.getCategoryId()+"_"+"1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功！！！");
    }
    @DeleteMapping
    public R<String> delete(Long[] ids){

      List<Long> idList = Arrays.asList(ids);
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = dishService.count(dishLambdaQueryWrapper);
        if (count>0){
            throw new CustomExcetion("菜品正在售卖，无法删除");
        }
       for (int i = 0;i<ids.length;i++){

           Long id = ids[i];

           LambdaQueryWrapper<DishFlavor> dishFlavorLambda = new LambdaQueryWrapper<>();
           dishFlavorLambda.eq(DishFlavor::getDishId,id);
           dishFlavorService.remove(dishFlavorLambda);
       }
        if (dishService.removeByIds(idList)){

            return R.success("删除成功！！");
        }

        return R.error("删除失败");
    }

    @PostMapping("/status/{st}")
    public R<String> status(@PathVariable int st,Long[] ids){

        for (int i = 0;i<ids.length;i++){
            Long id = ids[i];
            LambdaUpdateWrapper<Dish> dishLambdaWrapper = new LambdaUpdateWrapper<Dish>();
            dishLambdaWrapper.eq(Dish::getId,id).set(Dish::getStatus,st);
            dishService.update(dishLambdaWrapper);
        }

        return R.success("操作成功！！！");
    }

    /**
     * 查询菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
   public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        String key = "dish_"+dish.getCategoryId() +"_"+ dish.getStatus();

        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList!=null){
            //如果存在，直接返回，无需查询数据库
            R.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //只查询状态为1商品（在售状态）
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lambdaQueryWrapper);
        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId(); //分类id
            Category category = categoryService.getById(categoryId); //通过分类id查分类名称
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId,id);
            List<DishFlavor> list2 = dishFlavorService.list(lambdaQueryWrapper1);
            dishDto.setFlavors(list2);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的数据存入redis中
        redisTemplate.opsForValue().set(key,dishDtoList,1, TimeUnit.HOURS);

        return R.success(dishDtoList);
    }

}
