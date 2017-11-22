//DEPRECATED. INTEGRATION MOVED TO SUPER LAN CONNECT

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
        capability "Health Check"
        capability "Sensor"
        capability "Actuator"

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
    valueTile("nowplaying", "device.nowplaying", width: 2, height: 1, decoration:"flat") {
        state "nowplaying", label:'${currentValue}', action:"refresh.refresh"
    }

    standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        state "on", label: '${name}', action: "forceOff", icon: "st.Electronics.electronics16", backgroundColor: "#00a0dc", nextState:"turningOff"
        state "turningOff", label:'TURNING OFF', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
        state "off", label: '${name}', action: "forceOn", icon: "st.Electronics.electronics16", backgroundColor: "#ffffff", nextState:"turningOn"
        state "turningOn", label:'TURNING ON', icon:"st.Electronics.electronics16", backgroundColor:"#00a0dc"
    }
    valueTile("1", "device.station1", decoration: "flat", canChangeIcon: false) {
        state "station1", label:'${currentValue}', action:"preset1"
    }
    valueTile("2", "device.station2", decoration: "flat", canChangeIcon: false) {
        state "station2", label:'${currentValue}', action:"preset2"
    }
    valueTile("3", "device.station3", decoration: "flat", canChangeIcon: false) {
        state "station3", label:'${currentValue}', action:"preset3"
    }
    valueTile("4", "device.station4", decoration: "flat", canChangeIcon: false) {
        state "station4", label:'${currentValue}', action:"preset4"
    }
    valueTile("5", "device.station5", decoration: "flat", canChangeIcon: false) {
        state "station5", label:'${currentValue}', action:"preset5"
    }
    valueTile("6", "device.station6", decoration: "flat", canChangeIcon: false) {
        state "station6", label:'${currentValue}', action:"preset6"
    }
    valueTile("aux", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Auxillary\nInput', action:"aux"
    }

    standardTile("refresh", "device.nowplaying", decoration: "flat", canChangeIcon: false) {
        state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
    }

    controlTile("volume", "device.volume", "slider", height:1, width:3, range:"(0..100)") {
        state "volume", action:"music Player.setLevel"
    }

    standardTile("playpause", "device.playpause", decoration: "flat") {
        state "pause", label:'', icon:'st.sonos.play-btn', action:'music Player.play'
        state "play", label:'', icon:'st.sonos.pause-btn', action:'music Player.pause'
    }

    standardTile("prev", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn"
    }
    standardTile("next", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn"
    }

    valueTile("everywhere", "device.everywhere", width:2, height:1, decoration:"flat") {
        state "join", label:"Join\nEverywhere", action:"everywhereJoin"
        state "leave", label:"Leave\nEverywhere", action:"everywhereLeave"
        // Final state is used if the device is in a state where joining is not possible
        state "unavailable", label:"Not Available"
    }

    // Defines which tile to show in the overview
    main "switch"

    // Defines which tile(s) to show when user opens the detailed view
    details ([
        "nowplaying", "refresh",        // Row 1 (112)
        "prev", "playpause", "next",    // Row 2 (123)
        "volume",                       // Row 3 (111)
        "1", "2", "3",                  // Row 4 (123)
        "4", "5", "6",                  // Row 5 (123)
        "aux", "everywhere"])           // Row 6 (122)
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
def mute() { onAction("mute") }
def unmute() { onAction("unmute") }
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
    ]

    // No need to deal with non-XML data
    if (!data.headers || !data.headers?."content-type".contains("xml"))
        return null

    // Move any pending callbacks into ready state
    prepareCallbacks()

    def xml = new XmlSlurper().parseText(data.body)
    // Let each parser take a stab at it
    handlers.each { node,func ->
        if (xml.name() == node)
            actions << "$func"(xml)
    }
    // If we have callbacks waiting for this...
    actions << processCallbacks(xml)

    // Be nice and helpful
    if (actions.size() == 0) {
        log.warn "parse(): Unhandled data = " + lan
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
    startPoll()
}

/**
 * Called by health check if no events been generated in the last 12 minutes
 * If device doesn't respond it will be marked offline (not available)
 */
def ping() {
    TRACE("ping")
    boseSendGetNowPlaying()
}

/**
 * Schedule a 2 minute poll of the device to refresh the
 * tiles so the user gets the correct information.
 */
def startPoll() {
    TRACE("startPoll")
    unschedule()
    // Schedule 2 minute polling of speaker status (song average length is 3-4 minutes)
    def sec = Math.round(Math.floor(Math.random() * 60))
    //def cron = "$sec 0/5 * * * ?" // every 5 min
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
            boseSetPowerState(true)
            break
        case "off":
            boseSetNowPlaying(null, "STANDBY")
            boseSetPowerState(false)
            break
        case "volume":
            actions = boseSetVolume(data)
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
            boseSetNowPlaying(null, "REFRESH")
            actions = [boseRefreshNowPlaying(), boseGetPresets(), boseGetVolume(), boseGetEverywhereState()]
            break
        case "play":
            actions = [boseSetPlayMode(true), boseRefreshNowPlaying()]
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
            actions = boseSetMute(true)
            break
        case "unmute":
            actions = boseSetMute(false)
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
    sendEvent(name:"mute", value:(Boolean.toBoolean(xmlData.muteenabled.text()) ? "unmuted" : "muted"))

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
        def name = preset.ContentItem.itemName[0].text().replaceAll(~/ +/, "\n")
        if (name == "##TRANS_SONGS##")
            name = "Local\nPlaylist"
        sendEvent(name:"station${id}", value:name)
        missing = missing.findAll { it -> it != id }

        // Store the presets into the state for recall later
        state.preset["$id"] = XmlUtil.serialize(preset.ContentItem)
    }

    for (id in missing) {
        state.preset["$id"] = null
        sendEvent(name:"station${id}", value:"Preset $id\n\nNot set")
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
                sendEvent(name:"playpause", value:"play")
                break
            case "PAUSE_STATE":
            case "STOP_STATE":
                sendEvent(name:"playpause", value:"pause")
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
                nowplaying = "${xmlData.stationName.text()}\n\n${xmlData.description.text()}"
                break
            case "REFRESH":
                nowplaying = "Please wait"
                break
            case "SPOTIFY":
            case "DEEZER":
            case "PANDORA":
            case "IHEART":
                if (xmlData.ContentItem.itemName[0])
                    nowplaying += "[${xmlData.ContentItem.itemName[0].text()}]\n\n"
            case "STORED_MUSIC":
                nowplaying += "${xmlData.track.text()}"
                if (xmlData.artist)
                    nowplaying += "\nby\n${xmlData.artist.text()}"
                if (xmlData.album)
                    nowplaying += "\n\n(${xmlData.album.text()})"
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
        if (xmlData.attributes()['source'] == "STANDBY") {
            log.trace "nowPlaying reports standby: " + XmlUtil.serialize(xmlData)
            sendEvent(name:"switch", value:"off")
        } else {
            sendEvent(name:"switch", value:"on")
        }
        boseSetPlayerAttributes(xmlData)
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

    return needrefresh
}

/**
 * Updates the attributes exposed by the music Player capability
 *
 * @param xmlData The NowPlaying XML data
 */
def boseSetPlayerAttributes(xmlData) {
    // Refresh attributes
    def trackText = ""
    def trackDesc = ""
    def trackData = [:]

    switch (xmlData.attributes()['source']) {
        case "STANDBY":
            trackData["station"] = trackText = trackDesc = "Standby"
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
        case "STORED_MUSIC":
            trackText = trackDesc = "${xmlData.track.text()}"
            trackData["name"] = xmlData.track.text()
            if (xmlData.artist) {
                trackText += " by ${xmlData.artist.text()}"
                trackDesc += " - ${xmlData.artist.text()}"
                trackData["artist"] = xmlData.artist.text()
            }
            if (xmlData.album) {
                trackText += " (${xmlData.album.text()})"
                trackData["album"] = xmlData.album.text()
            }
            break
        case "INTERNET_RADIO":
            trackDesc = xmlData.stationName.text()
            trackText = xmlData.stationName.text() + ": " + xmlData.description.text()
            trackData["station"] = xmlData.stationName.text()
            break
        default:
            trackText = trackDesc = xmlData.ContentItem.itemName[0].text()
    }

    sendEvent(name:"trackDescription", value:trackDesc, descriptionText:trackText)
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
    def result = []
    int vol = Math.min(100, Math.max(level, 0))

    sendEvent(name:"volume", value:"${vol}")

    return [bosePOST("/volume", "<volume>${vol}</volume>"), boseGetVolume()]
}

/**
 * Sets the mute state, unfortunately, for now, we need to query current
 * state before taking action (no discrete mute/unmute)
 *
 * @param mute If true, mutes the system
 * @return command
 */
def boseSetMute(boolean mute) {
    queueCallback('volume', 'cb_boseSetMute', mute ? 'MUTE' : 'UNMUTE')
    return boseGetVolume()
}

/**
 * Callback for boseSetMute(), checks current state and changes it
 * if it doesn't match the requested state.
 *
 * @param xml The volume XML data
 * @param mute The new state of mute
 *
 * @return command
 */
def cb_boseSetMute(xml, mute) {
    def result = []
    if ((xml.muteenabled.text() == 'false' && mute == 'MUTE') ||
        (xml.muteenabled.text() == 'true' && mute == 'UNMUTE'))
    {
        result << boseKeypress("MUTE")
    }
    log.trace("muteunmute: " + ((mute == "MUTE") ? "unmute" : "mute"))
    sendEvent(name:"muteunmute", value:((mute == "MUTE") ? "unmute" : "mute"))
    return result
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

    if (!state.preset) {
        result << boseGetPresets()
        queueCallback('presets', 'cb_boseSetInput', input)
    } else {
        result << cb_boseSetInput(null, input)
    }
    return result
}

/**
 * Callback used by boseSetInput(), either called directly by
 * boseSetInput() if we already have presets, or called after
 * retreiving the presets for the first time.
 *
 * @param xml The presets XML data
 * @param input Desired input
 *
 * @return command
 *
 * @note Uses KEY commands for AUX, otherwise /select endpoint.
 *       Reason for this is latency. Since keypresses are done
 *       in pairs (press + release), you could accidentally change
 *       the preset if there is a long delay between the two.
 */
def cb_boseSetInput(xml, input) {
    def result = []

    if (input >= "1" && input <= "6" && state.preset["$input"])
        result << bosePOST("/select", state.preset["$input"])
    else if (input.toLowerCase() == "aux") {
        result << boseKeypress("AUX_INPUT")
    }

    // Horrible workaround... but we need to delay
    // the update by at least a few seconds...
    result << boseRefreshNowPlaying(3000)
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
    // Fix to get faster update of power status back from speaker after sending on/off
    // Instead of queuing the command to be sent after the refresh send it directly via sendHubCommand
    // Note: This is a temporary hack that should be replaced by a re-design of the
    // DTH to use sendHubCommand for all commands
    sendHubCommand(bosePOST("/key", "<key state=\"press\" sender=\"Gabbo\">POWER</key>")) 
    sendHubCommand(bosePOST("/key", "<key state=\"release\" sender=\"Gabbo\">POWER</key>"))
    sendHubCommand(boseGET("/now_playing"))
    if (enable) {
        queueCallback('nowPlaying', "cb_boseConfirmPowerOn", 5)
    }
}

/**
 * Callback function used by boseSetPowerState(), is used
 * to handle the fact that we only have a toggle for power.
 *
 * @param xml The XML data from nowPlaying
 * @param state The requested state
 *
 * @return command
 */
def cb_boseSetPowerState(xml, state) {
    def result = []
    if ( (xml.attributes()['source'] == "STANDBY" && state == "POWERON") ||
         (xml.attributes()['source'] != "STANDBY" && state == "POWEROFF") )
    {
        result << boseKeypress("POWER")
        if (state == "POWERON") {
            result << boseRefreshNowPlaying()
            queueCallback('nowPlaying', "cb_boseConfirmPowerOn", 5)
        }
    }
    return result.flatten()
}

/**
 * We're sometimes too quick on the draw and get a refreshed nowPlaying
 * which shows standby (essentially, the device has yet to completely
 * transition to awake state), so we need to poll a few times extra
 * to make sure we get it right.
 *
 * @param xml The XML data from nowPlaying
 * @param tries A counter which will decrease, once it reaches zero,
 *              we give up and assume that whatever we got was correct.
 * @return command
 */
def cb_boseConfirmPowerOn(xml, tries) {
    def result = []
    def attempt = tries as Integer
    log.warn "boseConfirmPowerOn() attempt #$attempt"
    if (xml.attributes()['source'] == "STANDBY" && attempt > 0) {
        result << boseRefreshNowPlaying()
        queueCallback('nowPlaying', "cb_boseConfirmPowerOn", attempt-1)
    }
    return result
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
 * Queues a callback function for when a specific XML root is received
 * Will execute on subsequent parse() call(s), never on the current
 * parse() call.
 *
 * @param root The root node that this callback should react to
 * @param func Name of the function
 * @param param Parameters for function (optional)
 */
def queueCallback(String root, String func, param=null) {
    if (!state.pending)
        state.pending = [:]
    if (!state.pending[root])
        state.pending[root] = []
    state.pending[root] << ["$func":"$param"]
}

/**
 * Transfers the pending callbacks into readiness state
 * so they can be executed by processCallbacks()
 *
 * This is needed to avoid reacting to queueCallbacks() within
 * the same loop.
 */
def prepareCallbacks() {
    if (!state.pending)
        return
    if (!state.ready)
        state.ready = [:]
    state.ready << state.pending
    state.pending = [:]
}

/**
 * Executes any ready callback for a specific root node
 * with associated parameter and then clears that entry.
 *
 * If a callback returns data, it's added to a list of
 * commands which is returned to the caller of this function
 *
 * Once a callback has been used, it's removed from the list
 * of queued callbacks (ie, it executes only once!)
 *
 * @param xml The XML data to be examined and delegated
 * @return list of commands
 */
def processCallbacks(xml) {
    def result = []

    if (!state.ready)
        return result

    if (state.ready[xml.name()]) {
        state.ready[xml.name()].each { callback ->
            callback.each { func, param ->
                if (func != "func") {
                    if (param)
                    result << "$func"(xml, param)
                    else
                        result << "$func"(xml)
                }
            }
        }
        state.ready.remove(xml.name())
    }
    return result.flatten()
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

def TRACE(text) {
    log.trace "${text}"
}