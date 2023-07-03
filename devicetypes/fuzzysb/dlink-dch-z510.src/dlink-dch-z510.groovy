/**
 *  Copyright 2015 Stuart Buchanan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Dlink DCH-Z510
 *
 *	Author: fuzzysb
 *	Date: 2015-01-05
 */
 
 
 
preferences {
 input "defaultSound", "enum", title: "Default Sound to use for the Siren?", options: ["Emergency","FireAlarm","Ambulance","PoliceCar","DoorChime"], required: false, defaultValue: "Emergency"
}
 
metadata {
 definition (name: "Dlink DCH-Z510", namespace: "fuzzysb", author: "Stuart Buchanan") {
	capability "Actuator"
	capability "Alarm"
	capability "Switch"
	capability "Configuration"

	command "Emergency"
	command "FireAlarm"
	command "Ambulance"
	command "PoliceCar"
	command "DoorChime"
    
	fingerprint deviceId: "0x1005", inClusters: "0x5E,0x71,0x20,0x25,0x85,0x70,0x72,0x86,0x30,0x59,0x73,0x5A,0x98,0x7A"
 }

simulator {

}

 tiles(scale: 2) {
	multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
		tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
			attributeState "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
			attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}
	}
	standardTile("Emergency", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'Emergency', action:"Emergency", icon:"st.Weather.weather1"
	}
	standardTile("FireAlarm", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'Fire Alarm', action:"FireAlarm", icon:"st.Outdoor.outdoor10"
	}
	standardTile("Ambulance", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'Ambulance', action:"Ambulance", icon:"st.Transportation.transportation2"
	}
	standardTile("PoliceCar", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'Police Car', action:"PoliceCar", icon:"st.Transportation.transportation8"
	}
	standardTile("DoorChime", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'Door Chime', action:"DoorChime", icon:"st.Home.home30"
	}
	standardTile("off", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'', action:"alarm.off", icon:"st.secondary.off"
	}
	main "alarm"
	details(["alarm", "Emergency", "FireAlarm", "Ambulance", "PoliceCar", "DoorChime", "off"])
 }
}

def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x26: 1, 0x70: 1, 0x80: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
            log.debug "Parse returned ${result?.inspect()}"
            		} else {
			log.debug("Couldn't zwave.parse '$description'")
			null
		}
		
	}
	log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}



def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 3, 0x26: 3, 0x70: 1, 0x80: 1])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "rx $cmd"
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off", displayed: false]),
		createEvent([name: "alarm", value: cmd.value ? "both" : "off"])
	]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	createEvent(name:"Alarm", cmd.sensorValue ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
if (cmd.notificationType == 6 && cmd.event == 22) {
		log.debug "Playing Door Chime"
	} else if (cmd.notificationType == 10 && cmd.event == 1) {
		log.debug "Playing Police Car"
	} else if (cmd.notificationType == 10 && cmd.event == 3) {
    	log.debug "Playing Ambulance"
    } else if (cmd.notificationType == 10 && cmd.event == 2) {
    	log.debug "Playing Fire Alarm"
    } else if (cmd.notificationType == 7 && cmd.event == 1) {
    	log.debug "Playing Emergency"
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}

def on() {
	log.debug("Sounding Siren")
	switch ( settings.defaultSound ) {
		case "Emergency":
		Emergency()
		break
		
		case "FireAlarm":
		FireAlarm()
		break
		
		case "Ambulance":
		Ambulance()
		break
		
		case "PoliceCar":
		PoliceCar()
		break
		
		case "DoorChime":
		DoorChime()
		break
		
		default:
		Emergency()
		break
	}
}

def off() {
	log.debug "sending off"
	[
		secure(zwave.basicV1.basicSet(value: 0x00)),
		secure(zwave.basicV1.basicGet())
	]
}

def Emergency() {
	log.debug "Sounding Siren With Emergency"
	[
		secure(zwave.notificationV3.notificationReport(event: 0x01, notificationType: 0x07)),
		secure(zwave.basicV1.basicGet())
	]
}

def FireAlarm() {
	log.debug "Sounding Siren With Fire Alarm"
	[
		secure(zwave.notificationV3.notificationReport(event: 0x02, notificationType: 0x0A)),
        secure(zwave.basicV1.basicGet())
	]
}

def Ambulance() {
	log.debug "Sounding Siren With Ambulance"
	[
		secure(zwave.notificationV3.notificationReport(event: 0x03, notificationType: 0x0A)),
        secure(zwave.basicV1.basicGet())
	]
}

def PoliceCar() {
	log.debug "Sounding Siren With Police Car"
	[
		secure(zwave.notificationV3.notificationReport(event: 0x01, notificationType: 0x0A)),
        secure(zwave.basicV1.basicGet())
	]
}

def DoorChime() {
	log.debug "Sounding Siren With Door Chime"
	[
		secure(zwave.notificationV3.notificationReport(event: 0x16, notificationType: 0x06)),
        secure(zwave.basicV1.basicGet())
	]
}

def configure() {
	log.debug "Resetting Siren Parameters to SmartThings Compatible Defaults"
	def cmds = []
    
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 29, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [6], parameterNumber: 31, size: 1)
    
    delayBetween(cmds, 500)
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
         log.debug "Sending Secure Command $cmd"
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
    	log.debug "Sending Insecure Command $cmd"
		cmd.format()
	}
}