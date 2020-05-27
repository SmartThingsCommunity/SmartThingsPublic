/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Cooper RF9500", namespace: "smartthings", author: "juano23@gmail.com", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Switch"
		capability "Switch Level"
		capability "Button"
		capability "Actuator"

		//fingerprint deviceId: "0x1200", inClusters: "0x77 0x86 0x75 0x73 0x85 0x72 0xEF", outClusters: "0x26"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#00a0dc"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}
		main "switch"
		details(["switch", "refresh", "level", "levelSliderControl"])
	}
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x26: 1, 0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	log.debug("Parsed '$description' to $results")
	return results
}

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}

def levelup() {
	def curlevel = device.currentValue('level') as Integer
	if (curlevel <= 90)
    	setLevel(curlevel + 10);
}

def leveldown() {
	def curlevel = device.currentValue('level') as Integer
	if (curlevel >= 10)
    	setLevel(curlevel - 10)
}

def setLevel(value, rate = null) {
	log.trace "setLevel($value)"
	sendEvent(name: "level", value: value)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

    results += configurationCmds().collect{ response(it) }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())

	return results
}

// A zwave command for a button press was received convert to button number
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd) {
	[ descriptionText: "startlevel $cmd"]
	log.info "startlevel $cmd"
	if (cmd.upDown == true) {
		Integer buttonid = 2
        leveldown()
		checkbuttonEvent(buttonid)
    } else if (cmd.upDown == false) {
		Integer buttonid = 3
        levelup()
		checkbuttonEvent(buttonid)
	}
}

// The controller likes to repeat the command... ignore repeats
def checkbuttonEvent(buttonid){

	if (state.lastScene == buttonid && (state.repeatCount < 4) && (now() - state.repeatStart < 2000)) {
    	log.debug "Button ${buttonid} repeat ${state.repeatCount}x ${now()}"
        state.repeatCount = state.repeatCount + 1
        createEvent([:])
    }
    else {
    	// If the button was really pressed, store the new scene and handle the button press
        state.lastScene = buttonid
        state.lastLevel = 0
        state.repeatCount = 0
        state.repeatStart = now()

        buttonEvent(buttonid)
    }
}

// Handle a button being pressed
def buttonEvent(button) {
	button = button as Integer
    log.trace "Button $button pressed"
    def result = []
	if (button == 1) {
    	def mystate = device.currentValue('switch');
        if (mystate == "on")
            off()
        else
            on()
    }
    updateState("currentButton", "$button")
        // update the device state, recording the button press
        result << createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
    result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicGet cmd) {
    [ descriptionText: "$cmd"]
    if(1){
		Integer buttonid = 1
		checkbuttonEvent(buttonid)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	[ descriptionText: "$cmd"]
    if(1){
		Integer buttonid = 1
        log.info "button $buttonid pressed"
		checkbuttonEvent(buttonid)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
        createEvent([:])
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[ descriptionText: "$cmd"]
}

// Update State
def updateState(String name, String value) {
	state[name] = value
	device.updateDataValue(name, value)
}


def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: 3)
}
