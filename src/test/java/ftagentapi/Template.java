package ftagentapi;

import java.util.List;

import static ftagentapi.Requests.*;
import static ftagentapi.Requests.status;

public class Template extends BaseClass {
    public static String testCase(String environment, String sourceSystem, String targetSystem, String operation) throws Exception {
        String body = getMethodBody(operation, environment, sourceSystem);
        List<String> traceIDTag = getValuesForGivenKey(body, "traceId");
        String traceID = String.valueOf(traceIDTag).replace("[", "").replace("]", "");
        pauseMethod(3000);
        switch (operation) {
            case "get-file":
                kafkaPutFile(environment, sourceSystem, traceID);
                pauseMethod(3000);
                System.out.println("downloadFile start");
                downloadFile(environment, targetSystem, traceID);
                System.out.println("downloadFile finished");
                pauseMethod(3000);
                completeFileTransfer(environment, sourceSystem, traceID);
                break;
            case "get-from-storage":
                downloadFile(environment, sourceSystem, traceID);
                pauseMethod(30000);
                completeFileTransfer(environment, sourceSystem, traceID);
                break;
            case "put-file":
                kafkaReceived(environment, sourceSystem, traceID);
                break;
            case "put-to-storage":
        }
        pauseMethod(3000);
        String status = status(traceID, environment, targetSystem);
        return status;
    }

    public static String testCaseNegative(String environment, String system, String operation, String parameter) throws Exception {
        List<String> response = getMethodBodyNegative(operation, environment, system, parameter);
        List<String> traceIDTag = getValuesForGivenKey(response.get(2), "traceId");
        String traceID = String.valueOf(traceIDTag).replace("[", "").replace("]", "");
        pauseMethod(3000);
        String responseForNegative = null;
        switch (operation) {
            case "get-file":
            case "put-file":
                responseForNegative = response.get(2);
                break;
            case "put-to-storage":
                switch (parameter) {
                    case "noRoute":
                        responseForNegative = response.get(2);
                        break;
                    case "noFile":
                        responseForNegative = response.get(1);
                }
                break;
            case "get-from-storage":
                switch (parameter) {
                    case "noRoute":
                        responseForNegative = response.get(2);
                        break;
                    case "noFile":
                        downloadFileNegative(environment, system, traceID);
                        pauseMethod(3000);
                        responseForNegative = completeFileTransfer(environment, system, traceID);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
        return responseForNegative;
    }

    public static String testCaseLPU(String environment, String sourceSystem, String targetSystem) throws Exception {
        String status;
        String downloadResponce = "";
        String body = getMethodBodyLPU(environment, sourceSystem);
        List<String> traceIDTag = getValuesForGivenKey(body, "traceId");
        String traceID = String.valueOf(traceIDTag).replace("[", "").replace("]", "");
        pauseMethod(3000);
        boolean isStatusCorrect = false;
        long startMs = System.currentTimeMillis();
        while (!isStatusCorrect) {
            if (System.currentTimeMillis() - startMs >= 120000) {
                throw new RuntimeException("Expired; incorrect Status.");
            }
            status = status(traceID, environment, sourceSystem);
            List<String> currentStateResponse = getValuesForGivenKey("[" + status + "]", "currentState");
            String currentState = String.valueOf(currentStateResponse).replace("[", "").replace("]", "");
            if (currentState.equals("SYS_NOTIFY_PUT_SENT")) {
                isStatusCorrect = true;
                System.out.println("downloadFile start");
                downloadResponce = downloadFile(environment, targetSystem, traceID);
                System.out.println("downloadFile finished");
            }
        }
        pauseMethod(3000);
        return downloadResponce;
    }

}
