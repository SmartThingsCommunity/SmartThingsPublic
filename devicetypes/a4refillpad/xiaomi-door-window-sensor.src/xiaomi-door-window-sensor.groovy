/**
 *  Xiaomi Door/Window Sensor
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
 * Based on original DH by Eric Maycock 2015 and Rave from Lazcad
 *  change log:
 *	added DH Colours
 *  added 100% battery max
 *  fixed battery parsing problem
 *  added lastcheckin attribute and tile
 *  added extra tile to show when last opened
 *
 */
metadata {
   definition (name: "Xiaomi Door/Window Sensor", namespace: "a4refillpad", author: "a4refillpad") {
   capability "Configuration"
   capability "Sensor"
   capability "Contact Sensor"
   capability "Refresh"
   capability "Battery"
   
   attribute "lastCheckin", "String"
   attribute "lastOpened", "String"
   
   fingerprint profileId: "0104", deviceId: "0104", inClusters: "0000, 0003", outClusters: "0000, 0004, 0003, 0006, 0008, 0005", manufacturer: "LUMI", model: "lumi.sensor_magnet", deviceJoinName: "Xiaomi Door Sensor"
   
   command "enrollResponse"
 
   }
    
   simulator {
      status "closed": "on/off: 0"
      status "open": "on/off: 1"
   }
    
   tiles(scale: 2) {
      multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
         tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
            attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
            attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
         }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
      }
      standardTile("icon", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label:'Last Opened:'
      }
      valueTile("lastopened", "device.lastOpened", decoration: "flat", inactiveLabel: false, width: 4, height: 1) {
			state "default", label:'${currentValue}'
		}
      valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
      standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
      }
      standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	  }

      main (["contact"])
      details(["contact","battery","configure","refresh","icon","lastopened"])
   }
}

def parse(String description) {
   log.debug "Parsing '${description}'"
   
//  send event for heartbeat    
   def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
   sendEvent(name: "lastCheckin", value: now)
    
   Map map = [:]

   log.debug "${resultMap}"
   if (description?.startsWith('on/off: ')) {
      map = parseCustomMessage(description) 
      sendEvent(name: "lastOpened", value: now)
	}
   if (description?.startsWith('catchall:')) 
      map = parseCatchAllMessage(description)
   log.debug "Parse returned $map"
   def results = map ? createEvent(map) : null

   return results;
}

private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)

	log.debug rawValue

	def result = [
		name: 'battery',
		value: '--'
	]
    
	def volts = rawValue / 1
    def maxVolts = 100

	if (volts > maxVolts) {
				volts = maxVolts
    }
   
    result.value = volts
	result.descriptionText = "${linkText} battery was ${result.value}%"

	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster
	if (cluster) {
		switch(cluster.clusterId) {
			case 0x0000:
			resultMap = getBatteryResult(cluster.data.last())
			break

			case 0xFC02:
			log.debug 'ACCELERATION'
			break

			case 0x0402:
			log.debug 'TEMP'
				// temp is last 2 data values. reverse to swap endian
				String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
				def value = getTemperature(temp)
				resultMap = getTemperatureResult(value)
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


def configure() {
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "${device.deviceNetworkId}"
    def endpointId = 1
    log.debug "${device.zigbeeId}"
    log.debug "${zigbeeEui}"
	def configCmds = [
			//battery reporting and heartbeat
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 1 {${device.zigbeeId}} {}", "delay 200",
			"zcl global send-me-a-report 1 0x20 0x20 600 3600 {01}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500",


			// Writes CIE attribute on end device to direct reports to the hub's EUID
			"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1", "delay 500",
	]

	log.debug "configure: Write IAS CIE"
	return configCmds
}

def enrollResponse() {
	log.debug "Enrolling device into the IAS Zone"
	[
			// Enrolling device into the IAS Zone
			"raw 0x500 {01 23 00 00 00}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1"
	]
}

def refresh() {
	log.debug "Refreshing Battery"
    def endpointId = 0x01
	[
	    "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0000 0x0000", "delay 200"

	] //+ enrollResponse()
}

private Map parseCustomMessage(String description) {
   def result
   if (description?.startsWith('on/off: ')) {
      if (description == 'on/off: 0') 		//contact closed
         result = getContactResult("closed")
      else if (description == 'on/off: 1') 	//contact opened
         result = getContactResult("open")
      return result
   }
}

private Map getContactResult(value) {
   def linkText = getLinkText(device)
   def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
   return [
      name: 'contact',
      value: value,
      descriptionText: descriptionText
	]
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}
private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
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