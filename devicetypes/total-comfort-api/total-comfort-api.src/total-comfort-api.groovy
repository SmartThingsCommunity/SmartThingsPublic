/**
 *  Total Comfort API
 *   
 *  Based on Code by Eric Thomas
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *   lgk v 3 added optional outdoor temp sensors and preferences for it, also made api login required.
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * lgk version 4 supports celsius and fahrenheit with option, and now colors.
 * lgk version 5, due to intermittant update failures added last update date/time tile so that you can see when it happended
 * not there is a new input tzoffset which defaults to my time ie -5 which you must set .
 * lgk version 6 add support for actually knowing the fan is on or not (added tile),
 * and also the actual operating state ie heating,cooling or idle via new response variables.
 * lgk version 7, change the new operating state to be a value vs standard tile
 * to work around a bug smartthings caused in the latest 2.08 release with text wrapping.
 * related also added icons to the operating state, and increase the width of the last update
 * to avoid wrapping.
 * 2-14-16 llb added full path to icons so the show on android Fire Tablet
 * 2-14-16 llb added check for indoor humidity sensor to avoid 128 value if not supported
 * 2-14-16 llb modified operating state tile to show control by Hold or Schedule
 *
 */
preferences {
    input("username", "text", title: "Username", description: "Your Total Comfort User Name", required: true)
    input("password", "password", title: "Password", description: "Your Total Comfort password",required: true)
    input("honeywelldevice", "text", title: "Device ID", description: "Your Device ID", required: true)
    input ("enableOutdoorTemps", "enum", title: "Do you have the optional outdoor temperature sensor and want to enable it?", options: ["Yes", "No"], required: false, defaultValue: "No")
    input ("tempScale", "enum", title: "Fahrenheit or Celsius?", options: ["F", "C"], required: false, defaultValue: "F")
  	input("tzOffset", "number", title: "Time zone offset +/-xx?", required: false, defaultValue: -5, description: "Time Zone Offset ie -5.")  
  }

metadata {
  definition (name: "Total Comfort API", namespace: 
"Total Comfort API", author: "Eric Thomas, modified lg kahn") {
    capability "Polling"
    capability "Thermostat"
    capability "Refresh"
    capability "Temperature Measurement"
    capability "Sensor"
    capability "Relative Humidity Measurement"    
    command "heatLevelUp"
    command "heatLevelDown"
    command "coolLevelUp"
    command "coolLevelDown"
    attribute "outdoorHumidity", "number"
    attribute "outdoorTemperature", "number"
    attribute "lastUpdate", "string"

    
  }

  simulator {
    // TODO: define status and reply messages here
  }

   tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
            state("temperature", label: '${currentValue}°', 
             icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@3x.png",
             unit:"F", backgroundColors: [            
             		[value: -14, color: "#1e9cbb"],
         	        [value: -10, color: "#90d2a7"],
               		[value: -5, color: "#44b621"],
             	    [value: -2, color: "#f1d801"],
             	    [value: 0, color: "#153591"],
               	    [value: 7, color: "#1e9cbb"],
     			    [value: 15, color: "#90d2a7"],           
              		[value: 23, color: "#44b621"],
            	    [value: 29, color: "#f1d801"],
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
        
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, canChangeIcon: true) {
            state "off", label:'${name}', action:"thermostat.cool", icon: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor19.png"
            state "cool", label:'${name}', action:"thermostat.heat", icon: "http://cdn.device-icons.smartthings.com/Weather/weather7.png", backgroundColor: '#1e9cbb'
            state "heat", label:'${name}', action:"thermostat.auto", icon: "http://cdn.device-icons.smartthings.com/Weather/weather14.png", backgroundColor: '#E14902'  
            state "auto", label:'${name}', action:"thermostat.off", icon: "http://cdn.device-icons.smartthings.com/Weather/weather3.png", backgroundColor: '#44b621'
        }
        standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: false, canChangeIcon: true) {
            state "auto", label:'${name}', action:"thermostat.fanAuto", icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png", backgroundColor: '#44b621'
            state "circulate", label:'${name}', action:"thermostat.fanCirculate", icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png", backgroundColor: '#44b621'
            state "on", label:'${name}', action:"thermostat.fanOn", icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png", backgroundColor: '#44b621'
        }

        controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 3, width: 1, inactiveLabel: false) {
            state "setCoolingSetpoint", label:'Set temperarure to', action:"thermostat.setCoolingSetpoint", 
            backgroundColors:[
             	    [value: 0, color: "#153591"],
               	    [value: 7, color: "#1e9cbb"],
     			    [value: 15, color: "#90d2a7"],           
              		[value: 23, color: "#44b621"],
            	    [value: 29, color: "#f1d801"],
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
            ]               
        }
       valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) 
    	  {
          state "default", label:'Cool\n${currentValue}°', unit:"F",
           backgroundColors: [
          		    [value: 0, color: "#153591"],
               	    [value: 7, color: "#1e9cbb"],
     			    [value: 15, color: "#90d2a7"],           
              		[value: 23, color: "#44b621"],
            	    [value: 29, color: "#f1d801"],
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
          ]   
        }
   valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) 
    	{
      state "default", label:'Heat\n${currentValue}°', unit: "F",
       backgroundColors:[
     			    [value: 0, color: "#153591"],
               	    [value: 7, color: "#1e9cbb"],
     			    [value: 15, color: "#90d2a7"],           
              		[value: 23, color: "#44b621"],
            	    [value: 29, color: "#f1d801"],
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
      ]   
    }
    
        
        //tile added for operating state - Create the tiles for each possible state, look at other examples if you wish to change the icons here. 
        
        valueTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false) {
            state 'default', label:'${currentValue}',
                    backgroundColors:[
                                 [value: 0, color: "#911535"]]
            state "Unknown", label:'${name}', backgroundColor : '#cc0000', icon: ""
        }
        
           standardTile("fanOperatingState", "device.fanOperatingState", inactiveLabel: false) {
            state "On", label:'${name}',icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png", backgroundColor : '#53a7c0'
            state "Idle", label:'${name}',icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png"
            state "Unknown", label:'${name}',icon: "http://cdn.device-icons.smartthings.com/Appliances/appliances11.png", backgroundColor : '#cc0000'
        }
        
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"http://cdn.device-icons.smartthings.com/secondary/refresh@2x.png"
        }
        
        standardTile("heatLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelUp", label:'  ', action:"heatLevelUp", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-up.png"
        }
        standardTile("heatLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelDown", label:'  ', action:"heatLevelDown", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-down.png"
        }
        standardTile("coolLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelUp", label:'  ', action:"coolLevelUp", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-up.png"
        }
        standardTile("coolLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelDown", label:'  ', action:"coolLevelDown", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-down.png"
        }
        
        valueTile("relativeHumidity", "device.relativeHumidity", inactiveLabel: false)
        {
        	state "default", label:'Humidity\n${currentValue}%',
             icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@3x.png",
             unit:"%", backgroundColors : [
                    [value: 01, color: "#724529"],
                    [value: 11, color: "#724529"],
                    [value: 21, color: "#724529"],
                    [value: 35, color: "#44b621"],
                    [value: 49, color: "#44b621"],
                    [value: 50, color: "#1e9cbb"]
                ]
        }

        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, canChangeIcon: true) 
        {
		}
        
        /* lgk new tiles for outside temp and hummidity */
          valueTile("outdoorTemperature", "device.outdoorTemperature", width: 1, height: 1, canChangeIcon: true) {
            state("temperature", label: 'Outdoor\n ${currentValue}°',
            icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@3x.png",
            unit:"F", backgroundColors: [
                    [value: -31, color: "#003591"],
                    [value: -10, color: "#90d2a7"],
               		[value: -5, color: "#44b621"],
             	    [value: -2, color: "#f1d801"],
             	    [value: 0, color: "#153591"],
               	    [value: 7, color: "#1e9cbb"],
                    [value: 00, color: "#cccccc"],
                    [value: 31, color: "#153500"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        
         valueTile("outdoorHumidity", "device.outdoorHumidity", inactiveLabel: false){
        	state "default", label:'Outdoor\n ${currentValue}%', 
            icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@3x.png",
            unit:"%", backgroundColors : [
                    [value: 01, color: "#724529"],
                    [value: 11, color: "#724529"],
                    [value: 21, color: "#724529"],
                    [value: 35, color: "#44b621"],
                    [value: 49, color: "#44b621"],
                    [value: 70, color: "#449c00"],
                    [value: 90, color: "#009cbb"]
                  
                ]
        }

      		valueTile("status", "device.lastUpdate", width: 3, height: 1, decoration: "flat") {
			state "default", label: 'Last Update: ${currentValue}'
		}

        main "temperature"
        details(["temperature", "thermostatMode", "thermostatFanMode",   
        "heatLevelUp", "heatingSetpoint" , "heatLevelDown", "coolLevelUp",
        "coolingSetpoint", "coolLevelDown" ,"thermostatOperatingState","fanOperatingState",
        "refresh","relativeHumidity","outdoorTemperature","outdoorHumidity", "status"])
       
    }
}

def coolLevelUp()
{
state.DisplayUnits = settings.tempScale
if (state.DisplayUnits == "F")
{
    int nextLevel = device.currentValue("coolingSetpoint") + 1
    
    if( nextLevel > 99){
      nextLevel = 99
    }
    log.debug "Setting cool set point up to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)
}
else
{
 int nextLevel = device.currentValue("coolingSetpoint") + 0.5
    
    if( nextLevel > 37){
      nextLevel = 37
    }
    log.debug "Setting cool set point up to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)

}
}

def coolLevelDown()
{
state.DisplayUnits = settings.tempScale
if (state.DisplayUnits == "F")
{
    int nextLevel = device.currentValue("coolingSetpoint") - 1
    
    if( nextLevel < 50){
      nextLevel = 50
    }
    log.debug "Setting cool set point down to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)
}

else

{
 double nextLevel = device.currentValue("coolingSetpoint") - 0.5
    
    if( nextLevel < 10){
      nextLevel = 10
    }
    log.debug "Setting cool set point down to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)

}
}

def heatLevelUp()
{
state.DisplayUnits = settings.tempScale
if (state.DisplayUnits == "F")
{
   log.debug "in fahrenheit level up"
    int nextLevel = device.currentValue("heatingSetpoint") + 1
    
    if( nextLevel > 90){
      nextLevel = 90
    }
    log.debug "Setting heat set point up to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}

else
{

   log.debug "in celsius level uo"
  double nextLevel = device.currentValue("heatingSetpoint") + 0.5
    
    if( nextLevel > 33){
      nextLevel = 33
    }
    log.debug "Setting heat set point up to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}

}



def heatLevelDown()
{
state.DisplayUnits = settings.tempScale
if (state.DisplayUnits == "F")
{
   log.debug "in fahrenheit level down"
    int nextLevel = device.currentValue("heatingSetpoint") - 1
    
    if( nextLevel < 40){
      nextLevel = 40
    }
    log.debug "Setting heat set point down to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}

else
{

   log.debug "in celsius level down"
  double nextLevel = device.currentValue("heatingSetpoint") - 0.5
    
    if( nextLevel < 4){
      nextLevel = 4
    }
    log.debug "Setting heat set point down to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}

}



// parse events into attributes
def parse(String description) {
    
}

// handle commands

def setHeatingSetpoint(Double temp)
{
 data.SystemSwitch = 'null' 
    data.HeatSetpoint = temp
    data.CoolSetpoint = 'null'
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()

    if(data.SetStatus==1)
  {
        sendEvent(name: 'heatingSetpoint', value: temp as double)

    }	
}

def setHeatingSetpoint(temp) {
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = temp
    data.CoolSetpoint = 'null'
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()

    if(data.SetStatus==1)
  {
        sendEvent(name: 'heatingSetpoint', value: temp as Integer)

    }
        
}

def setCoolingSetpoint(double temp) {
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = 'null'
    data.CoolSetpoint = temp
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()
    
    if(data.SetStatus==1)
  {
        sendEvent(name: 'coolingSetpoint', value: temp as double)

    }
}

def setCoolingSetpoint(temp) {
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = 'null'
    data.CoolSetpoint = temp
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()
    
    if(data.SetStatus==1)
  {
        sendEvent(name: 'coolingSetpoint', value: temp as Integer)

    }
}

def setTargetTemp(temp) {
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = temp
    data.CoolSetpoint = temp
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()
}



def setTargetTemp(double temp) {
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = temp
    data.CoolSetpoint = temp
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='1'
    data.StatusCool='1'
    data.FanMode = 'null'
  setStatus()
}


def off() {
  setThermostatMode(2)
}

def auto() {
  setThermostatMode(4)
}

def heat() {
  setThermostatMode(1)
}

def emergencyHeat() {

}

def cool() {
  setThermostatMode(3)
}

def setThermostatMode(mode) {
  data.SystemSwitch = mode 
    data.HeatSetpoint = 'null'
    data.CoolSetpoint = 'null'
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat=1
    data.StatusCool=1
    data.FanMode = 'null'

  setStatus()
    
      def switchPos

        if(mode==1)
          switchPos = 'heat'
        if(mode==2)
          switchPos = 'off'
        if(mode==3)
          switchPos = 'cool'
 /* lgk modified my therm has pos 5 for auto vision pro */
       if(mode==4 || swithPos == 5)
          switchPos = 'auto'

  if(data.SetStatus==1)
  {
        sendEvent(name: 'thermostatMode', value: switchPos)
    }
    
}

def fanOn() {
    setThermostatFanMode(1)
}

def fanAuto() {
    setThermostatFanMode(0)
}

def fanCirculate() {
    setThermostatFanMode(2)
}

def setThermostatFanMode(mode) {    
  
  data.SystemSwitch = 'null' 
    data.HeatSetpoint = 'null'
    data.CoolSetpoint = 'null'
    data.HeatNextPeriod = 'null'
    data.CoolNextPeriod = 'null'
    data.StatusHeat='null'
    data.StatusCool='null'
    data.FanMode = mode

  setStatus()

  def fanMode

    if(mode==0)
      fanMode = 'auto'
    if(mode==1)
      fanMode = 'on'
    if(mode==2)
      fanMode = 'circulate'
   
  if(data.SetStatus==1)
  {
      sendEvent(name: 'thermostatFanMode', value: fanMode)    
    }

}


def poll() {
refresh()
}


def setStatus() {

  data.SetStatus = 0

    login()
  log.debug "Executing 'setStatus'"
def today= new Date()
log.debug "https://www.mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges"
log.debug "setting heat setpoint to $data.HeatSetpoint"
log.debug "setting cool setpoint to $data.CoolSetpoint"
    
    def params = [
        uri: "https://www.mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges",
        headers: [
              'Accept': 'application/json, text/javascript, */*; q=0.01',
              'DNT': '1',
        	  'Accept-Encoding': 'gzip,deflate,sdch',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': "https://www.mytotalconnectcomfort.com/portal/Device/Control/${settings.honeywelldevice}",
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
        body: [ DeviceID: "${settings.honeywelldevice}", SystemSwitch : data.SystemSwitch ,HeatSetpoint : 
        data.HeatSetpoint, CoolSetpoint: data.CoolSetpoint, HeatNextPeriod: 
        data.HeatNextPeriod,CoolNextPeriod:data.CoolNextPeriod,StatusHeat:data.StatusHeat,
        StatusCool:data.StatusCool,FanMode:data.FanMode,ThermostatUnits: settings.tempScale]

]

log.debug "params = $params"
    httpPost(params) { response ->
        log.debug "Request was successful, $response.status"
 
    }
    
    log.debug "SetStatus is 1 now"
    data.SetStatus = 1

}

def getStatus() {
  log.debug "Executing getStatus"
  log.debug "enable outside temps = $enableOutdoorTemps"
def today= new Date()
log.debug "https://www.mytotalconnectcomfort.com/portal/Device/CheckDataSession/${settings.honeywelldevice}?_=$today.time"

    def params = [
        uri: "https://www.mytotalconnectcomfort.com/portal/Device/CheckDataSession/${settings.honeywelldevice}",
        headers: [
              'Accept': '*/*',
              'DNT': '1',
              'Cache' : 'false',
              'dataType': 'json',
              'Accept-Encoding': 'plain',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Referer': 'https://www.mytotalconnectcomfort.com/portal',
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
    ]

		log.debug "doing request"
        
        httpGet(params) { response ->
        log.debug "Request was successful, $response.status"
        //log.debug "data = $response.data"
        log.debug "ld = $response.data.latestData"
       
        def curTemp = response.data.latestData.uiData.DispTemperature
        def fanMode = response.data.latestData.fanData.fanMode
        def switchPos = response.data.latestData.uiData.SystemSwitchPosition
        def coolSetPoint = response.data.latestData.uiData.CoolSetpoint
        def heatSetPoint = response.data.latestData.uiData.HeatSetpoint
        def statusCool = response.data.latestData.uiData.StatusCool
        def statusHeat = response.data.latestData.uiData.StatusHeat
        def curHumidity = response.data.latestData.uiData.IndoorHumidity
        def Boolean hasOutdoorHumid = response.data.latestData.uiData.OutdoorHumidityAvailable
        def Boolean hasOutdoorTemp = response.data.latestData.uiData.OutdoorTemperatureAvailable
        def curOutdoorHumidity = response.data.latestData.uiData.OutdoorHumidity
        def curOutdoorTemp = response.data.latestData.uiData.OutdoorTemperature
        def displayUnits = response.data.latestData.uiData.DisplayUnits
        def fanIsRunning = response.data.latestData.fanData.fanIsRunning
        def equipmentStatus = response.data.latestData.uiData.EquipmentOutputStatus
        def Boolean hasIndoorHumid = response.data.latestData.uiData.IndoorHumiditySensorAvailable
        def curSetpointStatus = response.data.latestData.uiData.CurrentSetpointStatus
        def TempHoldTime = response.data.latestData.uiData.TemporaryHoldUntilTime
        def int TempHoldTimeHrs = TempHoldTime / 60
        def TempHoldTimeMins = TempHoldTime % 60
        
/*ld = [fanData:[fanModeCirculateAllowed:true, fanModeAutoAllowed:true, fanModeFollowScheduleAllowed:false, 
        fanIsRunning:false, fanModeOnAllowed:true, fanMode:0], 
        drData:[Load:127.5, HeatSetpLimit:0,
        OptOutable:false, DeltaHeatSP:-0.01, CoolSetpLimit:0, Phase:-1, DeltaCoolSP:-0.01], 
        uiData:[OutdoorTemperature:128.0000, TemporaryHoldUntilTime:0, ScheduleHeatSp:67.0000, 
        DeviceID:453824, DispTemperatureAvailable:true, VacationHold:0, VacationHoldUntilTime:0,
        CoolSetpoint:76.0000, ScheduleCoolSp:76.0000, SwitchHeatAllowed:true, CoolNextPeriod:67, 
        IndoorHumidity:31.0000, SwitchAutoAllowed:true, SetpointChangeAllowed:true, HeatLowerSetptLimit:40.0000,
        OutdoorHumidStatus:128, SwitchOffAllowed:true, OutdoorHumidityAvailable:false,
        StatusCool:0, OutdoorTemperatureAvailable:false, EquipmentOutputStatus:0, StatusHeat:0, 
        CurrentSetpointStatus:0, HoldUntilCapable:true, CoolUpperSetptLimit:99.0000, SwitchCoolAllowed:true, 
        OutdoorHumidity:128.0000, DualSetpointStatus:false, SwitchEmergencyHeatAllowed:false, Commercial:false, 
        CoolLowerSetptLimit:50.0000, OutdoorHumiditySensorNotFault:true, IndoorHumiditySensorAvailable:true,
        ScheduleCapable:true, DisplayUnits:F, DispTemperature:70.0000, Deadband:3.0000, HeatUpperSetptLimit:90.0000, 
        IsInVacationHoldMode:false, OutdoorTemperatureSensorNotFault:true, HeatSetpoint:67.0000, DispTemperatureStatus:0,
        HeatNextPeriod:67, IndoorHumiditySensorNotFault:true, OutdoorTempStatus:128, IndoorHumidStatus:0, 
        SystemSwitchPosition:4], canControlHumidification:false, hasFan:true] 
        
        log.trace("Fan operating state: ${response.data.latestData.fanData.fanIsRunning}")
        log.trace("EquipmentOutputStatus: ${response.data.latestData.uiData.EquipmentOutputStatus}")
        log.trace("IndoorHumidity: ${response.data.latestData.uiData.IndoorHumidity}")
          
        log.trace("OutdoorTemp = ${response.data.latestData.uiData.OutdoorTemperature}")
        log.trace("fanMode: ${response.data.latestData.fanData.fanMode}")
        log.trace("SystenSwitchPosition: ${response.data.latestData.uiData.SystemSwitchPosition}")
        log.trace("StatusCool: ${response.data.latestData.uiData.StatusCool}")
        log.trace("StatusHeat: ${response.data.latestData.uiData.StatusHeat}")
        
        log.trace("IndoorHumiditySensorAvailable: ${response.data.latestData.uiData.IndoorHumiditySensorAvailable}")        
        log.trace("IndoorHumidityAvailable: ${response.data.latestData.uiData.IndoorHumidityAvailable}")        
       
        log.debug "OutdoorHumidityAvailable: response.data.latestData.uiData.OutdoorHumidityAvailable"        
        log.debug "OutdoorTemperatureAvailable: $response.data.latestData.uiData.OutdoorTemperatureAvailable"        
        
		log.debug "OutdoorHumiditySensorNotFault = $response.data.latestData.uiData.OutdoorHumiditySensorNotFault"
		log.debug "OutdoorTemperatureSensorNotFault = $response.data.latestData.uiData.OutdoorTemperatureSensorNotFault"
          
        log.debug "IndoorHumiditySensorNotFault: $response.data.latestData.uiData.IndoorHumiditySensorNotFault"        
        log.debug "IndoorHumidStatus: $response.data.latestData.uiData.IndoorHumidStatus"       
        log.debug "OutdoorHumidStatus: $response.data.latestData.uiData.OutdoorHumidStatus"   
        log.debug "OutdoorHumidity: = $response.data.latestData.uiData.OutdoorHumidity"
        log.debug "OutdoorTemperature = $response.data.latestData.uiData.OutdoorTemperature"
        
        log.debug "got curOutdoorTemp = $curOutdoorTemp"
        log.debug "got curOutdoorHumidity = $curOutdoorHumidity"
        log.debug "hasOutdoorHumid = $hasOutdoorHumid"
        log.debug "hasOutdoorTemp =  $hasOutdoorTemp"
      */
      
      //  log.debug "displayUnits = $displayUnits"
        state.DisplayUnits = $displayUnits
        
        //Operating State Section 
        //Set the operating state to off 
        
        def operatingState = "Unknown"
          
// lgk operating state not working here.. shows both on ie 1 when heat doesnt go on to 67 and heat till 76  and current is 73 
        //Check the status of heat and cool 
     
     // lgk old method now use equipment status
     if (equipmentStatus == 1) {
        operatingState = "HEAT ON"
      } else if (equipmentStatus == 2) {
        operatingState = "COOL ON"
      } else if (equipmentStatus == 0) {
        operatingState = "IDLE"
      } else {
          	operatingState = "Unknown"
      }
      
     if(curSetpointStatus == 0) {
            operatingState = "$operatingState\nControl By\nFollowing\nSchedule"
        } else if(curSetpointStatus == 1) {  
            operatingState = "$operatingState\nControl By\nHold Until\n" + String.format("%02d:%02d", TempHoldTimeHrs, TempHoldTimeMins)
        } else if(curSetpointStatus == 2) {
            operatingState = "$operatingState\nControl By\nPermanent\nHold"
      
        } else {
            operatingState = "unknown"
        }
      
      /*
     if(statusCool == 1 && (switchPos == 3 || switchPos == 5 || swithPos == 4)) {
            operatingState = "cooling"
        } else if (statusHeat == 1 && (switchPos == 1 || switchPos == 5 || switchPos == 4)) {  
            operatingState = "heating"
        } else if (statusCool == 0 && statusHeat == 0) {
            operatingState = "idle"
           
        } else {
          	operatingState = "unknown"
        }
        */

        log.trace("Set operating State to: ${operatingState}")        

     // set fast state
     def fanState = "Unknown"

	if (fanIsRunning == true)
      fanState = "On"
    else fanState = "Idle" 

    log.trace("Set Fan operating State to: ${fanState}")        

        //End Operating State
        
      //  log.debug curTemp
       // log.debug fanMode
       // log.debug switchPos
       
        //fan mode 0=auto, 2=circ, 1=on
        
        if(fanMode==0)
          fanMode = 'auto'
        if(fanMode==1)
          fanMode = 'on'
        if(fanMode==2)
          fanMode = 'circulate'

        if(switchPos==1)
          switchPos = 'heat'
        if(switchPos==2)
          switchPos = 'off'
        if(switchPos==3)
          switchPos = 'cool'
        if(switchPos==4 || switchPos==5)
          switchPos = 'auto'

    def formattedCoolSetPoint = String.format("%5.1f", coolSetPoint)
    def formattedHeatSetPoint = String.format("%5.1f", heatSetPoint)
    def formattedTemp = String.format("%5.1f", curTemp)
    
   	def finalCoolSetPoint = formattedCoolSetPoint as BigDecimal
	def finalHeatSetPoint = formattedHeatSetPoint as BigDecimal
	def finalTemp = formattedTemp as BigDecimal

	//Send events 
        sendEvent(name: 'thermostatOperatingState', value: operatingState)
        sendEvent(name: 'fanOperatingState', value: fanState)
        sendEvent(name: 'thermostatFanMode', value: fanMode)
        sendEvent(name: 'thermostatMode', value: switchPos)
        sendEvent(name: 'coolingSetpoint', value: finalCoolSetPoint )
        sendEvent(name: 'heatingSetpoint', value: finalHeatSetPoint )
        sendEvent(name: 'temperature', value: finalTemp, state: switchPos)
        if (hasIndoorHumid){sendEvent(name: 'relativeHumidity', value: curHumidity as Integer)}
        
       if (settings.tzOffset == null)
        settings.tzOffset = -5

        def now = new Date()
        def tf = new java.text.SimpleDateFormat("MM/dd/yyyy h:mm a")
        tf.setTimeZone(TimeZone.getTimeZone("GMT${settings.tzOffset}"))
        def newtime = "${tf.format(now)}" as String   
        sendEvent(name: "lastUpdate", value: newtime, descriptionText: "Last Update: $newtime")

        
      if (enableOutdoorTemps == "Yes")
        {
    
       if (hasOutdoorHumid)
        {
          sendEvent(name: 'outdoorHumidity', value: curOutdoorHumidity as Integer)
        }
     
       if (hasOutdoorTemp)
        {
          sendEvent(name: 'outdoorTemperature', value: curOutdoorTemp as Integer)
        }
      }
      
    
        

}
}


def getHumidifierStatus()
{
/*
 $.ajax({
            url: humUrl,
            type: 'POST',
            cache: false,
            dataType: "json",
            success: function(data) {
            /portal/Device/Menu/GetHumData/454832';
     */
    def params = [
        uri: "https://www.mytotalconnectcomfort.com/portal/Device/Menu/GetHumData/${settings.honeywelldevice}",
        headers: [
              'Accept': '*/*',
              'DNT': '1',
              'dataType': 'json',
              'cache': 'false',
              'Accept-Encoding': 'plain',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': 'https://www.mytotalconnectcomfort.com/portal/Menu/${settings.honeywelldevice}',
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
    ]
        httpGet(params) { response ->
        log.debug "GetHumidity Request was successful, $response.status"
        log.debug "response = $response.data"
       
      //  log.debug "ld = $response.data.latestData"
       //  log.debug "humdata = $response.data.latestData.humData"

        log.trace("lowerLimit: ${response.data.latestData.humData.lowerLimit}")        
        log.trace("upperLimit: ${response.data.humData.upperLimit}")        
        log.trace("SetPoint: ${response.data.humData.Setpoint}")        
        log.trace("DeviceId: ${response.data.humData.DeviceId}")        
        log.trace("IndoorHumidity: ${response.data.humData.IndoorHumidity}")        

}
}

def api(method, args = [], success = {}) {

}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {

}

def refresh() {
  log.debug "Executing 'refresh'"
  def unit = getTemperatureScale()
  log.debug "units = $unit"
    login()
    //getHumidifierStatus()
    getStatus()
}

def login() {  
  log.debug "Executing 'login'"
      
    def params = [
        uri: 'https://www.mytotalconnectcomfort.com/portal',
        headers: [
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Encoding': 'sdch',
            'Host': 'www.mytotalconnectcomfort.com',
            'DNT': '1',
            'Origin': 'www.mytotalconnectcomfort.com/portal/',
            'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36'
        ],
        body: [timeOffset: '240', UserName: "${settings.username}", Password: "${settings.password}", RememberMe: 'false']
    ]

  data.cookiess = ''

    httpPost(params) { response ->
        log.debug "Request was successful, $response.status"
        log.debug response.headers
    response.getHeaders('Set-Cookie').each {
          String cookie = it.value.split(';|,')[0]
      log.debug "Adding cookie to collection: $cookie"
            if(cookie != ".ASPXAUTH_TH_A=") {
      data.cookiess = data.cookiess+cookie+';'
            }
        }
        log.debug "cookies: $data.cookiess"

    }
}

def isLoggedIn() {
    if(!data.auth) {
        log.debug "No data.auth"
        return false
    }
    
    def now = new Date().getTime();
    return data.auth.expires_in > now
}


def updated()
{
log.debug "in updated"
state.DisplayUnits = settings.tempScale
 log.debug "display units now = $state.DisplayUnits"
   
}

def installed() {
  state.DisplayUnits = settings.tempScale
  
  log.debug "display units now = $state.DisplayUnits"
}