/**
 *  	Denon Network Receiver 
 *    	Based on Denon/Marantz receiver by Kristopher Kubicki
 *    	SmartThings driver to connect your Denon Network Receiver to SmartThings
 *		Tested with AVR-S710W (game1 & game2 inputs are not available), AVR 1912

TESED DENON MODELS
    ModelId
	EnModelUnknown,		//(0)
	EnModelAVRX10,		//(1)
	EnModelAVRX20,		//(2)
	EnModelAVRX30,		//(3)
	EnModelAVRX40,		//(4)
	EnModelAVRX50,		//(5)
	EnModelAVRX70,		//(6)
	EnModelNR15,		//(7)
	EnModelNR16,		//(8)
	EnModelSR50,		//(9)
	EnModelSR60,		//(10)
	EnModelSR70,		//(11)
	EnModelAV77,		//(12)
	EnModelAV88,		//(13)
*/

metadata {
    definition (name: "Denon AVR Zone 2", namespace: "DAVRZ2", 
        author: "Bobby") {
        capability "Actuator"
        capability "Switch" 
        capability "Polling"
        capability "Switch Level"
        capability "Music Player" 
        
        attribute "mute", "string"
        attribute "input", "string"     
        attribute "cd", "string"
        attribute "tv", "string"
		attribute "dvd", "string"
		attribute "dock", "string"
		attribute "bt", "string"
		attribute "game", "string"
		attribute "sMovie", "string"        
		attribute "sMusic", "string"          
		attribute "sPure", "string"             

        command "mute"
        command "unmute"
        command "toggleMute"
        command "inputSelect", ["string"]
        command "inputNext"
		command "cd"
		command "tv"
		command "bd"
		command "dvd"
		command "dock"
		command "bt"
		command "game"
		command "sMovie"
		command "sMusic"
		command "sPure"    

        }


preferences {
    input("destIp", "text", title: "IP", description: "The device IP")
    input("destPort", "number", title: "Port", description: "The port you wish to connect", defaultValue: 80)
    input("networkID", "number", title: "Network ID", description: "Unique number of this AVR", defaultValue: 1)
	input(title: "Denon AVR version: ${getVersionTxt()}" ,description: null, type : "paragraph")
}


    simulator {
        // TODO-: define status and reply messages here
    }

    //tiles {
	tiles(scale: 2) {
		multiAttributeTile(name:"multiAVR", type: "mediaPlayer", width: 6, height: 4) {
           tileAttribute("device.status", key: "PRIMARY_CONTROL") { 	            
            	attributeState ("paused", label: 'Paused', backgroundColor: "#53a7c0", defaultState: true)
				attributeState ("playing", label: 'Playing', backgroundColor: "#79b821")
        	}             
            tileAttribute("device.status", key: "MEDIA_STATUS") { 	            
            	attributeState "playing", label: '${name}', action:"switch.off"
                attributeState "paused", label: '${name}', action:"switch.on"
			}  
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
           		attributeState ("level", action:"setLevel")
                }       
            tileAttribute ("device.mute", key: "MEDIA_MUTED") {
            	attributeState("unmuted", action:"mute", nextState: "muted")
            	attributeState("muted", action:"unmute", nextState: "unmuted")
        	}
        }        
        standardTile("input1", "device.dvd", width: 2, height: 2, decoration: "flat"){     
            state "OFF", label: 'Nvidia', action: "dvd", icon:"st.Electronics.electronics3", backgroundColor: "#FFFFFF", nextState:"ON"
            state "ON", label: 'Nvidia', action: "dvd", icon:"st.Electronics.electronics3" , backgroundColor: "#53a7c0", nextState:"OFF"        
            }
        standardTile("input2", "device.tv", width: 2, height: 2, decoration: "flat"){
        	 state "OFF", label: 'TV', action: "tv", icon:"st.Electronics.electronics18", backgroundColor:"#FFFFFF",nextState:"ON" 
             state "ON", label: 'TV', action: "tv", icon:"st.Electronics.electronics18", backgroundColor: "#53a7c0", nextState:"OFF"             
            }
        standardTile("input3", "device.bd", width: 2, height: 2, decoration: "flat"){
        	state "OFF", label: 'Blu-ray', action: "bd", icon:"st.Electronics.electronics8", backgroundColor: "#FFFFFF",nextState:"ON"  
            state "ON", label: 'Blu-ray', action: "bd", icon:"st.Electronics.electronics8", backgroundColor: "#53a7c0", nextState:"OFF"              
        	}
        standardTile("input4", "device.cd", width: 2, height: 2, decoration: "flat"){
        	state "OFF", label: 'Google', action: "cd", icon:"st.Electronics.electronics14", backgroundColor: "#FFFFFF",nextState:"ON"   
            state "ON", label: 'Google', action: "cd", icon:"st.Electronics.electronics14", backgroundColor: "#53a7c0", nextState:"OFF"               
        	}
		standardTile("input5", "device.dock", width: 2, height: 2, decoration: "flat"){
        	state "OFF", label: 'Dock', action: "dock", icon:"st.Electronics.electronics3", backgroundColor: "#FFFFFF",nextState:"ON"   
            state "ON", label: 'Dock', action: "dock", icon:"st.Electronics.electronics3", backgroundColor: "#53a7c0", nextState:"OFF"              
			}
        standardTile("input6", "device.bt", width: 2, height: 2, decoration: "flat"){
        	state "OFF", label: 'Bluetooth', action: "bt", icon:"st.Entertainment.entertainment2", backgroundColor: "#FFFFFF",nextState:"ON"   
            state "ON", label: 'Bluetooth', action: "bt", icon:"st.Entertainment.entertainment2", backgroundColor: "#53a7c0", nextState:"OFF"             
			}
        standardTile("input7", "device.game", width: 2, height: 2, decoration: "flat"){
        	state "OFF", label: 'Game', action: "game", icon:"st.Electronics.electronics5", backgroundColor: "#FFFFFF",nextState:"ON"   
            state "ON", label: 'Game', action: "game", icon:"st.Electronics.electronics5", backgroundColor: "#53a7c0", nextState:"OFF"   
			}               
		standardTile("poll", "device.poll", width: 1, height: 1, decoration: "flat") {
            state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
        }
main "multiAVR"
        details(["multiAVR", "input1", "input2", "input3","input4", "input5", "input6","input7", "poll"])
    }
}
def parse(String description) {
	//log.debug "Parsing '${description}'"
 	def map = stringToMap(description)
    if(!map.body || map.body == "DQo=") { return }
	def body = new String(map.body.decodeBase64())
	def statusrsp = new XmlSlurper().parseText(body)
	//POWER STATUS	
    def power = statusrsp.ZonePower.value.text()
	if(power == "ON") { 
    	sendEvent(name: "status", value: 'playing')
    }
    if(power != "" && power != "ON") {  
    	sendEvent(name: "status", value: 'paused')
	}
	//VOLUME STATUS    
    def muteLevel = statusrsp.Mute.value.text()
    if(muteLevel == "on") { 
    	sendEvent(name: "mute", value: 'muted')
	}
    if(muteLevel != "" && muteLevel != "on") {
	    sendEvent(name: "mute", value: 'unmuted')
    }
    if(statusrsp.MasterVolume.value.text()) { 
    	def int volLevel = (int) statusrsp.MasterVolume.value.toFloat() ?: -40.0
        volLevel = (volLevel + 80)
        	log.debug "Adjusted volume is ${volLevel}"
        def int curLevel = 36
        sendEvent(name: "level", value: volLevel)
        try {
        	// curLevel = device.currentValue("level")
            curLevel = volLevel
        	log.debug "Current volume is ${curLevel}"
        } catch(NumberFormatException nfe) { 
        	curLevel = 36
        }
        if(curLevel != volLevel) {
    		sendEvent(name: "level", value: volLevel)
        }
    } 
	//INPUT STATUS
	def inputCanonical = statusrsp.InputFuncSelect.value.text()
            sendEvent(name: "input", value: inputCanonical)
	        log.debug "Current Input is: ${inputCanonical}"
            
    def inputZone = statusrsp.RenameZone.value.text()
    		//sendEvent(name: "sound", value: inputSurr)
	        log.debug "Current Active Zone is: ${inputZone}"                      
}
    //TILE ACTIONS
    def setLevel(val) {
        sendEvent(name: "mute", value: "unmuted")     
        sendEvent(name: "level", value: val)
        def int scaledVal = val - 80
        request("cmd0=PutMasterVolumeSet%2F$scaledVal&ZoneName=ZONE2")
    }
    def on() {
        sendEvent(name: "status", value: 'playing')
        request('cmd0=PutZone_OnOff%2FON&ZoneName=ZONE2')
    }
    def off() { 
        sendEvent(name: "status", value: 'paused')
        request('cmd0=PutZone_OnOff%2FOFF&ZoneName=ZONE2')
    }
    def mute() { 
        sendEvent(name: "mute", value: "muted")
        request('cmd0=PutVolumeMute%2FON&ZoneName=ZONE2')
    }
    def unmute() { 
        sendEvent(name: "mute", value: "unmuted")
        request('cmd0=PutVolumeMute%2FOFF&ZoneName=ZONE2')
    }
    def toggleMute(){
        if(device.currentValue("mute") == "muted") { unmute() }
        else { mute() }
    }
    def cd() {
        def cmd = "CD"
        log.debug "Setting input to ${cmd}"
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
        }
    def tv() {
        def cmd = "TV"
        log.debug "Setting input to ${cmd}"
        syncTiles(cmd)   
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
        }
    def bd() {
        def cmd = "BD"
        log.debug "Setting input to ${cmd}"
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
        }
    def dvd() {
        def cmd = "DVD"
        log.debug "Setting input to ${cmd}"
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
        }
    def dock() {
        def cmd = "DOCK"
        log.debug "Setting input to '${cmd}'"
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
        }
    def bt() {
        def cmd = "BT"
        log.debug "Setting input to '${cmd}'"
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
    }
    def game() {
        def cmd = "GAME"
        log.debug "Setting input to '${cmd}'" 
        syncTiles(cmd)
        request("cmd0=PutZone_InputFunction%2F"+cmd+"&ZoneName=ZONE2")
    }

 
    def poll() { 
        //log.debug "Polling requested"
        refresh()
    }
    def syncTiles(cmd){
        if (cmd == "CD") sendEvent(name: "cd", value: "ON")	 
            else sendEvent(name: "cd", value: "OFF")						
        if (cmd == "TV") sendEvent(name: "tv", value: "ON")	 
            else sendEvent(name: "tv", value: "OFF")						
        if (cmd == "BD") sendEvent(name: "bd", value: "ON")	 
            else sendEvent(name: "bd", value: "OFF")						
        if (cmd == "DVD") sendEvent(name: "dvd", value: "ON")	 
            else sendEvent(name: "dvd", value: "OFF")						
        if (cmd == "DOCK") sendEvent(name: "dock", value: "ON")	 
            else sendEvent(name: "dock", value: "OFF")						
        if (cmd == "BT") sendEvent(name: "bt", value: "ON")	 
            else sendEvent(name: "bt", value: "OFF")						
        if (cmd == "GAME") sendEvent(name: "game", value: "ON")	 
            else sendEvent(name: "game", value: "OFF")
    }

	def refresh() {
        def hosthex = convertIPtoHex(destIp)
        def porthex = convertPortToHex(destPort)
        //device.deviceNetworkId = "$hosthex:$porthex" 
        device.deviceNetworkId = "DAVR$networkID"

        def hubAction = new physicalgraph.device.HubAction(
                'method': 'GET',
                'path': "/goform/formMainZone_MainZoneXml.xml?_=1500799497753&ZoneName=ZONE2",
                'headers': [ HOST: "$destIp:$destPort" ] 
            )   
        hubAction
    }
    def request(body) { 
        def hosthex = convertIPtoHex(destIp)
        def porthex = convertPortToHex(destPort)
        //device.deviceNetworkId = "$hosthex:$porthex" 
        device.deviceNetworkId = "DAVR$networkID"

        def hubAction = new physicalgraph.device.HubAction(
                'method': 'POST',
                'path': "/MainZone/index.put.asp",
                'body': body,
                'headers': [ HOST: "$destIp:$destPort" ]
            ) 

        hubAction
    }
    def request2(body) { 
        def hosthex = convertIPtoHex(destIp)
        def porthex = convertPortToHex(destPort)
        //device.deviceNetworkId = "$hosthex:$porthex" 
        device.deviceNetworkId = "DAVR$networkID"

        def hubAction = new physicalgraph.device.HubAction(
                'method': 'POST',
                'path': "/Zone2/index.put.asp",
                'body': body,
                'headers': [ HOST: "$destIp:$destPort" ]
            ) 

        hubAction
    }
    
    
    private String convertIPtoHex(ipAddress) { 
        String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        return hex
    }
    private String convertPortToHex(port) {
        String hexport = port.toString().format( '%04X', port.toInteger() )
        return hexport
    }
    def getVersionTxt(){
        return "2.1"
    }