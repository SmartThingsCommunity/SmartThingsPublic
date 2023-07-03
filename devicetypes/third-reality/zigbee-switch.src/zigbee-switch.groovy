metadata {
definition (name: "ZigBee Switch", namespace: "Third Reality", author: "Third Reality", ocfDeviceType: "oic.d.switch") {
capability "Switch"
fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS007Z", deviceJoinName: "RealitySwitch"
}
// simulator metadata
simulator {
// status messages
status "on": "on/off: 1"
status "off": "on/off: 0"
// reply messages
reply "zcl on-off on": "on/off: 1"
reply "zcl on-off off": "on/off: 0"
}
tiles(scale: 2) {
multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
}
}
main "switch"
details(["switch"])
}
}
// Parse incoming device messages to generate events
def parse(String description) {
log.debug "description is $description"
def event = zigbee.getEvent(description)
if (event) {
sendEvent(event)
}
else {
log.warn "DID NOT PARSE MESSAGE for description : $description"
log.debug zigbee.parseDescriptionAsMap(description)
}
}
def off() {
zigbee.off()
}
def on() {
zigbee.on()
}
/**
* PING is used by Device-Watch in attempt to reach the Device
* */
def ping() {
return refresh()
}
def refresh() {
zigbee.onOffRefresh() + zigbee.onOffConfig()
}
def configure() {
// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
log.debug "Configuring Reporting and Bindings."
zigbee.onOffRefresh() + zigbee.onOffConfig()
}