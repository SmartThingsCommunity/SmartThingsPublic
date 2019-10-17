definition(
    name: "Flash A Light/Device With Motion Or Button Press",
    namespace: "uksoftware",
    author: "Adrian Bellwood",
    description: "Flash A Light/Device When Motion Detected Or Button Pressed", 
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png"
    /* TO INSTALL THIS SMART APP, LOG IN TO SMARTTHINGS IDE, MAKE SURE YOU HAVE SET YOUR LOCATION CORRECTLY TOO!
    Click on My SmartApps, then top right, choose +New SmartApp. Now choose FROM CODE (Tab) then copy and paste
    all of this code in to the white box. Press Create.
    
    Now use your phones SmartThings CLASSIC APP (note classic version). Click AUTOMATION at the bottom.
    Now click + Add a SmartApp. Choose My Apps at the bottom. Now Choose 'Flash A Light/Device With Motion Or Button Press'
    You can do the rest from here, enjoy. Please feel free to play around, alter, delete or whatever else with this code.
    
    NOTE, I`m not a coder, this is just a bit of fun. It`s probably not written very neatly, but it does the job.*/
)

preferences {

	section("When a button is pressed flash a light or device.") {
		input "Abutton", "capability.button", title: "Which button?", multiple: true,required: false
	}

	section("Flash this light/device when a button is pressed.") {
		input "ButtonDevice", "capability.switch", title: "Which Device?", multiple: false,required: false
	}

	section("Flash Settings\n\nNOTE, the combined on, off times multiplied by the flashes must not exceed 10 seconds!") {
		input "buttonnblinks", "number", title: "Number of Flashes?", required: true,defaultValue: 5
		input "buttonmson", "number", title: "Milliseconds for the light on?", required: true,defaultValue: 500
		input "buttonmsoff", "number", title: "Milliseconds for the light off?", required: true,defaultValue: 500
	}

	section("Do not run this routine between the hours of") {
       input "buttonfromtime", "time", title:"start", required: true, defaultvalue: "00:00"
       input "buttontotime", "time", title:"end", required: true, defaultvalue: "00:01"
    }

    section(" ") {
    }

	section("When there is motion flash a light or device.") {
		input "motionDetector", "capability.motionSensor", title: "Which motion sensors?", multiple: true,required: false 
	}

	section("Flash this light/device when motion detected.") {
		input "SwitchDevice", "capability.switch", title: "Which Device?", multiple: false,required: false
	}

	section("Flash Settings\n\nNOTE, the combined on, off times multiplied by the flashes must not exceed 10 seconds!") {
		input "nblinks", "number", title: "Number of Flashes?", required: true,defaultValue: 5
		input "mson", "number", title: "Milliseconds for the light on?", required: true,defaultValue: 500
		input "msoff", "number", title: "Milliseconds for the light off?", required: true,defaultValue: 500
	}

	section("Do not run this routine between the hours of") {
       input "fromtime", "time", title:"start", required: true, defaultvalue: "00:00"
       input "totime", "time", title:"end", required: true, defaultvalue: "00:01"
    }

}

def installed() {
	subscribe(motionDetector, "motion.active", motionActive)
	subscribe(Abutton, "button", buttonPress)
    atomicState.running = "false" //We use atomic state because we need realtime saving and loading of the running state//
}

def updated() {
	unsubscribe()
	subscribe(motionDetector, "motion.active", motionActive)
	subscribe(Abutton, "button", buttonPress)
	atomicState.running = "false"
}

def turnOff(int ms) {
    SwitchDevice.off()
    pause(ms)
}

def turnOn(int ms) {
	SwitchDevice.on() 
  	pause(ms)
}

def turnOff2(int ms) {
    ButtonDevice.off()
    pause(ms)
}

def turnOn2(int ms) {
	ButtonDevice.on() 
  	pause(ms)
}

def timeOK(int nblinks, int mson, int msoff) {
   if(  nblinks * (mson + msoff) < 10000 ) {     /** Maximum Ten second time limit */
   		return 1
   }
	   log.error "Blink time exceeds ten seconds, cant run routine"
 	   return 0
}

def resetSwitch(String swval,String swname) {
   if(swval == "on") {
   		SwitchDevice.on()
        log.debug " Resetting $swname back to it`s original setting which was on"     
   } else {
   		if(swval == "off") {
   		log.debug " Resetting $swname back to it`s original setting which was off"
   } else {
   		if(swval == "null") {
   		log.debug " Resetting $swname back to it`s original setting which was null"
   } else {    
   		log.error " reset $swname ERROR: swval=$swval is not on or off" 
   }
  }
 }
}   

def resetButtonDevice(String swval,String swname) {
   if(swval == "on") {
   		ButtonDevice.on()
        log.debug " Resetting $swname back to it`s original setting which was on"     
   } else {
	    if(swval == "off") {
   		log.debug " Resetting $swname back to it`s original setting which was off"
   } else {
  	    if(swval == "null") {
   		log.debug " Resetting $swname back to it`s original setting which was null"
   } else {    
   		log.error " reset $swname ERROR: swval=$swval is not on or off" 
   }
  }
 }
}

def blinkLight(int na, int amson, int amsoff) {

	atomicState.running = "true"  //Save the running flag so that we cant run another instance whilst in the middle of a flash//
	def aswval = SwitchDevice.currentSwitch
    def aswname = SwitchDevice.displayName

	int count=0

   	log.debug "Current State Of $aswname is $aswval."

   	if (timeOK(nblinks,amson,amsoff)) {     /* Maximum allowed SmartThings run time is Ten seconds */
    	while(count<na) {
            turnOn(amson) 
        	turnOff(amsoff)
            count++
    	}
   	}

   	resetSwitch(aswval,aswname)
   	atomicState.running = "false"
}

def blinkLight2(int n, int mson, int msoff) {

	atomicState.running = "true"  //Save the running flag so that we cant run another instance whilst in the middle of a flash//
	def swval = ButtonDevice.currentSwitch
    def swname = ButtonDevice.displayName

	int count=0

   	log.debug "Current State Of $swname is $swval"

   	if (timeOK(nblinks,mson,msoff)) {     /* Maximum allowed SmartThings run time is Ten seconds */
    	while(count<n) {
            turnOn2(mson) 
        	turnOff2(msoff)
            count++
    	}
   	}

   	resetButtonDevice(swval,swname)  
   	atomicState.running = "false"
}

def motionActive(evt) {

    def lastrun1 = atomicState.lastrun1
	def isrunning = atomicState.running

	if (isrunning == "true") {

		log.debug "NEW MOTION DETECTED but were already in the middle of a switching routine, so cant run two instances"
    
    } else {
	    def between = timeOfDayIsBetween(fromtime, totime, new Date(), location.timeZone)
		/* CHECK IF WERE ALLOWED TO FLASH THE LIGHT BASED ON THE TIME RULES*/

		//See if both the TO time and FROM time are the same, if so allow the routine to proceed to next check//
        if (fromtime == totime) {
            between = false //start and end are the same to set between to false to allow it to proceed
        }
        
	  	if (between) {
			/* WE ARE NOT ALLOWED TO FLASH */
			log.debug "Motion Detected by: $evt.device.displayName,   WE CAN'T RUN though as were currently in the NO RUN time."
        } else {
			/* WE ARE ALLOWED TO FLASH */
			log.debug "Motion Detected by: $evt.device.displayName,   We can run this routine as were outside of the no run time."
			blinkLight(nblinks,mson,msoff)  /*ALL OK, LETS RUN THE FLASHING ROUTINE*/
		}
	}
}

def buttonPress(evt) {
 
    log.debug "The status of '${evt.displayName}' has changed to ${evt.value}"

    if (evt.value == "pushed") {

        /* THE BUTTON HAS BEEN PUSHED, SO CARRY ON*/ 
		def lastrun2 = atomicState.lastrun2
		def isrunning = atomicState.running
	
		if (isrunning == "true") {

			log.debug "Cant flash your device as were are already in the middle of a switching routine"

		} else {

			def between = timeOfDayIsBetween(buttonfromtime, buttontotime, new Date(), location.timeZone)
			/* CHECK IF WERE ALLOWED TO FLASH THE LIGHT BASED ON THE TIME RULES*/

			//See if both the TO time and FROM time are the same, if so allow the routine to proceed to next check//
            if (buttonfromtime == buttontotime) {
               between = false //start and end are the same to set between to false to allow it to proceed
            }

			if (between) {
				/* WE ARE NOT ALLOWED TO FLASH */
				log.debug "WE CAN'T RUN this flash routine though as were currently in the NO RUN time."
	        } else {
				/* WE ARE ALLOWED TO FLASH */
				log.debug "We can run this flash routine as were outside of the no run time."
				blinkLight2(buttonnblinks,buttonmson,buttonmsoff)  /*ALL OK, LETS RUN THE FLASHING ROUTINE*/
			}
		}

	}

}