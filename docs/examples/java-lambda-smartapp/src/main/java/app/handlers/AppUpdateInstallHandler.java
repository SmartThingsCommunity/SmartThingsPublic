package app.handlers;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.client.ApiClient;
import com.smartthings.sdk.client.methods.SubscriptionsApi;
import com.smartthings.sdk.client.models.DeviceSubscriptionDetail;
import com.smartthings.sdk.client.models.Subscription;
import com.smartthings.sdk.client.models.SubscriptionRequest;
import com.smartthings.sdk.client.models.SubscriptionSource;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.InstallHandler;
import com.smartthings.sdk.smartapp.core.extensions.UpdateHandler;
import com.smartthings.sdk.smartapp.core.models.ConfigEntries;
import com.smartthings.sdk.smartapp.core.models.ConfigEntry;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;
import com.smartthings.sdk.smartapp.core.models.InstallData;
import com.smartthings.sdk.smartapp.core.models.InstallResponseData;
import com.smartthings.sdk.smartapp.core.models.InstalledApp;
import com.smartthings.sdk.smartapp.core.models.UpdateData;
import com.smartthings.sdk.smartapp.core.models.UpdateResponseData;


/**
 * Implementations of both InstallHandler and UpdateHandler. These two handlers share
 * so much in common, we just implement them together here.
 */
public class AppUpdateInstallHandler {
    private abstract static class UpdateInstallHandler {
        private final Logger LOG = LoggerFactory.getLogger(this.getClass());

        private final ApiClient api = new ApiClient();

        public void handleRequest(ExecutionRequest request, String authToken, InstalledApp installedApp) throws Exception {
            LOG.debug(request.getLifecycle().name() + ": executionRequest = " + request);

            SubscriptionsApi subscriptionsApi = api.buildClient(SubscriptionsApi.class);
            String auth = "Bearer " + authToken;

            subscriptionsApi.deleteAllSubscriptions(installedApp.getInstalledAppId(), auth, Collections.emptyMap());

            // Iterate over devices returned in the InstalledApp's config for key "selectedSwitches"
            // (defined in AppConfigurationHandler).
            ConfigEntries deviceConfigs = installedApp.getConfig().get("selectedSwitches");
            for (ConfigEntry deviceConfig: deviceConfigs) {
                if (deviceConfig.getValueType() == ConfigEntry.ValueTypeEnum.DEVICE) {
                    DeviceSubscriptionDetail device = new DeviceSubscriptionDetail()
                        .deviceId(deviceConfig.getDeviceConfig().getDeviceId())
                        .componentId(deviceConfig.getDeviceConfig().getComponentId())
                        .capability("switch")
                        .attribute("switch")
                        .stateChangeOnly(true)
                        .value("*");
                    SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
                        .sourceType(SubscriptionSource.DEVICE)
                        .device(device);

                    // Create subscription for the device
                    Subscription subscription = subscriptionsApi.saveSubscription(installedApp.getInstalledAppId(), auth, subscriptionRequest);
                    LOG.info("Created subscription " + subscription.getId());
                }
            }
        }
    }

    public static class AppInstallHandler extends UpdateInstallHandler implements InstallHandler {
        @Override
        public ExecutionResponse handle(ExecutionRequest request) throws Exception {
            InstallData installData = request.getInstallData();
            handleRequest(request, installData.getAuthToken(), installData.getInstalledApp());
            return Response.ok(new InstallResponseData());
        }
    }

    public static class AppUpdateHandler extends UpdateInstallHandler implements UpdateHandler {
        @Override
        public ExecutionResponse handle(ExecutionRequest request) throws Exception {
            UpdateData updateData = request.getUpdateData();
            handleRequest(request, updateData.getAuthToken(), updateData.getInstalledApp());
            return Response.ok(new UpdateResponseData());
        }
    }
}
