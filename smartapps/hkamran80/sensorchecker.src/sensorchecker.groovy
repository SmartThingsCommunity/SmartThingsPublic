/**
 *  SensorChecker
 *
 *  Copyright 2018 H. Kamran
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
    name: "SensorChecker",
    namespace: "hkamran80",
    author: "H. Kamran",
    description: "Check the sensors for their readings.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png")


preferences {
	section("Door 1") {
		input "door1", "capability.contactSensor", title: "Door 1 Sensor"
        input "door2", "capability.contactSensor", title: "Door 2 Sensor"
        input "door3", "capability.contactSensor", title: "Door 3 Sensor"
	}
    
    section("Notifcations") {
    	input "phone", "phone", required: true
    }
}

def installed() {
	subscribe(door1, "contact.open", door1O)
    subscribe(door2, "contact.open", door2O)
    subscribe(door3, "contact.open", door3O)
    
    subscribe(door1, "contact.closed", door1C)
    subscribe(door2, "contact.closed", door2C)
    subscribe(door3, "contact.closed", door3C)
}

def updated() {
	unsubscribe()
	
    subscribe(door1, "contact.open", door1O)
    subscribe(door2, "contact.open", door2O)
    subscribe(door3, "contact.open", door3O)
    subscribe(door1, "contact.closed", door1C)
    subscribe(door2, "contact.closed", door2C)
    subscribe(door3, "contact.closed", door3C)
}

def door1O(evt) {
	if (phone) {
        sendSms(phone, "${door1.displayName} is now open.")
    }
}
def door2O(evt) {
	if (phone) {
        sendSms(phone, "${door2.displayName} is now open.")
    }
}
def door3O(evt) {
	if (phone) {
        sendSms(phone, "${door3.displayName} is now open.")
    }
}

def door1C(evt) {
	if (phone) {
        sendSms(phone, "${door1.displayName} is now closed.")
    }
}
def door2C(evt) {
	if (phone) {
        sendSms(phone, "${door2.displayName} is now closed.")
    }
}
def door3C(evt) {
	if (phone) {
        sendSms(phone, "${door3.displayName} is now closed.")
    }
}