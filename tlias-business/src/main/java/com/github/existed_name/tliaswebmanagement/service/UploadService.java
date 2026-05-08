package com.github.existed_name.tliaswebmanagement.service;

import org.springframework.web.multipart.MultipartFile;


public interface UploadService {
    /**
     * 上传文件(员工头像)
     *
     * @return 阿里云OSS的URL
     */
    String upload( MultipartFile file );
}
