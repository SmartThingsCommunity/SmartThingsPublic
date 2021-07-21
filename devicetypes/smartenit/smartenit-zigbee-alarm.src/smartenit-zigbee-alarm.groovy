/**
 *  ZBALRM
 *
 *  Copyright 2021 Luis Contreras
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
 import physicalgraph.zigbee.zcl.DataType
 import groovy.transform.Field

 @Field final DEFAULT_MAX_DURATION = 0x00B4
 @Field final DEFAULT_DURATION = 0xFFFE
 @Field final IAS_WD_CLUSTER = 0x0502
 @Field final ATTRIBUTE_IAS_WD_MAXDURATION = 0x0000
 @Field final ATTRIBUTE_IAS_ZONE_STATUS = 0x0002
 @Field final COMMAND_IAS_WD_START_WARNING = 0x00
 @Field final COMMAND_DEFAULT_RESPONSE = 0x0B
 @Field final MODE_SIREN = "03"
 @Field final MODE_STROBE = "DF"
 @Field final MODE_BOTH = "1A"
 @Field final MODE_OFF = "00"
 @Field final STROBE_DUTY_CYCLE = "40"
 @Field final STROBE_LEVEL = "03"
 @Field final BASIC_DUTY_CYCLE = "00"
 @Field final BASIC_LEVEL = "00"
 @Field final FRIENT_MODE_SIREN = "C1"
 @Field final ALARM_OFF = 0x00
 @Field final ALARM_SIREN = 0x01
 @Field final ALARM_STROBE = 0x02
 @Field final ALARM_BOTH = 0x03

metadata {
	definition (name: "Smartenit Zigbee Alarm", namespace: "Smartenit", author: "Luis Contreras") {
		capability "Actuator"
		capability "Alarm"
		capability "Configuration"
		capability "Health Check"

		attribute "testAttribute", "string"

		fingerprint model: "ZBALRM", manufacturer: "Compacta", deviceJoinName: "Smartenit Alarm"
	}
}

def installed() {
	log.debug "Installed"
	sendEvent(name: "alarm", value: "off")
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing description for Alarm! '${description}'"

	Map map = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

	if (!map) {
		if (description?.startsWith('enroll request')) {
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			return cmds
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == IAS_WD_CLUSTER) {
				def data = descMap.data

				Integer parsedAttribute = descMap.attrInt
				Integer command = Integer.parseInt(descMap.command, 16)
				if (parsedAttribute == ATTRIBUTE_IAS_WD_MAXDURATION && descMap?.value) {
					state.maxDuration = Integer.parseInt(descMap.value, 16)
				} else if (command == COMMAND_DEFAULT_RESPONSE) {
					Boolean isSuccess = Integer.parseInt(data[-1], 16) == 0
					Integer receivedCommand = Integer.parseInt(data[-2], 16)
					if (receivedCommand == COMMAND_IAS_WD_START_WARNING && isSuccess) {
						log.debug "Command was a success. Setting alarm state to ${state.value}"
						return createEvent(name: "alarm", value: state.value)
					}
				}
			}
		}
	}
	log.debug "Parse returned $map"
	def results = map ? createEvent(map) : null
	log.debug "parse results: " + results
	return results
}

def strobe() {
	log.debug "Executing 'strobe'"
	state.value = "strobe"
	startCmd(ALARM_STROBE)
}

def siren() {
	log.debug "Executing 'siren'"
	state.value = "siren"
	startCmd(ALARM_SIREN)
}

def both() {
	log.debug "Executing 'both'"
	state.value = "both"
	startCmd(ALARM_BOTH)
}

def off() {
	log.debug "Executing 'off'"
	state.value = "off"
	zigbee.command(IAS_WD_CLUSTER, COMMAND_IAS_WD_START_WARNING, "00", "0000", "00", "00")
}

def ping() {
	return zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: 30 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def configure() {
	def cmds = zigbee.enrollResponse() + 
		zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 0, 180, null)

	log.debug "configure: " + cmds
	off()
	return cmds
}

def startCmd(cmd) {
	log.debug "start command ${cmd}"

	state.alarmCmd = cmd
	def warningDuration = state.maxDuration ? state.maxDuration : DEFAULT_MAX_DURATION
	state.lastDuration = warningDuration

	def paramMode;
	def paramDutyCycle;
	def paramStrobeLevel;

	if (cmd == ALARM_SIREN) {
		paramMode = MODE_SIREN
		paramDutyCycle = BASIC_DUTY_CYCLE 
		paramStrobeLevel = BASIC_LEVEL 
	} else if (cmd == ALARM_STROBE) {
		paramMode = MODE_STROBE
		paramDutyCycle = STROBE_DUTY_CYCLE 
		paramStrobeLevel = STROBE_LEVEL
	} else if (cmd == ALARM_BOTH) {
		paramMode = MODE_BOTH
		paramDutyCycle = STROBE_DUTY_CYCLE
		paramStrobeLevel = STROBE_LEVEL
	}

	zigbee.command(IAS_WD_CLUSTER, COMMAND_IAS_WD_START_WARNING, paramMode, DataType.pack(warningDuration, DataType.UINT16))
}