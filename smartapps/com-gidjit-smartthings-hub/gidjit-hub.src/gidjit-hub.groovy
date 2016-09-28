/**
 *  Gidjit Hub
 *
 *  Copyright 2016 Matthew Page
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
    name: "Gidjit Hub",
    namespace: "com.gidjit.smartthings.hub",
    author: "Matthew Page",
    description: "Act as an endpoint so user's of Gidjit can quickly access and control their devices and execute routines. Users can do this quickly as Gidjit filters these actions  based on their environment",
    category: "Convenience",
    iconUrl: "http://www.gidjit.com/appicon.png",
    iconX2Url: "http://www.gidjit.com/appicon@2x.png",
    iconX3Url: "http://www.gidjit.com/appicon@3x.png",
    oauth: [displayName: "Gidjit", displayLink: "www.gidjit.com"])

preferences(oauthPage: "deviceAuthorization") {
    // deviceAuthorization page is simply the devices to authorize
    page(name: "deviceAuthorization", title: "Device Authorization", nextPage: "instructionPage",
         install: false, uninstall: true) {
  		section ("Allow Gidjit to have access, thereby allowing you to quickly control and monitor your following devices. Privacy Policy can be found at http://priv.gidjit.com/privacy.html") {
    		input "switches", "capability.switch", title: "Control/Monitor your switches", multiple: true, required: false
    		input "thermostats", "capability.thermostat", title: "Control/Monitor your thermostats", multiple: true, required: false
    		input "windowShades", "capability.windowShade", title: "Control/Monitor your window shades", multiple: true, required: false //windowShade
        }

    }
    page(name: "instructionPage", title: "Device Discovery", install: true) {
        section() {
            paragraph "Now the process is complete return to the Devices section of the Detected Screen. From there and you can add actions to each of your device panels, including launching SmartThings routines."
        }
    }
}

mappings {
  path("/structureinfo") {
    action: [
      GET: "structureInfo"
    ]
  }
  path("/helloactions") {
    action: [
      GET: "helloActions"
    ]
  }
  path("/helloactions/:label") {
    action: [
      PUT: "executeAction"
    ]
  }

  path("/switch/:id/:command") {
    action: [
      PUT: "updateSwitch"
    ]
  }

  path("/thermostat/:id/:command") {
    action: [
      PUT: "updateThermostat"
    ]
  }

  path("/windowshade/:id/:command") {
    action: [
      PUT: "updateWindowShade"
    ]
  }
  path("/acquiredata/:id") {
    action: [
      GET: "acquiredata"
    ]
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

def initialize() {
	// subscribe to attributes, devices, locations, etc.
}
def helloActions() {
	def actions = location.helloHome?.getPhrases()*.label
    if(!actions) {
    	return []
    }
	return actions
}
def executeAction() {
    def actions = location.helloHome?.getPhrases()*.label
    def a = actions?.find() { it == params.label }
    if (!a) {
        httpError(400, "invalid label $params.label")
        return
    }
    location.helloHome?.execute(params.label)
}
/*  this is the primary function called to query at the structure and its devices */
def structureInfo() { //list all devices
	def list = [:]
    def currId = location.id
    list[currId] = [:]
    list[currId].name = location.name
    list[currId].id = location.id
    list[currId].temperatureScale = location.temperatureScale
    list[currId].devices = [:]
  
    def setValues = {
   		if (params.brief) {
            return [id: it.id, name: it.displayName]
        }
        def newList = [id: it.id, name: it.displayName, suppCapab: it.capabilities.collect {
            "$it.name"
        }, suppAttributes: it.supportedAttributes.collect {
            "$it.name"      
        }, suppCommands: it.supportedCommands.collect {
            "$it.name"      
        }]

        return newList
    }
    switches?.each {
      list[currId].devices[it.id] = setValues(it)
    }
    thermostats?.each {
      list[currId].devices[it.id] = setValues(it)
    }
    windowShades?.each {
      list[currId].devices[it.id] = setValues(it)
    }

    return list

}
/*  This function returns all of the current values of the specified Devices attributes */
def acquiredata() {
	def resp = [:]
    if (!params.id) {
    	httpError(400, "invalid id $params.id")
        return
    }
    def dev = switches.find() { it.id == params.id } ?: windowShades.find() { it.id == params.id } ?:
    	thermostats.find() { it.id == params.id }    
   
    if (!dev) {
    	httpError(400, "invalid id $params.id")
        return    
    }
    def att = dev.supportedAttributes
    att.each {
    	resp[it.name] = dev.currentValue("$it.name")
    }
    return resp
}

void updateSwitch() {
    // use the built-in request object to get the command parameter
    def command = params.command
	def sw = switches.find() { it.id == params.id }
    if (!sw) {
    	httpError(400, "invalid id $params.id")
        return
    }
    switch(command) {
        case "on":
        	if ( sw.currentSwitch != "on" ) {
            	sw.on()
            }
            break
        case "off":
        	if ( sw.currentSwitch != "off" ) {
            	sw.off()
            }
            break
        default:
            httpError(400, "$command is not a valid")
    }
}


void updateThermostat() {
    // use the built-in request object to get the command parameter
    def command = params.command
	def therm = thermostats.find() { it.id == params.id }
    if (!therm || !command) {
    	httpError(400, "invalid id $params.id")
        return
    }
    def passComm = [
        "off",
        "heat",
        "emergencyHeat",
        "cool",
        "fanOn",
        "fanAuto",
        "fanCirculate",
        "auto"

	]
    def passNumParamComm = [
    	"setHeatingSetpoint",
    	"setCoolingSetpoint",   
    ]
    def passStringParamComm = [
        "setThermostatMode",
        "setThermostatFanMode",
	]
    if (command in passComm) {
    	therm."$command"()	
    } else if (command in passNumParamComm && params.p1 && params.p1.isFloat()) {
    	therm."$command"(Float.parseFloat(params.p1))	
    } else if (command in passStringParamComm && params.p1) {
    	therm."$command"(params.p1)	
    } else {
    	httpError(400, "$command is not a valid command")
    }
}

void updateWindowShade() {
    // use the built-in request object to get the command parameter
    def command = params.command
	def ws = windowShades.find() { it.id == params.id }
    if (!ws || !command) {
    	httpError(400, "invalid id $params.id")
        return
    }
    def passComm = [
		"open",
        "close",
        "presetPosition",
	]
    if (command in passComm) {
    	ws."$command"()		
    } else {
    	httpError(400, "$command is not a valid command")
    }
}
// TODO: implement event handlers