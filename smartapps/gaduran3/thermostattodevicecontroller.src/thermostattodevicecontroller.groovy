definition(
  name: "ThermostatToDeviceController",
  namespace: "gaduran3", 
  author: "Geoffrey Duran",
  description: "Fan and Window AC control by Thermostat",
  category: "Climate Control",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/support/honeywell-tcc.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/support/honeywell-tcc@2x.png",
  iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Partner/support/honeywell-tcc@3x.png",
  oauth: true
)

preferences {
  	section("SmartThings Hub") {
    	input name: "hostHub", type: "hub", title: "Select Hub", multiple: false, required: true
  	}
  	section("Choose thermostat") {
    	input name: "myThermostat", type: "capability.thermostat", title: "Select Thermostat", multiple: false, required: true
  	}
    section("Choose Thermostat Control Status Switch (Virtual Device) and Overall Delay") {
        input name: "statusSwitch", type: "capability.switch", title:"Thermostat Control Status Switch", required: true
        input "OnDelay", "number", title:"On Delay (minutes)", required: false, defaultValue: "0"
		input "OffDelay", "number", title:"Off Delay (minutes)", required: false, defaultValue: "0"
	}
  	section("Choose Device 1, modes and delays") {
        input name: "device1", type: "capability.switch", title:"Device 1", required: false
        input "device1OnDelay", "number", title:"Device 1 On Delay (minutes)", required: false, defaultValue: "0"
		input "device1OffDelay", "number", title:"Device 1 Off Delay (minutes)", required: false, defaultValue: "0"
        input "device1Modes", "enum", title:"Device 1 operating modes", multiple: true, options: ["cooling","heating"], required: false
	}
  	section("Choose Device 2, modes and delays") {
    	input name: "device2", type: "capability.switch", title:"Device 2", required: false
        input "device2OnDelay", "number", title:"Device 2 On Delay (minutes)", required: false, defaultValue: "0"
		input "device2OffDelay", "number", title:"Device 2 Off Delay (minutes)", required: false, defaultValue: "0"
        input "device2Modes", "enum", title:"Device 2 operating modes", multiple: true, options: ["cooling","heating"], required: false
  	}
  	section("Choose Device 3, modes and delays") {
        input name: "device3", type: "capability.switch", title:"Device 3", required: false
        input "device3OnDelay", "number", title:"Device 3 On Delay (minutes)", required: false, defaultValue: "0"
		input "device3OffDelay", "number", title:"Device 3 Off Delay (minutes)", required: false, defaultValue: "0"
        input "device3Modes", "enum", title:"Device 3 operating modes", multiple: true, options: ["cooling","heating"], required: false
  	}
  	section("Choose Device 4, modes and delays") {
        input name: "device4", type: "capability.switch", title:"Device 4", required: false
        input "device4OnDelay", "number", title:"Device 4 On Delay (minutes)", required: false, defaultValue: "0"
		input "device4OffDelay", "number", title:"Device 4 Off Delay (minutes)", required: false, defaultValue: "0"
        input "device4Modes", "enum", title:"Device 4 operating modes", multiple: true, options: ["cooling","heating"], required: false
  	}
  	section("Choose Device 5, modes and delays") {
        input name: "device5", type: "capability.switch", title:"Device 5", required: false
        input "device5OnDelay", "number", title:"Device 5 On Delay (minutes)", required: false, defaultValue: "0"
		input "device5OffDelay", "number", title:"Device 5 Off Delay (minutes)", required: false, defaultValue: "0"
        input "device5Modes", "enum", title:"Device 5 operating modes", multiple: true, options: ["cooling","heating"], required: false
  	}
  	section("Choose Device 6, modes and delays") {
        input name: "device6", type: "capability.switch", title:"Device 6", required: false
        input "device6OnDelay", "number", title:"Device On Delay (minutes)", required: false, defaultValue: "0"
		input "device6OffDelay", "number", title:"Device Off Delay (minutes)", required: false, defaultValue: "0"
        input "device6Modes", "enum", title:"Device 6 operating modes", multiple: true, options: ["cooling","heating"], required: false
  	}
	section("Notify me...") {
		input "pushNotification", "bool", title: "Push notification", required: false, defaultValue: "true"
  	}
}

import groovy.time.TimeCategory

def installed() {

  	subscribeToEvents()
}

def subscribeToEvents() {
    subscribe(myThermostat,"thermostatOperatingState", thermostatHandler)
}

def updated() {
	unsubscribe()
  	subscribeToEvents()    
}

def uninstalled() {
}

def thermostatHandler(evt) {
	def EventValue = evt.value
    def EventName = evt.name
    log.info "thermostatHandler Event Value: " + EventValue //  which event fired is here
    log.info "thermostatHandler Event Name: " + EventName  //  name of device firing it here
    def TOS = myThermostat.currentValue("thermostatOperatingState")
	def delay = settings.OffDelay*60
   	if (TOS == "idle") {
		log.info "Turning off in " + delay/60 + " minutes" + "(" + delay + " seconds)"
		runIn(delay, off)
	} else if (TOS == "heating") {
		log.info "Turning on in " + delay/60 + " minutes" + "(" + delay + " seconds)"
		runIn(delay, on)
	} else if (TOS == "cooling") {
		log.info "Turning on in " + delay/60 + " minutes" + "(" + delay + " seconds)"
		runIn(delay, on)
	} else {}   
}

def on() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")  
    if (TOS != "idle") {
        statusSwitch.on()
    	settings.device1Modes.each {
          	log.debug "Device1 Operating State: " + it   
	    	if (TOS == it) {
        		def delayDevice1 = settings.device1OnDelay*60
				log.debug "Turning Device1 on in " + settings.device1OnDelay + " minutes" + "(" + delayDevice1 + " seconds)"
				runIn(delayDevice1, device1On)
            }
       	}
    	settings.device2Modes.each {
          	log.debug "Device2 Operating State: " + it   
	    	if (TOS == it) {
        		def delayDevice2 = settings.device2OnDelay*60
				log.debug "Turning Device2 on in " + settings.device2OnDelay + " minutes" + "(" + delayDevice2 + " seconds)"
				runIn(delayDevice2, device2On)
            }
       	}
    	settings.device3Modes.each {
          	log.debug "Device3 Operating State: " + it   
	    	if (TOS == it) {
        		def delayDevice3 = settings.device3OnDelay*60
				log.debug "Turning Device3 on in " + settings.device3OnDelay + " minutes" + "(" + delayDevice3 + " seconds)"
				runIn(delayDevice3, device3On)
            }
       	}
    	settings.device4Modes.each {
          	log.debug "Device4 Operating State: " + it   
	    	if (TOS == it) {
	        	def delayDevice4 = settings.device1OnDelay*60
				log.debug "Turning Device4 on in " + settings.device4OnDelay + " minutes" + "(" + delayDevice4 + " seconds)"
				runIn(delayDevice1, device4On)
            }
       	}
    	settings.device5Modes.each {
          	log.debug "Device5 Operating State: " + it   
	    	if (TOS == it) {
        		def delayDevice5 = settings.device5OnDelay*60
				log.debug "Turning Device5 on in " + settings.device5OnDelay + " minutes" + "(" + delayDevice5 + " seconds)"
				runIn(delayDevice5, device5On)
            }
       	}
    	settings.device6Modes.each {
          	log.debug "Device6 Operating State: " + it   
	    	if (TOS == it) {
	        	def delayDevice6 = settings.device6OnDelay*60
				log.debug "Turning Device6 on in " + settings.device6OnDelay + " minutes" + "(" + delayDevice6 + " seconds)"
				runIn(delayDevice6, device6On)
            }
       	}
        } else {
    	log.debug "Thermostat operating state is idle, devices left off"
    }
}

def device1On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
    	log.debug "Thermostat operating state is not idle"
        device1.on()
    	log.debug "Device 1 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device1 left off"
    }
}

def device2On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
    	log.debug "Thermostat operating state is not idle"
        device2.on()
    	log.debug "Device 2 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device2 left off"
    }
}

def device3On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
     	log.debug "Thermostat operating state is not idle"
        device3.on()
    	log.debug "Device 3 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device3 left off"
    }
}

def device4On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
     	log.debug "Thermostat operating state is not idle"
        device4.on()
    	log.debug "Device 4 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device4 left off"
    }
}

def device5On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
     	log.debug "Thermostat operating state is not idle"
        device5.on()
    	log.debug "Device 5 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device5 left off"
    }
}

def device6On() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS != "idle") {
     	log.debug "Thermostat operating state is not idle"
        device6.on()
    	log.debug "Device 6 On"
  	} else {
    	log.debug "Thermostat operating state is idle, device6 left off"
    }
}

def off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "257 Operating State is " + TOS    
    if (TOS == "idle") {
        statusSwitch.off()    
    	def delayDevice1 = settings.device1OffDelay*60
		log.debug "Turning Device1 off in " + settings.device1OffDelay + " minutes" + "(" + delayDevice1 + " seconds)"
		runIn(delayDevice1, device1Off)
       	def delayDevice2 = settings.device2OffDelay*60
		log.debug "Turning Device2 off in " + settings.device2OffDelay + " minutes" + "(" + delayDevice2 + " seconds)"
		runIn(delayDevice2, device2Off)
       	def delayDevice3 = settings.device3OffDelay*60
		log.debug "Turning Device3 off in " + settings.device3OffDelay + " minutes" + "(" + delayDevice3 + " seconds)"
		runIn(delayDevice3, device3Off)
       	def delayDevice4 = settings.device4OffDelay*60
		log.debug "Turning Device4 off in " + settings.device4OffDelay + " minutes" + "(" + delayDevice4 + " seconds)"
		runIn(delayDevice4, device4Off)
       	def delayDevice5 = settings.device5OffDelay*60
		log.debug "Turning Device5 off in " + settings.device5OffDelay + " minutes" + "(" + delayDevice5 + " seconds)"
		runIn(delayDevice5, device5Off)
       	def delayDevice6 = settings.device6OffDelay*60
		log.debug "Turning Device6 off in " + settings.device6OffDelay + " minutes" + "(" + delayDevice6 + " seconds)"
		runIn(delayDevice6, device6Off)
    } else {
    	log.debug "Thermostat operating state is not idle, devices left on"    	
    }
}

def device1Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
     	log.debug "Thermostat operating state is idle"
        device1.off()
    	log.debug "Device 1 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device1 left on"
    }
}

def device2Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
    	log.debug "Thermostat operating state is idle"
        device2.off()
    	log.debug "Device 2 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device2 left on"
    }
}

def device3Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
    	log.debug "Thermostat operating state is idle"
        device3.off()
    	log.debug "Device 3 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device3 left on"
    }
}

def device4Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
    	log.debug "Thermostat operating state is idle"
        device4.off()
    	log.debug "Device 4 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device4 left on"
    }
}

def device5Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
    	log.debug "Thermostat operating state is idle"
        device5.off()
    	log.debug "Device 5 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device5 left on"
    }
}

def device6Off() {
	myThermostat.refresh()
    def TOS = myThermostat.currentValue("thermostatOperatingState")
    log.debug "Operating State is " + TOS    
	if (TOS == "idle") {
    	log.debug "Thermostat operating state is idle"
        device6.off()
    	log.debug "Device 6 Off"
  	} else {
    	log.debug "Thermostat operating state is not idle, device6 left on"
    }
}

def poll() {
	myThermostat.refresh()
}
