/**
 *  Set ZXT-120 Mode on temp
 *
 *  Author: Ronald Gouldner
 */
definition(
    name: "Set ZXT-120 Mode on temp",
    namespace: "gouldner",
    author: "Ronald Gouldner",
    description: "Set ZXT-120 Mode based on temp sensor.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Choose a temperature sensor... "){
		input "sensor", "capability.temperatureMeasurement", title: "Sensor"
	}
	section("Select the ZXT-120 Device... "){
		input "thermostat", "capability.Thermostat", title: "ZXT-120"
	}
	section("Set the desired trigger temperature...(Over=Heat/CoolOff,Under=Cool/HeatOff"){
		input "setpoint", "decimal", title: "Set Temp"
	}
	section("Set Mode"){
		input "mode", "enum", title: "Mode?", options: ["heat","cool","CoolOff","HeatOff"]
	}
    section("Notify with Push Notification"){
		input "pushNotify", "enum", title: "Send Push notification ?", options: ["yes","no"]
	}
}

def installed()
{
	subscribe(sensor, "temperature", temperatureHandler)
}

def updated()
{
    log.debug "updated  subscribing to sensor:${sensor.label ?: sensor.name}"
	unsubscribe()
	subscribe(sensor, "temperature", temperatureHandler)
}

def temperatureHandler(evt)
{	
	evaluate(evt.doubleValue, setpoint)
}

private evaluate(currentTemp, desiredTemp)
{
	log.debug "EVALUATE Current Temp:$currentTemp, DesiredTemp:$desiredTemp mode:$mode"
	def threshold = 1.0
	if (mode == "cool" || mode == "heatOff") {
		// air conditioner
		if (currentTemp - desiredTemp >= threshold) {
			if (mode == "cool") {
				log.debug "Turning on cool mode"
				thermostat.cool()
			} else {
			    log.debug "Turning Off Heat Mode"
				thermostat.off()
			}
			sendNotificationWithMode(mode)
		} else {
            log.debug "Threshold not met mode:$mode"
        }
	}
	if (mode == "heat" || mode == "coolOff") {
		// heater
		if (desiredTemp - currentTemp >= threshold) {
			if (mode == "heat") {
				log.debug "Turning on Heat Mode"
				thermostat.heat()
			} else {
			    log.debug "Turning off Cool Mode"
			    thermostat.off()
			}
			sendNotificationWithMode(mode)
		} else {
            log.debug "Threshold not met mode:$mode"
        }
	}
}

private sendNotificationWithMode(mode) {
	if (pushNotify == "yes") {
		sendPush("${thermostat.label ?: thermostat.name} mode changed to $mode")
	}
}