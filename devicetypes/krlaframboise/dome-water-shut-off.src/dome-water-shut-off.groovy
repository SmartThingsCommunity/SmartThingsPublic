/**
 *  Dome Water Shut-Off v1.2.4
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
 *    1.2.3 (08/15/2018)
 *      - Added support for new mobile app.
 *
 *    1.2.3 (reverted)
 *
 *    1.2.2 (12/25/2017)
 *      - Implemented ST new color scheme.
 *
 *    1.2.1 (10/15/2017)
 *      - Added workaround for new SmartThings bug with setting state values to null.
 *
 *    1.2 (03/11/2017)
 *      - Added health check capability and self polling functionality.
 *      - Removed Polling capability.
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
		author: "Kevin LaFramboise",
		vid: "generic-valve"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Valve"
		capability "Switch"
		capability "Refresh"
		capability "Health Check"
		
		attribute "status", "enum", ["open", "closed", "opening", "closing"]
		attribute "lastCheckin", "string"
		
		fingerprint deviceId: "0x1006", inClusters: "0x59, 0x5A, 0x5E, 0x72, 0x73, 0x85, 0x86, 0x25"
		
		fingerprint mfr:"021F", prod:"0003", model:"0002"
	}
	
	simulator { }
	
	preferences {
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
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
					backgroundColor:"#ffffff"
				attributeState "closed", 
					label:'Closed', 
					action: "valve.open", 
					icon:"st.valves.water.closed", 
					backgroundColor:"#ffffff",
					nextState: "opening"
				attributeState "opening", 
					label:'Opening', 
					action: "valve.close", 
					icon:"st.valves.water.open", 
					backgroundColor:"#00a0dc"
				attributeState "open", 
					label:'Open', 
					action: "valve.close", 
					icon:"st.valves.water.open", 
					backgroundColor:"#00a0dc",
					nextState: "closing"
			}
		}
		
		standardTile("openValve", "device.valve", width: 2, height: 2) {
			state "default",
				label:'Open',
				action:"valve.open",
				icon:"st.valves.water.open",
				nextState: "open",
				backgroundColor:"#00a0dc"
			state "open",
				label:'Open',
				action:"valve.open",
				icon:"st.valves.water.open",
				background: "#00a0dc"	
		}
		
		standardTile("closeValve", "device.valve", width: 2, height: 2) {
			state "default",
				label:'Close',
				action:"valve.close",
				icon:"st.valves.water.closed",
				backgroundColor:"#ffffff",
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

// Refreshes the valve status and sets the health check expected interval.
def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"		
		
		initializeCheckin()
		
		return response(refresh())
	}
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	switch (checkinIntervalSettingMinutes) {
		case 5:
			runEvery5Minutes(healthPoll)
			break
		case 10:
			runEvery10Minutes(healthPoll)
			break
		case 15:
			runEvery15Minutes(healthPoll)
			break
		case 30:
			runEvery30Minutes(healthPoll)
			break
		case [60, 120]:
			runEvery1Hour(healthPoll)
			break
		default:
			runEvery3Hours(healthPoll)			
	}
}

// Executed by internal schedule and requests version report to determine if the device is still online.
def healthPoll() {
	logTrace "healthPoll()"
	sendHubCommand(new physicalgraph.device.HubAction(versionGetCmd()))
}

// Executed by SmartThings if the specified checkInterval is exceeded.
def ping() {
	logTrace "ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return versionGetCmd()
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
	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		result << createLastCheckinEvent()
	}	
	return result
}

// Requested by health poll to verify that it's still online.
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	return []
}

// Creates events based on state of valve.
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: $cmd"
	def result = []
	def reported = (cmd.value == openStatus.cmdValue) ? openStatus : closedStatus
	
	if (state.pending?.abortTime && state.pending?.abortTime > new Date().time) {
		result << createEvent(createEventMap("status", state.pending.pendingValue, false))
		state.pending = [:]
	}
	else {
		logDebug "Valve is ${reported.value.capitalize()}"
		if (device.currentValue("status") != reported.value) {
			result << createEvent(createEventMap("status", reported.value, false))
		}
		result << createEvent(createEventMap("switch", reported.switchValue, false))
		result << createEvent(createEventMap("valve", reported.value))	
	}	
	return result
}

// Handles unexpected device event.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


private versionGetCmd() {
	return zwave.versionV1.versionGet().format()
}

private switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}

private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}


private createLastCheckinEvent() {
	logDebug "Device Checked In"
	state.lastCheckinTime = new Date().time
	return createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
}

private convertToLocalTimeString(dt) {
	return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(location.timeZone.ID))
}

private createEventMap(name, value, displayed=null) {	
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

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 720
}
private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getCheckinIntervalOptions() {
	[
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: formatDefaultOptionName("12 Hours"), value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
	]
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private formatDefaultOptionName(val) {
	return "${val}${defaultOptionSuffix}"
}

private findDefaultOptionName(options) {
	def option = options?.find { it.name?.contains("${defaultOptionSuffix}") }
	return option?.name ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private int safeToInt(val, int defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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