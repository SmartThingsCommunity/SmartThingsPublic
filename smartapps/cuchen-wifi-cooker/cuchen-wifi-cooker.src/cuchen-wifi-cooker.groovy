/**
 *  Cuchen Wifi Cooker
 *
 *  Copyright 2015 Cuchen
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
 */
 
import groovy.json.*
import static java.util.Calendar.*

definition(
    name: "Cuchen Wifi Cooker",
    namespace: "cuchen.wifi.cooker",
    author: "Cuchen",
    description: "control your Cuchen Wifi Cooker.",
    category: "My Apps",
    iconUrl: "http://lihom.jc-square.com/cuchen.png",
    iconX2Url: "http://lihom.jc-square.com/cuchen@2x.png"
    //iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    )


preferences {
	def msg = """Tap 'Next' after you have entered in your Cooker credentials.

Once your credentials are accepted, SmartThings will scan your installation for Cooker."""

	page(name: "selectDevices", title: "Connect Your Cuchen Wifi Cooker to SmartThings", install: true, uninstall: true) {
		section("Connected Cooker Credentials") {
			input "cookerId", "text", title: "Enter Cuchen Cooker Id(Mac address)", required: true
			input "password", "password", title: "Enter Cooker Password", required: true
			paragraph msg
		}
	}

	page(name: "chooseCooker", title: "Choose Cooker to Control With SmartThings", content: "initialize")
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    
	initialize()
}

def uninstalled() {
	def preInstalled = getChildDevice(settings.cookerId)
    if (preInstalled) {
	    deleteChildDevice(settings.cookerId)
    }
}

def initialize() {
	def preInstalled = getChildDevice(settings.cookerId)
    if (preInstalled) {
	    deleteChildDevice(settings.cookerId)
    }
    
	// TODO: subscribe to attributes, devices, locations, etc.
    def dev = addChildDevice("cuchen.wifi.cooker.pa-10", "Cuchen Wifi Cooker PA-10", settings.cookerId, null, [cookerId : settings.cookerId, password: settings.password, compeletedSetup: true])
    log.debug "Device commands = " + dev.supportedCommands
    log.debug "Device attrs = " + dev.supportedAttributes
    dev.refreshStatus()
}

// TODO: implement event handlers

def poll(childDevice) {
  log.debug "Executing 'poll'"
  getStatus(settings.cookerId, true)
}

def refreshStatus() {
  def dev = getChildDevice(settings.deviceId)
  refreshStatus(dev, false)
}

def refreshStatus(childDevice, once) {
  log.debug "(app) Executing 'refreshStatus'"
  atomicState.lastStatus = ""
  getStatus(settings.cookerId, once)
}

def cooking(childDevice, params) {
  log.debug "(app) Executing 'cooking'"
  if (!atomicState?.lastStatus?.trim()) {
      atomicState.lastStatus = childDevice.device.currentValue("status")
      atomicState.retry = 0
      atomicState.nextStatus = "cooking"
      sendCommand(settings.cookerId, "cooking", [passwd: settings.password, menu: params.menu, level: params.level, hour: params.hour, min: params.min])
  }
}

def warm(childDevice) {
  log.debug "(app) Executing 'warm'"
  if (!atomicState?.lastStatus?.trim()) {
      atomicState.lastStatus = childDevice.device.currentValue("status")
      atomicState.retry = 0
      atomicState.nextStatus = "warm"
      sendCommand(settings.cookerId, "setWarm", [passwd: settings.password, type:0, isPlus:true, tempr:1])
  }
}

def reheat(childDevice) {
  log.debug "(app) Executing 'reheat'"
  if (!atomicState?.lastStatus?.trim()) {
      atomicState.lastStatus = childDevice.device.currentValue("status")
      atomicState.retry = 0
      atomicState.nextStatus = "reheat"
      sendCommand(settings.cookerId, "reheat", [passwd: settings.password])
  }
}

def autoClean(childDevice) {
  log.debug "(app) Executing 'autoClean'"
  if (!atomicState?.lastStatus?.trim()) {
      atomicState.lastStatus = childDevice.device.currentValue("status")
      atomicState.retry = 0
      atomicState.nextStatus = "auto cleaning"

      //def now = Calendar.instance
      //sendCommand(settings.cookerId, "setTime", [passwd: settings.password, mon: now[MONTH], day: now[DATE], hour: now[HOUR], min: now[MINUTE], isAm: (now[AM_PM] == AM)])
      sendCommand(settings.cookerId, "autoClean", [passwd: settings.password])
  }
}

def cancel(childDevice) {
  log.debug "(app) Executing 'cancel'"
  if (!atomicState?.lastStatus?.trim()) {
      atomicState.lastStatus = childDevice.device.currentValue("status")
      atomicState.retry = 0
      atomicState.nextStatus = "stand by"
      sendCommand(settings.cookerId, "cancel", [passwd: settings.password])
  }
}

// function for axon
def retryMax() {
	return 5;
}

def getRequest(path, deviceId, apiName, params) {
  def axonServer = "http://axon-iot.com"
  def axonSvcKey = "1422266982285"
  def axonApiKey = "cbce8f97dd26fca681151996882672b129351ce6923259e022b785beb7d7de"
  def axonClientId = "947D9B3E-2CB5-4B26-99D9-F300F7171A05"

  def paramJson = JsonOutput.toJson(params)
  if(paramJson == "\"\"") {
    paramJson = ""
  }
  def body = "apiKey=${axonApiKey}&svcKey=${axonSvcKey}&clientId=${axonClientId}&deviceId=${deviceId}&apiName=${apiName}&params=${paramJson}"

  def request = [
    uri : axonServer,
    path : path,
    contentType : "application/json",
    body : body
  ]

  return request
}

def getStatus(deviceId, once) {
  def path = "/api/getStatus.action"
  def request = getRequest(path, deviceId, "", "")
  //log.debug "request : $request"
  
  def dev = getChildDevice(deviceId)
  try {
      httpPost(request) { resp ->
        def result = resp.data.success
        log.debug "response data: ${resp.data}"
        
        dev?.updateStatus(resp.data.bean)
        def temp = resp.data.bean[3]
        def status = dev?.device.currentValue("status")
        if (!once && atomicState.retry < retryMax() && status == atomicState.lastStatus) {
           runIn(3, refreshStatus)
           atomicState.retry = atomicState.retry + 1
           log.info("getStatus - retry after 3 seconds")
        } else {
           log.info("getStatus - stop")
        }
      }
  } catch (e) {
      log.debug "getStatus went wrong: $e"
  }
}

def sendCommand(deviceId, apiName, params) {
  def path = "/api/sendCommand.action"
  def request = getRequest(path, deviceId, apiName, params)
  log.info "request(sendCommand) : $request"
  try {
      httpPost(request) { resp ->
        log.debug "response data: ${resp.data}"
        // clear atomic status
        atomicState.lastStatus = ""
        def result = resp.data.success
        def dev = getChildDevice(settings.cookerId)
        def object = null
        def msg = null
        if (resp.data["bean"]) {
        	def jsonSlurper = new JsonSlurper()
        	object = jsonSlurper.parseText(resp.data?.bean)
            //sendPushMessage(msg)
        }
        if (result && (apiName == "cancel" || object?.resultCode == 0)) {
          msg = "명령 전달 성공!"
          dev.sendEvent(name: "status", value: atomicState.nextStatus, displayed : true)
        } else {
          def detail = dev.getResultMessage(object?.resultCode)
          msg = "명령 실패! ($detail)"
          dev.sendEvent(name: "status", value: "error${object?.resultCode}", displayed : true)
          
//          runIn(5, refreshStatus)	// 5초뒤 상태갱신(에러메세지 제거) 
        }
        //sendPushMessage(msg);
        //if (resp.data.success) {
        //	//getStatus(deviceId)
        //    runIn(3, refreshStatus)
        //}
      }
  } catch (e) {
      log.debug "sendCommand went wrong: $e"
  }
}