/**
 *  Vision 4-in-1 Motion Sensor
 *
 *  Author: Ray Tseng
 */
metadata {
  definition (name: "Vision 4-in-1 Motion Sensor", namespace: "vision-raytseng", author: "Ray Tseng", vid: "generic-motion-8", ocfDeviceType: "x.com.st.d.sensor.motion") {
    capability "Battery"
    capability "Motion Sensor"
    capability "Relative Humidity Measurement"
    capability "Temperature Measurement"
    capability "Illuminance Measurement"
    capability "Tamper Alert"
    capability "Health Check"
    
    fingerprint mfr:"0109", prod:"2021", model:"2112", deviceJoinName: "Vision ZP3113-7 4-in-1 Motion"
  }

  tiles(scale: 2) {
      multiAttributeTile(name: "motion", type: "generic", width: 6, height: 2) {
        tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
          attributeState("active", label: '${name}', icon: "st.motion.motion-detector.active", backgroundColor: "#e86d13")
          attributeState("inactive", label: '${name}', icon: "st.motion.motion-detector.inactive", backgroundColor: "#ffffff")
      }
    }

    valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "battery", label: '${currentValue}% battery', unit: ""
    }
    
    standardTile("tamper", "device.tamper", width: 2, height: 2, decoration: "flat") {
      state "detected", label: '${currentValue}'
      state "clear", label: '${currentValue}'
    }
    
    valueTile("temperature", "device.temperature", width: 2, height: 2) {
      state "temperature", label:'${currentValue}°'
    }

    valueTile("humidity", "device.humidity", width: 2, height: 2){
      state "humidity", label:'${currentValue}%'
    }

    valueTile("illuminance", "device.illuminance", width: 2, height: 2){
      state "illuminance", label:'${currentValue}lux'
    }

    main "motion"
    details(["motion", "temperature", "humidity", "illuminance", "battery", "tamper"])
  }
  
  preferences {
    input title: "", description: "Vision 4-in-1 Motion Sensor", type: "paragraph", element: "paragraph", displayDuringSetup: true, required: true
    
    getConfigurationNumber().each {idx ->
      switch (getConfigurationInfo(idx, "type")) {
        case "enum":
          input name: getConfigurationInfo(idx, "name"),
            title: getConfigurationInfo(idx, "title"),
            description: getConfigurationInfo(idx, "description"),
            type: getConfigurationInfo(idx, "type"), options: getConfigurationInfo(idx, "options"),
            defaultValue: getConfigurationInfo(idx, "default"),
            required: true, displayDuringSetup: true
          break
          
        case "number":
          input name: getConfigurationInfo(idx, "name"),
            title: getConfigurationInfo(idx, "title"),
            description: getConfigurationInfo(idx, "description"),
            type: getConfigurationInfo(idx, "type"), range: getConfigurationInfo(idx, "options"),
            defaultValue: getConfigurationInfo(idx, "default"),
            required: true, displayDuringSetup: true
          break
      }
    }
    
    input title: "", description: "Wake up settings", 
      type: "paragraph", element: "paragraph", displayDuringSetup: true, required: true
    input name: getWakeUpInfo("name"),
      title: getWakeUpInfo("title"),
      description: getWakeUpInfo("description"),
      type: getWakeUpInfo("type"), range: getWakeUpInfo("range"),
      defaultValue: getWakeUpInfo("default"),
      required: true, displayDuringSetup: true
  }
}

def installed() {
  def cmds = []
  def lightEnableDefault = [:]
  def configDefault = [:]
  def wakeupDefault = [:]
  
  getConfigurationNumber().each { idx ->
    configDefault."${getConfigurationInfo(idx, "name")}" = getConfigurationInfo(idx, "default")
    state."${getConfigurationInfo(idx, "name")}Refresh" = false
  }
  configurationUpdate(configDefault)
  
  wakeupDefault."${getWakeUpInfo("name")}" = getWakeUpInfo("default")
  state."${getWakeUpInfo("name")}Refresh" = false
  wakeUpIntervalUpdate(wakeupDefault)
  
  cmds += configure()
  if (cmds) {
    cmds += ["delay 5000", zwave.wakeUpV2.wakeUpNoMoreInformation().format()]
  }
  
  sendEvent(name: "motion", value: "inactive")
  sendEvent(name: "tamper", value: "clear")
  
  response(cmds)
}

def updated() {	
  logInfo "updated() Settings: ${settings}"
  configurationUpdate(settings)
  wakeUpIntervalUpdate(settings)
}

def configure() {
  logInfo "configure()"
  def cmds = []
  def value
  def optionValue
  
  if (device?.currentValue("temperature") == null) {
    optionValue = getConfigurationInfo(1, "enumMap").find { it.key == state."${getConfigurationInfo(1, "name")}" }?.value
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01, scale: optionValue?:0x00).format()
  }
  if (device?.currentValue("illuminance") == null) {
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03, scale: 0x00).format()
  }
  if (device?.currentValue("humidity") == null) {
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05, scale: 0x00).format()
  }
  if (canReportBattery() || device?.currentValue("battery") == null) {
    cmds << zwave.batteryV1.batteryGet().format()
  }
  
  getConfigurationNumber().each { idx ->
    if (state."${getConfigurationInfo(idx, "name")}Refresh" == true) {
      switch (getConfigurationInfo(idx, "type")) {
        case "enum":
          optionValue = getConfigurationInfo(idx, "enumMap").find { it.key == state."${getConfigurationInfo(idx, "name")}" }?.value
          value = integer2Array(optionValue, getConfigurationInfo(idx, "size"))
          break
          
        case "number":
          value = integer2Array(state."${getConfigurationInfo(idx, "name")}", getConfigurationInfo(idx, "size"))
          break
      }
      if (value != null) {
        cmds << zwave.configurationV2.configurationSet(parameterNumber: idx, defaultValue: false, size: getConfigurationInfo(idx, "size"), configurationValue: value).format()
        cmds << zwave.configurationV2.configurationGet(parameterNumber: idx).format()
        if (idx == 1) {
          cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01, scale: optionValue?:0x00).format()
        }
        value = null
      }
    }
  }
  
  if (state."${getWakeUpInfo("name")}Refresh" == true) {
     cmds << zwave.wakeUpV2.wakeUpIntervalSet(nodeid: zwaveHubNodeId, seconds: hour2Second(state.wakeUpInterval)).format()
     cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()
  }
  
  sendEvent(name: "checkInterval", value: (hour2Second(state.wakeUpInterval) + 2 * 60) * 2, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
  
  return cmds ? delayBetween(cmds, 500) : []
}

def configurationUpdate(config) {
  getConfigurationNumber().each { idx ->
    if (config?."${getConfigurationInfo(idx, "name")}" != null) {
      if (state."${getConfigurationInfo(idx, "name")}" != config?."${getConfigurationInfo(idx, "name")}") {
        state."${getConfigurationInfo(idx, "name")}" = config?."${getConfigurationInfo(idx, "name")}"
        state."${getConfigurationInfo(idx, "name")}Refresh" = true
      }
    }
  }
}

def configurationCheck(config) {
  getConfigurationNumber().each { idx ->
    if (config?."${getConfigurationInfo(idx, "name")}" != null) {
      if (config?."${getConfigurationInfo(idx, "name")}" == state."${getConfigurationInfo(idx, "name")}") {
        if (state."${getConfigurationInfo(idx, "name")}Refresh" == true) {
          state."${getConfigurationInfo(idx, "name")}Refresh" = false
          logInfo "config#${idx} <${getConfigurationInfo(idx, "name")} = ${state."${getConfigurationInfo(idx, "name")}"} > *** check passed"
        }
      }
    }
  }
}

def getConfigurationNumber() {
  return [1, 2, 3, 4, 5, 6, 7, 8]
}

def getConfigurationInfo(num, text) {
  def parameter = [:]
  
  parameter.parameter1name = "TemperatureUnit"
  parameter.parameter1title = "Temperature Unit [°C/°F]"
  parameter.parameter1description = ""
  parameter.parameter1type = "enum"
  parameter.parameter1options = ["°C", "°F"]
  parameter.parameter1enumMap = ["°C": 0, "°F": 1]
  parameter.parameter1default = "°C"
  parameter.parameter1size = 1
  
  parameter.parameter2name = "TempReportWhenChanged"
  parameter.parameter2title = "Report when temperature difference is over the setting [unit is 0.1°C/°F]"
  parameter.parameter2description = ""
  parameter.parameter2type = "number"
  parameter.parameter2options = "1..50"
  parameter.parameter2enumMap = []
  parameter.parameter2default = 30
  parameter.parameter2size = 1
  
  parameter.parameter3name = "HumiReportWhenChanged"
  parameter.parameter3title = "Report when humidity difference is over the setting [%]"
  parameter.parameter3description = ""
  parameter.parameter3type = "number"
  parameter.parameter3options = "1..50"
  parameter.parameter3enumMap = []
  parameter.parameter3default = 20
  parameter.parameter3size = 1
  
  parameter.parameter4name = "LightReportWhenChanged"
  parameter.parameter4title = "Report when illuminance difference is over the setting [%](1% is approximately equal to 4.5 lux)"
  parameter.parameter4description = ""
  parameter.parameter4type = "number"
  parameter.parameter4options = "5..50"
  parameter.parameter4enumMap = []
  parameter.parameter4default = 25
  parameter.parameter4size = 1
  
  parameter.parameter5name = "MotionRestoreTime"
  parameter.parameter5title = "Motion inactive report time [Minutes] after active"
  parameter.parameter5description = ""
  parameter.parameter5type = "number"
  parameter.parameter5options = "1..127"
  parameter.parameter5enumMap = []
  parameter.parameter5default = 3
  parameter.parameter5size = 1
  
  parameter.parameter6name = "MotionSensitivity"
  parameter.parameter6title = "Motion active sensitivity"
  parameter.parameter6description = ""
  parameter.parameter6type = "enum"
  parameter.parameter6options = ["Highest", "Higher", "High", "Medium", "Low", "Lower", "Lowest"]
  parameter.parameter6enumMap = ["Highest": 1, "Higher": 2, "High": 3, "Medium": 4, "Low": 5, "Lower": 6, "Lowest": 7]
  parameter.parameter6default = "Medium"
  parameter.parameter6size = 1
  
  parameter.parameter7name = "LedDispMode"
  parameter.parameter7title = "LED display mode"
  parameter.parameter7description = ""
  parameter.parameter7type = "enum"
  parameter.parameter7options = ["LED off when Temperature report/Motion active",
                       "LED blink when Temperature report/Motion active",
                       "LED blink when Motion active/LED off when Temperature report"]
  parameter.parameter7enumMap = ["LED off when Temperature report/Motion active": 1,
                       "LED blink when Temperature report/Motion active": 2,
                       "LED blink when Motion active/LED off when Temperature report": 3]
  parameter.parameter7default = "LED off when Temperature report/Motion active"
  parameter.parameter7size = 1
  
  parameter.parameter8name = "RetryTimes"
  parameter.parameter8title = "Motion notification retry times"
  parameter.parameter8description = ""
  parameter.parameter8type = "number"
  parameter.parameter8options = "0..10"
  parameter.parameter8enumMap = []
  parameter.parameter8default = 3
  parameter.parameter8size = 1
  
  return parameter."parameter${num}${text}"
}

def wakeUpIntervalUpdate(interval) {
  def value = interval?.find { it.key == "${getWakeUpInfo("name")}" }?.value
  
  if (value != null) {
    if (state."${getWakeUpInfo("name")}" != value) {
      state."${getWakeUpInfo("name")}" = value
      state."${getWakeUpInfo("name")}Refresh" = true
    }
  }
}

def wakeUpIntervalCheck(interval) {
  def value = interval?.find { it.key == "${getWakeUpInfo("name")}" }?.value
  
  if (value != null) {
    if (state."${getWakeUpInfo("name")}" == value) {
      if (state."${getWakeUpInfo("name")}Refresh" == true) {    
        state."${getWakeUpInfo("name")}Refresh" = false
        logInfo "wake up interval *** check passed"
      }
    }
  }
}

def getWakeUpInfo(text) {
  def wakeUp = [:]
  
  wakeUp.name = "wakeUpInterval"
  wakeUp.title = "Wake up interval [Hours]"
  wakeUp.description = ""
  wakeUp.type = "number"
  wakeUp.range = "1..4660"
  wakeUp.default = 24
  
  return wakeUp."${text}"
}

private getCommandClassVersions() {
	[
    0x5E: 2,
    0x22: 1,
    0x85: 2,
    0x59: 3,
    0x80: 1,
    0x70: 2,
    0x5A: 1,
    0x7A: 5,
    0x87: 3,
    0x72: 2,
    0x8E: 3,
    0X31: 5,
    0x71: 3,
    0x73: 1,
    0x98: 1,
    0x9F: 1,
    0x6C: 1,
    0x55: 2,
    0x86: 3,
    0x84: 2 
	]
}

def parse(String description) {
  def result = []
  def cmd = zwave.parse(description, commandClassVersions)
  
  if (cmd) {
    result += zwaveEvent(cmd)
    logInfo "Parsed ${cmd} to ${result.inspect()}"
  }
  else {
    logDebug "Unable to parse description: ${description}"
  }
  
  return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
  
  cmds += configure()
  if (cmds) {
    cmds << "delay 5000"
  }
  cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
  
	return cmds ? response(cmds) : []
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
  if (cmd.nodeid == zwaveHubNodeId) {
    def interval = [:]
    interval."${getWakeUpInfo("name")}" = second2Hour(cmd.seconds)
    logInfo "interval = ${interval}"
    wakeUpIntervalCheck(interval)
  }
  []
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
  def map = [name: "battery", unit: "%"]
  
  if (cmd.batteryLevel == 0xFF) {
    map.value = 1
    map.descriptionText = "${device.displayName} has a low battery"
    map.isStateChange = true
  } else {
    map.value = cmd.batteryLevel
  }
  
  state.lastBatteryReport = new Date().time	
  createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
  def size = getConfigurationInfo(cmd.parameterNumber, "size")
  def value = array2Integer(cmd.configurationValue)
  def config = [:]
  
  if (size && size == cmd.size) {
    switch (getConfigurationInfo(cmd.parameterNumber, "type")) {
      case "enum":
        def optionName = getConfigurationInfo(cmd.parameterNumber, "enumMap").find { it.value == value}?.key
        if (optionName) {
          config."${getConfigurationInfo(cmd.parameterNumber, "name")}" = optionName
        }
        break
        
      case "number":
        config."${getConfigurationInfo(cmd.parameterNumber, "name")}" = value
        break
    }
    
    if (config) {
      configurationCheck(config)
    }
  }
  []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
  def result = []
  
  if (cmd.notificationType == 0x07) {
    if (cmd.eventParametersLength) {
      cmd.eventParameter.each {
        if (it == 0x03) {
          result = createEvent(name: "tamper", value: "clear") 
        }
        else if( it == 0x08) {
          result = createEvent(name: "motion", value: "inactive") 
        }
      }
    }
    else if (cmd.event == 0x03) {
      result = createEvent(name: "tamper", value: "detected") 
    }
    else if (cmd.event == 0x08) {
      result = createEvent(name: "motion", value: "active") 
    }
  }
  
  return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
  def map = [:]
  
  switch (cmd.sensorType) {
    case 0x01:
      map.name = "temperature"
      map.value = cmd.scaledSensorValue
      map.unit = cmd.scale == 0 ? "C": "F"
      break
      
    case 0x03:
      map.name = "illuminance"
      map.value = getLuxFromPercentage(cmd.scaledSensorValue)
      map.unit = "lux"
      break
      
    case 0x05:
      map.name = "humidity"
      map.value = cmd.scaledSensorValue
      map.unit = "%"
      break
      
    default:
      map.descriptionText = cmd.toString()
      break
  }
  
  createEvent(map)
}

def getBatteryReportIntervalSeconds() {
  return 8 * 3600
}

def canReportBattery() {
	def reportEveryMS = (getBatteryReportIntervalSeconds() * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state?.lastBatteryReport > reportEveryMS))
}

def array2Integer(array) {
  switch (array.size()) {
    case 1:
      array[0]
      break
    case 2:
      ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
      break
    case 4:
      ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
      break
  }
}

def integer2Array(value, size) {
  switch (size) {
    case 1:
      [value]
      break
    case 2:
      [(value >> 8) & 0xFF, value & 0xFF]
      break
    case 4:
      [(value >> 24) & 0xFF, (value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF]
      break
  }
}

def hour2Second(hour) {
  return hour * 3600
}

def second2Hour(second) {
  return second / 3600
}

private getLuxFromPercentage(percentageValue) {
	def multiplier = luxConversionData.find {
		percentageValue >= it.min && percentageValue <= it.max
	}?.multiplier ?: 5.312
	def luxValue = percentageValue * multiplier
	Math.round(luxValue)
}

private getLuxConversionData() {[
  [min: 0, max: 9.99, multiplier: 3.843],
  [min: 10, max: 19.99, multiplier: 5.231],
  [min: 20, max: 29.99, multiplier: 4.999],
  [min: 30, max: 39.99, multiplier: 4.981],
  [min: 40, max: 49.99, multiplier: 5.194],
  [min: 50, max: 59.99, multiplier: 6.016],
  [min: 60, max: 69.99, multiplier: 4.852],
  [min: 70, max: 79.99, multiplier: 4.836],
  [min: 80, max: 89.99, multiplier: 4.613],
  [min: 90, max: 100, multiplier: 4.5]
]}

def logInfo(msg) {
}

def logDebug(msg) {
  log.debug "${msg}"
}