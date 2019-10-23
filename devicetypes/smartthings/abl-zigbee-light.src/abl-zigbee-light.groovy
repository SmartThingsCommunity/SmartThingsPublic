
metadata {
   definition (name: "ABL ZigBee Light", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, genericHandler: "Zigbee", mnmn : "Samsung Electronics", vid : "ABL-LIGHT-Z-001", ocfDeviceType : "oic.d.light") {
       capability "Actuator"
       capability "Color Temperature"
       capability "Configuration"
       capability "Refresh"
       capability "Switch"
       capability "Switch Level"
       capability "Health Check"
       
       fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters : "0019", manufacturer: "Samsung Electronics", model: "ABL-LIGHT-Z-001", deviceJoinName: "Ultra Thin Wafer"
   }
}

// Globals
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

// Parse incoming device messages to generate events
def parse(String description) {
   log.debug "parse: description is $description"
   def event = zigbee.getEvent(description)
   if (event) {
       if (event.name == "colorTemperature") {
           event.unit = "K"
       }
       
       log.debug "parse: Sending event $event"
       sendEvent(event)
   }
   else {
       log.warn "parse: DID NOT PARSE MESSAGE for description : $description"
       log.debug zigbee.parseDescriptionAsMap(description)
   }
}

def off() {
   zigbee.off()
}

def on() {
   zigbee.on()
}

def setLevel(value) {
   zigbee.setLevel(value)
}

def refresh() {
   def cmds =  zigbee.levelRefresh() + zigbee.colorTemperatureRefresh() + zigbee.onOffRefresh()
   cmds
}

def ping() {
   return zigbee.levelRefresh()
}

def configure() {
   log.debug "configure()"
   sendEvent(name: "checkInterval", value: 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
   zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
}

def updated() {
   log.debug "updated()"
}

def setColorTemperature(value) {
	log.debug "color control"
    value = value as Integer
   	def tempInMired = Math.round(1000000 / value)
   	def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))
   	zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex", "0000") +	
    ["delay 700"] +
   	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

def installed() {
   configure()
}