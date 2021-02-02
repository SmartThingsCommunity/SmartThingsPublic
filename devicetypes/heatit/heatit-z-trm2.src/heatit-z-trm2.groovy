/**
 *  Copyright 2018
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
 * V0.0.1 29/06/2018
 *
 *
 * Changelog:
 *
 * 0.0.1 - Initial version based on work by AdamV and Fibaro
 *         Using security encapsulation
 *         Changed the way configuration is done (borrowed from Fibaro), removed the configure button, configure is triggered by save
 *         Added power and energy visualisation
 *         Added the parameters new to Heatit ZTrm2
 *         Use the temp sensor relavant to the selected sensor mode, this ensures that the reported temp is the same as on the display of the device
 *         Unsolved issues:
 *            Can't differentiat the 3 different temperature sensorstly only using the floor sensor.
 *
 *         Roadmap:
 *            In the main display: 
 *                Use up/down buttons to control either ecoHeating or normal heating based on mode of operation.
 *                In power mode use up/down buttons to control power setting, also show power setting instead of temp set point
*/
 
 preferences {
     parameterMap().each {
         input (
             title: "${it.num}. ${it.title}",
             description: it.descr,
             type: "paragraph",
             element: "paragraph"
         )

         input (
             name: it.key,
             title: null,
             description: "Default: $it.def" ,
             type: it.type,
             options: it.options,
             range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
             defaultValue: it.def,
             required: false
         )
     }

     input ( name: "logging", title: "Logging", type: "boolean", required: false )
}



metadata {
	definition (name: "heatit Z-Trm2", namespace: "heatit", author: "magnusstam") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
        capability "Thermostat Mode"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Setpoint"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Zw Multichannel"

	
		command "switchMode"
        command "energySaveHeat"
        command "quickSetHeat"
        command "quickSetecoHeat"
        command "pressUp"
        command "pressDown"

    fingerprint mfr: "019B", prod: "0003", model: "0202"
		//fingerprint deviceId: "0x0806"
		//fingerprint inClusters: "0x5E, 0x43, 0x31, 0x86, 0x40, 0x59, 0x85, 0x73, 0x72, 0x5A, 0x70"
	}

	// simulator metadata
	simulator {
		status "off"			: "command: 4003, payload: 00"
		status "heat"			: "command: 4003, payload: 01"
		status "cool"			: "command: 4003, payload: 02"
		status "auto"			: "command: 4003, payload: 03"
		status "emergencyHeat"	: "command: 4003, payload: 04"

		status "fanAuto"		: "command: 4403, payload: 00"
		status "fanOn"			: "command: 4403, payload: 01"
		status "fanCirculate"	: "command: 4403, payload: 06"

		status "heat 60"        : "command: 4303, payload: 01 09 3C"
		status "heat 68"        : "command: 4303, payload: 01 09 44"
		status "heat 72"        : "command: 4303, payload: 01 09 48"

		status "cool 72"        : "command: 4303, payload: 02 09 48"
		status "cool 76"        : "command: 4303, payload: 02 09 4C"
		status "cool 80"        : "command: 4303, payload: 02 09 50"

		status "temp 58"        : "command: 3105, payload: 01 2A 02 44"
		status "temp 62"        : "command: 3105, payload: 01 2A 02 6C"
		status "temp 70"        : "command: 3105, payload: 01 2A 02 BC"
		status "temp 74"        : "command: 3105, payload: 01 2A 02 E4"
		status "temp 78"        : "command: 3105, payload: 01 2A 03 0C"
		status "temp 82"        : "command: 3105, payload: 01 2A 03 34"

		status "idle"			: "command: 4203, payload: 00"
		status "heating"		: "command: 4203, payload: 01"
		status "cooling"		: "command: 4203, payload: 02"
		status "fan only"		: "command: 4203, payload: 03"
		status "pending heat"	: "command: 4203, payload: 04"
		status "pending cool"	: "command: 4203, payload: 05"
		status "vent economizer": "command: 4203, payload: 06"

		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

    tiles (scale: 2)
    {

        multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) 
        {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") 
            {
                attributeState("default", label:'${currentValue}°', unit:"C", action:"switchMode", icon:"st.Home.home1")
            }

            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") 
            {
                attributeState("VALUE_UP", action: "pressUp")
                attributeState("VALUE_DOWN", action: "pressDown")
            }
            tileAttribute("device.tempSenseMode", key: "SECONDARY_CONTROL") 
            {
                attributeState("default", label:'${currentValue}', unit:"", icon:" ")
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") 
            {
                attributeState("idle", backgroundColor:"#44b621")
                attributeState("heating", backgroundColor:"#bc2323")
                attributeState("energySaveHeat", backgroundColor:"#ffa81e")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") 
            {
                attributeState("off", label:'${name}', action:"switchMode", nextState:"heat")
                attributeState("heat", label:'${name}', action:"switchMode", nextState:"energy")
                attributeState("energySaveHeat", label:'${name}', action:"switchMode", nextState:"off")
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") 
            {
                attributeState("default", label:'${currentValue}')
            }
        }

        valueTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "off", label:'${name}', action:"switchMode", nextState:"to_heat"
            state "heat", label:'${name}', action:"switchMode", nextState:"energySaveHeat"
            state "energySaveHeat", label: "eco heat", action:"switchMode", nextState:"off"
        }
        valueTile("heatLabel", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
            state "default", label:"Heat Set Point:"
        }
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: "(5..40)") {
            state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
        }
        //   standardTile("blank", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        //		state "default", label:""
        //	}
        valueTile("ecoLabel", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
            state "default", label:"Eco Mode Set Point:"
        }
        controlTile("ecoheatSliderControl", "device.ecoheatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: "(5..40)") {
            state "setecoHeatingSetpoint", action:"quickSetecoHeat", backgroundColor:"#d04e00"
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'${currentValue}\n W', action:"refresh"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "energy", label:'${currentValue}\n kWh', action:"refresh"
        }

        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }

        main "thermostatMulti"
        details(["thermostatMulti", "mode", "heatLabel", "heatSliderControl", "refresh", "ecoLabel", "ecoheatSliderControl", "power", "energy"])
	}
}


def updated() {
	log.debug "Updated called"
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    def cmds = []
    logging("${device.displayName} - Executing updated()","info")
    
//    removeChildDevices()
/*
 	if (!childDevices) {
        createChildDevices()
    }
    if (device.currentValue("numberOfButtons") != 6) { sendEvent(name: "numberOfButtons", value: 6) }
*/
	//createChildTempDevices()
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1) //verify if group 1 association is correct
 	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 2, nodeId:[zwaveHubNodeId])
 	
//    cmds << zwave.multiChannelV3.multiChannelEndPointGet()

    runIn(3,"syncStart")
    state.lastUpdated = now()
    response(encapSequence(cmds,1000))

    //configure()
}

def createChildTempDevices() {
    log.debug "Creating Temperature children"
    for (i in 3..5) {
    	try {
        	//If we have a temperature reading from this sensor (1 to 4) then try to create a child for it
            log.debug "Have received temperature readings for termperature${i} so creating a child for it if not already there"
            def currentchild = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-temperature${i}"}
            if (currentchild == null) {
                addChildDevice("smartthings", "Temperature Sensor", "${device.deviceNetworkId}-temperature${i}", device.hub.id,
                               [completedSetup: true, name: "${device.displayName} (Temp${i})", isComponent: false]) //, label: "${device.displayName} (Temp${i})"
            }
        } catch (e) {
            log.debug "Error adding Temperature # ${i} child: ${e}"
        }
    }
}

private removeChildDevices() {
	log.debug "Removing Child Devices"
    try {
        getChildDevices()?.each {
        	try {
            	deleteChildDevice(it.deviceNetworkId)
				log.debug "OK"
            } catch (e) {
                log.debug "Error deleting ${it.deviceNetworkId}, probably locked into a SmartApp: ${e}"
            }
        }
    } catch (err) {
        log.debug "Either no children exist or error finding child devices for some reason: ${err}"
    }
}



def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
    log.debug "multichannelv3.MultiChannelCapabilityReport: ${cmd}"
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "${device.displayName} - MultiChannelCmdEncap ${cmd}"
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        log.debug "${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}"
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    } else {
        log.warn "Unable to extract MultiChannel command from $cmd"
    }
}


def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
    log.debug "${device.displayName} - ThermostatSetpoinReport received, value: ${cmd.scaledValue} scale: ${cmd.scale}"
    
   	if (cmd.setpointType == 1){
        def heating = cmd.scaledValue
        sendEvent(name: "heatingSetpoint", value: heating)
    }
    if (cmd.setpointType == 11){
    	def energyHeating = cmd.scaledValue
        sendEvent(name: "ecoHeatingSetpoint", value: energyHeating)
        state.ecoheatingSetpoint = energyHeating
    }
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	//map
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd)
{
 //   log.debug "${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale}"
    switch (cmd.scale) {
        case 0:
            sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
        //    log.debug "energy $cmd.scaledMeterValue kwh"         
            break
        case 2:
            sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
        //    log.debug "power $cmd.scaledMeterValue W"         
            break
    }
    multiStatusEvent("${(device.currentValue("power") ?: "0.0")} W | ${(device.currentValue("energy") ?: "0.00")} kWh")
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    log.debug("Temperature is: $cmd.scaledSensorValue °C")
    sendEvent(name: "temperature", value: cmd.scaledSensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd)
{
	log.debug("operating rep: $cmd")
	def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
			break
	}
	map.name = "thermostatOperatingState"
	map
}


def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	log.debug("Thermostat Mode Report $cmd")
	def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
            sendEvent(name: "thermostatOperatingState", value: "idle")
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
            sendEvent(name: "thermostatOperatingState", value: "heating")
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
        case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT:
			map.value = "energySaveHeat"
            sendEvent(name: "thermostatOperatingState", value: "energySaveHeat")
			break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [:]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "fanAuto"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "fanOn"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "fanCirculate"
			break
	}
	map.name = "thermostatFanMode"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	log.debug("support reprt: $cmd")
    def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }
    if(cmd.energySaveHeat) { supportedModes += "energySaveHeat " }

	state.supportedModes = supportedModes
}

def zwaveEvent(physicalgraph.zwave.commands.multiinstanceassociationv1.MultiInstanceAssociationReport cmd)
{
    logging("${device.displayName} MultiInstanceAssociationReport - ${cmd}","info")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd)
{
    log.debug ("${device.displayName} MultiChannelAssociationReport - ${cmd}")//,"info")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Basic Zwave event received: $cmd.payload"
}
	
def pressUp(){
	log.debug("pressed Up")
	def currTemp = device.latestValue("heatingSetpoint")
    log.debug(" pressed up currently $currTemp")
    def newTemp = currTemp + 0.5
    log.debug(" pressed up new temp is $newTemp")
	quickSetHeat(newTemp)
}

def pressDown(){
	log.debug("pressed Down")
	def currTemp = device.latestValue("heatingSetpoint")
    def newTemp = currTemp - 0.5
	quickSetHeat(newTemp)
}

def quickSetHeat(degrees) {
	
	setHeatingSetpoint(degrees, 1000)
}

def setHeatingSetpoint(degrees, delay = 30000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 30000) {
	log.trace "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    //if (locationScale == "C" && deviceScaleString == "F") {
    //	convertedDegrees = celsiusToFahrenheit(degrees)
    //} else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    //} else {
    	convertedDegrees = degrees
    //}
    def cmds = []
    cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees)
    cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1)
    encapSequence(cmds)
}

def quickSetecoHeat(degrees) {
	
	setecoHeatingSetpoint(degrees, 1000)
}

def setecoHeatingSetpoint(degrees, delay = 30000) {
	setecoHeatingSetpoint(degrees.toDouble(), delay)
}

def setecoHeatingSetpoint(Double degrees, Integer delay = 30000) {
	log.trace "setecoHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    //if (locationScale == "C" && deviceScaleString == "F") {
    //	convertedDegrees = celsiusToFahrenheit(degrees)
    //} else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    //} else {
    	convertedDegrees = degrees
    //}

    def cmds = []
    cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 11, scale: deviceScale, precision: p, scaledValue: convertedDegrees)
    cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 11)
    encapSequence(cmds, delay)
}


def poll() {
	def cmds = []
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 2)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
 //   cmds << zwave.multiChannelAssociationV2.MultiChannelAssociationGroupingsGet()

//    cmds << zwave.configurationV2.configurationGet(parameterNumber: 1)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 2)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 3)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 4)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 5)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 6)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 7)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 8)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 9)
//    cmds << zwave.configurationV2.configurationGet(parameterNumber: 10)
//    cmds << zwave.configurationV2.configurationGet(parameterNumber: 11)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 12)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 13)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 14)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 15)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 16)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 17)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 18)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 19)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 20)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 21)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 22)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 23)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 24)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 25)
    encapSequence(cmds, 650)
}

def configure() 
{
   poll()    
}

def modes() {
	["off", "heat", "energySaveHeat"]
}


def switchMode() {
	
    def currentMode = device.currentState("thermostatMode")?.value
    if (!currentMode)
    {
    	currentMode = "off"
    }
    log.debug "currentMode $currentMode"
 	def cmds = []
   // log.debug("currentMode is $currentMode")
    if (currentMode == "off"){
    	def nextMode = "heat"
        sendEvent(name: "thermostatMode", value: "heat")
        sendEvent(name: "thermostatOperatingState", value: "heating")

    	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 1)
    	cmds << zwave.thermostatModeV2.thermostatModeGet()
    	encapSequence(cmds, 650)
        poll()
    }
    else if (currentMode == "heat"){
    	def nextMode = "energySaveHeat"
        sendEvent(name: "thermostatMode", value: "energySaveHeat")
        sendEvent(name: "thermostatOperatingState", value: "energySaveHeat")
    	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 11)
    	cmds << zwave.thermostatModeV2.thermostatModeGet()
    	encapSequence(cmds, 650)
        poll()
    }
    else if (currentMode == "energySaveHeat"){
    	def nextMode = "off"
        sendEvent(name: "thermostatMode", value: "off")
        sendEvent(name: "thermostatOperatingState", value: "idle")
    	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 11)
    	cmds << zwave.thermostatModeV2.thermostatModeGet()
    	encapSequence(cmds, 650)
        poll()
    }
}

def switchToMode(nextMode) {
	def supportedModes = getDataByName("supportedModes")
	if(supportedModes && !supportedModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}
def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"energySaveHeat": 11
]}

def setThermostatMode(String value) {
 	def cmds = []
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value])
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    encapSequence(cmds, standardDelay)
}

def off() {
 	def cmds = []
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 0)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    encapSequence(cmds, 650)
	delayBetween([
        sendEvent(name: "thermostatMode", value: "off"),
        sendEvent(name: "thermostatOperatingState", value: "idle"),
        poll()], 650)
    	
}

def heat() {
 	def cmds = []
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 1)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    encapSequence(cmds, 650)
    delayBetween([
        sendEvent(name: "thermostatMode", value: "heat"),
        sendEvent(name: "thermostatOperatingState", value: "heating"),
        poll()], 650)
}

def energySaveHeat() {
 	def cmds = []
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 11)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    encapSequence(cmds, 650)
    delayBetween([
        sendEvent(name: "thermostatMode", value: "energySaveHeat"),
        sendEvent(name: "thermostatOperatingState", value: "energySaveHeat"),
        poll()], 650)
}

def auto() {
 	def cmds = []
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 3)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    encapSequence(cmds, standardDelay)
}

private getStandardDelay() {
	1000
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
    def result = []
    logging("${device.displayName} - Parsing: ${description}")
    if (description.startsWith("Err 106")) {
        result = createEvent(
                descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
                eventType: "ALERT",
                name: "secureInclusion",
                value: "failed",
                displayed: true,
        )
    } else if (description == "updated") {
        return null
    } else {
        def cmd = zwave.parse(description, cmdVersions())
        if (cmd) {
            logging ("${device.displayName} - Parsed: ${cmd}")
            zwaveEvent(cmd)
        }
    }
}

private syncStart() {
    boolean syncNeeded = false
    parameterMap().each {
        if(settings."$it.key" != null) {
            if (state."$it.key" == null) 
            { 
            	state."$it.key" = [value: null, state: "synced", scale: null] 
            }
            if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state in ["notSynced","inProgress"]) {
                state."$it.key".value = settings."$it.key" as Integer
                state."$it.key".state = "notSynced"
                syncNeeded = true
            }
        }
    }
    if ( syncNeeded ) {
        logging("${device.displayName} - starting sync.", "info")
        multiStatusEvent("Sync in progress.", true, true)
        syncNext()
    }
}

private syncNext() {
    logging("${device.displayName} - Executing syncNext()","info")
    def cmds = []
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
            multiStatusEvent("Sync in progress. (param: ${param.num})", true)
            state."$param.key"?.state = "inProgress"
            state."$param.key"?.scale = param.scale
            cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size, param.scale), parameterNumber: param.num, size: param.size)))
            cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
            
            if (param.num == 2)
            {
    			cmds << response(encap(zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId:[zwaveHubNodeId])))
                cmds << response(encap(zwave.associationV2.associationRemove(groupingIdentifier:4, nodeId:[zwaveHubNodeId])))
                cmds << response(encap(zwave.associationV2.associationRemove(groupingIdentifier:5, nodeId:[zwaveHubNodeId])))
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 3, nodeId:[zwaveHubNodeId])
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 4, nodeId:[zwaveHubNodeId])
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 5, nodeId:[zwaveHubNodeId])

                def sensor = 3 as long // build in sensor
                if (state."$param.key".value == 0 || state."$param.key".value == 5)
                {
                	sensor = 5 // floor sensor
                }
                else if (state."$param.key".value == 3)
                {
                	sensor = 4 // external sensor
                }
                cmds << response(encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: sensor, nodeId:[zwaveHubNodeId])))
                cmds << response(encap(zwave.associationV2.associationSet(groupingIdentifier: sensor, nodeId:[zwaveHubNodeId])))

            }
            break
        }
    }
    if (cmds) {
        runIn(10, "syncCheck")
        log.debug "cmds!"
        sendHubCommand(cmds,1000)
    } else {
        runIn(1, "syncCheck")
    }
}

private syncCheck() {
    logging("${device.displayName} - Executing syncCheck()","info")
    def failed = []
    def incorrect = []
    def notSynced = []
    parameterMap().each {
        if (state."$it.key"?.state == "incorrect" ) {
            incorrect << it
        } else if ( state."$it.key"?.state == "failed" ) {
            failed << it
        } else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
            notSynced << it
        }
    }
    if (failed) {
        logging("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}","info")
        sendEvent(name: "syncStatus", value: "failed")
        multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
    } else if (incorrect) {
        logging("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
    } else if (notSynced) {
        logging("${device.displayName} - Sync incomplete!","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
    } else {
        logging("${device.displayName} - Sync Complete","info")
        sendEvent(name: "syncStatus", value: "synced")
        multiStatusEvent("Sync OK.", true, true)
    }
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
        sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    def value = state."$paramKey".value as double
    def scale = 1 as long
    if (state."$paramKey".scale)
    {
    	scale = state."$paramKey".scale
    }
    def scaledValue = value * scale as long
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + scaledValue , "info")
    state."$paramKey".state = (scaledValue == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// This will capture any commands not handled by other instances of zwaveEvent
	// and is recommended for development so you can see every command the device sends
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract Secure command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = cmdVersions()[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract CRC16 command from $cmd"
    }
}


private logging(text, type = "debug") {
    if (settings.logging == "true") {
        log."$type" text
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd", "info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
    log.info ("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd")//,"info")
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
    encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
    encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
    encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo.cc.contains("56")){
        crcEncap(cmd)
    } else {
        logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
    delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Long value, Integer size = 1, Integer scale = 1) {
	value = value * scale    
    def result = []
    size.times {
        result = result.plus(0, (value & 0xFF) as Short)
        value = (value >> 8)
    }
    return result
}

private secure(physicalgraph.zwave.Command cmd) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
    //zwave.crc16encapV1.crc16Encap().encapsulate(cmd).format()
    "5601${cmd.format()}0000"
}

private commands(commands, delay=200) {
    log.info "inside commands: ${commands}"
    delayBetween(commands.collect{ command(it) }, delay)
}

private setSecured() {
    updateDataValue("secured", "true")
}
private isSecured() {
    getDataValue("secured") == "true"
}


private Map cmdVersions() {
    [0x85: 2, 0x59: 1, 0x8E: 2, 0x86: 3, 0x70: 2, 0x72: 2, 0x5E: 2, 0x5A: 1, 0x73: 1, 0x7A: 4, 0x60: 3, 0x20: 1, 0x6C: 1, 0x31: 5, 0x43: 2, 0x40: 2, 0x98: 1, 0x9F: 1, 0x25: 1]
}

private parameterMap() {[
    [key: "sensorMode", num: 2, size: 1, type: "enum", options: [
        0: "F - Floor temperature mode",
        1: "A - Room temperature mode",
        2: "AF - Room mode w/floor limitations",
        3: "A2 - Room temperature mode (external)",
        4: "P - Power regulator mode",
        5: "FP - Floor mode with minimum power limitation"
    ], def: "1", title: "Sensor Mode",
     descr: "This parameter determines what kind of sensor is used to regulate the power", scale: 1],
    [key: "floorSensorType", num: 3, size: 1, type: "enum", options: [
    	0: "10k ntc",
        1: "12k ntc",
        2: "15k ntc",
        3: "22k ntc",
        4: "33k ntc",
        5: "47k ntc"
    ], def: "0", title: "Floor Sensor Type",
     descr: "This parameter determines floor sensor type, 10k ntc is default", scale: 1],
    
    [key: "TemperatureControlCysteresis", num: 4, size: 1, type: "number", def:0.5, min: 0.3, max: 3, title: "Hysteresis temp (0.3°..3°)",
     descr: "This parameter determines the control hysteresis", scale: 10],
    
    [key: "FLo", num: 5, size: 2, type: "number", def:5.0, min: 5.0, max: 40.0, title: "Minimum floor temperature(5°..40°)",
     descr: "Minimum floor temperature", scale: 10],
    
    [key: "FHi", num: 6, size: 2, type: "number", def:40.0, min: 5.0, max: 40.0, title: "Maxmum floor temperature (5°..40°)",
     descr: "Maxmum floor temperature", scale: 10],
    
    [key: "ALo", num: 7, size: 2, type: "number", def:5.0, min: 5.0, max: 40.0, title: "Minimum air temperature (5°..40°)",
     descr: "Minimum air temperature", scale: 10],
    
    [key: "AHi", num: 8, size: 2, type: "number", def:40.0, min: 5.0, max: 40.0, title: "Maxmum air temperature (5°..40°)",
     descr: "Maxmum air temperature", scale: 10],

    [key: "PLo", num: 9, size: 1, type: "number", def:0, min: 0, max: 9, title: "FP mode P setting",
     descr: "FP mode P setting (0..9)", scale: 1],
    
    [key: "P", num: 12, size: 1, type: "number", def:2, min: 0, max: 10, title: "P setting",
     descr: "P Setting (0..10)", scale: 1],

	[key: "COOL", num: 13, size: 2, type: "number", def:21.0, min: 5.0, max: 40.0, title: "Cooling temperature (5°..40°)",
     descr: "Cooling temperature", scale: 10],

    [key: "RoomSensorCalibration", num: 14, size: 1, type: "number", def:0.0, min: -4.0, max: 4.0, title: "Room sensor calibration (-4°..4°)",
     descr: "Room sensor calibration in deg. C (x10)", scale: 10],

    [key: "FloorSensorCalibration", num: 15, size: 1, type: "number", def:0.0, min: -4.0, max: 4.0, title: "Floor sensor calibration (-4°..4°)",
     descr: "Floor sensor calibration in deg. C (x10)", scale: 10],

 	[key: "ExtSensorCalibration", num: 16, size: 1, type: "number", def:0.0, min: -4.0, max: 4.0, title: "External sensor calibration (-4°..4°)",
     descr: "External sensor calibration in deg. C (x10)", scale: 10],

    [key: "tempDisplay", num: 17, size: 1, type: "enum", options: [
    	0: "Display setpoint temperature (Default)",
        1: "Display measured temperature"
    ], def: "0", title: "Temperatur Display",
     descr: "Selects which temperature is shown in the display", scale: 1],

	[key: "DimBtnBright", num: 18, size: 1, type: "number", def:50, min: 0, max: 100, title: "Button brightness – dimmed state (%)",
     descr: "Configure the brightness of the buttons, in dimmed state", scale: 1],

    [key: "ActBtnBright", num: 19, size: 1, type: "number", def:100, min: 0, max: 100, title: "Button brightness – active state (%)",
     descr: "Configure the brightness of the buttons, in active state", scale: 1],

    [key: "DimDplyBright", num: 20, size: 1, type: "number", def:50, min: 0, max: 100, title: "Display brightness – dimmed state (%)",
     descr: "Configure the brightness of the display, in dimmed state", scale: 1],

    [key: "ActDplyBright", num: 21, size: 1, type: "number", def:100, min: 0, max: 100, title: "Display brightness – active state (%)",
     descr: "Configure the brightness of the display, in active state", scale: 1],

    [key: "TmpReportIntvl", num: 22, size: 2, type: "number", def:60, min: 0, max: 32767, title: "Temperature report interval (seconds)",
     descr: "Time interval between consecutive temperature reports. Temperature reports can be also sent as a result of polling", scale: 1],

    [key: "TempReportHyst", num: 23, size: 1, type: "number", def:1.0, min: 0.1, max: 10.0, title: "Temperature report hysteresis (0.1°..10°)",
     descr: "The temperature report will be sent if there is a difference in temperature value from the previous value reported, defined in this parameter (hysteresis). Temperature reports can be also sent as a result of polling", scale: 10],

    [key: "MeterReportInterval", num: 24, size: 2, type: "number", def:60, min: 0, max: 32767, title: "Meter report interval (seconds)",
     descr: "Time interval between consecutive meter reports. Meter reports can be also sent as a result of polling.", scale: 1],

    [key: "MeterReportDeltaValue", num: 25, size: 1, type: "number", def:10, min: 0, max: 255, title: "Meter report delta value",
     descr: "Delta value in kWh between consecutive meter reports. Meter reports can be also sent as a result of polling.", scale: 1],

]}

