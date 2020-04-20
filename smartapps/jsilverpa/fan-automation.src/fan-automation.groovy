/**
 *  Turn on/off a fan
 *
 *  Copyright 2016 Joshua Silver
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
    name: "Fan Automation",
    namespace: "jsilverpa",
    author: "Joshua Silver",
    description: "Turns on a fan when the outside temperature is X degrees cooler than the inside temperature. ",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png")
 


preferences {
    section("Temperature Sensors") {
		input "temperatureSensorIn", "capability.temperatureMeasurement", title: "Fan Room",  required: true
		input "temperatureSensorOut", "capability.temperatureMeasurement", title: "Outdoor Temperature Sensor", required: true
	}

    section("Fan"){
		input "mySwitch", "capability.switch", title: "Fan Switch", required: true
	}
    section("Advanced Options") {
		input "offset", "number", title: "Turn on fan when temperature is more than this degrees hotter than outside", required: true, defaultValue: 5
		input "motion", "capability.motionSensor", title: "Only allow fan when motion is detected here. Leave blank to not require motion", required: false	
    	input "manualTime", "number", title: "Override fan automation for this many minutes when fan switch is manually pressed", defaultValue: 5 
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
	subscribe(temperatureSensorIn, "temperature", temperatureInHandler)
	subscribe(temperatureSensorOut, "temperature", temperatureOutHandler)
    subscribe(mySwitch, "switch", switchHandler)
    subscribe(motion, "motion", motionHandler)
	state.lastAppSwitchChange = 0   //track last time app caused switch state to change
    state.lastManualSwitchChange = 0   //track last time manual push caused switch state to change
	log.debug "Motion is $motion"
    // how many minutes a manual switch action should override automation
    state.manualOverrideTime = manualTime /* minutes */ * 60000  /*msec/min*/  
    evaluate()
}



def temperatureInHandler(evt) {
      log.debug "In tempInHandler"    
      evaluate()
}

def temperatureOutHandler(evt) {
    log.debug "In tempOutHandler" 
    evaluate()
}


def motionHandler(evt)
{
	log.debug "Motion Handler"
    evaluate()
 
}

//Need to detect why this switch was called (i.e. manual press of the switch or called by the SmartApp
//in response to some conditions.  It should be possible to call evt.source but this seems to have a bug in ST.
//Instead, use this hack/workaround to see if the app changed the switch state recently and if so assume
//that the source of this event was not a manual press of the device.
def switchHandler(evt) {
	
    double callDate = now();
    log.debug "call date $callDate, lastAppSwitchChange ${state.lastAppSwitchChange}"
    //if the app updated the switch < 5 seconds ago, assume this state switch was app initiated
    if ((callDate - state.lastAppSwitchChange) < (5 /*sec*/  * 1000  /* ms/sec */)) { 
    	log.debug "Switch called by app"
    }
    else {
    	state.lastManualSwitchChange = callDate;
        runIn(state.manualOverrideTime/1000 + 5 /* padding for scheduler */, evaluate)  //rerun once override time has passed
    	log.debug "Switch called by device at $callDate"
    }

}



//keep track of time when app turned the switch on or off.
//use this to distinguish a manual press of the switch from a 
//automated press of the switch
def appTurnSwitch(nextState) {
	if (mySwitch.currentSwitch == nextState) return;
     
    state.lastAppSwitchChange = now();
    if (nextState == "on") {
        log.debug "TURNING ON"
    	mySwitch.on()
    }
    else {
        log.debug "TURNING OFF"
        mySwitch.off()
    }
}


def evaluate() {
    log.debug "TSI ($temperatureSensorIn.currentTemperature  $state.lastManualSwitchChange )"
    
    /* ignore any changes if the user has recently pushed the switch manually  */
    if ((now() - state.lastManualSwitchChange) < state.manualOverrideTime)  {
      	log.debug "Skipping evaluate since user has manually pressed button recently"
        return;
    }
    
    int lastIn = temperatureSensorIn != null ? temperatureSensorIn.currentTemperature : -1
    int lastOut = temperatureSensorOut != null ? temperatureSensorOut.currentTemperature : 0
  
    boolean useMotion = (motion != 0 && motion != null);
    boolean active = motion && motion != null && (motion.currentMotion == "active");
    boolean motionOn = (useMotion && motion && ( motion.currentMotion == "active")) ? true : false;
    log.debug "in EVALUATE ($lastIn, $lastOut, $offset, $useMotion, $motionOn)"
    def currentSwitchState = mySwitch.currentSwitch
    log.debug "Current switch state is $currentSwitchState"
    
    if (lastIn >= (lastOut + offset) && (!useMotion || motionOn )) {
       appTurnSwitch("on");
    }
    else {
       //turn off if no motion
       appTurnSwitch("off");
    }
}