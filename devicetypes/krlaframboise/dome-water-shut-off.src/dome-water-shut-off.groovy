/**
 *  Dome Water Shut-Off v1.1
 *  (Model: DMWV1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to Forum Topic:  https://community.smartthings.com/t/release-dome-water-main-shut-off-official/75500?u=krlaframboise
 *    
 *  URL to Manual:  https://s3-us-west-2.amazonaws.com/dome-manuals/SmartThings/SmartThings+Water+Main+Shut-Off+Device+Handler.pdf
 *
 *  Changelog:
 *
 *    1.1 (02/09/2017)
 *      - Cleaned code for publication.
 *
 *    1.0 (01/26/2017)
 *      - Initial Release
 *
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
		name: "Dome Water Shut-Off", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Valve"
		capability "Switch"
		capability "Refresh"
		capability "Polling"
		
		attribute "status", "enum", ["open", "closed", "opening", "closing"]
		attribute "lastCheckin", "number"
		
		fingerprint deviceId: "0x1006", inClusters: "0x59, 0x5A, 0x5E, 0x72, 0x73, 0x85, 0x86, 0x25"
		
		fingerprint mfr:"021F", prod:"0003", model:"0002"
	}
	
	simulator { }
	
	preferences {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "default", 
					label:'', 
					icon:"st.valves.water.closed", 
					backgroundColor:"#ffffff"				
				attributeState "closing", 
					label:'Closing', 
					action: "valve.open", 
					icon:"st.valves.water.closed", 
					backgroundColor:"#ffe71e"
				attributeState "closed", 
					label:'Closed', 
					action: "valve.open", 
					icon:"st.valves.water.closed", 
					backgroundColor:"#e86d13",
					nextState: "opening"
				attributeState "opening", 
					label:'Opening', 
					action: "valve.close", 
					icon:"st.valves.water.open", 
					backgroundColor:"#ffe71e"
				attributeState "open", 
					label:'Open', 
					action: "valve.close", 
					icon:"st.valves.water.open", 
					backgroundColor:"#53a7c0",
					nextState: "closing"
			}
		}
		
		standardTile("openValve", "device.valve", width: 2, height: 2) {
			state "default",
				label:'Open',
				action:"valve.open",
				icon:"st.valves.water.open",
				nextState: "open",
				backgroundColor:"#53a7c0"
			state "open",
				label:'Open',
				action:"valve.open",
				icon:"st.valves.water.open",
				background: "#ffffff"	
		}
		
		standardTile("closeValve", "device.valve", width: 2, height: 2) {
			state "default",
				label:'Close',
				action:"valve.close",
				icon:"st.valves.water.closed",
				backgroundColor:"#e86d13",
				nextState: "closed"
			state "closed",
				label:'Close',
				action:"valve.close",
				icon:"st.valves.water.closed",
				background: "#ffffff"	
		}
	
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", 
				label: 'Refresh', 
				action: "refresh.refresh", 
				icon: "st.secondary.refresh-icon",
				background: "#A9A9A9"
		}		
		main "status"
		details(["status", "openValve", "closeValve", "refresh"])
	}
}

// Refreshes the valve status.
def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"		
		return response(refresh())
	}
}

// Refreshes the valve status.
def poll() {
	logTrace "poll()"
	if (canCheckin()) {			
		return refresh()
	}
	else {
		logDebug "Ignored poll request because it hasn't been long enough since the last poll."
	}
}

// Refreshes the valve status.
def refresh() {
	logDebug "Requesting valve position."
	return [switchBinaryGetCmd()]
}

// Opens the valve.
def on() {
	return open()
}

// Opens the valve.
def open() {
	logTrace "Executing open()"
	return toggleValve(openStatus)
}

// Closes the valve.
def off() {
	return close()
}

// Closes the valve.
def close() {
	logTrace "Executing close()"
	return toggleValve(closedStatus)
}

private toggleValve(pending) {
	state.pending = pending
	state.pending.abortTime = (new Date().time + (30 * 60 * 1000))
	logDebug "${pending.pendingValue.capitalize()} Valve"
	return [		
		switchBinarySetCmd(pending.cmdValue),
		switchBinaryGetCmd(),
		"delay 9000",
		switchBinaryGetCmd()
	]
}

// Handles device response.
def parse(String description) {
	def result = []

	def cmd = zwave.parse(description, getCommandClassVersions())
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	
	if (canCheckin()) {
		result << createEvent(name: "lastCheckin",value: new Date().time, isStateChange: true, displayed: false)
	}
	
	return result
}

private getCommandClassVersions() {
	[
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x25: 1  // Switch Binary
	]
}

// Creates events based on state of valve.
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: $cmd"
	def result = []
	def reported = (cmd.value == openStatus.cmdValue) ? openStatus : closedStatus
	
	if (state.pending?.abortTime && state.pending?.abortTime > new Date().time) {
		result << createEvent(getEventMap("status", state.pending.pendingValue, false))
		state.pending = null
	}
	else {
		logDebug "Valve is ${reported.value.capitalize()}"
		if (device.currentValue("status") != reported.value) {
			result << createEvent(getEventMap("status", reported.value, false))
		}
		result << createEvent(getEventMap("switch", reported.switchValue, false))
		result << createEvent(getEventMap("valve", reported.value))	
	}	
	return result
}

// Handles unexpected device event.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


private switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}

private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private getEventMap(name, value, displayed=null) {	
	def isStateChange = (device.currentValue(name) != value)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange
	]
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}

private getOpenStatus() {
	return [
		value: "open",		
		pendingValue: "opening",
		switchValue: "on",
		cmdValue: 0xFF,		
		pendingCmdValue: 0x00
	]
}

private getClosedStatus() { 
	return [
		value: "closed",
		pendingValue: "closing",
		switchValue: "off",
		cmdValue: 0x00,
		pendingCmdValue: 0xFF
	]
}

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}