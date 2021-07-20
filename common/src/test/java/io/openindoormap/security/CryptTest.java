package io.openindoormap.security;

import org.junit.jupiter.api.Test;

public class CryptTest {

	/**
	 * 암복호화 테스트
	 */
	@Test
	public void 암복호화() {
		System.out.println("url : " + Crypt.encrypt("jdbc:postgresql://localhost:15432/oim"));
		System.out.println("user : " + Crypt.encrypt("postgres"));
		System.out.println("password : " + Crypt.encrypt("postgres"));
		System.out.println("oim : " + Crypt.encrypt("oim"));
		
		System.out.println(Crypt.decrypt("t//vxAeu6orCXA7GKXoOjQ=="));
	}
}
