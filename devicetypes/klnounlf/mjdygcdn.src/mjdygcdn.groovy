/**
 *  mjdygcdn
 *
 *  Copyright 2016 tiqkf124
 *
 */
metadata {
	definition (name: "mjdygcdn", namespace: "klnounlf", author: "tiqkf124", oauth: [displayName: "ajhhotbs", displayLink: "1"]) {
		capability "Momentary"
		capability "Image Capture"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Indicator"
		capability "Valve"
		capability "Location Mode"
		capability "Signal Strength"
		capability "Smoke Detector"
		capability "Carbon Monoxide Detector"
		capability "Button"
		capability "Music Player"
		capability "Lock Codes"
		capability "Color Control"
		capability "Sensor"
		capability "Actuator"
		capability "Relay Switch"
		capability "Beacon"
		capability "Sleep Sensor"
		capability "Step Sensor"
		capability "Test Capability"
		capability "Door Control"
		capability "Media Controller"
		capability "Speech Synthesis"
		capability "Speech Recognition"
		capability "Thermostat Cooling Setpoint"
		capability "Touch Sensor"
		capability "Thermostat Mode"
		capability "Thermostat Fan Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Setpoint"
		capability "TV"
		capability "Color Temperature"
		capability "Garage Door Control"
		capability "Estimated Time Of Arrival"
		capability "Notification"
		capability "Thermostat Schedule"
		capability "Health Check"
		capability "Ultraviolet Index"
		capability "Video Camera"
		capability "Video Capture"
		capability "Zw Multichannel"
		capability "Sound Sensor"
		capability "Consumable"
		capability "Timed Session"
		capability "Carbon Dioxide Measurement"
		capability "Sound Pressure Level"
		capability "pH Measurement"
		capability "Tamper Alert"
		capability "Voltage Measurement"
		capability "Window Shade"
		capability "Shock Sensor"
		capability "Samsung TV"
		capability "Illuminance Measurement"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Switch"
		capability "Battery"
		capability "Contact Sensor"
		capability "Motion Sensor"
		capability "Presence Sensor"
		capability "Alarm"
		capability "Water Sensor"
		capability "Polling"
		capability "Configuration"
		capability "Tone"
		capability "Three Axis"
		capability "Switch Level"
		capability "Lock"
		capability "Acceleration Sensor"
		capability "Refresh"
		capability "Thermostat"

		attribute "1", "string"

		command "1"

		fingerprint deviceId: "1", deviceVersion: "1", endpointId: "1", inClusters: "1", noneClusters: "1", outClusters: "1", profileId: "1"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'image' attribute
	// TODO: handle 'energy' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'indicatorStatus' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'mode' attribute
	// TODO: handle 'lqi' attribute
	// TODO: handle 'rssi' attribute
	// TODO: handle 'smoke' attribute
	// TODO: handle 'carbonMonoxide' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'trackDescription' attribute
	// TODO: handle 'trackData' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'codeReport' attribute
	// TODO: handle 'codeChanged' attribute
	// TODO: handle 'hue' attribute
	// TODO: handle 'saturation' attribute
	// TODO: handle 'color' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'sleeping' attribute
	// TODO: handle 'steps' attribute
	// TODO: handle 'goal' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'phraseSpoken' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'touch' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'colorTemperature' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'eta' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'checkInterval' attribute
	// TODO: handle 'ultravioletIndex' attribute
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'epEvent' attribute
	// TODO: handle 'epInfo' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'consumableStatus' attribute
	// TODO: handle 'sessionStatus' attribute
	// TODO: handle 'timeRemaining' attribute
	// TODO: handle 'carbonDioxide' attribute
	// TODO: handle 'soundPressureLevel' attribute
	// TODO: handle 'pH' attribute
	// TODO: handle 'tamper' attribute
	// TODO: handle 'voltage' attribute
	// TODO: handle 'windowShade' attribute
	// TODO: handle 'shock' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'pictureMode' attribute
	// TODO: handle 'soundMode' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'messageButton' attribute
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'humidity' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'battery' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'alarm' attribute
	// TODO: handle 'water' attribute
	// TODO: handle 'threeAxis' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle '1' attribute

}

// handle commands
def push() {
	log.debug "Executing 'push'"
	// TODO: handle 'push' command
}

def take() {
	log.debug "Executing 'take'"
	// TODO: handle 'take' command
}

def indicatorWhenOn() {
	log.debug "Executing 'indicatorWhenOn'"
	// TODO: handle 'indicatorWhenOn' command
}

def indicatorWhenOff() {
	log.debug "Executing 'indicatorWhenOff'"
	// TODO: handle 'indicatorWhenOff' command
}

def indicatorNever() {
	log.debug "Executing 'indicatorNever'"
	// TODO: handle 'indicatorNever' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def play() {
	log.debug "Executing 'play'"
	// TODO: handle 'play' command
}

def pause() {
	log.debug "Executing 'pause'"
	// TODO: handle 'pause' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}

def playText() {
	log.debug "Executing 'playText'"
	// TODO: handle 'playText' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setTrack() {
	log.debug "Executing 'setTrack'"
	// TODO: handle 'setTrack' command
}

def resumeTrack() {
	log.debug "Executing 'resumeTrack'"
	// TODO: handle 'resumeTrack' command
}

def restoreTrack() {
	log.debug "Executing 'restoreTrack'"
	// TODO: handle 'restoreTrack' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def updateCodes() {
	log.debug "Executing 'updateCodes'"
	// TODO: handle 'updateCodes' command
}

def setCode() {
	log.debug "Executing 'setCode'"
	// TODO: handle 'setCode' command
}

def deleteCode() {
	log.debug "Executing 'deleteCode'"
	// TODO: handle 'deleteCode' command
}

def requestCode() {
	log.debug "Executing 'requestCode'"
	// TODO: handle 'requestCode' command
}

def reloadAllCodes() {
	log.debug "Executing 'reloadAllCodes'"
	// TODO: handle 'reloadAllCodes' command
}

def setHue() {
	log.debug "Executing 'setHue'"
	// TODO: handle 'setHue' command
}

def setSaturation() {
	log.debug "Executing 'setSaturation'"
	// TODO: handle 'setSaturation' command
}

def setColor() {
	log.debug "Executing 'setColor'"
	// TODO: handle 'setColor' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def startActivity() {
	log.debug "Executing 'startActivity'"
	// TODO: handle 'startActivity' command
}

def getAllActivities() {
	log.debug "Executing 'getAllActivities'"
	// TODO: handle 'getAllActivities' command
}

def getCurrentActivity() {
	log.debug "Executing 'getCurrentActivity'"
	// TODO: handle 'getCurrentActivity' command
}

def speak() {
	log.debug "Executing 'speak'"
	// TODO: handle 'speak' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}

def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	// TODO: handle 'channelUp' command
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	// TODO: handle 'channelDown' command
}

def setColorTemperature() {
	log.debug "Executing 'setColorTemperature'"
	// TODO: handle 'setColorTemperature' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}

def setSchedule() {
	log.debug "Executing 'setSchedule'"
	// TODO: handle 'setSchedule' command
}

def ping() {
	log.debug "Executing 'ping'"
	// TODO: handle 'ping' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def flip() {
	log.debug "Executing 'flip'"
	// TODO: handle 'flip' command
}

def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def enableEpEvents() {
	log.debug "Executing 'enableEpEvents'"
	// TODO: handle 'enableEpEvents' command
}

def epCmd() {
	log.debug "Executing 'epCmd'"
	// TODO: handle 'epCmd' command
}

def setConsumableStatus() {
	log.debug "Executing 'setConsumableStatus'"
	// TODO: handle 'setConsumableStatus' command
}

def setTimeRemaining() {
	log.debug "Executing 'setTimeRemaining'"
	// TODO: handle 'setTimeRemaining' command
}

def start() {
	log.debug "Executing 'start'"
	// TODO: handle 'start' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def pause() {
	log.debug "Executing 'pause'"
	// TODO: handle 'pause' command
}

def cancel() {
	log.debug "Executing 'cancel'"
	// TODO: handle 'cancel' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def presetPosition() {
	log.debug "Executing 'presetPosition'"
	// TODO: handle 'presetPosition' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setPictureMode() {
	log.debug "Executing 'setPictureMode'"
	// TODO: handle 'setPictureMode' command
}

def setSoundMode() {
	log.debug "Executing 'setSoundMode'"
	// TODO: handle 'setSoundMode' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def showMessage() {
	log.debug "Executing 'showMessage'"
	// TODO: handle 'showMessage' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def strobe() {
	log.debug "Executing 'strobe'"
	// TODO: handle 'strobe' command
}

def siren() {
	log.debug "Executing 'siren'"
	// TODO: handle 'siren' command
}

def both() {
	log.debug "Executing 'both'"
	// TODO: handle 'both' command
}

def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def beep() {
	log.debug "Executing 'beep'"
	// TODO: handle 'beep' command
}

def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def setSchedule() {
	log.debug "Executing 'setSchedule'"
	// TODO: handle 'setSchedule' command
}

def 1() {
	log.debug "Executing '1'"
	// TODO: handle '1' command
}