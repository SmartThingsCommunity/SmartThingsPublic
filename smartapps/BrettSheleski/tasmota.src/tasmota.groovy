
definition(
    name: "Tasmota",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "SmartApp for the Sonoff-Tasmota firmware.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

preferences {
	page("mainPage", "Main Configuration"){
        section ("SmartApp Name"){
            label title: "Assign a name", required: true
        }

		section("Sonoff Host") {
			input "ipAddress", "string", title: "IP Address", required: true
		}

		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: true, required: false)
			input(name: "password", type: "password", title: "Password", description: "Password", displayDuringSetup: true, required: false)
		}

	}
    
}

mappings {
  path("/status") {
    action: [
      POST: "postStatus"
    ]
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

def postStatus(){
    
    def status = request?.json;

    if (status){
        getMainDevice().updateStatus(status);
    }
}

def initialize(){
    def mainDevice = getMainDevice();

    mainDevice.initializeChild([ipAddress : ipAddress, username: username, password: password]);
}


def getMainDevice(){
    def children = getChildDevices()
	def namespace = app.namespace
	def deviceName = "Tasmota"
	def theHubId = location.hubs[0].id

    def deviceId = "${app.id}-master"
    def childDevice = children.find {
        it.deviceNetworkId == deviceId
    }
    
    if (childDevice){
        log.debug "FOUND child device found for ${childDevice.deviceNetworkId}"
    }
    else{
        def deviceMap = [completedSetup: true]

        deviceMap['name'] = app.label + " (Master)";
        deviceMap['label'] = deviceMap['name'];
        deviceMap['isComponent'] = true; // prevent device from showing in device list
        deviceMap['componentName'] = 'MasterDevice'
        deviceMap['componentLabel'] = 'Master Device'

        childDevice = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
    }

    return childDevice;
}