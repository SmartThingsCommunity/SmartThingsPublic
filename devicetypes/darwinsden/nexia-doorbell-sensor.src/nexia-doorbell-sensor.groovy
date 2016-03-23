/**
 *  Nexia Doorbell Sensor
 *
 *  Capabilities:
 *					Switch, Alarm, Battery, Configuration, Refresh
 *
 *  Copyright 2016 Darwin (DarwinsDen.com)
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
 *	Aeon Siren
 *
 *	Author: Darwin (DarwinsDen.com)
 *	Date: 2016-03-20
 *
 *	Changelog:
 *
 *	0.1 (03/20/2016)
 *		-	Initial Release
 *	0.2 (03/22/2016)
 *		-	handle missed button press and release events
 *
 */
 
metadata {
	definition (name: "Nexia Doorbell Sensor", namespace: "darwinsden", author: "Darwin") {
		capability "Configuration"
		capability "Switch"
		capability "Alarm"
		capability "Battery"
		capability "Refresh"

		attribute "status", "enum", ["off", "doorbell"]
        
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x73, 0x80, 0x70, 0x71, 0x85, 0x59, 0x84, 0x7A"
	}

	simulator {
	}

	preferences {
	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
				tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
					attributeState "off", label: "Off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
					attributeState "doorbell", label: "Ringing", icon:"st.Home.home30", backgroundColor:"#53a7c0"
				}
        }

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status", "battery", "refresh"])
	}
}

// Reload attributes from device configuration.
def refresh() {
	def result = []
	result += configure()
	result += refreshDeviceAttr()
	return result
}

private refreshDeviceAttr() {
	secureDelayBetween([
		doorbellGetCmd(),
		batteryHealthGetCmd()
	])
}

// Handles device reporting on and off
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		handleDeviceTurningOff()
	} 
	else if (cmd.value == 255) {
		sendDoorbellEvents()
	}
}

// Raises events switch.off, alarm.off, and status.off
def handleDeviceTurningOff() {
	[
		createEvent(name:"status", value: "off", isStateChange: true),
		createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off", isStateChange: true, displayed: false),
		createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off", isStateChange: true, displayed: false)
	]
}

// Checks battery level on wakeup notification
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug("WakeUpNotification: $cmd")

	def result = []
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)

	// Request every 24 hours
	if (!state.lastBatteryReport || (new Date().time) - state.lastBatteryReport > 24*60*60*1000) {
		result << response(batteryHealthGetCmd())
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	result
}

private batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	def batteryLevel = cmd.batteryLevel

	sendEvent(name: "battery", value: batteryLevel, unit: "%", descriptionText: "$batteryLevel", isStateChange: true)
	logInfo("Battery: $batteryLevel")
}

def turnOffSwitch() {
     def desc = "Doorbell verification check"
	 logDebug("$desc")
	 sendEvent(name: "status", value: "off", descriptionText: "${device.displayName} $desc", isStateChange: true)
	 sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
}

private notificationReport(cmd) {
    def notificationEvent = cmd.event
    
    //Set switch "on" if its a button press, or if it's a button release and the button hasn't been
    //pressed in the last 10 seconds (likely a missed press)
    
    if (notificationEvent == 1 || (!state.lastBellRing  || (new Date().time) - state.lastBellRing  > 10*1000))
    {
	  def desc = "Doorbell button is pressed"
	  logDebug("$desc")
	  sendEvent(name: "status", value: "doorbell", descriptionText: "${device.displayName} $desc", isStateChange: true)
	  sendEvent(name: "switch", value: "on", displayed: false, isStateChange: true)
      state.lastBellRing = new Date().time
      runIn(10, turnOffSwitch) //turn off switch in 10 seconds in case button release event is missed   
    }
    else // it's a button release and it's within 10 seconds of the button press
    {
      def desc = "Doorbell button is released"
	  logDebug("$desc")
	  sendEvent(name: "status", value: "off", descriptionText: "${device.displayName} $desc", isStateChange: true)
	  sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
    }
}

// Unexpected command received
def zwaveEvent (physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled Command: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

//Battery Level received
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
   logDebug("Battery Level: $cmd")
   batteryHealthReport(cmd)
}

//Notifical received
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport  cmd) {
   logDebug("Notification Report: $cmd")
   notificationReport(cmd)
}

// Parses incoming message warns if not paired securely
def parse(description) {
    log.debug("'$description'")
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent(descriptionText: description, isStateChange: true)
	} else if (description != "updated") {
		//def cmd = zwave.parse(description)
        //def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1])
        def cmd = zwave.parse(description, [0x71: 3, 0x80: 1])

		if (cmd) {
			result = zwaveEvent(cmd)
			//log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

// Send configuration
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 500)) {		
		state.lastUpdated = new Date().time
		state.debugOutput = validateBool(debugOutput, true)
		
		logDebug("Updating")
			
		response(refreshDeviceAttr())
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

// Sends secure configuration to device
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCommands = true
	logDebug("Secure Commands Supported")
}

// Initializes variables and sends settings to device
def configure() {
	state.debugOutput = validateBool(debugOutput, true)
	state.useSecureCommands = null
	
	logDebug "Sending configuration to ${device.displayName}"
		
	secureDelayBetween([
		supportedSecurityGetCmd(),
		assocSetCmd(),
		sendLowBatterySetCmd()
	])
}

private sendAttrChangeEvent(attrName, attrVal) {
	def desc = "$attrName set to $attrVal"
	logDebug(desc)
	sendEvent(name: attrName, value: attrVal, descriptionText: "${device.displayName} $desc", displayed: false)
}

private validateBool(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private supportedSecurityGetCmd() {
	zwave.securityV1.securityCommandsSupportedGet()
}

private assocSetCmd() {
	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
}

private batteryHealthGetCmd() {
	configGetCmd(42)
}

private sendLowBatterySetCmd() {
	configSetCmd(1, 1)
}

private doorbellGetCmd() {
	configGetCmd(5)
}

private configGetCmd(int paramNum) {
	zwave.configurationV1.configurationGet(parameterNumber: paramNum)
}

private configSetCmd(int paramNum, int val) {
	zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val)
}

private getNumAttr(attrName) {
	def result = device.currentValue(attrName)
	if(!result) {
		result = 0
	}
	return result
}

private secureDelayBetween(commands, delay=100) {
	delayBetween(commands.collect{ secureCommand(it) }, delay)
}

private secureCommand(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCommands == null || state.useSecureCommands) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		cmd.format()
	}
}

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} $msg"
}