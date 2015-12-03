/*****************************************************************************
Device:  VirtualBetterDimmer.device.groovy
Author:  twack@wackware.net
Version: 0.1
Date:    2013-11-16
Purpose: To implement horizontal slider and up/down toggle in virtual dimmer
         device. This is great device to use with "Dim With Me" app. This
         functionality can also be found in "BetterDimmer.app.groovy".

Use License: Non-Profit Open Software License version 3.0 (NPOSL-3.0)
             http://opensource.org/licenses/NPOSL-3.0

 ******************************************************************************
 *                                Changes
 ******************************************************************************
 *
 *  Change 1:	2013-11-16 (wackford)
 *				Initial Build
 *
 *  Change 2:	2014-10-10 (twackford)
 *				Rebuilt to add metadata
 *
 *  Change 3:	2014-10-12 (twackford)
 *				Moved preferences section into metadata section
 *
 *  Change 4:	2014-10-22 (twackford)
 *				Fixed on/off override so it will work with other apps/devices
 *
 ******************************************************************************
                
  Other Info:	Special thanks to Danny Kleinman at ST for helping me get the
				state stuff figured out. The Android state filtering had me 
                stumped.

 *****************************************************************************/

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Better Virtual Dimmer", author: "todd@wackford.net") {
		capability "Switch"
        capability "Switch Level"
		capability "Refresh"
		capability "Polling"

		attribute "stepsize", "string"

		command "levelUp"
		command "levelDown"
		command "getLevel"
		command "dimmerOn"
		command "dimmerOff"
	}
    
    preferences {
		input "stepsize", "number", title: "Step Size", description: "Dimmer Step Size", defaultValue: 10, required: false, displayDuringSetup: true
	}

    tiles {
        
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"dimmerOff", icon:"st.switches.light.on", backgroundColor:"#79b821"
            state "off", label:'${name}', action:"dimmerOn", icon:"st.switches.light.off", backgroundColor:"#ffffff"

        }
        
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"switch level.setLevel", unit:"", backgroundColor:"#ffe71e"
        }
        
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}

        standardTile("lUp", "device.switch", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
                        state "up", label:'', action:"levelUp",icon:"st.illuminance.illuminance.bright"
        }
        
        standardTile("lDown", "device.switch", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
                        state "down", label:'', action:"levelDown",icon:"st.illuminance.illuminance.light"
        }

        main(["switch"])
        details(["switch","lUp","lDown","levelSliderControl","level" ,"Preferences"])
    }
}


def initialize() {
    
	if ( !settings.stepsize )
    	state.stepsize = 10
    else
		state.stepsize = settings.stepsize
    
    if (!device.currentValue("level"))
    	setLevel(100)
}

def parse(String description) {}

def dimmerOn() { //made our own, since event was filtered by default on Android
    log.info "on"
    sendEvent(name:"switch",value:"on")
}

def dimmerOff() { //made our own, since event was filtered by default on Android
    log.info "off"
    sendEvent(name:"switch",value:"off")
    
}

def on() {
    log.info "on"
    sendEvent(name:"switch",value:"on")
}

def off() {
    log.info "off"
    sendEvent(name:"switch",value:"off")
}

def setLevel(val){
    log.info "setLevel $val"
    log.info "Step Size: ${state.stepsize}"
    
    // make sure we don't drive switches past allowed values (command will hang device waiting for it to
    // execute. Never commes back)
    if (val < 0){
    	val = 0
    }
    
    if( val > 100){
    	val = 100
    }
    
    if (val == 0){ // I liked that 0 = off
    	sendEvent(name:"level",value:val)
    	dimmerOff()
    }
    else
    {
    	dimmerOn()
    	sendEvent(name:"level",value:val)
    	sendEvent(name:"switch.setLevel",value:val) // had to add this to work if apps subscribed to
                                                    // setLevel event. "Dim With Me" was one.
    }
}

def levelUp(){
	if ( !state.stepsize ) {
    	initialize()
        log.info "initialized on first up"
    } else {
    	state.stepsize = settings.stepsize
    }
    
    def thisStep = state.stepsize as float
    int nextLevel = device.currentValue("level") + thisStep
    setLevel(nextLevel)
    log.info "level up $nextLevel"
}

def levelDown(){
	if ( !state.stepsize ) {
    	initialize()
        log.info "initialized on first down"
    } else {
    	state.stepsize = settings.stepsize
    }
    
    def thisStep = state.stepsize as float
    int nextLevel = device.currentValue("level") - thisStep
    setLevel(nextLevel)
    log.info "level down $nextLevel"
}

def setLevel(val, dur){ //not called, but leave here for hue and other controllables
  log.info "setLevel $val, $dur"
  sendEvent(name:"setLevel",value:val)
}

def getLevel(){ //not called, dunno why but I'll leave it for later playing
  log.info device.currentValue("level")
  log.info device.currentValue("switch")
}

def poll() {
    log.info "poll"
}

def refresh() {
    log.info "refresh"
}