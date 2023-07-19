/**
 *  Plex Manager
 *
 *  Copyright 2016 iBeech
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
 * 
 * 	===========INSTRUCTIONS===========
        1) For UK go to: https://graph-eu01-euwest1.api.smartthings.com3
        2) For US go to: https://graph.api.smartthings.com1
        3) Click 'My SmartApps'
        4) Click the 'From Code' tab
        5) Paste in the code from: https://github.com/iBeech/SmartThings/blob/master/PlexManager/PlexManager.groovy
        6) Click 'Create'
        7) Click 'Publish -> For Me'
 * 
 */

definition(
    name: "Plex Manager",
    namespace: "ibeech",
    author: "ibeech",
    description: "Add and Manage Plex Home Theatre endpoints",
    category: "Safety & Security",
    iconUrl: "http://download.easyicon.net/png/1126483/64/",
    iconX2Url: "http://download.easyicon.net/png/1126483/128/",
    iconX3Url: "http://download.easyicon.net/png/1126483/128/")

preferences {
    page(name: "startPage")
    page(name: "authPage")
    page(name: "clientPage")
}

def startPage() {
    if (state?.authenticationToken) { return clientPage() }
    else { return authPage() }
}

/* Auth Page */
def authPage() {
    return dynamicPage(name: "authPage", nextPage: clientPage, install: false) {
        section("Plex Media Server") {
            input "plexUserName", "text", "title": "Plex Username", multiple: false, required: true
            input "plexPassword", "password", "title": "Plex Password", multiple: false, required: true
            input "plexServerIP", "text", "title": "Server IP", multiple: false, required: true
            input "theHub", "hub", title: "On which hub?", multiple: false, required: true
        }
    }
}

def clientPage() {
    if (!state.authenticationToken) { getAuthenticationToken() }
    def showUninstall = state.appInstalled
    def devs = getClientList()
    return dynamicPage(name: "clientPage", uninstall: true, install: true) {
        section("Client Selection Page") {
            input "selectedClients", "enum", title: "Select Your Clients...", options: devs, multiple: true, required: false, submitOnChange: true
            href "authPage", title:"Go Back to Auth Page", description: "Tap to edit..."
            input "pollEnable", "bool", title: "Enable Polling", defaultValue: "true", submitOnChange: true
            input "showAllDevs", "bool", title: "Show All Devices Regardless of Capability", defaultValue: "false", submitOnChange: true

        }
    }
}

def clientListOpt() {
    getClientList().collect{[(it.key): it.value]}
}

def getClientList() {
    def devs = [:]
    log.debug "Executing 'getClientList'"

    def params = [
        uri: "https://plex.tv/devices.xml",
        contentType: 'application/xml',
        headers: [
            'X-Plex-Token': state.authenticationToken
        ]
    ]

    // GET 3rd level IP of Plex server

    def plexServerIPShort = settings.plexServerIP.substring(0 , plexServerIP.lastIndexOf("."))

    httpGet(params) { resp ->
        log.debug "Parsing plex.tv/devices.xml"
        def devices = resp.data.Device

        def deviceNames = []
        devices.each { thing ->

            def capabilities = thing.@provides.text()

            // If these capabilities
            if(capabilities.contains("player")||capabilities.contains("client")||settings.showAllDevs){ 

                //Define name based on name unless blank then use device name
                def whatToCallMe = "${thing.@name.text()}"
                if("${thing.@name.text()}"==""){whatToCallMe = "${thing.@device.text()}"}

                // Create alternative name if same name   
                def tempName = whatToCallMe
                for (int i = 2; i < 100; i++) {
                    if(deviceNames.contains(tempName)){
                        tempName = "${whatToCallMe} #$i"
                    }else{
                        whatToCallMe = tempName
                        break
                    }
                }

                deviceNames << whatToCallMe

                def addressVal = "0.0.0.0"

                // Get IP Address for those with an IP in the same range as your Plex Server if connection IP available (will only return a single entry for the local device)
                thing.Connection.each { con ->

                    def uri = con.@uri.text()
                    def address = (uri =~ 'https?://([^:]+)')[0][1]

                    //Check if IP on same range
                    if(plexServerIPShort == address.substring(0 , address.lastIndexOf("."))){
                        addressVal = address
                    }        
                }

                // Add to list
                if(devs.findIndexValues { it =~ /${thing.@clientIdentifier.text()}/ } == []){
                    devs << ["${whatToCallMe}|${thing.@clientIdentifier.text()}|${addressVal}": whatToCallMe as String]
                } 
            }
        }   
    }
    return devs.sort { a, b -> a.value.toLowerCase() <=> b.value.toLowerCase() }
}

def installed() {
    state.appInstalled = true
    log.debug "Installed with settings: ${settings}"
    initialize()   
}

def initialize() {

    if (pollEnable) {
        runEvery1Minute(regularPolling)
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()

    if(selectedClients) {

        selectedClients.each { client ->
            def item = client.tokenize('|')
            def name = item[0]
            def address = item[1]
            def uniqueIdentifier = item[2]

            updatePHT(name, address, uniqueIdentifier);
        }
    }


    if (!state.authenticationToken) {
        getAuthenticationToken()
    }

    initialize()

    subscribe(location, null, response, [filterEvents:false])   

}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {

    try {
        delete.each {
            deleteChildDevice(it.deviceNetworkId)
            log.info "Successfully Removed Child Device: ${it.displayName} (${it.deviceNetworkId})"
        }
    }
    catch (e) { log.error "There was an error (${e}) when trying to delete the child device" }
}

def response(evt) {	 

    log.trace "in response(evt)";

    def msg = parseLanMessage(evt.description);
    if(msg && msg.body && msg.body.startsWith("<?xml")){

        def mediaContainer = new XmlSlurper().parseText(msg.body)

        log.debug "Parsing /status/sessions"
        getChildDevices().each { pht ->

            log.debug "Checking $pht for updates"

            // Convert the devices full network id to just the IP address of the device
            //def address = getPHTAddress(pht.deviceNetworkId);
            def identifier = getPHTIdentifier(pht.deviceNetworkId);

            // Look at all the current content playing, and determine if anything is playing on this device
            def currentPlayback = mediaContainer.Video.find { d -> d.Player.@machineIdentifier.text() == identifier }

            // If there is no content playing on this device, then the state is stopped
            def playbackState = "stopped";            

            // If we found active content on this device, look up its current state (i.e. playing or paused)
            if(currentPlayback) {

                playbackState = currentPlayback.Player.@state.text();
            }            

            log.trace "Determined that $pht is: " + playbackState

            pht.setPlaybackState(playbackState);

            log.trace "Current playback type:" + currentPlayback.@type.text()
            pht.playbackType(currentPlayback.@type.text())
            switch(currentPlayback.@type.text()) {
                case "movie":
                pht.setPlaybackTitle(currentPlayback.@title.text());
                break;

                case "":
                pht.setPlaybackTitle("...");
                break;

                case "clip":
                pht.setPlaybackTitle("Trailer");
                break;  

                case "episode":
                pht.setPlaybackTitle(currentPlayback.@grandparentTitle.text() + ": " + currentPlayback.@title.text());
            }
        }

    }
}

def updatePHT(phtName, phtIP, phtIdentifier){

    if(phtName && phtIP && phtIdentifier) { 

        log.info "Updating PHT: " + phtName + " with IP: " + phtIP + " and machine identifier: " + phtIdentifier

        def children = getChildDevices()
        def child_deviceNetworkID = childDeviceID(phtIP, phtIdentifier);

        def pht = children.find{ d -> d.deviceNetworkId.contains(phtIP) }  

        if(!pht){ 
            // The PHT does not exist, create it
            log.debug "This PHT does not exist, creating a new one now"
            pht = addChildDevice("ibeech", "Plex Home Theatre", child_deviceNetworkID, theHub.id, [label:phtName, name:phtName])		
        } else {

            // Update the network device ID
            if(pht.deviceNetworkId != child_deviceNetworkID) {
                log.trace "Updating this devices network ID, so that it is consistant"
                pht.deviceNetworkId = childDeviceID(phtIP, phtIdentifier);
            }
        }

        // Renew the subscription
        subscribe(pht, "switch", switchChange)
    }
}

def String childDeviceID(phtIP, identifier) {

    def id = "pht." + settings.plexServerIP + "." + phtIP + "." + identifier
    //log.trace "childDeviceID: $id";
    return id;
}
def String getPHTAddress(deviceNetworkId) {

    def parts = deviceNetworkId.tokenize('.');
    def part = parts[6] + "." + parts[7] + "." + parts[8] + "." + parts[9];
    //log.trace "PHTAddress: $part"

    return part;
}
def String getPHTIdentifier(deviceNetworkId) {

    def parts = deviceNetworkId.tokenize('.');
    def part = parts[5];    
    //log.trace "PHTIdentifier: $part"

    return part;
}
def String getPHTCommand(deviceNetworkId) {

    def parts = deviceNetworkId.tokenize('.');
    def part = parts[10];
    //log.trace "PHTCommand: $part"

    return part
}
def String getPHTAttribute(deviceNetworkId) {

    def parts = deviceNetworkId.tokenize('.');
    def part = parts[11];
    //log.trace "PHTAttribute: $part"

    return parts[11];
}

def switchChange(evt) {

    // We are only interested in event data which contains 
    if(evt.value == "on" || evt.value == "off") return;   

    log.debug "Plex Home Theatre event received: " + evt.value;

    def parts = evt.value.tokenize('.');

    // Parse out the PHT IP address from the event data
    def phtIP = getPHTAddress(evt.value);

    // Parse out the new switch state from the event data
    def command = getPHTCommand(evt.value);

    //log.debug "phtIP: " + phtIP
    log.debug "Command: $command"

    switch(command) {
        case "next":
        log.debug "Sending command 'next' to $phtIP"
        next(phtIP);
        break;

        case "previous":
        log.debug "Sending command 'previous' to $phtIP"
        previous(phtIP);
        break;

        case "play":
        case "pause":
        // Toggle the play / pause button for this PHT
        playpause(phtIP);
        break;

        case "stop":            
        stop(phtIP);
        break;

        case "scanNewClients":
        getClients();

        case "setVolume":
        setVolume(phtIP, getPHTAttribute(evt.value));
        break;
    }

    return;
}

def setVolume(phtIP, level) {
    log.debug "Executing 'setVolume'"

    executeRequest("/system/players/$phtIP/playback/setParameters?volume=$level", "GET");
}

def regularPolling() { 


    log.debug "Polling for PHT state"

    if(state.authenticationToken) {
        updateClientStatus();
    }

}

def updateClientStatus(){
    log.debug "Executing 'updateClientStatus'"

    executeRequest("/status/sessions", "GET")
}

def playpause(phtIP) {
    log.debug "Executing 'playpause'"

    executeRequest("/system/players/" + phtIP + "/playback/play", "GET");
}

def stop(phtIP) {
    log.debug "Executing 'stop'"

    executeRequest("/system/players/" + phtIP + "/playback/stop", "GET");
}

def next(phtIP) {
    log.debug "Executing 'next'"

    executeRequest("/system/players/" + phtIP + "/playback/skipNext", "GET");
}

def previous(phtIP) {
    log.debug "Executing 'next'"

    executeRequest("/system/players/" + phtIP + "/playback/skipPrevious", "GET");
}

def executeRequest(Path, method) {

    log.debug "The " + method + " path is: " + Path;

    // We don't have an authentication token
    if(!state.authenticationToken) {
        getAuthenticationToken()
    }

    def headers = [:] 
    headers.put("HOST", "$settings.plexServerIP:32400")
    headers.put("X-Plex-Token", state.authenticationToken)

    try {    
        def actualAction = new physicalgraph.device.HubAction(
            method: method,
            path: Path,
            headers: headers)

        sendHubCommand(actualAction)        
    }
    catch (Exception e) {
        log.debug "Hit Exception $e on $hubAction"
    }
}

def getAuthenticationToken() {

    log.debug "Getting authentication token for Plex Server " + settings.plexServerIP      

    def params = [
        uri: "https://plex.tv/users/sign_in.json?user%5Blogin%5D=" + settings.plexUserName + "&user%5Bpassword%5D=" + URLEncoder.encode(settings.plexPassword),
        headers: [
            'X-Plex-Client-Identifier': 'Plex',
            'X-Plex-Product': 'Device',
            'X-Plex-Version': '1.0'
        ]
    ]

    try {    
        httpPostJson(params) { resp ->
            state.tokenUserName = settings.plexUserName            
            state.authenticationToken = resp.data.user.authentication_token;
            log.debug "Token is: " + state.authenticationToken
        }
    }
    catch (Exception e) {
        log.debug "Hit Exception $e on $params"
    }
}

/* Helper functions to get the network device ID */
private String NetworkDeviceId(){
    def iphex = convertIPtoHex(settings.piIP).toUpperCase()
    def porthex = convertPortToHex(settings.piPort)
    return "$iphex:$porthex" 
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}