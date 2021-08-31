package io.openindoormap.domain.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsrYear {

    private String year;
    private int month[] = new int[13];

}
