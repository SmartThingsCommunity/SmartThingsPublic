/**
 *  Simulated Device Manager
 *
 *  Author: SmartThings (Juan Risso - juan@smartthings.com)
 */

definition(
	name: "Simulated Device Manager",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Allows you to create virtual devices to populate your account.",
	category: "SmartThings Labs",
    iconUrl: "https://d3abxbgmpfhmxi.cloudfront.net/assets/downloads/smartthings-logo-ring.7311e9df.png",
    iconX2Url: "https://d3abxbgmpfhmxi.cloudfront.net/assets/downloads/smartthings-logo-ring.7311e9df.png",
    singleInstance: true
)

preferences {
	page(name:"selectDevices", title:"Simulated Device Manager")
}

//PAGES
def selectDevices() {
    return dynamicPage(name:"selectDevices", title:"Simulated Device Manager", nextPage:"", install:true, uninstall: true) {
    	def options = [1,2,3,4,5]
        section("Please select the number of virtual devices you want to install") {
            input "selectedswitch", "enum", required:false, title:"Select number of Simulated Switches", multiple:false, options:options
            input "selectedmotion", "enum", required:false, title:"Select number of Simulated Motion Sensors", multiple:false, options:options
            input "selectedpresence", "enum", required:false, title:"Select number of Simulated Presence Sensors", multiple:false, options:options
			input "selectedsmoke", "enum", required:false, title:"Select number of Simulated Smoke Alarms", multiple:false, options:options
            input "selectedleak", "enum", required:false, title:"Select number of Simulated Water Sensors", multiple:false, options:options
            input "selectedalarm", "enum", required:false, title:"Select number of Simulated Alarms", multiple:false, options:options
            input "selectedbutton", "enum", required:false, title:"Select number of Simulated Buttons", multiple:false, options:options
            input "selectedcontact", "enum", required:false, title:"Select number of Simulated Contact Sensors", multiple:false, options:options
            input "selectedlock", "enum", required:false, title:"Select number of Simulated Locks", multiple:false, options:options
            input "selectedtemperature", "enum", required:false, title:"Select number of Simulated Temperature Sensors", multiple:false, options:options
            input "selectedthermostat", "enum", required:false, title:"Select number of Simulated Thermostats", multiple:false, options:options
			input "selectedvalve", "enum", required:false, title:"Select number of Simulated Valves", multiple:false, options:options
		}
    }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	log.trace "deleting ${devices.size()} device"
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def initialize() {
	addDevice()
}

//CHILD DEVICE METHODS
def addDevice(){
    log.trace "addDevice()"
 	def type = [selectedswitch: selectedswitch,selectedmotion: selectedmotion,selectedpresence: selectedpresence,selectedsmoke: selectedsmoke,selectedleak: selectedleak,selectedalarm: selectedalarm,selectedbutton: selectedbutton,selectedcontact: selectedcontact,selectedlock: selectedlock,selectedtemperature: selectedtemperature,selectedthermostat: selectedthermostat,selectedvalve: selectedvalve]
 	def deviceNameList = [selectedswitch: 'Simulated Switch',selectedmotion: 'Simulated Motion Sensor',selectedpresence: 'Simulated Presence Sensor',selectedsmoke: 'Simulated Smoke Alarm',selectedleak: 'Simulated Water Sensor',selectedalarm: 'Simulated Alarm',selectedbutton: 'Simulated Button',selectedcontact: 'Simulated Contact Sensor',selectedlock: 'Simulated Lock',selectedtemperature: 'Simulated Temperature Sensor',selectedthermostat: 'Simulated Thermostat',selectedvalve: 'Simulated Water Valve']
	type.each {key, value ->
        if (value > 0) {
        	def deviceName = deviceNameList[key]
        	log.trace "Adding ${deviceName}"
            def aux
            for (aux = 1; aux <= value; aux++) {
            	def dni = "$key$aux"
                def d = getChildDevice(dni)
                if(!d) {
                    d = addChildDevice("smartthings/testing", deviceName, dni, null, [label:"${deviceName} ${aux}"])
                    log.trace "Created ${deviceName} with id ${dni}"
                } else {
                    log.trace "${dni} already exists"
                }
            }
    	}
	}
}
