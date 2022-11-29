package com.stx.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stx.reggie.entity.OrderDetail;
import com.stx.reggie.mapper.OrderDetailMapper;
import com.stx.reggie.service.OrderDetailService;
import com.stx.reggie.service.OrdersService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
