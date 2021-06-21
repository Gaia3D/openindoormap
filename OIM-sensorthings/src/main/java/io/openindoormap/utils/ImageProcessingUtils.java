package io.openindoormap.utils;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.GdalContourCommandParams;
import io.openindoormap.domain.GdalGridCommandParams;
import io.openindoormap.domain.GdalProcessJobStatus;
import io.openindoormap.security.Crypt;
import io.openindoormap.support.ProcessBuilderSupport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageProcessingUtils {

    //public static final UploadDirectoryType YEAR_MONTH = UploadDirectoryType.YEAR_MONTH;

    public static List<String> rasterize(PropertiesConfig propertiesConfig, GdalGridCommandParams params) {

        String targetPath = getGridDataTempPath(propertiesConfig);
        log.info("@@ targetPath : {}", targetPath);
        Path commandPath = Paths.get(propertiesConfig.getGdalCommandPath(), GdalGridCommandParams.GDAL_GRID);
        log.info("@@ commandPath : {}", commandPath);

        List<String> commandList = new ArrayList<>();
        commandList.add(commandPath.toString());

        commandList.add("-a_srs");
        commandList.add(GdalGridCommandParams.EPSG_4326);

        commandList.add("-txe");
        commandList.add(GdalGridCommandParams.MINX);
        commandList.add(GdalGridCommandParams.MAXX);
        commandList.add("-tye");
        commandList.add(GdalGridCommandParams.MINY);
        commandList.add(GdalGridCommandParams.MAXY);

        commandList.add("-outsize"); // pixel value
        commandList.add(GdalGridCommandParams.OUT_SIZE);
        commandList.add(GdalGridCommandParams.OUT_SIZE);

        commandList.add("-a"); // algorithm
        commandList.add("invdist");
        //commandList.add(String.format("invdist:nodata=%1$d", -9999));

        commandList.add("-zfield"); // pixel value
        commandList.add(params.getField());

        String queryString = GdalGridCommandParams.SENSORTHINGS_QUERY_FORMAT;
        if (propertiesConfig.isLinux()) {
            queryString = queryString.replaceAll("\\\\", "");
        }
        String sql = String.format(queryString, params.getObservedProperty().getName(), params.getUtcDateTime());

        commandList.add("-sql"); // pixel value
        commandList.add(sql);

        commandList.add(params.getPgInfo());
        commandList.add(targetPath);

        return commandList;

    }

    public static List<String> vectorize(PropertiesConfig propertiesConfig, GdalContourCommandParams params) {

        String sourcePath = getGridDataTempPath(propertiesConfig);
        log.info("@@ sourcePath : {}", sourcePath);
        String targetPath = getContourDataTempPath(propertiesConfig);
        log.info("@@ targetPath : {}", targetPath);

        Path commandPath = Paths.get(propertiesConfig.getGdalCommandPath(), GdalContourCommandParams.GDAL_CONTOUR);
        log.info("@@ commandPath : {}", commandPath);

        List<String> commandList = new ArrayList<>();
        commandList.add(commandPath.toString());

        commandList.add("-a");
        commandList.add(params.getAttributeName());

        commandList.add("-inodata");

        commandList.add("-i");
        commandList.add(String.valueOf(params.getInterval()));

        commandList.add(sourcePath);
        commandList.add(targetPath);

        return commandList;
    }

    public static List<String> spatialIndex(PropertiesConfig propertiesConfig, GdalContourCommandParams params) {

        String filePath = getContourDataTempPath(propertiesConfig);
        log.info("@@ filePath : {}", filePath);

        Path commandPath = Paths.get(propertiesConfig.getGdalCommandPath(), GdalContourCommandParams.OGRINFO);
        log.info("@@ commandPath : {}", commandPath);

        List<String> commandList = new ArrayList<>();
        commandList.add(commandPath.toString());

        commandList.add(filePath);
        commandList.add("-sql");
        commandList.add(GdalContourCommandParams.SPATIAL_INDEX_QUERY);

        return commandList;
    }

    public static void executeRasterizeAndVectorize(PropertiesConfig propertiesConfig, List<String> gridCommand, List<String> contourCommand) {
        GdalProcessJobStatus result;
        try {

            // gdal_grid 실행
            int exitCode = ProcessBuilderSupport.execute(gridCommand);
            if (exitCode == 0) result = GdalProcessJobStatus.SUCCESS;
            else result = GdalProcessJobStatus.FAIL;
            log.info("@@@@@@@ gdal_grid status = {}", result.getValue());
            if (result == GdalProcessJobStatus.FAIL) return;

            // gdal_contour 실행
            exitCode = ProcessBuilderSupport.execute(contourCommand);
            if (exitCode == 0) result = GdalProcessJobStatus.SUCCESS;
            else result = GdalProcessJobStatus.FAIL;
            log.info("@@@@@@@ gdal_contour status = {}", result.getValue());
            if (result == GdalProcessJobStatus.FAIL) return;

            // 공간 인덱스 파일(*.qix) 생성
            GdalContourCommandParams params = new GdalContourCommandParams();
            List<String> spatialIndexCommand = spatialIndex(propertiesConfig, params);
            exitCode = ProcessBuilderSupport.execute(spatialIndexCommand);
            if (exitCode == 0) result = GdalProcessJobStatus.SUCCESS;
            else result = GdalProcessJobStatus.FAIL;
            log.info("@@@@@@@ ogrinfo status = {}", result.getValue());
            if (result == GdalProcessJobStatus.FAIL) return;

            // 파일 복사
            ImageProcessingUtils.copyToDataSource(propertiesConfig);

        } catch (Exception e) {
            result = GdalProcessJobStatus.FAIL;
            log.info("-------- executeRasterizeAndVectorize. message = {}", e.getMessage());
        }
        log.info("@@@@@@@ execite status = {}", result.getValue());
    }

    public static String getPgInfo(PropertiesConfig propertiesConfig, String url, String username, String password) {
        String host = propertiesConfig.getGdalHost();
        String port = propertiesConfig.getGdalPort();
        String dbName = Crypt.decrypt(url);
        dbName = dbName.substring(dbName.lastIndexOf("/") + 1);
        return String.format("PG:" + GdalGridCommandParams.PG_INFO_FORMAT, host, port, dbName, Crypt.decrypt(username), Crypt.decrypt(password));
    }

    public static String getGridDataTempPath(PropertiesConfig propertiesConfig) {
        // 년, 월 디렉토리 생성
        //String desDirectory = FileUtils.makeDirectory("", YEAR_MONTH, propertiesConfig.getGridDataSourceDir());
        //FileUtils.makeDirectory(propertiesConfig.getGridDataSourceDir());
        return Paths.get(propertiesConfig.getGridDataTempDir(), GdalGridCommandParams.OUTPUT_FILE_NAME).toString();
    }

    public static String getContourDataTempPath(PropertiesConfig propertiesConfig) {
        return Paths.get(propertiesConfig.getContourDataTempDir(), GdalContourCommandParams.OUTPUT_FILE_NAME).toString();
    }

    public static void copyToDataSource(PropertiesConfig propertiesConfig) {

        // 미세먼지 격자 복사
        String srcDir = propertiesConfig.getGridDataTempDir();
        String tgtDir = propertiesConfig.getGridDataSourceDir();
        copy(srcDir, tgtDir);
        log.info("@@@@@@@ grid copy... src = {}, tgt = {}", srcDir, tgtDir);

        // 등치선 복사
        srcDir = propertiesConfig.getContourDataTempDir();
        tgtDir = propertiesConfig.getContourDataSourceDir();
        copy(srcDir, tgtDir);
        log.info("@@@@@@@ contour copy... src = {}, tgt = {}", srcDir, tgtDir);

    }

    private static void copy(String srcDir, String tgtDir) {
        try {
            Files.walk(Paths.get(srcDir))
                .filter(src -> !src.toFile().isDirectory())
                .forEach(src -> {
                    Path tgt = Paths.get(tgtDir, src.toString().substring(srcDir.length()));
                    try {
                        Files.copy(src, tgt, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log.info("-------- ImageProcessingUtils copyToDataSource Copy Error. message = {}", e.getMessage());
                    }
                });
        } catch (IOException e) {
            log.info("-------- ImageProcessingUtils copyToDataSource Error = {}", e.getMessage());
        }
    }

}
