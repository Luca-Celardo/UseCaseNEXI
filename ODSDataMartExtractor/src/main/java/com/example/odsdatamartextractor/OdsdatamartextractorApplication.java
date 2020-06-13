package com.example.odsdatamartextractor;

import com.example.odsdatamartextractor.model.DataMart;
import com.example.odsdatamartextractor.services.ODSDataMartExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class OdsdatamartextractorApplication {

    private static ODSDataMartExtractor odsDataMartExtractor;

    @Autowired
    public OdsdatamartextractorApplication(ODSDataMartExtractor odsDataMartExtractor){
        this.odsDataMartExtractor = odsDataMartExtractor;
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(OdsdatamartextractorApplication.class, args);

        Date finishTime = new Date(System.currentTimeMillis() + 300000);

        while(System.currentTimeMillis() < finishTime.getTime()) {
            List<DataMart> dataMartFiles = odsDataMartExtractor.getAllDataMarts();
            if(dataMartFiles.size() > 0) {
                for(DataMart dataMart : dataMartFiles) {
                    odsDataMartExtractor.sendEvent(dataMart, "datamartextractor-topic");
                }
                odsDataMartExtractor.sendEvent(new DataMart(0, "interface-id", "type"), "datamartextractor-source-topic");
            }
            TimeUnit.SECONDS.sleep(5);
        }
    }
}