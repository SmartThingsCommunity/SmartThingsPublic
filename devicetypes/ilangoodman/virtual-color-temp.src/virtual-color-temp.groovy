/**
 *  Virtual Color Temp
 *
 *  Copyright 2016 Ilan Goodman
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
	definition (name: "Virtual Color Temp", namespace: "ilangoodman", author: "Ilan Goodman") {
		capability "Switch"
		capability "Switch Level"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.Kids.kid10", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.Kids.kid10", backgroundColor: "#79b821", nextState: "off"
		}       
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, backgroundColor:"#ffe71e") {
            state "level", action:"switch level.setLevel"
        }
        valueTile("lValue", "device.level", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "levelValue", label:'${currentValue}%', unit:"", backgroundColor: "#53a7c0"
        }

		main(["button"])
		details(["button", "refresh","levelSliderControl","lValue"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
	// TODO: handle 'level' attribute

}

// handle commands
def on() {
	log.debug "Executing 'on'"
	sendEvent(name: "switch", value: "on")
    log.info "Virtual Color Temp On"
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name: "switch", value: "off")
    log.info "Virtual Color Temp Off"
}

def setLevel(value) {
	log.debug "Executing 'setLevel'"
	log.info "setLevel $value"
    
    if (value < 0) {
    	value = 0
    }
    else if(value > 100) {
    	value = 100
    }
    if(value == 0) {
    	off()
    }
    else {
 		on()
 		sendEvent(name: "level", value: value)
        log.debug "Setting level to $value"
    }
}




