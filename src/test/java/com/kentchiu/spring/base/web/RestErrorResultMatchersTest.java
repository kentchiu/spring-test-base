package com.kentchiu.spring.base.web;

import com.kentchiu.spring.base.domain.Validators;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;

public class RestErrorResultMatchersTest {

    @Test
    public void testErrorCode() throws Exception {
        String json = "{\"status\" : 404, \"message\" : \"Resource Not Found\", \"code\" : \"ResourceNotFound\", \"content\" : \"brand_uuid_001\"}";
        RestErrorResultMatchers m = new RestErrorResultMatchers();

        MvcResult result = Mockito.mock(MvcResult.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(result.getResponse()).thenReturn(response);
        response.getWriter().write(json);
        m.code(Validators.RESOURCE_NOT_FOUND).match(result);
        m.message("Resource Not Found").match(result);
    }
}