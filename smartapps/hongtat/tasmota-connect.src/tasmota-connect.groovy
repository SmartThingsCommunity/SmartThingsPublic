/**
 *  Tasmota (Connect)
 *
 *  Copyright 2020 AwfullySmart.com - HongTat Tan
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
String appVersion() { return "1.1.2" }

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
definition(
    name: "Tasmota (Connect)",
    namespace: "hongtat",
    author: "AwfullySmart",
    description: "Allows you to integrate your Tasmota devices with SmartThings.",
    iconUrl: "https://awfullysmart.github.io/st/awfullysmart-180.png",
    iconX2Url: "https://awfullysmart.github.io/st/awfullysmart-180.png",
    iconX3Url: "https://awfullysmart.github.io/st/awfullysmart-180.png",
    singleInstance: true,
    pausable: false
)

preferences {
    page(name: "mainPage", nextPage: "", uninstall: true, install: true)
    page(name: "configureDevice")
    page(name: "deleteDeviceConfirm")
    page(name: "addDevice")
    page(name: "addDeviceConfirm")
}

def mainPage() {
    if (state?.install) {
        dynamicPage(name: "mainPage", title: "Tasmota (Connect) - v${appVersion()}") {
            section(){
                href "addDevice", title:"New Tasmota Device", description:""
            }
            section("Installed Devices"){
                getChildDevices().sort({ a, b -> a.label <=> b.label }).each {
                    String typeName = it.typeName
                    if (moduleMap().find{ it.value.type == "${typeName}" }?.value?.settings?.contains('ip')) {
                        String actualDni = it.deviceNetworkId
                        String descText = ""
                        // If the device network id doesn't match the Tasmota device MAC (w/o ":"), it will not work correctly.
                        // Manually modifying the DNI to "fix" this issue is likely not going to work. :)
                        // Make sure you have a physical SmartThings hub v2 or v3,
                        // Tasmota devices and ST hub are on static IP and on the same subnetwork.
                        if (childSetting(it.id, "ip") != null) {
                            String dni = it.state?.dni
                            descText = childSetting(it.id, "ip")
                            if ((dni != null && dni != actualDni) || (dni == null)) {
                                descText += ' (!!!)'
                            }
                        } else {
                            descText = "Tap to set IP address"
                        }
                        href "configureDevice", title:"$it.label", description: descText, params: [did: actualDni]
                    } else {
                        href "configureDevice", title:"$it.label", description: "", params: [did: it.deviceNetworkId]
                    }
                }
            }
            section(title: "Settings") {
                input("dateformat", "enum",
                        title: "Date Format",
                        description: "Set preferred data format",
                        options: ["MM/dd/yyyy h:mm", "MM-dd-yyyy h:mm", "dd/MM/yyyy h:mm", "dd-MM-yyyy h:mm"],
                        defaultValue: "MM/dd/yyyy h:mm",
                        required: false, submitOnChange: false)
                input("frequency", "enum",
                        title: "Device Health Check",
                        description: "Check in on device health every so often",
                        options: ["Every 1 minute", "Every 5 minutes", "Every 10 minutes", "Every 15 minutes", "Every 30 minutes", "Every 1 hour"],
                        defaultValue: "Every 5 minutes",
                        required: false, submitOnChange: false)
            }
            section(title: "SmartThings Hub") {
                String st = getHub()?.localIP
                if (st == null || st == '') {
                    paragraph "Unable to find a SmartThings Hub. Do you have a physical SmartThings Hub (v2 or v3)?"
                } else {
                    paragraph "IP Address: ${st}"
                }
            }
            remove("Remove (Includes Devices)", "This will remove all devices.")
        }
    } else {
        dynamicPage(name: "mainPage", title: "Tasmota (Connect)") {
            section {
                paragraph "Success!"
            }
        }
    }
}

def configureDevice(params){
    def t = params?.did
    if (params?.did) {
        atomicState?.curPageParams = params
    } else {
        t = atomicState?.curPageParams?.did
    }
    def d = getChildDevice(t)
    state.currentDeviceId = d?.deviceNetworkId
    state.currentId = d?.id
    state.currentDisplayName = d?.displayName
    state.currentTypeName = d?.typeName
    state.currentVersion = (d?.currentVersion) ? (' - ' + d.currentVersion) : ''

    def moduleParameter = moduleMap().find{ it.value.type == "${state.currentTypeName}" }?.value
    return dynamicPage(name: "configureDevice", install: false, uninstall: false, nextPage: "mainPage") {
        section("${state.currentDisplayName} ${state.currentVersion}") {}
        if (moduleParameter && moduleParameter.settings.contains('ip')) {
            section("Device setup") {
                input("dev:${state.currentId}:ip", "text",
                        title: "IP Address",
                        description: "IP Address",
                        defaultValue: "",
                        required: true, submitOnChange: true)
                input("dev:${state.currentId}:username", "text",
                        title: "Username",
                        description: "Username",
                        defaultValue: "",
                        required: false, submitOnChange: true)
                input("dev:${state.currentId}:password", "text",
                        title: "Password",
                        description: "Password",
                        defaultValue: "",
                        required: false, submitOnChange: true)
            }
        }
        if (moduleParameter && moduleParameter.settings.contains('bridge')) {
            section("RF/IR Bridge") {
                input ("dev:${state.currentId}:bridge", "enum",
                        title: "RF/IR Bridge",
                        description: "Select a RF/IR bridge to communicate with RF/IR device",
                        multiple: false, required: false, options: childDevicesByType(["Tasmota RF Bridge", "Tasmota IR Bridge"]), submitOnChange: true)
            }
        }
        // Whether to mark device as "always online"
        if (moduleParameter && moduleParameter.settings.contains('healthState')) {
            section("Health State") {
                input ("dev:${state.currentId}:health_state", "bool",
                        title: "Health State",
                        description: "Mark device as Always Online",
                        defaultValue: false, required: false, submitOnChange: true)
            }
        }
        // Virtual Switch
        if (moduleParameter && moduleParameter.settings.contains('virtualSwitch') && childSetting(state.currentId, "bridge") != null) {
            section("RF/IR Code") {
                input("dev:${state.currentId}:command_on", "text",
                        title: "Command to send for 'ON'",
                        description: "Tap to set",
                        defaultValue: "", required: false, submitOnChange: true)
                input("dev:${state.currentId}:command_off", "text",
                        title: "Command to send for 'OFF'",
                        description: "Tap to set",
                        defaultValue: "", required: false, submitOnChange: true)
                input("dev:${state.currentId}:track_state", "bool",
                        title: "State tracking",
                        description: "Enable real-time tracking",
                        defaultValue: false, required: false, submitOnChange: true)

                if (childSetting(state.currentId, "track_state")) {
                    input("dev:${state.currentId}:payload_on", "text",
                        title: "Code that represents the 'ON' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
                    input("dev:${state.currentId}:payload_off", "text",
                        title: "Code that represents the 'OFF' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
                } else {
                    deleteChildSetting(state.currentId, "payload_on")
                    deleteChildSetting(state.currentId, "payload_off")
                }
            }
        }
        // Virtual Shade
        if (moduleParameter && moduleParameter.settings.contains('virtualShade') && childSetting(state.currentId, "bridge") != null) {
            section("RF/IR Code") {
                input("dev:${state.currentId}:command_open", "text",
                        title: "Command to send for 'OPEN'",
                        description: "Tap to set",
                        defaultValue: "", required: false, submitOnChange: true)
                input("dev:${state.currentId}:command_close", "text",
                        title: "Command to send for 'CLOSE'",
                        description: "Tap to set",
                        defaultValue: "", required: false, submitOnChange: true)
                input("dev:${state.currentId}:command_pause", "text",
                        title: "Command to send for 'PAUSE'",
                        description: "Tap to set",
                        defaultValue: "", required: false, submitOnChange: true)
                input("dev:${state.currentId}:track_state", "bool",
                        title: "State tracking",
                        description: "Enable real-time tracking",
                        defaultValue: false, required: false, submitOnChange: true)

                if (childSetting(state.currentId, "track_state")) {
                    input("dev:${state.currentId}:payload_open", "text",
                            title: "Code that represents the 'OPEN' state",
                            description: "Tap to set",
                            defaultValue: "", required: false)
                    input("dev:${state.currentId}:payload_close", "text",
                            title: "Code that represents the 'CLOSE' state",
                            description: "Tap to set",
                            defaultValue: "", required: false)
                    input("dev:${state.currentId}:payload_pause", "text",
                            title: "Code that represents the 'PAUSE' state",
                            description: "Tap to set",
                            defaultValue: "", required: false)
                } else {
                    deleteChildSetting(state.currentId, "payload_open")
                    deleteChildSetting(state.currentId, "payload_close")
                    deleteChildSetting(state.currentId, "payload_pause")
                }
            }
        }
        // Virtual Button
        if (moduleParameter && moduleParameter.settings.contains('virtualButton') && childSetting(state.currentId, "bridge") != null) {
            def numberOfButtons = moduleParameter?.channel ?: 1
            section("RF/IR Code") {
                for (def buttonNumer : 1..numberOfButtons) {
                    input("dev:${state.currentId}:button_${buttonNumer}", "text",
                            title: "Button ${buttonNumer} 'pushed' state code",
                            description: "Tap to set",
                            defaultValue: "", required: false, submitOnChange: false)
                }
            }
        }
        // Virtual Contact Sensor
        if (moduleParameter && moduleParameter.settings.contains('virtualContactSensor') && childSetting(state.currentId, "bridge") != null) {
            section("RF/IR Code") {
                input("dev:${state.currentId}:payload_open", "text",
                        title: "Code that represents the 'OPEN' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
                input("dev:${state.currentId}:payload_close", "text",
                        title: "Code that represents the 'CLOSE' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
            }
            section("If your sensor does not report a 'CLOSE' state, you can set a delay of seconds (0: Disabled) after which the state will be updated to 'CLOSE'") {
                input("dev:${state.currentId}:off_delay", "number",
                        title: "Number of seconds",
                        description: "Tap to set",
                        defaultValue: "0", required: false)
            }
        }
        // Virtual Motion Sensor
        if (moduleParameter && moduleParameter.settings.contains('virtualMotionSensor') && childSetting(state.currentId, "bridge") != null) {
            section("RF/IR Code") {
                input("dev:${state.currentId}:payload_active", "text",
                        title: "Code that represents the 'ACTIVE' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
                input("dev:${state.currentId}:payload_inactive", "text",
                        title: "Code that represents the 'INACTIVE' state",
                        description: "Tap to set",
                        defaultValue: "", required: false)
            }
            section("If your sensor does not report an 'INACTIVE' state, you can set a delay of seconds (0: Disabled) after which the state will be updated to 'INACTIVE'") {
                input("dev:${state.currentId}:off_delay", "number",
                        title: "Number of seconds",
                        description: "Tap to set",
                        defaultValue: "0", required: false)
            }
        }
        // Virtual Air Con
        if (moduleParameter && moduleParameter.settings.contains('virtualAircon') && childSetting(state.currentId, "bridge") != null) {
            def irBridge = getChildDevices().find { it.id == childSetting(state.currentId, "bridge") }?: null
            def supportedVendor = irBridge?.currentSupportedVendor

            if (irBridge != null && supportedVendor != null) {
                def vendor = new JsonSlurper().parseText(supportedVendor)
                if (vendor != null && vendor.size() > 0) {
                    section("Air Conditioner brand") {
                        input ("dev:${state.currentId}:hvac", "enum",
                            title: "Select air conditioner brand",
                            description: "Select air conditioner brand",
                            multiple: false, required: false, options: vendor, submitOnChange: true)
                    }
                    section("Open/Close Sensor") {
                        input ("dev:${state.currentId}:contactSensor", "capability.contactSensor", title: "Select Open/Close Sensor",
                            description: "Choose the sensor that senses if the AC is On or Off",
                            multiple: false, required: false)
                    }
                } else {
                    deleteChildSetting(state.currentId, "hvac")
                    section("Air Conditioner brand") {
                        paragraph "Please select an IR Bridge with \"tasmota-ir\" firmware. This firmware has all the available IR protocols."
                    }
                }
            } else {
                deleteChildSetting(state.currentId, "hvac")
                section("Air Conditioner brand") {
                    paragraph "Please select an IR Bridge with \"tasmota-ir\" firmware. This firmware has all the available IR protocols."
                }
            }
        }

        // Potential Problem
        if (moduleParameter && moduleParameter.settings.contains('ip')) {
            def dc = getChildDevice(state.currentDeviceId)
            String dni = dc.state?.dni
            String actualDni = dc.deviceNetworkId
            if ((dni != null && dni != actualDni)) {
                section("Potential Problem Detected") {
                    paragraph "It appears that this device is either offline or there is another device using the same IP address or Device Network ID."
                }
            }
        }

        section("DANGER ZONE", hideable: true, hidden: true) {
            href "deleteDeviceConfirm", title:"DELETE $state.currentDisplayName", description: "Tap here to delete this device."
        }
    }

}

def deleteDeviceConfirm(){
    try {
        def d = getChildDevice(state.currentDeviceId)
        unsubscribe(d)
        deleteChildDevice(state.currentDeviceId, true)
        deleteChildSetting(d.id)
        dynamicPage(name: "deleteDeviceConfirm", title: "", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted."
            }
        }
    } catch (e) {
        dynamicPage(name: "deleteDeviceConfirm", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            }
        }
    }
}

def addDevice(){
    Map deviceOptions = [:]
    moduleMap().sort({a, b -> a.value.name <=> b.value.name}).each { k,v ->
        deviceOptions[k] = v.name
    }
    dynamicPage(name: "addDevice", title: "", nextPage: "addDeviceConfirm") {
        section ("New Tasmota device") {
            input ("virtualDeviceType", "enum",
                title: "Which device do you want to add?",
                description: "", multiple: false, required: true, options: deviceOptions, submitOnChange: false
            )
            input ("deviceName", title: "Device Name", defaultValue: "Tasmota device", required: true, submitOnChange: false)
        }
    }
}

def addDeviceConfirm() {
    def latestDni = state.nextDni
    if (virtualDeviceType) {
        def selectedDevice = moduleMap().find{ it.key == virtualDeviceType }.value
        Map virtualParentData = [:]
        // Does this have child device(s)?
        def channel = selectedDevice?.channel
        if (channel != null && selectedDevice?.child != false) {
            if (channel > 1) {
                String parentChildName = selectedDevice.child[0]
                Map virtualParentChild = [:]
                for (i in 2..channel) {
                    parentChildName = (selectedDevice.child[i - 2]) ?: parentChildName
                    virtualParentChild[i] = parentChildName
                }
                virtualParentData["child"] = virtualParentChild.encodeAsJson()
            }
        }
        if (channel != null) {
            virtualParentData["endpoints"] = channel as String
        }
        try {
            def virtualParent = addChildDevice("hongtat", selectedDevice?.type, "AWFULLYSMART-tasmota-${latestDni}", getHub()?.id, [
                    "completedSetup": true,
                    "label": deviceName,
                    "data": virtualParentData
            ])
            // Tracks all installed devices
            def deviceList = state?.deviceList ?: []
            deviceList.push(virtualParent.id as String)
            state?.deviceList = deviceList
            virtualParent.initialize()
            latestDni++
            state.nextDni = latestDni
            dynamicPage(name: "addDeviceConfirm", title: "Add a device", nextPage: "mainPage") {
                section {
                    paragraph "The device has been added. Please proceed to configure device."
                }
            }
        } catch (e) {
            dynamicPage(name: "addDeviceConfirm", title: "Have you added all the device handlers?", nextPage: "mainPage") {
                section {
                    paragraph "Please follow these steps:", required: true
                    paragraph "1. Sign in to your SmartThings IDE.", required: true
                    paragraph "2. Under 'My Device Handlers' > click 'Settings' > 'Add new repository' > enter the following", required: true
                    paragraph "   Owner: hongtat, Name: tasmota-connect, Branch: master", required: true
                    paragraph "3. Under 'Update from Repo' > click 'tasmota-connect' > Select all files > Tick 'Publish' > then 'Execute Update'", required: true
                    paragraph "Error message: ${(e as String).split(":")[1]}.", required: true
                }
            }
        }
    } else {
        dynamicPage(name: "addDeviceConfirm", title: "Add a device", nextPage: "mainPage") {
            section {
                paragraph "Please try again."
            }
        }
    }
}

def installed() {
    state?.nextDni = 1
    state?.deviceList = []
    state?.install = true
}

def uninstalled() {
    // Delete all child devices upon uninstall
    getAllChildDevices().each {
        deleteChildDevice(it.deviceNetworkId, true)
    }
}

def updated() {
    if (!state?.nextDni) { state?.nextDni = 1 }
    if (!state?.deviceList) { state?.deviceList = [] }
    //log.debug "Updated with settings: ${settings}"

    unsubscribe()

    // Subscription
    getChildDevices().eachWithIndex { cd, i ->
        // contactSensor
        def cs = childSetting(cd.id, "contactSensor")
        if (cs != null) {
            subscribe(cs, "contact", contactHandler)
        }
        // crossDeviceMessaging
        def selectedDevice = moduleMap().find{ it.value.type == cd.typeName }?.value
        if (selectedDevice?.messaging == true) {
            subscribe(cd, "messenger", crossDeviceMessaging)
        }
        cd.initialize()
    }

    // Clean up uninstalled devices
    def deviceList = state?.deviceList ?: []
    def newDeviceList = []
    deviceList.each { entry ->
        if (!getChildDevices().find { it.id == entry }) { deleteChildSetting(entry) }
        else { newDeviceList.push(entry as String) }
    }
    state?.deviceList = newDeviceList

    // Set new Tasmota devices values to default
    settingUpdate("deviceName", "Tasmota Device", "text")
    settingUpdate("virtualDeviceType", "", "enum")
}

def initialize() {
}

/**
 * Call Tasmota
 * @param childDevice
 * @param command
 * @return
 */
def callTasmota(childDevice, command) {
    // Virtual device sends bridge's ID, find the actual device's object
    if (childDevice instanceof String) {
        childDevice = getChildDevices().find { it.id == childDevice }?: null
    }
    // Real device sends its object
    if (childSetting(childDevice.device.id, "ip")) {
        updateDeviceNetworkId(childDevice)
        def hubAction = new physicalgraph.device.HubAction(
            method: "POST",
            headers: [HOST: childSetting(childDevice.device.id, "ip") + ":80"],
            path: "/cm?user=" + (childSetting(childDevice.device.id, "username") ?: "") + "&password=" + (childSetting(childDevice.device.id, "password") ? URLEncoder.encode(childSetting(childDevice.device.id, "password")) : "") + "&cmnd=" + URLEncoder.encode(command),
            null,
            [callback: "calledBackHandler"]
        )
        log.debug "${childDevice.device.displayName} (" + childSetting(childDevice.device.id, "ip") + ") called: " + command
        childDevice.sendHubCommand(hubAction)
    } else {
        log.debug "Please add the IP address of ${childDevice.device.displayName}."
    }
    return
}

/**
 * Get the JSON value from the incoming
 * @param str
 * @return
 */
def getJson(str) {
    def parts = []
    def json = null
    if (str) {
        str.eachLine { line, lineNumber ->
            if (lineNumber == 0) {
                parts = line.split(" ")
                return
            }
        }
        if ((parts.length == 3) && parts[1].startsWith('/?json=')) {
            String rawCode = parts[1].split("json=")[1].trim().replace('%20', ' ')
            if ((rawCode.startsWith("{") && rawCode.endsWith("}")) || (rawCode.startsWith("[") && rawCode.endsWith("]"))) {
                json = new JsonSlurper().parseText(rawCode)
            }
        }
    }
    return json
}

def setNetworkAddress(String mac) {
    mac.toUpperCase().replaceAll(':', '')
}

def updateDeviceNetworkId(childDevice) {
    def actualDeviceNetworkId = childDevice.device.deviceNetworkId
    if (childDevice.state.dni != null && childDevice.state.dni != "" && actualDeviceNetworkId != childDevice.state.dni) {
        log.debug "Updated '${childDevice.device.displayName}' dni to '${childDevice.state.dni}'"
        childDevice.device.deviceNetworkId = "${childDevice.state.dni}"
    }
}

def channelNumber(String dni) {
    if (dni.indexOf("-ep") >= 0) {
        dni.split("-ep")[-1] as Integer
    } else {
        ""
    }
}

/**
 * Return a list of installed child devices that match the input list
 * @param typeList
 * @return
 */
def childDevicesByType(List typeList) {
    List result = []
    if (typeList && typeList.size() > 0) {
        getChildDevices().each {
            if (it.typeName in typeList) {
                result << [(it.id): "${it.displayName}"]
            }
        }
    }
    return result
}

Map moduleMap() {
    Map customModule = [
        "1":    [name: ".Sonoff Basic / Mini / RF / SV", type: "Tasmota Generic Switch"],
        "4":    [name: ".Sonoff TH", type: "Tasmota Generic Switch", channel: 2, child: ["Tasmota Child Temp/Humidity Sensor"]],
        "5":    [name: ".Sonoff Dual / Dual R2", type: "Tasmota Generic Switch", channel: 2],
        "6":    [name: ".Sonoff Pow / Pow R2 / S31", type: "Tasmota Metering Switch"],
        "25":   [name: ".Sonoff Bridge", type: "Tasmota RF Bridge"],
        "44":   [name: ".Sonoff iFan", type: "Tasmota Fan", channel: 2],
        "1000": [name: "Generic Switch (1ch)", type: "Tasmota Generic Switch"],
        "1001": [name: "Generic Switch (2ch)", type: "Tasmota Generic Switch", channel: 2],
        "1002": [name: "Generic Switch (3ch)", type: "Tasmota Generic Switch", channel: 3],
        "1003": [name: "Generic Switch (4ch)", type: "Tasmota Generic Switch", channel: 4],
        "1004": [name: "Generic Switch (5ch)", type: "Tasmota Generic Switch", channel: 5],
        "1005": [name: "Generic Switch (6ch)", type: "Tasmota Generic Switch", channel: 6],
        "1006": [name: "Generic Switch (7ch)", type: "Tasmota Generic Switch", channel: 7],
        "1007": [name: "Generic Switch (8ch)", type: "Tasmota Generic Switch", channel: 8],
        "1010": [name: "Generic Metering Switch (1ch)", type: "Tasmota Metering Switch"],
        "1011": [name: "Generic Metering Switch (2ch)", type: "Tasmota Metering Switch", channel: 2],
        "1012": [name: "Generic Metering Switch (3ch)", type: "Tasmota Metering Switch", channel: 3],
        "1013": [name: "Generic Metering Switch (4ch)", type: "Tasmota Metering Switch", channel: 4],
        "1014": [name: "Generic Metering Switch (5ch)", type: "Tasmota Metering Switch", channel: 5],
        "1015": [name: "Generic Metering Switch (6ch)", type: "Tasmota Metering Switch", channel: 6],
        "1016": [name: "Generic Metering Switch (7ch)", type: "Tasmota Metering Switch", channel: 7],
        "1017": [name: "Generic Metering Switch (8ch)", type: "Tasmota Metering Switch", channel: 8],
        "1020": [name: "Generic Dimmer Switch", type: "Tasmota Dimmer Switch"],
        "1021": [name: "Generic IR Bridge", type: "Tasmota IR Bridge"],
        "1022": [name: "Generic Light (RGBW)", type: "Tasmota RGBW Light"],
        "1023": [name: "Generic Light (RGB)", type: "Tasmota RGB Light"],
        "1024": [name: "Generic Light (CCT)", type: "Tasmota CCT Light"],
        "1100": [name: "Virtual Switch", type: "Tasmota Virtual Switch"],
        "1101": [name: "Virtual Shade/Blind", type: "Tasmota Virtual Shade"],
        "1111": [name: "Virtual 1-button", type: "Tasmota Virtual 1 Button"],
        "1112": [name: "Virtual 2-button", type: "Tasmota Virtual 2 Button"],
        "1114": [name: "Virtual 4-button", type: "Tasmota Virtual 4 Button"],
        "1116": [name: "Virtual 6-button", type: "Tasmota Virtual 6 Button"],
        "1117": [name: "Virtual Contact Sensor", type: "Tasmota Virtual Contact Sensor"],
        "1118": [name: "Virtual Motion Sensor", type: "Tasmota Virtual Motion Sensor"],
        "1119": [name: "Virtual Air Conditioner", type: "Tasmota Virtual Air Conditioner"]
    ]
    Map defaultModule = [
        "Tasmota Generic Switch":          [channel: 1, messaging: false,   virtual: false, child: ["Tasmota Child Switch Device"], settings: ["ip"]],
        "Tasmota Metering Switch":         [channel: 1, messaging: false,   virtual: false, child: ["Tasmota Child Switch Device"], settings: ["ip"]],
        "Tasmota Dimmer Switch":           [channel: 1, messaging: false,   virtual: false, child: false, settings: ["ip"]],
        "Tasmota RGBW Light":              [channel: 1, messaging: false,   virtual: false, child: false, settings: ["ip"]],
        "Tasmota RGB Light":               [channel: 1, messaging: false,   virtual: false, child: false, settings: ["ip"]],
        "Tasmota CCT Light":               [channel: 1, messaging: false,   virtual: false, child: false, settings: ["ip"]],
        "Tasmota Fan":                     [channel: 2, messaging: false,   virtual: false, child: ["Tasmota Child Switch Device"], settings: ["ip", "healthState"]],
        "Tasmota RF Bridge":               [channel: 1, messaging: true,    virtual: false, child: false, settings: ["ip"]],
        "Tasmota IR Bridge":               [channel: 1, messaging: true,    virtual: false, child: false, settings: ["ip"]],
        "Tasmota Virtual Contact Sensor":  [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualContactSensor", "bridge"]],
        "Tasmota Virtual Motion Sensor":   [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualMotionSensor", "bridge"]],
        "Tasmota Virtual Switch":          [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualSwitch", "bridge"]],
        "Tasmota Virtual Shade":           [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualShade", "bridge"]],
        "Tasmota Virtual 1 Button":        [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualButton", "bridge"]],
        "Tasmota Virtual 2 Button":        [channel: 2, messaging: true,    virtual: true,  child: false, settings: ["virtualButton", "bridge"]],
        "Tasmota Virtual 4 Button":        [channel: 4, messaging: true,    virtual: true,  child: false, settings: ["virtualButton", "bridge"]],
        "Tasmota Virtual 6 Button":        [channel: 6, messaging: true,    virtual: true,  child: false, settings: ["virtualButton", "bridge"]],
        "Tasmota Virtual Air Conditioner": [channel: 1, messaging: true,    virtual: true,  child: false, settings: ["virtualAircon", "bridge"]]
    ]
    Map modules = [:]
    customModule.each { k,v ->
        modules[k] = defaultModule[v.type] + v
    }
    return modules
}

/**
 * Cross device messaging between child devices
 * @param evt
 * @return
 */
def crossDeviceMessaging(evt) {
    def d = evt.getDevice()

    log.debug "CDM - value: ${evt.jsonValue} - DNI: ${d.deviceNetworkId} - ID: ${d.id}"
    def virtualDevices = moduleMap().findAll{ it.value.virtual }?.collect { it.value.type }
    getChildDevices().findAll { it.typeName in virtualDevices }?.each {
        def bridge = childSetting(it.id, "bridge")
        if (bridge && bridge == d.id) {
            it.parseEvents(200, evt.jsonValue)
        }
    }
}

void contactHandler(evt) {
    def d = evt.getDevice()
    def virtualDevices = moduleMap().findAll{ it.value.virtual }?.collect { it.value.type }

    Map message = [:]
    message.contactSensor = evt.value
    getChildDevices().findAll { it.typeName in virtualDevices }?.each {
        def cs = childSetting(it.id, "contactSensor")
        if (cs && cs.id == d.id) {
            it.parseEvents(200, message)
        }
    }
}

/**
 * Get SmartApp's general setting value
 * @param name
 * @return String | null
 */
def generalSetting(String name) {
    return (settings?."${name}") ?: null
}

/**
 * Health Check - online/offline
 * @return Integer
 */
Integer checkInterval() {
    Integer interval = ((generalSetting("frequency") ?: 'Every 1 minute').replace('Every ', '').replace(' minutes', '').replace(' minute', '').replace('1 hour', '60')) as Integer
    if (interval < 15) {
        return (interval * 2 * 60 + 1 * 60)
    } else {
        return (30 * 60 + 2 * 60)
    }
}

/**
 * Get child setting - this is stored in SmartApp
 * @param id String device ID
 * @param name String | List
 * @return
 */
def childSetting(String id, name) {
    def v = null
    if (name instanceof String)  {
        v = (settings?."dev:${id}:${name}")?: null
    } else if (name instanceof List) {
        v = [:]
        name.each() { entry ->
            v[entry] = (settings?."dev:${id}:${entry}")?.trim()?: null
        }
    }
    return (v instanceof String) ? v.trim() : v
}

/**
 * Delete child setting from SmartApp
 * @param id
 * @param name
 * @return
 */
def deleteChildSetting(id, name=null) {
    // If a name is given, delete the K/V
    if (id && name) {
        if (settings?.containsKey("dev:${id}:${name}" as String)) {
            app?.deleteSetting("dev:${id}:${name}" as String)
        }
    } else if (id && name==null) {
        // otherwise, delete everything
        ["ip", "username", "password", "bridge", "command_on", "command_off", "track_state", "payload_on", "payload_off", "off_delay", "command_open", "command_close", "command_pause", "payload_open", "payload_close", "payload_pause", "payload_active", "payload_inactive", "hvac", "contactSensor", "health_state"].each { n ->
            app?.deleteSetting("dev:${id}:${n}" as String)
        }
        // button
        for(def n : 1..6) {
            app?.deleteSetting("dev:${id}:button_${n}" as String)
        }
    }
}

def settingUpdate(name, value, type=null) {
    if(name && type) { app?.updateSetting("$name", [type: "$type", value: value]) }
    else if (name && type == null) { app?.updateSetting(name.toString(), value) }
}

private getHub() {
    return location.getHubs().find{ it.getType().toString() == 'PHYSICAL' }
}