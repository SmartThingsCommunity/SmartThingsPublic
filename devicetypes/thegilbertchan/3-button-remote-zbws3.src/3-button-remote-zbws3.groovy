/*
 *  3 Button Remote Zigbee ZBWS3  
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
 *  Thanks to Seth Jansen @sjansen for original contributions
 */
metadata {
    definition (name: "3 Button Remote (ZBWS3)", namespace: "thegilbertchan", author: "Gilbert Chan") {
        capability "Button"
        capability "Configuration"

        attribute "button2","ENUM",["released","pressed"]
        attribute "button3","ENUM",["released","pressed"]        

        fingerprint endpointId: "03", profileId: "0104", deviceId: "0000", deviceVersion: "00", inClusters: "03 0000 0003 0007", outClusters: "01 0006"

    }
    // Contributors

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles {

        standardTile("button1", "device.button", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }

        standardTile("button2", "device.button2", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }        

        standardTile("button3", "device.button3", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }

    main (["button3", "button2", "button1"])
    details (["button3", "button2", "button1"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "Parse description $description"
    def name = null
    def value = null              
    if (description?.startsWith("catchall: 0104 0006 01")) {
        name = "1"
        def currentST = device.currentState("button")?.value
        log.debug "Button 1 pushed"           

    } else if (description?.startsWith("catchall: 0104 0006 02")) {
        name = "2"
        def currentST = device.currentState("button2")?.value
        log.debug "Button 2 pushed"        

    } else if (description?.startsWith("catchall: 0104 0006 03")) {
        name = "3"
        def currentST = device.currentState("button3")?.value
        log.debug "Button 3 pushed"         
    } 

    def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    log.debug "Parse returned ${result?.descriptionText}"


    return result
}


def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

private getFPoint(String FPointHex){                   // Parsh out hex string from Value: 4089999a
    Long i = Long.parseLong(FPointHex, 16)         // Convert Hex String to Long
    Float f = Float.intBitsToFloat(i.intValue())   // Convert IEEE 754 Single-Precison floating point
    log.debug "converted floating point value: ${f}"
    def result = f

    return result
}


// Commands to device

def configure() { 
    log.debug "Binding SEP 0x01 DEP 0x01 Cluster 0x0006 On/Off cluster to hub"         
        def configCmds = [

        "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", "delay 500",        
        "zdo bind 0x${device.deviceNetworkId} 0x03 0x01 0x0006 {${device.zigbeeId}} {}", "delay 1500",        
        ]
    log.info "Sending ZigBee Bind commands to 3 Button Switch"
    return configCmds
}