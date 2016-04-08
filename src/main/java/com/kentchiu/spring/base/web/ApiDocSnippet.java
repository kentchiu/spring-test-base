package com.kentchiu.spring.base.web;

import com.google.common.base.CaseFormat;
import com.kentchiu.spring.base.domain.ApiDoc;
import com.kentchiu.spring.base.domain.Include;
import com.kentchiu.spring.base.domain.Position;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

import java.lang.reflect.Parameter;
import java.util.Arrays;
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

        ApiDoc apiDocs = helper.getMethodAnnotation();
        String title = getTitle(operation, apiDocs);
        results.put("title", title);
        results.put("className", helper.getTestClass().getSimpleName());
        try {
            results.put("methodName", helper.getTestMethod().getName());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            Include[] includes = helper.getTestMethod().getAnnotationsByType(Include.class);
            Arrays.stream(Position.values()).map(p -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, p.name())).forEach(name -> results.put(name, ""));
            for (Include each : includes) {
                if (StringUtils.isNotBlank(each.value())) {
                    String include = helper.includeApiDoc(each.value());
                    logger.debug("each file: {} " + include);
                    results.put(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, each.position().name()), include);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;
    }

    private String getTitle(Operation operation, ApiDoc apiDoc) {
        String title = "";
        if (apiDoc != null) {
            title = apiDoc.title();
        }
        if (StringUtils.isBlank(title)) {
            title = operation.getRequest().getMethod().name();
        }
        return title;
    }

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
