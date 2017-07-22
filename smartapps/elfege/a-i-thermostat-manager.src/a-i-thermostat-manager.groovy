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
        install: true,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        section("how many thermostats do you want to control?") { 
            input(name: "HowMany", type: "number", range: "1..4", title: "set a value between 1 and 4", description: null, submitOnChange: true)
            if(HowMany >= 1) {
                input(name: "Thermostat_1", type: "capability.thermostat", title: "Thermostat 1 is $Thermostat_1", required: false, multiple: false, description: null, submitOnChange: true, uninstall: true)
                input(name: "AltSensor_1", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_1){
                    input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_1", required: true, multiple: false, description: null, uninstall: true)
                }
                input(name: "NoTurnOffOnContact", type: "bool", title: "Do not turn off this unit upon contacts events", default: false, submitOnChange: true, description: "this is to be applied with a specific mode in the next section")

            }
            if(HowMany >= 2) {
                input(name: "Thermostat_2", type: "capability.thermostat", title: "Thermostat 2 is $Thermostat_2", required: false, multiple: false, description: null, submitOnChange: true, uninstall: true)
                input(name: "AltSensor_2", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_2){
                    input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_2", required: true, multiple: false, description: null, uninstall: true)
                }


            }
            if(HowMany >= 3) {
                input(name: "Thermostat_3", type: "capability.thermostat", title: "Thermostat 3 is $Thermostat_3", required: false, multiple: false, description: null, submitOnChange: true, uninstall: true)
                input(name: "AltSensor_3", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_3){
                    input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_3", required: true, multiple: false, description: null, uninstall: true)
                }

            }
            if(HowMany == 4) {
                input(name: "Thermostat_4", type: "capability.thermostat", title: "Thermostat 4 is $Thermostat_4", required: false, multiple: false, description: null, submitOnChange: true, uninstall: true)
            }


            input(name: "turnOffWhenReached", type: "bool", title: "Turn off thermostats when desired temperature is reached?", required: false, default: false, submitOnChange: true)
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

        section("Main Mode") {
            input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: false, required: false)
        }
        section("Other Modes"){
            input(name: "Night", type : "mode", title: "Select Night mode", multiple: false, required: true)
            input(name: "Away", type : "mode", title: "Select away mode", multiple: false, required: true)
        }
        section("MoreModes"){ 
            input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
            if(Moremodes){
                input(name: "CustomMode1", type : "mode", title: "Select mode", multiple: false, required: true)
                input(name: "CustomMode2", type : "mode", title: "Select mode", multiple: false, required: false, submitOnChange: true)
            }
        }

        section("Modify setpoints with a switch on/off status"){  
            input(name: "ExceptionSW", type : "bool", title: "Apply different settings for $Thermostat_1 when a switch is on", 
                  defaut: false, submitOnChange: true)
            input(name: "warmerorcooler", type : "enum", title: "Have this room Warmer or cooler", 
                  required: true, options: ["warmer", "cooler", "more heating, cooler cooling"], submitOnChange: true)
            if(ExceptionSW){   
                input(name: "CtrlSwt", type: "capability.switch", title: "Adjust $Thermostat_1 settings When this switch is on", required: true, submitOnChange: true)


                if(warmerorcooler == "more heating, cooler cooling"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $Thermostat_1 heat setting When $CtrlSwt is on", required: true, range: "1..5")
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $Thermostat_1 cooling setting When $CtrlSwt is on", required: true, range: "1..5")                     
                }
                else if(warmerorcooler == "warmer"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $Thermostat_1 for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = AddDegrees?.toInteger()
                    input(name: "SubDegrees", type: "decimal", title:"Enter the same value", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
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

        section("Thermostats temperatures for $Home Mode"){
            if(HowMany >= 1) {
                input(name: "HSPH1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Home mode", required: true)
                input(name: "CSPH1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Home mode", required: true)

            }
            if(HowMany >= 2) {
                input(name: "HSPH2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $Home mode", required: true)
                input(name: "CSPH2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $Home mode", required: true)
            }
            if(HowMany >= 3) {     
                input(name: "HSPH3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $Home mode", required: true)
                input(name: "CSPH3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $Home mode", required: true)                      
            }
            if(HowMany == 4) {   
                input(name: "HSPH4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $Home mode", required: true)
                input(name: "CSPH4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $Home mode", required: true)
            }
        }
        section("Thermostats temperatures for $Night Mode"){
            if(HowMany >= 1) {
                input(name: "HSPN1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Night mode", required: true)
                input(name: "CSPN1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Night mode", required: true)
            }
            if(HowMany >= 2) {
                input(name: "HSPN2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $Night mode", required: true)
                input(name: "CSPN2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $Night mode", required: true)
            }
            if(HowMany >= 3) {     
                input(name: "HSPN3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $Night mode", required: true)
                input(name: "CSPN3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $Night mode", required: true)
            }
            if(HowMany == 4) {   
                input(name: "HSPN4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $Night mode", required: true)
                input(name: "CSPN4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $Night mode", required: true)
            }
        }
        section("Thermostats temperatures for $Away Mode"){   
            paragraph "these values apply to all thermostats evenly"
            input(name: "HSPA", type: "decimal", title: "Set Heating temperature for $Away mode", required: true)
            input(name: "CSPA", type: "decimal", title: "Set Cooling temperature for $Away mode", required: true)
        }

        if(Moremodes){
            section("$CustomMode1 Mode"){
                if(HowMany >= 1) {
                    input(name: "HSPCust1_T1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $CustomMode1 mode", required: true)
                }
                if(HowMany >= 2) {
                    input(name: "HSPCust1_T2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $CustomMode1 mode", required: true)
                }
                if(HowMany >= 3) {     
                    input(name: "HSPCust1_T3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $CustomMode1 mode", required: true)
                }
                if(HowMany == 4) {   
                    input(name: "HSPCust1_T4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $CustomMode1 mode", required: true)
                }
            }
            if(CustomMode2){
                section("$CustomMode2 Mode"){
                    if(HowMany >= 1) {
                        input(name: "HSPCust2_T1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany >= 2) {
                        input(name: "HSPCust2_T2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany >= 3) {     
                        input(name: "HSPCust2_T3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany == 4) {   
                        input(name: "HSPCust2_T4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $CustomMode2 mode", required: true)
                    }
                }
            }
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

        section("Exception Thermostat"){
            if(NoTurnOffOnContact){
                paragraph "You selected $Thermostat_1 as one to not turn off when contacts are open. Now select under which modes you want this rule to apply "
                input(name: "DoNotTurnOffModes", type : "enum", title: "Select which modes", options: ["$Home", "$Night", "$Away", "$CustomMode1", "$CustomMode2"], multiple: true, required: true)
                input(name: "ContactException", type : "capability.contactSensor", multiple: true, title: "unless these specific contacts sensors are open", description: "Select a contact", required: false, submitOnChange: true)
            }

            else {
                paragraph "You did not select this option, fill in the next section below"
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


        section("Turn off thermostats when doors or windows are open"){

            input(name: "Maincontacts", type:"capability.contactSensor", title: "Turn off all units when these contacts are open", multiple: true, required: false, submitOnChange: true)
            if(Maincontacts){
                input(name: "TimeBeforeClosing", type: "number", title: "after this amount of time in seconds", required: false, description: "default is 60 seconds", default: 60, uninstall: true, install: true)
                input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "Enter a safety temperature value")
                input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true, submitOnChange: true)
                if(NoTurnOffOnContact){
                    def Same = Thermostat_1 == XtraTempSensor
                    //log.debug "SAME = $Same"
                    if(Same){
                        //log.debug "YES"
                        paragraph """WARNING! You chose $XtraTempSensor as a device to measure inside's temperature AND as the thermostat which will not shut down when 
windows are open. This will prevent windows from opening when outside's temperature is lower than in other rooms but not in this one (since AC may still run in this room)
It is highly recommended to select another device or this app will not be capable of handling temperatures properly"""
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

            input(name: "MotionSensor_1", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_1 settings when inactive", description: "Select a motion sensor", required: false)
            if(MotionSensor_1){
                input(name: "MotionModesT1", type: "mode", title: "Use $MotionSensor_1 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                input(name: "AddDegreesMotion1", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting", required: true)
                input(name: "SubDegreesMotion1", type: "decimal", title: "Substract this amount of degrees to $Thermostat_1 cooling setting", required: true)   
            }
            if(HowMany > 1) {
                input(name: "MotionSensor_2", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_2 settings when inactive", description: "Select a motion sensor", required: false)
                if(MotionSensor_2){
                    input(name: "MotionModesT2", type: "mode", title: "Use $MotionSensor_2 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                    input(name: "AddDegreesMotion2", type: "decimal", title: "Add this amount of degrees to $Thermostat_2 heat setting", required: true)
                    input(name: "SubDegreesMotion2", type: "decimal", title: "Substract this amount of degrees to $Thermostat_2 cooling setting", required: true)      
                }
            }
            if(HowMany > 2) {
                input(name: "MotionSensor_3", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_3 settings when inactive", description: "Select a motion sensor", required: false)
                if(MotionSensor_3){
                    input(name: "MotionModesT3", type: "mode", title: "Use $MotionSensor_3 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                    input(name: "AddDegreesMotion3", type: "decimal", title: "Add this amount of degrees to $Thermostat_3 heat setting", required: true)
                    input(name: "SubDegreesMotion3", type: "decimal", title: "Substract this amount of degrees to $Thermostat_3 cooling setting", required: true)      
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
// install and updated
def installed() {	 
    // //log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler

    // default values to avoid NullPointer // must be set as such only for new installation not in init or updated  

    atomicState.humidity = HumidityTolerance - 1

    //log.debug "atomicState.humidity is $atomicState.humidity (updated() loop)"
    atomicState.wind = 4
    atomicState.FeelsLike = OutsideSensor.currentValue("temperature")

    atomicState.OpenByApp = true
    atomicState.ClosedByApp = true

    init()
}
def updated() {
    atomicState.modeStartTime = now() 

    atomicState.LastTimeMessageSent = now() // for causes of !OkToOpen message

    log.info "updated with settings = $settings $Modes"

    unsubscribe()
    unschedule()

    init()
}
def init() {

    atomicState.now = now()

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

    subscribe(Thermostat_1, "temperature", temperatureHandler)
    subscribe(Thermostat_1, "thermostatMode", ThermostatSwitchHandler)


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

    subscribe(Thermostat_1, "heatingSetpoint", setpointHandler)
    subscribe(Thermostat_1, "coolingSetpoint", setpointHandler)

    if(Thermostat_2){
        subscribe(Thermostat_2, "temperature", temperatureHandler)
        subscribe(Thermostat_2, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_2, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_2, "coolingSetpoint", setpointHandler)


    }
    if(Thermostat_3){
        subscribe(Thermostat_3, "temperature", temperatureHandler)
        subscribe(Thermostat_3, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_3, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_3, "coolingSetpoint", setpointHandler)


    }
    if(Thermostat_4){
        subscribe(Thermostat_4, "temperature", temperatureHandler)
        subscribe(Thermostat_4, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_4, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_4, "coolingSetpoint", setpointHandler)
        subscribe(Thermostat_4, "app", appHandler)

        /* swichtCapableCap = Thermostat_4.hasCapability("Switch")
swichtCapableLow = Thermostat_4.hasCapability("switch")
if(swichtCapableLow){
subscribe(Thermostat_4, "switch", ThermostatSwitchHandler)
//log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
} else  if(swichtCapableCap){
subscribe(Thermostat_4, "Switch", ThermostatSwitchHandler)
//log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
} else { log.trace "no switch capability for $Thermostat_4" }
*/
    }
    if(ExceptionSW){
        if(CtrlSwt){
            subscribe(CtrlSwt, "switch", switchHandler)
        }


    }
    if(AltSensor_1 || AltSensor_2 || AltSensor_3){

        if(AltSensor_1){
            subscribe(Sensor_1, "temperature", temperatureHandler)
            //log.debug "Subscription for alternative Sensor for $Sensor_1"

        }
        if(AltSensor_2){
            subscribe(Sensor_2, "temperature", temperatureHandler)
            //log.debug "Subscription for alternative Sensor for $Sensor_2"

        }
        if(AltSensor_3){
            subscribe(Sensor_3, "temperature", temperatureHandler)
            //log.debug "Subscription for alternative Sensor for $Sensor_3"

        }
    }

    if(MotionSensor_1){
        subscribe(MotionSensor_1, "motion", motionSensorHandler)
    }
    if(MotionSensor_2){
        subscribe(MotionSensor_2, "motion", motionSensorHandler)
    }
    if(MotionSensor_3){
        subscribe(MotionSensor_3, "motion", motionSensorHandler)
    }

    if(Actuators){
        subscribe(Actuators, "switch", switchHandler)
    }
    if(ActuatorException){
        subscribe(ActuatorException, "switch", switchHandler)
    }


    atomicState.doorsAreOpen = false

    // these values will be reset to false later, for now they need to remain true so app doesn't go into override
    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true

    atomicState.T1_AppMgtSP = true
    atomicState.T2_AppMgtSP = true
    atomicState.T3_AppMgtSP = true
    atomicState.T4_AppMgtSP = true


    // motion sensor handler default start values
    atomicState.isInActive1 = false
    atomicState.isInActive2 = false
    atomicState.isInActive3 = false

    state.messageOkToOpenCausesSent = 0
    atomicState.LastTimeMessageSent = now()


    // first default values to be set to any suitable value so it doesn't crash with null value 
    // they will be updated within seconds with user's settings 
    atomicState.newValueT1CSP = 75
    atomicState.newValueT1HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75

    // same
    atomicState.Inside = XtraTempSensor.currentValue("temperature")

    // same idea but for therm modes and temps
    atomicState.currentTemperatureT1 = Thermostat_1?.currentValue("temperature")
    atomicState.currentThermModeT1 = Thermostat_1?.currentValue("thermostatMode") as String
    atomicState.currentTemperatureT2 = Thermostat_2?.currentValue("temperature") 
    atomicState.currentThermModeT2 = Thermostat_2?.currentValue("thermostatMode") as String   
    atomicState.currentTemperatureT3 = Thermostat_3?.currentValue("temperature") 
    atomicState.currentThermModeT3 = Thermostat_3?.currentValue("thermostatMode") as String 
    atomicState.currentTemperatureT4  = Thermostat_4?.currentValue("temperature") 
    atomicState.currentThermModeT4 = Thermostat_4?.currentValue("thermostatMode") as String

    atomicState.AllowToRunMainLoop = true // for motion handler 

    atomicState.ThisIsManual = false

    def ContactsClosed = AllContactsAreClosed()
    //log.debug "enter updated, state: $state"  


    atomicState.messageSent = 0

    atomicState.locationModeChange = true 

/*
    def Therm = ["0", "$Thermostat_1", "$Thermostat_2","$Thermostat_3", "$Thermostat_4"]
    def NullThermFind = Therm.findAll { val ->
        val == "null" ? true : false
    }
    def ThermsInvolved = Therm.size() - NullThermFind.size() - 1 
    // -1 because of index 0
    ThermsInvolved = ThermsInvolved.toInteger()
    */
   
    log.debug "Number of Thermostats Selected by User : $HowMany [init]"




    runIn(10, resetLocationChangeVariable)

    // false positive overrides management 


    schedules()


}

// MAIN LOOP
def Evaluate(){

    def doorsOk = AllContactsAreClosed()

    if(!doorsOk){
        TurnOffThermostats()
    }

    def ContactExceptionIsClosed = ExcepContactsClosed()
    log.debug "doorsOk?($doorsOk), ContactExceptionIsClosed?($ContactExceptionIsClosed)"

    def CurrMode = location.currentMode
    def Outside = OutsideSensor?.currentValue("temperature") 
    def outsideTemp = Outside
    def Inside = XtraTempSensor.currentValue("temperature")
    atomicState.Inside = Inside
    def humidity = atomicState.humidity
    humidity = humidity.toInteger()
    def TooHumid = humidity > HumidityTolerance && Outside > Inside

    //log.debug "humidity is $humidity && TooHumid is $TooHumid"
    if(atomicState.handlerrunning == false && atomicState.Motionhandlerrunning == false){
        if(doorsOk || ContactExceptionIsClosed ){


            // //log.debug "atomicState.ThisIsManual value is $atomicState.ThisIsManual"
            def CurrTemp1 = Thermostat_1?.currentValue("temperature") 
            def ThermState1 = Thermostat_1?.currentValue("thermostatMode") 
            def CurrTemp2 = Thermostat_2?.currentValue("temperature") 
            def ThermState2 = Thermostat_2?.currentValue("thermostatMode") 
            def CurrTemp3 = Thermostat_3?.currentValue("temperature") 
            def ThermState3 = Thermostat_3?.currentValue("thermostatMode") 
            def CurrTemp4 = Thermostat_4?.currentValue("temperature") 
            def ThermState4 = Thermostat_4?.currentValue("thermostatMode") 
            log.trace """
Override modes list: T1 : $atomicState.T1_AppMgt, T2 : $atomicState.T2_AppMgt, T3 : $atomicState.T3_AppMgt, T4 : $atomicState.T4_AppMgt
Current Thermostats Modes: ThermState1: $ThermState1, ThermState2: $ThermState2, ThermState3: $ThermState3, ThermState4: $ThermState4"""


            def CurrTemp_Alt1 = Sensor_1?.currentValue("temperature")
            def CurrTemp_Alt2 = Sensor_2?.currentValue("temperature") 
            def CurrTemp_Alt3 = Sensor_3?.currentValue("temperature")

            def CurrTempList_Alt = [0, CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3, null]
            def AltSensorBoolList = [false, AltSensor_1, AltSensor_2, AltSensor_3, false]
            def AltSensorDevicesList =  [null, Sensor_1, Sensor_2, Sensor_3, false]



            // which HSP? 
            // //log.debug "CurrMode is $CurrMode mode"
            //array heat
            def HSP1 = [0,HSPH1, HSPN1, HSPA, HSPCust1_T1, HSPCust2_T1]
            def HSP2 = [0,HSPH2, HSPN2, HSPA, HSPCust1_T2, HSPCust2_T2]
            def HSP3 = [0,HSPH3, HSPN3, HSPA, HSPCust1_T3, HSPCust2_T3]
            def HSP4 = [0,HSPH4, HSPN4, HSPA, HSPCust1_T4, HSPCust2_T4]
            //array cool
            def CSP1 = [0,CSPH1, CSPN1, CSPA, CSPCust1_T1, CSPCust2_T1]
            def CSP2 = [0,CSPH2, CSPN2, CSPA, CSPCust1_T2, CSPCust2_T2]
            def CSP3 = [0,CSPH3, CSPN3, CSPA, CSPCust1_T3, CSPCust2_T3]
            def CSP4 = [0,CSPH4, CSPN4, CSPA, CSPCust1_T4, CSPCust2_T4]

            // declare an integer value for current mode
            def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
            def ModeIndexValue = MapofIndexValues.find{ it.key == "$location.currentMode"}
            ModeIndexValue = ModeIndexValue.value.toInteger()
            log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
            HSP1 = HSP1[ModeIndexValue]
            HSP2 = HSP2[ModeIndexValue]
            HSP3 = HSP3[ModeIndexValue]
            HSP4 = HSP4[ModeIndexValue]
            CSP1 = CSP1[ModeIndexValue]
            CSP2 = CSP2[ModeIndexValue]
            CSP3 = CSP3[ModeIndexValue]
            CSP4 = CSP4[ModeIndexValue]    


            def inCtrlSwtchMode = CurrMode in ["$Home", "$Night", "$CustomMode1", "$CustomMode2"]

            if(inCtrlSwtchMode){
                def SwitchesOnTest = CtrlSwt?.currentValue("switch") == "on"
                SwitchesOn = SwitchesOnTest && ContactExceptionIsClosed
                //log.debug "SwitchesOn($SwitchesOn)"
            }

            def HSP = [0, HSP1, HSP2, HSP3, HSP4]
            //log.debug "HSP LIST is $HSP"
            def HSPSet = 0
            def CSP = [0, CSP1, CSP2, CSP3, CSP4]
            //log.debug "CSP LIST is $CSP"
            def CSPSet = 0
            def LatestThermostatMode = null
            // thermostats list 
            def Therm = [null, Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
            def ThermSet = 0
            log.debug "Therm list = $Therm"

            def ThermDeviceList = ["null", Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
            def CurrTempList = [0, CurrTemp1, CurrTemp2, CurrTemp3, CurrTemp4]
            def CurrTempListAlt = [0, CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3, CurrTemp4]
            def CurrTemp = 0
            def ThermStateList = [0, "$ThermState1", "$ThermState2", "$ThermState3", "$ThermState4"]
            def ThermState = 0


            def MotionSensorList = ["0", "$MotionSensor_1", "$MotionSensor_2", "$MotionSensor_3"]
            def MotionSensor = 0
            def MotionModesList = ["0", MotionModesT1, MotionModesT2 , MotionModesT3]
            def MotionModes = null

            def AddDegreesMotionList = [0, AddDegreesMotion1, AddDegreesMotion2, AddDegreesMotion3]
            def AddDegreesMotion = 0
            def SubDegreesMotionList  = [0, SubDegreesMotion1, SubDegreesMotion2, SubDegreesMotion3]
            def SubDegreesMotion = 0
            def InactiveList = [0, atomicState.isInActive1, atomicState.isInActive2, atomicState.isInActive3] 
            log.debug "InactiveList = $InactiveList"
            def Inactive = 0

            def OutsideTempHighThres = ExceptACModes()

            def loopValue = 0
           
			def ThermsInvolved = HowMany
            
            def CurrTempDevice = 0


            while(loopValue < ThermsInvolved){
             loopValue++
                    atomicState.loopValue = loopValue
            
                log.trace """WHILE LOOP 
                
                log.debug ThermsInvolved = $ThermsInvolved 
                loop($loopValue) 
                atomicState.loopValue = $atomicState.loopValue"""

               
                log.info ""

                log.debug "000"

                ThermState = ThermStateList[loopValue]

                ThermSet = Therm[loopValue]

                atomicState.EventAtTempLoop = ThermSet as String // used for false override prevention
                log.debug "111 ThermSet = $ThermSet - atomicState.EventAtTempLoop = $atomicState.EventAtTempLoop"

                def AltSensor = AltSensorBoolList[loopValue] 


                if(AltSensor){

                    CurrTemp = CurrTempList_Alt[loopValue] as double

                        CurrTempDevice = AltSensorDevicesList[loopValue] 
                    log.debug "$CurrTempDevice selected as CurrTemp source for $ThermSet and it returns a temperature of $CurrTemp F"
                }
                else {
                    CurrTemp = CurrTempList[loopValue] as double
                        //CurrTemp = CurrTemp.toInteger()
                        CurrTempDevice = AltSensorDevicesList[loopValue]
                    log.debug " $ThermSet returns a temperature of $CurrTemp F"
                }
                log.debug "222"

                log.debug "CurrTemp = $CurrTemp ($ThermSet)"
                atomicState.CurrTemp = CurrTemp

                // motion management
                MotionSensor = MotionSensorList[loopValue]
                atomicState.MotionSensor = MotionSensor
                MotionModes = MotionModesList[loopValue]

                AddDegreesMotion = AddDegreesMotionList[loopValue]
                SubDegreesMotion = SubDegreesMotionList[loopValue]
                Inactive = InactiveList[loopValue]

                HSPSet = HSP[loopValue]
                HSPSet = HSPSet.toInteger()            
                CSPSet = CSP[loopValue] 

                //CSPSet = CSPSet.toInteger()
                atomicState.CSPSet = CSPSet
                atomicState.HSPSet = HSPSet


                def AppMgtList = ["0", atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
                def AppMgt = AppMgtList[loopValue]
                log.trace """AppMgt = $AppMgt

Current Temperature Inside = $Inside
//log.debug "ShouldHeat = $ShouldHeat
"""

                if(ExceptionSW && ThermSet == "$Thermostat_1" && SwitchesOn){
                    HSPSet = HSPSet + AddDegrees
                    CSPSet = CSPSet + SubDegrees

                    //log.debug "$ThermSet SetPoints ExceptionSW active"                                
                }
                if(AppMgt){
                    log.info "NO OVERRIDE for $ThermSet -- EVALUATING"     
                    atomicState.withinApp = true

                    //modify with presence/motion in the room

                    def defaultCSPSet = CSPSet // recording this default value so if linear equation brings setpoint too low, it'll be recovered
                    def defaultHSPSet = HSPSet // same but with heat

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

                            //CSPSet = CSPSet.toInteger()

                            // no lower than defaultCSPSet 
                            if(CSPSet <= defaultCSPSet){

                                log.info """CurrTemp at ${ThermSet} is: $CurrTemp. CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp
But because CSPSet is $CSPSet and thus lower than default value ($defaultCSPSet), default settings are restored"""
                                CSPSet = defaultCSPSet
                            }
                        else {

                            log.info "CurrTemp at ${ThermSet} is: $CurrTemp  CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"
                        }

                    } 
                    else if(adjustments == "Yes, but use a logarithmic variation"){
                        // logarithmic treatment 

                        /* concept: x = log(72)75   to what power (that is to say "x") do I have to raise 72, to get to 75?

logb(n) = loge(n) / loge(b)
Where log can be a logarithm function in any base, n is the number and b is the base. For example, in Java this will find the base-2 logarithm of 256:

Math.log(256) / Math.log(2)
=> 8.0
*/
                        //outsideTemp = 90 // for test only MUST BE DELETED
                        CSPSet = (Math.log(outsideTemp) / Math.log(CSPSet)) * CSPSet
                        CSPSet = Math.round(CSPSet)

                        //log.debug "Logarithmic CSPSet = $CSPSet"

                        // no lower than defaultCSPSet 
                        if(CSPSet <= defaultCSPSet){

                            log.info """CurrTemp at ${ThermSet} is: $CurrTemp. CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp
But because CSPSet is $CSPSet and thus lower than default value ($defaultCSPSet), default settings are restored"""
                            CSPSet = defaultCSPSet
                        }
                        else {

                            log.info "CurrTemp at ${ThermSet} is: $CurrTemp  CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"
                        }
                    }
                    // end of algebraic adjustments        

                    // motion? 
                    def InMotionModes = CurrMode in MotionModes  
                    def AccountForMotion = MotionSensor != null 

                    if(AccountForMotion && InMotionModes ){

                        if(Inactive){
                            // record algebraic CSPSet for debug purpose
                            def algebraicCSPSet = CSPSet 
                            // log.info "$ThermSet default Cool: $CSPSet and default heat: $HSPSet "
                            CSPSet = CSPSet + AddDegreesMotion 
                            HSPSetDefault
                            HSPSet = HSPSet - SubDegreesMotion

                            log.trace "NO MOTION so $ThermSet CSP, which was $defaultCSPSet, then (if algebra) $algebraicCSPSet, is now set to $CSPSet and HSP was $defaultHSPSet and is now set to $HSPSet"

                        }
                        else {

                            //log.debug "There's motion in ${ThermSet}'s room (main loop)"
                        }
                    }
                    log.trace """
InMotionModes?($InMotionModes)
AccountForMotion?($AccountForMotion)
Motion at $MotionSensor inactive for the past $minutesMotion minutes?($Inactive)
FINAL CSPSet for $ThermSet = $CSPSet
"""

                    // for false overrides prevention
                    // and for back to normal action
                    // if user sets unit back to its currently calculated 
                    // value, then the app will end the override

                    if(loopValue == 1){
                        atomicState.newValueT1CSP = CSPSet as double
                            atomicState.newValueT1HSP = HSPSet as double

                            }    
                    if(loopValue == 2){
                        atomicState.newValueT2CSP = CSPSet as double
                            atomicState.newValueT2HSP = HSPSet as double

                            }
                    if(loopValue == 3){
                        atomicState.newValueT3CSP = CSPSet as double
                            atomicState.newValueT3HSP = HSPSet as double  

                            }
                    if(loopValue == 4){
                        atomicState.newValueT4CSP = CSPSet as double
                            atomicState.newValueT4HSP = HSPSet as double  

                            }

                    // evaluate needs

                    def ShouldCoolWithAC = CurrTemp > CSPSet
                    def ShouldHeat = outsideTemp < OutsideTempLowThres && CurrTemp <= HSPSet && !ShouldCoolWithAC
                    atomicState.ShouldCoolWithAC = ShouldCoolWithAC
                    atomicState.ShouldHeat = ShouldHeat
                    log.debug "ShouldCoolWithAC if $CurrTemp >= $CSPSet (loop $loopValue)"

                    // Now, before finally setting temperatures, pull current setpoint and compare to avoid redundencies

                    def CurrentCoolingSetPoint = ThermSet.currentValue("coolingSetpoint") 
                    def CurrentHeatingSetPoint = ThermSet.currentValue("heatingSetpoint") 

                    //log.debug "loading updated $CurrMode Cooling and Heating Set Points for $ThermSet"     
                    atomicState.withinApp = true


                    log.trace """($ThermSet: 
ShouldCoolWithAC = $ShouldCoolWithAC, 
ShouldHeat = $ShouldHeat
Current setpoint for $ThermSet is $CurrentCoolingSetPoint, 
Current Heating setpoint is $CurrentHeatingSetPoint,
Final CSPSet is $CSPSet
Current Set Points for $ThermSet are: cooling: $CurrentCoolingSetPoint, heating:CurrentHeatingSetPoint 
"""

                    // finally set devices' temperatures..
                    atomicState.withinApp = true
                    if(CurrentCoolingSetPoint != CSPSet){
                        ThermSet.setCoolingSetpoint(CSPSet)
                    }
                    else 
                    {
                        log.debug "Cooling SetPoint already set to $CSPSet for $ThermSet"
                    }
                    if(CurrentHeatingSetPoint != HSPSet){
                        ThermSet.setHeatingSetpoint(HSPSet)
                    }
                    else 
                    { 
                        log.debug "Heating SetPoint already set to $HSPSet for $ThermSet"
                    }
                    def ThisIsExceptionTherm =  false
                    if(NoTurnOffOnContact){
                        ThisIsExceptionTherm = "${ThermSet}" == "${Thermostat_1}"

                    }
                    else {
                        ThisIsExceptionTherm =  false

                        log.debug "No exception contact selected by user, ThisIsExceptionTherm set to false by default"
                    }
                    log.trace """
ThisIsExceptionTherm is: $ThisIsExceptionTherm (${ThermSet} == ${Thermostat_1})
ContactExceptionIsClosed = $ContactExceptionIsClosed"""

                    /////////////////////////MODIFICATIONS//////////////////////////yh
                    log.debug "doorsOk = $doorsOk"
                    if(doorsOk || (ContactExceptionIsClosed && ThisIsExceptionTherm)){

                        log.debug "turnOffWhenReached =  $turnOffWhenReached"
                        if(!ShouldCoolWithAC && !ShouldHeat && ThermState != "off" ){
                            if(AltSensor && (!turnOffWhenReached || turnOffWhenReached) && ThermState != "off"){ 
                                if(ThermState != "off"){
                                    AppMgtTrue() // override test value
                                    log.debug "$ThermSet TURNED OFF"  
                                    LatestThermostatMode = "off"
                                    atomicState.withinApp = true
                                    ThermSet.setThermostatMode("off") 
                                }
                                else {
                                    log.debug "$ThermSet already set to off"
                                }
                            }
                            else if(turnOffWhenReached && !AltSensor && ThermState != "off"){
                                if(ThermState != "off"){
                                    AppMgtTrue() // override test value
                                    log.debug "$ThermSet TURNED OFF"  
                                    LatestThermostatMode = "off"
                                    atomicState.withinApp = true
                                    ThermSet.setThermostatMode("off") 
                                }
                                else {
                                    log.debug "$ThermSet already set to off"
                                }
                            }
                        }
                        else if(ShouldCoolWithAC){
                            if(ThermState != "cool"){
                                AppMgtTrue() 
                                log.debug "$ThermSet set to cool"
                                LatestThermostatMode = "cool"
                                atomicState.withinApp = true
                                ThermSet.setThermostatMode("cool")                 
                            }
                            else {
                                log.debug "$ThermSet already set to cool"
                            }
                        }
                        else if(ShouldHeat && ThermState != "heat"){
                            if(ThermState != "heat"){
                                AppMgtTrue()
                                log.debug "$ThermSet set to Heat"
                                LatestThermostatMode = "heat"
                                atomicState.withinApp = true
                                ThermSet.setThermostatMode("heat")   
                            }
                            else {
                                log.debug "$ThermSet already set to heat"
                            }
                        } 

                    }
                    else {
                    log.debug "Not evaluating for $ThermSet because some windows are open"
                    }
                }
                else {
                    log.debug "${ThermSet} in OVERRIDE MODE, doing nothing"   
                }

                log.trace " END OF WHILE $loopValue" 

            }   

            // true end of while loop

        }
        else { 
            log.debug "not evaluating because some windows are open" 
        }
    }
    else {
        log.debug "HANDLERS NOT DONE YET"
    }

    withinAppFALSE()
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

    def ShouldCoolWithAC = atomicState.ShouldCoolWithAC
    def ShouldHeat = atomicState.ShouldHeat
    def LatestThermMode = "null"

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

    /*
if(loopValue == 1){
atomicState.LatestThermostatMode_T1 = LatestThermostatMode
}
else if(loopValue == 2){
atomicState.LatestThermostatMode_T2 = LatestThermostatMode
}
else if(loopValue == 3){
atomicState.LatestThermostatMode_T3 = LatestThermostatMode
}
else if(loopValue == 4){
atomicState.LatestThermostatMode_T4 = LatestThermostatMode
}*/

    return LatestThermMode
}

// A.I. and micro location evt management
def motionSensorHandler(evt){
    atomicState.Motionhandlerrunning = true

    def deltaMinutes = minutesMotion * 60000 as Long
    def motionEvents1 = MotionSensor_1.eventsSince(new Date(now() - deltaMinutes))
    def timeInMin = deltaMinutes/60000
    if(motionEvents1.find{it.value == "active" }){
        atomicState.isInActive1 = false
    }
    else {
        atomicState.isInActive1 = true
    }
    def motionEvents2 = MotionSensor_2.eventsSince(new Date(now() - deltaMinutes))
    if(motionEvents2.find{it.value == "active" }){
        atomicState.isInActive2 = false
    }
    else {
        atomicState.isInActive2 = true
    }
    def motionEvents3 = MotionSensor_3.eventsSince(new Date(now() - deltaMinutes))
    if(motionEvents3.find{it.value == "active" }){
        atomicState.isInActive3 = false
    }
    else {
        atomicState.isInActive3 = true
    }

    log.trace """
$evt.value motion @ $evt.device
deltaMinutes = $deltaMinutes
Found ${motionEvents1?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_1
Found ${motionEvents2?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_2
Found ${motionEvents3?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_3

$MotionSensor_1 is INACTIVE = $atomicState.isInActive1
$MotionSensor_2 is INACTIVE = $atomicState.isInActive2
$MotionSensor_3 is INACTIVE = $atomicState.isInActive3
"""
    atomicState.Motionhandlerrunning = false
    def minute = 1000*60

    if(now() > atomicState.now + (1*minute)){
        Evaluate()   
        atomicState.now = now()
    }
    atomicState.Motionhandlerrunning = false
    Evaluate()


}
def HumidityHandler(evt){

    log.info "humidity value is ${evt.value}%"


    atomicState.humidity = evt.value

}
def WindHandler(evt){

    log.info "Wind value is ${evt.value}mph"  

    atomicState.wind = evt.value

}
def FeelsLikeHandler(evt){

    log.info "Currently, outside temperature feels like ${evt.value}F"  

    atomicState.FeelsLike = evt.value

}

def switchHandler(evt){


    //log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        atomicState.exception = true
    } else {
        atomicState.exception = false
    }



    Evaluate()

}
def contactExceptionHandlerOpen(evt) {

    atomicState.thisIsWindowMgt = true

    atomicState.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Turning off all thermostats in $TimeBeforeClosing seconds"

    if(OperatingTime){
        runIn(TimeBeforeClosing, TurnOffThermostats)  
    }
    else{
        TurnOffThermostats()
    }
    runIn(60, thisIsWindowMgtFALSE)
}
def contactExceptionHandlerClosed(evt) {
    atomicState.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Resuming Evaluation for $thermostats_1"

    Evaluate()
}

// Main events management0
def temperatureHandler(evt) {

    atomicState.handlerrunning = true
    def doorsOk = AllContactsAreClosed()

    if(evt.device == XtraTempSensor) {
        atomicState.Inside = evt.value

    }

    def currentTemp = atomicState.Inside
    log.info """
current temperature value for $evt.device is $evt.value
Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp
"""

    if(evt.device in Thermostat_1) {

        atomicState.currentTemperatureT1 = evt.value 
    }
    else if(evt.device in Thermostat_2) {
        atomicState.currentTemperatureT2 = evt.value 
    }
    else if(evt.device in Thermostat_3) {
        atomicState.currentTemperatureT3 = evt.value 
    }
    else if(evt.device in Thermostat_4) {
        atomicState.currentTemperatureT4 = evt.value 
    }


    if(currentTemp <= CriticalTemp) {
        log.info "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 

        Thermostat_1.setThermostatMode("heat") 

        if(Thermostat_2){
            Thermostat_2.setThermostatMode("heat") 
        }
        if(Thermostat_3){Thermostat_3.setThermostatMode("heat") 
                        }
        if(Thermostat_4){Thermostat_4.setThermostatMode("heat") 
                        }


        atomicState.CRITICAL = true


        def message = ""
        if(Actuators && !doorsOk){
            //log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                message = "Closing windows because $state.causeClosed"
                send(message)
                Actuators?.off()
                ActuatorException?.off()
                // allow for user to reopen them if they want to. 
                state.windowswereopenandclosedalready = true // windows won't close again as long as temperature is still critical to allow for user's override 
                // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
            }
            else { 
                message = "doors and windows already reopened by user so not running emergency closing. BEWARE! these windows will not close again"
                log.info message
                send(message)

            }
        } 
        atomicState.TheresBeenCriticalEvent = true
    } 
    else 
    { 
        //log.debug "CriticalTemp OK"
        atomicState.CRITICAL = false
        state.windowswereopenandclosedalready = false
        if(!MainContactsClosed){

            atomicState.withinApp = true
            TurnOffThermostats

        }
        atomicState.TheresBeenCriticalEvent = false
    } 
    atomicState.handlerrunning = false
    Evaluate()
}
def contactHandlerClosed(evt) {

    atomicState.thisIsWindowMgt = true // prevent false ON/OFF override

    def message = ""
    def ContactsClosed = MainContactsClosed()

    //log.debug "$evt.device is $evt.value" 

    log.info "List of devices' status is $CurrentContactsState"


    if(!ContactsClosed){
        log.debug "Not all contacts are closed, doing nothing"

    }
    else {      
        //log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
        unschedule(TurnOffThermostats) // in case were closed within time frame

        //log.debug "atomicState.ClosedByApp = $atomicState.ClosedByApp"

        if(atomicState.ClosedByApp == false && atomicState.OpenByApp == true && evt.value == "closed"){ 

            message = "WINDOWS MANUALY CLOSED they will not open again until you open them yourself"
            log.info message
            send(message)
            AppMgtTrue()
            runIn(2, Evaluate)
        }
    }
} 
def contactHandlerOpen(evt) {
    atomicState.thisIsWindowMgt = true
    //log.debug "$evt.device is now $evt.value, Turning off all thermostats in $TimeBeforeClosing seconds"

    atomicState.thisIsWindowMgt = true // prevent false ON/OFF override


    runIn(TimeBeforeClosing, TurnOffThermostats)   
    def message = ""

    //log.debug "atomicState.OpenByApp = $atomicState.OpenByApp"
    if(atomicState.OpenByApp == false && atomicState.ClosedByApp == true && evt.value == "open"){ 

        message = "WINDOWS MANUALY OPENED windows will not close again until you close them yourself"
        log.info message
        send(message)

    }

    runIn(60, thisIsWindowMgtFALSE)
}
def ChangedModeHandler(evt) {

    //log.debug "mode changed to ${evt.value}"
    def ContactsClosed = MainContactsClosed()

    if(ContactsClosed) {
        // windows are closed 
        atomicState.ClosedByApp = true // app will open windows if needed 
        atomicState.OpenByApp = false 
        // has to be the default value so it doesn't close again if user opens windows manually or another app. 
        // Beware that this can be a serious safety concern (for example, if a user has these windows linked to a smoke detector
        // so do not modify these parameters under any circumstances 
        // and check that this works after any modification you'd bring to this app

        atomicState.ThermOff = false

    } 

    updated()

}

//override management
def setpointHandler(evt){
    log.trace """$evt.device set to $evt.value (setpointHandler)
atomicState.withinApp = $atomicState.withinApp"""

    atomicState.handlerrunning = true

    // declare an integer value for the thermostat which has had its values modified
    def MapModesThermostats = ["${Thermostat_1}": "1" , "${Thermostat_2}": 2, "${Thermostat_3}": "3", 
                               "${Thermostat_4}": "4"]
    def KeyValueForThisTherm = MapModesThermostats.find { it.key == "$evt.device"}
    log.info "device is ------------------- $KeyValueForThisTherm.value"
    def ThermNumber = KeyValueForThisTherm.value
    ThermNumber = KeyValueForThisTherm.value.toInteger()

    log.info "ThermNumber is ------------------- $ThermNumber"


    def AppMgtList = [null, atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
    def AppMgt = AppMgtList[ThermNumber]
    //log.debug "AppMgt at SetpointHandler for $ThermNumber is $AppMgt"

    if(AppMgt){

        if(atomicState.withinApp == false){
            def CurrMode = location.currentMode

            def HomeMode = null
            def ThisIsManual = false 
            def reference = null
            def termRef = null
            def AltSENSOR = false    

            log.info "Home modes are : $Home, Night modes are : $Night, Away mode is : $Away, CustomMode1 are : $CustomMode1, CustomMode2 are : $CustomMode2"

            //log.debug "CurrMode is $CurrMode mode"

            //array heat
            def HSPH = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPH4"]
            def HSPN = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPN4"]
            def HSPA = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
            def HSPCust1 = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPCust1_T4"]
            def HSPCust2 = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPCust2_T4"]


            // declare an integer value for current mode
            def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
            def ModeIndexValue = MapofIndexValues.find{ it.key == "$CurrMode"}
            //log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
            ModeIndexValue = ModeIndexValue.value

            ModeIndexValue = ModeIndexValue.toInteger()
            log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"

            //array cool

            def CSPH = ["0","$atomicState.newValueT1CSP", "$atomicState.newValueT2CSP", "$atomicState.newValueT3CSP", "$CSPH4"]
            def CSPN = ["0","$atomicState.newValueT1CSP", "$atomicState.newValueT2CSP", "$atomicState.newValueT3CSP", "$CSPN4"]
            def CSPA = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
            def CSPCust1 = ["0","$atomicState.newValueT1CSP", "$atomicState.newValueT2CSP", "$atomicState.newValueT3CSP", "$CSPCust1_T4"]
            def CSPCust2 = ["0","$atomicState.newValueT1CSP", "$atomicState.newValueT2CSP", "$atomicState.newValueT3CSP", "$CSPCust2_T4"]

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
            // by declaring the atomicState.AppMgnt_T_X variable as false.  

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


            def ThisIsModeChange = atomicState.locationModeChange
            def ExceptionState = atomicState.exception
            def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState


            def Value = evt.value
            //def Value = Math.round(Double.parseDouble(evt.value))
            Value = Value.toInteger()
            //log.debug "Evt value to Integer is : $Value and it is to be compared to reference: $reference"


            log.trace """
RefHeat for $evt.device is: $RefHeat 
RefCool for $evt.device is: $RefCool 
reference for $evt.device is: $reference
SetPoint Change was Manual? ($atomicState.ThisIsManual) if false then should have $reference = $Value 
(unless location mode just changed or Exception Switch is on or ThisIsMotion or ThisIsLinearEq)
"""

            def doorsOk = AllContactsAreClosed()

            if(Value == reference || ThisIsModeChange ||  (!doorsOk && !thisIsExceptionTemp))
            {  
                log.debug "NO SETPOINT OVERRIDE"
                atomicState.ThisIsManual = false

            }
            else {       
                atomicState.ThisIsManual = true
                log.debug "MANUAL SETPOINT OVERRIDE for $evt.device"
                def message = "user set temperature manually on $evt.device"
                log.info message



            }
            //     
            if(evt.displayName == "${Thermostat_1}")
            {

                if(atomicState.ThisIsManual == true){
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T1_AppMgt != false) {
                        atomicState.T1_AppMgt = false
                        atomicState.T1_AppMgtSP = false
                        log.info "atomicState.T1_AppMgt set to $atomicState.T1_AppMgt"
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_1"
                    }
                }
                else {
                    atomicState.T1_AppMgt = true
                    atomicState.T1_AppMgtSP = true
                    log.info "atomicState.T1_AppMgt set to $atomicState.T1_AppMgt"
                }

            }
            else if(evt.displayName == "${Thermostat_2}")
            {
                if(atomicState.ThisIsManual == true){
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T2_AppMgt != false) {
                        atomicState.T2_AppMgt = false
                        atomicState.T2_AppMgtSP = false

                        log.info "atomicState.T2_AppMgt set to $atomicState.T2_AppMgt"
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_2"
                    }
                }
                else {
                    atomicState.T2_AppMgt = true
                    atomicState.T2_AppMgtSP = true
                    log.info "atomicState.T2_AppMgt set to $atomicState.T2_AppMgt"
                }
            }
            else if(evt.displayName == "${Thermostat_3}")
            {
                if(atomicState.ThisIsManual == true){
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T3_AppMgt != false) {
                        atomicState.T3_AppMgt = false
                        atomicState.T3_AppMgtSP = false
                        log.info "atomicState.T3_AppMgt set to $atomicState.T3_AppMgt"
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_3"
                    }
                }
                else {
                    atomicState.T3_AppMgt = true
                    atomicState.T3_AppMgtSP = true
                    log.info "atomicState.T3_AppMgt set to $atomicState.T3_AppMgt"
                }
            }
            else if(evt.displayName == "${Thermostat_4}")
            {
                if(atomicState.ThisIsManual == true){
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T4_AppMgt != false) {
                        atomicState.T4_AppMgt = false
                        atomicState.T4_AppMgtSP = false
                        log.info "atomicState.T4_AppMgt set to $atomicState.T4_AppMgt"
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_4"
                    }
                }
                else {
                    atomicState.T4_AppMgt = true
                    atomicState.T4_AppMgtSP = true
                    log.info "atomicState.T4_AppMgt set to $atomicState.T4_AppMgt"
                }
            }   


            atomicState.RefHeat = RefHeat
            atomicState.RefCool = RefCool

        }
        else{
            //log.debug "Not evaluating SETPOINT OVERRIDE for $evt.device because the command came from the app"
        }

    }
    else {
        //log.debug "$evt.device already in OVERRIDE MODE, not checking SETPOINT override"
    }

    atomicState.handlerrunning = false
}
def ThermostatSwitchHandler(evt){

    atomicState.handlerrunning = true

    log.trace """$evt.device set to $evt.value (ThermostatSwitchHandler)
atomicState.withinApp = $atomicState.withinApp"""


    def LatestThermMode = LatestThermMode()

    //log.debug "CHECKING COMMAND ORIGIN for $evt.device -- INFO: atomicState.withinApp = $atomicState.withinApp  "
    if(atomicState.withinApp == false){
        if(atomicState.CRITICAL == false){
            def CurrMode = location.currentMode
            def LocatioModeChange = atomicState.locationModeChange
            def thisIsWindowMgt = atomicState.thisIsWindowMgt
            //log.debug "Location Mode Changed?($LocatioModeChange)"

            log.trace "Latest Thermostat ModeS : $atomicState.LatestThermostatMode_T1 | $atomicState.LatestThermostatMode_T2 | $atomicState.LatestThermostatMode_T3 | $atomicState.LatestThermostatMode_T4"

            // is current state app managed or manually set?
            //log.debug " for $evt.device shoudlbe: evt.value($evt.value) =? LatestThermMode($LatestThermMode)"
            def IdenticalShouldbe = evt.value == LatestThermMode 
            //log.debug "IDENTICAL?($IdenticalShouldbe)"


            def ThereWasChange = !IdenticalShouldbe && !LocatioModeChange  && !thisIsWindowMgt 


            log.trace """
Change($ThereWasChange)
thisIsExceptionTemp?($thisIsExceptionTemp)
thisIsWindowMgt?($thisIsWindowMgt)
"""

            if(evt.displayName == "${Thermostat_1}" ){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO MANUAL ON/OFF OVERRIDE for $Thermostat_1"
                    atomicState.T1_AppMgt = true
                }
                else { 
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T1_AppMgt != false) {
                        // command did not come from app so manual or set point is manual override is on
                        log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_1"
                        atomicState.T1_AppMgt = false
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_1"
                    }
                }       
            }
            else if(evt.displayName == "${Thermostat_2}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                    atomicState.T2_AppMgt = true
                }
                else {
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T2_AppMgt != false) {
                        // command did not come from app so manual override is on
                        log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                        atomicState.T2_AppMgt = false 
                    }
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_1"
                    }
                }
            } 
            else if(evt.displayName == "${Thermostat_3}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_3"
                    atomicState.T3_AppMgt = true
                }
                else {
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T3_AppMgt != false) {
                        // command did not come from app so manual override is on
                        log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_3"
                        atomicState.T3_AppMgt = false

                    }
                    else { 
                        log.debug "OVERRIDE ALREADY SET FOR $Thermostat_1"
                    }
                } 
            }
            else if(evt.displayName == "${Thermostat_4}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    //log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_4"
                    atomicState.T4_AppMgt = true
                }
                else {
                    //let's first make sure there's not already an override for that one
                    if(atomicState.T4_AppMgt != false) {
                        // command did not come from app so manual override is on
                        log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_4"
                        atomicState.T4_AppMgt = false
                    } 
                    else { 
                        //log.debug "OVERRIDE ALREADY SET FOR $Thermostat_1"
                    }
                }
            }
        }
        else { 
            log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" 
        }

    }
    else {
        log.debug "Not evaluating ON/OFF COMMAND OVERRIDE for $evt.device because the command came from app"
    }
    atomicState.handlerrunning = false
}
def thisIsWindowMgtFALSE(){
    //log.debug "Reset atomicState.thisIsWindowMgt to FALSE"
    atomicState.thisIsWindowMgt = false
}
def withinAppFALSE(){

    if(atomicState.handlerrunning == false){
        log.debug "Reset atomicState.withinApp to FALSE"
        atomicState.withinApp = false // this value is reset to false so if there's a manual setpoint override it'll be detected as such
    }
    else {
        log.debug "no reset, handlers running"
    }
}
def AppMgtTrue(){

    def loopValue = atomicState.loopValue

    if(loopValue == 1){
        atomicState.T1_AppMgt = true
        log.debug "atomicState.T1_AppMgt set to true -"
    }
    else if(loopValue == 2){
        atomicState.T2_AppMgt = true
        log.debug "atomicState.T2_AppMgt set to true -"
    }
    else if(loopValue == 3){
        atomicState.T3_AppMgt = true
        log.debug "atomicState.T3_AppMgt set to true -"
    }
    else if(loopValue == 4){
        atomicState.T4_AppMgt = true
        log.debug "atomicState.T4_AppMgt set to true -"
    }
}

def resetLocationChangeVariable(){
    atomicState.locationModeChange = false
    //log.debug "atomicState.locationModeChange reset to FALSE"
}

// contacts and windows management
def MainContactsClosed(){

    def MainContactsAreClosed = true // has to be true by default in case no contacts selected

    // //log.debug "Maincontacts are $Maincontacts"

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def ClosedContacts = CurrentContacts.findAll { AllcontactsAreClosed ->
        AllcontactsAreClosed == "closed" ? true : false}

    MainContactsAreClosed = ClosedContacts.size() == Maincontacts.size() 

    // //log.debug "${ClosedContacts.size()} windows/doors out of ${Maincontacts.size()} are closed SO MainContactsAreClosed = $MainContactsAreClosed"

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
        // //log.debug "${ClosedContactsExpt.size()} windows/doors out of ${ContactException.size()} are closed SO ContactsExepClosed = $ContactsExepClosed"

        def CurrTherMode = Thermostat_1.currentValue("thermostatMode") as String
        // //log.debug "Current Mode for $Thermostat_1 is $CurrTherMode"
        if(CurrTherMode != "off" && !ContactsExepClosed){
            // //log.debug "$Thermostat_1 is on, should be off. Turning it off" 
            Thermostat_1.setThermostatMode("off") 
            atomicState.LatestThermostatMode_T1 = "off"
        }
    }
    return ContactsExepClosed

}
def AllContactsAreClosed() {

    def AllContactsClosed = MainContactsClosed() && ExcepContactsClosed()
    // //log.debug "AllContactsAreClosed() returns AllContactsClosed = $AllContactsClosed"

    return AllContactsClosed
}
def AllContactsAreOpen() {

    def MainContactsAreAllOpen = false // has to be true by default in case no contacts selected

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def OpenContacts = CurrentContacts.findAll { AllcontactsAreOpen ->
        AllcontactsAreOpen == "open" ? true : false}
    //  //log.debug "${OpenContacts.size()} windows/doors out of ${Maincontacts.size()} are open SO ContactsExepOpen = $ContactsExepOpen"
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

    def ThermState1 = Thermostat_1?.currentValue("thermostatMode") as String   
    def ThermState2 = Thermostat_2?.currentValue("thermostatMode") as String
    def ThermState3 = Thermostat_3?.currentValue("thermostatMode") as String
    def ThermState4 = Thermostat_4?.currentValue("thermostatMode") as String

    log.trace "TurnOffThermostats || $ThermState1 || ThermState2 || ThermState3 || ThermState4"

    def doorsOk = AllContactsAreClosed()

    def ContactExceptionIsClosed = ExcepContactsClosed()

    if(atomicState.CRITICAL == false){

        def InExceptionContactMode = location.currentMode in DoNotTurnOffModes

        log.trace "Turning off thermostats: ContactExceptionIsClosed: $ContactExceptionIsClosed, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"

        if(!NoTurnOffOnContact || !InExceptionContactMode || !ContactExceptionIsClosed){
            if(ThermState1 != "off"){
                atomicState.withinApp = true // to avoid false end of override while windows are open and exception thermostat still needs to remain in override mode. 

                Thermostat_1.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                //log.debug "$Thermostat_1  turned off"

            }
        }
        else {

            //log.debug "Not turning off $Thermostat_1 because current mode is within exception modes selected by the user"

            atomicState.LatestThermostatMode = ThermState1
        }


        if(Thermostat_2){
            if(ThermState2 != "off"){  

                atomicState.withinApp = true 
                Thermostat_2.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                //log.debug "$Thermostat_2 turned off"
            }
        }
        if(Thermostat_3){
            if(ThermState3 != "off"){
                atomicState.withinApp = true 
                Thermostat_3.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                //log.debug "$Thermostat_3 turned off"

            }
        }
        if(Thermostat_4){
            if(ThermState4 != "off"){
                atomicState.withinApp = true 
                Thermostat_4.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                //log.debug "$Thermostat_4 turned off"
            }
        }
        atomicState.ThermOff = true
    }
    else { 
        log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 


    }
}

def CheckWindows(){


    def MessageMinutes = 60*60000 as Long
    def MessageTimeDelay = now() > atomicState.LastTimeMessageSent + MessageMinutes

    // for when it previously failed to turn off thermostats
    def AllContactsClosed = AllContactsAreClosed()



    //log.debug "Checking windows"
    def OkToOpen = OkToOpen() // outside and inside temperatures criteria and more... 

    def message = ""
    def allContactsAreOpen = AllContactsAreOpen()
    def ContactsClosed = AllContactsAreClosed()


    //log.debug "Contacts closed?($ContactsClosed)"


    def Inside = atomicState.Inside
    //log.debug "Inside = $Inside"
    def Outside = OutsideSensor.currentValue("temperature")
    //log.debug "Outside = $Outside"

    log.trace """
OkToOpen?($OkToOpen); 
OffSet?($OffSet) 
atomicState.ClosedByApp($atomicState.ClosedByApp) 
atomicState.OpenByApp($atomicState.OpenByApp) 
atomicState.messageSent($atomicState.messageSent) 

"""


    if(OkToOpen){

        if(ContactsClosed){

            if(atomicState.ClosedByApp == true) {

                Actuators?.on()

                atomicState.OpenByApp = true
                atomicState.ClosedByApp = false // so it doesn't open again

                //log.debug "opening windows"

                if(OperatingTime){
                    message = "I'm opening windows because $atomicState.causeOpen. Operation time is $OperatingTime seconds"
                    runIn(OperatingTime, StopActuators) 
                }
                else {
                    message = "I'm opening windows because $atomicState.causeOpen"
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
    else if (atomicState.OpenByApp == true && !AllContactsClosed) {

        Actuators?.off()
        ActuatorException?.off()

        message = "I'm closing windows because $atomicState.causeClose"
        send(message)
        log.info message 

        atomicState.ClosedByApp = true
        atomicState.OpenByApp = false // so it doesn't close again if user opens it manually


    }
}
def OkToOpen(){
    def message = ""
    //log.debug "Checking if it's O.K. to Open windows"
    def ContactsClosed = AllContactsAreClosed()

    def CSPSet = atomicState.CSPSet
    def HSPSet = atomicState.HSPSet 

    def CurrMode = location.currentMode
    def Inside = atomicState.Inside
    def CurrTemp = Inside
    def Outside = OutsideSensor.currentValue("temperature") 
    def outsideTemp = Outside
    atomicState.outsideTemp = Outside
    def WithinCriticalOffSet = (Inside >= (CriticalTemp + OffSet)) && (Outside >= (CriticalTemp + OffSet))

    def OutsideTempHighThres = ExceptACModes()
    def ExceptHighThreshold1 = ExceptHighThreshold1


    def humidity = atomicState.humidity
    humidity = humidity.toInteger()
    //log.debug "Inside = $Inside | Outside = $Outside"
    def TooHumid = humidity > HumidityTolerance && Outside > Inside


    def WindValue = atomicState.wind
    WindValue = WindValue.toInteger()
    TooHumid = (humidity > (HumidityTolerance - 10)) && WindValue <= 2 && Outside > Inside

    def ItfeelsLike = atomicState.FeelsLike
    ItfeelsLike = ItfeelsLike.toInteger()
    def OutsideFeelsHotter = ItfeelsLike > Outside + 2

    //log.debug "Does it feel like hotter outside?($OutsideFeelsHotter)"

    def ShouldCool = outsideTemp >= OutsideTempLowThres 
    def ShouldHeat = (outsideTemp <= OutsideTempLowThres || CurrTemp <= HSPSet) && !ShouldCool
    if(ShouldCool && ShouldHeat) {
        ShouldCool = false
    }
    if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet && outsideTemp >= CSPSet) {
        ShouldCool = true
    }        

    //log.debug "ShouldCool = $ShouldCool -|-"



    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4 && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    }


    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid



    state.OpenInFull = false
    if(OpenWhenEverPermitted) { 
        state.OpenInFull = true
    }

    // open all the way when gone?
    if(CurrMode in $Away && WithinCriticalOffSet && OpenInfullWhenAway ){
        result = true
        state.OpenInFull = true
    } 


    if(!result){
        // preparing a dynamic message which will tell why windows won't open (or fans won't turn on)
        def cause1 = !OutSideWithinMargin
        def cause2 = !WithinCriticalOffSet
        def cause3 = !ShouldCool
        def cause4 = TooHumid

        def causeNotList = [ cause1, cause2, cause3, cause4]

        def causeNotTest = causeNotList.findAll{ val ->
            val == true ? true : false
        }

        def and2 =" and"
        def and3 = " and"
        def and4 = " and"
        def outsideWord = "outside"

        if(!cause1){
            and2 = "" 
        }
        if(!cause2 ) {
            and3 = ""             
        }
        if(!cause3){
            and4 = ""
        }
        if(cause4)
        outsideWord = ""


        def causeNotMap = [ "outside temperature is not within user's comfortable margin" : cause1,  
                           "$and2 it is not too hot inside ${XtraTempSensor}'s room" : cause2 , 
                           "$and3 it is too hot $outsideWord" : cause3 ,  
                           "$and4 it is too humid outisde" : cause4 ]

        // creates a new map with only the keys that have values = true
        def causeNotOkToOpen = causeNotMap.findAll{it.value == true}
        // now get only the keys from this map 
        causeNotOkToOpen = causeNotOkToOpen.collect{ it.key }
        // build a string without the parentheses 
        def MessageStr = new StringBuilder();
        for (String value : causeNotOkToOpen) {
            MessageStr.append(value);
        }
        causeNotOkToOpen = MessageStr.toString();
        atomicState.causeClose = causeNotOkToOpen

        message = "Windows are closed because $causeNotOkToOpen"
        log.info message
        state.messageclosed = message
        // send a reminder every X minutes 
        def MessageMinutes = 60*60000 as Long
        def MessageTimeDelay = now() > atomicState.LastTimeMessageSent + MessageMinutes

        if(MessageTimeDelay && ContactsClosed) {
            send(message)
            atomicState.LastTimeMessageSent = now()
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
        atomicState.causeOpen = causeOkToOpen

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
Too Humid?($TooHumid)
Humidity is: $humidity
OkToOpen?($result)
"""

    /// FOR TESTS 
    // result = false


    return result
}

def StopActuators(){

    def SlightOpen = atomicState.SlightOpen

    def OpenInFull = state.OpenInFull
    //log.debug "STOP"
    if (Actuators?.hasCommand("stop") && !OpenInFull){
        Actuators?.stop()
    }
    if (ActuatorException?.hasCommand("stop") && !OpenInFull){
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
    runIn(20, StopActuators)

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

    //  schedule("0 0/$scheduledTimeA * * * ?", Evaluate)
    // //log.debug "Evaluate scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    //log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        //// //log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }

    Evaluate()
}
def polls(){

    def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
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
def send(msg) {
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


