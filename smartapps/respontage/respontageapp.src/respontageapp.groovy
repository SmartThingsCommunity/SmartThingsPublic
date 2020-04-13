/**
 *  RespontageApp
 *
 *  Copyright 2020 Joe Mari de Mesa
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
definition(
    name: "RespontageApp",
    namespace: "respontage",
    author: "Joe Mari de Mesa",
    description: "Access smartthings through this app",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
		// TODO: put inputs here
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers
mappings {
  path("/devices") {
    action: [
        GET: "listDevices"
    ]
  }
  path("/device/:id") {
    action: [
        GET: "deviceDetails"
    ]
  }
  path("/device/:id/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValue"
    ]
  }
  path("/device/:id/command/:name") {
    action: [
        POST: "deviceCommand"
    ]
  }
}
def listDevices() {
  def resp = []
  devices.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  return resp
}

def deviceDetails() {
  def device = getDeviceById(params.id)

  def supportedAttributes = []
  device.supportedAttributes.each {
    supportedAttributes << it.name
  }

  def supportedCommands = []
  device.supportedCommands.each {
    def arguments = []
    it.arguments.each { arg ->
      arguments << "" + arg
    }
    supportedCommands << [
        name: it.name,
        arguments: arguments
    ]
  }

  return [
      id: device.id,
      label: device.label,
      manufacturerName: device.manufacturerName,
      modelName: device.modelName,
      name: device.name,
      displayName: device.displayName,
      supportedAttributes: supportedAttributes,
      supportedCommands: supportedCommands
  ]
}

def deviceGetAttributeValue() {
  def device = getDeviceById(params.id)
  def name = params.name
  def value = device.currentValue(name);
  return [
      value: value
  ]
}
def deviceCommand() {
  def device = getDeviceById(params.id)
  def name = params.name
  def args = params.arg
  if (args == null) {
    args = []
  } else if (args instanceof String) {
    args = [args]
  }
  log.debug "device command: ${name} ${args}"
  switch(args.size) {
    case 0:
      device."$name"()
      break;
    case 1:
      device."$name"(args[0])
      break;
    case 2:
      device."$name"(args[0], args[1])
      break;
    default:
      throw new Exception("Unhandled number of args")
  }
}

def getDeviceById(id) {
  return devices.find { it.id == id }
}