
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
	section("Sonoff Host") {
        input "ipAddress", "string", title: "IP Address", required: true
	}

	section("Authentication") {
		input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: false, required: false)
		input(name: "password", type: "password", title: "Password", description: "Password", displayDuringSetup: false, required: false)
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

	// SmartApps cannot create LAN requests
	// Create a device that is specifically used to discover Sonoff device settings

	// get (or create) the discovery device
	def discoveryDevice = getDiscoveryDevice()

	// tell the discovery device to make a
	discoveryDevice.discover();
}

def discoverCompleted(Map deviceSettings){
	log.debug "$deviceSettings";
	deleteDiscoveryDevice();

}



def getDiscoveryDevice(){
	def namespace = app.namespace
	def deviceName = "Sonoff-Tasmota Discovery Device"
	def theHubId = location.hubs[0].id
	def deviceId = "${app.id}-discoveryDevice"

	def childDevice = getChildDevices().find {
			it.deviceNetworkId == deviceId
	}
		
	if (!childDevice){
		def deviceMap = [completedSetup: false]

		deviceMap['name'] = app.label + " - Device Discovery";
		deviceMap['label'] = deviceMap['name'];

		childDevice = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
	}

	return childDevice
}

def deleteDiscoveryDevice(){
	def deviceId = "${app.id}-discoveryDevice"

	def childDevice = getChildDevices().find {
			it.deviceNetworkId == deviceId
	}

	if (childDevice)
	{
		deleteChildDevice(childDevice.deviceNetworkId)	
	}
}
