/**
 *  Test Meter
 *
 *  Copyright 2015 Chuck J
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
import groovy.json.JsonSlurper;

metadata {
    definition (name: "Smappee Meter", author: "Chuck J", oauth: true) {
        capability "Power Meter"
        capability "Refresh"

        attribute "AlwaysOn", "number"
        attribute "Solar", "number"
    }

    simulator {
        for (int i = 0; i <= 10000; i += 1000) {
            status "power  ${i} W": "{\\\"power\\\": ${i}}"
        }
        for (int i = 0; i <= 10000; i += 1000) {
            status "AlwaysOn  ${i} W": "{\\\"AlwaysOn\\\": ${i}}"
        }

    }

    tiles(scale: 2) {
        multiAttributeTile(name:"power", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "default", label:'${currentValue} W', backgroundColor: "#15241F"
            }
        }

        valueTile("AlwaysOn", "device.AlwaysOn", decoration: "ring", width:2, height:2) {
            state "default", label:'${currentValue} W', backgroundColor: "#2B708F"
        }

        valueTile("Solar", "device.Solar", decoration: "ring", width:2, height:2) {
            state "default", label:'${currentValue} W', backgroundColor: "#99C031"
        }

//	    standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
//			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
//		}

        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main (["power"])
        details(["power","AlwaysOn", "Solar", "refresh"])
    }

}

def refresh() {
    log.debug "Smappee Meter refresh"
    parent.pollHandler(this)
}

def parse(String description) {
    log.debug "Smappee Meter parse: ${description}"
}

//(parent) service manager calls this method to generate an event if some params change
void generateEvent(Map results) {
    log.debug "(parent) called generateEvent $results"
    sendEvent([name: "power", value: Math.round(results.power * 12), unit: "W"])
    sendEvent([name: "AlwaysOn", value: Math.round(results.alwaysOn), unit: "W"])
    sendEvent([name: "Solar", value: Math.round(results.solar * 12), unit: "W"])
}
