package io.openindoormap;

import org.junit.jupiter.api.Test;

import io.openindoormap.domain.ShapeFileExt;

class OIMAdminApplicationTests {

	@Test
	void contextLoads() {
		
		 System.out.println(ShapeFileExt.findBy("shp"));
	}

}
