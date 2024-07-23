package org.example.worktest.test;

import com.itextpdf.html2pdf.HtmlConverter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TestHtml2Pdf {
    public static void main(String[] args) throws FileNotFoundException {
        // 包装 HTML 内容以设置段落样式
        String content = "<p>dddd</p>";
        String styledContent = "<html><head><style>p { color: red; }</style></head><body>" + content + "</body></html>";
//        Document document = Jsoup.parse("<p>dddd</p>");
//        String text = document.text();
//        PdfDocument pdf = new PdfDocument(new PdfWriter("output.pdf"));
//        com.itextpdf.layout.Document document1 = new com.itextpdf.layout.Document(pdf);
//        document1.add(new Paragraph(text));
//        document1.close();

        // 生成 PDF
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream outputStream = new FileOutputStream("output.pdf");
        HtmlConverter.convertToPdf(styledContent, outputStream);
//        byte[] pdfBytes = outputStream.toByteArray();

    }
}
