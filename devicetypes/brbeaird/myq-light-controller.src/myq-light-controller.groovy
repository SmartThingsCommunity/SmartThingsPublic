/**
 *  MyQ Light Controller
 *
 *  Copyright 2019 Brian Beaird
 *  Special thanks to @dcabtacoma for sending me a light controller so I could test the code.
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
 *
 */
metadata {
	definition (name: "MyQ Light Controller", namespace: "brbeaird", author: "Brian Beaird") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
        attribute "myQDeviceId", "string"

		command "updateDeviceStatus", ["string"]
	}

	simulator {	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.Lighting.light13", backgroundColor: "#00a0dc", nextState: "off"
			}
		}
		main "switch"
		details(["switch"])
	}
}

def on() {
	log.debug "Light turned on"
    parent.sendCommand(getMyQDeviceId(), "turnon")
    updateDeviceStatus("on")

}
def off() {
	log.debug "Light turned off"
    parent.sendCommand(getMyQDeviceId(), "turnoff")
    updateDeviceStatus("off")
}

def updateDeviceStatus(status) {
    if (status == "off"){
    	log.debug "Updating status to off"
        sendEvent(name: "switch", value: "off", display: true, displayed: true, isStateChange: true, descriptionText: device.displayName + " was off")
    }
	else if (status == "on"){
    	log.debug "Updating status to on"
        sendEvent(name: "switch", value: "on", displayed: true, display: true, isStateChange: true, descriptionText: device.displayName + " was on")
    }
    else{
    	log.warn "Unknown light status."
    }
}

def getMyQDeviceId(){
    if (device.currentState("myQDeviceId")?.value)
    	return device.currentState("myQDeviceId").value
	else{
        def newId = device.deviceNetworkId.split("\\|")[2]
        sendEvent(name: "myQDeviceId", value: newId, display: true , displayed: true)
        return newId
    }
}

def updateMyQDeviceId(Id) {
	log.debug "Setting MyQID to ${Id}"
    sendEvent(name: "myQDeviceId", value: Id, display: true , displayed: true)
}

def showVersion(){
	return "3.1.1"
}