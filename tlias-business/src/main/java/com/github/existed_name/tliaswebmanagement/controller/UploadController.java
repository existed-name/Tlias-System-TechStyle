package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping( "/upload" )
public class UploadController {
    private final UploadService uploadService;

    @Autowired
    public UploadController( UploadService uploadService ){
        this.uploadService = uploadService;
    }

    /**
     * 上传文件(员工头像)
     * <br>
     * 注意: ApiFox测试时, 选择Body → form-data → 参数名为file
     */
    @LogOperation
    @PostMapping
    public Result<String> upload( @RequestParam( "file" ) MultipartFile file) {
        log.info("上传文件: {}", file );
        String url = uploadService.upload(file);
        return Result.success(url);
    }
}