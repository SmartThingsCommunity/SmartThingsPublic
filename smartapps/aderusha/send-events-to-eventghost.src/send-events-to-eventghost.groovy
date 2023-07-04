/**
 *	Send Events to EventGhost
 *	
 *	Send SmartThings events to EventGhost
 *	
 *	https://github.com/aderusha/SmartThings/blob/master/Send-Events-to-EventGhost.groovy
 *	Copyright 2015 aderusha
 *	Version 1.0.0 - 2015-09-13 - Initial release
 *	Version 1.1.0 - 2015-09-15 - Changed handling of binary(ish) vs non-binary values to allow sending
 *	                             value data to EG to be handled via Python and eg.event.payload[]
 *	Version 1.2.0 - 2016-04-18 - Added support for individual button values
 *	
 *	This SmartApp will send selected events to an EventGhost server running the Webserver plugin.
 *	EventGhost is a Windows application used for event automation, find out more here: http://www.eventghost.org/
 *	How to setup the EventGhost Webserver plugin: http://www.eventghost.org/mediawiki/index.php?title=Webserver
 *	
 *	TODO:
 *	- Currently doesn't support user authentication or SSL.  EG Webserver authentication must be disabled by leaving
 *	  the username/password field in the plugin configuration blank
 *	- Figure out how to monitor Sonos "musicPlayer" events
 *	- Add additional capabilities to monitor
 *	
 *	ISSUES:
 *	- "Color" values are not being received by EG, presumably due to the "#" character being mishandled somehow
 *	
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *	
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *	
 */

// button handling is kinda working but the handler is disabled.  getting different responses from minimote vs scene 
// controller, should sort that out before tearing down the current minimote configs

definition(
    name: "Send Events to EventGhost",
    namespace: "aderusha",
    author: "aderusha",
    description: "Send SmartThings events to EventGhost",
    category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/aderusha/SmartThings/EventGhost_logo.png",
	iconX2Url: "https://s3.amazonaws.com/aderusha/SmartThings/EventGhost_logo@2x.png"
)

preferences {
	section("EventGhost server address and port"){
		input "egServer", "text", title: "Server", description: "EventGhost Web Server IP", required: true
		input "egPort", "number", title: "Port", description: "EventGhost Web Server Port", required: true, defaultValue: 80
	}
	section("EventGhost Command prefix"){
		input "egPrefix", "text", title: "Command prefix", required: false, defaultValue: "ST"
	}
	section("Select events to be sent to EventGhost"){
		input "mySwitch", "capability.switch", title: "Switches", required: false, multiple: true
		input "myDimmer", "capability.switchLevel", title: "Dimmers", required: false, multiple: true
		input "myColorControl", "capability.colorControl", title: "Color Controls", required: false, multiple: true
		input "myButton", "capability.button", title: "Buttons", required: false, multiple: true
		input "myMomentaryContact", "capability.momentary", title: "Momentary Contacts", required: false, multiple: true
		input "myMotion", "capability.motionSensor", title: "Motion Sensors", required: false, multiple: true
		input "myContact", "capability.contactSensor", title: "Contact Sensors", required: false, multiple: true
		input "myLock", "capability.lock", title: "Locks", required: false, multiple: true
		input "myThermostat", "capability.thermostat", title: "Thermostats", required: false, multiple: true
		input "myTemperature", "capability.temperatureMeasurement", title: "Temperature Sensors", required: false, multiple: true
		input "myBrightness", "capability.illuminanceMeasurement", title: "Light Sensors", required: false, multiple: true
		input "myHumidty", "capability.relativeHumidityMeasurement", title: "Humidty Sensors", required: false, multiple: true
		input "myEnergy", "capability.energyMeter", title: "Energy Sensors", required: false, multiple: true
		input "myPower", "capability.powerMeter", title: "Power Sensors", required: false, multiple: true
		input "myAcceleration", "capability.accelerationSensor", title: "Acceleration Sensors", required: false, multiple: true
		input "myPresence", "capability.presenceSensor", title: "Presence Sensors", required: false, multiple: true
		input "mySmoke", "capability.smokeDetector", title: "Smoke Sensors", required: false, multiple: true
		input "myWater", "capability.waterSensor", title: "Water Sensors", required: false, multiple: true
		input "myCO", "capability.carbonMonoxideDetector", title: "Carbon Monoxide Detectors", required: false, multiple: true
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
	subscribe(mySwitch, "switch", eventHandlerBinary)
	subscribe(myDimmer, "level", eventHandlerValue)
	subscribe(myColorControl, "color", eventHandlerValue)
	subscribe(myButton, "button", eventHandlerButton)
	subscribe(myMomentaryContact, "momentary", eventHandlerBinary)
	subscribe(myMotion, "motion", eventHandlerBinary)
	subscribe(myContact, "contact", eventHandlerBinary)
	subscribe(myLock, "lock", eventHandlerBinary)
	subscribe(myThermostat, "thermostat.thermostatMode", eventHandlerBinary)
	subscribe(myThermostat, "thermostat.thermostatFanMode", eventHandlerBinary)
	subscribe(myThermostat, "thermostat.thermostatOperatingState", eventHandlerBinary)
	subscribe(myThermostat, "thermostat.temperature", eventHandlerValue)
	subscribe(myThermostat, "thermostat.heatingSetpoint", eventHandlerValue)
	subscribe(myThermostat, "thermostat.coolingSetpoint", eventHandlerValue)
	subscribe(myThermostat, "thermostat.thermostatSetpoint", eventHandlerValue)
	subscribe(myTemperature, "temperature", eventHandlerValue)
	subscribe(myBrightness, "illuminance", eventHandlerValue)
	subscribe(myHumidty, "humidity", eventHandlerValue)
	subscribe(myEnergy, "energy", eventHandlerValue)
	subscribe(myPower, "power", eventHandlerValue)
	subscribe(myAcceleration, "acceleration", eventHandlerBinary)
	subscribe(myPresence, "presence", eventHandlerBinary)
	subscribe(mySmoke, "smoke", eventHandlerBinary)
	subscribe(myWater, "water", eventHandlerBinary)
	subscribe(myCO, "carbonMonoxide", eventHandlerBinary)
}

def eventHandlerBinary(evt) {
	def egHost = "${settings.egServer}:${settings.egPort}"
	def egRawCommand = "${settings.egPrefix}.${evt.displayName}.${evt.name}.${evt.value}"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	log.debug "processed binary event ${evt.name} from device ${evt.displayName} with value ${evt.value} and data ${evt.data}"
	log.debug "egRestCommand:  $egRestCommand"
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}

def eventHandlerValue(evt) {
	def egHost = "${settings.egServer}:${settings.egPort}"
	def egRawCommand = "${settings.egPrefix}.${evt.displayName}.${evt.name}"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	def egRestValue = java.net.URLEncoder.encode("${evt.value}")
	def egRestCommandValue = "$egRestCommand&$egRestValue"
	log.debug "processed data event ${evt.name} from device ${evt.displayName} with value ${evt.value} and data ${evt.data}"
	log.debug "egRestCommand:  $egRestCommandValue"
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?$egRestCommandValue HTTP/1.1\r\nHOST: $egHost\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}

def eventHandlerButton(evt) {
	def buttonNumber = evt.jsonData.buttonNumber
	def egHost = "${settings.egServer}:${settings.egPort}"
	def egRawCommand = "${settings.egPrefix}.${evt.displayName}.${evt.name}.$buttonNumber.${evt.value}"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	log.debug "processed button event ${evt.name} from device ${evt.displayName} with value ${evt.value} and button $buttonNumber"
	log.debug "egRestCommand:  $egRestCommand ,  [${egRestCommand}]"
	//sendHubCommand(new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""", physicalgraph.device.Protocol.LAN))
    
    sendHubCommand(new physicalgraph.device.HubAction(
    [
        path: "/?$egRestCommand",
        method: "GET",
        HOST: "${egHost}",
        headers: [
            "Host":"$egHost",
            "Accept":"application/json"
        ]        
    ],
    null,
    null
    ))
}