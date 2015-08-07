/**
 *  TED-5000 Device Type for SmartThings
 *
 *  Author: badgermanus@gmail.com
 *  Code: https://github.com/jwsf/device-type.ted5000
 *
 * INSTALLATION 
 * ========================================
 *
 * 1) Connect to the web admin interface of your TED5000 (typically available
 *    at //TED5000 on your local network and make sure password protection
 *    is turned on for all access. See TED5000_Settings.png for an example
 *
 * 2) Configure your firewall so that the TED5000 is visible outside your home 
 *    network. This topic is beyond the scope of this code, but at the end you 
 *    should be able to connect to your TED5000 from outside your home network
 *    (your phone's browser is a good option for this if you turn off wifi. The
 *    URL will be in this format:
 *
 *              http://YOUR_IP:YOUR_PORT/api/LiveData.xml
 *
 *    Don't even think about proceeding until the above is working!
 * 
 * 3) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: TED5000
 *     Author: badgermanus@gmail.com
 *
 * 4) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: TED5000 (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 5) Update device preferences
 *     Click on the new device in the mobile app to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your device access information - username, password & URL
 *     
 * Copyright (C) 2014 Jonathan Wilson  <badgermanus@gmail.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions: The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
 preferences {
    input("url", "text", title: "URL", description: "The URL (including port number) of your TED5000 device (must be available to the public internet - example http://12.34.56.78:4444)", required: true)
    input("usr", "email", title: "Username", description: "The username configured in Network Settings on your TED5000", required: true)
    input("pass", "password", title: "Password", description: "The password configured in Network Settings on your TED5000", required: true)
    input(type: "enum", name: "chartGranularity", title: "Chart Granularity", options: granularityOptions(), defaultValue: "Daily", style: "segmented")
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "TED5000", namespace: "smartthings", author: "badgermanus@gmail.com") {
		capability "Energy Meter"
		capability "Polling"
		capability "Refresh"
        capability "Sensor"
		capability "Power Meter"
	}

	// simulator metadata
	simulator {
		for (int i = 100; i <= 3000; i += 200) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
	}
	// UI tile definitions
	tiles {
	    
		
       
  
        
        valueTile(	"power", "device.power", canChangeBackground:true
                 ) 
        
        {state(	"power",
                    label:'${currentValue} W', 
                  	backgroundColors:[
					[value: 1, color: "#153591"],
					[value: 500, color: "#1e9cbb"],
					[value: 1000, color: "#90d2a7"],
					[value: 2500, color: "#44b621"],
					[value: 4000, color: "#f1d801"],
					[value: 6000, color: "#d04e00"],
					[value: 8000, color: "#bc2323"]
				    ]
                 )
		}
        
        
        
        
        
        valueTile(	"volts", 
                  "device.volts", decoration: "flat"
                 ) 
        {
            state("volts",
                    label:'${currentValue} V'
                 )
		}
 
        valueTile(	"cost", 
                  "device.cost", decoration: "flat"
                 ) 
        {
            state("cost",
                    label:'${currentValue} USD'
                 )
		}
     
       valueTile(	"costmtd", 
                  "device.costmtd", decoration: "flat"
                 ) 
        {
            state("costmtd",
                    label:'${currentValue} USD'
                 )
		}
        
        
        
        
        
        standardTile("refresh", "device.power") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}







		main(["power", "cost", "volts"])
		details(["power", "cost", "costmtd", "volts", "refresh"])
	}
}



def poll() {
	doUpdate()
}

def refresh() {
	doUpdate()
}

def doUpdate() {

    // Build auth string
    def auth = settings.usr + ":" + settings.pass
	auth = auth.getBytes()
    auth = auth.encodeBase64()
    String authString = auth

	// Build URL
    def URL = settings.url + "/api/LiveData.xml"
    log.debug "Connecting to " + URL
    
    def params = [
        uri: URL,
        headers: [
            'Authorization': "Basic ${authString}"
        ]
    ]
    
    // This closure is called to parse the XML if it is successfully retrieved
    def successClosure = { response ->
    	log.debug "Request to TED5000 was successful"
        def power = response.data.Power.Total.PowerNow
        def volts = response.data.Voltage.Total.VoltageNow
        def cost = response.data.Cost.Total.CostNow
        def costmtd = response.data.Cost.Total.CostMTD
      	log.debug "Sending event - power: $power W Volts: $voltageNow V Cost: $costNow and CostMTD: $costMTD"
        sendEvent (name: "cost", value: cost, unit:"USD")
        sendEvent (name: "volts", value: volts, unit:"V")
   		sendEvent (name: "power", value: power, unit:"W")
        sendEvent (name: "costmtd", value: costmtd, unit:"USD")
        
        
	}
    
    try {
    
        // Get the XML from the TED5000
    	httpGet(params,  successClosure)
    } catch ( java.net.UnknownHostException e) {
    	log.error "Unknown host - check the URL and PORT for your device"
    	sendEvent name: "power", value: "Unknown host"
   	} catch (java.net.NoRouteToHostException t) {
    	log.error "No route to host - check the URL and PORT for your device " + URL
    	sendEvent name: "power", value: "No route to host"
    } catch (java.io.FileNotFoundException fnf) {
    	log.error "File not found - check the URL and PORT for your device " + URL
        sendEvent name: "power", value: "XML not found"
    } catch (java.io.IOException e) {
    	log.error "Authentication error - check USERNAME and PASSWORD. This can also occur if the TED5000 cannot be reached"
        sendEvent name: "power", value: "Auth error"
    } catch (any) {
    	log.error "General error trying to connect to TED and retrieve data " URL
    	sendEvent name: "power", value: "ERROR"
    }
}


def getVisualizationData(attribute) {
	log.debug "getChartData for $attribute"
	def keyBase = "measure.${attribute}${getGranularity()}"
	log.debug "getChartData state = $state"

	def dateBuckets = state[keyBase]

	//convert to the right format
	def results = dateBuckets?.sort { it.key }.collect {
		[
			date: Date.parse("yyyy-MM-dd", it.key),
			average: it.value.average,
			min: it.value.min,
			max: it.value.max
		]
	}

	log.debug "getChartData results = $results"
	results
}


private storeData(attribute, value, dateString = getKeyFromDateDaily()) {
	log.debug "storeData initial state: $state"
	def keyBase = "measure.${attribute}"
	def numberValue = value.toBigDecimal()

	// create bucket if it doesn't exist
	if (!state[keyBase]) {
		state[keyBase] = [:]
		log.debug "storeData - attribute not found. New state: $state"
	}

	if (!state[keyBase][dateString]) {
		//no date bucket yet, fill with initial values
		state[keyBase][dateString] = [:]
		state[keyBase][dateString].average = numberValue
		state[keyBase][dateString].runningSum = numberValue
		state[keyBase][dateString].runningCount = 1
		state[keyBase][dateString].min = numberValue
		state[keyBase][dateString].max = numberValue

		log.debug "storeData date bucket not found. New state: $state"

		// remove old buckets
		def old = getKeyFromDateDaily(new Date() - 10)
		state[keyBase].findAll { it.key < old }.collect { it.key }.each { state[keyBase].remove(it) }
	} else {
		//re-calculate average/min/max for this bucket
		state[keyBase][dateString].runningSum = (state[keyBase][dateString].runningSum.toBigDecimal()) + numberValue
		state[keyBase][dateString].runningCount = state[keyBase][dateString].runningCount.toInteger() + 1
		state[keyBase][dateString].average = state[keyBase][dateString].runningSum.toBigDecimal() / state[keyBase][dateString].runningCount.toInteger()

		log.debug "storeData after average calculations. New state: $state"

		if (state[keyBase][dateString].min == null) {
			state[keyBase][dateString].min = numberValue
		} else if (numberValue < state[keyBase][dateString].min.toBigDecimal()) {
			state[keyBase][dateString].min = numberValue
		}
		if (state[keyBase][dateString].max == null) {
			state[keyBase][dateString].max = numberValue
		} else if (numberValue > state[keyBase][dateString].max.toBigDecimal()) {
			state[keyBase][dateString].max = numberValue
		}
	}
	log.debug "storeData after min/max calculations. New state: $state"
}



// This next method is only used from the simulator
def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// This next method is only used from the simulator
def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
	} else if (cmd.scale == 1) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
	}
	else {
		[name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}
}




def getGranularity() {
	chartGranularity ?: "Daily"
}

def granularityOptions() { ["Daily", "Hourly"] }

private getKeyFromDateDaily(date = new Date()) {
	date.format("yyyy-MM-dd")
}

private getKeyFromDateHourly(date = new Date()) {
	date.format("yyyy-MM-dd:HH")
}