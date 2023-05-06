package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController
{
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto)
    {
        log.info("新增套餐 {}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        // 模糊查询
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(setmealPage, lqw);
        // 拷贝对象
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        // 处理records数据
        List<Setmeal> records = setmealPage.getRecords();
        ArrayList<SetmealDto> list = new ArrayList<SetmealDto>();

        for (Setmeal item : records)
        {
            SetmealDto setmealDto = new SetmealDto();
            // 对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            // 分类ID
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null)
            {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
                list.add(setmealDto);
            }
        }

        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }


    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids)
    {
        log.info("删除套餐{}", ids);

        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 停售0 起售1
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/{st}")
    public R<String> status(@PathVariable int st, @RequestParam List<Long> ids)
    {
        log.info(String.valueOf(st));
        log.info(String.valueOf(ids));

        setmealService.updateStatus(st, ids);

        return R.success("状态更新成功");
    }


    /**
     * 查询单个套餐信息
     *
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id)
    {
        SetmealDto setmealDishById = setmealService.getSetmealDishById(id);
        return R.success(setmealDishById);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto)
    {
        setmealService.updateWithDish(setmealDto);
        return R.success("套餐信息保存成功");
    }

    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal)
    {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        lqw.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(lqw);

        return R.success(setmealList);
    }

}
