/**
 *	Fidure Thermostat, Based on ZigBee thermostat (SmartThings)
 *
 *	Author: Fidure
 *	Date: 2014-12-13
 *  Updated: 2015-08-26
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Fidure Thermostat", namespace: "smartthings", author: "SmartThings") {

        capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
        capability "Polling"

		attribute "displayTemperature","number"
		attribute "displaySetpoint", "string"
		command 	"raiseSetpoint"
		command 	"lowerSetpoint"
		attribute "upButtonState", "string"
		attribute "downButtonState", "string"

		attribute "runningMode", "string"
    	attribute "lockLevel", "string"

		command "setThermostatTime"
  		command "lock"

 		attribute "prorgammingOperation", "number"
  		attribute "prorgammingOperationDisplay", "string"
  		command   "Program"

  		attribute "setpointHold", "string"
		attribute "setpointHoldDisplay", "string"
		command "Hold"
  		attribute "holdExpiary", "string"

		attribute "lastTimeSync", "string"

		attribute "thermostatOperatingState", "string"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0201,0204,0B05", outClusters: "000A, 0019"

	}

	// simulator metadata
	simulator { }
  // pref
     preferences {

		input ("hold_time", "enum", title: "Default Hold Time in Hours",
        description: "Default Hold Duration in hours",
        range: "1..24", options: ["No Hold", "2 Hours", "4 Hours", "8 Hours", "12 Hours", "1 Day"],
        displayDuringSetup: false)
        input ("sync_clock", "boolean", title: "Synchronize Thermostat Clock Automatically?", options: ["Yes","No"])
        input ("lock_level", "enum", title: "Thermostat Screen Lock Level", options: ["Full","Mode Only", "Setpoint"])
 	}

	tiles {
		valueTile("temperature", "displayTemperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors:[
        [value: 0, color: "#153591"],
				[value: 7, color: "#1e9cbb"],
				[value: 15, color: "#90d2a7"],
				[value: 23, color: "#44b621"],
				[value: 29, color: "#f1d801"],
				[value: 35, color: "#d04e00"],
				[value: 36, color: "#bc2323"],
									// fahrenheit range
				[value: 37, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
        ]
			)
		}
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off",   action:"thermostat.setThermostatMode", icon:"st.thermostat.heating-cooling-off"
			state "cool",  action:"thermostat.setThermostatMode", icon:"st.thermostat.cool"
			state "heat",  action:"thermostat.setThermostatMode", icon:"st.thermostat.heat"
			state "auto",  action:"thermostat.setThermostatMode", icon:"st.thermostat.auto"
		}

		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'${name}', action:"thermostat.setThermostatFanMode"
			state "fanOn", label:'${name}', action:"thermostat.setThermostatFanMode"
		}

    standardTile("hvacStatus", "thermostatOperatingState", inactiveLabel: false, decoration: "flat") {
				state "Resting",  label: 'Resting'
				state "Heating",  icon:"st.thermostat.heating"
				state "Cooling",  icon:"st.thermostat.cooling"
		}


    standardTile("lock", "lockLevel", inactiveLabel: false, decoration: "flat") {
        state "Unlocked",   action:"lock", label:'${name}'
        state "Mode Only",  action:"lock", label:'${name}'
        state "Setpoint",   action:"lock", label:'${name}'
        state "Full",  action:"lock", label:'${name}'
    }

		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false, range: "$min..$max") {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit:"F", backgroundColor:"#ffffff"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false, range: "$min..$max") {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

        valueTile("scheduleText", "prorgammingOperation", inactiveLabel: false, decoration: "flat", width: 2) {
        	state "default", label: 'Schedule'
    	}
    	valueTile("schedule", "prorgammingOperationDisplay", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"Program", label: '${currentValue}'
    	}

    	valueTile("hold", "setpointHoldDisplay", inactiveLabel: false, decoration: "flat", width: 3) {
            state "setpointHold", action:"Hold", label: '${currentValue}'
		}

		valueTile("setpoint", "displaySetpoint", width: 2, height: 2)
		{
			state("displaySetpoint", label: '${currentValue}°',
				backgroundColor: "#919191")
		}

		standardTile("upButton", "upButtonState", decoration: "flat", inactiveLabel: false) {
			state "normal", action:"raiseSetpoint", backgroundColor:"#919191", icon:"st.thermostat.thermostat-up"
			state "pressed", action:"raiseSetpoint", backgroundColor:"#ff0000", icon:"st.thermostat.thermostat-up"
		}
		standardTile("downButton", "downButtonState", decoration: "flat", inactiveLabel: false) {
			state "normal", action:"lowerSetpoint", backgroundColor:"#919191", icon:"st.thermostat.thermostat-down"
			state "pressed", action:"lowerSetpoint", backgroundColor:"#ff9191", icon:"st.thermostat.thermostat-down"
		}


		main "temperature"
		details([ "temperature", "mode", "hvacStatus","setpoint","upButton","downButton","scheduleText", "schedule", "hold",
        "heatSliderControl", "heatingSetpoint","coolSliderControl", "coolingSetpoint", "lock", "refresh", "configure"])
	}
}

def getMin() {
	try
	{
	if (getTemperatureScale() == "C")
	 	return 10
	else
		return 50
	} catch (all)
	{
		return 10
	}
}

def getMax() {
	try {
		if (getTemperatureScale() == "C")
		return 30
	else
		return 86
	} catch (all)
	{
		return 86
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parse description $description"
	def result = []

	if (description?.startsWith("read attr -")) {

		//TODO: Parse RAW strings for multiple attributes
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		for ( atMap in descMap.attrs)
		{
			def map = [:]

		  if (descMap.cluster == "0201")
			{
				//log.trace "attribute: ${atMap.attrId} "
                switch(atMap.attrId.toLowerCase())
				{
	            case "0000":
	  						map.name = "temperature"
	  						map.value = getTemperature(atMap.value)
								result += createEvent("name":"displayTemperature", "value": getDisplayTemperature(atMap.value))
	  					break;
	            case "0005":
	            //log.debug "hex time: ${descMap.value}"
	            	if (atMap.encoding ==  "23")
	                {
	            	    	map.name = "holdExpiary"
	                  	map.value = "${convertToTime(atMap.value).getTime()}"
	                    //log.trace "HOLD EXPIRY: ${atMap.value} is ${map.value}"
	                    updateHoldLabel("HoldExp", "${map.value}")
	  				}
	            break;
	  				  case "0011":
	  						map.name = "coolingSetpoint"
	  						map.value = getDisplayTemperature(atMap.value)
								updateSetpoint(map.name,map.value)
	  					break;
	  					case "0012":
	  						map.name = "heatingSetpoint"
	  						map.value = getDisplayTemperature(atMap.value)
								updateSetpoint(map.name,map.value)
	  					break;
	  					case "001c":
                        	map.name = "thermostatMode"
	  						map.value = getModeMap()[atMap.value]
							updateSetpoint(map.name,map.value)
	  					break;
							case "001e":   //running mode enum8
			          			map.name = "runningMode"
								map.value = getModeMap()[atMap.value]
								updateSetpoint(map.name,map.value)
							break;
	            case "0023":   // setpoint hold enum8
	            map.name = "setpointHold"
	            map.value = getHoldMap()[atMap.value]
	            updateHoldLabel("Hold", map.value)
	            break;
	            case "0024":   // hold duration int16u
	            map.name = "setpointHoldDuration"
	            map.value = Integer.parseInt("${atMap.value}", 16)

	            break;
	            case "0025":   // thermostat programming operation bitmap8
	  					map.name = "prorgammingOperation"
	                      def val = getProgrammingMap()[Integer.parseInt("${atMap.value}", 16) & 0x01]
	  					result += createEvent("name":"prorgammingOperationDisplay", "value": val)
	            map.value = atMap.value
	  					break;
	            case "0029":
								// relay state
	              map.name = "thermostatOperatingState"
	              map.value = getThermostatOperatingState(atMap.value)
	            break;
	      }
	    } else if (descMap.cluster == "0204")
	    {
	      if (atMap.attrId == "0001")
	      {
	  			map.name = "lockLevel"
                map.value = getLockMap()[atMap.value]
	      }
	    }

		 if (map) {
            result += createEvent(map)
 	 	 }
	  }
  }

	log.debug "Parse returned $result"
	return result
}

def parseDescriptionAsMap(description) {
  def map = (description - "read attr - ").split(",").inject([:]) { map, param ->
  def nameAndValue = param.split(":")
  map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
  }

    def attrId = map.get('attrId')
    def encoding = map.get('encoding')
    def value = map.get('value')
    def result = map.get('result')
    def list = [];

  if (getDataLengthByType(map.get('encoding')) < map.get('value').length()) {
    def raw = map.get('raw')

    def size = Long.parseLong(''+ map.get('size'), 16)
    def index = 12;
    def len
	
    //log.trace "processing multi attributes"
    while((index-12) < size) {
       attrId = flipHexStringEndianness(raw[index..(index+3)])
       index+= 4;
       if (result == "success")
       index+=2;
       encoding = raw[index..(index+1)]
       index+= 2;
       len =getDataLengthByType(encoding)
       value = flipHexStringEndianness(raw[index..(index+len-1)])
       index+=len;
       list += ['attrId': "$attrId", 'encoding':"$encoding", 'value': "$value"]
    }
  }
  else 
    list += ['attrId': "$attrId", 'encoding': "$encoding", 'value': "$value"]
  
  map.remove('value')
  map.remove('encoding')
  map.remove('attrId')
  map += ['attrs' : list ]
}

def flipHexStringEndianness(s)
{
  s = s.reverse()
  def sb = new StringBuilder()
  for (int i=0; i < s.length() -1; i+=2)
   sb.append(s.charAt(i+1)).append(s.charAt(i))
  sb
}

def getDataLengthByType(t)
{
	// number of bytes in each static data type
	 def map = ["08":1,	"09":2,	"0a":3,	"0b":4,	"0c":5,	"0d":6,	"0e":7,	"0f":8,	"10":1,	"18":1,	"19":2,	"1a":3,	"1b":4,
	 "1c":5,"1d":6,	"1e":7,	"1f":8,	"20":1,	"21":2,	"22":3,	"23":4,	"24":5,	"25":6,	"26":7,	"27":8,	"28":1,	"29":2,
	 "2a":3,	"2b":4,	"2c":5,	"2d":6,	"2e":7,	"2f":8,	"30":1,	"31":2,	"38":2,	"39":4,	"40":8,	"e0":4,	"e1":4,	"e2":4,
	 "e8":2,	"e9":2,	"ea":4,	"f0":8,	"f1":16]

	// return number of hex chars
	 return map.get(t) * 2
}


def getProgrammingMap() { [
	0:"Off",
	1:"On"
]}

def getModeMap() { [
	"00":"off",
	"01":"auto",
	"03":"cool",
	"04":"heat"
]}

def getFanModeMap() { [
	"04":"fanOn",
	"05":"fanAuto"
]}

def getHoldMap()
{[
	"00":"Off",
	"01":"On"
]}


def updateSetpoint(attrib, val)
{
	def cool = device.currentState("coolingSetpoint")?.value
	def heat = device.currentState("heatingSetpoint")?.value
	def runningMode = device.currentState("runningMode")?.value
	def mode = device.currentState("thermostatMode")?.value

	def value = '--';


	if ("heat"  == mode && heat != null)
		value = heat;
	else if ("cool"  == mode && cool != null)
		value = cool;
    else if ("auto" == mode && runningMode == "cool" && cool != null)
    	value = cool;
    else if ("auto" == mode && runningMode == "heat" && heat != null)
    	value = heat;

	sendEvent("name":"displaySetpoint", "value": value)
}

def raiseSetpoint()
{
	sendEvent("name":"upButtonState", "value": "pressed")
	sendEvent("name":"upButtonState", "value": "normal")
	adjustSetpoint(5)
}

def lowerSetpoint()
{
	sendEvent("name":"downButtonState", "value": "pressed")
	sendEvent("name":"downButtonState", "value": "normal")
	adjustSetpoint(-5)
}

def adjustSetpoint(value)
{
	def runningMode = device.currentState("runningMode")?.value
	def mode = device.currentState("thermostatMode")?.value

		//default to both heat and cool
    def modeData = 0x02

    if ("heat" == mode || "heat" == runningMode)
    		modeData = "00"
    else if ("cool" == mode || "cool" == runningMode)
    	modeData = "01"

    def amountData = String.format("%02X", value)[-2..-1]


	"st cmd 0x${device.deviceNetworkId} 1 0x201 0 {" + modeData + " " + amountData + "}"

}


def getDisplayTemperature(value)
{
	def t = Integer.parseInt("$value", 16);


	if (getTemperatureScale() == "C") {
		t = (((t + 4) / 10) as Integer) / 10;
	} else {
		t = ((10 *celsiusToFahrenheit(t/100)) as Integer)/ 10;
	}


	return t;
}

def updateHoldLabel(attr, value)
{
	def currentHold = (device?.currentState("setpointHold")?.value)?: "..."

    def holdExp = device?.currentState("holdExpiary")?.value
		holdExp = holdExp?: "${(new Date()).getTime()}"

	if ("Hold" == attr)
    {
    	currentHold = value
    }

	    if ("HoldExp" == attr)
		{
			holdExp = value
		}
		boolean past = ( (new Date(holdExp.toLong()).getTime())  < (new Date().getTime()))

		if ("HoldExp" == attr)
		{
  			if (!past)
				currentHold = "On"
            else
				currentHold = "Off"
    }

	def holdString = (currentHold == "On")?
			( (past)? "Is On" : "Ends ${compareWithNow(holdExp.toLong())}") :
			((currentHold == "Off")? " is Off" : " ...")

    sendEvent("name":"setpointHoldDisplay", "value": "Hold ${holdString}")
}

def getSetPointHoldDuration()
{
	def holdTime = 0

    if (settings.hold_time?.contains("Hours"))
    {
    	holdTime = Integer.parseInt(settings.hold_time[0..1].trim())
    }
    else if (settings.hold_time?.contains("Day"))
    {
    	holdTime = Integer.parseInt(settings.hold_time[0..1].trim()) * 24
    }

    def currentHoldDuration = device.currentState("setpointHoldDuration")?.value


    if (Short.parseShort('0'+ (currentHoldDuration?: 0)) != (holdTime * 60))
    {
    	[
        	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x24 0x21 {" +
            String.format("%04X", ((holdTime * 60) as Short))  // switch to zigbee endian

            + "}", "delay 100",
			"st rattr 0x${device.deviceNetworkId} 1 0x201 0x24", "delay 200",
		]

    } else
    {
    	[]
    }

}

def Hold()
{
	def currentHold = device.currentState("setpointHold")?.value

	def next = (currentHold == "On") ? "00" : "01"
	def nextHold = getHoldMap()[next]

	sendEvent("name":"setpointHold", "value":nextHold)

	// set the duration first if it's changed

    [
    "st wattr 0x${device.deviceNetworkId} 1 0x201 0x23 0x30 {$next}", "delay 100" ,

    "raw 0x201 {04 21 11 00 00 05 00 }","delay 200",      // hold expiry time
  	"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ] + getSetPointHoldDuration()
}

def compareWithNow(d)
{
	long mins = (new Date(d)).getTime() - (new Date()).getTime()

	mins /= 1000 * 60;

    log.trace "mins: ${mins}"

    boolean past = (mins < 0)
    def ret = (past)? "" : "in "

    if (past)
    	mins *= -1;

    float t = 0;
	// minutes
	if (mins < 60)
	{
			ret +=  (mins as Integer) + " min" + ((mins > 1)? 's' : '')
	}else if (mins < 1440)
	{
		t = ( Math.round((14 + mins)/30) as Integer) / 2
        ret += t + " hr" +  ((t > 1)? 's' : '')
	} else
    {
		t = (Math.round((359 + mins)/720) as Integer) / 2
        ret +=  t + " day" + ((t > 1)? 's' : '')
	}
    ret += (past)? " ago": ""

    log.trace "ret: ${ret}"

    ret
}

def convertToTime(data)
{
	def time = Integer.parseInt("$data", 16) as long;
    time *= 1000;
    time += 946684800000; // 481418694
    time -= location.timeZone.getRawOffset() + location.timeZone.getDSTSavings();

    def d = new Date(time);

	//log.trace "converted $data to Time $d"
	return d;
}

def Program()
{
   	def currentSched = device.currentState("prorgammingOperation")?.value

    def next = Integer.parseInt(currentSched?: "00", 16);
    if ( (next & 0x01) == 0x01)
    	next = next & 0xfe;
    else
    	next = next | 0x01;

	def nextSched = getProgrammingMap()[next & 0x01]

    "st wattr 0x${device.deviceNetworkId} 1 0x201 0x25 0x18 {$next}"

}


def getThermostatOperatingState(value)
{
	String[] m = [ "heating", "cooling", "fan", "Heat2", "Cool2", "Fan2", "Fan3"]
	String desc = 'idle'
		value = Integer.parseInt(''+value, 16)

		// only check for 1-stage  for A1730
	for ( i in 0..2 ) {
		if (value & 1 << i)
			desc = m[i]
	}

	desc
}

def checkLastTimeSync(delay)
{
	def lastSync = device.currentState("lastTimeSync")?.value
    if (!lastSync)
    	lastSync = "${new Date(0)}"

    if (settings.sync_clock ?: false && lastSync != new Date(0))
    	{
        	sendEvent("name":"lastTimeSync", "value":"${new Date(0)}")
    	}



	long duration = (new Date()).getTime() - (new Date(lastSync)).getTime()

  //  log.debug "check Time: $lastSync duration: ${duration} settings.sync_clock: ${settings.sync_clock}"
	if (duration > 86400000)
		{
			sendEvent("name":"lastTimeSync", "value":"${new Date()}")
			return setThermostatTime()
		}

	return []
}

def readAttributesCommand(cluster, attribList)
{
	def attrString = ''

	for ( val in attribList ) {
    attrString += ' ' + String.format("%02X %02X", val & 0xff , (val >> 8) & 0xff)
	}

	//log.trace "list: " + attrString

	["raw "+ cluster + " {00 00 00 $attrString}","delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100",
	]
}

def refresh()
{
	log.debug "refresh called"
     // log.trace "list: " +       readAttributesCommand(0x201, [0x1C,0x1E,0x23])
      
        readAttributesCommand(0x201, [0x00,0x11,0x12]) +
        readAttributesCommand(0x201, [0x1C,0x1E,0x23]) +
        readAttributesCommand(0x201, [0x24,0x25,0x29]) +
        [
	    "st rattr 0x${device.deviceNetworkId} 1 0x204 0x01", "delay 200",  // lock status
        "raw 0x201 {04 21 11 00 00 05 00 }"                , "delay 500",  // hold expiary
	    "send 0x${device.deviceNetworkId} 1 1"             , "delay 1500",
		]  + checkLastTimeSync(2000)
}




def poll() {
	log.trace "poll called"
	refresh()
}

def getTemperature(value) {
	def celsius = Integer.parseInt("$value", 16) / 100

	if(getTemperatureScale() == "C"){
		return celsius as Integer
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

def setHeatingSetpoint(degrees) {
	def temperatureScale = getTemperatureScale()

	def degreesInteger = degrees as Integer
	sendEvent("name":"heatingSetpoint", "value":degreesInteger, "unit":temperatureScale)

	def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100) + "}"

}

def setCoolingSetpoint(degrees) {
	def degreesInteger = degrees as Integer
	sendEvent("name":"coolingSetpoint", "value":degreesInteger, "unit":temperatureScale)
	def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius*100) + "}"

}

def modes() {
	["off", "heat", "cool"]
}

def setThermostatFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	//log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "fanAuto":
			returnCommand = fanOn()
			break
		case "fanOn":
			returnCommand = fanAuto()
			break
	}
	if(!currentFanMode) { returnCommand = fanAuto() }
	returnCommand
}

def setThermostatMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def index = modeOrder.indexOf(currentMode)
	def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]

	setThermostatMode(next)
}

def setThermostatMode(String next) {
	def val = (getModeMap().find { it.value == next }?.key)?: "00"

	// log.trace "mode changing to $next sending value: $val"
    
	sendEvent("name":"thermostatMode", "value":"$next")
	["st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {$val}"] +
    refresh()
}

def setThermostatFanMode(String value) {
	log.debug "setThermostatFanMode({$value})"
	"$value"()
}

def off() {
	setThermostatMode("off")
}

def cool() {
	setThermostatMode("cool")}

def heat() {
	setThermostatMode("heat")
}

def auto() {
	setThermostatMode("auto")
}

def on() {
	fanOn()
}

def fanOn() {
	 sendEvent("name":"thermostatFanMode", "value":"fanOn")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {04}"
}


def fanAuto() {
	 sendEvent("name":"thermostatFanMode", "value":"fanAuto")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}

def updated()
{
	def lastSync = device.currentState("lastTimeSync")?.value
	if ((settings.sync_clock ?: false) == false)
			{
            	log.debug "resetting last sync time.  Used to be: $lastSync"
                sendEvent("name":"lastTimeSync", "value":"${new Date(0)}")

      }

}

def getLockMap()
{[
  "00":"Unlocked",
  "01":"Mode Only",
  "02":"Setpoint",
  "03":"Full",
  "04":"Full",
  "05":"Full",

]}
def lock()
{

	def currentLock = device.currentState("lockLevel")?.value
  def val = getLockMap().find { it.value == currentLock }?.key



  //log.debug "current lock is: ${val}"

  if (val == "00")
      val = getLockMap().find { it.value == (settings.lock_level ?: "Full") }?.key
  else
      val = "00"

 "st rattr 0x${device.deviceNetworkId} 1 0x204 0x01"

}


def setThermostatTime()
{

  if ((settings.sync_clock ?: false))
    {
      log.debug "sync time is disabled, leaving"
      return []
    }


  	Date date = new Date();
  	String zone = location.timeZone.getRawOffset() + " DST " + location.timeZone.getDSTSavings();

	long millis = date.getTime(); // Millis since Unix epoch
  	millis -= 946684800000;  // adjust for ZigBee EPOCH
  // adjust for time zone and DST offset
	millis += location.timeZone.getRawOffset() + location.timeZone.getDSTSavings();
	//convert to seconds
	millis /= 1000;

	// print to a string for hex capture
  	String s = String.format("%08X", millis);
	// hex capture for message format
  	String data = " " + s.substring(6, 8) + " " + s.substring(4, 6) + " " + s.substring(2, 4)+ " " + s.substring(0, 2);

	[
  	"raw 0x201 {04 21 11 00 02 0f 00 23 ${data} }",
  	"send 0x${device.deviceNetworkId} 1 ${endpointId}"
  	]
}

def configure() {

 	[
			"zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}", "delay 500",

            "zcl global send-me-a-report 0x201 0x0000 0x29 20 300 {19 00}",  // report temperature changes over 0.2C
  		    "send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

            "zcl global send-me-a-report 0x201 0x001C 0x30 10 305 { }",  // mode
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x0025 0x18 10 310 { 00 }",  // schedule on/off
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x001E 0x30 10 315 { 00 }",  // running mode
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x0011 0x29 10 320 {32 00}",  // cooling setpoint delta: 0.5C (0x3200 in little endian)
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x0012 0x29 10 320 {32 00}", // cooling setpoint delta: 0.5C (0x3200 in little endian)
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x0029 0x19 10 325 { 00 }", "delay 200",  // relay status
			"send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 500",

            "zcl global send-me-a-report 0x201 0x0023 0x30 10 330 { 00 }", 		// hold
            "send 0x${device.deviceNetworkId} 1 ${endpointId}","delay 1500",

	] + refresh()
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private getEndpointId()
{
	new BigInteger(device.endpointId, 16).toString()
}
