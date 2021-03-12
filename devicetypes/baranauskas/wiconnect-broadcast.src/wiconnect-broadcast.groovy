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
    name: "WiConnect Broadcast",
    version: "1.0 (2021-03-03)",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    runLocally: true,
    minHubCoreVersion: '000.021.00001',
    vid:"generic-switch"
  ) {
      capability "Refresh"
      capability "Switch"

      // WiConnect info about scene
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

def on(){
  sendEvent( name:"switch", value: "on" )
  def id   = device.currentValue("id")
//  def nome = device.currentValue("nome")
  sendData("/dispositivos/broadcast/${id}", onHandler )
//  /dispositivos/broadcast/0
//  /dispositivos/broadcast/1
}

def off(){
  sendEvent( name:"switch", value: "on" )
}

def refresh() {
    def endereco = device.currentValue("nome")
    log.debug "child.refresh() nome=${nome}: please refresh parent device"
}

def parse(String description) {
    def endereco = device.currentValue("nome")
    log.debug "child.parse nome=${nome} description $description"
}
//----------------------------------------------------------------------
//
//----------------------------------------------------------------------
def onHandler(physicalgraph.device.HubResponse hubResponse) {
//    log.debug "child.onHandler"
    def status = hubResponse.status
//    def body = hubResponse.body
    if ( parent.statusOk( status ) ) // body == Accepted
      sendEvent( name: "switch", value: "off")
    else
      log.error "Scene failed"
}

def initialState( d ) {
    sendEvent( name: "id",       value: d.id )
    sendEvent( name: "nome",     value: d.nome )
    sendEvent( name: "endereco", value: d.endereco )
    device.name = d.nome
    device.label = d.nome
//    device.deviceNetworkId = parent.deviceNetworkId + "-" + d.endereco
    refreshState( d )
}

def refreshState( d ) {
    sendEvent( name: "switch", value: "off" )
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
