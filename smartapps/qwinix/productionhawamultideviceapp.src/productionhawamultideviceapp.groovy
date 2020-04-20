include 'asynchttp_v1'
/**
*  ProductionHawaMultiDeviceApp for HAWA
*  Borrowed from the Union Denver codebase for a demo with hawa
*  Confidential to SpicyKey LLC for Qwinix
*
*  Examples:
*  GET /alldevices
*  PUT /cmd/1234-5123/off
*  PUT /thermostat/fan/off
*  PUT /lights/off
*  PUT /model/on
*  GET /alldevices
*  GET /devices/lights
*
* model:
* one or more light
* at most one thermostat
* one or more lock
* one or more sensor
*/
definition(
  name: "ProductionHawaMultiDeviceApp",
  namespace: "Qwinix",
  author: "azumkhawala@qwinix.io",
  description: "HAWA APIs.",
  category: "Convenience",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "Qwinix Home Dashboard", displayLink: "http://qwinix.io"]) {
    appSetting "pusherServerURI"
    appSetting "hawaEnvironmentDescription"
  }

  mappings {
    path("/alldevices") {
      action: [
      GET: "listAllDevices"
      ]
    }
    path("/devices/:type") {
      action: [
      GET: "listAllDevices"
      ]
    }
    path ("/device/:deviceid") {
      action: [
      GET: "listDeviceById"
      ]
    }
    path ("/cmd/:deviceid/:cmdname") {
      action: [
      PUT: "commandDeviceById"
      ]
    }
    path("/model/:cmd") {
      action: [
      PUT: "toggleEntireModelApartment"
      ]
    }
    path("/thermostat/mode/:mode") {
      action: [
      PUT: "updateThermostatMode"
      ]
    }
    path("/thermostat/fan/:mode") {
      action: [
      PUT: "updateThermostatFanMode"
      ]
    }
    path("/thermostat/setpoint/:setpoint/:modify") {
      action: [
      PUT: "updateThermostatSetPoint"
      ]
    }
  }

  preferences {
    // http://docs.smartthings.com/en/latest/capabilities-reference.html?highlight=capability
    section ("Select light(s),fans(s),switch(es),or blind(s)") {
      input "rangedswitches", "capability.switchLevel", multiple: true, required: false, title: "Which light(s),fans(s),switch(es),or blind(s)?"
    }
    section ("Select locks(s)...") {
      input "locks", "capability.lock", multiple: true, required: false, title: "Which lock(s)?"
    }
    section ("Select thermostat(s)...") {
      input "thermostats", "capability.thermostat", multiple: false, required: false, title: "Which thermostat(s)?"
    }
    section ("Select sensor(s)...") {
      input "sensors", "capability.contactSensor", multiple: true, required: false, title: "Which sensor(s)?"
    }
    section ("Select music players(s)...") {
      input "musicplayers", "capability.musicPlayer", multiple: true, required: false, title: "Which music players(s)?"
    }
    section ("Select water sensors(s)...") {
      input "watersensors", "capability.waterSensor", multiple: true, required: false, title: "Which water sensors(s)?"
    }
    section ("Select motion sensors(s)...") {
      input "motionsensors", "capability.motionSensor", multiple: true, required: false, title: "Which motion sensors(s)?"
    }
  }

  def identifySwitchQhublabel(switchableDevice) {
    // fan
    // camera
    // light
    // light with dimmer
//    log.debug "guessing type of ${switchableDevice.name} labeled ${switchableDevice.label}"
    def qhublabel = 'switch';
    def lclabel = switchableDevice.label.toLowerCase()
    if (lclabel.indexOf('fan') > -1) {
      qhublabel = 'fan'
    }
    else if (lclabel.indexOf('blind') > -1) {
      qhublabel = 'blind'
    }
    else if (lclabel.indexOf('light') > -1 ||
        lclabel.indexOf('spot') > -1 ||
        lclabel.indexOf('pendant') > -1 ||
        lclabel.indexOf('foyer hallway') > -1 ||
        lclabel.indexOf('bedroom hallway') > -1 ||
        lclabel.indexOf('bathroom vanity') > -1 ||
        lclabel.indexOf('lamp') > -1) {
      qhublabel = 'light'
    }
    else if (switchableDevice.currentState('camera') != null) {
      qhublabel = 'camera'
    }
    log.debug "${switchableDevice.name} labeled ${switchableDevice.label} is a ${qhublabel}"
    return qhublabel
  }

  def initialize() {
    def devicemap = [:]
    log.info "building device map"
    rangedswitches.each() {
      def qhublabel = identifySwitchQhublabel(it)
      devicemap.put(it.id, qhublabel)
    }
    locks.each() {
      devicemap.put(it.id, 'lock')
    }
    thermostats.each() {
      devicemap.put(it.id, 'thermostat')
    }
    sensors.each() {
      devicemap.put(it.id, 'sensor')
    }
    musicplayers.each() {
      devicemap.put(it.id, 'musicplayer')
    }
    motionsensors.each() {
      devicemap.put(it.id, ((it.currentState('camera') != null) ? 'camera' : 'motionsensor'))
    }
    watersensors.each() {
      devicemap.put(it.id, 'watersensor')
    }

    state.devicemap = devicemap
    log.info "subscribing to events................................."

    rangedswitches.each() {
      subscribe(it, "level", switchLevelHandler)
      subscribe(it, "switch", switchHandler)
    }
    locks.each() {
      subscribe(it, "lock", lockHandler)
    }
    thermostats.each() {
      subscribe(it, "coolingSetpoint", thermostatHandler)
      subscribe(it, "temperature", thermostatHandler)
      subscribe(it, "thermostatFanMode", thermostatHandler)
      subscribe(it, "thermostatMode", thermostatHandler)
    }
    sensors.each() {
      subscribe(it, "contact", contactHandler)
    }
    watersensors.each() {
      subscribe(it, "water", waterSensorHandler)
    }
    musicplayers.each() {
      subscribe(it, "level", musicPlayerHandler)
      subscribe(it, "mute", musicPlayerHandler)
      subscribe(it, "status", musicPlayerHandler)
    }
    motionsensors.each() {
      subscribe(it, "motion", motionSensorHandler);
    }
    log.info "subscribed"
  }

  def installed() {
   // log.info "Installed with settings: ${settings}"
    initialize()
  }

  def updated() {
   // log.info "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
  }

  void updateThermostatSetPoint() {
    def setpoint = params.setpoint
    def modify = params.modify
    def current = 0
    def method = ""
    switch (setpoint) {
      case "heating":
      current = thermostats.currentValue("heatingSetpoint")
      method = "setHeatingSetpoint"
      break
      case "cooling":
      current = thermostats.currentValue("coolingSetpoint")
      method = "setCoolingSetpoint"
      break
      default:
      httpError(400, "$setpoint is not a valid setpoint for a thermostat")
    }
    switch(modify) {
      case "up":
      thermostats."$method"(current + 1)
      break
      case "down":
      thermostats."$method"(current - 1)
      break
      default:
      httpError(400, "$modify is not a valid modification for a thermostat setpoint")
    }
  }

  void updateThermostatFanMode() {
    def mode = params.mode
    switch(mode) {
      case "on":
      thermostats.setThermostatFanMode("on")
      break
      case "circulate":
      thermostats.setThermostatFanMode("circulate")
      break
      case "auto":
      thermostats.setThermostatFanMode("auto")
      break
      default:
      httpError(400, "$command is not a valid mode for the thermostat fan")
    }
  }

  void updateThermostatMode() {
    def mode = params.mode
    switch(mode) {
      case "heat":
      thermostats.setThermostatMode("heat")
      break
      case "cool":
      thermostats.setThermostatMode("cool")
      break
      case "auto":
      thermostats.setThermostatMode("auto")
      break
      case "off":
      thermostats.setThermostatMode("off")
      break
      default:
      httpError(400, "$mode is not a valid mode for the thermostat")
    }
  }

  void updateLock(l, command) {
    switch(command) {
      case "lock":
        l.lock()
      break
      case "unlock":
        l.unlock()
      break
      default:
      httpError(400, "$command is not a valid command for the lock")
      }
   }

  void updateLevel(l, command) {
    switch(command) {
      case "on":
      l.setLevel(99)
      break
      case "off":
      l.setLevel(0)
      break
      case "l25":
      l.setLevel(25)
      break
      case "l50":
      l.setLevel(50)
      break
      case "l75":
      l.setLevel(75)
      break
      case "l100":
      l.setLevel(99)
      break
      default:
      httpError(400, "$command is not a valid command for the switch")
    }
    // switches get stitches
  }

  def updateMusicPlayer(m, command) {
    try {
      log.debug "$command music player ... " + command.indexOf('vo') + " yeah "
      if (command.indexOf('vo') > -1) {
        def newVolumeLevel = command.substring(2);
        m.setLevel(newVolumeLevel.toInteger());
      }
      else {
        m."$command"()
      }
    }
    catch (e) {
      log.debug "something went wrong: $e"
      httpError(400, "$command is not a valid command for the music player")
    }
  }

  def listDeviceById() {

    def deviceid = params.deviceid
    def dtype = state.devicemap[deviceid]
    if (dtype == null) {
      httpError(404, "No device installed with id " + deviceid + " in the qwinix smart app. Check the samsung smarthings developer dashboard.")
    }
    else {
      def match = null
      def response = null
      def d = null
        switch (dtype) {
        case 'light':
        case 'fan':
        case 'blind':
        case 'switch':
          d = rangedswitches.find { it.id == deviceid }
          response = switchResponse(d, dtype)
        break
        case 'lock':
          d = locks.find { it.id == deviceid }
          response = lockResponse(d)
        break
        case 'thermostat':
          d = thermostat
          response = thermostatResponse(d)
        break
        case 'sensor':
          d = sensors.find { it.id == deviceid }
          response = sensorResponse(d)
        break
        case 'watersensor':
          d = watersensors.find { it.id == deviceid }
          response = waterSensorResponse(d)
        break
        case 'motionsensor':
        case 'camera':
          d = motionsensors.find { it.id == deviceid }
          response = motionSensorResponse(d)
        break
        case 'musicplayer':
          d = musicplayers.find { it.id == deviceid }
          response = musicPlayerResponse(d)
        break

        default:
        httpError(404, "No device installed with id " + deviceid + " in the qwinix smart app. Check the samsung smarthings developer dashboard.")
      }
      if (response != null) {
        return response
      }
    }
  }

  def switchResponse(l,type) {
      return [name: l.getName(), status: l.currentValue('switch'), level: l.currentValue("level"), id: l.id, qhublabel: type, userlabel: l.label]
  }

  def lockResponse(l) {
    return [name: l.getName(), lock: l.currentValue("lock"), id: l.id, qhublabel: 'lock', userlabel: l.label]
  }

  def thermostatResponse(t) {
    return [name: t.getName(),
       id: t.id,
       temperature: t.currentValue("temperature"),
       thermostatMode: t.currentValue("thermostatMode"),
       thermostatFanMode: t.currentValue("thermostatFanMode"),
       coolingSetpoint: t.currentValue("coolingSetpoint"),
       heatingSetpoint: t.currentValue("heatingSetpoint"),
       qhublabel: 'thermostat',
       userlabel: t.label]
  }

  def sensorResponse(s) {
    return [name: s.getName(), contact: s.currentValue("contact"), id: s.id, qhublabel: 'sensor', userlabel: s.label]
  }

  def waterSensorResponse(s) {
    return [name: s.getName(), water: s.currentValue("water"), id: s.id, qhublabel: 'watersensor', userlabel: s.label]
  }

  def motionSensorResponse(s) {
    if (s.currentState('camera') != null) {
      return [name: s.getName(), motion: s.currentValue("motion"), id: s.id, qhublabel: 'camera', userlabel: s.label, camera: s.currentValue('camera')]
    }
    else {
      return [name: s.getName(), motion: s.currentValue("motion"), id: s.id, qhublabel: 'motionsensor', userlabel: s.label]
    }
  }

  def musicPlayerResponse(m) {
    return [name: m.getName(),
            mute: m.currentValue("mute"),
            level: m.currentValue("level"),
            status: m.currentValue("status"),
            trackdescription: m.currentValue("trackDescription"),
            id: m.id, qhublabel: 'musicplayer',
            userlabel: m.label]
  }

  def listAllDevices() {
    def type = (params.type != null) ? params.type : ''
    log.debug "List ${type} devices"
    def resp = [location: location.id, hawaenv: appSettings.hawaEnvironmentDescription]
    resp['devices'] = []
    rangedswitches.each{ resp['devices'].push(switchResponse(it, identifySwitchQhublabel(it))) }
    locks.each{ resp['devices'].push(lockResponse(it)) }
    sensors.each{ resp['devices'].push(sensorResponse(it)) }
    thermostats.each{ resp['devices'].push(thermostatResponse(it)) }
    musicplayers.each{ resp['devices'].push(musicPlayerResponse(it)) }
    motionsensors.each{ resp['devices'].push(motionSensorResponse(it)) }
    watersensors.each{ resp['devices'].push(waterSensorResponse(it)) }
    log.debug resp
    return resp
  }

  def toggleEntireModelApartment() {
     def cmd = params.cmd
     switch (cmd){
       case 'off':
        log.debug 'Turning all the lights and music players off.'
        rangedswitches.each { updateLevel(it, 'off') }
        musicplayers.each { updateMusicPlayer(it, 'off') }
       break
       default:
         httpError(400, "Sorry, I can only turn the model apartment off.")
     }
  }

  def commandDeviceById() {
    def deviceid = params.deviceid
    def cmdname = params.cmdname
    def attribute = params.attribute
    def dtype = state.devicemap[deviceid]
    if (dtype == null) {
      httpError(404, "No device installed with id " + deviceid + " in the qwinix smart app. Check the samsung smarthings developer dashboard.")
    }
    else if (cmdname == null) {
      httpError(400, "No cmd name provided.")
    }
    else {
      switch (dtype) {
        case 'light':
        case 'fan':
        case 'camera':
        case 'blind':
        case 'switch':
          def l = rangedswitches.find { it.id == deviceid }
          updateLevel(l, cmdname)
        break
        case 'lock':
          def l = locks.find { it.id == deviceid }
          updateLock(l, cmdname)
        break
        case 'musicplayer':
          def s = musicplayers.find { it.id == deviceid }
          updateMusicPlayer(s, cmdname)
        break
        default:
        httpError(404, "No device installed with id " + deviceid + " in the qwinix smart app. Check the samsung smarthings developer dashboard.")
      }
    }
  }

  def musicPlayerHandler(evt) {
    def m = evt.device
    log.trace "music player changed"
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: m.getDisplayName(),
               status: m.currentValue('status'),
               level: m.currentValue('level'),
               mute: m.currentValue('mute'),
               qhublabel: 'musicplayer',
               id: m.id,
               location: m.id,
               source: "${evt.source}",
               uuid: "${evt.id}",
               description: "${evt.descriptionText}",
               hawaenv: appSettings.hawaEnvironmentDescription]
    ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
       log.debug "POST: $params"
    }
    catch (e) {
           log.debug "something went wrong: $e"
    }
  }

  def waterSensorHandler(evt) {
    def ms = evt.device
    def params = [
      uri:  appSettings.pusherServerURI,
      body: [name: ms.getDisplayName(),
             water: ms.currentValue('water'),
             qhublabel: 'watersensor',
             id: ms.id,
             location: location.id,
             source: "${evt.source}",
             uuid: "${evt.id}",
             description: "${evt.descriptionText}",
             hawaenv: appSettings.hawaEnvironmentDescription]
      ]

    try {
      asynchttp_v1.post('responseHandlerMethod', params)
      log.debug "POST: $params"
    }
    catch (e) {
      log.debug "something went wrong: $e"
    }
  }

  def motionSensorHandler(evt) {
    def ms = evt.device
    def params = [
      uri:  appSettings.pusherServerURI,
      body: [name: ms.getDisplayName(),
             contact: ms.currentValue('motion'),
             qhublabel: 'motionsensor',
             id: ms.id,
             location: location.id,
             source: "${evt.source}",
             uuid: "${evt.id}",
             description: "${evt.descriptionText}",
             hawaenv: appSettings.hawaEnvironmentDescription]
     ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
        log.debug "POST: $params"
    }
    catch (e) {
        log.debug "something went wrong: $e"
    }
  }

  def contactHandler(evt) {
    def sensor = evt.device
   log.trace "contact changed"
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: sensor.getDisplayName(),
               contact: sensor.currentValue('contact'),
               qhublabel: 'sensor',
               id: sensor.id,
               location: location.id,
               source: "${evt.source}",
               uuid: "${evt.id}",
               description: "${evt.descriptionText}",
               hawaenv: appSettings.hawaEnvironmentDescription]
    ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
       log.debug "POST: $params"
    }
    catch (e) {
           log.debug "something went wrong: $e"
    }
  }

  def thermostatHandler(evt) {
    def t = evt.device
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: t.getDisplayName(),
               coolingSetpoint: t.currentValue('coolingSetpoint'),
               temperature: t.currentValue('temperature'),
               heatingSetpoint: t.currentValue('heatingSetpoint'),
               thermostatFanMode : t.currentValue('thermostatFanMode'),
               thermostatMode: t.currentValue('thermostatMode'),
               qhublabel: 'thermostat',
               id: t.id,
               location: location.id,
               source: "${evt.source}",
               uuid: "${evt.id}",
               description: "${evt.descriptionText}",
               hawaenv: appSettings.hawaEnvironmentDescription]
    ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
    }
    catch (e) {
            log.error "something went wrong: $e"
    }
  }

  def lockHandler(evt) {
    def lock = evt.device
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: lock.getDisplayName(),
               lock: lock.currentValue('lock'),
               qhublabel: 'lock',
               id: lock.id,
               location: location.id,
               source: "${evt.source}",
               uuid: "${evt.id}",
               description: "${evt.descriptionText}",
               hawaenv: appSettings.hawaEnvironmentDescription]
    ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
    }
    catch (e) {
            log.error "something went wrong: $e"
    }
  }

  def switchHandler(evt) {
    def switchWithLevel = evt.device
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: switchWithLevel.getDisplayName(),
              status: switchWithLevel.currentValue('switch'),
              level: (switchWithLevel.currentValue('level') != null) ? switchWithLevel.currentValue('level') : '',
              qhublabel: identifySwitchQhublabel(switchWithLevel),
              id: switchWithLevel.id,
              location: location.id,
              source: "${evt.source}",
              uuid: "${evt.id}",
              description: "${evt.descriptionText}",
              hawaenv: appSettings.hawaEnvironmentDescription],
    ]

    try {
        asynchttp_v1.post('responseHandlerMethod', params)
    }
    catch (e) {
   	 	log.error "something went wrong: $e"
	}
  }

  def switchLevelHandler(evt) {
    def switchWithLevel = evt.device
    def params = [
        uri:  appSettings.pusherServerURI,
        body: [name: switchWithLevel.getDisplayName(),
              status: switchWithLevel.currentValue('switch'),
              level: (switchWithLevel.currentValue('level') != null) ? switchWithLevel.currentValue('level') : '',
              qhublabel: identifySwitchQhublabel(switchWithLevel),
              id: switchWithLevel.id,
              location: location.id,
              source: "${evt.source}",
              uuid: "${evt.id}",
              description: "${evt.descriptionText}",
              hawaenv: appSettings.hawaEnvironmentDescription],
    ]

    try {
        if (switchWithLevel.currentValue('switch') == 'on') {
          asynchttp_v1.post('responseHandlerMethod', params)
        }
    }
    catch (e) {
   	 	log.error "something went wrong: $e"
	}
}


def responseHandlerMethod(response, data) {
//    log.trace "data map passed to handler method is: $data"
    if (response.hasError()) {
       log.error "${appSettings.hawaEnvironmentDescription} push response: $response.errorData"
    }
    else {
       log.trace "${appSettings.hawaEnvironmentDescription} push response: ${response.getData()}"
    }
}
