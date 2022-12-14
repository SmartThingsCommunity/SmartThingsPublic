/**
 *  UniFi Protect Camera
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
metadata {
    definition (name: "UniFi Protect Camera", namespace: "project802", author: "Chris Vincent") {
        capability "Motion Sensor"
        capability "Sensor"
        capability "Refresh"
        capability "Image Capture"
    }
    
    simulator {
        
    }
    
    tiles( scale: 2 ) {
        carouselTile("cameraSnapshot", "device.image", width: 6, height: 4) { }
        
        standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
        }
        
        standardTile("motion", "device.motion", width: 2, height: 2) {
            state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
            state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
        }
        
        standardTile( "connectionStatus", "device.connectionStatus", width: 2, height: 2 ) {
            state( "CONNECTED", label: "Connected", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#79b821" )
            state( "DISCONNECTED", label: "Disconnected", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#ffffff" )
        }
        
        main( "motion" )
        details( "cameraSnapshot", "take", "motion", "connectionStatus" )
    }
    
    preferences {
        input "pollInterval", "number", title: "Poll Interval", description: "Polling interval in seconds for motion detection", defaultValue: 5
        input "snapOnMotion", "bool", title: "Snapshot on motion", description: "If enabled, take a snapshot when the camera detects motion", defaultValue: false
    }
}

/**
 * installed()
 *
 * Called by ST platform.
 */
def installed()
{
    updated()
}

/**
 * updated()
 *
 * Called by ST platform.
 */
def updated()
{
    // Unschedule here to remove any zombie runIn calls that the platform
    // seems to keep around even if the code changes during dev
    unschedule()
    
    state.name                   = getDataValue( "name" )
    state.id                     = getDataValue( "id" )
    state.lastMotion             = null
    state.motion                 = "uninitialized"
    state.connectionStatus       = "uninitialized"
    state.pollInterval           = settings.pollInterval ? settings.pollInterval : 5
    state.pollIsActive           = false
    state.successiveApiFails     = 0
    state.lastPoll               = new Date().time
    
    log.info "${device.displayName} updated with state: ${state}"
    
    runEvery1Minute( nvr_cameraPollWatchdog )
    
    nvr_cameraPoll()
}

/**
 * refresh()
 * 
 * Called by ST platform, part of "Refresh" capability.  Usually only called when the user explicitly
 * refreshes the device details pane.
 */
def refresh()
{
    _sendMotion( state.motion )
    _sendConnectionStatus( state.connectionStatus )
}

/**
 * take()
 *
 * Called by ST platform, part of "Image Capture" capability.
 */
def take()
{
    def key = parent._getApiKey()
    def target = parent._getNvrTarget()
    
    if( state.connectionStatus == "CONNECTED" )
    {
        def hubAction = new physicalgraph.device.HubAction(
            [
                path: "/api/cameras/${state.id}/snapshot/?w=640&h=360",
                method: "GET",
                HOST: target,
                headers: [
                    "Host":"${target}",
                    "Accept":"*/*",
                    "Authorization":"Bearer ${key}"
                ]        
            ],
            null,
            [
                outputMsgToS3: true,
                callback: nvr_cameraTakeCallback 
            ]
        );
    
        sendHubCommand( hubAction )
    }
}

/**
 * nvr_cameraTakeCallback()
 *
 * Callback from the take() HubAction, results are in S3.
 */
def nvr_cameraTakeCallback( physicalgraph.device.HubResponse hubResponse )
{
    //log.debug( "nvr_cameraTakeCallback: ${hubResponse.description}" )
    
    def descriptionMap = stringToMap( hubResponse.description )
    
    if( descriptionMap?.tempImageKey )
    {
        try
        {
            storeTemporaryImage( descriptionMap.tempImageKey, java.util.UUID.randomUUID().toString().replaceAll('-', '') )
        }
        catch( Exception e )
        {
            log.error e
        }
    }
    else
    {
        log.error "API for camera take() FAILED: ${descriptionMap.error}"
    }
}

/**
 * nvr_cameraPollWatchdog()
 * 
 * Uses a different scheduling method to watch for failures with the runIn method used by nvr_cameraPoll
 */
def nvr_cameraPollWatchdog()
{
    def now = new Date().time
    
    def elapsed = Math.floor( (now - state.lastPoll) / 1000 )
    
    //log.debug "nvr_cameraPollWatchdog: it has been ${elapsed} seconds since a poll for ${device.displayName}"
    
    if( elapsed > (state.pollInterval * 5) )
    {
        log.error "nvr_cameraPollWatchdog: expired for ${device.displayName}!"
        nvr_cameraPoll()
    }
}

/**
 * nvr_cameraPoll()
 *
 * Once called, starts cyclic call to itself periodically.  Main loop of the device handler to make API
 * to the NVR software to see if motion has changed.  API call result is handled by nvr_cameraPollCallback().
 */
def nvr_cameraPoll() 
{
    def key = parent._getApiKey()
    def target = parent._getNvrTarget()
    
    if( state.pollIsActive )
    {
        log.error "nvr_cameraPoll() - ${device.displayName} failed to return API call to NVR"
        
        ++state.successiveApiFails
        
        if( (state.connectionStatus == "CONNECTED") && (state.successiveApiFails > 5) )
        {
            log.error "nvr_cameraPoll() - ${device.displayName} has excessive consecutive failed API calls, forcing disconnect status"
            state.connectionStatus = "DISCONNECTED"
            _sendConnectionStatus( "${state.connectionStatus}" )
        }
    }
    else
    {
        state.successiveApiFails = 0;
    }
    
    state.pollIsActive = true
    
    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/api/cameras/${state.id}",
            method: "GET",
            HOST: target,
            headers: [
                "Host":"${target}",
                "Accept":"application/json",
                "Authorization":"Bearer ${key}"
            ]        
        ],
        null,
        [
            callback: nvr_cameraPollCallback 
        ]
    );
    
    sendHubCommand( hubAction );
    
    // Set overwrite to true instead of using unschedule(), which is expensive, to ensure no dups
    runIn( state.pollInterval, nvr_cameraPoll, [overwrite: true] )
}

/**
 * nvr_cameraPollCallback()
 *
 * Callback from HubAction with result from GET.
 */
def nvr_cameraPollCallback( physicalgraph.device.HubResponse hubResponse )
{
    def motion = "inactive"
    def data = hubResponse?.json
    
    if( hubResponse.status != 200 )
    {
    	log.error "nvr_cameraPollCallback: unauthorized!  Camera = \"${device.displayName}\""
        return;
    }
    
    //log.debug "nvr_cameraPollCallback: ${device.displayName}"
    
    if( !data )
    {
        log.error "nvr_cameraPollCallback: no data returned";
        return;
    }
    
    state.pollIsActive = false;
    state.lastPoll = new Date().time
    
    if( data.state != state.connectionStatus )
    {
        state.connectionStatus = data.state
        _sendConnectionStatus( "${state.connectionStatus}" )
    }
    
    // Only do motion detection if the camera is connected and configured for it
    if( (state.connectionStatus == "CONNECTED") )
    {
    	// Motion is based on a new recording being present
    	if( state.lastMotion && (state.lastMotion != data.lastMotion) )
        {
            motion = "active"
        }
        
        state.lastMotion = data.lastMotion;
    }
    else
    {
        log.warn "nvr_cameraPollCallback: ${device.displayName} camera disconnected"
    }
    
    // Fall-through takes care of case if camera motion was active but became disconnected before becoming inactive
    if( motion != state.motion )
    {
        // Send motion before doing the take to prioritize the reaction time to motion.  It isn't clear what
        // the ST platform does for scheduling or blocking in either the sendEvent or sendHubCommand calls.
        state.motion = motion
        _sendMotion( motion )
        
        if( snapOnMotion && (motion == "active") )
        {
            take()
        }
    }
}

/**
 * _sendMotion()
 *
 * Sends a motion event to the ST platform.
 *
 * @arg motion Either "active" or "inactive"
 */
private _sendMotion( motion )
{
    if( (motion != "active") && (motion != "inactive") )
    {
    	return
    }
    
    //log.debug( "_sendMotion( ${motion} )" )
    
    def description = (motion == "active" ? " detected motion" : " motion has stopped")
    
    def map = [ 
                name: "motion",
                value: motion, 
                descriptionText: device.displayName + description
              ]
    
    sendEvent( map )
}

/**
 * _sendConnectionStatus()
 *
 * Sends a connection status event to the ST platform.
 *
 * @arg motion Either "CONNECTED" or "DISCONNECTED"
 */
private _sendConnectionStatus( connectionStatus )
{
    if( (connectionStatus != "CONNECTED") && (connectionStatus != "DISCONNECTED") )
    {
        return
    }
    
    //log.debug "_sendConnectionStatus: ${device.displayName} ${connectionStatus}"
    
    def map = [
                name: "connectionStatus",
                value: connectionStatus,
                descriptionText: device.displayName + " is ${connectionStatus}"
              ]
              
    sendEvent( map )
}