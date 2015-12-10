/**
 *  GE Link Bulb
 *
 *  Copyright 2014 SmartThings
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
 *  Thanks to Chad Monroe @cmonroe and Patrick Stuart @pstuart, and others
 *
 ******************************************************************************
 *                                Changes
 ******************************************************************************
 *
 *  Change 1:	2014-10-10 (wackford)
 *				Added setLevel event so subscriptions to the event will work
 *  Change 2:	2014-12-10 (jscgs350 using Sticks18's code and effort!)
 *				Modified parse section to properly identify bulb status in the app when manually turned on by a physical switch
 *  Change 3:	2014-12-12 (jscgs350, Sticks18's)
 *				Modified to ensure dimming was smoother, and added fix for dimming below 7
 *	Change 4:	2014-12-14 Part 1 (Sticks18)
 *				Modified to ignore unnecessary level change responses to prevent level skips
 *	Change 5:	2014-12-14 Part 2 (Sticks18, jscgs350)
 *				Modified to clean up trace&debug logging, added new code from @sticks18 for parsing "on/off" to determine if the bulb is manually turned on and immediately update the app
 *	Change 6:	2015-01-02	(Sticks18)
 *				Modified to allow dim rate in Preferences. Added ability to dim during On/Off commands and included this option in Preferences. Defaults are "Normal" and no dim for On/Off.
 *	Change 7:	2015-01-09	(tslagle13)
 *				dimOnOff is was boolean, and switched to enum. Properly update "rampOn" and "rampOff" when refreshed or a polled (dim transition for On/Off commands)
 *	Change 8:	2015-03-06	(Juan Risso)
 *				Slider range from 0..100
 *	Change 9:	2015-03-06	(Juan Risso)
 *				Setlevel -> value to integer (to prevent smartapp calling this function from not working).
 *
 */

metadata {
	definition (name: "GE Link Bulb", namespace: "smartthings", author: "SmartThings") {

    	capability "Actuator"
        capability "Configuration"
        capability "Refresh"
		capability "Sensor"
        capability "Switch"
		capability "Switch Level"
        capability "Polling"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE_Appliances", model: "ZLL Light", deviceJoinName: "GE Link Bulb"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState:"turningOff"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState:"turningOn"
 			state "turningOn", label:'${name}', action: "switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action: "switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}

		main(["switch"])
		details(["switch", "level", "levelSliderControl", "refresh"])
	}

	    preferences {

        	input("dimRate", "enum", title: "Dim Rate", options: ["Instant", "Normal", "Slow", "Very Slow"], defaultValue: "Normal", required: false, displayDuringSetup: true)
            input("dimOnOff", "enum", title: "Dim transition for On/Off commands?", options: ["Yes", "No"], defaultValue: "No", required: false, displayDuringSetup: true)

    }
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.trace description

    if (description?.startsWith("on/off:")) {
		log.debug "The bulb was sent a command to do something just now..."
		if (description[-1] == "1") {
        	def result = createEvent(name: "switch", value: "on")
            log.debug "On command was sent maybe from manually turning on? : Parse returned ${result?.descriptionText}"
            return result
        } else if (description[-1] == "0") {
        	def result = createEvent(name: "switch", value: "off")
            log.debug "Off command was sent : Parse returned ${result?.descriptionText}"
            return result
        }
    }

    def msg = zigbee.parse(description)

	if (description?.startsWith("catchall:")) {
		// log.trace msg
		// log.trace "data: $msg.data"

        def x = description[-4..-1]
        // log.debug x

        switch (x)
        {

        	case "0000":

            	def result = createEvent(name: "switch", value: "off")
            	log.debug "${result?.descriptionText}"
           		return result
                break

            case "1000":

            	def result = createEvent(name: "switch", value: "off")
            	log.debug "${result?.descriptionText}"
           		return result
                break

            case "0100":

            	def result = createEvent(name: "switch", value: "on")
            	log.debug "${result?.descriptionText}"
           		return result
                break

            case "1001":

            	def result = createEvent(name: "switch", value: "on")
            	log.debug "${result?.descriptionText}"
           		return result
                break
        }
    }

    if (description?.startsWith("read attr")) {

        // log.trace description[27..28]
        // log.trace description[-2..-1]

    	if (description[27..28] == "0A") {

        	// log.debug description[-2..-1]
        	def i = Math.round(convertHexToInt(description[-2..-1]) / 256 * 100 )
			sendEvent( name: "level", value: i )
        	sendEvent( name: "switch.setLevel", value: i) //added to help subscribers

    	}

    	else {

    		if (description[-2..-1] == "00" && state.trigger == "setLevel") {
        		// log.debug description[-2..-1]
        		def i = Math.round(convertHexToInt(description[-2..-1]) / 256 * 100 )
				sendEvent( name: "level", value: i )
        		sendEvent( name: "switch.setLevel", value: i) //added to help subscribers
        	}

        	if (description[-2..-1] == state.lvl) {
        		// log.debug description[-2..-1]
        		def i = Math.round(convertHexToInt(description[-2..-1]) / 256 * 100 )
				sendEvent( name: "level", value: i )
        		sendEvent( name: "switch.setLevel", value: i) //added to help subscribers
        	}

    	}
    }

}

def poll() {

    [
	"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} 1 8 0", "delay 500",
    "st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state?.dOnOff ?: '0000'}}"
    ]

}

def updated() {

	state.dOnOff = "0000"

	if (dimRate) {

		switch (dimRate)
        	{

        		case "Instant":

            		state.rate = "0000"
                	if (dimOnOff) { state.dOnOff = "0000"}
                    break

            	case "Normal":

            		state.rate = "1500"
                    if (dimOnOff) { state.dOnOff = "0015"}
                	break

            	case "Slow":

            		state.rate = "2500"
                    if (dimOnOff) { state.dOnOff = "0025"}
               		break

            	case "Very Slow":

            		state.rate = "3500"
                    if (dimOnOff) { state.dOnOff = "0035"}
                	break

        	}

    }

    else {

    	state.rate = "1500"
        state.dOnOff = "0000"

    }

        if (dimOnOff == "Yes"){
			switch (dimOnOff){
        		case "InstantOnOff":

            		state.rate = "0000"
                	if (state.rate == "0000") { state.dOnOff = "0000"}
                    break

            	case "NormalOnOff":

            		state.rate = "1500"
                    if (state.rate == "1500") { state.dOnOff = "0015"}
                	break

            	case "SlowOnOff":

            		state.rate = "2500"
                    if (state.rate == "2500") { state.dOnOff = "0025"}
               		break

            	case "Very SlowOnOff":

            		state.rate = "3500"
                    if (state.rate == "3500") { state.dOnOff = "0035"}
                	break

        	}

    }
    else{
    	state.dOnOff = "0000"
    }

    "st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state.dOnOff}}"


}

def on() {
	state.lvl = "00"
    state.trigger = "on/off"

    // log.debug "on()"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	state.lvl = "00"
    state.trigger = "on/off"

    // log.debug "off()"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def refresh() {

    [
	"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} 1 8 0", "delay 500",
    "st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state?.dOnOff ?: '0000'}}"
    ]
    poll()

}

def setLevel(value) {

    def cmds = []
	value = value as Integer
	if (value == 0) {
		sendEvent(name: "switch", value: "off")
		cmds << "st cmd 0x${device.deviceNetworkId} 1 8 0 {0000 ${state.rate}}"
	}
	else if (device.latestValue("switch") == "off") {
		sendEvent(name: "switch", value: "on")
	}

    sendEvent(name: "level", value: value)
    value = (value * 255 / 100)
    def level = hex(value);

    state.trigger = "setLevel"
    state.lvl = "${level}"

    if (dimRate && (state?.rate != null)) {
    	cmds << "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} ${state.rate}}"
    }
    else {
    	cmds << "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} 1500}"
    }

    log.debug cmds
    cmds
}

def configure() {

	log.debug "Configuring Reporting and Bindings."
	def configCmds = [

        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",

        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 500",
	]
    return configCmds + refresh() // send refresh cmds as part of config
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}