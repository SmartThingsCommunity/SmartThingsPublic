/**
 * DirecThings v0.31
 *
 * Created by Joe Beeson <jbeeson@gmail.com>
 *
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
definition(
	author	   : "Joe Beeson",
	category   : "Convenience",
	description: "Control your DirecTV receiver",
	iconUrl    : "http://a4.mzstatic.com/us/r30/Purple18/v4/41/93/a8/4193a8ee-0863-0458-7d03-fd050517c400/icon175x175.jpeg",
	iconX2Url  : "http://a4.mzstatic.com/us/r30/Purple18/v4/41/93/a8/4193a8ee-0863-0458-7d03-fd050517c400/icon175x175.jpeg",
	iconX3Url  : "http://a4.mzstatic.com/us/r30/Purple18/v4/41/93/a8/4193a8ee-0863-0458-7d03-fd050517c400/icon175x175.jpeg",
	name	   : "DirecThings",
	namespace  : "joebeeson"
)

preferences {
	section("Receiver") {
		href(
			description: "For help with setting up your receivers to be remotely controlled, click here.",
			name	   : "hrefNotRequired",
			required   : false,
			style      : "external",
			title      : "Receiver remote control help",
			url 	   : "https://support.directv.com/app/answers/detail/a_id/3150/~/i%E2%80%99m-having-trouble-with-the-receiver-control-feature-on-my-directv-app.-what"
		)
		input(
			name	: "receiverIpStr", 
			required: true,
			type	: "text", 
			title   : "IP Address"
		)
		input(
			defaultValue: 8080,
			name	    : "receiverPortInt", 
			range	    : "1..65535",
			required    : true,
			type        : "number", 
			title	    : "Port"
		)
		paragraph "You only need to set the below field if attempting to control a client (not primary) receiver."
		input(
			defaulValue: "0", 
			name	   : "receiverMacStr", 
			required   : false,
			type	   : "text", 
			title	   : "MAC Address"
		)
	}
	section("Control power with this switch") {
		input(
			name	: "monitorSwitchObj", 
			required: false,
			type	: "capability.switch"
		)
	}
	section("Control channel with this thermostat") {
		paragraph "If set, the setpoint of the thermostat will be used to control the channel and turn the receiver on."
		input(
			name	: "monitorThermostatObj",
			required: false,
			type	: "capability.thermostat"
		)
	}
}

/**
 * Using a Switch "Off" event, send the "poweroff" key.
 *
 * @param	eventWrapperObj		EventWrapper object.
 */
def doHandleSwitchOffEvent(eventWrapperObj) {
	doReceiverRequestKey("poweroff")
}

/**
 * Using a Switch "On" event, send the "poweron" key.
 *
 * @param	eventWrapperObj		EventWrapper object.
 */
def doHandleSwitchOnEvent(eventWrapperObj) {
	doReceiverRequestKey("poweron")
}

/**
 * Using a Thermostat event, set the channel from the setpoint value.
 *
 * @param	eventWrapperObj		EventWrapper object.
 */
def doHandleThermostatSetpointEvent(eventWrapperObj) {
	def thermostatSetpointStr = settings.monitorThermostatObj.currentValue("thermostatSetpoint").toString()
    if (thermostatSetpointStr.indexOf(".1") == -1) {
    	def thermostatSetpointInt = thermostatSetpointStr.toBigDecimal().toBigInteger()
        doHandleSwitchOnEvent(eventWrapperObj)
        doReceiverRequestTune(thermostatSetpointInt.toString())
        settings.monitorThermostatObj.setHeatingSetpoint(thermostatSetpointInt + 0.1)
        settings.monitorThermostatObj.setCoolingSetpoint(thermostatSetpointInt + 0.1)
	}
}

/**
 * Sends a receiver request to press a key.
 *
 * @param	keyNameStr			Name of the key to send.
 * @see		doReceiverRequest
 */
def doReceiverRequestKey(keyNameStr) {
	log.debug "[doReceiverRequestKey] Sending key '${keyNameStr}'"
	return doReceiverRequest(
		"/remote/processKey",
		[
			hold: "keyPress",
			key : keyNameStr
		]
	)
}

/**
 * Sends a tuning request.
 *
 * @param	channelNumberStr	The channel number in either "###" or "###-###" format.
 */
def doReceiverRequestTune(channelNumberStr) {
	channelNumberStr = channelNumberStr.tokenize(".")[0]
	def channelMajorStr = channelNumberStr.tokenize("-")[0]
	def channelMinorStr = channelNumberStr.tokenize("-")[1]
	doReceiverRequest(
		"/tv/tune",
		[
			major: channelMajorStr,
			minor: (channelMinorStr ?: "65535")
		]
	)
}

/**
 * Send a receiver request.
 *
 * @param	requestUrlStr		The path, without query parameters, to send. Required.
 * @param	requestQueryMap		Query parameters to send. Default empty.
 * @param	requestMethodStr	HTTP Method to use for the request. Default "GET"
 */
def doReceiverRequest(requestUrlStr, requestQueryMap = [], requestMethodStr = "GET") {
	log.debug "[doReceiverRequest] Requesting '${requestUrlStr} with query " +
			  "'${requestQueryMap}' as '${requestMethodStr}' to " + 
			  "'${settings.receiverIpStr}:${receiverPortInt}'"
	return sendHubCommand(
		new physicalgraph.device.HubAction(
			headers: [
				HOST: "${settings.receiverIpStr}:${receiverPortInt}"
			],
			method : requestMethodStr,
			path   : requestUrlStr,
			query  : requestQueryMap + [
				clientAddr: (settings.receiverMacStr ?: "0").replace(":", "").toUpperCase()
			]
		)
	)
}

def installed() {
	log.debug "[installed] Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "[updated] Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	if (settings.monitorSwitchObj) {
		subscribe(settings.monitorSwitchObj, "switch.on", "doHandleSwitchOnEvent")
		subscribe(settings.monitorSwitchObj, "switch.off", "doHandleSwitchOffEvent")
	}
	if (settings.monitorThermostatObj) {
		subscribe(settings.monitorThermostatObj, "thermostatSetpoint", "doHandleThermostatSetpointEvent")
	}
}