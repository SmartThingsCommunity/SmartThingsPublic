package app.service;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.smartthings.sdk.client.ApiClient;
import com.smartthings.sdk.client.methods.DevicesApi;
import com.smartthings.sdk.client.models.DeviceCommand;
import com.smartthings.sdk.client.models.DeviceCommandsRequest;
import com.smartthings.sdk.smartapp.core.extensions.contextstore.DefaultInstalledAppContextStore;
import com.smartthings.sdk.smartapp.core.models.ConfigEntries;
import com.smartthings.sdk.smartapp.core.models.ConfigEntry;
import com.smartthings.sdk.smartapp.core.models.DeviceConfig;


/**
 * A contrived example service that needs to make out-of-band requests.
 */
@Service
public class StartDayService {
    private final static DeviceCommandsRequest DEVICE_ON;
    static {
        DeviceCommand command = new DeviceCommand()
            .capability("switch").command("on").arguments(Collections.emptyList());
        DEVICE_ON = new DeviceCommandsRequest().addCommandsItem(command);
    }

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DefaultInstalledAppContextStore installedAppContextStore;
    private final ApiClient apiClient;

    public StartDayService(DefaultInstalledAppContextStore installedAppContextStore, ApiClient apiClient) {
        this.installedAppContextStore = installedAppContextStore;
        this.apiClient = apiClient;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void goodMorning() {
        DevicesApi devicesApi = apiClient.buildClient(DevicesApi.class);
        log.info("waking up");
        installedAppContextStore.get().forEach(context -> {
            log.info("context = " + context);
            ConfigEntries selectedSwitches = context.getInstalledApp().getConfig().get("selectedSwitches");
            for (ConfigEntry entry : selectedSwitches) {
                DeviceConfig deviceConfig = entry.getDeviceConfig();
                if (deviceConfig != null) {
                    String auth = context.getAuth();
                    devicesApi.executeDeviceCommands(auth, deviceConfig.getDeviceId(), DEVICE_ON);
                }
            }
        });
    }
}
