/**
 *  ST_AnyThing_ThingShield.device.groovy
 *
 *  Copyright 2014 Dan G Ogorchock & Daniel J Ogorchock
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
 *    2015-01-03  Dan & Daniel   Original Creation
 *    2017-02-12  Dan Ogorchock  Renamed to reflect use only with ThingShield
 *
 */
 
metadata {
	definition (name: "ST_AnyThing_ThingShield", namespace: "ogiewon", author: "Daniel Ogorchock") {
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Water Sensor"
		capability "Motion Sensor"
		capability "Switch"
		capability "Sensor"
		capability "Alarm"
		capability "Contact Sensor"
		capability "Polling"

		command "test"
		command "alarmoff"

	}

    simulator {
 
    }


    // Preferences
	preferences {
    	input "illuminanceSampleRate", "number", title: "Light Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
    	input "temphumidSampleRate", "number", title: "Temperature/Humidity Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
    	input "waterSampleRate", "number", title: "Water Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
	}

	// Tile Definitions
	tiles {
		valueTile("illuminance", "device.illuminance", width: 1, height: 1) {
			state("illuminance", label:'${currentValue} ${unit}', unit:"lux",
				backgroundColors:[
					[value: 9, color: "#767676"],
					[value: 315, color: "#ffa81e"],
					[value: 1000, color: "#fbd41b"]
				]
			)
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


        standardTile("water", "device.water", width: 1, height: 1) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}

		standardTile("motion", "device.motion", width: 1, height: 1) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
        
        standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}

		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		standardTile("contact", "device.contact", width: 1, height: 1) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}

		standardTile("alarm", "device.alarm", width: 1, height: 1) {
			state "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
            state "strobe", label:'', action:'alarmoff', icon:"st.secondary.strobe", backgroundColor:"#cccccc"
            state "siren", label:'siren!', action:'alarmoff', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13"
			state "both", label:'alarm!', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}

		standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"test", icon:"st.secondary.test"
		}
        
		standardTile("off", "device.alarm", , width: 1, height: 1) {
			state "default", label:'Alarm', action:"alarmoff", icon:"st.secondary.off"
		}
		//standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
		//	state "default", label:'', action:"alarmoff", icon:"st.secondary.off"
		//}

        main(["motion","temperature","humidity","illuminance","switch","contact","alarm","water"])
        details(["motion","temperature","humidity","illuminance","switch","contact","alarm","test","off","water","configure"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
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

// handle commands

def on() {
	log.debug "Executing 'switch on'"
	zigbee.smartShield(text: "switch on").format()
}

def off() {
	log.debug "Executing 'switch off'"
	zigbee.smartShield(text: "switch off").format()
}

def alarmoff() {
	log.debug "Executing 'alarm off'"
	zigbee.smartShield(text: "alarm off").format()
}

def strobe() {
	log.debug "Executing 'alarm strobe'"
	zigbee.smartShield(text: "alarm strobe").format()
}

def siren() {
	log.debug "Executing 'alarm siren'"
	zigbee.smartShield(text: "alarm siren").format()
}

def both() {
	log.debug "Executing 'alarm both'"
	zigbee.smartShield(text: "alarm both").format()
}

def test() {
	log.debug "Executing 'alarm test'"
	[
		zigbee.smartShield(text: "alarm both").format(),
		"delay 3000",
		zigbee.smartShield(text: "alarm off").format()
	]
}

def poll() {
	//temporarily implement poll() to issue a configure() command to send the polling interval settings to the arduino
	configure()
}


def configure() {
	log.debug "Executing 'configure'"
    //log.debug "illuminance " + illuminanceSampleRate + "|temphumid " + temphumidSampleRate + "|water " + waterSampleRate
    log.debug "water " + waterSampleRate
    log.debug "illuminance " + illuminanceSampleRate
	log.debug "temphumid " + temphumidSampleRate
	[
		zigbee.smartShield(text: "water " + waterSampleRate).format(),
        "delay 1000",
        zigbee.smartShield(text: "illuminance " + illuminanceSampleRate).format(),
        "delay 1000",
        zigbee.smartShield(text: "temphumid " + temphumidSampleRate).format()
    ]
}