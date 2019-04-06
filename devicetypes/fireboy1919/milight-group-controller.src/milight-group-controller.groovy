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
	definition (name: "MiLight Group Controller", namespace: "fireboy1919", author: "Rusty Phillips", singleInstance: false) {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
        capability "Color Control"
        capability "Polling"
        capability "Sensor"
        capability "Refresh" 
        command "httpCall" 
        command "unknown"
	}
    
    preferences {       
       input "ipAddress", "string", title: "IP Address",
       		  description: "The IP address of this MiLight bridge", defaultValue: "The IP address here",
              required: true, displayDuringSetup: false 
			  /*
        input "port", "string", title: "Port number",
       		  description: "The port number used by this MiLight bridge", defaultValue: "Theport number here",
              required: true, displayDuringSetup: false 
      */
       input "mac", "string", title: "Mac Address",
       		  description: "The MAC address of this MiLight bridge", defaultValue: "The Mac address here",
              required: true, displayDuringSetup: false 
       input "group", "number", title: "Group Number",
       		  description: "The group you wish to control (0-4), 0 = all", defaultValue: "0",
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
        
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
       
		main(["switch"])
		details(["switch","levelSliderControl", "rgbSelector", "refresh"])
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
  	if(value in String) {
        def j = value
        sendEvent(name: 'color', value: j, data: [sendReq: sendHttp])
    } else {
    	def h = value.hex
        sendEvent(name: 'color', value: h, data: [sendReq: sendHttp])
    }
	return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
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
    String hex = String.format( '%02x', port.toInteger())
    return hex
}

def httpCall(body, ipAddress, mac) {

    def path =  "/gateways/$mac/rgbw/$group"
    def bodyString = groovy.json.JsonOutput.toJson(body)
    def ipAddressHex = convertIPtoHex(ipAddress)
    def port = convertToHex("80");
    log.debug("Sending $bodyString to ${path}.")
    //device.deviceNetworkId = "$ipAddressHex:$port"
    try {
        def hubaction = new physicalgraph.device.HubAction(
            method: "PUT",
            path: path,
            body: body,
            headers: [ HOST: device.deviceNetworkId, "Content-Type": "application/json" ]
        )
        /*
        httpPut(path, JsonOutput.toJson(body)) {resp ->
            if(settings.isDebug) { log.debug "Successfully updated settings." }
            //parseResponse(resp, mac, evt)
        }
        */
        sendHubCommand(hubaction);
        //return hubAction;
    } catch (e) {
        log.error "Error sending: $e"
    }
}