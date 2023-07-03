
definition(
    name: "Sonoff-Tasmota RF Bridge",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "SmartApp for the Sonoff RF Bridge running Tasmota firmware.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

preferences {
	section("Sonoff Host") {
        input "ipAddress", "string", title: "IP Address", required: true
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

		}
		else{
			def deviceMap = [completedSetup: false]

			deviceMap['name'] = app.label + " - Key $i";
			deviceMap['label'] = deviceMap['name'];

			childDevice = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
		}

		childDevice.initChild([keyNumber : i]);
	}
}
