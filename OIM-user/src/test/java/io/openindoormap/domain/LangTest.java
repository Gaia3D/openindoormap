package io.openindoormap.domain;

import org.junit.jupiter.api.Test;

public class LangTest {

    @Test
    void 언어_변환() {

    }

    @Test
    void 페이징_테스트() {
        Long pageNo = 2L;
        Long pageListCount = 5L;
        Long startPage = ((pageNo - 1L) / pageListCount) * pageListCount + 1L;
        Long endPage = ((pageNo - 1L) / pageListCount) * pageListCount + pageListCount;

        System.out.println((pageNo - 1L));
        System.out.println((pageNo - 1L) / pageListCount);
        System.out.println(((pageNo - 1L) / pageListCount) * pageListCount);
        System.out.println(((pageNo - 1L) / pageListCount) * pageListCount + 1L);

    }

}
