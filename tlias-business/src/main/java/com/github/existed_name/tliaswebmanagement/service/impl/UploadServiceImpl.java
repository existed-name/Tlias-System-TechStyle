package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_util.AliyunOSSOperator;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import com.github.existed_name.tliaswebmanagement.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class UploadServiceImpl implements UploadService {

    // ==================== 常量 ====================

    /**
     * 支持上传的文件后缀名集合（小写）
     */
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(".jpg", ".png", ".jpeg");


    // ==================== 实例变量 ====================

    /**
     * 阿里云 OSS 操作工具类
     */
    private final AliyunOSSOperator aliyunOSSOperator;


    // ==================== 构造器 ====================

    @Autowired
    public UploadServiceImpl(AliyunOSSOperator aliyunOSSOperator) {
        this.aliyunOSSOperator = aliyunOSSOperator;
    }


    // ==================== 公有方法 ====================
    /**
     * 文件上传核心方法
     *
     * @param file 前端上传的 MultipartFile 文件
     * @return 上传成功后文件在阿里云 OSS 中的访问 URL
     * @throws BusinessException 文件校验失败或上传失败时抛出
     */
    @Override
    public String upload(MultipartFile file) {

        // 校验文件 + 获取合法后缀（你原有逻辑，保留）
        String extension = extractAndValidateExtension(file);

        // 在 Service 层统一决定文件路径（核心改动点）
        String objectName = buildPath("avatar", extension);

        try {
            // 上传文件（注意：这里传的是“完整路径”）
            return aliyunOSSOperator.upload(file.getBytes(), objectName);

        } catch ( IOException e) {
            // getBytes() 可能抛 IOException
            throw new BusinessException("文件读取失败");

        } catch (Exception e) {
            // OSS 上传异常统一转业务异常
            throw new BusinessException("文件上传失败，请稍后重试");
        }
    }


    // ==================== 私有方法 ====================

    /**
     * 提取并校验文件后缀名
     *
     * @param file MultipartFile 文件
     * @return 合法的小写后缀名（例如：".jpg"）
     * @throws BusinessException 文件为空、格式错误或类型不支持时
     */
    private String extractAndValidateExtension(MultipartFile file) {
        // 校验文件是否为空
        if ( file == null ){
            throw new BusinessException( "你还没有上传文件哦~~~" );
        }

        if ( file.isEmpty() ) {
            throw new BusinessException( "文件内容不能为空" );
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();

        // 校验文件名是否包含后缀
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("文件格式不正确, 未包含后缀");
        }

        // 提取后缀并转为小写
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // 校验文件类型是否支持
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("文件类型不支持，仅支持 jpg、png、jpeg 格式");
        }

        return extension;
    }


    /**
     * 构建 OSS 文件路径
     *
     * @param bizType   业务类型（例如：avatar、product）
     * @param extension 文件后缀（例如：.jpg）
     * @return 完整路径（例如：avatar/2026/04/uuid.jpg）
     */
    private String buildPath(String bizType, String extension) {

        // 生成日期目录：yyyy/MM（例如：2026/04）
        String datePath = LocalDate.now()
                .format( DateTimeFormatter.ofPattern("yyyy/MM"));

        // 生成唯一文件名（去掉-让文件名更紧凑）
        String fileName = UUID.randomUUID()
                .toString()
                .replace("-", "") + extension;

        // 拼接完整路径：业务 + 日期 + 文件名
        return bizType + "/" + datePath + "/" + fileName;
    }
}