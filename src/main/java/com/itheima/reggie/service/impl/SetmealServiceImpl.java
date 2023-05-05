package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.sun.org.apache.bcel.internal.generic.CALOAD;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService
{
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto)
    {
        // 保存套餐的基本信息
        this.save(setmealDto);
        // 保存套餐和菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes)
        {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids)
    {
        // 查询套餐状态 停售才可以删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);

        int count = this.count(lqw);

        // 不能删除 抛出业务异常
        if (count > 0)
        {
            throw new CustomException("有正在起售的套餐 不能删除");
        }

        // 可以删除 先删除套餐表中的数据
        this.removeByIds(ids);
        // 删除关联菜品信息
        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lqw2);
    }

    @Override
    public void updateStatus(int st, List<Long> ids)
    {
        // 查询要修改的套餐
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        List<Setmeal> setmealList = this.list(lqw);

        // 设置状态
        for (Setmeal setmeal : setmealList)
        {
            setmeal.setStatus(st);
        }
        // 更新数据
        this.updateBatchById(setmealList);

    }

    @Override
    public SetmealDto getSetmealDishById(Long id)
    {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 拷贝继承数据
        BeanUtils.copyProperties(setmeal,setmealDto);
        // 处理私有属性
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);
        // 设置SetmealDish集合
        setmealDto.setSetmealDishes(setmealDishList);
        // 设置categoryName
        Long categoryId = setmeal.getCategoryId();
        Category category = categoryService.getById(categoryId);
        setmealDto.setCategoryName(category.getName());

        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto)
    {
        // 更新基本信息
        this.updateById(setmealDto);
        // 更新菜品信息
        // 清除菜品关联套餐信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);
        // 重新设置菜品关联套餐信息
        // 原先ID需要重新设置
        Long id = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes)
        {
            setmealDish.setSetmealId(id);
        }
        // 保存关联信息
        setmealDishService.saveBatch(setmealDishes);
    }


}
