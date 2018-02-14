metadata {
	definition(name: "Tasmota", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Polling"
		capability "Refresh"

        command "reload"
	}

	// UI tile definitions
	tiles(scale: 2) {
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "refresh"
		details(["refresh"])
	}

	preferences {
		section("Tasmota Host") {
            input(name: "ipAddress", type: "string", title: "IP Address", description: "IP Address of Tasmota Device", displayDuringSetup: true, required: true)
		}

		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: false, required: false)
			input(name: "password", type: "password", title: "Password (sent cleartext)", description: "Caution: password is sent cleartext", displayDuringSetup: false, required: false)
		}
	}
}

def parse(String description) {
	log.debug description;
}

def initializeChild(Map options){
    state.ipAddress = options.ipAddress;
    state.username = options.username;
    state.password = options.password;

    reload();
}

def reload(){
    state.module = null;
    state.gpio = null;

    sendCommand("module", null, getModuleCompleted);
    sendCommand("gpio", null, getGpioCompleted);
}

def getModuleCompleted(physicalgraph.device.HubResponse response){
    state.module = response.json.Module;

    spawnChildDevices();
}

def getGpioCompleted(physicalgraph.device.HubResponse response){
    state.gpio = response.json;

    spawnChildDevices();
}

def spawnChildDevices(){
    if (state.module && state.gpio){
        log.debug "GPIO: ${state.gpio}"
        log.debug "Module: ${state.module}"

        def devices = [:];

        def parentId = parent.getId();

        def moduleId = state.module.split()[0].toInteger();

        switch (moduleId){
            case 1: // Sonoff Basic
                devices[parentId + '-Power'] = [namespace : "BrettSheleski", type: "Tasmota-Power", options : [powerChannel : 1]]
                break;
            case 23: // Sonoff 4CH Pro
            case 7: // Sonoff 4CH
                devices[parentId + '-Power-ch1'] = [namespace : "BrettSheleski", type: "Tasmota-Power", options : [powerChannel : 1]]
                devices[parentId + '-Power-ch2'] = [namespace : "BrettSheleski", type: "Tasmota-Power", options : [powerChannel : 2]]
                devices[parentId + '-Power-ch3'] = [namespace : "BrettSheleski", type: "Tasmota-Power", options : [powerChannel : 3]]
                devices[parentId + '-Power-ch4'] = [namespace : "BrettSheleski", type: "Tasmota-Power", options : [powerChannel : 4]]
                break;

            case 25: // Sonoff Bridge

                for ( i in 1..16 )
                {
                    devices[parentId + "-RF-Key${i}"] = [namespace : "BrettSheleski", type: "Tasmota-RF-Bridge Button", options : [keyNumber : i]]
                }
                break;

            case 2: // Sonoff RF
            case 3: // Sonoff SV
            case 4: // Sonoff TH
            case 5: // Sonoff Dual
            case 6: // Sonoff Pow
            case 8: // S20 Socket
            case 9: // Slampher
            case 10: // Sonoff Touch
            case 11: // Sonoff LED
            case 12: // 1 Channel
            case 13: // 4 Channel
            case 14: // Motor C/AC
            case 15: // ElectroDragon
            case 16: // EXS Relay
            case 17: // WiOn
            case 18: // Generic
            case 19: // Sonoff Dev
            case 20: // H801
            case 21: // Sonoff SC
            case 22: // Sonoff BN-SZ
            case 24: // Huafan SS
            case 26: // Sonoff B1
            case 27: // AiLight
            case 28: // Sonoff T1 1CH
            case 29: // Sonoff T1 2CH
            case 30: // Sonoff T1 3CH
            case 31: // Supla Espablo
            case 32: // Witty Cloud
            case 33: // Yunshan Relay
            case 34: // MagicHome
            case 35: // Luani HVIO
            case 36: // KMC 70011
            case 37: // Arilux LC01
            case 38: // Arilux LC11
            case 39: // Sonoff Dual R2
            case 40: // Arilux LC06
            case 41: // Sonoff S31
            default:
                log.debug "Unknown Module ${state.module}"
                break;
        }

        def existingDevices = getChildDevices();
        def deviceConfig = null;

        existingDevices.each{
            deviceConfig = devices[it.deviceNetworkId];

            log.debug "Existing Child DEVICE : ${it}"
            if (deviceConfig){

                log.debug "CHILD OPTIONS: ${deviceConfig}"


                // the device is already created previously.
                // remove it from the Map so we dont create a duplicate
                devices.remove(it.deviceNetworkId);

                it.initializeChild(deviceConfig["options"])
            }
            else{
                // there's a child device with an unknown ID, delete it

                deleteChildDevice(it.deviceNetworkId);
            }
        }

        def theHubId = location.hubs[0].id

        for (e in devices){
            log.debug "DEVICE : ${e}"
            def childDevice = addChildDevice(e.value.namespace, e.value.type, e.key, theHubId, e.value.options)
        }

    }
}

def poll() {
	log.debug "POLL"
	sendCommand("Status", null, refreshCallback)
}

def refresh() {
	log.debug "REFRESH"
	sendCommand("Status", null, refreshCallback)
}

def updateStatus(status){

}

def refreshCallback(physicalgraph.device.HubResponse response){
    def jsobj = response?.json;

    log.debug "JSON: ${jsobj}";

    // need to send jsobj to all child devices.

    def childDevices = getChildDevices();

    childDevices.each{
        it.updateStatus(jsobj);
    }
}

def sendCommand(String command, callback) {
    return sendCommand(command, null);
}

def sendCommand(String command, payload, callback) {
	sendHubCommand(createCommand(command, payload, callback))
}

private String convertIPtoHex(ipAddress) { 
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format('%04x', port.toInteger())
	return hexport
}

def createCommand(String command, payload, callback){
    log.debug "createCommandAction(${command}:${payload}) to device at ${state.ipAddress}:80"

	if (!state.ipAddress) {
		log.warn "aborting. ip address of device not set"
		return null;
	}

	def path = "/cm"
	if (payload){
		path += "?cmnd=${command}%20${payload}"
	}
	else{
		path += "?cmnd=${command}"
	}

	if (state.username){
		path += "&user=${state.username}"
		if (state.password){
			path += "&password=${state.password}"
		}
	}

    def dni = null;

    def params = [
        method: "GET",
        path: path,
        headers: [
            HOST: "${state.ipAddress}:80"
        ]
    ]

    def options = [
        callback : callback
    ];

	def hubAction = new physicalgraph.device.HubAction(params, dni, options);
}
