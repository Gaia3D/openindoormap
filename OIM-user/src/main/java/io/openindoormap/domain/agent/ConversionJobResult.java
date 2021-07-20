package io.openindoormap.domain.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversionJobResult implements Serializable {

    private static final long serialVersionUID = 9088821716989864642L;

    // 지리참조 여부
    private boolean bGeoReferenced;
    // bGeoReferenced가 false일때, bbox
    private float[] bbox;

    // 시작시각
    private String startTime;
    // 종료시각
    private String endTime;

    // 파일명
    private String fileName;
    // 파일전체 경로
    private String fullPath;

    // 실패 시 메세지
    private String message;
    // 성공여부 (success, failure)
    private ConverterJobResultStatus resultStatus;

    // 위치정보
    private ConverterLocation location;

    // 속성정보
    private String attributes;

    @JsonProperty(value = "bGeoReferenced")
    public boolean getBGeoReferenced() {
        return bGeoReferenced;
    }
    public void setBGeoReferenced(boolean bGeoReferenced) {
        this.bGeoReferenced = bGeoReferenced;
    }

}