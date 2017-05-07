metadata 
{
	definition(name: "Lumi's Motion sensor", namespace: "lumi", author: "phuclm") 
	{
		/* What capability of device?! */
		capability "Sensor" // is tagging
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"

		capability "Actuator"	// is tagging
		capability "Configuration"
		capability "Refresh"

		/* Custom commands and attributes */
		// command "enrollResponse"

		/* LM-PIR fingerprint */
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0500,0502", manufacturer: "Lumi", model: "LM-PIR"
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0500,0502", manufacturer: "Lumi R&D", model: "LM-PIR"
	}

	/* 6 wide - unlimited height */
	tiles(scale: 2) 
	{
    	multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
			}
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 32, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 92, color: "#d04e00"],
				[value: 98, color: "#bc2323"]
			]
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}

		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} ${unit}', unit:"lux"
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["motion", "temperature", "humidity", "illuminance"])
		details(["motion", "temperature", "humidity", "illuminance", "battery", "refresh"])
	}
}

/* **********************************************************************************************************************
 * Parse raw command was recived from device
 * It's may ZCL CLI command
 * */
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private def parseDescriptionAsMap(description)
{
	if (description?.startsWith("read attr -")) 
    {
		(description - "read attr - ").split(",").inject([:]) 
		{ 
			map, param ->
			def nameAndValue = param.split(":")
			map += [(nameAndValue[0].trim()): nameAndValue[1].trim()]
		}
	}
	else if (description?.startsWith("catchall: ")) 
    {
		def seg = (description - "catchall: ").split(" ")

		def zigbeeMap = [:]

		zigbeeMap += [raw: (description - "catchall: ")]
		zigbeeMap += [profileId: seg[0]]
		zigbeeMap += [cluster: seg[1]]
		zigbeeMap += [endpoint: seg[2]]
		zigbeeMap += [destinationEndpoint: seg[3]]
		zigbeeMap += [options: seg[4]]
		zigbeeMap += [messageType: seg[5]]
		zigbeeMap += [dni: seg[6]]
		zigbeeMap += [isClusterSpecific: Short.valueOf(seg[7], 16) != 0]
		zigbeeMap += [isManufacturerSpecific: Short.valueOf(seg[8], 16) != 0]
		zigbeeMap += [manufacturerId: seg[9]]
		zigbeeMap += [command: seg[10]]
		zigbeeMap += [direction: seg[11]]
		zigbeeMap += [data: seg.size() > 12 ? 
						seg[12].split("").findAll{it}.collate(2).collect{it.join('')} : 
						[] 
					 ]

		zigbeeMap
	}
}

def parse(String description) 
{
	//log.debug "RECV RAW: $description"
    
    def finalResult = isKnownDescription(description)
    
    log.debug "isKnownDescription() returned: $finalResult"
    
    if ((finalResult != null) && (finalResult != "false"))
    {
    	def result = createEvent(name: finalResult.type, value: finalResult.value)
    	log.debug "Parse returned ${result}"
		return result
    }
}

private def isKnownDescription(description) {
	if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) 
    {
		def descMap = parseDescriptionAsMap(description)
        
        switch (descMap.cluster) 
        {
        	case "0500":
            	log.debug "MOTION"
                isMotionStateUpdate(descMap)
            	break;
           	case "0400":
            	log.debug "LUX"
                isLuxValUpdate(descMap)
            	break;
           	case "0402":
            	log.debug "TEMP"
                isTempValUpdate(descMap)
            	break;
            case "0405":
            	log.debug "HUMI"
                isHumiValUpdate(descMap)
            	break;
            case "0001":
            	log.debug "BAT"
                isBatPercUpdate(descMap)
            	break;
            default:
            	break;
        }
	}
}

def isMotionStateUpdate(descMap)
{
	if (descMap.data == null) // is read attr -
	{
    	return "false"
	}
	else 					 // is catchall:
	{
		if (descMap.data[4] == "01")
        	return [type: "motion", value: "active"]
        else if (descMap.data[4] == "00")
        	return [type: "motion", value: "inactive"]
	}
}

def isLuxValUpdate(descMap)
{
	if (descMap.data == null) // is read attr -
	{
		return "false"
	}
	else 					 // is catchall:
	{
    	def luxVal = convertHexToInt(descMap.data[5])*16 + convertHexToInt(descMap.data[4])
		return [type: "illuminance", value: luxVal]
	}
}

def isTempValUpdate(descMap)
{
	if (descMap.data == null) // is read attr -
	{
		return "false"
	}
	else 					 // is catchall:
	{
		def tempVal = convertHexToInt(descMap.data[5])*16 + convertHexToInt(descMap.data[4])
		return [type: "temperature", value: tempVal]
	}
}

def isHumiValUpdate(descMap)
{
	if (descMap.data == null) // is read attr -
	{
		return "false"
	}
	else 					 // is catchall:
	{
		def humiVal = convertHexToInt(descMap.data[5])*16 + convertHexToInt(descMap.data[4])
		return [type: "humidity", value: humiVal]
	}
}

def isBatPercUpdate(descMap)
{
	if (descMap.data == null) // is read attr -
	{
		if (descMap.attrId == "0021" && descMap.result == "success")
		{
            return [type: "battery", value: convertHexToInt(descMap.value)]
		}
	}
	else 					 // is catchall:
	{
		return "false"
	}
}

/* **********************************************************************************************************************
 * Impleiment commands maybe use for SEND TO device
 * Pref. CLI Silabs's
 * */
def refresh() 
{
	def readInitValCmd = 
	[
    	"zcl global read 0x0500 0x0002", "delay 100",
        "send 0x${device.deviceNetworkId} 0x01 0x01", "delay 100",
        
        "zcl global read 0x0400 0x0000", "delay 100",
        "send 0x${device.deviceNetworkId} 0x01 0x02", "delay 100",
        
        "zcl global read 0x0402 0x0000", "delay 100",
        "send 0x${device.deviceNetworkId} 0x01 0x03", "delay 100",
        
        "zcl global read 0x0405 0x0000", "delay 100",
        "send 0x${device.deviceNetworkId} 0x01 0x04", "delay 100",
        
        "zcl global read 0x0001 0x0021", "delay 100",
        "send 0x${device.deviceNetworkId} 0x01 0x05", "delay 100",
	]

}

def configure() {
	refresh()
}