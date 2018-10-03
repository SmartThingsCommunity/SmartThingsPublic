import groovy.json.JsonOutput
metadata {
	definition (name: "Actions", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Refresh"
        
        attribute "switch", "string"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("status", "device.status", width: 3, height: 1, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "https://b1hub.github.io/images/smartthings/offn@2x.png", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "https://b1hub.github.io/images/smartthings/on@2x.png", backgroundColor: "#00A0DC", nextState: "off"
		}
        
  
        standardTile("refresh", "device.status", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		main "status"
		details (["status","refresh"])
	}
}

def parse(String description) {
}

def on() {
def cmdtype=device.currentValue("cmdType")
def statusv="true"
def deviceId =  device.currentValue("deviceBID")
def deviceMId =  device.currentValue("deviceID")
if(!(parent.setSwitchOnOff( statusv, deviceId,deviceMId)))
{
status=device.currentValue("status")
}
else
{
refresh()
}

generatePowerModeEvent("Enabled")
generateStatusEvent()

if(cmdtype!="02")
{
sendEvent(name:"status", value:"on", displayed: false)
}


}

def off() {
def cmdtype=device.currentValue("cmdType")
def status="false"
if(cmdtype=="02")
{
status="true"
}
def deviceId =  device.currentValue("deviceBID")
def deviceMId =  device.currentValue("deviceID")
if(!(parent.setSwitchOnOff( status, deviceId,deviceMId)))
{
status=device.currentValue("status")
}
else
{
refresh()
}
generatePowerModeEvent("Disabled")
generateStatusEvent()
if(cmdtype!="02")
{
sendEvent(name:"status", value:"off", displayed: false)
}

}

void installed() {
	initialize()
}
def initialize() {
	
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
	updateDataValue("EnrolledUTDH", "true")
}

def updated() {
	log.debug "updated()"
	//parent.setName(device.label, device.deviceNetworkId)
	initialize()
}

// Called when the DTH is uninstalled, is this true for cirrus/gadfly integrations?
// Informs parent to purge its associated data
def uninstalled() {
    log.debug "uninstalled() parent.purgeChildDevice($device.deviceNetworkId)"
    // purge DTH from parent
    //parent?.purgeChildDevice(this)
}

def ping() {
	
	log.debug "ping() NOP"
}

def refresh() {
    def deviceId=device.currentValue("deviceID")
	parent.poll1(deviceId,"1")
}

def generatePowerModeEvent(mode) {
	sendEvent(name: "status", value: mode,
			isStateChange: true, descriptionText: "$device.displayName is ${mode}",displayed:false)
}



def generateEvent(Map results) {
log.debug("data action for result:${results}")
	if(results) {
		def linkText = getLinkText(device)
		results.each { name, value ->
			def event = [name: name, linkText: linkText, handlerName: name]
			def sendValue = value
            if (name=="status") {
				sendValue =  value  // API return temperature values in F
				event << [value: sendValue]
			}else {
				event << [value: value.toString(),displayed:false]
			}

				event << [descriptionText: getActionDescriptionText(name, sendValue, linkText)]
				sendEvent(event)
		}
        generateStatusEvent()
		
	}
}

private getActionDescriptionText(name, value, linkText) {
	if(name == "status") {
    if(value=="on")
    {
    return "$device.displayName is Enabled "
    }
    else
    {
    return "$device.displayName is Disabled "
    }
		
	} 
}


def generateStatusEvent() {
	def mode = device.currentValue("status")
	def statusText = "Right Now: Idle"
	def operatingState = "idle"

	if (mode == "on") {
		
			statusText = "rule is enabled"
			operatingState = "on"
		
	}else if (mode == "off") {
		
			statusText = "rule is disable"
			operatingState = "off"
		
	}  else {
		statusText = "?"
	}
	//sendEvent("name":"status", "value":statusText, "description":statusText, displayed: true)
	sendEvent(name:"status", value:operatingState, displayed: false)
}