/**
 *  Smart Im Home
 *
 *  Copyright 2016 Fernando Hernandez
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
    name: "Smart Im Home",
    namespace: "Herna202",
    author: "Fernando Hernandez",
    description: "Only turn on the light after sunset when you are the first to come home",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
}


preferences 
{
	section("When I arrive and leave...")
    {
		input "presence1", "capability.presenceSensor", title: "Who?",required: true, multiple: true
	}
	section("Turn on/off a light...")
    {
		input "switch1", "capability.switchLevel",required: true, multiple: true
	}
    section("Light Level") 
    {
    	input "LightLevel", "number", required: true, title: "How Bright?"
    }
    section("Change to this mode when you leave (defaults to Away)") 
    {
        input "leaveMode", "mode", required: false ,title: "Mode?"
    }
    section("Change to this mode when you return (defaults to Home)") 
    {
        input "returnMode", "mode", required: false, title: "Mode?"
    }
    section("False alarm threshold (defaults to 2 min)") 
    {
        input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
    }
    section( "Notifications" ) 
    {
        input("recipients", "contact", title: "Send notifications to", required: false) 
        {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone", "phone", title: "Send a Text Message?", required: false
        }
    }
}

def installed()
{
    state.howManyPeopleAreHome = isAnyoneHome()
	subscribe(presence1, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

private findFalseAlarmThreshold() 
{
    (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 2
}

private findReturnMode() 
{
    (returnMode != null && returnMode != "") ? returnMode : "Home"
}

private findLeaveMode() 
{
    (leaveMode != null && leaveMode != "") ? leaveMode : "Away"
}

private isAnyoneHome()
{
    def result = 0
    for (person in presence1) 
    {
        if (person.currentPresence == "present") 
        {
            result++
        }
    }
    return result
}

def checkIfEveryoneIsActuallyGone() {
    if (isAnyoneHome() == 0) {
        def threshold = 1000 * 60 * falseAlarmThreshold - 1000
        def awayLongEnough = presence1.findAll { person ->
            def presenceState = presence1.currentState("presence")
            def elapsed = now() - presenceState.rawDateCreated.time
            elapsed >= threshold
        }
        log.debug "Found ${awayLongEnough.size()} out of ${presence1.size()} person(s) who were away long enough"
        if (awayLongEnough.size() == presence1.size()) {
            //def message = "${app.label} changed your mode to '${newMode}' because everyone left home"
            def message = "SmartThings changed your mode to '${findLeaveMode()}' because everyone left home"
            log.info message
            send(message)
            switch1.setLevel(0)
            state.howManyPeopleAreHome = 0
            log.warn "Everyone's away."
            changeMode(findLeaveMode())
            runIn(30, "VerifyAway", [overwrite: false])
        } else {
            log.debug "not everyone has been away long enough; doing nothing"
        }
    } else {
        log.debug "not everyone is away; doing nothing"
    }
}

private send(msg) {
    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }

    if ( phone ) {
        log.debug( "sending text message" )
        sendSms( phone, msg )
    }

    log.debug msg
}

def changeMode(newMode) 
{
    log.warn "changeMode, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"

    if (location.mode != newMode) {
        if (location.modes?.find{it.name == newMode}) {
            setLocationMode(newMode)
            log.warn "changeMode, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
        }  else {
            log.warn "Tried to change to undefined mode '${newMode}'"
        }
    }
}

def VerifyHome() 
{
	log.warn "VerifyHome, location.mode = $location.mode"
}

def VerifyAway() 
{
	log.warn "VerifyAway, location.mode = $location.mode"
}

def presenceHandler(evt)
{
	def now = new Date()
	def sunTime = getSunriseAndSunset()
    def tempHowManyPoepleAreHome = isAnyoneHome()
    
	log.debug "nowTime: $now"
	log.debug "riseTime: $sunTime.sunrise"
	log.debug "setTime: $sunTime.sunset"
	log.debug "presenceHandler $evt.name: $evt.value"
	log.debug "The current mode ID is: ${location.currentMode}"

    if(tempHowManyPoepleAreHome == 0)
    {
        log.info "starting debounce sequence"
        runIn(findFalseAlarmThreshold() * 60, "checkIfEveryoneIsActuallyGone", [overwrite: false])
    }
	else if(state.howManyPeopleAreHome)
    {
    	log.warn "Someone is already Home, no need to take action"
    }
    else
    {
        log.warn "First person to arrive, need to take action"
    	state.howManyPeopleAreHome = tempHowManyPoepleAreHome
        log.debug "How many people are home after update $state.howManyPeopleAreHome"
        def message = "SmartThings changed your mode to '${findReturnMode()}' because everyone came home"
        log.info message
        send(message)
        changeMode(findReturnMode())
        runIn(30, "VerifyHome", [overwrite: false])
        if(state.howManyPeopleAreHome && (now > sunTime.sunset)) 
        {
            switch1.setLevel(LightLevel)
            log.warn "Welcome home at night!"
        }
        else if(state.howManyPeopleAreHome && (now < sunTime.sunset)) 
        {
            log.warn "Welcome home at daytime!"
        }
    }
}


