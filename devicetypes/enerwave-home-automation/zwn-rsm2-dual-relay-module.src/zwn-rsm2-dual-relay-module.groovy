metadata {
// Automatically generated. Make future change here.
definition (name: "ZWN-RSM2 Dual Relay Module", namespace: "Enerwave Home Automation", author: "Enerwave Home Automation") {
capability "Switch"
capability "Refresh"
capability "Configuration"
capability "Actuator"
command "reset"
(1..2).each { n ->
attribute "switch$n", "enum", ["on", "off"]
command "on$n"
command "off$n"
}
fingerprint deviceId: "0x1001", inClusters:
"0x25,0x27,0x70,0x72,0x86,0x60"
}
// simulator metadata
simulator {
status "on": "command: 2003, payload: FF"
status "off": "command: 2003, payload: 00"
status "switch1 on": "command: 600D, payload: 01 00 25 03 FF"
status "switch1 off": "command: 600D, payload: 01 00 25 03 00"
status "switch2 on": "command: 600D, payload: 02 00 25 03 FF"
status "switch2 off": "command: 600D, payload: 02 00 25 03 00"
// reply messages
reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
reply "200100,delay 100,2502": "command: 2503, payload: 00"
}
// tile definitions
tiles {
(1..2).each { n ->
standardTile("switch$n", "switch$n", canChangeIcon: true) {
state "on", label: '${name}', action: "off$n", icon:
"st.switches.switch.on", backgroundColor: "#79b821"
state "off", label: '${name}', action: "on$n", icon:
"st.switches.switch.off", backgroundColor: "#ffffff"
}
}
standardTile("refresh", "device.switch", inactiveLabel: false,
decoration: "flat") {
state "default", label:"", action:"refresh.refresh",
icon:"st.secondary.refresh"
}
main(["switch1", "switch2"])
details(["switch1",
"switch2","refresh"])
}
}
def parse(String description) {
def result = null
if (description.startsWith("Err")) {
result = createEvent(descriptionText:description, isStateChange:true)
} else if (description != "updated") {
def cmd = zwave.parse(description, [0x60: 3, 0x25: 1, 0x20: 1])
if (cmd) {
result = zwaveEvent(cmd, null)
}
}
log.debug "parsed '${description}' to ${result.inspect()}"
result
}
def endpointEvent(endpoint, map) {
if (endpoint) {
map.name = map.name + endpoint.toString()
}
createEvent(map)
}
def
zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap
cmd, ep) {
def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1,
0x20: 1])
if (encapsulatedCommand) {
zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {
def map = [name: "switch", type: "physical", value: (cmd.value ? "on" : "off")]
def events = [endpointEvent(endpoint, map)]
events
}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
def map = [name: "switch", value: (cmd.value ? "on" : "off")]
def events = [endpointEvent(endpoint, map)]
events
}
def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
log.debug "${device.displayName}: Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}
def onOffCmd(value, endpoint = null) {
[
encap(zwave.basicV1.basicSet(value: value), endpoint),
"delay 500",
encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
]
}
def on() { onOffCmd(0xFF) }
def off() { onOffCmd(0x0) }
def on1() { onOffCmd(0xFF, 1) }
def on2() { onOffCmd(0xFF, 2) }
//def on3() { onOffCmd(0xFF, 3) }
//def on4() { onOffCmd(0xFF, 4) }
def off1() { onOffCmd(0, 1) }
def off2() { onOffCmd(0, 2) }
//def off3() { onOffCmd(0, 3) }
//def off4() { onOffCmd(0, 4) }
def refresh() {
delayBetween([
encap(zwave.basicV1.basicGet(), 1), // further gets are sent from the basic report handler
encap(zwave.basicV1.basicGet(), 2) // further gets are sent from the basic report handler
],200)
}
def resetCmd(endpoint = null) {
delayBetween([
encap(zwave.meterV2.meterReset(), endpoint),
encap(zwave.meterV2.meterGet(scale: 0), endpoint)
])
}
def reset() {
delayBetween([resetCmd(null), reset1(), reset2()])
}
def reset1() { resetCmd(1) }
def reset2() { resetCmd(2) }
//def reset3() { resetCmd(3) }
//def reset4() { resetCmd(4) }
def configure() {
}
private encap(cmd, endpoint) {
if (endpoint) {
zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).
encapsulate(cmd).format()
} else {
cmd.format()
}
}