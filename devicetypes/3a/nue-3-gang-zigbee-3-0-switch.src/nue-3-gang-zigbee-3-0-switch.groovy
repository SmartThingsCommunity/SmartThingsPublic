/**

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except

in compliance with the License. You may obtain a copy of the License at:

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed

on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License

for the specific language governing permissions and limitations under the License.

Updated by Kevin X. from 3A Smart Home

Date: 07/08/2019

*/

metadata {

definition (name: "Nue 3 Gang ZigBee 3.0 Switch", namespace: "3A", author: "Kevin") {

    capability "Actuator"

    capability "Configuration"

    capability "Refresh"

    capability "Switch"

    attribute "lastCheckin", "string"

    attribute "switch0", "string"

    attribute "switch1", "string"

    attribute "switch2", "string"

    attribute "switch3", "string"

    command "on0"

    command "off0"

    command "on1"

    command "off1"

    command "on2"

    command "off2"

    command "on3"

    command "off3"



    fingerprint profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0019", manufacturer: "FeiBit", model: "FNB56-ZSW03LX2.0", deviceJoinName: "Nue 3 Gang Switch"

    fingerprint profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0019", manufacturer: "3A Smart Home DE", model: "LXN-3S27LX1.0", deviceJoinName: "Nue 3 Gang Switch"

}



simulator {

    status "on": "on/off: 1"

    status "off": "on/off: 0"

    reply "zcl on-off on": "on/off: 1"

    reply "zcl on-off off": "on/off: 0"

}



tiles(scale: 2) {

    multiAttributeTile(name:"switch0", type: "device.switch", width: 6, height: 4, canChangeIcon: true){

        tileAttribute ("device.switch0", key: "PRIMARY_CONTROL") { 

        }

        tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {

            attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")

        }

    }

    multiAttributeTile(name:"switch1", type: "device.switch", width: 6, height: 4, canChangeIcon: true){

        tileAttribute ("device.switch1", key: "PRIMARY_CONTROL") { 

            attributeState "on", label:'switch1', action:"off1", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "off", label:'switch1', action:"on1", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

            attributeState "turningOn", label:'switch1', action:"off1", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "turningOff", label:'switch1', action:"on1", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

        }

        tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {

            attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")

        }

    }

    

   

    multiAttributeTile(name:"switch2", type: "device.switch", width: 6, height: 4, canChangeIcon: true){

        tileAttribute ("device.switch2", key: "PRIMARY_CONTROL") { 

            attributeState "on", label:'switch2', action:"off2", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "off", label:'switch2', action:"on2", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

            attributeState "turningOn", label:'switch2', action:"off2", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "turningOff", label:'switch2', action:"on2", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

        }

        tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {

            attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")

        }

    }

    multiAttributeTile(name:"switch3", type: "device.switch", width: 6, height: 4, canChangeIcon: true){

        tileAttribute ("device.switch3", key: "PRIMARY_CONTROL") { 

            attributeState "on", label:'switch3', action:"off3", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "off", label:'switch3', action:"on3", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

            attributeState "turningOn", label:'switch3', action:"off3", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"

            attributeState "turningOff", label:'switch3', action:"on3", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"

        }

        tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {

            attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")

        }

    }





    standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {

        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"

        }

    main(["switch0"])

    details(["switch1","switch2","switch3","refresh"])

}

}



def parse(String description) {

log.debug "Parsing '${description}'"



def value = zigbee.parse(description)?.text

log.debug "Parse: $value"

Map map = [:]



if (description?.startsWith('catchall:')) {

map = parseCatchAllMessage(description)

}

else if (description?.startsWith('read attr -')) {

map = parseReportAttributeMessage(description)

}

else if (description?.startsWith('on/off: ')){

log.debug "onoff"

def refreshCmds = zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x1]) +

zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x2]) +

zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x3])



return refreshCmds.collect { new physicalgraph.device.HubAction(it) }

}



log.debug "Parse returned $map"

def now = new Date()



sendEvent(name: "lastCheckin", value: now)



def results = map ? createEvent(map) : null

return results;

}



private Map parseCatchAllMessage(String description) {

Map resultMap = [:]

def cluster = zigbee.parse(description)

log.debug cluster



if (cluster.clusterId == 0x0006 && cluster.command == 0x01){

    if (cluster.sourceEndpoint == 0x1)

    {

    log.debug "Its Switch one"

    def onoff = cluster.data[-1]

    if (onoff == 1)

        resultMap = createEvent(name: "switch1", value: "on")

    else if (onoff == 0)

        resultMap = createEvent(name: "switch1", value: "off")

        }

        else if (cluster.sourceEndpoint == 0x2)

        {

        log.debug "Its Switch two"

    def onoff = cluster.data[-1]

    if (onoff == 1)

        resultMap = createEvent(name: "switch2", value: "on")

    else if (onoff == 0)

        resultMap = createEvent(name: "switch2", value: "off")

        }

         else if (cluster.sourceEndpoint == 0x3)

        {

        log.debug "Its Switch three"

    def onoff = cluster.data[-1]

    if (onoff == 1)

        resultMap = createEvent(name: "switch3", value: "on")

    else if (onoff == 0)

        resultMap = createEvent(name: "switch3", value: "off")

        }

     }

    

return resultMap

}



private Map parseReportAttributeMessage(String description) {

Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->

def nameAndValue = param.split(":")

map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]

}



Map resultMap = [:]



if (descMap.cluster == "0001" && descMap.attrId == "0020") {

    resultMap = getBatteryResult(convertHexToInt(descMap.value / 2))

}



else if (descMap.cluster == "0008" && descMap.attrId == "0000") {

    resultMap = createEvent(name: "switch", value: "off")

} 

return resultMap

}



def off1() {

log.debug "off1()"

sendEvent(name: "switch1", value: "off")

"st cmd 0x${device.deviceNetworkId} 0x1 0x0006 0x0 {}"

}



def on1() {

log.debug "on1()"

sendEvent(name: "switch1", value: "on")

"st cmd 0x${device.deviceNetworkId} 0x1 0x0006 0x1 {}"

}

def off2() {

log.debug "off2()"

sendEvent(name: "switch2", value: "off")

"st cmd 0x${device.deviceNetworkId} 0x2 0x0006 0x0 {}"

}



def on2() {

log.debug "on2()"

sendEvent(name: "switch2", value: "on")

"st cmd 0x${device.deviceNetworkId} 0x2 0x0006 0x1 {}"

}



def off3() {

log.debug "off3()"

sendEvent(name: "switch3", value: "off")

"st cmd 0x${device.deviceNetworkId} 0x3 0x0006 0x0 {}"

}



def on3() {

log.debug "on3()"

sendEvent(name: "switch3", value: "on")

"st cmd 0x${device.deviceNetworkId} 0x3 0x0006 0x1 {}"

}



def refresh() {

log.debug "refreshing"

[

    "st rattr 0x${device.deviceNetworkId} 0x1 0x0006 0x0", "delay 1000",

    "st rattr 0x${device.deviceNetworkId} 0x2 0x0006 0x0", "delay 1000",

    "st rattr 0x${device.deviceNetworkId} 0x3 0x0006 0x0", "delay 1000",

]

}





private Map parseCustomMessage(String description) {

def result

if (description?.startsWith('on/off: ')) {

if (description == 'on/off: 0')

result = createEvent(name: "switch", value: "off")

else if (description == 'on/off: 1')

result = createEvent(name: "switch", value: "on")

}



return result

}





private Integer convertHexToInt(hex) {

Integer.parseInt(hex,16)

}