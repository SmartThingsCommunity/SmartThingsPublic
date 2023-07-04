/**
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
 *  Sonos Player
 *
 *  Author: SmartThings
 *
 */

metadata {
	definition (name: "Sonos Player", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"

		attribute "model", "string"
		attribute "trackUri", "string"
		attribute "transportUri", "string"
		attribute "trackNumber", "string"

		command "subscribe"
		command "getVolume"
		command "getCurrentMedia"
		command "getCurrentStatus"
		command "seek"
		command "unsubscribe"
		command "setLocalLevel", ["number"]
		command "tileSetLevel", ["number"]
		command "playTrackAtVolume", ["string","number"]
		command "playTrackAndResume", ["string","number","number"]
		command "playTextAndResume", ["string","number"]
		command "playTrackAndRestore", ["string","number","number"]
		command "playTextAndRestore", ["string","number"]
		command "playSoundAndTrack", ["string","number","json_object","number"]
		command "playTextAndResume", ["string","json_object","number"]
	}

	// Main
	standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
		state "grouped", label:'Grouped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
	}

	// Row 1
	standardTile("nextTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
	}
	standardTile("play", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
		state "grouped", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
	}
	standardTile("previousTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
	}

	// Row 2
	standardTile("status", "device.status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#ffffff"
		state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "grouped", label:'Grouped', action:"", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
	}
	standardTile("pause", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", nextState:"paused", backgroundColor:"#ffffff"
		state "grouped", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", backgroundColor:"#ffffff"
	}
	standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
		state "unmuted", label:"", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
		state "muted", label:"", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
	}

	// Row 3
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
		state "level", action:"tileSetLevel", backgroundColor:"#ffffff"
	}

	// Row 4
	valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:1, width:3, decoration: "flat") {
		state "default", label:'${currentValue}', backgroundColor:"#ffffff"
	}

	// Row 5
	standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
	}
	standardTile("model", "device.model", width: 1, height: 1, decoration: "flat") {
		state "Sonos PLAY:1", label:'', action:"", icon:"st.sonos.sonos-play1", backgroundColor:"#ffffff"
		state "Sonos PLAY:2", label:'', action:"", icon:"st.sonos.sonos-play2", backgroundColor:"#ffffff"
		state "Sonos PLAY:3", label:'', action:"", icon:"st.sonos.sonos-play3", backgroundColor:"#ffffff"
		state "Sonos PLAY:5", label:'', action:"", icon:"st.sonos.sonos-play5", backgroundColor:"#ffffff"
		state "Sonos CONNECT:AMP", label:'', action:"", icon:"st.sonos.sonos-connect-amp", backgroundColor:"#ffffff"
		state "Sonos CONNECT", label:'', action:"", icon:"st.sonos.sonos-connect", backgroundColor:"#ffffff"
		state "Sonos PLAYBAR", label:'', action:"", icon:"st.sonos.sonos-playbar", backgroundColor:"#ffffff"
	}
	standardTile("unsubscribe", "device.status", width: 1, height: 1, decoration: "flat") {
		state "previous", label:'Unsubscribe', action:"unsubscribe", backgroundColor:"#ffffff"
	}

	main "main"

	details([
		"previousTrack","play","nextTrack",
		"status","pause","mute",
		"levelSliderControl",
		"currentSong",
		"refresh","model"
		//,"unsubscribe"
	])
}

// parse events into attributes
def parse(description) {
	log.trace "parse('$description')"
	def results = []
	try {

		def msg = parseLanMessage(description)
        log.info "requestId = $msg.requestId"
		if (msg.headers)
		{
			def hdr = msg.header.split('\n')[0]
			if (hdr.size() > 36) {
				hdr = hdr[0..35] + "..."
			}

			def uuid = ""
			def sid = ""
			if (msg.headers["SID"])
			{
				sid = msg.headers["SID"]
				sid -= "uuid:"
				sid = sid.trim()

				def pos = sid.lastIndexOf("_")
				if (pos > 0) {
					uuid = sid[0..pos-1]
					log.trace "uuid; $uuid"
				}
			}

			log.trace "${hdr} ${description.size()} bytes, body = ${msg.body?.size() ?: 0} bytes, sid = ${sid}"

			if (!msg.body) {
				if (sid) {
					updateSid(sid)
				}
			}
			else if (msg.xml) {
				log.trace "has XML body"

				// Process response to getVolume()
				def node = msg.xml.Body.GetVolumeResponse
				if (node.size()) {
					log.trace "Extracting current volume"
					sendEvent(name: "level",value: node.CurrentVolume.text())
				}

				// Process response to getCurrentStatus()
				node = msg.xml.Body.GetTransportInfoResponse
				if (node.size()) {
					log.trace "Extracting current status"
					def currentStatus = statusText(node.CurrentTransportState.text())
					if (currentStatus) {
						if (currentStatus != "TRANSITIONING") {
							def coordinator = device.getDataValue('coordinator')
							log.trace "Current status: '$currentStatus', coordinator: '${coordinator}'"
							updateDataValue('currentStatus', currentStatus)
							log.trace "Updated saved status to '$currentStatus'"

							if (coordinator) {
								sendEvent(name: "status", value: "grouped", data: [source: 'xml.Body.GetTransportInfoResponse'])
								sendEvent(name: "switch", value: "off", displayed: false)
							}
							else {
								log.trace "status = $currentStatus"
								sendEvent(name: "status", value: currentStatus, data: [source: 'xml.Body.GetTransportInfoResponse'])
								sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)
							}
						}
					}
				}

				// Process group change
				node = msg.xml.property.ZoneGroupState
				if (node.size()) {
					log.trace "Extracting group status"
					// Important to use parser rather than slurper for this version of Groovy
					def xml1 = new XmlParser().parseText(node.text())
					log.trace "Parsed group xml"
					def myNode =  xml1.ZoneGroup.ZoneGroupMember.find {it.'@UUID' == uuid}
					log.trace "myNode: ${myNode}"
                    
                    // TODO - myNode is often false and throwing 1000 exceptions/hour, find out why
                    // https://smartthings.atlassian.net/browse/DVCSMP-793
					def myCoordinator = myNode?.parent()?.'@Coordinator'

					log.trace "player: ${myNode?.'@UUID'}, coordinator: ${myCoordinator}"
					if (myCoordinator && myCoordinator != myNode.'@UUID') {
						// this player is grouped, find the coordinator

						def coordinator = xml1.ZoneGroup.ZoneGroupMember.find {it.'@UUID' == myCoordinator}
						def coordinatorDni = dniFromUri(coordinator.'@Location')
						log.trace "Player has a coordinator: $coordinatorDni"

						updateDataValue("coordinator", coordinatorDni)
						updateDataValue("isGroupCoordinator", "")

						def coordinatorDevice = parent.getChildDevice(coordinatorDni)
                        
                        // TODO - coordinatorDevice is also sometimes coming up null
                        if (coordinatorDevice) {
                            sendEvent(name: "trackDescription", value: "[Grouped with ${coordinatorDevice.displayName}]")
                            sendEvent(name: "status", value: "grouped", data: [
                                coordinator: [displayName: coordinatorDevice.displayName, id: coordinatorDevice.id, deviceNetworkId: coordinatorDevice.deviceNetworkId]
                            ])
                            sendEvent(name: "switch", value: "off", displayed: false)
                        }
                        else {
                        	log.warn "Could not find child device for coordinator 'coordinatorDni', devices: ${parent.childDevices*.deviceNetworkId}"
                        }
					}
					else if (myNode) {
						// Not grouped
						updateDataValue("coordinator", "")
						updateDataValue("isGroupCoordinator", myNode.parent().ZoneGroupMember.size() > 1 ? "true" : "")
					}
					// Return a command to read the current status again to take care of status and group events arriving at
					// about the same time, but in the reverse order
					results << getCurrentStatus()
				}

				// Process subscription update
				node = msg.xml.property.LastChange
				if (node.size()) {
					def xml1 = parseXml(node.text())

					// Play/pause status
					def currentStatus = statusText(xml1.InstanceID.TransportState.'@val'.text())
					if (currentStatus) {
						if (currentStatus != "TRANSITIONING") {
							def coordinator = device.getDataValue('coordinator')
							log.trace "Current status: '$currentStatus', coordinator: '${coordinator}'"
							updateDataValue('currentStatus', currentStatus)
							log.trace "Updated saved status to '$currentStatus'"

							if (coordinator) {
								sendEvent(name: "status", value: "grouped", data: [source: 'xml.property.LastChange.InstanceID.TransportState'])
								sendEvent(name: "switch", value: "off", displayed: false)
							}
							else {
								log.trace "status = $currentStatus"
								sendEvent(name: "status", value: currentStatus, data: [source: 'xml.property.LastChange.InstanceID.TransportState'])
								sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)
							}
						}
					}

					// Volume level
					def currentLevel = xml1.InstanceID.Volume.find{it.'@channel' == 'Master'}.'@val'.text()
					if (currentLevel) {
						log.trace "Has volume: '$currentLevel'"
						sendEvent(name: "level", value: currentLevel, description: description)
					}

					// Mute status
					def currentMute = xml1.InstanceID.Mute.find{it.'@channel' == 'Master'}.'@val'.text()
					if (currentMute) {
						def value = currentMute == "1" ? "muted" : "unmuted"
						log.trace "Has mute: '$currentMute', value: '$value"
						sendEvent(name: "mute", value: value, descriptionText: "$device.displayName is $value")
					}

					// Track data
					def trackUri = xml1.InstanceID.CurrentTrackURI.'@val'.text()
					def transportUri = xml1.InstanceID.AVTransportURI.'@val'.text()
					def enqueuedUri = xml1.InstanceID.EnqueuedTransportURI.'@val'.text()
					def trackNumber = xml1.InstanceID.CurrentTrack.'@val'.text()

					if (trackUri.contains("//s3.amazonaws.com/smartapp-")) {
						log.trace "Skipping event generation for sound file $trackUri"
					}
					else {
						def trackMeta = xml1.InstanceID.CurrentTrackMetaData.'@val'.text()
						def transportMeta = xml1.InstanceID.AVTransportURIMetaData.'@val'.text()
						def enqueuedMeta = xml1.InstanceID.EnqueuedTransportURIMetaData.'@val'.text()

						if (trackMeta || transportMeta) {
							def isRadioStation = enqueuedUri.startsWith("x-sonosapi-stream:")

							// Use enqueued metadata, if available, otherwise transport metadata, for station ID
							def metaData = enqueuedMeta ? enqueuedMeta :  transportMeta
							def stationMetaXml = metaData ? parseXml(metaData) : null

							// Use the track metadata for song ID unless it's a radio station
							def trackXml = (trackMeta && !isRadioStation) || !stationMetaXml ? parseXml(trackMeta) : stationMetaXml

							// Song properties
							def currentName = trackXml.item.title.text()
							def currentArtist = trackXml.item.creator.text()
							def currentAlbum  = trackXml.item.album.text()
							def currentTrackDescription = currentName
							def descriptionText = "$device.displayName is playing $currentTrackDescription"
							if (currentArtist) {
								currentTrackDescription += " - $currentArtist"
								descriptionText += " by $currentArtist"
							}

							// Track Description Event
							log.info descriptionText
							sendEvent(name: "trackDescription",
								value: currentTrackDescription,
								descriptionText: descriptionText
							)

							// Have seen cases where there is no engueued or transport metadata. Haven't figured out how to resume in that case
							// so not creating a track data event.
							//
							if (stationMetaXml) {
								// Track Data Event
								// Use track description for the data event description unless it is a queued song (to support resumption & use in mood music)
								def station = (transportUri?.startsWith("x-rincon-queue:") || enqueuedUri?.contains("savedqueues")) ? currentName : stationMetaXml.item.title.text()

								def uri = enqueuedUri ?: transportUri
								def previousState = device.currentState("trackData")?.jsonValue
								def isDataStateChange = !previousState || (previousState.station != station || previousState.metaData != metaData)

								if (transportUri?.startsWith("x-rincon-queue:")) {
									updateDataValue("queueUri", transportUri)
								}

								def trackDataValue = [
									station: station,
									name: currentName,
									artist: currentArtist,
									album: currentAlbum,
									trackNumber: trackNumber,
									status: currentStatus,
									level: currentLevel,
									uri: uri,
									trackUri: trackUri,
									transportUri: transportUri,
									enqueuedUri: enqueuedUri,
									metaData: metaData,
								]

								if (trackMeta != metaData) {
									trackDataValue.trackMetaData = trackMeta
								}

								results << createEvent(name: "trackData",
									value: trackDataValue.encodeAsJSON(),
									descriptionText: currentDescription,
									displayed: false,
									isStateChange: isDataStateChange
								)
							}
						}
					}
				}
				if (!results) {
					def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('\n\n','\n').encodeAsHTML() : ""
					results << createEvent(
						name: "sonosMessage",
						value: "${msg.body.encodeAsMD5()}",
						description: description,
						descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
						data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
						isStateChange: false, displayed: false)
				}
			}
			else {
				log.warn "Body not XML"
				def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
					.replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
					.replaceAll('\n\n','\n').encodeAsHTML() : ""
				results << createEvent(
					name: "unknownMessage",
					value: "${msg.body.encodeAsMD5()}",
					description: description,
					descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
					data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
					isStateChange: true, displayed: true)
			}
		}
		//log.trace "/parse()"
	}
	catch (Throwable t) {
		//results << createEvent(name: "parseError", value: "$t")
		sendEvent(name: "parseError", value: "$t", description: description)
		throw t
	}
	results
}

def installed() {
	def result = [delayAction(5000)]
	result << refresh()
	result.flatten()
}

def on(){
	play()
}

def off(){
	stop()
}

def setModel(String model)
{
	log.trace "setModel to $model"
	sendEvent(name:"model",value:model,isStateChange:true)
}

def refresh() {
	log.trace "refresh()"
	def result = subscribe()
	result << getCurrentStatus()
	result << getVolume()
	result.flatten()
}

// For use by apps, sets all levels if sent to a non-coordinator in a group
def setLevel(val)
{
	log.trace "setLevel($val)"
	coordinate({
		setOtherLevels(val)
		setLocalLevel(val)
	}, {
		it.setLevel(val)
	})
}

// For use by tiles, sets all levels if a coordinator, otherwise sets only the local one
def tileSetLevel(val)
{
	log.trace "tileSetLevel($val)"
	coordinate({
		setOtherLevels(val)
		setLocalLevel(val)
	}, {
		setLocalLevel(val)
	})
}

// Always sets only this level
def setLocalLevel(val, delay=0) {
	log.trace "setLocalLevel($val)"
	def v = Math.max(Math.min(Math.round(val), 100), 0)
	log.trace "volume = $v"

	def result = []
	if (delay) {
		result << delayAction(delay)
	}
	result << sonosAction("SetVolume", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master", DesiredVolume: v])
	//result << delayAction(500),
	result << sonosAction("GetVolume", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master"])
	result
}

private setOtherLevels(val, delay=0) {
	log.trace "setOtherLevels($val)"
	if (device.getDataValue('isGroupCoordinator')) {
		log.trace "setting levels of coordinated players"
		def previousMaster = device.currentState("level")?.integerValue
		parent.getChildDevices().each {child ->
			if (child.getDeviceDataByName("coordinator") == device.deviceNetworkId) {
				def newLevel = childLevel(previousMaster, val, child.currentState("level")?.integerValue)
				log.trace "Setting level of $child.displayName to $newLevel"
				child.setLocalLevel(newLevel, delay)
			}
		}
	}
	log.trace "/setOtherLevels()"
}

private childLevel(previousMaster, newMaster, previousChild)
{
	if (previousMaster) {
		if (previousChild) {
			Math.round(previousChild * (newMaster / previousMaster))
		}
		else {
			newMaster
		}
	}
	else {
		newMaster
	}
}

def getGroupStatus() {
	def result = coordinate({device.currentValue("status")}, {it.currentValue("status")})
	log.trace "getGroupStatus(), result=$result"
	result
}

def play() {
	log.trace "play()"
	coordinate({sonosAction("Play")}, {it.play()})
}

def stop() {
	log.trace "stop()"
	coordinate({sonosAction("Stop")}, {it.stop()})
}

def pause() {
	log.trace "pause()"
	coordinate({sonosAction("Pause")}, {it.pause()})
}

def nextTrack() {
	log.trace "nextTrack()"
	coordinate({sonosAction("Next")}, {it.nextTrack()})
}

def previousTrack() {
	log.trace "previousTrack()"
	coordinate({sonosAction("Previous")}, {it.previousTrack()})
}

def seek(trackNumber) {
	log.trace "seek($trackNumber)"
	coordinate({sonosAction("Seek", "AVTransport", "/MediaRenderer/AVTransport/Control", [InstanceID: 0, Unit: "TRACK_NR", Target: trackNumber])}, {it.seek(trackNumber)})
}

def mute()
{
	log.trace "mute($m)"
	// TODO - handle like volume?
	//coordinate({sonosAction("SetMute", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master", DesiredMute: 1])}, {it.mute()})
	sonosAction("SetMute", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master", DesiredMute: 1])
}

def unmute()
{
	log.trace "mute($m)"
	// TODO - handle like volume?
	//coordinate({sonosAction("SetMute", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master", DesiredMute: 0])}, {it.unmute()})
	sonosAction("SetMute", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master", DesiredMute: 0])
}

def setPlayMode(mode)
{
	log.trace "setPlayMode($mode)"
	coordinate({sonosAction("SetPlayMode", [InstanceID: 0, NewPlayMode: mode])}, {it.setPlayMode(mode)})
}

def playTextAndResume(text, volume=null)
{
	log.debug "playTextAndResume($text, $volume)"
	coordinate({
		def sound = textToSpeech(text)
		playTrackAndResume(sound.uri, (sound.duration as Integer) + 1, volume)
	}, {it.playTextAndResume(text, volume)})
}

def playTrackAndResume(uri, duration, volume=null) {
	log.debug "playTrackAndResume($uri, $duration, $volume)"
	coordinate({
		def currentTrack = device.currentState("trackData")?.jsonValue
		def currentVolume = device.currentState("level")?.integerValue
		def currentStatus = device.currentValue("status")
		def level = volume as Integer

		def result = []
		if (level) {
			log.trace "Stopping and setting level to $volume"
			result << sonosAction("Stop")
			result << setLocalLevel(level)
		}

		log.trace "Setting sound track: ${uri}"
		result << setTrack(uri)
		result << sonosAction("Play")

		if (currentTrack) {
			def delayTime = ((duration as Integer) * 1000)+3000
			if (level) {
				delayTime += 1000
			}
			result << delayAction(delayTime)
			log.trace "Delaying $delayTime ms before resumption"
			if (level) {
				log.trace "Restoring volume to $currentVolume"
				result << sonosAction("Stop")
				result << setLocalLevel(currentVolume)
			}
			log.trace "Restoring track $currentTrack.uri"
			result << setTrack(currentTrack)
			if (currentStatus == "playing") {
				result << sonosAction("Play")
			}
		}

		result = result.flatten()
		log.trace "Returning ${result.size()} commands"
		result
	}, {it.playTrackAndResume(uri, duration, volume)})
}

def playTextAndRestore(text, volume=null)
{
	log.debug "playTextAndResume($text, $volume)"
	coordinate({
		def sound = textToSpeech(text)
		playTrackAndRestore(sound.uri, (sound.duration as Integer) + 1, volume)
	}, {it.playTextAndRestore(text, volume)})
}

def playTrackAndRestore(uri, duration, volume=null) {
	log.debug "playTrackAndRestore($uri, $duration, $volume)"
	coordinate({
		def currentTrack = device.currentState("trackData")?.jsonValue
		def currentVolume = device.currentState("level")?.integerValue
		def currentStatus = device.currentValue("status")
		def level = volume as Integer

		def result = []
		if (level) {
			log.trace "Stopping and setting level to $volume"
			result << sonosAction("Stop")
			result << setLocalLevel(level)
		}

		log.trace "Setting sound track: ${uri}"
		result << setTrack(uri)
		result << sonosAction("Play")

		if (currentTrack) {
			def delayTime = ((duration as Integer) * 1000)+3000
			if (level) {
				delayTime += 1000
			}
			result << delayAction(delayTime)
			log.trace "Delaying $delayTime ms before restoration"
			if (level) {
				log.trace "Restoring volume to $currentVolume"
				result << sonosAction("Stop")
				result << setLocalLevel(currentVolume)
			}
			log.trace "Restoring track $currentTrack.uri"
			result << setTrack(currentTrack)
		}

		result = result.flatten()
		log.trace "Returning ${result.size()} commands"
		result
	}, {it.playTrackAndResume(uri, duration, volume)})
}

def playTextAndTrack(text, trackData, volume=null)
{
	log.debug "playTextAndTrack($text, $trackData, $volume)"
	coordinate({
		def sound = textToSpeech(text)
		playSoundAndTrack(sound.uri, (sound.duration as Integer) + 1, trackData, volume)
	}, {it.playTextAndResume(text, volume)})
}

def playSoundAndTrack(soundUri, duration, trackData, volume=null) {
	log.debug "playSoundAndTrack($soundUri, $duration, $trackUri, $volume)"
	coordinate({
		def level = volume as Integer
		def result = []
		if (level) {
			log.trace "Stopping and setting level to $volume"
			result << sonosAction("Stop")
			result << setLocalLevel(level)
		}

		log.trace "Setting sound track: ${soundUri}"
		result << setTrack(soundUri)
		result << sonosAction("Play")

		def delayTime = ((duration as Integer) * 1000)+3000
		result << delayAction(delayTime)
		log.trace "Delaying $delayTime ms before resumption"

		log.trace "Setting track $trackData"
		result << setTrack(trackData)
		result << sonosAction("Play")

		result = result.flatten()
		log.trace "Returning ${result.size()} commands"
		result
	}, {it.playTrackAndResume(uri, duration, volume)})
}

def playTrackAtVolume(String uri, volume) {
	log.trace "playTrack()"
	coordinate({
		def result = []
		result << sonosAction("Stop")
		result << setLocalLevel(volume as Integer)
		result << setTrack(uri, metaData)
		result << sonosAction("Play")
		result.flatten()
	}, {it.playTrack(uri, metaData)})
}

def playTrack(String uri, metaData="") {
	log.trace "playTrack()"
	coordinate({
		def result = setTrack(uri, metaData)
		result << sonosAction("Play")
		result.flatten()
	}, {it.playTrack(uri, metaData)})
}

def playTrack(Map trackData) {
	log.trace "playTrack(Map)"
	coordinate({
		def result = setTrack(trackData)
		//result << delayAction(1000)
		result << sonosAction("Play")
		result.flatten()
	}, {it.playTrack(trackData)})
}

def setTrack(Map trackData) {
	log.trace "setTrack($trackData.uri, ${trackData.metaData?.size()} B)"
	coordinate({
		def data = trackData
		def result = []
		if ((data.transportUri.startsWith("x-rincon-queue:") || data.enqueuedUri.contains("savedqueues")) && data.trackNumber != null) {
			// TODO - Clear queue?
			def uri = device.getDataValue('queueUri')
			result << sonosAction("RemoveAllTracksFromQueue", [InstanceID: 0])
			//result << delayAction(500)
			result << sonosAction("AddURIToQueue", [InstanceID: 0, EnqueuedURI: data.uri, EnqueuedURIMetaData: data.metaData, DesiredFirstTrackNumberEnqueued: 0, EnqueueAsNext: 1])
			//result << delayAction(500)
			result << sonosAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: uri, CurrentURIMetaData: metaData])
			//result << delayAction(500)
			result << sonosAction("Seek", "AVTransport", "/MediaRenderer/AVTransport/Control", [InstanceID: 0, Unit: "TRACK_NR", Target: data.trackNumber])
		} else {
			result = setTrack(data.uri, data.metaData)
		}
		result.flatten()
	}, {it.setTrack(trackData)})
}

def setTrack(String uri, metaData="")
{
	log.info "setTrack($uri, $trackNumber, ${metaData?.size()} B})"
	coordinate({
		def result = []
		result << sonosAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: uri, CurrentURIMetaData: metaData])
		result
	}, {it.setTrack(uri, metaData)})
}

def resumeTrack(Map trackData = null) {
	log.trace "resumeTrack()"
	coordinate({
		def result = restoreTrack(trackData)
		//result << delayAction(500)
		result << sonosAction("Play")
		result
	}, {it.resumeTrack(trackData)})
}

def restoreTrack(Map trackData = null) {
	log.trace "restoreTrack(${trackData?.uri})"
	coordinate({
		def result = []
		def data = trackData
		if (!data) {
			data = device.currentState("trackData")?.jsonValue
		}
		if (data) {
			if ((data.transportUri.startsWith("x-rincon-queue:") || data.enqueuedUri.contains("savedqueues")) && data.trackNumber != null) {
				def uri = device.getDataValue('queueUri')
				log.trace "Restoring queue position $data.trackNumber of $data.uri"
				result << sonosAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: uri, CurrentURIMetaData: data.metaData])
				//result << delayAction(500)
				result << sonosAction("Seek", "AVTransport", "/MediaRenderer/AVTransport/Control", [InstanceID: 0, Unit: "TRACK_NR", Target: data.trackNumber])
			} else {
				log.trace "Setting track to $data.uri"
				//setTrack(data.uri, null, data.metaData)
				result << sonosAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: data.uri, CurrentURIMetaData: data.metaData])
			}
		}
		else {
			log.warn "Previous track data not found"
		}
		result
	}, {it.restoreTrack(trackData)})
}

def playText(String msg) {
	coordinate({
		def result = setText(msg)
		result << sonosAction("Play")
	}, {it.playText(msg)})
}

def setText(String msg) {
	log.trace "setText($msg)"
	coordinate({
		def sound = textToSpeech(msg)
		setTrack(sound.uri)
	}, {it.setText(msg)})
}

// Custom commands

def subscribe() {
	log.trace "subscribe()"
	def result = []
	result << subscribeAction("/MediaRenderer/AVTransport/Event")
	result << delayAction(10000)
	result << subscribeAction("/MediaRenderer/RenderingControl/Event")
	result << delayAction(20000)
	result << subscribeAction("/ZoneGroupTopology/Event")

	result
}

def unsubscribe() {
	log.trace "unsubscribe()"
	def result = [
		unsubscribeAction("/MediaRenderer/AVTransport/Event", device.getDataValue('subscriptionId')),
		unsubscribeAction("/MediaRenderer/RenderingControl/Event", device.getDataValue('subscriptionId')),
		unsubscribeAction("/ZoneGroupTopology/Event", device.getDataValue('subscriptionId')),
		
		unsubscribeAction("/MediaRenderer/AVTransport/Event", device.getDataValue('subscriptionId1')),
		unsubscribeAction("/MediaRenderer/RenderingControl/Event", device.getDataValue('subscriptionId1')),
		unsubscribeAction("/ZoneGroupTopology/Event", device.getDataValue('subscriptionId1')),
		
		unsubscribeAction("/MediaRenderer/AVTransport/Event", device.getDataValue('subscriptionId2')),
		unsubscribeAction("/MediaRenderer/RenderingControl/Event", device.getDataValue('subscriptionId2')),
		unsubscribeAction("/ZoneGroupTopology/Event", device.getDataValue('subscriptionId2'))
	]
	updateDataValue("subscriptionId", "")
	updateDataValue("subscriptionId1", "")
	updateDataValue("subscriptionId2", "")
	result
}

def getVolume()
{
	log.trace "getVolume()"
	sonosAction("GetVolume", "RenderingControl", "/MediaRenderer/RenderingControl/Control", [InstanceID: 0, Channel: "Master"])
}

def getCurrentMedia()
{
	log.trace "getCurrentMedia()"
	sonosAction("GetPositionInfo", [InstanceID:0, Channel: "Master"])
}

def getCurrentStatus() //transport info
{
	log.trace "getCurrentStatus()"
	sonosAction("GetTransportInfo", [InstanceID:0])
}

def getSystemString()
{
	log.trace "getSystemString()"
	sonosAction("GetString", "SystemProperties", "/SystemProperties/Control", [VariableName: "UMTracking"])
}

private messageFilename(String msg) {
	msg.toLowerCase().replaceAll(/[^a-zA-Z0-9]+/,'_')
}

private getCallBackAddress()
{
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private sonosAction(String action) {
	sonosAction(action, "AVTransport", "/MediaRenderer/AVTransport/Control", [InstanceID:0, Speed:1])
}

private sonosAction(String action, Map body) {
	sonosAction(action, "AVTransport", "/MediaRenderer/AVTransport/Control", body)
}

private sonosAction(String action, String service, String path, Map body = [InstanceID:0, Speed:1]) {
	log.trace "sonosAction($action, $service, $path, $body)"
	def result = new physicalgraph.device.HubSoapAction(
		path:    path ?: "/MediaRenderer/$service/Control",
		urn:     "urn:schemas-upnp-org:service:$service:1",
		action:  action,
		body:    body,
		headers: [Host:getHostAddress(), CONNECTION: "close"]
	)

	//log.trace "\n${result.action.encodeAsHTML()}"
	log.debug "sonosAction: $result.requestId"
	result
}

private subscribeAction(path, callbackPath="") {
	log.trace "subscribe($path, $callbackPath)"
	def address = getCallBackAddress()
	def ip = getHostAddress()

	def result = new physicalgraph.device.HubAction(
		method: "SUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			CALLBACK: "<http://${address}/notify$callbackPath>",
			NT: "upnp:event",
			TIMEOUT: "Second-28800"])

	log.trace "SUBSCRIBE $path"
	//log.trace "\n${result.action.encodeAsHTML()}"
	result
}

private unsubscribeAction(path, sid) {
	log.trace "unsubscribe($path, $sid)"
	def ip = getHostAddress()
	def result = new physicalgraph.device.HubAction(
		method: "UNSUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			SID: "uuid:${sid}"])

	log.trace "UNSUBSCRIBE $path"
	//log.trace "\n${result.action.encodeAsHTML()}"
	result
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private statusText(s) {
	switch(s) {
		case "PLAYING":
			return "playing"
		case "PAUSED_PLAYBACK":
			return "paused"
		case "STOPPED":
			return "stopped"
		default:
			return s
	}
}

private updateSid(sid) {
	if (sid) {
		def sid0 = device.getDataValue('subscriptionId')
		def sid1 = device.getDataValue('subscriptionId1')
		def sid2 = device.getDataValue('subscriptionId2')
		def sidNumber = device.getDataValue('sidNumber') ?: "0"

		log.trace "updateSid($sid), sid0=$sid0, sid1=$sid1, sid2=$sid2, sidNumber=$sidNumber"
		if (sidNumber == "0") {
			if (sid != sid1 && sid != sid2) {
				updateDataValue("subscriptionId", sid)
				updateDataValue("sidNumber", "1")
			}
		}
		else if (sidNumber == "1") {
			if (sid != sid0 && sid != sid2) {
				updateDataValue("subscriptionId1", sid)
				updateDataValue("sidNumber", "2")
			}
		}
		else {
			if (sid != sid0 && sid != sid0) {
				updateDataValue("subscriptionId2", sid)
				updateDataValue("sidNumber", "0")
			}
		}
	}
}

private dniFromUri(uri) {
	def segs = uri.replaceAll(/http:\/\/([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+)\/.+/,'$1').split(":")
	def nums = segs[0].split("\\.")
	(nums.collect{hex(it.toInteger())}.join('') + ':' + hex(segs[-1].toInteger(),4)).toUpperCase()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private coordinate(closure1, closure2) {
	def coordinator = device.getDataValue('coordinator')
	if (coordinator) {
		closure2(parent.getChildDevice(coordinator))
	}
	else {
		closure1()
	}
}

