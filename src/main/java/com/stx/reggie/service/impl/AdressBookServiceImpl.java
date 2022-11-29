package com.stx.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stx.reggie.entity.AddressBook;
import com.stx.reggie.mapper.AdressBookMapper;
import com.stx.reggie.service.AdressBookService;
import org.springframework.stereotype.Service;

@Service
public class AdressBookServiceImpl extends ServiceImpl<AdressBookMapper, AddressBook> implements AdressBookService {
}
