/**
 *  HEIMAN Smart Combustible Gas Sensor
 *
 *  Copyright 2020 Bernd Brachmaier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "HEIMAN Smart Combustible Gas Sensor", namespace: "rockberte", author: "Bernd Brachmaier", cstHandler: true) {
    	capability "Sensor"
        capability "Smoke Detector"

		fingerprint mfr: "0260", prod: "8003", model: "1000", deviceJoinName: "Combustible Gas Sensor"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
				attributeState("clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("detected", label:"GAS", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
				attributeState("replacement required", label:"REPLACE", icon:"st.alarm.smoke.test", backgroundColor:"#FFFF66")
				attributeState("unknown", label:"UNKNOWN", icon:"st.alarm.smoke.test", backgroundColor:"#ffffff")
			}
		}
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "parse() >> description: ${description}"
	def result = null
    def cmd = zwave.parse(description)
    if (cmd) {
    	log.debug "Before zwaveEvent(cmd) >> Parsed '${description}' to ${cmd}"
        result = zwaveEvent(cmd)
        log.debug "After zwaveEvent(cmd) >> Parsed '${description}' to ${result.inspect()}"
    } else {
        log.debug "parse() >> not parsed description: ${description}"
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.info "Executing zwaveEvent 71 (NotificationV3): 05 (NotificationReport) with cmd: $cmd"
	def result = []
    if (cmd.notificationType == 18) { // Z-Wave gas alarm
    	result << gasAlarmEvent(cmd.event)
    } else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def gasAlarmEvent(value) {
	log.debug "gasAlarmEvent(value): $value"
	def map = [name: "smoke"]
    if (value == 1 || value == 2) {
		map.value = "detected"
		map.descriptionText = "$device.displayName: Combustible gas detected"
	} else if (value == 0) {
		map.value = "clear"
		map.descriptionText = "$device.displayName: Clear"
	} else if (value == 5) {
		map.value = "tested"
		map.descriptionText = "$device.displayName: Gas alarm test"
	} else if (value == 6) {
		map.value = "replacement required"
		map.descriptionText = "$device.displayName: Replacement required"
	} else {
		map.value = "unknown event: $value"
		map.descriptionText = "$device.displayName: Unknown event"
	}    
    createEvent(map)
}