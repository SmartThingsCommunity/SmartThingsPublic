/**
 *  Device Handler for Fibaro Roller Shutter 3 (FGR-223)
 *
 *  Based on the FGR-222 handler by Julien Bachmann
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
 */

metadata {
    definition (name: "Fibaro FGR-223", namespace: "philh30", author: "philh30", ocfDeviceType: "oic.d.blind", vid: "generic-shade") {
        capability "Sensor"
        capability "Contact Sensor"
        capability "Actuator"

        capability "Switch"
        capability "Switch Level"
        capability "Window Shade"

        capability "Polling"
        capability "Power Meter"
        capability "Energy Meter"
        capability "Refresh"
        capability "Configuration"

        attribute "syncStatus", "enum", ["syncing", "synced"]

        command "sync"
        command "stop"        
        command "up"   
        command "down"   

        fingerprint mfr:"010F", prod:"0303", model:"1000", deviceJoinName: "Fibaro Roller Shutter 3"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"mainTitle", type:"generic", width:6, height:4, canChangeIcon: true) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'Open', backgroundColor:"#ffa81e", action: "close", nextState: "closing"
                attributeState "partially open", label:'Partial', backgroundColor:"#d45614", action: "open", nextState: "opening"
                attributeState "closed", label:'Closed', backgroundColor:"#00a0dc", action: "open", nextState: "opening"
                attributeState "opening", label:'Opening', backgroundColor:"#ffa81e", action: "stop", nextState: "partially open"
                attributeState "closing", label:'Closing', backgroundColor:"#00a0dc", action: "stop", nextState: "partially open"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel", defaultState: true, icon:"st.Home.home9"
            }
        }
        valueTile("power", "device.power", width: 2, height: 2) {
            state "default", label:'${currentValue} W'
        }
        valueTile("energy", "device.energy", width: 2, height: 2) {
            state "default", label:'${currentValue} kWh'
        }
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"Refresh", action:"refresh", icon:"st.secondary.refresh-icon"
        }
        standardTile(name: "calibrate", width: 2, height: 2, decoration: "flat") {
            state "default", action:"configure", label:"Calibrate", backgroundColor:"#0000a8"
        }
        standardTile(name: "up", width: 2, height: 2, decoration: "flat") {
            state "default", action:"up", icon:"https://raw.githubusercontent.com/julienbachmann/smartthings/master/fibaro_fgr_222/up.png?v=3"
        }
        standardTile(name: "down", width: 2, height: 2, decoration: "flat") {
            state "default", action:"down", icon:"https://raw.githubusercontent.com/julienbachmann/smartthings/master/fibaro_fgr_222/down.png?v=3"
        }
        standardTile("sync", "device.syncStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", action:"sync" , label:"Sync", backgroundColor:"#00a800"
            state "synced", action:"sync" , label:"Sync", backgroundColor:"#00a800"
            state "syncing" , label:"Syncing", backgroundColor:"#a8a800"
        }
        main(["mainTitle"])
        details(["mainTitle", "up", "power", "refresh", "down", "sync", "calibrate"])
    }

    preferences {
        input name: "invert", type: "bool", title: "Invert up/down", description: "Invert up and down actions"
        input name: "openOffset", type: "decimal", title: "Open offset", description: "The percentage from which shutter is displayed as open"
        input name: "closeOffset", type: "decimal", title: "Close offset", description: "The percentage from which shutter is displayed as closed"
        input name: "offset", type: "decimal", title: "offset", description: "This offset will correct the value returned by the device so it matches the real value"

        section {
            input (
                type: "paragraph",
                element: "paragraph",
                title: "DEVICE PARAMETERS:",
                description: "Device parameters are used to customize the physical device. " +
            "Refer to the product documentation for a full description of each parameter."
        )

            getParamsMd().findAll( {!it.readonly} ).each { // Exclude readonly parameters.

                def lb = (it.description.length() > 0) ? "\n" : ""

                switch(it.type) {
                    case "number":
                        input (
                            name: "configParam${it.id}",
                        title: "#${it.id}: ${it.name}: \n" + it.description + lb +"Default Value: ${it.defaultValue}",
                        type: it.type,
                        range: it.range,
                        required: it.required
                    )
                        break

                    case "enum":
                        input (
                            name: "configParam${it.id}",
                        title: "#${it.id}: ${it.name}: \n" + it.description + lb + "Default Value: ${it.defaultValue}",
                        type: it.type,
                        options: it.options,
                        required: it.required
                    )
                        break
                }
            }
        } // section
    }
}

def parse(String description) {
    log.debug("New event to parse: ${description}")
    def result = null
    def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
    if (cmd) {
    	log.debug "Parsed event: ${cmd}"
        result = zwaveEvent(cmd)
        if (result) {
            log.debug("Dispatch events ${result}")
        }
    } else {
        log.debug("Couldn't zwave.parse ${description}")
    }
    result
}

def correctLevel(value) {
    def result = value
    if (value == "off") {
        result = 0;
    }
    if (value == "on" ) {
        result = 100;
    }
    result = result - (offset ?: 0)
    if (invert) {
        result = 100 - result
    }
    return result
}

def createWindowShadeEvent(value) {
    def theWindowShade = "partially open"
    def contactValue = "open"
    def switchValue = "on"
    if (value >= (openOffset ?: 95)) {
        theWindowShade = "open"
        contactValue = "open"
        switchValue = "on"
    }
    if (value <= (closeOffset ?: 5)) {
        theWindowShade = "closed"
        contactValue = "closed"
        switchValue = "off"
    }
	log.debug "Updating windowShade state to ${theWindowShade}"
	sendEvent(name: "windowShade", value: theWindowShade)
	log.debug "Updating contactSensor state to ${contactValue}"
	sendEvent(name: "contact", value: contactValue, displayed: false)
	log.debug "Updating switch state to ${switchValue}"
    return createEvent(name: "switch", value: switchValue, displayed: false)
}

def createSwitchEvent(value) {
    def switchValue = "on"
    if (value >= (openOffset ?: 95)) {
        switchValue = "on"
    }
    if (value <= (closeOffset ?: 5)) {
        switchValue = "off"
    }
    return createEvent(name: "switch", value: switchValue)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
	state.sec = 1
    log.debug "Secure message parsed: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug("Basic report: ${cmd.value}; Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    def result = []
    if (cmd.value != null) {
        def level = correctLevel(cmd.value)
        sendEvent(name: "level", value: level, unit: "%")  
        if (device.currentValue('windowShade') == "opening" || device.currentValue('windowShade') == "closing") {
        	result << response([secure(zwave.meterV2.meterGet(scale: 2))])
        }
    }
    //log.debug("Basic result: ${result}")
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    log.debug("Switch multilevel report: ${cmd.value}; Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    def result = []
    if (cmd.value != null) {
        if(cmd.value > 100) {
        	log.error "${device.displayName} requires calibration. Position of ${cmd.value}% reported."
        }
        def level = correctLevel(cmd.value)
        sendEvent(name: "level", value: level, unit: "%")   
        if (device.currentValue('windowShade') == "opening" || device.currentValue('windowShade') == "closing") {
        	result << response([secure(zwave.meterV2.meterGet(scale: 2))])
        }
        else {
            result << createWindowShadeEvent(level) 
        }
    }
    //log.debug("switch result ${result}")
    return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug("other event ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    log.debug "Meter report: ${cmd.meterType} ${cmd.scale} ${cmd.scaledMeterValue}; Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}"
    if (cmd.meterType == 1) {
        if (cmd.scale == 2) {
            def result = []
            result << createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
            if (cmd.scaledMeterValue < 1.0) {
              result << createWindowShadeEvent(device.currentValue('level'))
              result << response(["delay 500", secure(zwave.switchMultilevelV3.switchMultilevelGet())])
            }
            else {
              result << response(["delay 2000", secure(zwave.switchMultilevelV3.switchMultilevelGet())])
            }
            log.debug("power result ${result}")
            return result
        } else {
            return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug("zwaveEvent(): Configuration Report received: ${cmd}")
	if (cmd.parameterNumber==150) {
		try {
			device.updateSetting("configParam150",cmd.scaledConfigurationValue)
            if (cmd.scaledConfigurationValue==0) log.debug "Calibration report received - device not calibrated. Updating parameter ${cmd.parameterNumber} from ${configParam150} to ${cmd.scaledConfigurationValue}."
            if (cmd.scaledConfigurationValue==1) log.debug "Calibration report received - device is calibrated. Updating parameter ${cmd.parameterNumber} from ${configParam150} to ${cmd.scaledConfigurationValue}."
            if (cmd.scaledConfigurationValue==2) log.debug "Calibration report received - device is calibrating. Updating parameter ${cmd.parameterNumber} from ${configParam150} to ${cmd.scaledConfigurationValue}."
        } catch (e) {
        	if (cmd.scaledConfigurationValue==0) log.debug "Calibration report received - device not calibrated."
            if (cmd.scaledConfigurationValue==1) log.debug "Calibration report received - device is calibrated."
            if (cmd.scaledConfigurationValue==2) log.debug "Calibration report received - device is calibrating."
        }
	}
}

def updated() {
    sync();
}

def on() {
    open()
}

def off() {
    close()
}

def stop() {
    log.debug "Stop - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}"
	secureSequence([
    	zwave.switchMultilevelV3.switchMultilevelStopLevelChange(),
    	zwave.switchMultilevelV3.switchMultilevelGet()
    ], 2000)
}

def up() {
	log.debug("Up - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    def currentWindowShade = device.currentValue('windowShade')
    if (currentWindowShade == "opening" || currentWindowShade == "closing") {      
        return stop()        
    }
    return open()
}

def down() {
    log.debug("Down - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    def currentWindowShade = device.currentValue('windowShade')
    if (currentWindowShade == "opening" || currentWindowShade == "closing") {
        return stop()        
    }
    return close()
}

def open() {
    log.debug("Open - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    sendEvent(name: "windowShade", value: "opening")
    if (invert) {
        return privateClose()
    }
    else {
        return privateOpen()
    }
}

def close() {
    log.debug("Close - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    sendEvent(name: "windowShade", value: "closing")    
    if (invert) {
        return privateOpen()
    }
    else {
        return privateClose()
    }
}

def privateOpen() {
    secureSequence([
    	//zwave.basicV1.basicSet(value: 0xFF),
        zwave.switchMultilevelV3.switchMultilevelSet(value: 99, dimmingDuration: 0x00),
    	zwave.switchMultilevelV3.switchMultilevelGet()
    ], 2000)
}

def privateClose() {
    secureSequence([
        //zwave.basicV1.basicSet(value: 0),
        zwave.switchMultilevelV3.switchMultilevelSet(value: 0, dimmingDuration: 0x00),
        zwave.switchMultilevelV3.switchMultilevelGet()
    ], 2000)
}

def presetPosition() {
    setLevel(50)
}

def poll() {
    secureSequence([
        zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2)
	], 1000)
}

def refresh() {
    log.debug("Refresh - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    secureSequence([
        zwave.switchMultilevelV3.switchMultilevelGet(),
        zwave.meterV2.meterGet(scale: 2)
	], 500)
}

def setLevel(level) {
    if (invert) {
        level = 100 - level
    }
    if(level > 99) level = 99
    if (level <= (openOffset ?: 95) && level >= (closeOffset ?: 5)) {
        level = level - (offset ?: 0)
    }

    log.debug("Set level ${level} - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    secureSequence([
        //zwave.basicV1.basicSet(value: level),
        zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00),
        zwave.switchMultilevelV3.switchMultilevelGet()
	], 10000)
}

def setLevel(String strLevel) {
    def level = strLevel.toInteger()
    if (invert) {
        level = 100 - level
    }
    if(level > 99) level = 99
    if (level <= (openOffset ?: 95) && level >= (closeOffset ?: 5)) {
        level = level - (offset ?: 0)
    }

    log.debug("Set level ${level} s - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    secureSequence([
        //zwave.basicV1.basicSet(value: level),
        zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00),
        zwave.switchMultilevelV3.switchMultilevelGet()
	], 10000)
}

def configure() {
    log.debug("Configure roller shutter - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    secureSequence([
        zwave.configurationV1.configurationSet(parameterNumber: 150, size: 1, scaledConfigurationValue: 2),  // start calibration
        zwave.switchMultilevelV3.switchMultilevelGet(),
        zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2),
	], 500)
}

def sync() {
    log.debug("Sync roller shutter - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    def cmds = []
    sendEvent(name: "syncStatus", value: "syncing", isStateChange: true)
    getParamsMd().findAll( {!it.readonly} ).each { // Exclude readonly parameters.
        if (settings."configParam${it.id}" != null) {
            cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: it.id, size: it.size, scaledConfigurationValue: settings."configParam${it.id}".toInteger()))
            cmds << secure(zwave.configurationV1.configurationGet(parameterNumber: it.id))
        }
    }
    if(cmds) {
    	log.debug("Send configuration parameters ${cmds}")
    	runIn(0.5 * cmds.size(), setSynced)
    	delayBetween(cmds, 500)
    } else {
    	log.debug "No configuration parameters set"
    }
}

def setSynced() {
    log.debug("Synced - Shade state: ${device.currentValue('windowShade')}; Shade level: ${device.currentValue('level')}")
    sendEvent(name: "syncStatus", value: "synced", isStateChange: true)
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private secureSequence(Collection commands, ...delayBetweenArgs) {
    delayBetween(commands.collect{ secure(it) }, *delayBetweenArgs)
}

private getParamsMd() {
    return [
    [id:  20, size: 1, type: "number", range: "0..2", defaultValue: 2, required: false, readonly: false,
        name: "Switch type",
        description: "This parameter defines how the device should treat the switch connected to the S1 and S2 terminals.\n" +
        "If parameter 20 is set to 1 (toggle switch), change value of parameter 153 to 0 for slats to work properly.\n" +
        "0 - momentary switches.\n" +
        "1 - toggle switches.\n" +
        "2 - single, momentary switch (the switch should be connected to S1 terminal)." ],
    [id:  24, size: 1, type: "number", range: "0..1", defaultValue: 0, required: false, readonly: false,
        name: "Inputs orientation",
        description: "This parameter allows reversing the operation of switches connected to S1 and S2 without changing the wiring.\n" +
        "0 - default (S1 - 1st channel, S2 - 2nd channel)\n" +
        "1 - reversed (S1 - 2nd channel, S2 - 1st channel)\n" ],
    [id: 25, size:1, type: "number", range: "0..1", defaultValue: 0, required: false, readonly: false,
        name: "Outputs orientation",
        description: "This parameter allows reversing the operation of Q1 and Q2 without changing the wiring (in case of invalid motor connection) to ensure proper operation.\n" +
        "0 - default (Q1 - 1st channel, Q2 - 2nd channel)\n" +
        "1 - reversed (Q1 - 2nd channel, Q2 - 1st channel)\n" ],
    [id: 60, size:1, type: "number", range: "0..1", defaultValue: 0, required: false, readonly: false,
        name: "Measuring power consumed by the device itself",
        description: "This parameter determines whether the power metering should include the amount of active power consumed by the device itself.\n" +
        "0 - function inactive\n" +
        "1 - function active"],
    [id: 61, size:2, type: "number", range: "0..500", defaultValue: 15, required: false, readonly: false,
        name: "Power reports - on change",
        description: "This parameter determines the minimum change in consumed power that will result in sending new power report to the main controller. For loads under 50W, the parameter is not relevant and reports are sent every 5W change. Power report are sent no often then every 30 seconds.\n" +
        "0 - reports are disabled\n" +
        "1-500 -  (1-500%) - change in power"],
    [id: 62, size:2, type: "number", range: "0..32400", defaultValue: 3600, required: false, readonly: false,
        name: "Power reports - periodic",
        description: "This parameter determines in what time intervals the periodic power reports are sent to the main controller. Periodic reports do not depend on power change (parameter 61).\n" +
        "0 - periodic reports are disabled\n" +
        "30-32400 - (30-32400s) - report interval"],
    [id: 65, size:2, type: "number", range: "0..500", defaultValue: 10, required: false, readonly: false,
        name: "Energy reports - on change",
        description: "This parameter determines the minimum change in consumed energy that will result in sending new energy report to the main controller.\n" +
        "0 - reports are disabled\n" +
        "1-500 - (0.01 - 5 kWh) - change in energy"],
    [id: 66, size:2, type: "number", range: "0..32400", defaultValue: 3600, required: false, readonly: false,
        name: "Energy reports - periodic",
        description: "This parameter determines in what time intervals the periodic energy reports are sent to the main controller. Periodic reports do not depend on energy change (parameter 65).\n" +
        "0 - periodic reports are disabled.\n" +
        "30-32400 - (30-32400s) - report interval"],
    [id: 150, size:1, type: "number", range: "0..2", defaultValue: 0, required: false, readonly: false,
        name: "Force calibration",
        description: "By setting this parameter to 2 the device enters the calibration mode. The parameter relevant only if the device is set to work in positioning mode (parameter 151 set to 1, 2 or 4).\n" +
        "0 - device is not calibrated\n" +
        "1 - device is calibrated\n" +
        "2 - force device calibration"],
    [id: 151, size:1, type: "number", range: "1..6", defaultValue: 1, required: false, readonly: false,
        name: "Operating mode",
        description: "This parameter allows adjusting operation according to the connected device\n" +
        "1 - roller blind (with positioning) \n" +
        "2 - Venetian blind (with positioning)\n" +
        "3 - gate (without positioning)\n" +
        "4 - gate (with positioning)\n" +
        "5 - roller blind with built-in driver\n" +
        "6 - roller blind with built-in driver (impulse)"],
    [id: 152, size:4, type: "number", range: "0..90000", defaultValue: 150, required: false, readonly: false,
        name: "Venetian blind - time of full turn of the slats ",
        description: "For Venetian blinds (parameter 151 set to 2) the parameter determines time of full turn cycle of the slats. For gates (parameter 151 set to 3 or 4) the parameter determines time after which open gate will start closing automatically (if set to 0, gate will not close). The parameter is irrelevant for other modes.\n" +
        "0-90000 - (0 - 900s, every 0.01s) - time of turn "],
    [id: 153, size:1, type: "number", range: "0..2", defaultValue: 1, required: false, readonly: false,
        name: "Set slats back to previous position",
        description: "For Venetian blinds (parameter 151 set to 2) the parameter determines slats positioning in various situations.\n" +
        "The parameter is irrelevant for other modes.\n" +
        "If parameter 20 is set to 1 (toggle switch), change value of parameter 153 to 0 for slats to work properly.\n" +
        "0 - slats return to previously set position only in case of the main controller operation.\n" +
        "1 - slats return to previously set position in case of the main controller operation, momentary switch operation, or when the limit switch is reached.\n" +
        "2 - slats return to previously set position in case of the main controller operation, momentary switch operation, when the limit switch is reached or after receiving the Switch Multilevel Stop control frame."],
    [id: 154, size:2, type: "number", range: "0..600", defaultValue: 10, required: false, readonly: false,
        name: "Delay motor stop after reaching end switch",
        description: "For blinds (parameter 151 set to 1, 2, 5 or 6) the parameter determines the time after which the motor will be stopped after end switch contacts are closed. For gates (parameter 151 set to 3 or 4) the parameter determines time after which the gate will start closing automatically if S2 contacts are  opened (if set to 0, gate will not close).\n" +
        "0-600 - (0 - 60s) - time"],
    [id: 155, size:2, type: "number", range: "0..255", defaultValue: 10, required: false, readonly: false,
        name: "Motor operation detection",
        description: "Power threshold to be interpreted as reaching a limit switch.\n" +
        "0 - reaching a limit switch will not be detected\n" +
        "1-255 - (1-255W) - report interval"],
    [id: 156, size:4, type: "number", range: "1..90000", defaultValue: 6000, required: false, readonly: false,
        name: "Time of up movement",
        description: "This parameter determines the time needed for roller blinds to reach the top. For modes with positioning value is set automatically during calibration, otherwise it must be set manually.\n" +
        "1-90000 - (0.01 - 900.00s, every 0.01s) - movement time"],
    [id: 157, size:4, type: "number", range: "1..90000", defaultValue: 6000, required: false, readonly: false,
        name: "Time of down movement",
        description: "This parameter determines time needed for roller blinds to reach the bottom. For modes with positioning value is set automatically during calibration, otherwise it must be set manually.\n" +
        "1-90000 - (0.01 - 900.00s, every 0.01s) - movement time"]
]
}