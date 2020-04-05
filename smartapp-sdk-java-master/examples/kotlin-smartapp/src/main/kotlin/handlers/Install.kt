package app.handlers

import com.smartthings.sdk.client.ApiClient
import com.smartthings.sdk.client.methods.SubscriptionsApi
import com.smartthings.sdk.client.models.DeviceSubscriptionDetail
import com.smartthings.sdk.client.models.SubscriptionRequest
import com.smartthings.sdk.client.models.SubscriptionSource
import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.InstallHandler
import com.smartthings.sdk.smartapp.core.models.ConfigEntry
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import com.smartthings.sdk.smartapp.core.models.InstallResponseData


class Install(val api: ApiClient) : InstallHandler {

    override fun handle(request: ExecutionRequest): ExecutionResponse {
        with(request.installData) {
            updateSubscriptions(api, authToken, installedApp)
        }
        // Server expects empty Update response data
        println("Responding to the server")
        return Response.ok(InstallResponseData())
    }
}
