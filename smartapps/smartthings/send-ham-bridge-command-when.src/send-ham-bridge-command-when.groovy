/**
 *  Send HAM Bridge Command When…
 *  
 *  For more information about HAM Bridge please visit http://solutionsetcetera.com/HAMBridge/
 *
 *  Copyright 2014 Scottin Pollock
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
    name: "Send HAM Bridge Command When",
    namespace: "smartthings",
    author: "Scottin Pollock",
    description: "Sends a command to your HAM Bridge server when SmartThings are activated.",
    category: "Convenience",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/HB.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/HB@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
	}
	section("Send this command to HAM Bridge"){
		input "HAMBcommand", "text", title: "Command to send", required: true
	}
	section("Server address and port number"){
		input "server", "text", title: "Server IP", description: "Your HAM Bridger Server IP", required: true
		input "port", "number", title: "Port", description: "Port Number", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
}

def eventHandler(evt) {
	sendHttp()
}

def sendHttp() {
def ip = "${settings.server}:${settings.port}"
def deviceNetworkId = "1234"
sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.HAMBcommand} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}