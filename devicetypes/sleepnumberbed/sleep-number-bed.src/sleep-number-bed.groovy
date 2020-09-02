/**
 *  Sleep Number
 *
 *  Copyright 2019 Tim Parsons
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Sleep Number Bed", namespace: "sleepNumberBed", author: "Classic_Tim", cstHandler: true) {
		capability "Switch Level"
        capability "Switch"
        capability "PresenceSensor"
        
        attribute "bedId", "String"
        attribute "side", "String"
        
        
        command "setBedId", ["string"]
        command "setSide", ["string"]
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale:2){
        standardTile("switch", "device.switch", width: 4, height: 4, canChangeIcon: false) {
            state "on", label:'RAISED', action:"off", icon:"https://raw.githubusercontent.com/ClassicTim1/SleepNumberManager/master/icons/raisedBed-icn3.png", backgroundColor:"#79b821"
            state "off", label:'FLAT', action:"on", icon:"st.Bedroom.bedroom2", backgroundColor:"#ffffff"
        }
        
        controlTile("levelSliderControl", "device.level", "slider", height: 4, width: 2) {
   			state "level", action:"switch level.setLevel"
		}
		valueTile("Side", "device.side", width: 3, height: 1){
        	state "default", label: '${currentValue} Side'
        }   
		valueTile("Presence", "device.PresenceState", width: 3, height: 1){
        	state "default", label: '${currentValue}'
        } 
   //     valueTile("level", "device.level",height:2, width:2, inactiveLabel: false, decoration: "flat") {
	//		state "level", label: 'Sleep Number: ${currentValue}'
	//	} 
    }
}

def updateData(String state, Integer sleepNumber, boolean present){
	sendEvent(name: "switch", value: state)
	sendEvent(name: "level", value: sleepNumber)
    if(present)
		sendEvent(name: "PresenceState", value: "Present")
    else
		sendEvent(name: "PresenceState", value: "Not Present")
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'level' attribute

}

def setLevel(level) {
	sendEvent(name: "level", value: level)
    String side = "L"
    if(device.latestState('side').stringValue == "Right"){
    	side = "R"
    }
    parent.setNumber(device.latestState('bedId').stringValue, side, Math.round(level))
}


def setBedId(val){
	sendEvent(name: "bedId", value: val)
}

def setSide(val){
	sendEvent(name: "side", value: val)
}

def on(){
    String side = "L"
    if(device.latestState('side').stringValue == "Right"){
    	side = "R"
    }
	sendEvent(name: "switch", value: "on")
    parent.raiseBed(device.latestState('bedId').stringValue, side)
}

def off(){
    String side = "L"
    if(device.latestState('side').stringValue == "Right"){
    	side = "R"
    }
	sendEvent(name: "switch", value: "off")
    parent.lowerBed(device.latestState('bedId').stringValue, side)
}