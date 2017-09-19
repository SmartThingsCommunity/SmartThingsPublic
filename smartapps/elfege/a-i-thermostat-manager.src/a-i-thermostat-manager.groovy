definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """Manage one or more thermostats in parallel with several other features such as: 
- Home location mode (up to 5 modes)
- open/close windows and/or turn on/off fans instead of AC
- contact sensors 
- humidity measurment
- outside / inside temperatures amplitude
- wind speed 
- subjective ("feels like") outside's temperature
- adjust temperature with presence using motion sensors
- switch on/off state
""" ,
    category: "Green Living",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg",
    image: "http://elfege.com/penrose.jpg"
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
            if(OutsideTempHighThres){
                input(name:"adjustments", type: "enum", title: "Do you want to use dynamic temperatures adjustments?", 
                      options: ["no, just go with my default settings", 
                                "Yes, use a linear variation", "Yes, but use a logarithmic variation"], required: true)

                paragraph """
linear: save power and money and remain comfortable. 
Algorithmic: save less money but be even more comfortable"""
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

            input(name: "Thermostats", type: "capability.thermostat", title: "select thermostats", required: false, multiple: true, description: null, submitOnChange: true, uninstall: true)
            if(Thermostats){

                input(name: "AltSensor", type: "bool", title: "Control some thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor){
                    input(name: "ThermSensor", type: "enum", title: "Which devices do you want to control this way?", multiple: true, options: ["${Thermostats[0]}", "${Thermostats[1]}", "${Thermostats[2]}"], required: true, submitOnChange: true)

                    def t0 = Thermostats[0]
                    def t1 = Thermostats[1]
                    def t2 = Thermostats[2]
                    def ThermSensorColl = ThermSensor.collect{ it.toString() }
                    def t0Coll = t0.collect{ it.toString() }
                    def t1Coll = t1.collect{ it.toString() }
                    def t2Coll = t2.collect{ it.toString() }
                    def intersect0 = t0Coll.intersect(ThermSensorColl)
                    def intersect1 = t1Coll.intersect(ThermSensorColl) 
                    def intersect2 = t2Coll.intersect(ThermSensorColl)
                    log.debug """ t0Coll = $t0Coll, t1Coll = $t1Coll, t2Coll = $t2Coll
intersect0 = $intersect0, intersect1 = $intersect1, intersect2 = $intersect2"""

                    if(intersect0.size() != 0) {
                        input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[0]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(intersect1.size() != 0) {
                        input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[1]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(intersect2.size() != 0) {
                        input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[2]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(Thermostats.size() > 3){
                        paragraph: "You can control only 3 units with a separate sensor"

                    }
                }
                input(name: "turnOffWhenReached", type: "bool", title: "Turn off thermostats when desired temperature is reached?", required: false, default: false, submitOnChange: true)
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
                input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: false, required: false, submitOnChange: true)
            }
            section("Other Modes"){
                input(name: "Night", type : "mode", title: "Select Night mode", multiple: false, required: true, submitOnChange: true)
                input(name: "Away", type : "mode", title: "Select away mode", multiple: false, required: true, submitOnChange: true)
            }
            section("MoreModes"){ 
                input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
                if(Moremodes){
                    input(name: "CustomMode1", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                    input(name: "CustomMode2", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                }
            }



            section("Thermostats temperatures for $Home Mode"){
                if(Thermostats.size() > 0) {
                    input(name: "HSPH0", type: "decimal", title: "Set Heating temperature for ${Thermostats[0]} in $Home mode", required: true)
                    input(name: "CSPH0", type: "decimal", title: "Set Cooling temperature for ${Thermostats[0]} in $Home mode", required: true)
                }
                if(Thermostats.size() > 1) {
                    input(name: "HSPH1", type: "decimal", title: "Set Heating temperature for ${Thermostats[1]} in $Home mode", required: true)
                    input(name: "CSPH1", type: "decimal", title: "Set Cooling temperature for ${Thermostats[1]} in $Home mode", required: true)
                }
                if(Thermostats.size() > 2) {     
                    input(name: "HSPH2", type: "decimal", title: "Set Heating temperature for ${Thermostats[2]} in $Home mode", required: true)
                    input(name: "CSPH2", type: "decimal", title: "Set Cooling temperature for ${Thermostats[2]} in $Home mode", required: true)                      
                }
                if(Thermostats.size() >= 3) {   
                    input(name: "HSPH3", type: "decimal", title: "Set Heating temperature for ${Thermostats[3]} in $Home mode", required: true)
                    input(name: "CSPH3", type: "decimal", title: "Set Cooling temperature for ${Thermostats[3]} in $Home mode", required: true)
                }
            }
            section("Thermostats temperatures for $Night Mode"){
                if(Thermostats.size() > 0) {
                    input(name: "HSPN0", type: "decimal", title: "Set Heating temperature for ${Thermostats[0]} in $Night mode", required: true)
                    input(name: "CSPN0", type: "decimal", title: "Set Cooling temperature for ${Thermostats[0]} in $Night mode", required: true)
                }
                if(Thermostats.size() > 1) {
                    input(name: "HSPN1", type: "decimal", title: "Set Heating temperature for ${Thermostats[1]} in $Night mode", required: true)
                    input(name: "CSPN1", type: "decimal", title: "Set Cooling temperature for ${Thermostats[1]} in $Night mode", required: true)
                }
                if(Thermostats.size() > 2) {     
                    input(name: "HSPN2", type: "decimal", title: "Set Heating temperature for ${Thermostats[2]} in $Night mode", required: true)
                    input(name: "CSPN2", type: "decimal", title: "Set Cooling temperature for ${Thermostats[2]} in $Night mode", required: true)                      
                }
                if(Thermostats.size() >= 3) {   
                    input(name: "HSPN3", type: "decimal", title: "Set Heating temperature for ${Thermostats[3]} in $Night mode", required: true)
                    input(name: "CSPN3", type: "decimal", title: "Set Cooling temperature for ${Thermostats[3]} in $Night mode", required: true)
                }
            }
            section("Thermostats temperatures for $Away Mode"){   
                paragraph "these values apply to all thermostats evenly"
                input(name: "HSPA", type: "decimal", title: "Set Heating temperature for $Away mode", required: true)
                input(name: "CSPA", type: "decimal", title: "Set Cooling temperature for $Away mode", required: true)
            }

            if(Moremodes){
                section("$CustomMode1 Mode"){
                    if(Thermostats.size() > 0) {
                        input(name: "HSPCust1_T0", type: "decimal", title: "Set Heating temperature for ${Thermostats[0]} in $CustomMode1 mode", required: true)
                        input(name: "CSPCust1_T0", type: "decimal", title: "Set Cooling temperature for ${Thermostats[0]} in $CustomMode1 mode", required: true)
                    }
                    if(Thermostats.size() > 1) {
                        input(name: "HSPCust1_T1", type: "decimal", title: "Set Heating temperature for ${Thermostats[1]} in $CustomMode1 mode", required: true)
                        input(name: "CSPCust1_T1", type: "decimal", title: "Set Cooling temperature for ${Thermostats[1]} in $CustomMode1 mode", required: true)
                    }
                    if(Thermostats.size() > 2) {     
                        input(name: "HSPCust1_T2", type: "decimal", title: "Set Heating temperature for ${Thermostats[2]} in $CustomMode1 mode", required: true)
                        input(name: "CSPCust1_T2", type: "decimal", title: "Set Cooling temperature for ${Thermostats[2]} in $CustomMode1 mode", required: true)                      
                    }
                    if(Thermostats.size() >= 3) {   
                        input(name: "HSPCust1_T3", type: "decimal", title: "Set Heating temperature for ${Thermostats[3]} in $CustomMode1 mode", required: true)
                        input(name: "CSPCust1_T3", type: "decimal", title: "Set Cooling temperature for ${Thermostats[3]} in $CustomMode1 mode", required: true)
                    }
                }
                if(CustomMode2){
                    section("$CustomMode2 Mode"){
                        if(HowMany >= 1) {
                            input(name: "HSPCust2_T1", type: "decimal", title: "Set Heating temperature for ${Thermostats[0]} in $CustomMode2 mode", required: true)
                            input(name: "CSPCust2_T1", type: "decimal", title: "Set Cooling temperature for ${Thermostats[0]} in $CustomMode2 mode", required: true)
                        }
                        if(HowMany >= 2) {
                            input(name: "HSPCust2_T2", type: "decimal", title: "Set Heating temperature for ${Thermostats[1]} in $CustomMode2 mode", required: true)
                            input(name: "CSPCust2_T2", type: "decimal", title: "Set Cooling temperature for ${Thermostats[1]} in $CustomMode2 mode", required: true)
                        }
                        if(HowMany >= 3) {     
                            input(name: "HSPCust2_T3", type: "decimal", title: "Set Heating temperature for ${Thermostats[2]} in $CustomMode2 mode", required: true)
                            input(name: "CSPCust2_T3", type: "decimal", title: "Set Cooling temperature for ${Thermostats[2]} in $CustomMode2 mode", required: true)
                        }
                        if(HowMany == 4) {   
                            input(name: "HSPCust2_T4", type: "decimal", title: "Set Heating temperature for ${Thermostats[3]} in $CustomMode2 mode", required: true)
                            input(name: "CSPCust2_T4", type: "decimal", title: "Set Cooling temperature for ${Thermostats[3]} in $CustomMode2 mode", required: true)
                        }
                    }
                }
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
            if(ContactAndSwitch){
                input(name: "ToggleBack", type: "bool", title: "Turn $ContactAndSwitch back on once all windows are closed?", default: true, submitOnChange: true)
                if(ToggleBack){
                    input(name: "SwitchIfMode", type: "bool", title: "Keep $ContactAndSwitch off at all times when in certain modes", default: false, submitOnChange: true)
                    if(SwitchIfMode){
                        input(name: "SwitchMode", type : "mode", title: "Select modes", multiple: true, required: true, submitOnChange: true)
                    }
                }
            }
            if(Maincontacts){
                input(name: "TimeBeforeClosing", type: "number", title: "Turn off units after this amount of time", required: false, description: "time in seconds", uninstall: true, install: true)
                input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "Enter a safety temperature value")
                input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true, submitOnChange: true)			
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
                  description: "select which unit ")

            if(NoTurnOffOnContact){
                def XtraTempSensorColl = XtraTempSensor.collect{ it.toString() }
                def NoTurnOffOnContactColl = NoTurnOffOnContact.collect{ it.toString() }
                def Intersection = XtraTempSensorColl.intersect(NoTurnOffOnContactColl)
                log.debug "Intersection: ${Intersection}"

                def Same = Intersection.size() != 0
                log.debug "SAME = $Same"
                if(Same){
                    log.debug "YES"
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
                    required: false, 
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
If you do not have an outside temperature measurment device, you can 
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
                    log.debug "SensorIntersection: ${SensorIntersection}"
                    if(SensorIntersection.size() != 0){
                        log.debug "WRONG DEVICE"
                        paragraph "WRONG DEVICE! You selected a contact that is already being used by this app. Please select a different contact or uncheck this option" 
                    }
                    input(name: "HSPSetBedSensor", type: "decimal", title: "Set Heating temperature", required: true)
                    input(name: "CSPSetBedSensor", type: "decimal", title: "Set Cooling temperature", required: true)
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
            input(name: "Actuators", type: "capability.switch", required: false, multiple: true, title: "select some fan or windows switches that you want to control with outside's temperature", submitOnChange: true)
            def HasStop = Actuators?.hasCommand("stop") || Actuators?.hasCommand("Stop") 
            if(HasStop){
                input(name: "OperatingTime", type: "number", title: "Should I stop opening operation after this amount of time?", required: false, description: "time in seconds")
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
If within margin then open windows / turn on fans, but not if inside's temp is lower than heat setting minus offset. 
Reference measurment is taken from $XtraTempSensor. You may also chose to open windows at full lenght whenever outside's temperature 
allows for it, instead of only when cooling is required (see below)"""
                input(name: "OpenWhenEverPermitted", type: "bool", title: "Open in full whenever it is nice outside?", default: false, submitOnChange: true)
                input(name: "OpenInfullWhenAway", type: "bool", title: "Open in full whenever you're away and it's not cold outside?", default: false, submitOnChange: true)
                if(OpenInfullWhenAway){
                    paragraph "Be aware that you have selected $Away as your AWAY mode. Make sure this is the mode for when there's nobody home"
                }
            }
        }
        section("Micro Location"){
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}

            input(name: "thermMotion", type: "enum",
                  title: "Use motion sensors to adjust Thermostats settings when inactive", 
                  description: "select which thermostats you want to control this way",
                  options: MyThermostats.sort(),
                  multiple: true,
                  required: false, 
                  submitOnChange: true
                 )
            if(thermMotion){
                if(thermMotion.size() > 0){    
                    input(name: "MotionSensor_1", type: "capability.motionSensor", multiple: true, title: "", description: "Select a motion sensor for ${thermMotion[0]}", required: false, submitOnChange: true)
                    if(MotionSensor_1){
                        input(name: "MotionModesT1", type: "mode", title: "Use $MotionSensor_1 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                        input(name: "HeatNoMotion1", type: "decimal", title: "Substract this amount of degrees to ${thermMotion[0]} heat setting", required: true)
                        input(name: "CoolNoMotion1", type: "decimal", title: "Add this amount of degrees to ${thermMotion[0]} cooling setting", required: true)  
                    }
                }
                if(thermMotion.size() >= 1) {
                    input(name: "MotionSensor_2", type: "capability.motionSensor", multiple: true, description: "Select a motion sensor for ${thermMotion[1]}", required: false, submitOnChange: true)
                    if(MotionSensor_2){
                        input(name: "MotionModesT2", type: "mode", title: "Use $MotionSensor_2 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                        input(name: "HeatNoMotion2", type: "decimal", title: "Substrat this amount of degrees to ${thermMotion[1]} heat setting", required: true)
                        input(name: "CoolNoMotion2", type: "decimal", title: "Add this amount of degrees to ${thermMotion[1]} cooling setting", required: true)                
                    }
                }
                if(thermMotion.size() >= 2) {
                    input(name: "MotionSensor_3", type: "capability.motionSensor", multiple: true, description: "Select a motion sensor for ${thermMotion[2]}", required: false, submitOnChange: true)            
                    if(MotionSensor_2){ 
                        input(name: "MotionModesT3", type: "mode", title: "Use $MotionSensor_3 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                        input(name: "HeatNoMotion3", type: "decimal", title: "Substrat this amount of degrees to ${thermMotion[2]} heat setting", required: true)
                        input(name: "CoolNoMotion3", type: "decimal", title: "Add this amount of degrees to ${thermMotion[2]} cooling setting", required: true)        
                    }
                }
                input (name:"minutesMotion", type:"number", title: "For how long there must be no motion for those settings to apply? ", 
                       range: "2..999", 
                       description: "time in minutes",
                       required: false)
                paragraph "this timer will apply to each of the selected motion sensors"
            }
        }
    }
}

// install and updated
def installed() {	 
    // //log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler

    // default values to avoid NullPointer // must be set as such only for new installation not in init or updated  

    state.humidity = HumidityTolerance - 1

    //log.debug "state.humidity is $state.humidity (updated() loop)"
    state.wind = 4
    state.FeelsLike = OutsideSensor?.latestValue("feelsLike")

    state.OpenByApp = true
    state.ClosedByApp = true // these values must not be reset with updated, only here and modeCHangeHandler

    // first default values to be set to any suitable value so it doesn't crash with null value 
    // they will be updated within seconds with user's settings 
    state.newValueT1CSP = 75
    state.newValueT1HSP = 75
    state.newValueT2CSP = 75
    state.newValueT2HSP = 75
    state.newValueT2CSP = 75
    state.newValueT2HSP = 75


    init()
}
def updated() {
    state.modeStartTime = now() 

    state.LastTimeMessageSent = now() // for causes of !OkToOpen message

    log.info "updated with settings = $settings"


    unsubscribe()
    unschedule()
    EndEvalTRUE() 
    OverrideReset()
    init()
}
def init() {

    state.now = now()

    subscribe(Maincontacts, "contact.open", contactHandlerOpen)
    subscribe(Maincontacts, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(ContactException, "contact.open", contactExceptionHandlerOpen)
        subscribe(ContactException, "contact.closed", contactExceptionHandlerClosed)
        //  subscribe(ContactException, "contact.open", contactHandlerOpen)
        // subscribe(ContactException, "contact.closed", contactHandlerClosed)
        //log.debug "subscribed ContactException to ContactException Handler"     
    }

    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    if(BedSensor){
        subscribe(BedSensor, "contact.open", BedSensorHandler)
        subscribe(BedSensor, "contact.closed", BedSensorHandler)

    }

    subscribe(OutsideSensor, "temperature", temperatureHandler)
    def hasHumidity = OutsideSensor.hasAttribute("humidity")
    //log.debug "hasHumidity is $hasHumidity"
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


    def loopV = 0
    def t = Thermostats.size()
    for(t > 0; loopV < t; loopV++){

        hasHumidity = Thermostats[loopV].hasAttribute("humidity")
        if(hasHumidity){
            subscribe(Thermostats[loopV], "humidity", InsideHumidityHandler)
        }
        subscribe(Thermostats[loopV], "heatingSetpoint", setpointHandler)
        subscribe(Thermostats[loopV], "coolingSetpoint", setpointHandler)
        subscribe(Thermostats[loopV], "temperature", temperatureHandler)
        subscribe(Thermostats[loopV], "thermostatMode", ThermostatSwitchHandler)

        log.debug "init therm ${Thermostats[loopV]} = loop $loopV"
    }


    if(ExceptionSW){
        if(CtrlSwt){
            subscribe(CtrlSwt, "switch", switchHandler)
        }
    }
    if(ContactAndSwitch){

        subscribe(ContactAndSwitch, "switch", ContactAndSwitchHandler)

    }
    if(AltSensor){

        if(Sensor_1){
            subscribe(Sensor_1, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_1"
        }
        if(Sensor_2){
            subscribe(Sensor_2, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_2"
        }
        if(Sensor_3){
            subscribe(Sensor_3, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_3"
        }
    }

    if(MotionSensor_1){
        subscribe(MotionSensor_1, "motion", motionSensorHandler)
        log.debug "$MotionSensor_1 subscribed to evt"
    }
    else {
        log.debug "no subscription for $MotionSensor_1"
    }
    if(MotionSensor_2){
        subscribe(MotionSensor_2, "motion", motionSensorHandler)
        log.debug "$MotionSensor_2 subscribed to evt"
    }
    else {
        log.debug "no subscription for $MotionSensor_2"
    }
    if(MotionSensor_3){
        subscribe(MotionSensor_3, "motion", motionSensorHandler)
        log.debug "$MotionSensor_3 subscribed to evt"
    }
    else {
        log.debug "no subscription for $MotionSensor_3"
    }

    if(Actuators){
        subscribe(Actuators, "switch", switchHandler)
    }
    if(ActuatorException){
        subscribe(ActuatorException, "switch", switchHandler)
    }


    state.doorsAreOpen = false


    state.messageOkToOpenCausesSent = 0

    def ContactsClosed = AllContactsAreClosed()
    //log.debug "enter updated, state: $state"  


    state.messageSent = 0

    state.locationModeChange = true 



    log.debug "Number of Thermostats Selected by User : ${Thermostats.size()} [init]"

    runIn(10, resetLocationChangeVariable)

    // false positive overrides management 


    schedules()


}

// MAIN LOOP
def DoNotTurnBackOnMode(){
    def result = false
    def inMode = location.currentMode in SwitchMode

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
    log.debug "DoNotTurnBackOnMode = $result"
    return result 
}
def Evaluate(){
    EndEvalFALSE() ///// 

    def CurrMode = location.currentMode
    log.debug "Home is in $CurrMode"

    // motion? 
    def InMotionModes = CurrMode in MotionModes  
    def AccountForMotion = MotionSensor != null 
    //log.debug "InMotionModes?($InMotionModes), AccountForMotion?($AccountForMotion)"

    def doorsOk = AllContactsAreClosed()
    def ContactExceptionIsClosed = ExcepContactsClosed()
    log.debug "doorsOk?($doorsOk), ContactExceptionIsClosed?($ContactExceptionIsClosed)"

    def CurrentSwitch = ContactAndSwitch.currentSwitch
    def SwState = CurrentSwitch.findAll { switchVal ->
        switchVal == "off" ? true : false
    }
    log.trace "SwState.size() = ${SwState.size()}, ToggleBack = $ToggleBack, doorsOk = $doorsOk, state.turnedOffByApp = $state.turnedOffByApp"
    def contactClosed = false
    def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
    log.debug "InExceptionContactMode = $InExceptionContactMode DoNotTurnOffModes = $DoNotTurnOffModes"

    if(ContactException && FollowException && InExceptionContactMode){
        contactClosed = ExcepContactsClosed() 
    }
    else{
        contactClosed = AllContactsAreClosed()
    }
    log.debug "contactClosed = $contactClosed"
    def inAwayMode = CurrMode in Away

    if(!DoNotTurnBackOnMode() && contactClosed && ToggleBack){  //  && state.turnedOffByApp == true){  
        if(SwState.size() != 0){
            ContactAndSwitch.on()
            log.debug "$ContactAndSwitch TURNED ON"
            state.turnedOffByApp == false
        }else {
            log.debug "$ContactAndSwitch already on"
        }
    }
    else if(DoNotTurnBackOnMode()){
        if(SwState.size() == 0){
            ContactAndSwitch.off()
            log.debug "$ContactAndSwitch TURNED OFF"
            state.turnedOffByApp == true
        }
        else {
            log.debug "$ContactAndSwitch already off"
        }        
    }
    else if(!contactClosed || inAwayMode){
        if(SwState.size() == 0){
            ContactAndSwitch.off()
        }
    }

    def outsideTemp = OutsideSensor?.currentValue("temperature") //as double
    //outsideTemp = Double.parseDouble(outsideTemp)
    outsideTemp = outsideTemp.toInteger()
    def Outside = outsideTemp as int


        if(state.handlerrunning == false || state.recentModeChange == true){
            if(doorsOk || ContactExceptionIsClosed ){


                log.trace """
Override (AppMgt) modes list: $state.AppMgtList"""


                def CurrTemp_Alt1 = Sensor_1?.currentValue("temperature")
                def CurrTemp_Alt2 = Sensor_2?.currentValue("temperature")
                def CurrTemp_Alt3 = Sensor_3?.currentValue("temperature")

                def CurrTempList_Alt = [CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3]
                log.debug "CurrTempList_Alt = $CurrTempList_Alt"

                def AltSensorDevicesList =  [Sensor_1, Sensor_2, Sensor_3]
                def i = AltSensorDevicesList.size()
                def loopV = 0
                /*

for(i > 0; loopV < i; loopV++) {
if(AltSensorDevicesList[loopV] == null){
remove(AltSensorDevicesList[loopV])
}
}*/

                def AltSensorBoolList = [false, false, false]
                log.debug "BEFORE: AltSensorBoolList = $AltSensorBoolList, AltSensorDevicesList = $AltSensorDevicesList"

                for (i > 0; loopV < i; loopV++) {
                    if(AltSensorDevicesList[loopV] != null){
                        AltSensorBoolList[loopV] = true
                    }
                    log.debug "AltSensorBoolList[${loopV}] set to true"
                }

                log.debug "AFTER: AltSensorBoolList = $AltSensorBoolList, AltSensorDevicesList = $AltSensorDevicesList"

                def inCtrlSwtchMode = CurrMode in ["$Home", "$Night", "${CustomMode1[0]}", "${CustomMode2[0]}"]

                if(inCtrlSwtchMode){
                    def SwitchesOnTest = CtrlSwt?.currentValue("switch") == "on"
                    SwitchesOn = SwitchesOnTest && ContactExceptionIsClosed
                    //log.debug "SwitchesOn($SwitchesOn)"
                }


                def HSPSet = 0

                //log.debug "CSP LIST is $CSP"
                def CSPSet = 0
                def LatestThermostatMode = null

                def ThermDeviceList = Thermostats

                def MotionSensorList = [ "$MotionSensor_1", "$MotionSensor_2", "$MotionSensor_3"]
                def MotionSensor = 0
                def MotionModesList = [MotionModesT1, MotionModesT2 , MotionModesT3]
                def MotionModes = null

                def HeatNoMotionList = [HeatNoMotion1, HeatNoMotion2, HeatNoMotion3]
                def HeatNoMotionVal = 0
                def CoolNoMotionList  = [CoolNoMotion1, CoolNoMotion2, CoolNoMotion3]
                def CoolNoMotionVal = 0


                def OutsideTempHighThres = ExceptACModes()

                def CurrTempDevice = 0

                def loopValue = 0
                def t = Thermostats.size()

                ///// FOR LOOP
                for(t > 0; loopValue < t; loopValue++){

                    log.debug "FOR LOOP $loopValue"

                    def ThermSet = Thermostats[loopValue]
                    def CurrTemp = ThermSet.currentValue("temperature") 
                    def ThermState = ThermSet.currentValue("thermostatMode")  


                    def ModeList = ["$Home": "H", "$Night": "N", "$Away": "A", "${CustomMode1[0]}": "Cust1_T", "${CustomMode2[0]}": "Cust2_T" ]   
                    def ModeValue = ModeList.find{ it.key == "$CurrMode"}
                    ModeValue = ModeValue.value
                    log.debug """ThermState = $ThermState || ModeValue = $ModeValue || CurrMode = $CurrMode || 
CustomMode1 = $CustomMode1 || CustomMode2 = $CustomMode2 
|| HSPCust1_T = HSPCust1_T${[loopValue.toString()]}"""


                    def HSP = "HSP${ModeValue}${loopValue.toString()}"

                    if(ModeValue == "A"){
                        HSP = "HSPA"
                    }
                    log.debug "HSP is $HSP"
                    HSPSet = settings.find {it.key == "$HSP"} // retrieve from settings
                    HSPSet = HSPSet.value
                    log.debug "HSPSet for $ThermSet is $HSPSet "
                    HSPSet = HSPSet.toInteger()

                    def CSP = "CSP${ModeValue}${loopValue.toString()}"

                    if(ModeValue == "A"){
                        CSP = "CSPA"
                    }
                    CSPSet = settings.find {it.key == "$CSP"} // retrieve from settings
                    CSPSet = CSPSet.value
                    log.debug "CSPSet for $ThermSet is $CSPSet"
                    CSPSet = CSPSet.toInteger()

                    def AppMgt = state.AppMgtList[loopValue]
                    log.debug "AppMgt = $AppMgt"

                    def Inside = ThermSet.currentValue("temperature")
                    //Inside = Double.parseDouble(Inside)
                    //Inside = Inside.toInteger()

                    state.Inside = Inside
                    //def humidity = OutsideSensor?.latestValue("humidity")
                    def humidity = OutsideSensor.currentValue("temperature")
                    //humidity = Double.parseDouble(humidity)
                    //humidity = humidity.toInteger()

                    def TooHumid = humidity > HumidityTolerance && Outside > Inside + 3

                    def INSIDEhumidity = ThermSet.latestValue("humidity")   
                    //INSIDEhumidity = INSIDEhumidity?.toInteger()
                    def TooHumidINSIDE =  INSIDEhumidity > HumidityTolerance

                    log.trace """        
ThermsInvolved = ${Thermostats.size()} 
loop($loopValue) 
AppMgtList = $state.AppMgtList
AppMgt = $AppMgt
"""        

                    state.ThermSet = ThermSet as String
                    log.debug "--------------------"

                    state.EventAtTempLoop = ThermSet as String // used for false override prevention
                    log.debug "ThermSet = $ThermSet - state.EventAtTempLoop = $state.EventAtTempLoop"

                    def AltSensor = AltSensorBoolList[loopValue] 

                    if(AltSensor){

                        CurrTemp = CurrTempList_Alt[loopValue] 

                        CurrTempDevice = AltSensorDevicesList[loopValue] 
                        log.debug "$CurrTempDevice selected as CurrTemp source for $ThermSet and it returns a temperature of $CurrTemp F"
                    }
                    else {
                        //CurrTemp = CurrTempList[loopValue] 
                        //CurrTempDevice = AltSensorDevicesList[loopValue]
                        log.debug " $ThermSet returns a temperature of $CurrTemp F"
                    }

                    //CurrTemp = Double.parseDouble(CurrTemp)
                    //CurrTemp = CurrTemp.toInteger()

                    log.debug "CurrTemp = $CurrTemp ($ThermSet)"
                    state.CurrTemp = CurrTemp

                    // motion management
                    MotionSensor = MotionSensorList[loopValue]
                    state.MotionSensor = MotionSensor
                    MotionModes = MotionModesList[loopValue]

                    HeatNoMotionVal = HeatNoMotionList[loopValue]
                    CoolNoMotionVal = CoolNoMotionList[loopValue]
                    def ActiveList = MotionTest() 
                    def Active = ActiveList[loopValue]
                    log.debug """
ActiveList = $ActiveList
MotionSensorList = $MotionSensorList
Active?(from List) for $ThermSet && $MotionSensor = $Active
"""


                    def defaultCSPSet = CSPSet // recording this default value so if A.I. brings setpoint too low, it'll be recovered
                    def defaultHSPSet = HSPSet // same but with heat

                    //CSPSet = CSPSet.toInteger()
                    state.CSPSet = CSPSet
                    state.HSPSet = HSPSet


                    log.debug """
Current Temperature Inside = $Inside
//log.debug "ShouldHeat = $ShouldHeat
"""

                    //modify with presence/motion in the room



                    if(adjustments == "Yes, use a linear variation"){
                        // linear function for Cooling
                        def xa = 75	//outside temp a
                        def ya = CSPSet // desired cooling temp a 

                        def xb = 100 		//outside temp b
                        def yb = CSPSet + 5  // desired cooling temp b  

                        // take humidity into account
                        // if outside humidity is higher than .... 
                        if(TooHumid){
                            xa = 75				//outside temp a LESS VARIATION WHEN HUMID
                            ya = CSPSet	   // desired cooling temp a 
                            xb = 100 //outside temp b
                            yb = CSPSet + 2 // desired cooling temp b  LESS VARIATION WHEN HUMID
                        }

                        def coef = (yb-ya)/(xb-xa)

                        def b = ya - coef * xa // solution to ya = coef*xa + b // CSPSet = coef*outsideTemp + b

                        //CSPSet - (coef * outsideTemp) 
                        log.info "b is: $b ---------------------------------------"
                        CSPSet = coef*outsideTemp + b as double


                            } 
                    else if(adjustments == "Yes, but use a logarithmic variation"){
                        // logarithmic treatment 

                        /* concept: x = log(72)75   to what power (that is to say "x") do I have to raise 72, to get to 75?

logb(n) = loge(n) / loge(b)
Where log can be a logarithm function in any base, n is the number and b is the base. For example, in Java this will find the base-2 logarithm of 256:

Math.log(256) / Math.log(2)
=> 8.0
*/
                        //outsideTemp = 90 // for test only 
                        CSPSet = (Math.log(outsideTemp) / Math.log(CSPSet)) * CSPSet
                        log.debug "Logarithmic CSPSet = $CSPSet"
                        //CSPSet = Math.round(CSPSet)
                        CSPSet = CSPSet.toInteger()
                        log.debug "Integer CSPSet = $CSPSet"


                        // end of algebraic adjustments        


                        if(AccountForMotion && InMotionModes && AppMgt){

                            if(!Active){
                                // record algebraic CSPSet for debug purpose
                                def algebraicCSPSet = CSPSet 
                                // log.info "$ThermSet default Cool: $CSPSet and default heat: $HSPSet "
                                CSPSet = CSPSet + HeatNoMotionVal 
                                HSPSet = HSPSet - CoolNoMotionVal

                                log.trace "NO MOTION so $ThermSet CSP, which was $defaultCSPSet, then (if algebra) $algebraicCSPSet, is now set to $CSPSet and HSP was $defaultHSPSet and is now set to $HSPSet"

                            }
                            else {

                                log.debug "There's motion in ${ThermSet}'s room (main loop)"
                            }
                        }

                        if(TooHumid && Inside - 2 >= outsideTemp && Active){
                            CSPSet = CSPSet - 1 
                            log.debug "Substracting 2 to new CSP because it is too humid OUTSIDE"
                        }
                        else {
                            log.debug "not too humid outside"
                        }

                        if(TooHumidINSIDE && Inside - 2 >= outsideTemp && Active){
                            CSPSet = CSPSet - 1 
                            log.debug "Substracting 1 to new CSP because it is too humid INSIDE"
                        }
                        else {
                            log.debug "not too humid inside"
                        }

                        // no lower than defaultCSPSet 
                        log.debug "Calculated CSPSet = $CSPSet, defaultCSPSet = $defaultCSPSet (loop $loopValue)"
                        if(CSPSet < defaultCSPSet){

                            log.info """CurrTemp at ${ThermSet} is: $CurrTemp. CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp
But, because CSPSet is lower than default value ($defaultCSPSet), default settings are maintained"""
                            CSPSet = defaultCSPSet
                        }
                        else {

                            log.info "CurrTemp at ${ThermSet} is: $CurrTemp  CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"
                        }
                    }


                    // evaluate needs

                    def WarmOutside = outsideTemp >= (CSPSet - 1)
                    def WarmInside = CurrTemp - 1 > CSPSet
                    log.debug "WarmOutside = $WarmOutside, WarmInside = $WarmInside"
                    def ShouldCoolWithAC = WarmInside || WarmOutside
                    log.debug "$ThermSet ShouldCoolWithAC = $ShouldCoolWithAC (before other criteria loop $loopValue)"

                    def ShouldHeat = outsideTemp < OutsideTempLowThres && CurrTemp <= HSPSet

                    if((WarmInside || tooHumidINSIDE) && !ShouldHeat){
                        ShouldCoolWithAC = true
                        log.debug "ShouldCoolWithAC set to true loop $loopValue"
                    }
                    state.ShouldCoolWithAC = ShouldCoolWithAC
                    state.ShouldHeat = ShouldHeat

                    log.debug """
ShouldCoolWithAC = $ShouldCoolWithAC 
ShouldHeat = $ShouldHeat 
WarmOutside = $WarmOutside 
WarmInside = $WarmInside
"""       

                    def ThisIsExceptionTherm =  false

                    if(NoTurnOffOnContact){
                        ThisIsExceptionTherm = "${ThermSet}" == "${NoTurnOffOnContact}"
                        log.debug "This is Exception Thermostat"

                    }
                    else {
                        ThisIsExceptionTherm =  false

                        log.debug "No exception contact selected by user, ThisIsExceptionTherm set to false by default"
                    }

                    if(ExceptionSW && ThermSet == "$ExceptionSwTherm" && SwitchesOn){
                        HSPSet = HSPSet + AddDegrees
                        CSPSet = CSPSet + SubDegrees

                        log.debug "$ThermSet SetPoints ExceptionSW active"                                
                    }

                    // Now, before sending any command, pull current setpoint and compare to avoid redundencies
                    def CurrentCoolingSetPoint = ThermSet.currentValue("coolingSetpoint") 
                    def CurrentHeatingSetPoint = ThermSet.currentValue("heatingSetpoint") 


                    def CSPok = CurrentCoolingSetPoint == CSPSet
                    def HSPok = CurrentHeatingSetPoint == HSPSet


                    if(KeepACon && "${ThermSet}" == "${ThermContact}"){
                        def BedSensorResults = BedSensorStatus()
                        def NowBedisClosed = BedSensorResults[0]
                        def NowBedisOpen = BedSensorResults[1]
                        log.debug """
NowBedisClosed = $NowBedisClosed, 
NowBedisOpen = $NowBedisOpen, """

                        if(NowBedisClosed) {
                            log.debug "$BedSensor closed, applying settings accordingly"  
                            CSPSet = CSPSetBedSensor.toInteger()
                            HSPSet = HSPSetBedSensor.toInteger()
                            CSPok = CurrentCoolingSetPoint == CSPSet
                            HSPok = CurrentHeatingSetPoint == HSPSet
                            def needCool = CurrTemp > CSPSetBedSensor 
                            def needHeat = CurrTemp < HSPSetBedSensor 
                            log.trace """
BED SENSOR DEBUG

needHeat = $needHeat, 
needCool = $needCool, 

CurrTemp = $CurrTemp, 
CSPSetBedSensor = $CSPSetBedSensor
HSPSetBedSensor = $HSPSetBedSensor
CSPok = $CSPok
HSPok = $HSPok

"""

                            if(needCool){
                                if(!CSPok){
                                    ThermSet.setCoolingSetpoint(CSPSet)
                                    log.debug "$ThermSet CSP set to $CSPSet -- Bed Sensor" 
                                }
                                if(ThermSet != "cool"){
                                    ThermSet.setThermostatMode("cool") 
                                    log.debug "$ThermSet set to cool -- Bed Sensor"
                                }      
                            }
                            else if(needHeat){
                                if(!HSPok){
                                    ThermSet.setHeatingSetpoint(HSPSet)
                                    log.debug "$ThermSet HSP set to $CSPSet -- Bed Sensor" 
                                }
                                if(ThermSet != "heat" ){
                                    ThermSet.setThermostatMode("heat") 
                                    log.debug "$ThermSet set to heat -- Bed Sensor"
                                }
                            }
                            else if(ThermSet != "auto"){
                                ThermSet.setThermostatMode("auto") 
                                log.debug "$ThermSet set to auto -- Bed Sensor"
                                if(!CSPok){
                                    ThermSet.setCoolingSetpoint(CSPSet)
                                }
                                if(!HSPok){
                                    ThermSet.setHeatingSetpoint(HSPSet)
                                }
                            }
                        }
                    }

                    CSPok = CurrentCoolingSetPoint == CSPSet
                    HSPok = CurrentHeatingSetPoint == HSPSet

                    log.info "-- End of Temperatures Evals for $ThermSet" 

                    // for false overrides prevention
                    // and for back to normal action
                    // if user sets unit back to its currently calculated 
                    // value, then the app will end the override

                    if(loopValue == 1){
                        state.newValueT1CSP = CSPSet 
                        state.newValueT1HSP = HSPSet

                    }    
                    if(loopValue == 2){
                        state.newValueT2CSP = CSPSet 
                        state.newValueT2HSP = HSPSet

                    }
                    if(loopValue == 3){
                        state.newValueT3CSP = CSPSet 
                        state.newValueT3HSP = HSPSet  

                    }
                    if(loopValue == 4){
                        state.newValueT4CSP = CSPSet 
                        state.newValueT4HSP = HSPSet  

                    }

                    log.debug """
InMotionModes?($InMotionModes)
AccountForMotion?($AccountForMotion)
Motion at $MotionSensor Active for the past $minutesMotion minutes?($Active)
FINAL CSPSet for $ThermSet = $CSPSet
ThisIsExceptionTherm is: $ThisIsExceptionTherm (${ThermSet} == ${NoTurnOffOnContact})
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


                    /////////////////////////MODIFICATIONS//////////////////////////yh


                    log.debug "doorsOk = $doorsOk, CSPok = $CSPok, HSPok = $HSPok, $ThermSet"

                    if(doorsOk || (ContactExceptionIsClosed && ThisIsExceptionTherm)){

                        def inAutoOrOff = ThermState in ["auto","off"]
                        log.debug "turnOffWhenReached = $turnOffWhenReached, $ThermSet is inAutoOrOff = $inAutoOrOff"

                        if(!ShouldCoolWithAC && !ShouldHeat && turnOffWhenReached){

                            if(AltSensor && (!turnOffWhenReached || turnOffWhenReached)){ 
                                if(!inAutoOrOff){
                                    state.LatestThermostatMode = "off"   
                                    // that's a "should be" value used to compare eventual manual setting to what "should be"
                                    // that's why it must be recoreded even during override mode
                                    //state.AppMgtList[loopValue] = true //override test value
                                    if(AppMgt){
                                        log.debug "$ThermSet TURNED OFF"  
                                        ThermSet.setThermostatMode("off") 
                                    }
                                    else {
                                        log.debug "$ThermSet in OVERRIDE MODE, Setting it to AUTO"
                                        if(ThermSet != "auto"){
                                            ThermSet.setThermostatMode("auto") 
                                        }
                                    }
                                }
                                else {
                                    log.debug "$ThermSet already set to off"
                                }
                            }
                            else if(turnOffWhenReached && !AltSensor){
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

                        else if(ShouldCoolWithAC || !CSPok){
                            // it may happen that old settings get stuck if estimate of shouldcool is false 
                            // so if no override but discrepancy between current csp and what should be
                            // go on
                            log.debug """ShouldCoolWithAC EVAL $loopValue AppMgt = $AppMgt
CurrentCoolingSetPoint == CSPSet ? ${CurrentCoolingSetPoint == CSPSet}"""

                            state.LatestThermostatMode = "cool"
                            if(AppMgt){
                                log.debug " $CurrentCoolingSetPoint == $CSPSet {CurrentCoolingSetPoint == CSPSet}?"
                                if(!CSPok){
                                    ThermSet.setCoolingSetpoint(CSPSet)
                                    log.debug "$ThermSet CSP set to $CSPSet" 
                                }
                                else{
                                    log.debug "Cooling SetPoint already set to $CSPSet for $ThermSet ($CSPSet == $CurrentCoolingSetPoint)"
                                }                   
                                if(ShouldCoolWithAC && ThermState != "cool"){  
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
                        else if(ShouldHeat || !HSPok){
                            state.LatestThermostatMode = "heat"
                            if(AppMgt){
                                //state.AppMgtList[loopValue] = true
                                if(CurrentHeatingSetPoint != HSPSet){
                                    //state.AppMgtList[loopValue] = true //override test value
                                    ThermSet.setHeatingSetpoint(HSPSet)
                                }
                                else { 
                                    log.debug "Heating SetPoint already set to $HSPSet for $ThermSet"
                                }

                                if(ShouldHeat && ThermState != "heat"){
                                    // ShouldHeat has to be rechecked here otherwise !HSPok might trigger heat while no need

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
                                log.debug "device already off"
                            }
                        }
                    }

                    log.info """
INFO : 
$ThermSet CSP should be : $CSPSet current CSP: $CurrentCoolingSetPoint
$ThermSet HSP should be : $HSPSet current HSP: $CurrentHeatingSetPoint
"""
                    log.trace " END OF FOR LOOP $loopValue" 
                }   
                // true end of  loop
            }
            else { 
                log.debug "not evaluating because some windows are open" 
                TurnOffThermostats()
                Thermostats.setThermostatMode("off") // temporary because of those idiots at smartthings who pushed a fucking stupid useless update that prevents status refresh
            }
        }
    else {
        log.debug "HANDLERS NOT DONE YET"
    }

    if(!doorsOk){
        state.thisIsWindowMgt = false
    }

    /// disabled FOR TESTS
    EndEvalTRUE()
}
def EndEvalTRUE(){
    state.EndEval = true
    log.info "state.EndEval = $state.EndEval"
}
def EndEvalFALSE(){
    state.EndEval = false
    log.info "state.EndEval = $state.EndEval"
}

//shoulds
def LatestThermMode(){
    // recording currently set values for when users want to end an override
    // by setting a unit back to its current default's values 
    // first, it happens that no command was ever sent since last updated()
    // or due to contacts sensors or due to previous overrides
    // so let's reuse the same conditions, 
    // but without the consecutive actions/commands
    // nor the state comparisons which are meant only to avoid redundent z-wave commands
    // so we can record just the ShouldBe values (i.e. what should be if there were no override)
    // which are used by ThermostatSwitchHandler and setpointHandler for override assertion

    def ShouldCoolWithAC = state.ShouldCoolWithAC
    def ShouldHeat = state.ShouldHeat
    def LatestThermMode = "null"
    def ThermSet = state.ThermSet

    if(!ShouldCoolWithAC && !ShouldHeat){ 
        if(!AltSensor){
            LatestThermMode = "off"             
        } 
        else {
            log.debug "$ThermSet managed by $CurrTempDevice, so it won't be recorded as ShouldBe = off"
        }
    }

    else if(ShouldCoolWithAC){                    
        LatestThermMode = "cool"                            
    }
    else if(ShouldHeat){
        LatestThermMode = "heat"
    }

    return LatestThermMode
}

// A.I. and micro location evt management
def motionSensorHandler(evt){
    log.debug "motion is $evt.value at $evt.device"
    if(state.EndEval == true){
        if(evt.value == "active"){ 
            Evaluate()
        }
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def HumidityHandler(evt){

    log.info "humidity value is ${evt?.value}%"
    state.humidity = evt.value

}
def InsideHumidityHandler(evt){
    log.info "INSIDE humidity value at $evt.device is ${evt.value}%"
    state.INSIDEhumidity = evt.value
}
def WindHandler(evt){

    log.info "Wind value is ${evt.value}mph"  

    state.wind = evt.value

}
def FeelsLikeHandler(evt){

    log.info "Currently, outside temperature feels like ${evt.value}F"  

    state.FeelsLike = evt.value

}

def switchHandler(evt){

    log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        state.exception = true
    } else {
        state.exception = false
    }

}
def ContactAndSwitchHandler(evt){
    log.debug "ContactAndSwitchHandler : ${evt.device} is ${evt.value}"

    if(evt.value == "off"){
        state.contactAndSwtchOff = true
    } else {
        state.contactAndSwtchOff = false
    }

}
def BedSensorHandler(evt){

    log.debug """$evt.device is $evt.value 
BedSensor is $BedSensor------------------------------------------------------------------------"""


    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }

}
def Timer() {
    def minutes = findFalseAlarmThreshold() 
    def deltaMinutes = minutes * 60000 as Long

    def start = new Date(now() - deltaMinutes)
    def end = new Date(now())
    def ContactsEvents = BedSensor?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    //BedSensor[0].statesBetween("contact", start, end, [max: 200]))

    log.debug """
Found ${ContactsEvents.size()} events in the last $minutes minutes"
"""
    def size = ContactsEvents.size()
    return size

}
private findFalseAlarmThreshold() {
    // In Groovy, the return statement is implied, and not required.
    // We check to see if the variable we set in the preferences
    // is defined and non-empty, and if it is, return it.  Otherwise,
    // return our default value of 10
    (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 2
}
def BedSensorStatus(){
    def NowOpen = true // has to be true by default in case no contacts selected
    def BedSensorAreClosed = false // has to be false by default in case no contacts selected
    if(BedSensor){

        def CurrentContacts = BedSensor.currentValue("contact")    
        def ClosedContacts = CurrentContacts.findAll { val ->
            val == "closed" ? true : false}

        if(ClosedContacts.size() == BedSensor.size()){
            BedSensorAreClosed = true
        }

        log.debug "${ClosedContacts.size()} sensors out of ${BedSensor.size()} are closed SO BedSensorAreClosed = $BedSensorAreClosed"
        def ContactsEventsSize = Timer()

        def Open = BedSensor.findAll{it.currentValue("contact") == "open"}

        boolean isOpen = Open.size() != 0 && !BedSensorAreClosed
        log.debug "Open = ${Open}, isOpen = $isOpen"

        if(isOpen && ContactsEventsSize > 1){
            NowOpen = false
            log.debug "too many events in the last couple minutes"
        }
        else if (isOpen && ContactsEventsSize == 1){  

            // declare an integer value for the thermostat which has had its values modified
            def Map = [:]
            log.debug "Map = $Map ----------------------------------------------"
            Map = Thermostats.collectEntries{[it.name, it.val]}
            log.debug "Map = $Map"
            //["${Thermostats[0]}": "0" , "${Thermostats[1]}": 1, "${Thermostats[2]}": "2", "${Thermostats[3]}": "3"]

            def KeyValueForThisTherm = Map.find { it.key == "$ThermContact"}
            log.info "device is ------------------- $KeyValueForThisTherm.value"
            def ThermNumber = KeyValueForThisTherm.value
            ThermNumber = KeyValueForThisTherm.value.toInteger()

            state.AppMgtList[ThermNumber] = true

            NowOpen = true
            log.debug "Only one event within the last couple minutes"

        }

    }


    log.debug "BedSensorAreClosed = $BedSensorAreClosed, NowOpen = $NowOpen"
    return [BedSensorAreClosed, NowOpen]

}

// Main events management

def temperatureHandler(evt) {

    handlerrunningTRUE()

    def doorsOk = AllContactsAreClosed()

    if(evt.device == XtraTempSensor) {
        state.Inside = evt.value

    }

    def currentTemp = state.Inside
    log.info """
current temperature value for $evt.device is $evt.value
Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp
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
                Actuators?.off()
                ActuatorException?.off()
                // allow for user to reopen them if they want to. 
                state.windowswereopenandclosedalready = true // windows won't close again as Long as temperature is still critical to allow for user's override 
                // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
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
    handlerrunningFALSE()

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def contactHandlerClosed(evt) {

    state.thisIsWindowMgt = false 
    state.attempts = 0 // for future reset of thisiswindowsmgt()

    def message = ""

    log.debug "$evt.device is $evt.value" 

    log.info "List of devices' status is $CurrentContactsState"


    if(!AllContactsAreClosed()){
        log.debug "Not all contacts are closed, doing nothing"

    }
    else {      
        log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
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
    log.debug "$evt.device is now $evt.value, Turning off all thermostats in $TimeBeforeClosing seconds"

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
    handlerrunningTRUE()
    state.thisIsWindowMgt = true

    state.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Turning off all thermostats in $TimeBeforeClosing seconds"

    if(OperatingTime){
        runIn(TimeBeforeClosing, TurnOffThermostats)  
    }
    else{
        TurnOffThermostats()
    }
    handlerrunningFALSE()
    runIn(5, thisIsWindowMgtFALSE)
}
def contactExceptionHandlerClosed(evt) {
    state.ThermOff = false
    log.debug "$evt.device is now $evt.value (Contact Exception), Resuming Evaluation for $NoTurnOffOnContact"


    //AppMgtTrue()

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
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

    updated()
    
    state.recentModeChange = true
    runIn(60, recentModeChangeFALSE)

}
def recentModeChangeFALSE(){
state.recentModeChange = false
}

//override management
def handlerrunningTRUE(){
    state.handlerrunning = true
    log.info "handlerrunning($state.handlerrunning)"
}
def handlerrunningFALSE(){
    state.handlerrunning = false
    log.info "handlerrunning($state.handlerrunning)"
}
def setpointHandler(evt){
    log.trace """
$evt.device set to $evt.value (setpointHandler)

The source of this event was: $evt.source"
"""
    handlerrunningTRUE()
    def Endeval = state.EndEval
    /*while(Endeval != true){
Endeval = state.EndEval
log.debug "waiting"
}*/

    // declare an integer value for the thermostat which has had its values modified
    def MapModesThermostats = ["${Thermostats[0]}": "0" , "${Thermostats[1]}": 1, "${Thermostats[2]}": "2", 
                               "${Thermostats[3]}": "3"]
    def KeyValueForThisTherm = MapModesThermostats.find { it.key == "$evt.device"}
    log.info "device is ------------------- $KeyValueForThisTherm.key"
    def ThermNumber = KeyValueForThisTherm.value
    ThermNumber = KeyValueForThisTherm.value.toInteger()

    log.info "ThermNumber is ------------------- $ThermNumber"


    def AppMgt = state.AppMgtList[ThermNumber]

    log.trace """AppMgt at SetpointHandler for $ThermNumber ($KeyValueForThisTherm.key) is $AppMgt

"""

    if(AppMgt){

        def CurrMode = location.currentMode

        def HomeMode = null

        def reference = null
        def termRef = null
        def AltSENSOR = false    

        log.info "Home modes are : $Home, Night modes are : $Night, Away mode is : $Away, CustomMode1 are : $CustomMode1, CustomMode2 are : $CustomMode2"

        //log.debug "CurrMode is $CurrMode mode"

        //array heat
        def HSPH = ["0","$state.newValueT1HSP", "$state.newValueT2HSP", "$state.newValueT3HSP", "$HSPH4"]
        def HSPN = ["0","$state.newValueT1HSP", "$state.newValueT2HSP", "$state.newValueT3HSP", "$HSPN4"]
        def HSPA = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
        def HSPCust1 = ["0","$state.newValueT1HSP", "$state.newValueT2HSP", "$state.newValueT3HSP", "$HSPCust1_T4"]
        def HSPCust2 = ["0","$state.newValueT1HSP", "$state.newValueT2HSP", "$state.newValueT3HSP", "$HSPCust2_T4"]


        // declare an integer value for current mode
        def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
        def ModeIndexValue = MapofIndexValues.find{ it.key == "$CurrMode"}
        //log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
        ModeIndexValue = ModeIndexValue.value

        ModeIndexValue = ModeIndexValue.toInteger()
        log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"

        //array cool

        def CSPH = ["0","$state.newValueT1CSP", "$state.newValueT2CSP", "$state.newValueT3CSP", "$CSPH4"]
        def CSPN = ["0","$state.newValueT1CSP", "$state.newValueT2CSP", "$state.newValueT3CSP", "$CSPN4"]
        def CSPA = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
        def CSPCust1 = ["0","$state.newValueT1CSP", "$state.newValueT2CSP", "$state.newValueT3CSP", "$CSPCust1_T4"]
        def CSPCust2 = ["0","$state.newValueT1CSP", "$state.newValueT2CSP", "$state.newValueT3CSP", "$CSPCust2_T4"]

        // now transpose corresponding values among tables
        // for heat values
        def ListHSPs = ["0", HSPH, HSPN, HSPA, HSPCust1, HSPCust2]
        def HSPModecheck = ListHSPs[ModeIndexValue]
        //log.debug "HSPModecheck = $HSPModecheck"
        def RefHeat = HSPModecheck[ThermNumber]
        //do the same with cooling values
        def ListCSPs = ["0", CSPH, CSPN, CSPA, CSPCust1, CSPCust2]
        log.trace """
ListCSPs = $ListCSPs
ListHSPs = $ListHSPs"""
        def CSPModecheck = ListCSPs[ModeIndexValue]
        def RefCool = CSPModecheck[ThermNumber]

        // for thermostat set to work based on alternate temp sensors, the Alternativesensor() loops will 
        // simply stop running after this new setting has been compared to settings() in the arrays above
        // by declaring the state.AppMgnt_T_X variable as false.  

        if(evt.name == "heatingSetpoint"){              
            reference = RefHeat
            reference = Math.round(Double.parseDouble(RefHeat))
            reference = reference.toInteger()
            log.trace """RefHeat is $RefHeat and it is now converted to a reference for comparison"""

        }
        else  if(evt.name == "coolingSetpoint"){ 
            reference = RefCool
            reference = Math.round(Double.parseDouble(RefCool))
            reference = reference.toInteger()
            //log.debug "RefCool is $RefCool and it is now converted to a reference for comparison"
        }


        def ThisIsModeChange = state.locationModeChange
        def ExceptionState = state.exception
        def thisIsExceptionTemp = evt.displayName == "$NoTurnOffOnContact" && ExceptionState


        def Value = evt.value
        //def Value = Math.round(Double.parseDouble(evt.value))
        Value = Value.toInteger()
        //log.debug "Evt value to Integer is : $Value and it is to be compared to reference: $reference"


        log.trace """
RefHeat for $evt.device is: $RefHeat 
RefCool for $evt.device is: $RefCool 
reference for $evt.device is: $reference
ThisIsModeChange : $ThisIsModeChange
OVERRIDE? if true then should have $reference != $Value 
(unless location mode just changed or Exception Switch is on or ThisIsMotion or ThisIsLinearEq)
"""

        def doorsOk = AllContactsAreClosed()
        if(Value == reference || ThisIsModeChange ||  (!doorsOk && !thisIsExceptionTemp) && !AppMgt)
        {  
            log.debug "NO SETPOINT OVERRIDE for $evt.device"
        }
        else {

            state.AppMgtList[ThermNumber] = false // As of here this value won't change until location mode change or closing windows

            log.debug """AppMgt now is $AppMgt, override list: $state.AppMgtList

MANUAL SETPOINT OVERRIDE for $evt.device
state.AppMgtList = $state.AppMgtList
"""

            def message = "user set temperature manually on $evt.device"
            log.info message

        }

    }
    else {
        log.debug "$evt.device already in OVERRIDE MODE, not checking SETPOINT override"
    }

    handlerrunningFALSE()

}
def ThermostatSwitchHandler(evt){

    handlerrunningTRUE()

    def Endeval = state.EndEval
    /* while(Endeval != true)
{
Endeval = state.EndEval
log.debug "waiting"
}*/

    log.trace """$evt.device set to $evt.value (ThermostatSwitchHandler)
"""
    def t0 = Thermostats[0]
    def t1 = Thermostats[1]
    def t2 = Thermostats[2]
    def t3 = Thermostats[3]
    def t4 = Thermostats[4]

    def MapModesThermostats = ["$t0": "0" , "$t1": "1", "$t2": "2", "$t3": "3", "$t4": "4"]

    //row.columns.collectEntries{[it.name, it.val]}
    log.debug "MapModesThermostats = $MapModesThermostats ----------------------------------------------"

    //Thermostats.columns.collectEntries{[it.name, it.val]}

    def KeyValueForThisTherm = MapModesThermostats.find { it.key == "$evt.device"}
    log.info "device is ------------------- $KeyValueForThisTherm.value"
    def ThermNumber = KeyValueForThisTherm.value
    ThermNumber = KeyValueForThisTherm.value.toInteger()

    log.info "ThermNumber is ------------------- $ThermNumber"

    def AppMgt = state.AppMgtList[ThermNumber]

    def LatestThermMode = LatestThermMode()

    //log.debug "CHECKING COMMAND ORIGIN for $evt.device --   "

    if(state.CRITICAL == false){
        def CurrMode = location.currentMode
        def LocatioModeChange = state.locationModeChange
        //state.thisIsWindowMgt = false
        def thisIsWindowMgt = state.thisIsWindowMgt
        def ExceptionState = state.exception
        def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState
        //log.debug "Location Mode Changed?($LocatioModeChange)"

        log.trace "Latest Thermostat ModeS : $state.LatestThermostatMode_T1 | $state.LatestThermostatMode_T2 | $state.LatestThermostatMode_T3 | $state.LatestThermostatMode_T4"

        // is current state app managed or manually set?
        log.debug " for $evt.device shoudlbe: evt.value($evt.value) =? LatestThermMode($LatestThermMode)"
        def IdenticalShouldbe = evt.value == LatestThermMode 
        log.debug "IDENTICAL?($IdenticalShouldbe)"

        def ThereWasChange = !IdenticalShouldbe && !LocatioModeChange  && !thisIsWindowMgt 

        log.trace """
Change($ThereWasChange)
LocatioModeChange?($LocatioModeChange)
thisIsExceptionTemp?($thisIsExceptionTemp)
thisIsWindowMgt?($thisIsWindowMgt)
state.thisIsWindowMgt?($state.thisIsWindowMgt)
"""

        if(ThereWasChange){

            // command did not come from app so override is on
            log.debug "MANUAL ON/OFF OVERRIDE for $evt.device"
            state.AppMgtList[ThermNumber] = false

        }

    }
    else { 
        log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" 
    }

    handlerrunningFALSE()

}
def thisIsWindowMgtFALSE(){
    log.debug "state.thisIsWindowMgt = $state.thisIsWindowMgt"
    if(state.handlerrunning == false){
        state.thisIsWindowMgt = false
        log.debug "Reset state.thisIsWindowMgt to FALSE "        
    }
    else if(state.attempts > 3 && AllContactsAreClosed()){
        state.thisIsWindowMgt = false
        log.debug "Reset state.thisIsWindowMgt to FALSE " 
    }

    else {
        log.debug "Handler is running, trying reset of thisIsWindowMgt in 15 seconds"
        state.attempts = state.attempts + 1
        runIn(15, thisIsWindowMgtFALSE)
    }
}
def OverrideReset(){

    state.AppMgtList = [true, true, true, true, true]
    log.info "OVERRIDES RESET"
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

    log.debug "${ClosedContacts.size()} windows/doors out of ${Maincontacts.size()} are closed SO MainContactsAreClosed = $MainContactsAreClosed"

    return MainContactsAreClosed
}
def ExcepContactsClosed(){

    def ContactsExepClosed = true
    if(ContactException){
        def CurrentContactsExept = ContactException.currentValue("contact")    
        def ClosedContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreClosed ->
            AllcontactsExeptAreClosed == "closed" ? true : false
        }
        ContactsExepClosed = ClosedContactsExpt.size() == ContactException.size() 
        log.debug "${ClosedContactsExpt.size()} windows/doors out of ${ContactException.size()} are closed SO ContactsExepClosed = $ContactsExepClosed"

        def NoTurnOffOnContact = Thermostats.find {NoTurnOffOnContact << it.device}        

        log.debug "NoTurnOffOnContact = $NoTurnOffOnContact --------------"

        def CurrTherMode = NoTurnOffOnContact.currentValue("thermostatMode")
        // //log.debug "Current Mode for $Thermostat_1 is $CurrTherMode"
        if(CurrTherMode != "off" && !ContactsExepClosed){
            // //log.debug "$Thermostat_1 is on, should be off. Turning it off" 
            NoTurnOffOnContact.setThermostatMode("off") 
            state.LatestThermostatMode_T1 = "off"
        }
    }
    return ContactsExepClosed

}
def AllContactsAreClosed() {

    def AllContactsClosed = MainContactsClosed() && ExcepContactsClosed()
    log.debug "AllContactsAreClosed() $AllContactsClosed"

    return AllContactsClosed
}
def AllContactsAreOpen() {

    def MainContactsAreAllOpen = false // has to be true by default in case no contacts selected

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def OpenContacts = CurrentContacts.findAll { AllcontactsAreOpen ->
        AllcontactsAreOpen == "open" ? true : false}
    log.debug "${OpenContacts.size()} windows/doors out of ${Maincontacts.size()} are open"
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
        //  //log.debug "${OpenContactsExpt.size()} windows/doors out of ${ContactException.size()} are open SO ContactsExepOpen = $ContactsExepOpen"

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
    log.debug "InExceptionContactMode = $InExceptionContactMode  DoNotTurnOffModes = $DoNotTurnOffModes (TurnOffThermostats)"

    if(ContactAndSwitch){
        def contactClosed = false

        if(ContactException && FollowException && InExceptionContactMode){

            contactClosed = ExcepContactsClosed()
        }
        else{
            contactClosed =  AllContactsAreClosed()
        }
        log.debug "contactClosed = $contactClosed (TurnOffThermostats)"
        def CurrentSwitch = ContactAndSwitch.currentSwitch
        log.debug "$ContactAndSwitch currentSwitch = $currentSwitch"
        def SwState = CurrentSwitch.findAll { switchVal ->
            switchVal == "on" ? true : false
        }
        log.trace "SwState = $SwState"
        if(!contactClosed){
            if(SwState.size() != 0){
                ContactAndSwitch?.off()
                log.debug "$ContactAndSwitch TURNED OFF"
                //state.turnedOffByApp == true
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
            log.debug "${ThermSet}'s ThermState = $ThermState"


            log.trace "Turning off thermostats: ContactExceptionIsClosed: $ContactExceptionIsClosed, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"

            if((!NoTurnOffOnContact || !InExceptionContactMode || !ContactExceptionIsClosed) && "${ThermSet}" == "${NoTurnOffOnContact}"){
                if(ThermState != "off"){
                    // to avoid false end of override while windows are open and exception thermostat still needs to remain in override mode. 

                    ThermSet.setThermostatMode("off") 
                    state.LatestThermostatMode = "off"
                    log.debug "$ThermSet  turned off"

                }
            }
            else {

                // log.debug "Not turning off $ThermSet because current mode is within exception modes selected by the user"

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
        log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 


    }


}
def MotionTest(){
    def deltaMinutes = minutesMotion * 60000 as Long
    def motionEvents1 = MotionSensor_1?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    def motionEvents2 = MotionSensor_2?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    def motionEvents3 = MotionSensor_3?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()

    def Active1 = motionEvents1?.size() != 0
    def Active2 = motionEvents2?.size() != 0
    def Active3 = motionEvents3?.size() != 0

    if(state.AppMgtList[1] == false && !Active1){
        Active1 = true
    }
    if(state.AppMgtList[2] == false  && !Active2){
        Active2 = true
    }
    if(state.AppMgtList[3] == false  && !Active3){
        Active3 = true
    }
    //$evt.value motion @ $evt.device
    log.trace """

deltaMinutes = $deltaMinutes
Found ${motionEvents1?.size() ?: 0} events in the last $minutesMotion minutes at $MotionSensor_1
Found ${motionEvents2?.size() ?: 0} events in the last $minutesMotion minutes at $MotionSensor_2
Found ${motionEvents3?.size() ?: 0} events in the last $minutesMotion minutes at $MotionSensor_3

$MotionSensor_1 is ACTIVE = $Active1
$MotionSensor_2 is ACTIVE = $Active2
$MotionSensor_3 is ACTIVE = $Active3
"""

    state.Motionhandlerrunning = false

    return [null, Active1, Active2, Active3]
}

def CheckWindows(){

    //long MessageMinutes = 60*60000 as Long
    //long LastTimeMessageSent = state.LastTimeMessageSent as Long
    //def MessageTimeDelay = now() > LastTimeMessageSent + MessageMinutes 
    //log.debug "MessageTimeDelay = $MessageTimeDelay (checkwindows)"
    // for when it previously failed to turn off thermostats
    def AllContactsClosed = AllContactsAreClosed()
    log.debug "Checking windows"

    def OkToOpen = OkToOpen() // outside and inside temperatures criteria and more... 

    def message = ""

    def allContactsAreOpen = AllContactsAreOpen()

    log.debug "Contacts closed?($ContactsClosed)"

    def Inside = XtraTempSensor.currentValue("temperature")
    //Inside = Double.parseDouble(Inside)
    //Inside = Inside.toInteger()
    log.debug "Inside = $Inside"
    def Outside = OutsideSensor.currentValue("temperature")
    //Outside = Double.parseDouble(Outside)
    //Outside = Outside.toInteger()
    log.debug "Outside = $Outside"

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

        if(AllContactsClosed){
            if( inAway && ClosedByApp != true && OpenInfullWhenAway){
                ClosedByApp = true
            }
            if(ClosedByApp) {
                Actuators?.on()
                state.OpenByApp = true
                state.ClosedByApp = false // so it doesn't open again

                //log.debug "opening windows"
                if(OperatingTime){
                    message = "I'm opening windows because $state.causeOpen. Operation time is $OperatingTime seconds"
                    runIn(OperatingTime, StopActuators) 
                }
                else {
                    message = "I'm opening windows because $state.causeOpen"
                }
                log.info message 
                send(message)
            }
            else { 
                log.debug "Windows have already been opened, doing nothing" 
            }
        }
    }
    // if not ok to open and it is open then close
    else if (state.OpenByApp == true && !AllContactsClosed) {

        Actuators?.off()
        ActuatorException?.off()

        message = "I'm closing windows because $state.causeClose"
        send(message)
        log.info message 

        state.ClosedByApp = true
        state.OpenByApp = false // so it doesn't close again if user opens it manually
    }

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def OkToOpen(){
    def message = ""
    log.debug "Checking if it's O.K. to Open windows"
    def ContactsClosed = AllContactsAreClosed()

    def CSPSet = state.CSPSet
    def HSPSet = state.HSPSet 

    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature")
    //Inside = Double.parseDouble(Inside)
    //Inside = Inside.toInteger()
    def CurrTemp = Inside // as int
    def Outside = OutsideSensor.currentValue("temperature")
    //Outside = Double.parseDouble(Outside)
    //Outside = Outside.toInteger()
    def outsideTemp = Outside as int
        state.outsideTemp = Outside
    def WithinCriticalOffSet = (Inside >= (CriticalTemp + OffSet)) && (Outside >= (CriticalTemp + OffSet))

    def OutsideTempHighThres = ExceptACModes()
    def ExceptHighThreshold1 = ExceptHighThreshold1
    log.debug "test"

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


    log.debug "Humidity EVAL"
    def TooHumid = false
    if(humidity > HumidityTolerance){
        TooHumid = true
    }
    if(WindValue > 3 && Outside < Inside + 6 && humidity <= HumidityTolerance + 5){
        TooHumid = false
    }

    def ShouldCool = outsideTemp >= OutsideTempLowThres 
    def ShouldHeat = state.ShouldHeat
    if(ShouldCool && ShouldHeat) {
        ShouldCool = false
    }
    if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet && outsideTemp >= CSPSet) {
        ShouldCool = true
    }        

    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4 && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    }

    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid && !OutsideFeelsHotter

    state.OpenInFull = false
    if(OpenWhenEverPermitted && outsideTemp >= HSPSet) { 
        state.OpenInFull = true
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

        message = "Windows are closed because $causeNotOkToOpen"
        log.info message
        state.messageclosed = message
        // send a reminder every X minutes 

        long MessageMinutes = 60L*60000L
        long LastTimeMessageSent = state.LastTimeMessageSent
        long SinceLast = LastTimeMessageSent + MessageMinutes

        def MessageTimeDelay = now() > SinceLast
        log.debug "SinceLast = $SinceLast || MessageMinutes = $MessageMinutes || LastTimeMessageSent = $LastTimeMessageSent || MessageTimeDelay = $MessageTimeDelay"

        if(MessageTimeDelay && ContactsClosed) {
            send(message)
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

    def SlightOpen = state.SlightOpen

    def OpenInFull = state.OpenInFull
    //log.debug "STOP"
    if (Actuators?.hasCommand("stop")/* && !OpenInFull*/){
        log.debug "SENDING STOP COMMAND"
        Actuators?.stop()
    }
    if (ActuatorException?.hasCommand("stop") /* && !OpenInFull*/){
        ActuatorException?.stop()
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

    def scheduledTimeA = 1
    def scheduledTimeB = 5

    //schedule("0 0/$scheduledTimeA * * * ?", Evaluate)
    //log.debug "Evaluate scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }

}
def polls(){

    def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "${CustomMode1[0]}": "4", "${CustomMode2[0]}": "5" ]   
    def ModeIndexValue = MapofIndexValues.find{ it.key == "$location.currentMode"}
    ModeIndexValue = ModeIndexValue.value.toInteger()


    def CtrlSwtchPoll = CtrlSwt?.hasCommand("poll")
    def CtrlSwtchRefresh = CtrlSwt?.hasCommand("refresh")

    if(CtrlSwtchPoll){
        CtrlSwt?.poll()
        //log.debug "polling $CtrlSwt"
    }
    else if(CtrlSwtchRefresh){
        CtrlSwt?.refresh()
        //log.debug "refreshing $CtrlSwt"
    }
    else { 
        //log.debug "$CtrlSwt neither supports poll() nor refresh() commands"
    }

    if(Thermostat_1){
        def poll = Thermostat_1.hasCommand("poll")
        def refresh = Thermostat_1.hasCommand("refresh")
        if(poll){
            Thermostat_1.poll()
            //log.debug "polling $Thermostat_1"
        }
        else if(refresh){
            Thermostat_1.refresh()
            //log.debug "refreshing $Thermostat_1"
        }
        else { 
            //log.debug "$Thermostat_1 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_2){
        def poll = Thermostat_2.hasCommand("poll")
        def refresh = Thermostat_2.hasCommand("refresh")
        if(poll){
            Thermostat_2.poll()
            //log.debug "polling $Thermostat_2"
        }
        else if(refresh){
            Thermostat_2.refresh()
            //log.debug "refreshing $Thermostat_2"
        }
        else { 
            //log.debug "$Thermostat_2 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_3){
        def poll = Thermostat_3.hasCommand("poll")
        def refresh = Thermostat_3.hasCommand("refresh")
        if(poll){
            Thermostat_3.poll()
            //log.debug "polling $Thermostat_3"
        }
        else if(refresh){
            Thermostat_3.refresh()
            //log.debug "refreshing $Thermostat_3"
        }
        else { 
            //log.debug "$Thermostat_3 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_4){
        def poll = Thermostat_4.hasCommand("poll")
        def refresh = Thermostat_4.hasCommand("refresh")
        if(poll){
            Thermostat_4.poll()
            //log.debug "polling $Thermostat_4"
        }
        else if(refresh){
            Thermostat_4.refresh()
            //log.debug "refreshing $Thermostat_4"
        }
        else { 
            //log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
        }
    }
    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            //log.debug "polling $OutsideSensor"
        }
        else if(refresh){
            OutsideSensor.refresh()
            //log.debug "refreshing $OutsideSensor"
        }
        else { 
            //log.debug "$OutsideSensor does not support either poll() nor refresh() commands"
        }
    }

}
def send(msg){
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage) {
            // //log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            // //log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    //log.debug msg
}


