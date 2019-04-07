/**
 *  Copyright 2016 Eric Maycock
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
 *  Fosbaby Lullaby Control
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-01-27
 */
 
import groovy.util.XmlSlurper

metadata {
	definition (name: "Fosbaby Lullaby Control", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch") {
		capability "Actuator"
        capability "Indicator"
        capability "Switch"
		capability "Refresh"
        capability "Polling"
		capability "Sensor" 
        capability "Music Player"
        capability "Temperature Measurement"
        capability "Health Check"
        
        command "playOne"
        command "playTwo"
        command "playThree"
        command "playFour"
        command "playFive"
        command "playRandom"
        command "mode"
        command "timer"
	}

	simulator {
	}
    
    preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150", required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "88", required: true, displayDuringSetup: true)
        input("userName", "string", title:"User Name", required:true, displayDuringSetup:true)
        input("password", "password", title:"Password", required:false, displayDuringSetup:true)
        input("enableDebugging", "boolean", title:"Enable Debugging", value:false, required:false, displayDuringSetup:false)
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature",label:'${currentValue}°', icon:"st.Entertainment.entertainment2", backgroundColors:[
                	[value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
            }
        }
        
        standardTile("play", "device.status", inactiveLabel:false, decoration:"flat", width: 2, height: 2) {
            state "stopped", label:'', icon:"st.sonos.play-btn", nextState:"playing", action:"Music Player.play"
            state "playing", label:'', icon:"st.sonos.stop-btn", nextState:"stopped", action:"Music Player.stop"
        }

        standardTile("nextTrack", "device.status", inactiveLabel:false, decoration:"flat", width: 2, height: 2) {
            state "default", label:'', icon:"st.sonos.next-btn", action:"Music Player.nextTrack"
        }

        standardTile("previousTrack", "device.status", inactiveLabel:false, decoration:"flat", width: 2, height: 2) {
            state "default", label:'', icon:"st.sonos.previous-btn", action:"music Player.previousTrack"
        }

        controlTile("volume", "device.level", "slider", height:1, width:6, inactiveLabel:false) {
            state "level", action:"Music Player.setLevel"
        }
        
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on", nextState:"never"
			state "never", action:"indicator.indicatorWhenOn", icon:"st.indicators.never-lit", nextState:"when on"
		}
        standardTile("mode", "device.mode", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "order", label:"In Order", icon:"", nextState:"loopsong", action:"mode"
            state "loopsong", label:"Loop Song", icon:"", nextState:"looplist", action:"mode"
            state "looplist", label:"Loop List", icon:"", nextState:"order", action:"mode"
        }
        
        standardTile("timer", "device.timer", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"No Timer", icon:"", nextState:"10", action:"timer"
            state "10", label:"10 Minutes", icon:"", nextState:"20", action:"timer"
            state "20", label:"20 Minutes", icon:"", nextState:"30", action:"timer"
            state "30", label:"30 Minutes", icon:"", nextState:"off", action:"timer"
        }
        
        standardTile("playOne", "device.playOne", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Song 1", action:"playOne", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Song 1", action:"playOne", icon:"", backgroundColor:"#00a0dc"
        }
        standardTile("playTwo", "device.playTwo", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Song 2", action:"playTwo", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Song 2", action:"playTwo", icon:"", backgroundColor:"#00a0dc"
        }
        standardTile("playThree", "device.playThree", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Song 3", action:"playThree", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Song 3", action:"playThree", icon:"", backgroundColor:"#00a0dc"
        }
        standardTile("playFour", "device.playFour", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Song 4", action:"playFour", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Song 4", action:"playFour", icon:"", backgroundColor:"#00a0dc"
        }
        standardTile("playFive", "device.playFive", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Song 5", action:"playFive", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Song 5", action:"playFive", icon:"", backgroundColor:"#00a0dc"
        }
        standardTile("playRandom", "device.playRandom", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"Random", action:"playRandom", icon:"", backgroundColor:"#ffffff"
            state "on", label:"Random", action:"playRandom", icon:"", backgroundColor:"#00a0dc"
        }
        
    }

	main "main"
	details(["main", "volume",
             "previousTrack", "play", "nextTrack",
             "mode", "timer", "temperature",  
             "playOne", "playTwo", "playThree",
             "playFour", "playFive", "playRandom",
             "indicator", "refresh",])
}

def installed() {
	logging("installed()")
	configure()
}

def updated() {
	logging("updated()")
    configure()
}

def configure() {
	logging("configure()")
	logging("Configuring Device For SmartThings Use")
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    state.enableDebugging = settings.enableDebugging
    if (state.MAC != null) state.dni = setDeviceNetworkId(state.MAC)
    else if (ip != null && port != null) state.dni = setDeviceNetworkId(ip, port)
}

def parse(Map description) {
    def eventMap
    eventMap = [name:"$description.name", value:"$description.value"]
    [createEvent(eventMap), response(refresh())]
}

def parse(description) {
    def events = []
    def descMap = parseDescriptionAsMap(description)

    if (!state.MAC || state.MAC != descMap["mac"]) {
		logging("Mac address of device found ${descMap["mac"]}")
        updateDataValue("MAC", descMap["mac"])
        state.dni = state.MAC
	}
    
    def body = new String(descMap["body"].decodeBase64())

    def result = new XmlSlurper().parseText(body)
    def descriptionText = "$device.displayName is playing $currentTrackDescription"
    if (result.index != "") {
        logging("Current song is ${result.index}")
        toggleTiles(getSongName(result.index))
        events << createEvent(name: getSongName(result.index), value: "on", displayed:false)
        events << createEvent(name: "trackDescription", value: "Song $result.index", descriptionText: "$device.displayName is playing song $result.index")
    }
    if (result.dormantTime != "") {
        logging("Timer is currently set to ${ result.dormantTime == -1 ? 'off' : result.dormantTime}")
        events << createEvent(name:"timer", value: (result.dormantTime == -1 ? 'off' : result.dormantTime))
    }
    if (result.degree == "" && result.state != "") {
        logging("The device is currently ${ result.state == 1 ? 'playing' : 'stopped' }")
        events << createEvent(name:"status", value: (result.state == 1 ? 'playing' : 'stopped'))
    }
    if (result.mode != "") {
        logging("The device mode is currently \"$mode\"")
        def mode
        switch (result.mode.toInteger()) {
            case 1:
                mode = "order"
            break
            case 2:
                mode = "loopsong"
            break
            case 3:
                mode = "looplist"
            break
        }
        events << createEvent(name:"mode", value:mode)
    }
    if (result.degree != "") {
        logging("Current temperature is ${result.degree}")
        if(getTemperatureScale() == "C"){
	        events << createEvent([name:"temperature", value: Math.round(result.degree.toInteger() * 100) / 100])
	    } else {
            events << createEvent([name:"temperature", value: Math.round(celsiusToFahrenheit(result.degree.toInteger()) * 100) / 100])
	    }
    }
    if (result.humidity != "") {
        logging("Current humidity is ${result.humidity}")
        events << createEvent(name:'humidity', value:result.humidity)
    }
    if (result.volume != "") {
        logging("Current volume is ${result.volume}")
        events << createEvent(name:'level', value:result.volume)
    }
    if (result.isEnable != "") {
        logging("Indicator light is ${(result.isEnable == 1 ? "on" : "off")}")
        events << createEvent(name:'indicatorStatus', value: (result.isEnable == 1 ? "when on" : "never"))
    }

    if (events) return events
}

private toggleTiles(value) {
   def tiles = ["playOne", "playTwo", "playThree", "playFour", "playFive", "playRandom"]
   tiles.each {tile ->
      if (tile != value) sendEvent(name: tile, value: "off", displayed:false)
   }
}

private getSongName(song) {
	switch (song.toInteger()) {
		case 1: return "playOne"; break
		case 2: return "playTwo"; break
		case 3: return "playThree"; break
		case 4: return "playFour"; break
        case 5: return "playFive"; break
        case 6: return "playEnd"; break
	}
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def on() {
	play()
}

def off() {
	stop()
}

def play() {
    logging("play()")
    playTrack(1)
}

def stop() {
    logging("stop()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayStop")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return cmds
}

def pause() {
    logging("pause()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayStop")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return cmds
}

def nextTrack() {
    logging("nextTrack()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayNext")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return cmds
}

def previousTrack() {
    logging("previousTrack()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayPre")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return cmds
}

def setLevel(number) {
    logging("setLevel(${number})")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setAudioVolume&volume=${number}")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getAudioVolume")
    return cmds
}

def refresh() {
	logging("refresh()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getTemperatureState")
    return delayBetween(cmds, 1000)
}

def ping() {
	logging("ping()")
    return postAction("/cgi-bin/CGIProxy.fcgi?cmd=getTemperatureState")
}

def indicatorWhenOn() {
	logging("indicatorWhenOn()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setLedEnableState&isEnable=1")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getLedEnableState")
    return delayBetween(cmds, 1000)
}

def indicatorNever() {
	logging("indicatorNever()")
    def cmds = []
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setLedEnableState&isEnable=0")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getLedEnableState")
    return delayBetween(cmds, 1000)
}

def poll() {
    logging("poll()")
    return refresh()
}

private postAction(uri){ 
  logging("uri ${uri}")
  if (userName && password) uri = uri + "&usr=$userName&pwd=$password"
  updateDNI()
  def headers = getHeader()
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private setDeviceNetworkId(ip, port = null){
    def myDNI
    if (port == null) {
        myDNI = ip
    } else {
  	    def iphex = convertIPtoHex(ip)
  	    def porthex = convertPortToHex(port)
        myDNI = "$iphex:$porthex"
    }
    log.debug "Device Network Id set to ${myDNI}"
    return myDNI
}

private updateDNI() { 
    if (device.deviceNetworkId != state.dni) {
        device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private getHeader(){
    def headers = [:]
    headers.put("Host", getHostAddress())
    return headers
}

private def logging(message) {
    if (state.enableDebugging == "true") log.debug message
}

def playTrack(number) {
    def cmds = []
    cmds << stop()
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayStart&mode=1&index=${number - 1}&name=default")
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return delayBetween(cmds, 1000)
}

def playOne() {
	logging("playOne()")
	playTrack(1)
}
def playTwo() {
	logging("playTwo()")
	playTrack(2)
}
def playThree() {
	logging("playThree()")
	playTrack(3)
}
def playFour() {
	logging("playFour()")
	playTrack(4)
}
def playFive() {
	logging("playFive()")
	playTrack(5)
}
def playRandom() {
	logging("playRandom()")
    Random rand = new Random()
	int max = 4
	playTrack(rand.nextInt(max+1) + 1)
}

def mode() {
    logging("mode()")
    def cmds = []
    switch (device.currentValue('mode')) {
    case "order":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayMode&mode=2")
    break
    case "loopsong":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayMode&mode=3")
    break
    case "looplist":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayMode&mode=1")
    break
    default:
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicPlayMode&mode=1")
    break
    }
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicPlayState")
    return cmds
}

def timer() {
    logging("timer()")
    def cmds = []
    switch (device.currentValue('timer')) {
    case "off":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicDormantTime&minutes=10")
    break
    case "10":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicDormantTime&minutes=20")
    break
    case "20":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicDormantTime&minutes=30")
    break
    case "30":
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicDormantTime&minutes=-1")
    break
    default:
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=setMusicDormantTime&minutes=-1")
    break
    }
    cmds << postAction("/cgi-bin/CGIProxy.fcgi?cmd=getMusicDormantTime")
    return cmds
}
