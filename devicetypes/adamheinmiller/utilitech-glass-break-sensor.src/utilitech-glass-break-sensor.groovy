/**
 *  Utilitech Glass Break Sensor
 *
 *  Author: Adam Heinmiller
 *
 *  Date: 2014-11-09
 */

metadata 
{
    definition (namespace: "adamheinmiller", name: "Utilitech Glass Break Sensor", author: "Adam Heinmiller") 
    {
        capability "Contact Sensor"
		capability "Battery"
        
        fingerprint deviceId:"0xA102", inClusters:"0x20, 0x9C, 0x80, 0x82, 0x84, 0x87, 0x85, 0x72, 0x86, 0x5A"
	}

    simulator 
    {
		status "Activate Sensor": "command: 9C02, payload: 26 00 FF 00 00"
		status "Reset Sensor": "command: 9C02, payload: 26 00 00 00 00"

        status "Battery Status 25%": "command: 8003, payload: 19"
        status "Battery Status 50%": "command: 8003, payload: 32"
        status "Battery Status 75%": "command: 8003, payload: 4B"
        status "Battery Status 100%": "command: 8003, payload: 64"
	}

    tiles 
    {
        standardTile("contact", "device.contact", width: 2, height: 2) 
        {
            state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
            state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#FF0000"
        }
        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") 
        {
            state "battery", label:'${currentValue}% battery', unit:""
        }
        
        main "contact"
        details(["contact", "battery"])
    }
}

def installed()
{
	
	updated()
}

def updated()
{

}

def getTimestamp() 
{
    return new Date().time
}

def getBatteryLevel(int pNewLevel)
{
	def bl = state.BatteryLevel ?: [pNewLevel, pNewLevel, pNewLevel] as int[]

	def iAvg = 4 + ((int)(pNewLevel + bl[0] + bl[1] + bl[2]) / 4)
    
    state.BatteryLevel = [pNewLevel, bl[0], bl[1]]
    
    //log.debug "New Bat Level: ${iAvg - (iAvg % 5)}, $state.BatteryLevel" 
    
    return iAvg - (iAvg % 5)
}



def parse(String description) 
{
    def result = []
    
    // "0x20, 0x9C, 0x80, 0x82, 0x84, 0x87, 0x85, 0x72, 0x86, 0x5A"
    
    def cmd = zwave.parse(description)
    
    
    //log.debug "Parse:  Desc: $description, CMD: $cmd"
    
    if (cmd) 
    {
        result << zwaveEvent(cmd)
	}
    
    return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) 
{
    logCommand(cmd)

	def result = []
    

    result << response(zwave.wakeUpV2.wakeUpNoMoreInformation())

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) 
{
    logCommand(cmd)

	def result = []

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) 
{
    logCommand(cmd)

	def result = []

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) 
{
    logCommand(cmd)

    def result = [name: "battery", unit: "%", value: getBatteryLevel(cmd.batteryLevel)]
    
    return createEvent(result)
}


def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) 
{
    logCommand(cmd)

	
    def result = [name: "contact"]
    
    if (cmd.sensorState == 0)
    {
    	result += [value: "closed", descriptionText: "${device.displayName} has reset"]
    }
    else if (cmd.sensorState == 255)
    {
    	result += [value: "open", descriptionText: "${device.displayName} detected broken glass"]
    }


    return createEvent(result)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) 
{
    logCommand("**Unhandled**: $cmd")

	return createEvent([descriptionText: "Unhandled: ${device.displayName}: ${cmd}", displayed: false])
}


def logCommand(cmd)
{
	log.debug "Device Command:  $cmd"
}