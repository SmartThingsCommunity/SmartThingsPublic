metadata {
	preferences {
		input("locationname", "text", title: "Name of your neviweb® location", description: "Location name", required: true, displayDuringSetup: true)
		input("devicename", "text", title: "Name of your neviweb® thermostat", description: "Thermostat name", required: true, displayDuringSetup: true)
	}

	definition (name: "Sinopé Technologies Inc. Thermostat", namespace: "Sinopé Technologies Inc.", author: "Mathieu Virole") {
		capability "ThermostatHeatingSetpoint"
		capability "thermostatOperatingState"
		capability "TemperatureMeasurement"
		capability "Refresh"

		attribute "outdoorTemp", "string"
		attribute "outputPercentDisplay", "number"

		command "heatingSetpointDown"
		command "heatingSetpointUp"

		command "StartCommunicationWithServer"

	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type: "thermostat", width: 6, height: 4){
			tileAttribute ("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperatureMeasurement", label:'${currentValue}°', unit: "dF", backgroundColor:"#269bd2")
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatingSetpointUp")
				attributeState("VALUE_DOWN", action: "heatingSetpointDown")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE",inactiveLabel: true) {
				attributeState("idle",backgroundColor:"#44b621")
				attributeState("heating",backgroundColor:"#ffa81e")
			}
			
			tileAttribute("outputPercentDisplay", key: "SECONDARY_CONTROL") {
				attributeState("outputPercentDisplay", label:'${currentValue}%', unit:"%", icon:"st.Weather.weather2", defaultState: true)
			}
			
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label: '${currentValue}', unit: "dF")
			}
		}

		// //Heating Set Point Controls
		// controlTile("levelSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 6, inactiveLabel: false, range:"(5..30)", decoration: "flat") {
        // state "heatingSetpoint", action:"heatingSetpoint"
    	// }
		
		standardTile("heatLevelDown", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "heatLevelDown", action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down"
        }
        standardTile("heatLevelUp", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "heatLevelUp", action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up"
        }
		controlTile("heatingSetpointSlider", "device.heatingSetpoint","slider", height: 2, width: 2, range:"5..86") {
        state "heatingSetpoint", label:'${currentValue}', action:"setHeatingSetpoint"
    	}
       	valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false) {
			state "heatingSetpoint", label:'${currentValue}', backgroundColor:"#153591"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("error", "device.error", width: 4, height: 2, inactiveLabel: false, decoration: "flat") {
		    state "default", label:'${currentValue}', backgroundColor:"#ffffff", icon:"st.Office.office8"
		}

		main (["temperature"])
        details(["temperature", /*"heatLevelDown", "heatingSetpoint", "heatLevelUp", */"refresh", "error"])
	}
}


def setHeatingSetpoint(newSetpoint) {
	log.trace("Connexion verifiction - ${device.name}")
	try{
		state.heatingSetpoint = newSetpoint;
		sendEvent(name: 'heatingSetpoint', value: FormatTemp(state.heatingSetpoint,null), unit: location?.getTemperatureScale())
		def timeInSeconds = (Math.round(now()/1000))
		sendEvent(name: "thermostat", value: device.id+": "+timeInSeconds, state: "${newSetpoint}", data: [deviceId: device.id, action: "setHeatingSetpoint", value: "${newSetpoint}", evtTime: timeInSeconds])
	}
	catch(NullPointerException e1){
		refresh();
	}
}

def heatingSetpointUp(){
	log.trace("Connexion verifiction - ${device.name}")
	try{
		state.heatingSetpoint = state.heatingSetpoint.toDouble() + 0.5
		sendEvent(name: 'heatingSetpoint', value: FormatTemp(""+state.heatingSetpoint,null), unit: location?.getTemperatureScale())
		def timeInSeconds = (Math.round(now()/1000))
		log.info("Setting ${device.name} to ${state.heatingSetpoint.toDouble() + 0.5}")
		sendEvent(name: "thermostat", value: device.id+": "+timeInSeconds, state: "heatingSetpointUp", data: [deviceId: device.id, action: "heatingSetpointUp", evtTime: timeInSeconds])
	}
	catch(NullPointerException e1){
		refresh();
	}
}

def heatingSetpointDown(){
	log.trace("Connexion verifiction - ${device.name}")
	try{
		state.heatingSetpoint = state.heatingSetpoint.toDouble() - 0.5
		sendEvent(name: 'heatingSetpoint', value: FormatTemp(""+state.heatingSetpoint,null), unit: location?.getTemperatureScale())
		def timeInSeconds = (Math.round(now()/1000))
		log.info("Setting ${device.name} to ${state.heatingSetpoint.toDouble() - 0.5}")
		sendEvent(name: "thermostat", value: device.id+": "+timeInSeconds, state: "heatingSetpointDown", data: [deviceId: device.id, action: "heatingSetpointDown", evtTime: timeInSeconds])
	}
	catch(NullPointerException e1){
		refresh();
	}
}


def refresh(){
	log.trace("Connexion verifiction - ${device.name}")
	def timeInSeconds = (Math.round(now()/1000))
	sendEvent(name: "thermostat", value:  device.id+": "+timeInSeconds, state: "refresh", data: [deviceId: device.id, action: "refresh", evtTime: timeInSeconds])
	
}

def StartCommunicationWithServer(data){
	log.trace("Connexion comfirmed - \"${device.name}\"")
	log.info("Action \"${data?.action}\" - \"${device.name}\"")
	if( !state.deviceId || state.deviceId == true || state.deviceName != settings.devicename.toLowerCase().replaceAll("\\s", "") || state.locationName != settings.locationname.toLowerCase().replaceAll("\\s", "") ){
		state.deviceId = deviceId(data?.session)
	}
	def params = [
		path: "device/${state.deviceId}/attribute",
		headers: ['Session-Id' : data.session]
	]
	if(!state.deviceId){
		log.warn ("No device id found")
		sendEvent(name: 'temperature', value: null, unit: location?.getTemperatureScale())
		sendEvent(name: 'temperatureMeasurement', value: null, unit: location?.getTemperatureScale())
		sendEvent(name: 'heatingSetpoint', value: null, unit: location?.getTemperatureScale())
		sendEvent(name: 'outputPercentDisplay', value: null)
		sendEvent(name: 'thermostatOperatingState', value: null)
		state.heatingSetpoint = 0
    	return sendEvent(name: 'error', value: "${error(1004)}")
	}else{
		switch(data.action){
			case "heatingSetpointUp":
				params.body = ['roomSetpoint' : state.heatingSetpoint.toDouble()]
				params.body.floorSetpoint = params.body.roomSetpoint
				params.contentType = 'application/json'
				requestApi("setDevice", params);
				data.action = "refresh"
				StartCommunicationWithServer(data)
				break;
			case "heatingSetpointDown":
				params.body = ['roomSetpoint' : state.heatingSetpoint.toDouble()]
				params.body.floorSetpoint = params.body.roomSetpoint
				params.contentType = 'application/json'
				requestApi("setDevice", params);
				data.action = "refresh"
				StartCommunicationWithServer(data)
				break;
			case "setHeatingSetpoint":
				params.body = ['roomSetpoint' : FormatTemp(data.value,true)]
				params.body.floorSetpoint = params.body.roomSetpoint
				params.contentType = 'application/json'
				requestApi("setDevice", params);
				data.action = "refresh"
				StartCommunicationWithServer(data)
				break;
			case "refresh":
				params.query = ['attributes' : "airFloorMode,floorTemperature,floorSetpoint,roomTemperature,roomSetpoint,outputPercentDisplay"]
				requestApi("deviceData", params);
				break;
			default: 
				log.warn "invalide action"
		}
	}
}

def deviceId(session){
	data.deviceId = null
	locationId(session)
	if(data.locationId){
		def params = [
			uri: "${data.server}",
			path: "devices",
			requestContentType: "application/json, text/javascript, */*; q=0.01",
			headers: ['Session-Id' : session]
		]
		if(data?.locationId){
			params.query = ['location$id' : data.locationId]
		}

		requestApi("deviceList", params);

		def deviceName=settings.devicename
		if (deviceName!=null){
			deviceName=deviceName.toLowerCase().replaceAll("\\s", "")
		}
		data?.deviceId = null
		data.devices_list.each{var ->
			try{
				def name_device=var.name
				name_device=name_device.toLowerCase().replaceAll("\\s", "")
				if(name_device==deviceName){
					data.deviceId=var.id
						data.error=false
						state.deviceName = deviceName;
						state.deviceLocation = settings?.locationname.toLowerCase().replaceAll("\\s", "");
						return data.deviceId;
				}else{
					data.code=4001
				}
			}catch(e){
				data.code=4003
			}
		}
		if (!data?.deviceId || data.error){
			data.error=error(data.code)
			sendEvent(name: 'error', value: "${data.error}")
			data.deviceId=null;
			log.warn("${data.error}")
			data.error=true
		}
		else{
			data.deviceId
		}
		return data.deviceId
	}else{
		log.warn("${error(3001)}")
		return null;
	}
}



def locationId(session){
	def params = [
        path: "locations",
       	requestContentType: "application/json, text/javascript, */*; q=0.01",
        headers: ['Session-Id' : session]
    ]
    requestApi("locationList",params)
    def locationName=settings?.locationname
	if(locationName){
		locationName = locationName.toLowerCase().replaceAll("\\s", "")
	}
	data.locationId = null
	data.location_list.each{var ->    	
    	def name_location
		try{
			name_location = var.name
		}catch(e){
			log.error(var)
		}	
		if(name_location){
    		name_location = name_location.toLowerCase().replaceAll("\\s", "")
		}else{
			name_location = "INVALID LOCATION"
		}

    	if(name_location==locationName){
    		data.locationId = var.id
    		// log.info("Location ID is :: ${data.locationId}")
			state.locationName = locationName
    		data.error=null
    	}
    }
	
    if (!data.locationId){
    	sendEvent(name: 'error', value: "${error(3001)}")
    	data.error=true
    }
}

def isExpiredSessionEvent(resp){
	if( resp?.data?.error && resp?.data?.error?.code && resp?.data?.error?.code=="USRSESSEXP" ){
		sendEvent(name: "switch", value:  device.id+": "+timeInSeconds, state: "resetSession", data: [action: "resetSession", evtTime: timeInSeconds]);
	}
}

def isDeviceIdValid(session){
	def oldDeviceId = state.deviceId;
	if(state.deviceId){
		state.deviceId = deviceId(session)
	}
	if(!state.deviceId){
		log.warn ("No device id found")
		sendEvent(name: 'error', value: "${error(1004)}")
	}else if( oldDeviceId == state.deviceId ){
		data.error=error(2001)
		sendEvent(name: 'error', value: "${data.error}")
		log.error("${data.error}")
	}else{
		data.error=error(2001)
		sendEvent(name: 'error', value: "${data.error}")
		log.error("${data.error}")
	}
	return state.deviceId;
} 

def requestApi(actionApi, params){
	params.uri = "https://smartthings.neviweb.com/"
	log.trace("api call : ${actionApi} - ${device.name}");
	switch(actionApi){
		case "deviceList":
			httpGet(params) {resp ->
				isExpiredSessionEvent(resp)
				data.devices_list = resp.data
				
			}
		break;
		case "locationList":
			httpGet(params) {resp ->
				isExpiredSessionEvent(resp)
				data.location_list = resp.data
				
			}
		break;
		case "deviceData":
			def temperature;
			def heatingSetpoint;
			try{
				httpGet(params) {resp ->
					isExpiredSessionEvent(resp)
					data.status = resp.data
					if (!resp.data.error){
						sendEvent(name: 'error', value: " ")
						sendEvent(name: 'status', value: "OK")
						def temperatureInput,setpointInput
						if(data.status.airFloorMode == "floor"){
							temperatureInput = data.status.floorTemperature
							setpointInput = data.status.floorSetpoint
						}
						else{
							temperatureInput = data.status.roomTemperature
							setpointInput = data.status.roomSetpoint
						}
						temperature = FormatTemp(temperatureInput?.value,null)
						if(!temperature){
							temperature = FormatTemp(temperatureInput,null)
						}
						heatingSetpoint = FormatTemp(setpointInput,null)
						if(temperature){
							sendEvent(name: 'temperature', value: temperature, unit: location?.getTemperatureScale())
						}
						if(temperature){
							sendEvent(name: 'temperatureMeasurement', value: temperature, unit: location?.getTemperatureScale())
						}
						if(heatingSetpoint){
							state.heatingSetpoint = setpointInput
							sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: location?.getTemperatureScale())
						}
						sendEvent(name: 'outputPercentDisplay', value: data.status.outputPercentDisplay)
						sendEvent(name: 'thermostatOperatingState', value: ((data.status.outputPercentDisplay > 10)?"heating":"idle"))
						// sendEvent(name: "thermostatMode", value: ((data.status.outputPercentDisplay > 0)?"Heat":"Idle"))
					}else{
						sendEvent(name: 'temperature', value: null, unit: location?.getTemperatureScale())
						sendEvent(name: 'temperatureMeasurement', value: null, unit: location?.getTemperatureScale())
						sendEvent(name: 'heatingSetpoint', value: null, unit: location?.getTemperatureScale())
						sendEvent(name: 'outputPercentDisplay', value: null)
						sendEvent(name: 'thermostatOperatingState', value: null)
						return isDeviceIdValid(params?.headers["Session-Id"]);
					}
					return resp.data
				}
			} catch (SocketTimeoutException e) {
				return isDeviceIdValid(params?.headers["Session-Id"]);
			} catch (e) {
				return isDeviceIdValid(params?.headers["Session-Id"]);
			}
		break;
		case "setDevice":
			try{
				httpPut(params){resp -> 
					isExpiredSessionEvent(resp)
					if (!resp.data.error){
						// if(resp?.data?.roomSetpoint){
						// 	state.heatingSetpoint = resp.data.roomSetpoint
						// 	sendEvent(name: 'heatingSetpoint', value: FormatTemp(resp.data.roomSetpoint,null), unit: location?.getTemperatureScale())
						// }else if(resp?.data?.floorSetpoint){
						// 	state.heatingSetpoint = resp.data.floorSetpoint
						// 	sendEvent(name: 'heatingSetpoint', value: FormatTemp(resp.data.floorSetpoint,null), unit: location?.getTemperatureScale())
						// }
						// else{
						// 	sendEvent(name: 'heatingSetpoint', value: FormatTemp(state.heatingSetpoint,null), unit: location?.getTemperatureScale())
						// }
						// params.remove("body");
						// params.query = ['attributes' : "outputPercentDisplay"]
						// requestApi("deviceData", params);
					}else{
						isDeviceIdValid(params?.headers["Session-Id"]);
					}
				}
			} catch (SocketTimeoutException e) {
				return isDeviceIdValid(params?.headers["Session-Id"]);
			} catch (e) {
				return isDeviceIdValid(params?.headers["Session-Id"]);
			}
		break;
	}
	return ;

}

def error(error){
	switch (error) {
		case 0: return ""
		case 1: return "Location name or Device name is wrong."
		case 100: return "Your session expired."
        case 1005: return "This action cannot be executed while in demonstration mode."
        case 1004: return "The resource you are trying to access could not be found."
        case 1003: return "You are not authorized to see this resource."
        case 1002: return "Wrong e-mail address or password. Please try again."
        case 1101: return "The e-mail you have entered is already used.  Please select another e-mail address."
        case 1102: return "The password you have provided is incorrect."
        case 1103: return "The password you have provided is not secure."
        case 1104: return "The account you are trying to log into is not activated. Please activate your account by clicking on the activation link located in the activation email you have received after registring. You can resend the activation email by pressing the following button."
        case 1105: return "Your account is disabled."
        case 1110: return "The maximum login retry has been reached. Your account has been locked. Please try again later."
        case 1111: return "Your account is presently locked. Please try again later."
        case 1120: return "The maximum simultaneous connections on the same IP address has been reached. Please try again later."
        case 2001: return "The device you are trying to access is temporarily unaccessible. Please try later."
        case 2002: return "The network you are trying to access is temporarily unavailable. Please try later."
        case 2003: return "The web interface (GT125) that you are trying to add is already present in your account."
        case 3001: return "Wrong location name. Please try again."
        case 4001: return "Wrong device name. Please try again."
        case 4002: return "This device is not Ligthswitch. Please change DeviceName."
        default: return "An error has occurred, please try again later."

    }
}
def FormatTemp(temp,invert){
	if (temp!=null){
		if(invert){
			float i=Float.valueOf(temp)
			switch (location?.getTemperatureScale()) {
				case "C":
					return i.round(2)
				break;

				case "F":
					return (Math.round(fToC(i))).toDouble().round(2)
				break;
			}

		}else{

			float i=Float.valueOf(temp)
			switch (location?.getTemperatureScale()) {
				case "C":
					return i.round(2)
				break;

				case "F":
					return (Math.round(cToF(i))).toDouble().round(0)
				break;
			}
		}
    }else{
    	return null
    }
}

def cToF(temp) {
	return ((( 9 * temp ) / 5 ) + 32)
}

def fToC(temp) {
	return ((( temp - 32 ) * 5 ) / 9)
}