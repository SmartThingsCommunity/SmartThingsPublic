/**
 *  VLC Things. A SmartThings device handler for the VLC media player.
 *
 *  For more information, please visit
 *  <https://github.com/statusbits/smartthings/tree/master/VlcThing.md/>
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright © 2014 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  Version 2.0.0 (12/22/2016)
 */

import groovy.json.JsonSlurper

preferences {
    // NOTE: Android client does not accept "defaultValue" attribute!
    input("confIpAddr", "string", title:"VLC IP Address",
        required:true, displayDuringSetup:true)
    input("confTcpPort", "number", title:"VLC TCP Port",
        required:true, displayDuringSetup:true)
    input("confPassword", "password", title:"VLC Password",
        required:false, displayDuringSetup:true)
}

metadata {
    definition (name:"VLC Thing", namespace:"statusbits", author:"geko@statusbits.com") {
        capability "Actuator"
        capability "Switch"
        capability "Music Player"
        capability "Speech Synthesis"
        capability "Refresh"
        capability "Polling"

        // Custom attributes
        attribute "connection", "string"    // Connection status string

        // Custom commands
        command "enqueue", ["string"]
        command "seek", ["number"]
        command "playTrackAndResume", ["string","number","number"]
        command "playTrackAndRestore", ["string","number","number"]
        command "playTextAndResume", ["string","number"]
        command "playTextAndRestore", ["string","number"]
        command "playSoundAndTrack", ["string","number","json_object","number"]
        command "testTTS"
    }

    tiles(scale:2) {
		multiAttributeTile(name:"mediaplayer", type:"mediaPlayer", width:6, height:4) {
			tileAttribute("device.status", key:"PRIMARY_CONTROL") {
				attributeState("stopped", label:"Stopped", defaultState:true)
				attributeState("playing", label:"Playing")
				attributeState("paused", label:"Paused",)
			}
			tileAttribute("device.status", key:"MEDIA_STATUS") {
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState:"playing")
				attributeState("playing", label:"Playing", action:"music Player.pause", nextState:"paused")
				attributeState("paused", label:"Paused", action:"music Player.play", nextState:"playing")
			}
			tileAttribute("device.status", key:"PREVIOUS_TRACK") {
				attributeState("status", action:"music Player.previousTrack", defaultState:true)
			}
			tileAttribute("device.status", key:"NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState:true)
			}
			tileAttribute ("device.level", key:"SLIDER_CONTROL") {
				attributeState("level", action:"music Player.setLevel")
			}
			tileAttribute ("device.mute", key:"MEDIA_MUTED") {
				attributeState("unmuted", action:"music Player.mute", nextState:"muted", defaultState:true)
				attributeState("muted", action:"music Player.unmute", nextState:"unmuted")
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("trackDescription", label:"${currentValue}", defaultState:true)
			}
		}

        standardTile("status", "device.status", width:2, height:2, canChangeIcon:true) {
            state "stopped", label:'Stopped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff", action:"Music Player.play", nextState:"playing"
            state "paused", label:'Paused', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff", action:"Music Player.play", nextState:"playing"
            state "playing", label:'Playing', icon:"st.Electronics.electronics16", backgroundColor:"#79b821", action:"Music Player.pause", nextState:"paused"
        }

        standardTile("refresh", "device.connection", width:2, height:2, inactiveLabel:false, decoration:"flat") {
            state "default", icon:"st.secondary.refresh", backgroundColor:"#FFFFFF", action:"refresh.refresh", defaultState:true
            state "connected", icon:"st.secondary.refresh", backgroundColor:"#44b621", action:"refresh.refresh"
            state "disconnected", icon:"st.secondary.refresh", backgroundColor:"#ea5462", action:"refresh.refresh"
        }

        standardTile("testTTS", "device.status", width:2, height:2, inactiveLabel:false, decoration:"flat") {
            state "default", label:"Test", icon:"http://statusbits.github.io/icons/vlcthing.png", action:"testTTS"
        }

        main("status")
        details(["mediaplayer", "refresh", "testTTS"])
    }

    simulator {
        status "Stoped"         : "simulator:true, state:'stopped'"
        status "Playing"        : "simulator:true, state:'playing'"
        status "Paused"         : "simulator:true, state:'paused'"
        status "Volume 0%"      : "simulator:true, volume:0"
        status "Volume 25%"     : "simulator:true, volume:127"
        status "Volume 50%"     : "simulator:true, volume:255"
        status "Volume 75%"     : "simulator:true, volume:383"
        status "Volume 100%"    : "simulator:true, volume:511"
    }
}

def installed() {
    //log.debug "installed()"
    log.info title()

    // Initialize attributes to default values (Issue #18)
    sendEvent([name:'status', value:'stopped', displayed:false])
    sendEvent([name:'level', value:'0', displayed:false])
    sendEvent([name:'mute', value:'unmuted', displayed:false])
    sendEvent([name:'trackDescription', value:'', displayed:false])
    sendEvent([name:'connection', value:'disconnected', displayed:false])
}

def updated() {
	//log.debug "updated with settings: ${settings}"
    log.info title()

    unschedule()

    if (!settings.confIpAddr) {
	    log.warn "IP address is not set!"
        return
    }

    def port = settings.confTcpPort
    if (!port) {
	    log.warn "Using default TCP port 8080!"
        port = 8080
    }

    def dni = createDNI(settings.confIpAddr, port)
    device.deviceNetworkId = dni
    state.dni = dni
    state.hostAddress = "${settings.confIpAddr}:${settings.confTcpPort}"
    state.requestTime = 0
    state.responseTime = 0
    state.updatedTime = 0
    state.lastPoll = 0

    if (settings.confPassword) {
        state.userAuth = ":${settings.confPassword}".bytes.encodeBase64() as String
    } else {
        state.userAuth = null
    }

    startPollingTask()
    //STATE()
}

def pollingTask() {
    //log.debug "pollingTask()"

    state.lastPoll = now()

    // Check connection status
    def requestTime = state.requestTime ?: 0
    def responseTime = state.responseTime ?: 0
    if (requestTime && (requestTime - responseTime) > 10000) {
        log.warn "No connection!"
        sendEvent([
            name:           'connection',
            value:          'disconnected',
            isStateChange:  true,
            displayed:      true
        ])
    }

    def updated = state.updatedTime ?: 0
    if ((now() - updated) > 10000) {
        sendHubCommand(apiGetStatus())
    }
}

def parse(String message) {
    def msg = stringToMap(message)
    if (msg.containsKey("simulator")) {
        // simulator input
        return parseHttpResponse(msg)
    }

    if (!msg.containsKey("headers")) {
        log.error "No HTTP headers found in '${message}'"
        return null
    }

    // parse HTTP response headers
    def headers = new String(msg.headers.decodeBase64())
    def parsedHeaders = parseHttpHeaders(headers)
    //log.debug "parsedHeaders: ${parsedHeaders}"
    if (parsedHeaders.status != 200) {
        log.error "Server error: ${parsedHeaders.reason}"
        return null
    }

    // parse HTTP response body
    if (!msg.body) {
        log.error "No HTTP body found in '${message}'"
        return null
    }

    def body = new String(msg.body.decodeBase64())
    //log.debug "body: ${body}"
    def slurper = new JsonSlurper()
    return parseHttpResponse(slurper.parseText(body))
}

// switch.on
def on() {
    play()
}

// switch.off
def off() {
    stop()
}

// MusicPlayer.play
def play() {
    //log.debug "play()"

    def command
    if (device.currentValue('status') == 'paused') {
        command = 'command=pl_forceresume'
    } else {
        command = 'command=pl_play'
    }

    return apiCommand(command, 500)
}

// MusicPlayer.stop
def stop() {
    //log.debug "stop()"
    return apiCommand("command=pl_stop", 500)
}

// MusicPlayer.pause
def pause() {
    //log.debug "pause()"
    return apiCommand("command=pl_forcepause")
}

// MusicPlayer.playTrack
def playTrack(uri) {
    //log.debug "playTrack(${uri})"
    def command = "command=in_play&input=" + URLEncoder.encode(uri, "UTF-8")
    return apiCommand(command, 500)
}

// MusicPlayer.playText
def playText(text) {
    //log.debug "playText(${text})"
    def sound = myTextToSpeech(text)
    return playTrack(sound.uri)
}

// MusicPlayer.setTrack
def setTrack(name) {
    log.warn "setTrack(${name}) not implemented"
    return null
}

// MusicPlayer.resumeTrack
def resumeTrack(name) {
    log.warn "resumeTrack(${name}) not implemented"
    return null
}

// MusicPlayer.restoreTrack
def restoreTrack(name) {
    log.warn "restoreTrack(${name}) not implemented"
    return null
}

// MusicPlayer.nextTrack
def nextTrack() {
    //log.debug "nextTrack()"
    return apiCommand("command=pl_next", 500)
}

// MusicPlayer.previousTrack
def previousTrack() {
    //log.debug "previousTrack()"
    return apiCommand("command=pl_previous", 500)
}

// MusicPlayer.setLevel
def setLevel(number) {
    //log.debug "setLevel(${number})"

    if (device.currentValue('mute') == 'muted') {
        sendEvent(name:'mute', value:'unmuted')
    }

    sendEvent(name:"level", value:number)
    def volume = ((number * 512) / 100) as int
    return apiCommand("command=volume&val=${volume}")
}

// MusicPlayer.mute
def mute() {
    //log.debug "mute()"

    if (device.currentValue('mute') == 'muted') {
        return null
    }

    state.savedVolume = device.currentValue('level')
    sendEvent(name:'mute', value:'muted')
    sendEvent(name:'level', value:0)

    return apiCommand("command=volume&val=0")
}

// MusicPlayer.unmute
def unmute() {
    //log.debug "unmute()"

    if (device.currentValue('mute') == 'muted') {
        return setLevel(state.savedVolume.toInteger())
    }

    return null
}

// SpeechSynthesis.speak
def speak(text) {
    //log.debug "speak(${text})"
    def sound = myTextToSpeech(text)
    return playTrack(sound.uri)
}

// polling.poll 
def poll() {
    //log.debug "poll()"
    return refresh()
}

// refresh.refresh
def refresh() {
    //log.debug "refresh()"
    //STATE()

    if (!updateDNI()) {
        sendEvent([
            name:           'connection',
            value:          'disconnected',
            isStateChange:  true,
            displayed:      false
        ])

        return null
    }

    // Restart polling task if it's not run for 5 minutes
    def elapsed = (now() - state.lastPoll) / 1000
    if (elapsed > 300) {
        log.warn "Restarting polling task..."
        unschedule()
        startPollingTask()
    }

    return apiGetStatus()
}

def enqueue(uri) {
    //log.debug "enqueue(${uri})"
    def command = "command=in_enqueue&input=" + URLEncoder.encode(uri, "UTF-8")
    return apiCommand(command)
}

def seek(trackNumber) {
    //log.debug "seek(${trackNumber})"
    def command = "command=pl_play&id=${trackNumber}"
    return apiCommand(command, 500)
}

def playTrackAndResume(uri, duration, volume = null) {
    //log.debug "playTrackAndResume(${uri}, ${duration}, ${volume})"

    // FIXME
    return playTrackAndRestore(uri, duration, volume)
}

def playTrackAndRestore(uri, duration, volume = null) {
    //log.debug "playTrackAndRestore(${uri}, ${duration}, ${volume})"

    def currentStatus = device.currentValue('status')
    def currentVolume = device.currentValue('level')
    def currentMute = device.currentValue('mute')
    def actions = []
    if (currentStatus == 'playing') {
        actions << apiCommand("command=pl_stop")
        actions << delayHubAction(500)
    }

    if (volume) {
        actions << setLevel(volume)
        actions << delayHubAction(500)
    } else if (currentMute == 'muted') {
        actions << unmute()
        actions << delayHubAction(500)
    }

    def delay = (duration.toInteger() + 1) * 1000
    //log.debug "delay = ${delay}"

    actions << playTrack(uri)
    actions << delayHubAction(delay)
    actions << apiCommand("command=pl_stop")
    actions << delayHubAction(500)

    if (currentMute == 'muted') {
        actions << mute()
    } else if (volume) {
        actions << setLevel(currentVolume)
    }

    actions << apiGetStatus()
    actions = actions.flatten()
    //log.debug "actions: ${actions}"

    return actions
}

def playTextAndResume(text, volume = null) {
    //log.debug "playTextAndResume(${text}, ${volume})"
    def sound = myTextToSpeech(text)
    return playTrackAndResume(sound.uri, (sound.duration as Integer) + 1, volume)
}

def playTextAndRestore(text, volume = null) {
    //log.debug "playTextAndRestore(${text}, ${volume})"
    def sound = myTextToSpeech(text)
    return playTrackAndRestore(sound.uri, (sound.duration as Integer) + 1, volume)
}

def playSoundAndTrack(uri, duration, trackData, volume = null) {
    //log.debug "playSoundAndTrack(${uri}, ${duration}, ${trackData}, ${volume})"

    // FIXME
    return playTrackAndRestore(uri, duration, volume)
}

def testTTS() {
    //log.debug "testTTS()"
    def text = "VLC for Smart Things is brought to you by Statusbits.com"
    return playTextAndResume(text)
}

private startPollingTask() {
    //log.debug "startPollingTask()"

    pollingTask()

    Random rand = new Random(now())
    def seconds = rand.nextInt(60)
    def sched = "${seconds} 0/1 * * * ?"

    //log.debug "Scheduling polling task with \"${sched}\""
    schedule(sched, pollingTask)
}

private apiGet(String path) {
    //log.debug "apiGet(${path})"

    if (!updateDNI()) {
        return null
    }

    state.requestTime = now()
    state.responseTime = 0

    def headers = [
        HOST:       state.hostAddress,
        Accept:     "*/*"
    ]
    
    if (state.userAuth != null) {
        headers['Authorization'] = "Basic ${state.userAuth}"
    }

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

    //log.debug "httpRequest: ${httpRequest}"
    return new physicalgraph.device.HubAction(httpRequest)
}

private def delayHubAction(ms) {
    return new physicalgraph.device.HubAction("delay ${ms}")
}

private apiGetStatus() {
    return apiGet("/requests/status.json")
}

private apiCommand(command, refresh = 0) {
    //log.debug "apiCommand(${command})"

    def actions = [
        apiGet("/requests/status.json?${command}")
    ]

    if (refresh) {
        actions << delayHubAction(refresh)
        actions << apiGetStatus()
    }

    return actions
}

private def apiGetPlaylists() {
    //log.debug "getPlaylists()"
    return apiGet("/requests/playlist.json")
}

private parseHttpHeaders(String headers) {
    def lines = headers.readLines()
    def status = lines.remove(0).split()

    def result = [
        protocol:   status[0],
        status:     status[1].toInteger(),
        reason:     status[2]
    ]

    return result
}

private def parseHttpResponse(Map data) {
    //log.debug "parseHttpResponse(${data})"

    state.updatedTime = now()
    if (!state.responseTime) {
        state.responseTime = now()
    }

    def events = []

    if (data.containsKey('state')) {
        def vlcState = data.state
        //log.debug "VLC state: ${vlcState})"
        events << createEvent(name:"status", value:vlcState)
        if (vlcState == 'stopped') {
            events << createEvent([name:'trackDescription', value:''])
        }
    }

    if (data.containsKey('volume')) {
        //log.debug "VLC volume: ${data.volume})"
        def volume = ((data.volume.toInteger() * 100) / 512) as int
        events << createEvent(name:'level', value:volume)
    }

    if (data.containsKey('information')) {
        parseTrackInfo(events, data.information)
    }

    events << createEvent([
        name:           'connection',
        value:          'connected',
        isStateChange:  true,
        displayed:      false
    ])

    //log.debug "events: ${events}"
    return events
}

private def parseTrackInfo(events, Map info) {
    //log.debug "parseTrackInfo(${events}, ${info})"

    if (info.containsKey('category') && info.category.containsKey('meta')) {
        def meta = info.category.meta
        //log.debug "Track info: ${meta})"
        if (meta.containsKey('filename')) {
            if (meta.filename.contains("//s3.amazonaws.com/smartapp-")) {
                log.trace "Skipping event generation for sound file ${meta.filename}"
                return
            }
        }

        def track = ""
        if (meta.containsKey('artist')) {
            track = "${meta.artist} - "
        }
        if (meta.containsKey('title')) {
            track += meta.title
        } else if (meta.containsKey('filename')) {
            def parts = meta.filename.tokenize('/');
            track += parts.last()
        } else {
            track += '<untitled>'
        }

        if (track != device.currentState('trackDescription')) {
            meta.station = track
            events << createEvent(name:'trackDescription', value:track, displayed:false)
            events << createEvent(name:'trackData', value:meta.encodeAsJSON(), displayed:false)
        }
    }
}

private def myTextToSpeech(text) {
    def sound = textToSpeech(text, true)
    sound.uri = sound.uri.replace('https:', 'http:')
    return sound
}

private String createDNI(ipaddr, port) {
    //log.debug "createDNI(${ipaddr}, ${port})"

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())
 
    return "${hexIp}:${hexPort}"
}

private updateDNI() {
    if (!state.dni) {
	    log.warn "DNI is not set! Please enter IP address and port in settings."
        return false
    }
 
    if (state.dni != device.deviceNetworkId) {
	    log.warn "Invalid DNI: ${device.deviceNetworkId}!"
        device.deviceNetworkId = state.dni
    }

    return true
}

private def title() {
    return "VLC Thing. Version 2.0.0 (12/22/2016). Copyright © 2014 Statusbits.com"
}

private def STATE() {
    log.trace "state: ${state}"
    log.trace "deviceNetworkId: ${device.deviceNetworkId}"
    log.trace "status: ${device.currentValue('status')}"
    log.trace "level: ${device.currentValue('level')}"
    log.trace "mute: ${device.currentValue('mute')}"
    log.trace "trackDescription: ${device.currentValue('trackDescription')}"
    log.trace "connection: ${device.currentValue("connection")}"
}
