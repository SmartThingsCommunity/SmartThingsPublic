import java.security.MessageDigest
 
preferences {
    input("email", "password", title: "Email Address", description: "Your email address")
    input("pin", "password", title: "pin code", description: "Your pin code")
	input("roomID", "text", title: "Room ID", description: "The room id")
    input("deviceID", "text", title: "Device ID", description: "The device id")
}
 
metadata {
	definition (name: "Lightwave Lights", namespace: "smartthings-users", author: "Adam Clark") {
		capability "Switch"
	}

	simulator {}

	tiles {
    
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'on', action:"on", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"off"
			state "off", label:'off', action:"off", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"on"
		}
        
		main "switch"
		details (["switch"])
	}

}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

// handle commands
def on() {
    sendEvent(name: "switch", value: 'off')
    log.debug "Executing 'off'"
    sendCommand(settings.roomID, settings.deviceID, 0)
}

def off() {
    sendEvent(name: "switch", value: 'on')
	log.debug "Executing 'on'"
    sendCommand(settings.roomID, settings.deviceID, 1)
}

def MD5(s) {
	def digest = MessageDigest.getInstance("MD5")
	new BigInteger(1,digest.digest(s.getBytes())).toString(16).padLeft(32,"0")
} 

def timestamp() {
	Calendar.getInstance(TimeZone.getTimeZone('GMT')).getTimeInMillis().toString().substring(0,10)
}

def sendCommand(room, device, status) {

    def params = [
        uri: 'http://lightwaverfhost.co.uk/mobile/getsessionkey.php?email=' + settings.email + '&pin=' + settings.pin,
        headers: [
            'Origin' : 'lightwaverfhost.co.uk',
            'Host'   : 'lightwaverfhost.co.uk'
        ]
    ]

    httpGet(params) {response ->
    	def loginResponse = response.data.toString().tokenize('~')
        def secret = loginResponse[0]
        def mac = loginResponse[1]
        def currentTimestamp = timestamp()
        def commandParams = [
            uri: 'http://lightwaverfhost.co.uk/mobile/writerecord.php',
        	headers: [
                'Origin' : 'lightwaverfhost.co.uk',
                'Host'   : 'lightwaverfhost.co.uk'  
            ],
            body : [
                'signature' : 'JSMobile',
                'secret' : MD5(secret.trim() + currentTimestamp),
                'timestamp' : timestamp(),
                'action' : 'I',
                'email' : settings.email,
                'name' : mac,
                'commandstring' : '!R' + room + 'D' + device + 'F' + status + '|SMARTTHINGS|SMARTTHINGS'
            ]
        ]
        
        httpPost(commandParams) {commandResponse ->
        	log.debug commandResponse.data
        }
        
    }
    
}
