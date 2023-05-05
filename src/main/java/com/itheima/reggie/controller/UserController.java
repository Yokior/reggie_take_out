package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.EmailUtil;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController
{
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session)
    {
        // 获取邮箱号
        String phone = user.getPhone();

        if (Strings.isNotEmpty(phone))
        {
            // 生成随机4位验证码
            String checkCode = ValidateCodeUtils.generateValidateCode(4).toString();

            // 调用工具类发送邮件
            EmailUtil.sendAuthCodeEmail(phone,checkCode);
            log.info("邮箱验证码:{}",checkCode);

            // 将生成的验证码保存在session
//            session.setAttribute(phone,checkCode);

            // 将验证码保存到redis 5分钟有效
            redisTemplate.opsForValue().set(phone,checkCode,5, TimeUnit.MINUTES);

            return R.success("邮件验证码发送成功");
        }

        return R.error("邮箱发送失败");
    }

    /**
     * 移动端登录
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session)
    {
        // 获取邮箱号
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);

        // 从redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);


        // 比对成功 登录成功
        if (codeInSession != null && codeInSession.equals(code))
        {
            // 判断是不是新用户 如果是那就注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);

            User user = userService.getOne(lqw);
            // 新用户
            if (user == null)
            {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            // 在session中保存登录信息
            session.setAttribute("user",user.getId());

            // 如果用户登录成功 删除redis中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }
}
