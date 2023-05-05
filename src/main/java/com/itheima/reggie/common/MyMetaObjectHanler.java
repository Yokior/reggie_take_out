package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理
 * 自动填充公共字段值
 */
@Component
@Slf4j
public class MyMetaObjectHanler implements MetaObjectHandler
{

    /**
     * 插入自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject)
    {
        log.info("公共字段自动填充insert");
        log.info(metaObject.toString());

        long id = Thread.currentThread().getId();
        log.info("线程ID为:{}",id);

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }

    /**
     * 修改自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject)
    {
        log.info("公共字段自动填充update");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
