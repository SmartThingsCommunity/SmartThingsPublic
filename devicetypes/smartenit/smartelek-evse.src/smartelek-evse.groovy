/****************************************************************************
 * DRIVER NAME:  Smartenit EVSE
 * DESCRIPTION:	 Device handler for Smartenit SmartElek EVSE
 * 					
 * $Rev:         $: 2
 * $Author:      $: Luis Contreras
 * $Date:	 $: 06/23/2021
 * $HeadURL:	 $:
 
 ****************************************************************************
 * This software is owned by Compacta and/or its supplier and is protected
 * under applicable copyright laws. All rights are reserved. We grant You,
 * and any third parties, a license to use this software solely and
 * exclusively on Compacta products. You, and any third parties must reproduce
 * the copyright and warranty notice and any other legend of ownership on each
 * copy or partial copy of the software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS". COMPACTA MAKES NO WARRANTIES, WHETHER
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE,
 * ACCURACY OR LACK OF NEGLIGENCE. COMPACTA SHALL NOT, UNDERN ANY CIRCUMSTANCES,
 * BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, SPECIAL,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES FOR ANY REASON WHATSOEVER.
 *
 * Copyright Compacta International, Ltd 2016. All rights reserved
 ****************************************************************************/
 // EVSE Cluster Doc: https://docs.smartenit.io/display/SMAR/EVSE+Processor+Details 
 
 import groovy.transform.Field
 
 @Field final EVSECluster = 0xFF00
 @Field final MeteringCurrentSummation = 0x0000
 @Field final MeteringInstantDemand = 0x0400
 @Field final EnergyDivisor = 100000
 @Field final CurrentDivisor = 100
 @Field final ChargingStatus = 0x0000
 @Field final ChargerLevel = 0x0001
 @Field final ChargerAutoStart = 0x0003
 @Field final ChargerFault = 0x0004
 @Field final ChargerMaximumCurrent = 0x0011
 @Field final ChargerSessionDuration = 0x0013
 @Field final ChargerDeliveredSummation = 0x0014
 @Field final ChargerSessionSummation = 0x0015
 @Field final ChargerSessionPeakCurrent = 0x0016
 @Field final ChargerVRMS = 0x0020
 @Field final ChargerIRMS = 0x0021
 @Field final SmartenitMfrCode = 0x1075
 @Field final StartCharging = 0x00
 @Field final StopCharging = 0x02
 @Field final EnableAutoStartMode = 0x04
 @Field final DisableAutoStartMode = 0x05
 
metadata {
	definition (name: "SmartElek EVSE", namespace: "Smartenit", author: "Luis Contreras", mnmn: "SmartThingsCommunity", vid: "18da1704-2bbc-37ec-92a5-35e911024cea", ocfDeviceType: "oic.d.smartplug") {
		capability "monthpublic25501.chargerstate"
		capability "monthpublic25501.chargerlevel"
		capability "monthpublic25501.chargerfault"
		capability "monthpublic25501.chargerirms"
		capability "monthpublic25501.chargersessionpeakcurrent"
		capability "monthpublic25501.chargersessionsummation"
		capability "monthpublic25501.chargerautostart"
		capability "monthpublic25501.chargermaximumcurrent"
		capability "monthpublic25501.chargersessionduration"
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Switch"
		capability "Health Check"
		capability "Voltage Measurement"

		command "stopcharging"
		command "startcharging"

		fingerprint model: "IOTEVSE-Z", manufacturer: "Smartenit, Inc", deviceJoinName: "Smartenit EVSE"
	}
}

def getFPoint(String FPointHex){
	return (Float)Long.parseLong(FPointHex, 16)
}

// Parse incoming device messages to generate events
def parse(String description) {
	def event = zigbee.getEvent(description)
	if (event) {
		log.debug "event: ${event}, ${event.name}, ${event.value}"
		if (event.name == "power") {
			sendEvent(name: "power", value: (event.value/EnergyDivisor))
		} else { 
			sendEvent(event) 
		}
	}
	else {
		def mapDescription = zigbee.parseDescriptionAsMap(description)
		log.debug "mapDescription... : ${mapDescription}"
		if (mapDescription) {
			if (mapDescription.clusterInt == zigbee.SIMPLE_METERING_CLUSTER) {
				if (mapDescription.attrInt == MeteringCurrentSummation) {
					return sendEvent(name:"energy", value: getFPoint(mapDescription.value)/EnergyDivisor)
				} else if (mapDescription.attrInt == MeteringInstantDemand) {
					return sendEvent(name:"power", value: getFPoint(mapDescription.value/EnergyDivisor))
				}
			} else if (mapDescription.clusterInt == EVSECluster) {
				log.debug "EVSE cluster, attrId: ${mapDescription.attrId}, value: ${mapDescription.value}"
				if (mapDescription.attrInt == ChargingStatus) {
					def strvalue = parseChargerStatusValue(mapDescription.value)
					log.debug "charging status attribute: ${mapDescription.value}, ${strvalue}"
					if (strvalue == "unplugged") {
						sendEvent(name:"sessionDuration", value: "--")
						sendEvent(name:"sessionSummation", value: 0)
					}
					if (strvalue == "charging") {
						sendEvent(name:"switch", value:"on")
					} else {
						sendEvent(name:"switch", value:"off")
					}
					return sendEvent(name:"chargerStatus", value: strvalue)
				} else if (mapDescription.attrInt == ChargerLevel) {
					def strvalue = parseChargerLevelValue(mapDescription.value)
					return sendEvent(name:"level", value: strvalue)
				} else if (mapDescription.attrInt == ChargerAutoStart) {
					def val = zigbee.convertHexToInt(mapDescription.value)
					log.debug "autostart value: ${val} "
					return sendEvent(name:"autoStart", value: val)
				} else if (mapDescription.attrInt == ChargerFault) {
					def strvalue = parseChargerFaultValue(mapDescription.value)
					return sendEvent(name:"fault", value: strvalue)
				} else if (mapDescription.attrInt == ChargerMaximumCurrent) {
					log.debug "charger max current: ${mapDescription.value}"
					return sendEvent(name:"maximumCurrent", value: getFPoint(mapDescription.value)/CurrentDivisor, unit: "A")
				} else if (mapDescription.attrInt == ChargerSessionDuration) {
					int time = (int) Long.parseLong(mapDescription.value, 16);
					log.debug "ChargerSessionDuration attribute: ${mapDescription.value}, time: ${time}"
					def hours = Math.round(Math.floor(time / 3600))
					def secs = time % 3600
					def mins = Math.round(Math.floor(secs / 60))
					def timestr = "${hours} hr:${mins} min"
					return sendEvent(name:"sessionDuration", value: timestr)
				} else if (mapDescription.attrInt == ChargerSessionSummation) {
					log.debug "ChargerSessionSummation attribute: ${mapDescription.value}"
					return sendEvent(name:"sessionSummation", value: getFPoint(mapDescription.value)/EnergyDivisor, unit: "kWh")
				} else if (mapDescription.attrInt == ChargerSessionPeakCurrent) {
					log.debug "ChargerSessionPeakCurrent attribute: ${mapDescription.value}"
					return sendEvent(name:"sessionPeakCurrent", value: getFPoint(mapDescription.value) / 100, unit: "A")
				} else if (mapDescription.attrInt == ChargerVRMS) {
					log.debug "ChargerVRMS attribute: ${mapDescription.value}"
					return sendEvent(name:"voltage", value: getFPoint(mapDescription.value) / 100)
				} else if (mapDescription.attrInt == ChargerIRMS) {
					log.debug "ChargerIRMS attribute: ${mapDescription.value}"
					return sendEvent(name:"current", value: getFPoint(mapDescription.value) / 100, unit: "A")
				} else {
					log.debug "attribute not handled"
				}
			}
		}
	}
}

def setAutoStart(val) {
	log.debug "Set auto start to: ${val}"
	sendEvent(name:"autoStart", value: val)

	if (val == "1") {
		log.debug "Sending enable autostart"
		zigbee.command(EVSECluster, EnableAutoStartMode, "", [mfgCode: SmartenitMfrCode])
	} else if (val == "0") {
		log.debug "Sending disable autostart"
		zigbee.command(EVSECluster, DisableAutoStartMode, "", [mfgCode: SmartenitMfrCode])
	}
}

def setMaximumCurrent(val) {
	log.debug "Set max current val: ${val}"

	sendEvent(name:"maximumCurrent", value: val, unit: "A")

	int newMax = (int) (val * 100)
	int convert = ((newMax << 8) & 0xFF00) | ((newMax >> 8) & 0xFF)

	zigbee.writeAttribute(EVSECluster, ChargerMaximumCurrent, 0x21, convert, [mfgCode: SmartenitMfrCode])
}

def parseChargerLevelValue(val) {
	log.debug "parseChargerLevelValue: ${val}"
	switch (val as Integer) {
		case 0:
			log.debug "level is unknown"
			return "Unknown"
		case 1:
			log.debug "Charging @ L1"
			return "Level 1"
		case 2:
			log.debug "Charging @ L2"
			return "Level 2"
		default:
			return ""
	}
}

def parseChargerFaultValue(val) {
	log.debug "parseChargerFaultValue: ${val}"
	switch (val as Integer) {
		case 0:
			log.debug "No fault"
			return "None"
		case 1:
			log.debug "Meter failed"
			return "Meter failure"
		case 2:
			log.debug "Overvoltage"
			return "Overvoltage"
		case 3:
			log.debug "Undervoltage"
			return "Undervoltage"
		case 4:
			log.debug "Overcurrent"
			return "Overcurrent"
		case 5:
			log.debug "Overheating"
			return "Overheating"
		case 16:
			log.debug "Contact Wet"
			return "Contact Wet"
		case 17:
			log.debug "Contact Dry"
			return "Contact Dry"
		case 18:
			log.debug "Ground fault"
			return "Ground Fault"
		case 19:
			log.debug "Pilot Short Circuit"
			return "Short Circuit"
		case 20:
			log.debug "Wrong Supply"
			return "Wrong Supply"
		case 21:
			log.debug "GFCI Failure"
			return "GFCI Failure"
		case 22:
			log.debug "GMI Fault"
			return "GMI Fault"
		default:
			log.debug "Unknown fault"
			return "Unknown fault"
	}
}

def parseChargerStatusValue(val) {
	log.debug "parseChargerStatusValue: ${val}"
	switch (val as Integer) {
		case 0:
			log.debug "value is Unplugged"
			return "unplugged"
			break;
		case 1:
			log.debug "value is Plugged In"
			return "pluggedin"
			break;
		case 2:
			return "pluggedin"
			break;
		case 3:
			log.debug "value is Charging"
			return "charging"
			break;
		case 4:
			log.debug "value is Fault"
			return "fault"
			break;
		case 5:
			log.debug "value is Charging Completed"
			return "chargingcompleted"
			break;
		default:
			return ""
			break;
	}
}

def on() {
	log.debug "received on command"
	zigbee.command(EVSECluster, StartCharging, "", [mfgCode: SmartenitMfrCode])
}

def off() {
	log.debug "received off command"
	zigbee.command(EVSECluster, StopCharging, "", [mfgCode: SmartenitMfrCode])
}

def stopcharging() {
	log.debug "sending stopcharging command.."
	zigbee.command(EVSECluster, StopCharging, "", [mfgCode: SmartenitMfrCode])
}

def startcharging() {
	log.debug "sending startcharging command.."
	zigbee.command(EVSECluster, StartCharging, "", [mfgCode: SmartenitMfrCode])
}

def refresh() {
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, MeteringCurrentSummation) +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, MeteringInstantDemand) +
	zigbee.readAttribute(EVSECluster, ChargingStatus, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerVRMS, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerSessionSummation, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerSessionDuration, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerIRMS, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerLevel, [mfgCode: SmartenitMfrCode]) + 
	zigbee.readAttribute(EVSECluster, ChargerMaximumCurrent, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerFault, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerSessionPeakCurrent, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(EVSECluster, ChargerAutoStart, [mfgCode: SmartenitMfrCode])
}

def configure() {
	log.debug "in configure()"
	configureHealthCheck()
	return (zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, MeteringCurrentSummation, 0x25, 0, 600, 50) +
		zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, MeteringInstantDemand, 0x2a, 0, 600, 50) +
		zigbee.configureReporting(EVSECluster, ChargingStatus, 0x30, 0x0, 0x0, null, [mfgCode: SmartenitMfrCode]) + 
		refresh()
	)
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 10
	sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	log.debug "in updated()"
	// updated() doesn't have it's return value processed as hub commands, so we have to send them explicitly
	def cmds = configureHealthCheck()
	cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def ping() {
	return refresh()
}