package com.example.demotddapp.service;

import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileService {

    AppConfiguration appConfiguration;
    Tika tika;

    public FileService(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
        tika = new Tika();
    }

    public String saveProfileImage(String base64Image) throws IOException {
        String imageName = UUID.randomUUID().toString().replaceAll("-", "");

        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
        FileUtils.writeByteArrayToFile(target, decodedBytes);
        return imageName;
    }

    public void deleteProfileImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String detectType(byte[] imageArr) {
        return tika.detect(imageArr);
    }
}
