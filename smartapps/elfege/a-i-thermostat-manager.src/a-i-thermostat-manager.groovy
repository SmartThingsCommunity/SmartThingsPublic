definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """
Control an unlimited number of devices, sensors and many other features. This manager is probably the most complete and smartest solution you'll find out there. It'll make your 
home more comfortable than you ever thought possible. You might however get surprised by outside's weather so much this app will make you forget it even changes... 
""" ,
    category: "Green Living",

    iconUrl: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg",
    iconX2Url: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg",
    image: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg"
)

preferences {

    page name: "pageSetup"
    page name: "settings"
    page name: "Modes"
    page name: "AI"



}
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {


        section("""Manage one or more thermostats in parallel with several other features such as:

- Home location mode (up to 5 modes)
- open/close windows and/or turn on/off fans instead of AC
- contact sensors 
- humidity measurment
- outside / inside temperatures amplitude
- wind speed 
- subjective ("feels like") outside's temperature
- adjust temperature with presence using motion sensors
- switch on/off state 
"""){  }
        section("Main Settings") {
            href "settings", title: "Thermostats and other devices", description: ""

            href "Modes", title: "Modes and temperatures", description: ""

        }

        section("Set devices and values for smart management") {
            href "AI", title: "Artificial Intelligence: make this app adjust temperatures with different scenarios", description: ""

        }
        section(){
            input(name:"adjustments", type: "enum", title: "Do you want to use dynamic temperatures adjustments?", 
                  options: ["no, just go with my default settings", 
                            "Yes, use a linear variation", "Yes, but use a logarithmic variation"], required: true, submitOnChange: true)
            paragraph """
linear: save power and money and remain comfortable. 
Algorithmic: save less money but be even more comfortable"""

            if(adjustments != "no, just go with my default settings"){
                input(name:"MaxLinearHeat", type:"number", title:"Set a maximum Heating temperature", defaultValue: 78)
                input(name:"MinLinearCool", type:"number", title:"Set a minimum cooling temperature", defaultValue: 65)
            }

        }

        section(){
            mode(title: "Set for specific mode(s)")
        }
        section(){
            input(name:"sendPushMessage", type: "bool", title: "Send Push Notification?")
        }
    }
}
def settings() {

    def pageName = "settings"

    def pageProperties = [
        name:       "settings",
        title:      "Thermostats and other devices",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        section("Select the thermostats you want to control") { 

            input(name: "Thermostats", type: "capability.thermostat", title: "select thermostats", required: false, multiple: true, description: null, submitOnChange: true)
            if(Thermostats){

                input(name: "AltSensor", type: "bool", title: "Control some thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)

                if(AltSensor){

                    def MyThermostats = []
                    Thermostats.each {MyThermostats << "$it"}

                    input(name: "ThermSensor", type: "enum", title: "Which devices do you want to control this way?", multiple: true, options: MyThermostats.sort(), required: true, submitOnChange: true)
                    /*
def ts = ThermSensor.size()
def ThermSensorColl = ThermSensor.collect{ it.toString() }
def therm = 0
def tColl = []
def intersect = null
def intersectMap = [:]
def i = 0
for (ts != 0; i < ts; i++){
therm = Thermostats[i]
tColl << therm[i].collect{it.toString()} // create a non parethesized value 
intersect = tcoll.intersect(ThermSensorColl) // intersect thermostats and atl sensor
intersectMap."$tcoll" = "$intersect"
}                    

log.debug """ intersectMap = $intersectMap"""
*/
                    if(ThermSensor){
                        def i = 0
                        def s = ThermSensor.size()
                        for(s > 0; i < s; i++){
                            input(name: "Sensor${i}", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${ThermSensor[i]}", required: true, multiple: false, description: null)

                        }
                    }
                }
                input(name: "turnOffWhenReached", type: "bool", title: "Turn off thermostats when desired temperature is reached?", required: false, default: false, submitOnChange: true)
            }  
        }
        section("Virtual Thermostat") { 
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}

            input(name: "AddMoreVirT_A", type: "bool", title: "add a Virtual Thermostat", default: false, submitOnChange: true)
            if(AddMoreVirT_A){


                input(name: "VirThermSwitch_1", type: "capability.switch", title: "Control a switch in parallel with one of your thermostat's setpoints", required: true, submitOnChange: true)
                input(name: "coolOrHeat", type: "enum", title: "Cooling or Heating?", options: ["cooling", "heating"], defaultValue: "heating")
                input(name: "OtherSetP", type: "bool", title: "Set points are specific to this heater", default: false, submitOnChange: true)
                if(OtherSetP){
                    if(coolOrHeat == "heating"){
                        input(name: "HSPVir", type: "decimal", title: "Set Heating temperature", required: true,  defaultValue: setH)
                    }
                    else {
                        input(name: "CSPVir", type: "decimal", title: "Set Cooling temperature", required: true,  defaultValue: setH)
                    }
                }
                else {
                    input(name: "VirThermTherm_1", type: "capability.thermostat", title: "Select the thermostat used as set point reference", multiple: false, required: true)
                }
                input(name: "AltSensorVirTherm", type: "bool", title: "Read temperature from a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensorVirTherm){
                    input(name: "VirThermSensor", type: "capability.temperatureMeasurement", title: "Select a sensor", multiple: false, required: true)
                }
                input(name: "VirThermModes", type: "mode", title: "Run only in these modes", multiple: true, required: false)

                input(name: "AddMoreVirT_B", type: "bool", title: "add one more Virtual Thermostat", default: false, submitOnChange: true)

                if(AddMoreVirT_B){
                    input(name: "VirThermSwitch_2", type: "capability.switch", title: "Control a switch in parallel with one of your thermostat's setpoints", required: true, submitOnChange: true)

                    input(name: "coolOrHeat_2", type: "enum", title: "Cooling or Heating?", options: ["cooling", "heating"], required: true)
                    input(name: "OtherSetP_2", type: "bool", title: "Set points are specific to this heater", default: false, submitOnChange: true)
                    if(OtherSetP_2){
                        if(coolOrHeat_2 == "heating"){
                            input(name: "HSPVir_2", type: "decimal", title: "Set Heating temperature", required: true,  defaultValue: setH)
                        }
                        else {
                            input(name: "CSPVir_2", type: "decimal", title: "Set Cooling temperature", required: true,  defaultValue: setH)
                        }
                    }
                    else {
                        input(name: "VirThermTherm_2", type: "capability.thermostat", title: "Select the thermostat used as set point reference", multiple: false, required: true)
                    }
                    input(name: "AltSensorVirTherm_2", type: "bool", title: "Read temperature from a third party sensor", required: false, default: false, submitOnChange: true)
                    if(AltSensorVirTherm_2){
                        input(name: "VirThermSensor_2", type: "capability.temperatureMeasurement", title: "Select a sensor", multiple: false, required: true)
                    }
                    input(name: "VirThermModes_2", type: "mode", title: "Run only in these modes", multiple: true, required: false)

                }

                input(name: "AddMoreVirT_C", type: "bool", title: "add one more Virtual Thermostat", default: false, submitOnChange: true)
                if(AddMoreVirT_C){
                    input(name: "VirThermSwitch_3", type: "capability.switch", title: "Control a switch in parallel with one of your thermostat's setpoints", required: true, submitOnChange: true)

                    input(name: "coolOrHeat_3", type: "enum", title: "Cooling or Heating?", options: ["cooling", "heating"], required: true)
                    input(name: "OtherSetP_3", type: "bool", title: "Set points are specific to this heater", default: false, submitOnChange: true)
                    if(OtherSetP_3){
                        if(coolOrHeat_3 == "heating"){
                            input(name: "HSPVir_3", type: "decimal", title: "Set Heating temperature", required: true,  defaultValue: setH)
                        }
                        else {
                            input(name: "CSPVir_3", type: "decimal", title: "Set Cooling temperature", required: true,  defaultValue: setH)
                        }
                    }
                    else {
                        input(name: "VirThermTherm_3", type: "capability.thermostat", title: "Select the thermostat used as set point reference", multiple: false, required: true)
                    }
                    input(name: "AltSensorVirTherm_3", type: "bool", title: "Read temperature from a third party sensor", required: false, default: false, submitOnChange: true)
                    if(AltSensorVirTherm_3){
                        input(name: "VirThermSensor_3", type: "capability.temperatureMeasurement", title: "Select a sensor", multiple: false, required: true)
                    }
                    input(name: "VirThermModes_3", type: "mode", title: "Run only in these modes", multiple: true, required: false)

                }
            }
        }
    }
}

def Modes(){

    def pageName = "Modes"

    def pageProperties = [
        name:       "Modes",
        title:      "Modes and temperatures",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        if(Thermostats.size() > 0){
            section("Main Mode") {
                input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: true, required: false, submitOnChange: true)
            }
            section("Other Modes"){
                input(name: "Night", type : "mode", title: "Select Night mode", multiple: true, required: true, submitOnChange: true)
                input(name: "Away", type : "mode", title: "Select away mode", multiple: true, required: true, submitOnChange: true)
            }
            section("MoreModes"){ 
                input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
                if(Moremodes){
                    input(name: "CustomMode1", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                    input(name: "CustomMode2", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                }
            }
            section(){
                input(name: "LetAIdoIt", type: "bool", title: "Let A.I. chose best temperatures", required: false, defaut: false, submitOnChange: true)
                def setC = ""
                def setH = ""
                def setCA = ""
                def setHA = ""
                def tsize = Thermostats.size()
                def i = 0
                if(LetAIdoIt){          
                    setC = 72
                    setH = 70
                    setCA = 80
                    setHA = 66
                }

                i = 0
                for(tsize != 0; i < tsize; i++){    
                    if(!LetAIdoIt){
                        input(name: "HSPH${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Home mode", required: true)
                        input(name: "CSPH${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Home mode", required: true)
                        input(name: "HSPN${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Night mode", required: true)
                        input(name: "CSPN${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Night mode", required: true)
                        if(Moremodes){
                            input(name: "HSPCust1_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true)
                            input(name: "CSPCust1_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true)
                            if(CustomMode2){
                                input(name: "HSPCust2_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true)
                                input(name: "CSPCust2_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true)
                            }
                        }
                    }
                    else {
                        input(name: "HSPH${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Home mode", required: true,  defaultValue: setH)
                        input(name: "CSPH${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Home mode", required: true, defaultValue: setC)
                        input(name: "HSPN${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Night mode", required: true, defaultValue: setH)
                        input(name: "CSPN${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Night mode", required: true, defaultValue: setC)
                        if(Moremodes){
                            input(name: "HSPCust1_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true, defaultValue: setH)
                            input(name: "CSPCust1_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true, defaultValue: setC)
                            if(CustomMode2){
                                input(name: "HSPCust2_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true, defaultValue: setH)
                                input(name: "CSPCust2_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true, defaultValue: setC)
                            }
                        }
                    }         
                }
                paragraph "Away modes' values will apply to all thermostats evenly"
                input(name: "HSPA", type: "decimal", title: "Set Heating temperature for $Away mode", required: true, defaultValue: setHA)
                input(name: "CSPA", type: "decimal", title: "Set Cooling temperature for $Away mode", required: true, defaultValue: setCA)
            }
        }
        else {
            section("You must first set at least one thermostat in the settings page before you can define any setting here"){}
        }
    }
}
def AI() {

    def pageName = "AI"

    def pageProperties = [
        name:       "AI",
        title:      "Smart Management",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        section("Turn off thermostats when these contacts are open"){

            input(name: "Maincontacts", type:"capability.contactSensor", title: "Turn off all units when these contacts are open", multiple: true, required: false, submitOnChange: true)
            input(name: "ContactAndSwitch", type: "capability.switch", title: "And also control these switches", multiple: true, required: false, submitOnChange: true)
            input(name: "IsHeatPump", type: "bool", title: "$ContactAndSwitch is a heat pump", default: false, submitOnChange: true)
            if(IsHeatPump){
                paragraph "$ContactAndSwitch will never run when outside' temperature is too low."
                if(!OutsideSensor){
                    paragraph "Make sure you set an outside sensor in the sections below"
                }
            }

            if(Maincontacts){
                input(name: "TimeBeforeClosing", type: "number", title: "Turn off units after this amount of time when contacts are open", required: false, description: "time in seconds", uninstall: true, install: true)
                input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "Enter a safety temperature value")
                input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true, submitOnChange: true)			
            }
            if(ContactAndSwitch){
                input(name: "ToggleBack", type: "bool", title: "Turn $ContactAndSwitch back on once all windows are closed", default: true, submitOnChange: true)
                if(ToggleBack){
                    input(name: "SwitchIfMode", type: "bool", title: "Keep $ContactAndSwitch off at all times when in certain modes", default: false, submitOnChange: true)
                    if(SwitchIfMode){
                        input(name: "SwitchMode", type : "mode", title: "Select modes", multiple: true, required: true, submitOnChange: true)
                    }

                    input "ContactAndSwitchInSameRoom", "bool", title: "$ContactAndSwitch is in the same room as one of my other HVAC units", default: false, submitOnChange: true
                    if(ContactAndSwitchInSameRoom){
                        input "UnitToIgnore", "capability.thermostat", title: "When $ContactAndSwitch is ON, turn off these units", 
                            description: "highly recommended if in the same room ", 
                            required: true, multiple: true
                    }
                }
            }
        }
        section("Exception Thermostat"){
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}

            input(name: "NoTurnOffOnContact", 
                  type: "enum", 
                  options: MyThermostats.sort(),
                  multiple: true, 
                  title: "Do not turn off this unit upon contacts events", 
                  submitOnChange: true, 
                  required: false,
                  description: "select which unit ")

            if(NoTurnOffOnContact){
                def XtraTempSensorColl = XtraTempSensor.collect{ it.toString() }
                def NoTurnOffOnContactColl = NoTurnOffOnContact.collect{ it.toString() }
                def Intersection = XtraTempSensorColl.intersect(NoTurnOffOnContactColl)
                //log.debug "Intersection: ${Intersection}"

                def Same = Intersection.size() != 0
                //log.debug "SAME = $Same"
                if(Same){
                    //log.debug "YES"
                    paragraph """WARNING! You chose $XtraTempSensor as a device to measure inside's temperature AND as the thermostat which will not shut down when 
windows are open. This will prevent windows from opening when outside's temperature is lower than in other rooms but not in this one (since AC may still run in this room)
It is highly recommended to select another device or this app will not be capable of handling temperatures properly"""
                }

                paragraph "Under which modes you want this exception to apply? "
                input(name: "DoNotTurnOffModes", type : "mode", title: "Select which modes", 
                      /*options: ["$Home", "$Night", "$Away", "${CustomMode1[0]}", "${CustomMode2[0]}"], */
                      multiple: true, required: true)
                input(
                    name: "ContactException", 
                    type : "capability.contactSensor", 
                    multiple: true, title: "unless these specific contacts sensors are open", 
                    description: "Select a contact", 
                    required: false, 
                    submitOnChange: true
                )

            }


            if(NoTurnOffOnContact && ContactException && Actuators){

                input(
                    name: "ActuatorException", 
                    type: "capability.switch", 
                    required: true, 
                    multiple: true, 
                    title: "Select the windows' actuator (or fans' switches) associated to this exception contact sensor",
                    submitOnChange: true
                )
                // build strings for message without []
                if(ActuatorException){
                    def MessageStr = new StringBuilder();
                    for (String value : Actuators) {
                        MessageStr.append(value);
                    }
                    def ActuatorsSTr = MessageStr.toString();

                    def MessageStr2 = new StringBuilder();
                    for (String value : ActuatorException) {
                        MessageStr2.append(value);
                    }
                    def ActuatorExceptionSTr = MessageStr2.toString();

                    def MessageStr3 = new StringBuilder();
                    for (String value : ContactException) {
                        MessageStr3.append(value);
                    }
                    def ContactExceptionSTr = MessageStr3.toString();


                    paragraph """
$ActuatorExceptionSTr will not open with wheather like $ActuatorsSTr. 
But it will close with $ActuatorsSTr. 
$ContactExceptionSTr, when open, will turn off AC or heater but $Maincontacts won't. 

"""
                }
            }
            if(ContactException){
                paragraph """
You selected $ContactAndSwitch to be controled upon contact events.
Do you wish to bind it to the same rule and have it controled exclusively with $ContactException events?"""

                input(name: "FollowException", type: "bool", title: "yes? no?", default: false, submitOnChange: true)
            }
        }
        section("Select an outside sensor"){
            paragraph """
This sensor is essential to the A.I. of this app. 
if you do not have an outside temperature measurment device, you can 
allways create a SmartWeater virtual device. It is actually recommended 
for it is more reliable in many aspects than a physical device located outside
Visit Smartthings community which contains many pages indicating how to proceed step by step"""

            input(name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Pick a sensor for Outside's temperature", required: true, multiple: false, description: null, submitOnChange: true)        

            def hasHumidity = OutsideSensor?.hasAttribute("humidity")
            //log.debug "hasHumidity is $hasHumidity .."
            if(hasHumidity){
                //input(name: "HumidityMeasurement", type: "capability.relativeHumidityMeasurement", title: "Pick an outside humidity sensor", required: true, multiple: false, description: null)
                input(name: "HumidityTolerance", type: "number", title: "set a humidity tolerance threshold", required: true, description: "set a humidity threshold")
                paragraph """
This level of humidity will determine how to modulate several values such as, for example, 
cooling set points. The more humid, the more the AC will be 'sensitive' 
and run, the less humid, the less it'll run (and if this option has 
been picked elsewhere, it'll prefer to open the windows or activate a fan)"""
            }
        }
        section("Keep a unit running when a contact sensor is CLOSED"){
            input(name: "KeepACon", type: "bool", title: "Use specific settings when a different contact is CLOSED", default: false, submitOnChange: true, description: "")
            if(KeepACon){

                input(name: "ThermContact", type: "capability.thermostat", required: true, title: "Select a Thermostat", submitOnChange: true, description: "")

                if(ThermContact){
                    input "BedSensor", "capability.contactSensor", title: "Select a contact sensor", multiple: true, required: true, description: "$descript", submitOnChange: true 
                    def MainCon = Maincontacts.collect{ it.toString() }
                    def BedSen = BedSensor.collect{ it.toString() }
                    def SensorIntersection = MainCon.intersect(BedSen)
                    //log.debug "SensorIntersection: ${SensorIntersection}"
                    if(SensorIntersection.size() != 0){
                        //log.debug "WRONG DEVICE"
                        paragraph "WRONG DEVICE! You selected a contact that is already being used by this app. Please select a different contact or uncheck this option" 
                    }
                    input(name: "HSPSetBedSensor", type: "decimal", title: "Set Heating temperature", required: true)
                    input(name: "CSPSetBedSensor", type: "decimal", title: "Set Cooling temperature", required: true)
                }
                if(ContactAndSwitch){
                    input "ControlWithBedSensor", "bool", title: "Keep $ContactAndSwitch on when $BedSensor is closed", default: false
                }
                input "falseAlarmThreshold", "decimal", title: "False alarm threshold", required: false, description: "Number of minutes (default is 2 min)"

            }
        }
        section("Modify setpoints with a switch on/off status"){  
            input(name: "ExceptionSW", type : "bool", 
                  title: "Apply different settings for a specific thermostat when a switch is on", 
                  defaut: false, 
                  submitOnChange: true)

            if(ExceptionSW){  
                def MyThermostats = []
                Thermostats.each {MyThermostats << "$it"}
                input(name: "ExceptionSwTherm", 
                      type: "enum", 
                      options: MyThermostats.sort(),
                      multiple: false, 
                      title: "", 
                      submitOnChange: true, 
                      description: "select which thermostat")

                input(name: "warmerorcooler", type : "enum", title: "Have this room Warmer or cooler?", 
                      required: true, options: ["warmer", "cooler", "more heating, cooler cooling"], submitOnChange: true)
                input(name: "CtrlSwt", type: "capability.switch", title: "Adjust $ExceptionSwTherm settings When this switch is on", required: true, submitOnChange: true)
                input(name: "CtrlSwtModes", type: "mode", title: "only when location is in one of these modes", multiple: true)


                if(warmerorcooler == "more heating, cooler cooling"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $ExceptionSwTherm heat setting When $CtrlSwt is on", required: true, range: "1..5")
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $ExceptionSwTherm cooling setting When $CtrlSwt is on", required: true, range: "1..5")                     
                }
                else if(warmerorcooler == "warmer"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $ExceptionSwTherm for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = AddDegrees?.toInteger()
                    input(name: "SubDegrees", type: "decimal", title:"Enter here the same value than above", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
                    if(AddDegrees){
                        log.info "SubDegrees = $SubDegrees"
                    }
                }
                else if(warmerorcooler == "cooler"){        
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $Thermostat_1 for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = SubDegrees?.toInteger()
                    input(name: "AddDegrees", type: "decimal", title:"Enter the same value", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
                    if(AddDegrees){
                        log.info "AddDegrees = $AddDegrees"
                    }
                }
            }
        }
        section("Save power by turning on some fans or by opening some windows when outside's temperature is nice"){

            paragraph "this section is optimized for windows management but can also be used with fans"
            input(name: "Actuators", type: "capability.switch", required: true, multiple: true, title: "select some fan or windows switches that you want to control with outside's temperature", submitOnChange: true)
            input(name: "NotWindows", type: "bool", title: "Those devices ($Actuators) are NOT windows", default: false, submitOnChange: true)
            if(!NotWindows){
                def HasStop = Actuators?.hasCommand("stop") || Actuators?.hasCommand("Stop") 
                if(HasStop){
                    input(name: "OperatingTime", type: "number", title: "Should I stop opening operation after this amount of time?", 
                          required: false, description: "time in seconds")
                }     
            }
            if(Actuators){
                input(name: "OutsideTempLowThres", type: "number", title: "Outside temperature above which I open windows/turn on Fans", required: true, description: "Outside Temp's Low Threshold")
                input(name: "OutsideTempHighThres", type: "number", title: "Outside temperature above which I keep windows/fans closed/off", required: true, description: "Outside Temp's High Threshold")
                input(name: "ExceptACMode1", type: "mode", title: "if location is in this mode, lower outside temperature High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode1){
                    input(name: "ExceptHighThreshold1", type: "number", title: "pick an offset value for $ExceptACMode1 mode", required: true)
                }
                input(name: "ExceptACMode2", type: "mode", title: "if location is in this mode, lower Outside Temp's High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode2){
                    input(name: "ExceptHighThreshold2", type: "number", title: "pick an offset value for $ExceptACMode2 mode", required: true)
                }
                input(name: "OffSet", type: "decimal", title: "You set Critical Temp at: ${CriticalTemp}. Close windows / turn off fans when inside temp is inferior or equal to this value + OffSet ", required: true, description: "Set OffSet Value")
                paragraph """
if within margin then open windows / turn on fans, but not if inside's temp is lower than heat setting minus offset. 
Reference measurment is taken from $XtraTempSensor. You may also chose to open windows at full lenght whenever outside's temperature 
allows for it, instead of only when cooling is required (see below)"""
                input(name: "OpenWhenEverPermitted", type: "bool", title: "Open in full whenever it is nice outside?", default: false, submitOnChange: true)
                input(name: "OpenInfullWhenAway", type: "bool", title: "Open in full whenever you're away and it's not cold outside?", default: false, submitOnChange: true)
                if(OpenInfullWhenAway){
                    paragraph "Be aware that you have selected $Away as your AWAY mode. Make sure this is the mode for when there's nobody home"
                }
            }
            if(Actuators && !NotWindows){
                input(name: "Venting", type: "bool", title: "Allow A.I. to temporarily vent the place when wheather permits it", default: false, submitOnChange: true)
                if(Venting){
                    def MyWindows = []
                    Actuators?.each {MyWindows << "$it"}


                    paragraph """this option will allow your windows to temporarilly but safely override 
the temperature thresholds you set above and will use outside cool air to cool your 
place and never allow for the A.C. to do it while it's cold enough outside. """

                    input(name: "ActuatorsVenting", type: "capability.switch",
                          title: "using these windows...", 

                          multiple: true,
                          submitOnChange: true,
                          required: true
                         )
                    input(name: "VentingModes", type: "mode", title: "Under which modes should this be happening?", required: true, multiple: true, submitOnChange: true)


                    input(name: "VentingException", type: "bool", title: "Set some exceptions", default: false, submitOnChange: true)
                    if(VentingException){
                        input(name: "ActuatorsVentingException", type: "capability.switch", required: true, 
                              multiple: true, 
                              title: "Make an exception for these windows", 
                              submitOnChange: true, 
                              defaultValue: "$ActuatorException"
                             )
                        def ActuatorsVentingCOLL = ActuatorsVenting.collect{ it.toString() }
                        def ActuatorsVentingExceptionCOLL = ActuatorsVentingException.collect{ it.toString() }
                        def Intersection = ActuatorsVentingExceptionCOLL.intersect(ActuatorsVenting)
                        def WarningSame = Intersection.size() != 0
                        if(WarningSame){
                            paragraph "WARNING! IDENTICAL DEVICES!"
                            //log.debug "WARNING! IDENTICAL DEVICES!"
                        }
                        else {
                            input(name: "VentingModesException", type: "mode", required: true, multiple: true, title: "DO NOT OPEN $ActuatorsVentingException Under these modes", submitOnChange: true)
                        }       
                    }
                }
            }
        }
        section("Micro Location"){
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}
            input(name: "useMotion", type: "bool", title: "Use motion sensors to adjust Thermostats settings when inactive", submitOnChange: true, default: false)

            if(useMotion){
                // //log.debug "motion management"
                // list all modes minus $Away
                def i = 0
                state.modes = []
                def allModes = location.modes
                def amS = allModes.size()
                //log.debug "allModes size: = ${amS}"

                for(amS != 0; i < amS; i++){
                    if(allModes[i] in Away){
                        //log.debug "${allModes[i]} Skipped"
                    }
                    else {
                        state.modes << ["${allModes[i]}"]
                    }
                }

                //log.debug "all location modes available are: = $state.modes"
                /*
input(
name: "howmanySensors", 
type: "number", 
title: "How many sensors do you want to add overall?", 
required: true, 
submitOnChange: true, 
defaultValue: "${Thermostats.size()}"
)*/

                //log.debug "thermMotion list is : $thermMotion"
                def ts = Thermostats.size()

                input(name: "HeatNoMotion", type: "number", title: "Substract this amount of degrees to heat setting", required: true, defaultValue: 2)
                input(name: "CoolNoMotion", type: "number", title: "Add this amount of degrees to cooling setting", required: true, defaultValue: 2)  


                i = 0
                for(ts > 0; i < ts; i++){

                    input(name: "thermMotion${i}", type: "enum",
                          title: "For this thermostat...", 
                          description: "pick your thermostat",
                          options: MyThermostats.sort(),
                          multiple: false,
                          required: true,
                          defaultValue: "${MyThermostats[i]}"

                         )


                    input(name: "MotionSensor${i}", type: "capability.motionSensor", 
                          multiple: true, 
                          title: "Select the sensors to use with ${Thermostats[i]}", 
                          description: "pick a sensor", 
                          required: true,
                          //submitOnChange: true,
                          //defaultValue: "${state.MotionSensor[i]}"

                         )

                    def reference = MyThermostats[i]
                    input(
                        name: "MotionModes${i}", type: "mode", 
                        title: "Use motion only if home is in these modes", 
                        multiple: true, 
                        description: "select a mode", 
                        required: true  , 
                        // submitOnChange: true,
                        //defaultValue: "${state.MotionModesAndItsThermMap.reference}"
                    )


                    paragraph "_______________________________"


                }


                input (name:"minutesMotion", type:"number", title: "For how long there must be no motion for those settings to apply? ", 
                       range: "2..999", 
                       description: "time in minutes",
                       required: false)
                paragraph "this timer will apply indifferently to all selected motion sensors"


            }
        }
    }
}

// install and updated
def installed() {	 
    //log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler

    // default values to avoid NullPointer // must be set as such only for new installation not in init or updated  

    state.humidity = HumidityTolerance - 1

    //log.debug "state.humidity is $state.humidity (updated() loop)"
    state.wind = 4
    state.FeelsLike = OutsideSensor?.latestValue("feelsLike")

    state.OpenByApp = true
    state.ClosedByApp = true // these values must not be reset with updated, only here and modeCHangeHandler

    /* // first default values to be set to any suitable value so it doesn't crash with null value 
// they will be updated within seconds with user's settings 

def t = Thermostats.size()
def loopV = 0
for(t != 0; loopV < t; loopV++){
state."{newValueT${loopV}CSP}" = 72
state."{newValueT${loopV}HSP}" = 72
}
*/

    state.MotionSensor = []
    init()
}
def updated() {
    state.modeStartTime = now() 
    state.sendalert = 0
    state.LastTimeMessageSent = now() as Long // for causes of !OkToOpen message
    state.CriticalMessageSent = [false, false, false]

    log.info "updated with settings = $settings"


    unsubscribe()
    unschedule()

    //state.MotionSensor = []
    subscribe(Maincontacts, "contact.open", contactHandlerOpen)
    subscribe(Maincontacts, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(ContactException, "contact.open", contactExceptionHandlerOpen)
        subscribe(ContactException, "contact.closed", contactExceptionHandlerClosed)
        //  subscribe(ContactException, "contact.open", contactHandlerOpen)
        // subscribe(ContactException, "contact.closed", contactHandlerClosed)
        ////log.debug "subscribed ContactException to ContactException Handler"     
    }

    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	




    init()
}
def init() {

    state.CSPSet = [CSP:"72"]
    state.HSPSet = [HSP:"72"]// temporary values to prevent null error
    log.debug """
state.HSPSet = $state.HSPSet
state.CSPSet = $state.CSPSet"""

    state.doorsAreOpen = false
    state.messageOkToOpenCausesSent = 0
    def ContactsClosed = AllContactsAreClosed()
    //log.debug "enter updated, state: $state"  
    state.messageSent = 0
    state.locationModeChange = true 
    //log.debug "Number of Thermostats Selected by User : ${Thermostats.size()} [init]"
    runIn(30, resetLocationChangeVariable)
    // reset A.I. override maps
    state.HSPMap = [:]
    state.CSPMap = [:]
    // reset venting options
    state.coldbutneedcool = 0          
    state.ventingrun = 0

    MiscSubscriptions()
}
/// subscriptions
def MiscSubscriptions() {

    subscribe(Maincontacts, "contact.open", contactHandlerOpen)
    subscribe(Maincontacts, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(ContactException, "contact.open", contactExceptionHandlerOpen)
        subscribe(ContactException, "contact.closed", contactExceptionHandlerClosed)
    }

    if(BedSensor){
        subscribe(BedSensor, "contact.open", BedSensorHandler)
        subscribe(BedSensor, "contact.closed", BedSensorHandler)

    }

    subscribe(OutsideSensor, "temperature", temperatureHandler)
    def hasHumidity = OutsideSensor.hasAttribute("humidity")
    ////log.debug "hasHumidity is $hasHumidity"
    if(hasHumidity){
        subscribe(OutsideSensor, "humidity", HumidityHandler)

    }
    def hasFeelsLike = OutsideSensor.hasAttribute("feelsLike")
    if(hasFeelsLike){
        subscribe(OutsideSensor, "feelsLike", FeelsLikeHandler)

    }
    def hasWind = OutsideSensor.hasAttribute("wind")
    if(hasWind){
        subscribe(OutsideSensor, "wind", WindHandler)

    }

    ThermSubscriptions()
}
def ThermSubscriptions(){

    def loopV = 0
    def t = Thermostats.size()

    for(t > 0; loopV < t; loopV++){



        subscribe(Thermostats[loopV], "temperature", temperatureHandler)

        //log.debug "init therm ${Thermostats[loopV]} = loop $loopV"

        def hasHumidity = Thermostats[loopV].hasAttribute("humidity")

        if(hasHumidity){
            subscribe(Thermostats[loopV], "humidity", InsideHumidityHandler)
        }
    }


    AltSensorsSub()

}
def AltSensorsSub(){
    if(AltSensor){
        def ref = AltSensorsMaps()
        def AltSensorMap = ref[1]
        def AltSensorList = ref[0]
        // //log.debug "AltSensorMap = $AltSensorMap"

        def s = AltSensorList.size() 
        def loopV = 0

        for(s != 0; loopV < s; loopV++){

            //TheSensor = "${AltSensorList[loopV]}" // get the name
            //TheSensor = AltSensorMap."$TheSensor" // get the device

            def TheSensor = "Sensor${loopV}"
            TheSensor = settings.find{it.key == TheSensor}
            TheSensor = TheSensor?.value

            //TheSensor = TheSensor?.value
            //log.debug "TheSensor to subscribe is $TheSensor"
            subscribe(TheSensor, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor ${TheSensor} successful" // good

        }
    }

    schedules()

    if(useMotion){
        MotionSub()
    }
    else {
        //log.debug "END OF SUBSCRIPTIONS"

        Evaluate()
    }
}
def MotionSub(){

    def loopV = 0
    def ref = SetListsAndMaps()
    def MotionSensorList = ref[2]
    // //log.debug "MotionSensorList = $MotionSensorList"

    def ms = MotionSensorList.size()
    for(ms > 0; loopV < ms; loopV++){  

        def TheSensor = "MotionSensor${loopV}"
        TheSensor = settings.find{it.key == TheSensor}
        TheSensor = TheSensor?.value

        subscribe(TheSensor, "motion", motionSensorHandler)
        //log.debug "${TheSensor} subscribed to evt"  


        // these values will be used as defaultValue next time user goes to AI section so they don't need to reset them manually

        // state.MotionSensor << [TheSensor]

    }


    //log.debug "END OF SUBSCRIPTIONS"

    Evaluate()
}
////////////////////////////////////// END OF UPDATES AND SUBSCRIPTIONS ///////////////////////////////

def AltSensorsMaps(){
    def loopV = 0
    def s = ThermSensor.size()
    // //log.debug "ThermSensor.size() = ${s}"

    def AltSensorList = []
    def AltSensorMap = [:]
    def AltSensorBoolMap = [:]
    def refSensor = null
    def refTherm = null

    for(s != 0; loopV < s; loopV++){

        refSensor = "Sensor${loopV.toString()}"
        log.debug "refSensor String = $refSensor -- AltSensorsMaps"
        refSensor = settings.find{it.key == "$refSensor"}
        log.debug "refSensor Settings Map = $refSensor -- AltSensorsMaps"
        refSensor = refSensor?.value
        log.debug "refSensor value = $refSensor -- AltSensorsMaps"


        refTherm = ThermSensor[loopV]

        AltSensorList << ["$refSensor"] // can't be a device device warpper doesn't work with map and generates silent error and fucks everything up  
        AltSensorMap."$refTherm" = "$refSensor" // map for corresponding thermostat
        AltSensorBoolMap."$refTherm" = "true" // map for boolean values
    }
    def result = [AltSensorList, AltSensorMap, AltSensorBoolMap] 
    //// //log.debug "AltSensorsMaps returns: $result"

    return result
}
def SetListsAndMaps(){

    // reset lists and maps
    // these lists will allow to have sets of thermostats working with sets of motion sensors
    def thermMotionList = []              
    def MotionModesList = []
    def MotionSensorList = []

    // these maps' purpose is only to verify that all settings were properly arranged together 
    // and may be used as a debug ressource
    def MotionModesAndItsThermMap = [:]
    def SensorThermMap = [:]

    // recollect lists for motion sensors and associated thermostats
    def loopV = 0
    def s = Thermostats.size()
    def reftm = ""
    def refmmodes = ""
    def refms = ""

    for(s > 0; loopV < s; loopV++){

        reftm = "thermMotion${loopV.toString()}"
        refmmodes = "MotionModes${loopV.toString()}"
        refms = "MotionSensor${loopV.toString()}"  

        reftm = settings.find{it.key == "$reftm"}
        refmmodes = settings.find{it.key == "$refmmodes"}
        refms = settings.find{it.key == "$refms"}

        reftm = reftm?.value
        refmmodes = refmmodes?.value
        refms = refms?.value

        /*  log.debug """
reftm = $reftm
refmmodes = $refmmodes
refms = $refms
"""
*/

        thermMotionList << ["$reftm"]
        MotionModesList << ["$refmmodes"]
        MotionSensorList << ["$refms"] // we need a list of devices, not just names... 
        MotionModesAndItsThermMap << ["$reftm" : "$refmmodes"]
        SensorThermMap << ["$reftm" : "$refms"]


    }
    // recorded as future defaultValue in AI
    //state.MotionModesAndItsThermMap = MotionModesAndItsThermMap

    def results = [thermMotionList, MotionModesList, MotionSensorList, MotionModesAndItsThermMap, SensorThermMap]
    //log.debug """SetListsAndMaps returns: $results"""
    return results
}

////////////////////////////////////// VIRTUAL THERMOSTAT ///////////////////////////////
def ActiveTest(ThermSet) {
    def Active = true 
    def inMotionModes = false
    def TheSensor = null

    log.debug "useMotion: $useMotion"

    if(useMotion){
        def refm = SetListsAndMaps()
        def thermMotionList = refm[0]
        def MotionModesList = refm[1]
        def MotionSensorList = refm[2]
        def MotionModesAndItsThermMap = refm[3]
        def SensorThermMap = refm[4]

        def CurrMode = location.currentMode

        TheSensor = SensorThermMap.find{it.key == "${ThermSet}"}
        TheSensor = TheSensor?.value

        def MotionModes = MotionModesAndItsThermMap.find{it.key == "${ThermSet}"}
        //log.debug "MotionModes before value called: $MotionModes"
        MotionModes = MotionModes?.value

        inMotionModes = MotionModes.find("$CurrMode") == "$CurrMode"
        //log.debug "inMotionModes after value called: $inMotionModes"

        def ActiveMap = MotionTest() 
        def ActiveFind = ActiveMap.find{it.key == "${TheSensor}"}

        Active = ActiveFind?.value
        Active = "$Active" == "true"

        log.trace """
CurrMode is $CurrMode
Current Motion Sensor = $TheSensor
Current MotionModes = $MotionModes, 
$ThermSet is inMotionModes?($inMotionModes),
MotionModesAndItsThermMap = $MotionModesAndItsThermMap
useMotion?($useMotion)
ActiveMap = $ActiveMap
SensorThermMap = $SensorThermMap
MotionSensorList = $MotionSensorList
Active?(from List) for $ThermSet && $TheSensor = $Active
"""
    }

    log.debug "boolean motion test for $ThermSet returns $Active || inMotionModes = $inMotionModes"

    return [Active, inMotionModes, TheSensor]
}

def VirtualThermostat(){
    log.info "virtual thermostat"
    def Active = null
    def ContactsAreClosed = AllContactsAreClosed()
    // critical temp safety check 

    def tempcheck1 = null
    def tempcheck2 = null
    def tempcheck3 = null
    if(VirThermSensor){
        tempcheck1 = VirThermSensor.currentValue("temperature") 
    }
    else {
        tempcheck1 = VirThermTherm_1?.currentValue("temperature") 
    }

    if(VirThermSensor_2){
        tempcheck2 = VirThermSensor_2.currentValue("temperature") 
    }
    else {
        tempcheck3 = VirThermTherm_2?.currentValue("temperature") 
    }

    if(VirThermSensor_3){
        tempcheck3 = VirThermSensor_3.currentValue("temperature") 
    }
    else {
        tempcheck3 = VirThermTherm_3?.currentValue("temperature") 
    }
    def tempcheckList = [tempcheck1, tempcheck2, tempcheck3]
    def VTSwList = [VirThermSwitch_1, VirThermSwitch_2, VirThermSwitch_3]
    def CriticalTemp = 65
    def InMotionMode = true

    // critical temp will work only if at least on the VT is a heater
    def coolOrHeat = [coolOrHeat, coolOrHeat_2, coolOrHeat_3]
    def ThisIsHeating = coolOrHeat.find{it == "heating"} == "heating"

    // if any of these values is lower than critical temp, then we have a situation
    def Critical = tempcheckList.findAll{it < CriticalTemp}.size != 0 && ThisIsHeating

    log.debug "GLOBAL CRITICAL TEST ThisIsHeating : $ThisIsHeating && Critical = $Critical | $VTSwList | $tempcheckList" 
    // this potentially overrides all other VT features such as VT location modes 
    if(!AllContactsAreClosed() || Critical) {
        log.debug "Turning off VT ?"

        def i = 0
        def s = VTSwList.size()
        def thisSwt = null

        for(s > 0; i < s; i++){
            thisSwt = VTSwList[i]
            // allow now to turn on a heater only where the situation originates from
            Critical = tempcheckList[i] < CriticalTemp
            if(Critical){
                log.trace "VT Critical loop $i"
                if(thisSwt?.currentSwitch != "on"){
                    thisSwt?.on()         
                    def message = "CRITICAL TEMPERATURE (${tempcheckList[i]}F) AT VIRTHERM Turning on $thisSwt"
                    log.info message
                    def AlreadySent = state.CriticalMessageSent[i]
                    if(!AlreadySent){
                        send(message)       
                        state.CriticalMessageSent[i] = true
                    }
                    else {
                        log.debug "message already sent"
                    }
                }
            }
            else {
                state.CriticalMessageSent[i] = false
                //shutdownVT() // shut down all virtual thermostats
                if(thisSwt?.currentSwitch != "off"){
                    thisSwt?.off() 
                }
                log.debug "Some contacts are open, turning off $thisSwt"
            }
        }
        log.debug "state.CriticalMessageSent = $state.CriticalMessageSent"
    }

    Critical = tempcheckList[0] < CriticalTemp //avoid repeated on/off's if already managed by critical
    if(ContactsAreClosed && !Critical){

        if(!OtherSetP){
            // this option is not compatible with motion because motion sensor 
            // is designated by the thermostat used as SP source. 
            def ActiveT = ActiveTest(VirThermTherm_1)
            InMotionMode = ActiveT[1]
            Active = ActiveT[0]
        }
        else {
            Active = true
        }

        log.trace "$VirThermSwitch_1 Active? : $Active"
        def no = ""
        if(!Active){no = "no"}else{no = ""}
        log.debug "There's $no motion near $VirThermSwitch_1 --"

        def CurrSP = null
        log.debug "OtherSetP = $OtherSetP"
        if(coolOrHeat == "cooling"){
            if(OtherSetP){
                CurrSP = CSPVir
            }
            else {
                CurrSP = VirThermTherm_1.currentValue("coolingSetpoint")
            }
        }
        else {
            // heating
            if(OtherSetP){
                CurrSP = HSPVir
            }
            else {
                CurrSP = VirThermTherm_1.currentValue("heatingSetpoint")
            }
        }

        def CurrTemp = tempcheckList[0]

        def SwitchState = VirThermSwitch_1?.currentSwitch

        def inVirThermModes = false 

        if(VirThermModes){
            inVirThermModes = location.currentMode in VirThermModes
            log.debug " inVirThermModes set to $inVirThermModes"
        }

        log.debug """Values @ virtual thermostat for $VirThermSwitch_1 are: 
---------------------------- 
CurrTemp = $CurrTemp
CurrSP  = $CurrSP
Active ? : $Active
InMotionMode ? : $InMotionMode
Mode coolOrHeat = $coolOrHeat
Switch State = $SwitchState
inVirThermModes = $inVirThermModes

"""
        if(Active || !InMotionMode){
            if(coolOrHeat == "cooling"){
                if(CurrTemp > CurrSP && inVirThermModes){
                    if(SwitchState != "on" ){
                        VirThermSwitch_1?.on()
                        log.debug "$VirThermSwitch_1 [cool] turned on"
                    }
                    else {
                        log.debug "$VirThermSwitch_1 [cool] ALREADY turned on"
                    }       
                }
                else {
                    if(SwitchState != "off"){
                        VirThermSwitch_1?.off()
                        log.debug "$VirThermSwitch_1 [cool] turned off"
                    }
                    else {
                        log.debug "$VirThermSwitch_1 [cool] ALREADY turned OFF"

                    }
                }
            }
            else {
                if(CurrTemp < CurrSP && inVirThermModes){
                    if(SwitchState != "on"){
                        VirThermSwitch_1?.on()
                        log.debug "$VirThermSwitch_1 [heat] turned on"
                    }
                    else {
                        log.debug "$VirThermSwitch_1 [heat] ALREADY turned on"
                    }
                }
                else {
                    if(SwitchState != "off"){
                        VirThermSwitch_1?.off()
                        log.debug "$VirThermSwitch_1 [heat] turned off"
                    }
                    else {
                        log.debug "$VirThermSwitch_1 [heat] ALREADY turned OFF"

                    }
                }
            }
        }
        if(!inVirThermModes && SwitchState != "off"){
            VirThermSwitch_1?.off()
            log.debug "$VirThermSwitch_1 [heat] turned off because location is not in $VirThermModes"
        }
    }
    // for cases when it remained on while mode changed
    if(!InMotionMode && !Critical){
        VirThermSwitch_1?.off()
        log.debug "$VirThermSwitch_1 turned off due to recent mode change"
    }


    if(AddMoreVirT_B){
        Critical = tempcheckList[1] < CriticalTemp //avoid repeated on/off's if already managed by critical
        if(ContactsAreClosed && !Critical){

            if(!OtherSetP_2){
                // this option is not compatible with motion because motion sensor 
                // is designated by the thermostat used as SP source. 
                def ActiveT = ActiveTest(VirThermTherm_2)
                InMotionMode = ActiveT[1]
                Active = ActiveT[0]
            }
            else {
                Active = true
            }
            log.trace "$VirThermSwitch_2 Active? : $Active && ActiveT = $ActiveT"
            def no = ""
            if(!Active){no = "no"}else{no = ""}
            log.debug "There's $no motion near $VirThermSwitch_2 --"

            def CurrSP = null
            log.debug "OtherSetP_2 = $OtherSetP_2"
            if(coolOrHeat_2 == "cooling"){
                if(OtherSetP_2){
                    CurrSP = CSPVir_2
                }
                else {
                    CurrSP = VirThermTherm_2.currentValue("coolingSetpoint")
                }
            }
            else {
                // heating
                if(OtherSetP_2){
                    CurrSP = HSPVir_2
                }
                else {
                    CurrSP = VirThermTherm_2.currentValue("heatingSetpoint")
                }
            }

            def CurrTemp = tempcheckList[1]
            def SwitchState_2 = VirThermSwitch_2?.currentSwitch

            def inVirThermModes_2 = false 
            if(VirThermModes_2){
                inVirThermModes_2 = location.currentMode in VirThermModes_2
                log.debug " inVirThermModes set to $inVirThermModes_2"
            }

            log.debug """ Values @ virtual thermostat for $VirThermSwitch_2 are: 
---------------------------- 
CurrTemp_2 = $CurrTemp
CurrSP 2 = $CurrSP
Active ? : $Active
InMotionMode ? : $InMotionMode
Mode coolOrHeat_2 = $coolOrHeat_2
Switch State = $SwitchState_2
inVirThermModes_2 = $inVirThermModes_2
----------------------------    
"""
            if(Active || !InMotionMode){
                if(coolOrHeat_2 == "cooling"){
                    if(CurrTemp > CurrSP && inVirThermModes_2){
                        if(SwitchState_2 != "on" ){
                            VirThermSwitch_2?.on()
                            log.debug "$VirThermSwitch_2 [cool] turned on"
                        }
                        else {
                            log.debug "$VirThermSwitch_2 [cool] ALREADY turned on"
                        }       
                    }
                    else {
                        if(SwitchState_2 != "off"){
                            VirThermSwitch_2?.off()
                            log.debug "$VirThermSwitch_2 [cool] turned off"
                        }
                        else {
                            log.debug "$VirThermSwitch_2 [cool] ALREADY turned OFF"
                        }
                    }
                }
                else {
                    if(CurrTemp < CurrSP && inVirThermModes_2){
                        if(SwitchState_2 != "on"){
                            VirThermSwitch_2?.on()
                            log.debug "$VirThermSwitch_2 [cool] turned on"
                        }
                        else {
                            log.debug "$VirThermSwitch_2 [cool] ALREADY turned on"
                        }
                    }
                    else {
                        if(SwitchState_2 != "off"){
                            VirThermSwitch_2?.off()
                            log.debug "$VirThermSwitch_2 [heat] turned off"
                        }
                        else {
                            log.debug "$VirThermSwitch_2 [heat] ALREADY turned OFF"
                        }
                    }
                }
            }
            else { 
                log.debug "No motion arround $VirThermSwitch_2"
            }
            if(!inVirThermModes_2 && SwitchState_2 != "off"){
                VirThermSwitch_2?.off()
                log.debug "$VirThermSwitch_2 [heat] turned off because location is not in $VirThermModes_2"
            }
        }
        if(!InMotionMode && !Critical){
            VirThermSwitch_2?.off()
            log.debug "$VirThermSwitch_2 turned off due to recent mode change"
        }
    }

    if(AddMoreVirT_C){
        Critical = tempcheckList[2] < CriticalTemp //avoid repeated on/off's if already managed by critical
        if(ContactsAreClosed && !Critical){

            if(!OtherSetP_3){
                // this option is not compatible with motion because motion sensor 
                // is designated by the thermostat used as SP source. 
                def ActiveT = ActiveTest(VirThermTherm_3)
                InMotionMode = ActiveT[1]
                Active = ActiveT[0]
            }
            else {
                Active = true
            }

            log.trace "$VirThermSwitch_3 Active? : $Active && ActiveT = $ActiveT"

            def no = ""
            if(!Active){no = "no"}else{no = ""}
            log.debug "There's $no motion near $VirThermSwitch_3 --"

            def CurrSP = null
            log.debug "OtherSetP_3 = $OtherSetP_3"
            if(coolOrHeat_3 == "cooling"){
                if(OtherSetP_3){
                    CurrSP = CSPVir_3
                }
                else {
                    CurrSP = VirThermTherm_3.currentValue("coolingSetpoint")
                }
            }
            else {
                // heating
                if(OtherSetP_3){
                    CurrSP = HSPVir_3
                }
                else {
                    CurrSP = VirThermTherm_3.currentValue("heatingSetpoint")
                }
            }

            def CurrTemp = tempcheckList[2]
            def SwitchState_3 = VirThermSwitch_3?.currentSwitch

            def inVirThermModes_3 = false 
            if(VirThermModes_3){
                inVirThermModes_3 = location.currentMode in VirThermModes_3
                log.debug " inVirThermModes set to $inVirThermModes_3"
            }

            log.debug """ Values @ virtual thermostat for $VirThermSwitch_3 are: 
---------------------------- 
CurrTemp_3 = $CurrTemp
CurrSP 3 = $CurrSP
$CurrSP > $CurrTemp ? (${CurrSP > CurrTemp})
$CurrSP < $CurrTemp ? (${CurrSP < CurrTemp})
Active ? : $Active
InMotionMode ? : $InMotionMode
Mode coolOrHeat = $coolOrHeat_3
Switch State = $SwitchState_3
inVirThermModes_3 = $inVirThermModes_3
----------------------------    
"""

            if(Active || !InMotionMode){
                if(coolOrHeat_3 == "cooling"){
                    if(CurrTemp > CurrSP && inVirThermModes_3){
                        if(SwitchState_3 != "on" ){
                            VirThermSwitch_3?.on()
                            log.debug "$VirThermSwitch_3 [cool] turned on"
                        }
                        else {
                            log.debug "$VirThermSwitch_3 [cool] ALREADY turned on"
                        }       
                    }
                    else {
                        if(SwitchState_3 != "off"){
                            VirThermSwitch_3?.off()
                            log.debug "$VirThermSwitch_3 [cool] turned off"
                        }
                        else {
                            log.debug "$VirThermSwitch_3 [cool] ALREADY turned OFF"

                        }
                    }
                }
                else {
                    if(CurrTemp < CurrSP && inVirThermModes_3){
                        if(SwitchState_3 != "on"){
                            VirThermSwitch_3?.on()
                            log.debug "$VirThermSwitch_3 [heat] turned on"
                        }
                        else {
                            log.debug "$VirThermSwitch_3 [heat] ALREADY turned on"
                        }
                    }
                    else {
                        if(SwitchState_3 != "off"){
                            VirThermSwitch_3?.off()
                            log.debug "$VirThermSwitch_3 [heat] turned off"
                        }
                        else {
                            log.debug "$VirThermSwitch_3 [heat] ALREADY turned OFF"

                        }
                    }
                }
            }
            else { 
                log.debug "No motion arround $VirThermSwitch_3"
            }
            if(!inVirThermModes_3 && SwitchState_3 != "off"){
                VirThermSwitch_3?.off()
                log.debug "$VirThermSwitch_3 [heat] turned off because location is not in $VirThermModes_3"
            }
        }
        if(!InMotionMode && !Critical){
            VirThermSwitch_3?.off()
            log.debug "$VirThermSwitch_3 turned off due to recent mode change"
        }
    }

    CheckWindows()
}

////////////////////////////////////// MAIN EVALUATION ///////////////////////////////
def Evaluate(){

    log.trace "EVALUATE()"

    def BedSensorResults = BedSensorStatus()
    def NowBedisClosed = BedSensorResults[0]
    def NowBedisOpen = BedSensorResults[1]
    def CurrMode = location.currentMode
    def inAway = CurrMode in Away
    log.debug "Location is in $CurrMode mode"

    def outsideTemp = OutsideSensor?.currentValue("temperature") //as double
    //outsideTemp = Double.parseDouble(outsideTemp)
    outsideTemp = outsideTemp.toInteger()
    def Outside = outsideTemp as int

        def doorsOk = AllContactsAreClosed()
        def ContactExceptionIsClosed = ExcepContactsClosed()
        //log.debug "doorsOk?($doorsOk), ContactExceptionIsClosed?($ContactExceptionIsClosed)"

    def CurrentSwitch = ContactAndSwitch?.currentSwitch
    def SomeSwAreOff = CurrentSwitch.findAll { switchVal ->
        switchVal == "off" ? true : false
    }
    def SomeSwAreOn = CurrentSwitch.findAll { switchVal ->
        switchVal == "on" ? true : false
    }

    def contactClosed = false
    def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
    log.debug """
SomeSwAreOn.size() = ${SomeSwAreOn.size()}
SomeSwAreOff.size() = ${SomeSwAreOff.size()}
CurrMode in SwitchMode = ${CurrMode in SwitchMode}
ToggleBack = $ToggleBack
doorsOk = $doorsOk
state.turnedOffByApp = $state.turnedOffByApp
FollowException = $FollowException 
InExceptionContactMode = $InExceptionContactMode 
DoNotTurnOffModes = $DoNotTurnOffModes
ContactExceptionIsClosed = $ContactExceptionIsClosed
NowBedisClosed = $NowBedisClosed"""

    if(ContactException && FollowException && InExceptionContactMode){
        contactClosed = ContactExceptionIsClosed
        //log.debug "contactClosed = $contactClosed (Contact Exception only)"
    }
    else{
        contactClosed = AllContactsAreClosed()
        //log.debug "contactClosed = $contactClosed (ALL CONTACTS)"
    }
    //log.debug "contactClosed = $contactClosed"
    def inAwayMode = CurrMode in Away

    if(IsHeatPump && outsideTemp <= 29){
        if(SomeSwAreOn.size() != 0){
            ContactAndSwitch?.off()
            log.debug "$ContactAndSwitch TURNED OFF because it's a heat pump and outside temp is too low (${outsideTemp} <= 29)"
            state.turnedOffByApp = true
        }
    }
    else if(!KeepOffAtAllTimesWhenMode() && contactClosed && ToggleBack || (ControlWithBedSensor && NowBedisClosed && ContactExceptionIsClosed)){  //  && state.turnedOffByApp == true){  
        if(SomeSwAreOff.size() != 0){
        //if at least one is off, turn on
                ContactAndSwitch?.on()
                log.debug "$ContactAndSwitch TURNED ON"
                state.turnedOffByApp = false
        }
        else {
            log.debug "$ContactAndSwitch already on"
        }
    }
    else if(KeepOffAtAllTimesWhenMode() && CurrMode in SwitchMode){
        if(SomeSwAreOn.size() != 0){
        // if at least one is on, turn off
            ContactAndSwitch?.off()
            log.debug "$ContactAndSwitch TURNED OFF"
            state.turnedOffByApp = true
        }
        else {
            log.debug "$ContactAndSwitch already off --"
        }        
    }
    else if(!contactClosed || inAwayMode){
        if(SomeSwAreOn.size() != 0){
        // if at least one is on, turn off
            ContactAndSwitch.off()
            log.debug "$ContactAndSwitch turned off because window is open"
        }
        else {
            log.debug "$ContactAndSwitch already off --"
        }      

    }


    if(doorsOk || ContactExceptionIsClosed ){

        def inCtrlSwtchMode = CurrMode in CtrlSwtModes
        def CtrlSwtState = CtrlSwt?.currentSwitch
        def SwitchesOn = CtrlSwtState == "on"

        if(!inCtrlSwtchMode || !ContactExceptionIsClosed){
            SwitchesOn = false
        }
        log.debug """inCtrlSwtchMode($inCtrlSwtchMode) && CtrlSwtState = $CtrlSwtState && SwitchesOn($SwitchesOn) && 
SwitchesOnTest = $SwitchesOnTest && ContactExceptionIsClosed = $ContactExceptionIsClosed"""

        def HSPSet = 70
        def CSPSet = 70
        def LatestThermostatMode = null

        def ThermDeviceList = Thermostats
        def OutsideTempHighThres = ExceptACModes()

        def CurrTempDevice = 0

        def loopValue = 0
        def t = Thermostats.size()

        ///// MAIN LOOP
        for(t > 0; loopValue < t; loopValue++){

            log.debug "FOR LOOP $loopValue"

            def ThermSet = Thermostats[loopValue]
            def ActiveT = ActiveTest(ThermSet)
            def Active = ActiveT[0] 
            def inMotionModes = ActiveT[1]
            log.trace "inMotionModes = $inMotionModes ---------Active = $Active--------------"
            def MotionSensor = ActiveT[2]
            def ref = AltSensorsMaps()
            def AltSensorList = ref[0]
            def AltSensorMap = ref[1]
            def AltSensorBoolMap = ref[2]

            def CurrTemp = 72 // default value before requests

            def useAltSensor = AltSensorBoolMap."$ThermSet" == "true"

            log.debug """
useAltSensor for $ThermSet = $useAltSensor
AltSensorMap = $AltSensorMap"""

            ///// OVERRIDE TEST///// 
            def ThermState = ThermSet.currentValue("thermostatMode")  
            // override is activated when thermostat is in auto mode
            def AppMgt = ThermState != "auto"
            log.debug "NO OVERRIDE FOR $ThermSet = $AppMgt (ThermState == $ThermState)"
            ///////////////// END OF OVERRIDE TEST //////////////////

            if(AppMgt){

                if(useAltSensor){

                    def TheSensor = AltSensorMap.find{it.key == "$ThermSet"}
                    TheSensor = TheSensor?.value
                    log.info "TheSensor String = $TheSensor"	
                    def refSensor = null
                    /// retrieve corresponding device object
                    def c = 0
                    while("$refSensor" != "$TheSensor" /*&& c < 10*/){
                        refSensor = "Sensor${c.toString()}"
                        log.debug "refSensor String = $refSensor"
                        refSensor = settings.find{it.key == "$refSensor"}
                        refSensor = refSensor?.value
                        log.debug "refSensor Value = $refSensor"
                        c++
                            }

                    TheSensor = refSensor

                    log.info "TheSensor = $TheSensor"	                      

                    //log.info "------------------------------------TheAltSensor = $TheAltSensor"

                    CurrTemp = TheSensor?.currentValue("temperature")
                    log.debug "$TheSensor selected as CurrTemp source for $ThermSet and it returns ${CurrTemp}F "                   
                }
                else {
                    CurrTemp = ThermSet.currentValue("temperature") 
                }

                def ModeValueList = IndexValueMode()
                def ModeValue = ModeValueList[0]
                def Test = ModeValueList[1]
                //log.debug "@ loop ${loopValue}, ModeValue is $ModeValue &&& Test = $Test"
                log.debug "inAway = $inAway"
                if(inAway){
                    HSPSet = HSPA

                    CSPSet = CSPA
                    log.debug """

HSPSet for $ThermSet is $HSPSet
CSPSet for $ThermSet is $CSPSet

"""
                } 
                else {
                    def HSP = "HSP${ModeValue}${loopValue.toString()}"
                    //log.debug """HSP is $HSP """

                    //log.trace"SETTINGS: $settings"

                    HSPSet = settings.find{it.key == "$HSP"} // retrieve the String from settings
                    log.debug """HSPSet is $HSPSet (before collection) loop = $loopValue"""
                    HSPSet = HSPSet?.value
                    log.debug "HSPSet for $ThermSet is $HSPSet "
                    HSPSet = HSPSet.toInteger()

                    def CSP = "CSP${ModeValue}${loopValue.toString()}"
                    //log.debug """CSP is $CSP """

                    CSPSet = settings.find {it.key == "$CSP"} // retrieve from settings
                    log.debug """CSPSet is $CSPSet (before collection) loop = $loopValue"""
                    CSPSet = CSPSet?.value
                    //log.debug "CSPSet for $ThermSet is $CSPSet"
                    CSPSet = CSPSet.toInteger()
                }
                // end of collection for modes other than Away

                def Inside = ThermSet.currentValue("temperature")
                //Inside = Double.parseDouble(Inside)
                //Inside = Inside.toInteger()

                state.Inside = Inside
                def humidity = OutsideSensor.currentValue("temperature")
                def TooHumid = humidity > HumidityTolerance && Outside > CSPSet 

                def INSIDEhumidity = ThermSet.latestValue("humidity")   
                def TooHumidINSIDE =  INSIDEhumidity > 60 // 60% is the known healthy limit for a house

                log.trace """        
ThermsInvolved = ${Thermostats.size()} 
loop($loopValue) 
AppMgt = $AppMgt
INSIDEhumidity = $INSIDEhumidity
TooHumidINSIDE = $TooHumidINSIDE
outside humidity = $humidity
outside too humid: $TooHumid
inside humidity tolerance is 60 (built-in value, not modifiable by user)

"""        
                state.ThermSet = []
                state.ThermSet << "$ThermSet"

                state.EventAtTempLoop = ThermSet as String // used for false override prevention
                //log.debug "ThermSet = $ThermSet - state.EventAtTempLoop = $state.EventAtTempLoop"

                //CurrTemp = Double.parseDouble(CurrTemp)
                //CurrTemp = CurrTemp.toInteger()

                //log.debug "CurrTemp = $CurrTemp ($ThermSet)"
                state.CurrTemp = CurrTemp


                def defaultCSPSet = CSPSet // recording this default value so if A.I. brings setpoint too low, it'll be recovered
                def defaultHSPSet = HSPSet // same but with heat

                log.debug """
Current Temperature Inside = $Inside
defaultHSPSet = $defaultHSPSet
defaultCSPSet = $defaultCSPSet
inAway = $inAway
"""

                /// ALGEBRA

                def xa = 0
                def ya = 0

                def xb = 0
                def yb = 0
                def b = 0
                def coef = 0


                if(adjustments == "Yes, use a linear variation" && !inAway){
                    /////////////////////////COOL////////////////////  linear function for Cooling
                    xa = 75	//outside temp a
                    ya = CSPSet // desired cooling temp a 

                    xb = 100 		//outside temp b
                    yb = CSPSet + 5  // desired cooling temp b  

                    // take humidity into account
                    // if outside humidity is higher than .... 
                    if(TooHumid){
                        xa = 75				//outside temp a LESS VARIATION WHEN HUMID
                        ya = CSPSet	   // desired cooling temp a 
                        xb = 100 //outside temp b
                        yb = CSPSet + 2 // desired cooling temp b  LESS VARIATION WHEN HUMID
                    }

                    coef = (yb-ya)/(xb-xa)

                    b = ya - coef * xa // solution to ya = coef*xa + b // CSPSet = coef*outsideTemp + b

                    CSPSet = coef*outsideTemp + b as double
                        log.info "b is: $b ---------------------------------------"
                    //

                } 

                else if(adjustments == "Yes, but use a logarithmic variation" && !inAway){
                    // logarithmic treatment 

                    /* concept: x = log(72)75   to what power (that is to say "x") do I have to raise 72, to get to 75?

logb(n) = loge(n) / loge(b)
Where log can be a logarithm function in any base, n is the number and b is the base. For example, in Java this will find the base-2 logarithm of 256:

Math.log(256) / Math.log(2)
=> 8.0
*/
                    // log base is: CSPSet
                    def Base = CSPSet.toInteger()
                    /////////////////////////COOL//////////////////// 
                    //outsideTemp = 90 // for test only 
                    CSPSet = (Math.log(outsideTemp) / Math.log(Base)) * CSPSet
                    //log.debug "Logarithmic CSPSet = $CSPSet"
                    //CSPSet = Math.round(CSPSet)
                    //CSPSet = CSPSet.toInteger()
                    //log.debug "Integer CSPSet = $CSPSet"


                    /////////////////////////HEAT//////////////////// ALWAYS linear function for heating... for now... 

                    xa = 60	//outside temp a
                    ya = HSPSet.toInteger() // desired heating temp a 

                    xb = outsideTemp 	//outside temp b
                    yb = HSPSet.toInteger() + 10  // desired heating temp b  

                    coef = (yb-ya)/(xb-xa)
                    b = ya - coef * xa // solution to ya = coef*xa + b // HSPSet = coef*outsideTemp + b

                    log.info "b is: $b ---------------------------------------"
                    HSPSet = coef*outsideTemp + b 
                    HSPSet = HSPSet.toInteger()

                    log.debug "linear HSPSet for $ThermSet = $HSPSet && MaxLinearHeat = $MaxLinearHeat"

                    if(HSPSet > MaxLinearHeat){
                        HSPSet = MaxLinearHeat
                        def message = "$ThermSet heating set point is too high, brought back to MaxLinearHeat: ${MaxLinearHeat}F"
                        log.info message
                        if(state.sendalert != 1){
                            send(message)
                            state.sendalert = 1
                        }
                    }


                    //log.debug "end of algebra" 

                    ///////////////////humidity and thresholds///////////////////

                    if(TooHumid && Inside - 2 >= outsideTemp && Active){
                        CSPSet = CSPSet - 1 
                        //log.debug "Substracting 2 to new CSP because it is too humid OUTSIDE"
                    }
                    else {
                        //log.debug "not too humid outside"
                    }

                    if(TooHumidINSIDE && Inside - 2 >= outsideTemp && Active){
                        CSPSet = CSPSet - 1 
                        //log.debug "Substracting 1 to new CSP because it is too humid INSIDE"
                    }
                    else {
                        //log.debug "not too humid inside"
                    }


                }

                ///////////////////////////////////////////////////////// motion management/////////////////////////////////////////////////////////
                log.debug "----------------------------------------------------------motion management---------------------------------------------------"
                if(useMotion && !inAway){

                    def HeatNoMotionVal = HeatNoMotion
                    def CoolNoMotionVal = CoolNoMotion

                    log.debug "inMotionModes= $inMotionModes AppMgt = $AppMgt CoolNoMotionVal = $CoolNoMotionVal HeatNoMotionVal = $HeatNoMotionVal"

                    if(inMotionModes && AppMgt){

                        if(!Active){
                            //log.debug "TEST3"
                            // record algebraic CSPSet for debug purpose
                            def algebraicCSPSet = CSPSet.toInteger()
                            def algebraicHSPSet = HSPSet.toInteger()
                            state.algebraicCSPSet = [CSP: CSPSet] // record for venting and windows criteria
                            state.algebraicHSPSet = [HSP: HSPSet]  // record for venting and windows criteria
                            log.info "$ThermSet default Cool: $CSPSet and default heat: $HSPSet "
                            CSPSet = CSPSet + CoolNoMotionVal  
                            HSPSet = HSPSet - HeatNoMotionVal

                            log.trace """
NO MOTION so $ThermSet CSP, which was $defaultCSPSet, then (if algebra) $algebraicCSPSet, is now set to $CSPSet
NO MOTION so $ThermSet HSP, which was $defaultHSPSet, then (if algebra) $algebraicHSPSet, is now set to $HSPSet
"""

                        }
                        else {

                            log.debug "There's motion in ${ThermSet}'s room (main loop)"          
                        }
                    }
                }

                if(!useMotion || (Active && inMotionModes) && !inAway){

                    // no lower than defaultCSPSet 
                    //log.debug "Calculated CSPSet = $CSPSet, defaultCSPSet = $defaultCSPSet (loop $loopValue)"
                    if(CSPSet < (defaultCSPSet)){

                        log.info """CurrTemp at ${ThermSet} is: $CurrTemp. CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp
But, because CSPSet is too much lower than default value ($defaultCSPSet), default settings are maintained"""
                        CSPSet = defaultCSPSet
                    }
                    else {

                        log.info "CurrTemp at ${ThermSet} is: $CurrTemp CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"
                        // hspset threshold managed by linears
                    }
                    log.info "CurrTemp at ${ThermSet} is: $CurrTemp HSPSet was $defaultHSPSet. It is NOW $HSPSet due to outside's temperature being $outsideTemp"

                }

                /////////////////////////////////////////////////////////END OF SETPOINTS EVALS/////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////EVAL OF NEEDS ////////////////////////////////////////////////////////////////
                def WarmOutside = outsideTemp >= (CSPSet - 1)
                def WarmInside = (CurrTemp >= CSPSet + 1 && WarmOutside) || (CurrTemp >= CSPSet + 1 && TooHumidINSIDE && Active)
                //log.debug "CurrTemp = $CurrTemp, outsideTemp = $outsideTemp, CSPSet = $CSPSet, WarmOutside = $WarmOutside, WarmInside = $WarmInside"

                def ShouldCoolWithAC = WarmOutside
                state.ShouldCoolWithAC = ShouldCoolWithAC // will be used by venting option

                //log.debug "$ThermSet ShouldCoolWithAC = $ShouldCoolWithAC (before other criteria loop $loopValue)"

                def ShouldHeat = !WarmOutside && !WarmInside

                if((WarmInside && TooHumidINSIDE) && !ShouldHeat && Active){
                    ShouldCoolWithAC = true
                    //log.debug "ShouldCoolWithAC set to true loop $loopValue due to humidity levels inside the place"
                }
                state.ShouldCoolWithAC = ShouldCoolWithAC

                state.ShouldHeat = ShouldHeat

                log.debug """
ShouldCoolWithAC = $ShouldCoolWithAC 
ShouldHeat = $ShouldHeat 
state.ShouldHeat = $state.ShouldHeat
WarmOutside = $WarmOutside 
WarmInside = $WarmInside
OutsideTempLowThres = $OutsideTempLowThres
TooHumidINSIDE = $TooHumidINSIDE

"""       

                def ThisIsExceptionTherm = ThermSet.displayName in NoTurnOffOnContact  

                if(ExceptionSW && "$ThermSet" == "$ExceptionSwTherm" && SwitchesOn && !inAway){
                    if(warmerorcooler == "warmer"){
                        HSPSet = HSPSet + AddDegrees
                        CSPSet = CSPSet + SubDegrees
                    } 
                    else {
                        HSPSet = HSPSet - AddDegrees
                        CSPSet = CSPSet - SubDegrees
                    }

                    log.debug "$ThermSet SetPoints ExceptionSW active"                                
                }

                // Now, before sending any command, pull current setpoint and compare to avoid redundencies
                def CurrentCoolingSetPoint = ThermSet.currentValue("coolingSetpoint") 
                def CurrentHeatingSetPoint = ThermSet.currentValue("heatingSetpoint") 

                //// bedsensor/// 
                def CSPok = CurrentCoolingSetPoint == CSPSet
                def HSPok = CurrentHeatingSetPoint == HSPSet

                def BedSensorManagement = false
                //log.debug "BedSensorManagement defaulted to false (BedSensorManagement = $BedSensorManagement)"


                def CurrentContactAndSwitch = ContactAndSwitch.currentSwitch
                //log.debug "$ContactAndSwitch currentSwitch = $currentSwitch"
                def ContactAndSwitchState = CurrentContactAndSwitch.findAll { switchVal ->
                    switchVal == "on" ? true : false
                }

                log.debug """
NowBedisClosed = $NowBedisClosed, 
NowBedisOpen = $NowBedisOpen, """

                if(ContactAndSwitchInSameRoom && UnitToIgnore?.displayName == "${ThermContact}" && ThermSet.displayName == "${ThermContact}" && ContactAndSwitchState?.size() > O){
                    log.debug "not applying $BedSensor action because it is in the same room as $ContactAndSwitch, which is currently ON"
                }
                else if(KeepACon && ContactExceptionIsClosed && !inAway){

                    if("${ThermSet}" == "${ThermContact}" && NowBedisClosed ){ 
                        log.debug "BedSensorManagement set to true (BedSensorManagement = $BedSensorManagement)"
                        BedSensorManagement = true 


                        //log.debug "$BedSensor closed, applying settings accordingly"  
                        def CSPSetBedSensor = CSPSetBedSensor.toInteger()
                        def HSPSetBedSensor = HSPSetBedSensor.toInteger()
                        //log.debug "Integer HSPSetBedSensor = $HSPSetBedSensor"

                        def needCool = ShouldCoolWithAC
                        def needHeat = ShouldHeat
                        if(needHeat){
                            needCool = false 
                        }


                        if(needCool){
                            if(!CurrentCoolingSetPoint != CSPSetBedSensor){
                                ThermSet.setCoolingSetpoint(CSPSetBedSensor)
                                log.debug "$ThermSet CSP set to $CSPSetBedSensor -- Bed Sensor" 
                            }
                            else {
                                log.debug "$ThermSet CSP already set to $CSPSetBedSensor -- Bed Sensor" 
                            }
                            if(ThermState != "cool"){
                                ThermSet.setThermostatMode("cool") 
                                log.debug "$ThermSet set to cool -- Bed Sensor"
                            }
                            else {
                                log.debug "$ThermSet already set to cool -- Bed Sensor"
                            }
                        }
                        else if(needHeat){
                            if(CurrentHeatingSetPoint != HSPSetBedSensor){
                                ThermSet.setHeatingSetpoint(HSPSetBedSensor)
                                log.debug "$ThermSet HSP set to $HSPSetBedSensor -- Bed Sensor" 
                            }
                            else {
                                log.debug "$ThermSet HSP already set to $HSPSetBedSensor -- Bed Sensor" 
                            }
                            if(ThermState != "heat" ){
                                ThermSet.setThermostatMode("heat") 
                                //log.debug "$ThermSet set to heat -- Bed Sensor"
                            }
                            else {
                                //log.debug "$ThermSet already set to heat -- Bed Sensor"
                            }
                        }
                        log.trace """
BED SENSOR DEBUG

needHeat = $needHeat, 
needCool = $needCool, 

CurrTemp = $CurrTemp, 
CSPSetBedSensor = $CSPSetBedSensor
HSPSetBedSensor = $HSPSetBedSensor
CSPok = $CSPok
HSPok = $HSPok
ContactAndSwitchState.size() = $ContactAndSwitchState.size()
"""
                    }

                    else {
                        BedSensorManagement = false
                        //log.debug "BedSensorManagement set to false (BedSensorManagement = $BedSensorManagement)"
                    }
                }


                log.info "-- End of Temperatures Evals for $ThermSet" 

                log.debug """

CSPok?($CSPok)
HSPok?($HSPok)
HSPMap = $state.HSPMap
CSPMap = $state.CSPMap
inMotionModes?($inMotionModes)
useMotion?($useMotion)
Motion at $MotionSensor Active for the past $minutesMotion minutes?($Active)
FINAL CSPSet for $ThermSet = $CSPSet
ThisIsExceptionTherm is: $ThisIsExceptionTherm ($ThermSet.displayName is in '$NoTurnOffOnContact)
ContactExceptionIsClosed = $ContactExceptionIsClosed
Too Humid INSIDE?($TooHumidINSIDE : ${INSIDEhumidity}%)

Too Humid OUTSIDE?($TooHumid : $humidity)
ShouldCoolWithAC = $ShouldCoolWithAC (loop $loopValue), 
ShouldHeat = $ShouldHeat
Current setpoint for $ThermSet is $CurrentCoolingSetPoint, 
Current Heating setpoint is $CurrentHeatingSetPoint,
Final CSPSet is $CSPSet
Current Set Points for $ThermSet are: cooling: $CurrentCoolingSetPoint, heating: $CurrentHeatingSetPoint 
"""


                /////////////////////////END OF SP MODifICATIONS//////////////////////////

                // state.HSPMap = [:] // for debug and test only
                // state.CSPMap = [:]


                state.HSPMap."$ThermSet" = "${HSPSet.toInteger()}"
                state.CSPMap."$ThermSet" = "${CSPSet.toInteger()}"
                log.info """state.HSPMap : $state.HSPMap 
state.CSPMap : $state.CSPMap"""  

                //log.debug "SP Maps updated"

                // Set as integer
                CSPSet = CSPSet.toInteger()
                HSPSet = HSPSet.toInteger()
                // record those values for further references like in windowscheck or overrides management

                state.CSPSet.CSP = CSPSet
                state.HSPSet.HSP = HSPSet              


                /////////////////////////SENDING COMMANDS//////////////////////////

                if(ContactAndSwitchInSameRoom && UnitToIgnore?.displayName == ThermSet.displayName && ContactAndSwitchState.size() > O  && !inAway){
                    log.debug "Turning off $ThermSet because it is in the same room as $ContactAndSwitch, which is currently ON"
                    ThermSet.setThermostatMode("off")
                }
                else {

                    if(doorsOk || (ContactExceptionIsClosed && ThisIsExceptionTherm)){

                        def inAutoOrOff = ThermState in ["auto","off"]
                        log.debug """
ShouldCoolWithAC = $ShouldCoolWithAC, ShouldHeat = $ShouldHeat, 
turnOffWhenReached = $turnOffWhenReached, 
$ThermSet is inAutoOrOff = $inAutoOrOff, 
BedSensorManagement= $BedSensorManagement
CurrTemp >= HSPSet = ${CurrTemp >= HSPSet} CurrTemp = $CurrTemp && HSPSet = $HSPSet
CurrTemp <= CSPSet = ${CurrTemp <= CSPSet} CurrTemp = $CurrTemp && CSPSet = $CSPSet 
CurrTemp measurment Device is: 
AppMgt = $AppMgt
"""

                        if(!BedSensorManagement){ // avoid redundancies if BedSensor's already managing unit. 
                            if(CurrTemp >= HSPSet || (ShouldCoolWithAC && CurrTemp <= CSPSet)){
                                if(useAltSensor){ 

                                    /// this allows for turn off request whenever a unit is linked to an alternate sensor
                                    log.debug "$ThermSet uses Alternative Sensor so it will be turned off once temperature is reached"
                                    if(!inAutoOrOff){

                                        state.LatestThermostatMode = "off"   
                                        // that's a "should be" value used to compare eventual manual setting to what "should be"
                                        // that's why it must be recoreded even during override mode
                                        //state.AppMgtMap[loopValue] = true //override test value
                                        if(AppMgt){
                                            log.debug "$ThermSet TURNED OFF"  
                                            ThermSet.setThermostatMode("off") 
                                        }
                                    }
                                    else {
                                        log.debug "$ThermSet already set to off"
                                    }
                                }

                                else if(turnOffWhenReached && !useAltSensor){
                                    /// turn off request for all other devices if and only if this option has been selected by user
                                    if(!inAutoOrOff){
                                        state.LatestThermostatMode = "off"                
                                        if(AppMgt){
                                            log.debug "$ThermSet TURNED OFF" 
                                            ThermSet.setThermostatMode("off")   
                                        }
                                        else {
                                            log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                        }
                                    }
                                    else {
                                        log.debug "$ThermSet already set to off"
                                    }
                                }
                            }
                            def TurnedOffForced = false
                            if(turnOffWhenReached && (CurrTemp >= HSPSet || (ShouldCoolWithAC && CurrTemp <= CSPSet))){
                                //if user selected this option then cannot eval based on warinside/wamoutside otherwise would
                                // never turn off units when temp is reached
                                log.debug "$ThermSet Off at user's request, not evaluating other criteria"
                                TurnedOffForced = true
                            }

                            // if turnOffWhenReached as soon as temp is below setpoint normal eval will resume

                            // now turn on heat or cool depending on situation and if no turn off request previously occurred 
                            if(ShouldCoolWithAC /*|| !CSPok*/){
                                // it may happen that old settings get stuck if estimate of shouldcool is false 
                                // so if no override but discrepancy between current csp and what should be
                                // go on
                                log.debug """ShouldCoolWithAC EVAL $loopValue AppMgt = $AppMgt
CurrentCoolingSetPoint == CSPSet ? ${CurrentCoolingSetPoint == CSPSet}"""

                                state.LatestThermostatMode = "cool"
                                if(AppMgt){
                                    //log.debug " $CurrentCoolingSetPoint == $CSPSet {CurrentCoolingSetPoint == CSPSet}?"
                                    if(!CSPok){
                                        ThermSet.setCoolingSetpoint(CSPSet)
                                        log.debug "$ThermSet CSP set to $CSPSet" 
                                    }
                                    else{
                                        log.debug "Cooling SetPoint already set to $CSPSet for $ThermSet ($CSPSet == $CurrentCoolingSetPoint)"
                                    }                   
                                    if(ShouldCoolWithAC && ThermState != "cool" && !TurnedOffForced){  
                                        // ShouldCoolWithAC has to be rechecked here otherwise !CSPok might trigger heat while no need
                                        log.debug "$ThermSet set to cool"
                                        ThermSet.setThermostatMode("cool") 
                                    }
                                    else {
                                        if(!ShouldCoolWithAC){
                                            log.debug "no need to cool at $ThermSet"
                                        }
                                        else{
                                            log.debug "$ThermSet already set to cool"
                                        }
                                    }
                                }
                                else {
                                    log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                }
                            }
                            if(ShouldHeat /*|| !HSPok*/){
                                // hsp must be set despite no need for heat otherwise hsp become inacurate during comparisons in other functions
                                // very weird bug !HSPok test seems to trigger a different setpoint... 69 when HSPSet = 75... totally weird bug... 
                                // commenting out "/*|| !HSPok*/" seems to have resolved the issue... even weirder! 
                                log.debug "ShouldHeat EVAL"
                                state.LatestThermostatMode = "heat"
                                if(AppMgt){

                                    if(!HSPok){
                                        // HSPok has to be rechecked here otherwise ShouldHeat might trigger setpoint reset while there's no need 
                                        log.info "----------------------------------- HSPSet ($ThermSet) = $HSPSet --------------------------"
                                        ThermSet.setHeatingSetpoint(HSPSet)
                                        log.debug "$ThermSet HSP set to $HSPSet" 
                                    }
                                    else { 
                                        log.debug "Heating SetPoint already set to $HSPSet for $ThermSet"
                                    }

                                    if(ShouldHeat && ThermState != "heat" && !TurnedOffForced){
                                        // ShouldHeat has to be rechecked here otherwise !HSPok might trigger heat while there's no need 

                                        log.debug "$ThermSet set to Heat"
                                        ThermSet.setThermostatMode("heat")  
                                    }
                                    else {
                                        if(!ShouldHeat){
                                            log.debug "no need to heat at $ThermSet"
                                        }
                                        else{
                                            log.debug "$ThermSet already set to heat"
                                        }
                                    }
                                }
                                else {
                                    log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                }
                            } 
                        }
                        else {
                            log.debug "$ThermSet mangaged by $BedSensor status, skipping"
                        }
                    }
                    else {
                        log.debug "Not evaluating for $ThermSet because some windows are open"
                        // check that therms are off  

                        def AnyON = Thermostats.findAll{ it?.currentValue("thermostatMode") != "off"}
                        log.debug "there are ${AnyON.size()} untis that are still running: $AnyON"

                        def count = 0

                        for(count = 0; count < Thermostats.size(); count++){ 

                            def device = AnyON[count]
                            if(ThermState != "off"){
                                if(device != null && !ThisIsExceptionTherm){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE SOME CONTACTS ARE OPEN"
                                }
                                if(ThisIsExceptionTherm && !ContactExceptionIsClosed){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE EXCEPTION CONTACT IS OPEN"
                                }
                                if(ThisIsExceptionTherm && !InExceptionContactMode){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE this is not one of the exception modes"
                                }
                            }
                            else {
                                //log.debug "device already off"
                            }
                        }
                    }
                }

                log.debug " END OF FOR LOOP $loopValue" 
            }
            else {
                log.debug "$ThermSet in Override mode, doing nothing"
            }
        }   
        // true end of  loop
    }
    else { 
        //log.debug "not evaluating because some windows are open" 
        TurnOffThermostats()
        Thermostats.setThermostatMode("off") // temporary because of those idiots at smartthings who pushed a fucking stupid useless update that prevents status refresh
        state.thisIsWindowMgt = false
    }


    VirtualThermostat()

    log.debug "END EVAL"

}

def MotionTest(){

    //  returned totalList of lists in SetListsAndMaps(): [thermMotionList, MotionModesList, MotionSensorList, MotionModesAndItsThermMap, SensorThermMap]
    def thermMotionList = SetListsAndMaps()
    thermMotionList = thermMotionList[0]

    def MotionModesList = SetListsAndMaps()
    MotionModesList = MotionModesList[1]

    def MotionSensorList = SetListsAndMaps()
    MotionSensorList = MotionSensorList[2]

    def MotionModesAndItsThermMap = SetListsAndMaps()
    MotionModesAndItsThermMap = MotionModesAndItsThermMap[3]

    def SensorThermMap = SetListsAndMaps()
    SensorThermMap = SensorThermMap[4]

    /*  log.info """
thermMotionList = $thermMotionList
MotionModesList = $MotionModesList
MotionSensorList = $MotionSensorList

MotionModesAndItsThermMap = $MotionModesAndItsThermMap
SensorThermMap = $SensorThermMap
"""  
*/
    def loopV = 0
    def s = MotionSensorList.size()
    def t = thermMotionList.size()
    //def s = MotionSensor.findAll{it.device != null}.sort()

    loopV = 0
    def o = null
    def i = 0
    def x = []
    def ThisSensorList = []

    i = 0
    def deltaMinutes = minutesMotion * 60000 as Long

    def motionEvents = []
    def result = [:]
    def Active = false
    for(t > 0; i < t; i++){

        def TheSensor = "MotionSensor${i}"
        TheSensor = settings.find{it.key == TheSensor}
        TheSensor = TheSensor?.value

        motionEvents = TheSensor.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
        // //log.debug "motionEvents = $motionEvents"
        Active = motionEvents.size() != 0
        result << ["${TheSensor}": "$Active"]
        log.debug """
Found ${motionEvents.size() ?: 0} events in the last $minutesMotion minutes at ${MotionSensorList[i]}
deltaMinutes = $deltaMinutes"""
    }
    //log.debug "result = $result"
    return result
}
def IndexValueMode(){
    def ModeInArray = WhichMode()

    def ModeList = ["$Home", "$Night", "$Away", "$CustomMode1", "$CustomMode2"]  
    def LetterModeList = ["H", "N", "A", "Cust1_T", "Cust2_T"]
    def NumberModeList = ["0", "1", "2", "3", "4"]

    def ModeMapList = [:]
    def lv = 0
    def size = LetterModeList.size()
    def ModeValue = null
    def ModeFound = null
    def ModeMatches = false
    def ModeIndexValue = null
    for(size > 0; lv < size; lv++){
        /// DO NOT DELETE THIS EXAMPLE! (ModeMapList) it will be usefull for further A.I. developments 
        /// ModeMapList << [(ModeList[lv]) : (LetterModeList[lv])]    
        /// build a map of all modes and set a Srting key for each mode
        /// this allows for writing a new variable below
        ModeFound = ModeList[lv] // each mode in the array created above, is an array in itself

        ModeMatches = ModeFound.contains("$ModeInArray") // so find to which array mode the current mode belongs to

        //log.debug "ModeFound is : $ModeFound || ModeMatches = $ModeMatches || ModeInArray = $ModeInArray"
        if(ModeMatches){
            //log.debug "MATCH!"
            ModeValue = "${LetterModeList[lv]}" // attribute mode letter to start writing the new variable
            ModeIndexValue = "${NumberModeList[lv]}" 
            // break this loop so it doesn't apply a match to other modes (since now ModeMatches = true)
            break
        }

        //log.debug "mode found = $ModeValue && $ModeIndexValue"
    }

    log.debug """ModeValue = $ModeValue && ModeIndexValue = $ModeIndexValue"""

    return [ModeValue, ModeIndexValue]
}
def WhichMode(){
    def CurrMode = location.currentMode
    def ModeInArray = null
    if(CurrMode in CustomMode1){
        ModeInArray = CustomMode1.find{it == "$CurrMode"}
        //log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in CustomMode2){
        ModeInArray = CustomMode2.find{it == "$CurrMode"}
        //log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in Home){
        ModeInArray = Home.find{it == "$CurrMode"}
        //log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in Night){
        ModeInArray = Night.find{it == "$CurrMode"}
        //log.debug "ARRAY MODE IS: $ModeInArray"
    }
    return ModeInArray
}
def KeepOffAtAllTimesWhenMode(){
    def result = false
    def inMode = location.currentMode in SwitchMode
    log.debug """
ToggleBack = $ToggleBack
SwitchIfMode = $SwitchIfMode
inMode = $inMode
"""

    if(ToggleBack || SwitchIfMode){
        if(SwitchIfMode){
            if(inMode){
                result = true
            }
            else {
                result = false 
            }
        }
        else {
            result = false 
        }
    }
    else {
        result = false 
    }
    log.debug "KeepOffAtAllTimesWhenMode = $result"
    return result 
}

// Main events management
def temperatureHandler(evt) {

    def doorsOk = AllContactsAreClosed()

    if(evt.device == XtraTempSensor) {
        state.Inside = evt.value
    }

    def currentTemp = state.Inside
    log.info """
current temperature value for $evt.device is $evt.value
Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp and CriticalTemp is $CriticalTemp
"""

    if(currentTemp <= CriticalTemp) {
        log.info "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 

        Thermostats.setThermostatMode("heat") 

        state.CRITICAL = true

        def message = ""
        if(Actuators && !doorsOk){
            //log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                message = "Closing windows because $state.causeClosed"
                send(message)

                // see if these are the same devices as ventings'
                // see if these switches are the same devices as ventings'
                def ActuatorsVentingCOLL = ActuatorsVenting.collect{ it.toString() }
                def ActuatorsCOLL = Actuators.collect{ it.toString() }
                def Intersection = ActuatorsVentingCOLL.intersect(ActuatorsCOLL)
                def Same = Intersection.size() != 0
                //log.debug "Actuators intersect VentingActuators"

                if(Same){ // venting supercedes !OkToOpen() when the corresponding devices interesect

                    if(state.coldbutneedcool == 0){
                        Actuators?.off()
                        ActuatorException?.off()
                        state.ventingrun = 0 // allows future venting
                        state.more = 0 
                        // allow for user to reopen them if they want to. 
                        state.windowswereopenandclosedalready = true // windows won't close again as Long as temperature is still critical to allow for user's override 
                        // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
                    }
                    else {
                        //log.debug "not closing windows because state.coldbutneedcool = $state.coldbutneedcool"
                    }
                }
                else { // no intersection between those devices so these windows / fans will now stop/close
                    Actuators?.off()
                    ActuatorException?.off()
                    state.ventingrun = 0 // allows future venting
                    state.more = 0 
                    // allow for user to reopen them if they want to. 
                    state.windowswereopenandclosedalready = true // windows won't close again as Long as temperature is still critical to allow for user's override 
                    // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
                }
            }
            else { 
                message = "doors and windows already reopened by user so not running emergency closing. BEWARE! these windows will not close again"
                log.info message
                send(message)

            }
        } 
        state.TheresBeenCriticalEvent = true
    } 
    else 
    { 
        //log.debug "CriticalTemp OK"
        state.CRITICAL = false
        state.windowswereopenandclosedalready = false

        state.TheresBeenCriticalEvent = false
    } 

    Evaluate()

}
def contactHandlerClosed(evt) {

    state.thisIsWindowMgt = false 
    state.attempts = 0 // for future reset of thisiswindowsmgt()

    def message = ""

    //log.debug "$evt.device is $evt.value" 

    log.info "List of devices' status is $CurrentContactsState"


    if(!AllContactsAreClosed()){
        //log.debug "Not all contacts are closed, doing nothing"

    }
    else {      
        //log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
        unschedule(TurnOffThermostats) // in case were closed within time frame

        //log.debug "state.ClosedByApp = $state.ClosedByApp"

        if(state.ClosedByApp == false && state.OpenByApp == true && evt.value == "closed"){ 

            message = "Windows $evt.value manualy and they will not open again until you open them yourself"
            log.info message
            send(message)
        }
    }

    if(AllContactsAreClosed()){
        updated()
    }

} 
def contactHandlerOpen(evt) {
    //log.debug "$evt.device is now $evt.value, Turning off all thermostats in $TimeBeforeClosing seconds"

    state.attempts = 0 // reset of thisiswindowsmgt()
    state.thisIsWindowMgt = true // prevent false ON/OFF override


    runIn(TimeBeforeClosing, TurnOffThermostats)  
    def message = ""

    //log.debug "state.OpenByApp = $state.OpenByApp"
    if(state.OpenByApp == false && state.ClosedByApp == true && evt.value == "open"){ 

        message = "Windows $evt.value manualy and will not close again until you close them yourself"
        log.info message
        send(message)

    }
    runIn(60, thisIsWindowMgtFALSE)

}
def contactExceptionHandlerOpen(evt) {

    state.thisIsWindowMgt = true

    state.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Turning off all thermostats in $TimeBeforeClosing seconds"

    runIn(TimeBeforeClosing, TurnOffThermostats)  

    runIn(5, thisIsWindowMgtFALSE)
    //  Evaluate()
}
def contactExceptionHandlerClosed(evt) {
    state.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Resuming Evaluation for $NoTurnOffOnContact"

    Evaluate()
}
def ChangedModeHandler(evt) {

    //log.debug "mode changed to ${evt.value}"
    def ContactsClosed = MainContactsClosed()

    if(ContactsClosed) {
        // windows are closed 
        state.ClosedByApp = true // app will open windows if needed 
        state.OpenByApp = false 
        // has to be the default value so it doesn't close again if user opens windows manually or another app. 
        // Beware that this can be a serious safety concern (for example, if a user has these windows linked to a smoke detector
        // so do not modify these parameters under any circumstances 
        // and check that this works after any modification you'd bring to this app

        state.ThermOff = false

    } 

    state.recentModeChange = true
    state.ventingrun = 0
    runIn(60, recentModeChangeFALSE)

    updated()

}
def recentModeChangeFALSE(){
    state.recentModeChange = false
}

// A.I. and micro location evt management
def motionSensorHandler(evt){
    //log.debug "motion is $evt.value at $evt.device"  

    Evaluate()


}
def HumidityHandler(evt){

    log.info "humidity value is ${evt?.value}%"
    state.humidity = evt.value

    Evaluate()


}
def InsideHumidityHandler(evt){
    log.info "INSIDE humidity value at $evt.device is ${evt.value}%"
    state.INSIDEhumidity = evt.value


    Evaluate()

}
def WindHandler(evt){

    log.info "Wind value is ${evt.value}mph"  

    state.wind = evt.value
    Evaluate()
}
def FeelsLikeHandler(evt){

    log.info "Currently, outside temperature feels like ${evt.value}F"  

    state.FeelsLike = evt.value
    Evaluate()

}

def switchHandler(evt){

    //log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        state.exception = true
    } else {
        state.exception = false
    }
    Evaluate()

}
def ContactAndSwitchHandler(evt){
    //log.debug "ContactAndSwitchHandler : ${evt.device} is ${evt.value}"

    if(evt.value == "off"){
        state.contactAndSwtchOff = true
    } else {
        state.contactAndSwtchOff = false
    }

}
def BedSensorHandler(evt){

    log.debug """$evt.device is $evt.value 
BedSensor is $BedSensor------------------------------------------------------------------------"""

    // Evaluate()

}
def Timer() {
    def minutes = findFalseAlarmThreshold() 
    def deltaMinutes = minutes as Long

    def ContactsEvents = BedSensor?.collect{ it.eventsSince(new Date(now() - (60000 * deltaMinutes))) }.flatten()
    //BedSensor[0].statesBetween("contact", start, end, [max: 200]))

    log.debug """
Timer Found ${ContactsEvents.size()} events in the last $minutes minutes"
"""
    def size = ContactsEvents.size()
    return size

}
private findFalseAlarmThreshold() {
    // In Groovy, the return statement is implied, and not required.
    // We check to see if the variable we set in the preferences
    // is defined and non-empty, and if it is, return it.  Otherwise,
    // return our default value of 2
    (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 2
}
def BedSensorStatus(){
    def ConsideredOpen = true // has to be true by default in case no contacts selected
    def BedSensorAreClosed = false // has to be false by default in case no contacts selected

    if(BedSensor){

        def CurrentContacts = BedSensor.currentValue("contact")    
        def ClosedContacts = CurrentContacts.findAll { val ->
            val == "closed" ? true : false}

        if(ClosedContacts.size() == BedSensor.size()){
            BedSensorAreClosed = true
        }

        //log.debug "${ClosedContacts.size()} sensors out of ${BedSensor.size()} are closed SO BedSensorAreClosed = $BedSensorAreClosed"
        def ContactsEventsSize = Timer()

        def Open = BedSensor.findAll{it.currentValue("contact") == "open"}

        boolean isOpen = Open.size() != 0 && !BedSensorAreClosed
        //log.debug "Open = ${Open}, isOpen = $isOpen"

        if(isOpen && ContactsEventsSize > 1){
            ConsideredOpen = false
            //log.debug "too many events in the last couple minutes"
        }
        else if (isOpen && ContactsEventsSize == 1){  

            def Map = [:]
            def i = Thermostats.size()
            def loopV = 0
            def Therm = null
            for(i != 0; loopV < i; loopV++){
                Therm = Thermostats[loopV]
                log.info "Therm is $Therm"
                Map << ["$Therm": loopV]
            }


            def KeyValueForThisTherm = Map.find { it.key == "$ThermContact"}
            log.info "devices is/are ------------------- $KeyValueForThisTherm.value"
            def ThermNumber = KeyValueForThisTherm.value
            ThermNumber = KeyValueForThisTherm.value.toInteger()


            //state.AppMgtMap.remove("$ThermContact")
            // state.AppMgtMap["$ThermContact"] = false 

            ConsideredOpen = true
            //log.debug "Only one event within the last couple minutes"

        }
    }
    //log.debug "BedSensorAreClosed = $BedSensorAreClosed, ConsideredOpen = $ConsideredOpen"
    return [BedSensorAreClosed, ConsideredOpen]
}

//override management // deprecated
def setpointHandler(evt){

    log.trace "${evt.device}'s $evt.name set to $evt.value (setpointHandler)"


}
def thisIsWindowMgtFALSE(){

    state.thisIsWindowMgt = false

}
def resetLocationChangeVariable(){
    state.locationModeChange = false
    //log.debug "state.locationModeChange reset to FALSE"
}

// contacts and windows management and motion bool tests
def MainContactsClosed(){
    def MainContactsAreClosed = true // has to be true by default in case no contacts selected
    def CurrentContacts = Maincontacts.currentValue("contact")

    log.debug """Maincontacts are $Maincontacts
CurrentContacts States = $CurrentContacts"""

    def ClosedContacts = CurrentContacts.findAll { AllcontactsAreClosed ->
        AllcontactsAreClosed == "closed" ? true : false}

    MainContactsAreClosed = ClosedContacts.size() == Maincontacts.size() 

    log.debug """${ClosedContacts.size()} windows/doors out of ${Maincontacts.size()} are closed 
MainContactsClosed returns $MainContactsAreClosed"""
    return MainContactsAreClosed
}
def ExcepContactsClosed(){

    def ContactsExepClosed = true
    if(ContactException){
        def CurrentContactsExept = ContactException.currentValue("contact")    
        //log.debug "CurrentContactsExept = $CurrentContactsExept && ContactException are $ContactException"

        def ClosedContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreClosed ->
            AllcontactsExeptAreClosed == "closed" ? true : false
        }
        ContactsExepClosed = ClosedContactsExpt.size() == ContactException.size() 
        //log.debug "${ClosedContactsExpt.size()} windows/doors out of ${ContactException.size()} are closed SO ContactsExepClosed = $ContactsExepClosed"

        def NoTurnOffOnContact = Thermostats.find {NoTurnOffOnContact << it.device}        

        //log.debug "NoTurnOffOnContact = $NoTurnOffOnContact --------------"

        def CurrTherMode = NoTurnOffOnContact.currentValue("thermostatMode")
        //log.debug "Current Mode for $ThermContact is $CurrTherMode"
        if(CurrTherMode != "off" && !ContactsExepClosed){
            //log.debug "$NoTurnOffOnContact is on, should be off. Turning it off" 
            NoTurnOffOnContact.setThermostatMode("off") 
            state.LatestThermostatMode_T1 = "off"
        }
    }
    //log.debug "ExcepContactsClosed returns $ContactsExepClosed"
    return ContactsExepClosed

}
def AllContactsAreClosed() {

    def AllContactsClosed = MainContactsClosed() && ExcepContactsClosed()
    //log.debug "AllContactsAreClosed() $AllContactsClosed"

    return AllContactsClosed
}
def AllContactsAreOpen() {

    def MainContactsAreAllOpen = false // has to be true by default in case no contacts selected

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def OpenContacts = CurrentContacts.findAll { AllcontactsAreOpen ->
        AllcontactsAreOpen == "open" ? true : false}
    //log.debug "${OpenContacts.size()} windows/doors out of ${Maincontacts.size()} are open"
    def OpenMainSize = OpenContacts.size()
    def MainSize = Maincontacts.size() 
    MainContactsAreAllOpen = OpenMainSize == MainSize


    def AllOpen = MainContactsAreAllOpen
    ///
    if(ContactException){
        def ContactsExepOpen = false

        def CurrentContactsExept = ContactException.currentValue("contact")    
        def OpenContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreOpen ->
            AllcontactsExeptAreOpen == "open" ? true : false
        }
        //log.debug "${OpenContactsExpt.size()} windows/doors out of ${ContactException.size()} are open SO ContactsExepOpen = $ContactsExepOpen"

        def OpenExptSize = OpenContactsExpt.size()
        def ExceptionSize = ContactException.size() 
        ContactsExepOpen = OpenExptSize == ExceptionSize

        AllOpen = ContactsExepOpen && MainContactsAreAllOpen

    }
    //log.debug "AllOpen?($AllOpen)"

    return AllOpen  

}

def TurnOffThermostats(){

    def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
    //log.debug "InExceptionContactMode = $InExceptionContactMode  DoNotTurnOffModes = $DoNotTurnOffModes (TurnOffThermostats)"
    //CurrMode in SwitchMode


    if(ContactAndSwitch){
        def contactClosed = false

        if(ContactException && FollowException && InExceptionContactMode){

            contactClosed = ExcepContactsClosed()
        }
        else{
            contactClosed =  AllContactsAreClosed()
        }
        //log.debug "contactClosed = $contactClosed (TurnOffThermostats)"
        def CurrentSwitch = ContactAndSwitch.currentSwitch
        //log.debug "$ContactAndSwitch currentSwitch = $currentSwitch"
        def SomeSwAreOn = CurrentSwitch.findAll { switchVal ->
            switchVal == "on" ? true : false
        }
        log.trace "SomeSwAreOn = $SomeSwAreOn"
        if(!contactClosed){
            if(SomeSwAreOn.size() != 0){
                ContactAndSwitch?.off()
                log.debug "$ContactAndSwitch TURNED OFF"
                state.turnedOffByApp = true
            }
            else {
                log.debug "$ContactAndSwitch already off"
            }
        }
    }

    def loopValue = 0
    def t = Thermostats.size()


    def doorsOk = AllContactsAreClosed()

    def ContactExceptionIsClosed = ExcepContactsClosed()

    if(state.CRITICAL == false){
        for(t > 0; loopValue < t; loopValue++){

            def ThermSet =  Thermostats[loopValue]
            def ThermState = ThermSet.currentValue("thermostatMode")   
            //log.debug "${ThermSet}'s ThermState = $ThermState"


            log.trace "Turning off thermostats: ContactExceptionIsClosed: $ContactExceptionIsClosed, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"

            if((!NoTurnOffOnContact || !InExceptionContactMode || !ContactExceptionIsClosed) && "${ThermSet}" == "${NoTurnOffOnContact}"){
                if(ThermState != "off"){
                    // to avoid false end of override while windows are open and exception thermostat still needs to remain in override mode. 

                    ThermSet.setThermostatMode("off") 
                    state.LatestThermostatMode = "off"
                    //log.debug "$ThermSet  turned off"

                }
            }
            else {

                log.debug "Not turning off $ThermSet because current mode is within exception modes selected by the user"

                state.LatestThermostatMode = ThermState
            }

            if("${ThermSet}" != "${NoTurnOffOnContact}"){
                if(ThermState != "off"){  

                    ThermSet.setThermostatMode("off") 
                    state.LatestThermostatMode = "off"
                    log.debug "$ThermSet turned off"

                }
                else {
                    log.debug "$ThermSet ALREADY off"
                }
            }
        }
        state.ThermOff = true
    }

    else { 
        //log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 


    }


}

def CheckWindows(){
    //log.debug "Checking windows"
    //long MessageMinutes = 60*60000 as Long
    //long LastTimeMessageSent = state.LastTimeMessageSent as Long
    //def MessageTimeDelay = now() > LastTimeMessageSent + MessageMinutes 
    //log.debug "MessageTimeDelay = $MessageTimeDelay (checkwindows)"
    // for when it previously failed to turn off thermostats
    def AllContactsClosed = AllContactsAreClosed()
    def CurrMode = location.currentMode
    def OkToOpen = OkToOpen() // outside and inside temperatures criteria and more... 
    def message = ""
    def allContactsAreOpen = AllContactsAreOpen()
    def Inside = XtraTempSensor.currentValue("temperature")
    def Outside = OutsideSensor.currentValue("temperature")

    log.trace """
OkToOpen?($OkToOpen); 
OffSet?($OffSet) 
state.ClosedByApp($state.ClosedByApp) 
state.OpenByApp($state.OpenByApp) 
state.messageSent($state.messageSent) 

"""
    if(OkToOpen){

        def ClosedByApp = state.ClosedByApp
        def inAway = CurrMode in Away
        def outsideTemp = OutsideSensor?.currentValue("temperature")
        def HSPSet = state.HSPSet.HSP
        def CSPSet = state.CSPSet.CSP
        def WarmEnoughOutside = outsideTemp >= 60

        if(AllContactsClosed || state.ventingrun == 1){
            log.debug "OpenInfullWhenAway = $OpenInfullWhenAway, inAway = $inAway"
            if( inAway && ClosedByApp != true && OpenInfullWhenAway  && WarmEnoughOutside){
                ClosedByApp = true
            }
            if(ClosedByApp) {
                Actuators?.on()
                if(inAway && OpenInfullWhenAway){
                    ActuatorException?.on()
                }
                state.OpenByApp = true
                state.ClosedByApp = false // so it doesn't open again

                //log.debug "opening windows"
                if(!NotWindows){
                    if(OperatingTime){
                        message = "I'm opening windows because $state.causeOpen. Operation time is $OperatingTime seconds"
                        if(inAway && OpenInfullWhenAway){
                            //log.debug "not applying operation time because location is in $Away mode"
                        }
                        runIn(OperatingTime, StopActuators) 
                    }
                    else {
                        message = "I'm opening windows because $state.causeOpen"
                    }
                }
                else {
                    // when not windows
                    message = "I'm turning on $Actuators because $state.causeOpen"
                }
                log.info message 
                //send(message)
            }
            else { 
                //log.debug "Windows have already been opened, doing nothing" 
            }
        }
    }
    // if not ok to open and it is open then close
    else if (state.OpenByApp == true && !AllContactsClosed) {
        // see if these are the same devices as ventings'
        // see if these switches are the same devices as ventings'
        def ActuatorsVentingCOLL = ActuatorsVenting.collect{ it.toString() }
        def ActuatorsCOLL = Actuators.collect{ it.toString() }
        def Intersection = ActuatorsVentingCOLL.intersect(ActuatorsCOLL)
        def Same = Intersection.size() != 0
        //log.debug "Actuators intersect VentingActuators"

        if(Same){ // venting supercedes !OkToOpen() when the corresponding devices interesect       
            if(state.coldbutneedcool == 0){
                Actuators?.off()
                ActuatorException?.off()

                message = "I'm closing windows because $state.causeClose"
                //send(message)
                log.info message 
                state.ventingrun = 0 // allows future venting
                state.more = 0 
                state.ClosedByApp = true
                state.OpenByApp = false // so it doesn't close again if user opens it manually

            }
            else {
                //log.debug "not closing windows because state.coldbutneedcool = $state.coldbutneedcool"
            }
        }
        else { // no intersection between those devices so these windows / fans will now stop/close
            Actuators?.off()
            ActuatorException?.off()
            state.ventingrun = 0 // allows future venting
            state.more = 0 
            message = "I'm closing windows because $state.causeClose"
            //send(message)
            log.info message 

            state.ClosedByApp = true
            state.OpenByApp = false // so it doesn't close again if user opens it manually
        }
    }
}
def CloseWindows(){
    if(state.coldbutneedcool == 0){
        Actuators?.off()
        ActuatorException?.off()
        state.ventingrun = 0 // allows future venting
        state.more = 0 
        // //log.debug "state.coldbutneedcool = $state.coldbutneedcool"
    }
}
def OkToOpen(){

    def message = ""
    //log.debug "OkToOpen()"
    def ContactsClosed = AllContactsAreClosed()

    log.debug """ 
state.CSPSet.CSP = ${state.CSPSet} state.algebraicCSPSet = ${state.algebraicCSPSet.CSP}
state.HSPSet.CSP = ${state.HSPSet} state.algebraicHSPSet = ${state.algebraicHSPSet.CSP}
"""
    def CSPSet = state.algebraicCSPSet.CSP
    def HSPSet = state.algebraicHSPSet.HSP

    //log.debug "NOW: CSPSet = $CSPSet &&  HSPSet = $HSPSet)"

    CSPSet = CSPSet?.toInteger()
    HSPSet = HSPSet?.toInteger()

    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature")
    def CurrTemp = Inside 

    // build an average temperature reference for quick venting/cooling option
    def ts = Thermostats.size()
    def i = 0
    def thermTemp = 0
    def ThisTherm = null
    def ThermList = []
    def thermCSP = 0
    def totalCSP = 0
    def totalTemp = 0
    def ThermTempList = []
    def ThermCSPList = []
    def itscooling = false
    def currentOperation = ""

    for(ts != 0; i < ts; i++){
        ThisTherm = Thermostats[i]
        thermTemp = ThisTherm.currentValue("temperature").toInteger()
        //log.debug "thermTemp = $ThisTherm: $thermTemp"
        thermCSP = CSPSet?.toInteger()
        //ThermTempList << [thermTemp]
        //ThermCSPList << [thermCSP]
        totalTemp += thermTemp
        totalCSP += thermCSP
        // //log.debug "totalTemp = $totalTemp, totaCSP = $totalCSP"

        currentOperation = ThisTherm.latestValue("thermostatMode")
        //log.debug "currentOperation for $ThisTherm is '$currentOperation'"
        if(currentOperation == "cool"){
            itscooling = true
            //log.debug" itscooling set to TRUE" // this will allow to avoid using AC when it's cool outside and have venting operation
        }
    }    

    def AverageCurrTemp = totalTemp/ts
    def AverageCSPSet = totalCSP/ts
    //log.debug "AverageCurrTemp = $AverageCurrTemp && AverageCSPSet = $AverageCSPSet && itscooling = $itscooling"

    def Outside = OutsideSensor.currentValue("temperature")
    //Outside = Double.parseDouble(Outside)
    //Outside = Outside.toInteger()
    def outsideTemp = Outside 
    state.outsideTemp = Outside
    def WithinCriticalOffSet = (Inside >= (CriticalTemp - OffSet)) && (Outside >= (CriticalTemp + OffSet))

    def OutsideTempHighThres = ExceptACModes()
    def ExceptHighThreshold1 = ExceptHighThreshold1
    //log.debug "test"

    //def humidity = OutsideSensor?.latestValue("humidity")
    def humidity = OutsideSensor.currentValue("temperature")
    //humidity = Double.parseDouble(humidity)
    //humidity = humidity.toInteger()
    //log.debug "Inside = $Inside | Outside = $Outside"
    def WindValue = state.wind
    WindValue = WindValue.toInteger()

    def ItfeelsLike = state.FeelsLike
    ItfeelsLike = ItfeelsLike.toInteger()
    def OutsideFeelsHotter = ItfeelsLike > Outside + 2


    //log.debug "Humidity EVAL"
    def TooHumid = false
    if(humidity > HumidityTolerance){
        TooHumid = true
    }
    if((WindValue > 3 || Outside < Inside + 2) && humidity <= HumidityTolerance + 5){
        TooHumid = false
    }

    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres && (!OutsideFeelsHotter || OutsideFeelsHotter == null)     
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4 && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    }

    def ShouldCool = OutSideWithinMargin 
    def ShouldHeat = state.ShouldHeat
    //log.debug "ShouldHeat = ${ShouldHeat} "

    if(!ShouldHeat && ShouldCool && CurrTemp >= CSPSet + 3) {
        ShouldCool = false
        // //log.debug "ShouldCool && ShouldHeat && !OutSideWithinMargin && CurrTemp >= CSPSet ==>> ShouldCool = $ShouldCool"
    }


    def inHomeMode = CurrMode in Home

    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid && !OutsideFeelsHotter
    def ShouldCoolWithAC = AverageCurrTemp - 3 > AverageCSPSet

    def NeedVenting = AverageCurrTemp > AverageCSPSet && Outside < AverageCurrTemp - 1 && !ShouldHeat && state.more == 0
    def NeedToCloseAfterVenting = AverageCurrTemp < AverageCSPSet - 1 && state.ventingrun > 0


    if(state.ventingrun != 1 && ContactsClosed && NeedVenting){
        state.ventingrun = 0
    }

    def inVentingModes = CurrMode in VentingModes
    def inVentingModesException = CurrMode in VentingModesException
    log.debug """
inVentingModes = $inVentingModes
inVentingModesException = $inVentingModesException
NeedVenting = $NeedVenting
state.more = $state.more 
NeedToCloseAfterVenting = $NeedToCloseAfterVenting 
state.coldbutneedcool = $state.coldbutneedcool 
state.ventingrun = $state.ventingrun 
itscooling = $itscooling"""


    if(CurrMode in Away){
        //log.debug "in $CurrMode mode, not venting"
    }
    else if (inVentingModes){
        if(NeedVenting)
        {

            state.coldbutneedcool = state.coldbutneedcool + 1 
            log.info"state.coldbutneedcool set to $state.coldbutneedcool"

            // open windows just to cool down a little while it's cold outside but too hot inside instead of using AC 
            if(state.ventingrun == 0){
                //log.debug "VENTING THE HOUSE because NeedVenting = $NeedVenting"
                TurnOffThermostats()
                ActuatorsVenting?.on()

                if(!inVentingModesException){
                    ActuatorsVentingException?.on()
                }

                runIn(10, StopActuators)
                message = "Venting the place to cool it down a little"
                log.info message
                send(message)

                state.ventingrun = state.ventingrun + 1 

            }
            else {
                //log.debug "not opening because state.ventingrun is $state.ventingrun"


                if(!ContactsClosed && AverageCurrTemp - 2 > AverageCSPSet && AverageCurrTemp < AverageCSPSet + 4){
                    // if CSP is 72, Average 75 then Average is still lower than 76
                    // it got hotter inside despite opening windows
                    // open them more 
                    log.debug """
opening a bit more
AverageCurrTemp - 2 = ${AverageCurrTemp - 2}
AverageCSPSet = $AverageCSPSet
AverageCurrTemp < AverageCSPSet + 4 = ${AverageCurrTemp < AverageCSPSet}
state.more = $state.more
"""
                    ActuatorsVenting?.on()
                    if(!inVentingModesException){
                        ActuatorsVentingException?.on()
                    }

                    runIn(10, StopActuators)


                    state.more = state.more + 1
                }
                // still getting too hot
                // if CSP is 72, Average 76 then Average -3 = 73 is still higher than 72
                // and we already tried to open fully
                if(!ContactsClosed && AverageCurrTemp - 3 > AverageCSPSet && state.more > 4){
                    // trigger full closing so AC can run instead 
                    log.debug """still too hot, closing entirely

AverageCurrTemp - 2 = ${AverageCurrTemp - 2}
AverageCSPSet = $AverageCSPSet
AverageCurrTemp < AverageCSPSet + 4 = ${AverageCurrTemp < AverageCSPSet}
state.more = $state.more
"""
                    state.coldbutneedcool = 0 

                }
            }
        }
        else if(NeedToCloseAfterVenting) {
            state.coldbutneedcool = 0
            state.more = 0

            log.debug """state.coldbutneedcool set to $state.coldbutneedcool
no venting needed, making sure windows are closed"""
            if(state.ventingrun >= 1){
                ActuatorsVenting?.off()
                ActuatorsVentingException?.off()

                state.ventingrun = 0
                log.debug """state.ventingrun set to $state.ventingrun
CLOSING WINDOWS"""
            }
        }
    }
    else 
    {
        //log.debug "Location is not in venting Modes"
    }

    state.OpenInFull = false
    def ItIsNiceOutThere = Outside > HSPSet - 3 && Outside < HSPSet

    if( OpenWhenEverPermitted && ItIsNiceOutThere) { 
        state.OpenInFull = true
    }
    else {
        state.OpenInFull = false
    }


    // open all the way when gone?
    if(CurrMode in Away && WithinCriticalOffSet && OpenInfullWhenAway ){
        result = true
        state.OpenInFull = true
    } 
    def CRITICAL = false
    if(state.CRITICAL == true){
        result = false
        CRITICAL = true
        state.OpenByApp = true // so windows will close even if manually opened
    }
    /*
if(!result){
// preparing a dynamic message which will tell why windows won't open (or fans won't turn on)
def cause1 = !OutSideWithinMargin
def cause2 = !WithinCriticalOffSet
def cause3 = !ShouldCool
def cause4 = TooHumid
def cause5 = CurrMode in "$Away"
def cause6 = CRITICAL

def causeNotList = [ cause1, cause2, cause3, cause4]

def causeNotTest = causeNotList.findAll{ val ->
val == true ? true : false
}
def ManyCauses = causeNotTest.size() > 1
def and2 =""
def and3 = ""
def and4 = ""
def and5 = ""
def and6 = ""

if(ManyCauses && cause2){
and2 = ": " 
}
if(ManyCauses && cause3){
and3 = " and"
}
if(ManyCauses && cause4){
and4 = " and"
}
if(ManyCauses && cause5){
and5 = " and"
}
if(ManyCauses && cause6){
and6 = " and"
}


def causeNotMap = [ "outside temperature is not within comfortable margin" : cause1,  
"$and2 it is not too hot inside ${XtraTempSensor}'s room" : cause2 , 
"$and3 it is too hot $outsideWord" : cause3 ,  
"$and4 it is too humid outisde" : cause4, 
"$and5 home is in $Away Mode": cause5, 
"$and6 it is too cold" : cause6, 
]

// creates a new map with only the keys that have values = true
def causeNotOkToOpen = causeNotMap.findAll{it.value == true}
// now collect the keys from this map 
causeNotOkToOpen = causeNotOkToOpen.collect{ it.key }
// build a string without the parentheses 
def MessageStr = new StringBuilder();
for (String value : causeNotOkToOpen) {
MessageStr.append(value);
}
causeNotOkToOpen = MessageStr.toString();
state.causeClose = causeNotOkToOpen
if(state.coldbutneedcool == "false"){
message = "Windows are closed because $causeNotOkToOpen"
}
else {
message = "We need some fresh air here..."
}
log.info message
state.messageclosed = message
// send a reminder every X minutes 

long MessageMinutes = 60L*60000L
long LastTimeMessageSent = state.LastTimeMessageSent
long SinceLast = LastTimeMessageSent + MessageMinutes

def MessageTimeDelay = now() > SinceLast
//log.debug "SinceLast = $SinceLast || MessageMinutes = $MessageMinutes || LastTimeMessageSent = $LastTimeMessageSent || MessageTimeDelay = $MessageTimeDelay"

if(MessageTimeDelay && ContactsClosed) {
//send(message)
LastTimeMessageSent = now() as Long
state.LastTimeMessageSent = LastTimeMessageSent as Long
}

}
// causes for opening windows or turning on fans
else {
def cause1 = CurrMode in $Away && WithinCriticalOffSet && OpenInfullWhenAway 
def cause2 = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid
//def cause3 = WithinCriticalOffSet
//def cause4 = TooHumid

def causeOktList = [ cause1, cause2 ]
//log.debug "causeNotList = $causeNotList"
def causeOkTest = causeOktList.findAll{ val ->
val == true ? true : false
}
def and = ""

if(cause1 && cause2){
and = "and"
}
def causeOkMap = [ "Home is in $CurrMode and outside and inside temperatures are within safety margins" : cause1,  
"$and It is not too humid nor too hot nor cold outside" : cause2 , 
]

// create a new map with only the keys that have values = true
def causeOkToOpen = causeOkMap.findAll{it.value == true}
// now get only the keys from this map 
causeOkToOpen = causeOkToOpen.collect{ it.key }
// build a string without the parentheses 
def MessageStr = new StringBuilder();
for (String value : causeOkToOpen) {
MessageStr.append(value);
}
causeOkToOpen = MessageStr.toString();
state.causeOpen = causeOkToOpen

message = "Windows are open because $causeOkToOpen"

state.messageopened = message // sent once as push message by checkwindows()

}
*/
    log.info """
Inside?($Inside), Outside?($Outside), 
Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) 
closed?($ContactsClosed)
OutSideWithinMargin?($OutSideWithinMargin)
Inside is WithinCriticalOffSet?($WithinCriticalOffSet) 
Should Cool (no AC)?($ShouldCool)
Should Heat?($ShouldHeat)
ItfeelsLike ${ItfeelsLike}F
Wind = ${WindValue}mph
HumidityTolerance($HumidityTolerance)
Too Humid?($TooHumid)
Outside Humidity is: $humidity
OutsideFeelsHotter?($OutsideFeelsHotter)
OkToOpen?($result)
"""

    /// FOR TESTS 
    // result = false

    return result
}

def StopActuators(){
    def CurrMode = location.currentMode
    def inAway = CurrMode in Away

    def SlightOpen = state.SlightOpen

    def OpenInFull = state.OpenInFull
    // //log.debug "CHECKING FOR STOP COMMAND"

    if(!inAway && !OpenInfullWhenAway){
        // //log.debug "SENDING STOP COMMAND"
        if (Actuators?.hasCommand("stop")/* && !OpenInFull*/){
            Actuators?.stop()
        }
        if (ActuatorException?.hasCommand("stop") /* && !OpenInFull*/){
            ActuatorException?.stop()        
        }
    }
    else if(state.coldbutneedcool >= 1 || state.more >= 1){
        // //log.debug "SENDING STOP COMMAND"
        if (ActuatorException?.hasCommand("stop") /* && !OpenInFull*/){
            ActuatorException?.stop()        
        }
        if (Actuators?.hasCommand("stop")/* && !OpenInFull*/){
            Actuators?.stop()
        }
    }
}
def ActuatorsDelay() {

    def seconds = 5000 as Long
    def secondsLog = seconds / 1000
    def since = now() as Long
    def Delay = since + seconds as Long
    log.trace "seconds($secondsLog), since($since), Delay($Delay)"
    /* while(now() < Delay){
//log.debug "wait $secondsLog seconds"
}

StopActuators()
*/
    runIn(OperatingTime, StopActuators)

}

//miscellaneous 
def ExceptACModes(){
    def OutsideTempHighThres = OutsideTempHighThres
    if(ExceptACMode1 && CurrMode in ExceptACMode1){
        def ToMinus = OutsideTempHighThres
        //log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold1
        //log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold1" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    else if(ExceptACMode2 && CurrMode in ExceptACMode2){
        def ToMinus = OutsideTempHighThres
        //log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold2
        //log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold2" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    return OutsideTempHighThres
}
def schedules() { 

    def scheduledTimeA = 3 // less messes up devices' health
    def scheduledTimeB = 15

    //schedule("0 0/$scheduledTimeA * * * ?", Evaluate)
    //log.debug "Evaluate scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    log.debug "polls scheduled to run every $scheduledTimeB minutes"

}
def polls(){

    def s = Thermostats.size()
    def i = 0
    for(s != 0; i < s; i++){
        def therm = Thermostats[i]
        def poll = therm.hasCommand("poll")
        def refresh = therm.hasCommand("refresh") 

        if(poll){
            therm.poll()
            log.debug "polling $therm"
        }
        else if(refresh){
            therm.refresh()
            log.debug "refreshing $therm"
        }


    }


    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            log.debug "polling $OutsideSensor -"
        }
        else if(refresh){
            OutsideSensor.refresh()
            log.debug "refreshing $OutsideSensor -"
        }
        else { 
            log.debug "$OutsideSensor does not support either poll() nor refresh() commands"
        }
    }

}
def send(msg){
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage) {
            //log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            //log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    //log.debug msg
}


