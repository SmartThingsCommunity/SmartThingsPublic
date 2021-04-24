/**
 *  MiLight / EasyBulb / LimitlessLED Light Controller
 *
 *  Copyright 2015 Jared Jensen / jared /at/ cloudsy /dot/ com
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
metadata {
	definition (name: "MiLight Controller", namespace: "fireboy1919", author: "Rusty Phillips", singleInstance: false) {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
        capability "Color Control"
        capability "Polling"
        capability "Sensor"
        capability "Momentary" 
        command "httpCall" 
        command "unknown"
        command "pair"
        command "unpair"
        command "whiten"
	}
    
    preferences {       
       input "code", "number", title: "Address",
       		  description: "ID Code for Light Group", defaultValue: "1",
              required: true, displayDuringSetup: false 
       input "group", "number", title: "Group Number",
       		  description: "The group you wish to control (0-4), 0 = all", defaultValue: "1",
              required: true, displayDuringSetup: false       
       input "lightType", "string", title: "Light Type",
       		  description: "Type of bulb used", defaultValue: "rgbw",
              required: true, displayDuringSetup: false       
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light20", backgroundColor:"#79b821", nextState:"off"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light20", backgroundColor:"#ffffff", nextState:"on"
				attributeState "unknown", label:'unknwn', action:"switch.on", icon:"st.unknown.unknown.unknown", backgroundColor:"#d3d3d3", nextState:"on"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}
        
        standardTile("pair", "device.switch", width: 2, height: 2, canChangeIcon: true) {
           state "default", label:"Pair", defaultState: true, action: "pair", icon: "st.switches.switch.on"
        }
        
        standardTile("unpair", "device.switch", width: 2, height: 2, canChangeIcon: true) {
           state "default", label:"Unpair", defaultState: true, action: "unpair", icon: "st.switches.switch.off"
        }

        standardTile("white", "device.switch", width: 2, height: 2, canChangeIcon: true) {
           state "default", label:"White", defaultState: true, action: "whiten", icon: "st.switches.switch.off"
        }

		main(["switch"])
		details(["switch","levelSliderControl", "rgbSelector", "refresh", "pair", "unpair", "white"])
	} 
}

def poll() {
    return refresh()
}

def parse(String description) {
    parseResponse(description)
}

private parseResponse(String resp) {
    debug.log("Response: $resp" )
}

private parseResponse(resp) {
}

def setLevel(percentage, boolean sendHttp = true) {
    if (percentage < 1 && percentage > 0) {
		percentage = 1
	}
    sendEvent(name: 'level', value: percentage, data: [sendReq: sendHttp]) 
    return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
}

def setColor(value, boolean sendHttp = true) { 
    if(value.red == 255 && value.green == 255 && value.blue == 255) {
    	whiten()
    }
    else {
    	sendEvent(name: 'color', value: value.red + "," + value.green + "," + value.blue, data: [sendReq: sendHttp])
    }
	return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
}

def pair() {
// According to docs, value is required.  It won't sent it unless it's different every time.
    sendEvent(name: "pair", value: java.util.UUID.randomUUID().toString())
}

def unpair() {
// According to docs, value is required.  It won't sent it unless it's different every time.
    sendEvent(name: "unpair", value: java.util.UUID.randomUUID().toString())    
    log.debug("Sent unpair")
}

def whiten() {
   sendEvent(name: "whiten", value: java.util.UUID.randomUUID().toString())    
}

def unknown() {
    sendEvent(name: "switch", value: "unknown")
}

def on(boolean sendHttp = true) {
    return sendEvent(name: "switch", value: "on", data: [sendReq: sendHttp])
}

def off(boolean sendHttp = true) {
    return sendEvent(name: "switch", value: "off", data: [sendReq: sendHttp]);
}

def refresh() {
	return sendEvent(name: "refresh")
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertToHex(port) { 
    String hex = String.format( '%04x', port.toInteger())
    return hex
}

def httpCall(body, ipAddress, code, group) {
    /*
    def mac = getPreferences()['code']
    def ipAddress = getPreferences()['ipAddress']
    def group = getPreferences()['group']
    */
    def path =  "/gateways/$code/rgbw/$group"
    def bodyString = groovy.json.JsonOutput.toJson(body)
    def ipAddressHex = convertIPtoHex(ipAddress)
    def port = convertToHex(80);

    def deviceNetworkId = "$ipAddressHex:$port"
    try {
        def hubaction = new physicalgraph.device.HubAction([
            method: "POST",
            path: path,
            body: bodyString,
			headers: [ HOST: "$ipAddress:80", "Content-Type": "application/x-www-form-urlencoded" ]]
        )
        /*
        httpPut(path, JsonOutput.toJson(body)) {resp ->
            if(settings.isDebug) { log.debug "Successfully updated settings." }
            //parseResponse(resp, mac, evt)
        }
        */
        log.debug("Sending $bodyString to ${path}.")
        sendHubCommand(hubaction);
        return hubAction;
    } catch (e) {
        log.error "Error sending: $e"
    }
}