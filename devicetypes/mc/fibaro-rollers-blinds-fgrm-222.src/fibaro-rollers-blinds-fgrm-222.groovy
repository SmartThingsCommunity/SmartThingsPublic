/**
 *    fingerprint inClusters: "0x72,0x86,0x70,0x85,0x25,0x73,0x32,0x31,0x7a,0x25,0x91,0x75,0xef,0x32,0x31,0x91,0x2b,0x26,0x25"
 *
 * 0x72 V1 0x72 Manufacturer Specific
 * 0x86 V1 0x86 Version
 * 0x70 XX 0x70 Configuration
 * 0x85 V2 0x85 Association
 * 0x25 V1 0x25 Switch Binary
 * 0x73 V1 COMMAND_CLASS_POWERLEVEL 
 * 0x32 V2 0x32 Meter
 * 0x31 V2 0x31 Sensor Multilevel
 * 0x7a V1 COMMAND_CLASS_FIRMWARE_UPDATE_MD 
 * 0x25 V1 0x25 Switch Binary
 * 0x91 V1 COMMAND_CLASS_MANUFACTURER_PROPRIETARY 
 * 0x75 V2 COMMAND_CLASS_PROTECTION_V2 
 * 0xef XX COMMAND_CLASS_MARK 
 * 0x32 V2 0x32 Meter
 * 0x31 V2 0x31 Sensor Multilevel
 * 0x91 V1 COMMAND_CLASS_MANUFACTURER_PROPRIETARY 
 * 0x2b V1 COMMAND_CLASS_SCENE_ACTIVATION 
 * 0x26 V3 0x26 Switch Multilevel
 * 0x25 V1 0x25 Switch Binary
 * 0x20 V1 UNKNOWN
 *
 * References :
 *    https://graph.api.smartthings.com/ide/doc/zwave-utils.html
 *  http://www.pepper1.net/zwavedb/device/492
 * 
 *
 */
metadata {
    definition (name: "Fibaro Rollers Blinds FGRM-222", namespace: "MC", author: "MC") {
        capability "Actuator"
        capability "Switch Level"
        capability "Switch"
        capability "Door Control"
        capability "Contact Sensor"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"
        capability "Polling"

        
        
        
      fingerprint inClusters: "0x72,0x86,0x70,0x85,0x25,0x73,0x32,0x31,0x7A,0x25,0x91,0x75,0xEF,0x32,0x31,0x91,0x2B,0x26,0x25"
    
      command "open"
      command "stop"
      command "close"
      command "setposition"
    

    
    }
}

tiles {
        standardTile("open", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'open', action:"open", icon:"st.doors.garage.garage-open"
        }
        standardTile("stop", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'stop', action:"stop", icon:"st.doors.garage.garage-opening"
        }
        standardTile("close", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'close', action:"close", icon:"st.doors.garage.garage-closed"
        }
           controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"setposition"
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }


        details(["open", "stop", "close", "levelSliderControl", "refresh"])
    }

def parse(String description) {
    log.debug "Parsing '${description}'"

    def result = null
    def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x31: 2, 0x32: 2, 0x85: 2, 0x75: 2, 0x26: 3])
    log.debug "Parsed ${cmd}"

    if (cmd ) {
        result = createEvent(zwaveEvent(cmd))
        return result
    } else {
        log.debug "Non-parsed event: ${description}"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
    log.debug "Report SwitchMultilevelSet:${cmd}"

}

def init()
{
log.debug "oo"
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryGet cmd) {
    log.debug "Report SwitchBinaryGet:${cmd}"

}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "Report SwitchBinaryReport:${cmd}"
    
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    log.debug "Report SwitchMultilevelReport:${cmd}"

}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd) {
    log.debug "Report SwitchBinarySet:${cmd}"

}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    def value = "when off"
    if (cmd.configurationValue[0] == 1) {value = "when on"}
    if (cmd.configurationValue[0] == 2) {value = "never"}
    [name: "indicatorStatus", value: value, display: false]
}
 
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
def rcc = ""
rcc = Integer.toHexString(cmd.requestedCommandClass.toInteger()).toString() 

if (cmd.commandClassVersion > 0) {log.debug "0x${rcc}_V${cmd.commandClassVersion}"}
}


def refresh()
{
log.debug "refresh"
zwave.switchBinaryV1.switchBinaryGet().format()

}
def drefresh()
{
delayBetween([
new physicalgraph.device.HubAction("7202"),
new physicalgraph.device.HubAction("8602"),
new physicalgraph.device.HubAction("7002"),
new physicalgraph.device.HubAction("8502"),
new physicalgraph.device.HubAction("2502"),
new physicalgraph.device.HubAction("7302"),
new physicalgraph.device.HubAction("3202"),
new physicalgraph.device.HubAction("3102"),
new physicalgraph.device.HubAction("7A02"),
new physicalgraph.device.HubAction("9102"),
new physicalgraph.device.HubAction("7502"),
new physicalgraph.device.HubAction("EF02"),
new physicalgraph.device.HubAction("3202"),
new physicalgraph.device.HubAction("2B02"),
new physicalgraph.device.HubAction("2602"),
new physicalgraph.device.HubAction("2002"),

new physicalgraph.device.HubAction("7203"),
new physicalgraph.device.HubAction("8603"),
new physicalgraph.device.HubAction("7003"),
new physicalgraph.device.HubAction("8503"),
new physicalgraph.device.HubAction("2503"),
new physicalgraph.device.HubAction("7303"),
new physicalgraph.device.HubAction("3203"),
new physicalgraph.device.HubAction("3103"),
new physicalgraph.device.HubAction("7A03"),
new physicalgraph.device.HubAction("9103"),
new physicalgraph.device.HubAction("7503"),
new physicalgraph.device.HubAction("EF03"),
new physicalgraph.device.HubAction("3203"),
new physicalgraph.device.HubAction("2B03"),
new physicalgraph.device.HubAction("2603"),
new physicalgraph.device.HubAction("2003")

],500)
}

def arefresh() {
    delayBetween([
//zwave.switchMultilevelV3.switchMultilevelSupportedGet().format(),
//zwave.switchBinaryV1.switchBinaryReport().format(),
//zwave.switchBinaryV1.switchBinaryGet().format(),
//zwave.switchMultilevelV3.switchMultilevelReport().format()

//zwave.configurationV1.configurationGet(parameterNumber:1).format(),
//
//zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [1], size:1).format(),
//zwave.configurationV1.configurationGet(parameterNumber:3).format(),
//new physicalgraph.device.HubAction("91010F2601029900"),
    
    
    zwave.manufacturerProprietaryV1.manufacturerProprietarySet().format(),
    zwave.configurationV1.configurationGet(parameterNumber: 42).format(),
    zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [0], size:1).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 42).format(),
zwave.switchMultilevelV3.switchMultilevelSupportedGet().format(),
    zwave.switchBinaryV1.switchBinaryGet().format(),

    zwave.switchBinaryV1.switchBinaryGet().format()
],500)

}


def setLevel(value) {
log.debug "setting"
    log.trace "setLevel($value)"
    delayBetween([
        zwave.basicV1.basicSet(value: value).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ],500)
}
    
def poll() {
log.debug "polling"
        zwave.switchBinaryV1.switchBinaryGet().format()
}

def open() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ],500)
}

def setposition(value) {
    log.trace "setLevel($value)"
    delayBetween([
        zwave.basicV1.basicSet(value: value).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ],500)
}


def stop() {
    delayBetween([
        zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF).format()
    ],500)
}

def close() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ],500)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}


def eggs() {
//for (int i = 37; i < 40; i++) {
    delayBetween([
zwave.switchBinaryV1.switchBinaryGet().format(),
    zwave.configurationV1.configurationGet(parameterNumber: 10).format(),
    zwave.configurationV1.configurationSet(parameterNumber: 10, scaledConfigurationValue: [2], size:1).format(),
    zwave.configurationV1.configurationSet(parameterNumber: 12, scaledConfigurationValue: [200], size:2).format(),
    zwave.configurationV1.configurationSet(parameterNumber: 13, scaledConfigurationValue: [1], size:1).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 10).format(),
],500)
//}

    //zwave.configurationV1.configurationGet(parameterNumber: 14).format(),
    //zwave.configurationV1.configurationSet(parameterNumber: 14, configurationValue: [1], size:1).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 14).format()
//],1000)

}