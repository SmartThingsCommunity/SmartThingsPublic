/**
 *  Copyright 2018 Eric Maycock
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
 *  2018-02-12: Added temperature capability and reading. Increased checkInterval to 6 hours. 
 */
 
metadata {
	definition (name: "Ecolink Firefighter", namespace: "erocm123", author: "Eric Maycock", vid:"generic-smoke-co") {
		capability "Smoke Detector"
		capability "Carbon Monoxide Detector"
		capability "Sensor"
		capability "Battery"
        capability "Tamper Alert"
		capability "Health Check"
        capability "Temperature Measurement"

		attribute "alarmState", "string"

		fingerprint mfr:"014A", prod:"0005", model:"000F", deviceJoinName: "Ecolink Firefighter"
	}

	simulator {
	}
    
    preferences {
        input "tempReportInterval", "enum", title: "Temperature Report Interval\n\nHow often you would like temperature reports to be sent from the sensor. More frequent reports will have a negative impact on battery life.\n", description: "Tap to set", required: false, options:[60: "1 Hour", 120: "2 Hours", 180: "3 Hours", 240: "4 Hours", 300: "5 Hours", 360: "6 Hours", 720: "12 Hours", 1440: "24 Hours"], defaultValue: 240
        input "tempOffset", "decimal", title: "Temperature Offset\n\nCalibrate reported temperature by applying a negative or positive offset\nRange: -10.0 to 10.0", description: "Tap to set", required: false, range: "-10..10"
    }

	tiles (scale: 2){
		multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.alarmState", key: "PRIMARY_CONTROL") {
				attributeState("clear", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("smoke", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("carbonMonoxide", label:"MONOXIDE", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13")
				attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
                attributeState("detected", label:"TAMPERED", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
			}
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}°',icon: "")
            }
		}
        
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "smoke"
		details(["smoke", "battery"])
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: 24 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def cmds = []
	createSmokeOrCOEvents("allClear", cmds) // allClear to set inital states for smoke and CO
	cmds.each { cmd -> sendEvent(cmd) }
}

def updated() {
	sendEvent(name: "checkInterval", value: 24 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results << createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [ 0x80: 1, 0x84: 1, 0x71: 2, 0x72: 1 ])
		if (cmd) {
			zwaveEvent(cmd, results)
		}
	}
	//log.debug "'$description' parsed to ${results.inspect()}"
	return results
}

def createSmokeOrCOEvents(name, results) {
	def text = null
	switch (name) {
		case "smoke":
			text = "$device.displayName smoke was detected!"
			// these are displayed:false because the composite event is the one we want to see in the app
			results << createEvent(name: "smoke",          value: "detected", descriptionText: text, displayed: false)
			break
		case "carbonMonoxide":
			text = "$device.displayName carbon monoxide was detected!"
			results << createEvent(name: "carbonMonoxide", value: "detected", descriptionText: text, displayed: false)
			break
		case "tested":
			text = "$device.displayName was tested"
			results << createEvent(name: "smoke",          value: "tested", descriptionText: text, displayed: false)
			results << createEvent(name: "carbonMonoxide", value: "tested", descriptionText: text, displayed: false)
			break
		case "smokeClear":
			text = "$device.displayName smoke is clear"
			results << createEvent(name: "smoke",          value: "clear", descriptionText: text, displayed: false)
			name = "clear"
			break
		case "carbonMonoxideClear":
			text = "$device.displayName carbon monoxide is clear"
			results << createEvent(name: "carbonMonoxide", value: "clear", descriptionText: text, displayed: false)
			name = "clear"
			break
		case "allClear":
			text = "$device.displayName all clear"
			results << createEvent(name: "smoke",          value: "clear", descriptionText: text, displayed: false)
			results << createEvent(name: "carbonMonoxide", value: "clear", displayed: false)
			name = "clear"
			break
		case "testClear":
			text = "$device.displayName test cleared"
			results << createEvent(name: "smoke",          value: "clear", descriptionText: text, displayed: false)
			results << createEvent(name: "carbonMonoxide", value: "clear", displayed: false)
			name = "clear"
			break
        case "detected":
			text = "$device.displayName covering was removed"
			results << createEvent(name: "tamper",          value: "detected", descriptionText: text, displayed: false)
			name = "detected"
			break
        case "clear":
			text = "$device.displayName covering was restored"
			results << createEvent(name: "tamper",          value: "clear", descriptionText: text, displayed: false)
			name = "clear"
			break
	}
	// This composite event is used for updating the tile
	results << createEvent(name: "alarmState", value: name, descriptionText: text)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd, results)
{
    log.debug cmd
    state.wakeInterval = cmd.seconds
    return results
}
        
def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd, results) {
    log.debug cmd
	if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_SMOKE) {
		if (cmd.zwaveAlarmEvent == 3) {
			createSmokeOrCOEvents("tested", results)
		} else {
			createSmokeOrCOEvents((cmd.zwaveAlarmEvent == 1 || cmd.zwaveAlarmEvent == 2) ? "smoke" : "smokeClear", results)
		}
	} else if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_CO) {
		createSmokeOrCOEvents((cmd.zwaveAlarmEvent == 1 || cmd.zwaveAlarmEvent == 2) ? "carbonMonoxide" : "carbonMonoxideClear", results)
	} else if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_BURGLAR) {
		if (cmd.zwaveAlarmEvent == 0x00) {
            createSmokeOrCOEvents("clear", results)
		} else if (cmd.zwaveAlarmEvent == 0x03) {
            createSmokeOrCOEvents("detected", results)
        }
	} else if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_POWER_MANAGEMENT) {
		if (cmd.zwaveAlarmEvent == 0x0A) {
            results << createEvent(descriptionText: "Replace Battery Soon", displayed: true)
		} else if (cmd.zwaveAlarmEvent == 0x0B) {
            results << createEvent(descriptionText: "Replace Battery Now", displayed: true)
        }
	} else switch(cmd.alarmType) {
		case 1:
			createSmokeOrCOEvents(cmd.alarmLevel ? "smoke" : "smokeClear", results)
			break
		case 2:
			createSmokeOrCOEvents(cmd.alarmLevel ? "carbonMonoxide" : "carbonMonoxideClear", results)
			break
		case 12:  // test button pressed
			createSmokeOrCOEvents(cmd.alarmLevel ? "tested" : "testClear", results)
			break
		case 13:  // sent every hour -- not sure what this means, just a wake up notification?
			if (cmd.alarmLevel == 255) {
				results << createEvent(descriptionText: "$device.displayName checked in", isStateChange: false)
			} else {
				results << createEvent(descriptionText: "$device.displayName code 13 is $cmd.alarmLevel", isStateChange:true, displayed:false)
			}
			
			// Clear smoke in case they pulled batteries and we missed the clear msg
			if(device.currentValue("smoke") != "clear") {
				createSmokeOrCOEvents("smokeClear", results)
			}
			
			// Check battery if we don't have a recent battery event
			if (!state.lastbatt || (now() - state.lastbatt) >= 48*60*60*1000) {
				results << response(zwave.batteryV1.batteryGet())
			}
			break
		default:
			results << createEvent(displayed: true, descriptionText: "Alarm $cmd.alarmType ${cmd.alarmLevel == 255 ? 'activated' : cmd.alarmLevel ?: 'deactivated'}".toString())
			break
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd, results) {
	if (cmd.sensorType == physicalgraph.zwave.commandclasses.SensorBinaryV2.SENSOR_TYPE_SMOKE) {
		createSmokeOrCOEvents(cmd.sensorValue ? "smoke" : "smokeClear", results)
	} else if (cmd.sensorType == physicalgraph.zwave.commandclasses.SensorBinaryV2.SENSOR_TYPE_CO) {
		createSmokeOrCOEvents(cmd.sensorValue ? "carbonMonoxide" : "carbonMonoxideClear", results)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd, results) {
	if (cmd.sensorType == 1) {
		createSmokeOrCOEvents(cmd.sensorState ? "smoke" : "smokeClear", results)
	} else if (cmd.sensorType == 2) {
		createSmokeOrCOEvents(cmd.sensorState ? "carbonMonoxide" : "carbonMonoxideClear", results)
	}
	
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd, results) {
	results << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
    results << response(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format())
    
    if(state.wakeInterval == null || state.wakeInterval != (tempReportInterval? tempReportInterval.toInteger()*60:14400)){
        log.debug "Setting Wake Interval to ${tempReportInterval? tempReportInterval.toInteger()*60:14400}"
        results << response([
                   zwave.wakeUpV1.wakeUpIntervalSet(seconds: tempReportInterval? tempReportInterval.toInteger()*60:14400, nodeid:zwaveHubNodeId).format(),
                   "delay 1000",
                   zwave.wakeUpV1.wakeUpIntervalGet().format()
                   ])
    }
    
	if (!state.lastbatt || (now() - state.lastbatt) >= 24*60*60*1000) {
		results << response([
				zwave.batteryV1.batteryGet().format(),
				"delay 2000",
				zwave.wakeUpV1.wakeUpNoMoreInformation().format()
			])
	} else {
		results << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, results)
{
    log.debug cmd
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.value = getAdjustedTemp(state.realTemperature)
            map.unit = getTemperatureScale()
            log.debug "Temperature Report: $map.value"
            break;
        default:
            map.descriptionText = cmd.toString()
    }
    results << createEvent(map)
}

private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100
    if (tempOffset) {
       return value =  value + Math.round(tempOffset * 100) /100
    } else {
       return value
    }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, results) {
	def map = [ name: "battery", unit: "%", isStateChange: true ]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	results << createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, results) {
	def encapsulatedCommand = cmd.encapsulatedCommand([ 0x80: 1, 0x84: 1, 0x71: 2, 0x72: 1 ])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, results)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		results << createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd, results) {
    log.debug cmd
	def event = [ displayed: false ]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	results << createEvent(event)
}
