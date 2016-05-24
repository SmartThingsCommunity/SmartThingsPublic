/* Dimmer-Switch-Levels
	 *
	 * Variation of the stock SmartThings "Dimmer-Switch"
	 * Variation of the twack "Better-Dimmer-Switch"
	 * Variation of the ChadCK "Z-Wave Smart Fan Control"
	 *
	 * Device type adds set levels low, medium, and High for preset values (Moods).
	 * Adds increment up and down buttons with ability to set value.
	 *
	 * pmjoen@yahoo.com
	 * 20160517
	 *
     * v1.4 - Modified text for preferences.
	 * v1.3 - Improvement select stepper value up/down shows adjusting in multiAttributeTile
	 * v1.2 - Bug fix modify icon for current state in multiAttributeTile 
	 * v1.1 - Bug fix showing speed in multiAttributeTile 
	 * v1.0 - Initial release of device handler
	 *  
	*/
	

	metadata {
		// Automatically generated. Make future change here.
		definition (name: "Dimmer-Switch-Levels", namespace: "pmjoen", author: "SmartThings") {
			capability "Switch Level"
			capability "Actuator"
			capability "Indicator"
			capability "Switch"
			capability "Polling"
			capability "Refresh"
			capability "Sensor"
	

	        command "low"
			command "med"
			command "high"
	        command "levelUp"
	        command "levelDown"
	

			attribute "currentState", "string"
	        attribute "switch", "string"
	

			//fingerprint deviceId: "0x1101", inClusters: "0x26, 0x27, 0x70, 0x86, 0x72"
		}
	

			tiles(scale: 2) {
			multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
				tileAttribute ("device.currentState", key: "PRIMARY_CONTROL") {
					attributeState "default", label:'ADJUSTING', action:"refresh.refresh",icon:"st.Lighting.light13", backgroundColor:"#2179b8", nextState: "turningOff"
					attributeState "HIGH", label:'HIGH', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#79b821", nextState:"turningOff"
					attributeState "MED", label:'MED', action:"switch.off", icon:"st.illuminance.illuminance.light", backgroundColor:"#79b821", nextState:"turningOff" 
					attributeState "LOW", label:'LOW', action:"switch.off", icon:"st.Weather.weather4", backgroundColor:"#79b821", nextState:"turningOff" 
					attributeState "OFF", label:'OFF', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState: "turningOn"
	                attributeState "on", label:'ON', action:"switch.off", icon:"st.Lighting.light11", backgroundColor:"#79b821", nextState:"turningOff"
	                attributeState "turningOn", action:"switch.on", label:'TURNINGON', icon:"st.Lighting.light11", backgroundColor:"#2179b8", nextState: "turningOn"
					attributeState "turningOff", action:"switch.off", label:'TURNINGOFF', icon:"st.Lighting.light13", backgroundColor:"#2179b8", nextState: "turningOff"
				}
	            
	    	tileAttribute("device.level", key: "VALUE_CONTROL") {
	                attributeState("VALUE_UP", action: "levelUp")
	                attributeState("VALUE_DOWN", action: "levelDown")
				}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
					attributeState "level", action:"switch level.setLevel"
				}
			}
	

			standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
				state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
				state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
				state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
			}
	

			standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
				state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
			}
	

			valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
				state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
			}
	

	        standardTile("low", "device.currentState", inactiveLabel: false, width: 2, height: 2, canChangeBackground: false) {
	        	state "default", label: 'LOW', action: "low", icon:"st.Weather.weather4", backgroundColor: "#ffffff"
	            state "LOW", label:'LOW', action: "low", icon:"st.Weather.weather4", backgroundColor: "#79b821"
	            state "ADJUSTING.LOW", label:'LOW', action: "low", icon:"st.Weather.weather4", backgroundColor: "#2179b8"
	  		}
	        
	        standardTile("med", "device.currentState", inactiveLabel: false, width: 2, height: 2, canChangeBackground: false) {
				state "default", label: 'MED', action: "med", icon:"st.illuminance.illuminance.light", backgroundColor: "#ffffff"
	            state "MED", label: 'MED', action: "med", icon:"st.illuminance.illuminance.light", backgroundColor: "#79b821"
				state "ADJUSTING.MED", label:'MED', action: "med", icon:"st.illuminance.illuminance.light", backgroundColor: "#2179b8"
			}
	            
			standardTile("high", "device.currentState", inactiveLabel: false, width: 2, height: 2, canChangeBackground: false) {
				state "default", label: 'HIGH', action: "high", icon:"st.illuminance.illuminance.bright", backgroundColor: "#ffffff"
	            state "HIGH", label: 'HIGH', action: "high", icon:"st.illuminance.illuminance.bright", backgroundColor: "#79b821"
	            state "ADJUSTING.HIGH", label:'HIGH', action: "high", icon:"st.illuminance.illuminance.bright", backgroundColor: "#2179b8"
			}
	

			main(["switch"])
	        details(["switch", "low", "med", "high", "level", "indicator", "refresh"])
	        
		}
	    preferences {
			section("Light Level Values") {
				input "lowThreshold", "number", title: "Low Button Light Value", range: "1..99"
				input "medThreshold", "number", title: "Medium Button Light Value", range: "1..99"
				input "highThreshold", "number", title: "High Button Light Value", range: "1..99"
			}
	        section ("Interval Selection") {
	            input "stepper", "enum", title: "Up/Down Light Interval Value", defaultValue: "10", options: ["5","10","20"]
	        }
		}
	}
	

	def parse(String description) {
		def item1 = [
			canBeCurrentState: false,
			linkText: getLinkText(device),
			isStateChange: false,
			displayed: false,
			descriptionText: description,
			value:  description
		]
		def result
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = createEvent(cmd, item1)
		}
		else {
			item1.displayed = displayed(description, item1.isStateChange)
			result = [item1]
		}
		log.debug "Parse returned ${result?.descriptionText}"
		result
	}
	

	def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
		def result = doCreateEvent(cmd, item1)
		for (int i = 0; i < result.size(); i++) {
	  	result[i].type = "physical"
		}
		log.trace "BasicReport"
	  result
	}
	

	def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
		def result = doCreateEvent(cmd, item1)
		for (int i = 0; i < result.size(); i++) {
			result[i].type = "physical"
		}
		log.trace "BasicSet"
		result
	}
	

	def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
		[]
		log.trace "StartLevel"
	}
	

	def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
		[response(zwave.basicV1.basicGet())]
	}
	

	def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
		def result = doCreateEvent(cmd, item1)
		for (int i = 0; i < result.size(); i++) {
			result[i].type = "physical"
		}
		log.trace "SwitchMultiLevelSet"
		result
	}
	

	def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
		def result = doCreateEvent(cmd, item1)
		result[0].descriptionText = "${item1.linkText} is ${item1.value}"
		result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
		for (int i = 0; i < result.size(); i++) {
			result[i].type = "digital"
		}
		log.trace "SwitchMultilevelReport"
		result
	}
	

	def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
		def result = [item1]
		def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
		def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
		def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99
	

		item1.name = "switch"
		item1.value = cmd.value ? "on" : "off"
		if (item1.value == "off") {
			sendEvent(name: "currentState", value: "OFF" as String)
		}
		item1.handlerName = item1.value
		item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
		item1.canBeCurrentState = true
		item1.isStateChange = isStateChange(device, item1.name, item1.value)
		item1.displayed = false
	

		if (cmd.value) {
			def item2 = new LinkedHashMap(item1)
			item2.name = "level"
			item2.value = cmd.value as String
			item2.unit = "%"
			item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
			item2.canBeCurrentState = true
			item2.isStateChange = isStateChange(device, item2.name, item2.value)
			item2.displayed = false
	        
	        if (item2.value.toInteger() == lowThresholdvalue) { sendEvent(name: "currentState", value: "LOW" as String) } 
	        else if (item2.value.toInteger() == medThresholdvalue) { sendEvent(name: "currentState", value: "MED" as String) } 
	        else if (item2.value.toInteger() == highThresholdvalue) { sendEvent(name: "currentState", value: "HIGH" as String) } 
	        else if (item2.value.toInteger() == 0) { sendEvent(name: "currentState", value: "OFF" as String) } 
	        else { sendEvent(name: "currentState", value: "on" as String) } 
	

			result << item2
		}
		log.trace "doCreateEvent"
		result
	}
	

	def createEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd, Map map) {
	//def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
		def value = "when off"
		log.trace "ConfigurationReport"
		if (cmd.configurationValue[0] == 1) {value = "when on"}
		if (cmd.configurationValue[0] == 2) {value = "never"}
		[name: "indicatorStatus", value: value, display: false]
	}
	

	def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
		// Handles any Z-Wave commands we aren't interested in
		log.debug "UNHANDLED COMMAND $cmd"
	}
	

	def on() {
		log.info "on"
		delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
	}
	

	def off() {
		log.info "off"
		delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
	}
	

	def setLevel(value) {
		def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
		def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
		def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99
		
		if (value == "LOW") { value = lowThresholdvalue }
		if (value == "MED") { value = medThresholdvalue }
		if (value == "HIGH") { value = highThresholdvalue }
	

		def level = Math.min(value as Integer, 99)
	    
	    if (level == lowThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.LOW" as String, displayed: false) } 
	    else if (level == medThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.MED" as String, displayed: false) } 
	    else if (level == highThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.HIGH" as String, displayed: false) } 
	    else if (level == 0) { sendEvent(name: "currentState", value: "OFF" as String, displayed: false) } 
	    else { sendEvent(name: "currentState", value: "on" as String, displayed: false) } 
	    
	    sendEvent(name: "level", value: level, unit: "%")
	    
	    log.trace "setLevelValue: ${value}"
	    
	    delayBetween ([zwave.basicV1.basicSet(value: level as Integer).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
	}
	

	def resetLevel(value) {
	    def level = Math.min(value as Integer, 99)
		delayBetween ([
	    	delayBetween ([	zwave.basicV1.basicSet(value: level).format(),
	        				zwave.basicV1.basicSet(value: 0x00).format()], 10),	
	    	zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
	}
	

	def setLevel(value, duration) {
		def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
		def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
		def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99
	

		if (value == "LOW") { value = lowThresholdvalue }
		if (value == "MED") { value = medThresholdvalue }
		if (value == "HIGH") { value = highThresholdvalue }
	

		def level = Math.min(value as Integer, 99)
		def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	    
	    if (level == lowThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.LOW" as String, displayed: false) } 
	    else if (level == medThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.MED" as String, displayed: false) } 
	    else if (level == highThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.HIGH" as String, displayed: false) }
	    else if (level == 0) { sendEvent(name: "currentState", value: "OFF" as String, displayed: false) } 
	    else { sendEvent(name: "currentState", value: "on" as String, displayed: false) } 
	

	    log.trace "setLevelValueDuration: ${value}"
	    
		zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
	}
	

	def levelUp(){
	

		def steppervalue = (settings.stepper != null && settings.stepper != "") ? settings.stepper.toInteger() : 10
	    int nextLevel = device.currentValue("level") + steppervalue
	    sendEvent(name: "currentState", value: "default" as String, displayed: false)  
		    
		    if( nextLevel > 100){
		    	nextLevel = 100
		    }
	        
			log.trace "setLevelUp(value): ${level}"
		    log.debug "Setting dimmer level up to: ${nextLevel}"
		    delayBetween ([zwave.basicV1.basicSet(value: nextLevel).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1500)
		}
		
	def levelDown(){
	

		def steppervalue = (settings.stepper != null && settings.stepper != "") ? settings.stepper.toInteger() : 10
	    int nextLevel = device.currentValue("level") - steppervalue
		sendEvent(name: "currentState", value: "default" as String, displayed: false)  
	    
		    if (nextLevel < 0){
		    	nextLevel = 0
		    }
		    
		    if (nextLevel == 0){
		    	off()
		    }
	        
		    else
		    {
	        	log.trace "setLevelDown(value): ${level}"
		    	log.debug "Setting dimmer level down to: ${nextLevel}"
		        delayBetween ([zwave.basicV1.basicSet(value: nextLevel).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1500)
	    }
	}
	

	def low() {
		log.trace "setLow"
		setLevel("LOW")
	}
	

	def med() {
		log.trace "setMed"
		setLevel("MED")
	}
	

	def high() {
		log.trace "setHigh"
		setLevel("HIGH")
	}
	

	def poll() {
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	}
	

	def refresh() {
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	}
	

	def indicatorWhenOn() {
		sendEvent(name: "indicatorStatus", value: "when on", display: false)
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
	}
	

	def indicatorWhenOff() {
		sendEvent(name: "indicatorStatus", value: "when off", display: false)
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
	}
	

	def indicatorNever() {
		sendEvent(name: "indicatorStatus", value: "never", display: false)
		zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
	}