metadata {
    definition (name: "Securifi SZ-KFB01 Key Fob", namespace: "jfurtner", author: "jfurtner") {
    
    capability "Configuration"
    capability "Button"
    capability "Speech Recognition"
    
    //desc: 08 0104 0401 00 03 0000 0003 0500 02 0003 0501
    //inCluster - 0x0500 - IAS Zone, outCluster- 0x0501 - IAS ACE
    fingerprint profileId: "0104", deviceId: "0401", inClusters: "0000,0003,0500", outClusters: "0003,0501"
    }

    tiles {
	    standardTile("button", "device.button", width: 2, height: 2) {
		    state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        }
        valueTile("logEvent", 'device.phraseSpoken', width:2, height: 2) {
        	state 'val', label:'${currentValue', defaultState:''
        }
    }
    main (["button"])
    details (["button", 'logEvent'])
}

def parse(String description) {	       	            
	logDebug "** KFB01 parse received ** $description"
    if (description?.startsWith('enroll request')) {        
        List cmds = enrollResponse()
        logDebug "enroll response: ${cmds}"
        def result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        return result    
    } else if (description?.startsWith('catchall:')) {
        def msg = zigbee.parse(description)
        //logDebug msg
        buttonPush(msg.data[0])
    }   
}

def buttonPush(button){
    //Button Numbering vs positioning is slightly counterintuitive
    //Bottom Left Button (Unlock) = 0 and goes counterclockwise
    //Securifi Numbering - 0 = Unlock, 1 = * (only used to join), 2 = Home, 3 = Lock
    //For ST App Purposes 1=Lock, 2=Home, 3=Unlock , 4 = * (only used to join)
    def name = null
    if (button == 0) {
        //Unlock - ST Button 3
        name = "3"
        def currentST = device.currentState("button3")?.value
        //logDebug "Unlock button Pushed"           
    } else if (button == 2) {
    	//Home - ST Button 2
        name = "2"
        def currentST = device.currentState("button2")?.value
        //logDebug "Home button pushed"        
    } else if (button == 3) {
        //Lock ST Button 1
        name = "1"
     	def currentST = device.currentState("button")?.value
        //logDebug "Lock Button pushed"         
    } 

    def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    //logDebug "Parse returned ${result?.descriptionText}"
    return result
}


def enrollResponse() {
    logDebug "Sending enroll response"
    [            
    "raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1"        
    ]
}


def configure(){
    logDebug "Config Called"
    def configCmds = [
    "zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}"
    ]
    return configCmds
}

def logDebug(String message) {
	log.debug(message)
    sendEvent(name: 'phraseSpoken', value: message)
}
