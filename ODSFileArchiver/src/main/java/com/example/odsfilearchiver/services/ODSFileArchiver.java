package com.example.odsfilearchiver.services;

import com.example.odsfilearchiver.model.ODS;
import com.example.odsfilearchiver.persistence.OdsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Service
@RestController
public class ODSFileArchiver {
    private static final Logger logger = LoggerFactory.getLogger(ODSFileArchiver.class);
    private static KafkaTemplate kafkaTemplate;
    private static OdsMapper odsMapper;

    @Autowired
    public ODSFileArchiver(OdsMapper odsMapper, KafkaTemplate kafkaTemplate) {
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

    /*@KafkaListener(topics = "filemanager-topic", groupId = "ods-filearchiver-consumer-group-id")
    public void consumeKafkaEvent(String ack) {
        logger.info("Consumed event -> Data=[{}]", ack);
        List<ODS> readyOdsFiles = getAllReadyOdsFiles();
        if(readyOdsFiles.size() > 0) {
            for(ODS ods : readyOdsFiles) {
                insertOdsFile(ods);
            }
        }
    }*/

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
            logger.error("Impossible to write to archivedOds DB!");
        } else {
            logger.info("Written {} to archivedOds DB", ods.toString());
        }
        return result;
    }

    /*public static void archiveODSFilesToNAS(String path) {
        File from = new File(Paths.get(path).getParent().getParent().toString() + "\\readyODS");
        logger.debug("Archiving directory FROM '" + from + "'");
        File to = new File(Paths.get(from.toString()).getParent().toString() + "\\archivedODS");
        logger.debug("Archiving directory TO '" + to + "'");

        try {
            FileUtils.copyDirectory(from, to);
            logger.info("Copied whole directory successfully");
        }
        catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }*/
}
