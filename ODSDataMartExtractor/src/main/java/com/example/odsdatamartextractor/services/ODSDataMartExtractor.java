package com.example.odsdatamartextractor.services;

import com.example.odsdatamartextractor.model.DataMart;
import com.example.odsdatamartextractor.persistence.DataMartMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Service
public class ODSDataMartExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ODSDataMartExtractor.class);
    private static DataMartMapper dataMartMapper;
    private static KafkaTemplate kafkaTemplate;

    @Autowired
    public ODSDataMartExtractor(DataMartMapper dataMartMapper, KafkaTemplate kafkaTemplate) {
        this.dataMartMapper = dataMartMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<DataMart> getAllDataMarts() {
        List<DataMart> dataMartFiles= dataMartMapper.getAllDataMarts();
        if (dataMartFiles.isEmpty()) {
            logger.error("No one Data Mart has been found in DB!");
        } else {
            logger.info("List of all retrieved Data Mart files:");
            for (DataMart dM : dataMartFiles) {
                logger.info(dM.toString());
            }
        }
        return dataMartFiles;
    }

    public void sendEvent(DataMart dataMart, String topic) {
        logger.info("Producing event -> Data=[{}]", dataMart);
        ListenableFuture<SendResult<String, DataMart>> future = kafkaTemplate.send(topic, dataMart);
        future.addCallback(new ListenableFutureCallback<SendResult<String, DataMart>>() {
            @Override
            public void onSuccess(SendResult<String, DataMart> result) {
                logger.info("Sent event -> Data=[{}] over Topic=[{}]" , result.getProducerRecord().value(), result.getProducerRecord().topic());
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send event -> Data=[{}] due to : {}", dataMart, ex.getMessage());
            }
        });
    }
}