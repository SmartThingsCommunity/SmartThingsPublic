/**
 * HubConnect Remote Client for SmartThings
 *
 * Copyright 2019-2020 Steve White, Retail Media Concepts LLC.
 *
 * HubConnect for Hubitat is a software package created and licensed by Retail Media Concepts LLC.
 * HubConnect, along with associated elements, including but not limited to online and/or electronic documentation are
 * protected by international laws and treaties governing intellectual property rights.
 *
 * This software has been licensed to you. All rights are reserved. You may use and/or modify the software.
 * You may not sublicense or distribute this software or any modifications to third parties in any way.
 *
 * By downloading, installing, and/or executing this software you hereby agree to the terms and conditions set forth in the HubConnect license agreement.
 * <https://hubconnect.to/knowledgebase/5/HubConnect-License-Agreement.html>
 *
 * Hubitat is the trademark and intellectual property of Hubitat, Inc. Retail Media Concepts LLC has no formal or informal affiliations or relationships with Hubitat.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License Agreement
 * for the specific language governing permissions and limitations under the License.
 *
 */
Map getAppVersion() {[platform: "SmartThings", major: 2, minor: 0, build: 9800]}

import groovy.transform.Field
import groovy.json.JsonOutput
include 'asynchttp_v1'

definition(
	name: "HubConnect Remote Client",
	namespace: "shackrat",
	author: "Steve White",
	description: "Synchronizes devices and events across hubs..",
	category: "My Apps",
	iconUrl: "https://hubconnect.to/kbimages/hubconnect-logo.png",
	iconX2Url: "https://hubconnect.to/kbimages/hubconnect-logo.png",
	iconX3Url: "https://hubconnect.to/kbimages/hubconnect-logo.png"
)


// Preferences pages
preferences
{
	page(name: "mainPage")
	page(name: "connectPage")
	page(name: "devicePage")
	page(name: "sensorsPage")
	page(name: "customDevicePage")
	page(name: "shmConfigPage")
	page(name: "dynamicDevicePage")
	page(name: "upgradePage")
	page(name: "discoverPage")
	page(name: "connectFailPage")
	page(name: "connectWizard_DiscoverPage")
	page(name: "connectWizard_KeyPage")
	page(name: "uninstallPage")
	page(name: "resetPage")
}


// Map containing driver and attribute definitions for each device class
@Field static Map<String, Map> NATIVE_DEVICES =
[
	"arlocamera":		[driver: "Arlo Camera", displayName: "Arlo Pro Cameras", platform: "SmartThings", selector: "arloProCameras", capability: "device.ArloProCamera", prefGroup: "smartthings", type: "attr", attr: ["switch", "motion", "sound", "rssi", "battery"]],
	"arloqcamera":		[driver: "Arlo Camera", displayName: "Arlo Q Cameras", platform: "SmartThings", selector: "arloQCameras", capability: "device.ArloQCamera", prefGroup: "smartthings", type: "attr", attr: ["switch", "motion", "sound", "rssi", "battery"]],
	"arlogocamera":		[driver: "Arlo Camera", displayName: "Arlo Go Cameras", platform: "SmartThings", selector: "arloGoCameras", capability: "device.ArloGoCamera", prefGroup: "smartthings", type: "attr", attr: ["switch", "motion", "sound", "rssi", "battery"]],
	"arrival":			[driver: "Arrival Sensor", selector: "smartThingsArrival", capability: "presenceSensor", prefGroup: "presence", type: "attr", attr: ["presence", "battery", "tone"]],
	"audioVolume":		[driver: "AVR", selector: "audioVolume", capability: "audioVolume", prefGroup: "audio", type: "attr", attr: ["switch", "mediaInputSource", "mute", "volume"]],
	"bulb":				[driver: "Bulb", selector: "genericBulbs", capability: "changeLevel", prefGroup: "lighting", type: "attr", attr: ["switch", "level"]],
	"button":			[driver: "Button", selector: "genericButtons", capability: "button", prefGroup: "other", type: "attr", attr: ["numberOfButtons", "pushed", "held", "doubleTapped", "button", "temperature", "battery"]],
	"contact":			[driver: "Contact Sensor", selector: "genericContacts", capability: "contactSensor", prefGroup: "sensors", type: "attr", attr: ["contact", "temperature", "battery"]],
	"dimmer":			[driver: "Dimmer", selector: "genericDimmers", capability: "switchLevel", prefGroup: "lighting", type: "attr", attr: ["switch", "level"]],
	"domemotion":		[driver: "DomeMotion Sensor", selector: "domeMotions", capability: "motionSensor", prefGroup: "sensors", type: "attr", attr: ["motion", "temperature", "illuminance", "battery"]],
	"energy":			[driver: "Energy Meter", selector: "energyMeters", capability: "energyMeter", prefGroup: "power", type: "attr", attr: ["energy"]],
	"energyplug":		[driver: "DomeAeon Plug", selector: "energyPlugs", capability: "energyMeter", prefGroup: "lighting", type: "attr", attr: ["switch", "power", "voltage", "current", "energy", "acceleration"]],
	"fancontrol":		[driver: "Fan Controller", selector: "fanControl", capability: "fanControl", prefGroup: "fans", type: "attr", attr: ["speed"]],
	"fanspeed":			[driver: "FanSpeed Controller", selector: "fanSpeedControl", capability: "fanControl", prefGroup: "fans", type: "attr", attr: ["speed"]],
	"garagedoor":		[driver: "Garage Door", selector: "garageDoors", capability: "garageDoorControl", prefGroup: "other", type: "attr", attr: ["door", "contact"]],
	"irissmartplug":	[driver: "Iris SmartPlug", selector: "smartPlugs", capability: "switch", prefGroup: "shackrat", type: "synth", attr: ["switch", "power", "voltage", "ACFrequency"]],
	"irisv3motion":		[driver: "Iris IL071 Motion Sensor", selector: "irisV3Motions", capability: "motionSensor", prefGroup: "shackrat", type: "synth", attr: ["motion", "temperature", "humidity", "battery"]],
	"keypad":			[driver: "Keypad", selector: "genericKeypads", capability: "securityKeypad", prefGroup: "safety", type: "attr", attr: ["motion", "temperature", "battery", "tamper", "alarm", "lastCodeName"]],
	"lock":				[driver: "Lock", selector: "genericLocks", capability: "lock", prefGroup: "safety", type: "attr", attr: ["lock", "lockCodes", "lastCodeName", "codeChanged", "codeLength", "maxCodes", "battery"]],
	"mobileApp":		[driver: "Mobile App", selector: "mobileApp", capability: "notification", prefGroup: "presence", type: "attr", attr: ["presence", "notificationText"]],
	"moisture":			[driver: "Moisture Sensor", selector: "genericMoistures", capability: "waterSensor", prefGroup: "safety", type: "attr", attr: ["water", "temperature", "battery"]],
	"motion":			[driver: "Motion Sensor", selector: "genericMotions", capability: "motionSensor", prefGroup: "sensors", type: "attr", attr: ["motion", "temperature", "battery"]],
	"motionswitch":		[driver: "Motion Switch", selector: "genericMotionSwitches", capability: "switch", prefGroup: "lighting", type: "attr", attr: ["switch", "motion"]],
	"multipurpose":		[driver: "Multipurpose Sensor", selector: "genericMultipurposes", capability: "accelerationSensor", prefGroup: "sensors", type: "attr", attr: ["contact", "temperature", "battery", "acceleration", "threeAxis"]],
	"netatmowxbase":	[driver: "Netatmo Community Basestation", selector: "netatmoWxBasetations", capability: "relativeHumidityMeasurement", prefGroup: "netatmowx", type: "synth", attr: ["temperature", "humidity", "pressure", "carbonDioxide", "soundPressureLevel", "sound", "min_temp", "max_temp", "temp_trend", "pressure_trend"]],
	"netatmowxmodule":	[driver: "Netatmo Community Additional Module", selector: "netatmoWxModule", capability: "relativeHumidityMeasurement", prefGroup: "netatmowx", type: "synth", attr: ["temperature", "humidity", "carbonDioxide", "min_temp", "max_temp", "temp_trend", "battery"]],
	"netatmowxoutdoor":	[driver: "Netatmo Community Outdoor Module", selector: "netatmoWxOutdoor", capability: "relativeHumidityMeasurement", prefGroup: "netatmowx", type: "synth", attr: ["temperature", "humidity", "min_temp", "max_temp", "temp_trend", "battery"]],
	"netatmowxrain":	[driver: "Netatmo Community Rain", selector: "netatmoWxRain", capability: "sensor", prefGroup: "netatmowx", type: "synth", attr: ["rain", "rainSumHour", "rainSumDay", "units", "battery"]],
	"netatmowxwind":	[driver: "Netatmo Community Wind", selector: "netatmoWxWind", capability: "sensor", prefGroup: "netatmowx", type: "synth", attr: ["WindStrength", "WindAngle", "GustStrength", "GustAngle", "max_wind_str", "date_max_wind_str", "units", "battery"]],
	"omnipurpose":		[driver: "Omnipurpose Sensor", selector: "genericOmnipurposes", capability: "relativeHumidityMeasurement", prefGroup: "sensors", type: "attr", attr: ["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "tamper", "battery"]],
	"pocketsocket":		[driver: "Pocket Socket", selector: "pocketSockets", capability: "switch", prefGroup: "lighting", type: "attr", attr: ["switch", "power"]],
	"power":			[driver: "Power Meter", selector: "powerMeters", capability: "powerMeter", prefGroup: "power", type: "attr", attr: ["power"]],
	"presence":			[driver: "Presence Sensor", selector: "genericPresences", capability: "presenceSensor", prefGroup: "presence", type: "attr", attr: ["presence", "battery"]],
	"ringdoorbell":		[driver: "Ring Doorbell", selector: "ringDoorbellPros", capability: "device.RingDoorbellPro", prefGroup: "smartthings", type: "attr", attr: ["numberOfButtons", "pushed", "motion"]],
	"rgbbulb":			[driver: "RGB Bulb", selector: "genericRGBs", capability: "colorControl", prefGroup: "lighting", type: "attr", attr: ["switch", "level", "hue", "saturation", "RGB", "color", "colorMode", "colorTemperature"]],
	"rgbwbulb":			[driver: "RGBW Bulb", selector: "genericRGBW", capability: "colorMode", prefGroup: "lighting", type: "attr", attr: ["switch", "level", "hue", "saturation", "RGB(w)", "color", "colorMode", "colorTemperature"]],
	"shock":			[driver: "Shock Sensor", selector: "genericShocks", capability: "shockSensor", prefGroup: "sensors", type: "attr", attr: ["shock", "battery"]],
	"siren":			[driver: "Siren", selector: "genericSirens", capability: "alarm", prefGroup: "safety", type: "attr", attr: ["switch", "alarm", "battery"]],
	"smartsmoke":		[driver: "Smart SmokeCO", selector: "smartSmokeCO", capability: "device.HaloSmokeAlarm", prefGroup: "safety", type: "attr", attr: ["smoke", "carbonMonoxide", "battery", "temperature", "humidity", "switch", "level", "hue", "saturation", "pressure"]],
	"smoke":			[driver: "SmokeCO", selector: "genericSmokeCO", capability: "smokeDetector", prefGroup: "safety", type: "attr", attr: ["smoke", "carbonMonoxide", "battery"]],
	"speaker":			[driver: "Speaker", selector: "genericSpeakers", capability: "musicPlayer", prefGroup: "audio", type: "attr", attr: ["level", "mute", "volume", "status", "trackData", "trackDescription"]],
	"speechSynthesis":	[driver: "SpeechSynthesis", selector: "speechSynth", capability: "speechSynthesis", prefGroup: "other", type: "attr", attr: ["mute", "version", "volume"]],
	"switch":			[driver: "Switch", selector: "genericSwitches", capability: "switch", prefGroup: "lighting", type: "attr", attr: ["switch"]],
	"thermostat":		[driver: "Thermostat", selector: "genericThermostats", capability: "thermostat", prefGroup: "climate", type: "attr", attr: ["coolingSetpoint", "heatingSetpoint", "schedule", "supportedThermostatFanModes", "supportedThermostatModes", "temperature", "thermostatFanMode", "thermostatMode", "thermostatOperatingState", "thermostatSetpoint"]],
	"windowshade":		[driver: "Window Shade", selector: "windowShades", capability: "windowShade", prefGroup: "other", type: "attr", attr: ["switch", "position", "windowShade"]],
	"valve":			[driver: "Valve", selector: "genericValves", capability: "valve", prefGroup: "other", type: "attr", attr: ["valve"]],
	"v_acceleration":	[driver: "Virtual Acceleration Sensor", selector: "virtualAcceleration", capability: "accelerationSensor", prefGroup: "virtual", type: "synth", attr: ["acceleration"]],
	"v_audiovolume":	[driver: "Virtual audioVolume", selector: "virtualAudioVolume", capability: "audioVolume", prefGroup: "virtual", type: "synth", attr: ["volume", "mute"]],
	"v_co_detector":	[driver: "Virtual CO Detector", selector: "virtualCO", capability: "carbonMonoxideDetector", prefGroup: "virtual", type: "synth", attr: ["carbonMonoxide"]],
	"v_contact":		[driver: "Virtual Contact Sensor", selector: "virtualContact", capability: "contactSensor", prefGroup: "virtual", type: "synth", attr: ["contact"]],
	"v_humidity":		[driver: "Virtual Virtual Humidity Sensor", selector: "virtualHumidity", capability: "relativeHumidityMeasurement", prefGroup: "virtual", type: "synth", attr: ["humidity"]],
	"v_illuminance":	[driver: "Virtual Illuminance Sensor", selector: "virtualIlluminance", capability: "illuminanceMeasurement", prefGroup: "virtual", type: "synth", attr: ["illuminance"]],
	"v_moisture":		[driver: "Virtual Moisture Sensor", selector: "virtualMoisture", capability: "waterSensor", prefGroup: "virtual", type: "synth", attr: ["water"]],
	"v_motion":			[driver: "Virtual Motion Sensor", selector: "virtualMotion", capability: "motionSensor", prefGroup: "virtual", type: "synth", attr: ["motion"]],
	"v_multi":			[driver: "Virtual Multi Sensor", selector: "virtualMulti", capability: "accelerationSensor", prefGroup: "virtual", type: "synth", attr: ["acceleration", "contact", "temperature"]],
	"v_omni":			[driver: "Virtual Omni Sensor", selector: "virtualOmni", capability: "carbonDioxideMeasurement", prefGroup: "virtual", type: "synth", attr: ["acceleration", "carbonDioxide", "carbonMonoxide", "contact", "energy", "humidity", "illuminance", "power", "presence", "smoke", "temperature", "water"]],
	"v_presence":		[driver: "Virtual Presence Sensor", selector: "virtualPresence", capability: "presenceSensor", prefGroup: "virtual", type: "synth", attr: ["presence"]],
	"v_smoke_detector":	[driver: "Virtual Smoke Detector", selector: "virtualSmoke", capability: "smokeDetector", prefGroup: "virtual", type: "synth", attr: ["smoke"]],
	"v_temperature":	[driver: "Virtual Temperature Sensor", selector: "virtualTemperature", capability: "temperatureMeasurement", prefGroup: "virtual", type: "synth", attr: ["temperature"]],
	"vc_globalvar":		[driver: "RM Global Variable Connector", platform: "Hubitat", selector: "virtualGlobVar", capability: "*", prefGroup: "virtual", synthetic: true, attr: ["sensor"]],
	"zwaverepeater":	[driver: "Iris Z-Wave Repeater", selector: "zwaveRepeaters", capability: "device.IrisZ-WaveRepeater", prefGroup: "shackrat", type: "synth", attr: ["status", "lastRefresh", "deviceMSR", "deviceVersion", "deviceZWaveLibType", "deviceZWaveVersion", "lastMsgRcvd"]],

	// HubConnect Apps
	"automatic":		[driver: "Automatic Vehicle", selector: "automaticVehicles", capability: "presenceSensor", prefGroup: "hcautomatic", type: "hcapp", attr: ["presence"]],
	"NAMain":			[driver: "Netatmo Basestation", selector: "hcnetatmowxBasetations", capability: "relativeHumidityMeasurement", prefGroup: "hcnetatmowx", type: "hcapp", attr: ["temperature", "humidity", "pressure", "carbonDioxide", "soundPressureLevel", "sound", "lowTemperature", "highTemperature", "temperatureTrend", "pressureTrend"]],
	"NAModule1":		[driver: "Netatmo Outdoor Module", selector: "hcnetatmowxOutdoor", capability: "relativeHumidityMeasurement", prefGroup: "hcnetatmowx", type: "hcapp", attr: ["temperature", "humidity", "lowTemperature", "highTemperature", "temperatureTrend", "battery"]],
	"NAModule2":		[driver: "Netatmo Wind", selector: "hcnetatmowxWind", capability: "sensor", prefGroup: "hcnetatmowx", type: "hcapp", attr: ["WindStrength", "WindAngle", "GustStrength", "GustAngle", "max_wind_str", "date_max_wind_str", "units", "battery"]],
	"NAModule3":		[driver: "Netatmo Rain", selector: "hcnetatmowxRain", capability: "sensor", prefGroup: "hcnetatmowx", type: "hcapp", attr: ["rain", "rainSumHour", "rainSumDay", "units", "battery"]],
	"NAModule4":		[driver: "Netatmo Additional Module", selector: "hcnetatmowxModule", capability: "relativeHumidityMeasurement", prefGroup: "hcnetatmowx", type: "hcapp", attr: ["temperature", "humidity", "carbonDioxide", "lowTemperature", "highTemperature", "temperatureTrend", "battery"]]
]

// Map containing device group definitions
// NOTE: The parent: property is not supported because the SmartThings UI Cannot deal with with the extra layer of nested page calls.
@Field static Map<String, Map> DEVICE_GROUPS =
[
	"audio":		[title: "Audio Devices", description: "Speakers, AV Receivers"],
	"climate":		[title: "Climate Devices", description: "Thermostats & Weather Stations"],
	"fans":			[title: "Fans", description: "Celing Fans & Devices"],
	"lighting":		[title: "Lighting Devices", description: "Bulbs, Dimmers, RGB/RGBW Lights, and Switches"],
	"hcautomatic":	[title: "HubConnect Automatic Client", description: "HubConnect Automatic Driving Tracker Integration"],
	"hcnetatmowx":	[title: "HubConnect Netatmo Client", description: "HubConnect Netatmo Weather Station Client"],
	"netatmowx":	[title: "Netatmo Weather Station (Community)", description: "Netatmo Weather Station Community Integration"],
	"other":		[title: "Other Devices", description: "Presence, Button, Valves, Garage Doors, SpeechSynthesis, Window Shades"],
	"power":		[title: "Power & Energy Meters", description: "Power Meters & Energy Meters"],
	"presence":		[title: "Presence Sensors & Apps", description: "Arrival Sensors, Presence Sensors, and Mobile Apps"],
	"safety":		[title: "Safety & Security", description: "Locks, Keypads, Smoke & Carbon Monoxide, Leak, Sirens"],
	"sensors":		[title: "Sensors", description: "Contact, Mobile App, Motion, Multipurpose, Omnipurpose, Presence, Shock, GV Connector"],
	"shackrat":		[title: "Shackrat's Drivers", description: "Iris V3 Motion, Iris Smart Plug, Z-Wave Repeaters"],
	"virtual":		[title: "Virtual Devices", description: "Hubitat Virtual Devices"]
]


// Mapping to receive events - Note: ST can only support 4 path parts!
mappings
{
	// Client mappings
    path("/event/:deviceId/:deviceCommand/:commandParams")
	{
		action: [GET: "remoteDeviceCommand"]
	}
    path("/modes/get")
	{
		action: [GET: "getAllModes"]
	}
    path("/modes/set/:name")
	{
		action: [GET: "remoteModeChange"]
	}
    path("/hsm/get")
	{
		action: [GET: "getAllHSMStates"]
	}
    path("/hsm/set/:name")
	{
		action: [GET: "remoteHSMChange"]
	}

	// System endpoints
	path("/system/setCommStatus/:status")
	{
		action: [GET: "systemSetCommStatus"]
	}
    path("/system/setConnectString/:connectKey")
	{
		action: [GET: "systemSetConnectKey"]
	}
	path("/system/drivers/save")
	{
		action: [POST: "systemSaveCustomDrivers"]
	}
	path("/system/versions/get")
	{
		action: [GET: "systemGetVersions"]
	}
    path("/system/initialize")
	{
		action: [GET: "systemRemoteInitialize"]
	}
    path("/system/update")
	{
		action: [GET: "systemRemoteUpdate"]
	}
    path("/system/tsreport/get")
	{
		action: [GET: "systemGetTSReport"]
	}
    path("/system/disconnect")
	{
		action: [GET: "systemRemoteDisconnect"]
	}

	// Server mappings
    path("/devices/save")
	{
		action: [POST: "devicesSaveAll"]
	}
    path("/device/:deviceId/event/:event")
	{
		action: [GET: "deviceSendEvent"]
	}
    path("/device/:deviceId/sync/:type")
	{
		action: [GET: "getDeviceSync"]
	}
}


/*
	getDeviceSync

	Purpose:	Retrieves the current attribute and device name/label.

	URL Format: GET /device/:deviceId/sync/:type

	API:		https://hubconnect.to/knowledgebase/7/getDeviceSync.html
*/
def getDeviceSync() {getDeviceSync(params)}
def getDeviceSync(Map params)
{
	if (enableDebug) log.info "Received device update request from server: [${params.deviceId}, type ${params.type}]"

	def device = getDevice(params)
	if (device)
	{
		def currentAttributes = getAttributeMap(device, params.type)
		String label = device.label ?: device.name
		jsonResponse([status: "success", name: "${device.name}", label: "${label}", currentValues: currentAttributes])
	}
}


/*
	getDevice

	Purpose: Helper function to retreive a device from all groups of devices.
*/
def getDevice(Map params)
{
	def foundDevice = null

	NATIVE_DEVICES.each
	{
	  groupname, device ->
		if (foundDevice != null) return
		foundDevice = settings."${device.selector}"?.find{it.id == params.deviceId}
	}

	// Custom devices drivers
	if (foundDevice == null)
	{
		state.customDrivers?.each
		{
	 	  groupname, device ->
			if (foundDevice != null) return
			foundDevice = settings."custom_${groupname}".find{it.id == params.deviceId}
		}
	}
	foundDevice
}


/*
	remoteDeviceCommand

	Purpose: 	Executes a command on a device located on this hub.

	Parameters: params - (Map)

	URL Format:	GET /event/:deviceId/:deviceCommand/:commandParams

	API:		https://hubconnect.to/knowledgebase/6/remoteDeviceCommand.html
*/
def remoteDeviceCommand() {remoteDeviceCommand(params)}
def remoteDeviceCommand(Map params)
{
	List commandParams = params.commandParams != "null" ? parseJson(URLDecoder.decode(params.commandParams)) : []

	// Get the device
	def device = getDevice(params)
	if (device == null)
	{
		log.error "Could not locate a device with an id of ${params.deviceId}"
		return jsonResponse([status: "error"])
	}

	// DeleteSync: Uninstalling?
	if (params.deviceCommand == "uninstalled")
	{
		def driverDef = NATIVE_DEVICES.findResults{groupname, driver -> settings?."${driver.selector}"?.findResults{ if (it.id == "${device.id}") return driver.selector }?.join() }?.join() ?:
			 state.customDrivers.each.findResults{groupname, driver -> settings?."custom_${groupname}"?.findResults{ if (it.id == "${device.id}") return driver.selector }?.join() }?.join()

		if (driverDef)
		{
			// "de-select" the device - this probably doesn't work on SmartThings
			def newSetting = settings?."${driverDef}"?.findResults{if (it.id != "${device.id}") return it.id}
			app.updateSetting("${driverDef}", [type: "capability", value: newSetting])
			if (enableDebug) log.info "Received device delete requet from client: [\"${device.label ?: device.name}]\"..  Sharing of this device has been disabled on this hub."
		}

		// Update subscriptions
		subscribeLocalEvents()

		return jsonResponse([status: "success"])
	}

	if (enableDebug) log.info "Received command from server: [\"${device.label ?: device.name}\": ${params.deviceCommand}]"

	String deviceCommand = params.deviceCommand

	// Fix for ST to HE lock code fetch
	if (deviceCommand == "getCodes" && !device.hasCommand(deviceCommand)) deviceCommand = "reloadAllCodes"

	// Fix for broken button ST drivers
	if (deviceCommand == "push" && !device.hasCommand("push"))
	{
		deviceCommand = "push${commandParams[0]}"
		commandParams = []
	}

	// Make sure the physical device supports the command
	if (!device.hasCommand(deviceCommand))
	{
		log.error "The device [${device.label ?: device.name}] does not support the command ${params.deviceCommand}."
		return jsonResponse([status: "error"])
	}

	// Execute the command
	device."${deviceCommand}"(*commandParams)

	jsonResponse([status: "success"])
}


/*
	remoteModeChange

	Purpose:	Executes a mode change on this hub.

	URL Format: GET /modes/set/:name

	API:		https://hubconnect.to/knowledgebase/9/remoteModeChange.html
*/
def remoteModeChange() {remoteModeChange(params)}
def remoteModeChange(Map params)
{
    String modeName = params?.name ? URLDecoder.decode(params?.name) : ""
	if (enableDebug) log.debug "Received mode event from server: ${modeName}"

	// Send mode status event to the remote hub device to update even if it's not defined on this hub
	if (hubDevice != null) hubDevice.sendEvent([name: "modeStatus", value: modeName])

    if (location.modes?.find{it.name == modeName})
	{
        setLocationMode(modeName)
        jsonResponse([status: "complete"])
    }
	else
	{
		jsonResponse([status: "error"])
    }
}


/*
	remoteHSMChange

	Purpose:	Executes a SHM setArm command on this hub.

	URL Format: GET /hsm/set/:name

	API:		https://hubconnect.to/knowledgebase/11/remoteHSMChange.html
*/
def remoteHSMChange() {remoteHSMChange(params)}
def remoteHSMChange(Map params)
{
	String hsmState = params?.name ? URLDecoder.decode(params?.name) : ""

	// Send HSM status event to the remote hub device to update even if it's not configured on this hub
	if (hubDevice != null) hubDevice.sendEvent([name: "hsmStatus", value: hsmState])

	Map hsmToSHM =
	[
		armAway:	settings?.armAway,
		armHome:	settings?.armHome,
		armNight:	settings?.armNight,
		disarm:		"off"
	]

	if (hsmToSHM.find{it.key == hsmState})
	{
		String shmState = hsmToSHM?."${hsmState}"
		if (shmState != null)
		{
			if (enableDebug) log.debug "Received HSM/SHM event from server: ${hsmState}, setting SHM state to ${shmState}"
			sendLocationEvent(name: "alarmSystemStatus", value: shmState)
			jsonResponse([status: "complete"])
		}
		else
		{
			if (enableDebug) log.debug "HSM/SHM event error ${hsmState} SHM has not been configured for this alarm state."
			jsonResponse([status: "error"])
		}
	}
	else
	{
		log.error "Received HSM event from server: ${hsmState} does not exist!"
		jsonResponse([status: "error"])
	}
}


/*
	subscribeLocalEvents

	Purpose: Subscribes to all device events for all attribute returned by getSupportedAttributes()

	Notes: 	Thank god this isn't SmartThings, or this would time out after about 10 subscriptions!

*/
private void subscribeLocalEvents()
{
	log.info "Subscribing to events.."
	unsubscribe()

	NATIVE_DEVICES.each
	{
	  groupname, device ->
		def selectedDevices = settings."${device.selector}"
		if (selectedDevices?.size()) getSupportedAttributes(groupname).each
        {
        	switch (groupname)
            {
				case "button":
					subscribe(selectedDevices, it, buttonEventTranslator)
                	break
				case "lock":
				case "lockCodes":
					subscribe(selectedDevices, it, lockEventTranslator)
                	break
                default:
					subscribe(selectedDevices, it, realtimeEventHandler)
                	break
            }
		}
	}

	// Special handling for Smart Plugs & Power Meters - Kinda Kludgy
	if (!sp_EnablePower && smartPlugs?.size()) unsubscribe(smartPlugs, "power", realtimeEventHandler)
	if (!sp_EnableVolts && smartPlugs?.size()) unsubscribe(smartPlugs, "voltage", realtimeEventHandler)
	if (!pm_EnableVolts && powerMeters?.size()) unsubscribe(powerMeters, "voltage", realtimeEventHandler)

	// Custom defined drivers
	state.customDrivers?.each
	{
	  groupname, driver ->
		if (settings."custom_${groupname}"?.size()) getSupportedAttributes(groupname).each { subscribe(settings."custom_${groupname}", it, realtimeEventHandler) }
	}
}


/*
	buttonEventTranslator

	Purpose: Translates SmartThings button events into Hubitat button events.

	Platform: SmartThings Only
*/
def buttonEventTranslator(evt)
{
	def data = parseJson(evt.data)
    return realtimeEventHandler([name: evt.value, value: data.buttonNumber, unit: "", isStateChange: true, data: "", deviceId: evt.deviceId, device: evt.device])
}


/*
	lockEventTranslator

	Purpose: Translates SmartThings lock & lock code events into Hubitat lock events.

	Platform: SmartThings Only
*/
def lockEventTranslator(evt)
{
	Map codeEvent =
	[
		name:				evt.name,
		value:				null,
		displayName:		evt.displayName ?: (evt.device?.label ?: evt.device?.name),	// SmartThings will always populate displayName; Hubitat, not so much
		data:				[],
		unit:				null,
		deviceId: 			evt.deviceId,
        device:				evt.device
	]

    // If this is a lock/unlock event, then we need to send a "lastCodeName" event BEFORE we send the unlock event so that the value is populated
    // for any subscribers to the unlock event
    if ((evt.name == "lock") && (evt.value == "unlocked"))
    {
    	Map parsed = evt.data ? parseJson(evt.data) : [:]
        String method = parsed?.method ?: "unknown"
        codeEvent.name = "lastCodeName"

		// If not unlocked by the keypad, or no codeName is provided, then HE's "lastCodeName" attribute is invalid, so we have to clear it
        if ((method != "keypad") || (!parsed?.codeName))
        {
            codeEvent.value = null
        	// Send the "lastCodeName: null" codeEvent
            realtimeEventHandler(codeEvent)
        }

        // SmartThings' or the RBoy lock DTHs will provide the name of the unlocking user in [evt.data.codeName]
        else
        {
			codeEvent.value = parsed.codeName
        	// Send the "lastCodeName: name" codeEvent
            realtimeEventHandler(codeEvent)
        }

        // Now we send the original unlock evt (unmodified)
        return realtimeEventHandler(evt)
    }

    // Translate SmartThings lockCodes[id:codeName] to Hubitat lockCodes[id[name:codeName,code:""] format
    else if (evt.name == "lockCodes")
    {
    	Map codes = (Map) [:]
    	Map codeMap = parseJson(evt.value)		// Should be a String containing JSON like this "["1":"somebbody", "2":"somebody else", ...]"

        codeMap.each
        { id, codeName ->
        	// Send a blank code for each id, because SmartThings doesn't expose the actual codes as an attribute
        	codes << [(id as String):["name": (codeName as String), "code": ""]]
        }
		// Note that lockCodes value is actually a String, not raw JSON
		codeEvent.value = (codes.size() ? JsonOutput.toJson(codes) : evt.value)

        // Send the modified lockCodes codeEvent instead of the original evt
        return realtimeEventHandler(codeEvent)
    }

    // Not a lock event that we need to translate, so just send the original evt
	else return realtimeEventHandler(evt)
}


/*
	realtimeEventHandler

	Purpose: Event handler for all local device events.

	URL Format: /device/localDeviceId/event/name/value/unit

	Notes: Handles everything from this hub!
*/
void realtimeEventHandler(evt)
{
	if (state.commDisabled) return

	Map event =
	[
		name:			evt.name,
		value:			evt.value,
		unit:			evt.unit,
		displayName:	evt.displayName ?: (evt.device.label ?: evt.device.name),
		data:			evt.data
	]

	String data = URLEncoder.encode(JsonOutput.toJson(event), "UTF-8")

	if (enableDebug) log.debug "Sending event to server: ${evt.device?.label ?: evt.device?.name} [${evt.name}: ${evt.value} ${evt.unit}]"
	sendGetCommand("/device/${evt.deviceId}/event/${data}")
}


/*
	getAttributeMap

	Purpose: Returns a map of current attribute values for (device) with the device class (deviceType).

	Notes: Calls getSupportedAttributes() to obtain list of attributes.
*/
List getAttributeMap(Object device, String deviceClass)
{
	def deviceAttributes = getSupportedAttributes(deviceClass)
	List currentAttributes = []
	deviceAttributes.each
	{
		if (device.supportedAttributes.find{attr -> attr.toString() == it})	// Filter only attributes the device supports
		{
        	def value = device.currentValue("${it}")

        	// Lock code translation
        	if (it == "lockCodes")
            {
                Map codes = (Map) [:]
                Map codeMap = parseJson(value)	// Should be a String containing JSON like this "["1":"somebbody", "2":"somebody else", ...]"

                codeMap.each
                { id, codeName ->
                    // Send a blank code for each id, because SmartThings doesn't expose the actual codes as an attribute
                    codes << [(id as String):["name": (codeName as String), "code": ""]]
                }
                // Note that lockCodes value is actually a String, not raw JSON
                value = (codes.size() ? JsonOutput.toJson(codes) : value)
			}

			currentAttributes << [name: (String) "${it}", value: value, unit: it == "temperature" ? "°"+getTemperatureScale() : it == "power" ? "W" :  it == "voltage" ? "V" : ""]
		}
	}
	return currentAttributes
}


/*
	getSupportedAttributes

	Purpose: Returns a list of supported attribute values for the device class (deviceType).

	Notes: Called from getAttributeMap().
*/
private getSupportedAttributes(String deviceClass)
{
	if (NATIVE_DEVICES.find{it.key == deviceClass}) return NATIVE_DEVICES[deviceClass].attr
	if (state.customDrivers.find{it.key == deviceClass}) return state.customDrivers[deviceClass].attr
	return null
}


/*
	realtimeModeChangeHandler

	URL Format: GET /modes/set/modeName

	Purpose: Event handler for mode change events on the controller hub (this one).
*/
void realtimeModeChangeHandler(evt)
{
	if (state.commDisabled || !pushModes) return

	String newMode = evt.value
	if (enableDebug) log.debug "Sending mode change event to server: ${newMode}"
	sendGetCommand("/modes/set/${URLEncoder.encode(newMode)}")
}


/*
	realtimeHSMChangeHandler

	URL Format: GET /hsm/set/hsmStateName

	Purpose: Event handler for HSM state change events on the controller hub (this one).
*/
void realtimeHSMChangeHandler(evt)
{
	if (state.commDisabled || !pushHSM) return
	String newState = evt.value

    Map hsmToSHM =
	[
		armAway:	settings?.armAway,
		armHome:	settings?.armHome,
		armNight:	settings?.armNight,
		disarm:		"off"
	]

	String hsmState = hsmToSHM.find{it.value == newState}?.key
    if (hsmState)
	{
		if (enableDebug) log.debug "Sending SHM to HSM state change event to server: ${newState} to ${hsmState}"
		sendGetCommand("/hsm/set/${URLEncoder.encode(hsmState)}")
	}
    else if (enableDebug) log.debug "Error sending SHM to HSM state change to server: ${hsmState} is not mapped to ${newState}."
}


/*
	saveDevicesToServer

	Purpose: Sends all of the devices selected (& current attribute values) from this hub to the controller hub.

	URL Format: POST /devices/save

	Notes: Makes a single POST request for each group of devices.
*/
void saveDevicesToServer()
{
	if (devicesChanged == false) return

	// Fetch all devices and attributes for each device group and send them to the master.
	List idList = []
    List devices = []
	NATIVE_DEVICES.each
	{
	  groupname, device ->

		devices = []
		settings."${device.selector}".each
		{
			devices << [id: it.id, label: it.label ?: it.name, attr: getAttributeMap(it, groupname)]
			idList << it.id
		}
		if (devices != [])
		{
			if (enableDebug) log.info "Sending devices to server: ${groupname} - ${devices}"
			sendPostCommand("/devices/save", [deviceclass: groupname, devices: devices])
		}
	}

	// Custom defined device drivers
	state.customDrivers.each
	{
	  groupname, driver ->

		devices = []
		settings?."custom_${groupname}"?.each
		{
			devices << [id: it.id, label: it.label ?: it.name, attr: getAttributeMap(it, groupname)]
			idList << it.id
		}
		if (devices != [])
		{
			if (enableDebug) log.info "Sending custom devices to remote: ${groupname} - ${devices}"
			sendPostCommand("/devices/save", [deviceclass: groupname, devices: devices])
		}
	}
	if (cleanupDevices) sendPostCommand("/devices/save", [cleanupDevices: idList])
	state.saveDevices = false
}


/*
	sendDeviceEvent

	Purpose: Send an event to a client device.

	URL format: GET /event/:deviceId/:deviceCommand/:commandParams

	Notes: CALLED FROM CHILD DEVICE
*/
void sendDeviceEvent(String deviceId, String deviceCommand, List commandParams=[])
{
	if (state.commDisabled) return
	String[] dniParts = deviceId.split(":")

	String paramsEncoded = commandParams ? URLEncoder.encode(new groovy.json.JsonBuilder(commandParams).toString()) : null
	sendGetCommand("/event/${dniParts[1]}/${deviceCommand}/${paramsEncoded}")
}


/*
	deviceSendEvent

	Purpose:	Receives and forwards events received from a physical device located on a remote hub.

	URL Format: GET /device/:deviceId/event/:event

	API:		https://hubconnect.to/knowledgebase/22/deviceSendEvent.html
*/
def deviceSendEvent() {deviceSendEvent(params)}
def deviceSendEvent(Map params)
{
	String eventraw = params.event ? URLDecoder.decode(params.event) : null
	if (eventraw == null) return

	Map event = parseJson(new String(eventraw))
	String data = event?.data ?: ""
	String unit = event?.unit ?: ""

	event.displayName = event.displayName.replace("!@!", "/").replace("!#!", "’")

	def childDevice = getChildDevice("${serverIP}:${params.deviceId}")
	if (childDevice)
	{
		if (enableDebug) log.debug "Received event from Server/${childDevice.label}: [${event.name}, ${event.value} ${unit}, isStateChange: ${event.isStateChange}]"
		childDevice.sendEvent([name: event.name, value: event.value, unit: unit, descriptionText: "${childDevice.displayName} ${event.name} is ${event.value} ${unit}", isStateChange: event.isStateChange, data: data])
		return jsonResponse([status: "complete"])
	}
	else if (enableDebug) log.warn "Ignoring Received event from Server: Device Not Found!"

	return jsonResponse([status: "error"])
}


/*
	devicesSaveAll

	Purpose:	Creates virtual shadow devices and connects them the remote hub.

	URL Format: POST /devices/save

	API:		https://hubconnect.to/knowledgebase/21/devicesSaveAll.html
*/
def devicesSaveAll() {devicesSaveAll(request?.JSON)}
def devicesSaveAll(Map params)
{
	// Device cleanup?
	if (params?.cleanupDevices != null)
	{
		childDevices.each
		{
		  child ->
			if (child.deviceNetworkId != state.hubDeviceDNI && params?.cleanupDevices.find{"${serverIP}:${it}" == child.deviceNetworkId} == null)
			{
				if (enableDebug) log.info "Deleting device ${child.label} as it is no longer shared with this hub."
				deleteChildDevice(child.deviceNetworkId)
			}
		}
	}

	// Find the device class
	else if (!params?.deviceclass || !params?.devices)
	{
		return jsonResponse([status: "error"])
	}

	if (NATIVE_DEVICES.find {it.key == params.deviceclass})
	{
		// Create the devices
		params.devices.each { createLinkedChildDevice(it, "HubConnect ${NATIVE_DEVICES[params.deviceclass].driver}") }
	}
	else if (state.customDrivers.find {it.key == params.deviceclass})
	{
		// Get the custom device type and create the devices
		params.devices.each { createLinkedChildDevice(it, "${state.customDrivers[params.deviceclass].driver}") }
	}

	jsonResponse([status: "complete"])
}


/*
	createLinkedChildDevice

	Purpose: Helper function to create child devices.

	Notes: 	Called from saveDevices()
*/
private createLinkedChildDevice(Map dev, String driverType)
{
    def childDevice = getChildDevice("${serverIP}:${dev.id}")
	if (childDevice)
	{
		// Device exists
		if (enableDebug) log.trace "${driverType} ${dev.label} exists... Skipping creation.."
        return
	}
	else
	{
		if (enableDebug) log.trace "Creating Device ${driverType} - ${dev.label}... ${serverIP}:${dev.id}..."
		try
		{
			childDevice = addChildDevice("shackrat", driverType, "${serverIP}:${dev.id}", null, [name: dev.label, label: dev.label])
		}
		catch (errorException)
		{
			log.error "... Uunable to create device ${dev.label}: ${errorException}."
			childDevice = null
		}
	}

	// Set the value of the primary attributes
	if (childDevice)
	{
		dev.attr.each
		{
	 	 attribute ->
			childDevice.sendEvent([name: attribute.name, value: attribute.value, unit: attribute.unit])
		}
	}
}


/*
	syncDevice

	Purpose: Sync device details with the physcial device by requeting an update of all attribute values from the remote hub.

	Notes: CALLED FROM CHILD DEVICE
*/
void syncDevice(String deviceNetworkId, String deviceType)
{
	String[] dniParts = deviceNetworkId.split(":")
	Object childDevice = getChildDevice(deviceNetworkId)
	if (childDevice)
	{
		if (enableDebug) log.debug "Requesting device sync from server: ${childDevice.label}"

		def data = httpGetWithReturn("/device/${dniParts[1]}/sync/${deviceType}")
		if (data?.status == "success")
		{
			childDevice.setLabel(data.label)

			data?.currentValues.each
			{
			  attr ->
				childDevice.sendEvent([name: attr.name, value: attr.value, unit: attr.unit, descriptionText: "Sync: ${childDevice.displayName} ${attr.name} is ${attr.value} ${attr.unit}", isStateChange: true])
			}
		}
	}
}


/*
	httpGetWithReturn

	Purpose: Helper function to format GET requests with the proper oAuth token.

	Notes: 	Command is absolute and must begin with '/'
			Returns JSON Map if successful.
*/
def httpGetWithReturn(String command)
{
	Map requestParams =
	[
		uri:  state.clientURI + command,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		]
	]

	// Using HubAction?
	if (state.connectionType == "hubaction")
    {
    	// This is called asynchronously in the child device; if we try to send HubAction here, atomicState is never updated.
        // Send the command and wait for the result
        if (hubDevice)
        {
            hubDevice.httpGetWithReturn(requestParams)
            while (atomicState?.httpGetRequestResponse == null) pause(500)

            Map result = atomicState.httpGetRequestResponse
            atomicState.httpGetRequestResponse = null
            return result
        }
		else return [status: "error", message: "Remote Hub Device Missing"]
	}

	try
	{
		httpGet(requestParams)
		{
		  response ->
			if (response?.status == 200)
			{
				return response.data
			}
			else
			{
				log.error "httpGet() request failed with error ${response?.status}"
				return [status: "error", message: "httpGet() request failed with status code ${response?.status}"]
			}
		}
	}
	catch (Exception e)
	{
		log.error "httpGet() failed with error ${e.message}"
		return [status: "error", message: e.message]
	}
}


/*
	httpGetWithReturnResponse

	Purpose: Helper function called by Hub Device to send results back to the Remote Client app.

	Notes: 	This request is asynchronous, a second request should not be called before the first completes.
*/
void httpGetWithReturnResponse(Map responseData)
{
	atomicState.httpGetRequestResponse = responseData
}


/*
	sendGetCommand

	Purpose: Helper function to format GET requests with the proper oAuth token.

	Notes: 	Executes async http request and does not return data.
*/
void sendGetCommand(String command)
{
	if (state.clientURI == null) return
	Map requestParams =
	[
		uri:  state.clientURI + command,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		]
	]

	// Using HubAction?
	if (state.connectionType == "hubaction")
 	{
    	hubDevice?.sendGetCommand(requestParams)
    	return
    }

	try
	{
		asynchttp_v1.get((enableDebug ? "asyncHTTPHandler" : null), requestParams)
	}
	catch (Exception e)
	{
		log.error "asynchttpGet() failed with error ${e.message}"
	}
}


/*
	asyncHTTPHandler

	Purpose: Helper function to handle returned data from asyncHttpGet.

	Notes: 	Does not return data, only logs errors when debugging is enabled.
*/
void asyncHTTPHandler(response, data)
{
	if (response?.status != 200)
	{
		log.error "httpGet() request failed with error ${response?.status}"
	}
}


/*
	httpPostWithReturn

	Purpose: Helper function to format POST requests with the proper oAuth token.

	Notes: 	Command is absolute and must begin with '/'
			Returns JSON Map if successful.
*/
def httpPostWithReturn(String command, data)
{
	Map requestParams =
    [
		uri:  state.clientURI + command,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		],
		body: data
	]

	// Using HubAction?
	if (state.connectionType == "hubaction")
    {
    	// This is called asynchronously in the child device; if we try to send HubAction here, atomicState is never updated.
        // Send the command and wait for the result
    	hubDevice.sendPostCommand(requestParams)
        while (atomicState?.sendPostCommandResponse == null) pause(250)

		Map result = atomicState.sendPostCommandResponse
        atomicState.sendPostCommandResponse = null
        return result
	}

	try
	{
		httpPostJson(requestParams)
		{
		  response ->
			if (response?.status == 200)
			{
				return response.data
			}
			else
			{
				log.error "httpPost() request failed with error ${response?.status}"
			}
		}
	}
	catch (Exception e)
	{
		log.error "httpPostJson() failed with error ${e.message}"
		return [status: "error", message: e.message]
	}
}


/*
	httpPostWithReturnResponse

	Purpose: Helper function called by Hub Device to send results back to the Remote Client app.

	Notes: 	This request is asynchronous, a second request should not be called before the first completes.
*/
void httpPostWithReturnResponse(Map responseData)
{
	atomicState.httpGetRequestResponse = responseData
}


/*
	sendPostCommand

	Purpose: Helper function to format POST requests with the proper oAuth token.

	Notes: 	Executes async http request and does not return data.
*/
def sendPostCommand(String command, data)
{
	Map requestParams =
    [
		uri:  state.clientURI + command,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		],
		body: data
	]

	// Using HubAction?
	if (state.connectionType == "hubaction")
    {
    	// This is called asynchronously in the child device; if we try to send HubAction here, atomicState is never updated.
        // Send the command and wait for the result
    	hubDevice.sendPostCommand(requestParams)
        return
	}

	try
	{
		asynchttp_v1.post((enableDebug ? "asyncHTTPHandler" : null), requestParams)
	}
	catch (Exception e)
	{
		log.error "asynchttpPost() failed with error ${e.message}"
	}
}


/*
	appHealth

	Purpose: Checks in with the controller hub every 1 minute.

	URL Format: /ping

	Notes: 	Hubs are considered in a warning state after missing 2 pings (2 minutes).
			Hubs are considered offline after missing 5 pings (5 minutes).
			When a hub is offline, the virtual hub device presence state will be set to "not present".
*/
void appHealth()
{
	sendGetCommand("/ping")
}


/*
	systemSetCommStatus

	Purpose:	Enable or disable bi-directional communications between hubs.

	URL Format:	GET /system/setCommStatus/:status

	API: 		https://hubconnect.to/knowledgebase/13/systemSetCommStatus.html
*/
def systemSetCommStatus() {systemSetCommStatus(params)}
def systemSetCommStatus(Map params)
{
	log.info "Received setCommStatus command from server: disabled ${params.status}]"
	state.commDisabled = params.status == "false" ? false : true

	getHubDevice()?.(state.commDisabled ? "off" : "on")()
	jsonResponse([status: "success", switch: params.status == "false" ? "on" : "off"])
}


/*
	setCommStatus

	Purpose: Event handler which disables events communications between hubs.

	Notes: 	This is useful to stop the remote hub from listening to the server web socket.
			Called by Remote Hub Device
*/
void setCommStatus(Boolean status)
{
	log.info "Received setCommStatus command from virtual hub device: disabled ${status}]"
	log.info "Master bi-directional communciation status can only be set from the server hub."
	state.commDisabled = status
}


/*
	getAllModes

	Purpose: 	Returns a list of all configured modes on this hub.

	URL Format: GET /modes/get

	API:		https://hubconnect.to/knowledgebase/8/getAllModes.html
*/
def getAllModes()
{
	jsonResponse(modes: location.modes, active: location.mode)
}


/*
	getAllHSMStates

	Purpose:	Returns a list of all configured HSM States and the active state on this hub.

	URL Format: GET /hsm/get

	API:		https://hubconnect.to/knowledgebase/10/getAllHSMStates.html
*/
def getAllHSMStates()
{
	jsonResponse(hsmSetArm: ["armHome", "armNight", "off"], hsmStatus: location.currentState("alarmSystemStatus")?.value)
}


/*
	systemSaveCustomDrivers

	Purpose:	Saves the custom driver definitions from the server hub.

	URL Format:	GET /system/drivers/save

	API: 		https://hubconnect.to/knowledgebase/16/systemSaveCustomDrivers.html
*/
def systemSaveCustomDrivers() {systemSaveCustomDrivers(request?.JSON)}
def systemSaveCustomDrivers(Map params)
{
	if (params?.find{it.key == "customdrivers"})
	{
		// Clean up from deleted drivers
		state.customDrivers.each
		{
	  	  key, driver ->
			if (params?.customdrivers?.findAll{it.key == key}.size() == 0)
			{
				if (enableDebug) log.debug "Unsubscribing from events and removing device selector for ${key}"
				unsubscribe(settings."custom_${key}")
				settings.remove("custom_${key}")
			}
		}
		state.customDrivers = params?.customdrivers
        state.customDriverDBVersion = params?.customdriverdbversion
		jsonResponse([status: "success"])
	}
	else
	{
		jsonResponse([status: "error"])
	}
}


/*
	installed

	Purpose: Standard install function.

	Notes: Doesn't do much.
*/
void installed()
{
	log.info "${app.name} Installed"

	state.saveDevices = false
	state.installedVersion = appVersion

	if (!state?.customDrivers)
	{
		state.customDrivers = (Map) [:]
		state.customDriverDBVersion = 0
	}

	initialize()
}


/*
	updated

	Purpose: Standard update function.

	Notes: Still doesn't do much.
*/
void updated()
{
	log.info "${app.name} Updated"

	if (!state?.customDrivers)
	{
		state.customDrivers = (Map) [:]
		state.customDriverDBVersion = 0
	}

	// Clean up ghost hub devices
	childDevices.findAll{it.typeName == "HubConnect Remote Hub" && it.deviceNetworkId != state.hubDeviceDNI}.each{deleteChildDevice(it.deviceNetworkId)}

	initialize()

	state.installedVersion = appVersion
}


/*
	systemRemoteUpdate

	Purpose:	Processes the software update following the installation of new code.

	URL Format:	GET /system/update

	API: 		https://hubconnect.to/knowledgebase/19/systemRemoteUpdate.html
*/
def systemRemoteUpdate()
{
	updated()
	jsonResponse([status: "success"])
}


/*
	initialize

	Purpose: Initialize the server instance.

	Notes: Parses the oAuth link into the token and base URL.  A real token exchange would obviate the need for this.
*/
void initialize()
{
	log.info "${app.name} Initialized"
	unschedule()
    unsubscribe()

   	state.commDisabled = false
	resetHubDiscovery()

	// Build a lookup table & update device IPs if necessary
	if (updateDeviceIPs)
	{
		List parts = []
        childDevices.each
        {
            parts = it.deviceNetworkId.split(":")
            if (parts?.size() > 1)
            {
				it.deviceNetworkId = "${serverIP}:${parts[1]}"
            }
        }
	}
	app.updateSetting("updateDeviceIPs", [type: "bool", value: false])

	String[] connURI = state?.clientURI?.split(":")
	String serverPort = connURI?.size() > 2 ? connURI[2] : "80"

    // Only create HubDevice for hubaction (local) connections.
    if (state.connectionType == "hubaction")
	{
        def hubDevice = getHubDevice()
        if (hubDevice)
        {
            hubDevice.setConnectionType(state.connectionType, serverIP, serverPort, null, null, null)
        }
        else if (state?.clientToken && state.hubDeviceDNI != null)
        {
            hubDevice = createHubChildDevice()
            hubDevice?.setConnectionType(state.connectionType, serverIP, serverPort, null, null, null)
            hubDevice?.updateClientToken(state.clientToken)
		}
	}

	if (isConnected)
	{
		saveDevicesToServer()
		subscribeLocalEvents()
		if (pushModes) subscribe(location, "mode", realtimeModeChangeHandler)
		if (pushHSM) subscribe(location, "alarmSystemStatus", realtimeHSMChangeHandler)
		runEvery1Minute("appHealth")
	}
	state.saveDevices = false
	app.updateLabel("${ thisClientName ? thisClientName.replaceAll(/[^0-9a-zA-Z&_ ]/, "") + "${ isConnected ? ' [Online]' : ' [OFFLINE]' }" : 'HubConnect Remote Client' }")
}


/*
	systemRemoteInitialize

	Purpose:	Reinitializes the remote client & remote hub device on this hub.

	URL Format:	GET /system/initialize

	API:		https://hubconnect.to/knowledgebase/18/systemRemoteInitialize.html
*/
def systemRemoteInitialize()
{
	initialize()
	jsonResponse([status: "success"])
}


/*
	createHubChildDevice

	Purpose: Create child device for the server hub so up/down status can be managed with rules.

	Notes: 	Called from initialize()
*/
private def createHubChildDevice()
{
	String serverHubName = "Server Hub"
	def hubDevice = getHubDevice()
	if (hubDevice != null)
	{
		// Hub exists
		log.error "Remote hub device exists... Skipping creation.."
		hubDevice = null
	}
	else
	{
		if (enableDebug) log.trace "Creating remote hub Device ${serverHubName}... ${state.hubDeviceDNI}..."
		try
		{
			hubDevice = addChildDevice("shackrat", "HubConnect Remote Hub for SmartThings", state.hubDeviceDNI, (state.connectionType == "hubaction" ? location.hubs[0].id : null), [name: "HubConnect Hub", label: serverHubName])
		}
		catch (errorException)
		{
			log.error "Unable to create the Remote Hub device: ${errorException}.   Support Data: [id: \"${state.hubDeviceDNI}\", name: \"HubConnect Hub\", label: \"${serverHubName}\"]"
			hubDevice = null
		}

		// Set the value of the primary attributes
		if (hubDevice != null) hubDevice.sendEvent([name: "presence", value: "present"])
	}

	hubDevice
}


/*
	jsonResponse

	Purpose: Helper function to render JSON responses
*/
def jsonResponse(Map respMap)
{
	render contentType: 'application/json', data: JsonOutput.toJson(respMap)
}


/*
	getDevicePageStatus

	Purpose: Helper function to set flags for configured devices.
*/
def getDevicePageStatus()
{
	Map status = (Map) [:]
	NATIVE_DEVICES.each
	{
	  groupname, device ->
		status["${device.prefGroup}"] = (status["${device.prefGroup}"] ?: 0) + ((Integer) settings?."${device.selector}"?.size() ?: 0)
	}

	// Custom defined device drivers
	state.customDrivers.each
	{
	  groupname, driver ->
		status["custom"] = (status["custom"] ?: 0) + ((Integer) settings?."custom_${groupname}"?.size() ?: 0)
	}

	status["all"] = status.collect{it.value}.sum()
	status
}


/*
	deviceCategoryStatus

	Purpose: Helper function to set flags for configured devices on a category page.
*/
Integer deviceCategoryStatus(String page)
{
	(DEVICE_GROUPS.findResults{groupname, group -> if (page == group.parent) devicePageStatus."${groupname}"}.sum() ?: 0) + ((Integer) devicePageStatus."${page}" ?: 0)
}


/*
	mainPage

	Purpose: Displays the main (landing) page.

	Notes: 	Not very exciting.
*/
def mainPage()
{
	if (isConnected && state.installedVersion != null && state.installedVersion != appVersion) return upgradePage()
	app.updateSetting("removeDevices", [type: "bool", value: false])

	dynamicPage(name: "mainPage", uninstall: (hubDevice == null && !state.connected) ? true : false, install: true)
	{
		if (state.saveDevices)
		{
			section()
			{
				paragraph "Changes to remote devices will be saved on exit.  This will happen in the backgroud and may take several minutes to complete.", title: "Devices will be Saved!", required: true
			}
		}
		section(menuHeader("Connect"))
		{
			href "${(state.connected ? "connectPage" : "connectWizard_KeyPage")}", title: "Connect to Server Hub...", description: "", state: isConnected ? "complete" : null
			if (isConnected) href "devicePage", title: "Select devices to synchronize to Server hub...", description: "", state: devicePageStatus.all ? "complete" : null
		}
		if (isConnected)
		{
			section(menuHeader("Modes & SHM/HSM"))
			{
				href "shmConfigPage", title: "Configure SHM to HSM mapping...", description: "", state: (armAway != null || armHome != null || armNight != null) ? "complete" : null
				input "pushModes", "bool", title: "Push mode changes to Server Hub?", description: "", defaultValue: false
				input "pushHSM", "bool", title: "Send HSM changes to Server Hub?", description: "", defaultValue: false
			}
		}
		section(menuHeader("Admin"))
		{
			input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
			href "uninstallPage", title: "${isConnected ? "Disconnect Server Hub & " : ""}Remove this instance...", description: "", state: null
		}
		section()
		{
			paragraph title: "HubConnect v${appVersion.major}.${appVersion.minor}.${appVersion.build}", "Remote Client for SmartThings\n${appCopyright}"
		}
	}
}


/*
	upgradePage

	Purpose: Displays the splash page to force users to initialize the app after an upgrade.
*/
def upgradePage()
{
	dynamicPage(name: "upgradePage", uninstall: false, install: true)
	{
		section("New Version Detected!")
		{
			paragraph "This HubConnect Remote Client has an upgrade that has been installed... \n\n Please click [Save] to complete the installation."
		}
	}
}


/*
	systemSetConnectKey

	Purpose:	Sets the connection parameters from the connection key.

	URL Format: GET

	API:		https://hubconnect.to/knowledgebase/15/systemSetConnectKey.html
*/
def systemSetConnectKey()
{
	connectPage()
}


/*
	startHubDiscovery

	Purpose:	Initiates a SSDP search for Hubitat hubs and subscribes to location events to receive results.
*/
void startHubDiscovery()
{
	atomicState.discoveredHubs = [:]
    state.hubDiscoveryRefreshCount = 0
    atomicState.hubDiscoveryStatus = "running"
    subscribe(location, "ssdpTerm.urn:Hubitat:device:hub:1", hubDiscoveryEventHandler)
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:Hubitat:device:hub:1", physicalgraph.device.Protocol.LAN))
}


/*
	stopHubDiscovery

	Purpose:	Stops a SSDP search for Hubiat hubs by unsubscribing to location events.
*/
void stopHubDiscovery()
{
	atomicState.hubDiscoveryStatus = "complete"
	unsubscribe()
}


/*
	resetHubDiscovery

	Purpose:	Resets tracking variables used for hub discovery.
*/
void resetHubDiscovery()
{
	atomicState.hubDiscoveryStatus = null
	atomicState.discoveredHubs = null
    state.remove("hubDiscoveryRefreshCount")
}


/*
	hubDiscoveryEventHandler

	Purpose:	Receives hub discovery events.
*/
def hubDiscoveryEventHandler(Object event)
{
	// Tokenize the lan message, then parse it into a Map.
	String[] parsedData = event.description.toString().split(", ")
    Map<String, String> ssdpData = (Map) [:]
    parsedData.each
    {
		String[] parts = it.split(":")
        if (parts?.size() < 2) return // Shouldn't happen
		ssdpData["${parts[0]}"] = parts[1]
    }

    if (ssdpData.ssdpPath == "/api/hubitat.xml")
    {
    	Map discoveredHubs = atomicState.discoveredHubs
        discoveredHubs["${ssdpData.networkAddress}"] = [name: ssdpData.ssdpNTS, mac: ssdpData.mac]
		atomicState.discoveredHubs = discoveredHubs
    }
}


/*
	convertIPtoHex

	Purpose:	Utility function to convert an IP address in "dotted" notaton to hex.
*/
private String convertIPtoHex(ipAddress)
{
	ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join().toUpperCase()
}


/*
	connectWizard_KeyPage

	Purpose: Displays a connection key entry dialog.
*/
def connectWizard_KeyPage()
{
	String nextPage = (String) "connectWizard_KeyPage"
	String responseText = ""
	Map accessData = (Map) [:]
	if (settings?.serverKey != null && settings.serverKey != "hidden")
	{
		try
		{
			accessData = parseJson(new String(serverKey.decodeBase64()))
		}
		catch (errorException)
		{
			log.error "Error reading connection key: ${errorException}."
			responseText = "Error: Corrupt or invalid connection key"
			state.connected = false
            accessData = null
            state.hubDeviceDNI = null
		}

        if (accessData && accessData?.token && accessData.serverIP && accessData?.type == "smartthings")
		{
        		// Get the MAC for the server hub
            	if (accessData.connectionType == "hubaction")
            	{
				nextPage = "connectWizard_DiscoverPage"
			}
            	else if (accessData.connectionType == "http")
            	{
				nextPage = "connectPage"
            	}
		}
	}

	dynamicPage(name: "connectWizard_KeyPage", uninstall: false, install: false, nextPage: nextPage)
	{
		section(menuHeader("Server Details"))
		{
        		if (responseText.size()) paragraph responseText, required: true
				input "serverKey", "text", title: "Paste the server hub's connection key here:", required: true, defaultValue: null, submitOnChange: true
		}
		if (nextPage != "connectWizard_KeyPage")
		{
            	section()
            	{
            	    if (nextPage == "connectWizard_DiscoverPage") paragraph "Please tap [Next] to discover the server hub."
            	    else paragraph "Please tap [Next] to complete the connection to the server hub."
            	}
		}
	}
}


/*
	connectWizard_DiscoverPage

	Purpose: Displays a device discovery dialog.
*/
def connectWizard_DiscoverPage()
{
	if (atomicState.hubDiscoveryStatus == "running")
	{
		state.hubDiscoveryRefreshCount = state.hubDiscoveryRefreshCount + 1
    		if (state.hubDiscoveryRefreshCount >= 4) stopHubDiscovery()
	}

	// Hub MAC address discovery
	if ((state?.hubDeviceDNI == null || hubDevice == null) && atomicState?.hubDiscoveryStatus == null)
	{
	    startHubDiscovery()
	}

	String nextPage = (String) (atomicState.hubDiscoveryStatus == "complete" ? "connectPage" : "connectWizard_DiscoverPage")
	Integer refreshInterval = (Integer) (atomicState.hubDiscoveryStatus == "complete" ? 0 : 2)
	log.debug atomicState.hubDiscoveryStatus
	dynamicPage(name: "connectWizard_DiscoverPage", uninstall: false, install: false, refreshInterval: refreshInterval, nextPage: nextPage)
	{
		if (atomicState.hubDiscoveryStatus == "complete")
        {
            section(menuHeader("Discover Complete"))
            {
        		paragraph "Discovery is finished.", complete: true
				paragraph "Please tap [Next] to complete the connection to the server hub."
			}
        }
		else
		{
            section(menuHeader("Please wait..."))
            {
                Map found = atomicState.discoveredHubs
                paragraph "Please wait while HubConnect searches for hubs... Found: ${found?.size()}"
            }
		}
	}
}


/*
	macAddressPage

	Purpose: Notifies the user that the hub could not be located.
*/
def macAddressPage()
{
	dynamicPage(name: "macAddressPage", uninstall: false, install: false, nextPage: "connectPage")
	{
		section(menuHeader("Hub Not Found!"))
		{
			paragraph "HubConnect could not located the server hub.", required: true
            input "serverMAC", "text", title: "Please enter the MAC address of the server hub (no colons):", required: false, defaultValue: null
		}
	}
}


/*
	connectPage

	Purpose:	Displays the local & remote oAuth links.

	Notes:		Really should create a proper token exchange someday.
*/
def connectPage()
{
	if (!state?.accessToken)
	{
		state.accessToken = createAccessToken()
	}

	// Remote key update
	if (params?.connectKey)
	{
	    app.updateSetting("serverKey", [type: "text", value: params.connectKey])
	}
	
	String responseText = ""
	if (settings?.serverKey != null && settings.serverKey != "hidden")
	{
		def accessData
		try
		{
			accessData = parseJson(new String(serverKey.decodeBase64()))
		}
		catch (errorException)
		{
			log.error "Error reading connection key: ${errorException}."
			responseText = "Error: Corrupt or invalid connection key"
			state.connected = false
            	accessData = null
		}
        	if (accessData && accessData?.token && accessData.serverIP && accessData?.type == "smartthings")
		{
        		// Get the MAC for the server hub
			if (accessData.connectionType == "hubaction" && state?.hubDeviceDNI == null)
			{
				if (atomicState.hubDiscoveryStatus == "complete")
					{
					String serverIPhex = convertIPtoHex(accessData.serverIP)
					Map discoveredHubs = atomicState.discoveredHubs
						state.hubDeviceDNI = discoveredHubs?."${serverIPhex}"?.mac
					}

			    // Prompt for MAC if not found
				if (state.hubDeviceDNI == null)
					{
			    		if (serverMAC != null) state.hubDeviceDNI = serverMAC
					else return macAddressPage()
					}
			}

			// Set the server hub details
			state.clientURI = accessData.uri
			state.clientToken = accessData.token
			state.clientType = accessData.type
			state.connectionType = accessData.connectionType

			app.updateSetting("serverIP", [type: "text", value: accessData.serverIP])
			if (settings?.thisClientName == null) app.updateSetting("thisClientName", [type: "text", value: accessData.name])
			
			// If this remote is using HubAction the hub device has to be created now
			if (state.connectionType == "hubaction")
			{
				if (hubDevice == null)
				{
			      	def hubDevice = createHubChildDevice()
			      	if (hubDevice != null)
			      	{
			            	String[] connURI = accessData?.uri?.split(":")
			            	String serverPort = connURI?.size() > 2 ? connURI[2] : "80"

						hubDevice.setConnectionType(state.connectionType, accessData.serverIP, serverPort, null, null, null)
			        	}
				}
				resetHubDiscovery()
			}
			
			hubDevice?.updateClientToken(accessData.token)
		
			// Send our connect string to the coordinator
			String connectKey = new groovy.json.JsonBuilder([uri: (accessData.connectionType == "http" ? apiServerUrl("/api/smartapps/installations/${app.id}") : callBackAddress), name: location.name, type: "remote", token: state.accessToken, mac: location.hubs[0].id ?: "None", customDriverDBVersion: state.customDriverDBVersion]).toString().bytes.encodeBase64()
			def response = httpGetWithReturn("/connect/${connectKey}")
			if (response.status == "success")
			{
				state.connected = true
		    		app.updateSetting("serverKey", [type: "text", value: "hidden"])
			}
			else
			{
				state.connected = false
				app.updateSetting("serverKey", [type: "text", value: ""])
				app.updateSetting("disconnectHub", [type: "bool", value: false])
				responseText = "Error: ${response?.message}"
			}
		}
	else if (accessData?.type != "smartthings") responseText = "Error: Connection key is not for this platform"
	}

	// Reset connection data if handshake failed
	if (serverKey == null || disconnectHub || state.connected == false)
	{
		resetHubConnection()
	}

	dynamicPage(name: "connectPage", uninstall: (hubDevice == null && !state.connected) ? true : false, install: false, nextPage: "mainPage")
	{
		section(menuHeader("Server Details"))
		{
			input "serverKey", "text", title: "Paste the server hub's connection key here:", required: false, defaultValue: null, submitOnChange: true
			if (serverKey) input "serverIP", "text", title: "Local LAN IP Address of the Server Hub:", required: false, defaultValue: accessData?.serverIP, submitOnChange: true
		}
		if ((serverKey && serverIP) || state.connected )
		{
			section(menuHeader("Remote Details"))
			{
				input "thisClientName", "text", title: "Friendly Name of this Remote Hub (Optional):", required: false, defaultValue: accessData?.serverIP, submitOnChange: false
				if (serverIP && state.connected) input "updateDeviceIPs", "bool", title: "Update child devices with new IP address?", defaultValue: false
			}
		}
		section()
		{
			if (state.connected)
			{
				if (state?.lastError)
				{
					paragraph "${state.lastError}", required: true
					state.remove("lastError")
				}
				paragraph "Connected!"
				if (state?.installedVersion == null)
				{
					paragraph "Please click [Done] to complete installation."
				}
				else input "disconnectHub", "bool", title: "Disconnect Server Hub...", description: "This will erase the connection key.", required: false, submitOnChange: true
			}
			else
			{
				paragraph "Not Connected :: ${responseText}", required: true
				if (response?.status == null) input "disconnectHub", "bool", title: "Reset Connection to Server Hub...", description: "This will erase the connection key.", required: false, submitOnChange: true
			}
		}
	}
}


/*
	uninstallPage

	Purpose: Displays options for removing an instance.

	Notes: 	Really should create a proper token exchange someday.
*/
def uninstallPage()
{
	dynamicPage(name: "uninstallPage", title: "Uninstall HubConnect Remote", uninstall: true, install: false)
	{
		section(menuHeader("Warning!"))
		{
			paragraph "It is strongly recommended to back up your hub before proceeding. This action cannot be undone!\n\nClick the [Remove] button below to disconnect and remove this remote."
		}
		section(menuHeader("Options"))
		{
			input "removeDevices", "bool", title: "Remove virtual HubConnect shadow devices on this hub?", required: false, defaultValue: false, submitOnChange: true
		}
		section(menuHeader("Factory Reset"))
		{
			href "resetPage", title: "Factory Reset..", description: "Perform a factory reset of this remote.", state: null
		}
		section()
		{
			href "mainPage", title: "Cancel and return to the main menu..", description: "", state: null
		}
	}
}


/*
	resetPage

	Purpose:	Prompts a user to "factory reset" this app.

	Notes:		DO NOT USE unless directed by support
*/
def resetPage()
{
	dynamicPage(name: "resetPage", title: "Factory Reset HubConnect Remote", uninstall: false, install: true)
	{
	// Factory reset?
    	if (resetConfirmText == "reset" && resetToFactoryDefaultsSw)
		{
        	resetToFactoryDefaults()
			section()
			{
				paragraph "Reset is complete; please check the logs for details."
				href "mainPage", title: "Return to the main menu..", description: "", state: null
			}
		}
		else
		{
			section(menuHeader("Warning!"))
			{
				paragraph "Please DO NOT reset unless directed to by support!!"
				paragraph "It is strongly recommended to back up your hub before proceeding. This action cannot be undone!"
			}
			section(menuHeader("Factory Reset"))
			{
				input "resetConfirmText", "text", title: "Please confirm", description: "Please enter the word \"reset\" (without quotes) to confirm.", required: false, submitOnChange: true
				if (resetConfirmText == "reset") input "resetToFactoryDefaultsSw", "bool", title: "RESET TO DEFAULTS", required: false, submitOnChange: true
			}
		}
		section()
		{
			href "mainPage", title: "Cancel and return to the main menu..", description: "", state: null
		}
	}
}


/*
	devicePage

	Purpose: Displays the page where devices are selected to be linked to the controller hub.

	Notes: 	Really could stand to be better organized.
*/
def devicePage()
{
	Integer totalNativeDevices = 0
	String requiredDrivers = ""
	NATIVE_DEVICES.each
	{devicegroup, device ->
		if (settings."${device.selector}"?.size())
		{
			totalNativeDevices += settings."${device.selector}"?.size()
			requiredDrivers += "HubConnect ${device.driver}\n"
		}
	}

	Integer totalCustomDevices = 0
	state.customDrivers?.each
	{devicegroup, device ->
		totalCustomDevices += settings."custom_${devicegroup}"?.size() ?: 0
	}

	def quickNavOpts = NATIVE_DEVICES.findResults {devicegroup, driver ->
		if (!driver?.platform || driver?.platform == appVersion.platform)  ["${devicegroup}": driver.displayName ?: driver.driver]
	}

    Integer totalDevices = totalNativeDevices + totalCustomDevices

	// Changes in the quick select list?
	if (devicesChanged) state.saveDevices = true
	state.quickSelectState = null

	dynamicPage(name: "devicePage", uninstall: false, install: false, nextPage: "mainPage")
	{
		section(menuHeader("Quick Select"))
		{
			input "quickSelect", "enum", options: quickNavOpts, title: "Device Types", description: "Select the type of device to connect.", required: false, submitOnChange: true
			if (quickSelect)
			{
				def selector = renderDeviceSelector(NATIVE_DEVICES.find{devicegroup, device -> devicegroup == quickSelect}?.value)
				app.updateSetting("quickSelect", [type: "enum", value: ""])
				state.quickSelectState = [name: selector, devices: settings?."${selector}".collect{it.id}]
			}
		}

		section(menuHeader("Device Categories  (${totalDevices} connected)"))
		{
			DEVICE_GROUPS.each
			{
			  groupname, group ->
				if (!group?.parent) href "dynamicDevicePage", title: group.title, description: group.description, state: deviceCategoryStatus(groupname) ? "complete" : null, params: [prefGroup: groupname, title: group.title]
			}
			href "customDevicePage", title: "Custom Devices", description: "Devices with user-defined drivers.", state: devicePageStatus.custom ? "complete" : null
		}
		if (state.saveDevices)
		{
			section()
			{
				paragraph "Changes to remote devices will be saved on exit."
				input "cleanupDevices", "bool", title: "Remove unused devices on the remote hub?", required: false, defaultValue: true
			}
		}
		if (requiredDrivers?.size())
		{
			section(menuHeader("Required Drivers"))
			{
				paragraph "Please make sure the following native drivers are installed on the Server hub before clicking \"Done\": \n${requiredDrivers}"
			}
		}
	}
}


/*
	dynamicDevicePage

	Purpose: Displays a device selection page.
*/
def dynamicDevicePage(Map params)
{
	state.saveDevices = true

	dynamicPage(name: "dynamicDevicePage", title: params.title, uninstall: false, install: false, nextPage: "devicePage")
	{
		if (DEVICE_GROUPS.find{key, val -> val?.parent == params.prefGroup})
		{
			section(menuHeader("Device Categories"))
			{
				DEVICE_GROUPS.each
				{
				  groupname, group ->
					if (group?.parent == params.prefGroup) href "dynamicDevicePage", title: group.title, description: group.description, state: devicePageStatus."${groupname}" ? "complete" : null, params: [prefGroup: groupname, title: group.title]
				}
			}
		}
		NATIVE_DEVICES.each
		{
		  groupname, device ->
			if (device.prefGroup == params.prefGroup)
			{
				if (device?.platform && device?.platform != appVersion.platform) return
				section(menuHeader("Select ${device.driver} Devices (${settings?."${device.selector}"?.size() ?: "0"} connected)"))
				{
					renderDeviceSelector(device)

					// Customizations
					if (groupname == "irissmartplug")
					{
						input "sp_EnablePower", "bool", title: "Enable power meter reporting?", required: false, defaultValue: true
						input "sp_EnableVolts", "bool", title: "Enable voltage reporting?", required: false, defaultValue: true
					}
					else if (groupname == "power")
					{
						input "pm_EnableVolts", "bool", title: "Enable voltage reporting?", required: false, defaultValue: true
					}
				}
			}
		}
	}
}


/*
	customDevicePage

	Purpose: Displays the page where custom (user-defined) devices are selected to be linked to the controller hub.

	Notes: 	First attempt at remotely defined device definitions.
*/
def customDevicePage()
{
	state.saveDevices = true

	dynamicPage(name: "customDevicePage", uninstall: false, install: false)
	{
		state.customDrivers.each
		{
		  groupname, driver ->
			def customSel = settings."custom_${groupname}"
			section(menuHeader("Select ${driver.driver} Devices (${customSel?.size() ?: "0"} connected)"))
			{
				input "custom_${groupname}", "capability.${driver.selector.substring(driver.selector.lastIndexOf("_") + 1)}", title: "${driver.driver} Devices (${driver.attr}):", required: false, multiple: true, defaultValue: null
			}
		}
	}
}


/*
	shmConfigPage

	Purpose: Configures HSM to SHM Mappings.

	Notes: 	Not very exciting.
*/
def shmConfigPage()
{
	List<String> shmStates = ["away", "stay", "off"]
	dynamicPage(name: "shmConfigPage", uninstall: true, install: true)
	{
		section(menuHeader("SHM to HSM Mode Mapping"))
		{
			input "armAway", "enum", title: "Set HSM to this mode when HSM changes to armAway", options: shmStates, description: "", defaultValue: "away"
			input "armHome", "enum", title: "Set HSM to this mode when HSM changes to armHome", options: shmStates, description: "", defaultValue: "off"
			input "armNight", "enum", title: "Set HSM to this mode when HSM changes to armNight", options: shmStates, description: "", defaultValue: "stay"
		}
	}
}


/*
	systemGetVersions

	Purpose:	Returns a list of all versions including this remote client and any active/installed drivers on this hub.

	URL Format: GET /system/versions/get

	API			https://hubconnect.to/knowledgebase/17/systemGetVersions.html
*/
def systemGetVersions()
{
	// Get hub app & drivers
	Map remoteDrivers = (Map) [:]
	getChildDevices()?.each
	{
	   device ->
		if (remoteDrivers[device.typeName] == null) remoteDrivers[device.typeName] = device.getDriverVersion()
	}
	jsonResponse([apps: [[appName: app.label, appVersion: appVersion]], drivers: remoteDrivers])
}


/*
	systemRemoteDisconnect

	Purpose:	Accepts a command from the server to disconnect.

	URL Format: GET /system/disconnect

	API:		https://hubconnect.to/knowledgebase/25/systemRemoteDisconnect.html
*/
def systemRemoteDisconnect()
{
	resetHubConnection()

	app.updateSetting("serverKey", [type: "string", value: ""])
	app.updateSetting("disconnectHub", [type: "bool", value: false])
	initialize()
	jsonResponse([status: "success"])
}


/*
	resetHubConnection

	Purpose: Resets the connection to the server hub.
*/
def resetHubConnection(Boolean removeHub = false)
{
	log.info "Resetting connection settings and disconnecting from server hub."

	state.remove("clientURI")
	state.remove("clientToken")
	state.remove("clientType")
	state.remove("connectionType")
	state.hubDeviceDNI = null
	state.connected = false

	hubDevice?.off()

	// Remove all hub devices
    getChildDevices().each
    {
      child ->
        if (child.typeName == "HubConnect Remote Hub for SmartThings")
		{
			if (logDebug) log.info "Deleting Remote Hub Device: ${child.name} (${child.deviceNetworkId})"
			deleteChildDevice(child.deviceNetworkId)
		}
    }

	if (disconnectHub)
	{
		app.updateSetting("serverIP", [type: "string", value: ""])
		app.updateSetting("serverKey", [type: "string", value: ""])
		app.updateSetting("disconnectHub", [type: "bool", value: false])
        app.updateSetting("serverMAC", [type: "string", value: ""])
	}

    unsubscribe()
    unschedule()
}


/*
	resetToFactoryDefaults

	Purpose: 	Resets everything to a freshly-installed state.

	Notes:		This will disconnect this hub.  Do not use ubnless directed to by support.
*/
void resetToFactoryDefaults()
{
	log.warn "!!! HubConnect resetToFactoryDefaults() Called !!!"
	log.warn "** Resetting all settings and app storage to defaults; device selections and child devices will be preserved. **"
	log.warn "** This hub will be disconnected... **"

	// Destroy state
	log.info ">> Clearing state..."
	state.clear()

	// Destroy atomicState
	log.info ">> Clearing atomicState..."
	atomicState.httpGetRequestResponse = null
	atomicState.sendPostCommandResponse = null
	atomicState.discoveredHubs = null
	atomicState.hubDiscoveryStatus = null

	// Destroy all settings except for device selection
	log.info ">> Clearing settings..."
	settings.each
	{
	  k, v ->
		log.debug "checking: ${k}"

		if (settings?."${k}" != null && k.startsWith("custom_") == false && NATIVE_DEVICES.find{groupname, driver -> k == driver.selector} == null)
		{
			log.info ">>>> Erasing setting: ${k}"
			app.updateSetting(k, "")  // ST does not support app.removeSetting()
		}
	}

	// Remove all Hub Devices
	log.info ">> Removing all hub devices..."
	childDevices.findAll{it.typeName == "HubConnect Remote Hub"}.each{deleteChildDevice(it.deviceNetworkId)}

	log.warn "!!! HubConnect resetToFactoryDefaults() COMPLETE !!!"
}


/*
	renderDeviceSelector

	Purpose: Renders the DeviceMatch device selection dropdown.
*/
private String renderDeviceSelector(Map device)
{
	if (device == null) return
	String capability =
		(device.type == "attr") ? (device.capability.contains("device.") ? device.capability : "capability.${device.capability}")
		: (device.type == "hcapp") ? "device." + device.driver.replace(" ", "")
			: (!settings?."syn_${device.selector}" || settings."syn_${device.selector}" == "attribute") ? "capability.${device.capability}"
			: (settings."syn_${device.selector}" == "physical") ? "device." + device.driver.replace(" ", "")
			: "device.HubConnect" + device.driver.replace(" ", "")

	input "${device.selector}", "${capability}", title: "${device.driver} Device(s) ${device.attr}:", required: false, multiple: true, defaultValue: null
	if (device.type=="synth") input "syn_${device.selector}", "enum", title: "DeviceMatch Selection Type? ${settings."${device.selector}"?.size() ? " (Changing may affect the availability of previously selected devices)" : ""}", options: [physical: "Device Driver", synthetic: "HubConnect Driver", attribute: "Primary Attribute"], required: false, defaultValue: (capability.startsWith("device") ? "physical" : "attribute"), submitOnChange: true
	device.selector
}


/*
	systemGetTSReport

	Purpose:	Returns a full report on the current app including configuration and current status of this client.

	URL Format:	GET /system/tsreport/get

	API:		https://hubconnect.to/knowledgebase/20/systemGetTSReport.html
*/
def systemGetTSReport()
{
	jsonResponse([
		app: [
			appId: app.id,
			appVersion: getAppVersion().toString(),
			installedVersion: state.installedVersion
		],
		prefs: [
			thisClientName: thisClientName,
			serverKey: serverKey,
			pushModes: pushModes,
			pushHSM: pushHSM,
			enableDebug: enableDebug,
		],
		state: [
			clientURI: state?.clientURI,
			connectionType: state.connectionType,
			customDrivers: state.customDrivers,
			commDisabled: state.commDisabled
		],
		devices: [
			incomingDevices: getChildDevices()?.size() - (hubDevice != null ? 1 : 0),
			deviceIdList: "N/A"
		],
		hub: [
			deviceStatus: "N/A",
			connectionType: "http",
			eventSocketStatus:  "N/A",
			hsmStatus:  "N/A",
			modeStatus:  "N/A",
			presence:  "N/A",
			switch:  "N/A",
			version:  "N/A",
			subscribedDevices:  "N/A",
			connectionAttempts:  "N/A",
			refreshSocket:  "N/A",
			refreshHour:  "N/A",
			refreshMinute: "N/A",
			hardwareID: location?.hubs[0]?.getType(),
			firmwareVersion: location?.hubs[0]?.firmwareVersionString,
			localIP: location?.hubs[0]?.getLocalIP()
		]
	])
}
String menuHeader(String title) {"-= ${title} =-"}
def getHubDevice() { getChildDevice(state.hubDeviceDNI) }
Boolean getIsConnected(){(state?.clientURI?.size() > 0 && state?.clientToken?.size() > 0) ? true : false}
private String getCallBackAddress() { "http://" + location?.hubs[0]?.getLocalIP() + ":" + location?.hubs[0]?.localSrvPortTCP }
def getDevicesChanged() {state.saveDevices || (state?.quickSelectState && state?.quickSelectState.devices != (settings?."${state?.quickSelectState?.name}"?.collect{it.id} ?: []))}
String getAppCopyright(){"© 2019-2020 Steve White, Retail Media Concepts LLC\nhttps://hubconnect.to/knowledgebase/5/HubConnect-License-Agreement.html"}