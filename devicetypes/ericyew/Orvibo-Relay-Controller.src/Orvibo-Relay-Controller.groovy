/**
  *  Custom Device type for Orvibo Multi-purpose controller in relay mode
 
  */
 metadata {
 	definition (name: "Orvibo Relay Controller", namespace: "ericyew", author: "ericyew") {
         capability "Refresh"
         capability "Polling"
         capability "Configuration"
         capability "Switch"
         command "on1"
         command "off1"
         command "on2"
         command "off2"
         command "on3"
         command "off3"
         command "onoff1"
         command "onoff2"
         command "onoff3"
         attribute "switch1","ENUM", ["on","off"]
         attribute "switch2","ENUM", ["on","off"]
         attribute "switch3","ENUM", ["on","off"]


     	//fingerprint profileId: "0104", inClusters: "0000", outClusters: "000D,0006"
         fingerprint inClusters: "0000 0001 0003 0004 0005 0006", endpointId: "01", deviceId: "0100", profileId: "0104"

 	}

 	// simulator metadata
 	simulator {
     }

 	// UI tile definitions
 	tiles {
 		standardTile("switch1", "device.switch1", width: 1, height: 1, canChangeIcon: true) {
            state "off", label:'Down', action:"onoff1", icon:"st.Transportation.transportation14", backgroundColor:"#ffffff", nextState:"on"
            state "on", label:'Down', action:"off1", icon:"st.Transportation.transportation14", backgroundColor:"#79b821", nextState:"off"
        }
 		standardTile("switch2", "device.switch2", width: 1, height: 1, canChangeIcon: true) {
 			state "off", label: 'Up', action: "onoff2", icon: "st.Transportation.transportation14", backgroundColor: "#ffffff", nextState:"on"
 			state "on", label: 'Up', action: "off2", icon: "st.Transportation.transportation14", backgroundColor: "#79b821", nextState:"off"
 		}
        standardTile("switch3", "device.switch3", width: 1, height: 1, canChangeIcon: true) {
 			state "off", label: 'Stop', action: "onoff3", icon: "st.Transportation.transportation14", backgroundColor: "#ffffff", nextState:"on"
 			state "on", label: 'Stop', action: "off3", icon: "st.Transportation.transportation14", backgroundColor: "#79b821", nextState:"off"
 		}
         standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}

 		main (["switch1", "switch2", "switch3"])
 		details (["switch1", "switch2", "switch3", "refresh"])
 	}
 }

 // Parse incoming device messages to generate events
 def parse(String description) {
     log.debug "Parse description $description"
     def name = null
     def value = null

     if (description?.startsWith("catchall: 0104 0006 01")) {
         log.debug "On/Off command received from EP 1"
         if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001000")){
         	name = "switch1"
             value = "off"}
         else if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001001")){
         	name = "switch1"
             value = "on"}                        
     }  
     else if (description?.startsWith("catchall: 0104 0006 02")) {
         log.debug "On/Off command received from EP 2"    
         if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001000")){
         	name = "switch2"
             value = "off"}
         else if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001001")){
         	name = "switch2"
             value = "on"}
     }
     else if (description?.startsWith("catchall: 0104 0006 03")) {
         log.debug "On/Off command received from EP 3"    
         if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001000")){
         	name = "switch3"
             value = "off"}
         else if (description?.endsWith(" 01 0140 00 38A8 00 00 0000 01 01 0000001001")){
         	name = "switch3"
             value = "on"}
     }


 	def result = createEvent(name: name, value: value)
     log.debug "Parse returned ${result?.descriptionText}"
     return result
 }

 // Commands to device

 def on1() {
 	log.debug "Relay 1 on()"
 	sendEvent(name: "switch1", value: "on")
 	"st cmd 0x${device.deviceNetworkId} 0x01 0x0006 0x1 {}"
 }

 def off1() {
 	log.debug "Relay 1 off()"
 	sendEvent(name: "switch1", value: "off")
 	"st cmd 0x${device.deviceNetworkId} 0x01 0x0006 0x0 {}"
 }
 
 def onoff1() {
    log.debug "Relay 1 onoff()"
 	delayBetween([
    on1(),
    off1()
    ], 1000)
 }
 
 def on2() {
 	  log.debug "Relay 2 on()"
 	  sendEvent(name: "switch2", value: "on")
 	  "st cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x1 {}"     
 }

 def off2() {
 	log.debug "Relay 2 off()"
 	sendEvent(name: "switch2", value: "off")
 	"st cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x0 {}"
 }

def onoff2() {
    log.debug "Relay 1 onoff()"
 	delayBetween([
    on2(),
    off2()
    ], 1000)
 }
 
 def on3() {
     log.debug "Relay 3 on()"
 	 sendEvent(name: "switch3", value: "on")
 	 "st cmd 0x${device.deviceNetworkId} 0x03 0x0006 0x1 {}"
 }

 def off3() {
 	log.debug "Relay 3 off()"
 	sendEvent(name: "switch3", value: "off")
 	"st cmd 0x${device.deviceNetworkId} 0x03 0x0006 0x0 {}"
 }

 def onoff3() {
    log.debug "Relay 1 onoff()"
 	delayBetween([
    on3(),
    off3()
    ], 1000)
 }


 def poll(){
 	log.debug "Poll is calling refresh"
 	refresh()
 }

 def refresh() {
 	log.debug "sending refresh command"
     def cmd = []

     cmd << "st rattr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000"	// Read on / off value at End point 0x01 
     cmd << "st wattr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000"	// Read on / off value at End point 0x01 
     cmd << "delay 150"

     cmd << "st rattr 0x${device.deviceNetworkId} 0x02 0x0006 0x0000"	// Read on / off value at End point 0x02 
     cmd << "st wattr 0x${device.deviceNetworkId} 0x02 0x0006 0x0000"	// Read on / off value at End point 0x02 
     cmd << "delay 150"

     cmd << "st rattr 0x${device.deviceNetworkId} 0x03 0x0006 0x0000"	// Read on / off value at End point 0x03
     cmd << "st wattr 0x${device.deviceNetworkId} 0x03 0x0006 0x0000"	// Read on / off value at End point 0x03
     cmd
     zigbee.onOffRefresh() + zigbee.onOffConfig()
 }



 def configure() {
 	log.debug "Binding SEP 0x01 and 0x02 and 0x03 DEP 0x01 Cluster 0x0006 On / Off cluster to hub" 
     def cmd = []
     cmd << "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}"	    // Bind on/off output to SmartThings hub for end point 1
     cmd << "delay 150"
     cmd << "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}" 	// Bind on/off output to SmartThings hub for end point 2
     cmd << "delay 150"
     cmd << "zdo bind 0x${device.deviceNetworkId} 0x03 0x01 0x0006 {${device.zigbeeId}} {}" 	// Bind on/off output to SmartThings hub for end point 3
     cmd
 }  
