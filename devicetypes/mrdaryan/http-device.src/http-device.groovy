/*
*  HTTP Button
*  Category: Device Handler
*/

import groovy.json.JsonSlurper

metadata {
	definition (name: "HTTP Device", namespace: "mrdaryan", author: "David Ryan") {
	capability "Actuator"
    capability "Switch"
	capability "Momentary"
	     
    command "PushWithHTTP", ["string"]
    }

	preferences {
		input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Empty assumes port 80.", required: false, displayDuringSetup: true)
        input("DeviceContent", "string", title:"Content Type", description: "HTTP Content type", defaultValue: "application/json", displayDuringSetup: true)
		input(name: "DevicePostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], defaultValue: "POST", required: false, displayDuringSetup: true)
		input("DevicePathOn", "string", title:"URL Path ON", description: "URL path for ON, include forward slash (i.e. /jsonrpc)", displayDuringSetup: true)
        input("DeviceBodyOn", "string", title:"Body ON", description: "Body of message.", displayDuringSetup: true)
        input("DevicePathOff", "string", title:"URL Path OFF", description: "URL path for OFF, include forward slash (i.e. /jsonrpc)", displayDuringSetup: true)
        input("DeviceBodyOff", "string", title:"Body OFF", description: "Body of message", displayDuringSetup: true)
        input("DevicePathPush", "string", title:"URL Path PUSH", description: "URL path for PUSH, include forward slash (i.e. /jsonrpc)", displayDuringSetup: true)
        input("DeviceBodyPush", "string", title:"Body PUSH", description: "Body of message", displayDuringSetup: true)
		section() {
			input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
			input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
			input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
		}
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
       standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
    		state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
    		state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState:"off"
        }

		standardTile("button", "device.button", width: 2, height: 2, canChangeIcon: true) {
            state "default", label: 'Push', action: "push", backgroundColor: "#ffffff"
		}
		main "switch"
		details(["switch","button"])
	}
}

def parse(String description) {
	log.debug(description)
}

def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    runCmd(DevicePathOn,DeviceBodyOn)
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    runCmd(DevicePathOff,DeviceBodyOff)
}

def push() {
	runCmd(DevicePathPush,DeviceBodyPush)
}

def PushWithHTTP(HTTPContent) {
    if (DevicePostGet.toUpperCase() == "GET") {
    	runCmd(HTTPContent,"")
		} else {
        runCmd(DevicePathPush,HTTPContent)
    }
}

def runCmd(varCommand,varBody) {
    def path = varCommand
	def body = varBody
	def host = DeviceIP
	def LocalDevicePort = ''
	if (DevicePort==null) { LocalDevicePort = "80" } else { LocalDevicePort = DevicePort }

	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")
	headers.put("Content-Type", "${DeviceContent}")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"

	def method = "POST"
	try {
		if (DevicePostGet.toUpperCase() == "GET") {
			method = "GET"
            body = ' '
			}
		}
	catch (Exception e) {
		settings.DevicePostGet = "POST"
		log.debug e
		log.debug "You must not have set the preference for the DevicePOSTGET option"
	}
    log.debug "The device id configured is: $device.deviceNetworkId"
	log.debug "Path is: $path"
    log.debug "Body is: $body"
	log.debug "The method is $method"
    //if the body and the path contains nothing, don't worry about sending. 
    if (body != null && path != null){
		try {
			def hubAction = new physicalgraph.device.HubAction(
				method: method,
				path: path,
				body: body,
				headers: headers
				)
			log.debug hubAction
			return hubAction
		}
		catch (Exception e) {
			log.debug "Hit Exception $e on $hubAction"
		}
    } else {
		log.debug "Nothing to send"
	}
}