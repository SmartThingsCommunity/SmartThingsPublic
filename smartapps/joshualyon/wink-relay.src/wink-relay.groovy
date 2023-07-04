/**
 *  Wink Relay - SmartApp
 *
 *  Copyright 2017 Josh Lyon
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
 */
definition(
        name: "Wink Relay",
        namespace: "joshualyon",
        author: "Josh Lyon",
        description: "SmartApp for adding Wink Relay devices running the STWinkRelay custom app",
        category: "SmartThings Labs",
        iconUrl: "https://assets.ifttt.com/images/channels/423083547/icons/on_color_large.png",
        iconX2Url: "https://assets.ifttt.com/images/channels/423083547/icons/on_color_large.png",
        iconX3Url: "https://assets.ifttt.com/images/channels/423083547/icons/on_color_large.png")


preferences {
    page(name: "deviceDiscovery", title: "Wink Relay Device Setup", content: "deviceDiscovery")
    page(name: "manualAdd", title: "Manually Add a Device", content: "manualAdd")
    page(name: "manualValidation", title: "Validation of manually added device", content: "manualValidation")
}

def getSearchTarget(){
    return "urn:sharptools-io:device:WinkRelay:1";
}

def deviceDiscovery() {
    log.debug "╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
    
    def options = [:]
    def devices = getVerifiedDevices()
    devices.each {
        def value = "Wink Relay ${it.value.ssdpUSN.split(':')[1][-3..-1]}" //it.value.name ?: "Default"
        def key = it.value.mac
        options["${key}"] = value
        log.debug "║ ★ ${it.value.ssdpUSN} @ ${it.value.networkAddress}:${it.value.deviceAddress} (${it.value.mac})"
    }
    if(devices.size() == 0)
    	log.debug "║ [no devices are verified]"
    log.debug "║ Verified devices: "
    log.debug "║ "
    
    ssdpSubscribe()
    //subscribeNetworkEvents()
    ssdpDiscover()
    verifyDevices()
    
	log.debug "╔════PAGE: DEVICE DISCOVERY════════════════════════════════════════════════════════════════════════════"
	
    return dynamicPage(name: "deviceDiscovery", title: "Discovery Started!", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
        section("Please wait while we discover your Wink Relay Device. Please make sure you have installed the custom STWinkRelay app and have started the app at least once. \r\n\r\nDiscovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
            input "selectedDevices", "enum", required: false, title: "Select Devices (${options.size() ?: 0} found)", multiple: true, options: options
            href(name: "customDevice",
                 title: "Manually add by IP/host",
                 required: false,
                 page: "manualAdd",
                 description: "Click to continue to manual entry")
        }
    }
}

def manualAdd(){
	state.manualValidationInProgress = false; //reset the manual validation
    state.manualDevice = [] //clear the existing state for the manual device.
    
	//manualDevice = "" //reset to default value
	return dynamicPage(name: "manualAdd", title: "Manual Addition", nextPage: "manualValidation", install: false, uninstall: false) {
        section("Wink Relay IP address or host name") {
            input(name: "manualDevice", type: "text",  required: true, multiple: false, title: "IP / Hostname")
        }
    }
}

def manualValidation(){
	def canInstall = true //if this is false, the Save button just goes back one back
    def manualValidationStatus = "Starting validation of ${manualDevice}"
    
	//---validate that the IP or hostname is OK---
    //send a request to the webserver (on port 8080) to get the device.xml
    if(state.manualValidationInProgress == null || state.manualValidationInProgress == false){
    	log.debug "SENDING DEVICE.XML REQUEST"
        def host = manualDevice + ":8080"
        state.manualValidationInProgress = true
        log.debug "Sending command to ${host}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET /device.xml HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: manualValidationXmlCallback]))
    }
    else{
    	//if the request is in progress, the callback will parse the XML for use in creating the device
        //log.debug state.manualDevice
        if(state.manualDevice){
        	log.debug "We have verified ${manualDevice} manually!"
            //if we have a manualDevice object created, let's show the user the results and let them install
            canInstall = true
            //TODO: test pingback capabilities (can the Wink Relay send events to the hub?)
            log.debug "Can install: ${canInstall}"

            //then show the results (can we complete the install programatically?)
            manualValidationStatus = "Successfully validated ${manualDevice}.\r\n\r\nClick save to create the new device!"
        }
        else{
            	log.debug "DEVICE.XML request is in progress, but not completed"
        }
    }
    
    return dynamicPage(name: "manualValidation", title: "Manual Validation", refreshInterval: 3, nextPage: "", install: canInstall, uninstall: false) {
        section("") {
            paragraph(manualValidationStatus)
        }
    }
}

void manualValidationXmlCallback(physicalgraph.device.HubResponse hubResponse) {
	log.debug "---╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
    //log.debug hubResponse
    def body = hubResponse.xml
    //get the ID and store it in state for final addition at the end
    int port = convertHexToInt(hubResponse.port)
    String ip = convertHexToIP(hubResponse.ip)
    String host = "${ip}:${port}"
    String mac = hubResponse.mac
    String udn = body?.device?.UDN
    String hub = hubResponse.hubId
    log.debug "---║ ★ ${udn} @ ${ip}:${port} (${mac})"
    
    //USN = body.device.UDN uuid:2f9e00f8cbd2b6d8        
    //ip: ip, port: port,
    state.manualDevice = [mac: mac, hub: hub, networkAddress: hubResponse?.ip, deviceAddress: hubResponse?.port, ssdpUSN: udn, name: body?.device?.friendlyName?.text(), model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
    log.debug "---╔════MANUAL DEVICE CALLBACK═════════════════════════════════════════════════════════════════════════"
}

def addManualDevice(){
	//add the device
    def device = state.manualDevice
    def d 
    if(device){
        d = getChildDevices()?.find {
            it.deviceNetworkId == device.mac
        }
    }
    
    if(!d){
    	log.debug "Manually creating Wink Relay Device with dni: ${device.mac}"
        addChildDevice("joshualyon", "Wink Relay", device.mac, device.hub, [
            "label": device.name ?: "Wink Relay",
            "data": [
                "mac": device.mac,
                "ip": device.networkAddress,
                "port": device.deviceAddress
            ]
        ])
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    unsubscribe()
    unschedule()

    ssdpSubscribe()
    //subscribeNetworkEvents()

    if (selectedDevices) {
    	log.debug "Adding selected devices from SSDP discovery..."
        addDevices()
    }
    
    if(state.manualDevice){
    	log.debug "Adding manually entered devices..."
    	addManualDevice()
    }

    runEvery5Minutes("ssdpDiscover")
    log.debug "Done with initialize."
}

void ssdpDiscover() {
    log.debug "║ 2. Searching for ${searchTarget}"
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${searchTarget}", physicalgraph.device.Protocol.LAN))
}


void ssdpSubscribe() {
	log.debug "║ 1. Subscribing to events: ssdpTerm.${searchTarget}"
    subscribe(location, "ssdpTerm.${searchTarget}", ssdpHandler)
}


private subscribeNetworkEvents(force=false) {
	log.debug "║ 1. Subscribing to ALL LOCATION events."
    if (force) {
        unsubscribe()
        state.subscribe = false
    }

    if(!state.subscribe) {
        subscribe(location, null, ssdpHandler, [filterEvents:false])
        state.subscribe = true
    }
}

Map verifiedDevices() {
    def devices = getVerifiedDevices()
    def map = [:]
    devices.each {
        def value = it.value.name ?: "Wink Relay ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
        def key = it.value.mac
        map["${key}"] = value
    }
    map
}

void verifyDevices() {
	log.debug "║ 3. Verifying all devices which are not yet verified..."
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        int port = convertHexToInt(it.value.deviceAddress)
        String ip = convertHexToIP(it.value.networkAddress)
        String host = "${ip}:${port}"
        log.debug "--☆ Verifying device ${it.value.mac} at ${host}${it.value.ssdpPath}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
    }
}

def getVerifiedDevices() {
    getDevices().findAll{ it.value.verified == true }
}

def getDevices() {
    if (!state.devices) {
        state.devices = [:]
    }
    state.devices
}

def addDevices() {
    def devices = getDevices()

    selectedDevices.each { dni ->
        def selectedDevice = devices.find { it.value.mac == dni }
        def d
        if (selectedDevice) {
            d = getChildDevices()?.find {
                it.deviceNetworkId == selectedDevice.value.mac
            }
        }

        if (!d) {
            log.debug "Creating Wink Relay Device with dni: ${selectedDevice.value.mac}"
            addChildDevice("joshualyon", "Wink Relay", selectedDevice.value.mac, selectedDevice?.value.hub, [
                    "label": selectedDevice?.value?.name ?: "Wink Relay",
                    "data": [
                            "mac": selectedDevice.value.mac,
                            "ip": selectedDevice.value.networkAddress,
                            "port": selectedDevice.value.deviceAddress
                    ]
            ])
        }
    }
}

def ssdpHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId

    def parsedEvent = parseLanMessage(description)
    parsedEvent << ["hub":hub]

	log.debug "---╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
    //log.debug "---║ RAW PARSED EVENT: $parsedEvent"

    def devices = getDevices()
    devices.each {
        def star = it.value.verified ? "★" : "☆";
    	log.debug "---║ > ${star} ${it.value.ssdpUSN} @ ${it.value.networkAddress}:${it.value.deviceAddress} (${it.value.mac})"
    }
    log.debug "---║ Devices at start of ssdpHandler: "
    String ssdpUSN = parsedEvent.ssdpUSN.toString()
    if (devices."${ssdpUSN}") {
        def d = devices."${ssdpUSN}"
        if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
            d.networkAddress = parsedEvent.networkAddress
            d.deviceAddress = parsedEvent.deviceAddress
            def child = getChildDevice(parsedEvent.mac)
            if (child) {
                child.sync(parsedEvent.networkAddress, parsedEvent.deviceAddress)
            }
        }
    } else {
        log.debug "---║ ☆ Adding ${ssdpUSN} to devices short list"
        devices << ["${ssdpUSN}": parsedEvent]
    }
    log.debug "---╔════SSDP HANDLER════════════════════════════════════════════════════════════════════════════════════"
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.debug "---╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
    def body = hubResponse.xml
    def devices = getDevices()
    log.debug "---║ Got HTTP response for ${body.device.UDN}"
    def device = devices.find { it?.key?.contains(body?.device?.UDN?.text()) }
    if (device) {
        log.debug "---║ Found device in our short list: ${body.device.UDN} - marking VERIFIED★"
        device.value << [name: body?.device?.friendlyName?.text(), model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
    }
    log.debug "---╔════DEVICE DESCRIPTION HANDLER═════════════════════════════════════════════════════════════════════════"
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}