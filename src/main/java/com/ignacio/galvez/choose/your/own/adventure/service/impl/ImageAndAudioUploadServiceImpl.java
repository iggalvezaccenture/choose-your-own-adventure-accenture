package com.ignacio.galvez.choose.your.own.adventure.service.impl;

import com.ignacio.galvez.choose.your.own.adventure.service.ImageAndAudioUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageAndAudioUploadServiceImpl implements ImageAndAudioUploadService {


    @Value("classpath:image/")
    private Resource imageResource;


    @Value("classpath:audio/")
    private Resource audioResource;


    @Override
    public String uploadImage(MultipartFile multipartFile, String title) throws IOException {

        var uploadDir = Paths.get(imageResource.getFile().getPath());

        var path = uploadDir.resolve(Objects.requireNonNull(title));
            Files.write(path, multipartFile.getBytes(), StandardOpenOption.CREATE);

        return "Ok";
    }

    @Override
    public String uploadAudio(MultipartFile multipartFile, String title) throws IOException {

        var uploadDir = Paths.get(audioResource.getFile().getPath());

        var path = uploadDir.resolve(Objects.requireNonNull(title));
            Files.write(path, multipartFile.getBytes(), StandardOpenOption.CREATE);
        return "Ok";
    }
}
