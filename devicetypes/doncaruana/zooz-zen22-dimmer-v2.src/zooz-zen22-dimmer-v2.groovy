/**
 *  Zooz Zen22 Dimmer Switch v2
 *
 * Revision History:
 * 2017-08-29 - Initial release
 * 2017-09-06 - Change color scheme to match new ST standard
 * 2017-09-08 - Remove extra basic report that firmware returns causing duplicate events to display
 *
 *
 *  Supported Command Classes
 *         Association v2
 *         Association Group Information
 *         Basic
 *         Configuration
 *         Device Reset Local
 *         Manufacturer Specific v2
 *         Powerlevel
 *         Switch_all
 *         Switch_multilevel
 *         Version v2
 *         ZWavePlus Info v2
 *  
 *   Parm Size Description                                   Value
 *      1    1 Invert Switch                                 0 (Default)-Upper paddle turns light on, 1-Lower paddle turns light on
 *      2    1 LED Indicator                                 0 (Default)-LED is on when light is OFF, 1-LED is on when light is ON
 *      3    1 LED Disable                                   0 (Default)-LED is on based on parameter 2, 1-LED is off always
 */
metadata {
	definition (name: "Zooz Zen22 Dimmer v2", namespace: "doncaruana", author: "Don Caruana") {
		capability "Switch Level"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

//zw:L type:1101 mfr:027A prod:B112 model:1F1C ver:20.15 zwv:4.05 lib:06 cc:5E,85,59,70,5A,72,73,27,26,86,20 role:05 ff:9C02 ui:9C00

	fingerprint mfr:"027A", prod:"B112", model:"1F1C", deviceJoinName: "Zooz Zen22 Dimmer v2"
	fingerprint deviceId:"0x1101", inClusters: "0x5E,0x59,0x85,0x70,0x5A,0x72,0x73,0x27,0x26,0x86,0x20"
    fingerprint cc: "0x5E,0x59,0x85,0x70,0x5A,0x72,0x73,0x27,0x26,0x86,0x20", mfr:"027A", prod:"B112", model:"1F1C", deviceJoinName: "Zooz Zen22 Dimmer v2"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	preferences {
		input "ledIndicator", "bool", title: "LED on when light on", description: "LED will be on when light OFF if not set", required: false, defaultValue: false
		input "invertSwitch", "bool", title: "Invert Switch", description: "Flip switch upside down", required: false, defaultValue: false
		input "ledDisable", "bool", title: "LED Diabled", description: "Turn off LED completely", required: false, defaultValue: false
  }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])

	}
}

private getCommandClassVersions() {
	[
		0x59: 1,  // AssociationGrpInfo
		0x85: 2,  // Association
		0x5A: 1,  // DeviceResetLocally
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x86: 1,  // Version
		0x5E: 2,  // ZwaveplusInfo
		0x27: 1,  // All Switch
		0x26: 1,  // Multilevel Switch
		0x70: 1,  // Configuration
		0x20: 1,  // Basic
	]
}

def installed() {
	def cmds = []

	cmds << mfrGet()
	cmds << zwave.versionV1.versionGet().format()
	cmds << parmGet(1)
	cmds << parmGet(2)
	cmds << parmGet(3)
  return response(delayBetween(cmds,200))
}

def updated(){
		def commands = []
   	if (getDataValue("MSR") == null) {
   		def level = 99
		commands << mfrGet()
		commands << zwave.versionV1.versionGet().format()
	    commands << zwave.basicV1.basicSet(value: level).format()
	    commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
   	}
      //parmset takes the parameter number, it's size, and the value - in that order
    	commands << parmSet(3, 1, [ledDisable == true ? 1 : 0])
    	commands << parmSet(2, 1, [ledIndicator == true ? 1 : 0])
    	commands << parmSet(1, 1, [invertSwitch == true ? 1 : 0])
    	commands << parmGet(1)
    	commands << parmGet(2)
    	commands << parmGet(3)
		// Device-Watch simply pings if no device events received for 32min(checkInterval)
		sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    	return response(delayBetween(commands, 500))
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

//Removed because basic report gets automatically returned with every action as well as multilevel. Only multilevel is necessary.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
//	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def name = ""
    def value = ""
    def reportValue = cmd.configurationValue[0]
    log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
    switch (cmd.parameterNumber) {
        case 1:
            name = "topoff"
            value = reportValue == 1 ? "true" : "false"
            break
        case 2:
            name = "ledfollow"
            value = reportValue == 1 ? "true" : "false"
            break
        case 3:
            name = "ledoff"
            value = reportValue == 1 ? "true" : "false"
            break
        default:
            break
    }
	createEvent([name: name, value: value])
}


def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productTypeCode = String.format("%04X", cmd.productTypeId)
	def productCode = String.format("%04X", cmd.productId)
	def msr = manufacturerCode + "-" + productTypeCode + "-" + productCode
	updateDataValue("MSR", msr)
	updateDataValue("Manufacturer", "Zooz")
	updateDataValue("Manufacturer ID", manufacturerCode)
	updateDataValue("Product Type", productTypeCode)
	updateDataValue("Product Code", productCode)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
   	if (getDataValue("MSR") == null) {
		  commands << mfrGet()
      commands << zwave.versionV1.versionGet().format()
   	}
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	delayBetween(commands,100)
}

def parmSet(parmnum, parmsize, parmval) {
	return zwave.configurationV1.configurationSet(configurationValue: parmval, parameterNumber: parmnum, size: parmsize).format()
}

def parmGet(parmnum) {
	return zwave.configurationV1.configurationGet(parameterNumber: parmnum).format()
}

def mfrGet() {
	return zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
	updateDataValue("applicationVersion", "${cmd.applicationVersion}")
	updateDataValue("applicationSubVersion", "${cmd.applicationSubVersion}")
	updateDataValue("zWaveLibraryType", "${cmd.zWaveLibraryType}")
	updateDataValue("zWaveProtocolVersion", "${cmd.zWaveProtocolVersion}")
	updateDataValue("zWaveProtocolSubVersion", "${cmd.zWaveProtocolSubVersion}")
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	log.debug "vccr"
	def rcc = ""
	log.debug "version: ${cmd.commandClassVersion}"
	log.debug "class: ${cmd.requestedCommandClass}"
	rcc = Integer.toHexString(cmd.requestedCommandClass.toInteger()).toString() 
	log.debug "${rcc}"
	if (cmd.commandClassVersion > 0) {log.debug "0x${rcc}_V${cmd.commandClassVersion}"}
}