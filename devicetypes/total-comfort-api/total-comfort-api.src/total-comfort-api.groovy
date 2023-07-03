/**
 *  Total Comfort API
 *   
 *  Based on Code by Eric Thomas
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
preferences {
    input("username", "text", title: "Username", description: "Your Total Comfort User Name")
    input("password", "password", title: "Password", description: "Your Total Comfort password")
    input("honeywelldevice", "text", title: "Device ID", description: "Your Device ID")
    
}
metadata {
  definition (name: "Total Comfort API", namespace: "Total Comfort API", author: "Eric Thomas") {
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
  }

  simulator {
    // TODO: define status and reply messages here
  }

   tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
            state("temperature", label: '${currentValue}°F', unit:"F", backgroundColors: [
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
        	state "off", label:'${name}', action:"thermostat.cool", icon: "st.Outdoor.outdoor19"
            state "cool", label:'${name}', action:"thermostat.heat", icon: "st.Weather.weather7", backgroundColor: '#003CEC'
            state "heat", label:'${name}', action:"thermostat.auto", icon: "st.Weather.weather14", backgroundColor: '#E14902'  
            state "auto", label:'${name}', action:"thermostat.off", icon: "st.Weather.weather3", backgroundColor: '#44b621'
        }
        standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: false, canChangeIcon: true) {
            state "auto", label:'${name}', action:"thermostat.fanOn", icon: "st.Appliances.appliances11"
            state "on", label:'${name}', action:"thermostat.fanAuto", icon: "st.Appliances.appliances11", backgroundColor: '#44b621'
        
        }

        controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 3, width: 1, inactiveLabel: false) {
            state "setCoolingSetpoint", label:'Set temperarure to', action:"thermostat.setCoolingSetpoint", 
            backgroundColors:[
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
          state "default", label:'Cool @${currentValue}°F', unit:"F",
           backgroundColors:
           [
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
      state "default", label:'Heat @${currentValue}°F', unit:"F",
       backgroundColors:
       [
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
        
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false) {
            state "heating", label:'${name}'
            state "cooling", label:'${name}' 
            state "idle", label:'${name}'
        }
        
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        standardTile("heatLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelUp", label:'  ', action:"heatLevelUp", icon:"st.thermostat.thermostat-up"
        }
        standardTile("heatLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelDown", label:'  ', action:"heatLevelDown", icon:"st.thermostat.thermostat-down"
        }
        standardTile("coolLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelUp", label:'  ', action:"coolLevelUp", icon:"st.thermostat.thermostat-up"
        }
        standardTile("coolLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelDown", label:'  ', action:"coolLevelDown", icon:"st.thermostat.thermostat-down"
        }
        
        valueTile("relativeHumidity", "device.relativeHumidity", inactiveLabel: false){
        	state "default", label:'${currentValue}', unit:"%"
        	
		}
        main "temperature"
        details(["temperature", "thermostatMode", "thermostatFanMode",   "heatLevelUp", "heatingSetpoint" , "heatLevelDown", "coolLevelUp","coolingSetpoint", "coolLevelDown" ,"thermostatOperatingState", "refresh","relativeHumidity"])
    }
}

def coolLevelUp(){
    int nextLevel = device.currentValue("coolingSetpoint") + 1
    
    if( nextLevel > 99){
      nextLevel = 99
    }
    log.debug "Setting cool set point up to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)
}

def coolLevelDown(){
    int nextLevel = device.currentValue("coolingSetpoint") - 1
    
    if( nextLevel < 50){
      nextLevel = 50
    }
    log.debug "Setting cool set point down to: ${nextLevel}"
    setCoolingSetpoint(nextLevel)
}

def heatLevelUp(){
    int nextLevel = device.currentValue("heatingSetpoint") + 1
    
    if( nextLevel > 90){
      nextLevel = 90
    }
    log.debug "Setting heat set point up to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}

def heatLevelDown(){
    int nextLevel = device.currentValue("heatingSetpoint") - 1
    
    if( nextLevel < 40){
      nextLevel = 40
    }
    log.debug "Setting heat set point down to: ${nextLevel}"
    setHeatingSetpoint(nextLevel)
}



// parse events into attributes
def parse(String description) {
    
}

// handle commands
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
       if(mode==4)
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
log.debug "https://mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges"

    
    def params = [
        uri: "https://mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges",
        headers: [
              'Accept': 'application/json, text/javascript, */*; q=0.01',
              'DNT': '1',
        	  'Accept-Encoding': 'gzip,deflate,sdch',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': "https://mytotalconnectcomfort.com/portal/Device/Control/${settings.honeywelldevice}",
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
        body: [ DeviceID: "${settings.honeywelldevice}", SystemSwitch : data.SystemSwitch ,HeatSetpoint : data.HeatSetpoint, CoolSetpoint: data.CoolSetpoint, HeatNextPeriod: data.HeatNextPeriod,CoolNextPeriod:data.CoolNextPeriod,StatusHeat:data.StatusHeat,StatusCool:data.StatusCool,FanMode:data.FanMode]

]

    httpPost(params) { response ->
        log.debug "Request was successful, $response.status"
 
    }
    
    log.debug "SetStatus is 1 now"
    data.SetStatus = 1

}

def getStatus() {
  log.debug "Executing 'getStatus'"
def today= new Date()
log.debug "https://mytotalconnectcomfort.com/portal/Device/CheckDataSession/${settings.honeywelldevice}?_=$today.time"



    def params = [
        uri: "https://mytotalconnectcomfort.com/portal/Device/CheckDataSession/${settings.honeywelldevice}",
        headers: [
              'Accept': '*/*',
              'DNT': '1',
              'Accept-Encoding': 'plain',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': 'https://mytotalconnectcomfort.com/portal',
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
    ]

        httpGet(params) { response ->
        log.debug "Request was successful, $response.status"

        
        def curTemp = response.data.latestData.uiData.DispTemperature
        def fanMode = response.data.latestData.fanData.fanMode
        def switchPos = response.data.latestData.uiData.SystemSwitchPosition
        def coolSetPoint = response.data.latestData.uiData.CoolSetpoint
        def heatSetPoint = response.data.latestData.uiData.HeatSetpoint
        def statusCool = response.data.latestData.uiData.StatusCool
        def statusHeat = response.data.latestData.uiData.StatusHeat
        def curHumidity = response.data.latestData.uiData.IndoorHumidity
        

        log.trace("IndoorHumidity: ${response.data.latestData.uiData.IndoorHumidity}")
        log.trace("IndoorHumiditySensorAvailable: ${response.data.latestData.uiData.IndoorHumiditySensorAvailable}")        
        log.trace("IndoorHumiditySensorNotFault: ${response.data.latestData.uiData.IndoorHumiditySensorNotFault}")        
        log.trace("IndoorHumidStatus: ${response.data.latestData.uiData.IndoorHumidStatus}")        

        //Operating State Section 
        //Set the operating state to off 
        
        def operatingState = "off"
        
        //Check the status of heat and cool 
        if(statusCool == 1 && switchPos == 3) {
            operatingState = "cooling"
        } else if (statusHeat == 1 && switchPos == 1) {
            operatingState = "heating"
           
        } else {
          	operatingState = "unknown"
        }
        
        //End Operating State
        
        log.debug curTemp
        log.debug fanMode
        log.debug switchPos
       
        //fan mode 0=auto, 2=circ, 1=on
        
        if(fanMode==0)
          fanMode = 'auto'
        if(fanMode==1)
          fanMode = 'on'

        if(switchPos==1)
          switchPos = 'heat'
        if(switchPos==2)
          switchPos = 'off'
        if(switchPos==3)
          switchPos = 'cool'
        if(switchPos==4)
          switchPos = 'auto'

	//Send events 
        sendEvent(name: 'thermostatOperatingState', value: operatingState)
        sendEvent(name: 'thermostatFanMode', value: fanMode)
        sendEvent(name: 'thermostatMode', value: switchPos)
        sendEvent(name: 'coolingSetpoint', value: coolSetPoint as Integer)
        sendEvent(name: 'heatingSetpoint', value: heatSetPoint as Integer)
        sendEvent(name: 'temperature', value: curTemp as Integer, state: switchPos)
        sendEvent(name: 'relativeHumidity', value: curHumidity as Integer)
        
        
       

 
    }

}

def api(method, args = [], success = {}) {

}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {

}

def refresh() {
  log.debug "Executing 'refresh'"
    login()
    getStatus()
}

def login() {  
  log.debug "Executing 'login'"
      

        
    def params = [
        uri: 'https://mytotalconnectcomfort.com/portal',
        headers: [
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Encoding': 'sdch',
            'Host': 'mytotalconnectcomfort.com',
            'DNT': '1',
            'Origin': 'mytotalconnectcomfort.com/portal/',
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

