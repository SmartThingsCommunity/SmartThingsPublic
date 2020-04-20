/**
 *  Smartboard
 *
 *  Copyright 2017 Shane Pennicott
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
    name: "Smartboard",
    namespace: "pennihome",
    author: "Shane Pennicott",
    description: "Web Service for Smart Board",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: true
  }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/routines") {
    action: [
      GET: "listRoutines"
    ]
  }
  path("/switches/:id/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
  path("/routines/:id") {
    action: [
      PUT: "executeRoutine"
    ]
  }
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

def listSwitches() {
    def resp = []
    switches.each {
      resp << [id:it.id, name: it.displayName, type: it.typeName, value: it.currentValue("switch")]
    }
    return resp
}

def listRoutines() {
    location.getHelloHome().getPhrases().collect {
        [id: it.id, name: it.label]
    }
}

def executeRoutine(){
	log.debug "Called execute Routine"
    
    def sId = params.id;
 	def routine = location.getHelloHome().getPhrases().findAll {r ->
    	return (r.id == sId)
    }
    log.debug "With values id: ${sId}, found: ${routine.size()}";
    
    routine.each{
    	location.helloHome?.execute(it.label);
    }
}

void updateSwitches() {
	log.debug "Called Update Switches"
    // use the built-in request object to get the command parameter
    def sId = params.id;
    def command = params.command;
	log.debug "With values id: ${sId}, command: ${command}";

	def allSwitches = switches.findAll {m ->
        return (m.id == sId);
    };
    
    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    allSwitches.each{
    	log.debug "Switch is id: ${sId}, command: ${command}";
        switch(command) {
            case "on":
                it.on()
                break
            case "off":
                it.off()
                break
            default:
                httpError(400, "$command is not a valid command for all switches specified")
        }
    }
}