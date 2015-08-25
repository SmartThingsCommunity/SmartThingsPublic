/**
 *  SolarCity
 *
 *  Copyright 2015 joer293@icloud.com
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
 *	initial version 8/24/2015 - joer293
 *	
 *	todo: 
 *	1. Record a graph of daily usage
 *	2. make grabbing a token easier for the user. Currently it has to be found via the web browser cookie.
 *	3. add labels to the tiles.
 */
metadata {
	definition (name: "SolarCity", namespace: "joer293", author: "joer@noface.net") {
		capability "Energy Meter"
		capability "Polling"
		capability "Power Meter"
		capability "Refresh"

		attribute "SolarCostPerkWh", "string"
		attribute "UtilityCostPerkWh", "string"
		attribute "AvoidedCostPerkWh", "string"
		attribute "CloudPercentage", "string"
		attribute "HoursOfSunlight", "string"
        attribute "Consumed", "string"
        attribute "Produced", "string"
        
	}
	preferences {

    input("token", "text", title: "GUID", description: "The mysolarcity GUID token", required: true)
    input("LocationID", "text", title: "Location ID token", description: "The mysolarcity Location ID token", required: true)
    
	}
	
	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		valueTile("Energy", "device.energy") {
            state ("energy", label:'${currentValue} Wh')
            }
        valueTile("Power", "device.power") {
            state ("power", label:'${currentValue} Wh')
            }
        valueTile("SolarCostPerkWh", "device.SolarCostPerkWh", decoration: "flat") {
            state ("SolarCostPerkWh", label:'${currentValue}')
            }
        valueTile("UtilityCostPerkWh", "device.UtilityCostPerkWh", decoration: "flat") {
            state ("UtilityCostPerkWh", label:'${currentValue}')
            }
        valueTile("AvoidedCostPerkWh", "device.AvoidedCostPerkWh", decoration: "flat") {
            state ("AvoidedCostPerkWh", label:'${currentValue}')
            }
        valueTile("CloudPercentage", "device.CloudPercentage") {
            state ("CloudPercentage", label:'${currentValue} %')
            }
        valueTile("HoursOfSunlight", "device.HoursOfSunlight") {
            state ("HoursOfSunlight", label:'${currentValue} h')
            }
        chartTile(name: "HoursOfSunlightChart", attribute: "device.HoursOfSunlight")
        standardTile("Refresh", "device.image") {
			state ("default", action:"refresh.refresh", icon:"st.secondary.refresh")
			}
        standardTile("EnergyConsumed", "device.Consumed") {
            state ("EnergyConsumed", label:'${currentValue}', icon:"st.Appliances.appliances17")
            }
        standardTile("EnergyProduced", "device.Produced") {
            state ("EnergyProduced", label:'${currentValue}', icon:"st.custom.wuk.clear")
            }
        

		main(["EnergyConsumed", "EnergyProduced", "Energy", "CloudPercentage", "HoursOfSunlight"])
		details(["EnergyConsumed", "EnergyProduced", "Refresh", "CloudPercentage", "HoursOfSunlight", "SolarCostPerkWh", "UtilityCostPerkWh", "AvoidedCostPerkWh"])
	}
}

// parse events into attributes
def parse(String description) {
	

    
}

// handle commands
def poll() {
	log.debug "Executing Solar city API 'poll'"
	goImportSolarCity()
}

def refresh() {
	log.debug "Executing Solar city API 'refresh'"
	goImportSolarCity()
}


def goImportSolarCity(){
	// sendEvent(name: "energy", value: "polling", IsStateChange: "true")
	def today = new Date().format("yyyy-MM-dd'T'00:00:00", location.timeZone)
    def todayMidnight = new Date().format("yyyy-MM-dd'T'23:59:59", location.timeZone)
    def callBack = getCallBackAddress()
	log.debug "callback $callBack"

	def energyCostParams = [uri: "https://mysolarcity.com/solarcity-api/solarbid/api/installation/energycosts/$token"]

	log.debug "SC device list params: $energyCostParams"
    
    def weatherParams = [uri: "https://mysolarcity.com/solarcity-api/powerguide/v1.0/installations/$token/weather?ID=$LocationID"]

	log.debug "SC device list params: $weatherParams"
    
    def consumptionParams = [uri: "https://mysolarcity.com/solarcity-api/powerguide/v1.0/consumption/$token?EndTime=${todayMidnight}&ID=$LocationID&Period=hour&StartTime=${today}"]

	log.debug "SC device list params: $consumptionParams"
   
    def productionParams = [uri: "https://mysolarcity.com/solarcity-api/powerguide/v1.0/measurements/$token?EndTime=${todayMidnight}&ID=$LocationID&IsByDevice=true&Period=hour&StartTime=${today}"]

	log.debug "SC device list params: $productionParams"
    
    def successEnergyCostClosure = { response ->
    	log.debug "Request to SolarCity was successful $response.data"
//      	sendEvent(name: "energy", value: "1234", unit:"W", IsStateChange: "true")
//    	sendEvent(name: "power", value: "1234", unit:"W", IsStateChange: "true")
		sendEvent(name: "SolarCostPerkWh", value: '\$'+Math.round(response.data.SolarCostPerkWh*1000)/1000, unit: '\$', IsStateChange: "true")
  		sendEvent(name: "UtilityCostPerkWh", value: '\$'+Math.round(response.data.UtilityCostPerkWh*1000)/1000,  unit: '\$', IsStateChange: "true")
    	sendEvent(name: "AvoidedCostPerkWh", value: '\$'+Math.round(response.data.AvoidedCostPerkWh*1000)/1000, unit: '\$', IsStateChange: "true")
   
	}
    
    def successWeatherClosure = { response ->
    	log.debug "Request to SolarCity was successful $response.data"
      	sendEvent(name: "CloudPercentage", value: response.data.CloudPercentage, unit: '%', IsStateChange: "true")
        sendEvent(name: "HoursOfSunlight", value: response.data.HoursOfSunlight, unit:'h', IsStateChange: "true")
    
	}
    
    def successConsumptionClosure = { response ->
    	log.debug "Request to SolarCity was successful $response.data"
      	sendEvent(name: "Consumed", value: response.data.TotalConsumptionInIntervalkWh, unit: 'kWh', IsStateChange: "true")
	}
    
    def successProductionClosure = { response ->
    	log.debug "Request to SolarCity was successful $response.data"
      	sendEvent(name: "Produced", value: response.data.TotalEnergyInIntervalkWh, unit: 'kWh', IsStateChange: "true")
	}
    
     try {
    
            httpGet(consumptionParams,  successConsumptionClosure)
            
        } catch (any) {
    	
        log.error "General error trying to connect to mySolarCity and retrieve consumption data " 
    	
    
    }
    try {
    
            httpGet(productionParams,  successProductionClosure)
            
        } catch (any) {
    	
        log.error "General error trying to connect to mySolarCity and retrieve production data " 
    	
    
    }
    
    try {
    
            httpGet(energyCostParams,  successEnergyCostClosure)
            
        } catch (any) {
    	
        log.error "General error trying to connect to mySolarCity and retrieve cost data " 
    	
    
    }
    
    try {
    
            httpGet(weatherParams,  successWeatherClosure)
            
        } catch (any) {
    	
        log.error "General error trying to connect to mySolarCity and retrieve weather data " 
    	
    
    }

}





private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug "Port entered is $port and the converted hex code is $hexport"
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex, 16)
}


private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]), convertHexToInt(hex[2..3]), convertHexToInt(hex[4..5]), convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}


