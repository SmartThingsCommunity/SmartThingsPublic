definition(
    name: "stage.app.home.ai",
    namespace: "stage.app.home.ai",
    author: "Eric Greer",
    description: "SmartThings SmartApp for stage.app.home.ai.",
    category: "Fun & Social",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    )

// These are preferences displayed in the smart phone app
preferences {
  // we need a settings section to enable subscriptions
  section("Pick which devices home.ai will help you automate:"){
    input "motion", "capability.motionSensor", title: "Choose motion sensors", required: false, multiple: true
    input "contact", "capability.contactSensor", title: "Choose contact sensors", required: false, multiple: true
    input "lightswitch", "capability.switch", title: "Choose normal power switches", required: false, multiple: true
    input "lightswitchlevel", "capability.switchLevel", title: "Choose dimmer power switches", required: false, multiple: true
    input "presence", "capability.presenceSensor", title: "Choose presence sensors", required: false, multiple: true
    input "tempSensor", "capability.temperatureMeasurement", title: "Choose temperature sensors", required: false, multiple: true
    input "humidity", "capability.relativeHumidityMeasurement", title: "Choose humidity sensors", required: false, multiple: true
    input "waterSensor", "capability.waterSensor", title: "Choose water sensors", required: false, multiple: true
    input "lock", "capability.lock", title: "Pick Door Locks", required: false, multiple: true
    input "garagedoor", "capability.garageDoorControl", title: "Pick garage doors", required: false, multiple: true
	input "touchsensor", "capability.touchSensor", title: "Pick touch sensors", required: false, multiple: true
	input "speechparser", "capability.speechRecognition", title: "Pick speech recognizers", required: false, multiple: true
	input "soundsensor", "capability.soundSensor", title: "Pick sound sensors", required: false, multiple: true
	input "smokedetector", "capability.smokeDetector", title: "Pick smoke detectors", required: false, multiple: true
	input "sleepsensor", "capability.sleepSensor", title: "Pick sleep sensors", required: false, multiple: true
	input "carbonsensor", "capability.carbonMonoxideDetector", title: "Pick carbon monoxide detectors", required: false, multiple: true
	input "button", "capability.button", title: "Pick buttons", required: false, multiple: true
	input "beacon", "capability.beacon", title: "Pick beacons", required: false, multiple: true
	input "alarm", "capability.alarm", title: "Pick alarms", required: false, multiple: true
	input "thermostat", "capability.thermostat", title: "Pick thermostats", required: false, multiple: true
    input "voltage", "capability.voltageMeasurement", title: "Pick voltage sensors", required: false, multiple: true
    input "windowshade", "capability.windowShade", title: "Pick window shades", required: false, multiple: true
    input "powermeter", "capability.powerMeter", title: "Pick power meters", required: false, multiple: true
    }
}

// vlaues for security system are 'away', 'stay', or 'off'
// off security
def offSecurity() {
    sendLocationEvent(
            name: "alarmSystemStatus",
            value: "off",
            displayed: false,
            isStateChange: true
            )
}

// stay security
def staySecurity() {
    sendLocationEvent(
            name: "alarmSystemStatus",
            value: "stay",
            displayed: false,
            isStateChange: true
            )
}

// away security
def awaySecurity() {
    sendLocationEvent(
            name: "alarmSystemStatus",
            value: "away",
            displayed: false,
            isStateChange: true
            )
}

// sets window shade open temperature
def setWindowShadeOpen() {
	def deviceID = params.deviceID
	log.debug("setWindowShadeOpen command recieved ${deviceID}")
    windowshade.each {
        if (it.id == deviceID) {
        	log.debug("Operating window shade because it is the one specified: ${deviceID}");
    		it.open()
        } else {
        	log.debug("NOT operting window shade because it is not the one specified: ${deviceID}");
        }
    }
}

// sets window shade close temperature
def setWindowShadeClosed() {
	def deviceID = params.deviceID
	log.debug("setWindowShadeClosed command recieved ${deviceID}")
    windowshade.each {
        if (it.id == deviceID) {
        	log.debug("Operating window shade because it is the one specified: ${deviceID}");
    		it.close()
        } else {
        	log.debug("NOT operting window shade because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat heating temperature
def setThermostatHeatTemp() {
	def deviceID = params.deviceID
	log.debug("setThermostatHeat command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.setHeatingSetpoint(params.temp)
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat cooling temperature
def setThermostatCoolTemp() {
	def deviceID = params.deviceID
	log.debug("setThermostatCool command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.setCoolingSetpoint(params.temp)
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat off
def setThermostatOff() {
	def deviceID = params.deviceID
	log.debug("setThermostatOff command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.off()
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat to heat
def setThermostatHeat() {
	def deviceID = params.deviceID
	log.debug("setThermostatHeat command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.heat()
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat to cool
def setThermostatCool() {
	def deviceID = params.deviceID
	log.debug("setThermostatCool command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.cool()
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat mode
def setThermostatMode() {
	def deviceID = params.deviceID
	log.debug("setThermostatMode command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
    		it.setThermostatMode(params.mode)
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sets thermostat fan mode
def setThermostatFanMode() {
	def deviceID = params.deviceID
	log.debug("setThermostatFanMode command recieved ${deviceID}")
    thermostat.each {
        if (it.id == deviceID) {
        	log.debug("Operating thermostat because it is the one specified: ${deviceID}");
			it.setThermostatFanMode(params.mode)
        } else {
        	log.debug("NOT operting thermostat because it is not the one specified: ${deviceID}");
        }
    }
}

// sends an alarm strobe
def strobeAlarm() {
	def deviceID = params.deviceID
	log.debug("Alarm strobe command recieved ${deviceID}")
    alarm.each {
        if (it.id == deviceID) {
        	log.debug("Operating alarm because it is the one specified: ${deviceID}");
    		it.strobe()
        } else {
        	log.debug("NOT operting alarm because it is not the one specified: ${deviceID}");
        }
    }
}

// sends an alarm siren
def sirenAlarm() {
	def deviceID = params.deviceID
	log.debug("Alarm siren command recieved ${deviceID}")
    alarm.each {
        if (it.id == deviceID) {
        	log.debug("Operating alarm because it is the one specified: ${deviceID}");
    		it.siren()
        } else {
        	log.debug("NOT operting alarm because it is not the one specified: ${deviceID}");
        }
    }
}

// disables an alarm siren
def silenceAlarm() {
	def deviceID = params.deviceID
	log.debug("Alarm silence command recieved ${deviceID}")
    alarm.each {
        if (it.id == deviceID) {
        	log.debug("Operating alarm because it is the one specified: ${deviceID}");
    		it.off()
        } else {
        	log.debug("NOT operting alarm because it is not the one specified: ${deviceID}");
        }
    }
}

// opens a garage door
def openGarage() {
	def deviceID = params.deviceID
	log.debug("Open Garage command recieved ${deviceID}")
    garagedoor.each {
        if (it.id == deviceID) {
        	log.debug("Operating garage door because it is the one specified: ${deviceID}");
    		it.open()
        } else {
        	log.debug("NOT operting garage door device because it is not the one specified: ${deviceID}");
        }
    }
}

// closes a garage door
def closeGarage() {
	def deviceID = params.deviceID
	log.debug("Close Garage command recieved ${deviceID}")
    garagedoor.each {
        if (it.id == deviceID) {
        	log.debug("Operating garage door because it is the one specified: ${deviceID}");
    		it.close()
        } else {
        	log.debug("NOT operting garage door device because it is not the one specified: ${deviceID}");
        }
    }
}


// lock locks a door lock
def lockDoor() {
	def deviceID = params.deviceID
	log.debug("Lock command recieved ${deviceID}")
    lock.each {
        if (it.id == deviceID) {
        	log.debug("Operating lock device because it is the one specified: ${deviceID}");
    		it.lock()
        } else {
        	log.debug("NOT operting lock device because it is not the one specified: ${deviceID}");
        }
    }
}

// unlock unlocks a door lock
def unlockDoor() {
	def deviceID = params.deviceID
	log.debug("Unlock command recieved ${deviceID}")
    lock.each {
        if (it.id == deviceID) {
        	log.debug("Operating lock device because it is the one specified: ${deviceID}");
    		it.unlock()
        } else {
        	log.debug("NOT operting lock device because it is not the one specified: ${deviceID}");
        }
    }
}

// turns on a wall switch as instructed from the homeai webservice
def switchOn() {
	def deviceID = params.deviceID
	log.debug("Switch on command recieved ${deviceID}")
    lightswitch.each {
        if (it.id == deviceID) {
        	log.debug("Operating switch device because it is the one specified: ${deviceID}");
    		it.on()
        } else {
        	log.debug("Skipping switch device because it is not the one specified: ${deviceID}");
        }
    }
}

// turns off a wall switch as instructed from the homeai webservice
def switchOff() {
	def deviceID = params.deviceID
	log.debug("Switch off desired for ${deviceID}")
	lightswitch.each {
        if (it.id == deviceID) {
        	log.debug("Operating switch device because it is the one specified: ${deviceID}");
    		it.off()
        } else {
        	log.debug("Skipping switch device because it is not the one specified: ${deviceID}");
        }
    }
}

// fetch the id of this smartthings hub
def hubId() {
	log.debug("hub id requested.")
    def response = [hubId: location.hubs.id[0]]
}

// This handles requests for device inventories
def inventory() {
    def response = []

    lightswitch.each {
      response << [name: it.displayName, value: it.currentValue("switch"), deviceId: it.id, type: "lightSwitch"]
    }

    contact.each {
      response << [name: it.displayName, value: it.currentValue("contact"), deviceId: it.id, type: "contact"]
    }


    motion.each {
      response << [name: it.displayName, value: it.currentValue("motion"), deviceId: it.id, type: "motion"]
    }

    presence.each {
	  response << [name: it.displayName, value: it.currentValue("presence"), deviceId: it.id, type: "presence"]
    }

	// removed until dual device functions are supported on the backend
    //tempSensor.each {
	//  response << [name: it.displayName, value: it.currentValue("temperature"), deviceId: it.id, type: "tempSensor"]
    //}
    
    //humidity.each {
	//  response << [name: it.displayName, value: it.currentValue("humidity"), deviceId: it.id, type: "humiditySensor"]
    //}
    
    
    waterSensor.each {
	  response << [name: it.displayName, value: it.currentValue("water"), deviceId: it.id, type: "waterSensor"]
    }

    lock.each {
	  response << [name: it.displayName, value: it.currentValue("lock"), deviceId: it.id, type: "lock"]
    }


    garagedoor.each {
	  response << [name: it.displayName, value: it.currentValue("door"), deviceId: it.id, type: "garagedoor"]
    }
    
    
    touchsensor.each {
	  response << [name: it.displayName, value: it.currentValue("touch"), deviceId: it.id, type: "touchsensor"]
    }
    
    
    speechparser.each {
	  response << [name: it.displayName, value: it.currentValue("phraseSpoken"), deviceId: it.id, type: "speechparser"]
    }
    
    
    soundsensor.each {
	  response << [name: it.displayName, value: it.currentValue("sound"), deviceId: it.id, type: "sound"]
    }
    
    
    smokedetector.each {
	  response << [name: it.displayName, value: it.currentValue("smoke"), deviceId: it.id, type: "smoke"]
    }


    sleepsensor.each {
	  response << [name: it.displayName, value: it.currentValue("sleeping"), deviceId: it.id, type: "sleepsensor"]
    }


    carbonsensor.each {
	  response << [name: it.displayName, value: it.currentValue("carbonMonoxide"), deviceId: it.id, type: "carbonsensor"]
    }


    button.each {
	  response << [name: it.displayName, value: it.currentValue("button"), deviceId: it.id, type: "button"]
    }
    
    
    beacon.each {
	  response << [name: it.displayName, value: it.currentValue("presence"), deviceId: it.id, type: "beacon"]
    }
   
   
    alarm.each {
	  response << [name: it.displayName, value: it.currentValue("alarm"), deviceId: it.id, type: "alarm"]
    }


    thermostat.each {
	  response << [name: it.displayName, value: it.currentValue("thermostatMode"), deviceId: it.id, type: "thermostat"]
    }
    
    
    voltage.each {
	  response << [name: it.displayName, value: it.currentValue("voltage"), deviceId: it.id, type: "voltage"]
    }


    windowshade.each {
	  response << [name: it.displayName, value: it.currentValue("windowShade"), deviceId: it.id, type: "windowshade"]
    }
    
     
    powermeter.each {
	  response << [name: it.displayName, value: it.currentValue("power"), deviceId: it.id, type: "powermeter"]
    }
    
    
    lightswitchlevel.each {
	  response << [name: it.displayName, value: it.currentValue("level"), deviceId: it.id, type: "lightswitchlevel"]
    }



    log.debug("Inventory request processed. Response: " + response)
    return response
}

// After the user hits the 'install' button in the mobile app
def installed() {
	initialize()
}

// After app settings are changed.  All subscriptions are wiped before this is invoked by smartthings.
def updated() {
	unsubscribe()
	initialize()
}

// This appears to be what the tutorials meant to use in the examples
def initialize() {

	// SHM subscription
    // evt.value will be "off", "stay", or "away"
    subscribe(location, "alarmSystemStatus", eventForwarder)


    // motion sensor subscription
    subscribe(motion, "motion", eventForwarder)


    // Contact sensor subscription
    subscribe(contact, "contact", eventForwarder)


    // power plug subscription
    subscribe(lightswitch, "switch", eventForwarder)


    // presence sensor subscription
    subscribe(presence, "presence", eventForwarder)


    // temperature sensor subscription
    subscribe(tempSensor, "temperature", eventForwarder)


    // water sensor subscription
    subscribe(waterSensor, "water", eventForwarder)


    // humidity sensor subscription
    subscribe(humidity, "humidity", eventForwarder)


    // lock subscription
    subscribe(lock, "lock", eventForwarder)


    // garage door subscription
    subscribe(garagedoor, "garagedoor", eventForwarder)


    // touch sensor subscription
    subscribe(touchsensor, "touchsensor", eventForwarder)


    // speech parser subscription
    subscribe(speechparser, "phraseSpoken", eventForwarder)


    // sound sensor subscription
    subscribe(soundsensor, "sound", eventForwarder)


    // smoke detector subscription
    subscribe(smokedetector, "smoke", eventForwarder)


    // sleep sensor subscription
    subscribe(sleepsensor, "sleeping", eventForwarder)


    // carbon monoxide sensor subscription
    subscribe(carbonsensor, "carbonMonoxide", eventForwarder)
    
    
    // button subscription
    subscribe(button, "button", eventForwarder)


    // beacon subscription
    subscribe(beacon, "presence", eventForwarder)


    // alarm subscription
    subscribe(alarm, "alarm", eventForwarder)

    // thermostat subscriptions
    subscribe(thermostat, "temperature", eventForwarder)
    subscribe(thermostat, "heatingSetpoint", eventForwarder)
    subscribe(thermostat, "coolingSetpoint", eventForwarder)
    subscribe(thermostat, "thermostatSetpoint", eventForwarder)
    subscribe(thermostat, "thermostatMode", eventForwarder)
    subscribe(thermostat, "thermostatFanMode", eventForwarder)
    subscribe(thermostat, "thermostatOperatingState", eventForwarder)

    // voltage subscription
    subscribe(voltage, "voltage", eventForwarder)
    
    // window shade subscription
    subscribe(windowshade, "windowShade", eventForwarder)
    
	// shm events
    subscribe(location, "alarmSystemStatus", shmEventForwarder)
    
    // power meter subscription
    subscribe(powermeter, "power", eventForwarder)
    
    // level switch (dimmer switch)
    subscribe(lightswitchlevel, "level", eventForwarder)
}

def shmEventForwarder(evt) {
    // evt.value will be "off", "stay", or "away"
    log.debug("FORWARDING SHM CHANGE" + evt.value + " " + evt.hub.id)

    def deviceState = evt.value
    def deviceId = "smarthomemonitor"
    def hubId = hubId()
    def params = [
    	uri: "https://stage.app.home.ai",
    	path: "/smartThingsPostback/shmStateChange/${hubId}/${deviceId}/${deviceState}"
	]
	log.info(params)
    httpGet(params)
    
}

// This is used to forward events to the home.ai webservice
def eventForwarder(evt) {

    def hubId = location.hubs.id[0]

	log.debug(params.uri + " " + params.path)
	log.debug("FORWARDING EVENT" + evt.deviceId + " " + evt.value + " " + hubId)

    def deviceId = evt.deviceId
    def deviceState = evt.value
    def params = [
    	uri: "https://stage.app.home.ai",
    	path: "/smartThingsPostback/stateChange/${hubId}/${deviceId}/${deviceState}"
	]
	log.info(params)
    httpGet(params)
}

// Mappings that serve web requests against our smart app
mappings {
	path("/inventory") {
    	action: [
        	GET: "inventory"
        ]
    }
	path("/hubId") {
    	action: [
        	GET: "hubId"
        ]
    }
	path("/switchOn/:deviceID") {
    	action: [
        	GET: "switchOn"
        ]
    }
	path("/switchOff/:deviceID") {
    	action: [
        	GET: "switchOff"
        ]
    }
    path("/lock/:deviceID") {
    	action: [
        	GET: "lockDoor"
        ]
    }
    path("/unlock/:deviceID") {
    	action: [
        	GET: "unlockDoor"
        ]
    }
    path("/opengarage/:deviceID") {
    	action: [
        	GET: "openGarage"
        ]
    }
    path("/closegarage/:deviceID") {
    	action: [
        	GET: "closeGarage"
        ]
    }
    path("/strobeAlarm/:deviceID") {
    	action: [
        	GET: "strobeAlarm"
        ]
    }
    path("/sirenAlarm/:deviceID") {
    	action: [
        	GET: "sirenAlarm"
        ]
    }
    path("/silenceAlarm/:deviceID") {
    	action: [
        	GET: "silenceAlarm"
        ]
    }
    path("/setThermostatHeatTemp/:deviceID/:temp") {
    	action: [
        	GET: "setThermostatHeatTemp"
        ]
    }
    path("/setThermostatCoolTemp/:deviceID/:temp") {
    	action: [
        	GET: "setThermostatCoolTemp"
        ]
    }
    path("/setThermostatHeat/:deviceID") {
    	action: [
        	GET: "setThermostatHeat"
        ]
    }
    path("/setThermostatCool/:deviceID") {
    	action: [
        	GET: "setThermostatCool"
        ]
    }
    path("/setThermostatMode/:deviceID/:mode") {
    	action: [
        	GET: "setThermostatMode"
        ]
    }
    path("/setThermostatFanMode/:deviceID/:mode") {
    	action: [
        	GET: "setThermostatFanMode"
        ]
    }
    path("/closeWindowShade/:deviceID") {
    	action: [
        	GET: "setWindowShadeClosed"
        ]
    }
    path("/openWindowShade/:deviceID") {
    	action: [
        	GET: "setWindowShadeOpen"
        ]
    }
	path("/awaySecurity") {
    	action: [
        	GET: "awaySecurity"
        ]
    }
    path("/staySecurity") {
    	action: [
        	GET: "staySecurity"
        ]
    }
    path("/offSecurity") {
    	action: [
        	GET: "offSecurity"
        ]
    }
}
