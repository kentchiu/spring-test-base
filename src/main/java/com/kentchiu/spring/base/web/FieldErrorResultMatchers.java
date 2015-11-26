package com.kentchiu.spring.base.web;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import com.kentchiu.spring.base.domain.Validators;
import net.minidev.json.JSONArray;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FieldErrorResultMatchers {

    private String name;

    public FieldErrorResultMatchers(String name) {
        this.name = name;
    }

    public ResultMatcher cannotBeBlank() {
        return result -> verifyJson(result, Validators.NOT_BLANK);
    }

    public ResultMatcher cannotBeNull() {
        return result -> verifyJson(result, Validators.NOT_NULL);
    }


    public ResultMatcher isDuplicatedWith(String value) {
        return result -> verifyJson(result, Validators.DUPLICATED, new Object[]{value});
    }

    public ResultMatcher failByCode(String codeName) {
        return (MvcResult result) -> verifyJson(result, codeName);
    }

    public ResultMatcher inValues(String... values) {
        return result -> verifyJson(result, Validators.NOT_IN, values);
    }

    public ResultMatcher greatThenOrEqualTo(int value) {
        return result -> verifyJson(result, Validators.MIN, new Object[]{value});

    }

    private void verifyJson(MvcResult result, String code, Object... values) throws UnsupportedEncodingException {
        String json = result.getResponse().getContentAsString();
        JSONArray fields = JsonPath.read(json, "$.content.fieldErrors[?]", filter(where("field").is(name)));
        if (fields.isEmpty()) {
            fail("expect field [" + name + "], but not present");
        } else if (fields.size() > 1) {
            fail("more then one field called [" + name + "] present");
        }
        Preconditions.checkState(fields.size() == 1, "fieldErrors should have one and only one element");
        LinkedHashMap o = (LinkedHashMap) fields.get(0);
        assertEquals(o.get("field"), name);
        assertEquals(o.get("code"), code);
    }
}
