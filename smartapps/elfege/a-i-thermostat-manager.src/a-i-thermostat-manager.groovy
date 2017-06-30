definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """Manage one or more thermostats in parallel with several other features such as: 
- Home location mode (up to 5 sets of modes)
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

- Home location mode (up to 5 sets of modes)
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

        section("how many thermostats do you want to control?") { 
            input(name: "HowMany", type: "number", range: "1..4", title: "set a value between 1 and 4", description: null, submitOnChange: true)
            if(HowMany >= 1) {
                input(name: "Thermostat_1", type: "capability.thermostat", title: "Thermostat 1 is $Thermostat_1", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_1", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_1){
                    input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_1", required: true, multiple: false, description: null, uninstall: true)
                }
                input(name: "NoTurnOffOnContact", type: "bool", title: "Do not turn off this unit upon contacts events", default: false, submitOnChange: true, description: "this is to be applied with a specific mode in the next section")

            }
            if(HowMany >= 2) {
                input(name: "Thermostat_2", type: "capability.thermostat", title: "Thermostat 2 is $Thermostat_2", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_2", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_2){
                    input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_2", required: true, multiple: false, description: null, uninstall: true)
                }


            }
            if(HowMany >= 3) {
                input(name: "Thermostat_3", type: "capability.thermostat", title: "Thermostat 3 is $Thermostat_3", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_3", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_3){
                    input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_3", required: true, multiple: false, description: null, uninstall: true)
                }

            }
            if(HowMany == 4) {
                input(name: "Thermostat_4", type: "capability.thermostat", title: "Thermostat 4 is $Thermostat_4", required: false, multiple: false, description: null, submitOnChange: true)
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
                    def set = AddDegrees.toInteger()
                    input(name: "SubDegrees", type: "decimal", title:"Enter the same value", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
                    if(AddDegrees){
                        log.info "SubDegrees = $SubDegrees"
                    }
                }
                else if(warmerorcooler == "cooler"){        
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $Thermostat_1 for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = SubDegrees.toInteger()
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
                paragraph "Select the windows' actuator (or fans' switches) associated to this exception contact sensor"
                input(name: "ActuatorException", type: "capability.switch", required: false, multiple: true, title: "select the switches that may conrol this windows/door", submitOnChange: true)
                paragraph: """
$ActuatorException will open/close with wheather like $Actuactors. However its corresponding contact sensor will not turn off AC or heater if other contacts sensors are open"""

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

            def hasHumidity = OutsideSensor.hasAttribute("humidity")
            log.debug "hasHumidity is $hasHumidity .."
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
                input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true)
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

            }
        }
        section("Micro Location"){
            input(name: "MotionSensor_1", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_1 settings when inactive", description: "Select a motion sensor", required: false)
            if(MotionSensor_1){
                input(name: "MotionModesT1", type: "mode", title: "Use $MotionSensor_1 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                input(name: "AddDegreesMotion1", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting", required: true)
                input(name: "SubDegreesMotion1", type: "decimal", title: "Substract this amount of degrees to $Thermostat_1 cooling setting", required: true)   
            }
            input(name: "MotionSensor_2", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_2 settings when inactive", description: "Select a motion sensor", required: false)
            if(MotionSensor_2){
                input(name: "MotionModesT2", type: "mode", title: "Use $MotionSensor_2 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                input(name: "AddDegreesMotion2", type: "decimal", title: "Add this amount of degrees to $Thermostat_2 heat setting", required: true)
                input(name: "SubDegreesMotion2", type: "decimal", title: "Substract this amount of degrees to $Thermostat_2 cooling setting", required: true)      
            }
            input(name: "MotionSensor_3", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_3 settings when inactive", description: "Select a motion sensor", required: false)
            if(MotionSensor_3){
                input(name: "MotionModesT3", type: "mode", title: "Use $MotionSensor_3 only if home is in these modes", multiple: true, description: "select a mode", required: true)
                input(name: "AddDegreesMotion3", type: "decimal", title: "Add this amount of degrees to $Thermostat_3 heat setting", required: true)
                input(name: "SubDegreesMotion3", type: "decimal", title: "Substract this amount of degrees to $Thermostat_3 cooling setting", required: true)      
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
    // log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler
    init()
}
def updated() {
    atomicState.modeStartTime = now() 


    log.info "updated with settings = $settings $Modes"

    // default values 
    atomicState.humidity = 0
    atomicState.wind = 4
    atomicState.FeelsLike = OutsideSensor.currentValue("temperature")

    unsubscribe()
    unschedule()



    init()
}
def init() {



    subscribe(contacts, "contact.open", contactHandlerOpen)
    subscribe(contacts, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(ContactException, "contact.open", contactExceptionHandlerOpen)
        subscribe(ContactException, "contact.closed", contactExceptionHandlerClosed)
        subscribe(ContactException, "contact.open", contactHandlerOpen)
        subscribe(ContactException, "contact.closed", contactHandlerClosed)
        log.debug "subscribed ContactException to ContactException Handler"     
    }

    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    subscribe(Thermostat_1, "temperature", temperatureHandler)
    subscribe(Thermostat_1, "thermostatMode", ThermostatSwitchHandler)


    subscribe(OutsideSensor, "temperature", temperatureHandler)
    def hasHumidity = OutsideSensor.hasAttribute("humidity")
    log.debug "hasHumidity is $hasHumidity"
    if(hasHumidity){
        subscribe(OutsideSensor, "humidity", HumidityHandler)

    }
    def hasFeelsLike = OutsideSensor.hasAttribute("feelsLike")
    if(hasFeelsLike){
        subscribe(OutsideSensor, "feelsLike", FeelsLikeHandler)

    }
    def hasWind = OutsideSensor.hasAttribute("wind")
    if(hasWind){
        subscribe(OutsideSensor, "wind", HumidityHandler)

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
            log.debug "Subscription for alternative Sensor for $Sensor_1"

        }
        if(AltSensor_2){
            subscribe(Sensor_2, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_2"

        }
        if(AltSensor_3){
            subscribe(Sensor_3, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_3"

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



    // first default values to be set to any suitable value so it doesn't crash with null value 
    // they will be updated within seconds with user's settings 
    atomicState.newValueT1CSP = 75
    atomicState.newValueT1HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75


    atomicState.AllowToRunMainLoop = true // for motion handler 
    
    atomicState.ThisIsManual = false

    def ContactsClosed = allContactsTest()
    log.debug "enter updated, state: $state"  


    if(ContactsClosed) {
        // windows are closed 
        atomicState.ClosedByApp = true // app will open windows if needed 
        //atomicState.OpenByApp = false // so it doesn't close again if user opens it manually

    } 
    else {
        // windows are open so after mode change we want them to be able to close 
        atomicState.ClosedByApp = false //  so app won't open windows further
        atomicState.OpenByApp = true // always assume was opened by the app after mode change (or update) so it can close them if needed

        TurnOffThermostats()

    }

    state.messageTooHotWindows = 0
    atomicState.messageSent = 0

    atomicState.locationModeChange = true 

    atomicState.WhileMax = 50 // max iteration for while loops

    def Therm = ["0", "$Thermostat_1", "$Thermostat_2","$Thermostat_3", "$Thermostat_4"]
    def NullThermFind = Therm.findAll { val ->
        val == "null" ? true : false
    }
    def ThermsInvolved = Therm.size() - NullThermFind.size() - 1 
    // -1 because of index 0
    ThermsInvolved = ThermsInvolved.toInteger()
    atomicState.ThermsInvolved = ThermsInvolved
    log.debug "Number of Thermostats Selected by User : $ThermsInvolved [init]"



    runIn(10, resetLocationChangeVariable)

    // false positive overrides management 


    schedules()
    Evaluate()

}


// main loop
def Evaluate(){

    def doorsOk = allContactsTest()

    def ContactExceptionIsClosed = windowsExepTest()
    log.debug "doorsOk?($doorsOk), ContactExceptionIsClosed?($ContactExceptionIsClosed)"

    def CurrMode = location.currentMode
    def Outside = OutsideSensor?.currentValue("temperature") 
    def outsideTemp = Outside

    def humidity = atomicState.humidity
    humidity = humidity.toInteger()
    def TooHumid = humidity > HumidityTolerance

    log.debug "humidity is $humidity && TooHumid is $TooHumid"

    if(doorsOk || ContactExceptionIsClosed){

        log.trace "MAIN LOOP RUNNING"

        // log.debug "atomicState.ThisIsManual value is $atomicState.ThisIsManual"
        def CurrTemp1 = Thermostat_1?.currentTemperature 
        def ThermState1 = Thermostat_1?.currentValue("thermostatMode") as String
        def CurrTemp2 = Thermostat_2?.currentTemperature 
        def ThermState2 = Thermostat_2?.currentValue("thermostatMode") as String   
        def CurrTemp3 = Thermostat_3?.currentTemperature 
        def ThermState3 = Thermostat_3?.currentValue("thermostatMode") as String 
        def CurrTemp4 = Thermostat_4?.currentTemperature 
        def ThermState4 = Thermostat_4?.currentValue("thermostatMode") as String


        def CurrTemp_Alt1 = Sensor_1?.currentTemperature 
        def CurrTemp_Alt2 = Sensor_2?.currentTemperature 
        def CurrTemp_Alt3 = Sensor_3?.currentTemperature 
        def CurrTempList_Alt = [0, CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3, null]
        def AltSensorBoolList = [false, AltSensor_1, AltSensor_2, AltSensor_3, false]

        log.trace """T1 : $atomicState.T1_AppMgt, T2 : $atomicState.T2_AppMgt, T3 : $atomicState.T3_AppMgt, T4 : $atomicState.T4_AppMgt
Current Thermostats Modes: ThermState1: $ThermState1, ThermState2: $ThermState2, ThermState3: $ThermState3, ThermState4: $ThermState4"""

        // which HSP? 
        // log.debug "CurrMode is $CurrMode mode"
        //array heat
        def HSP1 = ["0","$HSPH1", "$HSPN1", "$HSPA", "$HSPCust1_T1", "$HSPCust2_T1"]
        def HSP2 = ["0","$HSPH2", "$HSPN2", "$HSPA", "$HSPCust1_T2", "$HSPCust2_T2"]
        def HSP3 = ["0","$HSPH3", "$HSPN3", "$HSPA", "$HSPCust1_T3", "$HSPCust2_T3"]
        def HSP4 = ["0","$HSPH4", "$HSPN4", "$HSPA", "$HSPCust1_T4", "$HSPCust2_T4"]
        //array cool
        def CSP1 = ["0","$CSPH1", "$CSPN1", "$CSPA", "$CSPCust1_T1", "$CSPCust2_T1"]
        def CSP2 = ["0","$CSPH2", "$CSPN2", "$CSPA", "$CSPCust1_T2", "$CSPCust2_T2"]
        def CSP3 = ["0","$CSPH3", "$CSPN3", "$CSPA", "$CSPCust1_T3", "$CSPCust2_T3"]
        def CSP4 = ["0","$CSPH4", "$CSPN4", "$CSPA", "$CSPCust1_T4", "$CSPCust2_T4"]
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
            log.debug "SwitchesOn($SwitchesOn)"
        }

        def HSP = ["0", "$HSP1", "$HSP2", "$HSP3", "$HSP4"]
        log.debug "HSP LIST is $HSP"
        def HSPSet = 0
        def CSP = ["0", "$CSP1", "$CSP2", "$CSP3", "$CSP4"]
        log.debug "CSP LIST is $CSP"
        def CSPSet = 0
        def LatestThermostatMode = null
        // thermostats list 
        def Therm = ["0", "$Thermostat_1", "$Thermostat_2","$Thermostat_3", "$Thermostat_4"]
        def ThermSet = 0

        def ThermDeviceList = ["null", Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
        def CurrTempList = ["0", CurrTemp1, CurrTemp2, CurrTemp3, CurrTemp4]
        def CurrTempListAlt = ["0", CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3, CurrTemp4]
        def CurrTemp = 0
        def ThermStateList = ["0", "$ThermState1", "$ThermState2", "$ThermState3", "$ThermState4"]
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

        def loopValue = 0


        while(loopValue < atomicState.ThermsInvolved &&  atomicState.SensorHandlerIsRunning == false){

            loopValue++
                atomicState.loopValue == loopValue
            log.info "loop($loopValue)"

            def AltSensor = AltSensorBoolList[loopValue] 

            if(AltSensor){
                CurrTemp = CurrTempList_Alt[loopValue] as int
                    log.debug "CurrTempListAlt[${loopValue}] selected as CurrTemp"
            }
            else {
                CurrTemp = CurrTempList[loopValue] as int
                    log.debug "CurrTempList[${loopValue}] selected as CurrTemp"
            }
            //CurrTemp = Double.parseDouble(CurrTemp).toInteger() 


            log.debug "CurrTemp = $CurrTemp"

            ThermState = ThermStateList[loopValue]
            ThermSet = Therm[loopValue]
            atomicState.EventAtTempLoop = ThermSet // used for false override prevention

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
            CSPSet = CSPSet.toInteger()
            atomicState.CSPSet = CSPSet
            atomicState.HSPSet = HSPSet


            def AppMgtList = ["0", atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
            def AppMgt = AppMgtList[loopValue]
            log.debug "AppMgt = $AppMgt"


            // log.debug "ShouldCool = $ShouldCool (Current Temperature Inside = $CurrTemp)"
            // log.debug "ShouldHeat = $ShouldHeat"

            if(ExceptionSW){
                if(ThermSet == "$Thermostat_1"){
                    if(SwitchesOn)
                    HSPSet = HSPSet + AddDegrees
                    CSPSet = CSPSet + SubDegrees

                    log.debug "$ThermSet SetPoints ExceptionSW active"                
                }
            }
            if(AppMgt){
                log.info "NO OVERRIDE for $ThermSet"

                log.debug "EVALUATING"     
                atomicState.withinApp = true

                //modify with presence/motion in the room

                def InMotionModes = CurrMode in MotionModes  
                def AccountForMotion = MotionSensor != null 

                if(AccountForMotion && InMotionModes ){

                    if(Inactive){

                        // log.info "$ThermSet default Cool: $CSPSet and default heat: $HSPSet "
                        CSPSet = CSPSet + AddDegreesMotion 
                        HSPSet = HSPSet - SubDegreesMotion
                        log.trace "$ThermSet CSP set to $CSPSet and HSP set to $HSPSet due to the absence of motion"

                    }
                    else {

                        log.debug "There's motion in ${ThermSet}'s room (main loop)"
                    }
                }
                log.trace """
InMotionModes?($InMotionModes)
AccountForMotion?($AccountForMotion)

Motion at $MotionSensor inactive for the past $minutesMotion minutes?($Inactive)"""


                //modify with outside's variation using linear equation
                def defaultCSPSet = CSPSet // recording this default value so if linear equation brings setpoint too low, it'll be recovered
                def defaultHSPSet = HSPSet // same but with heat


                // linear function for Cooling
                def xa = 75				//outside temp a
                def ya = CSPSet 	// desired cooling temp a 
                def xb = 90 //outside temp b
                def yb = CSPSet + 6 // desired cooling temp b  

                // take humidity into account
                // if outside humidity is higher than .... 
                if(TooHumid){
                    xa = 75				//outside temp a LESS VARIATION WHEN HUMID
                    ya = CSPSet	   // desired cooling temp a 
                    xb = 90 //outside temp b
                    yb = CSPSet + 2 // desired cooling temp b  LESS VARIATION WHEN HUMID
                }

                def coef = (yb-ya)/(xb-xa)

                def b = ya - coef * xa // solution to ya = coef*xa + b // CSPSet = coef*outsideTemp + b

                //CSPSet - (coef * outsideTemp) 
                log.info "b is: $b ---------------------------------------"
                CSPSet = coef*outsideTemp + b
                CSPSet = CSPSet.toInteger()

                log.info "coef is: $coef ||| ${ThermSet}'s CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"

                // no lower than 70         
                if(CSPSet <= 70){
                    CSPSet = defaultCSPSet
                    log.info "But because CSPSet is $CSPSet and thus lower than 70, default settings are restored" 
                }



                // for false overrides prevention
                // and for back to normal action
                // if user sets unit back to its currently calculated 
                // value, then the app will end the override

                if(loopValue == 1){
                    atomicState.newValueT1CSP = CSPSet
                    atomicState.newValueT1HSP = HSPSet

                }    
                if(loopValue == 2){
                    atomicState.newValueT2CSP = CSPSet
                    atomicState.newValueT2HSP = HSPSet  

                }
                if(loopValue == 3){
                    atomicState.newValueT3CSP = CSPSet
                    atomicState.newValueT3HSP = HSPSet   

                }

                // evaluate comfortability and needs
                def ShouldCool = outsideTemp >= OutsideTempHighThres && CurrTemp >= CSPSet && outsideTemp >= CSPSet
                if(!doorsOk && outsideTemp < CSPSet){
                    // whatever happens, if windows are open and it's cool enough outside, no AC cooling
                    ShouldCool = false
                }
                atomicState.TooHot = false
                def TooHot = CurrTemp > CSPSet && outsideTemp < CurrTemp && outsideTemp >= OutsideTempLowThres

                if(TooHot){
                    ShouldCool = true     
                    atomicState.TooHot = true
                    log.debug "TOO HOT"
                } 
                else {
                    log.debug "NOT TOO HOT TooHot?($TooHot)"
                }



                def ShouldHeat = (outsideTemp <= OutsideTempLowThres || CurrTemp <= HSPSet) && !ShouldCool
                if(ShouldCool && ShouldHeat) {
                    ShouldCool = false
                }
                if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet && outsideTemp >= CSPSet) {
                    ShouldCool = true
                }                   

                log.debug "loading updated $CurrMode Cooling and Heating Set Points for $ThermSet"     
                atomicState.withinApp = true

                // Now, before finally setting temperatures, we need to call Therm list as a list of objects, not string, 
                // so we can pull current setpoint and compare to avoid redundencies
                // this MUST be done in order to avoid any overflow of z-wave commands in this while loop

                def ThermObjectList = [null, Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
                def ThermSetObject = ThermObjectList[loopValue]

                def CurrentCoolingSetPoint = ThermSetObject.currentValue("coolingSetpoint") as Integer
                def CurrentHeatingSetPoint = ThermSetObject.currentValue("heatingSetpoint") as Integer

                log.trace """ShouldCool = $ShouldCool, 
ShouldHeat = $ShouldHeat
Current setpoint for $ThermSetObject is $CurrentCoolingSetPoint, 
Current Heating setpoint is $CurrentHeatingSetPoint,
Final CSPSet is $CSPSet
"""

                // finally set devices' temperatures..
                atomicState.withinApp = true
                if(CurrentCoolingSetPoint != CSPSet){
                    ThermSetObject.setCoolingSetpoint(CSPSet)
                }
                else 
                {
                    log.debug "Cooling SetPoint already set to $CSPSet"
                }
                if(CurrentHeatingSetPoint != HSPSet){
                    ThermSetObject.setHeatingSetpoint(HSPSet)
                }
                else 
                { 
                    log.debug "Heating SetPoint already set to $HSPSet"
                }
                def ThisIsExceptionTherm =  false
                if(ContactException){
                    ThisIsExceptionTherm = ThermSetObject == Thermostat_1
                    log.debug """
ThisIsExceptionTherm is: $ThisIsExceptionTherm (${ThermSet} == ${Thermostat_1})
ContactExceptionIsClosed = $ContactExceptionIsClosed"""
                }
                else {
                    ThisIsExceptionTherm =  false

                    log.debug "No exception contact selected by user, values set to false by default"
                }
                if(doorsOk || (ContactExceptionIsClosed && ThisIsExceptionTherm)){
                    if(!ShouldCool && !ShouldHeat && ThermState != "off" ){
                        AppMgtTrue() // override test value
                        log.debug "$ThermSet TURNED OFF"  
                        LatestThermostatMode = "off"
                        ThermSetObject.setThermostatMode("off") 
                    } 
                    else if(ThermState != "cool" && ShouldCool){
                        AppMgtTrue() 
                        log.debug "$ThermSet set to cool"
                        LatestThermostatMode = "cool"
                        ThermSetObject.setThermostatMode("cool")                 
                    }
                    else if(ThermState != "heat" && ShouldHeat){
                        AppMgtTrue()
                        log.debug "$ThermSet set to Heat"
                        LatestThermostatMode = "heat"
                        ThermSetObject.setThermostatMode("heat")                       
                    } 
                    // for all intend and purposes that are not accounted for by this app... 

                    if(Actuators && doorsOk && ThermState != "cool" && TooHot){
                        //state.messageTooHotWindows = 0 // 
                        LatestThermostatMode = "cool"
                        ThermSetObject.setCoolingSetpoint(defaultCSPSet) // this will trigger an override mode
                        ThermSetObject.setThermostatMode("cool") 

                        def message = "It is getting hot inside and windows are closed, forcing AC"
                        if(state.messageTooHotWindows == 0){
                            send(message)
                            state.messageTooHotWindows = 1
                        }
                        log.info message
                    }
                }              

            }
            else {
                log.debug "${ThermSet} in OVERRIDE MODE, doing nothing but recording should be values"   

                // recording currently set values for when users want to end an override
                // by setting a unit back to its current default's values 
                // first, it happens that no command was ever sent since last updated()
                // or due to contacts sensors or due to previous overrides
                // so let's reuse the same conditions, 
                // but without the consecutive actions/commands
                // nor the state comparisons which are meant only to avoid redundent z-wave commands
                // so we can record just the ShouldBe values
                // which are used by ThermostatSwitchHandler and setpointHandler for override assertion

                if(!ShouldCool && !ShouldHeat){      
                    LatestThermostatMode = "off"             
                } 
                else if(ShouldCool){                    
                    LatestThermostatMode = "cool"                            
                }
                else if(ShouldHeat){
                    LatestThermostatMode = "heat"
                }

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
                }

                log.trace """
atomicState.LatestThermostatMode_T1 = $atomicState.LatestThermostatMode_T1
atomicState.LatestThermostatMode_T2 = $atomicState.LatestThermostatMode_T2
atomicState.LatestThermostatMode_T3 = $atomicState.LatestThermostatMode_T3
"""
            }
             
            log.trace " END OF WHILE $loopValue" 
        }   
        // true end of while loop
       
        if(atomicState.SensorHandlerIsRunning != false){
            log.debug "Waiting for motion handler to be done"
        }
        // wait for other loops to finish before reseting this variable otherwhise there's a systemic risk of fals positive override
        runIn(5, withinAppFALSE)
    }
    else { 
        log.debug "not evaluating because some windows are open" 
    }
}

// A.I. and micro location evt management
def motionSensorHandler(evt){
    atomicState.SensorHandlerIsRunning = true
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

    log.debug """
$evt.value motion @ $evt.device
deltaMinutes = $deltaMinutes
Found ${motionEvents1?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_1
Found ${motionEvents2?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_2
Found ${motionEvents3?.size() ?: 0} events in the last $timeInMin minutes at $MotionSensor_3

$MotionSensor_1 is INACTIVE = $atomicState.isInActive1
$MotionSensor_2 is INACTIVE = $atomicState.isInActive2
$MotionSensor_3 is INACTIVE = $atomicState.isInActive3
"""
    atomicState.SensorHandlerIsRunning = false
    /* 
if(evt.value == "active") {
Evaluate()
}
*/
    OkToOpen() // FOR TEST ONLY MUST BE DELETED AFTER
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


    log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        atomicState.exception = true
    } else {
        atomicState.exception = false
    }



    Evaluate()

}
def contactExceptionHandlerOpen(evt) {

    log.debug "$evt.device is now $evt.value (Contact Exception), Turning off all thermostats in $TimeBeforeClosing seconds"

    runIn(TimeBeforeClosing, TurnOffThermostats)   

}
def contactExceptionHandlerClosed(evt) {

    log.debug "$evt.device is now $evt.value (Contact Exception), Resuming Evaluation for $thermostats_1"

    Evaluate()
}

// Main events management
def temperatureHandler(evt) { 
    def doorsOk = allContactsTest()

    def currentTemp = XtraTempSensor.currentValue("temperature")
    log.info """
current temperature value for $evt.device is $evt.value
Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp
"""

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



        if(Actuators && !doorsOk){
            log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                log.debug "closing windows"
                Actuators?.off()
                ActuatorException?.off()
                // allow for user to reopen them if they want to. 
                state.windowswereopenandclosedalready = true // windows won't close again as long as temperature is still critical to allow for user's override 
                // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
            }
            else { 
                def message = "doors and windows already reopened by user so not running emergency closing. BEWARE! these windows will not close again"
                log.info message
                send(message)

            }
        } 
        atomicState.TheresBeenCriticalEvent = true
    } 
    else 
    { 
        log.debug "CriticalTemp OK"
        atomicState.CRITICAL = false
        state.windowswereopenandclosedalready = false
        if(atomicState.doorsAreOpen == true && atomicState.TheresBeenCriticalEvent == true){
            TurnOffThermostats
        }
        atomicState.TheresBeenCriticalEvent = false
    } 

    Evaluate()
}
def contactHandlerClosed(evt) {



    state.messageTooHotWindows = 0 

    def ContactsClosed = windowsTest()

    log.debug "$evt.device is $evt.value" 

    atomicState.thisIsWindowMgt = true // prevent false ON/OFF override
    runIn(5, thisIsWindowMgtFALSE)


    log.info "List of devices' status is $CurrentContactsState"


    if(!ContactsClosed){
        log.debug "Not all contacts are closed, doing nothing"

    }
    else {      
        log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
        unschedule(TurnOffThermostats) // in case were closed within time frame

        //updated()
        Evaluate()
    }
} 
def contactHandlerOpen(evt) {

    log.debug "$evt.device is now $evt.value, Turning off all thermostats in $TimeBeforeClosing seconds"

    atomicState.thisIsWindowMgt = true // prevent false ON/OFF override
    def wait = TimeBeforeClosing + 10
    runIn(wait, thisIsWindowMgtFALSE)


    runIn(TimeBeforeClosing, TurnOffThermostats)   
    def message = ""
    if(atomicState.messageSent == 0 && !ContactsClosed && atomicState.ClosedByApp == true){ 

        message = "IMPORTANT MESSAGE $Actuators will not close again until you close them yourself!"
        log.info message
        send(message)
        state.messageSent = state.messageSent + 1
    }
    else if(!ContactsClosed && atomicState.OpenByApp == false){
        log.debug "WINDOWS MANUALLY OPENED"
    }


}
def ChangedModeHandler(evt) {

    log.debug "mode changed to ${evt.value}"

    updated()

}

//override management
def setpointHandler(evt){
    log.debug """
The source of this event is: ${evt.source}
log.debug "New $evt.name for $evt.device : $evt.value
log.debug "evt.displayName is $evt.displayName
"""


    if(atomicState.withinApp == false){
        def CurrMode = location.currentMode

        def HomeMode = null
        def ThisIsManual = false 
        def reference = null
        def termRef = null
        def AltSENSOR = false    

        log.info "Home modes are : $Home, Night modes are : $Night, Away mode is : $Away, CustomMode1 are : $CustomMode1, CustomMode2 are : $CustomMode2"

        log.debug "CurrMode is $CurrMode mode"
        // declare an integer value for the thermostat which has had its values modified
        def MapModesThermostats = ["${Thermostat_1}": "1" , "${Thermostat_2}": 2, "${Thermostat_3}": "3", 
                                   "${Thermostat_4}": "4"]
        def KeyValueForThisTherm = MapModesThermostats.find { it.key == "$evt.device"}
        log.info "device is ------------------- $KeyValueForThisTherm.value"
        def ThermNumber = KeyValueForThisTherm.value
        ThermNumber = KeyValueForThisTherm.value.toInteger()

        log.info "ThermNumber is ------------------- $ThermNumber"

        //array heat
        def HSPH = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPH4"]
        def HSPN = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPN4"]
        def HSPA = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
        def HSPCust1 = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPCust1_T4"]
        def HSPCust2 = ["0","$atomicState.newValueT1HSP", "$atomicState.newValueT2HSP", "$atomicState.newValueT3HSP", "$HSPCust2_T4"]

        /* backup copy
def HSPH = ["0","$HSPH1", "$HSPH2", "$HSPH3", "$HSPH4"]
def HSPN = ["0","$HSPN1", "$HSPN2", "$HSPN3", "$HSPN4"]
def HSPA_ = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
def HSPCust1 = ["0","$HSPCust1_T1", "$HSPCust1_T2", "$HSPCust1_T3", "$HSPCust1_T4"]
def HSPCust2 = ["0","$HSPCust2_T1", "$HSPCust2_T2", "$HSPCust2_T3", "$HSPCust2_T4"]
*/

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

        /* backup copy
def CSPH = ["0","$CSPH1", "$CSPH2", "$CSPH3", "$CSPH4"]
def CSPN = ["0","$CSPN1", "$CSPN2", "$CSPN3", "$CSPN4"]
def CSPA_ = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
def CSPCust1 = ["0","$CSPCust1_T1", "$CSPCust1_T2", "$CSPCust1_T3", "$CSPCust1_T4"]
def CSPCust2 = ["0","$CSPCust2_T1", "$CSPCust2_T2", "$CSPCust2_T3", "$CSPCust2_T4"]
*/

        // now transpose corresponding values among tables
        // for heat values
        def ListHSPs = ["0", HSPH, HSPN, HSPA, HSPCust1, HSPCust2]
        def HSPModecheck = ListHSPs[ModeIndexValue]
        log.debug "HSPModecheck = $HSPModecheck"
        def RefHeat = HSPModecheck[ThermNumber]
        //do the same with cooling values
        def ListCSPs = ["0", CSPH, CSPN, CSPA, CSPCust1, CSPCust2]
        log.debug """
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
            log.debug """RefHeat is $RefHeat and it is now converted to a reference for comparison"""

        }
        else  if(evt.name == "coolingSetpoint"){ 
            reference = RefCool
            reference = Math.round(Double.parseDouble(RefCool))
            reference = reference.toInteger()
            log.debug "RefCool is $RefCool and it is now converted to a reference for comparison"
        }


        def ThisIsModeChange = atomicState.locationModeChange
        def ExceptionState = atomicState.exception
        def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState


        def Value = evt.value
        //def Value = Math.round(Double.parseDouble(evt.value))
        Value = Value.toInteger()
        log.debug "Evt value to Integer is : $Value and it is to be compared to reference: $reference"


        log.debug """
RefHeat for $evt.device is: $RefHeat 
RefCool for $evt.device is: $RefCool 
reference for $evt.device is: $reference
SetPoint Change was Manual? ($atomicState.ThisIsManual) if false then should have $reference = $Value 
(unless location mode just changed or Exception Switch is on or ThisIsMotion or ThisIsLinearEq)
"""

        def doorsOk = allContactsTest()

        if(Value == reference || ThisIsModeChange || thisIsExceptionTemp || (!doorsOk && !thisIsExceptionTemp))
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
                atomicState.T1_AppMgt = false
                atomicState.T1_AppMgtSP = false
                log.info "atomicState.T1_AppMgt set to $atomicState.T1_AppMgt"
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
                atomicState.T2_AppMgt = false
                atomicState.T2_AppMgtSP = false

                log.info "atomicState.T2_AppMgt set to $atomicState.T2_AppMgt"
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
                atomicState.T3_AppMgt = false
                atomicState.T3_AppMgtSP = false
                log.info "atomicState.T3_AppMgt set to $atomicState.T3_AppMgt"
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
                atomicState.T4_AppMgt = false
                atomicState.T4_AppMgtSP = false
                log.info "atomicState.T4_AppMgt set to $atomicState.T4_AppMgt"
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
        log.debug "Not evaluating SETPOINT OVERRIDE because the command came from within the app itself"
    }
}
def ThermostatSwitchHandler(evt){

    log.debug "$evt.device set to $evt.value (ThermostatSwitchHandler)"



    log.debug "CHECKING COMMAND ORIGIN for $evt.device "
    if(atomicState.withinApp == false){
        if(atomicState.CRITICAL == false){
            def CurrMode = location.currentMode
            def LocatioModeChange = atomicState.locationModeChange
            log.debug "Location Mode Changed?($LocatioModeChange)"


            log.trace "Latest Thermostat ModeS : $atomicState.LatestThermostatMode_T1 | $atomicState.LatestThermostatMode_T2 | $atomicState.LatestThermostatMode_T3 | $atomicState.LatestThermostatMode_T4"


            def ltm1 = atomicState.LatestThermostatMode_T1.toString()
            def ltm2 = atomicState.LatestThermostatMode_T2.toString()
            def ltm3 = atomicState.LatestThermostatMode_T3.toString()
            def ltm4 = atomicState.LatestThermostatMode_T4.toString()

            def MapofShouldBe = ["$Thermostat_1": ltm1, "$Thermostat_2": ltm2, "$Thermostat_3": ltm3, "$Thermostat_4": ltm4]
            def WhichThermInvolved = MapofShouldBe.find{ it.key == "$evt.device"} 

            log.debug "Map of ShouldBe : ${MapofShouldBe}"
            log.debug "ShouldBe Value : ${WhichThermInvolved.value}"

            WhichThermInvolved = WhichThermInvolved.value
            def ShouldBe = WhichThermInvolved
            //ShouldBe = WhichThermInvolved.toString()

            log.debug "evt.device ShouldBe $ShouldBe and it was just set to $evt.value"

            // is current state app managed or manually set?
            log.debug " for $evt.device shoudlbe: evt.value($evt.value) =? ShouldBe($ShouldBe)"
            def IdenticalShouldbe = evt.value == ShouldBe 
            log.debug "IDENTICAL?($IdenticalShouldbe)"


            def ThereWasChange = !IdenticalShouldbe && !LocatioModeChange  && !thisIsWindowMgt 

            def thisIsWindowMgt = atomicState.thisIsWindowMgt

            log.debug """
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
                    // command did not come from app so manual or set point is manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_1"
                    atomicState.T1_AppMgt = false

                }       
            }
            else if(evt.displayName == "${Thermostat_2}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                    atomicState.T2_AppMgt = true


                }
                else {
                    // command did not come from app so manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                    atomicState.T2_AppMgt = false

                }     
            } 
            else if(evt.displayName == "${Thermostat_3}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_3"
                    atomicState.T3_AppMgt = true


                }
                else {
                    // command did not come from app so manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_3"
                    atomicState.T3_AppMgt = false

                }     
            } 
            else if(evt.displayName == "${Thermostat_4}"){
                if(!ThereWasChange){
                    // manual override deactivated
                    log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_4"
                    atomicState.T4_AppMgt = true


                }
                else {
                    // command did not come from app so manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_4"
                    atomicState.T4_AppMgt = false

                }     
            }

        }
        else { 
            log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" 
        }

    }
    else {
        log.debug "ON/OFF COMMAND FOR $evt.device came from app, not evaluating override"
    }
}
def thisIsWindowMgtFALSE(){
    log.debug "Reset atomicState.thisIsWindowMgt to FALSE"
    atomicState.thisIsWindowMgt = false
}
def withinAppFALSE(){
    log.debug "Reset atomicState.withinApp to FALSE"
    atomicState.withinApp = false // this value is reset to false so if there's a manual setpoint override it'll be detected as such
}
def AppMgtTrue(){

    def loopValue = atomicState.loopValue

    if(loopValue == 1){
        atomicState.T1_AppMgt = true
    }
    else if(loopValue == 2){
        atomicState.T2_AppMgt = true
    }
    else if(loopValue == 3){
        atomicState.T3_AppMgt = true
    }
    else if(loopValue == 4){
        atomicState.T4_AppMgt = true
    }
}

def resetLocationChangeVariable(){
    atomicState.locationModeChange = false
    log.debug "atomicState.locationModeChange reset to FALSE"
}

// contacts and windows management
def windowsTest(){

    def MainContactsAreClosed = true // has to be true by default in case no contacts selected

    // log.debug "Maincontacts are $Maincontacts"

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def ClosedContacts = CurrentContacts.findAll { AllcontactsAreClosed ->
        AllcontactsAreClosed == "closed" ? true : false}

    MainContactsAreClosed = ClosedContacts.size() == Maincontacts.size() 

    // log.debug "${ClosedContacts.size()} windows/doors out of ${Maincontacts.size()} are closed SO MainContactsAreClosed = $MainContactsAreClosed"

    return MainContactsAreClosed
}
def windowsExepTest(){
    def ContactsExepClosed = false

    // log.debug "Maincontacts are $Maincontacts"

    // log.debug "ContactException are $ContactException"

    def CurrentContactsExept = ContactException.currentValue("contact")    
    def ClosedContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreClosed ->
        AllcontactsExeptAreClosed == "closed" ? true : false
    }
    ContactsExepClosed = ClosedContactsExpt.size() == ContactException.size() 
    // log.debug "${ClosedContactsExpt.size()} windows/doors out of ${ContactException.size()} are closed SO ContactsExepClosed = $ContactsExepClosed"

    def CurrTherMode = Thermostat_1.currentValue("thermostatMode") as String
    // log.debug "Current Mode for $Thermostat_1 is $CurrTherMode"
    if(CurrTherMode != "off" && !ContactsExepClosed){
        // log.debug "$Thermostat_1 is on, should be off. Turning it off" 
        Thermostat_1.setThermostatMode("off") 
        atomicState.LatestThermostatMode_T1 = "off"
    }

    return ContactsExepClosed

}
def allContactsTest() {

    def AllContactsClosed = windowsTest() && windowsExepTest()
    // log.debug "allContactsTest() returns AllContactsClosed = $AllContactsClosed"

    return AllContactsClosed
}
def allContactsTestOpen() {

    def MainContactsAreAllOpen = false // has to be true by default in case no contacts selected

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def OpenContacts = CurrentContacts.findAll { AllcontactsAreOpen ->
        AllcontactsAreOpen == "open" ? true : false}
    //  log.debug "${OpenContacts.size()} windows/doors out of ${Maincontacts.size()} are open SO ContactsExepOpen = $ContactsExepOpen"
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
        //  log.debug "${OpenContactsExpt.size()} windows/doors out of ${ContactException.size()} are open SO ContactsExepOpen = $ContactsExepOpen"

        def OpenExptSize = OpenContactsExpt.size()
        def ExceptionSize = ContactException.size() 
        ContactsExepOpen = OpenExptSize == ExceptionSize

        AllOpen = ContactsExepOpen && MainContactsAreAllOpen

    }
    log.debug "AllOpen?($AllOpen)"

    return AllOpen  
}
def TurnOffThermostats(){

    def doorsOk = allContactsTest()

    def ContactExceptionIsClosed = windowsExepTest()

    if(atomicState.CRITICAL == false){
        log.debug "Turning off thermostats" 
        def InExceptionContactMode = location.currentMode in DoNotTurnOffModes

        /* if(ContactExceptionIsClosed && InExceptionContactMode){
Thermostat_1.setThermostatMode("auto") 
atomicState.LatestThermostatMode = "auto"
log.debug "$Thermostat_1  turned on (set to auto)"
atomicState.T1_AppMgt = true
}
*/// temporarily disabled


        // log.debug "ContactExceptionIsClosed: $ContactExceptionIsClosed, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"
        if(!NoTurnOffOnContact || !InExceptionContactMode || !ContactExceptionIsClosed){
            Thermostat_1.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_1  turned off"
            atomicState.T1_AppMgt = true
        }
        else {

            // log.debug "Not turning off $Thermostat_1 because current mode is within exception modes selected by the user"
            def ThermState1 = Thermostat_1?.currentValue("thermostatMode") as String
            atomicState.LatestThermostatMode = ThermState1
        }


        if(Thermostat_2){      
            Thermostat_2.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_2 turned off"
            atomicState.T2_AppMgt = true
        }
        if(Thermostat_3){
            Thermostat_3.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_3 turned off"
            atomicState.T3_AppMgt = true
        }
        if(Thermostat_4){

            Thermostat_4.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_4 turned off"
            atomicState.T4_AppMgt = true

        }
    }
    else { 
        // log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 
        //}
        //atomicState.ThermOff = true
    }
    log.debug "Thermostats already turned off, doing nothing"
}
def CheckWindows(){

    log.debug "Checking windows"
    def OkToOpen = OkToOpen() // outside and inside temperatures criteria and more... 

    def message = ""
    def allContactsAreOpen = allContactsTestOpen()
    def ContactsClosed = allContactsTest()


    log.debug "Contacts closed?($ContactsClosed)"


    def Inside = XtraTempSensor.currentValue("temperature")
    log.debug "Inside = $Inside"
    def Outside = OutsideSensor.currentValue("temperature")
    log.debug "Outside = $Outside"

    log.trace """
OkToOpen?($OkToOpen); 
OffSet?($OffSet) 
atomicState.ClosedByApp($atomicState.ClosedByApp) 
atomicState.OpenByApp($atomicState.OpenByApp) 
atomicState.messageSent($atomicState.messageSent) 

"""

    /* OkToOpen = true ///  TEST ONLY MUST BE DELETED AFTERWARD
atomicState.TooHot = true // TEST ONLY MUST BE DELETED AFTERWARD
atomicState.ClosedByApp = false///  TEST ONLY MUST BE DELETED AFTERWARD
ContactsClosed = false ///  TEST ONLY MUST BE DELETED AFTERWARD
*/

    if(OkToOpen){

        if(ContactsClosed){

            if(atomicState.ClosedByApp == true) {

                if(atomicState.TooHot != true){
                    Actuators?.on()
                    ActuatorException?.on()
                }
                atomicState.OpenByApp = true
                atomicState.ClosedByApp = false // so it doesn't open again

                log.debug "opening windows"

                if(OperatingTime){
                    message = "Conditions permitting, I'm opening $Actuators. Operation time is $OperatingTime seconds"
                }
                else {
                    message = "Conditions permitting, I'm opening $Actuators"

                }
                if(OperatingTime){
                    runIn(OperatingTime, StopActuators)   
                }
                log.info message 
                send(message)

            }
            else { 
                log.debug "Windows have already been opened, doing nothing" 
            }
        }

        else if(atomicState.TooHot == true && !allContactsAreOpen)  {


            // before anything make sure there's no thermostat already cooling
            def AllThermostats = null
            def ThermostatsAreCooling = null

            if(!NoTurnOffOnContact){
                AllThermostats = [Thermostat_1, Thermostat_2, Thermostat_3]
            }
            else {
                AllThermostats = [Thermostat_2, Thermostat_3]
            }

        }
        ThermostatsAreCooling = AllThermostats.findAll{ it.currentValue("thermostatMode") == "cool" }


        def ClosedWindows = Actuators.findAll{ it.currentValue("switch") == "off" }
        def ClosedWindowsExep = ActuatorException.findAll{ it.currentValue("switch") == "off" }
        message = """ ClosedWindows are $ClosedWindows, ClosedWindowsExep are $ClosedWindowsExep | 
It's too hot inside but outside is cooler, so I'm slightly opening the windows which are not already open"""
        log.info message
        send(message)


        if(ClosedWindows.size() > 0 && !ThermostatsAreCooling){
            log.info "ClosedWindows.size() = ${ClosedWindows.size()}"
            def ArraySize = ClosedWindows.size()
            def countArray = 0
            while(countArray < ArraySize){            // strictly inferior due to 0 start value of any array    
                ClosedWindows[countArray].on()
                countArray++
                    }           
        }

        if(ClosedWindowsExep.size() > 0){
            def ExceptionTherIsCooling = Thermostat_1.currentValue("thermostatMode") == "cool" 
            def ArraySizeExep = ClosedWindowsExep.size()
            def countArrayExep = 0
            if(!ExceptionTherIsCooling){
                while(countArrayExep < ArraySizeExep){        
                    // strictly inferior due to 0 start value of any array   
                    if(!ExceptionTherIsCooling){    
                        ClosedWindowsExep[countArrayExep].on()
                        countArrayExep++                       
                            } 
                }
            }
        }

        ActuatorsDelay()
        atomicState.TooHot = false
    }
    else if (atomicState.OpenByApp == true) {

        Actuators?.off()
        ActuatorException?.off()

        atomicState.ClosedByApp = true
        atomicState.OpenByApp = false // so it doesn't close again if user opens it manually

        if(atomicState.OpenByApp == true){
            def cause1 = "atomicState.OpenByApp == true"
        }
        //if(!OkToOpen)

        log.debug "closing windows"
        message = "I'm closing $Actuators and $ActuatorException"
        log.info message 
        send(message)
        atomicState.closed = true
        atomicState.hasRun = 0 // this is an integer beware of not replacing with bool


        atomicState.messageSent = 0
    }
}

def OkToOpen(){
    def message = ""
    log.debug "Checking if it's O.K. to Open windows"
    def ContactsClosed = allContactsTest()

    def CSPSet = atomicState.CSPSet
    def HSPSet = atomicState.HSPSet 

    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature") 
    def CurrTemp = Inside
    def Outside = OutsideSensor.currentValue("temperature") 
    def outsideTemp = Outside
    def WithinCriticalOffSet = Inside >= CriticalTemp + OffSet

    def OutsideTempHighThres = OutsideTempHighThres
    def ExceptHighThreshold1 = ExceptHighThreshold1


    def humidity = atomicState.humidity
    humidity = humidity.toInteger()
    def TooHumid = humidity > HumidityTolerance

    def WindValue = atomicState.wind
    WindValue = WindValue.toInteger()
    TooHumid = humidity > HumidityTolerance - 10 && WindValue <= 1

    def ItfeelsLike = atomicState.FeelsLike
    ItfeelsLike = ItfeelsLike.toInteger()
    def OutsideFeelsHotter = ItfeelsLike > Outside

    log.debug "Does it feel like hotter outside?($OutsideFeelsHotter)"



    def ShouldCool = outsideTemp >= OutsideTempHighThres /*&& CurrTemp >= CSPSet wrong because will never open when cooling with AC*/ && outsideTemp >= CSPSet
    def ShouldHeat = (outsideTemp <= OutsideTempLowThres || CurrTemp <= HSPSet) && !ShouldCool
    if(ShouldCool && ShouldHeat) {
        ShouldCool = false
    }
    if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet && outsideTemp >= CSPSet) {
        ShouldCool = true
    }        

    log.debug "ShouldCool = $ShouldCool -|-"


    if(ExceptACMode1 && CurrMode in ExceptACMode1){
        def ToMinus = OutsideTempHighThres
        log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold1
        log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold1" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    else if(ExceptACMode2 && CurrMode in ExceptACMode2){
        def ToMinus = OutsideTempHighThres
        log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold2
        log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold2" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }



    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres && !OutsideFeelsHotter
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4 && !OutsideFeelsHotter
    }


    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid

    // preparing a dynamic message on the causes of why windows won't open (or fans won't turn on)
    def cause1 = !OutSideWithinMargin
    def cause2 = !WithinCriticalOffSet
    def cause3 = !ShouldCool
    def cause4 = TooHumid

    def causeNotList = [ cause1, cause2, cause3, cause4]
    log.debug "causeNotList = $causeNotList"
    def causeNotTest = causeNotList.findAll{ val ->
        val == true ? true : false
    }
    def and = ""

    if(causeNotTest.size() > 1){
        and = "and"
    }
    def causeNotMap = [ "Outside temperature is not within user's comfortable margin" : cause1,  
                       "$and it is not too hot inside ${XtraTempSensor}'s room" : cause2 , 
                       "$and cooling is not needed" : cause3 ,  
                       "$and it is Too Humid outisde" : cause4 ]

    // creates a new map with only the keys that have values = true
    def causeNotOkToOpen = causeNotMap.findAll{it.value == true}
    // now get only the keys from this map 
    causeNotOkToOpen = causeNotOkToOpen.collect{ it.key }
    //causeNotOkToOpen = causeNotOkToOpen.toString() // takes off the parenthesis and... nope, I don't know how to do this, probably a slash thing! 
    //causeNotOkToOpen = causeNotOkToOpen.key
    //collect{[(it.key): it.value]}


    message = "Windows won't open because $causeNotOkToOpen"

    log.info message 
    if(state.messageOkToOpenCausesSent == 0){
        send(message)
        state.messageOkToOpenCausesSent = state.messageOkToOpenCausesSent + 1 
    }

    // open all the way? 
    if((CurrMode in $Away && WithinCriticalOffSet ) || (OutSideWithinMargin && WithinCriticalOffSet && !ShouldCool && !TooHumid) || (OpenWhenEverPermitted && !ShouldCool && !TooHumid) ){
        result = true
        state.OpenInFull = true
    } 
    else { 
        state.OpenInFull = false
    }


    log.info """
Inside?($Inside), Outside?($Outside), 
Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) 
closed?($ContactsClosed)
OutSideWithinMargin?($OutSideWithinMargin)
Inside is WithinCriticalOffSet?($WithinCriticalOffSet) 
ShouldCool?($ShouldCool)
ShouldHeat?($ShouldHeat)
hasFeelsLike?($hasFeelsLike)
ItfeelsLike $feelsLike F
hasWind?($hasWind)
WindValue = $WindValue
hasHumidity?($hasHumidity)
TooHumid?($TooHumid)
Humidity is: $humidity
OkToOpen?($result)
"""

    return result
}


def StopActuators(){

    def SlightOpen = atomicState.SlightOpen

    def OpenInFull = state.OpenInFull
    log.debug "STOP"
    if (Actuators?.hasCommand("stop")){
        Actuators?.stop()
    }
    if (ActuatorException?.hasCommand("stop")){
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
log.debug "wait $secondsLog seconds"
}

StopActuators()
*/
    runIn(20, StopActuators)

}

//miscellaneous 
def schedules() { 

    def scheduledTimeA = 1
    def scheduledTimeB = 5

    //  schedule("0 0/$scheduledTimeA * * * ?", Evaluate)
    // log.debug "Evaluate scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        //// log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }


}
def polls(){

    def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
    def ModeIndexValue = MapofIndexValues.find{ it.key == "$location.currentMode"}
    ModeIndexValue = ModeIndexValue.value.toInteger()


    def CtrlSwtchPoll = CtrlSwt?.hasCommand("poll")
    def CtrlSwtchRefresh = CtrlSwt?.hasCommand("refresh")

    if(CtrlSwtchPoll){
        CtrlSwt?.poll()
        log.debug "polling $CtrlSwt"
    }
    else if(CtrlSwtchRefresh){
        CtrlSwt?.refresh()
        log.debug "refreshing $CtrlSwt"
    }
    else { 
        log.debug "$CtrlSwt neither supports poll() nor refresh() commands"
    }

    if(Thermostat_1){
        def poll = Thermostat_1.hasCommand("poll")
        def refresh = Thermostat_1.hasCommand("refresh")
        if(poll){
            Thermostat_1.poll()
            log.debug "polling $Thermostat_1"
        }
        else if(refresh){
            Thermostat_1.refresh()
            log.debug "refreshing $Thermostat_1"
        }
        else { 
            log.debug "$Thermostat_1 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_2){
        def poll = Thermostat_2.hasCommand("poll")
        def refresh = Thermostat_2.hasCommand("refresh")
        if(poll){
            Thermostat_2.poll()
            log.debug "polling $Thermostat_2"
        }
        else if(refresh){
            Thermostat_2.refresh()
            log.debug "refreshing $Thermostat_2"
        }
        else { 
            log.debug "$Thermostat_2 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_3){
        def poll = Thermostat_3.hasCommand("poll")
        def refresh = Thermostat_3.hasCommand("refresh")
        if(poll){
            Thermostat_3.poll()
            log.debug "polling $Thermostat_3"
        }
        else if(refresh){
            Thermostat_3.refresh()
            log.debug "refreshing $Thermostat_3"
        }
        else { 
            log.debug "$Thermostat_3 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_4){
        def poll = Thermostat_4.hasCommand("poll")
        def refresh = Thermostat_4.hasCommand("refresh")
        if(poll){
            Thermostat_4.poll()
            log.debug "polling $Thermostat_4"
        }
        else if(refresh){
            Thermostat_4.refresh()
            log.debug "refreshing $Thermostat_4"
        }
        else { 
            log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
        }
    }
    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            log.debug "polling $OutsideSensor"
        }
        else if(refresh){
            OutsideSensor.refresh()
            log.debug "refreshing $OutsideSensor"
        }
        else { 
            log.debug "$OutsideSensor does not support either poll() nor refresh() commands"
        }
    }
}
def send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage) {
            // log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            // log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    log.debug msg
}


