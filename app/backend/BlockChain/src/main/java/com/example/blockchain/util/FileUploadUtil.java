package com.example.blockchain.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadUtil {

    private static final String PRODUCT_IMAGE_UPLOAD_DIR = "uploads/images/";

    public static String saveProductImage(MultipartFile file, long productId) throws IOException {
        return saveFile(file, productId, PRODUCT_IMAGE_UPLOAD_DIR);
    }

    public static String saveFile(MultipartFile file, long productId, String directory) throws IOException {
        // 获取项目根目录
        String projectDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectDir, directory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);  // 如果目录不存在，则创建目录
        }

        // 使用产品ID生成文件名
        String fileName = productId + ".jpg";  // 将文件命名为产品ID.jpg

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());  // 保存文件到指定路径

        // 返回图片的相对路径
        return "/" + directory + "/" + fileName;
    }
}
