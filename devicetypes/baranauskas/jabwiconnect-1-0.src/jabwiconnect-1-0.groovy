/**
 *  Copyright 2020 Jose Augusto Baranauskas
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
 * Notes: Please read remarks about attributes near theirs definitions.
 *
 */
metadata {
    definition (
       name: "jabWiConnect 1.0",
       version: "2.0 (2021-01-26)",
       namespace: "baranauskas",
       author: "Jose Augusto Baranauskas",
       runLocally: true,
       minHubCoreVersion: '000.021.00001'
    ) {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
//        capability "Contact Sensor"
//        capability "Motion Sensor"
//        capability "Acceleration Sensor"
//        capability "Infrared Level"
        capability "Refresh"

        command "childOn"
        command "childOff"
        command "sendData", ["string"]


        attribute "altitude",               "number"
    }

    preferences{
        input("serverIP", "text",
              title: "Controller Server IP",
              description: "Format 192.168.1.201",
              required: true,
              displayDuringSetup: true
        )
        input("serverPort", "decimal",
              title: "Controller Server Port",
              description: "Format 3000",
              range: "0..20000",
              required: true,
              displayDuringSetup: true
        )
        input("serverUser", "text",
              title: "Mobile user",
              description: "Enter mobile user",
              required: true,
              displayDuringSetup: true
        )
        input("serverPassword", "password",
              title: "Mobile user password",
              description: "Enter password",
              required: true,
              displayDuringSetup: true
        )
    }
}
import groovy.json.JsonSlurper

def getStateValue( key ) {
    return state[key]
}

def installed() {
    log.debug "installed() with ${settings}"

    def pwd = settings.serverPassword
    // provisorio ate ter MD5 implementado
    pwd = "dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
    state.token = "Basic ${pwd}"
    updated()
}

def uninstalled() {
    log.debug "uninstalled()"
    unschedule()
}

def updated() {
    log.debug "updated() with ${settings}"

    unschedule()
//    subscribe(location, null, lanResponseHandler, [filterEvents:false])
    initialize()

    def pwd = settings.serverPassword
    // provisorio ate ter MD5 implementado
    pwd = "dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
    state.token = "Basic ${pwd}"

    wcFeedback()
//    refresh()

    // Schedules
//    runEvery1Minute( refreshSunPosition )
//    int ss = 1 + 58 * Math.random()
//    def cronString = "${ss} 0 0 1/1 * ? *"
//    log.debug "cron schedule: ${cronString}"
//    schedule( cronString, refreshSunTimes )
}

def initialize() {
    // create child
    // first check if child device is already installed
    def childDevices = getChildDevices()
    log.trace "Child found ${childDevices.size()}: ${childDevices}"
    //DeviceWrapper addChildDevice(String typeName, String deviceNetworkId, hubId, Map properties)

    if ( childDevices.size() == 0 ) {
      log.trace "Creating Child Devices"

      def cmd = [
          method: "GET",
          path: "/dispositivos",
          headers: [
            HOST: "192.168.1.201:3000",
            "Content-Type": "application/json",
            "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
          ]
      ]
      def hubAction = new physicalgraph.device.HubAction( cmd
          , null
          , [callback: initializeHandler]
      )
      try {
        sendHubCommand( hubAction )
      }
      catch(Exception e) {
        log.error "Error: ${e}"
      }
   }
}

def getDispositivos() {
  def cmd = [
      method: "GET",
      path: "/dispositivos",
      headers: [
        HOST: "${settings.serverIP}:${settings.serverPort}",
        "Content-Type": "application/json",
        "Authorization": "${getStateValue('token')}"
      ]
  ]
  def hubAction = new physicalgraph.device.HubAction( cmd,
      null,
      [callback: getDispositivosHandler]
  )
  try {
    sendHubCommand( hubAction )
  }
  catch(Exception e) {
    log.error "Error: ${e}"
  }
}

void getDispositivosHandler(physicalgraph.device.HubResponse hubResponse) {
    log.debug "getDispositivosHandler()"
    def status = hubResponse.status
    if( status != 200 ) {
      log.error "status not okay"
      return
    }
    def body = hubResponse.body
    def jsonSlurper = new JsonSlurper()
    def dispositivos = jsonSlurper.parseText( body )
    dispositivos = dispositivos.dispositivos
    if( ! dispositivos ) {
      log.error "Nao sao dispositivos"
      return;
    }
    def len = dispositivos.size()
    log.debug "encontrados ${len} dispositivos"
    def i
    def tipos = [0, 0, 0] as ArrayList
    for(i=0; i<len; i++) {
      def d = dispositivos[ i ]
      //  log.debug "d: ${d.id},${d.nome},${d.endereco},${d.tipo}"
      int k = d.tipo
      tipos[ k ] = tipos [ k ] + 1
    }
    tipos.eachWithIndex { valor, index ->
      log.debug "tipo ${index}: ${valor} dispositivos"
    }
}



void initializeHandler(physicalgraph.device.HubResponse hubResponse) {
//    log.debug "hubResponse: ${body}"
    log.debug "initializeHandler()"
    def status = hubResponse.status
    if( status != 200 ) {
      log.error "status not okay"
      return
    }
    def body = hubResponse.body
    def jsonSlurper = new JsonSlurper()
    def dispositivos = jsonSlurper.parseText( body )
    dispositivos = dispositivos.dispositivos
    if( ! dispositivos ) {
      log.debug "Nao sao dispositivos"
      return;
    }
    def len = dispositivos.size()
    log.debug "encontrados ${len} dispositivos"
    def i
    def instalar = [ 42, 49, 50, 51, 26, 13, 12 ]
    def poucos = []
    def tipos = [0, 0, 0] as ArrayList
    for(i=0; i<len; i++) {
      def d = dispositivos[ i ]
      //  log.debug "d: ${d.id},${d.nome},${d.endereco},${d.tipo}"
      int k = d.tipo
      if( d.id in instalar )
        poucos << d
      tipos[ k ] = tipos [ k ] + 1
    }
    tipos.eachWithIndex { valor, index ->
      log.debug "tipo ${index}: ${valor} dispositivos"
    }
    log.debug "instalar ${poucos.size()} dispositivos"
    instalarDipositivos( poucos )
}

def wiconnectSwitch( estado ) {
  return estado == 1 ? "on" : "off"
}

// Map contem os dispositivos a serem instalados
def instalarDipositivos( dispositivos ) {
  def child
  dispositivos.each { d ->
    switch( d.tipo ) {
      case 0: // Switch
         child = addChildDevice("WiConnect Switch",
             "${device.deviceNetworkId}-${d.endereco}",
             device.hub.id,
             [completedSetup: true,
              label: "${d.nome}",
              isComponent: false,
              componentLabel: "${d.nome}-WC"
             ]
         )
         child.sendEvent( name: "id",       value: d.id )
         child.sendEvent( name: "nome",     value: d.nome )
         child.sendEvent( name: "endereco", value: d.endereco )
         child.refreshState( getDispositivoState( d ) )
         break

      case 1: // Dimmer
         child = addChildDevice("WiConnect Dimmer",
             "${device.deviceNetworkId}-${d.endereco}",
             device.hub.id,
             [completedSetup: true,
              label: "${d.nome}",
              isComponent: false,
              componentLabel: "${d.nome}-WC"
             ]
         )
         child.sendEvent( name: "id",       value: d.id )
         child.sendEvent( name: "nome",     value: d.nome )
         child.sendEvent( name: "endereco", value: d.endereco )
         child.refreshState( getDispositivoState( d ) )
         break

      case 2: // Shutter
         break

      default:
         log.error "Tipo de dispositivo desconhecido"
    }
  }
}

// d eh um unico dipositivo WiConnect
def getDispositivoState( d ) {
  def myState

  switch( d.tipo ) {
     case 0: // Switch
        myState = d.estado
        break

     case 1: // Dimmer
        myState = d.luminosidade
        break

     case 2: // Shutter
        myState = d.comando
        break

     default:
        log.error "Tipo de dispositivo desconhecido"
    }
    return myState
}


// Map contem os dispositivos WiConnect a serem refreshed
def refreshDispositivos( Map dispositivos ) {
  def childDevices = getChildDevices()
  log.debug "Child ${childDevices.size()}, dispositivos ${dispositivos.size()}"
  if ( childDevices.size() == 0 || dispositivos.size() == 0 )
    return

  dispositivos.each { d ->
    child = childDevices.find{ it.endereco == d.endereco }
    if( child ) {
        log.debug "refreshDispositivos id=${d.id},tipo=${d.tipo},state=${getDispositivoState( d )}"
        child.refreshState( getDispositivoState( d ) )
    }
  }
}

def childOn( id ) {
  log.debug "childOn(${id})"
}

def childOff( id ) {
  log.debug "childOff(${id})"
}

def sendData( message ) {
  log.debug "sendData( ${message} )"

  sendHubCommand(new physicalgraph.device.HubAction(
    [ method: "POST",
      path: "/dispositivos/${message}",
      headers: [ HOST: "192.168.1.201:3000",
                 "Content-Type": "application/json",
                 "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
      ]
   ]
  ))

}

def initializeOld() {
    // create child
    // first check if child device is already installed
    def childDevices = getChildDevices()
    log.trace "Child found ${childDevices.size()}: ${childDevices}"
    //DeviceWrapper addChildDevice(String typeName, String deviceNetworkId, hubId, Map properties)
//    log.debug "getCallBackAddress: " + getCallBackAddress()
    log.debug "device.deviceNetworkId: " + device.deviceNetworkId
    log.debug "getHostAddress: " + getHostAddress()

    if ( childDevices.size() == 0 ) {
      log.trace "Creating Child Sensors"

      def cmd = [
          method: "GET",
          path: "/dispositivos",
          headers: [
            HOST: "192.168.1.201:3000",
//            HOST: getHostAddress(),
            //"http://192.168.1.201:3000",
//            "Content-Type": "text/plain; charset=utf-8",
            "Content-Type": "application/json",
            "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
          ]
      ]
      def hubAction = new physicalgraph.device.HubAction( cmd
//          , null
//          , [callback: deviceDescriptionHandler]
      )
      try {
        sendHubCommand( hubAction )
      }
      catch(Exception e) {
        log.error "Error: ${e}"
      }


      def cmd2 = [
          method: "GET",
          path: "/cenas",
          headers: [
            HOST: "192.168.1.201:3000",
//            HOST: getHostAddress(),
            //"http://192.168.1.201:3000",
//            "Content-Type": "text/plain; charset=utf-8",
            "Content-Type": "application/json",
            "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
          ]
      ]
      def hubAction2 = new physicalgraph.device.HubAction( cmd2
          , null
          , [callback: deviceDescriptionHandler]
      )
      try {
        sendHubCommand( hubAction2 )
      }
      catch(Exception e) {
        log.error "Error 2: ${e}"
      }

      def cmd3 = [
          method: "GET",
          headers: [
            HOST: "192.168.1.201:10207"//,
//            HOST: getHostAddress(),
            //"http://192.168.1.201:3000",
//            "Content-Type": "text/plain; charset=utf-8",
//            "Content-Type": "application/json",
//            "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
          ]
      ]
      def hubAction3 = new physicalgraph.device.HubAction( cmd3
          , null
          , [callback: wcFeedbackHandler]
      )
      try {
        sendHubCommand( hubAction3 )
      }
      catch(Exception e) {
        log.error "Error 2: ${e}"
      }




/*      try {
        names.each { name ->
            addChildDevice("Empty Contact Sensor",
                           "${device.deviceNetworkId}-${name}",
                           device.hub.id,
                           [completedSetup: true, label: "${name}",
                            isComponent: false,   componentLabel: "${name}"
                           ]
            )
        }
      }
      catch(Exception e) {
        log.error "Error: ${e}"
      }
*/      log.trace "Child created"
    }
    else {
      log.trace "Child already exists"
    }
}

def parse(description) {
//    log.debug "parse(${description})"
    def msg = parseLanMessage(description)
    log.debug "parse(${msg})"

//    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
//    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
//    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
    log.debug "status: ${status}"
    log.debug "body: ${body}"

    return

    //log.debug "json: ${json}"
    def jsonSlurper = new JsonSlurper()
    if( ! body )
      return;
    def dispositivos = jsonSlurper.parseText( body )
    dispositivos = dispositivos.dispositivos
    if( ! dispositivos ) {
      log.debug "Outra coisa"
      return;
    }

    def len = dispositivos.size()
    log.debug "encontrados ${len} dispositivos"
    def i
    def tipos = [0, 0, 0] as ArrayList
    for(i=0; i<len; i++) {
      def d = dispositivos[ i ]
//      log.debug "d: ${d.id},${d.nome},${d.endereco},${d.tipo}"
      int k = d.tipo
//      log.debug "k = " + k
      tipos[ k ] = tipos [ k ] + 1
//      log.debug "tipos = " + tipos
    }
    tipos.eachWithIndex { valor, index ->
      log.debug "tipo ${index}: ${valor} dispositivos"
    }
}

// gets the address of the Hub
//private getCallBackAddress() {
//    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
//}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}



def wcFeedback() {
  log.debug "wcFeedback()"
  def cmd = [
      method: "GET",
      headers: [
        HOST: "192.168.1.201:10207"//,
//            HOST: getHostAddress(),
        //"http://192.168.1.201:3000",
//            "Content-Type": "text/plain; charset=utf-8",
//        "Content-Type": "application/json",
//        "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
      ]
  ]
  def hubAction = new physicalgraph.device.HubAction( cmd
//          , null
//          , [callback: wcFeedbackHandler]
  )
  try {
    sendHubCommand( hubAction )
  }
  catch(Exception e) {
    log.error "Error: ${e}"
  }
}

void wcFeedbackHandler(physicalgraph.device.HubResponse hubResponse) {
    def body = hubResponse.body
    log.info "feedback hubResponse: ${body}"
}

def lanResponseHandler(evt) {
	log.debug "entering lanResponceHandler"
  log.debug ""+evt.properties

}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
    def body = hubResponse.body
    log.debug "hubResponse: ${body}"
}


def refresh() {
    log.debug "refresh()"

    initialize()
    refreshChild()
}

def refreshChild() {
    log.debug "refreshChild()"
    def childDevices = getChildDevices()
    childDevices.each { child ->
      child.refresh()
    }
}

// Do not use/define getSettings() since it seems to be ST reserved
def getValidatedSettings() {
  return  ["angleFromNorth":   (settings.angleFromNorth   ?: 0),
           "angleOfIncidence": (settings.angleOfIncidence ?: 45)
          ]
}


def boolContact( boolean b )       { return ( b ? "open" : "closed") }
def boolMotion( boolean b )        { return ( b ? "active" : "inactive") }
def boolAcceleration( boolean b )  { return ( b ? "active" : "inactive") }
def sendEvents( Map e ) { e.each { key, value -> sendEvent( name: key, value: value ) } }

def childSensorValues( Map attr ) {
  // Datetime as string
  def t = [
      sunrise:       device.currentValue("sunrise"),
      midmorning:    device.currentValue("midmorning"),
      noon:          device.currentValue("noon"),
      midafternoon:  device.currentValue("midafternoon"),
      sunset:        device.currentValue("sunset"),
      nadir:         device.currentValue("nadir"),
      now:           attr.lastUpdatedSunPosition
  ]
  //log.debug "Times as string: ${t}"
  def childSensors = [
      SunNorth:          attr.sun_north,
      SunWest:           attr.sun_west,
      SunSouth:          attr.sun_south,
      SunEast:           attr.sun_east,
      SunEarlyMorning:   (t.sunrise      <= t.now && t.now <= t.midmorning),
      SunLateMorning:    (t.midmorning   <= t.now && t.now <= t.noon),
      SunEarlyAfternoon: (t.noon         <= t.now && t.now <= t.midafternoon),
      SunLateAfternoon:  (t.midafternoon <= t.now && t.now <= t.sunset),
      SunEarlyNight:     (t.sunset       <= t.now && t.now <= t.nadir),
      SunLateNight:      (t.nadir        <= t.now && t.now <= t.sunrise)
  ]
  return childSensors
}

def sendEventsChild( Map sensors ) {
  def childDevices = getChildDevices()
  def child, contactValue
  def contacts = [:]
  sensors.each { name, value ->
    child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${name}" }
    if( child ) {
      contactValue = boolContact( value )
      contacts << [ "${child}":  contactValue ]
      // sensors
      child.sendEvent( name: "contact", value: contactValue )
    }
  }
  log.debug "sendEvent to child: ${contacts}"
}

def formatDate( date ) {
  return date.format("yyyy-MM-dd HH:mm:ssZ", location.timeZone )
}

def formatDates ( dates ) {
  def result = [:]
  dates.each { key, value ->
        result[key] = formatDate( value )
  }
  return result
}
//-----------------------------------------------------------------------
// Computes compass arithmetic (in degrees)
double compassArithmetic( double expression ) {
  // Make -360 < compass < 360
  double compass = remainder( expression, 360 )

  // Next, correct negative angles to positive ones
  compass = (compass < 0) ? (compass + 360) : (compass)
  return compass
}
//-----------------------------------------------------------------------
// Computes remainder using doubles a and b (a % b)
double remainder( double a, double b ) {
  // Handling negative values
  long sign = (a < 0) ? -1 : 1
  a = Math.abs( a )
  b = Math.abs( b )

  long   q = a / b
  double r = a - q * b
  return ( sign * r )
}
