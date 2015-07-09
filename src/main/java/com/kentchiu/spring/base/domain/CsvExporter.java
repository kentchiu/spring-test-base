package com.kentchiu.spring.base.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CsvExporter {


    public static <T> List<T> csvToDomains(Class<T> clazz, Path path) throws IOException {
        Reader in = new FileReader(path.toFile());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

        return StreamSupport.stream(records.spliterator(), false).map(record -> {
            Map<String, String> map = record.toMap();
            Map<String, String> map2 = Maps.newHashMap();

            map.forEach((k, v) -> {
                String k2 = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k);
                if (!StringUtils.equalsIgnoreCase("null", v)) {
                    map2.put(k2, v);
                }
            });

            ObjectMapper om = new ObjectMapper();
            om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return om.convertValue(map2, clazz);
        }).collect(Collectors.toList());
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
