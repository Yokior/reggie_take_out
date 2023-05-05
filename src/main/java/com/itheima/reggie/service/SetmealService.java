package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;

import java.util.List;

public interface SetmealService extends IService<Setmeal>
{
    /**
     * 新增套餐 同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐 同时删除关联菜品
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    /**
     * 停售 起售
     * @param ids
     */
    void updateStatus(int st,List<Long> ids);

    /**
     * 根据ID获取setmealdish
     * @param id
     * @return
     */
    SetmealDto getSetmealDishById(Long id);

    /**
     * 更新套餐信息
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);

}
