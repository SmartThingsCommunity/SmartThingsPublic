/**
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Tesla 
 *
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "Tesla", namespace: "jonbur", author: "JB") {
		
        capability "Polling"
		capability "Refresh"
		capability "Switch"
        capability "presenceSensor"
        
		command "refresh"

	}
    
	preferences {
		input("ip", "text", title: "IP Address", description: "Local server IP address", required: true, displayDuringSetup: true)
		input("port", "number", title: "Port Number", description: "Port number (Default:5000)", defaultValue: "5000", required: true, displayDuringSetup: true)
	}

	tiles {

		 standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#ff0000"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ebeef2")
	    }
        
        main "presence"
        details(["presence", "switch", "refresh"])
     }
 }

def parse(String description) {
	def map
	def headerString
	def bodyString
	def slurper
	def result

	map = stringToMap(description)
	headerString = new String(map.headers.decodeBase64())
	if (headerString.contains("200 OK")) {
		bodyString = new String(map.body.decodeBase64())
		slurper = new JsonSlurper()
		result = slurper.parseText(bodyString)
        log.debug result
     
		switch (result.isclimateon) {
			case "False":
				sendEvent(name: 'switch', value: "off" as String)
			break;
			case "True":
				sendEvent(name: 'switch', value: "on" as String)
				
			}
         
      	switch (result.isvehiclehome) {
			case "False":
            	log.debug 'Vehicle is away'
				away()
			break;
			case "True":
            	log.debug 'Vehicle is home'
				present()
			}
    
		}
	else {
		sendEvent(name: 'status', value: "error" as String)
		log.debug headerString
	}
}

// handle commands

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	log.info "Tesla ${textVersion()} ${textCopyright()}"
	ipSetup()
	poll()
}

def on() {
	log.debug "Executing 'on'"
	ipSetup()
	api('on')
}

def off() {
	log.debug "Executing 'off'"
    ipSetup()
	api('off')
}

def away() {
	log.debug('not present')
	sendEvent(name: 'presence', value: 'not present')
}

def present() {
	log.debug('present')
	sendEvent(name: 'presence', value: 'present')
}




def poll() {
	log.debug "Executing 'poll'"
    
	if (device.deviceNetworkId != null) {
		refresh()
	}
	else {
		log.debug "DNI: Not set"
	}
}

def refresh() {
	log.debug "Executing 'refresh'"
	ipSetup()
	api('refresh')
    api('ishome')
}

def api(String APICommand, success = {}) {
	def APIPath
	def hubAction
	
	switch (APICommand) {
		case "on":
			APIPath = "/api/starthvac"
			log.debug "The start command was sent"
		break;
		case "off":
			APIPath = "/api/stophvac"
			log.debug "The stop command was sent"
		break;
		case "refresh":
			APIPath = "/api/isclimateon"
			log.debug "The Status Command was sent"
		break;		
        case "ishome":
			APIPath = "/api/isvehiclehome"
			log.debug "Request if vehicle is home sent"
		break;
        
	}
    
	switch (APICommand) {
		case "refresh":
			try {
				hubAction = new physicalgraph.device.HubAction(
				method: "GET",
				path: APIPath,
				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"])
			}
			catch (Exception e) {
				log.debug "Hit Exception $e on $hubAction"
			}
			break;
		default:
			try {
				hubAction = [new physicalgraph.device.HubAction(
				method: "GET",
				path: APIPath,
				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"]
				), delayAction(5000), api('refresh')]
			}
			catch (Exception e) {
				log.debug "Hit Exception $e on $hubAction"
			}
			break;
	}
	return hubAction
}

def ipSetup() {
	def hosthex
	def porthex
	if (settings.ip) {
		hosthex = convertIPtoHex(settings.ip)
	}
	if (settings.port) {
		porthex = convertPortToHex(settings.port)
	}
	if (settings.ip && settings.port) {
		device.deviceNetworkId = "$hosthex:$porthex"
	}
}

private String convertIPtoHex(ip) { 
	String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hexip
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}
private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private def textVersion() {
	def text = "Version 0.1"
}

private def textCopyright() {
	def text = "Copyright © 2017 JB"
}