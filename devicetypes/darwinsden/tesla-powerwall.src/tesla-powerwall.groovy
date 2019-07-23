/**
 *  Tesla Powerwall 
 *
 *  Copyright 2019 DarwinsDen.com
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
 *
 *	23-Jun-2019 >>> v0.1.1e.20190723 - Initial beta release
 *
 */


metadata {
    definition(name: "Tesla Powerwall", namespace: "darwinsden", author: "darwin@darwinsden.com") {
        capability "Battery"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Power Source"
        capability "Actuator"
        capability "Switch Level"
        capability "Polling"
        capability "Sensor"
        capability "Health Check"
        capability "Refresh"

        attribute "reservePercent", "number"
		attribute "solarPower", "number"
		attribute "loadPower", "number"
		attribute "gridPower", "number"
		attribute "powerWallPower", "number"
        attribute "currentOpState", "string"
        //attribute "siteNameAndVers", "string"
        attribute "currentStrategy", "string"
        attribute "siteName", "string"
        attribute "pwVersion", "string"
        
        command "setBackupReservePercent"
        command "raiseBackupReserve"
        command "lowerBackupReserve"
        command "setBackupOnlyMode"
        command "setTimeBasedControlMode"
        command "setSelfPoweredMode"
        command "setTbcBalanced"
        command "setTbcCostSaving"
    }

    preferences {}

    simulator {}

    tiles(scale: 2) {
        multiAttributeTile(name: "powerwallDisplay", type: "thermostat", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.reservePercent", key: "PRIMARY_CONTROL") {
                attributeState("default", label: 'Reserve: ${currentValue}%', unit: "percentage", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlpha5.png", defaultState: true)
            }
            tileAttribute("device.reserve_pending", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", unit: "percentage", action: "raiseBackupReserve")
                attributeState("VALUE_DOWN", unit: "percentage", action: "lowerBackupReserve")
            }
            tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
                attributeState("default", label: '${currentValue}%', unit: "percentage", defaultState: true)
            }
            tileAttribute('device.currentOpState', key: "OPERATING_STATE") {
                attributeState('Self-Powered', label: '${currentValue}', backgroundColor: "#81bb49") // green
                attributeState('Time-Based Control', label: '${currentValue}', backgroundColor: "#9b9bfc") // purple
                attributeState('Backup-Only', label: '${currentValue}', backgroundColor: "#5a91d4") // blue
                attributeState('Pending Self-Powered', label: '${currentValue}', backgroundColor: "#d28de0") // magenta
                attributeState('Pending Time-Based Control', label: '${currentValue}', backgroundColor: "#d28de0") // magenta
                attributeState('Pending Backup-Only', label: '${currentValue}', backgroundColor: "#d28de0") // magenta
                attributeState('default', label: '${currentValue}', backgroundColor: "#d28de0", defaultState: true) //gray
            }
        }
        valueTile("sitename", "device.sitenameAndVers", width: 4, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: ""
        }
        valueTile("reserve", "device.reservePercent", width: 2, height: 2, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: 'Reserve: ${currentValue}%', action: ""
        }
        standardTile("blankSkinny", "device.blank", width: 4, height: 1) {
            state("blank", label: "", backgroundColor: "#ffffff")
        }
        valueTile("solar", "device.solarPower", width: 2, height: 2) {
            state("solarPower", label: '\u2600\n${currentValue}\nkW', unit: "kW", backgroundColor: "#F9B732")
        }
        standardTile("blank", "device.blank", width: 1, height: 1) {
            state("blank", label: "", backgroundColor: "#ffffff")
        }
        valueTile("battery", "device.battery", width: 2, height: 2) {
            state("battery", label: ': Battery :\n ${currentValue}% \n', unit: "%", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/powerwall-Image.png")
        }
        valueTile("load", "device.loadPower", width: 2, height: 2) {
            state("loadPower", label: '\u2302\n${currentValue}\nkW', unit: "kW", backgroundColor: "#3265CB")
        }
        valueTile("grid", "device.gridPower", width: 2, height: 2) {
            state("gridPower", label: '\u2361\n${currentValue}\nkW', unit: "kW", backgroundColor: "#989286")
        }
        valueTile("powerwall", "device.powerwallPower", width: 2, height: 2) {
            state("powerwallPower", label: '\u2752\n${currentValue}\nkW', unit: "kW", backgroundColor: "#2ec214")
        }
        standardTile("refresh", "device.state", decoration: "flat", width: 2, height: 1) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        valueTile("strategy", "device.currentStrategy", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: ""
        }
        valueTile("commandBar", "device.commands", width: 4, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: '', action: "", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwControls3.png"
        }
        valueTile("blankTall", "device.commands", width: 1, height: 2, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: '', action: ""
        }
        valueTile("stormwatch", "device.stormwatch", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
            state "default", label: '${currentValue}', action: ""
        }
        standardTile("backup", "device.currentOpState", inactiveLabel: false, width: 2, height: 2) {
            state "default", label: 'Backup', action: "setBackupOnlyMode", backgroundColor: "#ffffff", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Backup-Only", label: 'Backup', action: "setBackupOnlyMode", backgroundColor: "#79b821", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Pending Backup-Only", label: 'Backup', action: "setBackupOnlyMode", backgroundColor: "#2179b8", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
        }
        standardTile("self-consumption", "device.currentOpState", inactiveLabel: false, width: 2, height: 2) {
            state "default", label: 'Self-P', action: "setSelfPoweredMode", backgroundColor: "#ffffff", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Self-Powered", label: 'Self-P', action: "setSelfPoweredMode", backgroundColor: "#79b821", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Pending Self-Powered", label: 'Self-P', action: "setSelfPoweredMode", backgroundColor: "#2179b8", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
        }
        standardTile("time-based", "device.currentOpState", inactiveLabel: false, width: 2, height: 2) {
            state "default", label: 'TBC', action: "setTimeBasedControlMode", backgroundColor: "#ffffff", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Time-Based Control", label: 'TBC', action: "setTimeBasedControlMode", backgroundColor: "#79b821", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Pending Time-Based", label: 'TBC', action: "setTimeBasedControlMode", backgroundColor: "#2179b8", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
        }
        standardTile("cost-saving", "device.currentStrategy", inactiveLabel: false, width: 2, height: 2) {
            state "default", label: 'Cost-Sav', action: "setTbcCostSaving", backgroundColor: "#ffffff", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Cost-Saving", label: 'Cost-Sav', action: "setTbcCostSaving", backgroundColor: "#79b821", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Pending Cost-Saving", label: 'Cost-Sav', action: "setTbcCostSaving", backgroundColor: "#2179b8", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
        }
        standardTile("balanced", "device.currentStrategy", inactiveLabel: false, width: 2, height: 2) {
            state "default", label: 'Bal', action: "setTbcBalanced", backgroundColor: "#ffffff", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Balanced", label: 'Bal', action: "setTbcBalanced", backgroundColor: "#79b821", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
            state "Pending Balanced", label: 'Bal', action: "setTbcBalanced", backgroundColor: "#2179b8", icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png"
        }
        main(["battery", "grid"])
        details(["powerwallDisplay", "sitename", "refresh", "solar", "grid", "battery", "load", "powerwall", "strategy", "stormwatch", "blank",
            "commandBar", "blank", "backup", "self-consumption", "time-based", "blankTall", "balanced", "cost-saving", "blankTall"
        ])
    }
}

def setBackupOnlyMode() {
    parent.setBackupOnlyMode(this)
}

def setSelfPoweredMode() {
    parent.setSelfPoweredMode(this)
}

def setTimeBasedControlMode() {
    parent.setTimeBasedControlMode(this)
}

def setTbcBalanced() {
    parent.setTbcBalanced(this)
}

def setTbcCostSaving() {
    parent.setTbcCostSaving(this)
}

def setBackupReservePercent(value) {
    parent.setBackupReservePercent(this, value)
}

def setBackupReservePercentHandler(data) {
    setBackupReservePercent(data.value)
}

def lowerBackupReserve(value) {

    def brp = device.currentValue("reserve_pending").toInteger()
    if (!brp || state.lastReserveSetTime == null || ((now() - state.lastReserveSetTime) > 20 * 1000)) {
        brp = device.currentValue("reserve").toInteger()
    }
    if (brp > 0) {
        brp = brp - 1
        runIn(10, setBackupReservePercentHandler, [data: [value: brp]])
        state.lastReserveSetTime = now()
        sendEvent(name: "reserve_pending", value: brp, displayed: false)
    }
}

def raiseBackupReserve(value) {
    def brp = device.currentValue("reserve_pending").toInteger()
    if (!brp || state.lastReserveSetTime == null || ((now() - state.lastReserveSetTime) > 20 * 1000)) {
        brp = device.currentValue("reservePercent").toInteger()
    }
    if (brp < 100) {
        brp = brp + 1
        runIn(10, setBackupReservePercentHandler, [data: [value: brp]])
        state.lastReserveSetTime = now()
        sendEvent(name: "reserve_pending", value: brp, displayed: false)
    }
}

def installed() {
    log.debug "Installed"
    initialize()
}

def updated() {
    log.debug "Updated"
    initialize()
}

def refresh() {
    log.debug "Executing refresh"
    def status = parent.refresh(this)
}

def initialize() {
    log.debug "initializing PW device"
}

def parse(String description) {
    log.debug "${description}"
}