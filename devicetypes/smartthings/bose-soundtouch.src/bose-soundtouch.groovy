/**
 *  Bose SoundTouch
 *
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
 */

// Needed to be able to serialize the XmlSlurper data back to XML
import groovy.xml.XmlUtil

// for the UI
metadata {
    definition (name: "Bose SoundTouch", namespace: "smartthings", author: "SmartThings") {
        /**
         * List our capabilties. Doing so adds predefined command(s) which
         * belong to the capability.
         */
        capability "Switch"
        capability "Refresh"
        capability "Music Player"
        capability "Audio Notification"
        capability "Health Check"

        /**
         * Define all commands, ie, if you have a custom action not
         * covered by a capability, you NEED to define it here or
         * the call will not be made.
         *
         * To call a capability function, just prefix it with the name
         * of the capability, for example, refresh would be "refresh.refresh"
         */
        command "preset1"
        command "preset2"
        command "preset3"
        command "preset4"
        command "preset5"
        command "preset6"
        command "aux"

        command "everywhereJoin"
        command "everywhereLeave"

        command "forceOff"
        command "forceOn"

        command "playTrackAtVolume", ["string", "number"]
        command "playTrackAndResume", ["string", "number", "number"]
        command "playTextAndResume", ["string", "number"]
        command "playTrackAndRestore", ["string", "number", "number"]
        command "playTextAndRestore", ["string", "number"]
        command "playSoundAndTrack", ["string", "number", "json_object", "number"]
    }

    /**
     * Define the various tiles and the states that they can be in.
     * The 2nd parameter defines an event which the tile listens to,
     * if received, it tries to map it to a state.
     *
     * You can also use ${currentValue} for the value of the event
     * or ${name} for the name of the event. Just make SURE to use
     * single quotes, otherwise it will only be interpreted at time of
     * launch, instead of every time the event triggers.
     */
    tiles(scale: 2) {
        multiAttributeTile(name: "main", type: "mediaPlayer", width: 6, height: 4) {//, canChangeIcon: true) {
            tileAttribute("device.status", key: "PRIMARY_CONTROL") {
                attributeState("paused", /*label: "Paused"*/)
                attributeState("playing", /*label: "Playing"*/)
                attributeState("stopped", /*label: "Stopped"*/)
            }
            tileAttribute("device.status", key: "MEDIA_STATUS") {
                attributeState("paused", label: "Paused", action: "music Player.play", nextState: "playing")
                attributeState("playing", label: "Playing", action: "music Player.pause", nextState: "paused")
                attributeState("stopped", label: "Stopped", action: "music Player.play", nextState: "playing")
            }
            tileAttribute("device.status", key: "PREVIOUS_TRACK") {
                attributeState("status", action: "music Player.previousTrack", defaultState: true)
            }
            tileAttribute("device.status", key: "NEXT_TRACK") {
                attributeState("status", action: "music Player.nextTrack", defaultState: true)
            }
            tileAttribute("device.volume", key: "SLIDER_CONTROL") {
                attributeState("volume", action: "music Player.setLevel")
            }
            tileAttribute ("device.mute", key: "MEDIA_MUTED") {
                attributeState("unmuted", action:"music Player.mute", nextState: "muted")
                attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
            }
            tileAttribute("device.trackDescription", key: "MARQUEE") {
                attributeState("trackDescription")
            }
        }
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
            state "on", label: '${name}', action: "forceOff", icon: "st.Electronics.electronics16", backgroundColor: "#79b821", nextState:"turningOff"
            state "turningOff", label:'TURNING OFF', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
            state "off", label: '${name}', action: "forceOn", icon: "st.Electronics.electronics16", backgroundColor: "#ffffff", nextState:"turningOn"
            state "turningOn", label:'TURNING ON', icon:"st.Electronics.electronics16", backgroundColor:"#79b821"
        }
        valueTile("1", "device.station1", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station1", label: '${currentValue}', action: "preset1"
        }
        valueTile("2", "device.station2", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station2", label: '${currentValue}', action: "preset2"
        }
        valueTile("3", "device.station3", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station3", label: '${currentValue}', action: "preset3"
        }
        valueTile("4", "device.station4", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station4", label: '${currentValue}', action: "preset4"
        }
        valueTile("5", "device.station5", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station5", label: '${currentValue}', action: "preset5"
        }
        valueTile("6", "device.station6", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "station6", label: '${currentValue}', action: "preset6"
        }
        valueTile("aux", "device.switch", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "default", label: 'Auxillary\nInput', action: "aux"
        }

        standardTile("refresh", "device.nowplaying", width:2, height:2, decoration: "flat", canChangeIcon: false) {
            state "default", label: '', action: "refresh", icon: "st.secondary.refresh"
        }

        valueTile("nowplaying", "device.nowplaying", width: 2, height: 2, decoration: "flat") {
            state "nowplaying", label: '${currentValue}', action: "refresh.refresh"
        }

        valueTile("everywhere", "device.everywhere", width: 2, height: 2, decoration: "flat") {
            state "join", label: "Join\nEverywhere", action: "everywhereJoin"
            state "leave", label: "Leave\nEverywhere", action: "everywhereLeave"
            // Final state is used if the device is in a state where joining is not possible
            state "unavailable", label: "Not Available"
        }

        // Defines which tile to show in the overview
        main "switch"

        // Defines which tile(s) to show when user opens the detailed view
        details([ "main",                          // Row 1 (111)
                  "1", "2", "3",                   // Row 2 (123)
                  "4", "5", "6",                   // Row 3 (123)
                  "aux", "everywhere", "refresh",  // Row 4 (123)
                  "switch"])                       // Row 5 (1--)
    }
}
/**************************************************************************
 * The following section simply maps the actions as defined in
 * the metadata into onAction() calls.
 *
 * This is preferred since some actions can be dealt with more
 * efficiently this way. Also keeps all user interaction code in
 * one place.
 *
 */
def off() {
    if (device.currentState("switch")?.value == "on") {
        onAction("off")
    }
}
def forceOff() {
    onAction("off")
}
def on() {
    if (device.currentState("switch")?.value == "off") {
        onAction("on")
    }
}
def forceOn() {
    onAction("on")
}
def volup() { onAction("volup") }
def voldown() { onAction("voldown") }
def preset1() { onAction("1") }
def preset2() { onAction("2") }
def preset3() { onAction("3") }
def preset4() { onAction("4") }
def preset5() { onAction("5") }
def preset6() { onAction("6") }
def aux() { onAction("aux") }
def refresh() { onAction("refresh") }
def setLevel(level) { onAction("volume", level) }
def play() { onAction("play") }
def pause() { onAction("pause") }
def mute() {
    if ("unmuted" == device.currentState("mute")?.value) {
        onAction("mute")
    }
}
def unmute() {
    if ("muted" == device.currentState("mute")?.value) {
        onAction("unmute")
    }
}
def previousTrack() { onAction("previous") }
def nextTrack() { onAction("next") }
def everywhereJoin() { onAction("ejoin") }
def everywhereLeave() { onAction("eleave") }
/**************************************************************************/

/**
 * Main point of interaction with things.
 * This function is called by SmartThings Cloud with the resulting data from
 * any action (see HubAction()).
 *
 * Conversely, to execute any actions, you need to return them as a single
 * item or a list (flattened).
 *
 * @param data Data provided by the cloud
 * @return an action or a list() of actions. Can also return null if no further
 *         action is desired at this point.
 */
def parse(String event) {
    def data = parseLanMessage(event)
    def actions = []

    // List of permanent root node handlers
    def handlers = [
        "nowPlaying" : "boseParseNowPlaying",
        "volume" : "boseParseVolume",
        "presets" : "boseParsePresets",
        "zone" : "boseParseEverywhere",
        "info" : "boseParseInfo",
    ]

    // No need to deal with non-XML data
    if (!data.headers || !data.headers?."content-type".contains("xml"))
        return null

    def xml = new XmlSlurper().parseText(data.body)
    // Let each parser take a stab at it
    handlers.each { node,func ->
        if (xml.name() == node)
            actions << "$func"(xml)
    }

    // Be nice and helpful
    if (actions.size() == 0) {
        return null
    }

    // Issue new actions
    return actions.flatten()
}

/**
 * Called when the devicetype is first installed.
 *
 * @return action(s) to take or null
 */
def installed() {
    // Notify health check about this device with timeout interval 12 minutes
    sendEvent(name: "checkInterval", value: 12 * 60, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    // Following only until installed smartapps can get dataValue from DTH
    sendEvent(name: "manufacturer", value: parent.getDeviceName(), displayed: false)
    startPoll()
}

/**
 * Called when the settings for the devicetype is changed.
 */
def updated() {
    // Notify health check about this device with timeout interval 12 minutes
    sendEvent(name: "checkInterval", value: 12 * 60, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    parent.updateDeviceName(device.deviceNetworkId, device.label, this)
}

/**
 * Called by health check if no events been generated in the last 12 minutes
 * If device doesn't respond it will be marked offline (not available)
 */
def ping() {
    boseSendGetNowPlaying()
}

/**
 * Schedule a 2 minute poll of the device to refresh the
 * tiles so the user gets the correct information.
 */
def startPoll() {
    unschedule()
    // Schedule 2 minute polling of speaker status (song average length is 3-4 minutes)
    def sec = Math.round(Math.floor(Math.random() * 60))
    def cron = "$sec 0/2 * * * ?" // every 2 min
    log.debug "schedule('$cron', boseSendGetNowPlaying)"
    schedule(cron, boseSendGetNowPlaying)
}

/**
 * Responsible for dealing with user input and taking the
 * appropiate action.
 *
 * @param user The user interaction
 * @param data Additional data (optional)
 * @return action(s) to take (or null if none)
 */
def onAction(String user, data=null) {
    log.info "onAction(${user})"

    // Keep IP address current (since device may have changed)
    state.address = parent.resolveDNI2Address(device.deviceNetworkId)

    // Process action
    def actions = null
    switch (user) {
        case "on":
            actions = [boseSetPowerState(true), boseRefreshNowPlaying(), boseGetPresets(), boseGetVolume(), boseGetEverywhereState()]
            break
        case "off":
            actions = [boseSetPowerState(false), boseRefreshNowPlaying()]
            break
        case "volume":
            actions = [boseSetVolume(data), boseGetVolume()]
            break
        case "aux":
            boseSetNowPlaying(null, "AUX")
            boseZoneReset()
            sendEvent(name:"everywhere", value:"unavailable")
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
            actions = boseSetInput(user)
            break
        case "refresh":
            actions = [boseRefreshNowPlaying(), boseGetPresets(), boseGetVolume(), boseGetEverywhereState(), boseGetInfo()]
            break
        case "play":
            actions = []
            if (device.currentState("switch")?.value == "off") {
                actions << [boseSetPowerState(true), boseGetPresets(), boseGetVolume(), boseGetEverywhereState()]
            }
            actions << [boseSetPlayMode(true), boseRefreshNowPlaying()]
            break
        case "pause":
            actions = [boseSetPlayMode(false), boseRefreshNowPlaying()]
            break
        case "previous":
            actions = [boseChangeTrack(-1), boseRefreshNowPlaying()]
            break
        case "next":
            actions = [boseChangeTrack(1), boseRefreshNowPlaying()]
            break
        case "mute":
            // NO BREAK
        case "unmute":
            actions = boseSetMute()
            break
        case "ejoin":
            actions = boseZoneJoin()
            break
        case "eleave":
            actions = boseZoneLeave()
            break
        default:
            log.error "Unhandled action: " + user
    }

    // Make sure we don't have nested lists
    if (actions instanceof List)
        return actions.flatten()
    return actions
}

/**
 * Joins this speaker into the everywhere zone
 */
def boseZoneJoin() {
    def results = []
    def posts = parent.boseZoneJoin(this)

    for (post in posts) {
        if (post['endpoint'])
            results << bosePOST(post['endpoint'], post['body'], post['host'])
    }
    sendEvent(name:"everywhere", value:"leave")
    results << boseRefreshNowPlaying()

    return results
}

/**
 * Removes this speaker from the everywhere zone
 */
def boseZoneLeave() {
    def results = []
    def posts = parent.boseZoneLeave(this)

    for (post in posts) {
        if (post['endpoint'])
            results << bosePOST(post['endpoint'], post['body'], post['host'])
    }
    sendEvent(name:"everywhere", value:"join")
    results << boseRefreshNowPlaying()

    return results
}

/**
 * Removes this speaker and any children WITHOUT
 * signaling the speakers themselves. This is needed
 * in certain cases where we know the user action will
 * cause the zone to collapse (for example, AUX)
 */
def boseZoneReset() {
    parent.boseZoneReset()
}

/**
 * Handles <nowPlaying></nowPlaying> information and can also
 * perform addtional actions if there is a pending command
 * stored in the state variable. For example, the power is
 * handled this way.
 *
 * @param xmlData Data to parse
 * @return command
 */
def boseParseNowPlaying(xmlData) {
    def result = []

    // Perform display update, allow it to add additional commands
    if (boseSetNowPlaying(xmlData)) {
        result << boseRefreshNowPlaying()
    }

    return result
}

/**
 * Parses volume data
 *
 * @param xmlData Data to parse
 * @return command
 */
def boseParseVolume(xmlData) {
    def result = []

    sendEvent(name:"volume", value:xmlData.actualvolume.text())
    if (Boolean.toBoolean(xmlData.muteenabled.text())) {
        sendEvent(name:"mute", value:"muted")
    } else {
        sendEvent(name:"mute", value:"unmuted")
    }

    return result
}

/**
 * Parses the result of the boseGetEverywhereState() call
 *
 * @param xmlData
 */
def boseParseEverywhere(xmlData) {
    // No good way of detecting the correct state right now
}

def boseParseInfo(xmlData) {
    // Device name xmlData?.name?.text()
    if (xmlData?.name && (xmlData?.name?.text() != device.label)) {
        parent.updateDeviceName(device.deviceNetworkId, xmlData?.name?.text())
    }
    // Device type
    if (xmlData?.type && (xmlData?.type != getDataValue("model"))) {
        updateDataValue("model", xmlData?.type?.text())
        // following only until installed smartapps can get dataValues from DTH
        sendEvent(name: "model", value: xmlData?.type?.text(), displayed: false)
    }
    // SW version
    if (xmlData?.components?.component?.softwareVersion && (xmlData?.components?.component?.softwareVersion?.text() != getDataValue("softwareVersion"))) {
        updateDataValue("softwareVersion", xmlData?.components?.component?.softwareVersion?.text())
    }
}

/**
 * Parses presets and updates the buttons
 *
 * @param xmlData Data to parse
 * @return command
 */
def boseParsePresets(xmlData) {
    def result = []

    state.preset = [:]

    def missing = ["1", "2", "3", "4", "5", "6"]
    for (preset in xmlData.preset) {
        def id = preset.attributes()['id']
        def name = preset.ContentItem.itemName[0].text()
        if (name == "##TRANS_SONGS##")
            name = "Local\nPlaylist"
        sendEvent(name:"station${id}", value:name)
        missing = missing.findAll { it -> it != id }

        // Store the presets into the state for recall later
        state.preset["$id"] = XmlUtil.serialize(preset.ContentItem)
    }

    for (id in missing) {
        state.preset["$id"] = null
        sendEvent(name:"station${id}", value:"Preset $id Not set")
    }

    return result
}

/**
 * Based on <nowPlaying></nowPlaying>, updates the visual
 * representation of the speaker
 *
 * @param xmlData The nowPlaying info
 * @param override Provide the source type manually (optional)
 *
 * @return true if it would prefer a refresh soon
 */
def boseSetNowPlaying(xmlData, override=null) {
    def needrefresh = false
    def nowplaying = null

    if (xmlData && xmlData.playStatus) {
        switch(xmlData.playStatus) {
            case "BUFFERING_STATE":
                nowplaying = "Please wait\nBuffering..."
                needrefresh = true
                break
            case "PLAY_STATE":
                sendEvent(name:"status", value:"playing")
                break
            case "PAUSE_STATE":
            case "STOP_STATE":
                sendEvent(name:"status", value:"paused")
                break
        }
    }

    // If the previous section didn't handle this, take another stab at it
    if (!nowplaying) {
        nowplaying = ""
        switch (override ? override : xmlData.attributes()['source']) {
            case "AUX":
                nowplaying = "Auxiliary Input"
                break
            case "AIRPLAY":
                nowplaying = "Air Play"
                break
            case "STANDBY":
                nowplaying = "Standby"
                break
            case "INTERNET_RADIO":
                nowplaying = "${xmlData.stationName.text()}\n${xmlData.description.text()}"
                break
            case "REFRESH":
                nowplaying = "Please wait"
                break
            case "SPOTIFY":
            case "DEEZER":
            case "PANDORA":
            case "IHEART":
                if (xmlData.ContentItem.itemName[0]?.text()?.trim())
                    nowplaying += "[${xmlData.ContentItem.itemName[0].text()}]\n"
            case "STORED_MUSIC":
                if (xmlData.track?.text()?.trim())
                    nowplaying += "${xmlData.track.text()}\n"
                if (xmlData.artist?.text()?.trim())
                    nowplaying += "by\n${xmlData.artist.text()}\n"
                if (xmlData.album?.text()?.trim())
                    nowplaying += "(${xmlData.album.text()})"
                break
            default:
                if (xmlData != null)
                    nowplaying = "${xmlData.ContentItem.itemName[0].text()}"
                else
                    nowplaying = "Unknown"
        }
    }

    // Some last parsing which only deals with actual data from device
    if (xmlData) {
        boseSetPlayerAttributes(xmlData)
        if (xmlData.attributes()['source'] == "STANDBY") {
            log.trace "nowPlaying reports standby: " + XmlUtil.serialize(xmlData)
            sendEvent(name:"status", value:"paused")
            sendEvent(name:"switch", value:"off")
        } else {
            if (device.currentState("switch")?.value != "on") {
                sendEvent(name:"switch", value:"on")
            }
        }
    }
    // Do not allow a standby device or AUX to be master
    if (!parent.boseZoneHasMaster() && (override ? override : xmlData.attributes()['source']) == "STANDBY")
        sendEvent(name:"everywhere", value:"unavailable")
    else if ((override ? override : xmlData.attributes()['source']) == "AUX")
        sendEvent(name:"everywhere", value:"unavailable")
    else if (boseGetZone()) {
        log.info "We're in the zone: " + boseGetZone()
        sendEvent(name:"everywhere", value:"leave")
    } else
        sendEvent(name:"everywhere", value:"join")

    sendEvent(name:"nowplaying", value:nowplaying)
    
    if (needrefresh) {
        runIn(2, boseSendGetNowPlaying, [overwrite: true])
    }
    return false
}

/**
 * Updates the attributes exposed by the music Player capability
 *
 * @param xmlData The NowPlaying XML data
 */
def boseSetPlayerAttributes(xmlData) {
    // Refresh attributes
    String trackText = ""
    String trackDesc = ""
    def trackData = [:]

    switch (xmlData.attributes()['source']) {
        case "STANDBY":
            trackData["station"] = trackText = trackDesc = "Off"
            break
        case "AUX":
            trackData["station"] = trackText = trackDesc = "Auxiliary Input"
            break
        case "AIRPLAY":
            trackData["station"] = trackText = trackDesc = "Air Play"
            break
        case "SPOTIFY":
        case "DEEZER":
        case "PANDORA":
        case "IHEART":
        case "BLUETOOTH":
            if (xmlData?.ContentItem?.itemName[0]?.text()?.trim())
                trackDesc += "[${xmlData.ContentItem.itemName[0].text()}]"
        case "STORED_MUSIC":
            if (xmlData.track?.text()?.trim()) {
                trackText = "${xmlData.track.text()}"
                trackData["name"] = xmlData.track.text()
            }
            if (xmlData.artist?.text()?.trim()) {
                trackText += "\nby ${xmlData.artist.text()}"
                trackData["artist"] = xmlData.artist.text()
            }
            if (xmlData.album?.text()?.trim()) {
                trackText += "\nfrom \"${xmlData.album.text()}\""
                trackData["album"] = xmlData.album.text()
            }
            break
        case "INTERNET_RADIO":
            trackDesc = xmlData.stationName?.text()
            trackText = xmlData.description?.text()
            trackData["station"] = xmlData.stationName?.text()
            break
        default:
            trackText = trackDesc = xmlData.ContentItem?.itemName[0]?.text()
    }

    def delay = 0
    // Reschedule boseRefreshNowPlaying if needed
    if ((trackData["name"]?.contains("Loading") ||
         trackData["name"]?.contains("Waiting for connection")) && !trackData["artist"] && !trackData["album"]) {
        // This can happen if Bose is playing media over bluetooth
        delay = 10
    } else if (trackDesc?.trim() && !trackText?.trim()) {
        // This happens when advertisment is streaming
        delay = 35
    }
    if (delay) {
        runIn(delay, boseSendGetNowPlaying, [overwrite: true])
    }
    def trackDescription = (trackDesc == trackText ? trackDesc : trackDesc + "\n" + trackText)
    sendEvent(name:"trackDescription", value:trackDescription, displayed:false)
}

def boseGetInfo() {
    return boseGET("/info")
}

/**
 * Queries the state of the "play everywhere" mode
 *
 * @return command
 */
def boseGetEverywhereState() {
    return boseGET("/getZone")
}

/**
 * Generates a remote key event
 *
 * @param key The name of the key
 *
 * @return command
 *
 * @note It's VITAL that it's done as two requests, or it will ignore the
 *       the second key info.
 */
def boseKeypress(key) {
    def press = "<key state=\"press\" sender=\"Gabbo\">${key}</key>"
    def release = "<key state=\"release\" sender=\"Gabbo\">${key}</key>"

    return [bosePOST("/key", press), bosePOST("/key", release)]
}

/**
 * Pauses or plays current preset
 *
 * @param play If true, plays, else it pauses (depending on preset, may stop)
 *
 * @return command
 */
def boseSetPlayMode(boolean play) {
    log.trace "Sending " + (play ? "PLAY" : "PAUSE")
    return boseKeypress(play ? "PLAY" : "PAUSE")
}

/**
 * Sets the volume in a deterministic way.
 *
 * @param New volume level, ranging from 0 to 100
 *
 * @return command
 */
def boseSetVolume(int level) {
    int vol = Math.min(100, Math.max(level, 0))

    return bosePOST("/volume", "<volume>${vol}</volume>")
}

/**
 * Sets the mute state, unfortunately, for now, we need to query current
 * state before taking action (no discrete mute/unmute)
 *
 * @param mute If true, mutes the system
 * @return command
 */
def boseSetMute() {
    return [boseKeypress("MUTE"), boseGetVolume()]
}

/**
 * Refreshes the state of the volume
 *
 * @return command
 */
def boseGetVolume() {
    return boseGET("/volume")
}

/**
 * Changes the track to either the previous or next
 *
 * @param direction > 0 = next track, < 0 = previous track, 0 = no action
 * @return command
 */
def boseChangeTrack(int direction) {
    if (direction < 0) {
        return boseKeypress("PREV_TRACK")
    } else if (direction > 0) {
        return boseKeypress("NEXT_TRACK")
    }
    return []
}

/**
 * Sets the input to preset 1-6 or AUX
 *
 * @param input The input (one of 1,2,3,4,5,6,aux)
 *
 * @return command
 *
 * @note If no presets have been loaded, it will first refresh the presets.
 */
def boseSetInput(input) {
    log.info "boseSetInput(${input})"
    def result = []

    if (input.toLowerCase() == "aux") {
        result << boseKeypress("AUX_INPUT")
    } else if (!state.preset) {
        result << boseGetPresets()
    } else if (input >= "1" && input <= "6" && state.preset["$input"]) {
        result << bosePOST("/select", state.preset["$input"])
    }

    result << boseRefreshNowPlaying()
    return result
}

/**
 * Sets the power state of the bose unit
 *
 * @param device The device in-question
 * @param enable True to power on, false to power off
 *
 * @return command
 *
 * @note Will first query state before acting since there
 *       is no discreete call.
 */
def boseSetPowerState(boolean enable) {
    log.info "boseSetPowerState(${enable})"
    return boseKeypress("POWER")
}

/**
 * Requests an update on currently playing item(s)
 *
 * @param delay If set to non-zero, delays x ms before issuing
 *
 * @return command
 */
def boseRefreshNowPlaying(delay=0) {
    if (delay > 0) {
        return ["delay ${delay}", boseGET("/now_playing")]
    }
    return boseGET("/now_playing")
}

def boseSendGetNowPlaying() {
    TRACE("boseSendGetNowPlaying")
    sendHubCommand(boseGET("/now_playing"))
}

/**
 * Requests the list of presets
 *
 * @return command
 */
def boseGetPresets() {
    return boseGET("/presets")
}

/**
 * Utility function, makes GET requests to BOSE device
 *
 * @param path What endpoint
 *
 * @return command
 */
def boseGET(String path) {
    new physicalgraph.device.HubAction([
        method: "GET",
        path: path,
        headers: [
            HOST: state.address + ":8090",
        ]])
}

/**
 * Utility function, makes a POST request to the BOSE device with
 * the provided data.
 *
 * @param path What endpoint
 * @param data What data
 * @param address Specific ip and port (optional)
 *
 * @return command
 */
def bosePOST(String path, String data, String address=null) {
    new physicalgraph.device.HubAction([
        method: "POST",
        path: path,
        body: data,
        headers: [
            HOST: address ?: (state.address + ":8090"),
        ]])
}

/**
 * State managament for the Play Everywhere zone.
 * This is typically called from the parent.
 *
 * A device is either:
 *
 * null = Not participating
 * server = running the show
 * client = under the control of the server
 *
 * @param newstate (see above for types)
 */
def boseSetZone(String newstate) {
    log.debug "boseSetZone($newstate)"
    state.zone = newstate

    // Refresh our state
    if (newstate) {
        sendEvent(name:"everywhere", value:"leave")
    } else {
        sendEvent(name:"everywhere", value:"join")
    }
}

/**
 * Used by the Everywhere zone, returns the current state
 * of zone membership (null, server, client)
 * This is typically called from the parent.
 *
 * @return state
 */
def boseGetZone() {
    return state.zone
}

/**
 * Sets the DeviceID of this particular device.
 *
 * Needs to be done this way since DNI is not always
 * the same as DeviceID which is used internally by
 * BOSE.
 *
 * @param devID The DeviceID
 */
def boseSetDeviceID(String devID) {
    state.deviceID = devID
}

/**
 * Retrieves the DeviceID for this device
 *
 * @return deviceID
 */
def boseGetDeviceID() {
    return state.deviceID
}

/**
 * Returns the IP of this device
 *
 * @return IP address
 */
def getDeviceIP() {
    return parent.resolveDNI2Address(device.deviceNetworkId)
}

def playTrackAndResume(uri, duration, volume=null) {
    TRACE("playTrackAndResume($uri, $duration, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playTrackAndResume(uri, duration, volume)
    }
    def action = []
    def currentStatus = device.currentState("status")?.value

    action << playTrackAndRestore(uri, duration, volume)
    // If speaker was playing it now needs to be turned back on, as otherwise speaker will be OFF
    if ("playing" == currentStatus) {
        action << boseKeypress("POWER")
    }
    return action.flatten()
}

def playTrackAtVolume(String uri, volume) {
    TRACE("playTrackAtVolume($uri, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playTrackAtVolume(uri, volume)
    }
    def service = "SmartThings"
    def reason = ""
    def message = ""
    def action = []
// start hack to enable volume change for the notification as BOSE API doesn't support that
    // First if there's something playing, pause it and wait before next action to avoid static noise crackling from speaker
    def currentStatus = device.currentState("status")?.value
    TRACE("Speaker is $currentStatus")
    if ("playing" == currentStatus) {
        action << [boseSetPlayMode(false), "delay 10"]
    }
    // Then, do we need to change volume level?
    Integer level = null
    Integer currentVolume = null
    if (volume != null && volume != "") {
        level = volume as Integer
        currentVolume = device.currentState("volume")?.value as Integer
        TRACE("Current speaker volume: $currentVolume, new volume:$level")
        if (level != currentVolume) {
            // Wait a bit longer before changing volume level to avoid volume ramping
            action << ["delay 50", boseSetVolume(level)]
        }
    }
// end
    // Play the notification
    action << [bosePOST("/speaker", "<play_info><app_key>0</app_key><url>${uri}</url><service>${service}</service><reason>${reason}</reason><message>${message}</message></play_info>")]
}

def playTrack(String uri, volume=null) {
    playTrackAtVolume(uri, volume)
}

def playText(text, volume=null) {
    TRACE("playText ($text, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playText(text, volume)
    }
    def spokenText = textToSpeech(text)

    playTrackAtVolume(spokenText.uri, (spokenText.duration as Integer) + 1, volume)
}

def playTextAndResume(text, volume=null) {
    TRACE("playTextAndResume ($text, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playTextAndResume(text, volume)
    }
    def spokenText = textToSpeech(text)

    playTrackAndResume(spokenText.uri, (spokenText.duration as Integer) + 1, volume)
}

def playTrackAndRestore(uri, duration, volume=null) {
    TRACE("playTrackAndRestore($uri, $duration, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playTrackAndRestore(uri, duration, volume)
    }
    def service = "SmartThings"
    def reason = ""
    def message = ""
    def action = []
// start hack to enable volume change for the notification as BOSE API doesn't support that
    // First if there's something playing, pause it and wait before next action to avoid static noise crackling from speaker
    def currentStatus = device.currentState("status")?.value
    TRACE("Speaker is $currentStatus")
    if ("playing" == currentStatus) {
        action << [boseSetPlayMode(false), "delay 10"]
    }
    // Then, do we need to change volume level?
    Integer level = null
    Integer currentVolume = null
    if (volume != null && volume != "") {
        level = volume as Integer
        currentVolume = device.currentState("volume")?.value as Integer
        TRACE("Current speaker volume: $currentVolume, new volume:$level")
        if (level != currentVolume) {
            // Wait a bit longer before changing volume level to avoid volume ramping
            action << ["delay 50", boseSetVolume(level)]
        }
    }
// end
    // Play the notification
    action << [bosePOST("/speaker", "<play_info><app_key>0</app_key><url>${uri}</url><service>${service}</service><reason>${reason}</reason><message>${message}</message></play_info>")]
// start hack to enable volume change for the notification as BOSE API doesn't support that
    // Now wait for the notification to complete before restoring states
    def delay = 1000 * ((duration as Integer) + 5)
    action << "delay ${delay}"

    // Restore volume if changed
    if (level != currentVolume) {
        action << boseSetVolume(currentVolume)
    }
// end
    return action.flatten()
}

def playTextAndRestore(text, volume=null) {
    TRACE("playTextAndRestore ($text, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playTextAndRestore(text, volume)
    }
    def spokenText = textToSpeech(text)
    playTrackAndRestore(spokenText.uri, (spokenText.duration as Integer) + 1, volume)
}

def playSoundAndTrack(soundUri, duration, trackData, volume=null) {
    TRACE("playSoundAndTrack:($soundUri, $duration, $trackData, $volume)")
    // First check if the speaker has a master that should play the notification.
    if (state.zone == "client") {
        return parent.boseZoneGetMaster()?.playSoundAndTrack(soundUri, duration, trackData, volume)
    }
    def action = playTrackAndRestore(soundUri, duration, volume)
    if (trackData?.name) {
        TRACE("calling ${trackData?.name}")
        action << "${trackData?.name}"()
    }
    return action.flatten()
}

def TRACE(text) {
    log.trace "${text}"
}
