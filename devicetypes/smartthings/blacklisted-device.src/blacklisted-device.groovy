/**
 *  Blacklisted Device
 *
 *  Copyright 2017 SmartThings
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

metadata {
    definition (name: "Blacklisted Device", namespace: "smartthings", author: "SmartThings") {
        fingerprint profileId: "0104", inClusters: "0000, 0009, 000A, 0101, FC00, 0001", manufacturer:"Yale", model:"Cap", deviceJoinName: "Blacklisted Yale Lock"
        fingerprint inClusters: "0000, 0003, 0101", manufacturer:"Kwikset", model:"Smartcode", deviceJoinName: "Blacklisted Kwikset Lock"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"alarm", type: "generic", width: 3, height: 4){
            tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
                attributeState "default", label:'blocked!', icon:"st.unknown.thing.thing-circle", backgroundColor:"#ff0000"
            }
        }

        htmlTile(name:"Home",action:"main", type: "HTML",width: 6, height: 2, whitelist: whitelist())

        main "alarm"
        details(["Home"])
    }
}

mappings {
	path("/main") { action: [GET:"main"] }
}

def installed() {
    log.warn "Blacklisted DTH device joined. DeviceId : ${device.id}, manufacturer: ${device.getDataValue('manufacturer')}, model: ${device.getDataValue('model')}"
}

def uninstalled() {
    log.trace "Blacklisted DTH device deleted. DeviceId : ${device.id}, manufacturer: ${device.getDataValue('manufacturer')}, model: ${device.getDataValue('model')}"
}

def main() {
    renderHTML() {
        head {
            """
                <meta name="viewport" content="initial-scale=1.0">
                <meta charset="utf-8">
                <style>
                    body {
                        font-size: 1em;
                        color: #888888;
                    }

                    a {
                        color: #15bfff;
                    }
                </style>
            """
        }
        body {
            """
            <div id="home" style="text-align: center; padding-top:15px;">
            This device is known to be incompatible with SmartThings and may not function as expected or cause other devices to malfunction.</br>
            For more information go to:</br></br>
            <a href="https://support.smartthings.com/hc/en-us/articles/115005123183" target="_blank">https://support.smartthings.com/hc/en-us/articles/115005123183</a>
            </div>
            """
        }
    }
}

def whitelist() {
    []
}
