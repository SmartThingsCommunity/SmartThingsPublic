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
	definition (name: "Simulated Minimote", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Actuator"
		capability "Button"
		capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"

        command "push1"
        command "push2"
        command "push3"
        command "push4"
        command "hold1"
        command "hold2"
        command "hold3"
        command "hold4"
	}

	simulator {
		status "button 1 pushed":  "command: 2001, payload: 01"
		status "button 1 held":  "command: 2001, payload: 15"
		status "button 2 pushed":  "command: 2001, payload: 29"
		status "button 2 held":  "command: 2001, payload: 3D"
		status "button 3 pushed":  "command: 2001, payload: 51"
		status "button 3 held":  "command: 2001, payload: 65"
		status "button 4 pushed":  "command: 2001, payload: 79"
		status "button 4 held":  "command: 2001, payload: 8D"
		status "wakeup":  "command: 8407, payload: "
	}
	tiles {
		standardTile("button", "device.button") {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
 		standardTile("push1", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Push 1", backgroundColor: "#ffffff", action: "push1"
		}
 		standardTile("push2", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Push 2", backgroundColor: "#ffffff", action: "push2"
		}
 		standardTile("push3", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Push 3", backgroundColor: "#ffffff", action: "push3"
		}
 		standardTile("push4", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Push 4", backgroundColor: "#ffffff", action: "push4"
		}
 		standardTile("dummy1", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: " ", backgroundColor: "#ffffff", action: "push4"
		}
 		standardTile("hold1", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Hold 1", backgroundColor: "#ffffff", action: "hold1"
		}
 		standardTile("hold2", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Hold 2", backgroundColor: "#ffffff", action: "hold2"
		}
 		standardTile("dummy2", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: " ", backgroundColor: "#ffffff", action: "push4"
		}
 		standardTile("hold3", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Hold 3", backgroundColor: "#ffffff", action: "hold3"
		}
 		standardTile("hold4", "device.button", width: 1, height: 1, decoration: "flat") {
			state "default", label: "Hold 4", backgroundColor: "#ffffff", action: "hold4"
		}

		main "button"
		details(["push1","push2","button","push3","push4","dummy1","hold1","hold2","dummy2","hold3","hold4"])
	}
}

def parse(String description) {

}

def push1() {
	push(1)
}

def push2() {
	push(2)
}

def push3() {
	push(3)
}

def push4() {
	push(4)
}

def hold1() {
	hold(1)
}

def hold2() {
	hold(2)
}

def hold3() {
	hold(3)
}

def hold4() {
	hold(4)
}

private push(button) {
	log.debug "$device.displayName button $button was pushed"
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
}

private hold(button) {
	log.debug "$device.displayName button $button was held"
	sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
}


def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: 4)
}
