package io.openindoormap.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.controller.view.SigninController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SigninController.class)
class SigninControllerTest extends BaseControllerTest {

    @Test
    void signin() throws Exception {
        mockMvc.perform(get("/sign/signin")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().hasNoErrors())
                .andExpect(status().is2xxSuccessful())
                .andDo(document("index"));

        System.out.println(mockMvc);
    }
}