package io.openindoormap.utils.airquality;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class IndexCalculatorTests {

    IndexCalculator calculatorForAQI;
    IndexCalculator calculatorForCAI;
    
    @Before
    public void setup() {
        Concentrations concentrationsForAQI = ConcentrationsGenerator.createAQIConcentrations();
        Concentrations concentrationsForCAI = ConcentrationsGenerator.createCAIConcentrations();

        calculatorForAQI = new IndexCalculator(concentrationsForAQI);
        calculatorForCAI = new IndexCalculator(concentrationsForCAI);
    }

    @Test
    @DisplayName("입력된 PM2.5의 농도값이 해당 농도 구간을 벗어난 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_pm25_001() {
        AirQuality airQuality = AirQuality.PM25;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM2.5의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_pm25_003() {
        AirQuality airQuality= AirQuality.PM25;
        
        double concentration = 15.0;
        int expected = 57;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 35.0;
        expected = 99;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 75.0;
        expected = 161;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM2.5의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_pm25_101() {
        AirQuality airQuality = AirQuality.PM25;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM2.5의 농도값이 해당 농도 구간 값이면 CAI 인덱스값을 계산한다")
    public void test_pm25_102() {
        AirQuality airQuality= AirQuality.PM25;
        
        double concentration = 15.0;
        int expected = 50;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 35.0;
        expected = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 75.0;
        expected = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간을 벗어난 작은 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_pm10_001() {
        AirQuality airQuality = AirQuality.PM10;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_pm10_002() {
        AirQuality airQuality = AirQuality.PM10;

        double concentration = 23;
        int expected = 21;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 30;
        expected = 28;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        
        concentration = 80;
        expected = 63;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);

        concentration = 150;
        expected = 98;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간을 벗어난 큰 값이면 AQI 마지막 인덱스값을 반환한다.")
    public void test_pm10_003() {
        AirQuality airQuality = AirQuality.PM10;
        double value = 800;
        int expected = 500;
        int result = this.calculatorForAQI.getAQI(airQuality, value);
        int grade = this.calculatorForAQI.getGrade(airQuality, value);
        assertEquals(expected, result);
        assertEquals(7, grade);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_pm10_101() {
        AirQuality airQuality = AirQuality.PM10;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간 값이면 CAI 인덱스값을 계산한다")
    public void test_pm10_102() {
        AirQuality airQuality = AirQuality.PM10;

        double concentration = 23;
        int expected = 38;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 30;
        expected = 50;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);

        concentration = 80;
        expected = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
        
        concentration = 150;
        expected = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 PM10의 농도값이 해당 농도 구간을 벗어난 큰 값이면 AQI 마지막 인덱스값을 반환한다.")
    public void test_pm10_103() {
        AirQuality airQuality = AirQuality.PM10;
        double value = 800;
        int expected = 500;
        int result = this.calculatorForCAI.getAQI(airQuality, value);
        int grade = this.calculatorForCAI.getGrade(airQuality, value);
        assertEquals(expected, result);
        assertEquals(4, grade);
    }

    @Test
    @DisplayName("입력된 O3의 농도값이 해당 농도 구간을 벗어난 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_o3_001() {
        AirQuality airQuality = AirQuality.O3;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 O3의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_o3_002() {
        AirQuality airQuality = AirQuality.O3;
        double scaleFactor = 1000.0;

        double concentration = 0.03;
        int expected = 28;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
        
        concentration = 0.09;
        expected = 161;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
        
        concentration = 0.15;
        expected = 247;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 O3의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_o3_101() {
        AirQuality airQuality = AirQuality.O3;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 O3의 농도값이 해당 농도 구간 값이면 CAI 인덱스값을 계산한다")
    public void test_o3_102() {
        AirQuality airQuality = AirQuality.O3;
        double scaleFactor = 1.0;

        double concentration = 0.03;
        int expected = 50;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
        
        concentration = 0.09;
        expected = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
        
        concentration = 0.15;
        expected = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 CO의 농도값이 해당 농도 구간을 벗어난 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_co_001() {
        AirQuality airQuality = AirQuality.CO;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 CO의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_co_002() {
        AirQuality airQuality = AirQuality.CO;

        double concentration = 2.0;
        int expectedAQI = 23;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);
        
        concentration = 9.0;
        expectedAQI = 96;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);

        concentration = 15.0;
        expectedAQI = 193;
        result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);
    }

    @Test
    @DisplayName("입력된 CO의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_co_101() {
        AirQuality airQuality = AirQuality.CO;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 CO의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_co_102() {
        AirQuality airQuality = AirQuality.CO;

        double concentration = 2.0;
        int expectedAQI = 50;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);
        
        concentration = 9.0;
        expectedAQI = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);

        concentration = 15.0;
        expectedAQI = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expectedAQI, result);
    }

    @Test
    @DisplayName("입력된 NO2의 농도값이 해당 농도 구간을 벗어난 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_no2_001() {
        AirQuality airQuality = AirQuality.NO2;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 NO2의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_no2_002() {
        AirQuality airQuality = AirQuality.NO2;
        double scaleFactor = 1000.0;

        double concentration = 0.03;
        int expectedAQI = 28;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
        
        concentration = 0.06;
        expectedAQI = 57;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);

        concentration = 0.20;
        expectedAQI = 120;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
    }

    @Test
    @DisplayName("입력된 NO2의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_no2_101() {
        AirQuality airQuality = AirQuality.NO2;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 NO2의 농도값이 해당 농도 구간 값이면 CAI 인덱스값을 계산한다")
    public void test_no2_102() {
        AirQuality airQuality = AirQuality.NO2;
        double scaleFactor = 1.0;

        double concentration = 0.03;
        int expectedAQI = 50;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
        
        concentration = 0.06;
        expectedAQI = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);

        concentration = 0.20;
        expectedAQI = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
    }

    @Test
    @DisplayName("입력된 SO2의 농도값이 해당 농도 구간을 벗어난 값이면 AQI 인덱스값을 계산하지 않는다")
    public void test_so2_001() {
        AirQuality airQuality = AirQuality.SO2;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 SO2의 농도값이 해당 농도 구간 값이면 AQI 인덱스값을 계산한다")
    public void test_so2_002() {
        AirQuality airQuality = AirQuality.SO2;
        double scaleFactor = 1000.0;

        double concentration = 0.02;
        int expectedAQI = 29;
        int result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
        
        concentration = 0.05;
        expectedAQI = 69;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);

        concentration = 0.15;
        expectedAQI = 134;
        result = this.calculatorForAQI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
    }

    @Test
    @DisplayName("입력된 SO2의 농도값이 해당 농도 구간을 벗어난 값이면 CAI 인덱스값을 계산하지 않는다")
    public void test_so2_101() {
        AirQuality airQuality = AirQuality.SO2;

        double concentration = -1.0;
        int expected = IndexCalculator.INDEX_MISSING;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("입력된 SO2의 농도값이 해당 농도 구간 값이면 CAI 인덱스값을 계산한다")
    public void test_so2_102() {
        AirQuality airQuality = AirQuality.SO2;
        double scaleFactor = 1.0;

        double concentration = 0.02;
        int expectedAQI = 50;
        int result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
        
        concentration = 0.05;
        expectedAQI = 100;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);

        concentration = 0.15;
        expectedAQI = 250;
        result = this.calculatorForCAI.getAQI(airQuality, concentration * scaleFactor);
        assertEquals(expectedAQI, result);
    }
}
