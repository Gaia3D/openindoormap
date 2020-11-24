package io.openindoormap.utils;

import io.openindoormap.OIMAdminApplication;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;

@Slf4j
@SpringBootTest(classes = OIMAdminApplication.class)
public class CellSpaceListFilterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void filteredCellSpaceRoom() {

        JSONParser parser = new JSONParser();

        JSONObject cellJson = null;
        FileWriter writer = null;
        try {
            cellJson = (JSONObject) parser.parse(new FileReader(
                    this.getClass().getClassLoader().getResource("admin_20201013064147_346094873669678_cellspacelist.json").getFile()));
            JSONObject newCellSpaceList = new JSONObject();
            log.info("size = {}", cellJson.size());
            Iterator iter = cellJson.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                if (key.contains("ROOM")) {
                    newCellSpaceList.put(key, cellJson.get(key));
                }
            }
            log.info("size = {}", newCellSpaceList.size());

            String fileName = "src/test/resources/admin_20201013064147_346094873669678_filtered_cellspacelist.json";
            writer = new FileWriter(fileName);
            writer.write(newCellSpaceList.toJSONString());
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }





    }


}
