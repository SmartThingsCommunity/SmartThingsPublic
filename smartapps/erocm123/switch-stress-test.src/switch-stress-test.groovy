/**
 *  Copyright 2018 Eric Maycock
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
 *  Switch Stress Test
 *
 *  Author: Eric Maycock
 */
definition(
    name: "Switch Stress Test",
    namespace: "erocm123",
    author: "Eric Maycock",
    description: "Stress test on, off, and config for any number of switches.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Run stress test on these switches") {
		input "switches", "capability.switch", multiple: true, required: true
	}
    section("Frequency") {
        input "checkFrequency", "enum", title: "Run at this interval", required: true, options: [
                0: "1 Minute",
                1: "5 Minutes",
                2: "10 Minutes",
                3: "15 Minutes",
                4: "30 Minutes",
                5: "1 Hour",
                6: "3 Hours",
                99:"Off"
            ]
    }
    section("Status") {
        paragraph "Switches turned on ${atomicState.on? atomicState.on : 0} times\nSwitches turned off ${atomicState.off? atomicState.off : 0} times\nSwitches configured ${atomicState.config? atomicState.config : 0} times"
	}
    section("Additional Options") {
		input "reset", "bool", title: "Reset SmartApp counters", required: false
	}
    
}

def installed()
{
    setFrequency()
}

def updated()
{
    if(reset) {
        log.debug "Resetting SmartApp Counters"
        atomicState.config = 0
        atomicState.off = 0
        atomicState.on = 0
        app.updateSetting("reset", false)
    }
	unsubscribe()
    setFrequency()
}

def setFrequency(){
    log.debug "Setting frequency" 
    switch (settings.checkFrequency as Integer) {
            case 0:
                runEvery1Minute(stressCommand)
                break
            case 1:
                runEvery5Minutes(stressCommand)
                break
            case 2:
                runEvery10Minutes(stressCommand)
                break
            case 3:
                runEvery15Minutes(stressCommand)
                break
            case 4:
                runEvery30Minutes(stressCommand)
                break
            case 5:
                runEvery1Hour(stressCommand)
                break
            case 6:
                runEvery3Hours(stressCommand)
                break
            case 99:
                log.debug "Stress test disabled. Not setting check frequency"
                unschedule()
                break
            default:
                log.debug "No regular check frequency chosen. Default to every 15 minutes"
                runEvery15Minutes(stressCommand)
                break
        }
}

def switchHandler(evt) {
	log.info evt.value
}

def stressCommand() {
    Random rand = new Random()
	int max = 2
	def random_number = rand.nextInt(max+1)
    switch(random_number) {
        case 0:
        log.debug "Running configure() on ${switches}"
        atomicState.config = (atomicState.config? atomicState.config : 0) + 1
        switches.each { thisSwitch ->
            if("${thisSwitch.capabilities}".indexOf("Configuration") >= 0) {
                thisSwitch.configure()
            }
        }
        break;
        case 1:
        log.debug "Running off() on ${switches}"
        //toggle(switches)
        atomicState.off = (atomicState.off? atomicState.off : 0) + 1
		switches.off()
        break;
        case 2:
        log.debug "Running on() on ${switches}"
        //toggle(switches)
        atomicState.on = (atomicState.on? atomicState.on : 0) + 1
		switches.on()
        break;
    }
    log.debug "Switches turned on ${atomicState.on? atomicState.on : 0} times"
    log.debug "Switches turned off ${atomicState.off? atomicState.off : 0} times"
    log.debug "Switches configured ${atomicState.config? atomicState.config : 0} times"
}

def toggle(devices) {
	log.debug "toggle: $devices = ${devices*.currentValue('switch')}"

	if (devices*.currentValue('switch').contains('on')) {
        atomicState.off = (atomicState.off? atomicState.off : 0) + 1
		devices.off()
	}
	else if (devices*.currentValue('switch').contains('off')) {
        atomicState.on = (atomicState.on? atomicState.on : 0) + 1
		devices.on()
	}
	else {
		devices.on()
	}
}
