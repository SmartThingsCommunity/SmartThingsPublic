/*
 * V 1.2 of zooZ Indoor Siren ZSE01
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 ---------------------------------------------------------------------------------------------------------------------------
   INSTRUCTIONS:
   * First you will need to create a new device handler in SmartThings. Log in to the IDE and click on Device Handlers in the top menu.
   Then click "from code" below. You'l be presented with a blank area where you can copy this code and paste it in.
   * Next, scroll to the bottom and click create. Now click save, then click publish > for me.
   * You should now see this device type in your list of device handlers. Now we need to pair the device.
   * Put SmartThings into inclusion mode by going through the add new device dialog.
   * Tap the button on the siren a few times (turning siren on and off) and you should see it appear on the app screen.
   * tap the discovered device on the app screen to continue.
   * Now go to your devices list in the SmartThings app and open up the newly added siren.
   * The configuration will get sent by pressing the button to activate the siren, then tapping again to cancel.
   * That's it. Remember this is a USB powered device and the batteries are for BACKUP ONLY. There is no battery level indicator by design.

*/

 metadata {
	definition (name: "zooZ Indoor Siren ZSE01 - RV v1.2", namespace: "robertvandervoort", author: "Robert Vandervoort") {
		capability "Actuator"
		capability "Alarm"
        capability "Battery"
		capability "Switch"
        capability "Sensor"
		capability "Polling"
		capability "Refresh"

		command "reset"
		// RAW DESCRIPTION: 0 0 0x1005 0 0 0 9 0x5E 0x86 0x72 0x73 0x85 0x59 0x25 0x20 0x27
		fingerprint deviceId: "0x1005", inClusters: "0x5E 0x86 0x72 0x73 0x85 0x59 0x25 0x20 0x27", manufacturer: "zooZ", model: "ZSE01"
        
	}
	// simulator metadata

	// tile definitions
	tiles (scale: 2) {
    	multiAttributeTile(name:"alarm", type:"generic", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
            	attributeState "off", label:'OFF', action:'on', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "on", label:'ALARM!', action:'off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
            }
		}
		standardTile("off", "device.alarm", inactiveLabel: false, width: 6, height: 4) {
			state "default", label:'', action:"off", icon:"st.secondary.off"
		}
		main "alarm"
		details(["alarm","off"])
	}
    preferences {
		input "debugOutput", "boolean", 
			title: "Enable debug logging?",
			defaultValue: false,
			displayDuringSetup: false,
            required: false
	}
}

def updated()
{
	updateDataValue("configured", "false")
	state.debug = ("true" == debugOutput)
	def result = createEvent(descriptionText: "${device.displayName} was updated", displayed: true)
    configure()
}

def parse(String description)
{
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x25: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (state.debug) log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	if (state.debug) log.debug "---MANUFACTURER SPECIFIC REPORT V2--- ${device.displayName} sent deviceIdDataFormat: ${cmd.deviceIdDataFormat}, deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}, deviceIdType: ${cmd.deviceIdType}, payload: ${cmd.payload}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	if (state.debug) log.debug "---COMMAND CLASS VERSION REPORT V1--- ${device.displayName} has command class version: ${cmd.commandClassVersion} - payload: ${cmd.payload}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.debug) log.debug "---VERSION REPORT V1--- ${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    if (state.debug) log.debug "---DEVICE RESET LOCALLY V1--- ${device.displayName} ${cmd}"
    createEvent(descriptionText: cmd.toString(), isStateChange: true, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelTestNodeReport cmd) {
	if (state.debug) log.debug "===Power level test node report received=== ${device.displayName}: statusOfOperation: ${cmd.statusOfOperation} testFrameCount: ${cmd.testFrameCount} testNodeid: ${cmd.testNodeid}"
	def request = [
        physicalgraph.zwave.commands.powerlevelv1.PowerlevelGet()
    ]
    response(commands(request))
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    cmd.nodeId.each({log.debug "---ASSOCIATION REPORT--- '${cmd}', hub: '$zwaveHubNodeId' reports nodeId: '$it' is associated in group: '${cmd.groupingIdentifier}'"})
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (state.debug) log.debug "---BASIC REPORT V1--- ${device.displayName} sent value: ${cmd.value}"
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true]),
		createEvent([name: "alarm", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true])
	]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	if (state.debug) log.debug "---BASIC SET V1--- ${device.displayName} sent value: ${cmd.value}"
	[name: "alarm", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	if (state.debug) log.debug "---SWITCH BINARY REPORT V1--- ${device.displayName} sent value: ${cmd.value}"
	[name: "alarm", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true]
}
	
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd) {
	if (state.debug) log.debug "---SWITCH BINARY SET V1--- ${device.displayName} sent value: ${cmd.value}"
	[name: "alarm", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	if (state.debug) log.debug "Unhandled: $cmd sent to ${device.displayName}"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def siren() {
	if (state.debug) log.debug "Siren command received"
	on()
}

def both() {
	if (state.debug) log.debug "Siren and Strobe commands received"
	on()
}

def on() {
	if (state.debug) log.debug "Sounding Alarm : ${device.displayName}"
	def request = [
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	]
    commands(request)
}

def off() {
	if (state.debug) log.debug "Cancelling Alarm : ${device.displayName}"
	def request = [
		zwave.basicV1.basicSet(value: 0x00),
        zwave.basicV1.basicGet()
	]
    commands(request)
}

def configure() {
	if (state.debug) log.debug "--Sending configuration commands to zooZ indoor siren--"
    if (state.debug) log.debug "settings: ${settings.inspect()}, state: ${state.inspect()}"
    setConfigured()
	def request = [
		//Get association
		zwave.associationV1.associationGet(groupingIdentifier:1),
        zwave.associationV1.associationGet(groupingIdentifier:2),

		// Get Version information
        zwave.versionV1.versionGet(),
        zwave.firmwareUpdateMdV2.firmwareMdGet(),
    ]
	commands(request)
}

private setConfigured() {
	updateDataValue("configured", "true")
    return []
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	cmd.format()
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}