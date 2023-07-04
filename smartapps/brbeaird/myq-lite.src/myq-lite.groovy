/**
 * -----------------------
 * ------ SMART APP ------
 * -----------------------
 *
 *  MyQ Lite
 *
 *  Copyright 2018 Jason Mok/Brian Beaird/Barry Burke/RBoy Apps
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
 *  Last Updated : 2019-04-30
 */
include 'asynchttp_v1'

definition(
	name: "MyQ Lite",
	namespace: "brbeaird",
	author: "Jason Mok/Brian Beaird/Barry Burke",
	description: "Integrate MyQ with Smartthings",
	category: "SmartThings Labs",
	iconUrl:   "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq.png",
	iconX2Url: "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq@3x.png"
)

preferences {
	page(name: "prefLogIn", title: "MyQ")
	page(name: "prefListDevices", title: "MyQ")
    page(name: "prefSensor1", title: "MyQ")
    page(name: "prefSensor2", title: "MyQ")
    page(name: "prefSensor3", title: "MyQ")
    page(name: "prefSensor4", title: "MyQ")
    page(name: "prefSensor5", title: "MyQ")
    page(name: "prefSensor6", title: "MyQ")
    page(name: "prefSensor7", title: "MyQ")
    page(name: "prefSensor8", title: "MyQ")
    page(name: "noDoorsSelected", title: "MyQ")
    page(name: "summary", title: "MyQ")
    page(name: "prefUninstall", title: "MyQ")
}


/* Preferences */
def prefLogIn() {
	state.previousVersion = state.thisSmartAppVersion
    if (state.previousVersion == null){
    	state.previousVersion = 0;
    }
    state.thisSmartAppVersion = "2.1.11"
    state.installMsg = ""
    def showUninstall = username != null && password != null
	return dynamicPage(name: "prefLogIn", title: "Connect to MyQ", nextPage:"prefListDevices", uninstall:false, install: false, submitOnChange: true) {
		section("Login Credentials"){
			input("username", "email", title: "Username", description: "MyQ Username (email address)")
			input("password", "password", title: "Password", description: "MyQ password")
		}
		section("Gateway Brand"){
			input(name: "brand", title: "Gateway Brand", type: "enum",  metadata:[values:["Liftmaster","Chamberlain","Craftsman"]] )
		}
        section("Uninstall") {
            paragraph "Tap below to completely uninstall this SmartApp and devices (doors and lamp control devices will be force-removed from automations and SmartApps)"
            href(name: "href", title: "Uninstall", required: false, page: "prefUninstall")
        }
	}
}

def prefUninstall() {
    log.debug "Removing MyQ Devices..."
    def msg = ""
    childDevices.each {
		try{
			deleteChildDevice(it.deviceNetworkId, true)
            msg = "Devices have been removed. Tap remove to complete the process."

		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
            msg = "There was a problem removing your device(s). Check the IDE logs for details."
		}
	}

    return dynamicPage(name: "prefUninstall",  title: "Uninstall", install:false, uninstall:true) {
        section("Uninstallation"){
			paragraph msg
		}
    }
}

def prefListDevices() {
	getVersionInfo(0, 0);
    getSelectedDevices("lights")
    if (forceLogin()) {
		def doorList = getDoorList()
		if ((state.doorList) || (state.lightList)){
        	def nextPage = "prefSensor1"
            if (!state.doorList){nextPage = "summary"}  //Skip to summary if there are no doors to handle
                return dynamicPage(name: "prefListDevices",  title: "Devices", nextPage:nextPage, install:false, uninstall:true) {
                    if (state.doorList) {
                        section("Select which garage door/gate to use"){
                            input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:state.doorList])
                        }
                    }
                    if (state.lightList) {
                        section("Select which lights to use"){
                            input(name: "lights", type: "enum", required:false, multiple:true, metadata:[values:state.lightList])
                        }
                    }
                    section("Advanced (optional)", hideable: true, hidden:true){
        	            paragraph "BETA: Enable the below option if you would like to force the Garage Doors to behave as Door Locks (sensor required)." +
                        			"This may be desirable if you only want doors to open up via PIN with Alexa voice commands. " +
                                    "Note this is still considered highly experimental and may break many other automations/apps that need the garage door capability."
        	            input "prefUseLockType", "bool", required: false, title: "Create garage doors as door locks?"
					}
                }

        }else {
			def devList = getDeviceList()
			return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
				section(""){
					paragraph "Could not find any supported device(s). Please report to author about these devices: " +  devList
				}
			}
		}


	} else {
		return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
			section(""){
				paragraph "The username or password you entered is incorrect. Try again. "
			}
		}
	}
}


def prefSensor1() {
    log.debug "Doors chosen: " + doors

    //Sometimes ST has an issue where stale options are not properly dropped from settings. Let's get a true count of valid doors selected
    state.validatedDoors = []
    if (doors instanceof List && doors.size() > 1){
        doors.each {
            if (state.data[it] != null){
                state.validatedDoors.add(it)
            }
        }
    }
    else{
    	state.validatedDoors = doors	//Handle single door
    }

    log.debug "Valid doors chosen: " + state.validatedDoors

    //Set defaults
    def nextPage = "summary"
    def titleText = ""

    //If no doors chosen, skip to summary
    if (!state.validatedDoors){
        return dynamicPage(name: "noDoorsSelected",  title: "No doors selected. Tap next to finish.", nextPage:nextPage, install:false, uninstall:true) {
            section(titleText){
                paragraph "No doors selected. Tap next to finish"
            }
        }
    }


    //Determine if we have multiple doors and need to send to another page
    if (doors instanceof String){ //simulator seems to just make a single door a string. For that reason we have this weird check.
        log.debug "Single door detected (string)."
        titleText = "Select Sensors for Door 1 (" + state.data[doors].name + ")"
    }
    else if (doors.size() == 1){
        log.debug "Single door detected (array)."
        titleText = "Select Sensors for Door 1 (" + state.data[doors[0]].name + ")"
    }
    else{
        log.debug "Multiple doors detected."
        log.debug state.validatedDoors[0]
        nextPage = "prefSensor2"
        titleText = "OPTIONAL: Select Sensors for Door 1 (" + state.data[state.validatedDoors[0]].name + ")"
    }


    return dynamicPage(name: "prefSensor1",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
			paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
            input(name: "door1Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door1Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor1PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor2() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 2 (" + state.data[state.validatedDoors[1]].name + ")"

    if (state.validatedDoors.size() > 2){
    	nextPage = "prefSensor3"
    }

    return dynamicPage(name: "prefSensor2",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door2Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door2Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor2PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor3() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 3 (" + state.data[state.validatedDoors[2]].name + ")"

    if (state.validatedDoors.size() > 3){
    	nextPage = "prefSensor4"
    }

    return dynamicPage(name: "prefSensor3",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door3Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door3Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor3PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor4() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 4 (" + state.data[state.validatedDoors[3]].name + ")"

    if (state.validatedDoors.size() > 4){
    	nextPage = "prefSensor5"
    }

    return dynamicPage(name: "prefSensor4",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door4Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door4Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor4PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor5() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 5 (" + state.data[state.validatedDoors[4]].name + ")"

    if (state.validatedDoors.size() > 5){
    	nextPage = "prefSensor6"
    }

    return dynamicPage(name: "prefSensor5",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door5Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door5Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor5PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor6() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 6 (" + state.data[state.validatedDoors[5]].name + ")"

    if (state.validatedDoors.size() > 6){
    	nextPage = "prefSensor7"
    }

    return dynamicPage(name: "prefSensor6",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door6Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door6Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor6PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor7() {
    def nextPage = "summary"
    def titleText = "Sensors for Door 7 (" + state.data[state.validatedDoors[6]].name + ")"

    if (state.validatedDoors.size() > 7){
    	nextPage = "prefSensor8"
    }

    return dynamicPage(name: "prefSensor7",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door7Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door7Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor7PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor8() {
	def titleText = "Contact Sensor for Door 8 (" + state.data[state.validatedDoors[7]].name + ")"
    return dynamicPage(name: "prefSensor8",  title: "Optional Sensors and Push Buttons", nextPage:"summary", install:false, uninstall:true) {
        section(titleText){
            paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " +
            	"as a switch you can turn on (to open) and off (to close). Note: if you choose an acceleration sensor, you must also choose a contact sensor."
			input(name: "door8Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: "false", multiple: "false")
			input(name: "door8Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."
            input "prefDoor4PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def summary() {
	state.installMsg = ""
    initialize()
    versionCheck()
    return dynamicPage(name: "summary",  title: "Summary", install:true, uninstall:true) {
        section("Installation Details:"){
			paragraph state.installMsg
            paragraph state.versionWarning
		}
    }
}

/* Initialization */
def installed() {
	if (door1Sensor && state.validatedDoors){
    	refreshAll()
        unschedule()
    	runEvery30Minutes(refreshAll)
    }
}

def updated() {
	log.debug "Updated..."
    if (state.previousVersion != state.thisSmartAppVersion){
    	getVersionInfo(state.previousVersion, state.thisSmartAppVersion);
    }
    if (door1Sensor && state.validatedDoors){
    	refreshAll()
        unschedule()
    	runEvery30Minutes(refreshAll)
    }
}

def uninstall(){
    log.debug "Removing MyQ Devices..."
    childDevices.each {
		try{
			deleteChildDevice(it.deviceNetworkId, true)
		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}
}

def uninstalled() {
	log.debug "MyQ removal complete."
    getVersionInfo(state.previousVersion, 0);
}

def initialize() {
	unsubscribe()
    log.debug "Initializing..."
    login()
    state.sensorMap = [:]

    // Get initial device status in state.data
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]

	// Create selected devices
	def doorsList = getDoorList()
	def lightsList = state.lightList

    if (doors != null){
        def firstDoor = state.validatedDoors[0]
        //Handle single door (sometimes it's just a dumb string thanks to the simulator)
        if (doors instanceof String)
        firstDoor = doors

        //Create door devices
        createChilDevices(firstDoor, door1Sensor, doorsList[firstDoor], prefDoor1PushButtons)
        if (state.validatedDoors[1]) createChilDevices(state.validatedDoors[1], door2Sensor, doorsList[state.validatedDoors[1]], prefDoor2PushButtons)
        if (state.validatedDoors[2]) createChilDevices(state.validatedDoors[2], door3Sensor, doorsList[state.validatedDoors[2]], prefDoor3PushButtons)
        if (state.validatedDoors[3]) createChilDevices(state.validatedDoors[3], door4Sensor, doorsList[state.validatedDoors[3]], prefDoor4PushButtons)
        if (state.validatedDoors[4]) createChilDevices(state.validatedDoors[4], door5Sensor, doorsList[state.validatedDoors[4]], prefDoor5PushButtons)
        if (state.validatedDoors[5]) createChilDevices(state.validatedDoors[5], door6Sensor, doorsList[state.validatedDoors[5]], prefDoor6PushButtons)
        if (state.validatedDoors[6]) createChilDevices(state.validatedDoors[6], door7Sensor, doorsList[state.validatedDoors[6]], prefDoor7PushButtons)
        if (state.validatedDoors[7]) createChilDevices(state.validatedDoors[7], door8Sensor, doorsList[state.validatedDoors[7]], prefDoor8PushButtons)
    }

    //Create light devices
    def selectedLights = getSelectedDevices("lights")
    selectedLights.each {
    	log.debug "Checking for existing light: " + it
    	if (!getChildDevice(it)) {
        	log.debug "Creating child light device: " + it

            try{
            	addChildDevice("brbeaird", "MyQ Light Controller", it, getHubID(), ["name": lightsList[it]])
                state.installMsg = state.installMsg + lightsList[it] + ": created light device. \r\n\r\n"
            }
            catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
            {
                log.debug "Error! " + e
                state.installMsg = state.installMsg + lightsList[it] + ": problem creating light device. Check your IDE to make sure the brbeaird : MyQ Light Controller device handler is installed and published. \r\n\r\n"
            }
        }
        else{
        	log.debug "Light device already exists: " + it
            state.installMsg = state.installMsg + lightsList[it] + ": light device already exists. \r\n\r\n"
        }

    }

    // Remove unselected devices
    def selectedDevices = [] + getSelectedDevices("doors") + getSelectedDevices("lights")
    getChildDevices().each{
        //Modify DNI string for the extra pushbuttons to make sure they don't get deleted unintentionally
        def DNI = it?.deviceNetworkId
        DNI = DNI.replace(" Opener", "")
        DNI = DNI.replace(" Closer", "")

        if (!(DNI in selectedDevices)){
            log.debug "found device to delete: " + it
            try{
                	deleteChildDevice(it.deviceNetworkId, true)
            } catch (e){
                	sendPush("Warning: unable to delete door or button - " + it + "- you'll need to manually remove it.")
                    log.debug "Error trying to delete device " + it + " - " + e
                    log.debug "Device is likely in use in a Routine, or SmartApp (make sure and check SmarTiles!)."
            }
        }
    }

    //Create subscriptions
    if (door1Sensor)
        subscribe(door1Sensor, "contact", sensorHandler)
    if (door2Sensor)
        subscribe(door2Sensor, "contact", sensorHandler)
    if (door3Sensor)
        subscribe(door3Sensor, "contact", sensorHandler)
    if (door4Sensor)
        subscribe(door4Sensor, "contact", sensorHandler)
    if (door5Sensor)
        subscribe(door5Sensor, "contact", sensorHandler)
    if (door6Sensor)
        subscribe(door6Sensor, "contact", sensorHandler)
    if (door7Sensor)
        subscribe(door7Sensor, "contact", sensorHandler)
    if (door8Sensor)
        subscribe(door8Sensor, "contact", sensorHandler)

    if (door1Acceleration)
        subscribe(door1Acceleration, "acceleration", sensorHandler)
    if (door2Acceleration)
        subscribe(door2Acceleration, "acceleration", sensorHandler)
    if (door3Acceleration)
        subscribe(door3Acceleration, "acceleration", sensorHandler)
    if (door4Acceleration)
        subscribe(door4Acceleration, "acceleration", sensorHandler)
    if (door5Acceleration)
        subscribe(door5Acceleration, "acceleration", sensorHandler)
    if (door6Acceleration)
        subscribe(door6Acceleration, "acceleration", sensorHandler)
    if (door7Acceleration)
        subscribe(door7Acceleration, "acceleration", sensorHandler)
    if (door8Acceleration)
        subscribe(door8Acceleration, "acceleration", sensorHandler)

    //Set initial values
    if (door1Sensor && state.validatedDoors){
    	log.debug "Doing the sync"
    	syncDoorsWithSensors()
    }

    //Force a refresh sync with sensors on mode change and each day at sunrise and sunset (in cases where the devices become out of sync)
    subscribe(location, "mode", refreshAll)
    subscribe(location, "sunset", refreshAll)
    subscribe(location, "sunrise", refreshAll)
}

def createChilDevices(door, sensor, doorName, prefPushButtons){
	log.debug "In CreateChild"
    def sensorTypeName = "MyQ Garage Door Opener"
    def noSensorTypeName = "MyQ Garage Door Opener-NoSensor"
    def lockTypeName = "MyQ Lock Door"

    if (door){
        //Has door's child device already been created?
        def existingDev = getChildDevice(door)
        def existingType = existingDev?.typeName

        if (existingDev){
        	log.debug "Child already exists for " + doorName + ". Sensor name is: " + sensor
            state.installMsg = state.installMsg + doorName + ": door device already exists. \r\n\r\n"

            if (prefUseLockType && existingType != lockTypeName){
                try{
                    log.debug "Type needs updating to Lock version"
                    existingDev.deviceType = lockTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to lock version." + "\r\n\r\n"
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to no-sensor type. Check your IDE to make sure the brbeaird : " + lockTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }
            else if ((!sensor) && existingType != noSensorTypeName){
            	try{
                    log.debug "Type needs updating to no-sensor version"
                    existingDev.deviceType = noSensorTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to No-sensor version." + "\r\n\r\n"
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to no-sensor type. Check your IDE to make sure the brbeaird : " + noSensorTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }

            else if (sensor && existingType != sensorTypeName && !prefUseLockType){
            	try{
                    log.debug "Type needs updating to sensor version"
                    existingDev.deviceType = sensorTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to sensor version." + "\r\n\r\n"
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to sensor type. Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }
        }
        else{
            log.debug "Creating child door device " + door

                if (prefUseLockType){
                try{
                        log.debug "Creating door with lock type"
                        addChildDevice("brbeaird", lockTypeName, door, getHubID(), ["name": doorName])
                        state.installMsg = state.installMsg + doorName + ": created lock device \r\n\r\n"
                    }
                    catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                    {
                        log.debug "Error! " + e
                        state.installMsg = state.installMsg + doorName + ": problem creating door device (lock type). Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"

                    }
                }

                else if (sensor){
                    try{
                        log.debug "Creating door with sensor"
                        addChildDevice("brbeaird", sensorTypeName, door, getHubID(), ["name": doorName])
                        state.installMsg = state.installMsg + doorName + ": created door device (sensor version) \r\n\r\n"
                    }
                    catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                    {
                        log.debug "Error! " + e
                        state.installMsg = state.installMsg + doorName + ": problem creating door device (sensor type). Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"

                    }
                }
                else{
                    try{
                        log.debug "Creating door with no sensor"
                        addChildDevice("brbeaird", noSensorTypeName, door, getHubID(), ["name": doorName])
                        state.installMsg = state.installMsg + doorName + ": created door device (no-sensor version) \r\n\r\n"
                    }
                    catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                    {
                        log.debug "Error! " + e
                        state.installMsg = state.installMsg + doorName + ": problem creating door device (no-sensor type). Check your IDE to make sure the brbeaird : " + noSensorTypeName + " device handler is installed and published. \r\n\r\n"
                    }
                }

        }

        //Create push button devices
        if (prefPushButtons){
        	def existingOpenButtonDev = getChildDevice(door + " Opener")
            def existingCloseButtonDev = getChildDevice(door + " Closer")
            if (!existingOpenButtonDev){
                try{
                	def openButton = addChildDevice("smartthings", "Momentary Button Tile", door + " Opener", getHubID(), [name: doorName + " Opener", label: doorName + " Opener"])
                	state.installMsg = state.installMsg + doorName + ": created push button device. \r\n\r\n"
                	subscribe(openButton, "momentary.pushed", doorButtonOpenHandler)
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem creating push button device. Check your IDE to make sure the smartthings : Momentary Button Tile device handler is installed and published. \r\n\r\n"
                }
            }
            else{
            	subscribe(existingOpenButtonDev, "momentary.pushed", doorButtonOpenHandler)
                state.installMsg = state.installMsg + doorName + ": push button device already exists. Subscription recreated. \r\n\r\n"
                log.debug "subscribed to button: " + existingOpenButtonDev



            }

            if (!existingCloseButtonDev){
                try{
                    def closeButton = addChildDevice("smartthings", "Momentary Button Tile", door + " Closer", getHubID(), [name: doorName + " Closer", label: doorName + " Closer"])
                    subscribe(closeButton, "momentary.pushed", doorButtonCloseHandler)
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                }
            }
            else{
                subscribe(existingCloseButtonDev, "momentary.pushed", doorButtonCloseHandler)
            }
        }

        //Cleanup defunct push button devices if no longer wanted
        else{
        	def pushButtonIDs = [door + " Opener", door + " Closer"]
            log.debug "ID's to look for: " + pushButtonIDs
            def devsToDelete = getChildDevices().findAll { pushButtonIDs.contains(it.deviceNetworkId)}
            log.debug "button devices to delete: " + devsToDelete
			devsToDelete.each{
            	log.debug "deleting button: " + it
                try{
                	deleteChildDevice(it.deviceNetworkId, true)
                } catch (e){
                	//sendPush("Warning: unable to delete virtual on/off push button - you'll need to manually remove it.")
                    state.installMsg = state.installMsg + "Warning: unable to delete virtual on/off push button - you'll need to manually remove it. \r\n\r\n"
                    log.debug "Error trying to delete button " + it + " - " + e
                    log.debug "Button  is likely in use in a Routine, or SmartApp (make sure and check SmarTiles!)."
                }

            }
        }
    }
}


def syncDoorsWithSensors(child){
    def firstDoor = state.validatedDoors[0]

    //Handle single door (sometimes it's just a dumb string thanks to the simulator)
    if (doors instanceof String)
    firstDoor = doors

    def doorDNI = null
    if (child) {								// refresh only the requesting door (makes things a bit more efficient if you have more than 1 door
    	doorDNI = child.device.deviceNetworkId
        switch (doorDNI) {
        	case firstDoor:
            	updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, child)
                break
            case state.validatedDoors[1]:
            	updateDoorStatus(state.validatedDoors[1], door2Sensor, door2Acceleration, door2ThreeAxis, child)
                break
            case state.validatedDoors[2]:
            	updateDoorStatus(state.validatedDoors[2], door3Sensor, door3Acceleration, door3ThreeAxis, child)
                break
            case state.validatedDoors[3]:
            	updateDoorStatus(state.validatedDoors[3], door4Sensor, door4Acceleration, door4ThreeAxis, child)
            case state.validatedDoors[4]:
            	updateDoorStatus(state.validatedDoors[4], door5Sensor, door5Acceleration, door5ThreeAxis, child)
            case state.validatedDoors[5]:
            	updateDoorStatus(state.validatedDoors[5], door6Sensor, door6Acceleration, door6hreeAxis, child)
            case state.validatedDoors[6]:
            	updateDoorStatus(state.validatedDoors[6], door7Sensor, door7Acceleration, door7ThreeAxis, child)
            case state.validatedDoors[7]:
            	updateDoorStatus(state.validatedDoors[7], door8Sensor, door8Acceleration, door8ThreeAxis, child)
     	}
    } else {    					// refresh ALL the doors
		if (firstDoor) updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, null)
		if (state.validatedDoors[1]) updateDoorStatus(state.validatedDoors[1], door2Sensor, door2Acceleration, door2ThreeAxis, null)
		if (state.validatedDoors[2]) updateDoorStatus(state.validatedDoors[2], door3Sensor, door3Acceleration, door3ThreeAxis, null)
		if (state.validatedDoors[3]) updateDoorStatus(state.validatedDoors[3], door4Sensor, door4Acceleration, door4ThreeAxis, null)
        if (state.validatedDoors[4]) updateDoorStatus(state.validatedDoors[4], door5Sensor, door5Acceleration, door5ThreeAxis, null)
        if (state.validatedDoors[5]) updateDoorStatus(state.validatedDoors[5], door6Sensor, door6Acceleration, door6ThreeAxis, null)
        if (state.validatedDoors[6]) updateDoorStatus(state.validatedDoors[6], door7Sensor, door7Acceleration, door7ThreeAxis, null)
        if (state.validatedDoors[7]) updateDoorStatus(state.validatedDoors[7], door8Sensor, door8Acceleration, door8ThreeAxis, null)
    }
}

def updateDoorStatus(doorDNI, sensor, acceleration, threeAxis, child){
    try{

        if (sensor == null)
            return 0

        //Get door to update and set the new value
        def doorToUpdate = getChildDevice(doorDNI)
        def doorName = "unknown"
        if (state.data[doorDNI]){
            doorName = state.data[doorDNI].name
        }

        def value = "unknown"
        def moving = "unknown"
        def door = doorToUpdate.latestValue("door")

        if (acceleration) moving = acceleration.latestValue("acceleration")
        if (sensor) value = sensor.latestValue("contact")

        if (moving == "active") {
            if (value == "open") {
                if (door != "opening") value = "closing" else value = "opening"  // if door is "open" or "waiting" change to "closing", else it must be "opening"
            } else if (value == "closed") {
                if (door != "closing") 	value = "opening" else value = "closed"
            }
        } else if (moving == "inactive") {
            if (door == "closing") {
                if (value == "open") { 	// just stopped but door is still open
                    value = "stopped"
                }
            }
        }

        doorToUpdate.updateDeviceStatus(value)
        doorToUpdate.updateDeviceSensor("${sensor} is ${sensor?.currentContact}")

        log.debug "Door: " + doorName + ": Updating with status - " + value + " -  from sensor " + sensor

        //Write to child log if this was initiated from one of the doors
        if (child)
            child.log("Door: " + doorName + ": Updating with status - " + value + " -  from sensor " + sensor)

        if (acceleration) {
            doorToUpdate.updateDeviceMoving("${acceleration} is ${moving}")
            log.debug "Door: " + doorName + ": Updating with status - " + moving + " - from sensor " + acceleration
            if (child)
                child.log("Door: " + doorName + ": Updating with status - " + moving + " - from sensor " + acceleration)
        }

        //Get latest activity timestamp for the sensor (data saved for up to a week)
        def eventsSinceYesterday = sensor.eventsSince(new Date() - 7)
        def latestEvent = eventsSinceYesterday[0]?.date
        def timeStampLogText = "Door: " + doorName + ": Updating timestamp to: " + latestEvent + " -  from sensor " + sensor

        if (!latestEvent)	//If the door has been inactive for more than a week, timestamp data will be null. Keep current value in that case.
            timeStampLogText = "Door: " + doorName + ": Null timestamp detected "  + " -  from sensor " + sensor + " . Keeping current value."
        else
            doorToUpdate.updateDeviceLastActivity(latestEvent)

        log.debug timeStampLogText

        //Write to child log if this was initiated from one of the doors
        if (child)
            child.log(timeStampLogText)

    }catch (e) {
        log.debug "Error updating door: ${doorDNI}: ${e}"
    }
}

def refresh(child){
    def door = child.device.deviceNetworkId
    def doorName = state.data[door].name
    child.log("refresh called from " + doorName + ' (' + door + ')')
    syncDoorsWithSensors(child)
}

def refreshAll(){
	getChildDevices().each{
    	syncDoorsWithSensors(it)
    }
}

def refreshAll(evt){
	refreshAll()
}

def sensorHandler(evt) {
    log.debug "Sensor change detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId

	//If we're seeing vibration sensor values, ignore them if it's been more than 30 seconds after a command was sent.
    // This keeps us from seeing phantom entries from overly-sensitive sensors
	if (evt.value == "active" || evt.value == "inactive"){
		if (state.lastCommandSent == null || state.lastCommandSent > now()-30000){
    		return 0;
    	}
	}

    switch (evt.deviceId) {
    	case door1Sensor.id:
        case door1Acceleration?.id:
            def firstDoor = state.validatedDoors[0]
			if (doors instanceof String) firstDoor = doors
        	updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, null)
            break
    	case door2Sensor?.id:
        case door2Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[1], door2Sensor, door2Acceleration, door2ThreeAxis, null)
            break
        case door3Sensor?.id:
        case door3Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[2], door3Sensor, door3Acceleration, door3ThreeAxis, null)
            break
    	case door4Sensor?.id:
        case door4Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[3], door4Sensor, door4Acceleration, door4ThreeAxis, null)
            break
        case door5Sensor?.id:
        case door5Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[4], door5Sensor, door5Acceleration, door5ThreeAxis, null)
            break
        case door6Sensor?.id:
        case door6Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[5], door6Sensor, door6Acceleration, door6ThreeAxis, null)
            break
        case door7Sensor?.id:
        case door7Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[6], door7Sensor, door7Acceleration, door7ThreeAxis, null)
            break
        case door8Sensor?.id:
        case door8Acceleration?.id:
        	updateDoorStatus(state.validatedDoors[7], door8Sensor, door8Acceleration, door8ThreeAxis, null)
            break
        default:
			syncDoorsWithSensors()
    }
}

def doorButtonOpenHandler(evt) {
    log.debug "Door open button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
    def doorDeviceDNI = evt.getDevice().deviceNetworkId
    doorDeviceDNI = doorDeviceDNI.replace(" Opener", "")
    def doorDevice = getChildDevice(doorDeviceDNI)
    log.debug "Opening door."
    doorDevice.openPrep()
    sendCommand(doorDevice, "desireddoorstate", 1)
}

def doorButtonCloseHandler(evt) {
    log.debug "Door close button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
    def doorDeviceDNI = evt.getDevice().deviceNetworkId
    doorDeviceDNI = doorDeviceDNI.replace(" Closer", "")
	def doorDevice = getChildDevice(doorDeviceDNI)
    log.debug "Closing door."
    doorDevice.closePrep()
    sendCommand(doorDevice, "desireddoorstate", 0)
}


def getSelectedDevices( settingsName ) {
	def selectedDevices = []
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName)))
	return selectedDevices
}

/* Access Management */
private forceLogin() {
	//Reset token and expiry
	state.session = [ brandID: 0, brandName: settings.brand, securityToken: null, expiration: 0 ]
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]
	return doLogin()
}

private login() { return (!(state.session.expiration > now())) ? doLogin() : true }

private doLogin() {
    log.trace "Logging in"

    return apiPostLogin("/api/v4/User/Validate", [username: settings.username, password: settings.password] ) { response ->
        if (response.data.SecurityToken != null) {
            state.session.brandID = response.data.BrandId
            state.session.brandName = response.data.BrandName
            state.session.securityToken = response.data.SecurityToken
            state.session.expiration = now() + (7*24*60*60*1000) // 7 days default
            return true
        } else {
            log.warn "No security token found, login unsuccessful"
            state.session = [ brandID: 0, brandName: settings.brand, securityToken: null, expiration: 0 ] // Reset token and expiration
            return false
        }
    }
}

// Listing all the garage doors you have in MyQ
private getDoorList() {
	state.doorList = [:]
    state.lightList = [:]

    def deviceList = [:]
	apiGet(getDevicesURL(), []) { response ->
		if (response.status == 200) {
            //log.debug "response data: " + response.data
            //sendAlert("response data: " + response.data.Devices)
			response.data.Devices.each { device ->
				// 2 = garage door, 5 = gate, 7 = MyQGarage(no gateway), 9 = commercial door, 17 = Garage Door Opener WGDO
				if (device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7||device.MyQDeviceTypeId == 17||device.MyQDeviceTypeId == 9) {
					log.debug "Found door: " + device.MyQDeviceId
                    def dni = [ app.id, "GarageDoorOpener", device.MyQDeviceId ].join('|')
					def description = ''
                    def doorState = ''
                    def updatedTime = ''
                    device.Attributes.each {

                        if (it.AttributeDisplayName=="desc")	//deviceList[dni] = it.Value
                        {
                        	description = it.Value
                        }

						if (it.AttributeDisplayName=="doorstate") {
                        	doorState = it.Value
                            updatedTime = it.UpdatedTime
						}
					}


                    //Sometimes MyQ has duplicates. Check and see if we've seen this door before
                        def doorToRemove = ""
                        state.data.each { doorDNI, door ->
                        	if (door.name == description){
                            	log.debug "Duplicate door detected. Checking to see if this one is newer..."

                                //If this instance is newer than the duplicate, pull the older one back out of the array
                                if (door.lastAction < updatedTime){
                                	log.debug "Yep, this one is newer."
                                    doorToRemove = door
                                }

                                //If this door is the older one, clear out the description so it will be ignored
                                else{
                                	log.debug "Nope, this one is older. Stick with what we've got."
                                    description = ""
                                }
                            }
                        }
                        if (doorToRemove){
                        	log.debug "Removing older duplicate."
                            state.data.remove(door)
                            state.doorList.remove(door)
                        }

                    //Ignore any doors with blank descriptions
                    if (description != ''){
                        log.debug "Storing door info: " + description + "type: " + device.MyQDeviceTypeId + " status: " + doorState +  " type: " + device.MyQDeviceTypeName
                        deviceList[dni] = description
                        state.doorList[dni] = description
                        state.data[dni] = [ status: doorState, lastAction: updatedTime, name: description, type: device.MyQDeviceTypeId ]
                    }
                    else{
                    	log.debug "Door " + device.MyQDeviceId + " has blank desc field. This is unusual..."
                    }
				}

                //Lights!
                if (device.MyQDeviceTypeId == 3) {
					log.debug "Found light: " + device.MyQDeviceId
                    def dni = [ app.id, "LightController", device.MyQDeviceId ].join('|')
					def description = ''
                    def lightState = ''
                    def updatedTime = ''
                    device.Attributes.each {

                        if (it.AttributeDisplayName=="desc")	//deviceList[dni] = it.Value
                        {
                        	description = it.Value
                        }

						if (it.AttributeDisplayName=="lightstate") {
                        	lightState = it.Value
                            updatedTime = it.UpdatedTime
						}
					}

                    //Ignore any lights with blank descriptions
                    if (description && description != ''){
                        log.debug "Storing light info: " + description + "type: " + device.MyQDeviceTypeId + " status: " + doorState +  " type: " + device.MyQDeviceTypeName
                        deviceList[dni] = description
                        state.lightList[dni] = description
                        state.data[dni] = [ status: lightState, lastAction: updatedTime, name: description, type: device.MyQDeviceTypeId ]
                    }
				}
			}
		}
	}
	return deviceList
}

private getDeviceList() {
	def deviceList = []
	apiGet(getDevicesURL(), []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				log.debug "MyQDeviceTypeId : " + device.MyQDeviceTypeId.toString()
				if (!(device.MyQDeviceTypeId == 1||device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 3||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7)) {
                    device.Attributes.each {
						def description = ''
                        def doorState = ''
                        def updatedTime = ''
                        if (it.AttributeDisplayName=="desc")	//deviceList[dni] = it.Value
                        	description = it.Value

                        //Ignore any doors with blank descriptions
                        if (description && description != ''){
                        	log.debug "found device: " + description
                        	deviceList.add( device.MyQDeviceTypeId.toString() + "|" + device.TypeID )
                        }
					}
				}
			}
		}
	}
	return deviceList
}

def getHubID(){
    def hubs = location.hubs.findAll{ it.type == physicalgraph.device.HubType.PHYSICAL }
    log.debug "Found ${hubs.size()} hub(s) at this location."

    //Try and find a valid hub on the account
    def chosenHub
    hubs.each {
        if (it != null){
        	log.debug "Valid hub found: ${it} (${it.id})"
            chosenHub = it
        }
    }

    if (chosenHub != null){
        log.debug "Chosen hub for child devices: ${chosenHub} (${chosenHub.id})"
        return chosenHub.id
    }
    else{
        log.debug "No physical hubs found. Sending NULL"
        return null
    }
}


/* api connection */
private getDevicesURL(){
	return "/api/v4/UserDeviceDetails/Get"
}

import groovy.transform.Field

@Field final MAX_RETRIES = 2 // Retry count before giving up

// get URL
private getApiURL() {
	if (settings.brand == "Craftsman") {
		return "https://craftexternal.myqdevice.com"
	} else {
		return "https://myqexternal.myqdevice.com"
	}
}

private getApiAppID() {
	if (settings.brand == "Craftsman") {
		return "eU97d99kMG4t3STJZO/Mu2wt69yTQwM0WXZA5oZ74/ascQ2xQrLD/yjeVhEQccBZ"
	} else {
		return "NWknvuBd7LoFHfXmKNMBcgajXtZEgKUh4V7WNzMidrpUUluDpVYVZx+xT4PCM5Kx"
	}
}

// HTTP GET call
private apiGet(apiPath, apiQuery = [], callback = {}) {
    if (!state.session.securityToken) { // Get a token
        if (!doLogin()) {
            log.error "Unable to complete GET, login failed"
            return
        }
    }

    def myHeaders = [
        "User-Agent": "Chamberlain/3.73",
        "SecurityToken": state.session.securityToken,
        "BrandId": "2",
        "ApiVersion": "4.1",
        "Culture": "en",
        "MyQApplicationId": getApiAppID()
    ]

    try {
        httpGet([ uri: getApiURL(), path: apiPath, headers: myHeaders, query: apiQuery ]) { response ->
            log.debug "Got GET response: Retry: ${atomicState.retryCount} of ${MAX_RETRIES}\nSTATUS: ${response.status}\nHEADERS: ${response.headers?.collect { "${it.name}: ${it.value}\n" }}\nDATA: ${response.data}"
            if (response.status == 200) {
                switch (response.data.ReturnCode as Integer) {
                    case -3333: // Login again
                    	if (atomicState.retryCount <= MAX_RETRIES) {
                        	atomicState.retryCount = (atomicState.retryCount ?: 0) + 1
                            log.warn "GET: Login expired, logging in again"
                            doLogin()
                            apiGet(apiPath, apiQuery, callback) // Try again
                        } else {
                            log.warn "Too many retries, dropping request"
                        }
                        break

                    case 0: // Process response
                    	atomicState.retryCount = 0 // Reset it
                    	callback(response)
                        break

                    default:
                    	log.error "Unknown GET return code ${response.data.ReturnCode}, error ${response.data.ErrorMessage}"
                }
            } else {
                log.error "Unknown GET status: ${response.status}"
            }
        }
    }	catch (e)	{
        log.error "API GET Error: $e"
    }
}

// HTTP PUT call
private apiPut(apiPath, apiBody = [], callback = {}) {
    if (!state.session.securityToken) { // Get a token
        if (!doLogin()) {
            log.error "Unable to complete PUT, login failed"
            return
        }
    }
    def myHeaders = [
        "User-Agent": "Chamberlain/3.73",
        "SecurityToken": state.session.securityToken,
        "BrandId": "2",
        "ApiVersion": "4.1",
        "Culture": "en",
        "MyQApplicationId": getApiAppID()
    ]

    try {
        httpPut([ uri: getApiURL(), path: apiPath, headers: myHeaders, body: apiBody ]) { response ->
            log.debug "Got PUT response: Retry: ${atomicState.retryCount} of ${MAX_RETRIES}\nSTATUS: ${response.status}\nHEADERS: ${response.headers?.collect { "${it.name}: ${it.value}\n" }}\nDATA: ${response.data}"
            if (response.status == 200) {
                switch (response.data.ReturnCode as Integer) {
                    case -3333: // Login again
                    	if (atomicState.retryCount <= MAX_RETRIES) {
                        	atomicState.retryCount = (atomicState.retryCount ?: 0) + 1
                            log.warn "PUT: Login expired, logging in again"
                            doLogin()
                            apiPut(apiPath, apiBody, callback) // Try again
                        } else {
                            log.warn "Too many retries, dropping request"
                        }
                        break

                    case 0: // Process response
                    	atomicState.retryCount = 0 // Reset it
                    	callback(response)
                        break

                    default:
                    	log.error "Unknown PUT return code ${response.data.ReturnCode}, error ${response.data.ErrorMessage}"
                }
            } else {
                log.error "Unknown PUT status: ${response.status}"
            }
        }
    } catch (e)	{
        log.error "API PUT Error: $e"
    }
}

// HTTP POST call
private apiPostLogin(apiPath, apiBody = [], callback = {}) {
    def myHeaders = [
        "User-Agent": "Chamberlain/3.73",
        "BrandId": "2",
        "ApiVersion": "4.1",
        "Culture": "en",
        "MyQApplicationId": getApiAppID()
    ]

    try {
        return httpPost([ uri: getApiURL(), path: apiPath, headers: myHeaders, body: apiBody ]) { response ->
            log.debug "Got LOGIN POST response: STATUS: ${response.status}\nHEADERS: ${response.headers?.collect { "${it.name}: ${it.value}\n" }}\nDATA: ${response.data}"
            if (response.status == 200) {
                switch (response.data.ReturnCode as Integer) {
                    case 0: // Process response
                    	return callback(response)
                        break

                    default:
                    	log.error "Unknown LOGIN POST return code ${response.data.ReturnCode}, error ${response.data.ErrorMessage}"
                }
            } else {
                log.error "Unknown LOGIN POST status: ${response.status}"
            }
            
            return false
        }
    } catch (e)	{
        log.warn "API POST Error: $e"
    }
    
    return false
}

// Get Device ID
def getChildDeviceID(child) {
	return child.device.deviceNetworkId.split("\\|")[2]
}

// Get single device status
def getDeviceStatus(child) {
	return state.data[child.device.deviceNetworkId].status
}

// Get single device last activity
def getDeviceLastActivity(child) {
	return state.data[child.device.deviceNetworkId].lastAction.toLong()
}

// Send command to start or stop
def sendCommand(child, attributeName, attributeValue) {
	state.lastCommandSent = now()
    if (login()) {
		//Send command
		apiPut("/api/v4/DeviceAttribute/PutDeviceAttribute", [ MyQDeviceId: getChildDeviceID(child), AttributeName: attributeName, AttributeValue: attributeValue ])

        if ((attributeName == "desireddoorstate") && (attributeValue == 0)) {		// if we are closing, check if we have an Acceleration sensor, if so, "waiting" until it moves
            def firstDoor = state.validatedDoors[0]
    		if (doors instanceof String) firstDoor = doors
        	def doorDNI = child.device.deviceNetworkId
        	switch (doorDNI) {
        		case firstDoor:
                	if (door1Sensor){if (door1Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
                	break
            	case state.validatedDoors[1]:
            		if (door2Sensor){if (door2Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
                	break
            	case state.validatedDoors[2]:
            		if (door3Sensor){if (door3Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
                	break
            	case state.validatedDoors[3]:
            		if (door4Sensor){if (door4Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
        			break
                case state.validatedDoors[4]:
            		if (door5Sensor){if (door5Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
        			break
                case state.validatedDoors[5]:
            		if (door6Sensor){if (door6Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
        			break
                case state.validatedDoors[6]:
            		if (door7Sensor){if (door7Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
        			break
                case state.validatedDoors[7]:
            		if (door8Sensor){if (door8Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("closing")}
        			break
            }
        }
		return true
	}
}



def getVersionInfo(oldVersion, newVersion){
    def params = [
        uri:  'http://www.fantasyaftermath.com/getMyQVersion/' + oldVersion + '/' + newVersion,
        contentType: 'application/json'
    ]
    asynchttp_v1.get('responseHandlerMethod', params)
}

def responseHandlerMethod(response, data) {
    if (response.hasError()) {
        log.error "response has error: $response.errorMessage"
    } else {
        def results = response.json
        state.latestSmartAppVersion = results.SmartApp;
        state.latestDoorVersion = results.DoorDevice;
        state.latestDoorNoSensorVersion = results.DoorDeviceNoSensor;
        state.latestLightVersion = results.LightDevice;
    }

    log.debug "previousVersion: " + state.previousVersion
    log.debug "installedVersion: " + state.thisSmartAppVersion
    log.debug "latestVersion: " + state.latestSmartAppVersion
    log.debug "doorVersion: " + state.latestDoorVersion
    log.debug "doorNoSensorVersion: " + state.latestDoorNoSensorVersion
    log.debug "lightVersion: " + state.latestLightVersion
}

def versionCheck(){
	state.versionWarning = ""
    state.thisDoorVersion = ""
	state.thisDoorNoSensorVersion = ""
    state.thisLightVersion = ""
    state.versionWarning = ""

    def usesDoorDev = false
    def usesDoorNoSensorDev = false
    def usesLightControllerDev = false

    getChildDevices().each { childDevice ->

        try {
            def devType = childDevice.getTypeName()

            if (devType != "Momentary Button Tile"){
                if (devType == "MyQ Garage Door Opener"){
                	usesDoorDev = true
                    state.thisDoorVersion = childDevice.showVersion()
                }
                if (devType == "MyQ Garage Door Opener-NoSensor"){
                	usesDoorNoSensorDev = true
                    state.thisDoorNoSensorVersion = childDevice.showVersion()
                }
                if (devType == "MyQ Light Controller"){
                	usesLightControllerDev = true
                    state.thisLightVersion = childDevice.showVersion()
                }
            }

		} catch (MissingPropertyException e)	{
			log.debug "API Error: $e"
		}
    }

    //log.debug "This door (no sensor) version: " + state.thisDoorNoSensorVersion
    //log.debug "This door version: " + state.thisDoorVersion
    //log.debug "This light version: " + state.thisLightVersion

    if (state.thisSmartAppVersion != state.latestSmartAppVersion) {
    	state.versionWarning = state.versionWarning + "Your SmartApp version (" + state.thisSmartAppVersion + ") is not the latest version (" + state.latestSmartAppVersion + ")\n\n"
	}
	if (usesDoorDev && state.thisDoorVersion != state.latestDoorVersion) {
    	state.versionWarning = state.versionWarning + "Your MyQ Door device version (" + state.thisDoorVersion + ") is not the latest version (" + state.latestDoorVersion + ")\n\n"
    }
	if (usesDoorNoSensorDev && state.thisDoorNoSensorVersion != state.latestDoorNoSensorVersion) {
    	state.versionWarning = state.versionWarning + "Your MyQ Door (No-sensor) device version (" + state.thisDoorNoSensorVersion + ") is not the latest version (" + state.latestDoorNoSensorVersion + ")\n\n"
    }
    if (usesLightControllerDev && state.thisLightVersion != state.latestLightVersion) {
    	state.versionWarning = state.versionWarning + "Your MyQ Light Controller device version (" + state.thisLightVersion + ") is not the latest version (" + state.latestLightVersion + ")\n\n"
    }
    log.debug state.versionWarning
}


def notify(message){
	sendNotificationEvent(message)
}