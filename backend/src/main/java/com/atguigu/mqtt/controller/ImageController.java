package com.atguigu.mqtt.controller;
import com.atguigu.mqtt.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/image")
public class ImageController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ImageService imageService;

    @GetMapping("/")
    public String index() {
        return "Server is running!";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: No file uploaded.";
        }

        try {
            // 1. 获取原始文件名和内容类型（如 image/jpeg）
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType(); // 如 image/png, image/jpeg

            // 2. 读取文件字节数组
            byte[] fileBytes = file.getBytes();

            // 3. 转为 Base64 编码字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // 4. 可选：加上 Data URL 前缀（前端可以直接用在 <img src>）
            String dataUrl = "data:" + contentType + ";base64," + base64String;

            // 输出或返回
            System.out.println("✅ Base64 编码成功，长度: " + base64String.length());
            messagingTemplate.convertAndSend("/topic/camera/1", base64String);
            // return dataUrl;  // 返回 Data URL（推荐）
            return "{\"status\":\"success\",\"dataUrl\":\"" + dataUrl + "\"}";


        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read file.";
        }
    }
    @PostMapping("/upload1")
    public String upload1(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: No file uploaded.";
        }

        try {
            // 1. 获取原始文件名和内容类型（如 image/jpeg）
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType(); // 如 image/png, image/jpeg

            // 2. 读取文件字节数组
            byte[] fileBytes = file.getBytes();

            // 3. 转为 Base64 编码字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // 4. 可选：加上 Data URL 前缀（前端可以直接用在 <img src>）
            String dataUrl = "data:" + contentType + ";base64," + base64String;

            // 输出或返回
            System.out.println("✅ Base64 编码成功，长度: " + base64String.length());
            messagingTemplate.convertAndSend("/topic/camera/1", base64String);
            // return dataUrl;  // 返回 Data URL（推荐）
            return "{\"status\":\"success\",\"dataUrl\":\"" + dataUrl + "\"}";


        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read file.";
        }
    }
    @PostMapping("/upload2")
    public String upload2(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: No file uploaded.";
        }

        try {
            // 1. 获取原始文件名和内容类型（如 image/jpeg）
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType(); // 如 image/png, image/jpeg

            // 2. 读取文件字节数组
            byte[] fileBytes = file.getBytes();

            // 3. 转为 Base64 编码字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // 4. 可选：加上 Data URL 前缀（前端可以直接用在 <img src>）
            String dataUrl = "data:" + contentType + ";base64," + base64String;

            // 输出或返回
            System.out.println("✅ Base64 编码成功，长度: " + base64String.length());
            messagingTemplate.convertAndSend("/topic/camera/2", dataUrl);
            // return dataUrl;  // 返回 Data URL（推荐）
            return "{\"status\":\"success\",\"dataUrl\":\"" + dataUrl + "\"}";


        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read file.";
        }
    }
    @PostMapping("/upload3")
    public String upload3(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: No file uploaded.";
        }

        try {
            // 1. 获取原始文件名和内容类型（如 image/jpeg）
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType(); // 如 image/png, image/jpeg

            // 2. 读取文件字节数组
            byte[] fileBytes = file.getBytes();

            // 3. 转为 Base64 编码字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // 4. 可选：加上 Data URL 前缀（前端可以直接用在 <img src>）
            String dataUrl = "data:" + contentType + ";base64," + base64String;

            // 输出或返回
            System.out.println("✅ Base64 编码成功，长度: " + base64String.length());
            messagingTemplate.convertAndSend("/topic/camera/3", dataUrl);
            // return dataUrl;  // 返回 Data URL（推荐）
            return "{\"status\":\"success\",\"dataUrl\":\"" + dataUrl + "\"}";


        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read file.";
        }
    }
    @PostMapping("/load")
    public ResponseEntity<String> load(@RequestBody Map<String, Object> request) throws  IOException {
            // 获取请求参数
            String base64image = (String) request.get("image");
            Integer room = (Integer) request.get("room");
            Integer person = (Integer) request.get("person");

            // 生成时间戳格式文件名（精确到毫秒）
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String fileName = String.format("camera_%s_%d.png", timestamp, room);
            Path filePath = Paths.get("D:/image/camera/", fileName);

            // 确保目录存在（自动创建父目录）
            Files.createDirectories(filePath.getParent());

            // 解码并保存图片
            byte[] imageBytes = Base64.getDecoder().decode(base64image);
            Files.write(filePath, imageBytes, StandardOpenOption.CREATE);

            // 存储路径到数据库（需实现imageService）
            imageService.addImage(room, filePath.toString(),person);

            return ResponseEntity.ok("图片保存成功！路径：" + filePath);

    }

}