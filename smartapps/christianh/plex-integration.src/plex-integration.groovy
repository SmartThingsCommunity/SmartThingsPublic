/**
 *  Plex Integration
 *
 *  Copyright 2015 Christian Hjelseth
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
 *  v1.4.1 - Fixed bug.
 *  -------------------------------------
 *  v1.4 - Added ability to execute routines. Fixed modes bug.
 *  -------------------------------------
 *  v1.3 - Added multiple rooms/players support, and config for when trailers play.  
 *  -------------------------------------
 *  v1.2 - Added ability to view api info with the App.  Also added ability to toggle debug logging on and off (@tonesto7)
 *		   Added ability to set certain modes only to trigger switches
 *  -------------------------------------
 *  v1.1 - Added optional mode changes
 */
 
import groovy.json.JsonBuilder

definition(
    name: "Plex Integration",
    namespace: "ChristianH",
    author: "Christian Hjelseth",
    description: "Allows web requests to dim/turn off/on lights when plex is playing.",
    category: "My Apps",
    iconUrl: "http://1sd3vh2v9afo91q38219tlj1.wpengine.netdna-cdn.com/wp-content/uploads/2015/05/plex-icon-server-big-3b6e2330294017827d0354f0c768a3ab.png",
    iconX2Url: "http://1sd3vh2v9afo91q38219tlj1.wpengine.netdna-cdn.com/wp-content/uploads/2015/05/plex-icon-server-big-3b6e2330294017827d0354f0c768a3ab.png",
    iconX3Url: "http://1sd3vh2v9afo91q38219tlj1.wpengine.netdna-cdn.com/wp-content/uploads/2015/05/plex-icon-server-big-3b6e2330294017827d0354f0c768a3ab.png",
    oauth: [displayName: "PlexServer", displayLink: ""])

preferences {
	page(name: "configPage", nextPage: "roomsPage") 
	page(name: "roomsPage")
}

//had to move to a dynamic page to handle the possible missing token
/*  Main Page  */
def configPage() {
	//Generates an accessToken if one has not been generated
	if (!state.accessToken) {
    	createAccessToken()
   	}
    if (state.debugLogging == null) 	{ state.debugLogging = false }
    
	dynamicPage(name: "configPage", title: "Plex2SmartThings", uninstall: true) {
        // Enables logging debug only when enabled
        section(title: "Debug Logging") {
       		paragraph "If you experiencing issues please enable logging to help troubleshoot"
            input "debugLogging", "bool", title: "Debug Logging...", required: false, defaultValue: false, refreshAfterSelection: true
            	
            if (debugLogging) { 
            	state.debugLogging = true 
                logWriter("Debug Logging has been ${state.debugLogging.toString().toUpperCase()}")
                paragraph "Debug Logging is Enabled: ${state.debugLogging}"
            }
            else { 
            	state.debugLogging = false 
            	logWriter("Debug Logging has been ${state.debugLogging.toString().toUpperCase()}")    
            }
    	}
    	section() { 
        	href url: "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/appinfo?access_token=${state.accessToken}", 
            		style:"embedded", required:false, title:"API Information", description: "Tap to view Info"
    	}
		section("Rooms") {
            paragraph "On the next page you can define up to 5 rooms."
		}
    }
}

/*  Rooms Page  */
def roomsPage() {
	dynamicPage(name: "roomsPage", title: "Rooms", install: true, uninstall: true) {
		section("Info") {
            paragraph "Each room can have up to 2 plex player names that control it. You may set the name to * in order to match any player. Only the first matching room will be used."
		}
		
		//It didn't work to loop through anything but devices. The values didn't save for some reason.. Will be fixed later sometime.
		
		//Get defined routines
		def actions = location.helloHome?.getPhrases()*.label
		if (actions) {
			actions.sort()
		}
		
		//ROOM 1
		section("Rooms #1") {
            input(name: "playerA1", type: "text", title: "Plex player name", required:false)
            input(name: "playerB1", type: "text", title: "Plex player name (alternative)", required:false)
        }
		section("Devices (room #1)") {
			input "hues1", "capability.colorControl", title: "Hue Bulbs", multiple:true, required:false
			input "dimmers1", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switches1", "capability.switch", title:"Switches", multiple: true, required: false
			input "playMode1", "mode", title: "Mode when playing", required:false
			input "pauseMode1", "mode", title: "Mode when paused", required:false
			input "stopMode1", "mode", title: "Mode when stopped", required:false
			if (actions) {
				input "playRoutine1", "enum", title: "Routine when playing", required:false, options: actions
				input "pauseRoutine1", "enum", title: "Routine when paused", required:false, options: actions
				input "stopRoutine1", "enum", title: "Routine when stopped", required:false, options: actions
			}
		}
		section("Config (room #1)") {
			input(name: "bSwitchOffOnPause1", type: "bool", title: "Turn switches off on pause")
			input(name: "bDimOnlyIfOn1", type: "bool", title: "Dim bulbs only if they're already on")
			input(name: "bTreatTrailersAsPause1", type: "bool", title: "Use pause config for movie trailers")
			input(name: "iLevelOnStop1", type: "number", title: "Bulb/dimmer levels on Stop", defaultValue:100)
			input(name: "iLevelOnPause1", type: "number", title: "Bulb/dimmer levels on Pause", defaultValue:30)
			input(name: "iLevelOnPlay1", type: "number", title: "Bulb/dimmer levels on Play", defaultValue:0)
		}
		
		//ROOM 2
		section("Rooms #2") {
            input(name: "playerA2", type: "text", title: "Plex player name", required:false)
            input(name: "playerB2", type: "text", title: "Plex player name (alternative)", required:false)
        }
		section("Devices (room #2)") {
			input "hues2", "capability.colorControl", title: "Hue Bulbs", multiple:true, required:false
			input "dimmers2", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switches2", "capability.switch", title:"Switches", multiple: true, required: false
			input "playMode2", "mode", title: "Mode when playing", required:false
			input "pauseMode2", "mode", title: "Mode when paused", required:false
			input "stopMode2", "mode", title: "Mode when stopped", required:false
			if (actions) {
				input "playRoutine2", "enum", title: "Routine when playing", required:false, options: actions
				input "pauseRoutine2", "enum", title: "Routine when paused", required:false, options: actions
				input "stopRoutine2", "enum", title: "Routine when stopped", required:false, options: actions
			}
		}
		section("Config (room #2)") {
			input(name: "bSwitchOffOnPause2", type: "bool", title: "Turn switches off on pause")
			input(name: "bDimOnlyIfOn2", type: "bool", title: "Dim bulbs only if they're already on")
			input(name: "bTreatTrailersAsPause2", type: "bool", title: "Use pause config for movie trailers")
			input(name: "iLevelOnStop2", type: "number", title: "Bulb/dimmer levels on Stop", defaultValue:100)
			input(name: "iLevelOnPause2", type: "number", title: "Bulb/dimmer levels on Pause", defaultValue:30)
			input(name: "iLevelOnPlay2", type: "number", title: "Bulb/dimmer levels on Play", defaultValue:0)
		}
				
		//ROOM 3
		section("Rooms #3") {
            input(name: "playerA3", type: "text", title: "Plex player name", required:false)
            input(name: "playerB3", type: "text", title: "Plex player name (alternative)", required:false)
        }
		section("Devices (room #3)") {
			input "hues3", "capability.colorControl", title: "Hue Bulbs", multiple:true, required:false
			input "dimmers3", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switches3", "capability.switch", title:"Switches", multiple: true, required: false
			input "playMode3", "mode", title: "Mode when playing", required:false
			input "pauseMode3", "mode", title: "Mode when paused", required:false
			input "stopMode3", "mode", title: "Mode when stopped", required:false
			if (actions) {
				input "playRoutine3", "enum", title: "Routine when playing", required:false, options: actions
				input "pauseRoutine3", "enum", title: "Routine when paused", required:false, options: actions
				input "stopRoutine3", "enum", title: "Routine when stopped", required:false, options: actions
			}
		}
		section("Config (room #3)") {
			input(name: "bSwitchOffOnPause3", type: "bool", title: "Turn switches off on pause")
			input(name: "bDimOnlyIfOn3", type: "bool", title: "Dim bulbs only if they're already on")
			input(name: "bTreatTrailersAsPause3", type: "bool", title: "Use pause config for movie trailers")
			input(name: "iLevelOnStop3", type: "number", title: "Bulb/dimmer levels on Stop", defaultValue:100)
			input(name: "iLevelOnPause3", type: "number", title: "Bulb/dimmer levels on Pause", defaultValue:30)
			input(name: "iLevelOnPlay3", type: "number", title: "Bulb/dimmer levels on Play", defaultValue:0)
		}
				
		//ROOM 4
		section("Rooms #4") {
            input(name: "playerA4", type: "text", title: "Plex player name", required:false)
            input(name: "playerB4", type: "text", title: "Plex player name (alternative)", required:false)
        }
		section("Devices (room #4)") {
			input "hues4", "capability.colorControl", title: "Hue Bulbs", multiple:true, required:false
			input "dimmers4", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switches4", "capability.switch", title:"Switches", multiple: true, required: false
			input "playMode4", "mode", title: "Mode when playing", required:false
			input "pauseMode4", "mode", title: "Mode when paused", required:false
			input "stopMode4", "mode", title: "Mode when stopped", required:false
			if (actions) {
				input "playRoutine4", "enum", title: "Routine when playing", required:false, options: actions
				input "pauseRoutine4", "enum", title: "Routine when paused", required:false, options: actions
				input "stopRoutine4", "enum", title: "Routine when stopped", required:false, options: actions
			}
		}
		section("Config (room #4)") {
			input(name: "bSwitchOffOnPause4", type: "bool", title: "Turn switches off on pause")
			input(name: "bDimOnlyIfOn4", type: "bool", title: "Dim bulbs only if they're already on")
			input(name: "bTreatTrailersAsPause4", type: "bool", title: "Use pause config for movie trailers")
			input(name: "iLevelOnStop4", type: "number", title: "Bulb/dimmer levels on Stop", defaultValue:100)
			input(name: "iLevelOnPause4", type: "number", title: "Bulb/dimmer levels on Pause", defaultValue:30)
			input(name: "iLevelOnPlay4", type: "number", title: "Bulb/dimmer levels on Play", defaultValue:0)
		}
		
		//ROOM 5
		section("Rooms #5") {
            input(name: "playerA5", type: "text", title: "Plex player name", required:false)
            input(name: "playerB5", type: "text", title: "Plex player name (alternative)", required:false)
        }
		section("Devices (room #5)") {
			input "hues5", "capability.colorControl", title: "Hue Bulbs", multiple:true, required:false
			input "dimmers5", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switches5", "capability.switch", title:"Switches", multiple: true, required: false
			input "playMode5", "mode", title: "Mode when playing", required:false
			input "pauseMode5", "mode", title: "Mode when paused", required:false
			input "stopMode5", "mode", title: "Mode when stopped", required:false
			if (actions) {
				input "playRoutine5", "enum", title: "Routine when playing", required:false, options: actions
				input "pauseRoutine5", "enum", title: "Routine when paused", required:false, options: actions
				input "stopRoutine5", "enum", title: "Routine when stopped", required:false, options: actions
			}
		}
		section("Config (room #5)") {
			input(name: "bSwitchOffOnPause5", type: "bool", title: "Turn switches off on pause")
			input(name: "bDimOnlyIfOn5", type: "bool", title: "Dim bulbs only if they're already on")
			input(name: "bTreatTrailersAsPause5", type: "bool", title: "Use pause config for movie trailers")
			input(name: "iLevelOnStop5", type: "number", title: "Bulb/dimmer levels on Stop", defaultValue:100)
			input(name: "iLevelOnPause5", type: "number", title: "Bulb/dimmer levels on Pause", defaultValue:30)
			input(name: "iLevelOnPlay5", type: "number", title: "Bulb/dimmer levels on Play", defaultValue:0)
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	if (!state.accessToken) {
    	createAccessToken()
   	}
}

// These Methods Generate Json for you Info Only
def appInfoJson() {
	def configJson = new groovy.json.JsonOutput().toJson([
    	appId:        app.id,
    	accessToken:  state.accessToken,
    	appUrl: 	"https://graph.api.smartthings.com/api/smartapps/installations/${app.id}",
    	onPlay: 	"https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/statechanged/onplay",
		onPause:	"https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/statechanged/onpause",
		onStop:		"https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/statechanged/onstop"
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

mappings {
  path("/statechanged/:command") 	{ action: [ GET: "OnCommandRecieved" ] }
  path("/appinfo") 					{ action: [ GET: "appInfoJson"]   }
}

def OnCommandRecieved() {
	def command = params.command
	def userName = params.user
	def playerName = params.player
	def mediaType = params.type
	logWriter ("Plex.$command($userName, $playerName, $mediaType)")
	
    //Find the right room from player name
	def roomIndex = getRoomIndexFromPlayerName(playerName);
	if(roomIndex == -1) {
		logWriter ("->Not matching any rooms.")
        return
    } else {
		logWriter ("->Matched room #$roomIndex")
    }
    
    //Translate play to pause if bTreatTrailersAsPause is enabled for this room
    if(TreatTrailersAsPause(roomIndex) && mediaType == "clip" && command == "onplay") {
    	command = "onpause"
		logWriter ("->Is playing trailer, treating it as a pause.")
    }
    
    if (command == "onplay") {
        ChangeMode(PlayMode(roomIndex))
		ExecRoutine(PlayRoutine(roomIndex))
		SetLevels(roomIndex, LevelOnPlay(roomIndex))
        SetSwitchesOff(roomIndex)
    }
    else if (command == "onpause") {
        ChangeMode(PauseMode(roomIndex))
		ExecRoutine(PauseRoutine(roomIndex))
    	SetLevels(roomIndex, LevelOnPause(roomIndex))
        if(SwitchOffOnPause(roomIndex)) {
       		SetSwitchesOff(roomIndex)
        } else {
        	SetSwitchesOn(roomIndex)
        }
    }
    else if (command == "onstop") {
        ChangeMode(StopMode(roomIndex))
		ExecRoutine(StopRoutine(roomIndex))
    	SetLevels(roomIndex, LevelOnStop(roomIndex))
        SetSwitchesOn(roomIndex)
    }
}

def ExecRoutine(routine) {
	if(!routine) return
	location.helloHome?.execute(routine)
}
def ChangeMode(newMode) {
    if (newMode != null && newMode != "" && location.mode != newMode) {
        if (location.modes?.find{it.name == newMode}) {
            setLocationMode(newMode)
        }  else {
            log.warn "Tried to change to undefined mode '${newMode}'"
        }
    }
}
def SetSwitchesOn(roomIndex) {
	logWriter ("SetSwitchesOn($roomIndex)")
	settings."switches${roomIndex}"?.on()
}
def SetSwitchesOff(roomIndex) {
	logWriter ("SetSwitchesOff($roomIndex)")
	settings."switches${roomIndex}"?.off()
}
def SetLevels(roomIndex, level) {
	if (level != null) {
		logWriter ("SetLevels($roomIndex, $level)")
		settings."hues${roomIndex}"?.each { hue -> dimIfOn(roomIndex, hue, level) }
		settings."dimmers${roomIndex}"?.each { dimmer -> dimIfOn(roomIndex, dimmer, level) }
	}
}
private def dimIfOn(roomIndex, bulb, level) {
	if (!DimOnlyIfOn(roomIndex) || "on" == bulb.currentSwitch) {
		bulb.setLevel(level)
	}
}
private def logWriter(value) {
	if (state.debugLogging) {
        log.debug "${value}"
    }	
}

//Couldnt make settings."bDimOnlyIfOn${roomIndex}" work for some reason..
//Using some functions instead until it's fixed.
def PlayMode(room) {
	if(room == 1) return playMode1
	else if(room == 2) return playMode2
	else if(room == 3) return playMode3
	else if(room == 4) return playMode4
	else if(room == 5) return playMode5
    else return null
}
def PauseMode(room) {
	if(room == 1) return pauseMode1
	else if(room == 2) return pauseMode2
	else if(room == 3) return pauseMode3
	else if(room == 4) return pauseMode4
	else if(room == 5) return pauseMode5
    else return null
}
def StopMode(room) {
	if(room == 1) return stopMode1
	else if(room == 2) return stopMode2
	else if(room == 3) return stopMode3
	else if(room == 4) return stopMode4
	else if(room == 5) return stopMode5
    else return null
}
def PlayRoutine(room) {
	if(room == 1) return playRoutine1
	else if(room == 2) return playRoutine2
	else if(room == 3) return playRoutine3
	else if(room == 4) return playRoutine4
	else if(room == 5) return playRoutine5
    else return null
}
def PauseRoutine(room) {
	if(room == 1) return pauseRoutine1
	else if(room == 2) return pauseRoutine2
	else if(room == 3) return pauseRoutine3
	else if(room == 4) return pauseRoutine4
	else if(room == 5) return pauseRoutine5
    else return null
}
def StopRoutine(room) {
	if(room == 1) return stopRoutine1
	else if(room == 2) return stopRoutine2
	else if(room == 3) return stopRoutine3
	else if(room == 4) return stopRoutine4
	else if(room == 5) return stopRoutine5
    else return null
}
def DimOnlyIfOn(room) {
	if(room == 1) return bDimOnlyIfOn1
	else if(room == 2) return bDimOnlyIfOn2
	else if(room == 3) return bDimOnlyIfOn3
	else if(room == 4) return bDimOnlyIfOn4
	else if(room == 5) return bDimOnlyIfOn5
    else return false
}
def TreatTrailersAsPause(room) {
	if(room == 1) return bTreatTrailersAsPause1
	else if(room == 2) return bTreatTrailersAsPause2
	else if(room == 3) return bTreatTrailersAsPause3
	else if(room == 4) return bTreatTrailersAsPause4
	else if(room == 5) return bTreatTrailersAsPause5
    else return false
}
def SwitchOffOnPause(room) {
	if(room == 1) return bSwitchOffOnPause1
	else if(room == 2) return bSwitchOffOnPause2
	else if(room == 3) return bSwitchOffOnPause3
	else if(room == 4) return bSwitchOffOnPause4
	else if(room == 5) return bSwitchOffOnPause5
    else return false
}
def LevelOnStop(room) {
	if(room == 1) return iLevelOnStop1
	else if(room == 2) return iLevelOnStop2
	else if(room == 3) return iLevelOnStop3
	else if(room == 4) return iLevelOnStop4
	else if(room == 5) return iLevelOnStop5
    else return 0
}
def LevelOnPause(room) {
	if(room == 1) return iLevelOnPause1
	else if(room == 2) return iLevelOnPause2
	else if(room == 3) return iLevelOnPause3
	else if(room == 4) return iLevelOnPause4
	else if(room == 5) return iLevelOnPause5
    else return 0
}
def LevelOnPlay(room) {
	if(room == 1) return iLevelOnPlay1
	else if(room == 2) return iLevelOnPlay2
	else if(room == 3) return iLevelOnPlay3
	else if(room == 4) return iLevelOnPlay4
	else if(room == 5) return iLevelOnPlay5
    else return 0
}

//Get the first matching room player name, or the first player with name *.
def getRoomIndexFromPlayerName(player) {
    if(playerA1 == "*" || playerA1 == player || playerB1 == "*" || playerB1 == player) return 1;
    else if(playerA2 == "*" || playerA2 == player || playerB2 == "*" || playerB2 == player) return 1;
    else if(playerA3 == "*" || playerA3 == player || playerB3 == "*" || playerB3 == player) return 1;
    else if(playerA4 == "*" || playerA4 == player || playerB4 == "*" || playerB4 == player) return 1;
    else if(playerA5 == "*" || playerA5 == player || playerB5 == "*" || playerB5 == player) return 1;
    else return -1;
}