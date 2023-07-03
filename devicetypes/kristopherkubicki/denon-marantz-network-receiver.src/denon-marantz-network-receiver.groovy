/**
 *  Denon / Marantz Network Receiver
 *     Works on Network Receivers newer than 2012
 *    SmartThings driver to connect your Denon Network Receiver to SmartThings
 *
 */

preferences {
	input("destIp", "text", title: "IP", description: "The device IP")
    input("destPort", "number", title: "Port", description: "The port you wish to connect", defaultValue: 80)
}
 

metadata {
	definition (name: "Denon / Marantz Network Receiver", namespace: "KristopherKubicki", 
    	author: "kristopher@acm.org") {
        capability "Actuator"
        capability "Switch" 
        capability "Polling"
        capability "Switch Level"
        
        attribute "mute", "string"
        attribute "input", "string"
        attribute "inputChan", "enum"
        
        command "mute"
        command "unmute"
        command "inputSelect", ["string"] //define that inputSelect takes a string of the input name as a parameter
        command "inputNext"
        command "toggleMute"
        
      	}

	simulator {
		// TODO-: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics16"
            state "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics16"
        }
		standardTile("poll", "device.poll", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
		}
        standardTile("input", "device.input", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "input", label: '${currentValue}', action: "inputNext", icon: "", backgroundColor: "#FFFFFF"
		}
        standardTile("mute", "device.mute", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "muted", label: '${name}', action:"unmute", backgroundColor: "#79b821", icon:"st.Electronics.electronics13"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: "#ffffff", icon:"st.Electronics.electronics13"
		}
        controlTile("level", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range: "(0..100)") {
			state "level", label: '${name}', action:"setLevel"
		}
        
		main "switch"
        details(["switch","input","mute","level","poll"])
	}
}



def parse(String description) {
	log.debug "Parsing '${description}'"
    
 	def map = stringToMap(description)

    
    if(!map.body || map.body == "DQo=") { return }
        log.debug "${map.body} "
	def body = new String(map.body.decodeBase64())
    
	def statusrsp = new XmlSlurper().parseText(body)
	def power = statusrsp.Power.value.text()
    if(power == "ON") { 
    	sendEvent(name: "switch", value: 'on')
    }
    if(power != "" && power != "ON") { 
    	sendEvent(name: "switch", value: 'off')
    }
    

    def muteLevel = statusrsp.Mute.value.text()
    if(muteLevel == "on") { 
    	sendEvent(name: "mute", value: 'muted')
	}
    if(muteLevel != "" && muteLevel != "on") {
	    sendEvent(name: "mute", value: 'unmuted')
    }
    
	def inputCanonical = statusrsp.InputFuncSelect.value.text()
    def inputTmp = []
    //check to see if the VideoSelectLists node is available
    if(!statusrsp.VideoSelectLists.isEmpty()){
    	log.debug "VideoSelectLists is available... parsing"
        statusrsp.VideoSelectLists.value.each {
        	log.debug "$it"
            if(it.@index != "ON" && it.@index != "OFF") {
                inputTmp.push(it.'@index')
                log.debug "Adding Input ${it.@index}"
                if(it.toString().trim() == inputCanonical) {     
                    sendEvent(name: "input", value: it.'@index')
                }
            }
        }
    }
    //if the VideoSelectLists node is not available, let's try the InputFuncList
    else if(!statusrsp.InputFuncList.isEmpty()){
    	log.debug "InputFuncList is available... parsing"
        statusrsp.InputFuncList.value.each {
            if(it != "ON" && it != "OFF") {
                inputTmp.push(it)
                //log.debug "Adding Input ${it}"
                if(it.toString().trim() == inputCanonical) {     
                    sendEvent(name: "input", value: it)
                }
            }
        }
    }
    
    sendEvent(name: "inputChan", value: inputTmp)
    
    if(statusrsp.MasterVolume.value.text()) { 
    	def int volLevel = (int) statusrsp.MasterVolume.value.toFloat() ?: -40.0
        volLevel = (volLevel + 80) * 0.9
        
   		def int curLevel = 36
        try {
        	curLevel = device.currentValue("level")
        } catch(NumberFormatException nfe) { 
        	curLevel = 36
        }
	
        if(curLevel != volLevel) {
    		sendEvent(name: "level", value: volLevel)
        }
    } 
}


def setLevel(val) {
	sendEvent(name: "mute", value: "unmuted")     
    sendEvent(name: "level", value: val)
	def int scaledVal = val * 0.9 - 80
	request("cmd0=PutMasterVolumeSet%2F$scaledVal")
}

def on() {
	sendEvent(name: "switch", value: 'on')
	request('cmd0=PutZone_OnOff%2FON')
}

def off() { 
	sendEvent(name: "switch", value: 'off')
	request('cmd0=PutZone_OnOff%2FOFF')
}

def mute() { 
	sendEvent(name: "mute", value: "muted")
	request('cmd0=PutVolumeMute%2FON')
}

def unmute() { 
	sendEvent(name: "mute", value: "unmuted")
	request('cmd0=PutVolumeMute%2FOFF')
}

def toggleMute(){
    if(device.currentValue("mute") == "muted") { unmute() }
	else { mute() }
}

def inputNext() { 

	def cur = device.currentValue("input")   
    def selectedInputs = device.currentValue("inputChan").substring(1,device.currentValue("inputChan").length()-1).split(', ').collect{it}
    selectedInputs.push(selectedInputs[0])
    log.debug "SELECTED: $selectedInputs"
    
    def semaphore = 0
    for(selectedInput in selectedInputs) {
    	if(semaphore == 1) { 
//        	log.debug "SELECT: ($semaphore) '$selectedInput'"
        	return inputSelect(selectedInput)
        }
    	if(cur == selectedInput) { 
        	semaphore = 1
        }
    }
}


def inputSelect(channel) {
 	sendEvent(name: "input", value: channel	)
	request("cmd0=PutZone_InputFunction%2F$channel")
}

def poll() { 
	refresh()
}

def refresh() {

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'GET',
    		'path': "/goform/formMainZone_MainZoneXml.xml",
            'headers': [ HOST: "$destIp:$destPort" ] 
		) 
        
    hubAction
}

def request(body) { 

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'POST',
    		'path': "/MainZone/index.put.asp",
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