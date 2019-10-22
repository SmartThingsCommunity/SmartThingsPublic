/**
 *  HA DTH2 (v.0.0.1)
 *
 *  Authors
 *   - fison67@nate.com
 *  Copyright 2018
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
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "HA DTH2", namespace: "fison67", author: "fison67") {
        capability "Sensor"
        capability "Contact Sensor"
		capability "Refresh"
        
		attribute "status", "string"
		attribute "value", "number"
        attribute "lastCheckinDate", "Date"
        attribute "unit", "String"
        
        command "setStatus"
        command "setUnitOfMeasurement"
	}


	simulator {
	}

	tiles {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState("status", label:'${currentValue}', backgroundColor: "#ffffff")
			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh", icon:"st.secondary.refresh"
        }
        valueTile("entity_id", "device.entity_id", width: 4, height: 2) {
            state "val", label:'${currentValue}', defaultState: true
        }
        valueTile("unit", "device.unit", width: 2, height: 2) {
            state "val", label:'${currentValue}', defaultState: true
        }
       
        main (["status"])
      	details(["status", "entity_id", "refresh", "unit"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setHASetting(url, password, deviceId){
	state.app_url = url
    state.app_pwd = password
    state.entity_id = deviceId
}

def setUnitOfMeasurement(value){
	if(value){
    	sendEvent(name:"unit", value:value)
    }
}

def setStatus(String value){
	if(state.entity_id == null){
    	return
    }
	log.debug "setStatus >> '${value}'"
    sendEvent(name:"status", value:value)
    if(value.isNumber()){
    	sendEvent(name:"value", value:value)
    }
    
    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "lastCheckinDate", value: now)
    sendEvent(name: "lastCheckin", value: now)
    sendEvent(name: "entity_id", value: state.entity_id)
}

def refresh(){
	log.debug "Refresh"
	def options = [
     	"method": "GET",
        "path": "/api/states/${state.entity_id}",
        "headers": [
        	"HOST": state.app_url,
            "x-ha-access": state.app_pwd,
            "Content-Type": "application/json"
        ]
    ]
    sendCommand(options)
}

def sendCommand(options){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: callback])
    sendHubCommand(myhubAction)
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
        def jsonObj = new JsonSlurper().parseText(msg.body)
        setStatus(jsonObj.state)
        setUnitOfMeasurement(jsonObj.attributes.unit_of_measurement)
    } catch (e) {
        log.error "Exception caught while parsing data: " + e 
    }
}