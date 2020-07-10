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
	definition (name: "Simulated Smoke Alarm", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Smoke Detector"
		capability "Sensor"
		capability "Health Check"

        command "smoke"
        command "test"
        command "clear"
	}

	simulator {

	}

	tiles {
		standardTile("main", "device.smoke", width: 2, height: 2) {
			state("clear", label:"Clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff", action:"smoke")
			state("detected", label:"Smoke!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13", action:"clear")
			state("tested", label:"Test", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13", action:"clear")
		}
 		standardTile("smoke", "device.smoke", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Smoke', action:"smoke"
		}  
 		standardTile("test", "device.smoke", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Test', action:"test"
		}
 		standardTile("reset", "device.smoke", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Clear', action:"clear"
		}
        main "main"
		details(["main", "smoke", "test", "clear"])
	}
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"

	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}

def parse(String description) {
	
}

def smoke() {
	log.debug "smoke()"
	sendEvent(name: "smoke", value: "detected", descriptionText: "$device.displayName smoke detected!")
}

def test() {
	log.debug "test()"
	sendEvent(name: "smoke", value: "tested", descriptionText: "$device.displayName tested")
}

def clear() {
	log.debug "clear()"
	sendEvent(name: "smoke", value: "clear", descriptionText: "$device.displayName clear")
}
