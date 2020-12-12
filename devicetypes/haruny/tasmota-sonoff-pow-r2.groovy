/**
 *  Copyright 2019 Harun Yayli
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
 *  Sonoff POW R2 Wifi Switch with Tasmota  V 0.1
 *  Tested with Tasmota 6.5.0
 *
 *  Author: Harun Yayli
 *  Date: 2019-07-02
 *
 *
 */
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "Tasmota Sonoff POW R2 Wifi Switch", namespace: "haruny", author: "Harun Yayli", vid:"generic-switch-power-energy") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
        capability "Configuration"
        capability "Voltage Measurement"
		capability "Power Meter"
        capability "Health Check"
        capability "Outlet"
	}

	simulator {
	}
    
    preferences {
          input name: "ipAddr", type: "text", title: "IP Address", description: "IP Address of the device", required: true,displayDuringSetup: true
          input name: "port", type: "number", title: "Port", description: "Port of the device",  defaultValue: 80 ,displayDuringSetup: true
          input name: "username", type: "text", title: "Username", description: "Username to manage the device", required: true, displayDuringSetup: true
          input name: "password", type: "password", title: "Password", description: "Username to manage the device", required: true, displayDuringSetup: true
          
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#00a0dc", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "turningOn", label:'Turning On', action:"switch.off", backgroundColor:"#00a0dc", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "turningOff", label:'Turning Off', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOff"
			}
        }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
        valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "default", label:'${currentValue} V'
		}
		valueTile("amperage", "device.amperage", width: 2, height: 2) {
			state "default", label:'${currentValue} A'
		}
        valueTile("amperage", "device.amperage", width: 2, height: 2) {
			state "default", label:'${currentValue} A'
		}
 }

	main(["switch"])
	details(["switch", "power", "amperage", "voltage", "refresh"])
}

def installed() {
	configure()
}

def configure() {
}

private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private callTasmota(cll){
    def hubip = device.hub.getDataValue("localIP")
    def hubport = device.hub.getDataValue("localSrvPortTCP")
    def hosthex = convertIPtoHex(ipAddr)
    def porthex = convertPortToHex(port)
    
	device.deviceNetworkId = "$hosthex:$porthex" 
    
try {
		def hubAction = new physicalgraph.device.HubAction(
			method: "GET",
			path: "/cm?user=${username}&password=${password}&cmnd=${cll}",
            headers: [
            	HOST: "${ipAddr}:${port}"
            ]
			)
        sendHubCommand(hubAction)
        return hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
    return null
}
private updateTasmotaStatus(){
    callTasmota("Status%208")
}

def updated() {
    if (ipAddr!=null){
    	device.deviceNetworkId  = ipAddr
        callTasmota("Backlog%20Weblog%204")
        sendEvent(name: "checkInterval", value: 5, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    	updateTasmotaStatus()
    }
}

def parse(description) {
    def msg = parseLanMessage(description)
    if (msg.json !=null){
        if (msg.json.StatusSNS!=null) {
            if (msg.json.StatusSNS.ENERGY.Voltage>0) { // I'm running.
            	if (device.currentValue("switch") == "off" ) {
                	sendEvent(name: "switch", value: 'on')
                }
            	runIn(1, ping)
            }
            else{
            	if (device.currentValue("switch") == "on" ) {
                	sendEvent(name: "switch", value: 'off')
                }
            }
			sendEvent(name:"power", value: msg.json.StatusSNS.ENERGY.Power, unit: "W", displayed: true)
            sendEvent(name:"voltage", value: msg.json.StatusSNS.ENERGY.Voltage, unit: "V", displayed: true)
			sendEvent(name:"amperage", value: msg.json.StatusSNS.ENERGY.Current, unit: "A", displayed: true)
       	}
        else if (msg.json.Status!=null) {
            if (msg.json.Status.Power == "1"){
            	ping()
                sendEvent(name: "switch", value: 'on')
            } 
            else if (msg.json.Status.Power=="0"){
                sendEvent(name: "switch", value: 'off')
            }
		}
        else if (msg.json.POWER!=null) {
            if (msg.json.POWER == "ON"){
            	ping()
                sendEvent(name: "switch", value: 'on')
            } 
            else{
                sendEvent(name: "switch", value: 'off')
            }
        }
    }
    else{
    	log.debug "Something else" + description
    }
}

def on() {
	 callTasmota("Power%20On")
     refresh()
}

def off() {
    callTasmota("Power%20Off")
}

def refresh() {
    callTasmota("Status")
    updated()
}

def ping(){
	callTasmota("Status%208")
}
