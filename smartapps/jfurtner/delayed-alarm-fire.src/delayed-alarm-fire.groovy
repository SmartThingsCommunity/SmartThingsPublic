/**
 *  Delayed alarm - fire
 *
 *  Copyright 2017 Jamie Furtner
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
    name: "Delayed Alarm - Fire",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Chime then alarm",
    category: "My Apps",
    iconUrl: "https://github.com/jfurtner/SmartThingsPublic_jfurtner/blob/master/icons/App-AudioVisualSmokeAlarm.png?raw=true",
    iconX2Url: "https://github.com/jfurtner/SmartThingsPublic_jfurtner/blob/master/icons/App-AudioVisualSmokeAlarm@2x.png?raw=true")

preferences {
	section("App") {
    	input "masterAlarm", "capability.alarm",multiple:false,title:"Master alarm switch"
		input "chime", "capability.switchLevel", multiple:false, title:"Chime dimmer switch"
        input "siren1", "capability.alarm", multiple:false,title:"Siren 1 alarm", required:false
        input "siren2", "capability.alarm", multiple:false, title:"Siren 2 alarm", required:false
        input "chimeNumber", "number", range:"1..10", title:"Chime number to use"
        input "delaySiren1", "number", range:"1..300",title:"Siren 1 delay", description:"Delay between chime and when first siren triggered in seconds"
        input "delaySiren2", "number", range:"0..1800",title:"Siren 2 delay",description:"Delay between first siren and when second siren triggered in seconds"
        input "alarmDuration", "number", range:"0..300", title:"Duration of all alarms in minutes"
	}
}

def installed() {
	logTrace("Installed with settings: ${settings}")

	initialize()
}

def updated() {
	logTrace("Updated with settings: ${settings}")

	unsubscribe()
	initialize()
}

def initialize() {
	logTrace("Initializing - subscribing to alarm")
	subscribe(masterAlarm, "alarm", alarmHandler)
}

def alarmHandler(evt){
    logTrace("Alarm changed: ${masterAlarm.alarm} ${evt}")
	if (evt.value == 'off')
    {
    	allOff()
    }
    else
    {    
    	startChime()
    }
}

def allOff(){
	logTrace('allOff')
    if (state.alarmStarted != '')
    {
    	logTrace("Disabling all alarms")
        logTrace('unschedule')
        unschedule()
        if (siren1 != null)
        {
        	logTrace('siren1 off')
        	siren1.off()
        }
        if (siren2 != null)
        {
        	logTrace('siren2 off')
        	siren2.off()
        }
        logTrace('chime off')
        chime.off()

		state.alarmStarted = ''
    }
}

def startChime(){
	if (state.alarmStarted == '')
    {
        logTrace("Starting chime $chimeNumber")
        state.alarmStarted = new Date()
        chime.setLevel(chimeNumber*10)
        logTrace("Delay for $delaySiren1 seconds")
        runIn(delaySiren1, startSiren1)
    }
    else
    {
    	logDebug "Alarm currently active ${state.alarmStarted}. Shouldn't be possible."
    }
}

def startSiren1(){
	logTrace("startSiren1")
    logTrace("Alarm: ${masterAlarm.alarm}")
    if (state.alarmStarted != '')
    {
        logTrace("Starting siren 1")
        if (siren1 != null)
	        siren1.both()
        runIn(delaySiren2, startSiren2)
    }
}

def startSiren2(){
	logTrace('startSiren2')
    logTrace("Alarm: ${masterAlarm.alarm}")
    if (state.alarmStarted != '')
    {
        logTrace("Starting siren 2")
        if (siren2 != null)
	        siren2.both()
        runIn(alarmDuration*60, allOff)
	}
}

// debugging helper methods
private getDebugOutputSetting() {
	return (settings?.debugOutput != false)
}


private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	log.trace "$msg"
}