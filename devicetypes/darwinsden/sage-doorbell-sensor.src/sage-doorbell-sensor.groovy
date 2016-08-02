/**
 *  SAGE Doorbell Sensor
 *
 *  For device information and images, questions or to provide feedback on this device handler, 
 *  please visit: 
 *
 *      darwinsden.com/sage-doorbell
 *
 *  White wire: common
 *  Green wire: doorbell 1 / front
 *  Yellow wire: doorbell 2 / back
 *
 * Factory reset:
 * Remove the plastic cover and the battery.
 * Press and hold the tiny RESET button (next to where the wires attach to the circuit board) while you reinstall the battery.
 * Continue holding RESET until the red LED blinks.
 *
 *  Copyright 2016 DarwinsDen.com
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
 *	Author: Darwin@DarwinsDen.com
 *	Date: 2016-06-13
 *
 *	Changelog:
 *
 *  0.20 (08/02/2016) -	Added preference option for allowd time between presses to eliminate duplicate notifications on some systems
 *	0.10 (06/13/2016) -	Initial 0.1 pre-beta Test Code
 *
 */
 
metadata {
	definition (name: "SAGE Doorbell Sensor", namespace: "darwinsden", author: "darwin@darwinsden.com") {
    	capability "Battery"
		capability "Configuration"
        capability "Button"
		capability "Refresh"
     
        command "enrollResponse"
 	   
        fingerprint endpointId: "12", inClusters: "0000,0003,0009,0001", outClusters: "0003,0006,0008,0019", model: "Bell", manufacturer: "Echostar"
    }
    
    simulator {
    
    }
    
    preferences {
        input "timeBetweenPresses", "number", title: "Seconds allowed between presses (increase this value to eliminate duplicate notifications)",  defaultValue: 10,  displayDuringSetup: true, required: false	
    } 
 
	tiles(scale: 2) {
		standardTile("button1", "device.button1", width: 3, height: 3) {
			state "1: ring", label: '${name}', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffa81e"
			state "1: silent", label: '${name}', icon: "st.Home.home30", backgroundColor: "#79b821"
		}

		standardTile("button2", "device.button2", width: 3, height: 3) {
			state "2: ring", label: '${name}', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffa81e"
			state "2: silent", label: '${name}', icon: "st.Home.home30", backgroundColor: "#79b821"
		}
        
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["button1","button2"])
		details(["button1","button2","battery","refresh"])
	}
}
 
def parse(String description) {
	//log.debug "description: $description"
    
	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
    else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
    
	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : null
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}
 
private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) { 
        log.debug ("cluster: $cluster")
        switch(cluster.clusterId) {
            case 0x0006:
            	resultMap = getDoorbellPressResult(cluster)
                break
        }
    }
    return resultMap
}

private boolean shouldProcessMessage(cluster) {
    // 0x0B is default response indicating message got through
    // 0x07 is bind message
    boolean ignoredMessage = cluster.profileId != 0x0104 || 
        cluster.command == 0x0B ||
        cluster.command == 0x07 ||
        (cluster.data.size() > 0 && cluster.data.first() == 0x3e)
    return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		resultMap = getBatteryResult(Integer.parseInt(descMap.value, 16))
	}
 
	return resultMap
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [
		name: 'battery',
		value: '--',
		translatable: true
	]

	def volts = rawValue / 10

	if (rawValue == 0 || rawValue == 255) {}
	else {
		if (volts > 3.5) {
			result.descriptionText = "{{ device.displayName }} battery has too much power: (> 3.5) volts."
		}
		else {
			if (device.getDataValue("manufacturer") == "SmartThings") {
				volts = rawValue // For the batteryMap to work the key needs to be an int
				def batteryMap = [28:100, 27:100, 26:100, 25:90, 24:90, 23:70,
								  22:70, 21:50, 20:50, 19:30, 18:30, 17:15, 16:1, 15:0]
				def minVolts = 15
				def maxVolts = 28

				if (volts < minVolts)
					volts = minVolts
				else if (volts > maxVolts)
					volts = maxVolts
				def pct = batteryMap[volts]
				if (pct != null) {
					result.value = pct
					result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
				}
			}
			else {
				def minVolts = 2.1
				def maxVolts = 3.0
				def pct = (volts - minVolts) / (maxVolts - minVolts)
				result.value = Math.min(100, (int) pct * 100)
				result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
			}
		}
	}

	return result
}

private Map getDoorbellPressResult(cluster) {
    def linkText = getLinkText(device)
    def buttonNumber = (cluster.command as int)
    def result = [:]
    
    // map buttons per Hughes described defaults for green and yellow wires
    
    switch(buttonNumber) {
        case 0: 
            if (!isDuplicateCall(state.lastButton2Updated, state.timeBetweenPresses) )
            {
		       //log.debug ("BUTTON2 PRESS!")               
               result = [ name: 'button', value: "pushed", data: [buttonNumber: 2], isStateChange: true ]
               sendEvent([name: "button2", value: "2: ring"])
               runIn(5, button2DisplayReset) 
            }
            state.lastButton2Updated = new Date().time	
            break

        case 1: 
            if (!isDuplicateCall(state.lastButton1Updated, state.timeBetweenPresses) )
            {		
		       //log.debug ("BUTTON1 PRESS!")
               result = [ name: 'button', value: "pushed", data: [buttonNumber: 1], isStateChange: true]
               sendEvent ([name: "button1", value: "1: ring"])
               runIn(5, button1DisplayReset) 
            }
            state.lastButton1Updated = new Date().time
            break
    }
    
    return result
}

def button1DisplayReset() {
    sendEvent ([name: "button1", value: "1: silent"])
}

def button2DisplayReset() {
    sendEvent ([name: "button2", value: "2: silent"])
}


def refresh() {
	log.debug "Refreshing Battery"
    
    def refreshCmds = [
        "st rattr 0x${device.deviceNetworkId} 18 0x0001 0x20", "delay 500", 
	]
    button1DisplayReset()
    button2DisplayReset()
    
    setPrefs()
    
	return refreshCmds + enrollResponse()
}

def configure() {

    setPrefs()
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = [
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 500",
		"zcl global send-me-a-report 1 0x20 0x20 30 21600 {01}",		//checkin time 6 hrs
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x402 {${device.zigbeeId}} {}", "delay 500",
		"zcl global send-me-a-report 0x402 0 0x29 30 3600 {6400}",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500"
	]
    return configCmds + refresh() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	[
		//Resending the CIE in case the enroll request is sent before CIE is written
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}",
		"send 0x${device.deviceNetworkId} 1 1", "delay 200"
	]
}

private isDuplicateCall(lastRun, allowedEverySeconds) {
	def result = false
	if (lastRun) {
		result =((new Date().time) - lastRun) < (allowedEverySeconds * 1000)
	}
	result
}

def setPrefs() 
{
   log.debug ("setting preferences")
   if (timeBetweenPresses == null)
   {
      state.timeBetweenPresses = 10
   }
      else if (timeBetweenPresses < 0)
   {
      state.timeBetweenPresses = 0
   }
   else
   {
      state.timeBetweenPresses = timeBetweenPresses
   }  
}

def updated()
{
   setPrefs()
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}