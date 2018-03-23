/**
*  This is a start to porting the Arduino and SmartShield based
*  Irrigation Controllers to an ESP8266 based controller
*  Author:  Aaron Nienhuis (aaron.nienhuis@gmail.com)
*
*  Date:  2017-04-07
*  Copyright 2017 Aaron Nienhuis
*  
*  
* 
*  Smart Sprinkler Controller discovery SmartApp
*  Uses SSDP to discover ESP8266 based Smart Sprinkler controllers
*
*
*  ESP8266 port based on the extensive previous work of:
*  Author: Stan Dotson (stan@dotson.info) and Matthew Nichols (matt@nichols.name)
*
*  Portions of this work previously copyrighted by Stan Dotson and Matthew Nichols
*
*	Some code and concepts incorporated from other projects by:
*  Eric Maycock
*
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
**/

definition(
    name: "Smart Sprinkler (Connect)",
    namespace: "anienhuis",
    author: "aaron.nienhuis@gmail.com",
    description: "Schedule sprinklers to run unless there is rain.",
    version: "1.0.1",
    singleInstance: true,
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png"
)


//*****
preferences {
	page(name: "mainPage")
    page(name: "configurePDevice")
    page(name: "deletePDevice")
    page(name: "changeName")
    page(name: "discoveryPage", title: "Device Discovery", content: "discoveryPage", refreshTimeout:5)
    page(name: "addDevices", title: "Add Irrigation Controller", content: "addDevices")
    page(name: "manuallyAdd")
    page(name: "manuallyAddConfirm")
    page(name: "deviceDiscovery")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "Manage your Irrigation Controllers", nextPage: null, uninstall: true, install: true) {
        section("Configure"){
           href "deviceDiscovery", title:"Discover Devices", description:""
           href "manuallyAdd", title:"Manually Add Device", description:""
        }
        section("Installed Devices"){
            getChildDevices().sort({ a, b -> a["deviceNetworkId"] <=> b["deviceNetworkId"] }).each {
                href "configurePDevice", title:"$it.label", description:"", params: [did: it.deviceNetworkId]
            }
        }
        section("Irrigation Schedules"){
        }
        section("Irrigation Schedule Creator"){
            app(name: "Irrigation Schedule", appName: "Smart Sprinkler Scheduler", namespace: "anienhuis", title: "Create New Schedule", multiple: true)
            
            //getChildDevices().sort({ a, b -> a["deviceNetworkId"] <=> b["deviceNetworkId"] }).each {
            //    href "configurePDevice", title:"$it.label", description:"", params: [did: it.deviceNetworkId]
            //}
        }
    }
}

def configurePDevice(params){
   def currentDevice
   getChildDevices().each {
       if(it.deviceNetworkId == params.did){
           state.currentDeviceId = it.deviceNetworkId
           state.currentDisplayName = it.displayName
       }      
   }
   if (getChildDevice(state.currentDeviceId) != null) getChildDevice(state.currentDeviceId).configure()
   dynamicPage(name: "configurePDevice", title: "Configure Irrigation Controllers created with this app", nextPage: null) {
		section {
            app.updateSetting("${state.currentDeviceId}_label", getChildDevice(state.currentDeviceId).label)
            input "${state.currentDeviceId}_label", "text", title:"Device Name", description: "", required: false
            href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it", params: [did: state.currentDeviceId]
            //href "schedulePage", title:"Configure Irrigation Schedule", description: "Click here to configure the irrigation schedule", params: [did: state.currentDeviceId]
        }
        section {
              href "deletePDevice", title:"Delete $state.currentDisplayName", description: "", params: [did: state.currentDeviceId]
        }
   }
}

def manuallyAdd(){
   dynamicPage(name: "manuallyAdd", title: "Manually add a Irrigation Controller", nextPage: "manuallyAddConfirm") {
		section {
			paragraph "This process will manually create a Irrigation Controller based on the entered IP address. The SmartApp needs to then communicate with the device to obtain additional information from it. Make sure the device is on and connected to your wifi network."
            input "deviceType", "enum", title:"Device Type", description: "", required: false, options: ["Smart Sprinkler Controller 4 Zones","Smart Sprinkler Controller 8 Zones"]
            input "ipAddress", "text", title:"IP Address", description: "", required: false 
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
       log.debug "Creating Smart Sprinkler Controller with dni: ${convertIPtoHex(ipAddress)}:${convertPortToHex("80")}"
       addChildDevice("anienhuis", deviceType ? deviceType : "Smart Sprinkler Controller 4 Zones", "${convertIPtoHex(ipAddress)}:${convertPortToHex("80")}", location.hubs[0].id, [
           "label": (deviceType ? deviceType : "Smart Sprinkler Controller 4 Zones") + " (${ipAddress})",
           "data": [
           "ip": ipAddress,
           "port": "80" 
           ]
       ])
   
       app.updateSetting("ipAddress", "")
            
       dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Smart Sprinkler Irrigation Controller", nextPage: "mainPage") {
		   section {
			   paragraph "The device has been added. Press next to return to the main page."
	    	}
       }
    } else {
        dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Smart Sprinkler Irrigation Controller", nextPage: "mainPage") {
		    section {
			    paragraph "The entered ip address is not valid. Please try again."
		    }
        }
    }
}

def deletePDevice(params){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted. Press next to continue"
            } 
        }
    
	} catch (e) {
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            } 
        }
    
    }
}

def changeName(params){
    def thisDevice = getChildDevice(state.currentDeviceId)
    thisDevice.label = settings["${state.currentDeviceId}_label"]

    dynamicPage(name: "changeName", title: "Change Name Summary", nextPage: "mainPage") {
	    section {
            paragraph "The device has been renamed. Press \"Next\" to continue"
        }
    }
}

def discoveryPage(){
   return deviceDiscovery()
}

def deviceDiscovery(params=[:])
{
	def devices = devicesDiscovered()
    
	int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
	state.deviceRefreshCount = deviceRefreshCount + 1
	def refreshInterval = 3
    
	def options = devices ?: []
	def numFound = options.size() ?: 0

	if ((numFound == 0 && state.deviceRefreshCount > 25) || params.reset == "true") {
    	log.trace "Cleaning old device memory"
    	state.devices = [:]
        state.deviceRefreshCount = 0
        app.updateSetting("selectedDevice", "")
    }

	ssdpSubscribe()

	//ESP8266 Smart Sprinkler Controller discovery request every 15 //25 seconds
	if((deviceRefreshCount % 5) == 0) {
		discoverDevices()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((deviceRefreshCount % 3) == 0) && ((deviceRefreshCount % 5) != 0)) {
		verifyDevices()
	}

	return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", nextPage:"addDevices", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Smart Sprinkler Controller devices. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedDevices", "enum", required:false, title:"Select Irrigation Controller (${numFound} found)", multiple:true, options:options
		}
        section("Options") {
			href "deviceDiscovery", title:"Reset list of discovered devices", description:"", params: ["reset": "true"]
		}
	}
}

Map devicesDiscovered() {
	def vdevices = getVerifiedDevices()
	def map = [:]
	vdevices.each {
		def value = "${it.value.name}"
		def key = "${it.value.mac}"
		map["${key}"] = value
	}
	map
}

def getVerifiedDevices() {
	getDevices().findAll{ it?.value?.verified == true }
}

private discoverDevices() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", physicalgraph.device.Protocol.LAN))
}

def configured() {
	
}

def isConfigured(){
   if(getChildDevices().size() > 0) return true else return false
}

def isVirtualConfigured(did){ 
    def foundDevice = false
    getChildDevices().each {
       if(it.deviceNetworkId != null){
       if(it.deviceNetworkId.startsWith("${did}/")) foundDevice = true
       }
    }
    return foundDevice
}

private virtualCreated(number) {
    if (getChildDevice(getDeviceID(number))) {
        return true
    } else {
        return false
    }
}

private getDeviceID(number) {
    return "${state.currentDeviceId}/${app.id}/${number}"
}


def initialize() {
    ssdpSubscribe()
    runEvery5Minutes("ssdpDiscover")
}

void ssdpSubscribe() {
    subscribe(location, "ssdpTerm.urn:schemas-upnp-org:device:Basic:1", ssdpHandler)
}

void ssdpDiscover() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", physicalgraph.device.Protocol.LAN))
}

def ssdpHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId
    def parsedEvent = parseLanMessage(description)
    //log.debug "ssdpHandler:description $description"
    
    parsedEvent << ["hub":hub]
    //log.debug "ssdpHandler:parsedEvent $parsedEvent"

    def devices = getDevices()
    
    String ssdpUSN = parsedEvent.ssdpUSN.toString()
    
    if (devices."${ssdpUSN}") {
        def d = devices."${ssdpUSN}"
        def child = getChildDevice(parsedEvent.mac)
        def childIP
        def childPort
        if (child) {
            childIP = child.getDeviceDataByName("ip")
            childPort = child.getDeviceDataByName("port").toString()
            //log.debug "Device data: ($childIP:$childPort) - reporting data: (${convertHexToIP(parsedEvent.networkAddress)}:${convertHexToInt(parsedEvent.deviceAddress)})."
            if(childIP != convertHexToIP(parsedEvent.networkAddress) || childPort != convertHexToInt(parsedEvent.deviceAddress).toString()){
               log.debug "Device data (${child.getDeviceDataByName("ip")}) does not match what it is reporting(${convertHexToIP(parsedEvent.networkAddress)}). Attempting to update."
               child.sync(convertHexToIP(parsedEvent.networkAddress), convertHexToInt(parsedEvent.deviceAddress).toString())
            }
        }

        if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
            d.networkAddress = parsedEvent.networkAddress
            d.deviceAddress = parsedEvent.deviceAddress
        }
    } else {
        devices << ["${ssdpUSN}": parsedEvent]
    }
}

void verifyDevices() {
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        def ip = convertHexToIP(it.value.networkAddress)
        def port = convertHexToInt(it.value.deviceAddress)
        String host = "${ip}:${port}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
    }
}

def getDevices() {
    state.devices = state.devices ?: [:]
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.trace "esp8266ic.xml response (application/xml)"
	def body = hubResponse.xml
    //log.debug body?.device?.friendlyName?.text()
	if (body?.device?.modelName?.text().startsWith("ESP8266Irrigation")) {
		def devices = getDevices()
		def device = devices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (device) {
			device.value << [name:body?.device?.friendlyName?.text() + " (" + convertHexToIP(hubResponse.ip) + ")", serialNumber:body?.device?.serialNumber?.text(), verified: true]
		} else {
			log.error "/esp8266ic.xml returned a device that didn't exist"
		}
	}
}

def addDevices() {
    def devices = getDevices()
    def sectionText = ""

    selectedDevices.each { dni ->bridgeLinking
        def selectedDevice = devices.find { it.value.mac == dni }
        def d
        if (selectedDevice) {
            d = getChildDevices()?.find {
                it.deviceNetworkId == selectedDevice.value.mac
            }
        }

        if (!d) {
            //log.debug selectedDevice
            //log.debug "Creating Irrigation Controller with dni: ${selectedDevice.value.mac}"
            //log.debug Integer.parseInt(selectedDevice.value.deviceAddress,16)
            addChildDevice("anienhuis", (selectedDevice?.value?.name?.startsWith("Smart Sprinkler Controller 4") ? "Smart Sprinkler Controller 4 Zones" : "Smart Sprinkler Controller 8 Zones"), selectedDevice.value.mac, selectedDevice?.value.hub, [
                "label": selectedDevice?.value?.name ?: "Smart Sprinkler ",
                "data": [
                    "mac": selectedDevice.value.mac,
                    "ip": convertHexToIP(selectedDevice.value.networkAddress),
                    "port": "" + Integer.parseInt(selectedDevice.value.deviceAddress,16)
                ]
            ])
            sectionText = sectionText + "Successfully added Smart Sprinkler Controller with ip address ${convertHexToIP(selectedDevice.value.networkAddress)} \r\n"
        }
        
	} 
    
        return dynamicPage(name:"addDevices", title:"Devices Added", nextPage:"mainPage",  uninstall: true) {
        if(sectionText != ""){
		section("Add Irrigation Controller Results:") {
			paragraph sectionText
		}
        }else{
        section("No devices added") {
			paragraph "All selected devices have previously been added"
		}
        }
}
    }

def uninstalled() {
    unsubscribe()
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
}



private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}