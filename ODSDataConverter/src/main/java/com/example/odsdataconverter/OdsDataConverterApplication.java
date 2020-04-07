package com.example.odsdataconverter;

import com.example.odsdataconverter.model.DataMart;
import com.example.odsdataconverter.model.ODS;
import com.example.odsdataconverter.services.ODSDataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class OdsDataConverterApplication {

    private static ODSDataConverter odsDataConverter;

    @Autowired
    public OdsDataConverterApplication(ODSDataConverter odsDataConverter) {
        this.odsDataConverter = odsDataConverter;
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(OdsDataConverterApplication.class, args);
    }
}
