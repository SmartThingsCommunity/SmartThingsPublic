/**
 *  ST_Anything_Doors Device Type - ST_Anything_Doors.device.groovy
 *
 *  Copyright 2015 Daniel Ogorchock
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2015-01-10  Dan Ogorchock  Original Creation
 *
 *
 */

metadata {
	definition (name: "ST_Anything_Doors", namespace: "Arduino:", author: "Daniel Ogorchock") {
		capability "Configuration"
		capability "Contact Sensor"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Sensor"
		capability "Polling"

		attribute "overheadDoor", "string"
		attribute "frontDoor", "string"

		command "relay"

	}

    simulator {
        status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
        status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

        // reply messages
        reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
        reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
    }
	
    // Preferences
	preferences {
    	input "temphumidSampleRate", "number", title: "Temperature/Humidity Sensor Sampling Interval (seconds)", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
	}

	// tile definitions
	tiles {
		standardTile("overheadDoor", "device.overheadDoor", width: 1, height: 1, canChangeIcon: false, canChangeBackground: true) {
			state "on", label: 'Garage', action: "garageRelay", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: 'Garage', action: "garageRelay", icon: "st.switches.switch.off", backgroundColor: "#79b821",  nextState:"switching"
            state "switching", label: 'Garage', action: "RelayOff2", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
 		}

        standardTile("frontDoor", "device.frontDoor", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
 		}

        valueTile("temperature", "device.temperature", width: 1, height: 1) {
			state("temperature", label:'${currentValue}°', 
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
        standardTile("motion", "device.motion", width: 1, height: 1) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        
        main (["temperature","humidity","motion"])
        details(["temperature","humidity","motion","overheadDoor","frontDoor","configure"])
	}
}

//Map parse(String description) {
def parse(String description) {
    def msg = zigbee.parse(description)?.text
    log.debug "Parse got '${msg}'"

    def parts = msg.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null

    name = value != "ping" ? name : null

    def result = createEvent(name: name, value: value)

    log.debug result

    return result
}

def garageRelay() {
	log.debug "Executing - toggle replay"
    zigbee.smartShield(text: "garageRelay").format()
}

def poll() {
	//temporarily implement poll() to issue a configure() command to send the polling interval settings to the arduino
	configure()
}

def configure() {
	log.debug "Executing 'configure'"
	log.debug "temphumid " + temphumidSampleRate
	[
        zigbee.smartShield(text: "temphumid " + temphumidSampleRate).format()
    ]
}