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
 * This DH is used as a childDevice by Parent DH WiConnect Controller
 *
 */
 metadata {
  definition (
    name: "WiConnect Shutter",
    version: "1.1 (2021-02-28)",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    runLocally: true,
    minHubCoreVersion: '000.021.00001',
    vid:"generic-shade"
  ) {
      capability "Refresh"
      capability "Switch"
//      capability "Switch Level"
      capability "Window Shade"
      capability "Window Shade Preset"

      // WiConnect info about device
      attribute "id",               "number"
      attribute "endereco",         "string"
      attribute "nome",             "string"
  }
}
//----------------------------------------------------------------------
// Core methods
//----------------------------------------------------------------------
//import groovy.json.JsonSlurper

def installed() {
//  log.debug "child ${device.label} installed"
}

def open() {
    on()
}

def on() {
    def endereco = device.currentValue("endereco")
    sendData("/dispositivos/${endereco}/acionar/2", onHandler )
}

def close() {
    off()
}

def off() {
  def endereco = device.currentValue("endereco")
  sendData("/dispositivos/${endereco}/acionar/3", offHandler )
}

def pause() {
  def endereco = device.currentValue("endereco")
  sendData("/dispositivos/${endereco}/acionar/1", pauseHandler )
}

def presetPosition() {
   setLevel( 50 )
}

def setLevel(value, rate = null) {
  def endereco = device.currentValue("endereco")
  value = ((int) (value / 10)) * 10
  if( value < 20 ) {
    off()
  }
  else {
    state.level = value
    sendData("/dispositivos/${endereco}/acionar/${value}", setLevelHandler )
  }
}

def refresh() {
    def endereco = device.currentValue("endereco")
    log.debug "child.refresh() endereco=${endereco}: please refresh parent device"
}

def parse(String description) {
    def endereco = device.currentValue("endereco")
    log.debug "child.parse endereco=${endereco} description $description"
}
//----------------------------------------------------------------------
//
//----------------------------------------------------------------------
def onHandler(physicalgraph.device.HubResponse hubResponse) {
    def status = hubResponse.status
//    def body = hubResponse.body
    if ( parent.statusOk( status ) ) { // body == Accepted
      sendEvent( name: "switch", value: "on")
      sendEvent( name: "level", value: 100 )
      sendEvent(name: "windowShade", value: "open")
    }
    else
      log.error "shutter on failed"
}

def offHandler(physicalgraph.device.HubResponse hubResponse) {
    def status = hubResponse.status
//    def body = hubResponse.body
    if ( parent.statusOk( status ) ) { // body == Accepted
      sendEvent( name: "switch", value: "off")
      sendEvent( name: "level", value: 0 )
      sendEvent(name: "windowShade", value: "closed")
    }
    else
      log.error "switch off failed"
}

def pauseHandler(physicalgraph.device.HubResponse hubResponse) {
    def status = hubResponse.status
//    def body = hubResponse.body
    if ( parent.statusOk( status ) ) { // body == Accepted
      sendEvent( name: "level", value: "50")
      sendEvent(name: "windowShade", value: "partially open")
    }
    else
      log.error "shutter pause failed"
}


def setLevelHandler(physicalgraph.device.HubResponse hubResponse) {
    def status = hubResponse.status
//    def body = hubResponse.body
    if ( parent.statusOk( status ) ) { // body == Accepted
      sendEvent( name: "switch", value: "on")
      sendEvent( name: "level", value: "${state.level}")
    }
    else
      log.error "switch level failed"
}
//----------------------------------------------------------------------
//
//----------------------------------------------------------------------
// * Window Shade Capability standardizes:  (these should not be changed, except by SmartThings capabilities updates)
// *	- windowShade: unknown, closed, open, partially open, closing, opening
// *	- Commands:  open(), close(), presetPosition()
def initialState( Map d ) {
    sendEvent( name: "id",       value: d.id )
    sendEvent( name: "nome",     value: d.nome )
    sendEvent( name: "endereco", value: d.endereco )
    device.name = d.nome
    device.label = d.nome
//    device.deviceNetworkId = parent.deviceNetworkId + "-" + d.endereco
    refreshState( d )
}

def refreshState( Map d ) {
    switch( d.comando ) {
      case 1: // Stop/pause
//        sendEvent( name: "switch", value: "off" )
        sendEvent( name: "level",  value: 50 )
        sendEvent( name: "windowShade", value: "partially open")
        break
      case 2: // Up/open
        sendEvent( name: "switch", value: "on" )
        sendEvent( name: "level",  value: 100 )
        sendEvent( name: "windowShade", value: "open")
        break
      case 3: // Down/close
        sendEvent( name: "switch", value: "off" )
        sendEvent( name: "level",  value: 0 )
        sendEvent( name: "windowShade", value: "closed")
        break
    }
}

def wiconnectShutter( comando ) {
  return comando == 2 ? "on" : "off"
}
//----------------------------------------------------------------------
// SmartThings Utilities
//----------------------------------------------------------------------
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

private sendData( message, handler ) {
//  log.debug "sendData( ${message} )"
  if( ! parent.getStateValue("isAuthenticated") ) {
    log.debug "Not authenticated, please check your settings or connection"
    return
  }
  def theHeader = [ HOST: "${parent.settings.serverIP}:${parent.settings.serverPort}",
                    "Content-Type": "application/json",
                    "Authorization": "${parent.getStateValue('basic')}"
  ]
  def cmd = [ method: "POST",
              path: "${message}",
              headers: theHeader
  ]
//  log.debug "cmd: ${cmd}"
  sendCmd( cmd, handler )
}
