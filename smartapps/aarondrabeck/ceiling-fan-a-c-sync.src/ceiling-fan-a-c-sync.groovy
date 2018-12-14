/**
 *  Copyright 2017 Aaron Drabeck
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
 *  
 *  Author: Aaron Drabeck
 */

definition(
    name: "Ceiling Fan A/C Sync",
    namespace: "aarondrabeck",
    author: "Aaron Drabeck",
    description: "Turns on switches when the operating mode of a thermostat changes.  Helps to mix the air in your home providing for a more even heating or cooling.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion-presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion-presence@2x.png"
)

preferences {
	section("When these thermostats...") {
		input "thermostat1", "capability.thermostat", title: "Which thermostats?", multiple: true
	}
    section("Are changed to these operating modes...") {
        input name: "opStates", type: "enum", title: "Operational Modes", options: ["heating","idle","pending cool","vent economizer","cooling","pending heat","fan only"], multiple: true
    }
	section("Turn on...") {
		input "switchesOn", "capability.switch", title: "Select switchs to turn on", multiple: true, required: false
	}
    section("Turn off...") {
		input "switchesOff", "capability.switch", title: "Select switchs to turn off", multiple: true, required: false
	}
    section("Delay Turn On (seconds)") {
		 input "onDelay", "number", title: "Seconds to delay turn on", required: false
	}
    section("Delay Turn Off (seconds)") {
		 input "offDelay", "number", title: "Seconds to delay turn off", required: false
	}
	
}

def installed() {
	subscribe(thermostat1, "thermostatOperatingState.heating", heatingHandler)
    subscribe(thermostat1, "thermostatOperatingState.idle", idleHandler)
    subscribe(thermostat1, "thermostatOperatingState.vent economizer", ventEcoHandler)
    subscribe(thermostat1, "thermostatOperatingState.pending cool", pendingCoolHandler)
    subscribe(thermostat1, "thermostatOperatingState.cooling", coolingHandler)
    subscribe(thermostat1, "thermostatOperatingState.pending heat", pendingHeatHandler)
    subscribe(thermostat1, "thermostatOperatingState.fan only", fanOnlyHandler)
}

def updated() {
	unsubscribe()
	subscribe(thermostat1, "thermostatOperatingState.heating", heatingHandler)
    subscribe(thermostat1, "thermostatOperatingState.idle", idleHandler)
    subscribe(thermostat1, "thermostatOperatingState.vent economizer", ventEcoHandler)
    subscribe(thermostat1, "thermostatOperatingState.pending cool", pendingCoolHandler)
    subscribe(thermostat1, "thermostatOperatingState.cooling", coolingHandler)
    subscribe(thermostat1, "thermostatOperatingState.pending heat", pendingHeatHandler)
    subscribe(thermostat1, "thermostatOperatingState.fan only", fanOnlyHandler)
}

def activate() {

   def _offDelay = 0;
   if(offDelay != null)  {
       _offDelay = offDelay
    }
       
   def _onDelay = 0;
   if(onDelay != null) {
       _onDelay = onDelay
	}
    
   if(switchesOn != null) {
       runIn(_onDelay, turnOn)   		
   }
   
   if(switchesOff != null) {
   		runIn(_offDelay, turnOff)  
   }
}

def turnOn() {
	switchesOn.on()	
}

def turnOff() {
	switchesOff.off()	
}

def heatingHandler(evt) {
     if ( opStates.any { it == "heating" }  ) {
        log.debug( "Operating State has changed to heating, activing switches." )
       activate()       
    }
}

def idleHandler(evt) {
     if ( opStates.any { it == "idle" }  ) {
        log.debug( "Operating State has changed to idle, activing switches." )
        activate()
    }
}

def ventEcoHandler(evt) {
     if ( opStates.any { it == "vent economizer" }  ) {
        log.debug( "Operating State has changed to vent economizer, activing switches." )
         activate()
    }
}

def pendingCoolHandler(evt) {
     if ( opStates.any { it == "pending cool" }  ) {
        log.debug( "Operating State has changed to pending cool, activing switches." )
        activate()
    }
}

def coolingHandler(evt) {
     if ( opStates.any { it == "cooling" }  ) {
        log.debug( "Operating State has changed to cooling, activing switches." )
         activate()	
    }
}

def pendingHeatHandler(evt) {
     if ( opStates.any { it == "pending heat" }  ) {
        log.debug( "Operating State has changed to pending heat, activing switches." )
         activate()	
    }
}

def fanOnlyHandler(evt) {
     if ( opStates.any { it == "fan only" }  ) {
        log.debug( "Operating State has changed to fan only, activing switches." )
         activate()
    }
}
