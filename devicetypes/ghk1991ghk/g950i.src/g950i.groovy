/**
 *  g950i
 *
 *  Copyright 2018 G K
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
metadata {
	definition (name: "g950i", namespace: "ghk1991ghk", author: "G K") {
		capability "Audio Notification"
		capability "Audio Track Addressing"
		capability "Battery"
		capability "Beacon"
		capability "Carbon Dioxide Measurement"
		capability "Carbon Monoxide Detector"
		capability "Color Temperature"
		capability "Consumable"
		capability "Contact Sensor"
		capability "Demand Response Load Control"
		capability "Dishwasher Mode"
		capability "Dishwasher Operating State"
		capability "Door Control"
		capability "Dryer Mode"
		capability "Dryer Operating State"
		capability "Dust Sensor"
		capability "Energy Meter"
		capability "Estimated Time Of Arrival"
		capability "Execute"
		capability "Fan Speed"
		capability "Filter State"
		capability "Filter Status"
		capability "Garage Door Control"
		capability "Geolocation"
		capability "Health Check"
		capability "Illuminance Measurement"
		capability "Image Capture"
		capability "Infrared Level"
		capability "Light"
		capability "Location Mode"
		capability "Lock"
		capability "Lock Codes"
		capability "Lock Only"
		capability "Media Controller"
		capability "Media Input Source"
		capability "Media Playback"
		capability "Media Playback Repeat"
		capability "Media Playback Shuffle"
		capability "Media Presets"
		capability "Media Track Control"
		capability "Momentary"
		capability "Music Player"
		capability "Network Meter"
		capability "Notification"
		capability "Ocf"
		capability "Odor Sensor"
		capability "Outlet"
		capability "Oven Mode"
		capability "Panic Alarm"
		capability "pH Measurement"
		capability "Polling"
		capability "Power"
		capability "Power Meter"
		capability "Power Source"
		capability "Rapid Cooling"
		capability "Refresh"
		capability "Refrigeration"
		capability "Relay Switch"
		capability "Remote Control"
		capability "Remote Control Status"
		capability "Robot Cleaner Cleaning Mode"
		capability "Robot Cleaner Movement"
		capability "Robot Cleaner Turbo Mode"
		capability "Samsung TV"
		capability "Security System"
		capability "Sensor"
		capability "Three Axis"
		capability "Tone"
		capability "Touch Sensor"
		capability "TV"
		capability "Tv Channel"
		capability "Ultraviolet Index"
		capability "Video Camera"
		capability "Video Clips"

		attribute "KNIGHTMARE1991", "string"

		command "KNIGHTMARE"

		fingerprint deviceId: "samsung galaxy s6"
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
	// TODO: handle 'battery' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'carbonDioxide' attribute
	// TODO: handle 'carbonMonoxide' attribute
	// TODO: handle 'colorTemperature' attribute
	// TODO: handle 'consumableStatus' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'drlcStatus' attribute
	// TODO: handle 'dishwasherMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dishwasherJobState' attribute
	// TODO: handle 'completionTime' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'dryerMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dryerJobState' attribute
	// TODO: handle 'completionTime' attribute
	// TODO: handle 'dustLevel' attribute
	// TODO: handle 'fineDustLevel' attribute
	// TODO: handle 'energy' attribute
	// TODO: handle 'eta' attribute
	// TODO: handle 'data' attribute
	// TODO: handle 'fanSpeed' attribute
	// TODO: handle 'filterLifeRemaining' attribute
	// TODO: handle 'filterStatus' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'latitude' attribute
	// TODO: handle 'longitude' attribute
	// TODO: handle 'method' attribute
	// TODO: handle 'accuracy' attribute
	// TODO: handle 'altitudeAccuracy' attribute
	// TODO: handle 'heading' attribute
	// TODO: handle 'speed' attribute
	// TODO: handle 'lastUpdateTime' attribute
	// TODO: handle 'checkInterval' attribute
	// TODO: handle 'DeviceWatch-DeviceStatus' attribute
	// TODO: handle 'healthStatus' attribute
	// TODO: handle 'healthStatus' attribute
	// TODO: handle 'healthStatus' attribute
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'image' attribute
	// TODO: handle 'infraredLevel' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'mode' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'codeChanged' attribute
	// TODO: handle 'lockCodes' attribute
	// TODO: handle 'scanCodes' attribute
	// TODO: handle 'codeLength' attribute
	// TODO: handle 'maxCodes' attribute
	// TODO: handle 'maxCodeLength' attribute
	// TODO: handle 'minCodeLength' attribute
	// TODO: handle 'codeReport' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'inputSource' attribute
	// TODO: handle 'supportedInputSources' attribute
	// TODO: handle 'playbackStatus' attribute
	// TODO: handle 'playbackRepeatMode' attribute
	// TODO: handle 'playbackShuffle' attribute
	// TODO: handle 'presets' attribute
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'trackDescription' attribute
	// TODO: handle 'trackData' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'uplinkSpeed' attribute
	// TODO: handle 'downlinkSpeed' attribute
	// TODO: handle 'n' attribute
	// TODO: handle 'icv' attribute
	// TODO: handle 'dmv' attribute
	// TODO: handle 'di' attribute
	// TODO: handle 'pi' attribute
	// TODO: handle 'mnmn' attribute
	// TODO: handle 'mnml' attribute
	// TODO: handle 'mnmo' attribute
	// TODO: handle 'mndt' attribute
	// TODO: handle 'mnpv' attribute
	// TODO: handle 'mnos' attribute
	// TODO: handle 'mnhw' attribute
	// TODO: handle 'mnfv' attribute
	// TODO: handle 'mnsl' attribute
	// TODO: handle 'st' attribute
	// TODO: handle 'vid' attribute
	// TODO: handle 'odorLevel' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'ovenMode' attribute
	// TODO: handle 'panicAlarm' attribute
	// TODO: handle 'pH' attribute
	// TODO: handle 'powerSource' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'powerSource' attribute
	// TODO: handle 'rapidCooling' attribute
	// TODO: handle 'rapidCooling' attribute
	// TODO: handle 'rapidFreezing' attribute
	// TODO: handle 'defrost' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'controlGesture' attribute
	// TODO: handle 'remoteControlEnabled' attribute
	// TODO: handle 'robotCleanerCleaningMode' attribute
	// TODO: handle 'robotCleanerMovement' attribute
	// TODO: handle 'robotCleanerTurboMode' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'pictureMode' attribute
	// TODO: handle 'soundMode' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'messageButton' attribute
	// TODO: handle 'securitySystemStatus' attribute
	// TODO: handle 'alarm' attribute
	// TODO: handle 'threeAxis' attribute
	// TODO: handle 'touch' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'tvChannel' attribute
	// TODO: handle 'ultravioletIndex' attribute
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'videoClip' attribute
	// TODO: handle 'KNIGHTMARE1991' attribute

}

// handle commands
def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def playTrackAndResume() {
	log.debug "Executing 'playTrackAndResume'"
	// TODO: handle 'playTrackAndResume' command
}

def playTrackAndRestore() {
	log.debug "Executing 'playTrackAndRestore'"
	// TODO: handle 'playTrackAndRestore' command
}

def setAudioTrack() {
	log.debug "Executing 'setAudioTrack'"
	// TODO: handle 'setAudioTrack' command
}

def setColorTemperature() {
	log.debug "Executing 'setColorTemperature'"
	// TODO: handle 'setColorTemperature' command
}

def setConsumableStatus() {
	log.debug "Executing 'setConsumableStatus'"
	// TODO: handle 'setConsumableStatus' command
}

def requestDrlcAction() {
	log.debug "Executing 'requestDrlcAction'"
	// TODO: handle 'requestDrlcAction' command
}

def overrideDrlcAction() {
	log.debug "Executing 'overrideDrlcAction'"
	// TODO: handle 'overrideDrlcAction' command
}

def setDishwasherMode() {
	log.debug "Executing 'setDishwasherMode'"
	// TODO: handle 'setDishwasherMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def setDryerMode() {
	log.debug "Executing 'setDryerMode'"
	// TODO: handle 'setDryerMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def execute() {
	log.debug "Executing 'execute'"
	// TODO: handle 'execute' command
}

def setFanSpeed() {
	log.debug "Executing 'setFanSpeed'"
	// TODO: handle 'setFanSpeed' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def ping() {
	log.debug "Executing 'ping'"
	// TODO: handle 'ping' command
}

def take() {
	log.debug "Executing 'take'"
	// TODO: handle 'take' command
}

def setInfraredLevel() {
	log.debug "Executing 'setInfraredLevel'"
	// TODO: handle 'setInfraredLevel' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def setMode() {
	log.debug "Executing 'setMode'"
	// TODO: handle 'setMode' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
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

def unlockWithTimeout() {
	log.debug "Executing 'unlockWithTimeout'"
	// TODO: handle 'unlockWithTimeout' command
}

def setCodeLength() {
	log.debug "Executing 'setCodeLength'"
	// TODO: handle 'setCodeLength' command
}

def nameSlot() {
	log.debug "Executing 'nameSlot'"
	// TODO: handle 'nameSlot' command
}

def updateCodes() {
	log.debug "Executing 'updateCodes'"
	// TODO: handle 'updateCodes' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def startActivity() {
	log.debug "Executing 'startActivity'"
	// TODO: handle 'startActivity' command
}

def setInputSource() {
	log.debug "Executing 'setInputSource'"
	// TODO: handle 'setInputSource' command
}

def setPlaybackStatus() {
	log.debug "Executing 'setPlaybackStatus'"
	// TODO: handle 'setPlaybackStatus' command
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

def setPlaybackRepeatMode() {
	log.debug "Executing 'setPlaybackRepeatMode'"
	// TODO: handle 'setPlaybackRepeatMode' command
}

def setPlaybackShuffle() {
	log.debug "Executing 'setPlaybackShuffle'"
	// TODO: handle 'setPlaybackShuffle' command
}

def selectPreset() {
	log.debug "Executing 'selectPreset'"
	// TODO: handle 'selectPreset' command
}

def playPreset() {
	log.debug "Executing 'playPreset'"
	// TODO: handle 'playPreset' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
}

def push() {
	log.debug "Executing 'push'"
	// TODO: handle 'push' command
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

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}

def postOcfCommand() {
	log.debug "Executing 'postOcfCommand'"
	// TODO: handle 'postOcfCommand' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def setOvenMode() {
	log.debug "Executing 'setOvenMode'"
	// TODO: handle 'setOvenMode' command
}

def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def setRapidCooling() {
	log.debug "Executing 'setRapidCooling'"
	// TODO: handle 'setRapidCooling' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def setRapidCooling() {
	log.debug "Executing 'setRapidCooling'"
	// TODO: handle 'setRapidCooling' command
}

def setRapidFreezing() {
	log.debug "Executing 'setRapidFreezing'"
	// TODO: handle 'setRapidFreezing' command
}

def setDefrost() {
	log.debug "Executing 'setDefrost'"
	// TODO: handle 'setDefrost' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def setRobotCleanerCleaningMode() {
	log.debug "Executing 'setRobotCleanerCleaningMode'"
	// TODO: handle 'setRobotCleanerCleaningMode' command
}

def setRobotCleanerMovement() {
	log.debug "Executing 'setRobotCleanerMovement'"
	// TODO: handle 'setRobotCleanerMovement' command
}

def setRobotCleanerTurboMode() {
	log.debug "Executing 'setRobotCleanerTurboMode'"
	// TODO: handle 'setRobotCleanerTurboMode' command
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

def armStay() {
	log.debug "Executing 'armStay'"
	// TODO: handle 'armStay' command
}

def armAway() {
	log.debug "Executing 'armAway'"
	// TODO: handle 'armAway' command
}

def disarm() {
	log.debug "Executing 'disarm'"
	// TODO: handle 'disarm' command
}

def beep() {
	log.debug "Executing 'beep'"
	// TODO: handle 'beep' command
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

def setTvChannel() {
	log.debug "Executing 'setTvChannel'"
	// TODO: handle 'setTvChannel' command
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	// TODO: handle 'channelUp' command
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	// TODO: handle 'channelDown' command
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

def captureClip() {
	log.debug "Executing 'captureClip'"
	// TODO: handle 'captureClip' command
}

def KNIGHTMARE() {
	log.debug "Executing 'KNIGHTMARE'"
	// TODO: handle 'KNIGHTMARE' command
}