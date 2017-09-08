/**
 *  Delayed alarm
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
    name: "Delayed alarm",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Delayed chime then alarm",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@3x.png")

preferences {
	section("App") {
    	input "masterAlarm", "capability.alarm",multiple:false,title:"Master alarm switch"
		input "chimes", "capability.switchLevel", multiple:true, title:"Chime dimmer switches"
        input "sirensLevel1", "capability.alarm", multiple:true, title:"Siren 1 alarms", required:false
        input "sirensLevel2", "capability.alarm", multiple:true, title:"Siren 2 alarms", required:false
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
        if (sirensLevel1 != null)
        {
        	logTrace('sirensLevel1 off')
        	sirensLevel1.off()
        }
        if (sirensLevel2 != null)
        {
        	logTrace('sirensLevel2 off')
        	sirensLevel2.off()
        }
        logTrace('chimes off')
        chimes.off()

		state.alarmStarted = ''
    }
}

def startChimes(){
	if (state.alarmStarted == '')
    {
        logTrace("Starting chimes $chimeNumber")
        state.alarmStarted = new Date()
        chimes.setLevel(chimeNumber*10)
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
        if (sirensLevel1 != null)
	        sirensLevel1.both()
        runIn(delaySiren2, startSiren2)
    }
}

def startSiren2(){
	logTrace('startSiren2')
    logTrace("Alarm: ${masterAlarm.alarm}")
    if (state.alarmStarted != '')
    {
        logTrace("Starting siren 2")
        if (sirensLevel2 != null)
	        sirensLevel2.both()
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