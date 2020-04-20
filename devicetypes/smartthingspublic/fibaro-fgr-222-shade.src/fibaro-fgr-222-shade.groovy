/**
 *  Copyright 2017 Andreas Schräder
 *
 *  Version 1.0.0 - Initial Release
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

 *
 *  Credit to SmartThings for the following device handler
 *  https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/zwave-dimmer-switch-generic.src/zwave-dimmer-switch-generic.groovy
 *
 *  Credit to Eric Ricale who built the generic Z-Wave Shade device handler which forms the base for this device handler
 *  Credit to Robin Winbourne, who built a FGR-222 device handler that provided me with the building blocks for customization for the Fibaro FGR 222
 *
 */
 
def dhVersion() { return "1.0.2" } 
 
metadata {
	definition (name: "Fibaro FGR-222 Shade", namespace: "SmartThingsPublic", author: "Andreas Schraeder") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Window Shade"
        
        command "open"
 		command "stop"
  		command "close"        
        command "setLevel"
        command "sceneOne"
        command "sceneTwo"
        command "sceneThree"
        command "doPoll"
        command "levelOpenClose"
        command "forceCalibration"
        
		attribute "lastActivity", "string"
        attribute "lastConfigured", "string"
        attribute "lastPoll", "string"
        attribute "motion", "enum"
          
        fingerprint mfr: "026E", prod: "4345", model: "0038"
        fingerprint deviceId: "0x1007", inClusters: "0x5E,0x80,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A,0x86,0x20,0x31,0x26", outClusters: "0x82", deviceJoinName: "Fibaro FGR-222 Shade"
	}
        
    preferences {
        input "switchType", "enum", title: "Switch Type", required: true, defaultValue: "Momentary", options: ["Momentary switches", "Toggle switches", "Single momentary switch"]
        input "blindType", "enum", title: "Blind Type", required: true, defaultValue: "Roller blind w/ positioning", options: ["Roller blind w/out Positioning", "Roller blind w/ positioning", "Venetian blind", "Gate w/ positioning", "Gate w/out positioning"]
		input "floorLevel", "number", title: "Floor Level", required: true, defaultValue: 10, range: "0..100"  // The floor level is when the window blind is down, but the motor resistance has not yet started - to allow improving the calibration.
        input "logging", "enum", title: "Log Level", required: false, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    }

	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
		    tileAttribute("device.level", key: "PRIMARY_CONTROL") {
		        attributeState "level", label:'${currentValue}', defaultState: true, backgroundColors:[
        		    [value: 0, color: "#000000"],  //black when shade closed
         		    [value: 100, color: "#cccccc"],//light grey when shade open
     		    ]
			}

			tileAttribute ("device.motion", key: "SECONDARY_CONTROL") {
		        attributeState "moving", label:'STOP', action:"stop", nextState:"idle"
		        attributeState "idle", label:'', action:"stop", nextState:"idle"
            }

			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute("device.level", key: "VALUE_CONTROL") {
		        attributeState "VALUE_UP", action: "open"
		        attributeState "VALUE_DOWN", action: "close"
		    }

		}
 
        standardTile("switchCompact", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${name}', action: "switch.on",  icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
            state "on", label: '${name}', action: "switch.off",  icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOn", label:'${name}', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
        }
 
 		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
              
        controlTile("levelSliderControl", "device.level", "slider", width: 6, height: 1) {
        	state "level", action:"switch level.setLevel"
        }
        
		standardTile("open", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "default", label:'open', action:"open", icon:"st.doors.garage.garage-open"
		}
        
		standardTile("stop", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "default", label:'stop', action:"stop", icon:"st.doors.garage.garage-opening"
		}

		standardTile("close", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "default", label:'close', action:"close", icon:"st.doors.garage.garage-closed"
		}

        standardTile("sceneOne", "device.sceneOne", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"25%", action:"sceneOne", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneTwo", "device.sceneTwo", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"50%", action:"sceneTwo", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneThree", "device.sceneThree", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"75%", action:"sceneThree", icon: "st.Weather.weather14"
		}
 
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
    	}
    
        standardTile("forceCalibration", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "default", label:"", action:"forceCalibration", icon:"st.secondary.configure"
        }
       
        valueTile("level", "device.level",inactiveLabel: false, decoration: "flat",  width: 3, height: 1) {
	        state "level", label: 'Shade is ${currentValue}% up'
        }
      
        valueTile("windowShade", "device.windowShade", width: 2, height: 1) {
            state "windowShade", label: '${currentValue}'
        }

        valueTile("motion", "device.motion", width: 2, height: 1) {
            state "motion", label: '${currentValue}'
        }

        standardTile("doPoll", "device.doPoll", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"Do Poll", action:"doPoll", icon: "st.Weather.weather14"
		}
        
//        valueTile("LastActivity", "device.lastActivity", width: 6, height: 2) {
//        	state "default", label: 'Last Activity ${currentValue}'
//        }
        
//        valueTile("LastConfigured", "device.lastConfigured", width: 6, height: 2) {
//        	state "default", label: 'Last Configured ${currentValue}'
//        }
        
//        valueTile("LastPoll", "device.lastPoll", width: 6, height: 2) {
//        	state "default", label: 'Last Poll ${currentValue}'
//        }
        
        main(["switch", "level"])
    	details(["switch", "sceneOne", "sceneTwo", "sceneThree", "refresh", "forceCalibration", "windowShade", "motion"])

	}
}

def installed() {
	poll()
}

def configure() {
    delayBetween([
		def result = zwave.wakeUpV1.wakeUpNoMoreInformation().format(),
        zwave.wakeUpV1.wakeUpIntervalSet(seconds:4 * 3600, nodeid:zwaveHubNodeId).format(),
	])
}

def updated() {  // triggered when "Done" is pushed in the preference pane
    log("${getVersionStatementString()}", "DEBUG")

	int p14
    int p10

	switch (switchType) {
		case "Momentary switches":  p14=0
       		break
        case "Toggle switches":  p14=1
        	break
        case "Single momentary switch": p14=2
        	break
	}        
    log("Parameter 14: ${p14}.", "INFO")
    delayBetween([
	    zwave.configurationV1.configurationSet(parameterNumber: 14, size: 1, configurationValue: [p14.value]).format()
		], 500)


	switch (blindType) {
		case "Roller blind w/out Positioning": p10=0
       		break
        case "Roller blind w/ positioning": p10=1
        	break
        case "Venetian blind": p10=2
        	break
        case "Gate w/ positioning": p10=3
        	break
        case "Gate w/out positioning": p10=4
        	break
	}        
    log("Parameter 10: ${p10}.", "INFO")
    delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, configurationValue:[p10.value]).format()
		], 500)


    log("Floor level defined: ${floorLevel}.", "INFO")
    log("Debug level selected: ${logging}.", "INFO")
    
    poll()
}

def doPoll() {
	poll()
}

def poll() {
	log("Polling...", "DEBUG")
    
    updateDeviceLastPoll(new Date())
    
    log("${getVersionStatementString()}", "DEBUG")
    
    configCheck()
    
    def commands = []
	
    commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	
    if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	
    def result = delayBetween(commands,100)
    
    log("result = ${result}", "DEBUG")
    
    return result
    
	//zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	
    log("Refreshing.", "DEBUG")
    log("${getVersionStatementString()}", "DEBUG")
    log("windowShade = ${device.currentValue('windowShade')}.", "INFO")
	
    def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	def result = delayBetween(commands,100)
    log("result = ${result}", "DEBUG")
    
    log("Listing of current parameter settings of ${device.displayName}", "DEBUG")
	def cmds = []
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format() 
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
	delayBetween(cmds, 500)

    return result
}

def parse(String description) {
	def result = null
    int adjustedLevel

	log("Parsing ${description}", "DEBUG")

	if (description != "updated") {
		log("parse() >> zwave.parse($description)", "DEBUG")
        if(description.trim().endsWith("payload: 00 00 00")) {
        	sendEvent(name: "level", value: 0, unit: "%")
            log("Shade is down, setting level to 0%.", "DEBUG")
        } else if(description.contains("command: 2603")) {   // update of level from device
        	def hexVal = description.trim()[-2..-1]
            try {
                def intVal = zigbee.convertHexToInt(hexVal)
                
                if(intVal >= 97) {   			 // window shade is practially open - a potential difference may be a calibration error
					sendEvent(name: "switch", value: "on")
			        sendEvent(name: "windowShade", value: "open")
				    sendEvent(name: "level", value: 100, unit: "%")
				} else if ((intVal <= 3)||(intVal <= floorLevel)) {        // window shade is practially closed - a potential difference may be a calibration error
					sendEvent(name: "switch", value: "off")
			        sendEvent(name: "windowShade", value: "closed")
				    sendEvent(name: "level", value: 0, unit: "%")
				} else {
                 	adjustedLevel = ((int) (intVal-floorLevel)/((100-floorLevel)/100))   // calculate correct level under consideration of floor level
	                log("intVal = ${intVal}. adjustedLevel = ${adjustedLevel}", "DEBUG")
                    sendEvent(name: "level", value: adjustedLevel, unit: "%")
               }
            } catch(e) {
            	log("Exception ${e}", "ERROR")
            }
        } else if(description.contains("command: 3105")) {
            def movingHex = description.trim()[-2..-1]

            try {
                def movingInt = zigbee.convertHexToInt(movingHex)
                
                if(movingInt == 0) {
                	log("Shade has stopped.", "INFO")
                    sendEvent(name: "motion", value: "idle")
                } else {
                	log("Shade is moving..", "INFO")
                    sendEvent(name: "motion", value: "moving")
                }
                
            } catch(e) {
            	log("Exception ${e}", "ERROR")
        		}
        }
        
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1,  0x31: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log("Was hailed: requesting state update", "DEBUG")
	} else {
		log("Parse returned ${result?.descriptionText}", "DEBUG")
	}
    
	return result
}

def configCheck() {
    if(shouldReconfigure() == true || isConfigured() == false) {
    	log("Reconfiguring the device as the state value has changed.", "DEBUG")
        configure()
        setStateVersion(getNewStateVersion())
        state.configured = true
        updateDeviceLastConfigured(new Date())
    } else {
    	log("Device already configured.", "DEBUG")
    }
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log("WakeUpNotification", "INFO")
    updateDeviceLastActivity(new Date())
        def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
        result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log("shadeEvent: BasicReport", "DEBUG")
	shadeEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log("shadeEvent: BasicSet", "DEBUG")
	shadeEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	log("shadeEvent: SwitchMultilevelReport", "DEBUG")
	shadeEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	log("shadeEvent: SwitchMultilevelSet", "DEBUG")
	shadeEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
	log("shadeEvent: SensorMultilevelReport", "DEBUG")
        	def hexVal = cmd.value
            int adjustedLevel
            try {
                def intVal = zigbee.convertHexToInt(hexVal)
                
                if(intVal >= 97) {   			 // window shade is practially open - a potential difference may be a calibration error
					result << createEvent(name: "level", value: 100, unit: "%")
				} else if ((intVal <= 3)||(intVal <= floorLevel)) {        // window shade is practially closed - a potential difference may be a calibration error
					result << createEvent(name: "level", value: 0, unit: "%")
				} else {
                 	adjustedLevel = ((int) (intVal-floorLevel)/((100-floorLevel)/100))   // calculate correct level under consideration of floor level
	                log("intVal = ${intVal}. adjustedLevel = ${adjustedLevel}", "DEBUG")
					result << createEvent(name: "level", value: adjustedLevel, unit: "%")
               }
            } catch(e) {
            	log("Exception ${e}", "ERROR")
            }
}

private shadeEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
        	def hexVal = cmd.value
            int adjustedLevel
            try {
                def intVal = zigbee.convertHexToInt(hexVal)
                
                if(intVal >= 97) {   			 // window shade is practially open - a potential difference may be a calibration error
					result << createEvent(name: "level", value: 100, unit: "%")
				} else if ((intVal <= 3)||(intVal <= floorLevel)) {        // window shade is practially closed - a potential difference may be a calibration error
					result << createEvent(name: "level", value: 0, unit: "%")
				} else {
                 	adjustedLevel = ((int) (intVal-floorLevel)/((100-floorLevel)/100))   // calculate correct level under consideration of floor level
	                log("intVal = ${intVal}. adjustedLevel = ${adjustedLevel}", "DEBUG")
					result << createEvent(name: "level", value: adjustedLevel, unit: "%")
               }
            } catch(e) {
            	log("Exception ${e}", "ERROR")
            }

	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log("Unhandled Event ${cmd}", "DEBUG")
	[:]
}

def on() {
    setLevel(100)
}

def off() {
	setLevel(0)
}

def setLevel() {
    log.trace "setLevel() is understood as close by HomeKit"
    setLevel(0) 
}

def presetPosition() {
    log.trace "presetPosition()"
    setLevel(50)
}

def setLevel(value) {

	log("setLevel(${value}).", "DEBUG")

	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 100), 0)
    int targetLevel = level
    
	if (level >3 && level < 97) {
		sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "partially open")
		targetLevel = 100-((int)(100-level)*(100-floorLevel)/100)    // correct level for floorLevel factor except for where about 100 or about 0
	} else if(level <= floorLevel) {
		sendEvent(name: "switch", value: "off")
        sendEvent(name: "windowShade", value: "closed")
        level = 0
        targetLevel = 0
	} else {
		sendEvent(name: "switch", value: "on")
    	sendEvent(name: "windowShade", value: "open")
        level = 100
        targetLevel = 100
    }

	log("setLevel(${level}) targetLevel ${targetLevel}.", "DEBUG")

	if (targetLevel >96) {  
		log("open", "DEBUG")
		delayBetween ([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchBinaryV1.switchBinaryGet().format()],500)
	} else {
		log("set(${level}", "DEBUG")
		delayBetween ([zwave.basicV1.basicSet(value: targetLevel).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
    }


//	delayBetween ([zwave.basicV1.basicSet(value: targetLevel).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)

//	sendEvent(name: "level", value: level, unit: "%")
}

def setLevel(value, duration) {
	setLevel(value)
}

def open() {
	setLevel(100)
}

def close() {
	setLevel(0)
}

def stop() {
	delayBetween([zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF).format()],500)
}

def levelOpenClose(value) {
    log("levelOpenClose called with value ${value}.", "DEBUG")
    if (value) {
        on()
    } else {
        off()
    }
}

def sceneOne() {
    setLevel(25)
}

def sceneTwo() {
    setLevel(50)
}

def sceneThree() {
    setLevel(75)
}

def forceCalibration() {

	int p14
    int p10

	switch (switchType) {
		case "Momentary switches":  p14=0
       		break
        case "Toggle switches":  p14=1
        	break
        case "Single momentary switch": p14=2
        	break
	}        
    log("Parameter 14: ${p14}.", "INFO")


	switch (blindType) {
		case "Roller blind w/out Positioning": p10=0
       		break
        case "Roller blind w/ positioning": p10=1
        	break
        case "Venetian blind": p10=2
        	break
        case "Gate w/ positioning": p10=3
        	break
        case "Gate w/out positioning": p10=4
        	break
	}        
    log("Parameter 10: ${p10}.", "INFO")



	log("Executing 'param 29 = 1'", "INFO")
    delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 29, size: 1, configurationValue: [1]).format(),   
        zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, configurationValue: [p10.value]).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 14, size: 1, configurationValue: [p14.value]).format()
		], 500)
}





/************ Begin Logging Methods *******************************************************/

def determineLogLevel(data) {
    switch (data?.toUpperCase()) {
        case "TRACE":
            return 0
            break
        case "DEBUG":
            return 1
            break
        case "INFO":
            return 2
            break
        case "WARN":
            return 3
            break
        case "ERROR":
        	return 4
            break
        default:
            return 1
    }
}

def log(data, type) {
    data = "Z-Wave Shade -- v${dhVersion()} --  ${data ?: ''}"
        
    if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
        switch (type?.toUpperCase()) {
            case "TRACE":
                log.trace "${data}"
                break
            case "DEBUG":
                log.debug "${data}"
                break
            case "INFO":
                log.info "${data}"
                break
            case "WARN":
                log.warn "${data}"
                break
            case "ERROR":
                log.error "${data}"
                break
            default:
                log.error "Z-Wave Shade -- Invalid Log Setting"
        }
    }
}

/************ End Logging Methods *********************************************************/

def isConfigured() {
	log("${getVersionStatementString()}", "DEBUG")
	if (state.configured == null || state.configured == false) {
    	return false
	} else {
    	return true
    }
}

def getStateVersion() {
	if(state.version != null) {
		return state.version
    } else {
    	return 0
    }
}

def setStateVersion(val) {
	log("Updating State Version to ${val}.", "INFO")
	state.version = val
}

def getNewStateVersion() {
	return 10
}

def getVersionStatementString() {
	return "Current state version is ${getStateVersion()} and new state version is ${getNewStateVersion()}."
}

def shouldReconfigure() {
	if(getNewStateVersion() > getStateVersion()) {
    	return true
    } else {
    	return false
    }
}

def updateDeviceLastActivity(lastActivity) {
	def finalString = lastActivity?.format('MM/d/yyyy hh:mm a',location.timeZone)
	sendEvent(name: "lastActivity", value: finalString, display: false , displayed: false)
}

def updateDeviceLastConfigured(lastConfigured) {
	def finalString = lastConfigured?.format('MM/d/yyyy hh:mm a',location.timeZone)
    log("Raising lastConfigured event with ${finalString}.", "INFO")
	sendEvent(name: "lastConfigured", value: finalString, display: false , displayed: false)
}

def updateDeviceLastPoll(lastPoll) {
	def finalString = lastPoll?.format('MM/d/yyyy hh:mm a',location.timeZone)    
	sendEvent(name: "lastPoll", value: finalString, display: false , displayed: false)
}
