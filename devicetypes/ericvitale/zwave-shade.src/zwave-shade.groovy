/**
 *  Copyright 2016 ericvitale@gmail.com
 *
 *  Version 1.0.7 - Updated "on" indicator color to match ST blue (#00a0dc). 08/03/2018
 *  Version 1.0.6 - Cleaned up a bit. 06/30/2017
 *  Version 1.0.5 - Added auto-detect support for Somfy by Bali. 03/31/2017
 *  Version 1.0.4 - Added support for the Window Shade capability. 10/15/2016
 *  Version 1.0.3 - Tweaked configuration calling.
 *  Version 1.0.2 - Added support for poll, fixed battery reporting bug.
 *  Version 1.0.1 - Added support for battery level.
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
 *  You can find this device handler @ https://github.com/ericvitale/ST-Z-Wave-Shade
 *  You can find my other device handlers & SmartApps @ https://github.com/ericvitale
 *
 *  Credit to SmartThings for the following device handler
 *  https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/zwave-dimmer-switch-generic.src/zwave-dimmer-switch-generic.groovy
 *
 */
 
def dhVersion() { return "1.0.2" } 
 
metadata {
	definition (name: "Z-Wave Shade", namespace: "ericvitale", author: "ericvitale@gmail.com") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Battery"
        capability "Configuration"
        capability "Window Shade"
        
        command "sceneOne"
        command "sceneTwo"
        command "sceneThree"
        command "sceneFour"
        command "sceneFive"
        command "getBattery"
        command "doPoll"
        command "levelOpenClose"
        
        attribute "lastActivity", "string"
        attribute "lastConfigured", "string"
        attribute "lastPoll", "string"
        attribute "lastBattery", "string"
        
        fingerprint mfr: "026E", prod: "4345", model: "0038"
        fingerprint mfr: "026E", prod: "4345", model: "5A31"
        fingerprint deviceId: "0x1007", inClusters: "0x5E,0x80,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A,0x86,0x20,0x26", outClusters: "0x82", deviceJoinName: "Z-Wave Shade"
        fingerprint deviceId: "0x1107", inClusters: "0x5E,0x80,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A,0x86,0x20,0x26", outClusters: "0x82", deviceJoinName: "Z-Wave Shade" 
	}
    
    preferences {
	    input "customLevel", "number", title: "Custom Level", required: true, defaultValue: 66, range: "0..100"
        input "logging", "enum", title: "Log Level", required: false, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    }

	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		multiAttributeTile(name:"switchDetails", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'Battery: ${currentValue}%', action: "refresh.refresh"
			}
		}
    
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        valueTile("ShadeLevel", "device.level", width: 2, height: 1) {
        	state "level", label: 'Shade is ${currentValue}% up'
        }
        
        controlTile("levelSliderControl", "device.level", "slider", width: 4, height: 1) {
        	state "level", action:"switch level.setLevel"
        }
        
        standardTile("sceneOne", "device.sceneOne", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:'${currentValue}%', action:"sceneOne", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneTwo", "device.sceneTwo", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"20%", action:"sceneTwo", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneThree", "device.sceneThree", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"40%", action:"sceneThree", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneFour", "device.sceneFour", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"60%", action:"sceneFour", icon: "st.Weather.weather14"
		}
        
        standardTile("sceneFive", "device.sceneFive", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"80%", action:"sceneFive", icon: "st.Weather.weather14"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}
        
        standardTile("doPoll", "device.doPoll", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"Do Poll", action:"doPoll", icon: "st.Weather.weather14"
		}
        
        valueTile("LastActivity", "device.lastActivity", width: 6, height: 2) {
        	state "default", label: 'Last Activity ${currentValue}'
        }
        
        valueTile("LastConfigured", "device.lastConfigured", width: 6, height: 2) {
        	state "default", label: 'Last Configured ${currentValue}'
        }
        
        valueTile("LastPoll", "device.lastPoll", width: 6, height: 2) {
        	state "default", label: 'Last Poll ${currentValue}'
        }
        
        valueTile("LastBattery", "device.lastBattery", width: 6, height: 2) {
        	state "default", label: 'Last Battery ${currentValue}'
        }

        main(["switch", "level"])
    	details(["switchDetails", "ShadeLevel", "levelSliderControl", "sceneOne", "sceneTwo", "sceneThree", "sceneFour", "sceneFive", "refresh", "top", "bottom"])

	}
}

def installed() {
	poll()
}

def configure() {
    delayBetween([
		def result = zwave.wakeUpV1.wakeUpNoMoreInformation().format(),
        zwave.wakeUpV1.wakeUpIntervalSet(seconds:4 * 3600, nodeid:zwaveHubNodeId).format()
	])
}

def updated() {
    log("${getVersionStatementString()}", "DEBUG")

	sendEvent(name: "sceneOne", value: customLevel, display: false , displayed: false)
    log("Custom Level Selected: ${customLevel}.", "INFO")
    log("Debug Level Selected: ${logging}.", "INFO")
    
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
    commands << zwave.batteryV1.batteryGet().format()
	
    if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	
    def result = delayBetween(commands,100)
    
    log("result = ${result}", "DEBUG")
    
    return result
}

def refresh() {
	
    log("Refreshing.", "DEBUG")
    log("${getVersionStatementString()}", "DEBUG")
    log("windowShade = ${device.currentValue('windowShade')}.", "INFO")
	
    def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
    commands << zwave.batteryV1.batteryGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	def result = delayBetween(commands,100)
    log("result = ${result}", "DEBUG")
    
    return result
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log("parse() >> zwave.parse($description)", "DEBUG")
        if(description.trim().endsWith("payload: 00 00 00")) {
        	sendEvent(name: "level", value: 0, unit: "%")
            log("Shade is down, setting level to 0%.", "DEBUG")
        } else if(description.contains("command: 2603")) {
        	def hexVal = description.trim()[-8..-7]
            def movingHex = description.trim()[-2..-1]
            log("hexVal = ${hexVal}.", "DEBUG")
            try {
                def intVal = zigbee.convertHexToInt(hexVal)
                def movingInt = zigbee.convertHexToInt(movingHex)
                log("intVal = ${intVal}.", "DEBUG")
                
                if(movingInt == 0) {
                	log("Shade has stopped.", "INFO")
                } else if(movingInt == 254) {
                	log("Shade is moving.", "INFO")
                } else {
                	log("movingInt = ${movingInt}.", "INFO")
                }
                
                sendEvent(name: "level", value: intVal, unit: "%")
            } catch(e) {
            	log("Exception ${e}", "ERROR")
            }
        } else if(description.contains("command: 8003")) {
        	log("Battery Reported.", "DEBUG")
        }
        
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
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

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        log("BatteryReport", "INFO")
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        updateDeviceLastBattery(new Date())
        createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log("WakeUpNotification", "INFO")
    updateDeviceLastActivity(new Date())
        def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
                result << response(zwave.batteryV1.batteryGet())
                result << response("delay 1200")  // leave time for device to respond to batteryGet
        }
        result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
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
    setLevel(99)
}

def off() {
	setLevel(0)
}

def setLevel(level) {
	log("setLevel(${level}).", "DEBUG")
    
    if(level >= 100) {
    	level = getMaxLevel()
    } else if(level < 0) {
    	level = 0
    }
    
	if (level > 0 && level < 99) {
		sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "partially open")
	} else if(level == 0) {
		sendEvent(name: "switch", value: "off")
        sendEvent(name: "windowShade", value: "closed")
	} else {
    	sendEvent(name: "windowShade", value: "open")
    }
    
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	setLevel(value)
}

def open() {
	setLevel(getMaxLevel())
}

def close() {
	setLevel(0)
}

def presetPosition() {
	setLevel(customLevel)
}

def levelOpenClose(value) {
    log("levelOpenClose called with value ${value}.", "DEBUG")
    if (value) {
        on()
    } else {
        off()
    }
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

def sceneOne() {
    setLevel(customLevel)
}

def sceneTwo() {
    setLevel(20)
}

def sceneThree() {
    setLevel(40)
}

def sceneFour() {
    setLevel(60)
}

def sceneFive() {
    setLevel(80)
}

def getMaxLevel() {
	return 99
}

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

def getBattery() {
	def commands = []
    commands << zwave.batteryV1.batteryGet().format()
	def result = delayBetween(commands,100)
    log("result = ${result}", "DEBUG")
    return result
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

def updateDeviceLastBattery(lastBattery) {
	def finalString = lastBattery?.format('MM/d/yyyy hh:mm a',location.timeZone)    
	sendEvent(name: "lastBattery", value: finalString, display: false , displayed: false)
}