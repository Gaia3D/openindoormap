package io.openindoormap.config;

import io.openindoormap.domain.OsType;
import io.openindoormap.domain.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Data
@Configuration
@PropertySource("classpath:openindoormap.properties")
@ConfigurationProperties(prefix = "openindoormap")
public class PropertiesConfig {

    /**
     * 로컬 : local, 개발 : develop, 운영 : product
     */
    private String profile;

    /**
     * 로컬 환경은 WINDOW, 운영 환경은 LINUX
     */
    private String osType;

    /**
     * 로컬 환경에서 mock 사용여부
     */
    private boolean mockEnable;
    private boolean scheduleEnable;
    private boolean callRemoteEnable;
    private String serverIp;
    // 운영 서버용. 로그 표시 유무 설정
    private boolean logDisplay;

    private String sensorThingsApiServer;

    // gdal command path
    private String gdalCommandPath;
    private String gdalHost;
    private String gdalPort;

    private String gridDataSourceDir;
    private String contourDataSourceDir;

    private String gridDataTempDir;
    private String contourDataTempDir;

    private String airkoreaApiServiceUrl;
    private String airkoreaAuthKey;

    private String geoserverUrl;
    private String geoserverUser;
    private String geoserverPassword;

    public boolean isLinux() {
        return OsType.valueOf(osType.toUpperCase()) == OsType.LINUX;
    }

    public boolean isProduct() {
        return Profile.valueOf(profile.toUpperCase()) == Profile.PRODUCT;
    }

}
