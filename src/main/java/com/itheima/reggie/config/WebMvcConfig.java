package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport
{
//    @Autowired
//    private LogInterceptor logInterceptor;

    // 设置静态资源映射
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }


    /**
     * 扩展mvc框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        log.info("扩展消息转换器");
        MappingJackson2HttpMessageConverter httpMessageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器 底层使用Jackson将Java对象转换为json
        httpMessageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将上面的消息追加
        converters.add(0,httpMessageConverter);
    }




    // 自己的拦截器
//    @Override
//    protected void addInterceptors(InterceptorRegistry registry)
//    {
//        registry.addInterceptor(logInterceptor).addPathPatterns("/*");
//    }
}
