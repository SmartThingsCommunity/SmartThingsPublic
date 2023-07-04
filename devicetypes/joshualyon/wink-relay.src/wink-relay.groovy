/**
 *  Wink Relay Device Handler
 *
 *  Copyright 2017 Joshua Lyon
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
    definition (name: "Wink Relay", namespace: "joshualyon", author: "Josh Lyon", ocfDeviceType: "oic.d.smartplug", mcdSync: true) {
        capability "Switch"
        //attribute "switch" //on/off
        //command "on"
        //command "off"
        capability "Polling"
        //command "poll"
        capability "Refresh"
        //command "refresh"
        capability "Temperature Measurement"
        //attribute "temperature"
        capability "Motion Sensor"
        //attribute "motion" //active/inactive
        capability "Relative Humidity Measurement"
        //attribute "humidity"

        attribute "proximityRaw", "string"
        attribute "proximity", "number"
        
        attribute "relay1", "enum", ["on", "off"]
        command "relay1On"
        command "relay1Off"
        //command "relay1Toggle"

        attribute "relay2", "enum", ["on", "off"]
        command "relay2On"
        command "relay2Off"
        //command "relay2Toggle"

        attribute "screenBacklight", "enum", ["on", "off"]
        command "screenBacklightOn"
        command "screenBacklightOff"
        //command "screenBacklightToggle"

        attribute "topButton", "enum", ["on", "off"]
        attribute "bottomButton", "enum", ["on", "off"]
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.switch", key: "SECONDARY_CONTROL") {
                attributeState "device.switch", label:'Controls both switches simultaneously', icon: "st.Appliances.appliances17"
            }
        }
        
        childDeviceTile("switch1", "switch1", height: 2, width: 2, childTileName: "switch")
        childDeviceTile("switch2", "switch2", height: 2, width: 2, childTileName: "switch")

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state "val", label:'${currentValue}°F', icon: "st.Weather.weather2", defaultState: true
        }
        valueTile("humidity", "device.humidity", width: 2, height: 2) {
            state "val", label:'${currentValue}%', icon: "st.Weather.weather12", defaultState: true
        }
        valueTile("proximity", "device.proximity", width: 2, height: 2) {
            state "val", label:'${currentValue}', icon: "st.Entertainment.entertainment15", defaultState: true
        }


        // the "switch" tile will appear in the Things view
        main("switch")
        // the tiles defined below will show in the detail view
        details(["switch", "switch1", "switch2", "refresh", "temperature", "humidity", "proximity"])

    }
}

def installed(){
	createChildDevices()
    sendHubCommand( refresh() )
    //setupEventSubscription() - refresh includes this now
}
def updated(){
	if(!childDevices){
    	createChildDevices()
    }
    else if (device.label != state.oldLabel) {
		childDevices.each { child ->
        	log.debug "Renaming child: ${child.displayName} using parent name: ${device.displayName}"
			def newLabel = "${device.displayName} (Switch ${getChildId(child.deviceNetworkId)})"
			child.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
    sendHubCommand( refresh() )
    //setupEventSubscription() - refresh includes this now
}

def createChildDevices(){
	for(i in 1..2){
    	addChildDevice(
        	"Wink Relay Switch", 
        	"${device.deviceNetworkId}.switch${i}", 
            null, 
            [
               completedSetup: true, 
               label: "${device.displayName} (Switch ${i})", 
               isComponent: true, 
               componentName: "switch$i", 
               componentLabel: "Switch $i"
            ]
        )   
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    log.debug "JSON: ${msg.json}"
    //get the child devices for later use (we'll search through them to find our devices to update)
    def children = childDevices
    def switch1 = children.find{it.deviceNetworkId.endsWith("1")}
	def switch2 = children.find{it.deviceNetworkId.endsWith("2")}
    
    if(msg?.json?.Relay1){
        log.info "Relay 1: ${msg.json.Relay1}"
        if (switch1) { switch1.sendEvent(name: "switch", value: msg.json.Relay1) }
    }
    if(msg?.json?.Relay2){
        log.info "Relay 2: ${msg.json.Relay2}"
        if (switch2) { switch2.sendEvent(name: "switch", value: msg.json.Relay2) }
    }
    if(msg?.json?.Temperature){
        if(msg?.json?.isRaw){
            log.info "Temperature (Raw): ${msg.json.Temperature}"
            def temperature = roundValue( (msg.json.Temperature.toInteger() / 1000) * 1.8 + 32 )
            log.info "Temperature: ${temperature}"
            sendEvent(name: "temperature", value: temperature)
        }
        else{
            log.info "Temperature: ${msg.json.Temperature}"
            sendEvent(name: "temperature", value: roundValue(msg.json.Temperature))
        }
    }
    if(msg?.json?.Humidity){
        if(msg?.json?.isRaw){
            log.info "Humidity (Raw): ${msg.json.Humidity}"
            def humidity = roundValue(msg.json.Humidity.toInteger() / 1000)
            log.info "Humidity: ${humidity}"
            sendEvent(name: "humidity", value: humidity)
        }
        else{
            log.info "Humidity: ${msg.json.Humidity}"
            sendEvent(name: "Humidity", value: roundValue(msg.json.Humidity))
        }
    }
    if(msg?.json?.Proximity){
        if(msg?.json?.isRaw){
            log.info "Proximity (RAW): ${msg.json.Proximity}"
            def prox = parseProximity(msg.json.Proximity)
            log.info "Proximity: ${prox}"
            sendEvent(name: "proximityRaw", value: msg.json.Proximity)
            sendEvent(name: "proximity", value: prox)
        }
        else{
            log.info "Proximity: ${msg.json.Proximity}"
            sendEvent(name: "proximity", value: msg.json.Proximity)
        }
    }
    if(msg?.json?.LCDBacklight){
        log.info "LCD Backlight: ${msg.json.LCDBacklight}"
        sendEvent(name: "screenBacklight", value: msg.json.LCDBacklight)
    }
    if(msg?.json?.BottomButton){
        log.info "Bottom Button: ${msg.json.BottomButton}"
        sendEvent(name: "bottomButton", value: msg.json.BottomButton)
    }
    if(msg?.json?.TopButton){
        log.info "Top Button: ${msg.json.TopButton}"
        sendEvent(name: "topButton", value: msg.json.TopButton)
    }

    //if both relays are on and the switch isn't currently on, let's raise that value
    if((switch1.currentValue("switch") == "on" || switch2.currentValue("switch") == "on") && device.currentValue("switch") != "on"){
        sendEvent(name: "switch", value: "on")
    }
    //and same in reverse
    if(switch1.currentValue("switch") == "off" && switch2.currentValue("switch") == "off" && device.currentValue("switch") != "off"){
        sendEvent(name: "switch", value: "off")
    }
}

def roundValue(x){
	Math.round(x * 10) / 10
}

def parseProximity(proxRaw){
	//split on spaces and grab the first value
    proxRaw.split(" ")[0] as Integer
}

//for now, we'll just have these turn on both relays
//in the future, we plan on providing the ability to disable either relay via the Android app
def on(){
    def action = []
    action << relay1On()
    action << relay2On()
    return action
}
def off(){
    def action = []
    action << relay1Off()
    action << relay2Off()
    return action
}

//commands passed up from the child devices
def relayOn(String dni){
	switch(getChildId(dni)){
    	case 1: sendHubCommand( relay1On() ); break;
        case 2: sendHubCommand( relay2On() ); break;
    }
}
def relayOff(String dni){
	switch(getChildId(dni)){
    	case 1: sendHubCommand( relay1Off() ); break;
        case 2: sendHubCommand( relay2Off() ); break;
    }
}
def relayToggle(String dni){
	switch(getChildId(dni)){
    	case 1: sendHubCommand( relay1Toggle() ); break;
        case 2: sendHubCommand( relay2Toggle() ); break;
    }
}
def getChildId(String dni){
	//trim off the parent DNI and the .
    dni.split(".switch")[-1] as Integer
}

//TODO: change actions to POST commands on the server and here
def relay1On(){
    httpGET("/relay/top/on")
}
def relay1Off(){
    httpGET("/relay/top/off")
}
def relay1Toggle(){} //TODO: implement relay1 toggle

def relay2On(){
    httpGET("/relay/bottom/on")
}
def relay2Off(){
    httpGET("/relay/bottom/off")
}
def relay2Toggle(){} //TODO: implement relay2 toggle

def screenBacklightOn(){ httpGET("/lcd/backlight/on") }
def screenBacklightOff(){ httpGET("/lcd/backlight/off") }
def screenBacklightToggle(){}

//Individual commands for retrieving the status of the Wink Relay over HTTP
def retrieveRelay1(){ httpGET("/relay/top") }
def retrieveRelay2(){ httpGET("/relay/bottom")}
def retrieveTemperature(){ httpGET("/sensor/temperature/raw") }
def retrieveHumidity(){ httpGET("/sensor/humidity/raw") }
def retrieveProximity(){ httpGET("/sensor/proximity/raw") }
def retrieveScreenBacklight(){ httpGET("/lcd/backlight") }

def setupEventSubscription(){
    log.debug "Subscribing to events from Wink Relay"
    def result = new physicalgraph.device.HubAction(
            method: "POST",
            path: "/subscribe",
            headers: [
                    HOST: getHostAddress(),
                    CALLBACK: getCallBackAddress()
            ]
    )
    //log.debug "Request: ${result.requestId}"
    return result
}



def poll(){
    refresh()
}
def refresh(){
    //manually get the state of sensors and relays via HTTP calls
    def httpCalls = []
    httpCalls << retrieveRelay1()
    httpCalls << retrieveRelay2()
    httpCalls << retrieveTemperature()
    httpCalls << retrieveHumidity()
    httpCalls << retrieveProximity()
    httpCalls << retrieveScreenBacklight()
    httpCalls << setupEventSubscription()
    return httpCalls
}


def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
    refresh()
}

def httpGET(path) {
	def hostUri = hostAddress
    log.debug "Sending command ${path} to ${hostUri}"
    def result = new physicalgraph.device.HubAction(
            method: "GET",
            path: path,
            headers: [
                    HOST: hostUri
            ]
    )
    //log.debug "Request: ${result.requestId}"
    return result
}




// gets the address of the Hub
private getCallBackAddress() {
    return "http://" + device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}