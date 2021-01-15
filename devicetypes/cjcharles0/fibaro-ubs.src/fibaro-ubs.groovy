/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro UBS - Dual Contact and Temperature Sensor
 *  File Name:			Fibaro UBS - Dual Contact and Temperature Sensor.groovy
 *	Initial Release:	2017-11-07
 *	Author:				Chris Charles
 *
 *  Copyright 2017 Chris Charles, based on original code by carlos.ir33, modified
 *  by Stuart Buchanan and Paul Crookes. Testing thanks to borristhecat.
 *
 ***************************************************************************************
 */
 
metadata {
	definition (name: "Fibaro UBS", namespace: "cjcharles0", author: "Chris Charles") {
    
    capability "Contact Sensor"
 	capability "Motion Sensor"
    capability "Sensor"
	capability "Temperature Measurement"
    capability "Configuration"
    
    command "removeChildDevices"
    command "createChildDevices"
    command "createChildTempDevices"
    command "updateCurrentParams"
    command "listCurrentParams"
    command "open1"
    command "open2"
    command "close1"
    command "close2"
    
    attribute "contact1","enum",["open1","close1"]
    attribute "contact2","enum",["open2","close2"]
	
	fingerprint type: "2001", cc: "30 60 85 8E 72 70 86 7A", ccOut: "2B"
}

simulator {
}

tiles(scale: 2) {
	standardTile("contact1", "device.contact1", width: 3, height: 2) {
		state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13", action: "close1"
		state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc", action: "open1"
	}
	standardTile("contact2", "device.contact2", width: 3, height: 2) {
		state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13", action: "close2"
		state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc", action: "open2"
	}
	
	standardTile("temp1text", "temp1text", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Temp 1:', action:"", icon:""
	}
	standardTile("temp2text", "temp2text", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Temp 2:', action:"", icon:""
	}
	standardTile("temp3text", "temp3text", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Temp 3:', action:"", icon:""
	}
	standardTile("temp4text", "temp4text", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Temp 4:', action:"", icon:""
	}
	valueTile("temperature1", "temperature1", width:1, height:1, decoration: "flat") {
		state "default", label:'${currentValue}°', unit:"C", backgroundColor:"#fab907"//, icon:"st.Weather.weather2"
    }
	valueTile("temperature2", "temperature2", width:1, height:1, decoration: "flat") {
		state "default", label:'${currentValue}°', unit:"C", backgroundColor:"#fab907"//, icon:"st.Weather.weather2"
    }
	valueTile("temperature3", "temperature3", width:1, height:1, decoration: "flat") {
		state "default", label:'${currentValue}°', unit:"C", backgroundColor:"#fab907"//, icon:"st.Weather.weather2"
    }
    valueTile("temperature4", "temperature4", width:1, height:1, decoration: "flat") {
		state "default", label:'${currentValue}°', unit:"C", backgroundColor:"#fab907"//, icon:"st.Weather.weather2"
    }

	standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
		state "default", label:'Send Config', action:"updateCurrentParams"//, icon:"st.secondary.configure"
	}
	standardTile("report", "device.report", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
		state "default", label:'List Config', action:"listCurrentParams"
	}
    
    standardTile("createchildren", "createchildren", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Create Contact Children', action:"createChildDevices"
	}
    standardTile("createtempchildren", "createtempchildren", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Create Temperature Children', action:"createChildTempDevices"
	}
    standardTile("removechildren", "removechildren", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
		state "default", label:'Remove Child Devices', action:"removeChildDevices"
	}
}

main(["contact1"]) //, "temperature1"
details(["contact1","contact2",
		"temp1text", "temperature1", "temp2text", "temperature2",
        "temp3text", "temperature3", "temp4text", "temperature4",
        "configure", "report", "createchildren", "createtempchildren", "removechildren"])

preferences {
        input name: "Info", type: "paragraph", title:"Device Handler by @cjcharles", description: "Parameter Settings:", displayDuringSetup: false

        input name: "removechildonsave", type: "bool", required: true, //defaultValue: "0",
            title: "Attempt to remove child devices on saving preferences. \n" +
                   "False = attempt create children on save (default).\n"
		   
        input name: "Info", type: "paragraph", title:"Device Handler by @cjcharles", description: "Parameter Settings:", displayDuringSetup: false

        input name: "param1", type: "number", range: "0..65535", required: true, //defaultValue: "0",
            title: "Parameter No. 1 - Input 1 Alarm Cancellation Delay. \n" +
                   "Additional delay after an alarm from input 1 has ceased.\n" +
                   "Time in seconds to delay the ceasing event.\n" +
                   "Default value: 0."
       
        input name: "param2", type: "number", range: "0..65535", required: true, //defaultValue: "0",
            title: "Parameter No. 2 - Input 2 Alarm Cancellation Delay. \n" +
                   "Additional delay after an alarm from input 2 has ceased.\n" +
                   "Time in seconds to delay the ceasing event.\n" +
                   "Default value: 0."
       
        input name: "param3", type: "number", range: "0..3", required: true, //defaultValue: "1",
            title: "Parameter No. 3 - Type of Input No 1." +
                   "Available settings:\n" +
                   "0 – INPUT_NO (Normal Open)\n" +
				   "1 – INPUT_NC (Normal Close)\n" +
				   "2 – INPUT_MONOSTABLE\n" +
				   "3 – INPUT_BISTABLE\n" +
                   "Default value: 1."

		input name: "param4", type: "number", range: "0..3", required: true, //defaultValue: "1",
            title: "Parameter No. 4 - Type of Input No 2." +
                   "Available settings:\n" +
                   "0 – INPUT_NO (Normal Open)\n" +
				   "1 – INPUT_NC (Normal Close)\n" +
				   "2 – INPUT_MONOSTABLE\n" +
				   "3 – INPUT_BISTABLE\n" +
                   "Default value: 1."

		input name: "param5", type: "number", range: "0..255", required: true, //defaultValue: "255",
            title: "Parameter No. 5 - Type of transmitted control or alarm frame for association group 1." +
                   "Available settings:\n" +
				   "0 – Frame ALARM GENERIC\n" +
				   "1 – Frame ALARM SMOKE\n" +
				   "2 – Frame ALARM CO\n" +
				   "3 – Frame ALARM CO2\n" +
				   "4 – Frame ALARM HEAT\n" +
				   "5 – Frame ALARM WATER\n" +
				   "255 – Control frame BASIC_SET\n" +
                   "Default value: 255."

		input name: "param6", type: "number", range: "0..255", required: true, //defaultValue: "255",
            title: "Parameter No. 6 - Type of transmitted control or alarm frame for association group 2." +
                   "Available settings:\n" +
				   "0 – Frame ALARM GENERIC\n" +
				   "1 – Frame ALARM SMOKE\n" +
				   "2 – Frame ALARM CO\n" +
				   "3 – Frame ALARM CO2\n" +
				   "4 – Frame ALARM HEAT\n" +
				   "5 – Frame ALARM WATER\n" +
				   "255 – Control frame BASIC_SET\n" +
                   "Default value: 255."

		input name: "param7", type: "number", range: "0..255", required: true, //defaultValue: "255",
            title: "Parameter No. 7 - Value of the parameter specifying the forced level of dimming / opening sun blinds when " +
            	   "sent a “switch on” / ”open” command from association group no. 1.\n" +
                   "Available settings:\n" +
                   "0-99 - Dimming or Opening Percentage\n" +
                   "255 - Last set percentage\n" +
                   "Default value: 255."

		input name: "param8", type: "number", range: "0..255", required: true, //defaultValue: "255",
            title: "Parameter No. 8 - Value of the parameter specifying the forced level of dimming / opening sun blinds when " +
            	   "sent a “switch on” / ”open” command from association group no. 2.\n" +
                   "Available settings:\n" +
                   "0-99 - Dimming or Opening Percentage\n" +
                   "255 - Last set percentage\n" +
                   "Default value: 255."

		input name: "param9", type: "number", range: "0..3", required: true, //defaultValue: "0",
            title: "Parameter No. 9 - Deactivating transmission of the frame cancelling the alarm or the " +
				   "control frame deactivating the device (Basic). Disable the alarm cancellation function.\n" +
                   "Available settings:\n" +
                   "0 – Cancellation sent for association group 1 and 2\n" +
				   "1 – Cancellation sent for association group 1 only\n" +
				   "2 – Cancellation sent for association group 2 only\n" +
				   "3 - Not sent for association group 1 or 2\n" +
                   "Default value: 0."

		input name: "param10", type: "number", range: "1..255", required: true, //defaultValue: "20",
            title: "Parameter No. 10 - Interval between successive readings of temperature from all " +
				   "sensors connected to the device. (A reading does not result in sending to ST)\n" +
                   "Available settings:\n" +
                   "1-255 - Seconds between readings\n" +
                   "Default value: 20."

		input name: "param11", type: "number", range: "0..255", required: true, //defaultValue: "200",
            title: "Parameter No. 11 - Interval between forcing to send report of the temperature. " +
				   "The forced report is sent immediately after the next temperature reading, " +
				   "irrespective of parameter 12. Advised to be 200 unless rapid temperature changes are expected.\n" +
                   "Available settings:\n" +
                   "0 - Deactivate temperature sending\n" +
                   "1-255 - Seconds between sends\n" +
                   "Default value: 200."

		input name: "param12", type: "number", range: "0..255", required: true, //defaultValue: "8",
            title: "Parameter No. 12 - Insensitiveness to temperature changes. This is the maximum " +
				   "difference between the last reported temperature and the current temperature. " +
				   "If they differ by more than this then a report is sent.\n" +
                   "Available settings:\n" +
                   "0-255 - x/16 = temp diff in C\n" +
                   "x/80*9 = temp diff in F\n" +
                   "Default value: 8 (0.5oC)."

		input name: "param13", type: "number", range: "0..3", required: true, //defaultValue: "0",
            title: "Parameter No. 13 - Transmitting the alarm or control frame in “broadcast” mode (i.e. to " +
				   "all devices within range), this information is not repeated by the mesh network." +
                   "Available settings:\n" +
                   "0 - IN1 and IN2 broadcast inactive,\n" +
                   "1 - IN1 broadcast mode active only,\n" +
                   "2 - IN2 broadcast mode active only,\n" +
                   "3 - INI and IN2 broadcast mode active.\n" +
                   "Default value: 0."

		input name: "param14", type: "number", range: "0..1", required: true, //defaultValue: "0",
            title: "Parameter No. 14 - Scene activation functionality." +
                   "Available settings:\n" +
                   "0 - Deactivated functionality,\n" +
                   "1 - Activated functionality.\n" +
                   "Default value: 0."
	}
}

def installed() {
	log.debug "installed()"
}
def updated() {
	log.debug "updated()"
	log.debug "Dont forget to press the button to send config to the device"
    //configure()
    if (removechildonsave == false) {
        removeChildDevices()
    }
    else {
        createChildDevices()
	createChildTempDevices()
    }
}
def uninstalled() {
    log.debug "uninstalled()"
    removeChildDevices()
}

def configure() {
	log.debug "configure()"
    updateCurrentParams()
}

def createChildDevices(){
	log.debug "Adding Child Devices if not already added"
    for (i in 1..2) {
    	try {
        	log.debug "Trying to create child switch if it doesn't already exist ${i}"
            def currentchild = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep${i}"}
            if (currentchild == null) {
            	log.debug "Creating child for ep${i}"
				addChildDevice("smartthings", "Open/Closed Sensor", "${device.deviceNetworkId}-ep${i}", device.hub.id,
                	[completedSetup: true, name: "${device.displayName} (Contact${i})", isComponent: false]) //, label: "${device.displayName} (Contact${i})"
                /*addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${deviceName}${deviceNumber}", null,
         			[completedSetup: true, label: "${device.displayName} (${deviceName}${deviceNumber})", 
                	isComponent: false, componentName: "${deviceName}${deviceNumber}", componentLabel: "${deviceName} ${deviceNumber}"])*/
            }
        } catch (e) {
            log.debug "Error adding child ${i}: ${e}"
        }
    }
}
def createChildTempDevices() {
    log.debug "Creating Temperature children"
    for (i in 1..4) {
    	try {
        	//If we have a temperature reading from this sensor (1 to 4) then try to create a child for it
        	if (device.currentValue("temperature${i}") != null) {
            	log.debug "Have received temperature readings for termperature${i} so creating a child for it if not already there"
                def currentchild = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-temperature${i}"}
                if (currentchild == null) {
                    addChildDevice("smartthings", "Temperature Sensor", "${device.deviceNetworkId}-temperature${i}", device.hub.id,
                        [completedSetup: true, name: "${device.displayName} (Temp${i})", isComponent: false]) //, label: "${device.displayName} (Temp${i})"
                }
            }
            else {
            	log.debug "No temperature received for temperature${i} so no child will be created" 
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
            } catch (e) {
                log.debug "Error deleting ${it.deviceNetworkId}, probably locked into a SmartApp: ${e}"
            }
        }
    } catch (err) {
        log.debug "Either no children exist or error finding child devices for some reason: ${err}"
    }
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [ 0x60: 3])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "parsed '$description' to result: ${result}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
	log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug("ConfigurationReport ${cmd.inspect()}")
}

def createEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, Map item1) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"

}

def createEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd, Map item1) {	
    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet V1 ${cmd.inspect()}"
    def currentstate
    def motionstate
	if (cmd.value) {
		currentstate = "open"
        motionstate = "inactive"
	} else {
    	currentstate = "closed"
        motionstate = "active"
	}
    createEvent(name: "contact1", value: currentstate, descriptionText: "${device.displayName} is ${currentstate}")
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep1"}
        if (childDevice)
        	childDevice.sendEvent(name: "motion", value: motionstate)
            childDevice.sendEvent(name: "contact", value: currentstate)
            log.debug "Fibaro is ${motionstate} and ${currentstate}"
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "ZWaveEvent V3 ${cmd.inspect()}"
	def result
	if (cmd.commandClass == 32) {
        def currentstate
        def motionstate
		if (cmd.parameter == [0]) {
        	currentstate = "closed"
            motionstate = "active"
		}
		if (cmd.parameter == [255]) {
        	currentstate = "open"
            motionstate = "inactive"
		}
        log.debug "ep${cmd.sourceEndPoint} is ${currentstate}"
        //First update tile on this device
        sendEvent(name: "contact${cmd.sourceEndPoint}", value: currentstate, descriptionText: "$device.displayName - ep${cmd.sourceEndPoint} is ${currentstate}")
		//If not null then we have found either ep1 or ep2, hence try to send to the child device aswell
        try {
            def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep${cmd.sourceEndPoint}"}
            if (childDevice)
                childDevice.sendEvent(name: "motion", value: motionstate)
                childDevice.sendEvent(name: "contact", value: currentstate)
        } catch (e) {
            log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
        }
    }
	else if (cmd.commandClass == 49) {
		if ((cmd.sourceEndPoint >= 3) && (cmd.sourceEndPoint <= 6)) {
            
			def tempsensorid = cmd.sourceEndPoint - 2
			def tempendpoint = "temperature" + tempsensorid.toString()
			
            def tempval = ((cmd.parameter[4] * 256) + cmd.parameter[5])
            if (tempval > 32767) {
            	//Here we deal with negative values
            	tempval = tempval - 65536
            }
            //Finally round the temperature
            def tempprocessed = (tempval / 100).toDouble().round(1)
            
			//def cmdScale = cmd.scale == 1 ? "F" : "C"
			//def tempval = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision).toDouble().round(1)
            
            log.debug "${tempendpoint} has changed to ${tempprocessed}"
            
            sendEvent(name: tempendpoint, value: tempprocessed, displayed: true) //unit: getTemperatureScale()
            
			//If not null then we have found either contact1 or contact2, hence try to send to the child
            try {
                def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-${tempendpoint}"}
                if (childDevice)
                	//We found a child device that matches so send it the new temperature
                    childDevice.sendEvent(name: "temperature", value: tempprocessed)
            } catch (e) {
            	//Not an error message here as people may not want child temperature devices
                log.debug "Couldn't find child ${tempendpoint} device, probably doesn't exist...? Error: ${e}"
            }
        }
	}
	else {
		//Send them here just in case we want to do more complicated processing (not doing it as need to have endpoint passed and that makes it a bit messy)
		def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 2, 0x60: 3, 0x85: 2, 0x8E: 2, 0x72: 1, 0x70: 1, 0x86: 1, 0x7A: 1, 0xEF: 1, 0x2B: 1]) // can specify command class versions here like in zwave.parse
		log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
		if (encapsulatedCommand) {
			result = zwaveEvent(encapsulatedCommand)
		}
	}
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	//This is no longer used as caught in an earlier event, but kept here in case the code is useful
	log.debug "Sensor MultiLevel Report - Sensor Type = ${cmd.sensorType}"
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			def tempval = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision).toDouble().round(1)
            sendEvent(name: "temperature1", value: tempval, displayed: false) //unit: getTemperatureScale()
			break;
	}
    log.debug map
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// This will capture any commands not handled by other instances of zwaveEvent
	// and is recommended for development so you can see every command the device sends
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def updateCurrentParams() {
	log.debug "Sending configuration parameters to device"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, configurationValue:[param1.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, configurationValue:[param2.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue:[param3.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 4, configurationValue:[param4.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, configurationValue:[param5.value]).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 6, configurationValue:[param6.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 7, configurationValue:[param7.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, configurationValue:[param8.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 9, configurationValue:[param9.value]).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, configurationValue:[param10.value]).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, configurationValue:[param11.value]).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 12, configurationValue:[param12.value]).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 13, configurationValue:[param13.value]).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, configurationValue:[param14.value]).format()
	delayBetween(cmds, 500)
}
def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:2).format()
	cmds << zwave.associationV2.associationGet(groupingIdentifier: 3).format()
	cmds << zwave.associationV1.associationGet(groupingIdentifier: 1).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
   	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
   	cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
   	cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
	delayBetween(cmds, 500)
}

def open1() {
    sendEvent(name: "contact1", value: "open", descriptionText: "$device.displayName (1) is opened manually")
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep1"}
        log.debug "Changing child ${childDevice} to open/inactive"
        if (childDevice)
        	childDevice.sendEvent(name: "motion", value: "inactive")
            childDevice.sendEvent(name: "contact", value: "open")
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
    }
}

def close1() {
    sendEvent(name: "contact1", value: "closed", descriptionText: "$device.displayName (1) is closed manually")
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep1"}
        log.debug "Changing child ${childDevice} to closed/active"
        if (childDevice)
        	childDevice.sendEvent(name: "motion", value: "active")
            childDevice.sendEvent(name: "contact", value: "closed")
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
    }
}

def open2() {
    sendEvent(name: "contact2", value: "open", descriptionText: "$device.displayName (2) is opened manually")
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep2"}
        log.debug "Changing child ${childDevice} to open/inactive"
        if (childDevice)
        	childDevice.sendEvent(name: "motion", value: "inactive")
            childDevice.sendEvent(name: "contact", value: "open")
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
    }
}

def close2() {
    sendEvent(name: "contact2", value: "closed", descriptionText: "$device.displayName (2) is closed manually")
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-ep2"}
        log.debug "Changing child ${childDevice} to closed/active"
        if (childDevice)
        	childDevice.sendEvent(name: "motion", value: "active")
			childDevice.sendEvent(name: "contact", value: "closed")
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist...? Error: ${e}"
    }
}