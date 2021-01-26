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
 * This DH is used as a childDevice by Parents DHs ou SmartApps
 */
 metadata {
  definition (
    name: "WiConnect Switch",
    version: "2.0 (2021-01-26)",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    runLocally: true,
    minHubCoreVersion: '000.021.00001',
    vid:"generic-switch"
  ) {
      capability "Actuator"
      capability "Sensor"
      capability "Switch"
      capability "Refresh"

      attribute "id",               "number"
      attribute "endereco",         "string"
      attribute "nome",             "string"

  }
}
import groovy.json.JsonSlurper

def installed() {
  def id = device.currentValue("id")
  log.debug "child installed id ${id}"
}

def on() {
    def id = device.currentValue("id")
    log.debug "child on ${id}"
    def endereco = device.currentValue("endereco")
//    sendEvent( name: "switch", value: "on")
    sendData("${endereco}/acionar/1", "on")
//    parent.childOn( id )
}

def off() {
  def id = device.currentValue("id")
  log.debug "child off ${id}"
  def endereco = device.currentValue("endereco")
//  sendEvent( name: "switch", value: "off")
  sendData("${endereco}/acionar/0", "off")
//  parent.childOff( id )
}

def sendData( message, event ) {
  log.debug "sendData( ${message}, ${event} )"

  def cmd = [
    method: "POST",
    path: "/dispositivos/${message}",
    headers: [ HOST: "${parent.settings.serverIP}:${parent.settings.serverPort}",
               "Content-Type": "application/json",
               "Authorization": "${parent.getStateValue('token')}"
    ]
  ]
  log.debug "sendData cmd ${cmd}"
  def hubAction
  if( event == "on" )
    hubAction = new physicalgraph.device.HubAction( cmd,
                null,
                [callback: sendDataHandlerOn ] )
  else
    hubAction = new physicalgraph.device.HubAction( cmd,
                null,
                [callback: sendDataHandlerOff ] )

  try {
    sendHubCommand( hubAction )
  }
  catch(Exception e) {
    log.error "sendHubCommand Error: ${e}"
  }
}

void sendDataHandlerOn(physicalgraph.device.HubResponse hubResponse) {
    log.debug "sendDataHandlerOn()"
    def status = hubResponse.status
    def body = hubResponse.body
    log.debug "hr  = ${hubResponse}"
    log.debug "status = ${status}"
    log.debug "body = ${body}"
    if ( status == 202 ) // body == Accepted
    {  sendEvent( name: "switch", value: "on")
       log.debug "switch on sucessful"
    }
    else
      log.error "switch on failed"
}

void sendDataHandlerOff(physicalgraph.device.HubResponse hubResponse) {
    log.debug "sendDataHandlerOff()"
    def status = hubResponse.status
    def body = hubResponse.body
    log.debug "hr  = ${hubResponse}"
    log.debug "status = ${status}"
    log.debug "body = ${body}"
    if ( status == 202 ) // body == Accepted
    {  sendEvent( name: "switch", value: "off")
       log.debug "switch off sucessful"
    }
    else
      log.error "switch off failed"
}

//--------------------------------------------------------------------
def refresh() {
  log.debug "CHILD refresh()"
  def id = device.currentValue("id")
  log.debug "id: ${id}"
  log.debug "device.deviceNetworkId: ${device.deviceNetworkId}"
  log.debug "parent.deviceNetworkId: ${parent.deviceNetworkId}"
  log.debug "parent.settings: ${parent.settings}"
  log.debug "parent.state.token: " + parent.getStateValue("token")

  def cmd = [
      method: "GET",
      path: "/dispositivos/${id}",
      headers: [ HOST: "${parent.settings.serverIP}:${parent.settings.serverPort}",
                 "Content-Type": "application/json",
                 "Authorization": "${parent.getStateValue('token')}"
      ]
  ]
  log.debug "refresh cmd ${cmd}"
  def hubAction = new physicalgraph.device.HubAction( cmd,
                  null,
                  [callback: refreshHandler ] )

  try {
    sendHubCommand( hubAction )
  }
  catch(Exception e) {
    log.error "sendHubCommand Error: ${e}"
  }
}

void refreshHandler(physicalgraph.device.HubResponse hubResponse) {
//    log.debug "hubResponse: ${body}"
    log.debug "refreshHandler()"
    log.debug "hr  = ${hubResponse}"
    def status = hubResponse.status
    def body = hubResponse.body
    log.debug "status = ${status}"
    log.debug "body = ${body}"
    // {"id":42,"nome":"Entrada","endereco":727623301,"tipo":0,"status":1,"estado":0,"velocidade":0,"luminosidade":0,"comando":0,"posicao":0}

    def jsonSlurper = new JsonSlurper()
    def d = jsonSlurper.parseText( body )
    if( ! d ) {
      log.error "Nao achei o dispositivo"
      return;
    }
    log.debug "evento (${d.estado}): ${wiconnectSwitch( d.estado )}"
    log.debug "device.label: ${device.label}"
    refreshState( d.estado )
}

def refreshState( estado ) {
    sendEvent( name: "switch", value: wiconnectSwitch( estado ) )
}

def wiconnectSwitch( estado ) {
  return estado == 1 ? "on" : "off"
}

def parse(String description) {
    log.debug "CHILD parse description $description"
    def id = device.currentValue("id")
    log.debug "id: ${id}"
}
