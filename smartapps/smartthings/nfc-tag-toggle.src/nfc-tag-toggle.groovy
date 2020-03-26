/**
 *  NFC Tag Toggle
 *
 *  Copyright 2014 SmartThings
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
 
definition(
    name: "NFC Tag Toggle",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Allows toggling of a switch, lock, or garage door based on an NFC Tag touch event",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2x.png")


preferences {
    page(name: "pageOne", title: "Device selection", uninstall: true, nextPage: "pageTwo") {
        section("Select an NFC tag") {
            input "tag", "capability.touchSensor", title: "NFC Tag"
        }
        section("Select devices to control") {
            input "switch1", "capability.switch", title: "Light or switch", required: false, multiple: true
            input "lock", "capability.lock", title: "Lock", required: false, multiple: true
            input "garageDoor", "capability.doorControl", title: "Garage door controller", required: false, multiple: true
        }
    }
    
    page(name: "pageTwo", title: "Master devices", install: true, uninstall: true)
}

def pageTwo() {
	dynamicPage(name: "pageTwo") {
    	section("If set, the state of these devices will be toggled each time the tag is touched, " + 
                "e.g. a light that's on will be turned off and one that's off will be turned on, " +
                "other devices of the same type will be set to the same state as their master device. " +
                "If no master is designated then the majority of devices of the same type will be used " +
                "to determine whether to turn on or off the devices.") {
            
            if (switch1 || masterSwitch) {
                input "masterSwitch", "enum", title: "Master switch", options: switch1.collect{[(it.id): it.displayName]}, required: false
            }
            if (lock || masterLock) {
                input "masterLock", "enum", title: "Master lock", options: lock.collect{[(it.id): it.displayName]}, required: false
            }
            if (garageDoor || masterDoor) {
                input "masterDoor", "enum", title: "Master door", options: garageDoor.collect{[(it.id): it.displayName]}, required: false
            }            
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}        
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe tag, "nfcTouch", touchHandler
    subscribe app, touchHandler
}

private currentStatus(devices, master, attribute) {
	log.trace "currentStatus($devices, $master, $attribute)"
	def result = null
	if (master) {
    	result = devices.find{it.id == master}?.currentValue(attribute)
    }
    else {
    	def map = [:]
        devices.each {
        	def value = it.currentValue(attribute)
            map[value] = (map[value] ?: 0) + 1
            log.trace "$it.displayName: $value"
        }
        log.trace map
        result = map.collect{it}.sort{it.value}[-1].key
    }
    log.debug "$attribute = $result"
    result
}

def touchHandler(evt) {
	log.trace "touchHandler($evt.descriptionText)"
    if (switch1) {
    	def status = currentStatus(switch1, masterSwitch, "switch")
        switch1.each {
            if (status == "on") {
                it.off()
            }
            else {
                it.on()
            }
        }
    }
    
    if (lock) {
    	def status = currentStatus(lock, masterLock, "lock")
        lock.each {
            if (status == "locked") {
                lock.unlock()
            }
            else {
                lock.lock()
            }
        }
    }
    
    if (garageDoor) {
        def status = currentStatus(garageDoor, masterDoor, "status")
    	garageDoor.each {
        	if (status == "open") {
            	it.close()
            }
            else {
            	it.open()
            }
        }
    }
}
