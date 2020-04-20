/**
 *  Brilliant Light Switch App
 *
 *  Copyright 2016 Brilliant Home Technology
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
    name: "Brilliant",
    namespace: "brilliant",
    author: "Brilliant Home Technology",
    description: "Control lights connected to your Smartthings hub from the Brilliant switch",
    category: "Convenience",
    iconUrl: "https://s3-us-west-1.amazonaws.com/dev-object-store/assets/logo-brilliant-square.png",
    iconX2Url: "https://s3-us-west-1.amazonaws.com/dev-object-store/assets/logo-brilliant-square.png",
    iconX3Url: "https://s3-us-west-1.amazonaws.com/dev-object-store/assets/logo-brilliant-square.png")


preferences {
	section("All Devices") {
        input "all", "capability.actuator", multiple: true, required: true
    }
}

mappings {
  path("/all") {
    action: [
      GET: "get_all"
    ]
  }
  path("/device/:device_id") {
  	action: [
    	GET: "get_device"
    ]
  }
  path("/device/:device_id/:command") {
    action: [
      PUT: "do_command"
    ]
  }
}

def get_all() {
    def resp = []
    all.each {
      def device = it
      def attributes = it.supportedAttributes*.name
      def state = [:]
      attributes.each {
        state.put(it, device.currentState(it))
      }
      def commands = it.supportedCommands*.name
      def capabilities = it.capabilities*.name
      def map = [id: it.id, name: it.name, label: it.label, commands:commands, capabilities: capabilities, state:state]
      resp << map
    }
    return resp
}

def get_device() {
	def deviceId = params.device_id

	def device = all.find { it.id == deviceId }

    def attributes = device.supportedAttributes*.name
    def state = [:]
    attributes.each {
    	state.put(it, device.currentState(it))
    }
    return [state: state]
}

def do_command() {
    // use the built-in request object to get the command parameter
    def deviceId = params.device_id
 	def command = params.command
    def json = request.JSON

    if (deviceId && command) {
    	def device = all.find { it.id == deviceId }
    	log.debug "Device: ${device}"
        if (!device.hasCommand(command)) {
        	httpError(501, "$command is not a valid command for all switches specified")
        }
        def foo = json.data
        if (foo) {
        	log.debug "Going to issue command ${command} with variable ${foo}"
        	device."$command"(json.data)
        } else {
        	log.debug "Going to issue command ${command} with no variables"
    		device."$command"()
        }
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
}