/**
 *  SmartThings
 *
 *  Copyright 2018 Raymond Carter
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
	definition (name: "SmartThings", namespace: "GitHub", author: "Raymond Carter") {
		capability "Audio Notification"
		capability "Battery"
		capability "Beacon"
		capability "Button"
		capability "Contact Sensor"
		capability "Door Control"
		capability "Execute"
		capability "Fan Speed"
		capability "Indicator"
		capability "Infrared Level"
		capability "Light"
		capability "Location Mode"
		capability "Lock"
		capability "Lock Codes"
		capability "Media Group"
		capability "Media Playback Shuffle"
		capability "Media Track Control"
		capability "Motion Sensor"
		capability "Network Meter"
		capability "Notification"
		capability "Object Detection"
		capability "Outlet"
		capability "Panic Alarm"
		capability "Power Consumption Report"
		capability "Power Meter"
		capability "Presence Sensor"
		capability "Relay Switch"
		capability "Remote Control"
		capability "Remote Control Status"
		capability "Security System"
		capability "Shock Sensor"
		capability "Signal Strength"
		capability "Speech Recognition"
		capability "Switch"
		capability "TV"
		capability "Tv Channel"
		capability "Video Camera"
		capability "Video Stream"
		capability "Zw Multichannel"

		attribute "Amazon Alexa echo dot", "string"

		command "Alexa"
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
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'supportedButtonValues' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'data' attribute
	// TODO: handle 'fanSpeed' attribute
	// TODO: handle 'indicatorStatus' attribute
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
	// TODO: handle 'groupRole' attribute
	// TODO: handle 'groupPrimaryDeviceId' attribute
	// TODO: handle 'groupId' attribute
	// TODO: handle 'playbackShuffle' attribute
	// TODO: handle 'supportedTrackControlCommands' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'uplinkSpeed' attribute
	// TODO: handle 'downlinkSpeed' attribute
	// TODO: handle 'detected' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'panicAlarm' attribute
	// TODO: handle 'powerConsumption' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'controlGesture' attribute
	// TODO: handle 'remoteControlEnabled' attribute
	// TODO: handle 'securitySystemStatus' attribute
	// TODO: handle 'alarm' attribute
	// TODO: handle 'shock' attribute
	// TODO: handle 'lqi' attribute
	// TODO: handle 'rssi' attribute
	// TODO: handle 'phraseSpoken' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'tvChannel' attribute
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'epEvent' attribute
	// TODO: handle 'epInfo' attribute
	// TODO: handle 'Amazon Alexa echo dot' attribute

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

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def execute() {
	log.debug "Executing 'execute'"
	// TODO: handle 'execute' command
}

def setFanSpeed() {
	log.debug "Executing 'setFanSpeed'"
	// TODO: handle 'setFanSpeed' command
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

def setGroupRole() {
	log.debug "Executing 'setGroupRole'"
	// TODO: handle 'setGroupRole' command
}

def makePrimary() {
	log.debug "Executing 'makePrimary'"
	// TODO: handle 'makePrimary' command
}

def makeAuxilary() {
	log.debug "Executing 'makeAuxilary'"
	// TODO: handle 'makeAuxilary' command
}

def ungroup() {
	log.debug "Executing 'ungroup'"
	// TODO: handle 'ungroup' command
}

def setPlaybackShuffle() {
	log.debug "Executing 'setPlaybackShuffle'"
	// TODO: handle 'setPlaybackShuffle' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
}

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
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

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
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

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}

def enableEpEvents() {
	log.debug "Executing 'enableEpEvents'"
	// TODO: handle 'enableEpEvents' command
}

def epCmd() {
	log.debug "Executing 'epCmd'"
	// TODO: handle 'epCmd' command
}

def Alexa() {
	log.debug "Executing 'Alexa'"
	// TODO: handle 'Alexa' command
}