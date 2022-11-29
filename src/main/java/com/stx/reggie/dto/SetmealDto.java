package com.stx.reggie.dto;


import com.stx.reggie.entity.Setmeal;
import com.stx.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
