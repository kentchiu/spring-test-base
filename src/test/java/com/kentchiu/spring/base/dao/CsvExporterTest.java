package com.kentchiu.spring.base.dao;

import com.kentchiu.spring.base.domain.CsvExporter;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CsvExporterTest {


    @Test
    public void testCsvToSql() throws Exception {
        Path path = Paths.get(CsvExporterTest.class.getResource("car.csv").toURI());
        Path sqlFile = CsvExporter.csvToSql("CAR", path);
        List<String> lines = Files.readAllLines(sqlFile);
        assertThat(lines.size(), is(3));
        assertThat(lines.get(0), is("INSERT INTO CAR(ID,PRICE,DOORS,COLOR,NAME) VALUES(1,'1000.50',4,'WHITE','CAR1');"));
        assertThat(lines.get(1), is("INSERT INTO CAR(ID,PRICE,DOORS,COLOR,NAME) VALUES(2,'123.45',5,'BLUE','CAR2');"));
        assertThat(lines.get(2), is("INSERT INTO CAR(ID,PRICE,DOORS,COLOR,NAME) VALUES(3,'null',3,'RED','CAR3');"));
    }


    @Test
    public void testFromCsvToObjects() throws Exception {
        Path path = Paths.get(CsvExporterTest.class.getResource("car.csv").toURI());
        List<Car> cars = CsvExporter.csvToDomains(Car.class, path);
        assertThat(cars.size(), is(3));
        assertThat(cars.get(0).getId(), is("1"));
        assertThat(cars.get(0).getName(), is("CAR1"));
        assertThat(cars.get(0).getDoors(), is(4));
        assertThat(cars.get(0).getColor(), is(Color.WHITE));
    }

}