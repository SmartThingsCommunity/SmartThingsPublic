definition(
    name: "Active Humidity Control",
    namespace: "smartthings",
    author: "Santosh Nair (smartsanty@gmail.com)",
    description: "Control the humidity by turning on switches (e.g exhaust/fans) when it rises above the high threshold and off when it falls below the low threshold.",
    category: "Convenience",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather9-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather9-icn?displaySize=2x",
    pausable: true
)

preferences {
	section("Monitor the humidity of") {
		input "humiditySensor", "capability.relativeHumidityMeasurement", required: true, title: "Humidity Sensor"
	}
	section("Humidity Range") {
		input "lowHumidity", "number", title: "Low %", required: true
        input "highHumidity", "number", title: "High %", required: true
	}
	section("Control these switches") {
		input "switches", "capability.switch", multiple: true, required: true, title: "Switches"
	}
}

def installed() {
	state.switchState = "unknown"
	unschedule(humidityCheckerSchedule)
	subscribe(humiditySensor, "humidity", humidityHandler)
    runIn(1*60, humidityCheckerSchedule)
    //schedule("? 0/3 * * * ?", humidityCheckerSchedule)
}

def updated() {
	state.switchState = "unknown"
	unschedule(humidityCheckerSchedule)
    unsubscribe()
	subscribe(humiditySensor, "humidity", humidityHandler)
    runIn(1*60, humidityCheckerSchedule)
    //schedule("? 0/3 * * * ?", humidityCheckerSchedule)
}

def humidityHandler(evt) {
	switchesControl(evt.value as Double)
}

def humidityCheckerSchedule() {
   	switchesControl(humiditySensor.currentHumidity as Double)
    runIn(3*60, humidityCheckerSchedule)
}

def switchesControl(currentHumidity) {
	if (currentHumidity) {
        if (state.switchState != "on" && currentHumidity > highHumidity) {
            log.debug "Humidity Rose Above ${highHumidity}, turning ON ${settings.switches}"
            switches.on()
            state.switchState = "on"
        } else if (state.switchState != "off" && currentHumidity < lowHumidity) {
            log.debug "Humidity Fell Below ${lowHumidity}, turning OFF ${settings.switches}"
            switches.off()
            state.switchState = "off"
        }
    } else {
    	log.trace "Humidity level not detected from ${humiditySensor.label}"
    }
}