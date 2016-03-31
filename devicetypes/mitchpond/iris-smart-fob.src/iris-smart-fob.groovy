/**
 *  Iris Smart Fob
 *
 *  Copyright 2015 Mitch Pond
 *	Presence code adapted from SmartThings Arrival Sensor HA device type
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
	definition (name: "Iris Smart Fob", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Battery"
		capability "Button"
        capability "Configuration"
		capability "Presence Sensor"
		capability "Sensor"

		command "testCmd"
        
		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0B05", outClusters: "0003,0006,0019", model:"3450-L", manufacturer: "CentraLite"
	}
    
    preferences{
	    input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
    		defaultValue: 3, displayDuringSetup: false)
        input "checkInterval", "enum", title: "Presence timeout (minutes)",
            defaultValue:"2", options: ["2", "3", "5"], displayDuringSetup: false
        input "logging", "bool", title: "Enable debug logging",
            defaultValue: false, displayDuringSetup: false
    }

	tiles(scale: 2) {
    	standardTile("presence", "device.presence", width: 4, height: 4, canChangeBackground: true) {
            state "present", label: "Present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
            state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
        }
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff", action: "testCmd"
        }
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["presence","battery"])
		details(["presence","button","battery"])
	}
}

def parse(String description) {
    def descMap = zigbee.parseDescriptionAsMap(description)
    logIt descMap
    state.lastCheckin = now()
    logIt "lastCheckin = ${state.lastCheckin}"
    handlePresenceEvent(true)
    
	def results = []
    if (description?.startsWith('catchall:'))
		results = parseCatchAllMessage(descMap)
	else if (description?.startsWith('read attr -'))
		results = parseReportAttributeMessage(descMap)
    else logIt(descMap, "trace")
        
	return results;
}

def updated() {
	startTimer()
    configure()
}

def configure(){
	logIt "Configuring Smart Fob..."
	[
	"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 2 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 3 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 4 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 1 1 0x20 {${device.zigbeeId}} {}", "delay 200"
    ] +
    zigbee.configureReporting(0x0001,0x0020,0x20,20,20,0x01) +
    zigbee.writeAttribute(0x0020,0x0000,0x23,0xF0)
}

def parseCatchAllMessage(descMap) {
    if (descMap?.clusterId == "0006" && descMap?.command == "01") 		//button pressed
    	handleButtonPress(descMap.sourceEndpoint as int)
    else if (descMap?.clusterId == "0006" && descMap?.command == "00") 	//button released
    	handleButtonRelease(descMap.sourceEndpoint as int)
    else if (descMap?.clusterId == "0020" && descMap?.command == "00") 	//poll control check-in
    	zigbee.command(0x0020, 0x00, "00", "0000")
    else logIt("Parse: Unhandled message: ${descMap}","trace")
}

def parseReportAttributeMessage(descMap) {
	if (descMap?.cluster == "0001" && descMap?.attrId == "0020") createBatteryEvent(getBatteryLevel(descMap.value))
    else logIt descMap
}

private createBatteryEvent(percent) {
	logIt "Battery level at " + percent
	return createEvent([name: "battery", value: percent])
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private handleButtonRelease(button) {
	logIt "lastPress state variable: ${state.lastPress}"
    def sequenceError = {logIt("Uh oh...missed a message? Dropping this event.", "error"); state.lastPress = null; return []}	
    
    if (!state.lastPress) return sequenceError()
	else if (state.lastPress.button != button) return sequenceError()
    
    def currentTime = now()
    def startOfPress = state.lastPress?.time
    def timeDif = currentTime - startOfPress
    def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000
    
    state.lastPress = null	//we're done with this. clear it to make error conditions easier to catch
    
    if (timeDif < 0) 
    //likely a message sequence issue or dropped packet. Drop this press and wait for another.
    	return sequenceError()
    else if (timeDif < holdTimeMillisec)
    	return createButtonEvent(button,"pushed")
    else 
    	return createButtonEvent(button,"held")
}

private handleButtonPress(button) {
	state.lastPress = [button: button, time: now()]
}

private createButtonEvent(button,action) {
	logIt "Button ${button} ${action}"
	return createEvent([
    	name: "button",
        value: action, 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was ${action}",
        isStateChange: true, 
        displayed: true])
}

private getBatteryLevel(rawValue) {
	def intValue = Integer.parseInt(rawValue,16)
	def min = 2.1
    def max = 3.0
    def vBatt = intValue / 10
    return ((vBatt - min) / (max - min) * 100) as int
}

private handlePresenceEvent(present) {
    def wasPresent = device.currentState("presence")?.value == "present"
    if (!wasPresent && present) {
        logIt "Sensor is present"
        startTimer()
    } else if (!present) {
        logIt "Sensor is not present"
        stopTimer()
    }
    def linkText = getLinkText(device)
    def eventMap = [
        name: "presence",
        value: present ? "present" : "not present",
        linkText: linkText,
        descriptionText: "${linkText} has ${present ? 'arrived' : 'left'}",
    ]
    logIt "Creating presence event: ${eventMap}"
    sendEvent(eventMap)
}

private startTimer() {
    logIt "Scheduling periodic timer"
    schedule("0 * * * * ?", checkPresenceCallback)
}

private stopTimer() {
    logIt "Stopping periodic timer"
    unschedule()
}

def checkPresenceCallback() {
    def timeSinceLastCheckin = (now() - state.lastCheckin) / 1000
    def theCheckInterval = (checkInterval ? checkInterval as int : 2) * 60
    logIt "Sensor checked in ${timeSinceLastCheckin} seconds ago"
    if (timeSinceLastCheckin >= theCheckInterval) {
        handlePresenceEvent(false)
    }
}

// ****** Utility functions ******

def testCmd() {
    
    
}

private logIt(str, logLevel = 'debug') {if (settings.logging) log."$logLevel"(str) }