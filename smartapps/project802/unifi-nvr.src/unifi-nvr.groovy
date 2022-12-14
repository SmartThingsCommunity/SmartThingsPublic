/**
 *  UniFi NVR SmartApp
 *
 *  Copyright 2016 Chris Vincent
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
 *  For more information, see https://github.com/project802/smartthings/unifi_nvr
 */
definition(
    name: "UniFi NVR",
    namespace: "project802",
    author: "Chris Vincent",
    description: "UniFi NVR SmartApp",
    category: "My Apps",
    iconUrl: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr.png",
    iconX2Url: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr_2x.png",
    iconX3Url: "http://project802.net/smartthings/smartapp-icons/ubiquiti_nvr_3x.png")


preferences {
    input name: "nvrAddress", type: "text", title: "NVR Address", description: "NVR IP address", required: true, displayDuringSetup: true, defaultValue: "10.0.0.205"
    input name: "nvrPort", type: "number", title: "NVR Port", description: "NVR HTTP port", required: true, displayDuringSetup: true, defaultValue: 7080
    input name: "username", type: "text", title: "Username", description: "Username", required: true, displayDuringSetup: true, defaultValue: "test@project802.net"
    input name: "password", type: "text", title: "Password", description: "Password", required: true, displayDuringSetup: true, defaultValue: "unifitest"
}

/**
 * installed() - Called by ST platform
 */
def installed() {
    log.info "UniFi NVR: installed with settings: ${settings}"
}

/**
 * updated() - Called by ST platform
 */
def updated() {
    log.info "UniFi NVR: updated with settings: ${settings}"
    
    nvr_initialize()
}

/**
 * nvr_initialize() - Clear state and poll the bootstrap API and the result is handled by nvr_bootstrapPollCallback
 */
def nvr_initialize()
{
    state.nvrName = "Unknown"
    state.loginCookie = null;
    state.apiKey = "";
    
    state.nvrTarget = "${settings.nvrAddress}:${settings.nvrPort}"
    log.info "nvr_initialize: NVR API is located at ${state.nvrTarget}"

    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/api/2.0/login",
            method: "POST",
            HOST: state.nvrTarget,
            body: "{\"email\":\"${settings.username}\", \"password\":\"${settings.password}\"}",
            headers: [
                "Host":"${state.nvrTarget}",
                "Accept":"application/json",
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
        log.error "nvr_loginCallback: unable to login.  Please check IP, username and password.  Status ${hubResponse.status}.";
        return;
    }
    
    String setCookieHeader = hubResponse?.headers['set-cookie'];
    
    if( !setCookieHeader )
    {
        log.error "nvr_loginCallback: no headers found for login token.  Please check IP, username and password.";
        return;
    }
    
    // JSESSIONID_AV is the login cookie we need to use for other API calls
    def cookies = setCookieHeader.split(";").inject([:]) { cookies, item ->
        def nameAndValue = item.split("=");
        if( nameAndValue[0] == "JSESSIONID_AV" )
        {
            state.loginCookie = nameAndValue[1];
        }
    }
    
    if( !state.loginCookie )
    {
        log.error "nvr_loginCallback: unable to login.  Please check IP, username and password.";
        log.debug "nvr_loginCallback: loginCookie is ${loginCookie}";
        return;
    }
    else
    {
        log.info "nvr_loginCallback: login successful!";
    }
    
    // If there is no API key or its off, the cameras won't work.
    // [todo] add API key validation in SmartApp?
    state.apiKey = hubResponse.json?.data?.apiKey[0];
    
    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/api/2.0/bootstrap",
            method: "GET",
            HOST: state.nvrTarget,
            headers: [ 
                "Host":"${state.nvrTarget}", 
                "Accept":"application/json", 
                "Content-Type":"application/json",
                "Cookie":"JSESSIONID_AV=${state.loginCookie}"
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
    def data = hubResponse.json?.data
    
    if( !data || !data.isLoggedIn )
    {
    	log.error "nvr_bootstrapPollCallback: unable to get data from NVR!"
        return
    }
    
    if( data.isLoggedIn[0] != true )
    {
    	log.error "nvr_bootstrapPollCallback: unable to log in!  Please check API key."
        return
    }
    
    state.nvrName = data.servers[0].name[0]
    log.info "nvr_bootstrapPollCallback: response from ${state.nvrName}"
    
    def camerasProcessed = 0
    
    data.cameras[0].each { camera ->
        def dni = "${camera.mac}"
        def child = getChildDevice( dni )
        
        ++camerasProcessed
        
        if( !camera.managed )
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
            def metaData = [   "label" : camera.name + " (" + camera.model + ")",
                               "data": [
                                   "uuid" : camera.uuid,
                                   "name" : camera.name,
                                   // 1st generation: doesn't enumerate the 'camera.platform', uses camera.uuid for API calls
                                   // 2nd generation: camera.platform = "GEN2", uses camera._id for API calls
                                   // 3rd generation: camera.platform = "GEN3L", uses camera._id for API calls
                                   "id" : camera.platform ? camera._id : camera.uuid
                               ]
                           ]
                           
            log.info "nvr_bootstrapPollCallback: adding child ${dni} ${metaData}"
            
            try
            {
                addChildDevice( "project802", "UniFi NVR Camera", dni, location.hubs[0].id, metaData )
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
