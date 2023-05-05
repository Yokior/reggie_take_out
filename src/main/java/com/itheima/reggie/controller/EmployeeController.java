package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController
{
    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee)
    {
        // 将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 判断是否有数据
        if (emp == null)
        {
            return R.error("登录失败");
        }

        // 判断密码
        if (!(emp.getPassword().equals(password)))
        {
            return R.error("登录失败");
        }

        // 查询员工状态是否可用
        if (emp.getStatus() == 0)
        {
            return R.error("账号已禁用");
        }

        // 登录成功 将员工id存入session
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request)
    {
        // 清除session中的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee)
    {
        log.info("新增员工，员工信息：{}", employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程ID为:{}",id);

        // 设置初始密码 MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置创建时间
//        employee.setCreateTime(LocalDateTime.now());
        // 设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        // 设置创建人
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
        // 设置更新人
//        employee.setUpdateUser(empId);
        // 保存信息
        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name)
    {
        log.info("page = {}, pageSize = {}, name = {}",page,pageSize,name);
        // 构造分页构造器
        Page list = new Page(page, pageSize);
        // 构建条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        // 添加条件
        lqw.like(Strings.isNotEmpty(name),Employee::getName,name);
        // 添加排序
        lqw.orderByDesc(Employee::getUpdateTime);
        employeeService.page(list,lqw);
        return R.success(list);
    }

    /**
     * 根据ID修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee)
    {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程ID为:{}",id);

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 编辑员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id)
    {
        log.info("根据ID查询员工");
        Employee employee = employeeService.getById(id);
        if (employee != null)
        {
            return R.success(employee);
        }
        return R.error("没有查询到信息");
    }



}
