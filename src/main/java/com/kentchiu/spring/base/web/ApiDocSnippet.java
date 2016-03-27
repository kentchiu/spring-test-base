package com.kentchiu.spring.base.web;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;
import java.util.Optional;

public class ApiDocSnippet extends TemplatedSnippet {


    public ApiDocSnippet() {
        super("ApiDoc", null);
    }

    @Override
    protected Map<String, Object> createModel(Operation operation) {
        RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());
        Map<String, Object> results = new HashedMap();
        findArgument(operation).ifPresent(arg -> results.put("argument", arg));
        results.put("httpMethod", operation.getRequest().getMethod());
        results.put("className", context.getTestClass().getSimpleName());
        results.put("methodName", context.getTestMethodName());
        return results;
    }

    private Optional<String> findArgument(Operation operation) {
        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
        HandlerMethod handler = (HandlerMethod) mvcResult.getHandler();
        MethodParameter[] methodParameters = handler.getMethodParameters();
        for (MethodParameter p : methodParameters) {
            Class<?> type = p.getParameterType();
            if (StringUtils.endsWith(type.getPackage().toString(), ".query") || StringUtils.endsWith(type.getPackage().toString(), ".dto")) {
                return Optional.of(type.getSimpleName());
            }
        }
        return Optional.empty();
    }
}
