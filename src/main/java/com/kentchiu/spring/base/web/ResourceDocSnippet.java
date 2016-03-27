package com.kentchiu.spring.base.web;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateEngine;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResourceDocSnippet implements Snippet {

    private Operation operation;
    private List<Section> sections = new ArrayList<>();

    @Override
    public void document(Operation operation) throws IOException {
        RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());
        this.operation = operation;
        String uri = (String) operation.getAttributes().get("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        Section section = sections.stream().filter(s -> StringUtils.equals(s.getUri(), uri)).findAny().orElseGet(() -> {
            Section s = new Section();
            s.setUri(uri);
            sections.add(s);
            return s;
        });
        String include = "include::{snippets}/" + context.getTestClass().getSimpleName() + "/" + context.getTestMethodName() + "/ApiDoc.adoc[]";
        Api api = new Api(operation.getRequest().getMethod().name(), include);
        section.api.add(api);

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
        List<Section> sortedSessions = sections.stream().sorted((o1, o2) -> o1.uri.length() > o2.getUri().length() ? 1 : -1).collect(Collectors.toList());
        result.put("section", sortedSessions);
        return result;
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
            return api;
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

        public Api(String httpMethod, String include) {
            this.httpMethod = httpMethod;
            this.include = include;
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
