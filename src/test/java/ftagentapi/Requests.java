package ftagentapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static ftagentapi.BaseClass.*;

public class Requests {

    static String putToStorage(String environment, String system) {
        String url = getURL(environment, system);
        String sourceSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        switch (system) {
            case "lan":
                sourceSystem = "system-4";
                break;
            case "dmz":
                sourceSystem = "system-5-dmz";
        }
        Response response = RestAssured.given()
                .header("Content-Type", "multipart/form-data")
                .queryParam("cid", cid)
                .queryParam("originalFilename", "123кириллица.pdf")
                .queryParam("sourceSystem", sourceSystem)
                .multiPart("file", new File("src/main/resources/123.txt"), "text/plain")
                .post(url + "put-to-storage")
                .then()
                .extract()
                .response();
        String putToStorageResponse = response.body().asString();
        log.info("Put To Storage Response: " + putToStorageResponse);
        return putToStorageResponse;
    }

    static String putFile(String environment, String system) {
        String url = getURL(environment, system);
        String sourceSystem = "";
        String targetSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        switch (system) {
            case "lan":
                sourceSystem = "system-4";
                targetSystem = "system-1";
                break;
            case "dmz":
                sourceSystem = "system-5-dmz";
                targetSystem = "system-4";
        }
        Response response = RestAssured.given()
                .header("Content-Type", "multipart/form-data")
                .queryParam("cid", cid)
                .queryParam("originalFilename", "123кириллица.pdf")
                .queryParam("sourceSystem", sourceSystem)
                .queryParam("targetSystem", targetSystem)
                .multiPart("file", new File("src/main/resources/123.txt"), "text/plain")
                .post(url + "put-to-storage")
                .then()
                .extract()
                .response();
        String putFileResponse = response.body().asString();
        log.info("Put File Response: " + putFileResponse);
        return putFileResponse;
    }

    static String getFile(String environment, String system) throws IOException {
        String url;
        String sourceSystem = "";
        String targetSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        String requestBody = new String(Files.readAllBytes(Paths.get(paths + "getFile.json")), StandardCharsets.UTF_8);
        url = getURL(environment, system);
        switch (system) {
            case "lan":
                sourceSystem = "system-3";
                targetSystem = "system-1";
                break;
            case "dmz":
                sourceSystem = "system-2-dmz";
                targetSystem = "system-1";
        }
        requestBody = requestBody.replace("${CID}", cid).replace("${SOURCE}", sourceSystem).replace("${TARGET}", targetSystem);
        log.info("body is: " + requestBody);
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(url + "get-file")
                .then()
                .extract()
                .response();
        String getFileResponse = response.body().asString();
        log.info("Get File Response: " + getFileResponse);
        return getFileResponse;
    }

    static String getFromStorage(String environment, String system) throws IOException {
        String url;
        String targetSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        String requestBody = new String(Files.readAllBytes(Paths.get(paths + "getFromStorage.json")), StandardCharsets.UTF_8);
        url = getURL(environment, system);
        switch (system) {
            case "lan":
                targetSystem = "system-4";
                break;
            case "dmz":
                targetSystem = "system-5-dmz";
                break;
        }
        requestBody = requestBody.replace("${CID}", cid).replace("${TARGET}", targetSystem);
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(url + "get-from-storage")
                .then()
                .extract()
                .response();
        String getFromStorageResponse = response.body().asString();
        log.info("Get From Storage Response: " + getFromStorageResponse);
        return getFromStorageResponse;
    }

    static String completeFileTransfer(String environment, String system, String traceID) {
        String url = getURL(environment, system);
        Response response = RestAssured.given()
                .put(url + "complete-file-transfer/" + traceID)
                .then()
                .extract()
                .response();
        String completeFileTransferResponse = response.body().asString();
        log.info("Complete File Transfer Response: " + completeFileTransferResponse);
        return completeFileTransferResponse;
    }

    static String downloadFile(String environment, String system, String traceID) {
        String url = getURL(environment, system);
        Response response = RestAssured.given()
                .get(url + "download/" + traceID)
                .then()
                .statusCode(200)
                .extract()
                .response();
        String downloadResponse = response.body().asString();
//        log.info(downloadResponse);
        return downloadResponse;
    }

    static String downloadFileNegative(String environment, String system, String traceID) {
        String url = getURL(environment, system);
        Response response = RestAssured.given()
                .get(url + "download/" + traceID)
                .then()
                .statusCode(500)
                .extract()
                .response();
        String downloadResponse = response.body().asString();
//        log.info(downloadResponse);
        return downloadResponse;
    }

    static String status(String traceID, String environment, String system) {
        String url = getURL(environment, system);
        url = url + "status/" + traceID;
        System.out.println(url);
        Response response = RestAssured.given()
                .get(url)
                .then()
                .extract()
                .response();
        String statusResponse = response.body().asString();
        log.info("Status Response: " + statusResponse);
        return statusResponse;
    }


    static void kafkaReceived(String environment, String source, String traceId) {
        Logger log = LoggerFactory.getLogger(UnitTests.class);
        Properties props = new Properties();
        String cid = String.valueOf(UUID.randomUUID());
        String service = getKafkaURL(environment, source);
        props.put("bootstrap.servers", service);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String msg =
                "{\"cid\":\"" + cid + "\"," +
                        "\"traceId\":\"" + traceId + "\"," +
                        "\"type\":\"FILE_RECEIVED\"}";
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("ft-system-notify", 0, String.valueOf(cid), msg));

        }
        log.info("Recieved sent:" + msg);
    }

    public static void kafkaPutFile(String environment, String source, String traceId) {
        String transferTarget = "";
        String targetSystem = "";
        String sourceSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        switch (source) {
            case "dmz":
                transferTarget = "sys2";
                sourceSystem = "system-2-dmz";
                targetSystem = "system-1";
                break;
            case "lan":
                transferTarget = "sys3";
                sourceSystem = "system-3";
                targetSystem = "system-1";
                break;
        }
        String fname = "test_" + cid + ".txt";
        transferMethod(cid, source, transferTarget, fname, environment);
        String fileName = fname;
        putMethod(cid, source, sourceSystem, targetSystem, fileName, environment, traceId);
    }


    static void transferMethod(String cid, String source, String targetSystem, String name, String environment) {
        Logger log = LoggerFactory.getLogger(UnitTests.class);
        Properties props = new Properties();
        String service = getKafkaURL(environment);
        props.put("bootstrap.servers", service);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String transferFileName = "transfer1.txt";
        String msg =
                "{\"cid\":\"" + cid + "\"," +
                        "\"operation\":" + "\"COPY\"" + "," +
                        "\"sourceFilename\":\"/opt/dmz/sys5/out/" + transferFileName + "\"," +
                        "\"targetFilename\":\"/opt/" + source + "/" + targetSystem + "/out/" + name + "\"}";
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("ft-transfer-request", msg));
        }
        log.info("Transfer:" + msg);
    }

    static void putMethod(String cid, String source, String sourceSystem, String targetSystem, String fileName, String environment, String traceId) {
        Logger log = LoggerFactory.getLogger(UnitTests.class);
        Properties props = new Properties();
        String service = getKafkaURL(environment, source);
        props.put("bootstrap.servers", service);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String msg =
                "{\"cid\":\"" + cid + "\"," +
                        "\"operation\":\"PUT_FILE\"," +
                        "\"traceId\":\"" + traceId + "\"," +
                        "\"sourceSystem\":\"" + sourceSystem + "\"," +
                        "\"targetSystem\":\"" + targetSystem + "\"," +
                        "\"originalFilename\":\"" + fileName + "\"}";
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("ft-agent-request", msg));
        }
        log.info("Message sent:" + msg);
    }


    static List<String> putToStorageNegative(String environment, String system, String parameter) {
        String url = getURL(environment, system);
        String sourceSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        String fileName;
        List<String> results = new ArrayList<>();
        switch (parameter) {
            case "noFile":
                fileName = "144j/df";
                switch (system) {
                    case "lan":
                        sourceSystem = "system-4";
                        break;
                    case "dmz":
                        sourceSystem = "system-5-dmz";
                }
                break;
            case "noRoute":
                fileName = "123кириллица.pdf";
                switch (system) {
                    case "lan":
                        sourceSystem = "system-3";
                        break;
                    case "dmz":
                        sourceSystem = "system-2-dmz";
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + parameter);
        }
        Response response = RestAssured.given()
                .log().all()
                .header("Content-Type", "multipart/form-data")
                .queryParam("cid", cid)
                .queryParam("originalFilename", fileName)
                .queryParam("sourceSystem", sourceSystem)
                .multiPart("file", new File("src/main/resources/123.txt"), "text/plain")
                .post(url + "put-to-storage")
                .then()
                .extract()
                .response();
        String putToStorageResponse = response.body().asString();
        int putToStorageCode = response.statusCode();
        results.add(0, putToStorageResponse);
        results.add(1, String.valueOf(putToStorageCode));
        log.info("Put To Storage Response: " + putToStorageResponse);
        return results;
    }

    static List<String> putFileNegative(String environment, String system) {
        String url = getURL(environment, system);
        List<String> results = new ArrayList<>();
        String sourceSystem = "";
        String targetSystem = "";
        String cid = String.valueOf(UUID.randomUUID());
        switch (system) {
            case "lan":
                sourceSystem = "system-3";
                targetSystem = "system-4";
                break;
            case "dmz":
                sourceSystem = "system-2-dmz";
                targetSystem = "system-4";
        }
        Response response = RestAssured.given()
                .header("Content-Type", "multipart/form-data")
                .queryParam("cid", cid)
                .queryParam("originalFilename", "123кириллица.pdf")
                .queryParam("sourceSystem", sourceSystem)
                .queryParam("targetSystem", targetSystem)
                .multiPart("file", new File("src/main/resources/123.txt"), "text/plain")
                .post(url + "put-to-storage")
                .then()
                .extract()
                .response();
        String putFileResponse = response.body().asString();
        log.info("Put File Response: " + putFileResponse);
        int putFileCode = response.statusCode();
        results.add(0, putFileResponse);
        results.add(1, String.valueOf(putFileCode));
        return results;
    }

    static List<String> getFileNegative(String environment, String system) throws IOException {
        String url;
        String sourceSystem = "";
        String targetSystem = "system-4";
        String cid = String.valueOf(UUID.randomUUID());
        String requestBody = new String(Files.readAllBytes(Paths.get(paths + "getFile.json")), StandardCharsets.UTF_8);
        url = getURL(environment, system);
        List<String> results = new ArrayList<>();
        switch (system) {
            case "lan":
                sourceSystem = "system-3";
                break;
            case "dmz":
                sourceSystem = "system-2-dmz";
        }
        requestBody = requestBody.replace("${CID}", cid).replace("${SOURCE}", sourceSystem).replace("${TARGET}", targetSystem);
        log.info("body is: " + requestBody);
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(url + "get-file")
                .then()
                .extract()
                .response();
        String getFileResponse = response.body().asString();
        log.info("Get File Response: " + getFileResponse);
        int getFileCode = response.statusCode();
        results.add(0, getFileResponse);
        results.add(1, String.valueOf(getFileCode));
        return results;
    }

    static List<String> getFromStorageNegative(String environment, String system, String parameter) throws IOException {
        String url;
        String targetSystem = "";
        String cid = String.valueOf(UUID.randomUUID());

        url = getURL(environment, system);
        List<String> results = new ArrayList<>();
        String fileName;
        switch (parameter) {
            case "noFile":
                fileName = "getFromStorageNegative.json";
                switch (system) {
                    case "lan":
                        targetSystem = "system-4";
                        break;
                    case "dmz":
                        targetSystem = "system-5-dmz";
                        break;
                }
                break;
            case "noRoute":
                fileName = "getFromStorage.json";
                switch (system) {
                    case "lan":
                        targetSystem = "system-3";
                        break;
                    case "dmz":
                        targetSystem = "system-2-dmz";
                        break;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + parameter);
        }
        String requestBody = new String(Files.readAllBytes(Paths.get(paths + fileName)), StandardCharsets.UTF_8);

        requestBody = requestBody.replace("${CID}", cid).replace("${TARGET}", targetSystem);
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(url + "get-from-storage")
                .then()
                .extract()
                .response();
        String getFromStorageResponse = response.body().asString();
        log.info("Get From Storage Response: " + getFromStorageResponse);
        int getFromStorageCode = response.statusCode();
        results.add(0, getFromStorageResponse);
        results.add(1, String.valueOf(getFromStorageCode));
        return results;
    }

    static String getFileLPU(String environment, String system) throws IOException {
        String url;
        String sourceSystem = "lpu-adapter";
        String targetSystem = "mains";
        String cid = String.valueOf(UUID.randomUUID());
        String requestBody = new String(Files.readAllBytes(Paths.get(paths + "getFile.json")), StandardCharsets.UTF_8);
        url = getURL(environment, system);
        requestBody = requestBody.replace("${CID}", cid).replace("${SOURCE}", sourceSystem).replace("${TARGET}", targetSystem);
        log.info("body is: " + requestBody);
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(url + "get-file")
                .then()
                .extract()
                .response();
        String getFileResponse = response.body().asString();
        log.info("Get File Response: " + getFileResponse);
        return getFileResponse;
    }
}
