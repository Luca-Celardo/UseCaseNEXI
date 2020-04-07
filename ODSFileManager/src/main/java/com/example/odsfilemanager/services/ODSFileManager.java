package com.example.odsfilemanager.services;

import com.example.odsfilemanager.model.ODS;
import com.example.odsfilemanager.persistence.OdsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Service
@RestController
public class ODSFileManager {

    private static final Logger logger = LoggerFactory.getLogger(ODSFileManager.class);
    private static KafkaTemplate kafkaTemplate;
    private static OdsMapper odsMapper;
    private int kafkaEventsReceived = 0;

    @Autowired
    public ODSFileManager(OdsMapper odsMapper, KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.odsMapper = odsMapper;
    }

    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> consumeCloudEvent(@RequestHeader Map<String, Object> headers, @RequestBody String body) {
        logger.info("Received request headers: {}", headers);
        logger.info("Received request body: {}", body);
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(headers.get("content-type").toString())) {
            logger.info("Consumed event -> Data=[{}]", body);
            return ResponseEntity.ok().build();
        }
        else {
            logger.error("Invalid event format received");
            return ResponseEntity.badRequest().build();
        }
    }

    @KafkaListener(topics = "dataconverter-topic", groupId = "ods-filemanager-consumer-group-id")
    public void consumeKafkaEvent(ODS ods) {
        logger.info("Consumed event -> Data=[{}]", ods);
        Integer result = insertOdsFile(ods);
        int convertedOdsFileCount = getAllConvertedOdsFiles().size();
        logger.info("Founded ODS Files into convertedOds DB: {}", convertedOdsFileCount);
        int readyOdsFileCount = getAllReadyOdsFiles().size();
        logger.info("Founded ODS Files into readyOds DB: {}", readyOdsFileCount);
        if((convertedOdsFileCount == readyOdsFileCount) && (convertedOdsFileCount > 0)) {
            sendEvent("ack", "filemanager-source-topic");
            sendEvent("ack", "filemanager-topic");
        }
    }

    public List<ODS> getAllConvertedOdsFiles() {
        List<ODS> odsList = odsMapper.getAllConvertedOdsFiles();
        if (odsList.isEmpty()) {
            logger.error("No one ODS file has been found in convertedOds DB!");
        } else {
            logger.info("List of all retrieved ODS files from convertedOds DB:");
            for (ODS ods : odsList) {
                logger.info(ods.toString());
            }
        }
        return odsList;
    }

    public List<ODS> getAllReadyOdsFiles() {
        List<ODS> odsList = odsMapper.getAllReadyOdsFiles();
        if (odsList.isEmpty()) {
            logger.error("No one ODS file has been found in readyOds DB!");
        } else {
            logger.info("List of all retrieved ODS files from readyOds DB:");
            for (ODS ods : odsList) {
                logger.info(ods.toString());
            }
        }
        return odsList;
    }

    public Integer insertOdsFile(ODS ods) {
        Integer result = odsMapper.insertOdsFile(ods);
        if(result <= 0) {
            logger.error("Impossible to write to readyOds DB!");
        } else {
            logger.info("Written {} to readyOds DB", ods.toString());
        }
        return result;
    }

    /*public static void copyODSFileToNAS(String pathFile) {
        Path from = Paths.get(pathFile);
        String odsName = pathFile.substring(pathFile.lastIndexOf('\\') + 1);
        Path directoryPath = Paths.get(from.getParent().getParent().toString().replace(odsName, "") + "\\readyODS");
        Path to = Paths.get(directoryPath + "\\" + odsName);
        CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        try {
            if(!Files.exists(directoryPath)) {
                Files.createDirectory(directoryPath);
                logger.info("Created directory:'" + directoryPath.toString() + "'");
            }
            Files.copy(from, to, options);
            logger.info("File '" + odsName + "' was successfully copied FROM:'" + from.toString() + "' TO:'" + to.toAbsolutePath() + "'");
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }*/

    public void sendEvent(String ack, String topic) {
        logger.info("Producing message -> [{}]", ack);
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, ack);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info("Sent message=[{}] over topic=[{}]" , result.getProducerRecord().value(), result.getProducerRecord().topic());
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message=[{}] due to : {}", ack, ex.getMessage());
            }
        });
    }
}
