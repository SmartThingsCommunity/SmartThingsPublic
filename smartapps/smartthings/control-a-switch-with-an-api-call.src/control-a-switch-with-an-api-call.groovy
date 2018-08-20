/**
 *  Control a Switch with an API call
 *
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
definition(
    name: "Control a Switch with an API call",
    namespace: "smartthings",
    author: "SmartThings",
    description: "V2 of 'RESTful Switch' example. Trying to make OAuth work properly.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

preferences {
    section("which switches?") {
        input "theSwitches", "capability.switch", multiple: true
    }
}

mappings {
  path("/switches") {
    // GET requests to /switches endpoint go to listSwitches.
    // PUT requests go to updateSwitches
    action: [
      GET: "getSwitches",
      PUT: "updateSwitches"
    ]
  }
  
  // GET requests to endpoint /switches/<id> go to getSwitch
  // PUT requests to endpoint /switches/<id> go to updateSwitch
  path("/switches/:id") {
    action: [
        GET: "getSwitch",
        PUT: "updateSwitch"
    ]
  }
}

// return a map in the form of [switchName, switchStatus]
// the returned value will be converted to JSON by the platform
def getSwitches() {
    def status = [:]
    theSwitches.each {theSwitch ->
        log.trace "will populate status map"
        log.trace "theSwitch id: ${theSwitch.id}"
        status.put(theSwitch.displayName, theSwitch.currentSwitch)
    }
    
    log.debug "listSwitches returning: $status"
    return status
}

def getSwitch() {
    def theSwitch = theSwitches.find{it.id == params.id}
    [theSwitch.displayName, theSwitch.currentSwitch]
}

// execute the command specified in the request
// returns a 400 error if a non-supported command
// is specified (only on, off, or toggle supported)
// assumes request body with JSON in format {"command" : "<value>"}
def updateSwitches() {
    log.trace "updateSwitches: request: $request"
    log.trace "updateSwitches: params: $params"
    
    theSwitches.each {
        doCommand(it, request.JSON.command)
    }
}

// execute the command specified in the request
// return a 400 error if a non-supported command 
// is specified (only on, off, or toggle supported)
// assumes request body with JSON in format {"command" : "<value>"}
def updateSwitch() {
    log.trace "updateSwitch: look for swithc with id ${params.id}"
    def theSwitch = theSwitches.find{it.id == params.id}
    doCommand(theSwitch, request.JSON.command)
}

def doCommand(theSwitch, command) {
    if (command == "toggle") {
        if (theSwitch.currentSwitch == "on") {
            log.debug "will try and turn switch ${theSwitch.displayName} on"
            theSwitch.off()
        } else {
            log.debug "will try and turn switch ${theSwitch.displayName} off"
            theSwitch.on()
        }
    } else if (command == "on" || command == "off") {
        theSwitch."$command"()
    } else {
        httpError(400, "Unsupported command - only 'toggle', 'off', and 'on' supported")
    }
}

// called when SmartApp is installed
def installed() {
    log.debug "Installed with settings: ${settings}"
}

// called when any preferences are changed in this SmartApp. 
def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
}