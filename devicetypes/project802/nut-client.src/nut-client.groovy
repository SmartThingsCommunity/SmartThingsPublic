/**
 *  NUT Client
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
 *  For more information, visit https://github.com/project802/smartthings
 */
metadata {
    definition (name: "NUT Client", namespace: "project802", author: "Chris Vincent") {
        capability "Refresh"
        capability "Sensor"
        capability "Battery"
        capability "Power Meter"
        capability "Power Source"
        capability "Voltage Measurement"
    }

    simulator {
        
    }

    tiles {
        valueTile( "batterySmall", "device.battery", decoration : "flat" )
        {
            state "default", label : '${currentValue}%'
        }
        
        valueTile( "battery", "device.battery", decoration : "flat" )
        {
            state "default", label : 'Battery ${currentValue}%'
        }
        
        valueTile( "power", "device.power", decoration : "flat" )
        {
            state "default", label : 'Load ${currentValue} W'
        }
        
        valueTile( "voltage", "device.voltage", decoration : "flat" )
        {
            state "default", label : 'Input ${currentValue} V'
        }
        
        valueTile( "powerSource", "device.powerSource", decoration : "flat" )
        {
            state "default", label : '${currentValue}'
        }
        
        main( "batterySmall" )
        details( "battery", "power", "voltage", "powerSource" )
    }
    
    preferences {
        input name: "nutServer", type: "text", title: "NUT Proxy Server IP:port", description: "IP:port address of the NUT proxy server", defaultValue: "10.0.0.6:80"
    }
}

def updated()
{
    unschedule()
    
    log.info "NUT client (${device.displayName}) updated"
    
    runEvery5Minutes( ups_poll )
}

def refresh()
{
}

def ups_poll()
{
    def hubAction = new physicalgraph.device.HubAction(
        [
            path: "/cgi-bin/upsstat.cgi",
            method: "GET",
            HOST: settings.nutServer,
            headers: [
                "Host":"${settings.nutServer}",
                "Accept":"*/*"
            ]        
        ],
        null,
        [
            callback: ups_pollCallback 
        ]
    );

    sendHubCommand( hubAction )
}

def ups_pollCallback( physicalgraph.device.HubResponse hubResponse )
{
    //log.info "ups_pollCallback"
    
    if( hubResponse.status != 200 )
    {
        log.error "ups_pollCallback: bad response from NUT proxy server"
        return
    }
    
    def body = hubResponse.body
    def upsVars = [:]
    
    body.eachLine { line ->
        def splitLine = line.trim()split( " " )
        upsVars.put( splitLine[2], splitLine[3].replace("\"", "") )
    }
    
    def batteryChargePercent = Math.floor( upsVars.get("battery.charge").toFloat() ) as Integer
    
    def inputVoltage = upsVars.get("input.voltage").toFloat()
    
    def maxLoadWatts = upsVars.get("ups.realpower.nominal").toFloat()
    def loadPercent = upsVars.get("ups.load").toFloat()
    def loadWatts = Math.ceil( (loadPercent/100) * maxLoadWatts ) as Integer
    
    def upsStatus = upsVars.get("ups.status")
    
    _sendBattery( batteryChargePercent )
    _sendPower( loadWatts )
    _sendVoltage( inputVoltage )
    
    if( upsStatus == "OL" )
    {
        _sendPowerSource( "mains" )
    }
    else
    {
        _sendPowerSource( "battery" )
    }
}

def installed()
{
    log.info "NUT client (${device.displayName}) installed"
}

private _sendBattery( batteryChargePercent )
{
    def map = [
                name : "battery",
                value : batteryChargePercent,
                descriptionText : device.displayName + " battery is ${batteryChargePercent}%",
                unit : "%"
              ]
    
    sendEvent( map )
}

private _sendPower( powerWatts )
{
    def map = [
                name : "power",
                value : powerWatts,
                descriptionText : device.displayName + " load is ${powerWatts} W",
                unit : "W"
              ]
    
    sendEvent( map )
}

private _sendVoltage( voltage )
{
    def map = [
                name : "voltage",
                value : voltage,
                descriptionText : device.displayName + " AC line is ${voltage} V",
                unit : "V"
              ]
    
    sendEvent( map )
}

private _sendPowerSource( powerSource )
{
    def map = [
                name : "powerSource",
                value : powerSource,
                descriptionText : device.displayName + " is on ${powerSource}"
              ]
    
    sendEvent( map )
}
