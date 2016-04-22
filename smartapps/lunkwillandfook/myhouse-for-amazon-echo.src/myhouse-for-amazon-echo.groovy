/**
 *  myHouse for Amazon Echo
 *
 *  Copyright 2015 Jeremy Huckeba
 *  Version 1.00 1/18/16
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
 *  Ties a Hello, Home phrase to a switch's (virtual or real) on/off state. Perfect for use with IFTTT.
 *  Simple define a switch to be used, then tie the on/off state of the switch to a specific Hello, Home phrases.
 *  Connect the switch to an IFTTT action, and the Hello, Home phrase will fire with the switch state change.
 *
 *
 */
definition(
    name: "myHouse for Amazon Echo",
    namespace: "LunkwillAndFook",
    author: "Jeremy Huckeba",
    description: "Allows deep, custom integration with Amazon Echo.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/myhouseapp/st/my-house-src-st.png",
    iconX2Url: "https://s3.amazonaws.com/myhouseapp/st/my-house-src-st@2x.png",
    iconX3Url: "https://s3.amazonaws.com/myhouseapp/st/my-house-src-st@3x.png")

def getLeavingPhraseTitle() {
  return "I'm leaving..."
}

def getWatchingMoviePhraseTitle() {
  return "I'm watching a movie..."
}

def getFinishedWatchingMoviePhraseTitle() {
  return "I'm finished watching a movie..."
}

def getHavingPartyPhraseTitle() {
  return "I'm having a party..."
}

def getBackPhraseTitle() {
  return "I'm back..."
}

def getCleaningPhraseTitle() {
  return "I'm cleaning..."
}

def getSleepPhraseTitle() {
  return "I'm going to sleep..."
}

def getAwakePhraseTitle() {
  return "I'm awake..."
}

// displays the preferences
preferences(oauthPage: "deviceAuthorization") {
    page(name: "deviceAuthorization", title: "", nextPage: "routinesPage", install: false, uninstall: true) {
        section ("Allow Alexa to control these switches...") {
            input "selectedSwitches", "capability.switch", title: "switches", multiple: true, required: false
        }
        section ("Allow Alexa to control these thermostats...") {
            input "selectedThermostats", "capability.thermostat", title: "thermostats", multiple: true, required: false
        }
        //section ("Allow Alexa to control these colored bulbs...") {
        //    input "selectedColorControls", "capability.colorControl", multiple: true, required: false
        //}
        section ("Allow Alexa to read these contact sensors...") {
            input "selectedContactSensors", "capability.contactSensor", title: "contact sensors", multiple: true, required: false
        }
        section ("Allow Alexa to read these humidity sensors...") {
            input "selectedHumiditySensors", "capability.relativeHumidityMeasurement", title: "humidity sensors", multiple: true, required: false
        }
        section ("Allow Alexa to read these temperature sensors...") {
            input "selectedTemperatureSensors", "capability.temperatureMeasurement", title: "temperature sensors", multiple: true, required: false
        }
        //section ("Allow Alexa to read these water sensors...") {
        //    input "selectedWaterSensors", "capability.waterSensor", multiple: true, required: false
        //}
       section ("Allow Alexa to read these smoke detectors...") {
            input "selectedSmokeDetectors", "capability.smokeDetector", title: "smoke detectors", multiple: true, required: false
        }
       section ("Allow Alexa to read these battery powered devices...") {
            input "selectedBatteries", "capability.battery", title: "battery powered devices", multiple: true, required: false
        }
    }
    page(name: "routinesPage")
    page(name: "phrasesPage")
}

// displays the routines page
def routinesPage() {
	def actions = location.helloHome?.getPhrases()*.label;

    dynamicPage(name: "routinesPage", uninstall: true, install: false, nextPage: "phrasesPage") {
        section {
            input "selectedRoutines", "enum", title: "Allow Alexa to run these routines...", options: actions, required: false, multiple: true
        }
    }
}

// displays the phrases page
def phrasesPage() {
	def actions = selectedRoutines;

    dynamicPage(name: "phrasesPage", uninstall: true, install: true) {
		section("I'm awake...") {
        	input(name: "awakePhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "awakePhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
        section("I'm back...") {
        	input(name: "backPhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "backPhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
		section("I'm cleaning...") {
        	input(name: "cleaningPhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "cleaningPhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
        section("I'm finished watching a movie...") {
        	input(name: "finishedWatchingMoviePhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "finishedWatchingMoviePhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
		section("I'm going to sleep...") {
        	input(name: "sleepPhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "sleepPhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
        section("I'm having a party...") {
        	input(name: "havingPartyPhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "havingPartyPhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }        
		section("I'm leaving...") {
        	input(name: "leavingPhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "leavingPhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false)
        }
        section("I'm watching a movie...") {
        	input(name: "watchingMoviePhraseDelay", type: "number", title: "Wait x minutes...", range: "0..60", required: false)
            input(name: "watchingMoviePhraseRoutine", type: "enum", title: "Run this routine...", options: actions, required: false, multiple: false) 
        }
    }
}

mappings {
  path("/thermostats") {
  	action: [
      GET: "listThermostats"
    ]
  }
  path("/thermostats/:name") {
  	action: [
      GET: "listThermostats"
    ]
  }
  path("/humiditySensors/:name") {
    action: [
      GET: "listHumiditySensors"
    ]
  }
  path("/humiditySensors") {
    action: [
      GET: "listHumiditySensors"
    ]
  }
  path("/temperatureSensors/:name") {
    action: [
      GET: "listTemperatureSensors"
    ]
  }
  path("/temperatureSensors") {
    action: [
      GET: "listTemperatureSensors"
    ]
  }
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switchCommand/:name/:command") {
    action: [
      PUT: "updateSwitch"
    ]
  }
  path("/switchLevel/:name/:command/:level") {
    action: [
      PUT: "updateSwitch"
    ]
  }
  path("/contactSensors/:name") {
    action: [
      GET: "listContactSensors"
    ]
  }
  path("/contactSensors") {
    action: [
      GET: "listContactSensors"
    ]
  }
  path("/lowBatteries/:level") {
    action: [
      GET: "listLowBatteries"
    ]
  }
  path("/lowBatteries") {
    action: [
      GET: "listLowBatteries"
    ]
  }
  path("/routines") {
    action: [
      GET: "listRoutines"
    ]
  }
  path("/routines/:name") {
    action: [
      PUT: "executeRoutine"
    ]
  }
  path("/phrases/:name") {
  	action: [
      PUT: "executePhrase"
    ]
  }
}

// runs when the smartapp is installed
def installed() {
	def immediatelocks = state.immediateLocks ?: []
	log.debug "Installed with settings: ${settings}"
	subscribe(controlSwitch, "switch", "switchHandler")
}

// runs when the smartapp is updated
def updated() {
	def immediatelocks = state.immediateLocks ?: []
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(controlSwitch, "switch", "switchHandler")
}

// returns a list like
// [[name: "thermostat", temperature: "74", heatingSetpoint: "70", coolingSetpoint: "76", thermostatSetpoint: "74", thermostatMode: "range", thermostatFanMode: "auto", thermostatOperatingState: "cooling", scale: "F"]]
def listThermostats() {
	log.debug "listing thermostats: ${selectedThermostats}"
    def resp = []
    if(params.name == null) {
        selectedTemperatureSensors.each {
          resp << [name: it.displayName, temperature: it.currentValue("temperature"), heatingSetpoint: it.currentValue("heatingSetpoint"), coolingSetpoint: it.currentValue("coolingSetpoint"), thermostatSetpoint: it.currentValue("thermostatSetpoint"), thermostatMode: it.currentValue("thermostatMode"), thermostatFanMode: it.currentValue("thermostatFanMode"), thermostatOperatingState: it.currentValue("thermostatOperatingState"), scale: location.temperatureScale]
        }
    } else {
    	selectedTemperatureSensors.each {
        	if(it.displayName.toLowerCase() == params.name.toLowerCase()) {
            	resp << [name: it.displayName, temperature: it.currentValue("temperature"), heatingSetpoint: it.currentValue("heatingSetpoint"), coolingSetpoint: it.currentValue("coolingSetpoint"), thermostatSetpoint: it.currentValue("thermostatSetpoint"), thermostatMode: it.currentValue("thermostatMode"), thermostatFanMode: it.currentValue("thermostatFanMode"), thermostatOperatingState: it.currentValue("thermostatOperatingState"), scale: location.temperatureScale]
            }
        }
    }
    return resp
}

// returns a list like
// [[name: "front door", value: "65"], [name: "smoke detector", value: "30"]]
def listLowBatteries() {
    def level = 20;
	if(params.level != null) {
    	level = new BigDecimal(params.level.replaceAll(",", ""))
    }
    
    log.debug "listing selected devices ${selectedBatteries} with a battery under ${level} percent"
	
    def resp = []
    selectedBatteries.each {
    	def batteryLevel = it.currentValue("battery")
        if(it.currentValue("battery") != null && it.currentValue("battery") < level) {
            resp << [name: it.displayName, value: it.currentValue("battery")]
        }
    }
    return resp
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {
    log.debug "listing switches: ${selectedSwitches}"
    def resp = []
    selectedSwitches.each {
      resp << [name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}

// returns a list like
// [[name: "front door", value: "closed"], [name: "back door", value: "opened"]]
def listContactSensors() {
    log.debug "listing contact sensors: ${selectedContactSensors}"
    def resp = []
    if(params.name == null) {
        selectedContactSensors.each {
          resp << [name: it.displayName, value: it.currentValue("contact")]
        }
    } else {
    	selectedContactSensors.each {
        	if(it.displayName.toLowerCase() == params.name.toLowerCase()) {
            	resp << [name: it.displayName, value: it.currentValue("contact")]
            }
        }
    }
    return resp
}

// returns a list like
// [[name: "front door", value: "74", scale: "F"], [name: "back door", value: "76", scale: "F"]]
def listTemperatureSensors() {
    log.debug "listing temperature sensors: ${selectedTemperatureSensors}"
    def resp = []
    if(params.name == null) {
        selectedTemperatureSensors.each {
          resp << [name: it.displayName, value: it.currentValue("temperature"), scale: location.temperatureScale]
        }
    } else {
    	selectedTemperatureSensors.each {
        	if(it.displayName.toLowerCase() == params.name.toLowerCase()) {
            	resp << [name: it.displayName, value: it.currentValue("temperature"), scale: location.temperatureScale]
            }
        }
    }
    return resp
}

// returns a list like
// [[name: "nest thermostat", value: "48"], [name: "bathroom sensor", value: "76"]]
def listHumiditySensors() {
    log.debug "listing humidity sensors: ${selectedHumiditySensors}"
    def resp = []
    if(params.name == null) {
        selectedHumiditySensors.each {
          resp << [name: it.displayName, value: it.currentValue("humidity")]
        }
    } else {
    	selectedHumiditySensors.each {
        	if(it.displayName.toLowerCase() == params.name.toLowerCase()) {
            	resp << [name: it.displayName, value: it.currentValue("humidity")]
            }
        }
    }
    return resp
}

// returns a list like
// [[name: "goodbye"], [name: "good morning"]]
def listRoutines() {
    log.debug "listRoutines: $selectedRoutines"
    def resp = []
    selectedRoutines.each {
      resp << [name: it]
    }
    return resp
}

// not used. reserved for future use
void updateSwitch() {
    // use the built-in request object to get the command parameter
    def name = params.name
    def command = params.command

    if (command && name) {

        // check that the switch supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        selectedSwitches.each {
            if (it.displayName == name && !it.hasCommand(command)) {
                httpError(501, "$command is not a valid command for the specified switch")
            } else if (it.displayName == name) {
            	it."$command"()
            }
        }
    }
}

// executes the specified routine if the user has granted access to it
def executeRoutine() {
    // use the built-in request object to get the command parameter
    def name = params.name
    def executeName = name
    if (name) {
		def canExecute = false
        // find the routine to execute
        selectedRoutines.each {
        	if(it.toLowerCase() == name.toLowerCase()) {
            	canExecute = true
                executeName = it
            }
        }
        
        if(canExecute) {
            location.helloHome?.execute(executeName)
            httpSuccess
        } else {
            httpError(501, "$name is not a valid routine")
        }
    }
}

// executes the specified phrase if the user has configured it and granted access to the configured routine
def executePhrase() {
    // use the built-in request object to get the command parameter
    def resp = []
    def name = params.name
    def delay = 0
    def routineName = name
    def isValidPhrase = true
    def phraseTitle = null
    def phraseHandler = null
    
    log.debug "Executing phrase ${name}"
    
    switch(name) {
    	case "leaving":
        	delay = leavingPhraseDelay
            routineName = leavingPhraseRoutine
            phraseTitle = getLeavingPhraseTitle()
            phraseHandler = "runScheduledLeavingPhraseHandler"
        	break
        case "watchingMovie":
        	delay = watchingMoviePhraseDelay
            routineName = watchingMoviePhraseRoutine
            phraseTitle = getWatchingMoviePhraseTitle()
            phraseHandler = "runScheduledWatchingMoviePhraseHandler"
        	break
        case "finishedWatchingMovie":
        	delay = finishedWatchingMoviePhraseDelay
            routineName = finishedWatchingMoviePhraseRoutine
            phraseTitle = getFinishedWatchingMoviePhraseTitle()
            phraseHandler = "runScheduledFinishedWatchingMoviePhraseHandler"
        	break
        case "havingParty":
        	delay = havingPartyPhraseDelay
            routineName = havingPartyPhraseRoutine
            phraseTitle = getHavingPartyPhraseTitle()
            phraseHandler = "runScheduledHavingPartyPhraseHandler"
            break
        case "back":
        	delay = backPhraseDelay
            routineName = backPhraseRoutine
            phraseTitle = getBackPhraseTitle()
          	phraseHandler = "runScheduledBackPhraseHandler"
            break
        case "cleaning":
        	delay = cleaningPhraseDelay
            routineName = cleaningPhraseRoutine
            phraseTitle = getCleaningPhraseTitle()
            phraseHandler = "runScheduledCleaningPhraseHandler"
            break
        case "sleep":
        	delay = sleepPhraseDelay
            routineName = sleepPhraseRoutine
            phraseTitle = getSleepPhraseTitle()
            phraseHandler = "runScheduledSleepPhraseHandler"
            break
        case "awake":
        	delay = awakePhraseDelay
            routineName = awakePhraseRoutine
            phraseTitle = getAwakePhraseTitle()
            phraseHandler = "runScheduledAwakePhraseHandler"
            break
        default:
        	isValidPhrase = false
            break
    }
    
    if (routineName != null && isValidPhrase == true) {
		def canExecute = false
        def executeName = null
        
        selectedRoutines.each {
        	if(it.toLowerCase() == routineName.toLowerCase()) {
            	canExecute = true
                executeName = it
        	}
    	}
        
        if(canExecute) {
        	if(delay == null || delay == 0) {
                log.debug "running ${phraseHandler} now"
            	runIn(1, phraseHandler)
            } else {
                log.debug "running ${phraseHandler} in ${delay * 60} minutes"
            	runIn(delay * 60, phraseHandler)
            }
            resp << [routine: executeName, delay: delay]
        } else {
        	resp << [error: "NotConfigured", phrase: phraseTitle]
        }
    } else {
    	if(isValidPhrase == false) {
            log.debug "phrase is invalid"
        	resp << [error: "InvalidPhrase", name: name]
        } else {
            log.debug "routine is not configured"
            resp << [error: "NotConfigured", phrase: phraseTitle]
        }
    }
    
    return resp
}

// runs the scheduled routine when the phrase is executed
def runScheduledLeavingPhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == leavingPhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$leavingPhraseRoutine"
    	location.helloHome?.execute(leavingPhraseRoutine)
    }
}

// runs the watching movie phrase routine when the phrase is executed
def runScheduledWatchingMoviePhraseHandler() {
    log.debug "running scheduled watching movie handler"
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == watchingMoviePhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$watchingMoviePhraseRoutine"
    	location.helloHome?.execute(watchingMoviePhraseRoutine)
    }
}

// runs the finished watching movie phrase routine when the phrase is executed
def runScheduledFinishedWatchingMoviePhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == finishedWatchingMoviePhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$finishedWatchingMoviePhraseRoutine"
    	location.helloHome?.execute(finishedWatchingMoviePhraseRoutine)
    }
}

// runs the having party phrase routine when the phrase is executed
def runScheduledHavingPartyPhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == havingPartyPhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$havingPartyPhraseRoutine"
    	location.helloHome?.execute(havingPartyPhraseRoutine)
    }
}

// runs the back phrase routine when the phrase is executed
def runScheduledBackPhraseHandler() {

    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == backPhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	location.helloHome?.execute(backPhraseRoutine)
    }
}

// runs the cleaning phrase routine when the phrase is executed
def runScheduledCleaningPhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == cleaningPhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$cleaningPhraseRoutine"
    	location.helloHome?.execute(cleaningPhraseRoutine)
    }
}

// runs the sleep phrase routine when the phrase is executed
def runScheduledSleepPhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == sleepPhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$sleepPhraseRoutine"
    	location.helloHome?.execute(sleepPhraseRoutine)
    }
}

// runs the sleep phrase routine when the phrase is executed
def runScheduledAwakePhraseHandler() {
    def canExecute = false
    
    selectedRoutines.each {
        if(it.toLowerCase() == awakePhraseRoutine.toLowerCase()) {
            canExecute = true
        }
    }
    
    if(canExecute) {
    	log.trace "$awakePhraseRoutine"
    	location.helloHome?.execute(awakePhraseRoutine)
    }
}