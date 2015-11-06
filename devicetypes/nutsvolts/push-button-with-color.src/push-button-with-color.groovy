/**
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
	definition (name: "Push Button with Color", namespace: "NutsVolts", author: "John.Rucker@Solar-Current.com") {
	
    capability "Actuator"
    capability "Configuration"
    capability "Refresh"
	capability "Sensor"
    capability "Switch"
	capability "Switch Level"
	capability "Color Control"  
    
    attribute "info","string"
    
	fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,FF00", outClusters: "0019"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"sent"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"sent"
				attributeState "sent", label: 'wait', icon: "st.motion.motion.active", backgroundColor: "#ffa81e"  
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}		      
        
		main(["switch"])
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	//log.debug "parse called with --> $description"
	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
    else if (description?.startsWith('on/off')) {
    	map = parseOnOff(description)
    }
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}  
    else {
    	log.debug "No parse method for: $description"
    }
    
    if (map.value == "Device Boot"){
    	def result = []
    	List cmds = bootResponse()
    	log.trace "Sending current state to device ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }  
        return result 
    }else{
    	log.trace map
		return map ? createEvent(map) : null
    }
}

private Map parseReportAttributeMessage(String description) {
	//log.debug "Map parseReportAttributeMessage called with --> $description"
    Map resultMap = [:]
    def descMap = parseDescriptionAsMap(description)
    //log.debug descMap
   
    if (descMap.cluster == "0008" && descMap.attrId == "0000") { 
        resultMap.name = "level"
        resultMap.value = (Integer.parseInt(descMap.value, 16))      
        resultMap.displayed = true  
        def v =(int)100 / (255/resultMap.value)
        if (v < 1){
        	resultMap.value = 1
        }else{
        	resultMap.value = v
        }
    }
    else if (descMap.cluster == "0008" && descMap.attrId == "0400") { 
        def cx = descMap.value
        cx = cx.substring(2, cx.length())												// Remove two 0 from front of string    
        resultMap.name = "color"        
        resultMap.value = "#${cx}"
        resultMap.displayed = true  
        setColorMapFromHexRGB(cx)														// Set Hue and Sat based on RGB Hex string
    }    
    
    else {
    	log.debug "Attribute match not found for --> $descMap"
    }
    return resultMap
}

private Map parseOnOff(String description) {
	//log.debug "Map parseOnOff called with --> $description"
    Map resultMap = [:]    
    
    if(description?.endsWith("0")) {
        resultMap.name = "switch"
        resultMap.value = "off"
        resultMap.displayed = true
    }    
    else if(description?.endsWith("1")) {
        resultMap.name = "switch"
        resultMap.value = "on"
        resultMap.displayed = true
    }  
    else if(description?.endsWith("2")) { 
        resultMap.name = "info"
        resultMap.value = "Device Boot"
        resultMap.displayed = true    
    }      
    
    else {
    	log.debug "On/Off match not found for --> $description"
    }    
    return resultMap
}

private Map parseCatchAllMessage(String description) {
	//log.debug "Map parseCatchAllMessage called with --> $description"
    Map resultMap = [:]    
    def cluster = zigbee.parse(description)
    //log.debug cluster
    
    if (cluster.clusterId == 0x0006 && cluster.command == 0x01) {			// command 0x01 = report attribute
    	//log.trace "On Off Cluster report = $cluster.data"
        switch(cluster.data) {
        
        case "[0, 0, 0, 16, 0]":							// Switch is off attribute report   
        resultMap.name = "switch"
        resultMap.value = "off" 
        resultMap.displayed = true
        break     
        
        case "[0, 0, 0, 16, 1]":							// Switch is on attribute report       
        resultMap.name = "switch"
        resultMap.value = "on"   
        resultMap.displayed = true
        break                 
        }
    }
    else if (cluster.clusterId == 0x0006 && cluster.command == 0x0B) {			// command 0x0B = default response to command sent
    	//log.trace "On Off Cluster default response = $cluster.data"
        switch(cluster.data) {
        
        case "[0, 0]":															// Switch acknowledged off command   
        resultMap.name = "switch"
        resultMap.value = "off" 
        resultMap.displayed = false
        break     
        
        case "[1, 0]":															// Switch acknowledged on command        
        resultMap.name = "switch"
        resultMap.value = "on"        
        resultMap.displayed = false
        break                 
        }
    } 
    else if (cluster.clusterId == 0x0008 && cluster.command == 0x0B) {			// command 0x0B = default response to command sent
    	//log.trace "level Cluster default response = $cluster.data"  
        switch(cluster.data) {
        
        case "[0, 0]":															// Level command acknowledged  
        resultMap.name = "info"
        resultMap.value = "set level cmd ack"         
        resultMap.displayed = false        
        break                  
        }
   } 
    else if (cluster.clusterId == 0x0008 && cluster.command == 0x04) {			// command 0x04 = write attribuite cmd response
    	//log.trace "level Cluster default response = $cluster.data"  
        switch(cluster.data) {
        
        case "[0, 4, 0]":													  	// setColor command acknowledged  
        resultMap.name = "info"
        resultMap.value = "write color attribute ack"        
        resultMap.displayed = false        
        break             
        }
   }    
    else {
    	log.debug "CatchAll match not found for --> $description"
        log.debug "cluster.data = $cluster"
    }        
    
    return resultMap
}

// Commands
def bootResponse() {
	log.debug "Creating boot response"
    def swtch = device.currentState("switch")?.value												// Get the current on off value
    if (swtch == "on"){
        swtch = "1"								
    }else{
        swtch = "0"							
    }    
    def lvl = device.currentState("level")?.value as int											// Get the current brightness level
    lvl=(int)lvl * 2.56																				// Scale value basee on 1 to 256 for level
    def level = hexString(Math.round(lvl))     														
    def cx = device.currentState("color")?.value													// Get the current color value
	cx = cx.substring(1, cx.length())																// Remove # from front of hex value.hex string     
	[
       	"st cmd 0x${device.deviceNetworkId} 1 6 ${swtch} {}", "delay 200",							// Set On or Off
        "st cmd 0x${device.deviceNetworkId} 1 8 0 {${level} 0000}", "delay 200",
		"st wattr 0x${device.deviceNetworkId} 0x38 0x0008 0x400 0x23 {${cx}}", "delay 200"			// Send new RGB Color value write attribute 0x0400        
	]
}

def on() {
	log.info "on cmd sent"
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	log.info "off cmd sent"
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def setLevel(value) {
    if (value < 1){
        value = 1
    }else if (value > 99){
        value = 99
    }
    sendEvent(name: "level", value: value, displayed: false)    
    def cLevel=(int)value * 2.56													// Scale value basee on 1 to 256 for level
    log.info "Level ${value}% sacle = ${cLevel} sent"
	def cmds = []
    def level = hexString(Math.round(cLevel))
	cmds << "st cmd 0x${device.deviceNetworkId} 1 8 0 {${level} 0000}"
	cmds
}

def setColor(value) {
	log.trace"setColor = ${value}"    
    def cx = value.hex    
    cx = cx.substring(1, cx.length())														// Remove # from front of hex value.hex string
    def cmds = []
    cmds << "st wattr 0x${device.deviceNetworkId} 0x38 0x0008 0x400 0x23 {${cx}}"			// Send new RGB Color value write attribute 0x0400
    cmds        
}

def refresh() {
	log.info "read attributes request sent"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0006 0x0000"     					// Read On / Off attribute
    cmd << "delay 500"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0008 0x0000"						// Read Level attribute 
    cmd << "delay 500"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0008 0x0400"    					// Read Custom attribute for RGB color value
    return cmd
}

def configure() {
    log.debug "Binding SEP 0x38 DEP 0x01 Cluster 0x0006 ON/Off cluster to hub"  
    log.debug "Binding SEP 0x38 DEP 0x01 Cluster 0x0008 Level cluster to hub"      
    
    def cmd = []
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x0006 {${device.zigbeeId}} {}"	// Bind to end point 0x38 and the On/Off Cluster
    cmd << "delay 150"
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x0008 {${device.zigbeeId}} {}"   // Bind to end point 0x38 and the Level Cluster
    cmd << "delay 1500"       
    
    return cmd + refresh() // send refresh cmds as part of config
}

// Utils

def setColorMapFromHexRGB(HexRGB) {
    //log.info "setColrMapFromHexRGB called with ${HexRGB}"    
    def r = Integer.parseInt(HexRGB.substring(0, HexRGB.length()-4), 16)
    def g = Integer.parseInt(HexRGB.substring(2, HexRGB.length()-2), 16)
    def b = Integer.parseInt(HexRGB.substring(4, HexRGB.length()), 16)
	def cCal = rgbToHSV(r, g, b)
	sendEvent(name: "hue", value: cCal.hue)
	sendEvent(name: "saturation", value: cCal.saturation)        
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

/*
private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
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
*/


def rgbToHSV(red, green, blue) {
	float r = red / 255f
	float g = green / 255f
	float b = blue / 255f
	float max = [r, g, b].max()
	float delta = max - [r, g, b].min()
	def hue = 13
	def saturation = 0
	if (max && delta) {
		saturation = 100 * delta / max
		if (r == max) {
			hue = ((g - b) / delta) * 100 / 6
		} else if (g == max) {
			hue = (2 + (b - r) / delta) * 100 / 6
		} else {
			hue = (4 + (r - g) / delta) * 100 / 6
		}
	}
	[hue: hue, saturation: saturation, value: max * 100]
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}