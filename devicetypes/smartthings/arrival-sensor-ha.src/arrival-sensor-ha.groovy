import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

/**
 *  Vision Zigbee Arrival Sensor
 *
 *  1. Delete --> (runLocally: true, executeCommandsLocally: true) in definition method ; all log.debug would be run.
 *  2. Delete --> (minHubCoreVersion: '000.025.00032') in definition method ; it don't care min Hub Core Version.
 *
 */
metadata {
    definition (name: "Arrival Sensor HA", namespace: "smartthings", author: "Steven Chen") {
    
        capability "Tone"
        capability "Actuator"
        capability "Presence Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"
        capability "Health Check"

        fingerprint inClusters: "0000,0001,0003,000F,0020", outClusters: "0003,0019", manufacturer: "SmartThings", model: "tagv4", deviceJoinName: "SmartThings Presence Sensor"
        fingerprint profileId: "0104", deviceId: "000C", inClusters: "0000,0001,0003,0006,0020", outClusters: "0003,0019", manufacturer: "Vision", model: "ArrivalTagv1", deviceJoinName: "Vision Zigbee Arrival Sensor"
    }

    preferences {
        section {
            image(name: 'educationalcontent', multiple: true, images: [
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival1.png",
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival2.png"
                ])
        }
        section {
            input "checkInterval", "enum", title: "Presence timeout (minutes)", description: "Tap to set",
                    defaultValue:"2", options: ["2", "3", "5"], displayDuringSetup: false
        }
        section {
            input "detectTime", "enum", title: "G Sensor detect time (base 16s)", description: "Tap to set",
                    defaultValue:"2", options: ["1", "2", "3", "4", "5", "6"], displayDuringSetup: false
        }
    }

    tiles {
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#00a0dc"
            state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
        }
        standardTile("beep", "device.beep", decoration: "flat") {
            state "beep", label:'', action:"tone.beep", icon:"st.secondary.beep", backgroundColor:"#ffffff"
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        main "presence"
        details(["presence", "beep", "battery"])
    }
}

def updated() {
	log.debug "updated()"
    state.gsensor = 0
    def thedetectTime = (detectTime ? detectTime as int : 2) * 1
    def updatecmds = zigbee.writeAttribute(0x0000, 0x0000, 0x20, thedetectTime, [mfgCode: 0x120D]) 
    log.debug "Updatecmds:  ${updatecmds}"
	return response(updatecmds)
}

def installed() {
	log.debug "installed()"
	// Arrival sensors only goes OFFLINE when Hub is off
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
}

def configure() {
    def cmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) + zigbee.batteryConfig(60, 60, 0x01) +   //30  //3600 -> 1hour
                zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000) + zigbee.onOffConfig() //+ //zigbee.configureReporting(0x0006, 0x0000, DataType.BOOLEAN, 5, 5, 0x01)
                //zigbee.readAttribute(0x0000, 0x0000,[mfgCode: 0x120D]) + zigbee.configureReporting(0x0000, 0x0000, DataType.UINT8, 60, 60, 0x00,[mfgCode: 0x120D])
    log.debug "configure -- cmds: ${cmds}"
    return cmds
}

def beep() {
    log.debug "Sending Identify command to beep the sensor for 5 seconds"
    return zigbee.command(0x0003, 0x00, "0500")
}

def parse(String description) {
	log.debug "description: $description"
	state.lastCheckin = now()
    if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
    	log.debug zigbee.parseDescriptionAsMap(description)
    	def descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap.clusterInt == 0x0006) {
            log.debug "Command: ${descMap.commandInt}"
        	if(descMap.commandInt == 0x01)
            {
            	log.debug "True"
				handlePresenceEvent(true)	
                state.gsensor = 1
            }    
            else
            {
            	log.debug "False"
                stopTimer()
    		}
	    } else if (descMap.clusterInt == 0x0001 && descMap.attrInt == 0x0020) {
            handleBatteryEvent(Integer.parseInt(descMap.value, 16))
        }
    }
    
    return []
}

/**
 * Create battery event from reported battery voltage.
 *
 * @param volts Battery voltage in .1V increments
 */
private handleBatteryEvent(volts) {
	def descriptionText
    if (volts == 0 || volts == 255) {
        log.debug "Ignoring invalid value for voltage (${volts/10}V)"
    }
    else {
        def batteryMap = [29:100, 28:90, 27:90, 26:70, 25:70, 24:50, 23:50,
                          22:30, 21:30, 20:15, 19:8, 18:1, 17:0, 16:0, 15:0]
        def minVolts = 15
        def maxVolts = 29

        if (volts < minVolts)
            volts = minVolts
        else if (volts > maxVolts)
            volts = maxVolts
        def value = batteryMap[volts]
        if (value != null) {
            def linkText = getLinkText(device)
            descriptionText = '{{ linkText }} battery was {{ value }}'
            def eventMap = [
                name: 'battery',
                value: value,
                descriptionText: descriptionText,
                translatable: true
            ]
            log.debug "Creating battery event for voltage=${volts/10}V: ${linkText} ${eventMap.name} is ${eventMap.value}%"
            sendEvent(eventMap)
        }
    }
}

private handlePresenceEvent(present) {
    //def wasPresent = device.currentState("presence")?.value == "present"
    if (!state.gsensor && present) {
        log.debug "Sensor is present"
        startTimer()
    } else if (!present) {
        log.debug "Sensor is not present"
        stopTimer()
    }
    def linkText = getLinkText(device)
    def descriptionText
    if ( present )
    	descriptionText = "{{ linkText }} has arrived"
    else
    	descriptionText = "{{ linkText }} has left"
    def eventMap = [
        name: "presence",
        value: present ? "present" : "not present",
        linkText: linkText,
        descriptionText: descriptionText,
        translatable: true
    ]
    log.debug "Creating presence event: ${device.displayName} ${eventMap.name} is ${eventMap.value}"
    sendEvent(eventMap)
}   

private startTimer() {
    log.debug "Scheduling periodic timer"
    // Unlike stopTimer, only schedule this when running in the cloud since the hub will take care presence detection
    // when it is running locally
    runEvery1Minute("checkPresenceCallback", [forceForLocallyExecuting: false])
}

private stopTimer() {
    log.debug "Stopping periodic timer"
    // Always unschedule to handle the case where the DTH was running in the cloud and is now running locally
    unschedule("checkPresenceCallback", [forceForLocallyExecuting: true])
    state.gsensor = 0
}

def checkPresenceCallback() {
	def timeSinceLastCheckin = (now() - state.lastCheckin ?: 0) / 1000
    def theCheckInterval = (checkInterval ? checkInterval as int : 2) * 60
    log.debug "Sensor checked in ${timeSinceLastCheckin} seconds ago"
    if (timeSinceLastCheckin >= theCheckInterval) {
        handlePresenceEvent(false)
    }
}