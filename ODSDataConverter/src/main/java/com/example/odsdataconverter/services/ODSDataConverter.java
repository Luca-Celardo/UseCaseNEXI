package com.example.odsdataconverter.services;

import com.example.odsdataconverter.model.DataMart;
import com.example.odsdataconverter.model.ODS;
import com.example.odsdataconverter.persistence.OdsMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.http.Unmarshallers;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RestController
public class ODSDataConverter {

    private static final Logger logger = LoggerFactory.getLogger(ODSDataConverter.class);
    //private static final Path path = Paths.get("/app", "nfs", "convertedODS");
    private static KafkaTemplate kafkaTemplate;
    private static OdsMapper odsMapper;
    private static HashMap<String, String> typeMap = null;
    private static HashMap<String, String> nameMap = null;
    private int kafkaEventsReceived = 0;

    @Autowired
    public ODSDataConverter(OdsMapper odsMapper, KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.odsMapper = odsMapper;

        typeMap = new HashMap<>();
        typeMap.put("Transactions", "Transactions");
        typeMap.put("Clearing", "Transactions");
        typeMap.put("Additional transaction records", "Transactions");
        typeMap.put("BIN Table", "Scheme data");
        typeMap.put("Dispute - Mastercard", "Transactions - Dispute");
        typeMap.put("Dispute - CBPT", "Transactions - Dispute");
        typeMap.put("Dispute - Lost Chargeback", "Transactions - Dispute");
        typeMap.put("Fraud - SAFE and TC40", "Transactions - Fraud events");
        typeMap.put("Payments", "Payments");
        typeMap.put("Revenue engine data", "Invoice");
        typeMap.put("Fees", "Invoice");

        nameMap = new HashMap<>();
        nameMap.put("WI0018", "ACQMOV");
        nameMap.put("WI0019", "ACQMOVCLEA");
        nameMap.put("WI0020", "ACQMOVADD");
        nameMap.put("WI0023", "ACQBIN");
        nameMap.put("WI0024", "ACQMAST");
        nameMap.put("WI0025", "ACQDISP");
        nameMap.put("WI0026", "ACQOPNOP");
        nameMap.put("WI0051", "ACQSAFE");
        nameMap.put("WI0053", "TBD1");
        nameMap.put("WI0058", "TBD2");
        nameMap.put("WI0061", "TBD3");
    }

    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> consumeCloudEvent(@RequestHeader Map<String, Object> headers, @RequestBody String body) {
        logger.info("Received request headers: {}", headers);
        logger.info("Received request body: {}", body);
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(headers.get("content-type").toString())) {
            /*CloudEvent<AttributesImpl, Map> cloudEvent = Unmarshallers.binary(Map.class)
                    .withHeaders(() -> headers)
                    .withPayload(() -> body)
                    .unmarshal();
            logger.info("Received CloudEvent: " + cloudEvent.getData().toString());*/
            logger.info("Consumed event -> Data=[{}]", body);
            return ResponseEntity.ok().build();
        }
        else {
            logger.error("Invalid event format received");
            return ResponseEntity.badRequest().build();
        }
    }

    @KafkaListener(topics = "datamartextractor-topic", groupId = "ods-dataconverter-consumer-group-id")
    public void consumeKafkaEvent(DataMart dataMart) {
        logger.info("Consumed event -> Data=[{}]", dataMart);
        logger.info("Kafka events received = {}", this.kafkaEventsReceived);
        this.kafkaEventsReceived++;
        logger.info("Kafka events received = {}", this.kafkaEventsReceived);
        ODS ods = convertDataMartFileIntoOdsFile(dataMart);
        Integer result = insertOdsFile(ods);
        if(result > 0) {
            if(this.kafkaEventsReceived == 1) {
                sendEvent(ods, "dataconverter-source-topic");
            }
            sendEvent(ods,"dataconverter-topic");
        }
    }

    public ODS convertDataMartFileIntoOdsFile(DataMart dataMart) {
        ODS ods = new ODS();
        ods.setName(nameMap.get(dataMart.getInterfaceId()));
        ods.setType(typeMap.get(dataMart.getType()));
        logger.info("Converted {} into {}", dataMart.toString(), ods.toString());
        return ods;
    }

    public List<ODS> getAllOdsFiles() {
        List<ODS> odsList = odsMapper.getAllOdsFiles();
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

    public Integer insertOdsFile(ODS ods) {
        Integer result = odsMapper.insertOdsFile(ods);
        if(result <= 0) {
            logger.error("Impossible to write to convertedOds DB!");
        } else {
            logger.info("Written {} to convertedOds DB", ods.toString());
        }
        return result;
    }

    /*public String writeODSFileToNAS(ODS ods) {
        String filePath = null;
        try {
            if(!Files.exists(path)) {
                Path dPath = Files.createDirectory(path);
                logger.info("Created directory:'{}'", dPath.toAbsolutePath().toString());
            }
            File file = new File(Paths.get(path.toString(), ods.getName() + ".ods").toString());
            Boolean fileCreation = file.createNewFile();
            if(fileCreation) {
                filePath = file.getAbsolutePath();
                logger.info("Created file {} at PATH='{}'", file.getName(), filePath);
            } else {
                logger.error("Impossible to create file {} at PATH='{}'", file.getName(), file.getAbsolutePath());
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(ods);
            objectOut.close();
            logger.info("The {} was successfully written to the NAS at PATH:'{}'", ods.toString(), path.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
        return filePath;
    }*/

    public void sendEvent(ODS ods, String topic) {
        logger.info("Producing message -> [{}]", ods);
        ListenableFuture<SendResult<String, ODS>> future = kafkaTemplate.send(topic, ods);
        future.addCallback(new ListenableFutureCallback<SendResult<String, ODS>>() {
            @Override
            public void onSuccess(SendResult<String, ODS> result) {
                logger.info("Sent message=[{}] over topic=[{}]" , result.getProducerRecord().value(), result.getProducerRecord().topic());
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message=[{}] due to : {}", ods, ex.getMessage());
            }
        });
    }
}
