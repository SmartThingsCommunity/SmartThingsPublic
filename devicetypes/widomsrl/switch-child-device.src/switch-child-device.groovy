/**
 *  Switch Child Device
 *
 *  Copyright 2017 Eric Maycock
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
	definition (name: "Switch Child Device", namespace: "WiDomsrl", author: "WiDomsrl", vid: "generic-switch") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
        capability "Refresh"
	}

	tiles {
		        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: "off", action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label: "on", action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            	state "turningOn", label:'Turning on', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState: "turningOff"
            	state "turningOff", label:'Turning off', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("multiStatus", label:'Channel 2')
            }

        }
        valueTile("power", "device.power", decoration: "flat", width: 6, height: 2) {
            state "power", label:'${currentValue}\n W', action:"refresh"
        }
//        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
//            state "energy", label:'${currentValue}\n kWh', action:"refresh"
//        }
//        valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
//            state "reset", label:'reset\n kWh', action:"reset"
//        }
        standardTile("main", "device.switch", decoration: "flat", canChangeIcon: true) {
            state "off", label: 'off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            state "on", label: 'on', action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
        }
        main "main"
//        details(["switch","power","energy","reset"])
		  details(["switch","power"])
	}
}

void on() {
	parent.childOn()
}

void off() {
	parent.childOff()
}

void refresh() {
	parent.childRefresh()
}