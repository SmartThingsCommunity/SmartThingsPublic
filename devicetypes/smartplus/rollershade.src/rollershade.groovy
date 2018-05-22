/**
 *  Copyright 2015 SmartThings
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
 */a
metadata {
    definition (name: "RollerShade", namespace: "smartplus", author: "jianfu") {
        capability "Configuration"
        capability "Actuator"
        capability "Refresh"
        
        command "upOpen"
        command "downClose"
        command "stop"
        command "refresh"
        command "gotoPercent", ["number"]
		command "microOpen"
        command "microClose"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", manufacturer: "Smartplus", model:"DongleZD", deviceJoinName:"Smartplus's DongleZD"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", manufacturer: "Smartplus", model:"DongleZR", deviceJoinName:"Smartplus's DongleZR"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", manufacturer: "Smartplus", model:"M2810EIR", deviceJoinName:"Smartplus's M2810EIR"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", manufacturer: "Smartplus", model:"M2810EIRB", deviceJoinName:"Smartplus's M2810EIRB"        
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", manufacturer: "Smartplus"
        
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"Generic", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("printPercent", key: "PRIMARY_CONTROL") {
                attributeState "default", label:'${currentValue}%', backgroundColor:"#00A0DC"
            }
            tileAttribute ("destPercent", key: "SLIDER_CONTROL", ) {
                attributeState "default", action:"gotoPercent", defaultState: true
            }
        }
        standardTile("upOpen", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: 'Open', action: "upOpen",icon: "http://47.89.178.62:8080/ShengTuRui/smartthings/up.png",  backgroundColor: "#00a0dc"
        }
        
        standardTile("stop", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: 'Stop', action: "stop",icon: "http://47.89.178.62:8080/ShengTuRui/smartthings/stop.png",   backgroundColor: "#00a0dc"
        }
        
        standardTile("downClose", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: 'Close', action: "downClose", icon: "http://47.89.178.62:8080/ShengTuRui/smartthings/down.png", backgroundColor: "#00a0dc"
        }
        //controlTile("destPercent", "destPercent", "slider", decoration: "flat", height: 1, width: 6, inactiveLabel: false) {
		//	state "destPercent", action:"gotoPercent"
		//}
        standardTile("microClose", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: '<<<', action: "microClose", icon: "http://47.89.178.62:8080/ShengTuRui/smartthings/down.png",  backgroundColor: "#00a0dc"
        }
        
           standardTile("microClose1", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: '', action: "microClose"
        }
        //standardTile("microStep", "None", width: 2, height: 1, decoration: "flat") {
        //    state "default", label: ''
        //}
        standardTile("microOpen", "None", width: 2, height: 2, decoration: "flat") {
            state "default", label: '>>>', action: "microOpen",  icon: "http://47.89.178.62:8080/ShengTuRui/smartthings/up.png", backgroundColor: "#00a0dc"
        }
        main "Generic"
        details(["Generic", "downClose", ,"stop", "upOpen", "destPercent", "microClose", "microStep","microClose1", "microOpen"])
    }
}



/**************************************************************************************************************************/
// Parse incoming device messages to generate events
def toHex(value, width){
	def table = ["0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"]
    if (width == 2){
    	def valueH = value.intdiv(256)
        def valueL = value % 256
        return table[valueL.intdiv(16)] + table[valueL % 16] + table[valueH.intdiv(16)] + table[valueH % 16]
    }
    else{
    	return table[value.intdiv(16)] + table[value % 16]
    }
}
def toInt(string){
	def value = []
	def table = "0123456789ABCDEF"
    string = string.toUpperCase()
    for(int i=0; i<string.length(); i+=2){
    	def vh = table.indexOf(string.getAt(i))
        def vl = table.indexOf(string.getAt(i+1))
        value.add(vh*16+vl)
    }
    if(value.size == 1){
    	return value[0]
    } else if(value.size == 2){
        return value[1]*256+value[0]
	}
}
def parse(String description) {
    //log.debug "description is $description"
    def event = zigbee.getEvent(description)    
    if (event) {
    } else {
        def descMap = zigbee.parseDescriptionAsMap(description)
        //log.debug "${descMap}"
        if( descMap.clusterId == "0102" && descMap.command == "0B" && descMap.isClusterSpecific == false){
        	if (descMap.data[0] == "00"){
            	log.debug "Send command: Open Successed"
            }
            else if (descMap.data[0] == "01"){
            	log.debug "Send command: Close Successed"
            }
            else if (descMap.data[0] == "02"){
            	log.debug "Send command: Stop Successed"
            }
            else if (descMap.data[0] == "05"){
            	log.debug "Send command: gotoPercent Successed"
            }
	    }else if(descMap.cluster == "0102" && descMap.attrId == "0008" && descMap.isValidForDataType == true){
        	def val = toInt(descMap.value)
            log.debug "currPercent: ${val}%"
            sendEvent(name:"destPercent", value:val)
        	//sendEvent(name:"printPercent", value:val)
            if(descMap.additionalAttrs[0].attrId == "f004"){
            	val = toInt(descMap.additionalAttrs[0].value)
                log.debug "destPercent: ${val}%"
                sendEvent(name:"printPercent", value:val)
        		//sendEvent(name:"destPercent", value:val)
            }
        }
    }
}
//Rollershade commands
def upOpen(){
	log.debug "Send command: Open"
	zigbee.command(0x0102, 0x00)
}
def downClose(){
	log.debug "Send command: Close"
	zigbee.command(0x0102, 0x01)
}
def microOpen(){
	log.debug "Send command: microOpen"
	zigbee.command(0x0102, 0x87, "040000")
}
def microClose(){
	log.debug "Send command: microClose"
	zigbee.command(0x0102, 0x87, "050000")
}
def stop(){
	log.debug "Send command: Stop"
	zigbee.command(0x0102, 0x02)
}
def gotoPercent(percent){
	log.debug "Send command: gotoPercent(${percent})"
	zigbee.command(0x0102, 0x05, toHex(percent, 1))
}
def installed() {
	log.debug "installed."
}
def refresh() {
	log.debug "Refresh attribute(CurrentPercent)"
	zigbee.readAttribute(0x0102, 0x0008)
}
def configure() {
    log.debug "Configuring Reporting and Bindings."
    zigbee.configureReporting(0x0102, 0x0008, 0x20, 1, 60, 0x01)+zigbee.configureReporting(0x0102, 0xf004, 0x20, 1, 60, 0x01)
}