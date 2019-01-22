package com.mtons.mblog.base.upload.impl;

import com.aliyun.oss.OSSClient;
import com.mtons.mblog.base.context.AppContext;
import com.mtons.mblog.base.lang.MtonsException;
import com.mtons.mblog.base.upload.FileRepo;
import com.mtons.mblog.base.utils.FileKit;
import com.upyun.UpYunUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * created by langhsu
 * on 2019/1/20
 *
 * @since 3.0
 */
@Slf4j
@Component
public class AliyunFileRepoImpl extends AbstractFileRepo implements FileRepo {
    @Autowired
    private AppContext appContext;

    @Override
    public String writeToStore(byte[] bytes, String pathAndFileName) throws Exception {
        String endpoint = appContext.getConfig().get("aliyun_oss_endpoint");
        String bucket = appContext.getConfig().get("aliyun_oss_bucket");
        String src = appContext.getConfig().get("aliyun_oss_src");

        if (StringUtils.isAnyBlank(endpoint, bucket)) {
            throw new MtonsException("请先在后台设置阿里云配置信息");
        }

        if (StringUtils.isNoneBlank(src) && src.length() > 1) {
            if (src.startsWith("/")) {
                src = src.substring(1);
            }

            if (!src.endsWith("/")) {
                src = src + "/";
            }
        }

        String key = UpYunUtils.md5(bytes);
        String path = src + key + FileKit.getSuffix(pathAndFileName);
        OSSClient client = builder();
        client.putObject(bucket, path, new ByteArrayInputStream(bytes));
        return "//" + bucket.trim() + "." + endpoint.trim() + "/" + path;
    }

    @Override
    public void deleteFile(String storePath) {
        String bucket = appContext.getConfig().get("aliyun_oss_bucket");
        String endpoint = appContext.getConfig().get("aliyun_oss_endpoint");
        String path = StringUtils.remove(storePath, "//" + bucket.trim() + "." + endpoint.trim() + "/");
        OSSClient client = builder();
        try {
            client.doesObjectExist(bucket, path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private OSSClient builder() {
        String endpoint = appContext.getConfig().get("aliyun_oss_endpoint");
        String accessKeyId = appContext.getConfig().get("aliyun_oss_key");
        String accessKeySecret = appContext.getConfig().get("aliyun_oss_secret");

        if (StringUtils.isAnyBlank(endpoint, accessKeyId, accessKeySecret)) {
            throw new MtonsException("请先在后台设置阿里云配置信息");
        }
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
}
