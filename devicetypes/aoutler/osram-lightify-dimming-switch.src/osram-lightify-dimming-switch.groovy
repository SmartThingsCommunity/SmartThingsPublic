/**
*  OSRAM Lightify Dimming Switch
*
*  Copyright 2016 Michael Hudson
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
*  Thanks to @Sticks18 for the Hue Dimmer remote code used as a base for this!
*
*/

metadata {
  definition (name: "OSRAM Lightify Dimming Switch", namespace: "aoutler", author: "Adam Outler") {
    capability "Actuator"
    capability "Battery"
    capability "Button"
    capability "Configuration"
    capability "Refresh"
    capability "Sensor"
       
    attribute "zMessage", "String"
       
    //fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000, 0001, 0003, 0020, 0402, 0B05", outClusters: "0003, 0006, 0008, 0019", /*manufacturer: "OSRAM", model: "Lightify 2.4GHZZB/SWITCH/LFY", */deviceJoinName: "OSRAM Lightify Dimming Switch"
  }

  // simulator metadata
  simulator {
    // status messages
 
  }
  
  
    preferences {
        input("device1", "string", title:"Device Network ID 1", description: "Device Network ID 1", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("end1", "string", title:"Device Endpoint ID 1", description: "Device Endpoint ID 1", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("device2", "string", title:"Device Network ID 2", description: "Device Network ID 2", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("end2", "string", title:"Device Endpoint ID 2", description: "Device Endpoint ID 2", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("device3", "string", title:"Device Network ID 3", description: "Device Network ID 3", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("end3", "string", title:"Device Endpoint ID 3", description: "Device Endpoint ID 3", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("device4", "string", title:"Device Network ID 4", description: "Device Network ID 4", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("end4", "string", title:"Device Endpoint ID 4", description: "Device Endpoint ID 4", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("device5", "string", title:"Device Network ID 5", description: "Device Network ID 5", defaultValue: "" ,required: false, displayDuringSetup: false)
        input("end5", "string", title:"Device Endpoint ID 5", description: "Device Endpoint ID 5", defaultValue: "" ,required: false, displayDuringSetup: false)

	}


  // UI tile definitions
  tiles(scale: 2) {
    standardTile("button", "device.button", width: 6, height: 4) {
      state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
    }
    valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
      state "battery", label:'${currentValue}% battery'
    }
    standardTile("refresh", "device.button", decoration: "flat", width: 2, height: 2) {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
    main "button"
    details(["button", "battery", "refresh"])
  }
}

// Parse incoming device messages to generate events
def parse(String description) {
  Map map = [:]
  def result = []
  log.debug "parse description: $description"
  if (description?.startsWith('catchall:')) {
    // call parseCatchAllMessage to parse the catchall message received
    map = parseCatchAllMessage(description)
    if (device1 == null) {} else {
		if (map.value == "pushed") {
            List cmds
    		if (map.data.buttonNumber == 1) {
        		cmds = onResponse()
    		} else {
        		cmds = offResponse()
    		}
            log.trace "Sending current state to device ${cmds}"
        	result = cmds?.collect { new physicalgraph.device.HubAction(it) }  
        	return result
    	}
    }
	if (device1 == null) {} else {
        if (map.value == "held") {
            List cmds
        	if (map.data.buttonNumber == 1) {
               cmds = dim1Response()
            
    		} else {
        		cmds = dim2Response()
       	   }
           log.trace "Sending current state to device ${cmds}"
           result = cmds?.collect { new physicalgraph.device.HubAction(it) }  
           cmds?.collect { new physicalgraph.device.HubAction(it)} 
           return result
        }
    }
  } else if (description?.startsWith('read')) {
    // call parseReadMessage to parse the read message received
    map = parseReadMessage(description)
  } else {
    log.debug "Unknown message received: $description"
  }
  return map ? createEvent(map) : null
}

def configure() {
  log.debug "Confuguring Reporting and Bindings."
  def configCmds = [
    // Bind the outgoing on/off cluster from remote to hub, so the hub receives messages when On/Off buttons pushed
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}",

    // Bind the outgoing level cluster from remote to hub, so the hub receives messages when Dim Up/Down buttons pushed
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}",
    
    // Bind the incoming battery info cluster from remote to hub, so the hub receives battery updates
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}",
  ]
  return configCmds 
}

def refresh() {
  def refreshCmds = [
    zigbee.readAttribute(0x0001, 0x0020)
  ]
  //when refresh button is pushed, read updated status
  return refreshCmds
}

private Map parseReadMessage(String description) {
  // Create a map from the message description to make parsing more intuitive
  def msg = zigbee.parseDescriptionAsMap(description)
  //def msg = zigbee.parse(description)
  if (msg.clusterInt==1 && msg.attrInt==32) {
    // call getBatteryResult method to parse battery message into event map
    def result = getBatteryResult(Integer.parseInt(msg.value, 16))
  } else {
    log.debug "Unknown read message received, parsed message: $msg"
  }
  // return map used to create event
  return result
}


private Map parseCatchAllMessage(String description) {
  // Create a map from the raw zigbee message to make parsing more intuitive
  def msg = zigbee.parse(description)
  def value="pushed"
  def button=0
  String data= "[buttonnumber:"+msg.command +"]"
  Map result = [:]

  
  switch(msg.clusterId) {
    case 1: // battery message
      // call getBatteryResult method to parse battery message into event map
      log.debug 'BATTERY MESSAGE'
      return getBatteryResult(Integer.parseInt(msg.value, 16))
      break
      
    case 6: //button pressed
      button = (msg.command == 1 ? 1 : 2)
      value="pushed"
      break
 
    case 8: //button held
      button=(msg.command == 1 ? 1 : 2)
      
      switch(msg.command) {
        case 1: // brightness decrease command
          value="held"
          button=2
          state.dimming=true
          break
           
        case 3: // brightness change stop command
          value="released"
          state.dimming=false
          break
          
        case 5: // brightness increase command
          value="held"
          button=1
          state.dimming=true
          break
      }
    }
    log.debug 'message '+value+ " button " + button + ". brightness " + state.brightness

    result = [
      name: 'button',
      value: value,
      data: [buttonNumber: button],
      descriptionText: "$device.displayName button $button was $value",
      brightness: state.brightness,
      isStateChange: true
    ]
    log.info "${result?.descriptionText}"
    return result
}

//obtained from other examples, converts battery message into event map
private Map getBatteryResult(rawValue) {
  def linkText = getLinkText(device)
  def result = [
    name: 'battery',
    value: '--'
  ]
  def volts = rawValue / 10
  def descriptionText
  if (rawValue == 0) {
  } else {
    if (volts > 3.5) {
      result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
    } else if (volts > 0){
      def minVolts = 2.1
      def maxVolts = 3.0
      def pct = (volts - minVolts) / (maxVolts - minVolts)
      result.value = Math.min(100, (int) pct * 100)
      result.descriptionText = "${linkText} battery was ${result.value}%"
    }
  }
  log.debug "Parse returned ${result?.descriptionText}"
  return result
}


private String createOnResponse(device, end){
  if ( device == null) return null
  return "st cmd 0x${device} 0x${end} 8 4 {FE 0000}"
}
private String createOffResponse(device, end){
  if ( device == null) return null
  return "st cmd 0x${device} 0x${end} 6 0 {}"
}

private ArrayList createListOfNotNullDevices(allDevices){
     ArrayList list = []
     for (item in allDevices ){
        if (item !=null ) {
             list.add(item)
        }
    }
    return list
}
def onResponse() {
    state.brightness=200
    def on1 =createOnResponse(device1,end1)
    def on2 =createOnResponse(device2,end2)
    def on3 =createOnResponse(device3,end3)
    def on4 =createOnResponse(device4,end4)
    def on5 =createOnResponse(device5,end5)
    return createListOfNotNullDevices([on1,on2,on3,on4,on5]).toArray()
}




def offResponse() {
    state.brightness=0
    def off1 = createOffResponse(device1,end1)
    def off2 = createOffResponse(device2,end2)
    def off3 = createOffResponse(device3,end3)
    def off4 = createOffResponse(device4,end4)
    def off5 = createOffResponse(device5,end5)
    return createListOfNotNullDevices([off1,off2,off3,off4,off5]).toArray()


}

private String createDimResponse(device, end, level){
  if ( device == null) return null
  return "st cmd 0x${device} 0x${end} 8 4 {${level} 0000}"
}

def dimResponse(level) {
    log.debug "Creating dim response to level "+level
    def d1 = createDimResponse(device1, end1, level);
    def d2 = createDimResponse(device2, end2, level);
    def d3 = createDimResponse(device3, end3, level);
    def d4 = createDimResponse(device4, end4, level);
    def d5 = createDimResponse(device5, end5, level);
    return createListOfNotNullDevices([d1,d2,d3,d4,d5]).toArray()
}

private controlMinMaxDim(){
  if (state.brightness>95) state.brightness=95
  if (state.brightness<10) state.brightness=10
}


def dim1Response() {
    if (state.brightness==0) state.brightness=5 //convience to turn it on immediately
    state.brightness=state.brightness+10
    controlMinMaxDim()
    return dimResponse(state.brightness)
}

def dim2Response() {
    log.debug("performing dim2 response")
    
    state.brightness=state.brightness-10
    controlMinMaxDim()
    return dimResponse(state.brightness)
}