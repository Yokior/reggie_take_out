package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService
{
    @Autowired
    private DishService dishService;

    @Autowired
    @Lazy // 延迟加载Bean
    private SetmealService setmealService;


    /**
     * 根据ID删除分类 删除前进行判断
     * @param id
     */
    @Override
    public void remove(Long id)
    {
        // 查询是否关联菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId,id);
        int count = dishService.count(lqw);
        if (count > 0)
        {
            // 已经关联菜品 抛出异常
            throw new CustomException("当前分类下关联了菜品 不能删除");
        }
        // 查询是否关联套餐
        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(lqw2);
        if (count1 > 0)
        {
            // 关联了套餐 抛出异常
            throw new CustomException("当前分类下关联了套餐 不能删除");
        }
        // 正常删除
        super.removeById(id);
    }
}
