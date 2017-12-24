metadata {
	definition (name: "NukiLock Controller", namespace: "Volski", author: "Volski") {
    	command "lock"
        command "unlock"
        command "refresh"
        capability "Lock"
	}
	// simulator metadata
	simulator {
	}   
     preferences {
	    input("ServerIp", "string", title:"Server IP Address", description: "Please enter server ip address", required: true, displayDuringSetup: true)
    	input("ServerPort", "number", title:"Server Port", description: "Please enter your server Port", defaultValue: 8080 , required: false, displayDuringSetup: true)
        input("DevicePathOff", "string", title:"URL Path for Lock", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)
		input("DevicePathOn", "string", title:"URL Path for UnLock", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)
        input("DeviceStatus", "string", title:"URL Status", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)		
	}
	tiles {
		standardTile("door", "_Nuki", width: 3, height: 2, canChangeIcon: true) {
			state "locked", label: "Locked", action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#FF0000"
            state "waiting", label: "Waiting", action: "waitdevice", icon:"st.locks.lock.locked", backgroundColor:"#c0c0c0"
            state "unlocked", label: "Unlocked", action:"lock", icon:"st.locks.lock.unlocked", backgroundColor:"#79b821"
		}
        
         standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "default", action:"refresh", icon:"st.secondary.refresh"
    	}

		main "door"
        details "door", "refresh"
	}
    
}
def waitdevice(){}

def lock() {
    log.debug "---LOCK COMMAND--- ${DevicePathOff}"
	runCmd(DevicePathOff)
    refresh()
}

def unlock() {
    log.debug "---UNLOCK COMMAND--- ${DevicePathOn}"
    runCmd(DevicePathOn)
    refresh()
}

def refresh() {
	log.debug "Refreshing"
    sendEvent(name: "_Nuki", value: "waiting")
    runCmd(DeviceStatus)
}

def runCmd(String varCommand) {
	def host = ServerIp
	def LocalDevicePort = ''
	if (ServerPort==null) { LocalDevicePort = "8080" } else { LocalDevicePort = ServerPort }
	log.debug "The device id configured is: $device.deviceNetworkId"

	def path = varCommand
	log.debug "path is: $path"

	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	log.debug "The Header is $headers"
	def method = "GET"
	log.debug "The method is $method"
	try {
    	def hostHex = makeNetworkId(ServerIp, ServerPort)
		device.deviceNetworkId = hostHex
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			headers: headers
			)
		log.debug hubAction
       	sendHubCommand(hubAction)
		return hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

def parse(String description) {

	def msg = parseLanMessage(description)
  	def bodyString = msg.body
  	if (bodyString) {
    	def json = msg.json;
    	if (json) {
      	log.debug("Values received: ${json}")
      	log.debug("Lock State: ${json.stateName}")
        if(json.stateName!=null)
        	sendEvent(name: "_Nuki", value: json.stateName)
    	}
  	}
    else{
    	log.debug ("Error : ${msg}")
    }
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}
private String makeNetworkId(ipaddr, port) { 
     String hexIp = ipaddr.tokenize('.').collect { 
     String.format('%02X', it.toInteger()) 
     }.join() 
     String hexPort = String.format('%04X', port) 
     log.debug "${hexIp}:${hexPort}" 
     return "${hexIp}:${hexPort}" 
}
def installed() {
    log.info('called shade installed()')
    //runCmd(waiting)
    runEvery1Minute(refresh)
}

def updated() {
    log.info('called shade updated()')
    //runCmd(waiting)
    runEvery1Minute(refresh)
}
def initialize() {
	log.debug "Initialized"
	poll()
	// Schedule it to run every 1 minutes
	runEvery1Minute("refresh")
}
def poll(){
}