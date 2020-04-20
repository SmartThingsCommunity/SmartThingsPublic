/**
 *  Copyright 2015 SmartThings
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
 * 
 */

//metadata for smartapps
metadata {
	definition (name: "Danfoss LC13 TRV V0.7", namespace: "richdp", author: "RichardP") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
		
		attribute "thermostatFanState", "string"

		command "switchMode"
		command "switchFanMode"
        command "quickSetCool"
        command "quickSetHeat"

		fingerprint deviceId: "0x0804"
		fingerprint inClusters: "0x80, 0x46, 0x81, 0x72, 0x8F, 0x75, 0x43, 0x86, 0x84"
		
	}

	// simulator metadata
	simulator {
		status "off"			: "command: 4003, payload: 00"
		status "heat"			: "command: 4003, payload: 01"
		status "cool"			: "command: 4003, payload: 02"
		status "auto"			: "command: 4003, payload: 03"
		status "emergencyHeat"	: "command: 4003, payload: 04"

		status "fanAuto"		: "command: 4403, payload: 00"
		status "fanOn"			: "command: 4403, payload: 01"
		status "fanCirculate"	: "command: 4403, payload: 06"

		status "heat 60"        : "command: 4303, payload: 01 09 3C"
		status "heat 68"        : "command: 4303, payload: 01 09 44"
		status "heat 72"        : "command: 4303, payload: 01 09 48"

		status "cool 72"        : "command: 4303, payload: 02 09 48"
		status "cool 76"        : "command: 4303, payload: 02 09 4C"
		status "cool 80"        : "command: 4303, payload: 02 09 50"

		status "temp 58"        : "command: 3105, payload: 01 2A 02 44"
		status "temp 62"        : "command: 3105, payload: 01 2A 02 6C"
		status "temp 70"        : "command: 3105, payload: 01 2A 02 BC"
		status "temp 74"        : "command: 3105, payload: 01 2A 02 E4"
		status "temp 78"        : "command: 3105, payload: 01 2A 03 0C"
		status "temp 82"        : "command: 3105, payload: 01 2A 03 34"

		status "idle"			: "command: 4203, payload: 00"
		status "heating"		: "command: 4203, payload: 01"
		status "cooling"		: "command: 4203, payload: 02"
		status "fan only"		: "command: 4203, payload: 03"
		status "pending heat"	: "command: 4203, payload: 04"
		status "pending cool"	: "command: 4203, payload: 05"
		status "vent economizer": "command: 4203, payload: 06"

		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

//UI configuration
	tiles {
        // Main Tile displays current set point recieved from thermostat. 
        // Background color changes with temp.
		valueTile("temperature", "device.currentHeatingSetpoint", width: 3, height: 2) {
			state("device.currentHeatingSetpoint", label:'${currentValue}°',
                  backgroundColors:[
                       [value: 0, color: "#ededed"],
                       [value: 4, color: "#153591"], //blue
                       [value: 16, color: "#178998"],
                       [value: 18, color: "#199f5c"],
                       [value: 20, color: "#2da71c"],//green
                       [value: 21, color: "#5baa1d"],
                       [value: 22, color: "#8aae1e"],
                       [value: 23, color: "#b1a81f"],
                       [value: 24, color: "#b57d20"],
                       [value: 26, color: "#b85122"],
                       [value: 28, color: "#bc2323"] //red
				]
			)
		}
        
        // Slider control for controling the set point which is next sent to the thermostat
        controlTile("heatSliderControl", "device.nextHeatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false, range:"(4..28)" ) {
			state "setHeatingSetpoint", action: "quickSetHeat", backgroundColor:"#d04e00"
		}
		
        // Tile to indicate the set point that will next be sent to the thermostat. Controlled by controlTile above.
        valueTile("nextHeatingSetpoint", "device.nextHeatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° next', backgroundColor:"#ffffff"
		}

        // Display battery %
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
         	tileAttribute ("device.battery", key: "PRIMARY_CONTROL"){
                        state "battery", label:'${currentValue}% battery', unit:""}
        }

        // Standard Refresh Tile
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

        // Standard Config Tile
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

        // Order of Tiles
		main "temperature"
		details(["temperature", "heatSliderControl", "nextHeatingSetpoint", "battery", "refresh", "configure"])
	}
} //end of UI config

//Parse string to recieve information from device
def parse(String description) {
    state.count = 0
    def results = []
    log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
		} 
    else {
       	def cmd = zwave.parse(description,[0x80: 1, 0x46: 1, 0x81: 1, 0x72: 2, 0x8F: 1, 0x75: 2, 0x43: 2, 0x86: 1, 0x84: 2])
        log.debug "Parsed Command: $cmd"
        if (cmd) {
       	results = zwaveEvent(cmd)
		}
    }
}

// Event Generation
// Event - Get Battery Report 
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
                log.debug ("Battery: $cmd.batteryLevel")
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        createEvent(map)
}

// Event - Get Override Schedule
def zwaveEvent(physicalgraph.zwave.commands.climatecontrolschedulev1.ScheduleOverrideReport cmd){
    log.debug "--- Schedule Override Report received: $cmd"
}

// Event - Thermostat Report
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
    log.debug "--- Thermostat Report recieved: $cmd"
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false
    map.name = "currentHeatingSetpoint"
    log.debug ("Parsed Thermostat Set Point: $map")

	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	map
    sendEvent(map)
}

// Event for waking up the battery powered device
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: true)]
      
    // Only ask for battery if we haven't had a BatteryReport in a while
       if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
              result << response(zwave.batteryV1.batteryGet())
              result << response("delay 1200")  // leave time for device to respond to batteryGet
       }
    //Send new set point to TRV on wake up
    result << response (configureSetPoint())
    result << response ("delay 2000")

    //Confirm current set point and update UI
    result << response (updateUI())
    result << response ("delay 2000")

    // wait and then send no more information confirmation
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

// Command Implementations
def poll() {
	delayBetween([
        zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(), //chaged to V1 for testing
    ], standardDelay)
}

def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees, 1000)
}

def nextHeatingSetpoint(degrees, delay = 3600) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 3600) {
	log.trace "setNextHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 2
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }
  
    sendEvent(name:"nextHeatingSetpoint", value:convertedDegrees, isStateChange: true)
    log.debug ("Value to be sent to thermostat on next wakeup: $convertedDegrees")
    state.nextHeatingSetpoint = convertedDegrees
}


//send new set point to thermostat
def configureSetPoint(){
    log.debug "*** sending new setpoint"
    def sendHeatingSetpoint = state.nextHeatingSetpoint
    delayBetween([
        zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: 0, precision: 2, scaledValue: sendHeatingSetpoint).format(),
        ], 2000) 
}     

//get the current setpoint and update the UI
def updateUI(){
    delayBetween([
    zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
    ], 2000)
}    

//configuration
def configure() {
	delayBetween([
        zwave.configurationV1.configurationSet(parameterNumber:1, size:2, scaledConfigurationValue:100).format(),
        zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),    	
        zwave.wakeUpV1.wakeUpIntervalSet(seconds:3600, nodeid:zwaveHubNodeId).format()
	], standardDelay)
}

//standardDelay
private getStandardDelay() {
	1000
}