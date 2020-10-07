package io.openindoormap.domain.landscape;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LandScapeDiffParam {
    private Long id;
    private Long landScapeDiffGroupId;
    private String captureCameraState;
    private String landscapeName;
    private MultipartFile image;
}

