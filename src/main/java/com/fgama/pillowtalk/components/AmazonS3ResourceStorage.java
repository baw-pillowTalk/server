package com.fgama.pillowtalk.components;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmazonS3ResourceStorage {
    public static final String S3URL = "https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/";
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void store(String fullPath, MultipartFile multipartFile) {
        File file = new File(MultipartUtil.getLocalHomeDirectory(), fullPath);
        try {
            multipartFile.transferTo(file);
            log.info(bucket);
            log.info(fullPath);
            log.info(file.toString());
            amazonS3Client.putObject(new PutObjectRequest(bucket, fullPath, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public String getUrl(String fullPath) {
        try {
            return amazonS3Client.getUrl(bucket, fullPath).toString();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public String getID(String FileName) {
        InitiateMultipartUploadResult initiateUpload =
                amazonS3Client.initiateMultipartUpload(
                        new InitiateMultipartUploadRequest(bucket, FileName)); // KEY: file name including directory
        log.info("upload id : {}", initiateUpload.getUploadId());
        return initiateUpload.getUploadId();
    }
}