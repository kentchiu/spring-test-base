package com.kentchiu.spring.base.web;

import com.jayway.jsonassert.JsonAssert;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

public class RestErrorResultMatchers {


    public RestErrorResultMatchers() {
    }


    public ResultMatcher code(String code) {
        return (MvcResult result) -> {
            String json = result.getResponse().getContentAsString();
            JsonAssert.with(json).assertEquals("$.code", code);
        };
    }

    public ResultMatcher message(String codeName) {
        return (MvcResult result) -> {
            String json = result.getResponse().getContentAsString();
            JsonAssert.with(json).assertEquals("$.message", codeName);
        };
    }

}
