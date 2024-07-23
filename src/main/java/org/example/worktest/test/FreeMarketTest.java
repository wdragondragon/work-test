package org.example.worktest.test;

import com.itextpdf.html2pdf.HtmlConverter;
import org.example.worktest.utils.FreemarkerUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FreeMarketTest {
    public static void main(String[] args) throws FileNotFoundException {
        test();
    }

    public static void test() throws FileNotFoundException {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "User Information Table");
        data.put("tableHeaders", Arrays.asList("ID", "First Name", "Last Name", "Username"));
        data.put("tableRows", Arrays.asList(
                Arrays.asList(1, "John", "Doe", "@johndoe"),
                Arrays.asList(2, "Jane", "Smith", "@janesmith"),
                Arrays.asList(3, "Sam", "Brown", "@sambrown"),
                Arrays.asList(4, "Lisa", "Johnson", "@lisajohnson")
        ));


        String s = FreemarkerUtil.printString("", "table.ftl", data);
        FileOutputStream outputStream = new FileOutputStream("table.pdf");
        HtmlConverter.convertToPdf(s, outputStream);
    }
}
