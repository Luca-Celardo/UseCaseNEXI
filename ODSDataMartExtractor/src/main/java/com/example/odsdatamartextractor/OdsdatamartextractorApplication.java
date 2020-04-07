package com.example.odsdatamartextractor;

import com.example.odsdatamartextractor.model.DataMart;
import com.example.odsdatamartextractor.services.ODSDataMartExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class OdsdatamartextractorApplication {

    private static ODSDataMartExtractor odsDataMartExtractor;

    @Autowired
    public OdsdatamartextractorApplication(ODSDataMartExtractor odsDataMartExtractor){
        this.odsDataMartExtractor = odsDataMartExtractor;
    }

    public static void main(String[] args) {
        SpringApplication.run(OdsdatamartextractorApplication.class, args);

        List<DataMart> dataMartFiles = odsDataMartExtractor.getAllDataMarts();
        if(dataMartFiles.size() > 0) {
            for(DataMart dataMart : dataMartFiles) {
                odsDataMartExtractor.sendEvent(dataMart, "datamartextractor-topic");
            }
            odsDataMartExtractor.sendEvent(new DataMart(0, "interface-id", "type"), "datamartextractor-source-topic");
        }
    }
}