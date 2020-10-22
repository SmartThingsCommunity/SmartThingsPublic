/**
 *  Computer
 *
 *  Copyright 2018 Patyi Andr&aacute;s
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
	definition (name: "Computer", namespace: "patyiandras", author: "Patyi Andr&aacute;s") {
		capability "Actuator"
		capability "Sensor"
		capability "switch"
        capability "presenceSensor"
        capability "Polling"
        capability "Refresh"
		command "online"
		command "offline"
	}

	tiles(scale: 2) {
        
        standardTile("switch", "device.switch", width: 6, height: 6, canChangeIcon: true) {
    		state "off", label: 'Offline', icon: "st.Electronics.electronics18", backgroundColor: "#ff0000", action: "on", nextState: "turningon"
    		state "turningon", label: 'Turning on', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", nextState: "turningoff"
    		state "on", label: 'Online', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", action: "off", nextState: "turningoff"
    		state "turningoff", label: 'Turning off', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", nextState: "turningon"
		}
 		standardTile("refresh", "device.pressure", inactiveLabel: false, decoration: "flat") {
 			state "default", action:"poll", icon:"st.secondary.refresh"
 		}
    	main("switch")
        details(["switch", "refresh"])
    }
}

preferences {
  section("Computer Information") {
    input "computerIP","text", required:true, title: "Computer IP Address"
    input "computerPort","number", required: true, title: "Webserver port"
    input "macaddress", "text", required: true, title: "Computer MAC Address without"
    input "secureonpassword", "text", required: false, title: "SecureOn Password (Optional)"
    input "timeout","number", required: false, title: "Timeout"
  }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	log.debug "initialize"
	unschedule()
	runEvery1Minute("poll")
}

def poll() {
	log.debug "poll: Running"
    hubGet()
}

def refresh() {
	log.debug "refresh: Running"
    poll()
}

def hubGet() {
	def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/",
        headers: [
                "HOST" : "$computerIP:$computerPort",
                "Content-Type": "application/json"],
                null,
                [callback: parse]
	)
    log.debug "sendHubCommand"
    sendHubCommand(result);
    runIn(10, offline)
} 

def parse(physicalgraph.device.HubResponse hubResponse) {
    log.debug "in parse: $hubResponse"
    log.debug "hubResponse json: ${hubResponse.json}"
    unschedule(offline)
    online()
}

def on() {
	log.debug "on: Running"
    sendHubCommand(myWOL())
    def t=10
    if (timeout) {
    	t=timeout
    }
    runIn(t, actualizeSwitch)
}

def off() {
	log.debug "off: Running"
    sendHubCommand(myEventGhostShutdown())
    runIn(10, actualizeSwitch)
}

def actualizeSwitch() {
    def currentPresence = device.currentState("presence")
    def currentSwitch = device.currentState("switch")
    log.debug "actualizeSwitch: switch is ${currentSwitch?.value} and presence is ${currentPresence?.value}"
	if (currentPresence?.value != "present") {
	    log.debug "actualizeSwitch: sending forced off event"
		sendEvent(name: "switch", value: "off", isStateChange:true)
    }
    else {
	    log.debug "actualizeSwitch: sending forced on event"
		sendEvent(name: "switch", value: "on", isStateChange:true)
    }
}

def myWOL(evt) {
	//Determines if there is a secure password used for WOL
	if(secureonpassword){
    	//if a secure password exists
        //creates a new physicalgraph.device.hubaction
        def result = new physicalgraph.device.HubAction (
        	"wake on lan $macaddress",
        	physicalgraph.device.Protocol.LAN,
        	null,
        	[secureCode: "$secureonpassword"]
        )
        //returns the result
    	return result
    } else {
    	//if no secure password exists
        //creates a new physicalgraph.device.hubaction
    	log.debug "myWOL: SecureOn Password False"
        log.debug "myWOL: MAC Address $macaddress"
        def result = new physicalgraph.device.HubAction (
        	"wake on lan $macaddress",
        	physicalgraph.device.Protocol.LAN,
        	null
        )    
        //returns the result
        return result
    }
}

def myEventGhostShutdown(evt) {
	//concatenate computer IP and MAC
    //set even ghost command
    //encodes the eventghost command
	//creates new physical.device.graph from given values
	log.debug "eventghostPowerOff: Running"
	def egHost = computerIP + ":" + computerPort
	def egRawCommand = "ST.PCPower.Shutdown"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	def result = new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""",
    	physicalgraph.device.Protocol.LAN
    )
    //returns result
    return result
}

def online() {
    def currentSwitch = device.currentState("switch")
    def currentPresence = device.currentState("presence")
	if (currentPresence?.value != "present") {
    	sendEvent(name: "presence", value: "present")
    }
    if (currentSwitch?.value != "on") {
		sendEvent(name: "switch", value: "on")
    }
    log.debug "online: switch is ${currentSwitch?.value} and presence is ${currentPresence?.value}"
}

def offline() {
    def currentSwitch = device.currentState("switch")
    def currentPresence = device.currentState("presence")
	if (currentPresence?.value != "not present") {
    	sendEvent(name: "presence", value: "not present")
    }
    if (currentSwitch?.value != "off") {
		sendEvent(name: "switch", value: "off")
    }
    log.debug "offline: switch is ${currentSwitch?.value} and presence is ${currentPresence?.value}"
}
