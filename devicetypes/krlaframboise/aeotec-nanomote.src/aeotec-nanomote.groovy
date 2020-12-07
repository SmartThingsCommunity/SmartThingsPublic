/**
 *  Aeotec NanoMote One/Quad v1.1.1
 *  (Models: ZWA003-A/ZWA004-A)
 *
 *  Hank Scene Controller/Hank Four-Key Scene Controller
 *  (Models: HKZW-SCN01/HKZW-SCN04)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.1.1 (09/26/2020)
 *      - Implemented single button child devices for buttons 2-4, but didn't remove those buttons from the parent device for backwards compability.
 *      - YOU MUST INSTALL MY CHILD BUTTON DTH
 *
 *    1.0.2 (03/14/2020)
 *      - Removed vid because it breaks the new mobile app.
 *
 *    1.0.1 (07/19/2019)
 *      - Added meta definitions for new mobile app. 
 *			- Set value of supportedButtonValues attribute for new mobile app.
 *      - Set default value of numberOfButtons to 4. 
 *			- Added sequence number check to prevent duplicate button pushes.
 *			- Added support for button released in the new mobile app, but I had to use the "double" event because the button capability doesn't support the "released" event.
 *
 *    1.0 (05/26/2018)
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
		name: "Aeotec NanoMote", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.remotecontroller"
	) {
		capability "Sensor"
		capability "Battery"
		capability "Button"
		capability "Configuration"
		capability "Refresh"
		
		attribute "lastCheckIn", "string"
		attribute "lastAction", "string"
		
		fingerprint mfr:"0371", prod:"0102", model:"0003", deviceJoinName: "Aeotec NanoMote Quad"// ZWA003-A
		
		fingerprint mfr: "0208", prod:"0201", model: "000B", deviceJoinName: "Hank Four-Key Scene Controller" // HKZW-SCN04		
		
		fingerprint mfr:"0371", prod:"0102", model:"0004", deviceJoinName: "Aeotec NanoMote One" // ZWA004-A
					
		fingerprint mfr: "0208", prod:"0201", model: "0009", deviceJoinName: "Hank Scene Controller" // HKZW-SCN01
	}
	
	simulator { }
	
	preferences {		
		input "backwardsCompatible", "bool", 
			title: "Create events on parent device for buttons 2-4?", 
			defaultValue: false, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"lastAction", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.lastAction", key: "PRIMARY_CONTROL") {
				attributeState "lastAction", 
					label:'${currentValue}', 
					icon:"st.unknown.zwave.remote-controller", 
					backgroundColor:"#cccccc"				
			}	
			tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'Battery ${currentValue}%'
			}
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		main "lastAction"
		details(["lastAction", "refresh"])
	}
}


def installed() {
	state.pendingRefresh = true
	state.newInstall = true
	
	initialize()
}


def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		initialize()
		
		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."		
	}		
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}


private initialize() {
	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:4, displayed: false)	
	}
	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name: "supportedButtonValues", value: ["pushed", "held", "double"].encodeAsJSON(), displayed: false)
	}
	
	runIn(5, createChildButtons)
}

def createChildButtons() {	
	if (device.currentValue("numberOfButtons") == 4) {
		def children = childDevices
		
		(2..4).each { btnNum ->
			if (!children?.find { it.getDataValue("btnNum") == btnNum.toString() }) {
				addChildButton(btnNum)				
			}
		}		
	}
}

private addChildButton(btnNum) {	
	logDebug "Creating child for button ${btnNum}"
	
	try {
		def child = addChildDevice(
			"krlaframboise",
			"Child Button",
			"${device.deviceNetworkId}-Button${btnNum}", 
			device.getHub().getId(), 
			[
				completedSetup: true,
				isComponent: false,
				label: "${device.displayName}-${btnNum}",
				data: [btnNum: btnNum.toString()]
			]
		)
		child?.sendEvent(name:"supportedButtonValues", value:["pushed", "held", "double"].encodeAsJSON(), displayed:false)		
	}
	catch (e) {
		log.warn "You must have the krlaframboise Child Button DTH installed in order to use the child button devices"
	}
}
	

def configure() {
	logDebug "configure()"
	
	def cmds = []	
	if (!device.currentValue("battery") || state.pendingRefresh) {
		cmds << batteryGetCmd()
	}
	
	if (!device.currentValue("numberOfButtons") || state.pendingRefresh) {
		cmds << manufacturerSpecificGetCmd()
	}
	state.pendingRefresh = false
	return cmds ? delayBetween(cmds, 1000) : []
}

def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.pendingRefresh = true
}

private logForceWakeupMessage(msg) {
	logDebug("${msg}  You can force the device to wake up immediately by holding the button until the LED turns green.")
}


def parse(String description) {
	def result = []
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}
	catch (e) {
		log.error "$e"
	}
	
	sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
		
	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	
	def cmds = []	
	cmds += configure()
			
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()	
	return response(cmds)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
		
	if (val > 100) { val = 100 }
		
	sendEvent(getEventMap("battery", val, null, null, "%"))
	return []
}	


def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	logTrace "CentralSceneNotification: ${cmd}"
	
	if (state.lastSequenceNumber != cmd.sequenceNumber) {	
		state.lastSequenceNumber = cmd.sequenceNumber
		
		def btn = cmd.sceneNumber
		def action	
		switch (cmd.keyAttributes) {
			case 0:
				action = "pushed"
				break
			case 1:			
				action = "double" // Released
				break
			case 2:
				action = "held"
				break
		}
		
		if (action) {
			if ((btn == 1) || (settings?.backwardsCompatible == true) || (!state.newInstall && (settings?.backwardsCompatible != false))) {
				sendButtonEvent(btn, action)
			}
			
			if (btn > 1) {
				sendChildButtonEvent(childDevices.find { it.getDataValue("btnNum") == btn.toString() }, action)
			}
		}
	}
	return []
}

private sendButtonEvent(btn, action) {
	if (btn == 1) {
		logDebug "Button ${btn} ${action}" + (action == "double" ? " (released)" : "")
	}
	
	def lastAction = (device.currentValue("numberOfButtons") == 1) ? "${action}" : "${action} ${btn}"

	sendEvent(name:"lastAction", value: "${lastAction}".toUpperCase(), displayed: false)
		
	sendEvent(
		name: "button", 
		value: "${action}", 
		descriptionText: "Button ${btn} ${action}",
		data: [buttonNumber: btn],
		displayed: true,
		isStateChange: true)
}

private sendChildButtonEvent(child, action) {
	if (child) {
		logDebug "${child.displayName} ${action}" + (action == "double" ? " (released)" : "")
		
		child.sendEvent(
			name: "button",
			value: action,
			descriptionText: "${child.displayName} Pushed",
			data: [buttonNumber: 1],
			displayed: true,
			isStateChange: true)
	}
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "ManufacturerSpecificReport $cmd"
	
	def btnCount = 4
	if ((cmd.productTypeId == 258 && cmd.productId == 4) || (cmd.productTypeId == 513 && cmd.productId == 9)) {	
		// Aeotec NanoMote One or Hank Scene Controller
		btnCount = 1
	}
	if (btnCount != device.currentValue("numberOfButtons")) {
		sendEvent(name: "numberOfButtons", value: btnCount)
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


private getEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def isStateChange = (device.currentValue(name) != value)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange
	]
	if (desc) {
		eventMap.descriptionText = desc
	}
	if (unit) {
		eventMap.unit = unit
	}	
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}


private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private manufacturerSpecificGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private getCommandClassVersions() {
	[
		0x26: 2, // Switch Multilevel
		0x55: 1, // TransportServices (2)
		0x59: 1, // AssociationGrpInfo
		0x5A: 1, // DeviceResetLocally
		0x5B: 1, // Central Scene (3)
		0x5E: 2, // ZwaveplusInfo
		0x70: 1, // Configuration
		0x72: 2, // ManufacturerSpecific
		0x73: 1, // Powerlevel
		0x7A: 2, // FirmwareUpdateMd (3)
		0x80: 1, // Battery
		0x84: 2, // WakeUp
		0x85: 2, // Association
		0x86: 1, // Version (2)
		0x98: 1, // Security
		0xEF: 1	 // Mark		
		// Security S2
		// Supervision
	]
}


private getChildByBtnNum(btnNum) {
	return childDevices?.find { it.getDataValue("btnNum") == btnNum.toString() }
}


private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}	
	}
	catch (e) {
		return "$dt"
	}
}


private getLastEventTime(name) {
	return device.currentState("${name}")?.date?.time
}

private minutesElapsed(time, minutes) {
	if (time) {
		def cutoff = (new Date().time - (minutes * 60 * 1000)) + 5000
		return (time > cutoff)
	}
	else {
		return false
	}
}


private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
  // log.trace "$msg"
}