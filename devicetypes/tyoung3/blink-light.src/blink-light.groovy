/**
 *  Copyright 2018 Tom Young, blinklight@twyoung.com
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
 *  Blink Light:
 *		Blink a light when motion is detected or  open/close sensor opens or closes then reset light to original state.
 *	Optionally, send blink SOS when motion is detected.  
 *
 *  Author: Tom Young, blinklight@twyoung.com, 11/12/'18
 */
definition(
    name: "Blink Light",
    namespace: "tyoung3",
    author: "Tom Young",
    description: "Blink a light when a motion or contact event occurs.", 
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When there is motion or a contact change, blink a light up to ten seconds.  Optionally, blink S.O.S if motion detected.") {
		input "motion1", "capability.motionSensor", title: "What motion?", multiple: true,required: false 
	}
	section("Blink a light when this contact opens or closes.") {
		input "contact1", "capability.contactSensor", title: "What contact?",multiple: true,required: false
	}
	section("Blink this light, up to ten seconds.") {
		input "switch1", "capability.switch", title: "Which light?",required: true
	}
	section("Light timing for motion or open contact.") {
		input "nblinks", "number", title: "Number of Blinks?\nWARNING: Too high a number may exceed ST limits.", required: true,defaultValue: 5
		input "mson", "number", title: "Milliseconds light on?", required: true,defaultValue: 100
		input "msoff", "number", title: "Milliseconds light off?", required: true,defaultValue: 100
	}
	section("Light timing for closed contact or SOS signal.") {
		input "cnblinks", "number", title: "Number of Blinks? If over 1000, motion will issue S.O.S..", required: true,defaultValue: 0
		input "cmson", "number", title: "Milliseconds light on or S.O.S. ms timing unit?", required: true,defaultValue: 100
		input "cmsoff", "number", title: "Milliseconds light off?", required: true,defaultValue: 102
	}
}

def installed() {
	subscribe(motion1, "motion.active", motionActive)
	subscribe(contact1, "contact", blinker)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion.active", motionActive)
	subscribe(contact1, "contact", blinker)
}

   
def turnOff(int ms) {
        switch1.off()
       	pause(ms)
}

def turnOn(int ms) {
	switch1.on() 
  	pause(ms)
}

def timeOK(int nblinks, int mson, int msoff) {

   if(  nblinks * (mson + msoff) < 10000 ) {     /** Maximum Ten second time limit */
   		return 1
   }
   
   log.error "Blink time exceeds ten seconds"
   return 0
}

def resetSwitch( String swval) {

   if(swval == "on") {
   		switch1.on()
        log.debug " resetSwitch:"     
   } else {
   if(swval == "off") {
   		log.debug " resetSwitch: sw is off"
   } else {
   if(swval == "null") {
   		log.debug "resetSwitch: sw is null"
   } else {    
   		log.error " resetSwitch: swval=$swval is not on or off" 
   }}}
   
}   
   
def blinkLight(int n, int mson, int msoff) {
	int count=0
    
   def swval = switch1.currentSwitch
   log.debug "blinkLight/swval: $swval"
   log.debug "swval instanceof String? ${swval instanceof String}"
   log.debug "swval instanceof Number? ${swval instanceof Number}"
   
   if (timeOK(nblinks,mson,msoff)) {     /** Maximum Ten second time limit */
    	while(count<n) {
            turnOn(mson) 
        	turnOff(msoff)
            count++
    	}
   }
        
   resetSwitch(swval)  
   
}

def blinker(evt) {     /* Note twenty second limit on SmartApps.  10 second limit here.  */ 
    
	log.debug "blinker: $evt.value"
	if (evt.value == "open") {
    	blinkLight(nblinks,mson,msoff)
	} else if (evt.value == "closed") { 
    	if(cnblinks<1001) {
    		blinkLight(cnblinks,cmson,cmsoff)
        }    
	}
}

def motionActive(evt) {

	log.debug "Motion: $evt.value"
    if(cnblinks>1000) {
    	sendSOS()
    } else {    
		blinkLight(nblinks,mson,msoff)
    }    
}
  
def sendSOS() {
	def swval = switch1.currentSwitch
	log.debug "sendSOS tu=$cmson swval=$swval"
    if(swval == "on") {
        turnOff(7*cmson)  /* seven time units between words. For five words/minute set Morse timing unit(cmson) = 240ms.*/
    }
    def dit = { turnOn(cmson);   turnOff(cmson); }
    def dah = { turnOn(3*cmson); turnOff(cmson); }
    def ics = { pause(2*cmson) } /* Inter character spacing is three time units, but one unit has elapsed already */
    dit(); dit(); dit(); ics()
    dah(); dah(); dah(); ics()
    dit(); dit(); dit() 
    pause(6*cmson)   
    resetSwitch(swval) 
}