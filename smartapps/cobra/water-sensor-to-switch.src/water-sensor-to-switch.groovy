/**
 *  ****************  Switch Follows Water Sensor  ****************
 *
 *  Design Usage:
 *  This was designed to be used with a water sensor to control a switch
 *  Uses a water sensor to receive commands and converts that to switch actions
 *
 *
 *  Copyright 2017 Andrew Parker
 *  
 *  This SmartApp is free!
 *  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://www.paypal.me/smartcobra
 *  
 *
 *  I'm very happy for you to use this app without a donation, but if you find it 
 *  useful then it would be nice to get a 'shout out' on the forum! -  @Cobra
 *  Have an idea to make this app better?  - Please let me know :)
 *
 *  Website: http://securendpoint.com/smartthings
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @Cobra
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Last Update: 18/11/2017
 *
 *  Changes:
 *
 *  
 * 	
 *  
 *  V2.0.0 - Complete re-write - Added additional options and re-wrote actions 
 *  V1.0.0 - POC
 *
 */
 
 
 
 
definition(
    name: "Water Sensor to Switch",
    namespace: "Cobra",
    author: "Andrew Parker",
    description: "Uses a water sensor to receive commands and converts that to switch actions",
    category: "",
    iconUrl: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
	iconX2Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
    iconX3Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
    )

preferences {
	section("") {
        paragraph " V2.0.0 "
        paragraph image: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
                  title: "Water Sensor to Switch",
                  required: false,
                  "Uses a water sensor to receive commands and converts that to switch actions"
         }         

 	section("Switch to enable/disable app"){
		input "enableswitch1",  "capability.switch", title: "SmartApp Control Switch - Optional", multiple: true, required: false
}
	section("") {
		input "alarm", "capability.waterSensor", title: "Water Sensor", required: true
        
	}
	 section("Turn On/Off this switch "){
		input "switch1",  "capability.switch", title: "Switch to control", multiple: true, required: false
     	input "trigger", "enum", title: "Select Actions",required: true, submitOnChange: true, options: ["On when wet - Off when dry", "On when dry - Off when wet", "On when dry only (No off)", "On when wet only (No off)", "Off when dry only (No on)", "Off when wet only (No on)"]
	    input "msgDelay", "number", title: "Delay between actions (Enter 0 for no delay)", defaultValue: '0', description: "Seconds", required: true
     }
    section("Logging"){
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: false
        }
	}


def installed() {
	initialise()
   
}

def updated() {
	unsubscribe()
	initialise()
   
}

def initialise() {
log.debug "Initialised with settings: ${settings}"
 setAppVersion()
 logCheck()
 state.enable1 = 'on'
 state.timer1 = true
 
subscribe(enableswitch1, "switch", enableswitchHandler)
subscribe(switch1, "switch", switch1Handler)    
    
   if(trigger == 'On when wet - Off when dry'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option1Handler)
    }

	else if(trigger == 'On when dry - Off when wet'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option2Handler)
    }
    
	else if(trigger == 'On when dry only (No off)'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option3Handler)
    }    

	else if(trigger == 'On when wet only (No off)'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option4Handler)
    }    
    
	else if(trigger == 'Off when dry only (No on)'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option5Handler)
    }  

	else if(trigger == 'Off when wet only (No on)'){
     LOGDEBUG( "Trigger is $trigger")
subscribe(alarm, "water", option6Handler)
    }  
  
    

}


// Handlers
  
def enableswitchHandler(evt) {
state.enable1 = evt.value
LOGDEBUG("Control switch event = $state.enable1")
} 

def switch1Handler(evt){
LOGDEBUG("Switch event = $evt.value")

}


  
// On when wet - Off when dry
def option1Handler(evt){
def option1 = evt.value
LOGDEBUG("Action: On when wet - Off when dry")
LOGDEBUG("Sensor is: $option1")

	if(state.enable1 == 'on' && state.timer1 == true){  
    LOGDEBUG("state.enable1 = $state.enable1 - state.timer1 = $state.timer1")
   	 	if(option1 == "wet"){turnOn()}
     	if(option1 == "dry"){turnOff()}
        
    }
    
   else if(state.enable1 == 'off'){
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
    else if(state.timer1 == false){
   LOGDEBUG("Timer is active so not processing further")
   }
}

// On when dry - Off when wet
def option2Handler(evt){
def option2 = evt.value
LOGDEBUG("Action: On when dry - Off when wet")
LOGDEBUG("Sensor is: $option2")
	if(state.enable1 == 'on' && state.timer1 == true){        
   	 	if(option2 == 'wet'){turnOff()}
     	if(option2 == 'dry'){turnOn()}
    }
    
   else if(state.enable1 == 'off'){
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
}


// On when dry only (No off)
def option3Handler(evt){
def option3 = evt.value
LOGDEBUG("Action: On when dry only (No off)")
LOGDEBUG("Sensor is: $option3")
	if(state.enable1 == 'on' && state.timer1 == true){       
   	 	if(option3 == 'wet'){LOGDEBUG("Wet - Take no action")}
     	if(option3 == 'dry'){turnOn()}
    }
    
   else if(state.enable1 == 'off'){
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
}

// On when wet only (No off)
def option4Handler(evt){
def option4 = evt.value
LOGDEBUG("Action: On when wet only (No off)")
LOGDEBUG("Sensor is: $option4")
	if(state.enable1 == 'on' && state.timer1 == true){       
   	 	if(option4 == 'wet'){turnOn()}
      	if(option4 == 'dry'){ LOGDEBUG("Dry - Take no action")}
    }
    
   else if(state.enable1 == 'off'){
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
}

// Off when dry only (No on)
def option5Handler(evt){
def option5 = evt.value
LOGDEBUG("Action: On when wet only (No off)")
LOGDEBUG("Sensor is: $option5")
	if(state.enable1 == 'on' && state.timer1 == true){      
   	 	if(option5 == 'wet'){ LOGDEBUG("Wet - Take no action")}
      	if(option5 == 'dry'){turnOff()}
    }
    
   else if(state.enable1 == 'off' && state.timer1 == true){    
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
}

// Off when wet only (No on)
def option6Handler(evt){
def option6 = evt.value
LOGDEBUG("Action: On when wet only (No off)")
LOGDEBUG("Sensor is: $option6")
	if(state.enable1 == 'on' && state.timer1 == true){        
   	 	if(option6 == 'wet'){turnOff()}
        if(option6 == 'dry') { LOGDEBUG("Dry - Take no action")}
    }
    
   else if(state.enable1 == 'off'){
   LOGDEBUG("Control Switch is OFF so not processing further")
   }
}












// Control on/off actions & timer

def turnOn(){
LOGDEBUG("Turning on...")
		switch1.on()
		startTimer()
}



def turnOff(){
LOGDEBUG("turning off...")
		switch1.off()
		startTimer()
}

def startTimer(){
LOGDEBUG("Starting timer...")
state.timer1 = false
state.timeDelay =  msgDelay
LOGDEBUG(" state.timeDelay =  $state.timeDelay seconds")
LOGDEBUG("Waiting for $msgDelay seconds before resetting timer to allow further actions")
runIn(state.timeDelay, resetTimer)
}

def resetTimer() {
state.timer1 = true
LOGDEBUG( "Timer reset - Actions allowed")
}




 

     
     
     
     
// Logging & App version...
     
// define debug action
def logCheck(){
state.checkLog = debugMode
if(state.checkLog == true){
log.info "All Logging Enabled"
}
else if(state.checkLog == false){
log.info "Further Logging Disabled"
}

}
def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${app.label.replace(" ","_").toUpperCase()}  (AppVersion: ${state.appversion}) - ${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}


def setAppVersion(){
    state.appversion = "2.0.0"
}