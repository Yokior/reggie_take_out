package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController
{

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)
    {
        // file是一个临时文件 需要转存
        log.info(file.toString());
        // 转存文件
        // 使用UUID防止文件名重复
        String fileName = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        // 获取后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        fileName += suffix;
        // 判断目录是否存在 不存在就创建
        File dir = new File(basePath);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        try
        {
            file.transferTo(new File(basePath + fileName));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)
    {
        BufferedInputStream bis = null;
        ServletOutputStream outputStream = null;
        try
        {
            // 输入流 读取文件内容
            bis = new BufferedInputStream(new FileInputStream(new File(basePath + name)));

            // 输出流
            outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = bis.read(bytes)) != -1)
            {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bis.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

}
