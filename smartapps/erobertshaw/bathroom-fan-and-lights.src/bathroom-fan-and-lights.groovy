/**
 *  Bathroom fan and lights
 *
 *  Copyright 2015 Aperations.com llc
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
definition(
    name: "Bathroom fan and lights",
    namespace: "erobertshaw",
    author: "Aperations.com llc",
    description: "Presence sensor based intelligence for bathroom lights and fans.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Main room sensor") {
        input "primaryLights", "capability.switch", multiple: true, required: true ,  title: "Primary lights"
        input "showerLights", "capability.switch", multiple: true, required: false ,  title: "Shower lights"
        input "primaryFan", "capability.switch", multiple: true, required: false ,  title: "Primary Fan"
    	input "primarySensor", "capability.motionSensor", multiple: true, required: true ,  title: "Primary Sensor"
        input "showerSensor", "capability.motionSensor", multiple: true, required: false ,  title: "Shower Sensor"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {    
    state.occupied = false 
    subscribe(primarySensor, "motion", primaryMotionDetected)
	subscribe(showerSensor, "motion", showerMotionDetected)
}


def primaryMotionDetected(evt) {
  if("active" == evt.value) {
    log.debug "active" 
    turnOnPrimaryLightsAndFan()
  } else if("inactive" == evt.value) {
    log.debug "inactive"
    Boolean lastInactive = true;
    primarySensor.each { 
    	if( it.currentValue("motion") == "active"){
        	lastInactive = false;
        }
    }
    if(lastInactive) lastPrimarySensorInactive();
  }
  log.debug "Bathroom is occupied: " + state.occupied
}

def showerMotionDetected(evt) {
  if("active" == evt.value) {
    log.debug "shower active" 
    turnOnShowerLightsAndFan()
  } else if("inactive" == evt.value) {
    log.debug "shower inactive"
    Boolean lastShowerInactive = true;
    showerSensor.each { 
    	if( it.currentValue("motion") == "active"){
        	lastShowerInactive = false;
        }
    }
    if(lastShowerInactive) lastShowerSensorInactive();
  }
  log.debug "Shower is occupied: " + state.occupied
}

def lastPrimarySensorInactive(){
	log.debug "Last primary sensor went inactive"
    state.occupied = false
    runIn(60*1, turnOffPrimaryLightsAndFan)
}

def lastShowerSensorInactive(){
	log.debug "Last shower sensor went inactive"
    state.showerOccupied = false
    runIn(60*1, turnOffShowerLightsAndFan)
}

def turnOffPrimaryLightsAndFan(){
	if(state.occupied || state.showerOccupied){
    	// if sensors re occupied the room - don't turn the lights and fan off :)
        return;
    }
    
	primaryLights.each { 
    	if( it.currentValue("switch") == "on"){
        	it.off()
        }
    }
 
 	if(!state.showerOccupied){
    	runIn(60*3, turnOffFan)
    }
}

def turnOffShowerLightsAndFan(){
	if(state.showerOccupied){
    	// if sensors re occupied the room - don't turn the lights and fan off :)
        return;
    }
    
	showerLights.each { 
    	if( it.currentValue("switch") == "on"){
        	it.off()
        }
    }
 
    runIn(60*9, turnOffFan)
}


def turnOffFan(){
	if(state.occupied || state.showerOccupied){
    	// if sensors re occupied the room - don't turn the lights and fan off :)
        return;
    }
    primaryFan.each { 
    	if( it.currentValue("switch") == "on"){
        	it.off()
        }
    }
}

def turnOnPrimaryLightsAndFan(){
	
	state.occupied = true
    log.debug "Turn on lights"
	primaryLights.each { 
    	log.debug it
    	log.debug it.currentValue("switch")
    	if( it.currentValue("switch") != "on"){
        	it.on()
        }
    }
    
    turnOnFan()
  
}

def turnOnShowerLightsAndFan(){
	state.showerOccupied = true
    log.debug "Turn on shower lights"
	showerLights.each { 
    	log.debug it
    	log.debug it.currentValue("switch")
    	if( it.currentValue("switch") != "on"){
        	it.on()
        }
    }
    turnOnFan()
}

def turnOnFan(){
  log.debug "Turn on fan"
  primaryFan.each { 
  	if( it.currentValue("switch") != "on"){
    	it.on()
    }
  }
}
