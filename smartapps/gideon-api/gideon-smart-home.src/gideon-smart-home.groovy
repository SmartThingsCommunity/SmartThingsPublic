/**
 *  Gideon
 *
 *  Copyright 2016 Nicola Russo
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
definition(
    name: "Gideon Smart Home",
    namespace: "gideon.api",
    author: "Braindrain Solutions ltd",
    description: "Gideon Smart Home SmartApp allows you to connect and control all of your SmartThings devices through the Gideon app, making your SmartThings devices even smarter.",
    category: "Family",
    iconUrl: "http://s33.postimg.org/t77u7y7v3/logo.png",
    iconX2Url: "http://s33.postimg.org/t77u7y7v3/logo.png",
    iconX3Url: "http://s33.postimg.org/t77u7y7v3/logo.png",
    oauth: [displayName: "Gideon Smart Home API app", displayLink: "gideon.ai"])

preferences {
	section("Control these contact sensors...") {
        input "contact", "capability.contactSensor", multiple:true, required:false
    }
    section("Control these switch levels...") {
        input "switchlevels", "capability.switchLevel", multiple:true, required:false
    }
/*    section("Control these thermostats...") {
        input "thermostats", "capability.thermostat", multiple:true, required:false
    }*/
    section("Control the color for these devices...") {
        input "colors", "capability.colorControl", multiple:true, required:false
    }
    section("Control the color temperature for these devices...") {
        input "kelvin", "capability.colorTemperature", multiple:true, required:false
    }
	section("Control these switches...") {
        input "switches", "capability.switch", multiple:true, required:false
    }
    section("Control these smoke alarms...") {
        input "smoke_alarms", "capability.smokeDetector", multiple:true, required:false
    }
    section("Control these window shades...") {
        input "shades", "capability.windowShade", multiple:true, required:false
    }
    section("Control these garage doors...") {
        input "garage", "capability.garageDoorControl", multiple:true, required:false
    }
    section("Control these water sensors...") {
        input "water_sensors", "capability.waterSensor", multiple:true, required:false
    }
    section("Control these motion sensors...") {
        input "motions", "capability.motionSensor", multiple:true, required:false
    }
    section("Control these presence sensors...") {
    	input "presence_sensors", "capability.presenceSensor", multiple:true, required:false
    }
	section("Control these outlets...") {
    	input "outlets", "capability.outlet", multiple:true, required:false
    }
    section("Control these power meters...") {
        input "meters", "capability.powerMeter", multiple:true, required:false
    }
    section("Control these locks...") {
    	input "locks", "capability.lock", multiple:true, required:false
    }
    section("Control these temperature sensors...") {
	    input "temperature_sensors", "capability.temperatureMeasurement", multiple:true, required:false
    }
    section("Control these batteries...") {
	    input "batteries", "capability.battery", multiple:true, required:false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
}

private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}

//API Mapping
mappings {
   path("/getalldevices") {
    action: [
      	GET: "getAllDevices"
   ]
  }
  /*
  path("/thermostat/setcool/:id/:temp") {
    action: [
      GET: "setCoolTemp"
    ]
  }
  path("/thermostat/setheat/:id/:temp") {
    action: [
      GET: "setHeatTemp"
    ]
  }
  path("/thermostat/setfanmode/:id/:mode") {
    action: [
      GET: "setFanMode"
    ]
  }
  path("/thermostat/setmode/:id/:mode") {
    action: [
      GET: "setThermostatMode"
    ]
  }
  path("/thermostat/:id") {
    action: [
      GET: "getThermostatStatus"
    ]
  }
  */
  path("/light/dim/:id/:dim") {
    action: [
      GET: "setLevelStatus"
    ]
  }
  path("/light/kelvin/:id/:kelvin") {
    action: [
      GET: "setKelvin"
    ]
  }
  path("/colorlight/:id/:hue/:sat") {
    action: [
      GET: "setColor"
    ]
  }
  path("/light/status/:id") {
    action: [
      GET: "getLightStatus"
    ]
  }
  path("/light/on/:id") {
    action: [
      GET: "turnOnLight"
    ]
  }
  path("/light/off/:id") {
    action: [
      GET: "turnOffLight"
    ]
  }
  path("/doorlocks/lock/:id") {
    action: [
      GET: "lockDoorLock"
    ]
  }
  path("/doorlocks/unlock/:id") {
    action: [
      GET: "unlockDoorLock"
    ]
  }
  path("/doorlocks/:id") {
    action: [
      GET: "getDoorLockStatus"
    ]
  }
  path("/contacts/:id") {
    action: [
  	GET: "getContactStatus"
    ]
  }
  path("/smoke/:id") {
    action: [
   	GET: "getSmokeStatus"
	]
  }
    path("/shades/open/:id") {
    action: [
      GET: "openShade"
    ]
  }
  path("/shades/preset/:id") {
    action: [
      GET: "presetShade"
    ]
  }
  path("/shades/close/:id") {
    action: [
      GET: "closeShade"
    ]
  }
  	path("/shades/:id") {
    action: [
	GET: "getShadeStatus"
	]
}
    path("/garage/open/:id") {
    action: [
      GET: "openGarage"
    ]
  }
  path("/garage/close/:id") {
    action: [
      GET: "closeGarage"
    ]
  }
  	path("/garage/:id") {
    action: [
  		GET: "getGarageStatus"
  	]
  }
    path("/watersensors/:id") {
    action: [
      		GET: "getWaterSensorStatus"
    	]
  }
  	path("/tempsensors/:id") {
    action: [
      GET: "getTempSensorsStatus"
    ]
  }
  path("/meters/:id") {
    action: [
      GET: "getMeterStatus"
    ]
  }
  path("/batteries/:id") {
    action: [
      GET: "getBatteryStatus"
    ]
  }
  	path("/presences/:id") {
    action: [
      GET: "getPresenceStatus"
    ]
  }
  	path("/motions/:id") {
    action: [
      GET: "getMotionStatus"
    ]
  }
  	path("/outlets/:id") {
    action: [
      GET: "getOutletStatus"
    ]
  }
  	path("/outlets/turnon/:id") {
    action: [
      GET: "turnOnOutlet"
    ]
  }
  path("/outlets/turnoff/:id") {
    action: [
      GET: "turnOffOutlet"
    ]
  }
  path("/switches/turnon/:id") {
    action: [
      GET: "turnOnSwitch"
    ]
  }
  path("/switches/turnoff/:id") {
    action: [
      GET: "turnOffSwitch"
    ]
  }
  path("/switches/:id") {
    action: [
      GET: "getSwitchStatus"
    ]
  }
}

//API Methods
def getAllDevices() {
    def locks_list = locks.collect{device(it,"Lock")}
    /*def thermo_list = thermostats.collect{device(it,"Thermostat")}*/
    def colors_list = colors.collect{device(it,"Color")}
    def kelvin_list = kelvin.collect{device(it,"Kelvin")}
    def contact_list = contact.collect{device(it,"Contact Sensor")}
    def smokes_list = smoke_alarms.collect{device(it,"Smoke Alarm")}
    def shades_list = shades.collect{device(it,"Window Shade")}
    def garage_list = garage.collect{device(it,"Garage Door")}
    def water_sensors_list = water_sensors.collect{device(it,"Water Sensor")}
    def presences_list = presence_sensors.collect{device(it,"Presence")}
    def motions_list = motions.collect{device(it,"Motion")}
	def outlets_list = outlets.collect{device(it,"Outlet")}
    def switches_list = switches.collect{device(it,"Switch")}
    def switchlevels_list = switchlevels.collect{device(it,"Switch Level")}
    def temp_list = temperature_sensors.collect{device(it,"Temperature")}
    def meters_list = meters.collect{device(it,"Power Meters")}
    def battery_list = batteries.collect{device(it,"Batteries")}
    return outlets_list + kelvin_list + colors_list + switchlevels_list + smokes_list + contact_list + water_sensors_list + shades_list + garage_list + locks_list + presences_list + motions_list + switches_list + temp_list + meters_list + battery_list
}

//thermostat
/*
def setCoolTemp() {
	def device = thermostats.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            if(device.hasCommand("setCoolingSetpoint")) {
            	device.setCoolingSetpoint(params.temp.toInteger());
                return [result_action: "200"]
            }
            else {
            	httpError(510, "Not supported!")
            }
       }
}
def setHeatTemp() {
	def device = thermostats.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            if(device.hasCommand("setHeatingSetpoint")) {
            	device.setHeatingSetpoint(params.temp.toInteger());
                return [result_action: "200"]
            }
            else {
            	httpError(510, "Not supported!")
            }
       }
}
def setFanMode() {
	def device = thermostats.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
        if(device.hasCommand("setThermostatFanMode")) {
            	device.setThermostatFanMode(params.mode);
                return [result_action: "200"]
            }
            else {
            	httpError(510, "Not supported!")
            }
       }
}
def setThermostatMode() {
	def device = thermostats.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
        if(device.hasCommand("setThermostatMode")) {
            	device.setThermostatMode(params.mode);
                return [result_action: "200"]
            }
            else {
            	httpError(510, "Not supported!")
            }
       }
}
def getThermostatStatus() {
	def device = thermostats.find{ it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [ThermostatOperatingState: device.currentValue('thermostatOperatingState'), ThermostatSetpoint: device.currentValue('thermostatSetpoint'), 
            			ThermostatFanMode: device.currentValue('thermostatFanMode'), ThermostatMode: device.currentValue('thermostatMode')]
       	}
}
*/
//light
def turnOnLight() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            device.on();
                  
            return [Device_id: params.id, result_action: "200"]
        }
    }

def turnOffLight() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            device.off();
                  
            return [Device_id: params.id, result_action: "200"]
        }
}

def getLightStatus() {
	def device = switches.find{ it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Status: device.currentValue('switch'), Dim: getLevelStatus(params.id), Color: getColorStatus(params.id), Kelvin: getKelvinStatus(params.id)]
        }
}

//color control
def setColor() {
    def device = colors.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
        	
            def map = [hue:params.hue.toInteger(), saturation:params.sat.toInteger()]
            
            device.setColor(map);
                  
            return [Device_id: params.id, result_action: "200"]
    }
}

def getColorStatus(id) {
	def device = colors.find { it.id == id }
    if (!device) {
            return [Color: "none"]
        } else {
        	return [hue: device.currentValue('hue'), saturation: device.currentValue('saturation')]
        }
}

//kelvin control
def setKelvin() {
    def device = kelvin.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
        
            device.setColorTemperature(params.kelvin.toInteger());
                  
            return [Device_id: params.id, result_action: "200"]
    }
}

def getKelvinStatus(id) {
	def device = kelvin.find { it.id == id }
    if (!device) {
            return [kelvin: "none"]
        } else {
        	return [kelvin: device.currentValue('colorTemperature')]
        }
}

//switch level
def getLevelStatus() {
	def device = switchlevels.find { it.id == params.id }
    if (!device) {
            [Level: "No dimmer"]
        } else {
        	return [Level: device.currentValue('level')]
        }
}

def getLevelStatus(id) {
	def device = switchlevels.find { it.id == id }
    if (!device) {
            [Level: "No dimmer"]
        } else {
        	return [Level: device.currentValue('level')]
        }
}


def setLevelStatus() {
	def device = switchlevels.find { it.id == params.id }
    def level = params.dim
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	device.setLevel(level.toInteger())
        	return [result_action: "200", Level: device.currentValue('level')]
        }
}


//contact sensors
def getContactStatus() {
	def device = contact.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def args = getTempSensorsStatus(device.id)
        	return [Device_state: device.currentValue('contact')] + args
        }
}

//smoke detectors
def getSmokeStatus() {
	def device = smoke_alarms.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        def bat = getBatteryStatus(device.id)
        	return [Device_state: device.currentValue('smoke')] + bat
        }
}

//garage
def getGarageStatus() {
	def device = garage.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_state: device.currentValue('door')]
        }
}

def openGarage() {
    def device = garage.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.open();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

def closeGarage() {
	def device = garage.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.close();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }
//shades
def getShadeStatus() {
	def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_state: device.currentValue('windowShade')]
        }
}

def openShade() {
    def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.open();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }
    
def presetShade() {
    def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.presetPosition();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

def closeShade() {
	def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.close();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

//water sensor
def getWaterSensorStatus() {
	def device = water_sensors.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def bat = getBatteryStatus(device.id)
        	return [Device_state: device.currentValue('water')] + bat
        }
}
//batteries
def getBatteryStatus() {
	def device = batteries.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_state: device.latestValue("battery")]
        }
}

def getBatteryStatus(id) {
	def device = batteries.find { it.id == id }
    if (!device) {
            return []
        } else {
        	return [battery_state: device.latestValue("battery")]
        }
}

//LOCKS
def getDoorLockStatus() {
	def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def bat = getBatteryStatus(device.id)
        	return [Device_state: device.currentValue('lock')] + bat
        }
}

def lockDoorLock() {
    def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.lock();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

def unlockDoorLock() {
	def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.unlock();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }
//PRESENCE
def getPresenceStatus() {

	def device = presence_sensors.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        def bat = getBatteryStatus(device.id)
        	return [Device_state: device.currentValue('presence')] + bat
   }
}

//MOTION
def getMotionStatus() {

	def device = motions.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def args = getTempSensorsStatus(device.id)
        	return [Device_state: device.currentValue('motion')] + args
   }
}

//OUTLET
def getOutletStatus() {

    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
     def watt = getMeterStatus(device.id)
       	
     return [Device_state: device.currentValue('switch')] + watt
}

def getMeterStatus() {

    def device = meters.find { it.id == params.id }
   	if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_id: device.id, Device_type: device.type, Current_watt: device.currentValue("power")]
  }
}

def getMeterStatus(id) {

    def device = meters.find { it.id == id }
   	if (!device) {
            return []
        } else {
        	return [Current_watt: device.currentValue("power")]
  }
}


def turnOnOutlet() {
    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
     
     device.on();
                  
     return [Device_id: params.id, result_action: "200"]
}

def turnOffOutlet() {
    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
            
     device.off();
                  
     return [Device_id: params.id, result_action: "200"]
}

//SWITCH
def getSwitchStatus() {
	def device = switches.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_state: device.currentValue('switch'), Dim: getLevelStatus(params.id)]
	}
}

def turnOnSwitch() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.on();
                  
            return [Device_id: params.id, result_action: "200"]
        }
}

def turnOffSwitch() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.off();
            return [Device_id: params.id, result_action: "200"]
        }
}


//TEMPERATURE
def getTempSensorsStatus() {
    def device = temperature_sensors.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def bat = getBatteryStatus(device.id)
            def scale = [Scale: location.temperatureScale]
        	return [Device_state: device.currentValue('temperature')] + scale + bat
   }
}

def getTempSensorsStatus(id) {	
    def device = temperature_sensors.find { it.id == id }
    if (!device) {
            return []
        } else {
        	def bat = getBatteryStatus(device.id)
            def scale = [Scale: location.temperatureScale]
        	return [temperature: device.currentValue('temperature')] + bat + scale
   		}
   }
