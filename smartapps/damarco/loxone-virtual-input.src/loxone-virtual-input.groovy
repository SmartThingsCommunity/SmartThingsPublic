/**
 *  Loxone Virtual Input
 *
 *  Copyright 2016 Daniel Schmidt
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
    name: "Loxone Virtual Input",
    namespace: "damarco",
    author: "Daniel Schmidt",
    description: "Trigger a Loxone Virtual Input.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When...") {
		input "motion", "capability.motionSensor", title: "Motion", required: false
        input "contact", "capability.contactSensor", title: "Contact", required: false
        input "presence", "capability.presenceSensor", title: "Presence", required: false
        input "water", "capability.waterSensor", title: "Water", required: false
        input "temperature", "capability.temperatureMeasurement", title: "Temperature", required: false
		input "humidity", "capability.relativeHumidityMeasurement", title: "Humidity", required: false
	}
    section("Trigger Loxone...") {
		input "ip", "text", title: "Miniserver IP", required: true
        input "port", "number", title: "Miniserver Port", required: true
        input "username", "text", title: "Miniserver Username", required: true
        input "password", "password", title: "Miniserver Password", required: true
        input "vi", "text", title: "Virtual Input Name", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motion, "motion.active", eventHandler)
    subscribe(motion, "motion.inactive", eventHandler)
    subscribe(contact, "contact.open", eventHandler)
    subscribe(contact, "contact.closed", eventHandler)
    subscribe(presence, "presence.present", eventHandler)
    subscribe(presence, "presence.not present", eventHandler)
    subscribe(water, "water.dry", eventHandler)
    subscribe(water, "water.wet", eventHandler)
    subscribe(temperature, "temperature", numberHandler)
    subscribe(humidity, "humidity", numberHandler)
}

def eventHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
    
    def command
    
    switch (evt.value) {
    	case "active":
        	command = "On"
            break
		case "closed":
        	command = "On"
            break
        case "present":
        	command = "On"
            break
		case "wet":
        	command = "On"
            break
		case "inactive":
        	command = "Off"
            break
		case "open":
        	command = "Off"
            break
		case "not present":
        	command = "Off"
            break
		case "dry":
        	command = "Off"
            break
	}
    
    loxoneCall(command)
}

def numberHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
    
    def command = evt.doubleValue
    
    loxoneCall(command)
}

def loxoneCall(command) {
    def auth = "$username:$password"
    def auth64 = auth.bytes.encodeBase64()
           
	def httpRequest = [
          	method:		"GET",
            path: 		"/dev/sps/io/$vi/$command",
            headers:	[HOST:		"$ip:$port",
    						Accept: 	"*/*",
                            Authorization: "Basic $auth64"
                        ]
    	]

    def hubAction = new physicalgraph.device.HubAction(httpRequest)
    sendHubCommand(hubAction)
}

