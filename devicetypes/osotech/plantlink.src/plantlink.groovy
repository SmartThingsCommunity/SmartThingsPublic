/**
 *  PlantLink
 *
 *  This device type takes sensor data and converts it into a json packet to send to myplantlink.com
 *  where its values will be computed for soil and plant type to show user readable values of how your
 *  specific plant is doing.
 *
 *
 *  Copyright 2015 Oso Technologies
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
 
import groovy.json.JsonBuilder

metadata {
    definition (name: "PlantLink", namespace: "OsoTech", author: "Oso Technologies") {
        capability "Sensor"
        capability "Health Check"

        command "setStatusIcon"
        command "setPlantFuelLevel"
        command "setBatteryLevel"
        command "setInstallSmartApp"

        attribute "plantStatus","string"
        attribute "plantFuelLevel","number"
        attribute "linkBatteryLevel","string"
        attribute "installSmartApp","string"

        fingerprint profileId: "0104", inClusters: "0000,0001,0B04", deviceJoinName: "Plant Link Humidity Sensor"
    }

    simulator {
        status "battery": "read attr - raw: C0720100010A000021340A, dni: C072, endpoint: 01, cluster: 0001, size: 0A, attrId: 0000, encoding: 21, value: 0a34"
        status "moisture": "read attr - raw: C072010B040A0001290000, dni: C072, endpoint: 01, cluster: 0B04, size: 0A, attrId: 0100, encoding: 29, value: 0000"
    }

    tiles {
        standardTile("Title", "device.label") {
            state("label", label:'PlantLink ${device.label}')
        }

        valueTile("plantMoistureTile", "device.plantFuelLevel", width: 1, height: 1) {
            state("plantMoisture", label: '${currentValue}% Moisture')
        }

        valueTile("plantStatusTextTile", "device.plantStatus", decoration: "flat", width: 2, height: 2) {
            state("plantStatusTextTile", label:'${currentValue}')
        }

        valueTile("battery", "device.linkBatteryLevel" ) {
            state("battery", label:'${currentValue}% battery')
        }
        
        valueTile("installSmartApp","device.installSmartApp", decoration: "flat", width: 3, height: 1) {
        	state "needSmartApp", label:'Please install SmartApp "Required PlantLink Connector"', defaultState:true
            state "connectedToSmartApp", label:'Connected to myplantlink.com'
        }

        main "plantStatusTextTile"
        details(['plantStatusTextTile', "plantMoistureTile", "battery", "installSmartApp"])
    }
}

def updated() {
    // Device-Watch allows 2 check-in misses from device
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def installed() {
    // Device-Watch allows 2 check-in misses from device
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def setStatusIcon(value){
    def status = ''
    switch (value) {
    	case '0':
	        status = 'Needs Water'
            break
    	case '1':
        	status = 'Dry'
            break
    	case '2':
   		case '3':
        	status = 'Good'
            break
        case '4':
        	status = 'Too Wet'
            break
    	case 'No Soil':
        	status = 'Too Dry'
        	setPlantFuelLevel(0)
            break
    	case 'Recently Watered':
        	status = 'Watered'
            setPlantFuelLevel(100)
    		break
    	case 'Low Battery':
        	status = 'Low Battery'
    		break
    	case 'Waiting on First Measurement':
	        status = 'Calibrating'
            break
    	default:
        	status = "?"
            break
    }
    sendEvent("name":"plantStatus", "value":status, "description":statusText, displayed: true, isStateChange: true)
}

def setPlantFuelLevel(value){
    sendEvent("name":"plantFuelLevel", "value":value, "description":statusText, displayed: true, isStateChange: true)	
}

def setBatteryLevel(value){
    sendEvent("name":"linkBatteryLevel", "value":value, "description":statusText, displayed: true, isStateChange: true)
}

def setInstallSmartApp(value){
	sendEvent("name":"installSmartApp", "value":value)
}

def parse(String description) {
	log.debug description
	def description_map = parseDescriptionAsMap(description)
    def event_name = ""
    def measurement_map = [
            type: "link",
            signal: "0x00",
            zigbeedeviceid: device.zigbeeId,
            created: new Date().time /1000 as int
    ]
    if (description_map.cluster == "0001"){
        /* battery voltage in mV (device needs minimium 2.1v to run) */
        log.debug "PlantLink - id ${device.zigbeeId} battery ${description_map.value}"
        event_name = "battery_status"
        measurement_map["battery"] = "0x${description_map.value}"
        
    } else if (description_map.cluster == "0B04"){
        /* raw moisture reading (needs to be sent to plantlink for soil/plant type conversion) */
        log.debug "PlantLink - id ${device.zigbeeId}  raw moisture ${description_map.value}"
        measurement_map["moisture"] = "0x${description_map.value}"
        event_name = "moisture_status"
        
    } else{
        log.debug "PlantLink - id ${device.zigbeeId} Unknown '${description}'"
        return
    }
    
    def json_builder = new JsonBuilder(measurement_map)
    def result = createEvent(name: event_name, value: json_builder.toString())
    return result
}


def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        if(nameAndValue.length == 2){
	        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        }else{
        	map += []
        }
    }
}
