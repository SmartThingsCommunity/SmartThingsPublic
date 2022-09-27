/*
 *  Copyright 2022 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "SiHAS People Counter", namespace: "shinasys", author: "SHINA SYSTEM", mnmn: "SmartThingsCommunity", vid: "c798ff70-928b-36b4-aebc-1fefc7b49030") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		capability "afterguide46998.peopleCounterV2"
		capability "afterguide46998.inOutDirectionV2"
		capability "afterguide46998.freeze"
		capability "Momentary"
		
		//////////////////////////////////////////////////////////////
        // People Counter version description
        //////////////////////////////////////////////////////////////
		// application version > 10 : People Counter V2(TOF) Version (People Counter for Setting : 81~99)
        // application version < 10 : People Counter Version
		//////////////////////////////////////////////////////////////
        fingerprint inClusters: "0000,0001,0003,000C,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "CSM-300Z", deviceJoinName: "SiHAS People Counter", ocfDeviceType: "x.com.st.d.sensor.motion"
	}
	preferences {
		section {
        	/* korean language
			input (
					title: "설정 설명", 
					description: "아래 설정은 V2(TOF) 버전에 해당하는 설정입니다.", 
					displayDuringSetup: false, 
					type: "paragraph", 
					element: "paragraph")        
			input ("ledStatus", "boolean", 
					title: "LED 상태 표시 여부", 
					description: "동작 상태를 LED로 표시할지 여부를 설정합니다.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("transationInterval", "enum",
					title: "트랜잭션간 간격 설정", 
					description: "트랜잭션간 간격을 설정합니다. 연속으로 들어갈 일이 잦을 시 트랜잭션 인터벌을 짧게 반대의 경우 길게 설정하시면 됩니다.", 
					displayDuringSetup: false, 
					options: [0: "지연없음",
							  1: "0.2초",
							  2: "0.4초(기본값)",
							  3: "0.6초",
							  4: "0.8초",
							  5: "1.0초"],
					defaultValue: "2",
					required: false)
			input ("inFastStatus", "boolean", 
					title: "들어갈때 빠른 동작설정", 
					description: "카운터가 0이고 들어갈때 카운터 1로 설정하는것을 한 트랜잭션이 끝나기 전에 빠르게 설정을 할지 여부를 정할수 있습니다.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("outFastStatus", "boolean", 
					title: "나갈때 빠른 동작설정", 
					description: "카운터가 1이고 나갈때 카운터를 0으로 설정하는것을 한 트랜잭션이 끝나기 전에 빠르게 설정을 할지 여부를 정할수 있습니다.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("rfStatus", "boolean", 
					title: "RF 통신 동작", 
					description: "시하스 스위치와 연동을 위해서 RF 통신 동작 여부를 설정합니다.", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
			input ("rfPairing", "boolean", 
					title: "RF 페어링", 
					description: "시하스 스위치와 RF 페어링을 시작합니다. (먼저 RF통신 동작이 활성화되어야 합니다.)", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
			input ("distanceInit", "boolean", 
					title: "거리 재 조정", 
					description: "설치 위치가 바뀌면 거리 재조정을 진행해야합니다. 거리 재 조정 설정을 시작합니다. 5초동안 동작합니다.)", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
			*/
            // english version
            input (
					title: "Setting Description", 
					description: "The settings below correspond to the V2 (TOF) version.", 
					displayDuringSetup: false, 
					type: "paragraph", 
					element: "paragraph")        
			input ("ledStatus", "boolean", 
					title: "LED status indication", 
					description: "Sets whether the operational status is indicated by LED.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("transationInterval", "enum",
					title: "Set transaction interval", 
					description: "Sets the transaction interval. If you frequently enter consecutively, you can set the transaction interval to be short and long in the opposite case.", 
					displayDuringSetup: false, 
					options: [0: "No delay",
							  1: "0.2 seconds",
							  2: "0.4 seconds(default)",
							  3: "0.6 seconds",
							  4: "0.8 seconds",
							  5: "1.0 seconds"],
					defaultValue: "2",
					required: false)
			input ("inFastStatus", "boolean", 
					title: "Set up quick action when entering", 
					description: "When the counter is zero and enters, you can decide whether to set counter 1 quickly before one transaction ends.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("outFastStatus", "boolean", 
					title: "Set up quick action when out", 
					description: "When the counter is 1 and leaves, you can decide whether to set the counter to 0 quickly before one transaction ends.", 
					displayDuringSetup: false, 
					defaultValue: "true", 
					required: false)
			input ("rfStatus", "boolean", 
					title: "RF Communication Operation", 
					description: "Set RF communication operation for interworking with SiHAS switch.", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
			input ("rfPairing", "boolean", 
					title: "RF Pairing", 
					description: "Start RF pairing with the SiHAS switch. (RF communication operation must be enabled first.)", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
			input ("distanceInit", "boolean", 
					title: "Distance readjustment", 
					description: "If the installation location changes, the distance must be readjusted. Start distance re-adjustment setting and operate for 5 seconds.", 
					displayDuringSetup: false, 
					defaultValue: "false", 
					required: false)
		}
	}
}

private getOCCUPANCY_SENSING_CLUSTER() { 0x0406 }
private getANALOG_INPUT_BASIC_CLUSTER() { 0x000C }
private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }
private getOCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE() { 0x0000 }
private getANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE() { 0x0055 }

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()
	descMaps.add(descMap)
	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}
	return  descMaps
}

def parse(String description) {
	log.debug "Parsing message from device: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('read attr')) {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
				List<Map> descMaps = collectAttributes(descMap)
				def battMap = descMaps.find { it.attrInt == POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE }
				if (battMap) {
					map = getBatteryResult(Integer.parseInt(battMap.value, 16))
				}
			} else if (descMap?.clusterInt == ANALOG_INPUT_BASIC_CLUSTER && descMap.attrInt == ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE && descMap?.value) {
				map = getAnalogInputResult(Integer.parseInt(descMap.value,16))
			}
		}
	}

	def result = map ? createEvent(map) : [:]

	log.debug "result: $result"
	return result
}

private Map getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
	def result = [:]
	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		def minVolts = 1.9
		def maxVolts = 3.1
		
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct <= 0)
			roundedPct = 1
		result.value = Math.min(100, roundedPct)
		result.descriptionText = "${device.displayName} battery was ${result.value}%"        
	}
	return result
}

private Map getAnalogInputResult(value) {
	def application = getDataValue("application")
	int version = zigbee.convertHexToInt(application)
	Float fpc = Float.intBitsToFloat(value.intValue())
	def prevInOut = device.currentState('inOutDir')?.value // in out status
	def prevCnt = device.currentState('peopleCounter')?.value 
	def freezeSts = device.currentState('freeze')?.value
	int pc = ((int)(fpc*10))/10 //people counter
	int inout = ((int)(fpc*10).round(0))%10; // inout direction : .1 = in, .2 = out, .0 = ready	
	 
	if (freezeSts == null) {
		sendEvent(name: "freeze", value: "off", displayed: true, isStateChange: true)		
	}
	
	if (freezeSts == null || freezeSts == "off") { // freeze off
		String inoutString = ( (inout==1) ? "in" : (inout==2) ? "out":"ready")
		String descriptionText1 = "${device.displayName} : $pc"
		String descriptionText2 = "${device.displayName} : $inoutString"

		log.debug "[$fpc] = people: $pc, dir: $inout, $inoutString"

		String motionActive = pc ? "active" : "inactive"
		sendEvent(name: "motion", value: motionActive, displayed: true, isStateChange: false)

		if((inoutString != "ready") && (prevInOut == inoutString)) {
			sendEvent(name: "inOutDir", value: "ready", displayed: true)
		}

		sendEvent(name: "inOutDir", value: inoutString, displayed: true, descriptionText: descriptionText2)
		if ( version > 10 && pc > 80 && pc < 100) { // version > 10 and People Count > 80 and People Count < 100 : TOF Setting Value, so ignore
			pc = prevCnt.toInteger()
		}		
		return [
			name           : 'peopleCounter',
			value          : pc,
			descriptionText: descriptionText1,
			translatable   : true
		]
	} else { // freeze on
		String descriptionText1 = "${device.displayName} : $prevCnt"
		pc = prevCnt.toInteger()
		return [
			name           : 'peopleCounter',
			value          : pc,
			descriptionText: descriptionText1,
			translatable   : true
		]
	}
}

def setPeopleCounter(peoplecounter) {
	int pc =  Float.floatToIntBits(peoplecounter);
	log.debug "SetPeopleCounter = $peoplecounter"
	zigbee.writeAttribute(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE, DataType.FLOAT4, pc)
}

def setFreeze(freezeSts) {
	def application = getDataValue("application")
	int version = zigbee.convertHexToInt(application)
    
	sendEvent(name: "freeze", value: freezeSts, displayed: true, isStateChange: true)
	if( freezeSts == "on") {
		if ( version > 10 ) {
			return setPeopleCounter(82)
		}
	} else {
		if ( version > 10 ) {
			return setPeopleCounter(84)
		} else {
        	return zigbee.readAttribute(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE)	
        }
	}
	return null
}

def push() {
	setPeopleCounter(0)
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
}

def updated() {
	def application = getDataValue("application")
	int version = zigbee.convertHexToInt(application)
	
	if (version > 10) { // version > 10 and People Count > 80 and People Count < 100 : TOF Setting Value
		
		def ledStatusRet = (ledStatus != null) ? ledStatus : "true"
		if (ledStatusRet != device.latestValue("ledStatus")) {
			sendEvent(name: "ledStatus", value: ledStatusRet, descriptionText: "ledStatus set to ${ledStatusRet}")
			if ( ledStatusRet == "true") {
				sendHubCommand(setPeopleCounter(86), 1)
			} else {
				sendHubCommand(setPeopleCounter(87), 1)
			}
		}
		def transationIntervalRet = (transationInterval != null) ? transationInterval : "2"		
		if (transationIntervalRet != device.latestValue("transationInterval")) {
			sendEvent(name: "transationInterval", value: transationIntervalRet, descriptionText: "transationInterval set to ${transationIntervalRet}")
			if ( transationIntervalRet == "0") {
				sendHubCommand(setPeopleCounter(90), 1)
			} else if ( transationIntervalRet == "1") {
				sendHubCommand(setPeopleCounter(91), 1)
			} else if ( transationIntervalRet == "2") {
				sendHubCommand(setPeopleCounter(92), 1)
			} else if ( transationIntervalRet == "3") {
				sendHubCommand(setPeopleCounter(93), 1)
			} else if ( transationIntervalRet == "4") {
				sendHubCommand(setPeopleCounter(94), 1)
			} else if ( transationIntervalRet == "5") {
				sendHubCommand(setPeopleCounter(95), 1)
			}			
		}
		def inFastStatusRet = (inFastStatus != null) ? inFastStatus : "true"
		if (inFastStatusRet != device.latestValue("inFastStatus")) {
			sendEvent(name: "inFastStatus", value: inFastStatusRet, descriptionText: "inFastStatus set to ${inFastStatusRet}")
			if ( inFastStatusRet == "true") {
				sendHubCommand(setPeopleCounter(96), 1)
			} else {
				sendHubCommand(setPeopleCounter(97), 1)
			}
		}
		def outFastStatusRet = (outFastStatus != null) ? outFastStatus : "true"
		if (outFastStatusRet != device.latestValue("outFastStatus")) {
			sendEvent(name: "outFastStatus", value: outFastStatusRet, descriptionText: "outFastStatus set to ${outFastStatusRet}")
			if ( outFastStatusRet == "true") {
				sendHubCommand(setPeopleCounter(98), 1)
			} else {
				sendHubCommand(setPeopleCounter(99), 1)
			}
		}
		def rfStatusRet = (rfStatus != null) ? rfStatus : "false"
		if (rfStatusRet != device.latestValue("rfStatus")) {
			sendEvent(name: "rfStatus", value: rfStatusRet, descriptionText: "rfStatus set to ${rfStatusRet}")
			if ( rfStatusRet == "true") {
				sendHubCommand(setPeopleCounter(88), 1)
			} else {
				sendHubCommand(setPeopleCounter(89), 1)
			}
		}
		def rfPairingRet = (rfPairing != null) ? rfPairing : "false"
		if (rfPairingRet != device.latestValue("rfPairing")) {
			sendEvent(name: "rfPairing", value: rfPairingRet, descriptionText: "rfPairing set to ${rfPairingRet}")
			if ( rfPairingRet == "true") {
				sendHubCommand(setPeopleCounter(81), 1)
			}
		}
		def distanceInitRet = (distanceInit != null) ? distanceInit : "false"
		if (distanceInitRet != device.latestValue("distanceInit")) {
			sendEvent(name: "distanceInit", value: distanceInitRet, descriptionText: "distanceInit set to ${distanceInitRet}")
			if ( distanceInitRet == "true") {
				sendHubCommand(setPeopleCounter(83), 1)
			}
		}
	}
    return null
}

def refresh() {
	def refreshCmds = []
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
	refreshCmds += zigbee.readAttribute(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE)	
	return refreshCmds
}

def configure() {
	def configCmds = []
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)
	configCmds += zigbee.configureReporting(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE, DataType.FLOAT4, 1, 600, 1)
	return configCmds + refresh()
}

def installed() {
	log.info("installed")
	sendEvent(name: "ledStatus", value:"true")
    sendEvent(name: "transationInterval", value:"2")
    sendEvent(name: "inFastStatus", value:"true")
    sendEvent(name: "outFastStatus", value:"true")
    sendEvent(name: "rfStatus", value:"false")
    sendEvent(name: "rfPairing", value:"false")
    sendEvent(name: "distanceInit", value:"false")
}