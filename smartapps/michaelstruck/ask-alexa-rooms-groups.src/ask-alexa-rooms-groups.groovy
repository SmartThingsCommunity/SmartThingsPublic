/**
 *  Ask Alexa Rooms/Groups
 *
 *  Copyright © 2017 Michael Struck
 *  Version 1.0.2 10/31/17
 * 
 *  Version 1.0.0 (9/13/17) - Initial release
 *  Version 1.0.1a (9/26/17) - Fixed text area variable issue
 *  Version 1.0.2 (10/31/17) - Added a summary option for switch status outputs
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
 */
definition(
    name: "Ask Alexa Rooms/Groups",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Extension Application of Ask Alexa. Do not install directly from the Marketplace",
    category: "My Apps",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    )
preferences {
    page name:"mainPage"
    page name:"pageSwitchRPT"
    page name:"pageDoorRPT"
    page name:"pageShadeRPT"
    page name:"pageTstatRPT"
    page name:"pageTempRPT"
    page name:"pageHumidRPT"
    page name:"pageSensors"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title:"Ask Alexa Rooms/Groups Options", install: true, uninstall: true) {
        section {
        	label title:"Room/Group Name (Required)", required: true, image: parent.imgURL() + "room.png"
            href "pageExtAliases", title: "Room/Group Aliases", description: extAliasDesc(), state: extAliasState()
        }
        section ("Devices to control this room/group"){
        	href "pageSwitchRPT", title: "Lights/Switches/Dimmers", description: getDesc("switch"), state: (switches ? "complete" : null), image: parent.imgURL() + "power.png"
            href "pageDoorRPT", title: "Doors/Locks", description: getDesc("door"), state: (locks||doors ? "complete" : null), image: parent.imgURL() + "lock.png"
            href "pageShadeRPT", title: "Window Shades", description: getDesc("shade"), state: (shades ? "complete" : null), image: parent.imgURL() + "shade.png"
            href "pageTstatRPT", title: "Thermostats", description: getDesc("tstat"), state: (tstats ? "complete" : null), image: parent.imgURL() + "temp.png"
        }
        section ("Sensors in this room/group"){
        	href "pageTempRPT", title: "Temperature Sensors", description: getDesc("temp"), state: (temp ? "complete" : null), image: parent.imgURL() + "heating.png"
            href "pageHumidRPT", title: "Humdity Sensors", description: getDesc("humid"), state: (humid ? "complete" : null), image: parent.imgURL() + "humidity.png"
            href "pageSensors", title: "Other Sensors", description: getDesc("sensor"), state: ( motion || contact || water ? "complete" : null), image: parent.imgURL() + "sensor.png"
		}
        if ((doors && shades) || (switches && tstats)){
       		section("Command options"){
            	if (doors && shades) input "openCMD", "enum", title: "Open/Close Command Defaults", options: ["doors":"Open/Close Doors Only", "shades":"Open/Close Window Shades Only", "both":"Open/Close All Devices (Doors and Window Shades)"], defaultValue: "doors", required: false, submitOnChange: true 
            	if (switches && tstats) input "setCMD", "enum", title: "Set Level Command Defaults", options: ["dimmers":"Set Dimmer/Colored Lights Levels Only", "tstats":"Set Thermostats Only"], required: false, defaultValue: "dimmers", submitOnChange: true
            }
        }
        section("Custom acknowledgment (For device action only)"){
        	if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after action runs", required: false, capitalization: "sentences"
        	input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
		}
        //section ("Switch trigger for this macro", hideable: true, hidden: !(rmTriggerSwitch)){
		//	input "rmTriggerSwitch", "capability.switch", title: "Trigger Switch", multiple: false, required: false, submitOnChange:true
		//	paragraph "A virtual dimmer switch is recommended (but not required) to trigger a room/group. You may associate the switch with other automations (including native Alexa Routines) to turn on/off this room/group when the switch state change. ", image: imgURL()+"info.png"
		//}
        section("Tap below to remove this room/group"){ }
	}
}
def pageSwitchRPT() {
	dynamicPage(name: "pageSwitchRPT", install: false, uninstall: false) {
    	section { paragraph "Lights/Switches/Dimmers", image: parent.imgURL() + "power.png" }
    	section (" "){
        	input "switches", "capability.switch", title: "Lights/Switches/Dimmers", multiple: true, required: false, submitOnChange: true
    	}
        if (switches){
            section ("Lighting options"){
            	if ((!setCMD || setCMD == "dimmers")){
					input "defaultDim", "enum", title: "Default Dimmer Level When Turned 'On'", options:
                 	[0:"Set to previous level",10:"10",20:"20",30:"30",40:"40",50:"50",60:"60",70:"70",80:"80",90:"90",100:"100"], defaultValue: 0, required: false
            		input "defaultColor", "enum", title: "Default Color When Turned 'On'", options: parent.STColors().name, required: false
                }
                input "on100", "bool", title: "Turn On All Switches When 100% Level Command Is Given", defaultValue: false 
                input "kLights", "bool", title:"Utilize Kelvin Temperature Settings (For White Kelvin Bulbs)", defaultValue: false
            	input "switchesExcludeOn", "enum", title: "Lights/Switches/Dimmers To Exclude From 'On' Command", options: switchList(), multiple: true, required: false
                input "switchesExcludeOff", "enum", title: "Lights/Switches/Dimmers To Exclude From 'Off' Command", options: switchList(), multiple: true, required: false
            }
            section ("Status reporting"){
            	input "switchesRPT", "bool", title: "Include Switches In Status Report", defaultValue: false, submitOnChange: true
        		if (switchesRPT) {
                	if (!switchesRPTSummary) input "switchesRPTOn", "bool", title: "Report Only Switches That Are On", defaultValue: false, submitOnChange: true
                 	if (!switchesRPTOn && switches.size()>1 ) input "switchesRPTSummary", "bool", title: "Summarize When All Devices Are Not In Same State", defaultValue: false , submitOnChange: true
                }
            }
            if (setCMD == "tstats" && tstats && switches){
            	section ("Please note"){
                	paragraph "You have your default level control set to thermostats only. You can not adjust the levels of the dimmers or colored lights in this room, but can get the status of these devices.", image: parent.imgURL()+"info.png"
                }
            }
        }
    }   
}
def pageDoorRPT() {
	dynamicPage(name: "pageDoorRPT", install: false, uninstall: false) {
    	section { paragraph "Doors/Locks", image: parent.imgURL() + "lock.png" }
    	section ("Doors"){
        	input "doors", "capability.doorControl", title: "Doors", multiple: true, required: false, submitOnChange: true
            if (doors && parent.pwNeeded) input "usePWDoor", "bool", title: "Require PIN For Door Actions", defaultValue: false
        	if (doors) input "doorsRPT", "bool", title: "Include Doors In Status Report", defaultValue: false, submitOnChange: true
            if (doors && doorsRPT) input "doorsRPTOn", "bool", title: "Report Only Doors That Are Open", defaultValue: false
    	}
        section ("Locks"){
        	input "locks", "capability.lock", title: "Locks", multiple: true, required: false, submitOnChange: true
            if (locks && parent.pwNeeded) input "usePWLock", "bool", title: "Require PIN For Lock Actions", defaultValue: false
        	if (locks) input "locksRPT", "bool", title: "Include Locks In Status Report", defaultValue: false, submitOnChange: true
            if (locks && locksRPT) input "locksRPTOn", "bool", title: "Report Only Locks That Are Unlocked", defaultValue: false
    	}
        if (openCMD == "shades" && doors && shades){
			section ("Please note"){
				paragraph "You have your default open/close controls set to window shades only. You can not adjust control the doors in this room, but can get the status of these devices.", image: parent.imgURL()+"info.png"
			}
		}
        if (openCMD == "both" && doors && shades){
			section ("Please note"){
				paragraph "You have your default open/close controls set to control both doors and window shades. Both sets of devices will open or close when you give these commands.", image: parent.imgURL()+"info.png"
			}
		}
    }   
}
def pageShadeRPT() {
	dynamicPage(name: "pageShadeRPT", install: false, uninstall: false) {
    	section { paragraph "Window Shades", image: parent.imgURL() + "shade.png" }
    	section (" "){
        	input "shades", "capability.windowShade", title: "Window Shades", multiple: true, required: false, submitOnChange: true
        	if (shades) input "shadesRPT", "bool", title: "Include Window Shades In Status Report", defaultValue: false, submitOnChange: true
            if (shades && shadesRPT) input "shadesRPTOn", "bool", title: "Report Only Window Shades That Are Open", defaultValue: false
    	}
        if ((openCMD == "doors" || !setCMD) && shades && doors){
			section ("Please note"){
				paragraph "You have your default open/close controls set to doors only. You can not adjust control the window shades in this room, but can get the status of these devices.", image: parent.imgURL()+"info.png"
			}
		}
        if (openCMD == "both" && doors && shades){
			section ("Please note"){
				paragraph "You have your default open/close controls set to control both doors and window shades. Both sets of devices will open or close when you give these commands.", image: parent.imgURL()+"info.png"
			}
		}
    }   
}
def pageTstatRPT() {
	dynamicPage(name: "pageTstatRPT", install: false, uninstall: false) {
    	section { paragraph "Thermostats", image: parent.imgURL() + "temp.png" }
    	section (" "){
        	input "tstats", "capability.thermostat", title: "Thermostats", multiple: true, required: false, submitOnChange: true
        }
        if (tstats){
        	if (setCMD == "tstats" || !switches){
            	section("Default thermostat commands"){
                    if (!tstatCool) input "tstatHeat", "bool", title: "Set Heating Setpoint By Default", defaultValue:false, submitOnChange:true
                    if (!tstatHeat) input "tstatCool", "bool", title: "Set Cooling Setpoint By Default", defaultValue:false, submitOnChange:true
                }
            }
            section("Status Reporting"){
            	input "tstatsRPT", "bool", title: "Include Temperature In Status Report", defaultValue: false, submitOnChange: true
    			if (tstatsRPT) input "tstatsRPTSet", "bool", title: "Include Setpoint In Status Report", defaultValue: false, submitOnChange: true
            	if (tstatsRPT && tstatsRPTSet) input "tstatsRPTSetType", "enum", title: "Which Setpoint To Report", defaultValue: "heatingSetpoint", submitOnChange:true, 
            	options: ["autoAll":"Cooling & Heating Setpoints (must be in a compatible mode to read both values)","coolingSetpoint":"Cooling Setpoint Only","heatingSetpoint": "Heating Setpoint Only","thermostatSetpoint":"Single Setpoint (Not compatible with all thermostats)"]
            }
            if ((setCMD == "dimmers" || !setCMD) && switches && tstats){
            	section ("Please note"){
                	paragraph "You have your default level control set to dimmers and colored lights only. To control the temperature in this room you must append 'heating' or 'cooling' to the temperature commands.", image: parent.imgURL()+"info.png"
                }
            }
		}
    }   
}
def pageTempRPT() {
	dynamicPage(name: "pageTempRPT", install: false, uninstall: false) {
    	section { paragraph "Temperature Sensors", image: parent.imgURL() + "heating.png" }
    	section (" "){
        	input "temp", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false, submitOnChange: true
    		if (temp && temp.size()>1) paragraph "Please note: When multiple temperature devices are selected above, the status output will be an average of the device readings", image: parent.imgURL() + "info.png"
        }
    }   
}
def pageHumidRPT() {
	dynamicPage(name: "pageHumidRPT", install: false, uninstall: false) {
    	section { paragraph "Humidity Sensors", image: parent.imgURL() + "humidity.png" }
    	section (" "){
        	input "humid", "capability.relativeHumidityMeasurement", title: "Humidity Sensors", multiple: true, required: false, submitOnChange: true
        	if (humid && humid.size()>1) paragraph "Please note: When multiple humidity devices are selected above, the status output will be an average of the device readings", image: parent.imgURL() + "info.png"
        }
    }   
}
def pageSensors() {
	dynamicPage(name: "pageSensors", install: false, uninstall: false) {
    	section { paragraph "Other Sensors", image: parent.imgURL() + "sensor.png" }
        section ("Motion sensors"){
            input "motion", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false, submitOnChange: true
            if (motion) input "motionOnly", "bool",title: "Report Only Sensors That Read 'Active'", defaultValue: false
        }
        section ("Open/Close sensors"){
            input "contact", "capability.contactSensor", title: "Open/Close Sensors", multiple: true, required: false, submitOnChange: true
            if (contact) input "openOnly", "bool",title: "Report Only Sensors That Read 'Open'", defaultValue: false
        }
        section ("Water sensors", hideWhenEmpty: true){
			input "water", "capability.waterSensor", title: "Water Sensors", multiple: true, required: false, submitOnChange: true
            if (water) input "wetOnly", "bool", title: "Report Only Water Sensors That Are 'Wet'", defaultValue: false
        }
        
    }   
}
page(name: "pageExtAliases", title: "Enter alias names for this room/group"){
	section {
    	for (int i = 1; i < extAliasCount()+1; i++){
        	input "extAlias${i}", "text", title: "Room/Group Alias Name ${i}", required: false
		}
    }
}
def installed() {
    initialize()
}
def updated() {
	unsubscribe() 
    initialize()
}
def initialize() { }
//Main Handlers
def getOutput(room, mNum, op, param, mPW, xParam){
	String outputTxt = "", result = ""
    if ((tstats && switches && setCMD == "tstats") || (tstats && !switches && (setCMD == "tstats" || !setCMD))){
    	if (tstatHeat && !tstatCool) param="heating"
    	else if (!tstatHeat && tstatCool) param="cooling"
    }
    if (mNum == "0" && op==~/undefined|null/ && param ==~/undefined|null/) op="off"
    def num = mNum ==~/undefined|null|\?/ || !mNum  ? 0 : mNum as int
    num = num < 1 ? 0 : num >99 ? 100 : num
    if (param ==~/heating|cooling|heat|cool/ && num>0) result=setTemp(room, op, param, num)
    else if (param ==~/heating|cooling|heat|cool/ && (num==0 || num==~/null|undefined/)) result="I did not hear a valid setpoint for the temperature setting in the group named ${room}. %1%"
    else if (op ==~/low|medium|high/){
        if (op=="low" && parent.dimmerLow) num = parent.dimmerLow else if (op=="low" && parent.dimmerLow=="") num =0 
        if (op=="medium" && parent.dimmerMed) num = parent.dimmerMed else if (op=="medium" && !parent.dimmerMed) num = 0 
        if (op=="high" && parent.dimmerHigh) num = parent.dimmerHigh else if (op=="high" && !parent.dimmerhigh) num = 0 
        if (num==0) result = "You don't have a default value set up for the '${op}' level. I am not making any changes to '${room}'. %1%"
        else if (num > 0 && param ==~/null|undefined/) result = levelSet(room, num,xParam)
        else if (num > 0 && param ==~/heating|cooling|heat|cool/) result=setTemp(room, op, param, num)
        else if (num > 0 && param !="undefined" && param != "null" && param != "heating" && param != "cooling" && param != "heat" && param != "cool" ) result = colorSet(room, param, num)
    }
    else if (op == "maximum") {
    	if (param ==~/null|undefined/) result = levelSet(room, 100, xParam)
        else if (param ==~/heating|cooling|heat|cool/) result=setTemp(room, op, param, num)
        else result = colorSet(room, param, 100)
    }
    else if (op==~/on|off/ && num==0) result=onOff(room, op)
    else if (op==~/open|close/) result = openClose(room, op, mPW, xParam)
    else if (op==~/lock|unlock/) result = lockUnlock(room, op, mPW)
    else if (op=="toggle") result = roomToggle(room)
    else if (op==~/increase|raise|up|decrease|down|lower|brighten|dim/ && param==~/null|undefined/) result=increaseDecrease(room, op)
    else if (op==~/undefined|status|null/ && num==0 && param ==~/null|undefined/) {
    	op="status"
        result = roomStatus(room)
    }
    else if (num && param ==~/null|undefined/ && switches && !tstats) result = levelSet(room, num, xParam)
    else if (num && param ==~/null|undefined/ && !switches && tstats) result = setTemp(room, op, param, num)
    else if (num && param ==~/null|undefined/ && switches && tstats && setCMD=="tstats") result = setTemp(room, op, param, num)
    else if (num && param ==~/null|undefined/ && switches && tstats && setCMD=="dimmers") result = levelSet(room, num, xParam)
    else if (param !="undefined" && param != "null") result = colorSet(room, param, num)
    else result="I did not understand what you wanted to do with the room, '${room}'. %1%"
    if (op !="status") outputTxt = voicePost && !noAck ? parent.replaceVoiceVar(voicePost,"","","Room", room, 0, xParam) : noAck ? " " : result 
    else outputTxt = result
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%")) outputTxt += "%4%"
    return outputTxt
}
def onOff(room, op){
    String result = ""
    def deviceList, count = 0
    deviceList = switches
    if (deviceList){       
        deviceList.each {
            if (!((switchesExcludeOn && switchesExcludeOn.contains(it.label) && op=="on") || (switchesExcludeOff && switchesExcludeOff.contains(it.label) && op=="off"))){
                it."$op"()
        		count ++
                if (defaultDim && defaultDim != "0" && op=="on" && it.getSupportedCommands().name.contains("setLevel")) it.setLevel(defaultDim as int)
                if (defaultColor && op=="on"){
                    if (defaultColor==~/Soft White|Warm White|Cool Chite|Daylight White/ && kLights && it.getSupportedCommands().name.contains("setColorTemperature")){
            			def sWhite, wWhite, cWhite, dWhite, kelvin
                        sWhite = parent.kSoftWhite ? parent.kSoftWhite as int : 2700
            			wWhite= parent.kWarmWhite ? parent.kWarmWhite as int: 3500
            			cWhite= parent.kCoolWhite ? parent.kCoolWhite as int: 4500
            			dWhite= parent.kDayWhite ? parent.kDayWhite as int: 6500
           				kelvin = defaultColor=="Soft White" ? sWhite : defaultColor=="Warm White" ? wWhite : defaultColor=="Cool White" ? cWhite : defaultColor=="Daylight White" ? dWhite : 9999
        				it.setColorTemperature(kelvin)
                    }
                    else if (it.getSupportedCommands().name.contains("setColor") && !kLights){
                        def getColorData = parent.STColors().find {it.name==defaultColor}
                        if (getColorData){
            				def hueColor = Math.round(getColorData.h / 3.6), satLevel = getColorData.s, colorData = [hue: hueColor as int, saturation: satLevel as int]
                            it.setColor(colorData) 
                    	}
                    }
                }
            }
        }
        def sss= count>1 ? "es" : ""
    	result = "I am turning ${op} the switch${sss} in the group named: '${room}'. "    
    }
    else result = "You don't have any switches to turn ${op} in the group named: '${room}'. %1%"
    return result
}
def lockUnlock(room, op, mPW){
	String result = ""
   	def deviceList = locks, count = 0
    if (deviceList){
    	count=deviceList.size()
        deviceList."$op"()
        def sss= count>1 ? "s" : ""
        result = "I am ${op}ing the device${sss} in the group named: '${room}'. "
    }
    else result = "You don't have any locks in the group named '${room}' to ${op}. %1%"
	return result
}
def openClose(room, op, mPW, xParam){
    String result = ""
    def deviceList, noun = "doors or window shades"
    if (shades && doors){
        if (openCMD=="both"){ 	
			deviceList=doors + shades
			noun = doors.size()>1 && shades.size()>1 ? "doors and window shades" :
				doors.size()==1 && shades.size()>1 ? "door and window shades" :
				doors.size()>1 && shades.size()==1 ? "door and window shade" : "door and window shade"
		}
        else if ((openCMD=="doors" || !openCMD) && doors) { deviceList=doors ; noun = doors.size()>1 ? "doors": "door"	}
        else if (openCMD=="shades" && shades) { deviceList=shades ; noun = shades.size()>1 ? "window shades": "window shade" }
    }
    else if (doors && !shades) { deviceList=doors; noun = doors.size()>1 ? "doors": "door" }
	else if (!doors && shades) { deviceList=shades; noun = shades.size()>1 ? "window shades" : "window shades" }
    if (deviceList){
        deviceList."$op"()
        def verb = op=="close" ? "clos" : "open"
        result = "I am ${verb}ing the ${noun} in the group named: '${room}'. "
    }
    else result = "You don't have any ${noun} to ${op} in the group named: '${room}'. %1%"
	return result
}
def roomToggle(room){
	String result = ""
    def deviceList = switches
    if (deviceList){
        deviceList.each{ 
            def oldstate = it.currentValue("switch"), newstate = oldstate == "off" ? "on" : "off"
            it."$newstate"()
        }
        def sss=deviceList.size() >1 ? "s" : ""
    	result = "I am toggling the on and off state${sss} of the device${sss} in the group named: '${room}'. "   
	}
	else result = "You don't have any switches to turn toggle in the group named: '${room}'. %1%"
    return result
}
def setTemp(room, op, param, num){
	String result = ""
	if (parent.getTstatLimits().hi) num = num <= (parent.getTstatLimits().hi as int)? num : parent.getTstatLimits().hi as int
	if (parent.getTstatLimits().lo) num = num >= (parent.getTstatLimits().lo as int) ? num : parent.getTstatLimits().lo as int
    if (op =="maximum" && parent.getTstatLimits().hi) num = parent.getTstatLimits().hi as int
    if (op =="minimum" && parent.getTstatLimits().lo) num = parent.getTstatLimits().lo as int
    def deviceList = tstats
    if (deviceList){
    	if (param =~/heat|cool/){
        	if (param =~/heat/)  deviceList?.setHeatingSetpoint(num) 
        	if (param =~/cool/) deviceList?.setCoolingSetpoint(num) 
        	def sss=deviceList.size() >1 ? "s" : ""
    		result = "I am setting the ${param} setpoint of the thermostat${sss} to ${num} degrees in the group named: '${room}'. " 
		}
        else result="You must designate a 'heating' or 'cooling' directive when setting the temperature of a group. %1%"
    }
    else result = "You don't have any thermostats to set in the group named: '${room}'. %1%"
    return result
}
def levelSet(room, num, xParam){
	String result = ""
    def lvl = num as int, deviceList = switches.findAll{it.getSupportedCommands().name.contains("setLevel")}, count = deviceList.size() as int
    if (deviceList) {
    	deviceList.each { it.setLevel(lvl) }
  		def sss= count > 1 ? "s" : ""
    	result = "I am setting the dimmable device${sss} to ${lvl}% in the group named: '${room}'. "
    }
    if (on100 && switches && lvl==100) {
		switches?.on()
        count ++
        result = "I am turning on all of the devices and setting them to ${lvl}% in the group named: '${room}'. "
    }
    else if (!count) result = "You don't have any dimmable devices in the group named: '${room}'. %1%"
	return result
}
def increaseDecrease(room, op){
	String result = ""
	def defMove = parent.lightAmt as int ?: 0, count = 0, deviceList
    if ((setCMD=="dimmers" || !setCMD)) deviceList = switches.findAll{it.getSupportedCommands().name.contains("setLevel")}
    if (defMove){
    	if (op==~/decrease|down|lower|dim/) defMove = -defMove
        if (deviceList){
        	deviceList.each{
            	def newLvl = it.currentValue("level") + defMove
                def newLevel = newLvl > 100 ? 100 : newLvl <0 ? 0 : newLvl
                it.setLevel(newLevel)
                if (newLvl > 0 && newLvl <100) count++
        	}
            def sss= count>1 ? "s" : ""
            if (count && op==~/increase|raise|up|brighten/) result = "I am increasing the dimmable device${sss} in the group named: '${room}'. "
            else if (count && op==~/decrease|down|lower|dim/) result = "I am decreasing the dimmable device${sss} in the group named: '${room}'. "
            else if (!count && op==~/increase|raise|up|brighten/) result = "All of the dimmable devices are already at maximum. %1%"
            else if (!count && op==~/decrease|down|lower|dim/) result = "All of the dimmable devices are already dimmed to off. %1%"
    	}
		else if (!deviceList && (setCMD=="dimmers" || !setCMD)) result = "You don't have any dimmable devices in the group named: '${room}'. %1%"
        else if (setCMD=="tstats") result = "Increase and decrease commands are not yet compatible with thermostats in the group named: '${room}'. %1%"
    }
    else result = "You do not have a default ${op} value set up in your smartapp. I did not take any action. %1%."
	return result
}	
def colorSet(room, param, num){
	String result = "", level=""
    def lvl = num as int, colorData, deviceList = switches, countDim = 0, countColor=0
    def getColorData = parent.STColors().find {it.name.toLowerCase()==param}, sWhite, wWhite, cWhite, dWhite, kelvin
    if (deviceList){
        if (param==~/soft white|warm white|cool white|daylight white/ && kLights){
            sWhite = parent.kSoftWhite ? parent.kSoftWhite as int : 2700
            wWhite= parent.kWarmWhite ? parent.kWarmWhite as int: 3500
            cWhite= parent.kCoolWhite ? parent.kCoolWhite as int: 4500
            dWhite= parent.kDayWhite ? parent.kDayWhite as int: 6500
            kelvin = param=="soft white" ? sWhite : param=="warm white" ? wWhite : param=="cool white" ? cWhite : param=="daylight white" ? dWhite : 9999
        }
        if (getColorData){
            def hueColor = Math.round(getColorData.h / 3.6), satLevel = getColorData.s
            colorData = [hue: hueColor as int, saturation: satLevel as int] 
            deviceList.each{ 
                if (it.getSupportedCommands().name.contains("setLevel") && lvl>0) {
                    it.setLevel(lvl)
                    countDim ++
                }
                if (it.getSupportedCommands().name.contains("setColor") && colorData) {
                    it.setColor(colorData) 
                    countColor ++
                }
                if (it.getSupportedCommands().name.contains("setColorTemperature") && param==~/soft white|warm white|cool white|daylight white/ && kLights) it.setColorTemperature(kelvin)
            }
            def ssD= countDim>1 ? "s" : "", ssC= countColor>1 ? "s" : ""
            if (!lvl && countColor && !countDim) result = "I am setting the colored light${ssC} to ${param} in the group named: '${room}'. "
            else if (lvl && countColor && countDim && countColor == countDim) result = "I am setting the colored light${ssC} to ${param} and a level of ${lvl}% in the group named: '${room}'. "
            else if (lvl && countColor && countDim && countColor != countDim)  result ="I am setting the dimmable device${ssD} to ${lvl}% and the colored light${ssD} to ${param} in the group named: '${room}'. "
            else if (lvl && !countColor && countDim)  result ="I am setting the dimmable devices to ${lvl}% in the group named: '${room}'. I did not find any colored lights in this group to set to ${param}. " 
            else if (lvl && !countColor && !countDim)  result ="I didn't find any colored or dimmable lights in the group named: '${room}'.  %1%"
            else if (!lvl  && !countColor && !countDim)  result ="I didn't find any colored lights in the group named: '${room}'.  %1%"
        }
        else result = "I did not understand the color you wanted me to set the lights to. %1%"
    }
    else result = "You don't have any colored lights in the group named: '${room}'. %1%"
    return result
}
def roomStatus(room){
	String result = ""
    if ((switches && switchesRPT)  || (doors && doorsRPT ) || (locks && locksRPT) || (shades && shadesRPT) || (tstats && tstatsRPT) || temp || humid || water || contact || motion) {
        if (switches && switchesRPT){
            def countOn = switches?.findAll{it.currentValue("switch")=="on"}.size() as int, countOff=switches?.findAll{it.currentValue("switch")=="off"}.size() as int, onVerb=countOn==1 ? "is" : "are", offVerb = countOff==1 ? "is" : "are"
            if (switchesRPTOn) switches.each { if (it.currentValue("switch")=="on") result += "The ${it.label} is on. " }	
            else if (switches?.size()>1 && switchesRPTSummary && countOn && countOff) {
            	result += "Of the ${countOn+countOff} switches, ${countOn} ${onVerb} on, ${countOff} ${offVerb} off. "
            }
            else {
            	if (!switches?.currentValue("switch").contains("off")) result += "All of the switches are on. "
                else if (!switches?.currentValue("switch").contains("on")) result += "All of the switches are off. "
                else switches.each{ result += "The ${it.label} is ${it.currentValue('switch')}. " }
            }
        }
		if (doors && doorsRPT){
        	if (doorsRPTOn) doors.each { if (it.currentValue("door")=="open") result += "The '${it.label}' is open. " }	
            else {
            	if (!doors?.currentValue("door").contains("closed")) result += "All of the doors are open. "
                else if (!doors?.currentValue("door").contains("open")) result += "All of the doors are closed. "
                else doors.each{ result += "The ${it.label} is ${it.currentValue('door')}. " }
            }
        }
        if (locks && locksRPT){
        	if (locksRPTOn) locks.each { if (it.currentValue("lock")=="unlocked") result += "The '${it.label}' is unlocked. " }	
            else {
            	if (!locks?.currentValue("lock").contains("unlocked")) result += "All of the locks are locked. "
                else if (!locks?.currentValue("lock").contains("locked")) result += "All of the locks are unlocked. "
                else locks.each{ result += "The ${it.label} is ${it.currentValue("locks")}. " }
            }
        }
		if (shades && shadesRPT){
            if (shadesRPTOn) shades.each { if (it.currentValue("windowShade").toLowerCase()=="open") result += "The '${it.label}' is open. " }	
            else {
            	if (shades?.currentValue("windowShade").contains("Closed") || shades?.currentValue("windowShade").contains("Open") || shades?.currentValue("windowShade").contains("Partially open")){
                	if (!shades?.currentValue("windowShade").contains("Closed") && !shades?.currentValue("windowShade").contains("Partially open")) result += "All of the window shades are open. "
                	else if (!shades?.currentValue("windowShade").contains("Open") && !shades?.currentValue("windowShade").contains("Partially open")) result += "All of the window shades are closed. "
                    else if (shades?.currentValue("windowShade").contains("Partially open") && !shades?.currentValue("windowShade").contains("Closed") && !shades?.currentValue("windowShade").contains("Open") ) result += "All of the window shades are partially open. "
                	else shades.each{ result += "The ${it.label} is ${it.currentValue("windowShade").toLowerCase()}. " }
                }
                else {
            		if (!shades?.currentValue("windowShade").contains("closed")) result += "All of the window shades are open. "
                	else if (!shades?.currentValue("windowShade").contains("open")) result += "All of the window shades are closed. "
                	else shades.each{ result += "The ${it.label} is ${it.currentValue("windowShade").toLowerCase()}. " }
            	}
            }
        }
		if (tstats && tstatsRPT) {
        	tstats.each{ 
            	result += "The ${it.label} is reading ${it.currentValue('temperature')} degrees"
				if (tstatsRPTSet) {
                    if (tstatsRPTSetType == "autoAll") {
                        try { result += ", has a cooling setpoint of ${Math.round(it.latestValue("coolingSetpoint"))} degrees, " +
                        "and a heating setpoint of ${Math.round(deviceName.latestValue("heatingSetpoint"))} degrees"
                        }
                        catch (e) { result += " but is not able to provide one of its setpoints. Ensure you are in a thermostat mode that allows reading of these values" }
                    }
                    else if (tstatsRPTSetType != "autoAll" && it.latestValue(tstatsRPTSetType)) result += " and has a setpoint of ${Math.round(it.latestValue(tstatsRPTSetType))} degrees"
                }
                result +=". "
            }
        }
        if (temp && temp.size() > 1) result += "The average temperature is " + parent.getAverage(temp, "temperature") + " degrees. "
        else if (temp && temp.size() ==1 ) result +="The temperature is ${temp.currentValue("temperature")} degrees. "
        if (humid && humid.size() > 1) result += "The average relative humidity is " + parent.getAverage(humid, "humidity") + "%. "
        else if (humid && humid.size() ==1 ) result +="The relative humidity is ${temp.currentValue("humidity")}%. "  
		if (motion) result += motionReport()
        if (contact) result +=contactReport()
        if (water) result +=waterReport()
        if (result) result = "The group named, '${room}', is reporting the following: " + result
        else result = "None of the devices in the group named, '${room}' are reporting information. This may be normal based on your set up. %1%"
    }
    else result = "There are no devices set up to report status in the '${room}' group. %1%"
    return result 
}
//Common Code
def switchList() { 
	def result=[]
    switches.each{result << it.label} 
	return result
}
def motionReport(type){
    String result =""
    def deviceList = motionOnly ?  motion.findAll{it.currentValue("motion")=="active"} : motion
    if (deviceList) {
        if (motionOnly) deviceList.each { result += "'${it.label}' is reading motion. "}
        else {
            deviceList.each {
                def currVal = [active: "movement", inactive: "no movement"][it.latestValue("motion")] ?: it.latestValue("motion")
                result += "The '${it.label}' is reading " + currVal + ". "
            }
        }
    }
    return result 
}
def contactReport(type){
    String result =""
    def deviceList = openOnly ? contact.findAll{it.latestValue("contact")=="open"} : contact
    if (deviceList){
    	if (openOnly) deviceList.each { result += "'${it.label}' is open. "}
		else deviceList.each {result += "The '${it.label}' is reading ${it.latestValue("contact")}. "}
    }
    return result 
}
def waterReport(type){
    String result =""
    def deviceList = wetOnly ? water.findAll{it.latestValue("water")=="wet"} : water
    if (deviceList){
    	if (wetOnly) deviceList.each { result += "'${it.label}' is read moisture is present. "}
		else deviceList.each {result += "The '${it.label}' is reading ${it.latestValue("water")}. "}
    }
    return result 
}
def extAliasCount() { return 3 }
def extAliasDesc(){
	def result =""
	for (int i= 1; i<extAliasCount()+1; i++){
		result += settings."extAlias${i}" ? settings."extAlias${i}" : ""
		result += (result && settings."extAlias${i+1}") ? "\n" : ""
	}
    result = result ? "Alias Names currently configured; Tap to edit:\n"+result :"Tap to add alias names to this room/group"
    return result
}
def extAliasState(){
	def count = 0
    for (int i= 1; i<extAliasCount()+1; i++){
    	if (settings."extAlias${i}") count ++
    }
    return count ? "complete" : null
}
def getDesc(type){
	def result="Status: UNCONFIGURED - Tap to configure", countNoun, countVerb, countAdj
    switch (type){
    	case "switch" :
            if (switches){
            	countNoun = switches.size()>1 ? "es" : ""
                countVerb = switches.size()>1 ? "are" : "is"
                countAdj = switches.size()>1 ? "all" : "the"
                result= "Switch${countNoun} configured"
                if (switchesExcludeOn || switchesExcludeOff) result +="; Some lights excluded from ${switchesExcludeOn && !switchesExcludeOff ? "'On' command" : switchesExcludeOff && !switchesExcludeOn ? "'Off' command" : "'On' and 'Off' commands"}"
                if (switchesRPT && switchesRPTOn) result += "; Include in status report only when switch${countNoun} ${countVerb} 'On'"
                else if (switchesRPT && switchesRPTSummary) result += "; Summarize status of switch${countNoun}"
                else if (switchesRPT && !switchesRPTOn && !switchesRPTSummary) result += "; Include ${countAdj} switch${countNoun} in status report"
            }
    	break
		case "door" :
            if (doors || locks){
                def devices
                if (doors && !locks) devices = doors.size()>1 ? "Doors" : "Door"
                if (doors && locks) devices = doors.size()>1 && locks.size()>1 ? "Doors & locks" : doors.size()==1 && locks.size()>1 ? "Door & locks" : doors.size()==1 && locks.size()>1 ? "Doors & Lock" :  "Door & Lock"
                if (!doors && locks) devices = locks.size()>1 ? "Locks" : "Lock"
                result = "${devices} configured"   
                if (doors) {
                    countNoun = doors.size()>1 ? "s" : ""
                    countAdj = doors.size()>1 ? "all" : "the"
                    if (doorsRPT && doorsRPTOn) result += "; Include door${countNoun} in status report only when 'Open'"
                    else if (doorsRPT && !doorsRPTOn) result += "; Include ${countAdj} door${countNoun} in status report"
            		if (usePWDoor) result +="; Password required for 'Open'/'Close' actions"
                }
                result += locks && doors && doorsRPT && locksRPT ? "\n" : locks && !doors && locksRPT? "; " : ""
                if (locks) {
                	countNoun = locks.size()>1 ? "s" : ""
                	countAdj = locks.size()>1 ? "all" : "the"
                	if (locksRPT && locksRPTOn) result += "Include lock${countNoun} in status report only when 'Unlocked'"
                	else if (locksRPT && !locksRPTOn) result += "Include ${countAdj} lock${countNoun} in status report"
                    if (usePWLock) result +="; Password required for 'Lock'/'Unlock' actions"
             	}
            }
    	break
        case "shade" :
            if (shades){
            	countNoun = shades.size()>1 ? "s" : ""
                countVerb = shades.size()>1 ? "are" : "is"
                countAdj = shades.size()>1 ? "all" : "the"
                result= "Window shade${countNoun} configured"
                if (shadesRPT && shadessRPTOn) result += "; Include in status report only when window shade${countNoun} ${countVerb} 'Open'"
                else if (shadesRPT && !shadessRPTOn) result += "; Include ${countAdj} window shade${countNoun} in status report"
            }
    	break
		case "tstat" :
            if (tstats){
            	countNoun = tstats.size()>1 ? "s" : ""
                countAdj = tstats.size()>1 ? "all" : "the"
                result= "Thermostat${countNoun} configured"
                if (tstatsRPT) result += "; Include ${countAdj} thermostat${countNoun} in status report"
            }
    	break
        case "temp" :
            if (temp){
            	countNoun = temp.size()>1 ? "s" : ""
                countVerb = temp.size()>1 ? "; Temperature status is average of all devices" : ""
                result= "Temperature sensor${countNoun} configured${countVerb}"
            }
    	break
        case "humid" :
            if (humid){
            	countNoun = humid.size()>1 ? "s" : ""
                countVerb = humid.size()>1 ? "; Humidity status is average of all devices" : ""
                result= "Humidity sensor${countNoun} configured${countVerb}"
            }
    	break
        case "sensor" :
			if (motion || contact || water){
                result = ""
                if (motion){
                    countNoun = motion.size()>1 ? "s" : ""
                    result+= "Motion sensor${countNoun} configured"
                }
                if (motion && contact) result +="\n"
                if (contact){
                    countNoun = contact.size()>1 ? "s" : ""
                    result+= "Open/Close sensor${countNoun} configured"
                }
                if (water && (motion || contact)) result +="\n"
                if (water){
                    countNoun = water.size()>1 ? "s" : ""
                    result+= "Water sensor${countNoun} configured"
                }
            }
    	break
  	}
    return result
}
//Version/Copyright/Information/Help
private versionInt(){ return 102 }
private def textAppName() { return "Ask Alexa Rooms/Groups" }	
private def textVersion() { return "Rooms/Groups Version: 1.0.2 (10/31/2017)" }