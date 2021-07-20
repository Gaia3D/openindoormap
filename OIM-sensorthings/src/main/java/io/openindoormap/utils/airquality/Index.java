package io.openindoormap.utils.airquality;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Index {
    private int min;
    private int max;
    private int grade;
}
