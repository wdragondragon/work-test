package org.example.worktest.controller;


import com.itextpdf.html2pdf.HtmlConverter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.worktest.entity.User;
import org.example.worktest.test.FreeMarketTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api("测试")
@RestController
public class TestController {

    @GetMapping
    @ApiOperation("测试")
    public String test() {
        return "test";
    }

    private static final String HTML_FILE_PATH = "saved_content.html";
    private static final String PDF_FILE_PATH = "generated_document.pdf";

    @ApiOperation(value = "根据html生成pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    @PostMapping(value = "/saveContent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> saveContent(@RequestBody Map<String, String> request) throws IOException {
        String content = request.get("content");

        // Base64 解密
        byte[] decodedBytes = Base64.getDecoder().decode(content);
        content = new String(decodedBytes);


        // 模拟从数据库获取的替换数据
        Map<String, String> replacementData = new HashMap<>();
        replacementData.put("count", "1");

        for (Map.Entry<String, String> entry : replacementData.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            content = content.replace(placeholder, entry.getValue());
        }

        // 包装 HTML 内容以设置段落样式
        String styledContent = "<html><head><style>p { color: red; }</style></head><body>" + content + "</body></html>";

        // 将 HTML 内容保存为文件
        Path htmlFilePath = Paths.get(HTML_FILE_PATH);
        Files.write(htmlFilePath, styledContent.getBytes());

        // 生成 PDF 文件
        HtmlConverter.convertToPdf(Files.newInputStream(Paths.get(HTML_FILE_PATH)), Files.newOutputStream(Paths.get(PDF_FILE_PATH)));

        // 将 PDF 文件返回给前端
        Path pdfPath = Paths.get(PDF_FILE_PATH);
        byte[] pdfBytes = Files.readAllBytes(pdfPath);

        // 生成 PDF
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        HtmlConverter.convertToPdf(content, outputStream);
//        byte[] pdfBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
                .contentType(MediaType.valueOf("application/octet-stream;charset=UTF-8"))
                .body(pdfBytes);
    }

    @ApiOperation("freemarket测试")
    @GetMapping("/testFreemarker")
    public String testFreemarker() throws FileNotFoundException {
        FreeMarketTest.test();
        return "成功";
    }


    @ApiOperation("base64测试")
    @GetMapping("/base64")
    public User base64() {
        return new User("123", "2443");
    }

    @ApiOperation("base64测试")
    @PostMapping("/base64")
    public User base64(@RequestBody User user) {
        log.info("User: {}", user);
        return user;
    }
}
