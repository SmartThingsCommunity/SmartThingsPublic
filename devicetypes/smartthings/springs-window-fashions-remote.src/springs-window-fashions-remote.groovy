/**
 *  Copyright 2015 SmartThings
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
    definition (name: "Springs Window Fashions Remote", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller", hidden: true) {

        capability "Battery"

        fingerprint mfr:"026E", prod:"5643", model:"5A31", deviceJoinName: "2 Button Window Remote"
        fingerprint mfr:"026E", prod:"4252", model:"5A31", deviceJoinName: "3 Button Window Remote"
    }

    simulator {

    }

    tiles {
        standardTile("state", "device.state", width: 2, height: 2) {
            state 'connected', icon: "st.unknown.zwave.remote-controller", backgroundColor:"#ffffff"
        }

        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'batt.', unit:"",
                    backgroundColors:[
                            [value: 0, color: "#bc2323"],
                            [value: 6, color: "#44b621"]
                    ]
        }

        main "state"
        details(["state", "battery"])
    }

}

def installed() {
    if (zwaveInfo.zw && zwaveInfo.zw.cc?.contains("84")) {
        response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    }
}

def parse(String description) {
    def result = null
    if (description.startsWith("Err")) {
        if (description.startsWith("Err 106") && !state.sec) {
            state.sec = 0
        }
        result = createEvent(descriptionText:description, displayed:true)
    } else {
        def cmd = zwave.parse(description)
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    def result = []
    result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: true)
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    state.sec = 1
    createEvent(isStateChange: true, descriptionText: "$device.displayName: ${cmd.encapsulatedCommand()} [secure]")
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    createEvent(isStateChange: true, descriptionText: "$device.displayName: ${cmd.encapsulatedCommand()}")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    createEvent(isStateChange: true, "$device.displayName: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbatt = now()
    createEvent(map)
}

private command(physicalgraph.zwave.Command cmd) {
    if (deviceIsSecure) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private getDeviceIsSecure() {
    if (zwaveInfo && zwaveInfo.zw) {
        return zwaveInfo.zw.endsWith("s")
    } else {
        return state.sec ? true : false
    }
}
