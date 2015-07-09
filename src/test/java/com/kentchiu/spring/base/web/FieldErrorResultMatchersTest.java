package com.kentchiu.spring.base.web;

import com.kentchiu.spring.base.domain.Validators;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;

public class FieldErrorResultMatchersTest extends TestCase {

    public void testCannotBeBlank() throws Exception {
        String json = " {\"status\" : 404, \"message\" : \"There is a validation error, check content for detail\", \"code\" : \"BindingException\", \"content\" : {\"globalErrors\" : [ ], \"fieldErrors\" : [ {\"field\" : \"no\", \"code\" : \"NotBlank\", \"rejected\" : \"\", \"message\" : \"may not be empty\"} ] } }";

        FieldErrorResultMatchers m = new FieldErrorResultMatchers("no");

        MvcResult result = Mockito.mock(MvcResult.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(result.getResponse()).thenReturn(response);
        response.getWriter().write(json);
        m.cannotBeBlank().match(result);
    }

    public void testCannotBeNull() throws Exception {
        String json = " {\"status\" : 404, \"message\" : \"There is a validation error, check content for detail\", \"code\" : \"BindingException\", \"content\" : {\"globalErrors\" : [ ], \"fieldErrors\" : [ {\"field\" : \"no\", \"code\" : \"NotNull\", \"rejected\" : \"\", \"message\" : \"may not be Null\"} ] } }";

        FieldErrorResultMatchers m = new FieldErrorResultMatchers("no");

        MvcResult result = Mockito.mock(MvcResult.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(result.getResponse()).thenReturn(response);
        response.getWriter().write(json);
        m.cannotBeNull().match(result);
    }

    public void testIsDuplicatedWith() throws Exception {
        String json = "{\"status\" : 404, \"message\" : \"There is a validation error, check content for detail\", \"code\" : \"BindingException\", \"content\" : {\"globalErrors\" : [ ], \"fieldErrors\" : [ {\"field\" : \"no\", \"code\" : \"Duplicated\", \"rejected\" : \"001\", \"message\" : \"the value of %s key is duplicated\"} ] } }";

        FieldErrorResultMatchers m = new FieldErrorResultMatchers("no");

        MvcResult result = Mockito.mock(MvcResult.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(result.getResponse()).thenReturn(response);
        response.getWriter().write(json);
        m.isDuplicatedWith("001").match(result);
    }

    public void testFailByCode() throws Exception {
        String json = "{\"status\" : 404, \"message\" : \"There is a validation error, check content for detail\", \"code\" : \"BindingException\", \"content\" : {\"globalErrors\" : [ ], \"fieldErrors\" : [ {\"field\" : \"no\", \"code\" : \"Duplicated\", \"rejected\" : \"001\", \"message\" : \"the value of %s key is duplicated\"} ] } }";
        FieldErrorResultMatchers m = new FieldErrorResultMatchers("no");

        MvcResult result = Mockito.mock(MvcResult.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(result.getResponse()).thenReturn(response);
        response.getWriter().write(json);
        m.failByCode(Validators.DUPLICATED).match(result);
    }


    public void testInValues() throws Exception {

    }

    public void testGreatThenOrEqualTo() throws Exception {

    }
}