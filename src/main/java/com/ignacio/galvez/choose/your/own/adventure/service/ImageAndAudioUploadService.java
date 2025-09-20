package com.ignacio.galvez.choose.your.own.adventure.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageAndAudioUploadService {




    String uploadImage(MultipartFile multipartFile, String title) throws IOException;

    String uploadAudio(MultipartFile multipartFile, String title) throws IOException;
}
