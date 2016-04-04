package com.kentchiu.spring.base.web;

import com.kentchiu.spring.base.domain.ApiDoc;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

public class ApiDocSnippet extends TemplatedSnippet {

    private RestDocumentationContextHelper helper;
    private Logger logger = LoggerFactory.getLogger(ApiDocSnippet.class);

    public ApiDocSnippet() {
        super("ApiDoc", null);
    }

    @Override
    protected Map<String, Object> createModel(Operation operation) {
        helper = new RestDocumentationContextHelper(operation);
        Map<String, Object> results = new HashedMap();
        findArgument(operation).ifPresent(arg -> results.put("argument", arg));

        ApiDoc apiDoc = helper.getMethodAnnotation();
        String title = "";
        if (apiDoc != null) {
            title = apiDoc.title();
        }
        if (StringUtils.isBlank(title)) {
            title = operation.getRequest().getMethod().name();
            logger.warn("");
        }
        results.put("title", title);
        results.put("className", helper.getTestClass().getSimpleName());
        try {
            results.put("methodName", helper.getTestMethod().getName());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;
    }

    //    private Optional<String> findArgument(Operation operation) {
//        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
//        HandlerMethod handler = (HandlerMethod) mvcResult.getHandler();
//        MethodParameter[] methodParameters = handler.getMethodParameters();
//        for (MethodParameter p : methodParameters) {
//            Class<?> type = p.getParameterType();
//            if (type.isPrimitive() || type.isArray()) {
//                return Optional.empty();
//            }
//            if ( StringUtils.endsWith(type.getPackage().toString(), ".query") || StringUtils.endsWith(type.getPackage().toString(), ".dto")) {
//                return Optional.of(type.getSimpleName());
//            }
//        }
//        return Optional.empty();
//    }
    private Optional<String> findArgument(Operation operation) {
        Parameter[] parameters = helper.getTargetMethod().getParameters();
        for (Parameter p : parameters) {
            Class<?> type = p.getType();
            if (type.isPrimitive() || type.isArray()) {
                return Optional.empty();
            }
            if (StringUtils.endsWith(type.getPackage().toString(), ".query") || StringUtils.endsWith(type.getPackage().toString(), ".dto")) {
                return Optional.of(type.getSimpleName());
            }
        }
        return Optional.empty();
    }
}
