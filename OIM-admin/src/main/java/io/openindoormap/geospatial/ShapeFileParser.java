package io.openindoormap.geospatial;

import io.openindoormap.domain.ShapeFileField;
import io.openindoormap.support.LogMessageSupport;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFileType;
import org.geotools.data.shapefile.files.ShpFiles;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

/**
 * Shape file 관련 유틸
 */
@Slf4j
public class ShapeFileParser {

    // shapefile 경로
    private final String filePath;

    public ShapeFileParser(String filePath) {
        this.filePath = filePath;
    }

    /**
     * shape file의 필수 칼럼 검사
     *
     * @return
     */
    public Boolean fieldValidate() {
        DbaseFileReader reader = null;
        boolean fieldValid = false;
        try {
            ShpFiles shpFile = new ShpFiles(filePath);
            // 메타 정보만 수정하는 경우
            if (!shpFile.exists(ShpFileType.SHP)) {
                return true;
            }
            // field만 검사할 것이기 때문에 따로 인코딩은 설정하지 않음 
            reader = new DbaseFileReader(shpFile, false, Charset.defaultCharset());
            DbaseFileHeader header = reader.getHeader();
            int filedValidCount = 0;
            // 필드 카운트
            for (int iField = 0; iField < header.getNumFields(); iField++) {
                String fieldName = header.getFieldName(iField);
                if (ShapeFileField.findBy(fieldName) != null) filedValidCount++;
            }
            // 필수 칼럼이 모두 있는지 확인한 결과 리턴 
            fieldValid = filedValidCount == ShapeFileField.values().length;

            reader.close();
        } catch (MalformedURLException e) {
            LogMessageSupport.printMessage(e, "MalformedURLException ============ {}", e.getMessage());
        } catch (IOException e) {
            LogMessageSupport.printMessage(e, "IOException ============== {} ", e.getMessage());
        } finally {
            if(reader != null) { try { reader.close(); } catch(Exception e) {} }
        }

        return fieldValid;
    }
}
