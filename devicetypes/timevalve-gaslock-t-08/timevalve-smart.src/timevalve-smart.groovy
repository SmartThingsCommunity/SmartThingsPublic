metadata {
        definition (name: "Timevalve Smart", namespace: "timevalve.gaslock.t-08", author: "ruinnel") {
                capability "Valve"
                capability "Refresh"
                capability "Battery"
                capability "Temperature Measurement"
                
                command "setRemaining"
                command "setTimeout"
                command "setTimeout10"
                command "setTimeout20"
                command "setTimeout30"
                command "setTimeout40"
                
                command "remainingLevel"
                
                attribute "remaining", "number"
                attribute "remainingText", "String"
                attribute "timeout", "number"
                
                //raw desc : 0 0 0x1006 0 0 0 7 0x5E 0x86 0x72 0x5A 0x73 0x98 0x80
                //fingerprint deviceId:"0x1006", inClusters:"0x5E, 0x86, 0x72, 0x5A, 0x73, 0x98, 0x80"
        }

        tiles (scale: 2) {
        	multiAttributeTile(name:"statusTile", type:"generic", width:6, height:4) {
            	tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
	            	attributeState "open", label: '${name}', action: "close", icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
	            	attributeState "closed", label:'${name}', action: "", icon:"st.contact.contact.closed", backgroundColor:"#79b821"
    	        }
                tileAttribute("device.remainingText", key: "SECONDARY_CONTROL") {
	            	attributeState "open", label: '${currentValue}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
	            	attributeState "closed", label:'', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
    	        }
            }
            
            standardTile("refreshTile", "command.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
                state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
            }
            
            controlTile("remainingSliderTile", "device.remaining", "slider", inactiveLabel: false, range:"(0..590)", height: 2, width: 4) {
            	state "level", action:"remainingLevel"
            }
            valueTile("setRemaining", "device.remainingText", inactiveLabel: false, decoration: "flat", height: 2, width: 2){
                state "remainingText", label:'${currentValue}\nRemaining'//, action: "setRemaining"//, icon: "st.Office.office6"
            }

            standardTile("setTimeout10", "device.remaining", inactiveLabel: false, decoration: "flat") {
                state "default", label:'10Min', action: "setTimeout10", icon:"st.Health & Wellness.health7", defaultState: true
                state "10", label:'10Min', action: "setTimeout10", icon:"st.Office.office13"
            }
            standardTile("setTimeout20", "device.remaining", inactiveLabel: false, decoration: "flat") {
                state "default", label:'20Min', action: "setTimeout20", icon:"st.Health & Wellness.health7", defaultState: true
                state "20", label:'20Min', action: "setTimeout20", icon:"st.Office.office13"
            }
            standardTile("setTimeout30", "device.remaining", inactiveLabel: false, decoration: "flat") {
                state "default", label:'30Min', action: "setTimeout30", icon:"st.Health & Wellness.health7", defaultState: true
                state "30", label:'30Min', action: "setTimeout30", icon:"st.Office.office13"
            }
            standardTile("setTimeout40", "device.remaining", inactiveLabel: false, decoration: "flat") {
                state "default", label:'40Min', action: "setTimeout40", icon:"st.Health & Wellness.health7", defaultState: true
                state "40", label:'40Min', action: "setTimeout40", icon:"st.Office.office13"
            }

            valueTile("batteryTile", "device.battery", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
                state "battery", label:'${currentValue}% battery', unit:""
            }

            main (["statusTile"])
//            details (["statusTile", "remainingSliderTile", "setRemaining", "setTimeout10", "setTimeout20", "batteryTile", "refreshTile", "setTimeout30", "setTimeout40"])
//            details (["statusTile", "batteryTile", "setRemaining", "refreshTile"])
            details (["statusTile", "batteryTile", "refreshTile"])
        }
}

def parse(description) {
//	log.debug "parse - " + description
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent(descriptionText: description, isStateChange: true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x70: 1, 0x71: 1, 0x98: 1])
		if (cmd) {
        	log.debug "parsed cmd = " + cmd
			result = zwaveEvent(cmd)
			//log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	return result
}

// 복호화 후 zwaveEvent() 호출
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	//log.debug "SecurityMessageEncapsulation - " + cmd
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1, 0x70: 1, 0x71: 1, 0x98: 1])
	if (encapsulatedCommand) {
		state.sec = 1
        log.debug "encapsulatedCommand = " + encapsulatedCommand
        zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	//log.debug "switch status - " + cmd.value
    createEvent(name:"contact", value: cmd.value ? "open" : "closed")
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    
    log.debug "battery - ${map.value}${map.unit}"
    // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    state.lastbatt = new Date().time
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
  //log.debug "zwaveEvent - ${device.displayName}: ${cmd}"
  createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def result = []
	log.info "zwave.configurationV1.configurationGet - " + cmd
    def array = cmd.configurationValue
    def value = ( (array[0] * 0x1000000) + (array[1] * 0x10000) + (array[2] * 0x100) + array[3] ).intdiv(60)
    if (device.currentValue("contact") == "open") {
    	value = ( (array[0] * 0x1000000) + (array[1] * 0x10000) + (array[2] * 0x100) + array[3] ).intdiv(60)
    } else {
    	value = 0
    }
    
    if (device.currentValue('contact') == 'open') {
    	def hour = value.intdiv(60);
        def min = (value % 60).toString().padLeft(2, '0');
        def text = "${hour}:${min}M"
        
        log.info "remain - " + text
        result.add( createEvent(name: "remaining", value: value, displayed: false, isStateChange: true) )
        result.add( createEvent(name: "remainingText", value: text, displayed: false, isStateChange: true) )
    } else {
    	result.add( createEvent(name: "timeout", value: value, displayed: false, isStateChange: true) )
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def type = cmd.notificationType
    if (type == cmd.NOTIFICATION_TYPE_HEAT) {
    	log.info "NotificationReport - ${type}"
        createEvent(name: "temperature", value: 999, unit: "C", descriptionText: "${device.displayName} is over heat!", displayed: true, isStateChange: true)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd) {
	def type = cmd.alarmType
    def level = cmd.alarmLevel
    
    log.info "AlarmReport - type : ${type}, level : ${level}"
    def msg = "${device.displayName} is over heat!"
    def result = createEvent(name: "temperature", value: 999, unit: "C", descriptionText: msg, displayed: true, isStateChange: true)
    if (sendPushMessage) {
    	sendPushMessage(msg)
    }
    return result
}

// remote open not allow
def open() {}

def close() {
//	log.debug 'cmd - close()'
    commands([
        zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00),
		zwave.switchBinaryV1.switchBinaryGet()
	])
}

def setTimeout10() { setTimeout(10) }
def setTimeout20() { setTimeout(20) }
def setTimeout30() { setTimeout(30) }
def setTimeout40() { setTimeout(40) }


def setTimeout(value) {
//	log.debug "setDefaultTime($value)"
    commands([
    	zwave.configurationV1.configurationSet(parameterNumber: 0x01, size: 4, scaledConfigurationValue: value * 60),
        zwave.configurationV1.configurationGet(parameterNumber: 0x01)
    ]);
}

def remainingLevel(value) {
//	log.debug "remainingLevel($value)"
    def hour = value.intdiv(60);
    def min = (value % 60).toString().padLeft(2, '0');
    def text = "${hour}:${min}M"
    sendEvent(name: "remaining", value: value, displayed: false, isStateChange: true)
    sendEvent(name: "remainingText", value: text, displayed: false, isStateChange: true)
}

def setRemaining() {
	def remaining = device.currentValue("remaining")
//	log.debug "setConfiguration() - remaining : $remaining"
    commands([
    	zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 4, scaledConfigurationValue: remaining * 60),
        zwave.configurationV1.configurationGet(parameterNumber: 0x03)
    ]);
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec != 0 && !(cmd instanceof physicalgraph.zwave.commands.batteryv1.BatteryGet)) {
    	log.debug "cmd = " + cmd + ", encapsulation"
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
    	log.debug "cmd = " + cmd + ", plain"
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def refresh() {
//	log.debug 'cmd - refresh()'
	commands([
    			zwave.batteryV1.batteryGet(),
                zwave.switchBinaryV1.switchBinaryGet(),
                zwave.configurationV1.configurationGet(parameterNumber: 0x01),
                zwave.configurationV1.configurationGet(parameterNumber: 0x03)
        ], 400)
}
