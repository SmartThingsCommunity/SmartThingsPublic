/**
 *  Poweshades Controller
 *
 *  Copyright 2018 Powershades Developer
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
 
include 'asynchttp_v1'
 
metadata {
	definition (name: "PowerShades", namespace: "powershades", author: "Ben McNelly") {
		capability "Window Shade"
        capability "Switch Level"
        capability "Switch"
        // capability "Polling"
        // capability "Refresh"
        
        command "changeShadeState", ["string"]
      
	}

	simulator {
	}

tiles() {
        multiAttributeTile(name:"shade", type: "lighting", width: 4, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
                attributeState("closed",  label:'down', action:"open", icon:"https://i.imgur.com/uj2nW3o.png", backgroundColor:"#FFFFFF", nextState: "opening")
                attributeState("open",    label:'up', action:"close", icon:"https://i.imgur.com/sl0AEhw.png", backgroundColor:"#FFFFFF", nextState: "closing")
                attributeState("partially open", label:'preset', action:"presetPosition", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
                attributeState("closing", label:'${name}', action:"presetPosition", icon:"st.doors.garage.garage-closing", backgroundColor:"#bbbbdd")
                attributeState("opening", label:'${name}', action:"presetPosition", icon:"st.doors.garage.garage-opening", backgroundColor:"#ffcc33")
            }
        }

		// Main Top Tile and Tile on Front Scree, works as toggle.
        standardTile("switchmain", "device.switch") {
            state("unknown", label:'dunno', action:"refresh.refresh", icon:"https://i.imgur.com/uj2nW3o.png", backgroundColor:"#ffa81e")
            state("closed",  label:'down', action:"open", icon:"st.doors.garage.garage-opening", backgroundColor:"#bbbbdd", nextState: "opening")
            state("open",    label:'up', action:"close", icon:"https://i.imgur.com/sl0AEhw.png", backgroundColor:"#ffcc33", nextState: "closing")
            state("partially open", label:'partially open', action:"https://i.imgur.com/sl0AEhw.png", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
            state("closing", label:'closing', action:"presetPosition", icon:"st.doors.garage.garage-closing", backgroundColor:"#bbbbdd")
            state("opening", label:'opening', action:"presetPosition", icon:"https://i.imgur.com/uj2nW3o.png", backgroundColor:"#ffcc33")
            state("default", label:'preset', action:"presetPosition", icon:"https://i.imgur.com/uj2nW3o.png", backgroundColor:"#ffcc33")
        }          
            
        
        // the slider for shade position
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 6, inactiveLabel: false) {
            state("level", action:"switch level.setLevel")
        }

		// Static Buttons
		standardTile("on", "device.switch", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state("on", label:'open', action:"open", icon:"st.doors.garage.garage-opening")
        }
        standardTile("off", "device.stopStr", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state("close", label:'close', action:"close", icon:"st.doors.garage.garage-closing")
            state("default", label:'close', action:"close", icon:"st.doors.garage.garage-closing")
        }

	}
		


       
        // the "switchmain" tile will appear in the Things view
        main(["switchmain"])
        details(["shade", "on", "off", "levelSliderControl"])
        
    	preferences {
    	// Set up settings options to get API credentials and Shade Name
		input("APItoken", "string", title:"API Token", description: "Please enter your Powershades API Token", required: true, displayDuringSetup: true)
		input("ShadeName", "string", title:"Shade Name", description: "Please enter your Powershades Shade Name", required: true, displayDuringSetup: true)
		}        
        
    }
    

// parse events into attributes
def parse(String description) {
	log.error "SHOULD NOT BE HERE"
}

def poll() {
	log.debug "Executing 'poll'"   
        def lastState = device.currentValue("windowShade")
    	sendEvent(name: "windowShade", value: device.deviceNetworkId + ".refresh")
        // sendEvent(name: "motion", value: lastState);
}

def refresh() {
	log.debug "Executing 'refresh'" 
	poll();
}

// not sure if this is needed on a interactive device
def changeShadeState(newState) {

	log.trace "Received update that this sensor is now $newState"
	switch(newState) {
    	case 1:
        	log.trace 'handling case 1'
			sendEvent(name: "windowShade", value: "open")
            break;
    	case 0:
        	log.trace 'handling case 0'
        	sendEvent(name: "windowShade", value: "closed")
            break;
    }
}


def on(){
	log.debug "ON COMMAND RECEIVED"
    sendEvent(name: "switch", value: "open");
 	
}
def off() {
	log.debug "OFF COMMAND RECEIVED"
    sendEvent(name: "switch", value: "closed");

}

def open() {
    log.trace "open()"
    on()
	log.debug "$ShadeName"
    def params = [
        uri: 'https://alexa.powershades.com',
        path: '/api/shade/'+ShadeName+'/open/'+APItoken+'/',
    ]
    asynchttp_v1.get(processResponse, params)
}

def close() {
    log.trace "close()"
    off()
 	log.debug "$ShadeName"
    def params = [
        uri: 'https://alexa.powershades.com',
        path: '/api/shade/'+ShadeName+'/close/'+APItoken+'/',
    ]
    asynchttp_v1.get(processResponse, params)
}

def setLevel(lvl) {
	log.debug "Value Changed to: " + lvl
    // if level is > 0 
    if (lvl == 0) {
	sendEvent(name: "switch", value: "close"); 
    
    } else if (lvl == 100) {
    sendEvent(name: "switch", value: "open")
    } else{
         def params = [
            uri: 'https://alexa.powershades.com',
            path: '/api/shade/'+ShadeName+'/'+lvl+'/'+APItoken+'/',
        ]
        asynchttp_v1.get(processResponse, params)
   	}
}
