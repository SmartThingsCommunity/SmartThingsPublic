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
       version: "1.0 (2021-02-28)",
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
        capability "Refresh"

        // Open if isAuthenticated && Connection is okay
        capability "Contact Sensor"

//        capability "Motion Sensor"
//        capability "Switch"
//        capability "Acceleration Sensor"
//        capability "Infrared Level"
//        capability "Health Check"

//        command "childOn"
//        command "childOff"
//        command "sendData", ["string"]

//        attribute "altitude",               "number"
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
//----------------------------------------------------------------------
// Core methods
//----------------------------------------------------------------------
import groovy.json.JsonSlurper

def installed() {
    log.debug "installed() with ${settings}"
    updated()
}

def on(){
  sendEvent( name:"switch", value: "on" )
  authenticate()
  sendEvent( name:"switch", value: "off" )
}
def off(){
  sendEvent( name:"switch", value: "off" )
}

def uninstalled() {
    log.debug "uninstalled()"
    unschedule()
}

def updated() {
    log.debug "updated() with ${settings}"
    unschedule()
    authenticate()

    // Schedules
    runEvery5Minutes( refresh )
//    int ss = 1 + 58 * Math.random()
//    def cronString = "${ss} 0 0 1/1 * ? *"
//    log.debug "cron schedule: ${cronString}"
//    schedule( cronString, refresh )
}

def refresh()
{ //log.debug "refresh()"
  getDispositivos()
}
//----------------------------------------------------------------------
// WiConnect autenticacao
//----------------------------------------------------------------------
def authenticate() {
  resetState()
  if( ! isSettingsOk() ) {
    log.debug "Please check your settings"
    return
  }

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
//  log.debug "cmd: ${cmd}"
  sendCmd( cmd,  authenticateHandler )
}

void authenticateHandler(physicalgraph.device.HubResponse hubResponse) {
//    log.debug "hubResponse: ${body}"
//    log.debug "authenticateHandler()"
    def status = hubResponse.status
    if( ! statusOk( status ) ) {
      log.debug "authenticateHandler status not okay"
      return
    }

    def body = hubResponse.body
    def jsonSlurper = new JsonSlurper()
    def response = jsonSlurper.parseText( body )
    def token = response.token
    def basic = "Basic " + base64("token:" + response.token)

    authenticateState( token, basic )
    // notar que resposta /autenticar eh diferente da resposta /dispositivos
    // mas ambas incluem os dipositivos ao devolver o resultado
    // pelo sendHubCommand
    log.debug "authenticateHandler encontrou ${response.size()} respostas"
    log.debug "Token: ${state.token}, Basic: ${state.basic}"

    // atualizar completamente os dispositivos
    def dispositivos = responseToDispositivos( hubResponse )
    addRemoveRefreshDevices( dispositivos )
}
//----------------------------------------------------------------------
def getDispositivos() {
  if( ! state.isAuthenticated ) {
    log.debug "Not authenticated, please check your settings or connection"
    return
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
  sendCmd( cmd, getDispositivosHandler )
}

def getDispositivosHandler(physicalgraph.device.HubResponse hubResponse) {
  def dispositivos = responseToDispositivos( hubResponse )
  def childDevices = getChildDevices()
  def d
  def refreshed = 0
  childDevices.each { child ->
    d = dispositivos.find{ it.endereco == (child.currentValue("endereco") as Integer)}
    if( d ) {
      child.refreshState( getDispositivoState( d ) )
      refreshed++
    }
  }
  log.debug "getDispositivosHandler refreshed ${refreshed} devices from ${childDevices.size()} childDevices"
}

def responseToDispositivos(physicalgraph.device.HubResponse hubResponse) {
  def status = hubResponse.status
  def dispositivos = []
  connectionState( statusOk( status ) )
  if( ! statusOk( status ) ) {
    log.debug "hubResponse not okay"
    return dispositivos
  }
  def body = hubResponse.body
  def jsonSlurper = new JsonSlurper()
  def response = jsonSlurper.parseText( body )
  dispositivos = response.dispositivos

  // Contar dispositivos encontrados
  def len = dispositivos.size()
  def i
  def tipos = [0, 0, 0] as ArrayList
  for(i=0; i<len; i++) {
    def d = dispositivos[ i ]
    //  log.debug "d: ${d.id},${d.nome},${d.endereco},${d.tipo}"
    int k = d.tipo
    tipos[ k ] = tipos [ k ] + 1
  }
  Map mapTipos = [:]
  tipos.eachWithIndex { valor, index ->
    mapTipos << ["${index}": "${valor}"]
  }
  log.debug "Encontrados ${len} dispositivos dos tipos ${mapTipos}"
  return dispositivos
}
//----------------------------------------------------------------------
// SmartThings Utilities
//----------------------------------------------------------------------
def getStateValue( key ) {
    return state[key]
}

def boolContact( boolean b )       { return ( b ? "open" : "closed") }
//def boolMotion( boolean b )        { return ( b ? "active" : "inactive") }

def resetState() {
  state.isAuthenticated = false
  state.token = "*"
  state.basic = "**"
  connectionState( state.isAuthenticated )
}

def authenticateState( token, basic ) {
  state.isAuthenticated = true
  state.token = token
  state.basic = basic
  connectionState( state.isAuthenticated )
}

def connectionState( b ) {
  sendEvent( name: "contact", value: boolContact( b ) )
  sendEvent( name: "switch", value: "off" )
}

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

private sendCmd( cmd, handler ) {
//  log.debug "sendCmd(${cmd})"
  def hubAction = new physicalgraph.device.HubAction(
        cmd,
        null,
        [callback: handler]
  )
  try {
      sendHubCommand( hubAction )
  }
  catch(Exception e) {
      log.error "Error sendHubCommand: ${e}"
  }
}
//----------------------------------------------------------------------
// Set Utilities
//----------------------------------------------------------------------
Map makeActionMapAboutDevices( dispositivos ) {
  def dSet = makeDispositivosSet( dispositivos )
  def cSet = makeChildDevicesSet()
  def m = [ add:     dSet.minus( cSet ),
            delete:  cSet.minus( dSet ),
            refresh: dSet.intersect( cSet )
          ]
  log.debug "makeActionMapAboutDevices add=${m.add.size()}, refresh=${m.refresh.size()}, delete=${m.delete.size()}"
  return m
}

Set makeDispositivosSet( dispositivos ) {
  def dArray = []
  dispositivos.each { d ->
     dArray << (d.endereco as Integer)
  }
//  log.debug "getSetDispositivos: size=${dArray.size()}, array=size=${dArray}"
  return dArray as Set
}

Set makeChildDevicesSet() {
  def childDevices = getChildDevices()
  def childArray = []
  childDevices.each { child ->
     childArray << (child.currentValue("endereco") as Integer)
  }
//  log.debug "getSetChild: size=${childArray.size()}, array=size=${childArray}"
  return childArray as Set
}
//----------------------------------------------------------------------
// Add, remove and refresh
//----------------------------------------------------------------------
def addRemoveRefreshDevices( dispositivos ) {
  if( dispositivos ) {
    def actions = makeActionMapAboutDevices( dispositivos )

//    def instalar = [ 727623301, 727618602, 727618301, 727618302, 727621301, 727623401, 727600301, 727567901 ]
//    addDevices( dispositivos, instalar )

    addDevices( dispositivos, actions.add )
    refreshDevices( dispositivos, actions.refresh )
    deleteDevices( dispositivos, actions.delete )
  }
}

def refreshDevices( dispositivos, refreshSet ) {
  def childDevices = getChildDevices()
  def d, child
  def refreshedSet = [] as Set
  refreshSet.each { endereco ->
    d = dispositivos.find{ it.endereco == endereco }
    if( d ) {
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
      if( child ) {
        child.sendEvent( name: "id", value: d.id )
        child.sendEvent( name: "nome", value: d.nome )
        child.refreshState( getDispositivoState( d ) )
        refreshedSet << endereco
      }
    }
  }
  log.debug "Refresh ${refreshSet.size()}, refreshed ${refreshedSet.size()}, unrefreshed ${refreshSet.minus( refreshedSet )}"
}

def deleteDevices( dispositivos, deleteSet ) {
  def childDevices = getChildDevices()
  def d, child
  def deletedSet = [] as Set
  deleteSet.each { endereco ->
    d = dispositivos.find{ it.endereco == endereco }
    if( d ) {
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
      if( child ) {
        try {
          deleteChildDevice( child.deviceNetworkId )
          deletedSet << endereco
        }
        catch (e) {
          log.debug "Error deleting ${child.deviceNetworkId}: ${e}"
        }
      }
    }
  }
  log.debug "Delete ${deleteSet.size()}, deleted ${deletedSet.size()}, undeleted ${deleteSet.minus( deletedSet )}"
}

def getChildDeviceHandler( d ){
  def dth = ""
  switch( d.tipo ) {
    case 0: // Switch
      dth = "WiConnect Switch"
      break
    case 1: // Dimmer
      dth = "WiConnect Dimmer"
      break
    case 2: // Shutter
      dth = "WiConnect Shutter"
      break
  }
  return dth
}

def addDevices( dispositivos, addSet ) {
  def child, d, dth
  def addedSet = [] as Set
  addSet.each{ endereco ->
    d = dispositivos.find{ it.endereco == endereco }
    if( d ) {
      dth = getChildDeviceHandler( d )
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${d.endereco}" }
      if( ! child && dth ) {
        try {
          child = addChildDevice( dth,
             "${device.deviceNetworkId}-${d.endereco}",
             device.hub.id,
             [completedSetup: true,
              name: "${d.nome}",
              label: "${d.nome}",
              isComponent: false,
              componentLabel: "${d.nome}"
             ]
          )
          child.sendEvent( name: "id",       value: d.id )
          child.sendEvent( name: "nome",     value: d.nome )
          child.sendEvent( name: "endereco", value: d.endereco )
          child.refreshState( getDispositivoState( d ) )
          addedSet << endereco
         }
         catch (e) {
           log.debug "Error adding ${d.nome}: ${e}"
         }
      }
    }
  }
  log.debug "Add ${addSet.size()}, added ${addedSet.size()}, unadded ${addSet.minus( addedSet )}"
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

// d eh um unico dipositivo WiConnect
def getDispositivoState( d ) {
  def myState = 0
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
