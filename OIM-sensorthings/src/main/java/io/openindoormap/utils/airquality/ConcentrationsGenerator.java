package io.openindoormap.utils.airquality;

public class ConcentrationsGenerator {
    public static Concentrations createAQIConcentrations() {
        Concentrations concentrations = new Concentrations();

        Index index1 = new Index(0, 50, 1);    // good
        Index index2 = new Index(51, 100, 2);  // moderate
        Index index3 = new Index(101, 150,3); // unhealthy for sensitive groups
        Index index4 = new Index(151, 200, 4); // unhealthy
        Index index5 = new Index(201, 300, 5); // very unhealthy
        Index index6 = new Index(301, 400, 6); // hazardous
        Index index7 = new Index(401, 500, 7); // hazardous

        Concentration pm25 = new Concentration(AirQuality.PM25);    // 24Hour(μg/m3)
        pm25.addIndexStep(new IndexStep(index1,   0.0,  12.0));
        pm25.addIndexStep(new IndexStep(index2,  12.1,  35.4));
        pm25.addIndexStep(new IndexStep(index3,  35.5,  55.4));
        pm25.addIndexStep(new IndexStep(index4,  55.5, 150.4));
        pm25.addIndexStep(new IndexStep(index5, 150.5, 250.4));
        pm25.addIndexStep(new IndexStep(index6, 250.5, 350.4));
        pm25.addIndexStep(new IndexStep(index7, 350.5, 500.4));

        Concentration pm10 = new Concentration(AirQuality.PM10);    // 24Hour(μg/m3)
        pm10.addIndexStep(new IndexStep(index1,   0.0,  54.0));
        pm10.addIndexStep(new IndexStep(index2,  55.0, 154.0));
        pm10.addIndexStep(new IndexStep(index3, 155.0, 254.0));
        pm10.addIndexStep(new IndexStep(index4, 255.0, 354.0));
        pm10.addIndexStep(new IndexStep(index5, 355.0, 424.0));
        pm10.addIndexStep(new IndexStep(index6, 425.0, 504.0));
        pm10.addIndexStep(new IndexStep(index7, 505.0, 604.0));

        // Concentration o3 = new Concentration(AirQuality.O3);    // 1Hour(ppb)
        // o3.addIndexStep(new IndexStep(index3, 125.0, 164.0));
        // o3.addIndexStep(new IndexStep(index4, 165.0, 204.0));
        // o3.addIndexStep(new IndexStep(index5, 205.0, 404.0));
        // o3.addIndexStep(new IndexStep(index6, 405.0, 504.0));
        // o3.addIndexStep(new IndexStep(index7, 505.0, 604.0));

        Concentration o3 = new Concentration(AirQuality.O3);    // 8Hour(ppb)
        o3.addIndexStep(new IndexStep(index1,   0.0,  54.0));
        o3.addIndexStep(new IndexStep(index2,  55.0,  70.0));
        o3.addIndexStep(new IndexStep(index3,  71.0,  85.0));
        o3.addIndexStep(new IndexStep(index4,  86.0, 105.0));
        o3.addIndexStep(new IndexStep(index5, 106.0, 200.0));

        Concentration co = new Concentration(AirQuality.CO);    // 8Hour(ppm)
        co.addIndexStep(new IndexStep(index1,  0.0,  4.4));
        co.addIndexStep(new IndexStep(index2,  4.5,  9.4));
        co.addIndexStep(new IndexStep(index3,  9.5, 12.4));
        co.addIndexStep(new IndexStep(index4, 12.5, 15.4));
        co.addIndexStep(new IndexStep(index5, 15.5, 30.4));
        co.addIndexStep(new IndexStep(index6, 30.5, 40.4));
        co.addIndexStep(new IndexStep(index7, 40.5, 50.4));

        Concentration so2 = new Concentration(AirQuality.SO2);  // 1Hour(ppb)
        so2.addIndexStep(new IndexStep(index1,   0.0,   35.0));
        so2.addIndexStep(new IndexStep(index2,  36.0,   75.0));
        so2.addIndexStep(new IndexStep(index3,  76.0,  185.0));
        so2.addIndexStep(new IndexStep(index4, 186.0,  304.0));
        so2.addIndexStep(new IndexStep(index5, 305.0,  604.0));
        so2.addIndexStep(new IndexStep(index6, 605.0,  804.0));
        so2.addIndexStep(new IndexStep(index7, 805.0, 1004.0));

        Concentration no2 = new Concentration(AirQuality.NO2);  // 1Hour(ppb)
        no2.addIndexStep(new IndexStep(index1,    0.0,   53.0));
        no2.addIndexStep(new IndexStep(index2,   54.0,  100.0));
        no2.addIndexStep(new IndexStep(index3,  101.0,  360.0));
        no2.addIndexStep(new IndexStep(index4,  361.0,  649.0));
        no2.addIndexStep(new IndexStep(index5,  650.0, 1249.0));
        no2.addIndexStep(new IndexStep(index6, 1250.0, 1649.0));
        no2.addIndexStep(new IndexStep(index7, 1650.0, 2049.0));

        concentrations.addConcentration(pm25);
        concentrations.addConcentration(pm10);
        concentrations.addConcentration(o3);
        concentrations.addConcentration(co);
        concentrations.addConcentration(so2);
        concentrations.addConcentration(no2);

        return concentrations;
    }

    public static Concentrations createCAIConcentrations() {
        Concentrations concentrations = new Concentrations();

        Index index1 = new Index(0, 50, 1);    // 좋음
        Index index2 = new Index(51, 100, 2);  // 보통
        Index index3 = new Index(101, 250, 3); // 나쁨
        Index index4 = new Index(251, 500, 4); // 매우나쁨

        Concentration pm25 = new Concentration(AirQuality.PM25);    // 24Hour(μg/m3)
        pm25.addIndexStep(new IndexStep(index1,  0.0,  15.0));
        pm25.addIndexStep(new IndexStep(index2, 16.0,  35.0));
        pm25.addIndexStep(new IndexStep(index3, 36.0,  75.0));
        pm25.addIndexStep(new IndexStep(index4, 76.0, 500.0));

        Concentration pm10 = new Concentration(AirQuality.PM10);    // 24Hour(μg/m3)
        pm10.addIndexStep(new IndexStep(index1,   0.0,  30.0));
        pm10.addIndexStep(new IndexStep(index2,  31.0,  80.0));
        pm10.addIndexStep(new IndexStep(index3,  81.0, 150.0));
        pm10.addIndexStep(new IndexStep(index4, 151.0, 600.0));

        Concentration o3 = new Concentration(AirQuality.O3);    // 1Hour(ppm)
        o3.addIndexStep(new IndexStep(index1, 0.000, 0.030));
        o3.addIndexStep(new IndexStep(index2, 0.031, 0.090));
        o3.addIndexStep(new IndexStep(index3, 0.091, 0.150));
        o3.addIndexStep(new IndexStep(index4, 0.151, 0.600));

        Concentration co = new Concentration(AirQuality.CO);    // 1Hour(ppm)
        co.addIndexStep(new IndexStep(index1,  0.00,  2.00)); 
        co.addIndexStep(new IndexStep(index2,  2.01,  9.00)); 
        co.addIndexStep(new IndexStep(index3,  9.01, 15.00)); 
        co.addIndexStep(new IndexStep(index4, 15.01, 50.00)); 
        
        Concentration so2 = new Concentration(AirQuality.SO2);  // 1Hour(ppm)
        so2.addIndexStep(new IndexStep(index1, 0.000, 0.020));
        so2.addIndexStep(new IndexStep(index2, 0.021, 0.050));
        so2.addIndexStep(new IndexStep(index3, 0.051, 0.150));
        so2.addIndexStep(new IndexStep(index4, 0.151, 1.000));
        
        Concentration no2 = new Concentration(AirQuality.NO2);  // 1 Hour(ppm)
        no2.addIndexStep(new IndexStep(index1, 0.000, 0.030));
        no2.addIndexStep(new IndexStep(index2, 0.031, 0.060));
        no2.addIndexStep(new IndexStep(index3, 0.061, 0.200));
        no2.addIndexStep(new IndexStep(index4, 0.201, 2.000));

        concentrations.addConcentration(pm25);
        concentrations.addConcentration(pm10);
        concentrations.addConcentration(o3);
        concentrations.addConcentration(co);
        concentrations.addConcentration(so2);
        concentrations.addConcentration(no2);

        return concentrations;
    }
}
