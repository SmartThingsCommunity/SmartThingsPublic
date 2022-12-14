/**
 *  Dark Sky Weather
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
 *  Powered by Dark Sky (https://darksky.net/poweredby/)
 *
 *  For more information, visit https://github.com/project802/smartthings
 *
 */

include 'asynchttp_v1'

metadata {
    definition( name: "Dark Sky Weather", namespace: "project802", author: "Chris Vincent" )
    {
        capability "Illuminance Measurement"
        capability "Refresh"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Water Sensor"
    }

    tiles
    {
        valueTile( "temperature", "device.temperature", width: 1, height: 1 )
        {
            state( "default", label:'${currentValue}°',
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        
        valueTile( "humidity", "device.humidity" )
        {
            state "default", label:'${currentValue}% humidity', unit:""
        }
        
        standardTile( "water", "device.water", width: 1, height: 1 )
        {
            state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
            state "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc"
        }
        
        valueTile( "light", "device.illuminance", decoration: "flat" )
        {
            state "default", label:'${currentValue} lux'
        }
        
        standardTile( "darkskylogo", "", width: 2, height: 1 )
        {
            state( "icon", icon: "http://project802.net/smartthings/devicehandler-icons/darksky-poweredby.png" )
        }
        
        main( "temperature" )
        details( ["temperature", "humidity", "water", "light", "darkskylogo"] )
    }
    
    preferences
    {
        input "lat", type: "text", title: "Latitude", required: true, displayDuringSetup: true, defaultValue: "42.2675"
        input "lon", type: "text", title: "Longitude", required: true, displayDuringSetup: true, defaultValue: "-71.8080"
        input "apiKey", type: "text", title: "API Key", required: true, displayDuringSetup: true, defaultValue: ""
    }
}

/**
 * installed()
 *
 * Called by ST platform.
 */
def installed()
{
    log.debug "Dark Sky Weather: installed"
    updated()
}

/**
 * updated()
 *
 * Called by ST platform.
 */
def updated()
{
    // Unschedule here to remove any zombie runIn calls that the platform
    // seems to keep around even if the code changes during dev
    unschedule()
    
    state.temperature   = -460
    state.humidity      = -1
    state.water         = "uninitialized"
    state.illuminance   = -1
    
    log.info "${device.displayName} updated with state: ${state}"
    log.info "${device.displayName} updated with settings: ${settings}"
    
    runEvery10Minutes( darksky_update )
}

def refresh()
{
    if( state.temperature != -460 )
    {
        sendEvent( name: "temperature", value: state.temperature, unit: "F" )
    }
    
    if( state.humidity != -1 )
    {
        sendEvent( name: "humidity", value: state.humidity, unit: "%" )
    }
    
    if( state.water != "uninitialized" )
    {
        sendEvent( name: "water", value: state.water )
    }
    
    if( state.illuminance != -1 )
    {
        sendEvent( name: "illuminance", value: state.illuminance, unit: "lux" )
    }
}

def darksky_update()
{
    //log.debug "darksky_update"
    
    if( settings.apiKey == "" )
    {
        log.error "darksky_update: No API key found.  Please configure in preferences."
        return
    }
    
    def apiRequest = [
        uri : "https://api.darksky.net",
        path : "/forecast/${settings.apiKey}/${settings.lat},${settings.lon}",
        query : [ exclude : "minutely,hourly", units : "us" ],
        contentType : "application/json"
    ]
    
    asynchttp_v1.get( darksky_updateCallback, apiRequest );
}

def darksky_updateCallback( response, data )
{
    //log.debug "darksky_updateCallback: status code ${response.status}"
    
    if( response.hasError() )
    {
        log.error "darksky_updateCallback: ${response.getErrorMessage()}"
        return
    }
    
    def json = response?.json
    
    if( !json )
    {
        log.error "darksky_updateCallback: unable to retrieve data!"
        return
    }
    
    def temperature = Math.round(json.currently.temperature)
    if( temperature != state.temperature )
    {
        state.temperature = temperature
        sendEvent( name: "temperature", value: state.temperature, unit: "F" )
    }
    
    def humidity = Math.round(json.currently.humidity * 100)
    if( humidity != state.humidity )
    {
        state.humidity = humidity
        sendEvent( name: "humidity", value: state.humidity, unit: "%" )
    }
    
    def water = json.currently.precipType ? "wet" : "dry"
    if( water != state.water )
    {
        state.water = water
        sendEvent( name: "water", value: state.water )
    }
    
    def illuminance = _estimateLux( json.daily.data[0].sunriseTime, json.daily.data[0].sunsetTime, json.currently.icon, json.currently.cloudCover )
    if( illuminance != state.illuminance )
    {
        state.illuminance = illuminance
        sendEvent( name: "illuminance", value: state.illuminance, unit: "lux" )
    }   
}

private _estimateLux( sunrise, sunset, weatherIcon, cloudCover )
{
    def now = new Date().time / 1000
    
    // shift civil sunset/sunrise by 30 minutes to adjust approximately for nautical sunrise/sunset
    sunset = sunset + (60 * 30)
    sunrise = sunrise - (60 * 30)
    
    def timeToSunrise = sunrise - now
    def timeToSunset = sunset - now
    
    def isNight = (now < sunrise) || (now > sunset)
    
    def lux = 0
    
    // 60 minutes after/before nautical sunrise/sunset, start the ramp
    def sunsetSunriseDelta = ( 60 * 60 )
    
    switch( weatherIcon )
    {
        case ['fog']:
        	lux = 1000
        	break

        case ['rain', 'snow', 'sleet', 'cloudy']:
        	lux = 2500
        	break

        case 'partly-cloudy-day':
        	lux = 25000 * (1 - cloudCover)
        	break

        case ['clear-night', 'partly-cloudy-night']:
        	lux = 0
        	break;

        default:
            //['clear-day', 'wind']
            lux = 35000 * (1 - cloudCover)
        	break
    }
    
    // Idealy we locate the angle of the sun (given a geography and time) and use the available sun area above the horizon
    // but without that complexity, just use a fixed exponential growth/decay
    def horizonFactor = 1
    
    if( (timeToSunrise <= 0) && (Math.abs(timeToSunrise) <= sunsetSunriseDelta) )
    {
        // daybreak
        horizonFactor = 0.0041 * Math.exp( 0.0031 * Math.abs(timeToSunrise) ) / 100
    }
    else if( (timeToSunset >= 0) && (timeToSunset <= sunsetSunriseDelta) )
    {
        // golden hour
        horizonFactor = 0.0041 * Math.exp( 0.0031 * timeToSunset ) / 100
    }
    else if( isNight )
    {
        horizonFactor = 0
    }

    def newLux = Math.round( lux * horizonFactor )
    
    log.info "${now},${timeToSunrise},${timeToSunset},${weatherIcon},${cloudCover},${isNight},${lux},${horizonFactor},${newLux}"

    newLux
}
