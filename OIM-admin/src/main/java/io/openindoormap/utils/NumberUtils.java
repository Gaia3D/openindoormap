package io.openindoormap.utils;

/**
 * 숫자 관련 유틸
 */
public class NumberUtils {

    /**
     * 입력받은 숫자를 자리수만큼 반올림한 결과를 리턴
     * @param cutSize 자리수
     * @param num 입력값
     * @return double
     */
    public static double round(int cutSize, double num) {
        int multiply = (int) Math.pow(10, cutSize);
        double division = Math.pow(10.0, cutSize);
        return Math.round(num * multiply) / division;
    }
}
