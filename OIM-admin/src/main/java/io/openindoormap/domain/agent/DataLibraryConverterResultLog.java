package io.openindoormap.domain.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openindoormap.domain.converter.ConverterJob;
import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryConverterJob;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * TODO extrusion 으로 합쳐야 함
 */
@ToString(callSuper = true)
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLibraryConverterResultLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private DataLibraryConverterJob dataLibraryConverterJob;

    // ConverterJob 변환결과
    private List<DataLibraryConversionJobResult> conversionJobResult;

    // 시작시각
    private String startTime;
    // 종료시각
    private String endTime;

    // 실패로그
    private String failureLog;
    // 성공여부
    private boolean isSuccess;

    // 변환된 파일 갯수
    private int numberOfFilesConverted;
    // 변환 되어야 할 파일 갯수
    private int numberOfFilesToBeConverted;

    @JsonProperty(value="isSuccess")
    public boolean getIsSuccess() {
        return isSuccess;
    }
    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

}