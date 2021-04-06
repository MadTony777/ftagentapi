package ftagentapi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ftagentapi.Requests.*;
import static ftagentapi.Requests.putFile;

public class BaseClass {
    private static String testLANurl = "http://fs-app-lan-tst.vsk.ru:8100/ft-agent-api/";
    private static String stageLANurl = "http://fs-app-lan-stg.vsk.ru:8100/ft-agent-api/";
    private static String testDMZUrl = "http://fs-app-dmz-tst.vsk.ru:8100/ft-agent-api/";
    private static String stageDMZUrl = "http://fs-app-dmz-stg.vsk.ru:8100/ft-agent-api/";
    private static String urldmzSTAGE = "192.168.217.137:9092";
    private static String urllanSTAGE = "192.168.70.143:9092";
    private static String urldmzTEST = "192.168.217.93:9092";
    private static String urllanTEST = "192.168.70.100:9092";
    public static Logger log = LoggerFactory.getLogger(UnitTests.class);
    private String arg = System.getProperty("arg", "test");
    public String environment = arg;

    public static final String paths = "src/test/java/ftagentapi/Examples/";

    public static List<String> getValuesForGivenKey(String jsonArrayStr, String key) {
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        return IntStream.range(0, jsonArray.length())
                .mapToObj(index -> ((JSONObject) jsonArray.get(index)).optString(key))
                .collect(Collectors.toList());
    }
    public static void pauseMethod(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getURL(String environment, String system) {
        String url = null;
        switch (environment){
            case "stage":
                switch (system){
                    case "lan":
                        url= stageLANurl;
                        break;
                    case "dmz":
                        url= stageDMZUrl;
                        break;
                }
                break;
            case "test":
                switch (system){
                    case "lan":
                        url= testLANurl;
                        break;
                    case "dmz":
                        url= testDMZUrl;
                        break;
                }
        }
        return url;
    }


    public static String getKafkaURL(String environment, String source) {
        String service = null;
        switch (environment) {
            case "test":
                switch (source){
                    case "lan":
                        service = urllanTEST;
                        break;
                    case "dmz":
                        service = urldmzTEST;
                        break;
                }
                break;
            case "stage":
                switch (source){
                    case "lan":
                        service = urllanSTAGE;
                        break;
                    case "dmz":
                        service = urldmzSTAGE;
                        break;
                }
        }
        return service;
    }


    public static String getKafkaURL(String environment) {
        String service = null;
        switch (environment) {
            case "test":
                service = urllanTEST;
                break;
            case "stage":
                service = urllanSTAGE;
        }
        return service;
    }


    public static String getMethodBody(String operation, String environment, String system) throws IOException {
        String operationTrace;
        switch (operation){
            case "get-file":
                operationTrace = getFile(environment, system);
                break;
            case "put-file":
                operationTrace = putFile(environment, system);
                break;
            case "get-from-storage":
                operationTrace = getFromStorage(environment, system);
                break;
            case "put-to-storage":
                operationTrace = putToStorage(environment, system);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
        return "[" + operationTrace + "]";
    }

    public static List<String> getMethodBodyNegative(String operation, String environment, String system, String parameter) throws IOException {
        List<String> operationTrace;
        switch (operation){
            case "get-file":
                operationTrace = getFileNegative(environment, system);
                break;
            case "put-file":
                operationTrace = putFileNegative(environment, system);
                break;
            case "get-from-storage":
                operationTrace = getFromStorageNegative(environment, system, parameter);
                break;
            case "put-to-storage":
                operationTrace = putToStorageNegative(environment, system, parameter);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
        String respBody= "[" + operationTrace.get(0) + "]";
        operationTrace.add(2, respBody);
        return operationTrace;
    }

    public static String getMethodBodyLPU(String environment, String system) throws IOException {
        String operationTrace = getFileLPU(environment, system);
        return "[" + operationTrace + "]";
    }
}
