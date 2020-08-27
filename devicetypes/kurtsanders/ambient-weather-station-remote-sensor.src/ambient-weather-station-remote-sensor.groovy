/**
*  Copyright 2018, 2019 SanderSoft
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
*  Ambient Weather Station Remote Sensor
*
*  Author: Kurt Sanders, SanderSoft™
*  Version:	5.00
*  Date:	5-14-2020
*/

import groovy.time.*
import java.text.SimpleDateFormat;

String getAppImg(imgName) 		{ return "https://raw.githubusercontent.com/KurtSanders/STAmbientWeather/master/images/$imgName" }

metadata {
    definition (
        name		: "Ambient Weather Station Remote Sensor",
        namespace	: "kurtsanders",
        author		: "kurt@kurtsanders.com",
        vid			: "SmartThings-smartthings-SmartSense_Temp/Humidity_Sensor",
        mnmn		: "SmartThings",          // for the new Samsung (Connect) app
    )
    {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Battery"
        capability "Refresh"

        attribute "date", "string"
        attribute "lastSTupdate", "string"
        attribute "version", "string"
        
        command "refresh"
    }
    tiles(scale: 2) {
        multiAttributeTile(name:"temperature", type:"generic", width:6, height:4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default",label:'${currentValue}º',
                               backgroundColors:[
                                   [value: 32, color: "#153591"],
                                   [value: 44, color: "#1e9cbb"],
                                   [value: 59, color: "#90d2a7"],
                                   [value: 74, color: "#44b621"],
                                   [value: 84, color: "#f1d801"],
                                   [value: 92, color: "#d04e00"],
                                   [value: 98, color: "#bc2323"]
                               ])
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
            }
        }
    }

    standardTile("battery", "device.battery", width: 2, height: 1, decoration: "flat", wordWrap: true) {
        state "default", 	label: '', icon: getAppImg('battery-na.png')
        state "0", 			label: '', icon: getAppImg('battery-bad.png')
        state "100", 		label: '', icon: getAppImg('battery-good.png')
    }
    valueTile("date", "device.date", width: 4, height: 1, decoration: "flat", wordWrap: true) {
        state("default", label: 'Ambient Server DateTime\n${currentValue}')
    }
    valueTile("lastSTupdate", "device.lastSTupdate", width: 4, height: 1, decoration: "flat", wordWrap: true) {
        state "default", label: '${currentValue}', action: "refresh"
    }
    standardTile("refresh", "device.weather", width: 2, height: 1, decoration: "flat", wordWrap: true) {
        state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
    }

    main(["temperature"])
    details(
        [
            // Inside Sensors
            "temperature",
            "humidity",
            "battery",
            "date",
            "lastSTupdate",
            "refresh"
        ]
    )
}
def refresh() {
    Date now = new Date()
    def timeString = now.format("EEE MMM dd h:mm:ss a", location.timeZone)
    log.info "User requested a 'Manual Refresh' from Ambient Weather Station device, sending refresh() request to parent smartApp"
    sendEvent(name: "lastSTupdate", value: "Cloud Refresh Requested at\n${timeString}...", "displayed":false)
    sendEvent(name: "secondaryControl", value: "Cloud Refresh Requested...", "displayed":false)
    parent.refresh()
}
def installed() {
}
def updated() {
}
def uninstalled() {
}