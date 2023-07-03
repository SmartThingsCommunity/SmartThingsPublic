/**
 *  Kodi (formerly XBMC)
 *
 *  Copyright 2016 Josh Lyon
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
    name: "Kodi (formerly XBMC)",
    namespace: "boshdirect",
    author: "Josh Lyon",
    description: "Integration with the Kodi media center software (formerly known as XBMC).",
    category: "Fun & Social",
    iconUrl: "http://kodi.wiki/images/1/14/Thumbnail-light.png",
    iconX2Url: "http://kodi.wiki/images/1/14/Thumbnail-light.png",
    iconX3Url: "http://kodi.wiki/images/1/14/Thumbnail-light.png")


preferences {
	page(name: "deviceDiscovery", title: "Device Setup", content: "deviceDiscovery", refreshTimeout: 5)
	section("Title") {
		// TODO: put inputs here
        
	}
}

/**
 * Get the urn that we're looking for
 *
 * @return URN which we are looking for
 *
 * @todo This + getUSNQualifier should be one and should use regular expressions
 */
def getDeviceType() {
    return "urn:schemas-upnp-org:device:MediaRenderer:1" // Media Renderer (Kodi)
}

/**
 * Get the name of the new device to instantiate in the user's smartapps
 * This must be an app owned by the namespace (see #getNameSpace).
 *
 * @return name
 */
def getDeviceName() {
    return "Kodi Media Center"
}

/**
 * Returns the namespace this app and siblings use
 *
 * @return namespace
 */
def getNameSpace() {
    return "boshdirect"
}


/**
 * Called by SmartThings Cloud when user has selected device(s) and
 * pressed "Install".
 */
def installed() {
    log.trace "Installed with settings: ${settings}"
    initialize()
}

/**
 * Called by SmartThings Cloud when app has been updated
 */
def updated() {
    log.trace "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
    scheduleChecks()
}

def scheduleChecks(){
	//set the CheckEventSubscription to run every 20 minutes
    def minutes = 20
    def cron = "0 0/${minutes} * * * ?" //run every 20 minutes
   	schedule(cron, checkHandler)
    log.trace "Event Subscription Check is scheduled for every ${minutes} minutes"
}

def checkHandler(){
	log.trace "Checking subscriptions for child devices."
    def devices = getChildDevices()
	devices.each {
		it.CheckEventSubscription()
	}
}


/**
 * Called by SmartThings Cloud when user uninstalls the app
 *
 * We don't need to manually do anything here because any children
 * are automatically removed upon the removal of the parent.
 *
 * Only time to do anything here is when you need to notify
 * the remote end. And even then you're discouraged from removing
 * the children manually.
 */
def uninstalled() {
}

/**
 * If user has selected devices, will start monitoring devices
 * for changes (new address, port, etc...)
 */
def initialize() {
    log.trace "initialize()"
    state.subscribe = false
    if (selecteddevice) {
        addDevice()
        refreshDevices()
        subscribeNetworkEvents(true)
    }
}

// TODO: implement event handlers
def locationHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId

    def parsedEvent = parseEventMessage(description)
    parsedEvent << ["hub":hub]

    if (parsedEvent?.ssdpTerm?.contains(deviceUrn)) {
      def devices = getDevices()
      if (!(devices."${parsedEvent.ssdpUSN.toString()}")) {
        devices << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
      }
    }
 }


/**
 * The deviceDiscovery page used by preferences. Will automatically
 * make calls to the underlying discovery mechanisms as well as update
 * whenever new devices are discovered AND verified.
 *
 * @return a dynamicPage() object
 */
def deviceDiscovery()
{
    if(canInstallLabs())
    {
        def refreshInterval = 3 // Number of seconds between refresh
        int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
        state.deviceRefreshCount = deviceRefreshCount + refreshInterval

        def devices = getSelectableDevice()
        def numFound = devices.size() ?: 0

        // Make sure we get location updates (contains LAN data such as SSDP results, etc)
        subscribeNetworkEvents()

        //device discovery request every 15s
        if((deviceRefreshCount % 15) == 0) {
            discoverDevices()
        }

        // Verify request every 3 seconds except on discoveries
        if(((deviceRefreshCount % 3) == 0) && ((deviceRefreshCount % 15) != 0)) {
            verifyDevices()
        }

        log.trace "[deviceDiscovery] Discovered devices: ${devices}"

        return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
            section("Please wait while we discover your ${getDeviceName()}. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
                input "selecteddevice", "enum", required:false, title:"Select ${getDeviceName()} (${numFound} found)", multiple:true, options:devices
            }
        }
    }
    else
    {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.
To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"deviceDiscovery", title:"Upgrade needed!", nextPage:"", install:true, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}


/**
 * Adds the child devices based on the user's selection
 *
 * Uses selecteddevice defined in the deviceDiscovery() page
 */
def addDevice(){
    def devices = getVerifiedDevices()
    def devlist
    log.trace "Adding child devices"

    // If only one device is selected, we don't get a list (when using simulator)
    if (!(selecteddevice instanceof List)) {
        devlist = [selecteddevice]
    } else {
        devlist = selecteddevice
    }

    log.trace "These are being installed: ${devlist}"

    devlist.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newDevice = devices.find { (it.value.mac) == dni }
            def deviceName = newDevice?.value.name
            def udn = getUDN(newDevice.key)
            //d.networkAddress = lanEvent.networkAddress
            //d.deviceAddress = lanEvent.deviceAddress
            log.debug newDevice
            def udnAddress = convertHexToIP(newDevice?.value?.networkAddress)
            def udnPort = convertHexToInt(newDevice?.value?.deviceAddress)
            if (!deviceName)
                deviceName = getDeviceName() + "[${newDevice?.value.name}]" //TODO: this logic seems odd -- change to numbering?
            d = addChildDevice(getNameSpace(), getDeviceName(), dni, newDevice?.value.hub, [label:"${deviceName}", UDN: udn])
            d.setupDevice(newDevice.value?.url, udn, udnAddress, udnPort)
            log.trace "Created ${d.displayName} with id '$dni', IP '$udnAddress', PORT '$udnPort', UDN '${udn}' and url ${newDevice.value?.url}"
        } else {
            log.trace "${d.displayName} with id $dni already exists"
        }
    }
}

/**
 * Parse out the UDN and return it
 * Gets the first item afer the ':'
 *
 * @param USN
 * @return a UDN string
 */
def getUDN(USN){
	def parts = USN.tokenize(":")
    parts[1]
}


/*------------------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------   SUPPORTING CODE -----------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------------------*/


/**
 * Called when location has changed, contains information from
 * network transactions. See deviceDiscovery() for where it is
 * registered.
 *
 * @param evt Holds event information
 */
def onLocation(evt) {
    // Convert the event into something we can use
    def lanEvent = parseLanMessage(evt.description, true)
    lanEvent << ["hub":evt?.hubId]
    
    //log.debug "LAN EVENT: ${lanEvent}"

    // Determine what we need to do...
    if ( lanEvent?.ssdpTerm?.contains(getDeviceType()) )
    {
        parseSSDP(lanEvent)
    }
    else if (
        lanEvent.headers && lanEvent.body &&
        lanEvent.headers."content-type".contains("xml")
        )
    {
        def xmlData = new XmlSlurper().parseText(lanEvent.body)
        parseDESC(xmlData)
    }
}

/**
 * Handles SSDP description file.
 *
 * @param xmlData
 */
private def parseDESC(xmlData) {
    log.info "parseDESC()"

    def devicetype = getDeviceType().toLowerCase()
    def devicetxml = xmlData?.device?.deviceType?.text().toLowerCase() //body.device.deviceType.text().toLowerCase()

    // Make sure it's the type we want
    if (devicetxml == devicetype) {
        def devices = getDevices()
        def device = devices.find {it?.key?.contains(xmlData?.device?.UDN?.text())}
        if (device && !device.value?.verified) {
            def friendlyName = xmlData?.device?.friendlyName?.text()
            def modelName = xmlData?.device?.modelName?.text()
            def manufacturerName = xmlData?.device?.manufacturer?.text()
            def presentationURL = xmlData?.device?.presentationURL?.text()
            log.debug "Found ${friendlyName} (${modelName} by ${manufacturerName})"
            if(modelName == "Kodi"){
            	log.trace "Found a Kodi device. Adding it!"
            	device.value << [name:friendlyName, model:modelName, manufacturer:manufacturerName, url: presentationURL, verified:true]
            }
        } else {
            log.error "parseDESC(): The xml file returned a device that didn't exist"
        }
    }
    else{
    	log.debug "${devicetxml} is not an accepted device"
    }
}


/**
 * Handles SSDP discovery messages and adds them to the list
 * of discovered devices. If it already exists, it will update
 * the port and location (in case it was moved).
 *
 * @param lanEvent
 */
def parseSSDP(lanEvent) {
    //SSDP DISCOVERY EVENTS
    def USN = lanEvent.ssdpUSN.toString()
    def devices = getDevices()

    if (!(devices."${USN}")) {
        //device does not exist
        log.trace "parseSDDP() Adding Device \"${USN}\" to known list"
        devices << ["${USN}":lanEvent]
    } else {
        // update the values
        def d = devices."${USN}"
        if (d.networkAddress != lanEvent.networkAddress || d.deviceAddress != lanEvent.deviceAddress) {
            log.trace "parseSSDP() Updating device location (ip & port)"
            d.networkAddress = lanEvent.networkAddress
            d.deviceAddress = lanEvent.deviceAddress
        }
    }
}

/**
 * Generates a Map object which can be used with a preference page
 * to represent a list of devices detected and verified.
 *
 * @return Map with zero or more devices
 */
Map getSelectableDevice() {
    def devices = getVerifiedDevices()
    log.trace "There are ${devices.size()} verified devices"
    def map = [:]
    devices.each {
        def value = "${it.value.name}"
        def key = it.value.mac
        map["${key}"] = value
    }
    map
}

/**
 * Starts the refresh loop, making sure to keep us up-to-date with changes
 *
 */
private refreshDevices() {
    discoverDevices()
    verifyDevices()
    runIn(300, "refreshDevices")
}

/**
 * Starts a subscription for network events
 *
 * @param force If true, will unsubscribe and subscribe if necessary (Optional, default false)
 */
private subscribeNetworkEvents(force=false) {
    if (force) {
        unsubscribe()
        state.subscribe = false
    }

    if(!state.subscribe) {
        subscribe(location, null, onLocation, [filterEvents:false])
        state.subscribe = true
    }
}

/**
 * Issues a SSDP M-SEARCH over the LAN for a specific type (see getDeviceType())
 */
private discoverDevices() {
    log.trace "discoverDevice() Issuing SSDP request"
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${getDeviceType()}", physicalgraph.device.Protocol.LAN))
}

/**
 * Walks through the list of unverified devices and issues a verification
 * request for each of them (basically calling verifyDevice() per unverified)
 */
private verifyDevices() {
    def devices = getDevices().findAll { it?.value?.verified != true }
    
    log.debug "Verifying ${devices.size()} devices"

    devices.each {
    	//log.debug "Verifying ${it}"
        verifyDevice(
            it?.value?.mac,
            convertHexToIP(it?.value?.networkAddress),
            convertHexToInt(it?.value?.deviceAddress),
            it?.value?.ssdpPath
        )
    }
}

/**
 * Verify the device, in this case, we need to obtain the info block which
 * holds information such as the actual mac to use in certain scenarios.
 *
 * Without this mac (henceforth referred to as deviceID), we can't do multi-speaker
 * functions.
 *
 * @param deviceNetworkId The DNI of the device
 * @param ip The address of the device on the network (not the same as DNI)
 * @param port The port to use (0 will be treated as invalid and will use 80)
 * @param devicessdpPath The URL path (for example, /desc)
 *
 * @note Result is captured in locationHandler()
 */
private verifyDevice(String deviceNetworkId, String ip, int port, String devicessdpPath) {
    if(ip) {
        def address = ip + ":" + port
        sendHubCommand(new physicalgraph.device.HubAction([
            method: "GET",
            path: devicessdpPath,
            headers: [
                HOST: address,
            ]]))
    } else {
        log.warn("verifyDevice() IP address was empty")
    }
}

/**
 * Returns an array of devices which have been verified
 *
 * @return array of verified devices
 */
def getVerifiedDevices() {
    getDevices().findAll{ it?.value?.verified == true }
}

/**
 * Returns all discovered devices or an empty array if none
 *
 * @return array of devices
 */
def getDevices() {
    state.devices = state.devices ?: [:]
}

/**
 * Converts a hexadecimal string to an integer
 *
 * @param hex The string with a hexadecimal value
 * @return An integer
 */
private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

/**
 * Converts an IP address represented as 0xAABBCCDD to AAA.BBB.CCC.DDD
 *
 * @param hex Address represented in hex
 * @return String containing normal IPv4 dot notation
 */
private String convertHexToIP(hex) {
    if (hex)
        [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
    else
        hex
}

/**
 * Tests if this setup can support SmarthThing Labs items
 *
 * @return true if it supports it.
 */
private Boolean canInstallLabs()
{
    return hasAllHubsOver("000.011.00603")
}

/**
 * Tests if the firmwares on all hubs owned by user match or exceed the
 * provided version number.
 *
 * @param desiredFirmware The version that must match or exceed
 * @return true if hub has same or newer
 */
private Boolean hasAllHubsOver(String desiredFirmware)
{
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

/**
 * Creates a list of firmware version for every hub the user has
 *
 * @return List of firmwares
 */
private List getRealHubFirmwareVersions()
{
	def realHubs = location.hubs*.findAll{ it.type == "PHYSICAL" } //filter out the virtual hubs -- only get physical hubs
    return realHubs*.firmwareVersionString.findAll { it }
}