
definition(
    name: "Tasmota",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "SmartApp for the Sonoff-Tasmota firmware.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png",
    singleInstance : false
){
    appSetting "username"
    appSetting "password"
}

preferences {
	page("mainPage", "Main Configuration"){
        section ("SmartApp Name"){
            label title: "Assign a name", required: true
        }

		section("Sonoff Host") {
			input "ipAddress", "string", title: "IP Address", required: true
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

    mainDevice.initializeChild();
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

        log.debug "CREATING child device for ${settings.label} (Master)"

        deviceMap['name'] = settings.label + " (Master)";
        deviceMap['label'] = deviceMap['name'];
        //deviceMap['isComponent'] = true; // prevent device from showing in device list
        //deviceMap['componentName'] = 'MasterDevice'
        //deviceMap['componentLabel'] = 'Master Device'

        childDevice = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
    }

    return childDevice;
}

def getHostInfo(){
    return [ipAddress : settings.ipAddress, username : appSettings.username, password : appSettings.password]
}

def getAppLabel(){
    def myLabel = settings.label;

    if (myLabel == null){
        myLabel = app.label;
    }

    return myLabel;
}