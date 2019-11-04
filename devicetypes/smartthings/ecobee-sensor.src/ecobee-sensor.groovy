/**
 *  Copyright 2015 SmartThings
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
 *  See Changelog for change history
 *
 *  Current Version: 0.7.5
 *  Release Date: 20160125
 *  See separate Changelog for change history
 */

def getVersionNum() { return "0.9.0" }
private def getVersionLabel() { return "Ecobee Sensor Version 0.9.0-RC2" }

metadata {
	definition (name: "Ecobee Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Health Check"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Motion Sensor"
		capability "Refresh"
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", defaultState: true, label:'${currentValue}°', unit:"dF",
				backgroundColors:[
                	// Celsius Color Range
					[value: 0, color: "#1e9cbb"],
					[value: 15, color: "#1e9cbb"],
					[value: 19, color: "#1e9cbb"],

					[value: 21, color: "#44b621"],
					[value: 22, color: "#44b621"],
					[value: 24, color: "#44b621"],

					[value: 21, color: "#d04e00"],
					[value: 35, color: "#d04e00"],
					[value: 37, color: "#d04e00"],
					// Fahrenheit Color Range
                	[value: 40, color: "#1e9cbb"],
					[value: 59, color: "#1e9cbb"],
					[value: 67, color: "#1e9cbb"],

					[value: 69, color: "#44b621"],
					[value: 72, color: "#44b621"],
					[value: 74, color: "#44b621"],

					[value: 76, color: "#d04e00"],
					[value: 95, color: "#d04e00"],
					[value: 99, color: "#d04e00"],
					[value: 99, color: "#d04e00"],
					[value: 451, color: "#ffa81e"] // Nod to the book and temp that paper burns. Used to catch when the device is offline
				]
			)
		}

		standardTile("motion", "device.motion") {
			state("active", label:'Motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'No Motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
            state("unknown", label:'Offline', icon:"st.contact.contact.open", backgroundColor:"ffa81e")
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["temperature","motion"])
		details(["temperature","motion","refresh"])
	}
}

def initialize() {
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
	updateDataValue("EnrolledUTDH", "true")
}

void installed() {
	initialize()
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	parent.pollChild()


def generateEvent(Map results) {
	log.debug "generateEvent(): parsing data $results. F or C? ${getTemperatureScale()}"
	if(results) {
		results.each { name, value ->
			def linkText = getLinkText(device)
			def isChange = false
			def isDisplayed = true
			def event = [name: name, linkText: linkText, handlerName: name]

			if (name=="temperature") {
				def sendValue = value

                if (sendValue == "unknown") {
                	// We are OFFLINE
                    LOG( "Warning: Remote Sensor (${name}) is OFFLINE. Please check the batteries or move closer to the thermostat.", 2, null, "warn")
                    state.onlineState = false
                    sendValue = "unknown"
                } else {
                	// must be online
                    state.onlineState = true
                }

				isChange = isTemperatureStateChange(device, name, sendValue.toString())
				isDisplayed = isChange
				event << [value: sendValue, isStateChange: isChange, displayed: isDisplayed]

			} else if (name=="motion") {
            	def sendValue = value

                if ( (sendValue == "unknown") || (!state.onlineState) ) {
                	// We are OFFLINE
                    LOG( "Warning: Remote Sensor (${name}) is OFFLINE. Please check the batteries or move closer to the thermostat.", 2, null, "warn")
                    sendValue = "unknown"
                }

				isChange = isStateChange(device, name, sendValue.toString())
				isDisplayed = isChange
				event << [value: sendValue.toString(), isStateChange: isChange, displayed: isDisplayed]
			} else {
            	event << [value: value.toString()]

            }
			sendEvent(event)
		}
	}
}



//generate custom mobile activity feeds event
def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}


private debugLevel(level=3) {
	def debugLvlNum = parent.settings.debugLevel?.toInteger() ?: 3
    def wantedLvl = level?.toInteger()

    return ( debugLvlNum >= wantedLvl )
}



private def LOG(message, level=3, child=null, logType="debug", event=false, displayEvent=false) {
	def prefix = ""
	if ( parent.settings.debugLevel?.toInteger() == 5 ) { prefix = "LOG: " }
	if ( debugLevel(level) ) {
    	log."${logType}" "${prefix}${message}"
        // log.debug message
        if (event) { debugEvent(message, displayEvent) }
	}
}


private def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	if ( debugLevel(4) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)
}
