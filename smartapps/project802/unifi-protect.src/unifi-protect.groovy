/**
 *  UniFi Protect SmartApp
 *
 *  Copyright 2020 Chris Vincent
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  -----------------------------------------------------------------------------------------------------------------
 * 
 *  For more information, see https://github.com/project802/smartthings/
 */
definition(
    name: "UniFi Protect",
    namespace: "project802",
    author: "Chris Vincent",
    description: "UniFi Protect SmartApp",
    category: "My Apps",
    iconUrl: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr.png",
    iconX2Url: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr_2x.png",
    iconX3Url: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr_3x.png")


preferences {
    input name: "nvrAddress", type: "text", title: "NVR Address", description: "NVR IP address", required: true, displayDuringSetup: true, defaultValue: "192.168.1.160"
    input name: "nvrPort", type: "number", title: "NVR Port", description: "NVR HTTP port", required: true, displayDuringSetup: true, defaultValue: 7080
    input name: "username", type: "text", title: "Username", description: "Username", required: true, displayDuringSetup: true, defaultValue: ""
    input name: "password", type: "text", title: "Password", description: "Password", required: true, displayDuringSetup: true, defaultValue: ""
}

/**
 * installed() - Called by ST platform
 */
def installed() {
    log.info "UniFi Protect: installed with settings: ${settings}"
}

/**
 * updated() - Called by ST platform
 */
def updated() {
    log.info "UniFi Protect: updated with settings: ${settings}"
    
    nvr_initialize()
}

/**
 * nvr_initialize() - Clear state and poll the bootstrap API and the result is handled by nvr_bootstrapPollCallback
 */
def nvr_initialize()
{
    state.nvrName = "Unknown"
    state.apiKey = "";
    
    state.nvrTarget = "${settings.nvrAddress}:${settings.nvrPort}"
    log.info "nvr_initialize: Protect API is located at ${state.nvrTarget}"

    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/api/auth",
            method: "POST",
            HOST: state.nvrTarget,
            body: "{\"username\":\"${settings.username}\",\"password\":\"${settings.password}\"}",
            headers: [
                "Host":"${state.nvrTarget}",
                "Accept":"*/*",
                "Content-Type":"application/json"
            ]        
        ],
        null,
        [
            callback: nvr_loginCallback 
        ]
    );

    sendHubCommand( hubAction );
}

/**
 * nvr_loginCallback() - Callback from hubAction that sends the login API request
 */
def nvr_loginCallback( physicalgraph.device.HubResponse hubResponse )
{
    log.info "nvr_loginCallback: callback received"
    
    if( hubResponse.status != 200 )
    {
        log.error "nvr_loginCallback: unable to login.  Please check IP, port, username and password.  Status ${hubResponse.status}.";
        return;
    }
    
    String authToken = hubResponse?.headers['Authorization'];
    
    if( !authToken )
    {
        log.error "nvr_loginCallback: no headers found for login credentials.  Please check IP, port, username and password.";
        return;
    }
    else
    {
        log.info "nvr_loginCallback: login successful!";
    }
    
    state.apiKey = authToken;
    
    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/api/bootstrap",
            method: "GET",
            HOST: state.nvrTarget,
            headers: [ 
                "Host":"${state.nvrTarget}", 
                "Accept":"application/json", 
                "Content-Type":"application/json",
                "Authorization":"Bearer ${state.apiKey}"
            ]        
        ],
        null,
        [
            callback: nvr_bootstrapPollCallback 
        ]
    );

    sendHubCommand( hubAction );
}

/**
 * nvr_bootstrapPollCallback() - Callback from HubAction with result from GET
 */
def nvr_bootstrapPollCallback( physicalgraph.device.HubResponse hubResponse )
{
    def data = hubResponse.json
    
    log.info "nvr_bootstrapPollCallback: callback received"
    
    if( !data || hubResponse.status != 200 )
    {
    	log.error "nvr_bootstrapPollCallback: unauthorized! HTTP status ${hubResponse.status}"
        return
    }
    
    state.nvrName = data.nvr.name
    log.info "nvr_bootstrapPollCallback: response from \"${state.nvrName}\""
    
    def camerasProcessed = 0
    
    data.cameras.each { camera ->
        def dni = "${camera.mac}"
        def child = getChildDevice( dni )
        
        ++camerasProcessed
        
        if( !camera.isManaged )
        {
            log.info "nvr_bootstrapPollCallback: skipping unmanaged camera \"${camera.name}\""
            return
        }
        
        if( child )
        {
            log.info "nvr_bootstrapPollCallback: already have child ${dni}"
            child.updated()
        }
        else
        {
            def metaData = [   "label" : camera.name + " (" + camera.type + ")",
                               "data": [
                                   "name" : camera.name,
                                   "id" : camera.id
                               ]
                           ]
                           
            log.info "nvr_bootstrapPollCallback: adding child ${dni} ${metaData}"
            
            try
            {
                addChildDevice( "project802", "UniFi Protect Camera", dni, location.hubs[0].id, metaData )
            }
            catch( exception )
            {
                log.error "nvr_bootstrapPollCallback: adding child ${dni} failed (child probably already exists), continuing..."
            }
        }
    }
    
    log.info "nvr_bootstrapPollCallback: processed ${camerasProcessed} cameras"
}

/**
 * _getApiKey() - Here for the purpose of children
 */
def _getApiKey()
{
    return state.apiKey
}
/**
 * _getNvrTarget() - Here for the purpose of children
 */
def _getNvrTarget()
{
    return state.nvrTarget
}