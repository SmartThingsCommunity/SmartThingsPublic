/** 
 *  MediaRenderer Player v2.2.1
 *
 *  Author: SmartThings - Ulises Mujica (Ule)
 *
 */


preferences {
        input(name: "customDelay", type: "enum", title: "Delay before msg (seconds)", options: ["0","1","2","3","4","5"])
        input(name: "actionsDelay", type: "enum", title: "Delay between actions (seconds)", options: ["0","1","2","3"])
        input "noDelay", "bool", title: "Avoid Secure Delay", required: false, defaultValue: false
        input(name: "refreshFrequency", type: "enum", title: "Refresh frequency (minutes)", options:[0:"Auto",3:"3",5:"5",10:"10",15:"15",20:"20"])
        input "externalTTS", "bool", title: "Use External Text to Speech", required: false, defaultValue: false
        input "ttsApiKey", "text", title: "TTS Key", required: false
        input(name: "genre", type: "enum", title: "Music Genre", options:getGenres())
        input "useGenres", "bool", title: "Multiple Genres Instead of Genre", required: false, defaultValue: false
        input "genres", "text", title: "Multiple Genres, Write Exact Like Music Genre List", required: false, description:"genre1,genre2,genre3"
}
metadata {
	// Automatically generated. Make future change here.
	definition (name: "DLNA Player", namespace: "mujica", author: "SmartThings-Ulises Mujica") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
		capability "Polling"
		capability "Speech Synthesis"

		attribute "model", "string"
		attribute "trackUri", "string"
		attribute "transportUri", "string"
		attribute "trackNumber", "string"
		attribute "doNotDisturb", "string"
        attribute "btnMode", "string"
		


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
		command "setDoNotDisturb", ["string"]
		command "switchDoNotDisturb"
        command "switchBtnMode"
		command "speak", ["string"]
		command "playTrack", ["string","string"]
        command "playStation", ["number","number"]
        command "previousStation"
        command "nextStation"
        command "previousGenre"
        command "nextGenre"
	}

	// Main
	standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
		state "playing", label:'Playing', action:"music Player.stop", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
		state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "no_media_present", label:'No Media', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
        state "no_device_present", label:'No Present', icon:"st.Electronics.electronics16", backgroundColor:"#b6b6b4"
		state "grouped", label:'Grouped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
	}
/*
	// Row 1
	standardTile("nextTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
	}
	standardTile("play", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
	}
	standardTile("previousTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
	}
    */
  // Row 1a
	standardTile("nextTrack", "device.btnMode", width: 1, height: 1, decoration: "flat") {
        state "default", label:'', action:"nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff",nextState:"default"
        state "station", label:'Next Station', action:"nextStation", icon:"http://urbansa.com/icons/next-btn@2x.png", backgroundColor:"#ffffff",nextState:"station"
        state "genre", label:'Next Genre', action:"nextGenre", icon:"http://urbansa.com/icons/next-btn@2x.png", backgroundColor:"#ffffff",nextState:"genre"
	}
	standardTile("play", "device.btnMode", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"play", icon:"st.sonos.play-btn", nextState:"default", backgroundColor:"#ffffff"
        state "station", label:'Play Station', action:"playStation", icon:"http://urbansa.com/icons/play-btn@2x.png", nextState:"station", backgroundColor:"#ffffff"
        state "genre", label:'Play Station', action:"playStation", icon:"http://urbansa.com/icons/play-btn@2x.png", nextState:"genre", backgroundColor:"#ffffff"
	}
	standardTile("previousTrack", "device.btnMode", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff",nextState:"default"
        state "station", label:'Prev Station', action:"previousStation", icon:"http://urbansa.com/icons/previous-btn@2x.png", backgroundColor:"#ffffff",nextState:"station"
        state "genre", label:'Prev Genre', action:"previousGenre", icon:"http://urbansa.com/icons/previous-btn@2x.png", backgroundColor:"#ffffff",nextState:"genre"
	}
    
	// Row 2
	standardTile("status", "device.status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "playing", label:'Playing', action:"music Player.stop", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#ffffff"
		state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "no_media_present", label:'No Media', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "no_device_present", label:'No Present', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
	}
	standardTile("stop", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
		state "grouped", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
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
	standardTile("refreshPlayer", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
	}
	standardTile("doNotDisturb", "device.doNotDisturb", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "off", label:"MSG Enabled", action:"switchDoNotDisturb", icon:"st.alarm.beep.beep",nextState:"on"
		state "on", label:"MSG Disabled", action:"switchDoNotDisturb", icon:"st.custom.sonos.muted",nextState:"on_playing"
        state "on_playing", label:"MSG on Stopped", action:"switchDoNotDisturb", icon:"st.alarm.beep.beep",nextState:"off_playing"
        state "off_playing", label:"MSG on Playing", action:"switchDoNotDisturb", icon:"st.alarm.beep.beep",nextState:"off"
	}
    standardTile("btnMode", "device.btnMode", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "default", label:"Normal", action:"switchBtnMode", icon:"st.Electronics.electronics14",nextState:"station"
		state "station", label:"Station", action:"switchBtnMode", icon:"st.Entertainment.entertainment2",nextState:"genre"
        state "genre", label:"Genre", action:"switchBtnMode", icon:"st.Electronics.electronics1",nextState:"normal"
	}

	main "main"

	details([
		"previousTrack","play","nextTrack",
      /*  "previousTrackGenre","playGenre","nextTrackGenre",*/
		"status","stop","mute",
		"levelSliderControl",
		"currentSong",
		"refreshPlayer", "doNotDisturb","btnMode"
	])
}


def parse(description) {
    def results = []
	try {
		def msg = parseLanMessage(description)
        
        if (msg.headers)
		{
			def hdr = msg.header.split('\n')[0]
			if (hdr.size() > 36) {
				hdr = hdr[0..35] + "..."
			}

			def sid = ""
			if (msg.headers["sid"])
			{
				sid = msg.headers["sid"]
				sid -= "uuid:"
				sid = sid.trim()
			}

			if (!msg.body && msg.headers["timeout"] && sid ) {
				updateSid(sid)
			}
			else if (msg.xml) {

				// Process response to getVolume()
				def node = msg.xml.Body.GetVolumeResponse

				node = msg.xml.Body.GetVolumeResponse
				if (node.size()) {
					def currentVolume = node.CurrentVolume.text()
                    if (currentVolume) {
						sendEvent(name: "level", value: currentVolume, description: "$device.displayName Volume is $currentVolume")
					}
				}
				// Process response to getCurrentStatus()
				node = msg.xml.Body.GetTransportInfoResponse
				if (node.size()) {
					def currentStatus = statusText(node.CurrentTransportState.text())
					if (currentStatus) {
						state.lastStatusTime = new Date().time
						if (currentStatus != "TRANSITIONING") {
							sendEvent(name: "status", value: currentStatus, data: [source: 'xml.Body.GetTransportInfoResponse'])
							sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)

						}
					}
				}
				node = msg.xml.Body.GetTransportSettingsResponse
				if (node.size()) {
                	def currentPlayMode = node.PlayMode.text()
                    
                    if (currentPlayMode) {
						sendEvent(name: "playMode", value: currentPlayMode, description: "$device.displayName Play Mode is $currentPlayMode")
					}
				}
				node = msg.xml.Body.GetPositionInfoResponse
				if (node.size()) {
                	
				}
				

				// Process subscription update
				node = msg.xml.property.LastChange
				if (node?.text()?.size()>40  && node?.text()?.contains("</") ) {
					state.lastChange = true
					def xml1 = parseXml(node.text())

					// Play/pause status
					def currentStatus = statusText(xml1.InstanceID.TransportState.'@val'.text())
                    if (currentStatus) {
						state.lastStatusTime = new Date().time
						if (currentStatus != "TRANSITIONING") {
							updateDataValue('currentStatus', currentStatus)
							sendEvent(name: "status", value: currentStatus, data: currentStatus, displayed: false)
							sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)
						}
						if (currentStatus == "no_media_present") {sendEvent(name: "trackDescription",value: "",descriptionText: "", displayed: false)}
					}

					// Volume level 
					def currentLevel = xml1.InstanceID.Volume.find{it.'@channel' == 'Master'}.'@val'.text()
					currentLevel = currentLevel?:xml1.InstanceID.Volume.find{it.'@Channel' == 'Master'}.'@val'.text()

					if (currentLevel) {
						sendEvent(name: "level", value: currentLevel, description: "$device.displayName volume is $currentLevel")
					}
                    
					
					// PlayMode 
					def currentPlayMode = xml1.InstanceID.CurrentPlayMode.'@val'.text()
					
                    if (currentPlayMode) {
						sendEvent(name: "playMode", value: currentPlayMode, description: "$device.displayName Play Mode is $currentPlayMode")
					}
		

					// Mute status
					def currentMute = xml1.InstanceID.Mute.find{it.'@channel' == 'Master'}.'@val'.text()
                    currentMute = currentMute?:xml1.InstanceID.Mute.find{it.'@Channel' == 'Master'}.'@val'.text()
					if (currentMute) {
						def value = currentMute == "1" ? "muted" : "unmuted"
                        sendEvent(name: "mute", value: value, descriptionText: "$device.displayName is $value")
					}

					// Track data
					def trackUri = xml1.InstanceID.CurrentTrackURI.'@val'.text()
					def transportUri = xml1.InstanceID.AVTransportURI.'@val'.text()
					def trackNumber = xml1.InstanceID.CurrentTrack.'@val'.text()
					//|| trackUri.contains("translate.google.com/translate_tts") || transportUri.contains("translate.google.com/translate_tts")
					if (trackUri.contains("//s3.amazonaws.com/smartapp-") || transportUri.contains("//s3.amazonaws.com/smartapp-")   ) {
						log.trace "Skipping event generation for sound file $trackUri"
					}
					else {
						
						transportUri = transportUri ? transportUri : (state.transportUri ?: "")
						transportUri = trackNumber.length() >0 ? transportUri.replaceAll(/fii%3d.*?%26/, "fii%3d${Math.max(trackNumber.toInteger() - 1,0)}%26") : transportUri
						state.transportUri = transportUri
					
						def trackMeta = xml1.InstanceID.CurrentTrackMetaData.'@val'.text()
                        trackMeta = trackMeta == "NOT_IMPLEMENTED" ? "":trackMeta
						def transportMeta = xml1.InstanceID.AVTransportURIMetaData.'@val'.text()
						transportMeta = transportMeta == "NOT_IMPLEMENTED" ? "":transportMeta

						if (trackMeta || transportMeta) {
							def metaDataLoad = trackMeta  ? trackMeta : transportMeta
                            
                            def metaData = metaDataLoad?.startsWith("<item") ?  "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:pxn=\"urn:schemas-panasonic-com:pxn\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">$metaDataLoad</DIDL-Lite>": metaDataLoad 
							metaData = metaData.contains("dlna:dlna") &&  !metaData.contains("xmlns:dlna") ? metaData.replace("<DIDL-Lite"," <DIDL-Lite xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"") : metaData
							metaData = metaData.contains("pxn:ContentSourceType") &&  !metaData.contains("xmlns:pxn") ? metaData.replace("<DIDL-Lite"," <DIDL-Lite xmlns:pxn=\"urn:schemas-panasonic-com:pxn\"") : metaData
                            metaData = metaData != "<DIDL-Lite></DIDL-Lite><DIDL-Lite></DIDL-Lite>" ? metaData : null 
                            def parsedMetaData
                            try {
								if (metaData){
                                	parsedMetaData = parseXml(metaData)
                                }
       						}catch (e) {
                                log.debug "Error when parsing XML Metadata: " + e
                                log.debug "Help us to fix this error, report this incident"
                                log.debug metaData
                            }

							if (parsedMetaData){
                                def trackXml = parsedMetaData;
                                // Song properties
                                def currentName = trackXml.item.title.text()
                                def currentArtist = trackXml.item.creator.text()
                                def currentAlbum  = trackXml.item.album.text()
                                def currentItemId  = trackXml.item.'@id'.text()
                                def currentParentId  = trackXml.item.'@parentID'.text()
                                def currentTrackDescription = currentName

                                if (transportUri.contains(currentParentId) && currentItemId){
                                    transportUri = transportUri.replaceAll(/%26fid%3d.*?%26/, "%26fid%3d$currentItemId%26")
                                    state.transportUri = transportUri
                                }


                                def descriptionText = "$device.displayName is playing $currentTrackDescription"
                                if (currentArtist) {
                                    currentTrackDescription += " - $currentArtist"
                                    descriptionText += " by $currentArtist"
                                }

                                // Track Description Event
                                    sendEvent(name: "trackDescription",value: currentTrackDescription.replaceAll("_"," "),descriptionText: descriptionText	)

								// Track Data Event
								def station =  currentName

								def uri = transportUri ?  transportUri : trackUri
								def previousState = device.currentState("trackData")?.jsonValue
								def isDataStateChange = !previousState || (previousState.name != currentName || previousState.metaData != metaData)

								def trackDataValue = [
									station:  station ,
									name: currentName,
									artist: currentArtist,
									album: currentAlbum,
									trackNumber: trackNumber,
									status: currentStatus,
									level: currentLevel,
									uri: trackUri,
									trackUri: trackUri,
									transportUri: transportUri,
									enqueuedUri: "",
									metaData: metaData,
								]

								if (uri?.startsWith("dlna-playcontainer:") && trackUri && !trackUri?.startsWith("dlna-playcontainer:") && !trackUri?.startsWith("http://127.0.0.1")){
									results << createEvent(name: "trackData",value: trackDataValue.encodeAsJSON(),descriptionText: currentDescription,displayed: false,isStateChange: isDataStateChange	)
								}
								trackDataValue.uri = uri
								trackDataValue.station = uri?.startsWith("http://127.0.0.1")? "UNA-$station":(uri?.startsWith("dlna-playcontainer:")? "P.L. $station": station )
								results << createEvent(name: "trackData",value: trackDataValue.encodeAsJSON(),descriptionText: currentDescription,displayed: false,isStateChange: isDataStateChange	)

                            }
						}
					}
				}
				if (!results) {
					def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('\n\n','\n').encodeAsHTML() : ""
					results << createEvent(
						name: "mediaRendererMessage",
						value: "${msg.body.encodeAsMD5()}",
						description: description,
						descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
						data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
						isStateChange: false, displayed: false)
				}
			}
			else {
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
	}
	catch (Throwable t) {
		//results << createEvent(name: "parseError", value: "$t")
		sendEvent(name: "parseError", value: "$t", description: description)
		throw t
	}
	results
}

def installed() {
	sendEvent(name:"model",value:getDataValue("model"),isStateChange:true)
//	def result = []
//    result << delayAction(10000)
//	result << refresh()
//	result.flatten()
}

def on(){
	play()
}

def off(){
	stop()
}

def poll() {
log.trace "poll_()"
	timer()
}

def timer(){
    def eventTime = new Date().time

	state.gapTime = refreshFrequency > 0 ? (refreshFrequency? (refreshFrequency as Integer):0) * 60  : (parent.refreshMRInterval? (parent.refreshMRInterval as Integer):0) * 60 
    if ((state.lastRefreshTime ?:0) + (state.lastChange ? state.gapTime * 1000 : 300000)  <=  eventTime ){
    	log.trace "refresh()"
        refresh()
    }
}
def refresh() {
    def eventTime = new Date().time

    if( eventTime > state.secureEventTime ?:0)
    {
        if ((state.lastRefreshTime ?: 0) > (state.lastStatusTime ?:0)){
            sendEvent(name: "status", value: "no_device_present", data: "no_device_present", displayed: false)
        }
        state.lastRefreshTime = eventTime
        log.trace "Refresh()"
        def result = []
        //result << unsubscribe()
        //result << delayAction(10000)
        result << subscribe()
        result << getCurrentStatus()
        result << getVolume()
        result << getPlayMode()
        result << getCurrentMedia() 
        result.flatten()
    }else{
        log.trace "Refresh skipped"
    }

}

def setLevel(val)
{
	setLocalLevel(val)
}

def tileSetLevel(val)
{
	setLocalLevel(val)

}
def setDoNotDisturb(val)
{
	sendEvent(name:"doNotDisturb",value:val,isStateChange:true)
}
def setBtnMode(val)
{
	sendEvent(name:"btnMode",value:val,isStateChange:true)
}

// Always sets only this level
def setLocalLevel(val, delay=0) {
	def v = Math.max(Math.min(Math.round(val), 100), 0)
	def result = []
	if (delay) {
		result << delayAction(delay)
	}
	result << mediaRendererAction("SetVolume", "RenderingControl", getDataValue("rccurl") , [InstanceID:0, Channel:"Master", DesiredVolume:v])
	//result << delayAction(50)
	result << mediaRendererAction("GetVolume", "RenderingControl", getDataValue("rccurl"), [InstanceID:0, Channel:"Master"])
	result
}
// Always sets only this level
def setVolume(val) {
	mediaRendererAction("SetVolume", "RenderingControl", getDataValue("rccurl") , [InstanceID:0, Channel:"Master", DesiredVolume:Math.max(Math.min(Math.round(val), 100), 0)])
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


def play() {
	mediaRendererAction("Play")
	
}

def stop() {
	mediaRendererAction("Stop")
}

def pause() {
	mediaRendererAction("Pause")
}

def nextTrack() {
	mediaRendererAction("Next")
}

def previousTrack() {
	mediaRendererAction("Previous")
}

def nextStation() {
	playStation(1,0)
}

def previousStation() {
	playStation(-1,0)
}

def nextGenre() {
	playStation(0,1)
}

def previousGenre() {
	playStation(0,-1)
}


def seek(trackNumber) {
	mediaRendererAction("Seek", "AVTransport", getDataValue("avtcurl") , [InstanceID:0, Unit:"TRACK_NR", Target:trackNumber])
}

def mute()
{
	// TODO - handle like volume?
	mediaRendererAction("SetMute", "RenderingControl", getDataValue("rccurl"), [InstanceID:0, Channel:"Master", DesiredMute:1])
}

def unmute()
{
	// TODO - handle like volume?
	mediaRendererAction("SetMute", "RenderingControl", getDataValue("rccurl"), [InstanceID:0, Channel:"Master", DesiredMute:0])
}

def setPlayMode(mode)
{
	mediaRendererAction("SetPlayMode", [InstanceID:0, NewPlayMode:mode])
}
def switchDoNotDisturb(){
    switch(device.currentValue("doNotDisturb")) {
        case "off":
			setDoNotDisturb("on")
            break
        case "on":
			setDoNotDisturb("on_playing")
            break
		case "on_playing":
			setDoNotDisturb("off_playing")
            break
		default:
			setDoNotDisturb("off")
	}
}
def switchBtnMode(){
    switch(device.currentValue("btnMode")) {
        case "normal":
			setBtnMode("station")
            break
        case "station":
			setBtnMode("genre")
            break
		case "genre":
			setBtnMode("normal")
            break
		default:
			setBtnMode("station")
	}
}


def playByMode(uri, duration, volume,newTrack,mode) {
	def playTrack = false
    def restoreVolume = true
	def eventTime = new Date().time
	def track = device.currentState("trackData")?.jsonValue
	def currentVolume = device.currentState("level")?.integerValue
	def currentStatus = device.currentValue("status")
    def currentPlayMode = device.currentValue("playMode")
    def currentDoNotDisturb = device.currentValue("doNotDisturb")
	def level = volume as Integer
	def actionsDelayTime =  actionsDelay ? (actionsDelay as Integer) * 1000 :0
	def result = []
	duration = duration ? (duration as Integer) : 0
	switch(mode) {
        case 1:
			playTrack = currentStatus == "playing" ? true : false
            break
		case 3:
			track = newTrack
            restoreVolume = false
			playTrack = !track?.uri?.startsWith("http://127.0.0.1") ? true : false
            break
	}
	if( !(currentDoNotDisturb  == "on_playing" && currentStatus == "playing" ) && !(currentDoNotDisturb  == "off_playing" && currentStatus != "playing" ) && currentDoNotDisturb != "on"  ){//&& eventTime > state.secureEventTime ?:0
		if (uri){
			uri = cleanUri(uri)
            uri = uri +  ( uri.contains("?") ? "&":"?") + "ts=$eventTime"

            result << mediaRendererAction("Stop")
            result << delayAction(delayControl(1000) + actionsDelayTime)

            if (level && (currentVolume != level )) {
                //if(actionsDelayTime > 0){result << delayAction(actionsDelayTime)}
                result << setVolume(level)
				result << delayAction(delayControl(2200) + actionsDelayTime)
			}
            if (currentPlayMode != "NORMAL") {
                result << setPlayMode("NORMAL")
                result << delayAction(delayControl(2000) + actionsDelayTime)
			}
			result << setTrack(uri)
			result << delayAction(delayControl(2000) + actionsDelayTime)
			result << mediaRendererAction("Play")
			if (duration < 2){
				def matcher = uri =~ /[^\/]+.mp3/
				if (matcher){duration =  Math.max(Math.round(matcher[0].length()/8),2)}
			}
			def delayTime = (duration * 1000) + 3000
			delayTime = customDelay ? ((customDelay as Integer) * 1000) + delayTime : delayTime
			state.secureEventTime = eventTime + delayTime + 7000
			result << delayAction(delayTime)
		}
		if (track ) {

            result << mediaRendererAction("Stop")
            result << delayAction(delayControl(1000) + actionsDelayTime)

            if (level && restoreVolume ) {
                result << setVolume(currentVolume)
                result << delayAction(delayControl(2200) + actionsDelayTime)
			}
            if (currentPlayMode != "NORMAL") {
                result << setPlayMode(currentPlayMode)
                result << delayAction(delayControl(2000) + actionsDelayTime)
			}
			if (!track.uri.startsWith("http://127.0.0.1")){
				result << setTrack(track)
				result << delayAction(delayControl(2000) + actionsDelayTime)
			}
			if (playTrack) {
				if (!track.uri.startsWith("http://127.0.0.1")){
					result << mediaRendererAction("Play")
				}else{
                    result << mediaRendererAction("Next")
				}
                if (!state.lastChange){
                	result << getCurrentMedia()
            	}
			}else{
				result << mediaRendererAction("Stop")
			}
		}
		result = result.flatten()
	}
	else{
		log.trace "previous notification in progress or Do Not Disturb Activated"
	}
	result
}

def playTextAndResume(text, volume=null){
	def sound = externalTTS ? textToSpeechT(text) : safeTextToSpeech(text)
	playByMode(sound.uri, Math.max((sound.duration as Integer),1), volume, null, 1)
}
def playTextAndRestore(text, volume=null){
	def sound = externalTTS ? textToSpeechT(text) : safeTextToSpeech(text)
	playByMode(sound.uri, Math.max((sound.duration as Integer),1), volume, null, 2)
}
def playTextAndTrack(text, trackData, volume=null){
	def sound = externalTTS ? textToSpeechT(text) : safeTextToSpeech(text)
	playByMode(sound.uri, Math.max((sound.duration as Integer),1), volume, trackData, 3)
}
def playTrackAndResume(uri, duration, volume=null) {
    playByMode(uri, duration, volume, null, 1)
}
def playTrackAndRestore(uri, duration, volume=null) {
	playByMode(uri, duration, volume, null, 2)
}
def playSoundAndTrack(uri, duration, trackData, volume=null) {
    playByMode(uri, duration, volume, trackData, 3)
}

def playTrackAtVolume(String uri, volume) {
	playByMode(uri, 0, volume, null, 3)
}

def playTrack(String uri, metaData="") {
    def actionsDelayTime =  actionsDelay ? (actionsDelay as Integer) * 1000 :0
    def result = []

  //  result << mediaRendererAction("Stop")
  //  result << delayAction(100 + actionsDelayTime)
    result << setTrack(uri, metaData)
    result << delayAction(1000 + actionsDelayTime)
    result << mediaRendererAction("Play")
    if (!state.lastChange){
    	result << delayAction(2000 + actionsDelayTime)
    	result << getCurrentMedia()
    }
	result.flatten()
}

def playTrack(Map trackData) {
	def result = []
    result << setTrack(trackData)
	result << mediaRendererAction("Play")
    if (!state.lastChange){
        result << getCurrentMedia()
    }
	result.flatten()
}



def setTrack(Map trackData) {
	setTrack(trackData.uri, trackData?.metaData)
}

def setTrack(String uri, metaData="")
{
	//metaData = metaData?:"<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.musicTrack</upnp:class><upnp:album>SmartThings Catalog</upnp:album><upnp:artist>SmartThings</upnp:artist><upnp:albumArtURI>https://graph.api.smartthings.com/api/devices/icons/st.Entertainment.entertainment2-icn?displaySize=2x</upnp:albumArtURI><dc:title>SmartThings Message</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01500000000000000000000000000000\" >${groovy.xml.XmlUtil.escapeXml(uri)} </res></item> </DIDL-Lite>"
    metaData = metaData?:cleanUri("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.audioBroadcast</upnp:class><upnp:album>SmartThings Catalog</upnp:album><upnp:artist>SmartThings</upnp:artist><upnp:albumArtURI>https://graph.api.smartthings.com/api/devices/icons/st.Entertainment.entertainment2-icn?displaySize=2x</upnp:albumArtURI><dc:title>SmartThings Message</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:*\" >${groovy.xml.XmlUtil.escapeXml(uri)} </res></item> </DIDL-Lite>")
    metaData = removeAccents(metaData)
    metaData = new String(metaData.getBytes("ASCII"), "UTF-8")
    mediaRendererAction("SetAVTransportURI", [InstanceID:0, CurrentURI:cleanUri(uri),CurrentURIMetaData:cleanUri(metaData)])
}

def resumeTrack(Map trackData = null) {
	def result = []
    def actionsDelayTime =  actionsDelay ? (actionsDelay as Integer) * 1000 :0
    result << restoreTrack(trackData)
    result << delayAction(2000 + actionsDelayTime)
	result << mediaRendererAction("Play")
	result.flatten()
}

def restoreTrack(Map trackData = null) {
	def result = []
	def data = trackData
	if (!data) {
		data = device.currentState("trackData")?.jsonValue
	}
	if (data) {
		result << mediaRendererAction("SetAVTransportURI", [InstanceID:0, CurrentURI:cleanUri(data.uri), CurrentURIMetaData:cleanUri(data.metaData)])
	}
	else {
		log.warn "Previous track data not found"
	}
	result
}

def playText(String msg) {
	def result = setText(msg)
	result << mediaRendererAction("Play")
}

def setText(String msg) {
	def sound = externalTTS ? textToSpeechT(msg) : safeTextToSpeech(msg)
	setTrack(sound.uri)
}

def speak(String msg){
	playTextAndResume(msg, null)
}

// Custom commands

def subscribe() {
	def result = []
	result << subscribeAction(getDataValue("rceurl"))
	result << delayAction(2500)
	result << subscribeAction(getDataValue("avteurl"))
	result << delayAction(2500)
	result
}
def unsubscribe() {
    def result = [
		unsubscribeAction(getDataValue("avteurl"), device.getDataValue('subscriptionId')),
		unsubscribeAction(getDataValue("rceurl"), device.getDataValue('subscriptionId')),
		unsubscribeAction(getDataValue("avteurl"), device.getDataValue('subscriptionId1')),
		unsubscribeAction(getDataValue("rceurl"), device.getDataValue('subscriptionId1')),
	]
	//updateDataValue("subscriptionId", "")
	//updateDataValue("subscriptionId1", "")
	result
}

def getVolume()
{
	mediaRendererAction("GetVolume", "RenderingControl", getDataValue("rccurl"), [InstanceID:0, Channel:"Master"])
}

def getPlayMode()
{
	mediaRendererAction("GetTransportSettings", [InstanceID:0])
}
def getCurrentMedia()
{
	mediaRendererAction("GetPositionInfo", [InstanceID:0])
}

def getCurrentStatus() //transport info
{
	mediaRendererAction("GetTransportInfo", [InstanceID:0])
}

def getSystemString()
{
	mediaRendererAction("GetString", "SystemProperties", "/SystemProperties/Control", [VariableName:"UMTracking"])
}

private messageFilename(String msg) {
	msg.toLowerCase().replaceAll(/[^a-zA-Z0-9]+/,'_')
}

private getCallBackAddress()
{
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private mediaRendererAction(String action) {
	def result
	if(action=="Play"){
		result = mediaRendererAction(action, "AVTransport", getDataValue("avtcurl"), [InstanceID:0, Speed:1])
    }
	else if (action=="Mute"){
		result = mediaRendererAction("SetMute", "RenderingControl", getDataValue("rccurl"), [InstanceID: 0, Channel:"Master", DesiredMute:1])
	}
	else if (action=="UnMute"){
		result = mediaRendererAction("SetMute", "RenderingControl", getDataValue("rccurl"), [InstanceID: 0, Channel:"Master", DesiredMute:0])
	}
	else{
		result = mediaRendererAction(action, "AVTransport", getDataValue("avtcurl"), [InstanceID:0])
    }
	result
}

private mediaRendererAction(String action, Map body) {
	mediaRendererAction(action, "AVTransport", getDataValue("avtcurl"), body)
}

private mediaRendererAction(String action, String service, String path, Map body = [InstanceID:0, Speed:1]) {

    def result = new physicalgraph.device.HubSoapAction(
		path:    path ?: "/MediaRenderer/$service/Control",
		urn:     "urn:schemas-upnp-org:service:$service:1",
		action:  action,
		body:    body,
		headers: [Host:getHostAddress(), CONNECTION: "close"]
	)
	result
}

private subscribeAction(path, callbackPath="") {
	def address = getCallBackAddress()
	def ip = getHostAddress()

	def result = new physicalgraph.device.HubAction(
		method: "SUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			CALLBACK: "<http://${address}/notify$callbackPath>",
			NT: "upnp:event",
			TIMEOUT: "Second-${state.gapTime ?:300}"])
	result
}


private unsubscribeAction(path, sid) {
	def ip = getHostAddress()
	def result = new physicalgraph.device.HubAction(
		method: "UNSUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			SID: "uuid:${sid}"])
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
	def parts = getDataValue("dni")?.split(":")
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
		case "NO_MEDIA_PRESENT":
			return "no_media_present"
        case "NO_DEVICE_PRESENT":
        	retun "no_device_present"
		default:
			return s
	}
}

private updateSid(sid) {
	if (sid) {
		def sid0 = device.getDataValue('subscriptionId')
		def sid1 = device.getDataValue('subscriptionId1')
		def sidNumber = device.getDataValue('sidNumber') ?: "0"
		if (sidNumber == "0") {
			if (sid != sid1) {
				updateDataValue("subscriptionId", sid)
				updateDataValue("sidNumber", "1")
			}
		}
		else {
			if (sid != sid0) {
				updateDataValue("subscriptionId1", sid)
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

private cleanUri(uri) {
    def model = getDataValue("model")
    if (uri){
        uri = uri.replace("https:","http:")
        if (!model?.toLowerCase()?.contains("sonos")){
        	uri = uri.replace("x-rincon-mp3radio:","http:")
        }
    }
    return uri
}


private textToSpeechT(message){
    if (message) {
    	if (ttsApiKey){
            [uri: "x-rincon-mp3radio://api.voicerss.org/" + "?key=$ttsApiKey&hl=en-us&r=0&f=48khz_16bit_mono&src=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        }else{
        	message = message.length() >100 ? message[0..90] :message
        	[uri: "x-rincon-mp3radio://www.translate.google.com/translate_tts?tl=en&client=t&q=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
     	}
    }else{
    	[uri: "https://s3.amazonaws.com/smartapp-media/tts/633e22db83b7469c960ff1de955295f57915bd9a.mp3", duration: "10"]
    }
}

private delayControl(time){
	noDelay?0:time
}

private safeTextToSpeech(message) {
	message = message?:"You selected the Text to Speach Function but did not enter a Message"
    try {
        textToSpeech(message)
    }
    catch (Throwable t) {
        log.error t
        textToSpeechT(message)
    }
}

private removeAccents(String s) {
    s = s.replaceAll("[áàâãä]","a");
    s = s.replaceAll("[éèêë]","e");
    s = s.replaceAll("[íìîï]","i");
    s = s.replaceAll("[óòôõö]","o");
    s = s.replaceAll("[úùûü]","u");
    s = s.replaceAll("ç","c");

    s = s.replaceAll("[ÁÀÂÃÄ]","A");
    s = s.replaceAll("[ÉÈÊË]","E");
    s = s.replaceAll("[ÍÌÎÏ]","I");
    s = s.replaceAll("[ÓÒÔÕÖ]","O");
    s = s.replaceAll("[ÚÙÛÜ]","U");
    s = s.replaceAll("Ç","C");

    return s;
}


def getGenres(){
	["<<<  Alternative  >>>","Adult Alternative","Britpop","Classic Alternative","College","Dancepunk","Dream Pop","Emo","Goth","Grunge","Hardcore","Indie Pop","Indie Rock","Industrial","LoFi","Modern Rock","New Wave","Noise Pop","Post Punk","Power Pop","Punk","Ska","Xtreme","<<<  Blues  >>>","Acoustic Blues","Cajun and Zydeco","Chicago Blues","Contemporary Blues","Country Blues","Delta Blues","Electric Blues","<<<  Classical  >>>","Baroque","Chamber","Choral","Classical Period","Early Classical","Impressionist","Modern","Opera","Piano","Romantic","Symphony","<<<  Country  >>>","Alt Country","Americana","Bluegrass","Classic Country","Contemporary Bluegrass","Contemporary Country","Honky Tonk","Hot Country Hits","Western","<<<  Decades  >>>","00s","30s","40s","50s","60s","70s","80s","90s","<<<  Easy Listening  >>>","Exotica","Light Rock","Lounge","Orchestral Pop","Polka","Space Age Pop","<<<  Electronic  >>>","Acid House","Ambient","Big Beat","Breakbeat","Dance","Demo","Disco","Downtempo","Drum and Bass","Dubstep","Electro","Garage","Hard House","House","IDM","Jungle","Progressive","Techno","Trance","Tribal","Trip Hop","<<<  Folk  >>>","Alternative Folk","Contemporary Folk","Folk Rock","New Acoustic","Old Time","Traditional Folk","World Folk","<<<  Inspirational  >>>","Christian","Christian Metal","Christian Rap","Christian Rock","Classic Christian","Contemporary Gospel","Gospel","Praise and Worship","Sermons and Services","Southern Gospel","Traditional Gospel","<<<  International  >>>","African","Afrikaans","Arabic","Asian","Bollywood","Brazilian","Caribbean","Celtic","Chinese","Creole","European","Filipino","French","German","Greek","Hawaiian and Pacific","Hebrew","Hindi","Indian","Islamic","Japanese","Klezmer","Korean","Mediterranean","Middle Eastern","North American","Russian","Soca","South American","Tamil","Turkish","Worldbeat","Zouk","<<<  Jazz  >>>","Acid Jazz","Avant Garde","Big Band","Bop","Classic Jazz","Cool Jazz","Fusion","Hard Bop","Latin Jazz","Smooth Jazz","Swing","Vocal Jazz","World Fusion","<<<  Latin  >>>","Bachata","Banda","Bossa Nova","Cumbia","Flamenco","Latin Dance","Latin Pop","Latin Rap and Hip Hop","Latin Rock","Mariachi","Merengue","Ranchera","Reggaeton","Regional Mexican","Salsa","Samba","Tango","Tejano","Tropicalia","<<<  Metal  >>>","Black Metal","Classic Metal","Death Metal","Extreme Metal","Grindcore","Hair Metal","Heavy Metal","Metalcore","Power Metal","Progressive Metal","Rap Metal","Thrash Metal","<<<  Misc  >>>","<<<  New Age  >>>","Environmental","Ethnic Fusion","Healing","Meditation","Spiritual","<<<  Pop  >>>","Adult Contemporary","Barbershop","Bubblegum Pop","Dance Pop","Idols","JPOP","KPOP","Oldies","Soft Rock","Teen Pop","Top 40","World Pop","<<<  Public Radio  >>>","College","News","Sports","Talk","Weather","<<<  R&B and Urban  >>>","Classic R&B","Contemporary R&B","Doo Wop","Funk","Motown","Neo Soul","Quiet Storm","Soul","Urban Contemporary","<<<  Rap  >>>","Alternative Rap","Dirty South","East Coast Rap","Freestyle","Gangsta Rap","Hip Hop","Mixtapes","Old School","Turntablism","Underground Hip Hop","West Coast Rap","<<<  Reggae  >>>","Contemporary Reggae","Dancehall","Dub","Pop Reggae","Ragga","Reggae Roots","Rock Steady","<<<  Rock  >>>","Adult Album Alternative","British Invasion","Celtic Rock","Classic Rock","Garage Rock","Glam","Hard Rock","Jam Bands","JROCK","Piano Rock","Prog Rock","Psychedelic","Rock & Roll","Rockabilly","Singer and Songwriter","Surf","<<Seasonal and Holiday>>","Anniversary","Birthday","Christmas","Halloween","Hanukkah","Honeymoon","Kwanzaa","Valentine","Wedding","Winter","<<<  Soundtracks  >>>","Anime","Kids","Original Score","Showtunes","Video Game Music","<<<  Talk  >>>","BlogTalk","Comedy","Community","Educational","Government","News","Old Time Radio","Other Talk","Political","Scanner","Spoken Word","Sports","Technology","<<<  Themes  >>>","Adult","Best Of","Chill","Eclectic","Experimental","Female","Heartache","Instrumental","LGBT","Love and Romance","Party Mix","Patriotic","Rainy Day Mix","Reality","Sexy","Shuffle","Travel Mix","Tribute","Trippy","Work Mix"]
}

def playStation(incStatation = 0, incGenre = 0){
    def genre
    if (settings["useGenres"]){
        if (state.selectedGenres != settings["genres"]){
            state.selectedGenres = settings["genres"]
            state.genres = []
            settings["genres"]?.tokenize(",").each{item ->
				if (getGenres().collect{it.replaceAll(/<|>|  /, "").trim().toLowerCase()}.contains(item.trim().toLowerCase())){
                    state.genres << item.trim()
                }
            }
			log.trace "Genres parsed ${state.genres}"
        }
        if (incGenre == 1 || incGenre == -1) Collections.rotate(state.genres, -incGenre)
        genre = state.genres[0]
    }else{
        genre = settings["genre"]
    }

        
	if (genre){
          def stations = getStationGenre(genre)
        if (stations){

            if (incStatation == 1 || incStatation == -1) Collections.rotate(state[genre], -incStatation)
            def stationUri = getUriStation(stations[0].keySet()[0])  //"x-rincon-mp3radio://listen.radionomy.com/${radionomyStations[station[0]].key[0]}"
            log.trace "genre: $genre / station: ${stations[0].values().n[0]} / link: $stationUri"
			playTrack(stationUri,"<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.audioBroadcast</upnp:class><upnp:album>${groovy.xml.XmlUtil.escapeXml(genre)}</upnp:album><upnp:artist>SHOUTcast</upnp:artist><upnp:albumArtURI>http://www.radionomygroup.com/img/shoutcast-logo.png</upnp:albumArtURI><dc:title>${groovy.xml.XmlUtil.escapeXml(stations[0].values().n[0])}</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01500000000000000000000000000000\" >${groovy.xml.XmlUtil.escapeXml(stationUri)} </res></item> </DIDL-Lite>")
        }
    }
}

def getUriStation(id){
    def  uri
    def params = [
            uri: "http://www.shoutcast.com/Player/GetStreamUrl",
            body: [ station: id],
            contentType: "text/plain",
        ]
        try {
            httpPost(params) { resp ->
                uri = resp.data.text
               if (uri){
                uri =  uri.replaceAll("\"","");
               }
            }
        } catch (ex) {
            log.debug "something went wrong: $ex"
        }
	uri
}

def getStationGenre(genre){
    if (genre){
        if(!state[genre] || state[genre]?.size() == 0 ){
            state[genre] = []
                try {
                def params = [
                    uri: "http://www.shoutcast.com/Home/BrowseByGenre",
                    body: [
                        genrename: genre
                    ]
                ]
                httpPostJson(params)  { resp ->
                     resp.data.any { element ->
                        if (!element.IsRadionomy && !element.AACEnabled && element.Bitrate == 128 ){
                            state[genre] << ["${element.ID}":[n:"${element.Name}",a:""]]
                        }
                        if (state[genre].size() >=10 ){
                            return true // break
                        }
                    }
                }
            } catch (ex) {
                log.debug "something went wrong: $ex"
            }
        }
        state[genre]
    }else{
    	[]
    }
}