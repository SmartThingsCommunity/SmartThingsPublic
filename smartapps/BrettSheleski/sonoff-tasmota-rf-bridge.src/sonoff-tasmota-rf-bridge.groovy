
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

def scheduledTimeHandler() {
    
}

def initialize(){

	def children = getChildDevices()

	for ( i in 0..15 )
	{
		def childDevice = children.find {
				it.deviceNetworkId == "$device.deviceNetworkId-key$i"
			}
		
		if (childDevice){
			log.debug "FOUND child device found for ${it.deviceNetworkId}"
		}
		else{
			log.debug "no child device found for $i"
		}
	}
}
