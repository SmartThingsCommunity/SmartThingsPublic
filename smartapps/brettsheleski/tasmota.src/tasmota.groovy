
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
	
    section("Tasmota Devices") {
        input "tasmotaDevices", "device.tasmota", title: "Devices", required: true, multiple: true
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
    
}

def postStatus(){
    
    def status = request?.JSON;

    def splitTopic = "${status?.topic}".split("/");

    def mqttPrefix = splitTopic[0];
    def mqttTopic = splitTopic[1];
    def mqttSuffix = splitTopic[2];

    def targetDevice =  tasmotaDevices.find { it.currentValue("topic") == mqttTopic };

    if (targetDevice){
        log.debug "UPDATING DEVICE: ${targetDevice}"

        targetDevice.updateStatus(status.payload);
    }
    else{
        log.debug "NO DEVICE CONFIGURED FOR TOPIC: ${mqttTopic}";
    }
}

def initialize(){
    runEvery1Minute(updateAllDevices);
}

def updateAllDevices(){
    log.debug "Updating all devices"
    
    tasmotaDevices.each{
        it.refresh();
    }
}