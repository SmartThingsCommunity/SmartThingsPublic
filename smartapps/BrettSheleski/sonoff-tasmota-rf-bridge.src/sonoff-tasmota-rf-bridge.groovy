
definition(
    name: "Sonoff-Tasmota RF Bridge SmartApp",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "SmartApp for the Sonoff RF Bridge running Tasmota firmware.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
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

def initialize(){

	def children = getChildDevices()
	def namespace = "BrettSheleski"
	def deviceName = "Sonoff-Tasmota RF Bridge Button"
	def theHub = null;


	for ( i in 1..16 )
	{
		def deviceId = "${app.id}-key${i}"
		def childDevice = children.find {
				it.deviceNetworkId == deviceId
			}
		def deviceMap = [keyNumber : i]
		
		if (childDevice){
			log.debug "FOUND child device found for ${childDevice.deviceNetworkId}"

		}
		else{
			childDevice = addChildDevice(namespace, deviceName, deviceId, theHub, deviceMap)
		}
	}
}
