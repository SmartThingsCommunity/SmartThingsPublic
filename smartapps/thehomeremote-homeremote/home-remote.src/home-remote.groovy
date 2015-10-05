/**
 *  Home Remote
 *
 *  Copyright 2015 The Home Remote
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
    name: "Home Remote",
    namespace: "thehomeremote.homeremote",
    author: "The Home Remote",
    description: "Web service that to enables communication between the Home Remote app and a SmartThings hub.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "The Home Remote", displayLink: "http://thehomeremote.com/"])


preferences {
    section() {
	input "accelerationSensors", "capability.accelerationSensor", multiple: true, required: false
	input "actuators", "capability.actuator", multiple: true, required: false
	input "alarms", "capability.alarm", multiple: true, required: false
	input "batteries", "capability.battery", multiple: true, required: false
	input "beacons", "capability.beacon", multiple: true, required: false
	input "buttonGroup", "capability.button", multiple: true, required: false
	input "carbonMonoxideDetectors", "capability.carbonMonoxideDetector", multiple: true, required: false
	input "colorControls", "capability.colorControl", multiple: true, required: false
	input "configurations", "capability.configuration", multiple: true, required: false
	input "contactSensors", "capability.contactSensor", multiple: true, required: false
	input "doorControls", "capability.doorControl", multiple: true, required: false
	input "energyMeters", "capability.energyMeter", multiple: true, required: false
	input "illuminanceMeasurements", "capability.illuminanceMeasurement", multiple: true, required: false
	input "imageCaptures", "capability.imageCapture", multiple: true, required: false
	input "locks", "capability.lock", multiple: true, required: false
	input "mediaControllers", "capability.mediaController", multiple: true, required: false
	input "momentaries", "capability.momentary", multiple: true, required: false
	input "motionSensors", "capability.motionSensor", multiple: true, required: false
	input "musicPlayers", "capability.musicPlayer", multiple: true, required: false
	input "notifications", "capability.notification", multiple: true, required: false
	input "pollings", "capability.polling", multiple: true, required: false
	input "powerMeters", "capability.powerMeter", multiple: true, required: false
	input "presenceSensors", "capability.presenceSensor", multiple: true, required: false
	input "refreshes", "capability.refresh", multiple: true, required: false
	input "relativeHumidityMeasurements", "capability.relativeHumidityMeasurement", multiple: true, required: false
	input "relaySwitches", "capability.relaySwitch", multiple: true, required: false
	input "sensors", "capability.sensor", multiple: true, required: false
	input "signalStrengths", "capability.signalStrength", multiple: true, required: false
	input "sleepSensors", "capability.sleepSensor", multiple: true, required: false
	input "smokeDetectors", "capability.smokeDetector", multiple: true, required: false
	input "speechSyntheses", "capability.speechSynthesis", multiple: true, required: false
	input "stepSensors", "capability.stepSensor", multiple: true, required: false
	input "switches", "capability.switch", multiple: true, required: false
	input "switchLevels", "capability.switchLevel", multiple: true, required: false
	input "temperatureMeasurements", "capability.temperatureMeasurement", multiple: true, required: false
	input "thermostats", "capability.thermostat", multiple: true, required: false
	input "thermostatCoolingSetpoints", "capability.thermostatCoolingSetpoint", multiple: true, required: false
	input "thermostatFanModes", "capability.thermostatFanMode", multiple: true, required: false
	input "thermostatHeatingSetpoints", "capability.thermostatHeatingSetpoint", multiple: true, required: false
	input "thermostatModes", "capability.thermostatMode", multiple: true, required: false
	input "thermostatOperatingStates", "capability.thermostatOperatingState", multiple: true, required: false
	input "thermostatSetpoints", "capability.thermostatSetpoint", multiple: true, required: false
	input "threeAxes", "capability.threeAxis", multiple: true, required: false
	input "tones", "capability.tone", multiple: true, required: false
	input "touchSensors", "capability.touchSensor", multiple: true, required: false
	input "valves", "capability.valve", multiple: true, required: false
	input "waterSensors", "capability.waterSensor", multiple: true, required: false
    }
}

mappings {
  path("/GetCurrentValues") {
    action: [
      GET: "getCurrentValues"
    ]
  }
  path("/GetCurrentValuesWithDisplayName") {
    action: [
      GET: "getCurrentValuesWithDisplayName"
    ]
  }
  path("/ExecuteCommand") {
    action: [
      PUT: "executeCommand"
    ]
  }
}

def getCurrentValues() {
    def resp = []    
        
 	accelerationSensors.each {
      resp << [id: it.id, capability: "AccelerationSensor", attribute: "acceleration", value: it.currentValue("acceleration")]
    }

	alarms.each {
      resp << [id: it.id, capability: "Alarm", attribute: "alarm", value: it.currentValue("alarm")]
    }

	batteries.each {
      resp << [id: it.id, capability: "Battery", attribute: "battery", value: it.currentValue("battery")]
    }

	beacons.each {
      resp << [id: it.id, capability: "Beacon", attribute: "presence", value: it.currentValue("presence")]
    }

	buttonGroup.each {
      resp << [id: it.id, capability: "Button", attribute: "button", value: it.currentValue("button")]
    }

	carbonMonoxideDetectors.each {
      resp << [id: it.id, capability: "CarbonMonoxideDetector", attribute: "carbonMonoxide", value: it.currentValue("carbonMonoxide")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "hue", value: it.currentValue("hue")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "saturation", value: it.currentValue("saturation")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "color", value: it.currentValue("color")]
    }

	contactSensors.each {
      resp << [id: it.id, capability: "ContactSensor", attribute: "contact", value: it.currentValue("contact")]
    }

	doorControls.each {
      resp << [id: it.id, capability: "DoorControl", attribute: "door", value: it.currentValue("door")]
    }

	energyMeters.each {
      resp << [id: it.id, capability: "EnergyMeter", attribute: "energy", value: it.currentValue("energy")]
    }

	illuminanceMeasurements.each {
      resp << [id: it.id, capability: "IlluminanceMeasurement", attribute: "illuminance", value: it.currentValue("illuminance")]
    }

	imageCaptures.each {
      resp << [id: it.id, capability: "ImageCapture", attribute: "image", value: it.currentValue("image")]
    }

	locks.each {
      resp << [id: it.id, capability: "Lock", attribute: "lock", value: it.currentValue("lock")]
    }

	mediaControllers.each {
      resp << [id: it.id, capability: "MediaController", attribute: "activities", value: it.currentValue("activities")]
    }

	mediaControllers.each {
      resp << [id: it.id, capability: "MediaController", attribute: "currentActivity", value: it.currentValue("currentActivity")]
    }

	motionSensors.each {
      resp << [id: it.id, capability: "MotionSensor", attribute: "motion", value: it.currentValue("motion")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "status", value: it.currentValue("status")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "level", value: it.currentValue("level")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "trackDescription", value: it.currentValue("trackDescription")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "trackData", value: it.currentValue("trackData")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "mute", value: it.currentValue("mute")]
    }

	powerMeters.each {
      resp << [id: it.id, capability: "PowerMeter", attribute: "power", value: it.currentValue("power")]
    }

	presenceSensors.each {
      resp << [id: it.id, capability: "PresenceSensor", attribute: "presence", value: it.currentValue("presence")]
    }

	relativeHumidityMeasurements.each {
      resp << [id: it.id, capability: "RelativeHumidityMeasurement", attribute: "humidity", value: it.currentValue("humidity")]
    }

	relaySwitches.each {
      resp << [id: it.id, capability: "RelaySwitch", attribute: "switch", value: it.currentValue("switch")]
    }

	signalStrengths.each {
      resp << [id: it.id, capability: "SignalStrength", attribute: "lqi", value: it.currentValue("lqi")]
    }

	signalStrengths.each {
      resp << [id: it.id, capability: "SignalStrength", attribute: "rssi", value: it.currentValue("rssi")]
    }

	sleepSensors.each {
      resp << [id: it.id, capability: "SleepSensor", attribute: "sleeping", value: it.currentValue("sleeping")]
    }

	smokeDetectors.each {
      resp << [id: it.id, capability: "SmokeDetector", attribute: "smoke", value: it.currentValue("smoke")]
    }

	stepSensors.each {
      resp << [id: it.id, capability: "StepSensor", attribute: "steps", value: it.currentValue("steps")]
    }

	stepSensors.each {
      resp << [id: it.id, capability: "StepSensor", attribute: "goal", value: it.currentValue("goal")]
    }

	switches.each {
      resp << [id: it.id, capability: "Switch", attribute: "switch", value: it.currentValue("switch")]
    }

	switchLevels.each {
      resp << [id: it.id, capability: "SwitchLevel", attribute: "level", value: it.currentValue("level")]
    }

	temperatureMeasurements.each {
      resp << [id: it.id, capability: "TemperatureMeasurement", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	thermostatCoolingSetpoints.each {
      resp << [id: it.id, capability: "ThermostatCoolingSetpoint", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	thermostatFanModes.each {
      resp << [id: it.id, capability: "ThermostatFanMode", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostatHeatingSetpoints.each {
      resp << [id: it.id, capability: "ThermostatHeatingSetpoint", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostatModes.each {
      resp << [id: it.id, capability: "ThermostatMode", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostatOperatingStates.each {
      resp << [id: it.id, capability: "ThermostatOperatingState", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	thermostatSetpoints.each {
      resp << [id: it.id, capability: "ThermostatSetpoint", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    }

	threeAxes.each {
      resp << [id: it.id, capability: "ThreeAxis", attribute: "threeAxis", value: it.currentValue("threeAxis")]
    }

	touchSensors.each {
      resp << [id: it.id, capability: "TouchSensor", attribute: "touch", value: it.currentValue("touch")]
    }

	valves.each {
      resp << [id: it.id, capability: "Valve", attribute: "contact", value: it.currentValue("contact")]
    }

	waterSensors.each {
      resp << [id: it.id, capability: "WaterSensor", attribute: "water", value: it.currentValue("water")]
    }
   
    return resp
}

def getCurrentValuesWithDisplayName() {
    def resp = []    
        
 	accelerationSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "AccelerationSensor", attribute: "acceleration", value: it.currentValue("acceleration")]
    }

	alarms.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Alarm", attribute: "alarm", value: it.currentValue("alarm")]
    }

	batteries.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Battery", attribute: "battery", value: it.currentValue("battery")]
    }

	beacons.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Beacon", attribute: "presence", value: it.currentValue("presence")]
    }

	buttonGroup.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Button", attribute: "button", value: it.currentValue("button")]
    }

	carbonMonoxideDetectors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "CarbonMonoxideDetector", attribute: "carbonMonoxide", value: it.currentValue("carbonMonoxide")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "hue", value: it.currentValue("hue")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "saturation", value: it.currentValue("saturation")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "color", value: it.currentValue("color")]
    }

	contactSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ContactSensor", attribute: "contact", value: it.currentValue("contact")]
    }

	doorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "DoorControl", attribute: "door", value: it.currentValue("door")]
    }

	energyMeters.each {
      resp << [id: it.id, displayName: it.displayName, capability: "EnergyMeter", attribute: "energy", value: it.currentValue("energy")]
    }

	illuminanceMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "IlluminanceMeasurement", attribute: "illuminance", value: it.currentValue("illuminance")]
    }

	imageCaptures.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ImageCapture", attribute: "image", value: it.currentValue("image")]
    }

	locks.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Lock", attribute: "lock", value: it.currentValue("lock")]
    }

	mediaControllers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MediaController", attribute: "activities", value: it.currentValue("activities")]
    }

	mediaControllers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MediaController", attribute: "currentActivity", value: it.currentValue("currentActivity")]
    }

	motionSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MotionSensor", attribute: "motion", value: it.currentValue("motion")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "status", value: it.currentValue("status")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "level", value: it.currentValue("level")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "trackDescription", value: it.currentValue("trackDescription")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "trackData", value: it.currentValue("trackData")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "mute", value: it.currentValue("mute")]
    }

	powerMeters.each {
      resp << [id: it.id, displayName: it.displayName, capability: "PowerMeter", attribute: "power", value: it.currentValue("power")]
    }

	presenceSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "PresenceSensor", attribute: "presence", value: it.currentValue("presence")]
    }

	relativeHumidityMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "RelativeHumidityMeasurement", attribute: "humidity", value: it.currentValue("humidity")]
    }

	relaySwitches.each {
      resp << [id: it.id, displayName: it.displayName, capability: "RelaySwitch", attribute: "switch", value: it.currentValue("switch")]
    }

	signalStrengths.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SignalStrength", attribute: "lqi", value: it.currentValue("lqi")]
    }

	signalStrengths.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SignalStrength", attribute: "rssi", value: it.currentValue("rssi")]
    }

	sleepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SleepSensor", attribute: "sleeping", value: it.currentValue("sleeping")]
    }

	smokeDetectors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SmokeDetector", attribute: "smoke", value: it.currentValue("smoke")]
    }

	stepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "StepSensor", attribute: "steps", value: it.currentValue("steps")]
    }

	stepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "StepSensor", attribute: "goal", value: it.currentValue("goal")]
    }

	switches.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Switch", attribute: "switch", value: it.currentValue("switch")]
    }

	switchLevels.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SwitchLevel", attribute: "level", value: it.currentValue("level")]
    }

	temperatureMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "TemperatureMeasurement", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	thermostatCoolingSetpoints.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatCoolingSetpoint", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	thermostatFanModes.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatFanMode", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostatHeatingSetpoints.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatHeatingSetpoint", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostatModes.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatMode", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostatOperatingStates.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatOperatingState", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	thermostatSetpoints.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThermostatSetpoint", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    }

	threeAxes.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThreeAxis", attribute: "threeAxis", value: it.currentValue("threeAxis")]
    }

	touchSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "TouchSensor", attribute: "touch", value: it.currentValue("touch")]
    }

	valves.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Valve", attribute: "contact", value: it.currentValue("contact")]
    }

	waterSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "WaterSensor", attribute: "water", value: it.currentValue("water")]
    }
   
    return resp
}

def getDevices(capability){

	def result

	switch (capability) {
    	case "Alarm":
        	result = alarms
        	break
    	case "ColorControl":
        	result = colorControls
        	break
        case "Configuration":
        	result = configurations
        	break
    	case "DoorControl":
        	result = doorControls
        	break
        case "ImageCapture":
        	result = imageCaptures
        	break
    	case "Lock":
        	result = locks
        	break
        case "MediaController":
        	result = mediaControllers
        	break
    	case "Momentary":
        	result = momentaries
        	break
        case "MusicPlayer":
        	result = musicPlayers
        	break
    	case "Notification":
        	result = notifications
        	break
        case "Polling":
        	result = pollings
        	break
    	case "Refresh":
        	result = refreshes
        	break
        case "RelaySwitch":
        	result = relaySwitches
        	break
    	case "SpeechSynthesis":
        	result = speechSyntheses
        	break
    	case "Switch":
        	result = switches
        	break
        case "SwitchLevel":
        	result = switchLevels
        	break
    	case "Thermostat":
        	result = thermostats
        	break
		case "ThermostatCoolingSetpoint":
        	result = thermostatCoolingSetpoints
        	break
    	case "ThermostatFanMode":
        	result = thermostatFanModes
        	break
        case "ThermostatHeatingSetpoint":
        	result = thermostatHeatingSetpoints
        	break
    	case "ThermostatMode":
        	result = thermostatModes
        	break
    	case "Tone":
        	result = tones
        	break
        case "Valve":
        	result = valves
        	break     
    	default:
        	result = valves
	}
	return result
}

def getDoorControlCommand(value){
	def result
	switch (value) {
        case "closed":
        	result = "close"
        	break
    	case "open":
        	result = "open"
        	break
    	default:
        	result = value
	}    
	return result
}

def getLockCommand(value){
	def result
	switch (value) {
        case "locked":
        	result = "lock"
        	break
    	case "unlocked":
        	result = "unlock"
        	break
    	default:
        	result = value
	}    
	return result
}

def getMuteCommand(value){
	def result
	switch (value) {
        case "muted":
        	result = "mute"
        	break
    	case "unmuted":
        	result = "unmute"
        	break
    	default:
        	result = value
	}    
	return result
}

def getContactCommand(value){
	def result
	switch (value) {
        case "closed":
        	result = "close"
        	break
    	case "open":
        	result = "open"
        	break
    	default:
        	result = value
	}    
	return result
}

def getThermostatFanModeCommand(value){
	def result
	switch (value) {
        case "on":
        	result = "fanOn"
        	break
    	case "auto":
        	result = "fanAuto"
        	break
    	case "circulate":
        	result = "fanCirculate"
        	break
    	default:
        	result = value
	}    
	return result
}

void executeCommand() {
    // use the built-in request object to get the command parameter
    def deviceId = request.JSON?.deviceId
    def capability = request.JSON?.capability
    def attribute = request.JSON?.attribute 
    def value = request.JSON?.value    
    if (deviceId) {
		def devices = getDevices(capability)
        def command
        def valueIsParamter = false
        
        switch (attribute) {    	       	
        	case "hue":
        		command = "setHue"
                valueIsParamter = true
        		break
    		case "saturation":
        		command = "setSaturation"
                valueIsParamter = true
        		break
    		case "color":
        		command = "setColor"
                valueIsParamter = true
        		break
			case "level":
        		command = "setLevel"
                valueIsParamter = true
        		break            
        	case "heatingSetpoint":
        		command = "setHeatingSetpoint"
                valueIsParamter = true
        		break    
       		case "coolingSetpoint":
        		command = "setCoolingSetpoint"
                valueIsParamter = true
        		break
          	case "currentActivity":
        		command = "startActivity"
                valueIsParamter = true
        		break  
 	  		case "door":
        		command = getDoorControlCommand(value)
        		break    
      		case "lock":
        		command = getLockCommand(value)
        		break           
      		case "mute":
        		command = getMuteCommand(value)
        		break         
      		case "thermostatFanMode":
        		command = getThermostatFanModeCommand(value)
        		break         
       		case "thermostatMode":
        		if (value == "emergency heat") {
                	command = "emergencyHeat"
           		}
        		break     
      		case "contact":
        		command = getContactCommand(value)
        		break
    		default:
        		result = value
		}
        
        // check that the switch supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        devices.each {
        	if (it.id == deviceId) {
            	if (!it.hasCommand(command)) {
                	httpError(501, "$command is not a valid command for all devices specified")
            	}                
                if(valueIsParamter){
                	it."$command"(value)
                }
                else{
                	it."$command"()
                }   
           }       
        }
    }
}

def installed() {}

def updated() {}
