package ftagentapi;

import org.junit.jupiter.api.Test;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class UnitTests extends Template {
    @Test
    public void putToStorageLAN() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "lan","put-to-storage");
       assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void putToStorageDMZ() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "dmz","put-to-storage");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void getFileLAN() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "lan", "get-file");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void getFileDMZ() throws Exception {
        String responseWithStatus = testCase(environment, "dmz", "lan","get-file");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void putFileLAN() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "lan","put-file");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void putFileDMZ() throws Exception {
        String responseWithStatus = testCase(environment, "dmz", "lan","put-file");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void getFromStorageLAN() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "lan","get-from-storage");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void getFromStorageDMZ() throws Exception {
        String responseWithStatus = testCase(environment, "lan", "dmz","get-from-storage");
        assertThat(responseWithStatus, containsString("COMPLETED"));
    }

    @Test
    public void putToStorageLAN_noFile() throws Exception {
        String responseWithStatus = testCaseNegative(environment, "lan", "put-to-storage", "noFile");
        assertThat(responseWithStatus, containsString("500"));
    }

    @Test
    public void putToStorageLAN_noRoute() throws Exception {
        String response = testCaseNegative(environment, "lan", "put-to-storage", "noRoute");
        assertThat(response, containsString("NoSuchElementException: Route 'system-3' -> 'filestore' not found"));
    }


    @Test
    public void getFromStorageLAN_noFile() throws Exception {
        String response = testCaseNegative(environment, "lan", "get-from-storage", "noFile");
        assertThat(response, containsString("IllegalStateException: Current state for process"));
        assertThat(response, containsString("must be SYS_NOTIFY_PUT_SENT, but actual is SYS_NOTIFY_GET_SENT"));
    }

    @Test
    public void getFromStorageLAN_noRoute() throws Exception {
        String response = testCaseNegative(environment, "lan", "get-from-storage", "noRoute");
        assertThat(response, containsString("NoSuchElementException: Route 'filestore' -> 'system-3' not found"));
    }

    @Test
    public void putFileLAN_noRoute() throws Exception {
        String response = testCaseNegative(environment, "lan", "put-file", "noRoute");
        assertThat(response, containsString("NoSuchElementException: Route 'system-3' -> 'filestore' not found"));
    }


    @Test
    public void getFileLAN_noRoute() throws Exception {
        String response = testCaseNegative(environment, "lan", "get-file", "noRoute");
        assertThat(response, containsString("NoSuchElementException: Route 'system-3' -> 'system-4' not found"));
    }

    @Test
    public void getFile_LPU() throws Exception {
        String response = testCaseLPU(environment, "lan", "dmz");
        assertThat(response, containsString("CLINICS"));
        assertThat(response, containsString("LPUCODE"));
        assertThat(response, containsString("LPUSTATUS"));
    }
}
