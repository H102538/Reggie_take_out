package com.stx.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stx.reggie.common.R;
import com.stx.reggie.dto.OrdersDto;
import com.stx.reggie.dto.SetmealDto;
import com.stx.reggie.entity.AddressBook;
import com.stx.reggie.entity.Category;
import com.stx.reggie.entity.Orders;
import com.stx.reggie.entity.Setmeal;
import com.stx.reggie.service.AdressBookService;
import com.stx.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private AdressBookService adressBookService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        ordersService.submit(orders);

        return R.success("下单成功！！！");
    }

    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
       Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.orderByAsc(Orders::getOrderTime);
        ordersService.page(ordersPage,ordersLambdaQueryWrapper);
       return R.success(ordersPage);
    }

    @GetMapping("/page")
    public R<Page> page1(int page,int pageSize,String number, String beginTime, String endTime){
        log.info("{}{}{}",number,beginTime,endTime);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.like(number!=null,Orders::getId,number);
        ordersLambdaQueryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        ordersLambdaQueryWrapper.le(endTime != null,Orders::getOrderTime,endTime);
        ordersLambdaQueryWrapper.orderByAsc(Orders::getOrderTime);
        ordersService.page(ordersPage,ordersLambdaQueryWrapper);
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");


        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            Long categoryId = item.getAddressBookId();
            AddressBook addressBook = adressBookService.getById(categoryId);
            ordersDto.setUserName(addressBook.getConsignee());
            return ordersDto;

        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

    @PutMapping
    public R<String> status(@RequestBody Orders orders){

        LambdaUpdateWrapper<Orders> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(orders.getNumber()!=null,Orders::getNumber,orders.getNumber());
        lambdaUpdateWrapper.set(orders.getStatus()!=null,Orders::getStatus,orders.getStatus());
        ordersService.update(lambdaUpdateWrapper);

        return R.success("修改成功！！！");
    }

}
