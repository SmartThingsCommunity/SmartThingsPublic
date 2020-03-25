/**
 *  HVAC Auto Off
 *
 *  Author: dianoga7@3dgo.net
 *  Date: 2013-07-21
 */

// Automatically generated. Make future change here.
definition(
    name: "Thermostat Auto Off",
    namespace: "dianoga",
    author: "dianoga7@3dgo.net",
    description: "Automatically turn off thermostat when windows/doors open. Turn it back on when everything is closed up.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Control") {
		input("thermostat", "capability.thermostat", title: "Thermostat")
	}
    
    section("Open/Close") {
    	input("sensors", "capability.contactSensor", title: "Sensors", multiple: true)
        input("delay", "number", title: "Delay (seconds)")
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
	state.changed = false
	subscribe(sensors, 'contact', "sensorChange")
}

def sensorChange(evt) {
	log.debug "Desc: $evt.value , $state"
    if(evt.value == 'open' && !state.changed) {
    	unschedule()
        runIn(delay, 'turnOff')
    } else if(evt.value == 'closed' && state.changed) {
    	// All closed?
        def isOpen = false
        for(sensor in sensors) {
        	if(sensor.id != evt.deviceId && sensor.currentValue('contact') == 'open') {
        		isOpen = true
            }
        }
        
        if(!isOpen) {
        	unschedule()
        	runIn(delay, 'restore')
        }
    }
}

def turnOff() {
	log.debug "Turning off thermostat due to contact open"
	state.thermostatMode = thermostat.currentValue("thermostatMode")
	thermostat.off()
    state.changed = true
    log.debug "State: $state"
}

def restore() {
    log.debug "Setting thermostat to $state.thermostatMode"
    thermostat.setThermostatMode(state.thermostatMode)
    state.changed = false
}