/**
 *  Vinli Home Beta
 *
 *  Copyright 2015 Daniel
 *
 */
definition(
    name: "Vinli Home Connect",
    namespace: "com.vinli.smartthings",
    author: "Daniel",
    description: "Allows Vinli users to connect their car to SmartThings",
    category: "SmartThings Labs",
    iconUrl: "https://d3azp77rte0gip.cloudfront.net/smartapps/baeb2e5d-ebd0-49fe-a4ec-e92417ae20bb/images/vinli_oauth_60.png",
    iconX2Url: "https://d3azp77rte0gip.cloudfront.net/smartapps/baeb2e5d-ebd0-49fe-a4ec-e92417ae20bb/images/vinli_oauth_120.png",
    iconX3Url: "https://d3azp77rte0gip.cloudfront.net/smartapps/baeb2e5d-ebd0-49fe-a4ec-e92417ae20bb/images/vinli_oauth_120.png",
    oauth: true)
  
preferences {
  section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: true
    input "locks", "capability.lock", multiple: true, required: true
  }
}

mappings {
  
  path("/devices") {
    action: [
      GET: "listAllDevices"
    ]
  }
	
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
  path("/switches/:id/:command") {
    action: [
      PUT: "updateSwitch"
    ]
  }
   path("/locks/:command") {
    action: [
      PUT: "updateLocks"
    ]
  }
  path("/locks/:id/:command") {
    action: [
      PUT: "updateLock"
    ]
  }
  
   path("/devices/:id/:command") {
    action: [
      PUT: "commandDevice"
    ]
  }
}

// returns a list of all devices
def listAllDevices() {
   	def resp = []
   	switches.each {
      resp << [name: it.name, label: it.label, value: it.currentValue("switch"), type: "switch", id: it.id, hub: it.hub.name]
    }
    
    locks.each {
      resp << [name: it.name, label: it.label, value: it.currentValue("lock"), type: "lock", id: it.id, hub: it.hub.name]
    }
    return resp
}
 
// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {
    def resp = []
    switches.each {
      resp << [name: it.displayName, value: it.currentValue("switch"), type: "switch", id: it.id]
    }
    return resp
}

void updateLocks() {
    // use the built-in request object to get the command parameter
    def command = params.command

    if (command) {

        // check that the switch supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        locks.each {
            if (!it.hasCommand(command)) {
                httpError(501, "$command is not a valid command for all switches specified")
            } 
        }
        
        // all switches have the comand
        // execute the command on all switches
        // (note we can do this on the array - the command will be invoked on every element
        locks."$command"()
    }
}

void updateLock() {
    def command = params.command
    
    locks.each {
      	if (!it.hasCommand(command)) {
            httpError(400, "$command is not a valid command for all lock specified")
        }
        
        if (it.id == params.id) {
            it."$command"()
        }
    }
}

void updateSwitch() {
    def command = params.command
    
    switches.each {
      	if (!it.hasCommand(command)) {
            httpError(400, "$command is not a valid command for all switches specified")
        }
        
        if (it.id == params.id) {
            it."$command"()
        }
    }
}

void commandDevice() {
    def command = params.command
    def devices = []
    
    switches.each {
    	devices << it
    }
    
    locks.each {
    	devices << it
    }
    
    devices.each {
        if (it.id == params.id) {
            if (!it.hasCommand(command)) {
                httpError(400, "$command is not a valid command for specified device")
            }
            it."$command"()
        }
    }
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command

    if (command) {

        // check that the switch supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        switches.each {
            if (!it.hasCommand(command)) {
                httpError(400, "$command is not a valid command for all switches specified")
            } 
        }
        
        // all switches have the comand
        // execute the command on all switches
        // (note we can do this on the array - the command will be invoked on every element
        switches."$command"()
    }
}

 def installed() {
	log.debug "Installed with settings: ${settings}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
}
