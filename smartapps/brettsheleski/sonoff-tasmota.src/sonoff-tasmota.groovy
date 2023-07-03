
definition(
    name: "Sonoff-Tasmota",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "SmartApp for the Sonoff-Tasmota firmware.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

preferences {
	page("mainPage", "Main Configuration"){
		section("Sonoff Host") {
			input "ipAddress", "string", title: "IP Address", required: true
		}

		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: true, required: false)
			input(name: "password", type: "password", title: "Password", description: "Password", displayDuringSetup: true, required: false)
		}

		section("Module"){
			input(name: "moduleType", type: "enum", title: "Module Type", description: "Module Type", displayDuringSetup: true, required: false, refreshInterval: 5, options : getModuleTypesEnum())
		}
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize(){
	sendCommand("Status", "0", "discoverCompleted")
}

def discoverCompleted(physicalgraph.device.HubResponse hubResponse){
	
	log.debug "Discovery Completed"

	def body = hubResponse.body;
	def allSettings = [:];
	

	if (body){
		//log.debug "RESPONSE: ${body}";

		def slurper = new groovy.json.JsonSlurper();

		def settingsLine;
        

        def lines = body.split('\n');

        lines.each {
			settingsLine = slurper.parseText(it.substring(it.indexOf('=') + 1));
			allSettings << settingsLine;
        }

		def json = new groovy.json.JsonBuilder(allSettings).toPrettyString();
		log.debug "SETTINGS : ${json}"

		state.settings = allSettings;

		setModuleTypeById(allSettings.Status.Module);

		initializeCompleted(allSettings);
	}
	else{
		log.debug "NO BODY!"
	}
}

def setModuleTypeById(int id){

	log.debug "Trying to set module for type ID : $id";

	getModuleTypesMap().each{key, value -> 
		if (key == "$id"){
			log.debug "Setting Module Type to ${value.name}"
			settings.moduleType = value.name
		}
	}
}

def getModuleTypesEnum(){

	def types = [];

	getModuleTypesMap().each{key, value -> types << value.name}

	return types;
}

def getModuleTypesMap(){
	def deviceTypes = [:];

	deviceTypes["1"] = ["name" : "Sonoff Basic"]

	deviceTypes["2"] = ["name" : "Sonoff RF"]

	deviceTypes["18"] = ["name" : "WeMos D1 mini"]

	deviceTypes["25"] = ["name" : "Sonoff Bridge"]

	return deviceTypes;
}

def initializeCompleted(Map options = null){

	def deviceTypesMap = getModuleTypesMap();

	if (options == null)
	{
		options = state.settings;
	}

	if (options?.Status?.Module == 1){ // Sonoff Basic
		initializeSonoffBasic(options);
	}
	else if (options?.Status?.Module == 20){ // Sonoff RF Bridge
		initializeSonoffRfBridge(options);
	}
}

def initializeSonoffBasic(Map options){

}

def initializeSonoffRfBridge(Map options){
	def children = getChildDevices()
	def namespace = app.namespace
	def deviceName = "Sonoff-Tasmota RF Bridge Button"
	def theHubId = location.hubs[0].id


	for ( i in 1..16 )
	{
		def deviceId = "${app.id}-key${i}"
		def childDevice = children.find {
				it.deviceNetworkId == deviceId
			}
		
		
		if (childDevice){
			log.debug "FOUND child device found for ${childDevice.deviceNetworkId}"
			children.remove(childDevice)
		}
		else{
			def deviceMap = [completedSetup: false]

			deviceMap['name'] = app.label + " - Key $i";
			deviceMap['label'] = deviceMap['name'];

			childDevice = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
		}

		childDevice.initChild([keyNumber : i]);

		// remove any unused child Devices
		removeChildDevices(children);
	}
}

private def sendCommand(String command, String payload, String callback = null){

    log.debug "sendCommand(${command}:${payload})"

	def ipAddress = ipAddress;
	def port = 80;
    def hosthex = convertIPtoHex(ipAddress);
    def porthex = convertPortToHex(port);

	def dni = "$hosthex:$porthex";

	def deviceNetworkId = "$hosthex:$porthex"

	def path = "/cm"

	def options = [callback : callback];
	def headers = ["HOST": "$ipAddress:$port"]
	def query = [:]

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

	def params = [path : path, method : "GET", protocol : physicalgraph.device.Protocol.LAN, headers : headers, query : query]

	def hubAction = new physicalgraph.device.HubAction(params, dni, options) 

	sendHubCommand(hubAction)
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format('%04x', port.toInteger())
    return hexport
}