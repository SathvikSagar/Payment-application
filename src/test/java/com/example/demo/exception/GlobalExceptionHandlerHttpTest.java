package com.example.demo.exception;

import org.junit.jupiter.api.Test;
import com.example.demo.service.JwtService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerHttpTest {
	@MockitoBean
	private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unexpectedExceptionReturnsSafeHttp500Response()
            throws Exception {

        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.nullValue()));
    }
}