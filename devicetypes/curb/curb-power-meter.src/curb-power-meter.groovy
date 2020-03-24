/**
 *  Curb Power Meter
 *
 *  Copyright 2017 Curb
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
    definition(name: "CURB Power Meter", namespace: "curb", author: "Curb", ocfDeviceType: "x.com.st.d.energymeter") {
        capability "Power Meter"
        capability "Energy Meter"
    }

    tiles {

        multiAttributeTile(name: "power", type: "generic", width: 6, height: 4, canChangeIcon: false) {

            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "power",
                    label: '${currentValue} W',
                    icon: 'st.switches.switch.off',
                    backgroundColors: [
                        [value: -1000, color: "#25c100"],
                        [value: -500, color: "#76ce61"],
                        [value: -100, color: "#bbedaf"],
                        [value: 0, color: "#bcbbb5"],
                        [value: 100, color: "#efc621"],
                        [value: 1000, color: "#ed8c25"],
                        [value: 2000, color: "#db5e1f"]
                    ]
            }
            tileAttribute ("device.energy", key: "SECONDARY_CONTROL") {
                attributeState "energy", label:'${currentValue} kWh'
            }
        }
        main(["power"])

        details(["power"])
    }
}

def handlePower(value) {
    sendEvent(name: "power", value: value)
}

def handleKwhBilling(kwh) {
    sendEvent(name: "energy", value: kwh.round(3))
}