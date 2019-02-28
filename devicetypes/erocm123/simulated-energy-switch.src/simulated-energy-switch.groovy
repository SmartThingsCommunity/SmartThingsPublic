/**
 *  Copyright 2015 Eric Maycock
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
	
    definition (name: "Simulated Energy Switch", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch-power-energy") {
		capability "Switch"
        capability "Relay Switch"
        capability "Energy Meter"
		capability "Power Meter"
        capability "Sensor"
        capability "Actuator"

		command "onPhysical"
		command "offPhysical"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"	
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
		}
        valueTile("power", "power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "energy", decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
        main "switch"
		details(["switch"])
	}
}

def parse(String description) {

}

def parse(Map description) {
    def eventMap = []
    if (description.type == null) eventMap << createEvent(name:"$description.name", value:"$description.value")
    else eventMap << createEvent(name:"$description.name", value:"$description.value", type:"$description.type")
    def statusTextmsg = ""
    if (description.name == "power") {
        if(device.currentState('energy')) statusTextmsg = "${description.value} W ${device.currentState('energy').value} kWh"
        else statusTextmsg = "${description.value} W"
    } else if (description.name == "energy") {
        if(device.currentState('power')) statusTextmsg = "${device.currentState('power').value} W ${description.value} kWh"
        else statusTextmsg = "${description.value} kWh"
    }
    if (statusTextmsg != "") eventMap << createEvent(name:"statusText", value:statusTextmsg, displayed:false)
    return eventMap
}

def on() {
	log.debug "$version on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "$version off()"
	sendEvent(name: "switch", value: "off")
}

def onPhysical() {
	log.debug "$version onPhysical()"
	sendEvent(name: "switch", value: "on", type: "physical")
}

def offPhysical() {
	log.debug "$version offPhysical()"
	sendEvent(name: "switch", value: "off", type: "physical")
}

private getVersion() {
	"PUBLISHED"
}
