/**
 *  CAS App
 *
 *  Copyright 2018 Ariel Sepulveda
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
definition(
    name: "CAS App",
    namespace: "cascompany",
    author: "Ariel Sepulveda",
    description: "CAS App",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Samsung TV Settings") {
        input "settingIpAddress", "text", title: "IP Address", required: true
        input "settingMacAddress", "text", title: "MAC Address", required: true
        input "tvCommand", "enum", title: "Perform This Command", metadata:[values:["POWEROFF","POWERON","AV1","AV2","AV3","CLOCK_DISPLAY","COMPONENT1", "COMPONENT2", "HDMI", "HDMI1", "HDM2", "HDM3", "HDMI4", "INFO", "SLEEP"]], required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}


def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {

    subscribe(app, appTouch)
    subscribe(location, changedLocationMode)
    tvAction("AUTHENTICATE")
}

def changedLocationMode(evt) {
    log.debug "changedLocationMode: $evt"
    tvAction(tvCommand)
}

def appTouch(evt) {
    log.debug "appTouch: $evt"
    tvAction(tvCommand)
}

def parse(event) {
    log.debug "${event}"
}

private tvAction(key) {
    log.debug "Executing ${tvCommand}"

    // Standard Connection Data
    def appString = "iphone..iapp.samsung"
    def appStringLength = appString.getBytes().size()

    def tvAppString = "iphone.UN60ES8000.iapp.samsung"
    def tvAppStringLength = tvAppString.getBytes().size()

    def remoteName = "SmartThings".encodeAsBase64().toString()
    def remoteNameLength = remoteName.getBytes().size()

    // Device Connection Data
    def ipAddress = settingIpAddress.encodeAsBase64().toString()
    def ipAddressLength = ipAddress.getBytes().size()
    def ipAddressHex = settingIpAddress.tokenize( '.' ).collect { String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP Address (HEX): ${ipAddressHex}"

    def macAddress = settingMacAddress.replaceAll(":","").encodeAsBase64().toString()
    def macAddressLength = macAddress.getBytes().size()

    // The Authentication Message
    def authenticationMessage = "${(char)0x64}${(char)0x00}${(char)ipAddressLength}${(char)0x00}${ipAddress}${(char)macAddressLength}${(char)0x00}${macAddress}${(char)remoteNameLength}${(char)0x00}${remoteName}"
    def authenticationMessageLength = authenticationMessage.getBytes().size()
    
    def authenticationPacket = "${(char)0x00}${(char)appStringLength}${(char)0x00}${appString}${(char)authenticationMessageLength}${(char)0x00}${authenticationMessage}"

    // If our initial run, just send the authentication packet so the prompt appears on screen
    if (key == "AUTHENTICATE") {
	    sendHubCommand(new physicalgraph.device.HubAction(authenticationPacket, physicalgraph.device.Protocol.LAN, "${ipAddressHex}:D6D8"))
    } else {
        // Build the command we will send to the Samsung TV
        def command = "KEY_${key}".encodeAsBase64().toString()
        def commandLength = command.getBytes().size()

        def actionMessage = "${(char)0x00}${(char)0x00}${(char)0x00}${(char)commandLength}${(char)0x00}${command}"
        def actionMessageLength = actionMessage.getBytes().size()

        def actionPacket = "${(char)0x00}${(char)tvAppStringLength}${(char)0x00}${tvAppString}${(char)actionMessageLength}${(char)0x00}${actionMessage}"

        // Send both the authentication and action at the same time
        sendHubCommand(new physicalgraph.device.HubAction(authenticationPacket + actionPacket, physicalgraph.device.Protocol.LAN, "${ipAddressHex}:D6D8"))
    }
}