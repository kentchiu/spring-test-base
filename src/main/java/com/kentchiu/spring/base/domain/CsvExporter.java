package com.kentchiu.spring.base.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.persistence.JoinColumn;
import java.beans.PropertyDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CsvExporter {

    public static <T> List<T> csvToDomains(Class<T> clazz, Path path) throws IOException {
        return csvToDomains(clazz, path, ImmutableMap.of());
    }

    /**
     * @param clazz
     * @param path
     * @param substitutes 重新 mapping 屬性，常用於縮寫字還原成原始字，ex: 在csv的title可能是 qty，但在domain object的屬性是quantity, qty -> quantity
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> csvToDomains(Class<T> clazz, Path path, Map<String, String> substitutes) throws IOException {
        Reader in = new FileReader(path.toFile());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

        return StreamSupport.stream(records.spliterator(), false).map(record -> {
            Map<String, String> map = record.toMap();
            Map<String, Object> map2 = Maps.newHashMap();

            map.forEach((k, v) -> {
                String k2;
                if (substitutes.containsKey(k)) {
                    k2 = substitutes.get(k);
                } else {
                    k2 = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k);
                }
                if (!StringUtils.equalsIgnoreCase("null", v)) {
                    map2.put(k2, v);
                }
            });

            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);

            List<PropertyDescriptor> pdList = Arrays.stream(pds).filter(pd -> IdentifiableObject.class.isAssignableFrom(pd.getPropertyType())).collect(Collectors.toList());
            for (PropertyDescriptor pd : pdList) {
                IdentifiableObject identityObject = createIdentityObject(map, pd);
                if (identityObject != null) {
                    map2.put(pd.getName(), identityObject);
                }
            }

            ObjectMapper om = new ObjectMapper();
            om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            T t = om.convertValue(map2, clazz);
            return t;
        }).collect(Collectors.toList());
    }

    private static IdentifiableObject createIdentityObject(Map<String, String> map, PropertyDescriptor pd) {
        Class<?> type = pd.getPropertyType();
        try {
            Object o = type.newInstance();
            IdentifiableObject io = (IdentifiableObject) o;
            JoinColumn annotation = pd.getReadMethod().getAnnotation(JoinColumn.class);
            if (annotation == null) {
                return null;
            }
            String name = annotation.name();
            String uuid = map.get(name);
            io.setUuid(uuid);
            return io;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Path csvToSql(String tableName, Path path) throws IOException {
        Reader in = new FileReader(path.toFile());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

        List<String> lines = StreamSupport.stream(records.spliterator(), false).map(record -> {
            Map<String, String> map = record.toMap();
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(tableName).append("(").append(Joiner.on(',').join(map.keySet()));
            Collection<String> values = map.values().stream().map(s -> StringUtils.isNumeric(s) ? s : "'" + s + "'").collect(Collectors.toList());
            sb.append(") ").append("VALUES(").append(Joiner.on(',').join(values)).append(");");
            return sb.toString();
        }).collect(Collectors.toList());

        Path sqlFile = path.getParent().resolve(tableName + ".sql");
        Files.write(sqlFile, lines);
        return sqlFile;
    }

}


