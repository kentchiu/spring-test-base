package com.kentchiu.spring.base.web;

import com.google.common.base.Preconditions;
import com.kentchiu.spring.base.domain.ApiDoc;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateEngine;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceDocSnippet implements Snippet {

    public static final String DEFAULT_MESSAGE = "FIXME: NOT FOUND";
    private Operation operation;
    private List<Section> sections = new ArrayList<>();
    private String attributeInclude = DEFAULT_MESSAGE;
    private RestDocumentationContextHelper helper;
    private Logger logger = LoggerFactory.getLogger(ResourceDocSnippet.class);

    @Override
    public void document(Operation operation) throws IOException {
        helper = new RestDocumentationContextHelper(operation);
        RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());
        this.operation = operation;
        String uri = (String) operation.getAttributes().get("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        Section section = sections.stream().filter(s -> StringUtils.equals(s.getUri(), uri)).findAny().orElseGet(() -> {
            Section s = new Section();
            s.setUri(uri);
            sections.add(s);
            return s;
        });

        ApiDoc apiDoc = helper.getMethodAnnotation();
        if (apiDoc != null) {
            String include = helper.includeSnippet("/" + context.getTestClass().getSimpleName() + "/" + context.getTestMethodName() + "/ApiDoc.adoc");
            Api api = new Api(operation.getRequest().getMethod().name(), include);
            setOrder(context, api);
            section.api.add(api);

            if (HttpMethod.GET == operation.getRequest().getMethod()) {
                String url = operation.getRequest().getUri().toString();
                if (isMatches(url)) {
                    String returnType = helper.getTargetMethod().getReturnType().getSimpleName();
                    Preconditions.checkState(StringUtils.equals(returnType, helper.getTargetMethod().getReturnType().getSimpleName()));
                    attributeInclude = helper.includeApiDoc("/" + context.getTestClass().getSimpleName() + "/" + context.getTestMethodName() + "/" + returnType + ".adoc");
                }
            }
        }
    }

    boolean isMatches(String url) {
        if (url.endsWith("/")) {
            return url.matches(".*\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}[\\?? /b]?.*");
        } else {
            String s = StringUtils.substringAfterLast(url, "/");
            return s.matches(".*\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}[\\?? /b]?.*");
        }
    }

    private void setOrder(RestDocumentationContext context, Api api) {
        ApiDoc annotation = helper.getMethodAnnotation();
        if (annotation != null) {
            api.setOrder(annotation.order());
        }
    }

    public void writeAndFlush() throws IOException {
        RestDocumentationContext context = (RestDocumentationContext) operation
                .getAttributes().get(RestDocumentationContext.class.getName());
        WriterResolver writerResolver = (WriterResolver) operation.getAttributes().get(
                WriterResolver.class.getName());
        String snippetName = "ResourceDoc";
        try (Writer writer = writerResolver.resolve("{ClassName}", snippetName, context)) {
            Map<String, Object> model = createModel(operation);
            TemplateEngine templateEngine = (TemplateEngine) operation.getAttributes().get(TemplateEngine.class.getName());
            writer.append(templateEngine.compileTemplate(snippetName).render(model));
        }
        sections.clear();
    }

    private Map<String, Object> createModel(Operation operation) {
        Map<String, Object> result = new HashedMap();

        RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());

        ApiDoc annotation = context.getTestClass().getAnnotation(ApiDoc.class);

        if (annotation != null) {
            result.put("resourceChineseName", annotation.title());
        } else {
            result.put("resourceChineseName", helper.getTargetClass().getSimpleName());
            logger.warn("Missing @ApiDoc in test class");
        }

        result.put("snippet", getSnippet());

        if (StringUtils.equals(DEFAULT_MESSAGE, attributeInclude)) {
            logger.warn("Can't found attributeInclude");
        }
        result.put("attributeInclude", attributeInclude);
        List<Section> sortedSessions = sections.stream().sorted((o1, o2) -> o1.uri.length() > o2.getUri().length() ? 1 : -1).collect(Collectors.toList());
        List<Section> sections = sortedSessions.stream().filter(s -> !s.api.isEmpty()).collect(Collectors.toList());
        result.put("section", sections);
        return result;
    }

    private String getSnippet() {
        if (helper.getClassAnnotation() != null) {
            String snippet = helper.getClassAnnotation().include();
            if (StringUtils.isNotBlank(snippet)) {
                return helper.includeApiDoc(snippet);
            }
        }
        return "";
    }

    class Section {
        private String uri;
        private List<Api> api = new ArrayList<>();

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public List<Api> getApi() {
            Map<String, Integer> map = new HashedMap();
            map.put("GET", 100);
            map.put("POST", 90);
            map.put("PATCH", 80);
            map.put("PUT", 70);
            map.put("DELETE", 60);


            Comparator<Api> c1 = (o1, o2) -> {
                Integer grade1 = map.get(o1.getHttpMethod());
                Integer grade2 = map.get(o2.getHttpMethod());
                return grade2 - grade1;
            };

            List<Api> result = api.stream().sorted(c1.thenComparing(Api::getOrder)).collect(Collectors.toList());
            return result;
        }

        public void setApi(List<Api> api) {
            this.api = api;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Section section = (Section) o;
            return Objects.equals(uri, section.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }
    }

    class Api {
        private String httpMethod;
        private String include;
        private int order = Integer.MAX_VALUE;

        public Api(String httpMethod, String include) {
            this.httpMethod = httpMethod;
            this.include = include;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getInclude() {
            return include;
        }

        public void setInclude(String include) {
            this.include = include;
        }


    }
}

