/**
 *  Sharp Aquos TV
 *     Works on Sharp TVs
 *    Very basic asynchronous, non-polling control of most Sharp TVs made since 2010
 *   NOTE: PLEASE HARDCODE THE INPUTS YOU WANT TO CONTROL ON LINE 92
 *
 *    Losely based on: https://github.com/halkeye/sharp.aquos.devicetype.groovy
 *   and page 36 of this manual http://snpi.dell.com/sna/manuals/A1534250.pdf
 */

preferences {
	input("destIp", "text", title: "IP", description: "The device IP",required:true)
	input("destPort", "number", title: "Port", description: "The port you wish to connect", required:true)
	input("login", "text", title: "Login", description: "The login")
	input("password", "password", title: "Password", description: "The password")}
 

metadata {
	definition (name: "Sharp Aquos", namespace: "KristopherKubicki", 
    	author: "kristopher@acm.org") {
        capability "Actuator" 
		capability "Switch"
        capability "Music Player"
    
        attribute "input", "string"
        attribute "blocked", "number"
        
        command "inputNext"
        
      	}

	simulator {
		// TODO-: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics18"
            state "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics18"
        }
        standardTile("mute", "device.mute", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "muted", label: '${name}', action:"unmute", backgroundColor: "#79b821", icon:"st.Electronics.electronics13"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: "#ffffff", icon:"st.Electronics.electronics13"
		}
        standardTile("input", "device.input", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "input", label: 'Toggle', action: "inputNext", icon: "", backgroundColor: "#FFFFFF"
		}
        standardTile("mute", "device.mute", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "muted", label: '${name}', action:"unmute", backgroundColor: "#79b821", icon:"st.Electronics.electronics13"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: "#ffffff", icon:"st.Electronics.electronics13"
		}
        controlTile("level", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range: "(0..60)") {
			state "level", label: '${name}', action:"setLevel"
		}
   
		main "switch"
        details(["switch","input","mute","level"])
	}
}


//  There is no real parser for this device
// ST cannot interpret the raw packet return, and thus we cannot 
//  do anything with the return data.  
//  http://community.smartthings.com/t/raw-tcp-socket-communications-with-sendhubcommand/4710/10
//
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def setLevel(val) {
	val = sprintf("%02d",val)
	sendEvent(name: "mute", value: "unmuted")
    sendEvent(name: "level", value: val)    
    request("VOLM" + val + "  \r")
}

def mute() { 
	sendEvent(name: "mute", value: "muted")
	request('MUTE1   \r')
}

def unmute() { 
	sendEvent(name: "mute", value: "unmuted")
	request('MUTE2   \r')
}

def inputNext() { 
	//request('ITGDx   \r')

	def cur = device.currentValue("input")
    def selectedInputs = ["1","2","1"]
    
    def semaphore = 0
    for(selectedInput in selectedInputs) {
    	if(semaphore == 1) { 
        	return inputSelect(selectedInput)
        }
    	if(cur == selectedInput) { 
        	semaphore = 1
        }
    }
     
    return inputSelect(selectedInputs[0]) 
}


def inputSelect(channel) {
	sendEvent(name: "input", value: channel)
	request("IAVD$channel   \r")
}


// If lastAction is not null, we should probably block
def on() {
	sendEvent(name: "switch", value: 'on')
    sendEvent(name: "mute", value: "unmuted")
	request("POWR1   \r")
}

def off() { 
	sendEvent(name: "switch", value: 'off')
	request("RSPW2   \r\rPOWR0   \r")
}

def request(body) { 

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

	
// sleep up to 9 seconds before issuing the next command
	
    def cur = new BigDecimal(device.currentValue("blocked") ?: 0)
    
    def waitTime = 0
    def cmds = []
    def c = new GregorianCalendar()
    if(cur > 0 && cur > c.time.time && cur - c.time.time < 9000) { 
    	waitTime = cur - c.time.time
		cmds << "delay $waitTime"
    }
    c = new GregorianCalendar()
    sendEvent(name: "blocked", value: c.time.time + 9000 + waitTime)

    def hubAction = new physicalgraph.device.HubAction(body,physicalgraph.device.Protocol.LAN)
	cmds << hubAction

    log.debug cmds
        
    cmds
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}