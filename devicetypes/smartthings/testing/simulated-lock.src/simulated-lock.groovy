/**
 *  Copyright 2017 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  An enhanced virtual lock that allows for testing failure modes
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Simulated Test Lock", namespace: "smartthings/testing", author: "SmartThings") {
    	capability "Actuator"
		capability "sensor"

		capability "Lock"
		capability "Battery"
		command "jam"
		command "setBatteryLevel"
		command "setJamNextOperation"
		command "clearJamNextOperation"
		attribute "doesNextOperationJam", "enum", ["true", "false"]
	}

	// Simulated lock
	tiles {
		multiAttributeTile(name:"toggle", type: "generic", width: 6, height: 4){
			tileAttribute ("device.lock", key: "PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#FFFFFF", nextState:"locking"
				attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.samsung_sds.sub_door_status_unknown", backgroundColor:"#E86D13"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#00A0DC"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#FFFFFF"
			}
			tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label: 'battery ${currentValue}%', unit: "%"
			}
		}

		standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'lock', action:"lock.lock", icon: "st.locks.lock.locked"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'unlock', action:"lock.unlock", icon: "st.locks.lock.unlocked"
		}
		standardTile("jam", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'jam', action:"jam", icon: "st.locks.lock.unknown"
		}
		standardTile("blank", "device.id", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:''
		}
		standardTile("jamToggle", "device.doesNextOperationJam", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "false", label:'jam next', action:"setJamNextOperation", backgroundColor:"#DDDDDD", defaultState: true
			state "true", label:'jam next', action:"clearJamNextOperation", backgroundColor:"#FFA81E"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'battery ${currentValue}%', unit:"%"
		}
		controlTile("batterySliderControl", "device.battery", "slider",
					height: 2, width: 4, range:"(1..100)") {
			state "battery", action:"setBatteryLevel"
		}

		main "toggle"
		details(["toggle",
			"lock", "unlock", "blank",
			"jam", "jamToggle", "blank",
			"battery", "batterySliderControl" ])
	}
}

def installed() {
	log.trace "installed()"
	setBatteryLevel(94)
	unlock()
	initialize()

}

def updated() {
	log.trace "updated()"
	processPreferences()
	initialize()
}

def initialize() {
	log.trace "initialize()"
	clearJamNextOperation()
}

private processPreferences() {
	log.debug "prefBatteryLevel: $prefBatteryLevel"
	log.debug "prefJamNextOperation: $prefJamNextOperation"
	log.debug "prefJamImmediately: $prefJamImmediately"

	String strBatteryLevel = "$prefBatteryLevel"
	Integer batteryLevel = strBatteryLevel.isInteger() ? strBatteryLevel.toInteger() : null
	if (batteryLevel) {
		setBatteryLevel(batteryLevel)
	}

	if (prefJamNextOperation) {
		setJamNextOperation()
	} else {
		clearJamNextOperation()
	}

	if (prefJamImmediately) {
		jam()
	}
}

def lock() {
	log.trace "lock()"
	if (device.currentValue("doesNextOperationJam") == "true") {
		jam()
	} else {
		sendEvent(name: "lock", value: "locked")
	}
}

def unlock() {
	log.trace "unlock()"
	if (device.currentValue("doesNextOperationJam") == "true") {
		jam()
	} else {
		sendEvent(name: "lock", value: "unlocked")
	}
}

def jam() {
	log.trace "jam()"
	sendEvent(name: "lock", value: "unknown")
	if (device.currentValue("doesNextOperationJam") == "true") {
		clearJamNextOperation()
	}
}

def setJamNextOperation() {
	log.trace "setJamNextOperation() -  next lock operation will jam"
	sendEvent(name: "doesNextOperationJam", value: "true")
}

def clearJamNextOperation() {
	log.trace "clearJamNextOperation() -  next lock operation will NOT jam"
	sendEvent(name: "doesNextOperationJam", value: "false")
}

def setBatteryLevel(Number lvl) {
	log.trace "setBatteryLevel(level)"
	sendEvent(name: "battery", value: lvl)
}

