package com.kentchiu.spring.base.web;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.kentchiu.spring.attribute.Attribute;
import com.kentchiu.spring.attribute.AttributeInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.WriterResolver;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttributeSnippet implements Snippet {

    private Logger logger = LoggerFactory.getLogger(AttributeSnippet.class);
    private RestDocumentationContextHelper helper;

    public List<String> attributeTable(Class<?> clazz) {
        if (ClassUtils.isPrimitiveOrWrapper(clazz) || Date.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz)) {
            return Lists.newArrayList();
        }

        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
        List<Attribute> attributes = Lists.newArrayList();

        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getReadMethod().isAnnotationPresent(AttributeInfo.class)) {
                Attribute attribute = createAttribute(clazz, pd);
                attributes.add(attribute);
            } else {
                logger.info("AttributeInfo is not present, ignore the property : {}", pd.getName());
            }
        }

        List<Attribute> collect = attributes.stream()
                .filter(a -> !a.isIgnore())
                .filter(a -> !ArrayUtils.contains(new String[]{"class"}, a.getPath()))
                .collect(Collectors.toList());

        return toTable(collect, showTableName(clazz));
    }

    private Attribute createAttribute(Class<?> responseClass, PropertyDescriptor pd) {
        String propertyName = pd.getName();

        AttributeInfo.Type type = AttributeInfo.Type.valueOf(pd.getPropertyType());
        Attribute attribute = new Attribute();
        attribute.setPath(propertyName);
        if (AttributeInfo.Type.UNKNOWN == type || AttributeInfo.Type.OBJECT == type) {
            attribute.setType(pd.getPropertyType().getSimpleName());
        } else {
            attribute.setType(type.name());
        }

        Column column = pd.getReadMethod().getAnnotation(Column.class);
        JoinColumn joinColumn = pd.getReadMethod().getAnnotation(JoinColumn.class);

        if (responseClass.getAnnotation(Table.class) != null && responseClass.getAnnotation(Table.class).name() != null) {
            if (column != null) {
                attribute.setColumn(responseClass.getAnnotation(Table.class).name() + "." + column.name());
            }

            if (joinColumn != null) {
                attribute.setColumn(responseClass.getAnnotation(Table.class).name() + "." + joinColumn.name());
            }
        }

        try {
            boolean required = isRequired(responseClass, pd, propertyName);
            attribute.setRequired(required);
        } catch (Exception e) {
            attribute.setRequired(false);
        }

        Optional<AttributeInfo> infoOptional = findAttributeInfo(responseClass, pd, propertyName);

        if (infoOptional.isPresent()) {
            AttributeInfo info = infoOptional.get();
            attribute.setDefaultValue(info.defaultValue());
            attribute.setFormat(info.format());
            attribute.setDescription(info.description());
            attribute.setIgnore(info.ignore());

            if (StringUtils.isNotBlank(info.path())) {
                attribute.setPath(info.path());
            }
            if (AttributeInfo.Type.UNKNOWN != info.type()) {
                attribute.setPath(info.type().name());
            }
        }
        return attribute;
    }

    protected List<String> toTable(List<Attribute> collect, boolean showColumn) {
        List<String> results = Lists.newArrayList();
        results.add("|===");

        StringBuilder header = new StringBuilder();
        header.append("| Field");
        header.append("| Required");
        header.append("| Type");
        header.append("| default");
        header.append("| Format");
        if (showColumn) {
            header.append("| Column");
        }
        header.append("| Description");
        results.add(header.toString());


        for (Attribute a : collect) {
            results.add("");
            results.add("| " + a.getPath());
            results.add("| " + (a.isRequired() ? "*" : ""));
            results.add("| " + a.getType().toLowerCase());
            results.add("| " + a.getDefaultValue());
            results.add("| " + a.getFormat());
            if (showColumn) {
                results.add("| " + a.getColumn());
            }
            results.add("| " + a.getDescription());

        }
        results.add("|===");
        return results;
    }

    private Optional<AttributeInfo> findAttributeInfo(Class<?> responseClass, PropertyDescriptor pd, String propertyName) {
        Optional<AttributeInfo> infoOptional = Optional.empty();
        try {
            Field field = responseClass.getField(propertyName);
            infoOptional = Optional.ofNullable(field.getAnnotation(AttributeInfo.class));
        } catch (NoSuchFieldException e) {

        }

        if (!infoOptional.isPresent()) {
            try {
                Field declaredField = responseClass.getDeclaredField(propertyName);
                infoOptional = Optional.ofNullable(declaredField.getAnnotation(AttributeInfo.class));
            } catch (NoSuchFieldException e) {

            }
        }
        if (!infoOptional.isPresent()) {
            infoOptional = Optional.ofNullable(pd.getReadMethod().getAnnotation(AttributeInfo.class));
        }
        return infoOptional;
    }

    private boolean isRequired(Class<?> responseClass, PropertyDescriptor pd, String propertyName) {
        boolean required = false;
        try {
            Field field = responseClass.getField(propertyName);
            if (field.isAnnotationPresent(NotNull.class)) {
                required = true;
            }
            if (field.isAnnotationPresent(NotBlank.class)) {
                required = true;
            }
        } catch (NoSuchFieldException e) {

        }

        try {
            Field declaredField = responseClass.getDeclaredField(propertyName);
            if (declaredField.isAnnotationPresent(NotNull.class)) {
                required = true;
            }
            if (declaredField.isAnnotationPresent(NotBlank.class)) {
                required = true;
            }
        } catch (NoSuchFieldException e) {

        }

        if (pd.getReadMethod().isAnnotationPresent(NotNull.class)) {
            required = true;
        }
        if (pd.getReadMethod().isAnnotationPresent(NotBlank.class)) {
            required = true;
        }

        return required;
    }


    @Override
    public void document(Operation operation) throws IOException {
        helper = new RestDocumentationContextHelper(operation);
//        MvcResult mvcResult = (MvcResult) operation.getAttributes().get("org.springframework.test.web.servlet.MockMvc.MVC_RESULT_ATTRIBUTE");
//        HandlerMethod hm = (HandlerMethod) mvcResult.getHandler();
//        Method method = hm.getMethod();
//        Preconditions.checkState(StringUtils.equals(method.getName(),  helper.getTargetMethod().getName()));
        //Parameter[] parameters = method.getParameters();
        Parameter[] parameters = helper.getTargetMethod().getParameters();
        write(operation, helper.getTargetMethod().getReturnType());

        for (Parameter parameter : parameters) {
            write(operation, parameter.getType());
        }
    }

    private void write(Operation operation, Class parameterType) throws IOException {
        List<String> strings = attributeTable(parameterType);


        RestDocumentationContext context = (RestDocumentationContext) operation
                .getAttributes().get(RestDocumentationContext.class.getName());
        WriterResolver writerResolver = (WriterResolver) operation.getAttributes().get(
                WriterResolver.class.getName());
        try (Writer writer = writerResolver.resolve(operation.getName(), parameterType.getSimpleName(), context)) {
            String lines = Joiner.on("\n").join(strings);
            writer.append(lines);
        }
    }

    private boolean showTableName(Class<?> parameterType) {
        boolean showColumn;
        showColumn = !(parameterType.getAnnotation(Table.class) == null || parameterType.getAnnotation(Table.class).name() == null);
        return showColumn;
    }
}
