/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Sump Pump Controller", namespace: "fortrezz", author: "FortrezZ, LLC") {
		capability "Sensor"

		attribute "alarmState", "string"
        attribute "powerAlert", "number"
        attribute "sumpAlert", "number"

		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x5A,0x85,0x59,0x73,0x20,0x7A,0x71"
	}

	simulator {
		status "replace battery now": "command: 7105, payload: 00 00 00 FF 08 0B 00 00"
		status "battery fully charged": "command: 7105, payload: 00 00 00 FF 08 0D 00 00"
		status "AC mains disconnected": "command: 7105, payload: 00 00 00 FF 08 02 00 00"
		status "AC mains reconnected": "command: 7105, payload: 00 00 00 FF 08 03 00 00"
		status "Excess Pumping": "command: 7105, payload: 00 00 00 FF 09 03 01 01 00"
		status "Float Error": "command: 7105, payload: 00 00 00 FF 09 03 01 02 00"
		status "General Error": "command: 7105, payload: 00 00 00 FF 09 01 00 00"
	}

	tiles (scale: 2){
		multiAttributeTile(name:"alert", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState("clear", label:"clear", icon:"http://swiftlet.technology/wp-content/uploads/2016/04/Ok-96.png", backgroundColor:"#ffffff")
                attributeState("alert", label:"Alert!", icon:"http://swiftlet.technology/wp-content/uploads/2016/04/Error-96.png", backgroundColor:"#ff5b5b")
			}
		}
		main "alert"
		details(["alert"])
	}
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
	log.debug "\"$description\" parsed to ${results.inspect()}"
	return results
}


// Notification Report from SPM
def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd, results) {
    def powerAlert = device.currentValue("powerAlert")
    if (powerAlert == null)
    {
    	powerAlert = 0
    }
    def sumpAlert = device.currentValue("sumpAlert")
    if (sumpAlert == null)
    {
    	sumpAlert = 0
    }

if (cmd.zwaveAlarmType == 8) {
    	if (cmd.zwaveAlarmEvent == 2) {
        	results << createEvent(name: "alarmState", value: "acMainsDisconnected", descriptionText: "AC Mains Disconnected")
            powerAlert = powerAlert + 1
        } else if (cmd.zwaveAlarmEvent == 3) {
        	results << createEvent(name: "alarmState", value: "acMainsReconnected", descriptionText: "AC Mains Reconnected")
            powerAlert = powerAlert - 1
        } else if (cmd.zwaveAlarmEvent == 11) {
        	results << createEvent(name: "alarmState", value: "replaceBatteryNow", descriptionText: "Replace AA batteries in Z-wave module")
            powerAlert = powerAlert + 1
        } else if (cmd.zwaveAlarmEvent == 13) {
        	results << createEvent(name: "alarmState", value: "batteryFullyCharged", descriptionText: "AA Batteries Replaced")
            powerAlert = powerAlert - 1
        }
        
        if (powerAlert < 0)
        {
        	powerAlert = 0
        }
	} else if (cmd.zwaveAlarmType == 9) {
    	if (cmd.zwaveAlarmEvent == 1) {
        	results << createEvent(name: "alarmState", value: "systemHardwareFailure", descriptionText: "Sump Pump Error, refer to Sump Pump Controller Unit")
            sumpAlert = sumpAlert + 1
        } else if (cmd.zwaveAlarmEvent == 3) {
			if (cmd.eventParameter[0] == 0) {
            	results << createEvent(name: "alarmState", value: "alarmClear", descriptionText: "Alarm has been cleared")
                sumpAlert = 0
            } else if (cmd.eventParameter[0] == 1) {
            	results << createEvent(name: "alarmState", value: "floatError", descriptionText: "Pump or float problem")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 2) {
            	results << createEvent(name: "alarmState", value: "backupPumpActivated", descriptionText: "Backup pump was activated")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 3) {
            	results << createEvent(name: "alarmState", value: "highWater", descriptionText: "High water alarm")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 4) {
            	results << createEvent(name: "alarmState", value: "addWater", descriptionText: "Add distilled water to the battery")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 5) {
            	results << createEvent(name: "alarmState", value: "backupPumpError", descriptionText: "Backup pump is defective or not connected")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 6) {
            	results << createEvent(name: "alarmState", value: "9VBatteryLow", descriptionText: "9 volt battery is low or slide switch is OFF")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 7) {
            	results << createEvent(name: "alarmState", value: "12VBatteryError", descriptionText: "Problem with backup sump pump battery")
	            sumpAlert = sumpAlert + 1
            } else if (cmd.eventParameter[0] == 8) {
            	results << createEvent(name: "alarmState", value: "checkCable", descriptionText: "Check your cable connection (to Z-wave module)")
	            sumpAlert = sumpAlert + 1
            }
		}

		if (sumpAlert < 0)
        {
        	sumpAlert = 0
        }
	}
    
    if ((sumpAlert > 0) || (powerAlert > 0))
    {
    	sendEvent(name: "alarm", value: "alert", displayed: false)
    }
    else
    {
    	sendEvent(name: "alarm", value: "clear", displayed: false)
    }
    results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd, results) {
	results << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	results << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, results) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	results << createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd, results) {
	def event = [ displayed: false ]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	results << createEvent(event)
}