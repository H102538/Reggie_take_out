package com.stx.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stx.reggie.common.BaseContext;
import com.stx.reggie.common.R;
import com.stx.reggie.entity.AddressBook;
import com.stx.reggie.service.AdressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AdressBookContreller {

    @Autowired
    private AdressBookService adressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook > save(@RequestBody AddressBook addressBook){

        addressBook.setUserId(BaseContext.getCurrentId());

        adressBookService.save(addressBook);

        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<AddressBook> update(@RequestBody AddressBook addressBook){

        LambdaUpdateWrapper<AddressBook> addressBookLambda = new LambdaUpdateWrapper<>();
        addressBookLambda.eq(AddressBook::getUserId,BaseContext.getCurrentId()).set(AddressBook::getIsDefault,0);
        adressBookService.update(addressBookLambda);
        addressBook.setIsDefault(1);
        adressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable Long id){

        AddressBook adress = adressBookService.getById(id);
        if (null!=adress){
            return R.success(adress);
        }
        return R.error("没有找到该对象！！！");
    }

    @GetMapping("/default")
    public R getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = adressBookService.getOne(queryWrapper);
        if (null!=addressBook){
            return R.success(addressBook);
        }
        return R.error("没有默认的地址");
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null!=addressBook.getUserId(),AddressBook::getUserId,addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = adressBookService.list(queryWrapper);
        return R.success(list);
    }

    @PutMapping
    public R<String> updateById(@RequestBody AddressBook addressBook){

        adressBookService.updateById(addressBook);

        return R.success("修改成功！！");

    }
}
