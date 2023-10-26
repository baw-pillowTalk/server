package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.AmazonS3ResourceStorage;
import com.fgama.pillowtalk.components.FileDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;

    public FileDetail save(MultipartFile multipartFile) {

        FileDetail fileDetail = FileDetail.multipartOf(multipartFile);
        amazonS3ResourceStorage.store(fileDetail.getPath(), multipartFile);
        fileDetail.setUrl(amazonS3ResourceStorage.getUrl(fileDetail.getPath()));

        return fileDetail;
    }
}
