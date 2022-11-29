package com.stx.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stx.reggie.dto.SetmealDto;
import com.stx.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);

    public void deleteWithDish(List<Long> id);

    public void updateWithDish(int st,Long[] id);
}
