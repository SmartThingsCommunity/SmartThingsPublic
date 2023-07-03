/**
 *  Broadlink SPMini (10024) Smart Switch Device Handler v1.4
 *
 *  Copyright 2016 Neal Hinson
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
 *  Modified to directly drive SPMini smart switch via LAN
 *  Author:   D. Neal Hinson
 *  Date Started:   Nov 23, 2016
 *  Last Edited:    Dec 05, 2016
 *
 */
 
metadata {								//This portion is used by/for the device handler
	definition (name: "Broadlink Switch v1.4", namespace: "DNH-Automation", author: "Neal Hinson") {
		capability "switch"				 // Possible attrib types : “STRING”, “NUMBER”, “VECTOR3”, “ENUM
		attribute "RM_Bridge","string"   // This is the IP/Port of the Amdroid RM Bridge that replaces the Broadlink hub 
		attribute "Hub_ID","string"      // This is the UUID of the SmartThings Hun used to receive network responses 
        attribute "Callback_ID","string" // This is the IP:Port of the SmartThings used to receive network responses 
        attribute "MAC","string"         // Stored in "Device Network ID" - - "11:22:33:44:55:66" "b4:43:0d:c1:c7:6a"
        attribute "MAC_HEX","string"     // Same mac as above without colons - "b4430dc1c76a"
        attribute "RM_ip","string"       // To be derived from RM_Bridge 
        attribute "RMport","string"      // To be derived from RM_Bridge
		attribute "currentIP", "string"  // To hold the IP address of the SPMini device
        attribute "onCodeID","string"    // Code for turning on device 
        attribute "offCodeID","string"   // Code for turning off device
        attribute "currState","string"   // Code for toggling states on device

        command "Toggle"
        command "Subscribe"
        command "Unsubscribe"
        command "SetOffline"
	}   
    // simulator metadata
    simulator {
    }

	// tile definitions
	tiles {								//This portion is used by/for the device handler
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on",     label:'${name}', action: "switch.off",    icon: "st.switches.switch.on",  backgroundColor: "#79b821", nextState:"off"
			state "off",    label:'${name}', action: "switch.on",     icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "toggle", label:'${name}', action: "switch.toggle", icon: "st.switches.switch.on",  backgroundColor: "#792079", nextState:"on"
	}
	main "switch"
	details "switch"
	}
    
    preferences {						//This portion is used by/for the SMARTAPP
    	page(name: "InputPage", title: "Broadlink Switch Configurer", install: true, uninstall: true) {
            section("You can find the MAC address from your e-Control app which is required to setup your switches.") {
            	input "switches", "capability.switch", multiple: true  // <<=== The only part that seems to work!!
                input "SPmac", "string",
                    title: "MAC address",
                    description: "MAC address of SPmini device being created.",
                    defaultValue: "b4:43:0d:ef:73:31",
                    required: true,
                    displayDuringSetup: true
            }
            displayDuringSetup: true
        }
    }
	log.debug "------------------------- Done with setup & Metadata -------------------------------"
}



def installed() {						//This portion is used by/for the device-handler AND the SmartApp.
   	initialize()
}

def updated() {							//This portion is used by/for the SmartApp only ... I think???
	initialize()
}


/**********************************************************************************************
						HERE IS WHERE THE ACTUAL PROGRAMMING BEGINS.
***********************************************************************************************/
def initialize() {					//This portion is used by/for the device-handler AND the SmartApp.
	log.debug "Entering initialize() --------------------------------------------"
	log.debug "Installed with settings: ${settings}"
    
	//First get all the attributes populated for system use.
    populateAtributes()
	getDevices()
    
    

    log.debug "----------------------- This is as far as I've gotten -------------------------"
}

void populateAtributes()
{	log.debug "Entering populateAtributes() -------------------------------------"
    def STHubId, SPmac, SPmacHEX, SPip, SPport, myON, myOFF, myTOG

	//First thing, setup the link to the RM Bridge which acts as a replacement for the Broadlink hub
    //This is the Android device sitting on the WiFi Lan that hosts the RM_Tasker plug-in created by Torin Ngyuen
    //TODO: Figure out how to retrieve this automatically.   
	if ( !"${device.currentValue("RM_Bridge")}" )   sendEvent([name:"RM_Bridge", value:"192.168.2.231:9876"])
    log.debug "------ Getting Attribute \"RM_Bridge\" is ${device.currentValue("RM_Bridge")}"

    //Find UUID of ST Hub to use for future ResponseHandler(), then Save to Attribute.
    STHubId = location.hubs[0].id
    sendEvent([name:"Hub_ID",value: "${STHubId}"])
    log.debug "------ Getting Attribute \"Hub_ID\" is now ${device.currentValue("Hub_ID")}"

    //Find ST Hub IP to use for future ResponseHandler(), then Save to Attribute.
    def callBackIP = getCallBackAddress()
    sendEvent([name:"Callback_ID",value: "${callBackIP}"])
    log.debug "------ Getting Attribute \"Callback_ID\" is ${device.currentValue("Callback_ID")}"
    
    //Capture and use the "Device Network Id"
    //log.debug "Processing Network MAC (URI) ----------------------------- "
    log.debug "------ DeviceNetworkId is set to ${device.deviceNetworkId}"
    SPmac = device.deviceNetworkId
    if(!SPmac) return   //This param MUST be filled out at time of creation ... this is olny an anomoly trap.
    sendEvent([name:"MAC",value: "${SPmac}"])
    log.debug "------ Getting Attribute \"MAC\" is now ${device.currentValue("MAC")}" 
    
    //Convert MAC string to ST usable MAC without colons
    SPmacHEX = SPmac.replaceAll(':', '')
    sendEvent([name:"MAC_HEX",value: "${SPmacHEX}"])
    log.debug "------ Getting Attribute \"MAC_HEX\" is now ${device.currentValue("MAC_HEX")}"

	//Setup my command strings
	myON =  "/send?deviceMac=${SPmac}&on=true"  
	myOFF = "/send?deviceMac=${SPmac}&off=true"
    sendEvent(name:"onCodeID",value: "${myON}")
    sendEvent(name:"offCodeID",value: "${myOFF}")
    log.debug "------ ON = ${myON}"
    log.debug "------ OFF = ${myOFF}"
}

def fetchIPaddr()  {
    //Find the IP for given MAC address Supplied from "Device Network ID" table.
    log.debug "FetchIPaddr() -------------------------------------------------------------------------------"
    ssdpDiscover()
}
def switchesHandler() {
    log.debug "In switchesHandler() ----- RMhub = ${device.currentValue('RM_Bridge')} "
    log.debug "------------------------------ Nothing defined here yet --------------------------------"
}


def getDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	state.devices
}
private getCallBackAddress() {
    // Gets the address of the hub
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}
private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

def changedata(attrib, newstate) {
	sendEvent([name:"${attrib}", value:"${newstate}"])
}
def on() {									//For use in the device-type handler.
 //	log.debug "In on() ------"
 //	def codeID = device.currentValue("onCodeID")	//	put("$codeID")
    sendEvent([name:"currState",value: "ON"])
	sendEvent(name:"switch", value:"on")
    put("on")
 // poll()
}
def off() {									//For use in the device-type handler.
 //	log.debug "In off() -----"
    sendEvent([name:"currState",value: "OFF"])
	sendEvent(name:"switch", value:"off")
	put("off")
 // poll()
}
def Toggle() {								//For use in the device-type handler.
    log.debug " ----- currState = ${device.currentValue("currState")} --- "
    if ("ON" == device.currentValue("currState"))  {  off()  } else {  on()  }
}
def put(payload) {							//For use in the device-type handler.
//	log.debug "In put() --- payload = \"${payload}\" ---  "
//	log.debug "-------------- SThub = \"${device.currentValue("RM_Bridge")}\" --- "
    
    def httpReqst = [	method:	'GET',
						path:  "/send", 
                        headers: [ HOST:"${device.currentValue("RM_Bridge")}", Accept:"*/*" ],
                        query:   [ deviceMac:"${device.deviceNetworkId}", "${payload}":"true" ]  
                    ]
	log.debug "In put() ----- httpReqst = $httpReqst"
    def hubaction = new physicalgraph.device.HubAction( httpReqst ) 
    try {	hubaction            
    } catch (e) {
        	log.debug "Something went wrong with HubAction: $e"
    }
	return hubaction
}


def poll() {								//For use in the device-type handler.
    def results = /*parent.*/pollChildren()   
    log.debug "Results equals ----- $results" 
    log.debug "Results.description equals ----- ${results.description}" 
    //lanResponseHandler()
}
def parseEventData(Map results){			//For use in the device-type handler.
    results.each { name, value ->	        //Parse events and optionally create SmartThings events
    	log.debug "---- name: ${it.name}  ---  value: ${it.value} "
    }
}

/*************************************************************************************
def pollChildren() {						//For use in the SERVICE MANAGER app.
    def myUri = "https:${device.currentValue("RM_Bridge")}"
    log.debug " --- myUri is set to ... ${myUri} --- "

	def httpReqst = [	method:	'GET',
                        path:  "/status", 
                        headers: [ HOST:"${device.currentValue("RM_Bridge")}", Accept:"* /*" ],
                        query:   [ deviceMac:"${device.deviceNetworkId}"  ]  
					]
	def hubaction = new physicalgraph.device.HubAction( httpReqst ) 
    
    try {	hubaction            
    } catch (e) {
        	log.debug "Something went wrong with HubAction: $e"
    }
	return hubaction
} ********************************************************************************/



/****************************************************************************************************************
				BELOW IS WHERE I STASH CODE THAT I'M GOING TO GET AROUND TO LATER.
*****************************************************************************************************************/
//childName.generateEvent(data)			//Method inside the service manager:

def generateEvent(Map results) {		//In the device handler:
  results.each { name, value ->
    sendEvent(name: name, value: value)
  }
  return null
}
def parse(description) {
	log.debug "Entering parse() -----------------"
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
     //Expected resonse =>>  {msg: "Code Fan • Mist successfully sent", status: "ok", codeId: "19", deviceMac: "b4:43:0d:10:56:3a"}
	log.debug "Leaving parse() -----------------"
}

/****************************
def addDevices() {
	def devices = getDevicesXXX()
	log.debug "In addDevices() ------- it.value.mac equals ${it?.value?.mac} "
    
	selectedDevices.each { dni ->
		def selectedDevice = devices.find { it.value.mac == dni }
		def d
		if (selectedDevice) {
			log.debug "made it into first loop ----- "
            
            d = getChildDevices()?.find {
				it.deviceNetworkId == selectedDevice.value.mac
			}
		}
		if (!d) {
		log.debug "made it into second loop ----- "
		log.debug "Creating Broadlink Switch with dni: ${selectedDevice.value.mac}"
        
	return
    
			addChildDevice("smartthings", "Broadlink Switch", selectedDevice.value.mac, selectedDevice?.value.hub, [
				"label": selectedDevice?.value?.name ?: "Broadlink Switch v1.2",
				"data": [
					"mac": selectedDevice.value.mac,
					"ip": selectedDevice.value.networkAddress,
					"port": selectedDevice.value.deviceAddress
				]
			])
		}
	}
} ******************************/