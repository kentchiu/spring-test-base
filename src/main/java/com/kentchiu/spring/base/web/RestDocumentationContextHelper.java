package com.kentchiu.spring.base.web;

import com.kentchiu.spring.base.domain.ApiDoc;
import com.kentchiu.spring.base.domain.ResourceDoc;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

public class RestDocumentationContextHelper {

    public static final String INCLUDE_SNIPPETS = "include::{snippets}";
    private static final String INCLUDE_API_DOC = "include::{apiDoc}";
    private Operation operation;
    private RestDocumentationContext context;

    public RestDocumentationContextHelper(Operation operation) {
        this.operation = operation;
        this.context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());
    }


    public Class getTestClass() {
        return context.getTestClass();
    }

    public Class getTargetClass() {
        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
        HandlerMethod handler = (HandlerMethod) mvcResult.getHandler();
        return handler.getBeanType();
    }

    public Method getTestMethod() throws NoSuchMethodException {
        return getTestClass().getDeclaredMethod(context.getTestMethodName());
    }

    public Method getTargetMethod() {
        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
        HandlerMethod handler = (HandlerMethod) mvcResult.getHandler();
        return handler.getMethod();
    }

    public ResourceDoc getClassAnnotation() {
        return (ResourceDoc) getTestClass().getAnnotation(ResourceDoc.class);
    }

    public ApiDoc getMethodAnnotation() {
        try {
            return getTestMethod().getAnnotation(ApiDoc.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public String includeApiDoc(String apiDoc) {
        return INCLUDE_API_DOC + apiDoc + "[]";
    }

    public String includeSnippet(String snippetPath) {
        return INCLUDE_SNIPPETS + snippetPath + "[]";
    }

}