metadata {
	definition(name: "Tasmota", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Polling"
		capability "Refresh"

        command "reload"

        attribute "module", "string"
        attribute "friendlyName", "string"
        attribute "version", "string"
        attribute "topic", "string"
        attribute "groupTopic", "string"
        attribute "ssid1", "string"
        attribute "ssid2", "string"
        attribute "hostname", "string"
        attribute "macAddress", "string"
        attribute "upTime", "string"
        attribute "vcc", "number"
        attribute "apSsid", "string"
        attribute "apMac", "string"
	}

	// UI tile definitions
	tiles(scale: 2) {
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

        valueTile("module", "module", width: 3, height: 1) {
			state "module", label: 'Module: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("friendlyName", "friendlyName", width: 3, height: 1) {
			state "friendlyName", label: "Friendly Name: ${currentValue}", backgroundColor: "#ffffff"
		}

        valueTile("version", "version", width: 3, height: 1) {
			state "version", label: 'Tasmota Version: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("topic", "topic", width: 3, height: 1) {
			state "topic", label: 'Topic: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("groupTopic", "groupTopic", width: 3, height: 1) {
			state "groupTopic", label: "Group Topic: ${currentValue}", backgroundColor: "#ffffff"
		}

        valueTile("ssid1", "ssid1", width: 3, height: 1) {
			state "ssid1", label: 'SSID #1: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("ssid2", "ssid2", width: 3, height: 1) {
			state "ssid2", label: 'SSID #2: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("hostname", "hostname", width: 3, height: 1) {
			state "hostname", label: 'Hostname: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("macAddress", "macAddress", width: 3, height: 1) {
			state "macAddress", label: 'MAC Address: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("upTime", "upTime", width: 3, height: 1) {
			state "upTime", label: "Up Time: ${currentValue}", backgroundColor: "#ffffff"
		}

        valueTile("vcc", "vcc", width: 3, height: 1) {
			state "vcc", label: 'VCC: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("apSsid", "apSsid", width: 3, height: 1) {
			state "apSsid", label: 'AP SSID: ${currentValue}', backgroundColor: "#ffffff"
		}

        valueTile("apMac", "apMac", width: 3, height: 1) {
			state "apMac", label: 'AP MAC: ${currentValue}', backgroundColor: "#ffffff"
		}

		main "refresh"
		details(["refresh", "module", "friendlyName", "version", "topic", "groupTopic", "ssid1", "ssid2", "hostname", "macAddress", "upTime", "vcc", "apSsid", "apMac"])
	}
}

def parse(String description) {
	log.debug description;
}

def initializeChild(){
    reload();
}

def reload(){
    state.module = null;
    state.gpio = null;

    sendCommand("module", null, getModuleCompleted);
    sendCommand("gpio", null, getGpioCompleted);

    refresh();
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
        def appLabel = parent.getAppLabel();

        log.debug "PARENT LABEL : $appLabel"

        switch (moduleId){

            case 1: // Sonoff Basic

            // the next batch are mostly guesses if they work.  
            //  I'm assuming they will provide basic functionality until more specific implementations are done
            case 2: // Sonoff RF
            case 3: // Sonoff SV
            case 4: // Sonoff TH
            case 6: // Sonoff Pow
            case 8: // S20 Socket
            case 9: // Slampher
            case 10: // Sonoff Touch
            case 11: // Sonoff LED
            case 12: // 1 Channel
            case 21: // Sonoff SC
            case 22: // Sonoff BN-SZ
            case 26: // Sonoff B1
            case 28: // Sonoff T1 1CH
            case 41: // Sonoff S31
                devices[parentId + '-Power'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch", options : [powerChannel : 1]]
                break;

            case 5: // Sonoff Dual
            case 19: // Sonoff Dev
            case 39: // Sonoff Dual R2
            case 29: // Sonoff T1 2CH
                devices[parentId + '-Power-ch1'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 1", options : [powerChannel : 1]]
                devices[parentId + '-Power-ch2'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 2", options : [powerChannel : 2]]
                break;
            
            case 7: // Sonoff 4CH
            case 13: // 4 Channel
            
            case 23: // Sonoff 4CH Pro
                devices[parentId + '-Power-ch1'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 1", options : [powerChannel : 1]]
                devices[parentId + '-Power-ch2'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 2", options : [powerChannel : 2]]
                devices[parentId + '-Power-ch3'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 3", options : [powerChannel : 3]]
                devices[parentId + '-Power-ch4'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 4", options : [powerChannel : 4]]
                break;

            case 30: // Sonoff T1 3CH
                devices[parentId + '-Power-ch1'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 1", options : [powerChannel : 1]]
                devices[parentId + '-Power-ch2'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 2", options : [powerChannel : 2]]
                devices[parentId + '-Power-ch3'] = [namespace : "BrettSheleski", type: "Tasmota-Power", label : "${appLabel} Switch - Channel 3", options : [powerChannel : 3]]
                break;

            case 25: // Sonoff Bridge

                for ( i in 1..16 )
                {
                    devices[parentId + "-RF-Key${i}"] = [namespace : "BrettSheleski", type: "Tasmota-RF-Bridge Button", label : "${appLabel} - Button ${i}", options : [keyNumber : i]]
                }
                break;

            case 14: // Motor C/AC
            case 15: // ElectroDragon
            case 16: // EXS Relay
            case 17: // WiOn
            case 18: // Generic (eg: Wemos D1 Mini)
            case 20: // H801
            case 24: // Huafan SS
            case 27: // AiLight
            case 31: // Supla Espablo
            case 32: // Witty Cloud
            case 33: // Yunshan Relay
            case 34: // MagicHome
            case 35: // Luani HVIO
            case 36: // KMC 70011
            case 37: // Arilux LC01
            case 38: // Arilux LC11
            case 40: // Arilux LC06
            
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


                it.initializeChild(deviceConfig["options"])

                // the device is already created previously.
                // remove it from the Map so we dont create a duplicate
                devices.remove(it.deviceNetworkId);
            }
            else{
                // there's a child device with an unknown ID, delete it
                deleteChildDevice(it.deviceNetworkId);
            }
        }

        def theHubId = location.hubs[0].id
        def label = null;
        def properties = [:];

        for (e in devices){
            log.debug "DEVICE : ${e}"
            
            properties = [
                label : e.value.label,
                name : e.value.label,
                completedSetup : true
            ];

            properties = properties + e.value.options;

            def childDevice = addChildDevice(e.value.namespace, e.value.type, e.key, theHubId, properties)

            childDevice.initializeChild(e.value.options)
        }

    }
}

def poll() {
	log.debug "POLL"
	sendCommand("Status", "0", refreshCallback)
}

def refresh() {
	log.debug "REFRESH"
	sendCommand("Status", "0", refreshCallback)
}

def updateStatus(status){

}

def refreshCallback(physicalgraph.device.HubResponse response){
    def jsobj = response?.json;

    log.debug "JSON: ${jsobj}";


    sendEvent(name : "module", value : response?.json?.Status?.Module)
    sendEvent(name : "friendlyName", value : response?.json?.Status?.FriendlyName)
    sendEvent(name : "version", value : response?.json?.StatusFWR?.Version)
    sendEvent(name : "topic", value : response?.json?.Status?.Topic)
    sendEvent(name : "groupTopic", value : response?.json?.Status?.GroupTopic)
    sendEvent(name : "ssid1", value : response?.json?.StatusLOG?.SSId1)
    sendEvent(name : "ssid2", value : response?.json?.StatusLOG?.SSId2)
    sendEvent(name : "hostname", value : response?.json?.StatusNET?.Hostname)
    sendEvent(name : "macAddress", value : response?.json?.StatusNET?.Mac)
    sendEvent(name : "upTime", value : response?.json?.StatusSTS?.UPtime)
    sendEvent(name : "vcc", value : response?.json?.StatusSTS?.Vcc)
    sendEvent(name : "apSsid", value : response?.json?.StatusSTS?.Wifi?.SSId)
    sendEvent(name : "apMac", value : response?.json?.StatusSTS?.Wifi?.APMac)

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

    def hostInfo = parent.getHostInfo();

    def ipAddress = hostInfo.ipAddress;
    def username = hostInfo.username;
    def password = hostInfo.password;

    log.debug "createCommandAction(${command}:${payload}) to device at ${ipAddress}:80"

	if (!ipAddress) {
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

	if (username){
		path += "&user=${username}"
		if (password){
			path += "&password=${password}"
		}
	}

    def dni = null;

    def params = [
        method: "GET",
        path: path,
        headers: [
            HOST: "${ipAddress}:80"
        ]
    ]

    def options = [
        callback : callback
    ];

	def hubAction = new physicalgraph.device.HubAction(params, dni, options);
}
