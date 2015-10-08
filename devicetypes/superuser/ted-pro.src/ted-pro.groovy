 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "TED Pro", author: "joer@noface.net") {

		capability "Energy Meter"
        capability "Power Meter"
		capability "Polling"
		capability "Refresh"

        attribute "now", "string"
        attribute "tdy", "string"
        attribute "mtd", "string"
        attribute "avg", "string"
        attribute "proj", "string"
        attribute "voltage", "string"
        attribute "phase", "string"

	}

	preferences {

    input("ip", "text", title: "IP", description: "The IP of your TED6000 device", required: true)
    input("port", "text", title: "port", description: "The port of your TED6000 device", required: true)
    input("usr", "text", title: "Username", description: "The username configured in Network Settings on your TED6000", required: false)
    input("pass", "password", title: "Password", description: "The password configured in Network Settings on your TED6000", required: false)
	
	}



	simulator {
		}
        
	tiles {
        
        chartTile(name: "EnergyChart", attribute: "device.energy.currentState.value")
        valueTile("Energy", "device.energy") {
            state "energy", label:'${currentValue} Wh'
            }
        valueTile("Power", "device.power") {
            state "power", label:'${currentValue} Wh'
            }
        valueTile("TDY", "device.tdy") {
            state "tdy", label:'${currentValue} Wh'
            }
       valueTile("MTD", "device.mtd") {
            state "mtd", label:'${currentValue} Wh'
            }
        valueTile("Avg", "device.avg") {
            state "avg", label:'${currentValue} Wh'
            }
        valueTile("Proj", "device.proj") {
            state "proj", label:'${currentValue} Wh'
            }
        valueTile("Phase", "device.phase") {
            state "phase", label:'${currentValue}'
            }
        standardTile("Refresh", "device.image") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["Energy"])
		details(["EnergyChart", "Energy", "Power", "Refresh", "TDY", "MTD", "Avg", "Proj", "Phase"])
	}
}



def poll() {
   
    log.debug "poll Ted Pro Home"
    goImportTed()
}

def refresh() {
	log.debug "refresh Ted Pro Home"
    goImportTed()
   
}

def goImportTed(){
	sendEvent(name: "energy", value: "polling", IsStateChange: "true")
	def callBack = getCallBackAddress()
 //   log.debug "Ted callback $callBack"
 //   log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    def porthex = convertPortToHex(port)
	def hosthex = convertIPtoHex(ip)
    def request = """GET /api/DashData.xml HTTP/1.1\r\nAccept: */*\r\nHost: $ip:$port\r\n\r\n"""
	log.debug "Ted Request: $request"
	device.deviceNetworkId = "$hosthex:$porthex"
    log.debug "The device id configured is: $device.deviceNetworkId"
    try {
    	def hubAction = new physicalgraph.device.HubAction(request, physicalgraph.device.Protocol.LAN, "${device.deviceNetworkId}")
		return hubAction
        } catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }

}

// This next method is only used from the simulator
def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
	} else if (cmd.scale == 1) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
	}
	else {
		[name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}
}


def parse(String description) {
	def msg = parseLanMessage(description)
    log.debug "${msg.data}"
    log.debug "${msg.data.name()}"
    
    sendEvent(name: "now", value: msg.data.Now.text(), IsStateChange: "true")
  	sendEvent(name: "power", value: msg.data.Now.text(), IsStateChange: "true")
    sendEvent(name: "tdy", value:  msg.data.TDY.text(), IsStateChange: "true")
    sendEvent(name: "mtd", value: msg.data.MTD.text(), IsStateChange: "true")
	sendEvent(name: "energy", value: msg.data.MTD.text(), IsStateChange: "true")
    sendEvent(name: "avg", value: msg.data.Avg.text(), IsStateChange: "true")
    sendEvent(name: "proj", value: msg.data.Proj.text(), IsStateChange: "true")
    sendEvent(name: "voltage", value: msg.data.Voltage.text(), IsStateChange: "true")
    sendEvent(name: "phase", value: msg.data.Phase.text(), IsStateChange: "true")
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug "Port entered is $port and the converted hex code is $hexport"
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex, 16)
}


private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]), convertHexToInt(hex[2..3]), convertHexToInt(hex[4..5]), convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}
