/**
 *  Copyright 2017 Big Pine Systems
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Ecolink Flood Freeze Sensor
 *
 *  Author: Keith Ballantyne
 *  Date: 2017-11-28
 */

metadata {
	definition (name: "EcoLink Flood/Freeze Sensor", namespace: "bigpinesystems", author: "Keith Ballantyne", ocfDeviceType: "x.com.st.d.sensor.water") {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Temperature Measurement"
        capability "Tamper Alert"

		fingerprint mfr: "014A", prod: "0005", model: "0010"
}

	simulator {
        status "dry": "command: 3003, payload: 00 06"
        status "wet": "command: 3003, payload: FF 06"
        status "freezing": "command: 3003, payload: FF 07"
        status "normal": "command: 3003, payload: 00 07"
		status "dry notification": "command: 7105, payload: 04"
		status "wet notification": "command: 7105, payload: 00 FF 00 FF 05 02 00 00"
		status "wake up": "command: 8407, payload: "
        status "battery": "command: 8003, payload: 34"
	}

	tiles(scale:2) {
        
        multiAttributeTile(name:"water", type: "generic", width: 2, height: 2){
            tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
                attributeState "dry", label: "Dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
                attributeState "wet", label: "Wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
            }
            
            tileAttribute ("device.lastActivity", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'Last activity: ${currentValue}', action: "refresh.refresh"
			}
        }
		standardTile("temperatureState", "device.temperature", width: 2, height: 2) {
			state "normal", icon:"st.alarm.temperature.normal", backgroundColor:"#ffffff"
			state "freezing", icon:"st.alarm.temperature.freeze", backgroundColor:"#53a7c0"
		}

		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state "temperature", label: '${currentValue}°',
					backgroundColors: [
							[value: 30, color: "#153591"],
							[value: 50, color: "#44b621"]
					]
		}
        
		standardTile("tamper", "device.tamper", width: 2, height: 2) {
			state "clear", icon:"st.locks.lock.locked", backgroundColor:"#ffffff"
			state "detected", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff2730"
		}
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }


		main (["water", "temperature", "tamper"])
		details(["water", "temperature", "temperatureState", "tamper", "battery"])
	}
}



def installed() {
    log.debug "Device Installed..."
    return response(configure())
}

def updated() { // neat built-in smartThings function which automatically runs whenever any setting inputs are changed in the preferences menu of the device handler
    
    log.debug "Settings Updated..."
    return response(delayBetween([
        configure(), // the response() function is used for sending commands in reponse to an event, without it, no zWave commands will work for contained function
    	zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId),
    	zwave.associationV2.associationGet(groupingIdentifier:1),
		zwave.wakeUpV2.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId)
    ], 200))

}

def configure() {
	log.debug "Configuring...." 
    return zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
}


def parse(String description) {
	def result = []
    log.debug "Parsing ${description}"
	def parsedZwEvent = zwave.parse(description, [0x30: 2, 0x80: 1, 0x84: 2, 0x71: 3, 0x72: 2])

	if(parsedZwEvent) {
		if(parsedZwEvent.CMD == "8407") {
			def lastStatus = device.currentState("battery")
			def ageInMinutes = lastStatus ? (new Date().time - lastStatus.date.time)/60000 : 600
			log.debug "Battery status was last checked ${ageInMinutes} minutes ago"

			if (ageInMinutes >= 600) {
				log.debug "Battery status is outdated, requesting battery report"
				result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
			}
			result << new physicalgraph.device.HubAction(zwave.wakeUpV2.wakeUpNoMoreInformation().format())
		}
		result << createEvent( zwaveEvent(parsedZwEvent) )
	}
	if(!result) result = [ descriptionText: parsedZwEvent, displayed: false ]
	log.debug "Parse returned ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
		def event
        def cmds = []
        switch (cmd.sensorType) {
                case 6:
                        event = createEvent(name:"water", value: cmd.sensorValue ? "wet" : "dry")
                        cmds << zwave.batteryV1.batteryGet().format()
                        cmds << "delay 1200"
                        cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId).format()
                        break
                case 7:
                        event = createEvent(name:"temperature", value: cmd.sensorValue ? "30" : "50")
                        cmds << zwave.batteryV1.batteryGet().format()
                        cmds << "delay 1200"
                        cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId).format()
                        break
        }
    [event]
}



def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
    def event
    def cmds = []
	if (cmd.notificationType == 0x05) {
		switch (cmd.event) {
            case 0x04:
                event = createEvent(name: "water", value: "dry", descriptionText: "Water alarm cleared", isStateChange: true)
                break
            case 0x02:
                event = createEvent(name: "water", value: "wet", descriptionText: "Water alarm ACTIVE", isStateChange: true)
                break
		}
	} else if (cmd.notificationType == 0x07) {
		switch(cmd.event) {
        	case 0x03: 
				event = createEvent(name:"tamper", value: "detected", descriptionText: "$device.displayName covering was removed", isStateChange: true)
                break;
            case 0x00:
				event = createEvent(name:"tamper", value: "clear", descriptionText: "$device.displayName covering was replaced", isStateChange: true)
                break;
        }
	} else if (cmd.notificationType == 0x08) {
		switch (cmd.event) {
        	case 0x0A:
				event = createEvent(name:"battery replacement", value: "soon", descriptionText: "$device.displayName replace battery soon", isStateChange: true)
                break;
            case 0x0B:
				event = createEvent(name:"battery replacement", value: "now", descriptionText: "$device.displayName replace battery NOW", isStateChange: true)
                break;
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
    cmds << zwave.batteryV1.batteryGet().format()
    cmds << "delay 1200"
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId).format()
    [event] //, response(cmds)]
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if(cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
	map
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled ${cmd}"
    // Handles all Z-Wave commands we aren't interested in
    return null
}

