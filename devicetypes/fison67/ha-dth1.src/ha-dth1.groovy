/**
 *  HA DTH1 (v.0.0.5)
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
	definition (name: "HA DTH1", namespace: "fison67", author: "fison67") {
		capability "Motion Sensor"				//"active", "inactive"
      	capability "Contact Sensor"  			//"open","closed"
        capability "Switch"						//"on", "off"
        capability "Smoke Detector"    			//"detected", "clear", "tested"
        capability "Water Sensor"      			//"dry", "wet"
        capability "Sound Sensor"      			//"detected", "not detected"
        capability "Presence Sensor"			//"present", "not present"
        capability "Acceleration Sensor"		//"active", "inactive"
        capability "Refresh"		
        
        attribute "lastCheckin", "Date"
         
        command "setStatus"
	}


	simulator {
	}
    
    preferences {
        input name: "sensorType", title:"Select a Sensor Type" , type: "enum", required: true, options: ["Motion Sensor", "Contact Sensor", "Switch", "Smoke Detector", "Water Sensor", "Sound Sensor", "Presence Sensor", "Acceleration Sensor"]
        
        input name: "motionActiveStr", title:"Motion Active Value" , type: "string", required: false
        input name: "contactActiveStr", title:"Contact Open Value" , type: "string", required: false
        input name: "smoketActiveStr", title:"Smoke Detect Value" , type: "string", required: false
        input name: "waterActiveStr", title:"Water Wet Value" , type: "string", required: false
        input name: "soundActiveStr", title:"Sound Detect Value" , type: "string", required: false
        input name: "presenceActiveStr", title:"Presence Present Value" , type: "string", required: false
	}

	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
                attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
                attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
                attributeState "dry", label:'${name}', backgroundColor: "#ffffff", icon:"st.alarm.water.dry" 
            	attributeState "wet", label:'${name}', backgroundColor: "#53a7c0", icon:"st.alarm.water.wet"
                attributeState "clear", label:'${name}', backgroundColor: "#ffffff", icon:"st.alarm.smoke.clear" 
            	attributeState "detected", label:'${name}', backgroundColor: "#e86d13", icon:"st.alarm.smoke.smoke" 
                attributeState "not present", label:'${name}', backgroundColor: "#ffffff", icon:"st.presence.tile.presence-default" 
            	attributeState "present", label:'present', backgroundColor: "#53a7c0", icon:"st.presence.tile.presence-default" 
                attributeState "not_home", label:'not present', backgroundColor: "#ffffff", icon:"st.presence.tile.presence-default" 
            	attributeState "home", label:'present', backgroundColor: "#53a7c0", icon:"st.presence.tile.presence-default" 
				attributeState "on", label:'${name}', action:"off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'${name}', action:"off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
        
        valueTile("entity_id", "device.entity_id", width: 4, height: 2) {
            state "val", label:'${currentValue}', defaultState: true
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh", icon:"st.secondary.refresh"
        }
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setStatus(String value){
    if(state.entity_id == null){
    	return
    }
	log.debug "Status[${state.entity_id}] >> ${value}"
    sendEvent(name:"switch", value:value)
    setSensorValue(value)
    
    sendEvent(name: "lastCheckin", value: new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone), displayed: false)
    sendEvent(name: "entity_id", value: state.entity_id, displayed: false)
}

def setHASetting(url, password, deviceId){
	state.app_url = url
    state.app_pwd = password
    state.entity_id = deviceId
}

def setSensorValue(value){
    switch(settings.sensorType){
    case "Motion Sensor":
    	sendEvent(name:"motion", value: (settings.motionActiveStr == "" ? value : (settings.motionActiveStr == value ? "active" : "inactive")))
    	break;
    case "Contact Sensor":
    	sendEvent(name:"contact", value: (settings.contactActiveStr == "" ? value : (settings.contactActiveStr == value ? "open" : "closed")))
    	break;
    case "Smoke Sensor":
    	sendEvent(name:"smoke", value: (settings.smoketActiveStr == "" ? value : (settings.smoketActiveStr == value ? "detected" : "clear")))
    	break;
    case "Water Sensor":
    	sendEvent(name:"water", value: (settings.waterActiveStr == "" ? value : (settings.waterActiveStr == value ? "wet" : "dry")))
    	break;
    case "Sound Sensor":
    	sendEvent(name:"sound", value: (settings.soundActiveStr == "" ? value : (settings.soundActiveStr == value ? "detected" : "not detected")))
    	break;
    case "Presence Sensor":
    	sendEvent(name:"presence", value: (settings.presenceActiveStr == "" ? value : (settings.presenceActiveStr == value ? "present" : "not present")))
    	break;
    }
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
    sendCommand(options, callback)
}

def on(){
	log.debug "Command [${state.entity_id}] >> on" 
    def temp = state.entity_id.split("\\.")
    def options = [
     	"method": "POST",
        "path": "/api/services/" + temp[0] + "/turn_on",
        "headers": [
        	"HOST": state.app_url,
            "x-ha-access": state.app_pwd,
            "Content-Type": "application/json"
        ],
        "body":[
        	"entity_id":"${state.entity_id}"
        ]
    ]
    sendCommand(options, null)
}

def off(){
	log.debug "Command [${state.entity_id}] >> off"
    def temp = state.entity_id.split("\\.")
    def options = [
     	"method": "POST",
        "path": "/api/services/" + temp[0] + "/turn_off",
        "headers": [
        	"HOST": state.app_url,
            "x-ha-access": state.app_pwd,
            "Content-Type": "application/json"
        ],
        "body":[
        	"entity_id":"${state.entity_id}"
        ]
    ]
    sendCommand(options, null)
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg
    try {
        msg = parseLanMessage(hubResponse.description)
		def jsonObj = new JsonSlurper().parseText(msg.body)
        setStatus(jsonObj.state)
        setSensorValue(jsonObj.state)
    } catch (e) {
        log.error "Exception caught while parsing data: "+e;
    }
}

def updated() {
}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}