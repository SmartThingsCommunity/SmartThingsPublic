/**
 * 
 *
 * Modified EJ 02/19/2017
 *
 * Version 1.0.3
 *
 * Window Shade Capability standardizes:  (these should not be changed, except by SmartThings capabilities updates)
 *	- windowShade: unknown, closed, open, partially open, closing, opening 
 *	- Commands:  open(), close(), presetPosition()
 *
 *	Baised off of "Somfy Z-Wave Shades and Blinds Multi tile" by E_Sch
 */
  metadata {
    definition (name: "Monoprice Z-Wave Curtain Module ", namespace: "ejordan1", author: "Ed") {
        capability "Switch"
        capability "Window Shade"
        capability "Refresh"
        capability "Actuator"
		capability "Configuration"

        fingerprint deviceId: "0x1105", inClusters: "0x25,0x26,0x70,0x72,0x86" , manufacturer: "0109", model: "0D03"      //	zw:L type:1105 mfr:0109 prod:200D model:0D03 ver:4.84 zwv:3.52 lib:06 cc:25,26,70,72,86
    }

    simulator {
        status "Up":  "command: 2003, payload: FF"
        status "Down": "command: 2003, payload: 00"
        status "Stop": "command: 2003, payload: FE"
        
        // reply messages
        reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
        reply "200100,delay 5000,2602": "command: 2603, payload: 00"
    }

    preferences {                
    
        input ("downTime", "number", title: "Down Time", 
       			description: "Seconds for Down Relay On",
              	range: "1..250", displayDuringSetup: true   )         
    
        input ("upTime", "number", title: "Up Time", 
        		description: "Seconds for Up Relay On",
              	range: "1..250", displayDuringSetup: true   )                   
    
        input ("stopTime", "number", title: "Stop Time", 
        		description: "Seconds for Stop Relay On",
              	range: "1..250", displayDuringSetup: true   )   
              
              }

    tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState("unknown",			label:'${name}', action:"refresh.refresh",	icon:"st.doors.garage.garage-open", 		backgroundColor:"#ffa81e")
                attributeState("lowered",        	label:'${name}', action:"open",		icon:"st.doors.garage.garage-closed", 		backgroundColor:"#00bb00", nextState: "raising")
                attributeState("raised",           	label:'${name}', action:"close",	icon:"st.doors.garage.garage-open",			backgroundColor:"#0000ff", nextState: "lowering")
                attributeState("partially down",  	label:'${name}', action:"open",		icon:"st.Transportation.transportation13", 	backgroundColor:"#00b799", nextState: "raising")
                attributeState("lowering",        	label:'${name}', action:"open",		icon:"st.doors.garage.garage-closing", 		backgroundColor:"#00ff00", nextState: "raising")
                attributeState("raising",         	label:'${name}', action:"close",	icon:"st.doors.garage.garage-opening", 		backgroundColor:"#0099ff", nextState: "lowering")
            

            }
        }

        standardTile("on", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("lowering", label:'UP', action:"window shade.open", icon:"st.doors.garage.garage-opening")
        }
        standardTile("off", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("raising", label:'Down', action:"window shade.close", icon:"st.doors.garage.garage-closing")
        }
        standardTile("stop", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("partially down", label:'stop', action:"window shade.presetPosition", icon:"st.Transportation.transportation13")
        }
        standardTile("configure", "device.configure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state( "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure")
		}


        main(["shade"])
        details(["shade", "on", "off", "stop","configure"])
    }
}

def configure() {
    log.trace "configure() called"
    
	def upTimeVal = upTime.toInteger()    
    // Divide the prefernces into a 2 byte value
    def short upTimeLow = upTimeVal & 0xFF
    def short upTimeHigh = (upTimeVal >> 8) & 0xFF
    def upTimeBytes = [upTimeHigh, upTimeLow]
    
    log.debug "New DownTime is: ${upTimeBytes}"
    
	def stopTimeVal = stopTime.toInteger()    
    // Divide the prefernces into a 2 byte value
    def short stopTimeLow = stopTimeVal & 0xFF
    def short stopTimeHigh = (stopTimeVal >> 8) & 0xFF
    def stopTimeBytes = [stopTimeHigh, stopTimeLow]
    
    log.debug "New stopTime is: ${stopTimeBytes}"
    
	def downTimeVal = downTime.toInteger()    
    // Divide the prefernces into a 2 byte value
    def short downTimeLow = downTimeVal & 0xFF
    def short downTimeHigh = (downTimeVal >> 8) & 0xFF
    def downTimeBytes = [downTimeHigh, downTimeLow]
    
    log.debug "New DownTime is: ${downTimeBytes}"
     
    delayBetween([
    zwave.configurationV1.configurationSet(configurationValue: upTimeBytes,	parameterNumber: 1, size: 2).format(), //UP/Open relay on, time in seconds
    zwave.configurationV1.configurationSet(configurationValue: stopTimeBytes,	 	parameterNumber: 2, size: 1).format(), //Stop/Off Relay on, when off pressed, time in seconds
    zwave.configurationV1.configurationSet(configurationValue: downTimeBytes, 	parameterNumber: 3, size: 1).format()  //Down/Close time relay on, time in seconds
    ],500)
    
}

def ping() {
	refresh()
}

def parse(String description) {
    description
    def result = null
    def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
    log.debug "Parsed ${description} to ${cmd}"
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "zwaveEvent( ${cmd} ) returned ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    def result = []
    def tempstr = ""

    log.trace "Report cmd.value:  ${cmd.value}"

    if (cmd.value == 0) {
        sendEvent(name: "windowShade", value: "lowered")
        tempstr = "lowered"
    } else if (cmd.value == 0xFF) {
        sendEvent(name: "windowShade", value: "raised")
        tempstr = "raised"
    } else {  
        sendEvent(name: "windowShade", value: "partially down")
        tempstr = "stopped"
    }
        
    log.debug "Zwave state is ${tempstr}"
    return tempstr
}


def on() {
    log.trace "on() treaded as down()"  
        sendEvent(name: "windowShade", value: "lowering")

        delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: 0x00).format(),
            zwave.basicV1.basicGet().format()
        ], 10000)
}

def off() {
    log.trace "off() treated as up()"  
        sendEvent(name: "windowShade", value: "raising")

        delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: 0xFF).format(),
            zwave.basicV1.basicGet().format()
        ], 10000)
}

def presetPosition() {
    log.trace "Stop()"    
        sendEvent(name: "windowShade", value: "partially down")
    
        delayBetween([
            zwave.switchMultilevelV1.switchMultilevelStopLevelChange().format(),
            zwave.basicV1.basicGet().format()
        ], 4000)
    }

def open() {
    log.trace "open()"
    off()
}

def close() {
    log.trace "close()"
    on()
}

def refresh() {
    log.trace "refresh()"
    
    zwave.basicV1.basicGet().format()
}
