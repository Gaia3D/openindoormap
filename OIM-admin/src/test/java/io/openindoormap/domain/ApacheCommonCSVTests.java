package io.openindoormap.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.domain.extrusionmodel.DesignLayerFileInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.amqp.core.ExchangeTypes.HEADERS;

@Slf4j
public class ApacheCommonCSVTests {

    @Disabled
    void test() throws IOException {
        String FILE_NAME = "D:\\샘플SHP_최종_real\\샘플SHP_최종\\획지 데이터 메타.csv";
//        Reader in = new FileReader(FILE_NAME);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(FILE_NAME), Charset.forName("CP949"));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(HEADERS)
                .withFirstRecordAsHeader()
                .parse(isr);
        for (CSVRecord record : records) {
            System.out.println("record ========================= " + record);
            System.out.println("record1 ========================= " + record.get(0));
            System.out.println("record2 ========================= " + record.get(1));
            System.out.println("record3 ========================= " + record.get(2));
        }
    }

    @Test
    void streamTest () {
        List<DesignLayerFileInfo> list = new ArrayList<>();
        DesignLayerFileInfo file1 = new DesignLayerFileInfo();
        file1.setFileExt("shp");
        file1.setFileRealName("file.shp");
        file1.setFilePath("test/test/");
        DesignLayerFileInfo file2 = new DesignLayerFileInfo();
        file2.setFileExt("shx");
        file2.setFileRealName("file.shx");
        file2.setFilePath("test/test/");
        DesignLayerFileInfo file3 = new DesignLayerFileInfo();
        file3.setFileExt("csv");
        file3.setFileRealName("file.csv");
        file3.setFilePath("test/test/");

        list.add(file1);
        list.add(file2);
        list.add(file3);

        String path = list.stream()
                .map(f-> {
                    if(DesignLayer.AttributeType.findBy(f.getFileExt()) != null) {
                        return f.getFilePath() + f.getFileRealName();
                    } else {
                        return "";
                    }
                })
                .collect(Collectors.joining());


        System.out.println("path ================= " + path);

    }
}
