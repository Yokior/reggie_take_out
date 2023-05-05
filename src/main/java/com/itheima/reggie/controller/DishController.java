package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController
{
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto)
    {
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        // 删除缓存数据
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }


    /**
     * 分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Dish::getName, name);
        lqw.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, lqw);

        // 对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        ArrayList<DishDto> dishDtos = new ArrayList<>();

        for (Dish item : dishPage.getRecords())
        {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            // 根据id查分类对象
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            dishDtos.add(dishDto);
        }

        dishDtoPage.setRecords(dishDtos);

        return R.success(dishDtoPage);
    }

    /**
     * 根据ID查询菜品信息和对应口味数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id)
    {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto)
    {
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        // 清除缓存数据
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品信息
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish)
//    {
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        // 查询状态为1的 起售状态
//        lqw.eq(Dish::getStatus,1);
//        // 排序
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(lqw);
//
//        return R.success(dishList);
//    }


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish)
    {
        List<DishDto> dishDtoList = new ArrayList<>();
        // 从redis中查询数据
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null)
        {
            // 如果查到 直接返回 无需调用数据库
            return R.success(dishDtoList);
        }

        // 没查到 查询数据库

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        // 查询状态为1的 起售状态
        lqw.eq(Dish::getStatus,1);
        // 排序
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lqw);

        // 补充缺失数据
        dishDtoList = new ArrayList<>();

        for (Dish dish1 : dishList)
        {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1,dishDto);
            // 找到口味
            LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(DishFlavor::getDishId,dish1.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lqw2);
            // 存入Dto
            dishDto.setFlavors(dishFlavorList);
            // 找到categoryName
            Long categoryId = dish1.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null)
            {
                // 存入名称
                dishDto.setCategoryName(category.getName());
            }
            // 存入集合
            dishDtoList.add(dishDto);
        }

        // 将查到的数据缓存进redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);



        return R.success(dishDtoList);
    }

}
