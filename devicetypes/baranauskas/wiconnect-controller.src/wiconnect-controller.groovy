/**
 *  Copyright 2021 Jose Augusto Baranauskas
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
 *  This code was written for the WiConnect controller distributed by Legrand in Brazil. In this version,
 *  commands (for authentication, obtaining devices, activating devices, etc.) are in Portuguese.
 *
 *  If you have a controller distributed in another language, just change a few lines in the code:
 *  search for "path:" (without quotes) in this PDH as well as on CDH code and replace them with
 *  commands from your controller. Fell free to contact me if you need any help.
 *
 */
metadata {
    definition (
       name: "WiConnect Controller",
       description: "Create Legrand WiConnect child devices",
       version: "1.2 (2021-03-13)",
       namespace: "baranauskas",
       author: "Jose Augusto Baranauskas",
       runLocally: true,
       minHubCoreVersion: '000.021.00001'
    ) {
      capability "Actuator"
      capability "Sensor"

      // On forces a full update (add, delete and remove) on devices
      // just like updating settings
      capability "Switch"

      // Open if isAuthenticated && Connection is okay
      capability "Contact Sensor"

      // Number of switches and dimmers ON
      capability "Air Quality Sensor"

//        capability "Motion Sensor"
//        capability "Acceleration Sensor"
//        capability "Infrared Level"
//        capability "Health Check"
      capability "Refresh"
    }

    preferences {
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
//----------------------------------------------------------------------
// Core methods
//----------------------------------------------------------------------
import groovy.json.JsonSlurper

def installed() {
    updated()
}

def uninstalled() {
    log.debug "uninstalled()"
    unschedule()
}

def updated() {
    def show = settings.findAll{ it.key != 'serverPassword' }
    log.debug "updated() with ${show}"
    unschedule()
    autenticar()

    // Schedules
    // Update (refresh only) switches, dimmers and shutters
    runEvery5Minutes( refresh )

    // Update (add/remove/refresh) devices to keep in sync with WiConnect
    int ss = 1 + 58 * Math.random()
    int mm = 1 +  5 * Math.random()
    def cronString = "${ss} ${mm} 0 1/1 * ? *"
    log.debug "cron schedule: ${cronString}"
    schedule( cronString, refreshAll )
}

def refresh() {
    log.debug "refresh()"
    connectionState()
    if( state.continueRefreshAll )
      refreshAll()
    else
      getDispositivos()
}

def parse( description ) {
    log.debug "parse()"
    def msg = parseLanMessage(description)
    def status = msg.status
    log.debug "status = ${status}"
}
//----------------------------------------------------------------------
// Switch
//----------------------------------------------------------------------
def on() {
    sendEvent( name:"switch", value: "on" )
    autenticar()
    sendEvent( name:"switch", value: "off" )
}

def off() {
    sendEvent( name:"switch", value: "off" )
}
//----------------------------------------------------------------------
// WiConnect autenticar and refreshAll
//----------------------------------------------------------------------
def autenticar() {
    log.debug "autenticar()"
    resetState()
    if( ! isSettingsOk() ) {
      log.debug "Please check your settings"
      return
    }
    def cmd = mapOfAutenticarCmd()
    //  log.debug "cmd: ${cmd}"
    state.isConnected = false
    sendCmd( cmd,  refreshAllHandler )
}

def refreshAll() {
    log.debug "refreshAll()"
    if( ! state.isAuthenticated ) {
      log.debug "Not authenticated, please check your settings"
      return
    }
    if( ! state.isConnected ) {
      log.debug "No connection, please check your network"
    }

    def cmd = mapOfAutenticarCmd()
    //  log.debug "cmd: ${cmd}"
    state.isConnected = false
    sendCmd( cmd,  refreshAllHandler )
}

def refreshAllHandler( physicalgraph.device.HubResponse hubResponse ) {
    state.isConnected = true

    def status = hubResponse.status
    connectionState( statusOk( status ) )
    if( ! statusOk( status ) ) {
      log.debug "refreshAllHandler status not okay"
      return
    }

    def body = hubResponse.body
    def jsonSlurper = new JsonSlurper()
    def response = jsonSlurper.parseText( body )

    if ( ! state.isAuthenticated ) {
       def token = response.token
       def basic = "Basic " + base64("token:" + response.token)
       authenticateState( token, basic )
       //    log.debug "Token: ${state.token}, Basic: ${state.basic}"
    }

    // notar que resposta /autenticar eh diferente da resposta /dispositivos
    // mas ambas incluem os dipositivos ao devolver o resultado
    // pelo sendHubCommand

    // atualizar completamente os dispositivos
    refreshComplete( response.dispositivos, response.cenas, response.macros )
}

def mapOfAutenticarCmd() {

    def user = [ nome:  "${settings.serverUser}",
                 senha: "${md5( settings.serverPassword )}"
    ]
    def theHeader = [ HOST: "${settings.serverIP}:${settings.serverPort}",
                      "Content-Type": "application/json",
                      "Connection": "Keep-Alive"
    ]
    def cmd = [ method: "POST",
                path: "/autenticar",
                headers: theHeader,
                body: user
    ]
    return cmd
}
//----------------------------------------------------------------------
def refreshComplete( dispositivos, scenes, macros ) {

    dispositivos = coerceToString( dispositivos, "endereco")
    def switches = dispositivos.findAll { it.tipo == 0 }
    def dimmers  = dispositivos.findAll { it.tipo == 1 }
    def shutters = dispositivos.findAll { it.tipo == 2 }

    // Add endereco into scenes and macros
    scenes.each { s -> s["endereco"] = "Scene-" + s["id"] }
    macros.each { m -> m["endereco"] = "Macro-" + m["id"] }

    // Broadcast
    def broadcast    = [
      [id: 0, nome: device.label + " Off", endereco: "broadcast/0"],
      [id: 1, nome: device.label + " On",  endereco: "broadcast/1"]
    ]

    scenes    = coerceToString( scenes, "endereco")
    macros    = coerceToString( macros, "endereco")
    broadcast = coerceToString( broadcast, "endereco")

    log.debug( device.label + " has ${switches.size()} switches, ${dimmers.size()} dimmers, ${shutters.size()} shutters"
            + ", ${scenes.size()} scenes, ${macros.size()} macros, ${broadcast.size()} broadcasts"
    )
    onDevicesState( countOnDevices( dispositivos ) )

    def maxAllowed = 10
    def totalAllowed = maxAllowed
    def types = mapOfDeviceTypes()
    totalAllowed = addRemoveRefreshDevices( switches,  types["switch"],    totalAllowed )
    totalAllowed = addRemoveRefreshDevices( dimmers,   types["dimmer"],    totalAllowed )
    totalAllowed = addRemoveRefreshDevices( shutters,  types["shutter"],   totalAllowed )
    totalAllowed = addRemoveRefreshDevices( scenes,    types["scene"],     totalAllowed )
    totalAllowed = addRemoveRefreshDevices( macros,    types["macro"],     totalAllowed )
    totalAllowed = addRemoveRefreshDevices( broadcast, types["broadcast"], totalAllowed )
    state.continueRefreshAll = ( totalAllowed == 0 )
    if( totalAllowed == 0 ) {
      log.debug "Added/deleted ${maxAllowed} devices. Please, be patient... scheduled another run to add/remove more child devices"
    }
    else {
      log.debug "Finished adding/deleting the last ${maxAllowed - totalAllowed} devices"
    }
}

def myRunIn( seconds, function ) {
    def now = new Date()
    def runTime = new Date(now.getTime() + (seconds * 1000))
	  runOnce( runTime, function ) // runIn isn't reliable, use runOnce instead
//    runOnce(runTime, function, [overwrite: false] ) // runIn isn't reliable, use runOnce instead
//    runIn( seconds, function, [overwrite: false] )
}

def coerceToString( dispositivos, String field ) {
  dispositivos.each { d ->
    d[ field ] = ( d[ field ] as String )
  }
  return dispositivos
}

Map mapOfDeviceTypes() {
  def types = [ switch:    "WiConnect Switch",
                dimmer:    "WiConnect Dimmer",
                shutter:   "WiConnect Shutter",
                scene:     "WiConnect Scene",
                macro:     "WiConnect Macro",
                broadcast: "WiConnect Broadcast"
  ]
  return types
}

def countOnDevices( dispositivos ) {
  def switchesOn = dispositivos.findAll{ it.tipo == 0 && it.estado == 1}.size()
  def dimmersOn  = dispositivos.findAll{ it.tipo == 1 && it.luminosidade > 0}.size()
//  log.debug "Switches on: ${switchesOn}, Dimmers on: ${dimmersOn}"
  return switchesOn + dimmersOn
}
//----------------------------------------------------------------------
// Refresh Simple
//----------------------------------------------------------------------
def getDispositivos() {
    if( ! state.isAuthenticated ) {
      log.debug "Not authenticated, please check your settings"
      return
    }
    if( ! state.isConnected ) {
      log.debug "No connection, please check your network"
    }
    def theHeader = [ HOST: "${settings.serverIP}:${settings.serverPort}",
                      "Content-Type": "application/json",
                      "Authorization": "${state.basic}"
    ]
    def cmd = [ method: "GET",
                path: "/dispositivos",
                headers: theHeader
    ]
  //  log.debug "cmd: ${cmd}"
    state.isConnected = false
    sendCmd( cmd, getDispositivosHandler )
}

def getDispositivosHandler(physicalgraph.device.HubResponse hubResponse) {
    //  log.debug "getDispositivosHandler"
    state.isConnected = true

    def status = hubResponse.status
    def dispositivos = []
    connectionState( statusOk( status ) )
    if( ! statusOk( status ) ) {
      log.debug "hubResponse not okay"
      return
    }
    def body = hubResponse.body
    def jsonSlurper = new JsonSlurper()
    def response = jsonSlurper.parseText( body )
    dispositivos = response.dispositivos
    dispositivos = coerceToString( dispositivos, "endereco")

    //  def switches = dispositivos.findAll { it.tipo == 0 }
    //  def dimmers  = dispositivos.findAll { it.tipo == 1 }
    //  def shutters = dispositivos.findAll { it.tipo == 2 }
    //  log.debug "Got ${switches.size()} switches, ${dimmers.size()} dimmers, ${shutters.size()} shutters"
    refreshSimple( dispositivos )
}

def refreshSimple( dispositivos ) {
  def types = mapOfDeviceTypes()
  def deviceTypes = [ types["switch"], types["dimmer"], types["shutter"] ]
  def childDevices = getChildDevices().findAll{ it.typeName in deviceTypes }
  def child
  def refreshed = 0
  onDevicesState( countOnDevices( dispositivos ) )
  dispositivos.each { d ->
    child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
    if( child ) {
      child.refreshState( d )
      refreshed++
    }
  }
  log.debug "refreshSimple ${refreshed} refreshed devices from ${childDevices.size()} childDevices"
}
//----------------------------------------------------------------------
// Set Utilities
//----------------------------------------------------------------------
Map makeActionMap( dispositivos, String deviceType, String field ) {
  def dSet = dispositivos[ field ] as Set
//  def dSet = makeDevicesSet( dispositivos, field )
  def cSet = makeChildDevicesSet( deviceType, field )
//  log.debug "dSet: ${dSet}, cSet: ${cSet}"
  def m = [ add:     dSet.minus( cSet ),
            delete:  cSet.minus( dSet ),
            refresh: dSet.intersect( cSet )
          ]
  log.debug "makeActionMap ${deviceType}: add=${m.add.size()}, refresh=${m.refresh.size()}, delete=${m.delete.size()}"
  return m
}

Set makeDevicesSet( dispositivos, String field ) {
  def dArray = []
  dispositivos.each { d ->
    dArray << ( d[ field ] as String )
  }
  return dArray as Set
}

Set makeChildDevicesSet( String deviceType, String field ) {
  def childDevices = getChildDevices().findAll { it.typeName == deviceType }
  def childArray = []
  childDevices.each { child ->
     childArray << ( child.currentValue( field ) as String )
  }
//  log.debug " makeChildDevicesSet: size=${childArray.size()}, array=size=${childArray}"
  return childArray as Set
}
//----------------------------------------------------------------------
// Add, remove and refresh devices
//----------------------------------------------------------------------
// map, device type, device NetworkId field on map,
def addRemoveRefreshDevices( dispositivos, String deviceType, int totalAllowed ) {
  if( dispositivos ) {
    def actions = makeActionMap( dispositivos, deviceType, "endereco" )
//    def instalar = [ 727623301, 727618602, 727618301, 727618302, 727621301, 727623401, 727600301, 727567901 ]
//    instalar.each { it = (it as String) }
//    addDevices( dispositivos, instalar )
    totalAllowed = addDevices( dispositivos, deviceType, actions.add, totalAllowed )
    totalAllowed = deleteDevices( dispositivos, deviceType, actions.delete, totalAllowed )
    refreshDevices( dispositivos, deviceType, actions.refresh )
  }
  return totalAllowed
}

def refreshDevices( dispositivos, String deviceType, refreshSet ) {
  def childDevices = getChildDevices().findAll{ it.typeName == deviceType }
  def d, child
  Set refreshedSet = []
  refreshSet.each { endereco ->
    d = dispositivos.find{ it.endereco == endereco }
    if( d ) {
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
      if( child ) {
        child.initialState( d )
        refreshedSet << endereco
      }
    }
  }
//  log.debug "Refresh ${deviceType}: ${refreshSet.size()}, refreshed ${refreshedSet.size()}, unrefreshed ${refreshSet.minus( refreshedSet )}"
  return refreshedSet.size()
}

def deleteDevices( dispositivos, String deviceType, deleteSet, int totalAllowed ) {
  def childDevices = getChildDevices().findAll{ it.typeName == deviceType }
  def d, child
  Set deletedSet = []
  deleteSet.each { endereco ->
    child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${endereco}" }
    if( child ) {
      if( totalAllowed > 0 ) {
        try {
          deleteChildDevice( child.deviceNetworkId )
          deletedSet << endereco
          totalAllowed--
        }
        catch (e) {
          log.debug "Error deleting ${child.deviceNetworkId}: ${e}"
        }
      }
      else {
//        log.debug "Delete ${deviceType}: ${deleteSet.size()}, deleted ${deletedSet.size()}, undeleted ${deleteSet.minus( deletedSet )}"
        return totalAllowed
      }
    }
  }
//  log.debug "Delete ${deviceType}: ${deleteSet.size()}, deleted ${deletedSet.size()}, undeleted ${deleteSet.minus( deletedSet )}"
  return totalAllowed
}

def addDevices( dispositivos, String deviceType, addSet, int totalAllowed ) {
  def child, d, dth
  Set addedSet = []
  addSet.each{ endereco ->
    d = dispositivos.find{ it.endereco == endereco }
    if( d ) {
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
      if( ! child ) {
        if( totalAllowed > 0 ) {
          try {
            child = addChildDevice( deviceType,
               "${device.deviceNetworkId}-${d.endereco}",
               device.hub.id,
               [completedSetup: true,
                name: "${d.nome}",
                label: "${d.nome}",
                isComponent: false,
                componentLabel: "${d.nome}"
               ]
            )
            child.initialState( d )
            addedSet << endereco
            totalAllowed--
           }
           catch (e) {
             log.debug "Error adding ${d.nome}: ${e}"
           }
        }
        else
          return totalAllowed
      }
    }
  }
//  log.debug "Add ${deviceType}: ${addSet.size()}, added ${addedSet.size()}, unadded ${addSet.minus( addedSet )}"
  return totalAllowed
}
//----------------------------------------------------------------------
// WiConnect Utilities
//----------------------------------------------------------------------
// Status de retorno dos comandos GET e POST da controladora WiConnect
def Boolean statusOk( status )
{
  if ((status == 204) || (status == 200) || (status == 202)) {
    return true;
  }
  return false;
}
//----------------------------------------------------------------------
// SmartThings Utilities
//----------------------------------------------------------------------
def getStateValue( key ) {
    return state[key]
}

def boolContact( boolean b )       { return ( b ? "open" : "closed") }
//def boolMotion( boolean b )        { return ( b ? "active" : "inactive") }

//----------------------------------------------------------------------
// State methods
//----------------------------------------------------------------------
def resetState() {
    state.isAuthenticated = false
    state.isConnected = false
    state.continueRefreshAll = false
    state.token = "*"
    state.basic = "**"
    connectionState( state.isAuthenticated )
    onDevicesState( 0 )
}

def authenticateState( token, basic ) {
    state.isAuthenticated = true
    state.continueRefreshAll = false
    state.token = token
    state.basic = basic
    connectionState( state.isAuthenticated )
}

def connectionState( b = null ) {
//    log.debug "connectionState(${b}), state.isConnected=${state.isConnected}, state.isAuthenticated=${state.isAuthenticated}"
    b = b ?: state.isConnected
    b = state.isAuthenticated && b
    state.isConnected = b
    sendEvent( name: "contact", value: boolContact( b ) )
    sendEvent( name: "switch", value: "off" )
}

def onDevicesState( devicesOn ) {
  def value = state.isAuthenticated ? devicesOn : 0
  sendEvent( name: "airQuality", value:  value )
}
//----------------------------------------------------------------------
private String md5(String str) {
	def digest = java.security.MessageDigest.getInstance("MD5").digest(str.bytes).encodeHex()
	return digest as String
}

private String base64(String str) {
  def digest = str.bytes.encodeBase64()
  return digest as String
}

private Boolean isIP(String str)
{   try
    {
       String[] parts = str.split("\\.");
       if (parts.length != 4) return false;
       for (int i = 0; i < 4; ++i)
       {
         int p = Integer.parseInt( parts[i] );
         if (p > 255 || p < 0) return false;
       }
       return true;
    } catch (Exception e)
    {
        return false;
    }
}

private Boolean isPort( p )
{   if (p <= 0) return false;
    return true;
}

private Boolean isUser(String str)
{  return str.size() >= 3
}

private Boolean isPassword(String str)
{  return str.size() >= 3
}

private Boolean isSettingsOk()
{  return isIP( settings.serverIP ) &&
          isPort( settings.serverPort ) &&
          isUser( settings.serverUser ) &&
          isPassword ( settings.serverPassword )
}

def sendCmd( cmd, handler ) {
//  log.debug "sendCmd(${cmd})"
  def hubAction = new physicalgraph.device.HubAction(
        cmd,
        null,
        [callback: handler]
  )
  try {
      sendHubCommand( hubAction )
  }
  catch (Exception e) {
      log.error "Error sendHubCommand: ${e}"
  }
}
