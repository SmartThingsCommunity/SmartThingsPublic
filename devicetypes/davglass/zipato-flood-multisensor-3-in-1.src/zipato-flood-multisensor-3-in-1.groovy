/**
 *  Zipato Flood Multisensor 3 in 1
 *
 *  Copyright 2017 Dav Glass
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
	definition (name: "Zipato Flood Multisensor 3 in 1", namespace: "davglass", author: "Dav Glass") {
		capability "Battery"
        capability "Sensor"
        capability "Refresh"
		capability "Health Check"
		capability "Relative Humidity Measurement"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Water Sensor"
        
        fingerprint deviceId: "0701", mfr: "013C", prod: "0002", model: "001F", deviceJoinName: "Zipato Flood Multisensor"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
    	standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
        standardTile("tamper", "device.tamper") {
			state("secure", 	label:"secure",   	icon:"st.locks.lock.locked",   backgroundColor:"#ffffff")
			state("tampered", 	label:"tampered", 	icon:"st.locks.lock.unlocked", backgroundColor:"#53a7c0")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        /*
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}*/
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def result = null

	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent(descriptionText: description, isStateChange: true)
        return result
    }
    def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x07: 1, 0x03: 1])
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug("'$description' parsed to $result")
    } else {
        log.debug("Couldn't zwave.parse '$description'")
    }
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
    
    log.debug cmd
    
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
        case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.debug "Got Device Reset Locally Notification"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log.debug "Got Wakeup Notifcation"
}

def resetTamper() {
	def map = [:]
    map.name = "tamper"
    map.value = "secure"
    map.descriptionText = "$device.displayName is secure"
    map.isStateChange = true
    sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "Got Notification Event"
    log.debug cmd
    if (cmd.notificationType == 7) {
    	log.debug "TAMPER"
        def map = [:]
        map.name = "tamper"
        map.value = "tampered"
        map.descriptionText = "$device.displayName was tampered with.."
        map.isStateChange = true
        sendEvent(map)
        runIn(30, 'resetTamper')
        return
    }
    
    if (cmd.notificationType == 5) {
    	def map = [:]
        map.name = "water"
    	if (cmd.event == 2) {
        	log.debug "Water Leak Detected"
            map.value = "wet"
            map.descriptionText = "$device.displayName has detected a leak!"
        } else {
        	log.debug "Water Leak Cleared"
            map.value = "dry"
            map.descriptionText = "$device.displayName is now reporting dry."
        }
        
        map.isStateChange = true
        sendEvent(map)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec != 0) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def updated() {
	response(refresh())
}
def refresh() {
	log.debug "Refreshing..."
	command(zwave.switchBinaryV1.switchBinaryGet())
}