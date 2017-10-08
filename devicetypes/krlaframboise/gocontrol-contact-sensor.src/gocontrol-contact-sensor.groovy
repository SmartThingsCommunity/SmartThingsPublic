/**
 *  GoControl Contact Sensor v1.6.1
 *  (WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.6.1 (08/02/2016)
 *      - Fixed iOS UI issue caused by using multiple states
 *        with a value tile.
 *
 *    1.6 (06/22/2016)
 *      - Added support for the external contact.
 *      - Added attributes for internal and external contact
 *        so you can use them independently, but the main
 *        contact reflects the last state of either contact.
 *
 *    1.5 (06/19/2016)
 *      -  Bug with initial battery reporting.
 *
 *    1.4.3 (06/17/2016)
 *      -  Fixed issue with battery level being debug logged.
 *
 *    1.4.2 (05/21/2016)
 *      -  Fixing polling so that it doesn't require forcing
 *         state changes or always displaying events.
 *
 *    1.4.1 (05/5/2016)
 *      -  UI Enhancements
 *      -  Added Debug Logging
 *      -  Fixed default tamper state
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
	definition (
		name: "GoControl Contact Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		
		attribute "internalContact", "enum", ["open", "close"]
		attribute "externalContact", "enum", ["open", "close"]
		attribute "lastPoll", "number"

		fingerprint deviceId: "0x2001", 
			inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"
	}

	// simulator metadata
	simulator {
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}
	
	preferences {
		input "reportBatteryEvery", "number", 
			title: "Report Battery Every? (Hours)", 
			defaultValue: 4,
			range: "4..167",
			displayDuringSetup: true, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}
	
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
			}
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No Tamper", backgroundColor: "#cccccc"			
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:""
		}
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh"])
	}
}

def parse(String description) {		
	def result = []
	if (description.startsWith("Err")) {
		result << createEvent(descriptionText:description, displayed:true)
	} 
	else {		
		def cmd = zwave.parse(description, [0x20: 1, 0x30: 2, 0x80: 1, 0x84: 2, 0x71: 3, 0x86: 1, 0x85: 2, 0x72: 2])		
		if (cmd) {		
			result += zwaveEvent(cmd)
		}
	}
		result << createEvent(name: "lastPoll",value: new Date().time, isStateChange: true, displayed: false)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	def reportEveryHours = settings.reportBatteryEvery ?: 4
	def reportEveryMS = (reportEveryHours * 60 * 60 * 1000)

	def result = []
	if (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) {
		logDebug "Requesting battery level"
		result << response(zwave.batteryV1.batteryGet().format())
		result << response("delay 3000")  
	}
	else {
		logDebug "Skipping battery check because it was already checked within the last $reportEveryHours hours."
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {	
	def map = [ 
		name: "battery", 		
		unit: "%"
	]
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.isStateChange = true
	}
	else {	
		def isNew = (device.currentValue("battery") != cmd.batteryLevel)
		map.value = cmd.batteryLevel
		map.displayed = isNew
		map.isStateChange = isNew
		logDebug "Battery is ${cmd.batteryLevel}%"
	}	
	
	state.lastBatteryReport = new Date().time	
	[
		createEvent(map)
	]
}

def installed() {
	return response([
		zwave.batteryV1.batteryGet().format(),
		zwave.basicV1.basicGet().format()
	])
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{	
	return createContactEvents(cmd.value, null)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{	
	
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	

	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 3:
				if (cmd.notificationStatus == 0xFF) {
					logDebug "Tamper is detected"
					state.lastBatteryReport = null
					result << createEvent(getTamperEventMap("detected"))		
				}
				break
			case 2:
				result += createContactEvents(cmd.v1AlarmLevel, "internalContact")
				break
			case 0xFE:
				result += createContactEvents(cmd.v1AlarmLevel, "externalContact")
				break
		}
	}	
	return result
}

private createContactEvents(val, contactType) {
	def contactVal = (val == 0xFF) ? "open" : "closed"
	def desc = "Contact is $contactVal"

	def result = []	
	
	result << createEvent(name: "contact", value: contactVal, isStateChange: true, descriptionText: desc)
	
	if (contactType) {
		logDebug "$desc ($contactType)"
		result << createEvent(name: contactType, value: contactVal, isStateChange: true, descriptionText: desc, displayed: false)
	}
	else {
		logDebug desc
	}
		
	if (device.currentValue("tamper") != "clear") {
		logDebug "Tamper is clear"
		result << createEvent(getTamperEventMap("clear"))
	}
	return result	
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
}

// Resets the tamper attribute to clear.
def refresh() {	
	state.lastBatteryReport = null
	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getTamperEventMap("clear"))		
	}
	
	logDebug "The Battery level will be refreshed the next time the device wakes up.  If you want this change to happen immediately, open the back cover of the device, wait until the red light turns solid, and then put the cover back on."
	return [zwave.batteryV1.batteryGet().format()]
}

def getTamperEventMap(val) {	
	def isNew = currentTamper() != val
	[
		name: "tamper", 
		value: val, 
		displayed: isNew,
		descriptionText: "Tamper is $val"
	]
}

private currentTamper() {
	return device.currentValue("tamper")
}

def logDebug(msg) {
	if (settings.debugOutput) {
		log.debug "${device.displayName}: $msg"
	}
}