package com.kentchiu.spring.base.web;

import com.kentchiu.spring.base.domain.ApiDoc;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

public class RestDocumentationContextHelper {

    private Operation operation;

    public RestDocumentationContextHelper(Operation operation) {
        this.operation = operation;
    }

    public RestDocumentationContext getContext() {
        return (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());

    }

    public Class getTestClass() {
        return getContext().getTestClass();
    }

    public Class getTargetClass() {
        return null;
    }

    public Method getTestMethod() throws NoSuchMethodException {
        return getTestClass().getDeclaredMethod(getContext().getTestMethodName());
    }

    public Method getTargetMethod() {
        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
        HandlerMethod handler = (HandlerMethod) mvcResult.getHandler();
        return handler.getMethod();
    }

    public ApiDoc getClassAnnotation() {
        return null;
    }

    public ApiDoc getMethodAnnotation() {
        try {
            return getTestMethod().getAnnotation(ApiDoc.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}