/**
 *  Powershades Motorized Shade Device Handler for SmartThings Classic
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
 
 
 
// VERSION 1.0 JAC 11/19/2018 Initial Version
// VERSION 2.0 JAC 02/18/2019 100% is now Open, 0% is now closed.
 
include 'asynchttp_v1'
 
metadata {
	definition (name: "Powershade", namespace: "powershades", author: "Wideband Labs LLC") {
		capability "Window Shade"
        capability "Switch Level"
        capability "Switch"
        
        command "changeShadeState", ["string"]
       	command "open"
		command "close"
        command "on"
        command "off"
	}

	simulator {
	}

tiles() {
        multiAttributeTile(name:"shade", type: "device.switch", width: 4, height: 6) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("closed",  label:'', action:"open", icon:"https://s3-us-west-2.amazonaws.com/powershades-stc/ShadeDownTrans-small.png", backgroundColor:"#FFFFFF", nextState: "open")
                attributeState("open",    label:'', action:"close", icon:"https://s3-us-west-2.amazonaws.com/powershades-stc/ShadeUpTrans-small.png", backgroundColor:"#FFFFFF", nextState: "closed")
             }
               //puts a slider on the right sid of the main tile
             tileAttribute("device.level", key: "SLIDER_CONTROL") {
               	attributeState "level", action:"switch level.setLevel", defaultState: true
           	 }
        }

		// Main Top Tile and Tile on Front Screen, works as toggle.
        standardTile("switchmain", "device.switch") { 
            state("closed",  label:'down', action:"open", icon:"https://s3-us-west-2.amazonaws.com/powershades-stc/ShadeDownTrans-small.png", backgroundColor:"#FFFFFF", nextState: "open")
            state("open",    label:'up', action:"close", icon:"https://s3-us-west-2.amazonaws.com/powershades-stc/ShadeUpTrans-small.png", backgroundColor:"#FFFFFF", nextState: "closed")
            state("default", label:'up', action:"close", icon:"https://s3-us-west-2.amazonaws.com/powershades-stc/ShadeUpTrans-small.png", backgroundColor:"#FFFFFF")
            
        }
      

		// Static Buttons
		standardTile("on", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("on", label:'open', action:"open")
        }
        standardTile("off", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("close", label:'close', action:"close")
            state("default", label:'close', action:"close")
        }
	}
		

        // the "switchmain" tile will appear in the Things view
        main(["switchmain"])
        details(["shade", "on", "off", "lengthyTile"])
        
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
    close()
}
def off() {
	log.debug "OFF COMMAND RECEIVED"
    open()
}

def open() {
    log.trace "open()"
	log.debug "$ShadeName"
    def params = [
        uri: 'https://alexa.powershades.com',
        //path: '/api/shade/'+ShadeName+'/open/'+APItoken+'/',
        path: '/api/shade/'+ShadeName+'/'+ 100 +'/'+APItoken+'/',
    ]
    asynchttp_v1.get(processResponse, params)
    sendEvent(name:"level", value:100)
    
}

def close() {
    log.trace "close()"
 	log.debug "$ShadeName"
    def params = [
        uri: 'https://alexa.powershades.com',
        //path: '/api/shade/'+ShadeName+'/close/'+APItoken+'/',
        path: '/api/shade/'+ShadeName+'/'+ 0 +'/'+APItoken+'/',
    ]
    asynchttp_v1.get(processResponse, params)
    sendEvent(name:"level", value:0)
    
}

def setLevel(lvl) {
	
	log.debug "Value Changed to: " + lvl
    def lvlInt = Math.min(lvl as Integer, 100)
    // if level is > 0 
    if (lvlInt == 0) {
		sendEvent(name: "switch", value: "close"); 
    	sendEvent(name:"level", value:lvlInt)
    } else if (lvlInt == 100) {
    	sendEvent(name: "switch", value: "open")
        sendEvent(name:"level", value:lvlInt)
    } else
    {
    	sendEvent(name:"level", value:lvlInt)
        def params = [
            uri: 'https://alexa.powershades.com',
            path: '/api/shade/'+ShadeName+'/'+lvlInt+'/'+APItoken+'/',
        ]
        asynchttp_v1.get(processResponse, params)
   	}
}

