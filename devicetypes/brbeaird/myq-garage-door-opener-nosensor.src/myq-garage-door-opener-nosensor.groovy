/**
 *  MyQ Garage Door Opener NoSensor
 *
 *  Copyright 2018 Brian Beaird
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
 *  Last Updated : 2019-01-04
 *
 */
metadata {
	definition (name: "MyQ Garage Door Opener-NoSensor", namespace: "brbeaird", author: "Brian Beaird", vid: "generic-contact-4", ocfdevicetype: "oic.d.garagedoor", mnmn: "SmartThings") {
		capability "Door Control"
		capability "Garage Door Control"
		capability "Actuator"
        //capability "Health Check" Will be needed eventually for new app compatability but is not documented well enough yet
        
        attribute "OpenButton", "string"
        attribute "CloseButton", "string"
        
        command "open"
        command "close"
	}

	simulator {	}

	tiles {
		
		multiAttributeTile(name:"door", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute ("device.door", key: "PRIMARY_CONTROL") {
				attributeState "unknown", label:'MyQ Door (No sensor)', icon:"st.doors.garage.garage-closed",    backgroundColor:"#6495ED"				
			}			
		}
      
        standardTile("openBtn", "device.OpenButton", width: 3, height: 3) {
            state "normal", label: 'Open', icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13", action: "open", nextState: "opening"
            state "opening", label: 'Opening', icon: "st.doors.garage.garage-opening", backgroundColor: "#cec236", action: "open"
		}
        standardTile("closeBtn", "device.CloseButton", width: 3, height: 3) {            
            state "normal", label: 'Close', icon: "st.doors.garage.garage-closed", backgroundColor: "#00a0dc", action: "close", nextState: "closing"
            state "closing", label: 'Closing', icon: "st.doors.garage.garage-closing", backgroundColor: "#cec236", action: "close"
		}
        
		main "door"
		details(["door", "openBtn", "closeBtn"])
	}
}

def open()  {
    openPrep()
    parent.sendCommand(this, "desireddoorstate", 1)    
}
def close() {
    closePrep()
    parent.sendCommand(this, "desireddoorstate", 0) 
}

def openPrep(){
	sendEvent(name: "door", value: "opening", descriptionText: "Open button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Opening!"
    runIn(20, resetToUnknown) //Reset to normal state after 20 seconds
}

def closePrep(){
	sendEvent(name: "door", value: "closing", descriptionText: "Close button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Closing!"
    runIn(20, resetToUnknown)  //Reset to normal state after 20 seconds
}



def resetToUnknown(){
	sendEvent(name: "door", value: "unknown", isStateChange: true, display: false, displayed: false)
    sendEvent(name: "OpenButton", value: "normal", displayed: false, isStateChange: true)
    sendEvent(name: "CloseButton", value: "normal", displayed: false, isStateChange: true)
}

def log(msg){
	log.debug msg
}

def showVersion(){
	return "1.1.8"
}