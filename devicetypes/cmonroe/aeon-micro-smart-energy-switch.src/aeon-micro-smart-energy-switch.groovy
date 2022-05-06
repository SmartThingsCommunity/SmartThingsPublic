/**
 *  Aeon Micro Smart Energy Switch
 *
 *  Copyright 2014 Chad Monroe (chad@monroe.io)
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

preferences 
{
    input( type: "enum", name: "wallSwitchType", title: "Wall Switch Type", options: wallSwitchTypes(), defaultValue: "Momentary", style: "segmented" )
}

metadata 
{
	definition ( name: "Aeon Micro Smart Energy Switch", namespace: "cmonroe", author: "chad@monroe.io" ) 
	{
		capability "Energy Meter"
		capability "Power Meter"
		capability "Switch"
		capability "Actuator"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "reset"

		fingerprint deviceId: "0x1001", inClusters: "0x25,0x32,0x27,0x2C,0x2B,0x70,0x85,0x56,0x72,0x86", outClusters: "0x82"
	}

	tiles 
	{
		standardTile( "switch", "device.switch", width: 2, height: 2, canChangeIcon: true ) 
		{
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		valueTile( "power", "device.power", decoration: "flat" ) 
		{
			state "default", label:'${currentValue} W'
		}

		valueTile( "energy", "device.energy", decoration: "flat" ) 
		{
			state "default", label:'${currentValue} kWh'
		}

		standardTile( "reset", "device.energy", inactiveLabel: false, decoration: "flat" ) 
		{
			state "default", label:'reset kWh', action:"custom.reset"
		}

		standardTile( "configure", "device.power", inactiveLabel: false, decoration: "flat" ) 
		{
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		standardTile( "refresh", "device.switch", inactiveLabel: false, decoration: "flat" ) 
		{
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main( ["switch"] )
		details( ["switch", "power", "energy", "reset", "configure", "refresh"] )
	}
}

def parse(String description) 
{
	//log.debug "parse() desc=($description)"

	def result
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def cmd = zwave.parse( description, [0x20: 1, 0x26: 1, 0x70: 1, 0x32: 2, 0x32: 3] )

	if ( cmd ) 
	{
		log.debug "Parse got cmd ${cmd}"
		result = createEvent( cmd, item1 )

		//if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602"))
		if (result?.name == 'hail')
		{		
			//result = [result, response(zwave.basicV1.basicGet())]
            //result = [result, response(refresh())]
			log.debug "Was hailed: requesting refresh"
		} 
	}
	else 
	{
		log.debug "Parse cmd is empty"
		item1.displayed = displayed( description, item1.isStateChange )
		result = [item1]
	}

	log.debug "Parse returned ${result?.descriptionText}"
    
	result
}

def createEvent(physicalgraph.zwave.commands.hailv1.Hail cmd, Map item1)
{
    log.debug "createEvent(hailv1.Hail) cmd=${cmd}"
    
	createEvent( [name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false] )
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) 
{
	log.debug "createEvent(basicv1.BasicReport) cmd=${cmd}"

	def result = doCreateEvent( cmd, item1 )

	for (int i = 0; i < result.size(); i++) 
	{
		result[i].type = "physical"
	}

	result
}

//def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) 
def createEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, Map item1)
{
	log.debug "createEvent(switchbinaryv1.SwitchBinaryReport) cmd=${cmd}"

	def result = doCreateEvent( cmd, item1 )

	for (int i = 0; i < result.size(); i++) 
	{
		result[i].type = "digital"
	}

	result
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) 
{
	log.debug "doCreateEvent() cmd=${cmd}"

	def result = [item1]

	item1.name = "switch"
	item1.value = cmd.value ? "on" : "off"
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange( device, item1.name, item1.value )
	item1.displayed = item1.isStateChange

	result
}

def createEvent(physicalgraph.zwave.commands.meterv2.MeterReport cmd, Map item1)
{
	log.debug "createEvent(meterv2.MeterReport) cmd=${cmd}"

	if (cmd.scale == 0) 
	{
		createEvent( [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"] )
	} 
	else if (cmd.scale == 1) 
	{
		createEvent( [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"] )
	}
	else 
	{
		createEvent( [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"] )
	}
}

def createEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, Map item1)
{
	log.debug "createEvent(meterv3.MeterReport) cmd=${cmd}"

	if (cmd.scale == 0) 
	{
		createEvent( [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"] )
	} 
	else if (cmd.scale == 1) 
	{
		createEvent( [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"] )
	}
	else if (cmd.scale == 2)
	{
		createEvent( [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"] )
	}
	else if (cmd.scale == 4)
	{
		createEvent( [name: "voltage", value: Math.round(cmd.scaledMeterValue), unit: "V"] )
	}
	else 
	{
		log.info "createEvent(meterv3.MeterReport) ignoring cmd with scale=${cmd.scale}"
	}
}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) 
{
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def on() 
{
	log.info "on"

	zwave.basicV1.basicSet( value: 0xFF ).format()
	//zwave.switchBinaryV1.SwitchBinarySet( switchValue: 0xFF ).format()
}

def off() 
{
	log.info "off"
    
	zwave.basicV1.basicSet( value: 0x00 ).format()
    ///zwave.switchBinaryV1.SwitchBinarySet( switchValue: 0x00 ).format()
}

def poll() 
{
	log.info "poll"

	delayBetween([
		zwave.switchMultilevelV2.switchMultilevelGet().format(),
		zwave.meterV3.meterGet( scale: 0 ).format(),
		zwave.meterV3.meterGet( scale: 2 ).format()
	])
}

def refresh() 
{
	log.info "refresh"

	delayBetween([
		zwave.switchMultilevelV2.switchMultilevelGet().format(),
		//zwave.sensorMultilevelV3.sensorMultilevelGet().format(),
		zwave.meterV3.meterGet( scale: 0 ).format(),
		zwave.meterV3.meterGet( scale: 2 ).format()
	])
}

def reset() 
{
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet().format()
	]
}

def configure() 
{
	log.info "configure"

	delayBetween([
    	/* content of multilevel sensor report.. 0 = power (default), 1 = voltage */
		zwave.configurationV1.configurationSet( parameterNumber: 1, size: 1, scaledConfigurationValue: 1 ).format(),
		
		/* enable overload protection.. 0 = disable (default), 1 = enable */
		zwave.configurationV1.configurationSet( parameterNumber: 3, size: 1, scaledConfigurationValue: 1 ).format(),

		/* enable instant reporting.. 0 = disable (default), 1 = hail, 2 = basic CC report */
		zwave.configurationV1.configurationSet( parameterNumber: 80, size: 1, scaledConfigurationValue: 1 ).format(),

		/* enable parameters 91 and 92 (min change in wattage or wattage % respectively).. 0 = disable (default), 1 = enable */
		zwave.configurationV1.configurationSet( parameterNumber: 90, size: 1, scaledConfigurationValue: 1 ).format(),
        /* report on change in wattage (default = 50W) */
		zwave.configurationV1.configurationSet( parameterNumber: 91, size: 2, scaledConfigurationValue: 10 ).format(),
        /* report on change in wattage by percent (default = 10%) */
		zwave.configurationV1.configurationSet( parameterNumber: 92, size: 1, scaledConfigurationValue: 5 ).format(),

		/* 1st report.. combined power in watts */
		zwave.configurationV1.configurationSet( parameterNumber: 101, size: 4, scaledConfigurationValue: 4 ).format(),
		/* 1st report interval.. 30 sec */
		zwave.configurationV1.configurationSet( parameterNumber: 111, size: 4, scaledConfigurationValue: 30 ).format(),
   		/* 2nd report.. combined energy over time in kWh */
		zwave.configurationV1.configurationSet( parameterNumber: 102, size: 4, scaledConfigurationValue: 8 ).format(),
		/* 2nd report interval.. 5 min */
		zwave.configurationV1.configurationSet( parameterNumber: 112, size: 4, scaledConfigurationValue: 300 ).format(),
		/* 3rd report.. nada */
		zwave.configurationV1.configurationSet( parameterNumber: 103, size: 4, scaledConfigurationValue: 0 ).format(),
		/* 3rd report interval.. never */
		zwave.configurationV1.configurationSet( parameterNumber: 113, size: 4, scaledConfigurationValue: 0 ).format(),

		/* wall switch type.. 0 = momentary (default), 1 = toggle  */
		zwave.configurationV1.configurationSet( parameterNumber: 120, size: 1, scaledConfigurationValue: getWallSwitchType() ).format()
	])
}

def getWallSwitchType()
{
	if ( settings.wallSwitchType == "Momentary" )
	{
    	return 0;
	}
	else if ( settings.wallSwitchType == "Toggle" )
	{
    	return 1;
	}
	else
	{
		log.debug "getWallSwitchType(): Unknwon type: ${settings.wallSwitchType}; defaulting to Momentary"
		return 0;
	}
}

def wallSwitchTypes()
{
	[ "Momentary", "Toggle" ]
}