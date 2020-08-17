/**
 *  cast-web-device
 *
 *  Copyright 2017 Tobias Haerke
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
import org.json.JSONObject

preferences {
    input("configOn", "enum", title: "Switch on does?",
        required: false, multiple:false, value: "nothing", options: ["Play","Pause","Stop","Play preset 1","Play preset 2","Play preset 3","Play preset 4","Play preset 5","Play preset 6"])
    input("configOff", "enum", title: "Switch off does?",
        required: false, multiple:false, value: "nothing", options: ["Play","Pause","Stop","Play preset 1","Play preset 2","Play preset 3","Play preset 4","Play preset 5","Play preset 6"])
    input("configNext", "enum", title: "Next song does?",
        required: false, multiple:false, value: "nothing", options: ["Play","Pause","Stop","Next preset","Previous preset","Play preset 1","Play preset 2","Play preset 3","Play preset 4","Play preset 5","Play preset 6"])
    input("configPrev", "enum", title: "Previous song does?",
        required: false, multiple:false, value: "nothing", options: ["Play","Pause","Stop","Next preset","Previous preset","Play preset 1","Play preset 2","Play preset 3","Play preset 4","Play preset 5","Play preset 6"])
    input("configResume", "enum", title: "Resume/restore (if nothing was playing before) plays preset?",
        required: false, multiple:false, value: "nothing", options: ["1","2","3","4","5","6"])
    input("configLoglevel", "enum", title: "Log level?",
        required: false, multiple:false, value: "nothing", options: ["0","1","2","3","4"])
    input("googleTTS", "bool", title: "Use Google's TTS voice?", required: false)
    input("googleTTSLanguage", "enum", title: "Google TTS language?",
        required: false, multiple:false, value: "nothing", options: ["cs-CZ","da-DK","de-DE","en-AU","en-CA","en-GH","en-GB","en-IN","en-IE","en-KE","en-NZ","en-NG","en-PH","en-ZA","en-TZ","en-US","es-AR","es-BO","es-CL","es-CO","es-CR","es-EC","es-SV","es-ES","es-US","es-GT","es-HN","es-MX","es-PA","es-PY","es-PE","es-PR","es-DO","es-UY","es-VE","eu-ES","fr-CA","fr-FR","it-IT","lt-LT","hu-HU","nl-NL","nb-NO","pl-P","pt-BR","pt-PT","ro-RO","sk-SK","sl-SI","fi-FI","sv-SE","ta-IN","vi-VN","tr-TR","el-GR","bg-BG","ru-RU","sr-RS","he-IL","ar-AE","fa-IR","hi-IN","th-TH","ko-KR","cmn-Hant-TW","yue-Hant-HK","ja-JP","cmn-Hans-HK","cmn-Hans-CN"])
}
 
metadata {
    definition (name: "cast-web-device", namespace: "vervallsweg", author: "Tobias Haerke") {
        capability "Actuator"
        capability "Audio Notification"
        capability "Music Player"
        capability "Polling"
        capability "Refresh"
        capability "Speech Synthesis"
        capability "Switch"
        //capability "Health Check" //TODO: Implement health check

        command "checkForUpdate"
        command "preset1"
        command "preset2"
        command "preset3"
        command "preset4"
        command "preset5"
        command "preset6"
        command "playPreset", ["number"]
        command "playText", ["string"]
        command "playText", ["string", "number"]
        command "playTextAndResume", ["string"]
        command "playTextAndResume", ["string", "number"]
        command "playTextAndRestore", ["string"]
        command "playTextAndRestore", ["string", "number"]
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) { //, canChangeIcon: true
        multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4) {
            tileAttribute("device.status", key: "PRIMARY_CONTROL") { //recents, events tab, background of controls
                attributeState("paused", label:"Paused", icon:"st.sonos.pause-icon")
                attributeState("playing", label:"Playing", icon:"st.sonos.play-icon")
                attributeState("ready", label:"Ready to cast", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-cast-white-box-75-1200.png")
                attributeState("group", label:"Group playback", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-speaker-group-white-box-75-1200.png")
            }
            tileAttribute("device.status", key: "MEDIA_STATUS") { //devices overview
                attributeState("paused", label:"Paused", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-cast-gray-75-1200.png", action:"music Player.play", nextState: "playing")
                attributeState("playing", label:"Playing", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-cast-gray-75-1200.png", action:"music Player.pause", nextState: "paused", backgroundColor:"#00a0dc")
                attributeState("ready", label:"Ready", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-cast-gray-75-1200.png")
                attributeState("group", label:"Group", icon:"https://raw.githubusercontent.com/vervallsweg/smartthings/master/icn/ic-speaker-group-gray-75-1200.png", backgroundColor:"#00a0dc")
            }
            tileAttribute("device.status", key: "PREVIOUS_TRACK") {
                attributeState("status", action:"music Player.previousTrack", defaultState: true)
            }
            tileAttribute("device.status", key: "NEXT_TRACK") {
                attributeState("status", action:"music Player.nextTrack", defaultState: true)
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL", icon: "st.custom.sonos.unmuted") {
                attributeState("level", action:"music Player.setLevel", icon: "st.custom.sonos.unmuted")
            }
            tileAttribute ("device.mute", key: "MEDIA_MUTED") {
                attributeState("unmuted", action:"music Player.mute", nextState: "muted", icon: "st.custom.sonos.unmuted")
                attributeState("muted", action:"music Player.unmute", nextState: "unmuted", icon: "st.custom.sonos.muted")
            }
            tileAttribute("device.trackDescription", key: "MARQUEE") {
                attributeState("trackDescription", label:"${currentValue}", defaultState: true)
            }
        }
        
        standardTile("updateDeviceStatus", "device.connection", width: 2, height: 2, decoration: "flat") {
            state "val", label:'${currentValue}', action: "refresh", icon: "st.secondary.refresh-icon", backgroundColor: "#ffffff", defaultState: true
            state "disconnected", label:'${currentValue}', action: "refresh", icon: "st.secondary.refresh-icon", backgroundColor: "#e86d13", defaultState: false
        }
        
        standardTile("stop", "device", width: 2, height: 2, decoration: "flat") {
            state "val", label: '', action: "music Player.stop", icon: "st.sonos.stop-btn", backgroundColor: "#ffffff", defaultState: true
        }
        
        valueTile("updateStatus", "device.updateStatus", width: 2, height: 2) {
            state "val", label:'${currentValue}', defaultState: true, action: "checkForUpdate"
        }
        
        standardTile("preset1", "device.preset1Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset1', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset1', backgroundColor: "#00a0dc", defaultState: false
        }
        
        standardTile("preset2", "device.preset2Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset2', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset2', backgroundColor: "#00a0dc", defaultState: false
        }
        
        standardTile("preset3", "device.preset3Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset3', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset3', backgroundColor: "#00a0dc", defaultState: false
        }
        
        standardTile("preset4", "device.preset4Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset4', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset4', backgroundColor: "#00a0dc", defaultState: false
        }
        
        standardTile("preset5", "device.preset5Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset5', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset5', backgroundColor: "#00a0dc", defaultState: false
        }
        
        standardTile("preset6", "device.preset6Name", width: 2, height: 1, decoration: "flat") {
            state "val", label:'${currentValue}', action:'preset6', defaultState: true
            state "Playing", label:'-\n ${currentValue} \n-', action:'preset6', backgroundColor: "#00a0dc", defaultState: false
        }
        
        main "mediaMulti"
        details(["mediaMulti",
                "updateDeviceStatus",
                "stop",
                "updateStatus",
                "preset1",
                "preset2",
                "preset3",
                "preset4",
                "preset5",
                "preset6"
                ])
    }
}

// Device handler states
def installed() {
    logger('debug', "Executing 'installed'")
    log.debug "installed"
    
    //Preset, update-status tiles
    refresh()
    sendEvent(name: "updateStatus", value: ("Version "+getThisVersion() + "\nClick to check for updates"), displayed: false)
    parsePresets()
    refresh() //If callback exists already
}

def updated() {
    logger('debug', "Executing 'updated'")
    log.debug "updated"
    
    //Preset, update-status tiles
    refresh()
    sendEvent(name: "updateStatus", value: ("Version "+getThisVersion() + "\nClick to check for updates"), displayed: false)
    parsePresets()
}

def refresh() {
    apiCall('/', true);
}

// parse events into attributes
def parse(String description) {
    try {
        logger('debug', "'parse', parsing: '${description}'")
        def msg = parseLanMessage(description)
        logger('debug', 'parse, msg.json: ' + msg.json)
        
        if(msg.json!=null){
            if(!msg.json.response) {
                if(msg.json.status) {
                    setTrackData(msg.json.status)
                    generateTrackDescription()
                }
                if(msg.json.connection) {
                    logger('debug', "msg.json.connection: "+msg.json.connection)
                    sendEvent(name: "connection", value: msg.json.connection, displayed:false)
                }
            } else if( !msg.json.response.equals('ok') ) {
                logger('error', "json response not ok: " + msg.json)
            }
        }
    } catch (e) {
        logger('error', "Exception caught while parsing data: "+e) //TODO: Health check
    }
}

// handle commands
def play() {
    logger('debug', "Executing 'play'")
    apiCall('/play', true);
}

def pause() {
    logger('debug', "Executing 'pause'")
    apiCall('/pause', true);
}

def stop() {
    logger('debug', "Executing 'stop'")
    apiCall('/stop', true);
}

def nextTrack() {
    logger('debug', "Executing 'nextTrack' encode: ")
    selectableAction(settings.configNext)
}

def previousTrack() {
    logger('debug', "Executing 'previousTrack'")
    selectableAction(settings.configPrev)
}

def setLevel(level) {
    logger('debug', "Executing 'setLevel', level: " + level)
    double lvl
    try { lvl = (double) level; } catch (e) {
        lvl = Double.parseDouble(level)
    }
    if( lvl == device.currentValue("level") ){
        logger('debug', "Setting group level: " + level)
        apiCall('/volume/'+lvl+'/group', true)
        return
    }
    apiCall('/volume/'+lvl, true)
}

def mute() {
    logger('debug', "Executing 'mute'")
    apiCall('/muted/true', true)
}

def unmute() {
    logger('debug', "Executing 'unmute'")
    apiCall('/muted/false', true)
}

def setTrack(trackToSet) {
    logger('debug', "Executing 'setTrack'")
    return playTrack(trackToSet)
}

def resumeTrack(trackToSet) {
    logger('debug', "Executing 'resumeTrack'")
    return playTrack(trackToSet)
}

def restoreTrack(trackToSet) {
    logger('debug', "Executing 'restoreTrack'")
    return playTrack(trackToSet)
}

def removePresetMediaSubtitle(mediaSubtitle) {
    if( isPreset(mediaSubtitle) ) {
        setTrackData( ["preset":getPresetNumber(mediaSubtitle)] )
        logger('debug', "'removePresetMediaSubtitle, new: '" + mediaSubtitle.substring(0, mediaSubtitle.length()-10) ) 
        return mediaSubtitle.substring(0, mediaSubtitle.length()-10) //remove substr[0, length-presetN] ' - Preset N'
    }
    removeTrackData(['preset'])
    return mediaSubtitle
}

def isPreset(mediaSubtitle) {
    if( mediaSubtitle.contains(" - Preset ") ) {
        return true
    }
    return false
}

def getPresetNumber(mediaSubtitle) {
    logger( 'debug', "'getPresetNumber', preset: "+mediaSubtitle.substring(mediaSubtitle.length() - 1) )
    return mediaSubtitle.substring(mediaSubtitle.length() - 1)
}

def parsePresets(def excluding=7) { //was: setDefaultPresets
    logger('debug', "parsePresets() excluding: " + excluding)
    if( !getDataValue("presetObject") ) { setDefaultPresetObject() }
    
    try {
        JSONObject testPresets = new JSONObject( getDataValue("presetObject") )
        if(testPresets.length()<5) {setDefaultPresetObject()}
    } catch (Exception e) {
        logger('debug', "parsePresets() cannot parse JSON testPresets exception: " + e)
        setDefaultPresetObject()
    }
    
    JSONObject presets = new JSONObject( getDataValue("presetObject") )
    
    for(int i=0; i<presets.length(); i++) {
        if( ((i+1)+"") != (excluding+"") ) {
            def key = "preset"+(i+1)
            def mediaTitle = "Preset "+(i+1)
            try {
                if(presets.get(key)) {
                    if(presets.get(key).mediaTitle) {
                        logger('warn', key+" is old preset.")
                        mediaTitle = presets.get(key).get('mediaTitle')
                    } else {
                        logger('info', key+" is new preset.")
                        mediaTitle = presets.get(key)[0].get('mediaTitle')
                    }
                }
            } catch (Exception e) {
                logger('debug', mediaTitle+" not set.")
            }
            //logger('debug', "parsePresets i: "+i+", key: "+key+", mediaTitle: "+mediaTitle+", excluding: " + excluding)
            sendEvent(name: key+"Name", value: mediaTitle, displayed: false)
        }
    }
}

def setDefaultPresetObject() {
    //def defaultObject = '{"preset1":{"mediaTitle":"Preset 1","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""},"preset2":{"mediaTitle":"Preset 2","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""},"preset3":{"mediaTitle":"Preset 3","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""},"preset4": {"mediaTitle":"Preset 4","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""},"preset5":{"mediaTitle":"Preset 5","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""},"preset6":{"mediaTitle":"Preset 6","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}}'
    def defaultObject = '{"preset1":[{"mediaTitle":"Preset 1","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}],"preset2":[{"mediaTitle":"Preset 2","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}],"preset3":[{"mediaTitle":"Preset 3","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}],"preset4":[{"mediaTitle":"Preset 4","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}],"preset5":[{"mediaTitle":"Preset 5","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}],"preset6":[{"mediaTitle":"Preset 6","mediaSubtitle":"","mediaType":"","mediaUrl":"","mediaStreamType":"","mediaImageUrl":""}]}';
    updateDataValue("presetObject", defaultObject)
}

def playPreset(number) {
    def preset = getPresetObject(number)
    
    if (preset) {
        setMediaPlaybacks( preset.toString() )
    }   
}

def getPresetObject(number) {
    def key = "preset"+number
    def defaultMediaTitle = "Preset "+number
    logger('debug', "Executing 'playPreset': "+number+", key: "+key+", defaultMediaTitle: "+defaultMediaTitle)
    JSONObject presets = new JSONObject( getDataValue("presetObject") )
    if(presets.get(key).mediaTitle) {
        logger('debug', "getPresetObject() is old preset object")
        presets[key].put( "mediaSubtitle", (presets[key]["mediaSubtitle"]+" - Preset "+number) )
   
        if( presets.get(key).mediaTitle.equals(defaultMediaTitle) ) {
            logger('debug', "'getPresetObject' key: "+key+", is default!")
            return null
        }
        def newP = [ presets.get(key) ];
        presets.put(key, newP);
    } else {
        logger('debug', "getPresetObject() is new preset object")
        presets[key].each {
            it.put( "mediaSubtitle", (it["mediaSubtitle"]+" - Preset "+number) )
        }
        if( presets.get(key)[0].mediaTitle.equals(defaultMediaTitle) ) {
            logger('debug', "'getPresetObject' key: "+key+", is default!")
            return null
        }
    }
    
    return presets.get(key)
}

def nextPreset() {
    def nextPreset = 1
    if( getTrackData(['preset'])[0] ) {
        def currentPreset = getTrackData(['preset'])[0] as int
        if(currentPreset<6) { nextPreset = currentPreset+1 }
    }
    playPreset(nextPreset)
}

def previousPreset() {
    def nextPreset = 1
    if( getTrackData(['preset'])[0] ) {
        def currentPreset = getTrackData(['preset'])[0] as int
        if(currentPreset>1) { nextPreset = currentPreset-1 }
        else {nextPreset=6}
    }
    playPreset(nextPreset)
}

def preset1() {
    logger('debug', "Executing 'preset1'")
    playPreset(1)
}

def preset2() {
    logger('debug', "Executing 'preset2': ")
    playPreset(2)
}

def preset3() {
    logger('debug', "Executing 'preset3': ")
    playPreset(3)    
}

def preset4() {
    logger('debug', "Executing 'preset4': ")
    playPreset(4)
}

def preset5() {
    logger('debug', "Executing 'preset5': ")
    playPreset(5)
}

def preset6() {
    logger('debug', "Executing 'preset6': ")
    playPreset(6)
}

def on() {
    logger('debug', "Executing 'on'")
    selectableAction(settings.configOn)
}

def off() {
    logger('debug', "Executing 'off'")
    selectableAction(settings.configOff ?: 'Stop')
}

def speak(phrase, resume = false) {
    if(settings.googleTTS && settings.googleTTSLanguage){
        if(settings.googleTTS==true) {
            return playTrack( phrase, 0, 0, true, settings.googleTTSLanguage )
        }
    }
    return playTrack( textToSpeech(phrase, true).uri, 0, 0, true )
}
//AUDIO NOTIFICATION, TEXT
def playText(message, level = 0, resume = false) {
    logger('info', "playText, message: " + message + " level: " + level)
    
    if (level!=0&&level!=null) { setLevel(level) }
    return speak(message, true)
}

def playTextAndResume(message, level = 0, thirdValue = 0) {
    logger('info', "playTextAndResume, message: " + message + " level: " + level)
    playText(message, level, true)
}

def playTextAndRestore(message, level = 0, thirdValue = 0) {
    logger('info', "playTextAndRestore, message: " + message + " level: " + level)
    //TODO: Reset level to level before the message was played
    playText(message, level, true)
}

def playTrackAtVolume(trackToPlay, level = 0) {
    logger('info', "playTrackAtVolume" + trackToPlay)
    
    def url = "" + trackToPlay;
    return playTrack(url, level)
}
//AUDIO NOTIFICATION, TRACK
def playTrack(uri, level = 0, thirdValue = 0, resume = false, googleTTS = false) {
    logger('info', "Executing 'playTrack', uri: " + uri + " level: " + level + " resume: " + resume)

    if (level!=0&&level!=null) { setLevel(level) }
    
    def data = '{ "mediaType":"audio/mp3", "mediaUrl":"'+uri+'", "mediaStreamType":"BUFFERED", "mediaTitle":"SmartThings", "mediaSubtitle":"SmartThings playback", "mediaImageUrl":"https://lh3.googleusercontent.com/nQBLtHKqZycERjdjMGulMLMLDoPXnrZKYoJ8ijaVs8tDD6cypInQRtxgngk9SAXHkA=w300"}'
    if(googleTTS) {
        data = '{ "mediaType":"audio/mp3", "mediaUrl":"", "mediaStreamType":"BUFFERED", "mediaTitle":"'+uri+'", "mediaSubtitle":"SmartThings notification", "mediaImageUrl":"https://lh3.googleusercontent.com/nQBLtHKqZycERjdjMGulMLMLDoPXnrZKYoJ8ijaVs8tDD6cypInQRtxgngk9SAXHkA=w300", "googleTTS":"'+googleTTS+'"}'
    }
    if(resume) {
        def number = 0
        JSONObject preset = null
        if(settings.configResume) { number = settings.configResume }
        if(getTrackData(['preset'])[0]) { number = getTrackData(['preset'])[0] }
        log.warn 'number: '+number
        if(number > 0) {
            preset = getPresetObject(number)
        }
    
        if (preset) {
            preset.each {
                data = data + ', '+it.toString()
            }
            //data = data + ', '+preset.toString()
        }
    }
    data = "["+data+"]"
    log.warn 'playTrack() data: '+data
    return setMediaPlaybacks(data)
}

def playTrackAndResume(uri, level = 0) {
    logger('info', "Executing 'playTrackAndResume', uri: " + uri + " level: " + level)
    return playTrack(uri, level, 0, true)
}

def playTrackAndResume(String uri, String duration, level = 0) {
    logger('info', "Executing 'playTrackAndResume', uri: " + uri + " duration: " + duration + " level: " + level)
    return playTrack(uri, level, 0, true)
}

def playTrackAndRestore(uri, level = 0) {
    logger('info', "Executing 'playTrackAndRestore', uri: " + uri + " level: " + level)
    //TODO: Reset level to level before the track was played
    return playTrack(uri, level, 0, true)
}

def playTrackAndRestore(String uri, String duration, level = 0) {
    logger('info', "Executing 'playTrackAndRestore', uri: " + uri + " duration: " + duration + " level: " + level)
    //TODO: Reset level to level before the track was played
    return playTrack(uri, level, 0, true)
}

def generateTrackDescription() {
    def trackDescription = getTrackData(["title"])[0] + "\n" + getTrackData(["application"])[0] + "\n" + removePresetMediaSubtitle(getTrackData(["subtitle"])[0])
    
    logger('debug', "Executing 'generateTrackDescription', trackDescription: "+ trackDescription)
    sendEvent(name: "trackDescription", value: trackDescription, displayed:false)
}

def setTrackData(newTrackData) {
    JSONObject currentTrackData = new JSONObject( device.currentValue("trackData") ?: "{}" )
    logger('debug', "setTrackData() currentTrackData: "+currentTrackData+", newTrackData: "+newTrackData)
    def changed = false
    
    newTrackData.each { key, value ->
        if(key=='connection'||key=='volume'||key=='muted'||key=='application'||key=='status'||key=='title'||key=='subtitle'||key=='image'||key=='preset'||key=='groupPlayback') {
            if(currentTrackData.has(key)) {
                if(currentTrackData.get(key)==value) { return }
            }
            currentTrackData.put(key, value); changed=true;
            if(currentTrackData.has('volume')) {
                sendEvent(name: "level", value: currentTrackData.get('volume'), unit: "%", changed: true)
            }
            if(currentTrackData.has('muted')) {
                if(currentTrackData.get('muted')) {
                    sendEvent(name: "mute", value: "muted", changed: true)
                } else {
                    sendEvent(name: "mute", value: "unmuted", changed: true)
                }
            }
            if(currentTrackData.has('status')) {
                if( currentTrackData.get('status').equals("PLAYING") || currentTrackData.get('status').equals("PAUSED") ) {
                    if( currentTrackData.has('groupPlayback') ) {
                        if( currentTrackData.get('groupPlayback') ) {
                            sendEvent(name: "status", value: 'group', changed: true)
                            sendEvent(name: "switch", value: on, displayed: false)
                        } else {
                            sendEvent(name: "status", value: currentTrackData.get('status').toLowerCase(), changed: true)
                            sendEvent(name: "switch", value: on, displayed: false)
                        }
                    } else {
                        sendEvent(name: "status", value: currentTrackData.get('status').toLowerCase(), changed: true)
                        sendEvent(name: "switch", value: on, displayed: false)
                    }
                } else if( currentTrackData.get('application').equals("") || currentTrackData.get('application').equals("Backdrop") ) {
                    sendEvent(name: "status", value: "ready", changed: true)
                    sendEvent(name: "switch", value: off, displayed: false)
                }
            }
            if(currentTrackData.has('preset')) {
                logger( 'debug', "setTrackData() sendEvent presetName playing for: "+ currentTrackData.get('preset') )
                sendEvent(name: "preset"+currentTrackData.get('preset')+"Name", value: "Playing", displayed: false, changed: true)
                parsePresets( currentTrackData.get('preset') )
            }
        }
    }
    
    if(changed){
        logger('debug', "sendEvent trackdata, currentTrackData: "+currentTrackData)
        sendEvent(name: "trackData", value: currentTrackData, displayed:false)
    }
}

def getTrackData(keys) {
    def returnValues = []
    logger('debug', "getTrackData, keys: "+keys)
    JSONObject trackData = new JSONObject( device.currentValue("trackData") ?: "{}" )
    
    keys.each {
        def defaultValue = null
        if( it.equals('title') || it.equals('subtitle') ) { defaultValue="--" }
        if( it.equals('application') ) { defaultValue="Ready to cast" }
        
        returnValues.add( trackData.optString(it, defaultValue) ?: defaultValue )
    }
    
    return returnValues
}

def removeTrackData(keys) {
    JSONObject trackData = new JSONObject( device.currentValue("trackData") ?: "{}" )
    keys.each{
        if( trackData.has( it ) ) {
            if( it.equals('preset') ) { 
                logger('debug', 'removeTrackData, resetPresetName for: ' + getTrackData(['preset'])[0])
                parsePresets()
            }
            logger('debug', "removeTrackData, removing key: "+it+", value: "+trackData.get(it))
            trackData.remove(it)
        }
    }
    sendEvent(name: "trackData", value: trackData, displayed:false)
}

// GOOGLE CAST
def setMediaPlayback(mediaType, mediaUrl, mediaStreamType, mediaTitle, mediaSubtitle, mediaImageUrl) {
    apiCall("/playMedia", true, '[ { "contentType":"'+mediaType+'", "mediaUrl":"'+mediaUrl+'", "mediaStreamType":"'+mediaStreamType+'", "mediaTitle":"'+mediaTitle+'", "mediaSubtitle":"'+mediaSubtitle+'", "mediaImageUrl":"'+mediaImageUrl+'" } ]')
}

def setMediaPlaybacks(def data) {
    apiCall("/playMedia", true, data)
}

// NETWORKING STUFF
def apiCall(String path, def dev, def media=null) {
    if (dev) {
        path = '/device/' + device.deviceNetworkId + path
    }
    if ( path.contains('subscribe') ) {
        def hub = location.hubs[0]
        path = path + '/' + hub.localIP + ':' + hub.localSrvPortTCP
    }
    if (media) {
        sendHttpPost(getDataValue('apiHost'), path, media)
        return
    }
    sendHttpRequest(getDataValue('apiHost'), path)  
}

def sendHttpRequest(String host, String path, def defaultCallback=hubResponseReceived) {
    logger('debug', "Executing 'sendHttpRequest' host: "+host+" path: "+path)
    sendHubCommand(new physicalgraph.device.HubAction("""GET ${path} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: defaultCallback]))
}

def sendHttpPost(String host, String path, def data) {
    logger('debug', "Executing 'sendHttpPost' host: "+host+" path: "+path+" data: "+data+" data.length():"+data.length()+1)
    def ha = new physicalgraph.device.HubAction("""POST ${path} HTTP/1.1\r\nHOST: $host\r\nContent-Type: application/json\r\nContent-Length:${data.length()+1}\r\n\r\n ${data}""", physicalgraph.device.Protocol.LAN, host, [callback: hubResponseReceived])
    logger('debug', "HubAction: "+ha)
    sendHubCommand(ha)
}

void hubResponseReceived(physicalgraph.device.HubResponse hubResponse) {
    parse(hubResponse.description)
}

// HELPERS
def getTimeStamp() {
    Date now = new Date(); 
    def timeStamp = (long)(now.getTime()/1000)
    logger('info', "Timestamp generated: "+timeStamp)
    return timeStamp;
}

def urlEncode(String) {
    return java.net.URLEncoder.encode(String, "UTF-8")
}

def selectableAction(action) {
    if( action.equals("Play") ) { play() }
    if( action.equals("Pause") ) { pause() }
    if( action.equals("Stop") ) { stop() }
    if( action.equals("Play preset 1") ) { playPreset(1) }
    if( action.equals("Play preset 2") ) { playPreset(2) }
    if( action.equals("Play preset 3") ) { playPreset(3) }
    if( action.equals("Play preset 4") ) { playPreset(4) }
    if( action.equals("Play preset 5") ) { playPreset(5) }
    if( action.equals("Play preset 6") ) { playPreset(6) }
    if( action.equals("Next preset") ) { nextPreset() }
    if( action.equals("Previous preset") ) { previousPreset() }
}

//UPDATE
def getThisVersion() {
    return "1.2.0"
}

def getLatestVersion() {
    try {
        httpGet([uri: "https://raw.githubusercontent.com/vervallsweg/smartthings/master/devicetypes/vervallsweg/cast-web.src/version.json"]) { resp ->
            logger('debug', "response status: ${resp.status}")
            String data = "${resp.getData()}"
            logger('debug', "data: ${data}")
            
            if(resp.status==200 && data!=null) {
                return parseJson(data)
            } else {
                return null
            }
        }
    } catch (e) {
        logger('error', "something went wrong: $e")
        return null
    }
}

def checkForUpdate() {
    def latestVersion = getLatestVersion()
    if (latestVersion == null) {
        logger('error', "Couldn't check for new version, thisVersion: " + getThisVersion())
        sendEvent(name: "updateStatus", value: ("Version "+getThisVersion() + "\n Error getting latest version \n"), displayed: false)
        return null
    } else {
        logger('info', "checkForUpdate thisVersion: " + getThisVersion() + ", latestVersion: " + getLatestVersion().version)
        sendEvent(name: "updateStatus", value: ("Current: "+getThisVersion() + "\nLatest: " + getLatestVersion().version), displayed: false)
    }
}

//DEBUGGING
def logger(level, message) {
    def logLevel=1
    if(settings.configLoglevel) {
        logLevel = settings.configLoglevel.toInteger() ?: 0
    }
    if(level=="error"&&logLevel>0) {
        log.error message
    }
    if(level=="warn"&&logLevel>1) {
        log.warn message
    }
    if(level=="info"&&logLevel>2) {
        log.info message
    }
    if(level=="debug"&&logLevel>3) {
        log.debug message
    }
}