/**
 *  Minoston Wallmote v1.0.1
 *
 *  	Models: Eva Logik (ZW924) / MINOSTON (MR40Z)
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.1 (07/07/2021)
 *      - Initial Release
 *
 * Reference：
 *  https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/aeotec-wallmote.src/aeotec-wallmote.groovy
 *
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
    definition (name: "Minoston Wallmote", namespace: "sky-nie", author: "winnie", ocfDeviceType: "x.com.st.d.remotecontroller",mcdSync: true) {
        capability "Actuator"
        capability "Button"
        capability "Battery"
        capability "Configuration"
        capability "Sensor"
        capability "Health Check"

        fingerprint mfr: "0312", prod: "0924", model: "D001", deviceJoinName: "Minoston Remote Control", mnmn: "SmartThings", vid: "generic-4-button"//MR40Z
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "rich-control", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL") {
                attributeState "default", label: ' ', action: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        main("rich-control")
        details(["rich-control", childDeviceTiles("endpoints"), "battery"])
    }

    preferences {
        configParams.each {
            if (it.name) {
                if (it.range) {
                    getNumberInput(it)
                }
                else {
                    getOptionsInput(it)
                }
            }
        }
    }
}

def getNumberOfButtons() {
    def modelToButtons = ["D001":4]
    return modelToButtons[zwaveInfo.model] ?: 1
}

def installed() {
    createChildDevices()
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
    sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJson(), displayed: false)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
}

def updated() {
    createChildDevices()
    if (device.label != state.oldLabel) {
        childDevices.each {
            def segs = it.deviceNetworkId.split(":")
            def newLabel = "${device.displayName} button ${segs[-1]}"
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }
}

def configure() {
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false)
    response([
            secure(zwave.batteryV1.batteryGet()),
            "delay 2000",
            secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
    ])
}

def parse(String description) {
    def results = []
    if (description.startsWith("Err")) {
        results = createEvent(descriptionText:description, displayed:true)
    } else {
        def cmd = zwave.parse(description)
        if (cmd) results += zwaveEvent(cmd)
        if (!results) results = [ descriptionText: cmd, displayed: false ]
    }
    return results
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    def button = cmd.sceneNumber

    def value = buttonAttributesMap[(int)cmd.keyAttributes]
    if (value) {
        def child = getChildDevice(button)
        child?.sendEvent(name: "button", value: value, data: [buttonNumber: 1], descriptionText: "$child.displayName was $value", isStateChange: true)
        createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, displayed: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    def linkText = device.label ?: device.name
    [linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def results = []
    results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
    results += response([
            secure(zwave.batteryV1.batteryGet()),
            "delay 2000",
            secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
    ])
    results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%", isStateChange: true ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "$device.displayName battery is low!"
    } else {
        map.value = cmd.batteryLevel
    }
    createEvent(map)
}

def createChildDevices() {
    if (!childDevices) {
        state.oldLabel = device.label
        def child
        for (i in 1..numberOfButtons) {
            child = addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
                    [completedSetup: true, label: "${device.displayName} button ${i}",
                     isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
            child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJson(), displayed: false)
            child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true, displayed: false)
        }
    }
}

def secure(cmd) {
    if (zwaveInfo.zw.contains("s")) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

def getChildDevice(button) {
    String childDni = "${device.deviceNetworkId}:${button}"
    def child = childDevices.find{it.deviceNetworkId == childDni}
    if (!child) {
        log.error "Child device $childDni not found"
    }
    return child
}

private static getSupportedButtonValues() {
    return [
            "pushed",
            "held",
            "double",
            'pushed_3x'
    ]
}

private static getButtonAttributesMap() {
    [
            0: "pushed",
            1: "held",
            3: "double",
            4: "pushed_3x",
    ]
}

private getOptionsInput(param) {
    input "configParam${param.num}", "enum",
            title: "${param.name}:",
            required: false,
            defaultValue: "${param.value}",
            options: param.options
}

private getNumberInput(param) {
    input "configParam${param.num}", "number",
            title: "${param.name}:",
            required: false,
            defaultValue: "${param.value}",
            range: param.range
}

// Configuration Parameters
private getConfigParams() {
    [
            batteryReportThresholdParam,
            lowBatteryAlarmReportParam,
            ledIndicator1ColorParam,
            ledIndicator2ColorParam,
            ledIndicator3ColorParam,
            ledIndicator4ColorParam,
            ledIndicatorBrightnessParam
    ]
}

private getBatteryReportThresholdParam() {
    return getParam(1, "Battery report threshold\n(1% - 20%)", 1, 10, null,"1..20")
}

private getLowBatteryAlarmReportParam() {
    return getParam(2, "Low battery alarm report\n(5% - 20%)", 1, 5, null, "5..20")
}

private getLedIndicator1ColorParam() {
    return getParam(3, "Led Indicator Color for the First button remote control", 1, 0, ledColorOptions)
}

private getLedIndicator2ColorParam() {
    return getParam(4, "Led Indicator Color for the Second button remote control", 1, 1, ledColorOptions)
}

private getLedIndicator3ColorParam() {
    return getParam(5, "Led Indicator Color for the Third button remote control", 1, 2, ledColorOptions)
}

private getLedIndicator4ColorParam() {
    return getParam(6, "Led Indicator Color for the Fourth button remote control", 1, 3, ledColorOptions)
}

private getLedIndicatorBrightnessParam() {
    return getParam(7, "Led Indicator Brightness", 1, 5, brightnessOptions)
}

private getParam(num, name, size, defaultVal, options=null, range=null) {
    def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

    def map = [num: num, name: name, size: size, value: val]
    if (options) {
        map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
        map.options = setDefaultOption(options, defaultVal)
    }
    if (range) map.range = range

    return map
}

private static setDefaultOption(options, defaultVal) {
    return options?.collectEntries { k, v ->
        if ("${k}" == "${defaultVal}") {
            v = "${v} [DEFAULT]"
        }
        ["$k": "$v"]
    }
}

// Setting Options
private static getLedColorOptions() {
    return [
            "0":"White",
            "1":"Purple",
            "2":"Orange",
            "3":"Cyan",
            "4":"Red",
            "5":"Green",
            "6":"Blue"
    ]
}

private static getBrightnessOptions() {
    def options = [:]
    options["0"] = "LED off"

    (1..10).each {
        options["${it}"] = "${it}% Brightness"
    }
    return options
}

private static safeToInt(val, defaultVal=0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}
