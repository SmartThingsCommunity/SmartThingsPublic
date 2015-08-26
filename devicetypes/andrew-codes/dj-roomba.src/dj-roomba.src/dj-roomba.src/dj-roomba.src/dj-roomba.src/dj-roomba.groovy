/**
 *  DJ Roomba!
 *
 *  Copyright 2014 Andrew Smith
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
	definition (name: "DJ Roomba!", namespace: "andrew-codes", author: "Andrew Smith") {
		capability "Switch"
		capability "Battery"
		capability "Polling"
		capability "Refresh"

		command "empty"
	}
    
	preferences {
		input("rooUser", "text", title: "Username", description: "Your RooWifi username", required: true)
		input("rooPass", "password", title: "Password", description: "Your RooWifi password", required: true)
		input("rooIp", "text", title: "IP Address", description: "The IP address of your RooWifi", required: true)
	}

	simulator {
	}

	tiles {
		valueTile("battery", "device.battery", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label: '${currentValue}% Battery ${currentState}', icon: "st.Appliances.appliances.10", backgroundColors: [
				[value: 15, color: "#bc2323"],
				[value: 50, color: "#ffff00"],
				[value: 96, color: "#79b821"]
			])
		}
        standardTile("clean", "device.switch", inactiveLabel: false){
            state "on", label:'Clean', action:"switch.on", icon:"st.Appliances.appliances13", backgroundColor: "#79b821"
            state "off", label:'Dock', action:"switch.off", icon:"st.Appliances.appliances13", backgroundColor: "#79b821"
        }
        standardTile("emptyBin", "device.switch") {
                state "empty", label:'Empty', action:"empty", icon:"st.Office.office10"
        }
        standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
                state("default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh")
        }
		main "clean"
		details(["clean", "emptyBin", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "Parsing '${description}'"
	return
	def map = stringToMap(description)
	def result = []
	if (map.body) {
		def bodyString = new String(map.body.decodeBase64())
		log.debug bodyString
		switch (bodyString) {
			case "0":
				log.debug "The Command Failed"
			break;
			case "1":
				log.debug "The Command Succeeded"
			break;
			default:
				log.debug "default"
				def body = new XmlSlurper().parseText(bodyString)
				def rooBattState = body?.r14
				def rooBattCharge = body?.r18?.toInteger()
				def rooBattCapacity = body?.r19.toInteger()
				if (rooBattCharge & rooBattCapacity) {
					log.trace "Current Battery Charge: $rooBattCharge"
					log.trace "Current Battery Capacity: $rooBattCapacity"
					log.trace "Current Battery State: $rooBattState"
					def rooBattPercent = ((rooBattCharge/rooBattCapacity)*100).toFloat().round()
					sendEvent(name: "battery", value: rooBattPercent as Integer, state: rooBattState as String)        
				}
            return [:]
		}
	}
	result
}

def on() {
	log.debug "Executing 'clean'"
	control("CLEAN")
	sendEvent(name: 'switch', value: "off")
}

def off() {
	log.debug "Executing 'dock'"
	control("DOCK")
	sendEvent(name: 'switch', value: "on")
}

def poll() {
	sendEvent(descriptionText: "poll keep alive", isStateChange: false)
	refresh
}

def refresh () {
	log.trace "Executing 'Refresh'"
	control("REFRESH")
}

def empty() {
	// TODO: implement empty
}

def control (String command, success = {}) {
	log.trace "Executing '${command}'"
    def path = "/rwr.xml"
    if (command != "REFRESH"){
    	path = "/roomba.cgi?button=${command}"
    }
	def rooPort = "80"
	def userpassascii = "${rooUser}:${rooPass}"
	def hosthex = convertIPtoHex(rooIp)
	def porthex = convertPortToHex(rooPort)
	def userPass = "Basic " + userpassascii.encodeAsBase64().toString()
	device.deviceNetworkId = "${hosthex}:${porthex}"
	try {
		def hubAction = [new physicalgraph.device.HubAction(
			method: "GET",
			path: path,
			headers: [HOST: "${rooIp}:${rooPort}"]
		), delayAction(1000), refresh()]
		sendHubCommand(hubAction)
		log.debug hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
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