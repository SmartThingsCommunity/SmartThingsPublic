/*
 *  Aeon HEM Gen5(zwave plus)
 *
 *  Copyright 2016 Dillon A. Miller
 *
 *  v0.8 of Aeon HEM Gen5(zwave plus) code, released 04/15/2016 for Aeotec Model zw095-a
 *  This Gen5 device handler is not backward compatible with the Aeon V1 or V2 device. If your model number is not zw095-a, don't use it.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Some code used from various SmartThings device handlers created by:
 *		Brock Haymond, Barry A. Burke, and Robert Vandervoort. Portions of the metering code came from ElasticDev.
 *
 *  Link to find the latest version of the Device Handler code:
 *		https://gist.github.com/DuncanIdahoCT/deb2bafdd28af4fce3073b9d9f4ecafa
 *
 *  General notes:
 *		You may need to change the device type to this device handler after you pair it to your hub. I'm not sure why it doesn't always automatically pair using this handler even though the fingerprint matches...
 *		Also, you need to hit the button on the back of the HEM Gen5 a couple (two-three) times to get it to flash the red light on the front rapidly to ensure it's in inclusion mode. It isn't in the right mode if it's just flashing slow.
 *		Once the red light is solid, look in your things list and find it, could be just called z-wave, then change it's name and device type using the graph api to this custom handler to make it start working.
 *		The config with default intervals will send right away after you set the HEM Gen5 to this handler, please wait a few minutes before sending the config again or changing the intervals under preferences.
 *		The purpose of the config button is just really to resend the config with monitor interval prefs, as sometimes the config isn't fully applied, See the list of config items at the bottom of this code.
 *		You may need to send the config a few times, with about 2 minutes delay between to get all the properties to take. Not sure why-but if you look in the debug log in the graph api for this device you can see if they all applied.
 *		I had to unplug and replug my device after extensive testing and repeatedly setting the config over and over. Could be it works for you the first time, or you may need to pull power and re plug too in order to get all the Pole 1/2 data to show in the app.
 *
 *	Known Issues:
 *		Issue 1:
 *			Not sure if this is the device handler or just a general SmartThings IOS App issue but sometimes when you set the preferences, it just spins in the app.
 *			If you are watching the log you can see it did set the preferences. Not sure why the app gets stuck, but just reload the app and should be fine.
 *		Issue 2:
 *			Not really a problem, more like a limitation; the clamps 1/2 data packets are sent to the ST hub based on monitor intervals for reports groups 1-3 which you can set in the device preferences.
 *			What this means is that, if you are paying attention to the tile data, you'll notice the center Total values don't quite work out to be the sum of Pole 1/2 values.
 *			This is simply because the base HEM data can be "polled" whereas the clamp 1/2 data is sent. And only sent on a schedule.
 *
 *  Change log:
 *		v0.1 - released 04/04/16:
 *			Added support for secure inclusion and command encapsulation
 *		v0.2 - released 04/05/16:
 *			Added configuration settings using some preference variables that you can control from the app
 *		v0.3 - released 04/05/16:
 *			Added clamp1 and clamp2 data display, may have to hit configure a few times (wait at least 2 minute each time) to make the top left and right boxes show data from the clamps.
 *		v0.4 - released 04/06/16:
 *			Changed the "main" tile to display a clean total kWh, although the ST app seems to make everything on the main Things list all CAPS so it's actually displayed as KWH...
 *		v0.5 - released 04/07/16:
 *			Changed the main thing list device icon to st.Lighting.light14 cause it has a leaf!.
 *		v0.6 - released 04/07/16:
 *			Added a cost per kWh preference and a cost tile that is calculated on kWh.
 *			Also added a timestamp of last reset button tap to work with above cost feature.
 *		v0.7 - released 04/08/16:
 *			Removed individual value tile polling actions and polling function.
 *			Cleaned up and well-formed code
 *		v0.8 - released 04/15/16:
 *			Slightly adjusted the size of the tiles to fit all tiles into the App screen without scrolling.
 *			Further commented and cleaned up the code in preparation for submission to SmartThings as an official device handler.
 *			Submitted for consideration by SmartThings.
 *
 *	To do:
 *		Features:
 *			Color code tiles or tile values for hi/low usage conditions.
 *			Possibly integrate a second tile set page with peak/min usage of watts and/or amps values over time.
 *			Integrate PlotWatt or other online Energy tracking API based service.
 *		Fixes:
 *			Determine why app sometimes freezes when setting preferences, I have seen this with other devices, e.g. Arrival Sensor, Jasco Wall Switches, etc...
 *
 */

// metadata
metadata {
	definition (name: "Aeon HEM Gen5(zwave plus)", namespace: "DuncanIdahoCT", author: "Dillon A. Miller") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		command "reset"
		fingerprint deviceId: "0x3101", inClusters: "0x98"
		fingerprint inClusters: "0x5E,0x86,0x72,0x32,0x56,0x60,0x70,0x59,0x85,0x7A,0x73,0xEF,0x5A", outClusters: "0x82"
	}

	// simulator
	simulator {

		for (int i = 0; i <= 10000; i += 1000) {
	  	status "power  ${i} W": 
			new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 1, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh":
			new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 1, scale: 0, size: 4).incomingMessage()
		}
	}

	// tile definitions
	tiles (scale: 2) {
		// This tile is not displayed on the tile screen for this device but rather in the Things list.
		valueTile("list-energy", "device.energy") {
			state "default", label:'${currentValue} kWh', icon: "st.Lighting.light14"
		}
		// Tiles with a digit below relate to Clamp 1 and Clamp 2. Tiles with no digit are totals for clamps or volts.
		valueTile("current1", "device.current1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Pole 1\n${currentValue}\nAmps'
		}
		valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Total\n${currentValue}\nAmps'
		}
		valueTile("current2", "device.current2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Pole 2\n${currentValue}\nAmps'
		}
		valueTile("power1", "device.power1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Pole 1\n${currentValue}\nWatts'
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Total\n${currentValue}\nWatts'
		}
		valueTile("power2", "device.power2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Pole 2\n${currentValue}\nWatts'
		}
		valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 1) {
			state "default", label:'${currentValue} V'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 1) {
			state "default", label:'${currentValue} kWh'
		}
		valueTile("cost", "device.cost", decoration: "flat", width: 2, height: 1) {
			state "default", label:'\$${currentValue}'
		}
		valueTile("lastresetlabel", "device.lastresettime", decoration: "flat", width: 3, height: 1) {
			state "default", label:'Last Reset Timestamp'
		}
		valueTile("lastresettime", "device.lastresettime", decoration: "flat", width: 3, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "list-energy"
		details(["current1","current","current2","power1","power","power2","voltage","energy","cost","lastresetlabel","lastresettime","configure","reset","refresh"])
	}

	preferences {
		
		input "kWhCost", "string",
			title: "Cost in \$/kWh",
			description: "Your Electric Bill Cost Per kWh",
			defaultValue: "0.19514" as String,
			required: false,
			displayDuringSetup: true
		input "monitorInterval1", "integer",
			title: "Volts & kWh Report",
			description: "Interval (secs) for Volts & kWh Report",
			defaultValue: 60,
			range: "1..4294967295?",
			required: false,
			displayDuringSetup: true
		input "monitorInterval2", "integer",
			title: "Amps Report",
			description: "Interval (secs) for Amps Report",
			defaultValue: 30,
			range: "1..4294967295?",
			required: false,
			displayDuringSetup: true
		input "monitorInterval3", "integer",
			title: "Watts Report",
			description: "Interval (secs) for Watts Report",
			defaultValue: 6,
			range: "1..4294967295?",
			required: false,
			displayDuringSetup: true
	}

}

def updated(){
	if (state.sec && !isConfigured()) {
		// in case we miss the SCSR
		response(configure())
	}
}

def parse(String description){
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x32: 3, 0x56: 1, 0x59: 1, 0x5A: 1, 0x60: 3, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2, 0x8E: 2, 0xEF: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
//log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x56: 1, 0x59: 1, 0x5A: 1, 0x60: 3, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2, 0x8E: 2, 0xEF: 1])
	state.sec = 1
//log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} groupingIdentifier: ${cmd.groupingIdentifier}, maxNodesSupported: ${cmd.maxNodesSupported}, nodeId: ${cmd.nodeId}, reportsToFollow: ${cmd.reportsToFollow}"
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    def meterTypes = ["Unknown", "Electric", "Gas", "Water"]
    def electricNames = ["energy", "energy", "power", "count",  "voltage", "current", "powerFactor",  "unknown"]
    def electricUnits = ["kWh",    "kVAh",   "W",     "pulses", "V",       "A",       "Power Factor", ""]

    //NOTE ScaledPreviousMeterValue does not always contain a value
    def previousValue = cmd.scaledPreviousMeterValue ?: 0

    //Here is where all HEM polled values are defined. Scale(0-7) is in reference to the Aeon Labs HEM Gen5 data for kWh, kVAh, W, V, A, and M.S.T. respectively.
    //If scale 7 (M.S.T.) is polled, you would receive Scale2(0-1) which is kVar, and kVarh respectively. We are ignoring the Scale2 ranges in this device handler.
    def map = [ name: electricNames[cmd.scale], unit: electricUnits[cmd.scale], displayed: state.display]
    switch(cmd.scale) {
        case 0: //kWh
						previousValue = device.currentValue("energy") ?: cmd.scaledPreviousMeterValue ?: 0
						BigDecimal costDecimal = cmd.scaledMeterValue * (kWhCost as BigDecimal)
						def costDisplay = String.format("%5.2f",costDecimal)
						sendEvent(name: "cost", value: costDisplay, unit: "", descriptionText: "Display Cost: \$${costDisp}")
						map.value = cmd.scaledMeterValue
            break;
        case 1: //kVAh (not used in the U.S.)
            map.value = cmd.scaledMeterValue
            break;
        case 2: //Watts
            previousValue = device.currentValue("power") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 3: //pulses
						map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 4: //Volts
            previousValue = device.currentValue("voltage") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 5: //Amps
            previousValue = device.currentValue("current") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 6: //Power Factor
        case 7: //Scale2 values (not currently implimented or needed)
            map.value = cmd.scaledMeterValue
            break;
        default:
            break;
    }
createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	//This is where the HEM clamp1 and clamp2 (subdevice) report values are defined. Scale(2,5) is in reference to the Aeon Labs HEM Gen5 (subdevice) data for W, and A respectively.
	//Z-Wave Command Class 0x60 (multichannelv3) is necessary to interpret the subdevice data from the HEM clamps.
	//In addition, "cmd.commandClass == 50" and "encapsulatedCommand([0x30: 1, 0x31: 1])" below is necessary to properly receive and inturpret the encasulated subdevice data sent to the SmartThings hub by the HEM.
	//The numbered "command class" references: 50, 0x30v1, and 0x31v1 do not seem to be true Z-Wave Command Classes and any correlation is seemingly coincidental.
	//It should also be noted that without the above, the data received will not be processed here under the 0x60 (multichannelv3) command class and you will see unhandled messages from the HEM along with references to command class 50 as well as Meter Types 33, and 161.
	//sourceEndPoint 1, and 2 are the Clamps 1, and 2.
	def dispValue
	def newValue
	def formattedValue
	def MAX_AMPS = 220
	def MAX_WATTS = 24000
	if (cmd.commandClass == 50) { //50 is likely a manufacturer specific code, Z-Wave specifies this as a "Basic Window Covering" so it's not a true Z-Wave Command Class.   
		def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // The documentation on working with Z-Wave subdevices and the technical specs from Aeon Labs do not explain this adequately, but it's necessary.
	//log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
		if (encapsulatedCommand) {
			if (cmd.sourceEndPoint == 1) {
				if (encapsulatedCommand.scale == 2 ) {
						newValue = Math.round(encapsulatedCommand.scaledMeterValue)
	                    if (newValue > MAX_WATTS) { return }
						formattedValue = newValue
						dispValue = "${formattedValue}"
						sendEvent(name: "power1", value: dispValue as String, unit: "", descriptionText: "L1 Power: ${formattedValue} Watts")
				}
				if (encapsulatedCommand.scale == 5 ) {
						newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
	                    if (newValue > MAX_AMPS) { return }
						formattedValue = String.format("%5.2f", newValue)
						dispValue = "${formattedValue}"
						sendEvent(name: "current1", value: dispValue as String, unit: "", descriptionText: "L1 Current: ${formattedValue} Amps")
				}
			}
			else if (cmd.sourceEndPoint == 2) {
				if (encapsulatedCommand.scale == 2 ) {
						newValue = Math.round(encapsulatedCommand.scaledMeterValue)
	                    if (newValue > MAX_WATTS) { return }
						formattedValue = newValue
						dispValue = "${formattedValue}"
						sendEvent(name: "power2", value: dispValue as String, unit: "", descriptionText: "L2 Power: ${formattedValue} Watts")
				}
				if (encapsulatedCommand.scale == 5 ) {
						newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
	                    if (newValue > MAX_AMPS) { return }
						formattedValue = String.format("%5.2f", newValue)
						dispValue = "${formattedValue}"
						sendEvent(name: "current2", value: dispValue as String, unit: "", descriptionText: "L2 Current: ${formattedValue} Amps")
				}
			}
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	//This will log any unhandled command output to the debug window.
	log.debug "Unhandled: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def refresh() {
	def request = [
	//This is where the tile action "refresh" is defined. Refresh is very basic. It simply gets and displays the latest values from the HEM exclusive of the clamp subdevices.
		zwave.meterV3.meterGet(scale: 0),	//kWh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
	]
	commands(request)
}

def reset() {
	//This is where the tile action "reset" is defined. Reset is only meant to be used once a month on the end/beginning of your electric utility billing cycle.
	//Tapping reset will send the meter reset command to HEM and zero out the kWh data so you can start fresh.
	//This will also clear the cost data and reset the last reset timestamp. Finally it will poll for latest values from the HEM.
	//This has no impact on Pole1 or Pole2 (clamp1 and clamp2 subdevice) tile data as that is sent via reports from the HEM.
	def dateString = new Date().format("M/d/YY", location.timeZone)
	def timeString = new Date().format("h:mm a", location.timeZone)    
	state.lastresettime = dateString+" @ "+timeString
	sendEvent(name: "lastresettime", value: state.lastresettime)	
	def request = [
		zwave.meterV3.meterReset(),
		zwave.meterV3.meterGet(scale: 0),	//kWh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
	]
	commands(request)
}

def configure() {
	//This is where the tile action "configure" is defined. Configure resends the configuration commands below (using the variables set by the preferences section above) to the HEM Gen5 device.
	//If you're watching the debug log when you tap configure, you should see the full configuration report come back slowly over about a minute.
	//If you don't see the full configuration report (seven messages) followed by the association report, tap configure again.
	def monitorInt1 = 60
		if (monitorInterval1) {
			monitorInt1=monitorInterval1.toInteger()
		}
	def monitorInt2 = 30
		if (monitorInterval2) {
			monitorInt2=monitorInterval2.toInteger()
		}
	def monitorInt3 = 6
		if (monitorInterval3) {
			monitorInt3=monitorInterval3.toInteger()
	}
	log.debug "Sending configure commands - kWhCost '${kWhCost}', monitorInterval1 '${monitorInt1}', monitorInterval2 '${monitorInt2}', monitorInterval3 '${monitorInt3}'"
	def request = [
		// Reset switch configuration to defaults.
	//zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 1),
		// Disable selective reporting, so always update based on schedule below <set to 1 to reduce network traffic>.
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 1),
		// (DISABLED by first option) Don't send unless watts have changed by 50 <default>.
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: 10),
		// (DISABLED by first option) Or by 10% <default>.
		zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 5),

		// Which reports need to send in Report group 1.
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6149),
		// Which reports need to send in Report group 2.
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1572872),
		// Which reports need to send in Report group 3.
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 770),
		// Interval to send Report group 1.
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: monitorInt1),
		// Interval to send Report group 2.
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: monitorInt2),
		// Interval to send Report group 3.
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: monitorInt3),

		// Report which configuration commands were sent to and received by the HEM Gen5 successfully.
		zwave.configurationV1.configurationGet(parameterNumber: 3),
		zwave.configurationV1.configurationGet(parameterNumber: 4),
		zwave.configurationV1.configurationGet(parameterNumber: 8),
		zwave.configurationV1.configurationGet(parameterNumber: 101),
		zwave.configurationV1.configurationGet(parameterNumber: 102),
		zwave.configurationV1.configurationGet(parameterNumber: 103),
		zwave.configurationV1.configurationGet(parameterNumber: 111),
		zwave.configurationV1.configurationGet(parameterNumber: 112),
		zwave.configurationV1.configurationGet(parameterNumber: 113),
		zwave.associationV2.associationGet(groupingIdentifier: 1)
	]
	commands(request)
}

private setConfigured() {
	updateDataValue("configured", "true")
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}