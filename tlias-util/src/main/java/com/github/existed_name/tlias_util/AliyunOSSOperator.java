package com.github.existed_name.tlias_util;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Slf4j
@Component
public class AliyunOSSOperator {

    private final AliyunOSSProperties aliyunOSSProperties;

    @Autowired
    public AliyunOSSOperator( AliyunOSSProperties aliyunOSSProperties ){
        this.aliyunOSSProperties = aliyunOSSProperties;
    }

    /**
     * 上传文件到阿里云 OSS
     *
     * @param content     文件字节数组
     * @param objectName  OSS中的完整路径（例如：avatar/2026/04/xxx.jpg）
     * @return 文件访问URL
     */
    public String upload(byte[] content, String objectName) {

        // ===== 获取配置 =====
        String endpoint = aliyunOSSProperties.getEndpoint();     // 例如：https://oss-cn-hangzhou.aliyuncs.com
        String bucketName = aliyunOSSProperties.getBucketName(); // Bucket名称
        String region = aliyunOSSProperties.getRegion();         // 区域（V4签名需要）

        OSS ossClient = null;

        try {
            // ===== 创建凭证提供者（从环境变量读取 AK / SK）=====
            // 需要系统环境变量中存在：
            // OSS_ACCESS_KEY_ID
            // OSS_ACCESS_KEY_SECRET
            EnvironmentVariableCredentialsProvider credentialsProvider =
                    CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

            // ===== 配置客户端（指定使用 V4 签名）=====
            ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
            conf.setSignatureVersion(SignVersion.V4);

            // ===== 创建 OSS 客户端 =====
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(conf)
                    .region(region)
                    .build();

            // ===== 执行上传 =====
            // objectName 就是你在 Service 里设计好的完整路径
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));

        } catch (Exception e) {
            // 记录详细日志，方便排查（包含路径）
            log.error("OSS上传失败, objectName={}", objectName, e);

            // 向上抛运行时异常，让 Service 统一转换为业务异常
            throw new RuntimeException("OSS文件上传失败", e);

        } finally {
            // ===== 释放资源 =====
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // ===== 拼接访问URL =====
        // endpoint: https://oss-cn-hangzhou.aliyuncs.com
        // 拼接后: https://bucket.oss-cn-hangzhou.aliyuncs.com/xxx.jpg
        return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
    }
}
