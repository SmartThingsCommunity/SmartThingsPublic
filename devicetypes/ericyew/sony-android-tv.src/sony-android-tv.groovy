/**
 *  Sony Android TV Smartthings Integration, Currently testing on: KD-75X8500C
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
 *  Based on Ed Anuff, Jamie Yates and Steve Abratt's Code
 *
 *  Based on Jamie Yates's example:
 *  	https://gist.github.com/jamieyates79/fd49d23c1dac1add951ec8ba5f0ff8ae
 *
 *  Note: Within the Device on the SmartThings IDE the Device Network ID for Device instance must be hex of IP address and port
 *  in the form of 00000000:0000 (i.e. 10.0.1.220:80 is 0A0001DC:0050) - if you check the logs the smart device will log the 
 *  correct address that you can copy paste, once the IP address has been set. If this is not set you wont get updated on/off status
 *
 *  Please make sure the TV is on when you set up the device. 
 *
 *  Wake on Lan button works when the TV is in ECO mode and goes to sleep even on wifi. however it takes a slight bit longer to boot
 *  wake on lan wont yet update the status to on very quickly and status polls happen about every 5 mins so it maybe 5 mins before
 *  the TV shows as on.
 *
 */

def getDefaultTheme(){
    def userDefaultThemeMap = [:]
    //v1.2 START
    //ICONS
    userDefaultThemeMap.themeName = "Default"
    userDefaultThemeMap.iconStop = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/stop-icon.png"
    userDefaultThemeMap.iconShutdown = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/power-icon.png"
    userDefaultThemeMap.iconUp = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/up-icon.png"
    userDefaultThemeMap.iconDown = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/down-icon.png"
    userDefaultThemeMap.iconLeft = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/left-icon.png"
    userDefaultThemeMap.iconRight = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/right-icon.png"
    userDefaultThemeMap.iconBack = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/back-icon.png"
    userDefaultThemeMap.iconInfo = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/info-icon.png"
    userDefaultThemeMap.iconSkipFwd = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/small-fwd-icon.png"
    userDefaultThemeMap.iconSkipRwd = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/small-rwd-icon.png"
    userDefaultThemeMap.iconNext = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/next-icon.png"
    userDefaultThemeMap.iconPrevious = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/prev-icon.png"
    userDefaultThemeMap.iconMenu = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/menu-icon.png"
    userDefaultThemeMap.iconHome = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/home-icon.png"
    userDefaultThemeMap.iconPgUp = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/pg-up-icon.png"
    userDefaultThemeMap.iconPgDown = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/themes/default/pg-down-icon.png"
    //COLOURS
    userDefaultThemeMap.colMainWaiting = "#ffffff"     //White
    userDefaultThemeMap.colMainStartup = "#90d2a7"     //Light Green
    userDefaultThemeMap.colMainPlaying = "#79b821"     //Green
    userDefaultThemeMap.colMainStopped = "#153591"     //Blue
    userDefaultThemeMap.colMainPaused = "#e86d13"      //Orange
    userDefaultThemeMap.colMainShutdown = "#e84e4e"    //Red
    //v1.2 END
    //v1.3 START
    //v1.3 END
    //Return
    return userDefaultThemeMap
}

def getUserPref(pref){
    def prefsMap = [:]
    //Main Icon
    prefsMap.iconMain = "https://raw.githubusercontent.com/north3221/north3221SmartThings/master/resources/main-icon.png"
    //Select Colour
    prefsMap.colSelectActive = "#22a3ec"    //Blue
    prefsMap.colSelectInactive = "#ffffff"  //White
    //DECORATION
    prefsMap.decPush = "ring"
    prefsMap.decStop = "ring"
    prefsMap.decShutdown = "flat"
    prefsMap.decUp = "flat"
    prefsMap.decDown = "flat"
    prefsMap.decLeft = "flat"
    prefsMap.decRight = "flat"
    prefsMap.decBack = "flat"
    prefsMap.decInfo = "ring"
    prefsMap.decSkipF = "flat"
    prefsMap.decSkipB = "flat"
    prefsMap.decNext = "flat"
    prefsMap.decPrev = "flat"
    prefsMap.decMenu = "flat"
    prefsMap.decHome = "flat"
    prefsMap.decPup = "flat"
    prefsMap.decPdown = "flat"
    //CATEGORY SETTINGS
    prefsMap.movieLabels = "cinema, movie, film"
    prefsMap.sportLabels = "sport"
    prefsMap.tvLabels = "bbc, itv, channel, sky, amc, fox"
    prefsMap.minMovieRuntime = 4200
    return prefsMap[pref]
}


metadata {
  definition (name: "Sony Android TV", namespace: "ericyew", author: "Eric Yew") {
    capability "Switch"
    capability "Polling"
    capability "Refresh"
    capability "Media Controller"
	capability "Music Player"
    capability "Switch Level"
	capability "TV"
    
    command "sendremotecommand"
    command "digital"
    command "picoff"    
    command "tv_source"
    command "hdmi1"
    command "hdmi2"
    command "hdmi3"
    command "hdmi4"
    command "mute"
    command "netflix"
    command "WOLC"
    command "ipaddress"
    command "iphex"
    command "macaddress"
    command "home"
    command "gguide"
    command "epg"
    command "favorites"
    command "display"
    command "actionMenu"
    command "retu"
    command "up"
    command "down"
    command "right"
    command "left"
    command "confirm"
	command "red"
    command "green"
    command "yellow"
    command "blue"
    command "num1"
    command "num2"
    command "num3"
    command "num4"
    command "num5"
    command "num6"
    command "num7"
    command "num8"
    command "num9"
    command "num0"
    command "num11"
    command "num12"
    command "volumeup"
    command "volumedown"
    command "ChannelUp"
    command "ChannelDown"
    command "SubTitle"
    command "ClosedCaption"
    command "Enter"
    command "DOT"
    command "Analog"
    command "Teletext"
    command "Exit"
    command "Analog2"
    command "AD"
    command "Analogg"
    command "BS"
    command "CS"
    command "BSCS"
    command "Ddata"
    command "PicOff"
    command "Tv_Radio"
    command "Theater"
    command "SEN"
    command "InternetWidgets"
    command "InternetVideo"
    command "SceneSelect"
    command "Mode3D"
    command "iManual"
    command "Audio"
    command "Wide"
    command "Jump"
    command "PAP"
    command "MyEPG"
    command "ProgramDescription"
    command "WriteChapter"
    command "TrackID"
    command "TenKey"
    command "AppliCast"
    command "acTVila"
    command "DeleteVideo"
    command "PhotoFrame"
    command "TvPause"
    command "KeyPad"
    command "Media"
    command "Forward"
    command "Play"
    command "Rewind"
    command "Prev"
    command "Stop"
    command "Next"
    command "Rec"
    command "Pause"
    command "Eject"
    command "FlashPlus"
    command "FlashMinus"
    command "TopMenu"
    command "RakurakuStart"
    command "OneTouchTimeRec"
    command "OneTouchView"
    command "OneTouchRec"
    command "OneTouchStop"
    command "DUX"
    command "FootballMode"
    command "SyncMenu"
  }

  simulator {
    status "on": "on/off: 1"
    status "off": "on/off: 0"
  }
  
tiles(scale: 2) {
    multiAttributeTile(name: "TVMulti", type:"generic", width:6, height:2) {
        tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
			attributeState "on", label:'ON', backgroundColor:"#00A0DC", nextState:"turningOff"
        	attributeState "off", label:'${name}', backgroundColor:"#ffffff", nextState:"turningOn"
        	attributeState "turningOn", label:'${name}', backgroundColor:"#79b821", nextState:"turningOff"
        	attributeState "turningOff", label:'${name}', backgroundColor:"#ffffff", nextState:"turningOn"
        }
        tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
            attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
            attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
            attributeState "turningOn", label:'…', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
            attributeState "turningOff", label:'…', action:"switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
    }
        tileAttribute("device.level", key: "VALUE_CONTROL") {
            attributeState "VALUE_UP", action: "volumeup"
        	attributeState "VALUE_DOWN", action: "volumedown"
        }
	}
	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
      state "off", label: '${name}', action: "switch.on", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#ffffff"
      state "on", label: 'ON', action: "switch.off", icon: "st.thermostat.heating-cooling-on", backgroundColor: "#79b821"
    }
    
    standardTile("power", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "off", label: '', action: "on", icon:"st.thermostat.heating-cooling-off", backgroundColor: "#ffffff"//, nextState: "on"
		state "on", label: '', action: "off", icon:"st.thermostat.heating-cooling-off", backgroundColor: "#79b821"//, nextState: "off"
    }  
    standardTile("on", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"On", action:"on", icon:""
    }  
    standardTile("off", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"off", action:"off", icon:""
    }
    standardTile("digital", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Digital", action:"digital", icon:""
    }
    
    standardTile("picoff", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Pic Off", action:"picoff", icon:""
    }
    
    standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
   
   standardTile("tv_source", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Source", action:"tv_source", icon:""
    }

   standardTile("WOLC", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Wake on Lan", action:"WOLC", icon:""
    }

   standardTile("hdmi1", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 1", action:"hdmi1", icon:""
    }

   standardTile("hdmi2", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 2", action:"hdmi2", icon:""
    }
    
   standardTile("hdmi3", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 3", action:"hdmi3", icon:""
    }
    
   standardTile("hdmi4", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Yamaha", action:"hdmi4", icon:""
    }    

   standardTile("netflix", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Apps", action:"netflix", icon:""
    }
    
   standardTile("home", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"", action:"home", icon:"${getUserTheme('default','iconHome')}"
    }

   standardTile("mute", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Mute", action:"mute", icon:""
    }
    
    standardTile("gguide", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
   state "default", label:"gguide", action:"gguide", icon:""
	} 
    
    standardTile("epg", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Guide", action:"epg", icon:""
    } 

    standardTile("favorites", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"favorites", action:"favorites", icon:""
    } 

    standardTile("display", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"", action:"display", icon:"${getUserTheme('default', 'iconInfo')}"
    } 

    standardTile("actionMenu", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"", action:"actionMenu", icon:"${getUserTheme('default', 'iconMenu')}"
    } 

    standardTile("confirm", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "${getUserPref('decPush')}") {
       state "default", label:"Select", action:"confirm", backgroundColor:"${getUserPref('colSelectActive')}"
    } 

    standardTile("up", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
       state "default", label:"", action:"up", icon:"${getUserTheme('default','iconUp')}"
    } 

    standardTile("retu", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"return", action:"retu", icon:""
    } 
	
    standardTile("red", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"red", action:"red", icon:"", backgroundColor: "#ff0000"
    }
	
    standardTile("green", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Freeview", action:"green", icon:"", backgroundColor: "#008000"
    } 

    standardTile("yellow", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"yellow", action:"yellow", icon:"", backgroundColor: "#ffff00"
    } 

    standardTile("down", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
       state "default", label:"", action:"down", icon:"${getUserTheme('default','iconDown')}"
    } 

    standardTile("right", "device.switch", inactiveLabel: false, height: 2, width: 1, decoration: "flat") {
       state "default", label:"", action:"right", icon:"${getUserTheme('default','iconRight')}"
    } 

    standardTile("left", "device.switch", inactiveLabel: false, height: 2, width: 1, decoration: "flat") {
       state "default", label:"", action:"left", icon:"${getUserTheme('default','iconLeft')}"
    } 

    standardTile("blue", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"blue", action:"blue", icon:"", backgroundColor: "#0000ff"
    } 

    standardTile("num1", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num1", action:"num1", icon:""
    } 

    standardTile("num2", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num2", action:"num2", icon:""
    } 

    standardTile("num3", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num3", action:"num3", icon:""
    } 

    standardTile("num4", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num4", action:"num4", icon:""
    } 

    standardTile("num5", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num5", action:"num5", icon:""
    } 

    standardTile("num6", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num6", action:"num6", icon:""
    } 

    standardTile("num7", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num7", action:"num7", icon:""
    } 

    standardTile("num8", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num8", action:"num8", icon:""
    } 

    standardTile("num9", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num9", action:"num9", icon:""
    } 

    standardTile("num0", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num0", action:"num0", icon:""
    } 

    standardTile("num11", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num11", action:"num11", icon:""
    } 

    standardTile("num12", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"num12", action:"num12", icon:""
    } 

    standardTile("volumeup", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Vol", action:"volumeup", icon:"${getUserTheme('default','iconUp')}"
    } 

    standardTile("volumedown", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Vol", action:"volumedown", icon:"${getUserTheme('default','iconDown')}"
    } 

    standardTile("ChannelUp", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Channel", action:"ChannelUp", icon:"${getUserTheme('default','iconUp')}"
    }

    standardTile("ChannelDown", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Channel", action:"ChannelDown", icon:"${getUserTheme('default','iconDown')}"
    }

    standardTile("SubTitle", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"SubTitle", action:"SubTitle", icon:""
    } 

    standardTile("ClosedCaption", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"ClosedCaption", action:"ClosedCaption", icon:""
    } 

    standardTile("Enter", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Enter", action:"Enter", icon:""
    } 

    standardTile("DOT", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"DOT", action:"DOT", icon:""
    } 

    standardTile("Analog", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Analog", action:"Analog", icon:""
    } 

    standardTile("Teletext", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Teletext", action:"Teletext", icon:""
    } 

    standardTile("Exit", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Exit", action:"Exit", icon:""
    } 

    standardTile("Mode3D", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"3D", action:"Mode3D", icon:""
    } 

    standardTile("iManual", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Help", action:"iManual", icon:""
    } 

    standardTile("Audio", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Audio", action:"Audio", icon:""
    } 

    standardTile("Wide", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Aspect Ratio", action:"Wide", icon:""
    } 

    standardTile("Jump", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Jump", action:"Jump", icon:""
    } 

    standardTile("PAP", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"PIP", action:"PAP", icon:""
    } 

    standardTile("PhotoFrame", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Recorded", action:"PhotoFrame", icon:""
    } 

    standardTile("Media", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Streaming", action:"Media", icon:""
    } 

    standardTile("SyncMenu", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Sync Menu", action:"SyncMenu", icon:""
    } 

    standardTile("Forward", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Forward", action:"Forward", icon:""
    } 

    standardTile("Play", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Play", action:"Play", icon:""
    } 

    standardTile("Rewind", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Rewind", action:"Rewind", icon:""
    } 

    standardTile("Prev", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Prev", action:"Prev", icon:""
    } 

    standardTile("Stop", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Stop", action:"Stop", icon:""
    } 

    standardTile("Next", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Next", action:"Next", icon:""
    } 

    standardTile("Rec", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Rec", action:"Rec", icon:""
    } 

    standardTile("Pause", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Pause", action:"Pause", icon:""
    }

    standardTile("Eject", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Eject", action:"Eject", icon:""
    } 

    standardTile("OneTouchView", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"OneTouchView", action:"OneTouchView", icon:""
    } 

    standardTile("OneTouchTimeRec", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"OneTouchTimeRec", action:"OneTouchTimeRec", icon:""
    } 

    standardTile("OneTouchRec", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"OneTouchRec", action:"OneTouchRec", icon:""
    } 

    standardTile("OneTouchStop", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"OneTouchStop", action:"OneTouchStop", icon:""
    } 

    standardTile("DUX", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Discovery", action:"DUX", icon:""
    } 

    standardTile("FootballMode", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
       state "default", label:"Football Mode", action:"FootballMode", icon:""
    } 
    
    controlTile("volume", "device.level", "slider", height: 1,width: 2) {
    	state "level", action:"switch level.setLevel"
	}

    /**uncomment any extra buttons you need from the lines below*/
    main "switch"
    details(["hdmi1", "hdmi2", "hdmi3", "hdmi4", "switch", "tv_source", "netflix", "Media", "digital", "picoff", "display", "up", "epg", "DUX", "ChannelUp", "left", "confirm", "right", "volumeup", "ChannelDown", "volumedown", "refresh", "actionMenu", "down", "home", "mute", "red", "green", "yellow", "blue", "SyncMenu", "Forward", "Play", "Rewind", "Prev", "Stop", "Next", "Rec", "Pause", "Eject", "retu", "power", "on", "off", "num1", "num2", "num3", "num4", "num5", "num6", "num7", "num8", "num9", "num0", "num11", "num12", "SubTitle", "ClosedCaption", "Enter", "DOT", "Analog", "Teletext", "Exit", "Mode3D", "iManual", "Audio", "Wide", "Jump", "PAP", "MyEPG", "ProgramDescription", "WriteChapter", "TrackID", "TenKey", "AppliCast", "acTVila", "DeleteVideo", "PhotoFrame", "TvPause", "KeyPad", "Media", "FlashPlus", "FlashMinus", "TopMenu", "RakurakuStart", "OneTouchTimeRec", "OneTouchView", "OneTouchRec", "OneTouchStop", "DUX", "gguide", "FootballMode", "Social", "WOLC"])
  }


preferences {
		input name: "ipadd1", type: "number", range: "0..254", required: true, title: "Ip address part 1", displayDuringSetup: true
		input name: "ipadd2", type: "number", range: "0..254", required: true, title: "Ip address part 2", displayDuringSetup: true
		input name: "ipadd3", type: "number", range: "0..254", required: true, title: "Ip address part 3", displayDuringSetup: true
		input name: "ipadd4", type: "number", range: "0..254", required: true, title: "Ip address part 4", displayDuringSetup: true
		input name: "tv_port", type: "number", range: "0..9999", required: true, title: "Port Usually: 80", displayDuringSetup: true
		input name: "tv_psk", type: "text", title: "PSK Passphrase Set on your TV", description: "Enter passphrase", required: true, displayDuringSetup: true	
	}
}


def updated(){
	log.debug( "Preferences Updated rebuilding IP Address, MAC address and Hex Network ID")
	state.tv_poll_count = 0
	ipaddress()
	iphex()
	refresh()
}

def ipaddress(){
	//Build an IP Address from the 4 input preferences
	log.debug( "building IP address from Preferences")
	state.tv_ip = "${ipadd1}" + "." + "${ipadd2}" + "." + "${ipadd3}" + "." + "${ipadd4}"
	log.debug( "IP Address State Value Set to = ${state.tv_ip}:${tv_port}" )
}
	
def iphex(){
	//create a Hex of the IP this will need to be set as the Network ID
	//TO DO Set the Network IP automatically or Show the user the Value to set manually
	log.debug( "Creating Hex of IP: ${state.tv_ip}")
	
	String tvipstring = state.tv_ip
	String tv_ip_hex = tvipstring.tokenize( '.' ).collect {
		String.format( '%02x', it.toInteger() )
	}.join()

	//set the global value of state.ip_hex
	state.ip_hex = tv_ip_hex
	log.debug ("IP Hex stored Globaly as '${state.ip_hex}'")

	log.debug( "Creating Hex of Port: ${tv_port}")

    String tvportstring = tv_port
    String tv_port_hex = tvportstring.tokenize( '.' ).collect {
    	String.format( '%04x', it.toInteger() )
    }.join()

    //Set the Global Value of state.port_hex
    state.port_hex = tv_port_hex
    log.debug ("Port Hex stored Globaly as '${state.port_hex}'")

    log.debug( "Please set your Device Network ID to the following to allow the TV state to be captured: ${state.ip_hex}:${state.port_hex}" )
    String netid = ("${state.ip_hex}:${state.port_hex}")
    log.debug( "Netid ${netid}" )
    //device.deviceNetworkId = ("${netid}")
}

def parse(description) {
  //log.debug ("Parsing '${description}'")
  def msg = parseLanMessage(description)
	//Set the Global Value of state.tv_mac
	//log.debug "${msg}"
    state.tv_mac = msg.mac
    log.debug ("MAC Address stored Globally as '${state.tv_mac}'")
    //log.debug "msg '${msg}'"
    //log.debug "msg.json '${msg.json?.id}'"
    
  
  if (msg.json?.id == 2) {
  	//Set the Global value of state.tv on or off
    state.tv = (msg.json.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: state.tv)
    log.debug "TV is '${state.tv}'"
    state.tv_poll_count = 0
  }
}

private sendJsonRpcCommand(json) {

  def headers = [:]
  headers.put("HOST", "${state.tv_ip}:${tv_port}")
  headers.put("Content-Type", "application/json")
  headers.put("X-Auth-PSK", "${tv_psk}")

  def result = new physicalgraph.device.HubAction(
    method: 'POST',
    path: '/sony/system',
    body: json,
    headers: headers
  )

  result

}

def installed() {
  log.debug "Executing 'installed'"
  poll()
}

def refresh() {
  log.debug "Executing 'refresh'"
  poll()
}

def poll() {
  //set state.tv to 0ff
  log.debug "poll count ${state.tv_poll_count}"
  state.tv = "polling"
  state.tv_poll_count = (state.tv_poll_count + 1)
  if (state.tv_poll_count > 1 ) {
  	  sendEvent(name: "switch", value: "off")
  }
  log.debug "Executing 'poll'"
  def json = "{\"id\":2,\"method\":\"getPowerStatus\",\"version\":\"1.0\",\"params\":[]}"
  def result = sendJsonRpcCommand(json)
}


/**-------------------------------------------------------
All remote Functions Assigned below
--------------------------------------------------------*/
def setLevel(vol) { state.remotecommand = "*SCVOLU0000000000000" + String.format('%03d',vol) }

def on() {
  log.debug "Executing 'on'"
  if (state.tv == "polling"){
  	  WOLC()
  } else {
  	  def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.0\",\"params\":[{\"status\":true}],\"id\":102}"
  	  def result = sendJsonRpcCommand(json)
  }
}

def off() {
  log.debug "Executing 'off'"
  def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.0\",\"params\":[{\"status\":false}],\"id\":102}"
  def result = sendJsonRpcCommand(json)
  
}

def digital(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAyAw=="
	state.button = "digital"
	sendremotecommand()
}

def picoff(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA+Aw=="
	state.button = "Pic Off"
	sendremotecommand()
}

def gguide(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAOAw=="
	state.button = "gguide"
	sendremotecommand()
}

def epg(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAKQAAABbAw=="
	state.button = "epg"
	sendremotecommand()
}

def favorites(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAHcAAAB2Aw=="
	state.button = "favorites"
	sendremotecommand()
}

def display(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA6Aw=="
	state.button = "display"
	sendremotecommand()
}

def home(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAABgAw=="
	state.button = "home"
	sendremotecommand()
}

def actionMenu(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAA2Aw=="
	state.button = "actionMenu"
	sendremotecommand()
}

def retu(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAjAw="
	state.button = "retu"
	sendremotecommand()
}

def up(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAB0Aw=="
	state.button = "up"
	sendremotecommand()
}

def down(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAB1Aw=="
	state.button = "down"
	sendremotecommand()
}

def right(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAzAw=="
	state.button = "right"
	sendremotecommand()
}

def left(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA0Aw=="
	state.button = "left"
	sendremotecommand()
}

def confirm(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAABlAw=="
	state.button = "confirm"
	sendremotecommand()
}

def red(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAlAw=="
	state.button = "red"
	sendremotecommand()
}

def green(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAmAw=="
	state.button = "green"
	sendremotecommand()
}

def yellow(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAnAw=="
	state.button = "yellow"
	sendremotecommand()
}

def blue(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAkAw=="
	state.button = "blue"
	sendremotecommand()
}

def num1(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAAAw=="
	state.button = "num1"
	sendremotecommand()
}

def num2(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAABAw=="
	state.button = "num2"
	sendremotecommand()
}

def num3(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAACAw=="
	state.button = "num3"
	sendremotecommand()
}

def num4(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAADAw=="
	state.button = "num4"
	sendremotecommand()
}

def num5(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAEAw=="
	state.button = "num5"
	sendremotecommand()
}

def num6(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAFAw=="
	state.button = "num6"
	sendremotecommand()
}

def num7(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAGAw=="
	state.button = "num7"
	sendremotecommand()
}

def num8(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAHAw=="
	state.button = "num8"
	sendremotecommand()
}

def num9(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAIAw=="
	state.button = "num9"
	sendremotecommand()
}

def num0(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAJAw=="
	state.button = "num0"
	sendremotecommand()
}

def num11(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAKAw=="
	state.button = "num11"
	sendremotecommand()
}

def num12(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAALAw=="
	state.button = "num12"
	sendremotecommand()
}

def volumeup(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAASAw=="
	state.button = "volumeup"
	sendremotecommand()
}

def volumedown(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAATAw=="
	state.button = "volumedown"
	sendremotecommand()
}

def ChannelUp(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAAQAw=="
	state.button = "ChannelUp"
	sendremotecommand()
}

def ChannelDown(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAARAw=="
	state.button = "ChannelDown"
	sendremotecommand()
}

def SubTitle(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAoAw=="
	state.button = "SubTitle"
	sendremotecommand()
}

def ClosedCaption(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAKQAAAAQAw=="
	state.button = "ClosedCaption"
	sendremotecommand()
}

def Enter(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAALAw=="
	state.button = "Enter"
	sendremotecommand()
}

def DOT(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAdAw=="
	state.button = "DOT"
	sendremotecommand()
}

def Analog(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAHcAAAANAw=="
	state.button = "Analog"
	sendremotecommand()
}

def Teletext(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA/Aw=="
	state.button = "Teletext"
	sendremotecommand()
}

def Exit(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAABjAw=="
	state.button = "Exit"
	sendremotecommand()
}

def Analog2(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA4Aw=="
	state.button = "Analog2"
	sendremotecommand()
}

def AD(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAABoAAAA7Aw=="
	state.button = "*AD"
	sendremotecommand()
}

def Analogg(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAuAw=="
	state.button = "Analog?"
	sendremotecommand()
}

def BS(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAsAw=="
	state.button = "BS"
	sendremotecommand()
}

def CS(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAArAw=="
	state.button = "CS"
	sendremotecommand()
}

def BSCS(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAQAw=="
	state.button = "BSCS"
	sendremotecommand()
}

def Ddata(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAVAw=="
	state.button = "Ddata"
	sendremotecommand()
}

def Tv_Radio(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAABoAAABXAw=="
	state.button = "Tv_Radio"
	sendremotecommand()
}

def Theater(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAHcAAABgAw=="
	state.button = "Theater"
	sendremotecommand()
}

def SEN(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAABoAAAB9Aw=="
	state.button = "SEN"
	sendremotecommand()
}

def InternetWidgets(){
	//Set Remote command to InternetWidgetsd
	state.remotecommand = "AAAAAgAAABoAAAB6Aw=="
	state.button = "InternetWidgets"
	sendremotecommand()
}

def InternetVideo(){
	//Set Remote command to InternetVideod
	state.remotecommand = "AAAAAgAAABoAAAB5Aw=="
	state.button = "InternetVideo"
	sendremotecommand()
}

def netflix(){
	//Set Remote command to Netflix
	state.remotecommand = "AAAAAgAAABoAAAB8Aw=="
	state.button = "Netflix"
	sendremotecommand()
}

def SceneSelect(){
	//Set Remote command to SceneSelectd
	state.remotecommand = "AAAAAgAAABoAAAB4Aw=="
	state.button = "SceneSelect"
	sendremotecommand()
}

def Mode3D(){
	//Set Remote command to Mode3Dd
	state.remotecommand = "AAAAAgAAAHcAAABNAw=="
	state.button = "Mode3D"
	sendremotecommand()
}

def iManual(){
	//Set Remote command to iManuald
	state.remotecommand = "AAAAAgAAABoAAAB7Aw=="
	state.button = "iManual"
	sendremotecommand()
}

def Audio(){
	//Set Remote command to Audiod
	state.remotecommand = "AAAAAQAAAAEAAAAXAw=="
	state.button = "Audio"
	sendremotecommand()
}

def Wide(){
	//Set Remote command to Wided
	state.remotecommand = "AAAAAgAAAKQAAAA9Aw=="
	state.button = "Wide"
	sendremotecommand()
}

def Jump(){
	//Set Remote command to Jumpd
	state.remotecommand = "AAAAAQAAAAEAAAA7Aw=="
	state.button = "Jump"
	sendremotecommand()
}

def PAP(){
	//Set Remote command to PAPd
	state.remotecommand = "AAAAAgAAAKQAAAB3Aw=="
	state.button = "PAP"
	sendremotecommand()
}

def MyEPG(){
	//Set Remote command to MyEPGd
	state.remotecommand = "AAAAAgAAAHcAAABrAw=="
	state.button = "MyEPG"
	sendremotecommand()
}

def ProgramDescription(){
	//Set Remote command to ProgramDescriptiond
	state.remotecommand = "AAAAAgAAAJcAAAAWAw=="
	state.button = "ProgramDescription"
	sendremotecommand()
}

def WriteChapter(){
	//Set Remote command to WriteChapterd
	state.remotecommand = "AAAAAgAAAHcAAABsAw=="
	state.button = "WriteChapter"
	sendremotecommand()
}

def TrackID(){
	//Set Remote command to TrackIDd
	state.remotecommand = "AAAAAgAAABoAAAB+Aw=="
	state.button = "TrackID"
	sendremotecommand()
}

def TenKey(){
	//Set Remote command to TenKeyd
	state.remotecommand = "AAAAAgAAAJcAAAAMAw=="
	state.button = "TenKey"
	sendremotecommand()
}

def AppliCast(){
	//Set Remote command to AppliCastd
	state.remotecommand = "AAAAAgAAABoAAAByAw=="
	state.button = "AppliCast"
	sendremotecommand()
}

def DeleteVideo(){
	//Set Remote command to DeleteVideod
	state.remotecommand = "AAAAAgAAAHcAAAAfAw=="
	state.button = "DeleteVideo"
	sendremotecommand()
}

def PhotoFrame(){
	//Set Remote command to PhotoFramed
	state.remotecommand = "AAAAAgAAABoAAABVAw=="
	state.button = "PhotoFrame"
	sendremotecommand()
}

def TvPause(){
	//Set Remote command to TvPaused
	state.remotecommand = "AAAAAgAAABoAAABnAw=="
	state.button = "TvPause"
	sendremotecommand()
}

def KeyPad(){
	//Set Remote command to KeyPadd
	state.remotecommand = "AAAAAgAAABoAAAB1Aw=="
	state.button = "KeyPad"
	sendremotecommand()
}

def Media(){
	//Set Remote command to Mediad
	state.remotecommand = "AAAAAgAAAJcAAAA4Aw=="
	state.button = "Media"
	sendremotecommand()
}

def SyncMenu(){
	//Set Remote command to SyncMenud
	state.remotecommand = "AAAAAgAAABoAAABYAw=="
	state.button = "SyncMenu"
	sendremotecommand()
}

def Forward(){
	//Set Remote command to Forwardd
	state.remotecommand = "AAAAAgAAAJcAAAAcAw=="
	state.button = "Forward"
	sendremotecommand()
}

def Play(){
	//Set Remote command to Playd
	state.remotecommand = "AAAAAgAAAJcAAAAaAw=="
	state.button = "Play"
	sendremotecommand()
}

def Rewind(){
	//Set Remote command to Rewindd
	state.remotecommand = "AAAAAgAAAJcAAAAbAw=="
	state.button = "Rewind"
	sendremotecommand()
}

def Prev(){
	//Set Remote command to Prevd
	state.remotecommand = "AAAAAgAAAJcAAAA8Aw=="
	state.button = "Prev"
	sendremotecommand()
}

def Stop(){
	//Set Remote command to Stopd
	state.remotecommand = "AAAAAgAAAJcAAAAYAw=="
	state.button = "Stop"
	sendremotecommand()
}

def Next(){
	//Set Remote command Next
	state.remotecommand = "AAAAAgAAAJcAAAA9Aw=="
	state.button = "Next"
	sendremotecommand()
}

def Rec(){
	//Set Remote command Rec
	state.remotecommand = "AAAAAgAAAJcAAAAgAw=="
	state.button = "Rec"
	sendremotecommand()
}

def Pause(){
	//Set Remote command Pause
	state.remotecommand = "AAAAAgAAAJcAAAAZAw=="
	state.button = "Pause"
	sendremotecommand()
}

def Eject(){
	//Set Remote command Eject
	state.remotecommand = "AAAAAgAAAJcAAABIAw=="
	state.button = "Eject"
	sendremotecommand()
}

def FlashPlus(){
	//Set Remote command FlashPlus
	state.remotecommand = "AAAAAgAAAJcAAAB4Aw=="
	state.button = "FlashPlus"
	sendremotecommand()
}

def FlashMinus(){
	//Set Remote command FlashMinus
	state.remotecommand = "AAAAAgAAAJcAAAB5Aw=="
	state.button = "FlashMinus"
	sendremotecommand()
}

def TopMenu(){
	//Set Remote command TopMenu
	state.remotecommand = "AAAAAgAAABoAAABgAw=="
	state.button = "TopMenu"
	sendremotecommand()
}

def PopUpMenu(){
	//Set Remote command PopUpMenu
	state.remotecommand = "AAAAAgAAABoAAABhAw=="
	state.button = "PopUpMenu"
	sendremotecommand()
}

def RakurakuStart(){
	//Set Remote command RakurakuStart
	state.remotecommand = "AAAAAgAAAHcAAABqAw=="
	state.button = "RakurakuStart"
	sendremotecommand()
}

def OneTouchView(){
	//Set Remote command OneTouchView
	state.remotecommand = "AAAAAgAAABoAAABlAw=="
	state.button = "OneTouchView"
	sendremotecommand()
}

def OneTouchTimeRec(){
	//Set Remote command OneTouchTimeRec
	state.remotecommand = "AAAAAgAAABoAAABkAw=="
	state.button = "OneTouchTimeRec"
	sendremotecommand()
}

def OneTouchRec(){
	//Set Remote command OneTouchRec
	state.remotecommand = "AAAAAgAAABoAAABiAw=="
	state.button = "OneTouchRec"
	sendremotecommand()
}

def OneTouchStop(){
	//Set Remote command OneTouchStop
	state.remotecommand = "AAAAAgAAABoAAABjAw=="
	state.button = "OneTouchStop"
	sendremotecommand()
}

def DUX(){
	//Set Remote command DUX
	state.remotecommand = "AAAAAgAAABoAAABzAw=="
	state.button = "DUX"
	sendremotecommand()
}

def FootballMode(){
	//Set Remote command FootballMode
	state.remotecommand = "AAAAAgAAABoAAAB2Aw=="
	state.button = "FootballMode"
	sendremotecommand()
}

def Social(){
	//Set Remote command Social
	state.remotecommand = "AAAAAgAAABoAAAB0Aw=="
	state.button = "Social"
	sendremotecommand()
}

def sendremotecommand(){
	log.debug "Sending Button: ${state.button} ${state.remotecommand}"
    def rawcmd = "${state.remotecommand}"
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     log.debug( "hubAction = ${sonycmd}" )
}

def tv_source() {
	log.debug "Executing Source"
    def rawcmd = "AAAAAQAAAAEAAAAlAw=="  //tv source
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}


def hdmi1() {
	log.debug "Selecting HDMI 1 as input"
    def rawcmd = "AAAAAgAAABoAAABaAw=="  //HDMI 1
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi2() {
	log.debug "Selecting HDMI 2 as input"
    def rawcmd = "AAAAAgAAABoAAABbAw=="  //HDMI 2
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi3() {
	log.debug "Selecting HDMI 3 as input"
    def rawcmd = "AAAAAgAAABoAAABcAw=="  //HDMI 3
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi4() {
	log.debug "Selecting HDMI 4 as input"
    def rawcmd = "AAAAAgAAABoAAABdAw=="  //HDMI 4
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def mute() {
	log.debug "mute"
    def rawcmd = "AAAAAQAAAAEAAAAUAw=="  //mute
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def WOLC() {
    log.debug "Executing Wake on Lan"
	def result = new physicalgraph.device.HubAction (
  	  	"wake on lan ${state.tv_mac}", 
   		physicalgraph.device.Protocol.LAN,
   		null,
    	[secureCode: "111122223333"]
	)
	return result
}

def getUserTheme(index){
    return getUserTheme(inputTheme ?: state?.theme ?: "default", index)
}

//Themes
def getUserTheme(theme, index){
    switch (theme){
        case "glyphs":
            if (!state?.glyphsTheme){
                return glyphsTheme[index]
            }
            return state.glyphsTheme[index]
            break;
        case "mayssam":
            if (!state?.mayssamTheme){
                return mayssamTheme[index]
            }
            return state.mayssamTheme[index]
            break;
        default:
            if (!state?.defaultTheme){
                return defaultTheme[index]
            }
            return state.defaultTheme[index]
    }
}
