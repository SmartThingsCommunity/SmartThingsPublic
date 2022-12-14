/**
 *  SmartStreams
 *
 *  Copyright 2017 Chris Vincent
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
 *  Based on GroveStreams from Jason Steele
 *
 */
definition(
    name: "SmartStreams",
    namespace: "project802",
    author: "Chris Vincent",
    description: "Stream SmartThings events to an API endpoint",
    category: "My Apps",
    iconUrl: "http://project802.net/smartthings/smartapp-icons/smartstreams.png",
    iconX2Url: "http://project802.net/smartthings/smartapp-icons/smartstreams_2x.png",
    iconX3Url: "http://project802.net/smartthings/smartapp-icons/smartstreams_3x.png"
    )

preferences {
    section("Log devices...")
    {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true, displayDuringSetup: true
        input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true, displayDuringSetup: true
        input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true, displayDuringSetup: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true, displayDuringSetup: true
        input "presences", "capability.presenceSensor", title: "Presence", required: false, multiple: true, displayDuringSetup: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true, displayDuringSetup: true
        input "waterSensors", "capability.waterSensor", title: "Water Sensors", required: false, multiple: true, displayDuringSetup: true
        input "lightSensors", "capability.illuminanceMeasurement", title: "Light Sensors", required: false, multiple: true, displayDuringSetup: true
        input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Humidity Sensors", required: false, multiple: true, displayDuringSetup: true
        input "powerMeters", "capability.powerMeter", title: "Power Meters", required: false, multiple: true, displayDuringSetup: true
        input "energyMeters", "capability.energyMeter", title: "Energy Meters", required: false, multiple: true, displayDuringSetup: true
        input "powerSources", "capability.powerSource", title: "Power Sources", required: false, multiple: true, displayDuringSetup: true
        input "batteries", "capability.battery", title: "Batteries", required: false, multiple: true, displayDuringSetup: true
        input "voltageMeters", "capability.voltageMeasurement", title: "Voltage Meters", required: false, multiple: true, displayDuringSetup: true
        input "buttons", "capability.button", title: "Buttons", required: false, multiple: true, displayDuringSetup: true
    }

    section ("SmartStreams Feed PUT API key...")
    {
        input "api_uri", "text", title: "API Endpoint", required: true, displayDuringSetup: true, default: "http://yoursitehere.com/api/feed"
        input "api_key", "text", title: "PUT API key", required: true, displayDuringSetup: true, default: ""
    }
}

def installed()
{
    initialize()
}

def updated()
{
    unsubscribe()
    initialize()
}

def initialize()
{
    subscribe( temperatures, "temperature", handleTemperatureEvent )
    subscribe( contacts, "contact", handleContactEvent )
    subscribe( accelerations, "acceleration", handleAccelerationEvent )
    subscribe( motions, "motion", handleMotionEvent )
    subscribe( presences, "presence", handlePresenceEvent )
    subscribe( switches, "switch", handleSwitchEvent )
    subscribe( waterSensors, "water", handleWaterEvent )
    subscribe( lightSensors, "illuminance", handleLightEvent )
    subscribe( humiditySensors, "humidity", handleHumidityEvent )
    subscribe( powerMeters, "power", handlePowerEvent )
    subscribe( energyMeters, "energy", handleEnergyEvent )
    subscribe( powerSources, "powerSource", handlePowerSourceEvent )
    subscribe( batteries, "battery", handleBatteryEvent )
    subscribe( voltageMeters, "voltage", handleVoltageEvent )
    if( buttons )
        subscribe( buttons, "button", handleButtonEvent )
}

def handleButtonEvent( evt )
{
    sendValue( evt )
}

def handleEnergyEvent( evt )
{
    sendValue( evt )
}

def handlePowerEvent( evt )
{
    sendValue( evt )
}

def handlePowerSourceEvent( evt )
{
    sendValue( evt )
}

def handleBatteryEvent( evt )
{
    sendValue( evt )
}

def handleVoltageEvent( evt )
{
    sendValue( evt )
}

def handleHumidityEvent( evt )
{
    sendValue( evt )
}

def handleLightEvent( evt )
{
    sendValue( evt )
}

def handleWaterEvent( evt )
{
    sendValue( evt )
}

def handleTemperatureEvent( evt )
{
    sendValue( evt )
}

def handleContactEvent( evt )
{
    sendValue( evt )
}

def handleAccelerationEvent( evt )
{
    sendValue( evt )
}

def handleMotionEvent( evt )
{
    sendValue( evt )
}

def handlePresenceEvent( evt )
{
    sendValue( evt )
}

def handleSwitchEvent( evt )
{
    sendValue( evt )
}


private sendValue( evt )
{
    def component = URLEncoder.encode( evt.displayName.trim() )
    def name = URLEncoder.encode( evt.name.trim() )
    def value = URLEncoder.encode( evt.value.trim() )
    def hub_id = URLEncoder.encode( evt.hubId ? evt.hubId : "nohub" )
    def dev_id = URLEncoder.encode( evt.deviceId ? evt.deviceId : "nodev" )
    def loc_id = URLEncoder.encode( evt.locationId ? evt.locationId : "nolocid" )
    
    //log.debug "SmartStreams sending ${component}, ${evt.name} = ${evt.value} from ${hub_id}"
    
	def url = "${api_uri}?hub_id=${hub_id}&loc_id=${loc_id}&dev_id=${dev_id}&api_key=${api_key}&component=${component}&${name}=${value}"
    //log.debug url
    
    def putParams = [
        uri: url,
        body: []
    ]
    
    httpPut( putParams ) { response -> 
        if( response.status != 200 )
        {
            log.error "SmartStreams sending failed, status = ${response.status}"
        }
    }
}
