definition(
    name: " Multi Thermostat Manager 7.0",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description:  """Manage up to 4 thermostats, in parallel with these options: contact sensors, 
third party temperature sensor, modes, off override and temperature set point override""",
    category: "Green Living",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg"
)

preferences {

    page name: "pageSetup"
    page name: "settings"
    page name: "Modes"

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
        section("About this App:"){
            paragraph "Manage one or more thermostats under different modes" 
            //paragraph image: "http://elfege.com/penrose.jpg",
        }
        section("Setup Menu") {
            href "settings", title: "Thermostats and other devices", description: ""
            href "Modes", title: "Modes and temperatures", description: ""
        }
        section(){
            mode(title: "Set for specific mode(s)")
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


        section("Select an outside sensor"){
            input(name: "HowMany", type: "number", range: "1..4", title: "set a value between 1 and 4", description: null, submitOnChange: true)

            input(name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Pick a sensor for Outside's temperature", required: true, multiple: false, description: null, submitOnChange: true)
            paragraph """
This sensor is essential to the A.I. of this app. 
If you do not have an outside temp measurment device, you can 
allways create a SmartWeater virtual device. Visit Smartthings 
Forums for help or contact me directly at elfege at elfege . com"""

            input(name: "HumidityMeasurement", type: "capability.relativeHumidityMeasurement", title: "Pick an outside humidity sensor", required: true, multiple: false, description: null)
            input(name: "HumidityTolerance", type: "number", title: "set a humidity tolerance threshold", required: true, description: "set a humidity threshold")
            paragraph """
This level of humidity will determine how to modulate several values such as, for example, 
cooling set points. The more humid, the more the AC will be 'sensitive' 
and run, the less humid, the less it'll run (and if this option has 
been picked elsewhere, it'll prefer to open the windows or activate a fan)"""
        }
        section("how many thermostats do you want to control?") { 
            if(HowMany >= 1) {
                input(name: "Thermostat_1", type: "capability.thermostat", title: "Thermostat 1 is $Thermostat_1", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_1", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_1){
                    input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_1", required: true, multiple: false, description: null, uninstall: true)
                }
                input(name: "ExceptionSW", type : "bool", title: "Make exception for this device when a switch is on | setting to be completed in the next page", 
                      defaut: false, submitOnChange: true)

                input(name: "NoTurnOffOnContact", type: "bool", title: "Do not turn off this unit upon contacts events", default: false, submitOnChange: true, description: "this is to be applied with a specific mode in the next section")
                input(name: "MotionSensor_1", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_1 settings when inactive", description: "Select a motion sensor", required: false)
                if(MotionSensor_1){
                    input(name: "MotionModesT1", type: "mode", title: "But only if home is in these modes", multiple: true, description: "select a mode", required: true)
                    input(name: "AddDegreesMotion1", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting", required: true)
                    input(name: "SubDegreesMotion1", type: "decimal", title: "Substract this amount of degrees to $Thermostat_1 cooling setting", required: true)   

                    input "minutesMotion", "number", title: "For how long there must be no motion for those settings to apply? ", range: "2..999", description: "time in minutes"
                    paragraph "this timer will apply to all motion sensors you may add below"
                }
            }
            if(HowMany >= 2) {
                input(name: "Thermostat_2", type: "capability.thermostat", title: "Thermostat 2 is $Thermostat_2", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_2", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_2){
                    input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_2", required: true, multiple: false, description: null, uninstall: true)
                }

                input(name: "MotionSensor_2", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_2 settings when inactive", description: "Select a motion sensor", required: false)
                if(MotionSensor_2){
                    input(name: "MotionModesT2", type: "mode", title: "But only if home is in these modes", multiple: true, description: "select a mode", required: true)
                    input(name: "AddDegreesMotion2", type: "decimal", title: "Add this amount of degrees to $Thermostat_2 heat setting", required: true)
                    input(name: "SubDegreesMotion2", type: "decimal", title: "Substract this amount of degrees to $Thermostat_2 cooling setting", required: true)      
                }
            }
            if(HowMany >= 3) {
                input(name: "Thermostat_3", type: "capability.thermostat", title: "Thermostat 3 is $Thermostat_3", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_3", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_3){
                    input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control $Thermostat_3", required: true, multiple: false, description: null, uninstall: true)
                }

                input(name: "MotionSensor_3", type: "capability.motionSensor", title: "Use this motion sensor to lower $Thermostat_3 settings when inactive", description: "Select a motion sensor", required: false)
                if(MotionSensor_3){
                    input(name: "MotionModesT3", type: "mode", title: "But only if home is in these modes", multiple: true, description: "select a mode", required: true)
                    input(name: "AddDegreesMotion3", type: "decimal", title: "Add this amount of degrees to $Thermostat_3 heat setting", required: true)
                    input(name: "SubDegreesMotion3", type: "decimal", title: "Substract this amount of degrees to $Thermostat_3 cooling setting", required: true)      
                }
            }
            if(HowMany == 4) {
                input(name: "Thermostat_4", type: "capability.thermostat", title: "Thermostat 4 is $Thermostat_4", required: false, multiple: false, description: null, submitOnChange: true)
            }
        }
        section(""){
            if(AltSensor1 || AltSensor_2 || AltSensor_3){
                input(name: "AltThermOffSet", type: "number", title: "OffSet value for your alternative sensors?", default: 0, required: false, description: "leave blank if none")
            }

            input(name: "contact", type:"capability.contactSensor", title: "select windows / contacts", multiple: true, required: true)
            input(name: "TimeBeforeClosing", type: "number", title: "after this amount of time in seconds", required: false, description: "default is 60 seconds", default: 60, uninstall: true, install: true)
            input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "set a safety value, default is 65", defaut: 65)
            input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true)

        }

        section("Open some windows when it's nor too hot nor too cool outside (beta: will run only once to prevent annoyances)"){
            input(name: "Actuators", type: "capability.switch", required: false, multiple: true, title: "select the switches that you want to control", submitOnChange: true)
            if(Actuators){
                input(name: "OutsideTempLowThres", type: "number", title: "Outside temperature above which I open windows", required: true, description: "Outside Temp's Low Threshold")
                input(name: "OutsideTempHighThres", type: "number", title: "Outside temperature above which I keep windows closed", required: true, description: "Outside Temp's High Threshold")
                input(name: "ExceptACMode1", type: "mode", title: "if location is in this mode, lower Outside Temp's High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode1){
                    input(name: "ExceptHighThreshold1", type: "number", title: "pick an offset value for $ExceptACMode1 mode", required: true)
                }
                input(name: "ExceptACMode2", type: "mode", title: "if location is in this mode, lower Outside Temp's High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode2){
                    input(name: "ExceptHighThreshold2", type: "number", title: "pick an offset value for $ExceptACMode2 mode", required: true)
                }
                input(name: "OffSet", type: "decimal", title: "You set Critical Temp at: ${CriticalTemp}. Close windows when inside temp is inferior or equal to this value + OffSet ", required: true, description: "Set OffSet Value")
                paragraph """If within margin then open except if inside's temp is lower then heat setting minus offset. Reference is $XtraTempSensor
You may also chose to open windows at full lenght whenever outside's temperature allows it instead of only when cooling is required (see below)"""
                input(name: "OpenWhenEverPermitted", type: "bool", title: "Open in full whenever it is nice outside?", default: false, submitOnChange: true)
                def HasStop = Actuators?.hasCommand("stop") || Actuators?.hasCommand("Stop") 
                if(HasStop){
                    input(name: "OperatingTime", type: "number", title: "Should I stop opening operation after this amount of time?", required: false, description: "time in seconds")
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

        if(NoTurnOffOnContact){
            section("Exception Contacts: you selected $Thermostat_1 as one to not turn off when contacts are open. Now select under which modes you want this rule to apply "){
                input(name: "DoNotTurnOffModes", type : "enum", title: "Select which modes", options: ["$Home", "$Night", "$Away", "$CustomMode1", "$CustomMode2"], multiple: true, required: true)
                input(name: "ContactException", type : "capability.contactSensor", multiple: true, title: "unless these specific contacts sensors are open", description: "Select a contact that IS NOT among those already selected", required: false)
            }
        }


        section("Thermostats temperatures for $Home Mode"){
            if(HowMany >= 1) {
                input(name: "HSPH1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Home mode", required: true)
                input(name: "CSPH1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Home mode", required: true)

                if(ExceptionSW){              
                    input(name: "CtrlSwtH", type: "capability.switch", title: "Make an exception When this switch is on", required: false, submitOnChange: true)
                    if(CtrlSwtH){
                        input(name: "AddDegrees", type: "decimal", title: "Add this value to $Thermostat_1 heat setting in $Home mode", required: true)
                        input(name: "SubDegrees", type: "decimal", title: "Substract this value to $Thermostat_3 cooling setting in $Home mode", required: true)                     
                    }
                }
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

                if(ExceptionSW){              
                    input(name: "CtrlSwtN", type: "capability.switch", title: "When this switch is on", required: false, submitOnChange: true)
                    if(CtrlSwtN){
                        input(name: "AddDegrees", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting in $Night mode", required: true)
                        input(name: "SubDegrees", type: "decimal", title: "Substract this amount of degrees to $Thermostat_3 cooling setting in $Home mode", required: true)

                    }
                }
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

                    if(ExceptionSW){              
                        input(name: "CtrlSwtCust1", type: "capability.switch", title: "When this switch is on", required: false, submitOnChange: true)
                        if(CtrlSwtCust1){
                            input(name: "AddDegrees", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting in $CustomMode1 mode", required: true)
                            input(name: "SubDegrees", type: "decimal", title: "Substract this amount of degrees to $Thermostat_1 cooling setting in $CustomMode1 mode", required: true)

                        }
                    }
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

                        if(ExceptionSW){              
                            input(name: "CtrlSwtCust2", type: "capability.switch", title: "When this switch is on", required: false, submitOnChange: true)
                            if(CtrlSwtCust2){
                                input(name: "AddDegrees", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting in $CustomMode2 mode", required: true)
                                input(name: "SubDegrees", type: "decimal", title: "Substract this amount of degrees to $Thermostat_1 cooling setting in $CustomMode2 mode", required: true)
                                i
                            }
                        }
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
def installed() {	 
    // log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler
    init()
}
def updated() {
    atomicState.modeStartTime = now() 

    if(atomicState.doorsAreOpen){
        atomicState.WindowsAppOpened = true // if there are open contacts then by default set a new update as if they were turned on by the app
        // this will work even if they were manually turned on. No other way, so far. 
    }
    else {
        atomicState.WindowsAppOpened = false // needed at first to avoid false turning on when updated or mode changed
    }
    atomicState.ContactException = false
    //atomicState.ThermOff = false // allows units override in open windows modes
    log.info "updated with settings = $settings $Modes"
    unsubscribe()
    unschedule()

    init()
}
def init() {

    // log.debug "enter updated, state: $state"  

    subscribe(contact, "contact.open", contactHandlerOpen)
    subscribe(contact, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(contact, "ContactException.open", contactHandlerOpen)
        subscribe(contact, "ContactException.closed", contactHandlerClosed)
    }

    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    subscribe(Thermostat_1, "temperature", temperatureHandler)
    subscribe(Thermostat_1, "thermostatMode", ThermostatSwitchHandler)


    subscribe(OutsideSensor, "temperature", temperatureHandler)

    subscribe(HumidityMeasurement, "humidity", HumidityHandler)

    subscribe(Thermostat_1, "heatingSetpoint", setpointHandler)
    subscribe(Thermostat_1, "coolingSetpoint", setpointHandler)


    def swichtCapableCap = Thermostat_1.hasCapability("Switch")
    def swichtCapableLow = Thermostat_1.hasCapability("switch")
    if(swichtCapableLow){
        subscribe(Thermostat_1, "switch", ThermostatSwitchHandler)
        // log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
    } else if(swichtCapableCap){
        subscribe(Thermostat_1, "Switch", ThermostatSwitchHandler)
        // log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
    } else { 
        log.debug "no switch capability for $Thermostat_1" 
    }

    if(Thermostat_2){
        subscribe(Thermostat_2, "temperature", temperatureHandler)
        subscribe(Thermostat_2, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_2, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_2, "coolingSetpoint", setpointHandler)

        swichtCapableCap = Thermostat_2.hasCapability("Switch")
        swichtCapableLow = Thermostat_2.hasCapability("switch")

        if(swichtCapableLow){
            subscribe(Thermostat_2, "switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_2 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else if(swichtCapableCap){
            subscribe(Thermostat_2, "Switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_2 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else { log.trace "no switch capability for $Thermostat_2" }
    }
    if(Thermostat_3){
        subscribe(Thermostat_3, "temperature", temperatureHandler)
        subscribe(Thermostat_3, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_3, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_3, "coolingSetpoint", setpointHandler)

        swichtCapableCap = Thermostat_3.hasCapability("Switch")
        swichtCapableLow = Thermostat_3.hasCapability("switch")

        if(swichtCapableLow){
            subscribe(Thermostat_3, "switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_3 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else if(swichtCapableCap){
            subscribe(Thermostat_3, "Switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_3 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else { log.trace "no switch capability for $Thermostat_3" }
    }
    if(Thermostat_4){
        subscribe(Thermostat_4, "temperature", temperatureHandler)
        subscribe(Thermostat_4, "thermostatMode", ThermostatSwitchHandler)
        subscribe(Thermostat_4, "heatingSetpoint", setpointHandler)
        subscribe(Thermostat_4, "coolingSetpoint", setpointHandler)
        subscribe(Thermostat_4, "app", appHandler)
        swichtCapableCap = Thermostat_4.hasCapability("Switch")
        swichtCapableLow = Thermostat_4.hasCapability("switch")
        if(swichtCapableLow){
            subscribe(Thermostat_4, "switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else  if(swichtCapableCap){
            subscribe(Thermostat_4, "Switch", ThermostatSwitchHandler)
            //log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else { log.trace "no switch capability for $Thermostat_4" }
    }
    if(ExceptionSW){
        if(CtrlSwtH){
            subscribe(CtrlSwtH, "switch", switchHandler)
        }
        if(CtrlSwtN){
            subscribe(CtrlSwtN, "switch", switchHandler)
        }
        if(CtrlSwtCust1){
            subscribe(CtrlSwtCust1, "switch", switchHandler)
        }
        if(CtrlSwtCust2){
            subscribe(CtrlSwtCust2, "switch", switchHandler)
        }

    }
    if(AltSensor_1 || AltSensor_2 || AltSensor_3){

        if(AltSensor_1){
            subscribe(Sensor_1, "temperature", temperatureHandler)
            //// log.debug "Subscription for alternative Sensor for $Sensor_1"

        }
        if(AltSensor_2){
            subscribe(Sensor_2, "temperature", temperatureHandler)
            //// log.debug "Subscription for alternative Sensor for $Sensor_2"

        }
        if(AltSensor_3){
            subscribe(Sensor_3, "temperature", temperatureHandler)
            //// log.debug "Subscription for alternative Sensor for $Sensor_3"

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



    // first default values to be set to any suitable value so it doesn't crash with null value 
    // they will be updated within seconds with user's settings 
    atomicState.newValueT1CSP = 75
    atomicState.newValueT1HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75
    atomicState.newValueT2CSP = 75
    atomicState.newValueT2HSP = 75


    atomicState.AllowToRunMainLoop = true // for motion handler 
    state.ThermostatOverriden = "none"
    atomicState.ThisIsManual = false



    def ContactsOpen = contact.latestValue("contact").contains("open")
    if(!ContactsOpen) {
        // windows are closed 
        atomicState.ClosedByApp = true // app will open windows if needed 

    } 
    else {
        // windows are open so after mode change we want them to be able to close 
        atomicState.ClosedByApp = false //  so app won't open windows further
        atomicState.OpenByApp = true // always assume was opened by the app after mode change (or update) so it can close them if needed
    }

    atomicState.OpenByApp = false // so it doesn't close again if user opens it manually

    state.messageTooHotWindows = 0
    atomicState.messageSent = 0

    atomicState.locationModeChange = true 

    atomicState.SetpointDone = false

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
    TemperaturesModes()

}

def motionSensorHandler(evt){

    atomicState.SensorHandlerIsRunning = true // forces main loop to wait 

    log.debug "there is $evt.value motion @ $evt.device"


    def deltaMinutes = minutesMotion as Long
    log.debug "deltaMinutes = $deltaMinutes"

    def timeFrame = now() - (60000 * deltaMinutes)
    log.debug "timeFrame : $timeFrame"

    if(evt.device == MotionSensor_1){
        def motionEvents1 = MotionSensor_1.eventsSince(new Date(timeFrame))

        if(motionEvents1.find{it.value == "active" }){
            atomicState.isInActive1 = false
        }
        else {
            atomicState.isInActive1 = true
        }
    }
    else if(evt.device == MotionSensor_2){
        def motionEvents2 = MotionSensor_2.eventsSince(new Date(timeFrame))
        if(motionEvents2.find{it.value == "active" }){
            atomicState.isInActive2 = false
        }
        else {
            atomicState.isInActive2 = true
        }
    }
    else if(evt.device == MotionSensor_3){
        def motionEvents3 = MotionSensor_3.eventsSince(new Date(timeFrame))
        if(motionEvents3.find{it.value == "active" }){
            atomicState.isInActive3 = false
        }
        else {
            atomicState.isInActive3 = true
        }
    }


    log.debug """
Found ${motionEvents1?.size() ?: 0} events in the last $deltaMinutes minutes at $evt.device
Found ${motionEvents2?.size() ?: 0} events in the last $deltaMinutes minutes at $evt.device
Found ${motionEvents3?.size() ?: 0} events in the last $deltaMinutes minutes at $evt.device

$MotionSensor_1 is INACTIVE = $atomicState.isInActive1
$MotionSensor_2 is INACTIVE = $atomicState.isInActive2
$MotionSensor_3 is INACTIVE = $atomicState.isInActive3
"""

    atomicState.SensorHandlerIsRunning = false 

    TemperaturesModes()


}

def HumidityHandler(evt){

    atomicState.humidity = evt.value
    log.info "humidity value is ${evt.value}%"

}
def switchHandler(evt){


    // log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        atomicState.exception = true
    } else {
        atomicState.exception = false
    }

    def count = 0
    while(atomicState.loopValue < atomicState.ThermsInvolved && count < atomicState.WhileMax){
        count++
            // log.debug "wait($count)"

            }  

    TemperaturesModes()

}
def setpointHandler(evt){
    log.debug """
The source of this event is: ${evt.source}
log.debug "New $evt.name for $evt.device : $evt.value
log.debug "evt.displayName is $evt.displayName
state.ThermostatOverriden = evt.displayName"""


    if(atomicState.withinApp == false){

        atomicState.SetpointDone = false

        def CurrMode = location.currentMode

        def HomeMode = null
        def ThisIsManual = false 
        def reference = null
        def termRef = null
        //def EventDevice = null 
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

        def doorsOk = atomicState.doorsAreClosed

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


    def count = 0
    while(atomicState.loopValue < atomicState.ThermsInvolved && count < atomicState.WhileMax){
        count++
            //log.debug "wait($count)"

            }  

    atomicState.SetpointDone = true
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

            /*
            // is the current SetPoint app managed or manually set?
            def IsSpAppManagedMap = ["${Thermostat_1}": atomicState.T1_AppMgtSP, "${Thermostat_2}" : atomicState.T2_AppMgtSP, "${Thermostat_3}" : atomicState.T3_AppMgtSP, "${Thermostat_4}" : atomicState.T4_AppMgtSP]
            def IsSpAppManaged = IsSpAppManagedMap.find{it.key == "$evt.device"}
            IsSpAppManaged = IsSpAppManaged.value
            log.debug "IsSpAppManaged?($IsSpAppManaged)"
*/
            // is current state app managed or manually set?
            log.debug " for $evt.device shoudlbe: evt.value($evt.value) =? ShouldBe($ShouldBe)"
            def IdenticalShouldbe = evt.value == ShouldBe 
            log.debug "IDENTICAL?($IdenticalShouldbe)"

            def ExceptionState = atomicState.exception
            def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState
            log.debug "ExceptionSwitch?($thisIsExceptionTemp)"




            //make sure that  : 
            // 1) the thermostat DID effectively had its operating mode changed
            // 2) this change was not triggered by a home location change 
            // 3) or by the TemperaturesModes() loop
            // 4) no setpoint exception


            def ThereWasChange = !IdenticalShouldbe && !LocatioModeChange // && !IsSpAppManaged

            log.debug " Change($ThereWasChange)"

            if(evt.displayName == "${Thermostat_1}" ){
                if(IdenticalShouldbe || thisIsExceptionTemp || LocatioModeChange){
                    // manual override deactivated
                    log.debug "NO MANUAL ON/OFF OVERRIDE for $Thermostat_1"
                    atomicState.T1_AppMgt = true


                }
                else if(!IdenticalShouldbe && !thisIsExceptionTemp && ThereWasChange){
                    // command did not come from app so manual or set point is manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_1"
                    atomicState.T1_AppMgt = false

                }       
            }
            else if(evt.displayName == "${Thermostat_2}"){
               if(IdenticalShouldbe || thisIsExceptionTemp || LocatioModeChange){
                    // manual override deactivated
                    log.debug "NO MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                    atomicState.T2_AppMgt = true


                }
                else if(!IdenticalShouldbe && !thisIsExceptionTemp && ThereWasChange){
                    // command did not come from app so manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_2"
                    atomicState.T2_AppMgt = false

                }     
            } 
            else if(evt.displayName == "${Thermostat_3}"){
                if(IdenticalShouldbe || thisIsExceptionTemp || LocatioModeChange){
                    // manual override deactivated
                    log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_3"
                    atomicState.T3_AppMgt = true


                }
                else if(!IdenticalShouldbe && !thisIsExceptionTemp && ThereWasChange){
                    // command did not come from app so manual override is on
                    log.debug "MANUAL ON/OFF OVERRIDE for $Thermostat_3"
                    atomicState.T3_AppMgt = false

                }     
            } 
            else if(evt.displayName == "${Thermostat_4}"){
                if(IdenticalShouldbe || thisIsExceptionTemp || LocatioModeChange){
                    // manual override deactivated
                    log.debug "NO ON/OFF MANUAL OVERRIDE for $Thermostat_4"
                    atomicState.T4_AppMgt = true


                }
                 else if(!IdenticalShouldbe && !thisIsExceptionTemp && ThereWasChange){
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
        log.debug "ON/OFF COMMAND FOR $evt.device came from app, not evaluation override"
    }
}


def temperatureHandler(evt) { 

    //log.debug "The source of this event is: ${evt.source}"

    log.info "current temperature value for $evt.device is $evt.value" 
    def currentTemp = XtraTempSensor.currentValue("temperature")
    log.info "Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp"


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

        if(Actuators && atomicState.doorsAreOpen == true){
            // log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                log.debug "closing windows"
                Actuators?.off()
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
        // log.debug "CriticalTemp OK"
        atomicState.CRITICAL = false
        state.windowswereopenandclosedalready = false
        if(atomicState.doorsAreOpen == true && atomicState.TheresBeenCriticalEvent == true){
            TurnOffThermostats
        }
        atomicState.TheresBeenCriticalEvent = false
    } 



    def count = 0
    while(atomicState.loopValue < atomicState.ThermsInvolved && count < atomicState.WhileMax){
        count++
            // log.debug "wait($count)"

            }  

    TemperaturesModes()
}
def contactHandlerClosed(evt) {
    state.messageTooHotWindows = 0 
    // log.debug "$evt.device is $evt.value"

    if(contact.latestValue("contact").contains("open")){
        log.debug "Not all contacts are closed, doing nothing"
        atomicState.doorsAreClosed = false

    }
    else {             
        atomicState.doorsAreClosed = true


        // all contacts are closed, unscheduling previous TurnOffThermostats command

        unschedule(TurnOffThermostats) // in case were closed within time frame

        //updated()
        TemperaturesModes()
    }


} 
def contactHandlerOpen(evt) {

    atomicState.doorsAreClosed = false
    // log.debug "$evt.device is now $evt.value" 
    // log.debug "Turning off all thermostats in $TimeBeforeClosing seconds"

    if(ContactException){
        ContactException.latestValue("contact").contains("open")
        atomicState.ContactException = true
    }
    else {
        atomicState.ContactException = false
    }

    runIn(TimeBeforeClosing, TurnOffThermostats)   


    if(Actuators){

        CheckWindows()
    }
}
def ChangedModeHandler(evt) {

    log.debug "mode changed to ${evt.value}"

    updated()

}

// main loop
def TemperaturesModes(){

    //updated()



    log.trace "MAIN LOOP RUNNING"

    def doorsOk = atomicState.doorsAreClosed

    def CurrMode = location.currentMode
    def Outside = OutsideSensor.currentValue("temperature") 
    def outsideTemp = Outside
    def humidity = HumidityMeasurement.latestValue("humidity") 

    def TooHumid = humidity > HumidityTolerance


    // log.debug "atomicState.ThisIsManual value is $atomicState.ThisIsManual"
    def CurrTemp1 = Thermostat_1?.currentTemperature
    def ThermState1 = Thermostat_1?.currentValue("thermostatMode") as String
    def CurrTemp2 = Thermostat_2?.currentTemperature
    def ThermState2 = Thermostat_2?.currentValue("thermostatMode") as String   
    def CurrTemp3 = Thermostat_3?.currentTemperature
    def ThermState3 = Thermostat_3?.currentValue("thermostatMode") as String 
    def CurrTemp4 = Thermostat_4?.currentTemperature
    def ThermState4 = Thermostat_4?.currentValue("thermostatMode") as String

    //log.trace """T1 : $atomicState.T1_AppMgt, T2 : $atomicState.T2_AppMgt, T3 : $atomicState.T3_AppMgt, T4 : $atomicState.T4_AppMgt"""

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
    def CtrlSwtch = [null, CtrlSwtH, CtrlSwtN, null, CtrlSwtCust1, CtrlSwtCust2]
    //def CtrlSwtch = ["null", "$CtrlSwtH", "$CtrlSwtN", "null", "$CtrlSwtCust1", "$CtrlSwtCust2"]       
    CtrlSwtch = CtrlSwtch[ModeIndexValue]

    def inCtrlSwtchMode = CurrMode in ["$Home", "$Night", "$CustomMode1", "$CustomMode2"]

    if(inCtrlSwtchMode){
        def SwitchesOn = CtrlSwtch?.latestValue("switch").contains("on")
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
    def ThermSetDevice = 0
    def ThermDeviceList = ["null", Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
    def CurrTempList = ["0", "$CurrTemp1", "$CurrTemp2", "$CurrTemp3", "$CurrTemp4"]
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
        CurrTemp = CurrTempList[loopValue]
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



        ThermSetDevice = ThermDeviceList[loopValue]
        //log.info "THERM DEVICE IS $ThermSetDevice"
        //ThermSetDevice = ThermSetDevice.value
        HSPSet = HSP[loopValue]
        HSPSet = HSPSet.toInteger()            
        CSPSet = CSP[loopValue]
        CSPSet = CSPSet.toInteger()
        atomicState.CSPSet = CSPSet
        atomicState.HSPSet = HSPSet
        def AltSensorList = ["0", "AltSensor_1", "AltSensor_2", "AltSensor_3"]
        def currentAltLoop = AltSensorList[loopValue]
        def AltSensorMap = ["AltSensor_1": "AlternativeSensor1", "AltSensor_2": "AlternativeSensor2", "AltSensor_3": "AlternativeSensor3"]
        def AltSensorToRun = AltSensorMap.find{ it.key == "$currentAltLoop"}
        AltSensorToRun = AltSensorToRun.value
        //log.info "current Alt loop value is $currentAltLoop : $AltSensorToRun"
        def AltLoop = settings.find{ it.key == "$currentAltLoop" }
        //// log.debug "AltLoop.value (for $ThermSet) is $AltLoop.value"
        AltLoop = AltLoop.value == true
        def AppMgtList = ["0", atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
        def AppMgt = AppMgtList[loopValue]
        log.debug "AppMgt = $AppMgt"
        def ShouldCool = atomicState.ShouldCool || (outsideTemp >= OutsideTempHighThres && CurrTemp >= CSPSet)
        def ShouldHeat = (outsideTemp <= OutsideTempLowThres || CurrTemp <= HSPSet) && !ShouldCool
        if(ShouldCool && ShouldHeat) {
            ShouldCool = false
        }
        if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet) {
            ShouldCool = true
        }        

        // log.debug "ShouldCool = $ShouldCool (Current Temperature Inside = $CurrTemp)"
        // log.debug "ShouldHeat = $ShouldHeat"

        if(ExceptionSW){
            if(ThermSet == "$Thermostat_1"){
                if(SwitchesOn)
                HSPSet = HSPSet + AddDegrees
                CSPSet = CSPSet + SubDegrees
                AppMgt = true
                // log.debug "$ThermSet SetPoints ExceptionSW active and AppMgt set to $AppMgt (should read 'true') to avoid triggering override"                
            }
        }
        if(AppMgt){
            log.info "NO OVERRIDE for $ThermSet"
            if(AltLoop){
                // log.debug "$ThermSet managed by $AltSensorToRun"
                if(loopValue == 1){
                    AlternativeSensor1()
                }
                else if(loopValue == 2){
                    AlternativeSensor2()
                }
                else if(loopValue == 3){
                    AlternativeSensor3()
                }
            } 
            else {
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

                        log.debug "There's motion in ${ThermSet}'s room"
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

                log.debug "CSPSet is $CSPSet"

                // for false overrides prevention

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

                // finally set devices temperatures... 

                log.debug "loading updated $CurrMode Cooling and Heating Set Points for $ThermSet"     
                atomicState.withinApp = true

                // need to call Therm list as a list of objects, not string, so we can pull current setpoint... 

                def ThermObjectList = [null, Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]

                def ThermSetObject = ThermObjectList[loopValue]

                def CurrentCoolingSetPoint = ThermSetObject.currentValue("coolingSetpoint") as Integer
                def CurrentHeatingSetPoint = ThermSetObject.currentValue("heatingSetpoint") as Integer

                log.debug "Current setpoint for $ThemSet is $CurrentCoolingSetPoint and Current Heating setpoint is $CurrentHeatingSetPoint"

                //but only if there's not already the same 
                // this MUST be done in order to avoid overflow of z-wave commands in this while loop

				atomicState.withinApp = true
                
                if(CurrentCoolingSetPoint != CSPSet){
                    ThermSetDevice.setCoolingSetpoint(CSPSet)
                }
                else 
                {
                    log.debug "Cooling SetPoint already set to $CSPSet"
                }

                if(CurrentHeatingSetPoint != HSPSet){
                    ThermSetDevice.setHeatingSetpoint(HSPSet)
                }
                else 
                { 
                    log.debug "Heating SetPoint already set to $HSPSet"
                }

				


                def ThisIsExceptionTherm =  false
                def ContactExceptionIsOpen = false

                if(NoTurnOffOnContact){
                    ThisIsExceptionTherm =  "$ThermSetDevice" == "$Thermostat_1"
                    ContactExceptionIsOpen = atomicState.ContactException
                    // log.debug "ThisIsExceptionTherm is: $ThisIsExceptionTherm (${ThermSetDevice} == ${Thermostat_1})"
                }
                else {
                    ThisIsExceptionTherm =  false
                    ContactExceptionIsOpen = false
                    // log.debug "No exception contact selected by user, values set to false by default"
                }

                if(doorsOk || (ThisIsExceptionTherm && !ContactExceptionIsOpen)){
                    if(((CurrTemp >= HSPSet && ShouldHeat) || (CurrTemp <= CSPSet && ShouldCool)) && ThermState != "off" ){
                        AppMgtTrue() // override test value
                        log.debug "$ThermSet TURNED OFF"      
                        LatestThermostatMode = "off"
                        ThermSetDevice.setThermostatMode("off") 

                    } 
                    else if(CurrTemp >= CSPSet && ThermState != "cool" && ShouldCool){
                        AppMgtTrue() 
                        log.debug "$ThermSet set to cool"
                        LatestThermostatMode = "cool"
                        ThermSetDevice.setThermostatMode("cool")                 
                    }
                    else if(CurrTemp < HSPSet && ThermState != "heat" && ShouldHeat){
                        AppMgtTrue()
                        log.debug "$ThermSet set to Heat"
                        LatestThermostatMode = "heat"
                        ThermSetDevice.setThermostatMode("heat")                       
                    } 

                    // for all situations that are not accounted for by this program... 
                    def TooHot = ($CurrTemp > defaultCSPSet + 4) && ShouldCool
                    if(Actuators && DorrsOk && ThermState != "cool" && TooHot){
                        //state.messageTooHotWindows = 0 // 
                        LatestThermostatMode = "cool"
                        ThermSetDevice.setCoolingSetpoint(defaultCSPSet) // this will trigger an override mode
                        ThermSetDevice.setThermostatMode("cool") 

                        def message = "It is getting hot inside and windows are closed, forcing AC"
                        if(state.messageTooHotWindows == 0){

                            send(message)
                            state.messageTooHotWindows = 1
                        }
                        log.info message
                    }

                }
                else { 
                    log.debug "not evaluating because some windows are open" 
                }
            }   
        }
        else {

            log.debug "${ThermSet} in OVERRIDE MODE, doing nothing -- AppMgt = $AppMgt"
            // record restore modes after end of override


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
        }
        log.trace " END WHILE $loopValue" 

    }   

    // end of while loop


    if(atomicState.SensorHandlerIsRunning != false){
        log.debug "Waiting for motion handler to be done"
    }


    // wait for other loops to finish before reseting this variable otherwhise there's a systemic risk of fals positive override
    runIn(5, withinAppFALSE)

}

def withinAppFALSE(){
    log.debug "Reset atomicState.withinApp to FALSE"
    atomicState.withinApp = false // this value is reset to false so if there's a manual setpoint override it'll be detected as such
}
def AppMgtTrue(){

    def loopValue = state.loopValue

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
def AlternativeSensor1(){
    def doorsOk = atomicState.doorsAreClosed
    if(doorsOk){
        // log.debug "Running Alternative Sensor Loop for $Thermostat_1"
        def SenTemp = Sensor_1.currentTemperature
        // log.debug "Current Temperature at $Sensor_1 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = false 
        def NewCoolSet = false
        atomicState.ThermState = Thermostat_1.currentValue("thermostatMode") as String
        log.trace "atomicState.ThermState for $Thermostat_1 is $atomicState.ThermState"


        def IsOn = atomicState.ThermState in ["heat", "cool"] 

        // log.debug "IsOn?($IsOn)"


        def CurrMode = location.currentMode

        def DefaultSetHeat = null
        def DefaultSetCool = null

        if(CurrMode in Home){
            DefaultSetHeat = HSPH1
            DefaultSetCool = CSPH1
        }
        else if(CurrMode in Night){
            DefaultSetHeat = HSPN1
            DefaultSetCool = CSPN1
        }
        else if(CurrMode in Away){
            DefaultSetHeat = HSPA
            DefaultSetCool = CSPA
        }
        else if(CurrMode in CustomMode1){
            DefaultSetHeat = HSPCust1_T1
            DefaultSetCool = CSPCust1_T1
        }
        else if(CurrMode in CustomMode2){
            DefaultSetHeat = HSPCust2_T1
            DefaultSetCool = CSPCust2_T1
        }

        NewHeatSet = DefaultSetHeat + AltThermOffSet
        NewCoolSet = DefaultSetCool - AltThermOffSet

        NewHeatSet = DefaultSetHeat + AltThermOffSet
        NewCoolSet = DefaultSetCool - AltThermOffSet

        def CurHSP = Thermostat_1.currentHeatingSetpoint
        def CurCSP = Thermostat_1.currentCoolingSetpoint
        log.trace "CurHSP: $CurHSP, CurCSP: $CurCSP, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"

        def NoSPOverride = atomicState.T1_AppMgtSP
        def NoOverride = atomicState.T1_AppMgt

        if(NoSPOverride){
            if(CurHSP != NewHeatSet){
                atomicState.withinApp = true
                Thermostat_1.setHeatingSetpoint(NewHeatSet)
                // log.debug "$Thermostat_1 heating now set to $NewHeatSet"
            }
            else {
                // log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_1.setCoolingSetpoint(NewCoolSet)            
                // log.debug "$Thermostat_1 cooling now set to $NewCoolSet"
            }
            else {
                // log.debug "CurCSP already OK"
            }
        } 
        else {
            // log.debug "$Thermostat_1 in SP OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        // log.debug "OVERRIDEN IS $Overriden"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        // log.debug "ShouldCool = $ShouldCool"

        if(NoOverride){
            // no setpoint override, no on/off override
            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){

                // set proper mode
                if(SenTemp < DefaultSetHeat && !ShouldCool){

                    Thermostat_1.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T1 = "heat"
                    // log.debug "$Thermostat_1 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool && ShouldCool){

                    Thermostat_1.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T1 = "cool"
                    // log.debug "$Thermostat_1 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            } 
            else {
                //turning off this unit
                if(atomicState.ThermState == "off"){
                    // log.debug "$Thermostat_1 stays off"
                    // Thermostat_1.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T1 = "off" // redundant
                    atomicState.T1_AppMgt = true
                }
                else {
                    // log.debug "turning off $Thermostat_1" 
                    atomicState.T1_AppMgt = true
                    Thermostat_1.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T1 = "off"     
                }
            }
            atomicState.NewHeatSet1 = NewHeatSet
            atomicState.NewCoolSet1 = NewCoolSet // references used by heatingSetpointHandler()
            //ShouldbeT1()
        }
        else { 
            // log.debug "$Thermostat_1 in OVERRIDE MODE, doing nothing (alt loop) " 
        }    
    }
    else { 
        // log.debug "some doors are open, AlternativeSensor1 loop not running"

    }
    // runIn(10, resetAppMgt)
}
def AlternativeSensor2(){

    def doorsOk = atomicState.doorsAreClosed // this state must return false for doorsOk to return true

    if(doorsOk){
        // log.debug "Running Alternative Sensor Loop for $Thermostat_2"
        def SenTemp = Sensor_2.currentTemperature
        // log.debug "Current Temperature at $Sensor_2 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        // log.debug "Current Temperature OUTSIDE is ${OutsideTemp}F"
        def NewHeatSet = false 
        def NewCoolSet = false
        def CurrMode = location.currentMode
        atomicState.ThermState = Thermostat_2.currentValue("thermostatMode") // as String
        log.trace "atomicState.ThermState for $Thermostat_2 is $atomicState.ThermState"
        def IsOn = atomicState.ThermState in ["heat", "cool"]

        // log.debug "IsOn?($IsOn)"

        def DefaultSetHeat = null
        def DefaultSetCool = null

        if(CurrMode in Home){
            DefaultSetHeat = HSPH2
            DefaultSetCool = CSPH2
        }
        else if(CurrMode in Night){
            DefaultSetHeat = HSPN2
            DefaultSetCool = CSPN2
        }
        else if(CurrMode in Away){
            DefaultSetHeat = HSPA
            DefaultSetCool = CSPA
        }
        else if(CurrMode in CustomMode1){
            DefaultSetHeat = HSPCust1_T2
            DefaultSetCool = CSPCust1_T2
        }
        else if(CurrMode in CustomMode2){
            DefaultSetHeat = HSPCust2_T2
            DefaultSetCool = CSPCust2_T2
        }

        // log.debug "DefaultSetHeat is : $DefaultSetHeat"
        // log.debug "DefaultSetCool is : $DefaultSetCool"
        NewHeatSet = DefaultSetHeat + AltThermOffSet
        NewCoolSet = DefaultSetCool - AltThermOffSet

        def CurHSP = Thermostat_2.currentHeatingSetpoint
        def CurCSP = Thermostat_2.currentCoolingSetpoint
        log.trace "CurHSP: $CurHSP, CurCSP: $CurCSP, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"

        log.trace "OVERRIDES VALUES (Alt loop2 at 0):  atomicState.T2_AppMgt : $atomicState.T2_AppMgt"

        def NoSPOverride = atomicState.T2_AppMgtSP
        def NoOverride = atomicState.T2_AppMgt

        if(NoSPOverride){
            if(CurHSP != NewHeatSet){
                atomicState.withinApp = true
                Thermostat_2.setHeatingSetpoint(NewHeatSet)
                // log.debug "$Thermostat_2 heating now set to $NewHeatSet"
            }
            else {
                // log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_2.setCoolingSetpoint(NewCoolSet)
                // log.debug "$Thermostat_2 cooling now set to $NewCoolSet"
            }
            else {
                // log.debug "CurCSP already OK"
            }
        } 
        else {
            // log.debug "$Thermostat_2 in SP OVERRIDE MODE (alt loop2 at 0)"
        }

        log.trace "OVERRIDES VALUES (Alt loop2 at 1):  atomicState.T2_AppMgt : $atomicState.T2_AppMgt"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        // log.debug "ShouldCool = $ShouldCool"

        if(NoOverride){

            // no setpoint override, no on/off override
            // log.debug "evaluating for AlternativeSensor2"
            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){

                // set proper mode

                if(SenTemp < DefaultSetHeat && !ShouldCool){

                    Thermostat_2.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T2 = "heat"              
                    // log.debug "$Thermostat_2 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool  && ShouldCool){
                    Thermostat_2.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T2 = "cool"           
                    // log.debug "$Thermostat_2 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            }
            //turning off this unit
            else {

                if(atomicState.ThermState == "off"){
                    // log.debug "$Thermostat_2 stays off"
                    //Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T2 = "off" // redundant 
                    atomicState.T1_AppMgt = true
                }
                else {    
                    // log.debug "turning off $Thermostat_2"
                    atomicState.T2_AppMgt = true
                    Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T2 = "off"
                }
            }
            atomicState.NewHeatSet2 = NewHeatSet
            // log.debug "atomicState.NewHeatSet2 = DefaultSetHeat+$AltThermOffSet, that is: $DefaultSetHeat + $AltThermOffSet = $atomicState.NewHeatSet2"
            atomicState.NewCoolSet2 = NewCoolSet
            // log.debug "atomicState.NewCoolSet2 = DefaultSetCool-$AltThermOffSet, that is: $DefaultSetCool - $AltThermOffSet = $atomicState.NewCoolSet2"
            //shouldBeT2()
        }
        else { 
            // log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing (alt loop2 at 1)" 
        }
    }     
    else 
    { 
        // log.debug "some doors are open, AlternativeSensor2 loop not running"

    }
    // runIn(10, resetAppMgt)
}
def AlternativeSensor3(){

    def doorsOk = atomicState.doorsAreClosed
    if(doorsOk){
        // log.debug "Running Alternative Sensor Loop for $Thermostat_3"
        def SenTemp = Sensor_3.currentTemperature
        // log.debug "Current Temperature at $Sensor_3 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = false 
        def NewCoolSet = false
        def CurrMode = location.currentMode
        atomicState.ThermState = Thermostat_3.currentValue("thermostatMode") as String
        log.trace "atomicState.ThermState for $Thermostat_3 is $atomicState.ThermState"
        def IsOn = atomicState.ThermState in ["heat", "cool"]

        // log.debug "IsOn?($IsOn)"

        def DefaultSetHeat = null
        def DefaultSetCool = null

        if(CurrMode in Home){
            DefaultSetHeat = HSPH3
            DefaultSetCool = CSPH3
        }
        else if(CurrMode in Night){
            DefaultSetHeat = HSPN3
            DefaultSetCool = CSPN3
        }
        else if(CurrMode in Away){
            DefaultSetHeat = HSPA
            DefaultSetCool = CSPA
        }
        else if(CurrMode in CustomMode1){
            DefaultSetHeat = HSPCust1_T3
            DefaultSetCool = CSPCust1_T3
        }
        else if(CurrMode in CustomMode2){
            DefaultSetHeat = HSPCust2_T3
            DefaultSetCool = CSPCust2_T3
        }

        NewHeatSet = DefaultSetHeat + AltThermOffSet
        NewCoolSet = DefaultSetCool - AltThermOffSet

        def CurHSP = Thermostat_3.currentHeatingSetpoint
        def CurCSP = Thermostat_3.currentCoolingSetpoint
        log.trace "CurHSP: $CurHSP, CurCSP: $CurCSP, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"

        def NoSPOverride = atomicState.T3_AppMgtSP
        def NoOverride = atomicState.T3_AppMgt

        if(NoSPOverride){
            if(CurHSP != NewHeatSet){
                atomicState.withinApp = true
                Thermostat_3.setHeatingSetpoint(NewHeatSet)
                // log.debug "$Thermostat_3 heating now set to $NewHeatSet"
            }
            else {
                // log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_3.setCoolingSetpoint(NewCoolSet)
                // log.debug "$Thermostat_3 cooling now set to $NewCoolSet"
            }
            else {
                // log.debug "CurCSP already OK"
            }
        } 
        else {
            // log.debug "$Thermostat_3 in SP OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        // log.debug "OVERRIDEN IS $Overriden"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        // log.debug "ShouldCool = $ShouldCool"


        if(NoOverride){

            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){
                // set proper mode
                if(SenTemp < DefaultSetHeat && !ShouldCool){
                    Thermostat_3.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T3 = "heat"
                    // log.debug "$Thermostat_3 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool  && ShouldCool){
                    Thermostat_3.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T3 = "cool"
                    // log.debug "$Thermostat_3 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            } 
            else {
                //turning off this unit

                if(atomicState.ThermState == "off"){
                    // log.debug "$Thermostat_3 stays off"
                    // Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T3 = "off" // redundant
                    atomicState.T1_AppMgt = true
                }
                else {
                    atomicState.T3_AppMgt = true
                    // log.debug "turning off $Thermostat_3"      
                    Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T3 = "off"            
                }
            }
            atomicState.NewHeatSet3 = NewHeatSet
            atomicState.NewCoolSet3 = NewCoolSet
            //shouldBeT3()
        }
        else { 
            // log.debug "$Thermostat_3 in OVERRIDE MODE, doing nothing" 
        }
    }
    else { 
        // log.debug "some doors are open, AlternativeSensor1 loop not running"

    }
    //runIn(10, resetAppMgt)
}

def TurnOffThermostats(){

    //if(atomicState.ThermOff == false){
    // this conditions was meant to allow to turn thermostats on even when windows open... 
    if(atomicState.CRITICAL == false){
        // log.debug "Turning off thermostats" 
        def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
        def ContactExceptionIsOpen = true // needed to prevent exception error if this option wasn't picked

        if(ContactException){
            ContactExceptionIsOpen = ContactException.latestValue("contact").contains("open")   
            atomicState.doorsAreClosed = ContactExceptionIsOpen
        }

        if(!ContactExceptionIsOpen){
            Thermostat_1.setThermostatMode("auto") 
            atomicState.LatestThermostatMode = "auto"
            log.debug "$Thermostat_1  turned on (set to auto)"
            atomicState.T1_AppMgt = true
        }


        // log.debug "ContactExceptionIsOpen: $ContactExceptionIsOpen, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"
        if(!NoTurnOffOnContact || !InExceptionContactMode || ContactExceptionIsOpen){
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
def resetLocationChangeVariable(){
    atomicState.locationModeChange = false
    log.debug "atomicState.locationModeChange reset to FALSE"
}
def resetOverride(){
    // log.debug "OVERRIDE RESET"
    state.ThermostatOverriden = "none"


}
def polls(){

    def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
    def ModeIndexValue = MapofIndexValues.find{ it.key == "$location.currentMode"}
    ModeIndexValue = ModeIndexValue.value.toInteger()
    def CtrlSwtch = [null, CtrlSwtH, CtrlSwtN, null, CtrlSwtCust1, CtrlSwtCust2]
    //def CtrlSwtch = ["null", "$CtrlSwtH", "$CtrlSwtN", "null", "$CtrlSwtCust1", "$CtrlSwtCust2"]       
    CtrlSwtch = CtrlSwtch[ModeIndexValue]
    def CtrlSwtchPoll = CtrlSwtch?.hasCommand("poll")
    def CtrlSwtchRefresh = CtrlSwtch?.hasCommand("refresh")

    if(CtrlSwtchPoll){
        CtrlSwtch?.poll()
        // log.debug "polling $CtrlSwtch"
    }
    else if(CtrlSwtchRefresh){
        CtrlSwtch?.refresh()
        // log.debug "refreshing $CtrlSwtch"
    }
    else { // log.debug "$CtrlSwtch does not support either poll() nor refresh() commands"
    }

    if(Thermostat_1){
        def poll = Thermostat_1.hasCommand("poll")
        def refresh = Thermostat_1.hasCommand("refresh")
        if(poll){
            Thermostat_1.poll()
            // log.debug "polling Thermostat_1"
        }
        else if(refresh){
            Thermostat_1.refresh()
            // log.debug "refreshing Thermostat_1"
        }
        else { 
            log.debug "Thermostat_1 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_2){
        def poll = Thermostat_2.hasCommand("poll")
        def refresh = Thermostat_2.hasCommand("refresh")
        if(poll){
            Thermostat_2.poll()
            // log.debug "polling Thermostat_2"
        }
        else if(refresh){
            Thermostat_2.refresh()
            // log.debug "refreshing Thermostat_2"
        }
        else { // log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_3){
        def poll = Thermostat_3.hasCommand("poll")
        def refresh = Thermostat_3.hasCommand("refresh")
        if(poll){
            Thermostat_3.poll()
            // log.debug "polling Thermostat_3"
        }
        else if(refresh){
            Thermostat_3.refresh()
            // log.debug "refreshing Thermostat_3"
        }
        else { // log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
        }
    }
    if(Thermostat_4){
        def poll = Thermostat_4.hasCommand("poll")
        def refresh = Thermostat_4.hasCommand("refresh")
        if(poll){
            Thermostat_4.poll()
            // log.debug "polling Thermostat_4"
        }
        else if(refresh){
            Thermostat_4.refresh()
            // log.debug "refreshing Thermostat_4"
        }
        else { 
            // log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
        }
    }
    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            // log.debug "polling OutsideSensor"
        }
        else if(refresh){
            OutsideSensor.refresh()
            // log.debug "refreshing OutsideSensor"
        }
        else { // log.debug "OutsideSensor does not support either poll() nor refresh() commands"
        }
    }
}
def CheckWindows(){

    def CurrentContacts = contact.latestValue("contact")
    log.debug "contact latest Value $CurrentContacts"

    def ClosedContacts = CurrentContacts.findAll { AllcontactsClosed ->
        AllcontactsClosed == "closed" ? true : false
    }
    log.debug "${ClosedContacts.size()} windows/doors out of ${contact.size()} are closed"

    def ContactsOpen = contact.latestValue("contact").contains("open")
    def ContactExceptionIsOpen = ContactException.latestValue("contact").contains("open")   

    if(ContactsOpen || ContactExceptionIsOpen ){
        atomicState.doorsAreClosed = false
    }

    log.debug "Contacts Opened?($ContactsOpen)"

    log.debug "Checking windows"
    def Inside = XtraTempSensor.currentValue("temperature")
    def Outside = OutsideSensor.currentValue("temperature")

    def OkToOpen = OkToOpen() // outside and inside temperatures criteria

    log.trace """
OkToOpen?($OkToOpen); 
OffSet?($OffSet) 
atomicState.ClosedByApp($atomicState.ClosedByApp) 
atomicState.OpenByApp($atomicState.OpenByApp) 
atomicState.messageSent($atomicState.messageSent) 

"""
    def message = ""

    if(OkToOpen){

        // atomicState.hasRun changes with modes change //              atomicState.ClosedByUser = false

        if(!ContactsOpen &&  atomicState.ClosedByApp == true)  {

            Actuators?.on()

            atomicState.OpenByApp = true
            atomicState.ClosedByApp = false // so it doesn't open again

            log.debug "opening windows"

            message = "Conditions permitting, I'm opening $Actuators. Operation time is $OperatingTime seconds"
            log.info message 
            send(message)


            atomicState.messageSent = atomicState.messageSent + 1

            if(OperatingTime){
                // log.debug "stop in $OperatingTime seconds"
                runIn(OperatingTime, StopActuators)              
            }
        }
        else { 
            log.debug "Windows have already been opened, doing nothing" 
        }
    }
    else if (atomicState.OpenByApp == true) {

        Actuators?.off()

        atomicState.ClosedByApp = true
        atomicState.OpenByApp = false // so it doesn't close again if user opens it manually


        log.debug "closing windows"
        message = "I'm closing $Actuators."
        log.info message 
        send(message)
        atomicState.closed = true
        atomicState.hasRun = 0 // this is an integer beware of not replacing with bool

        atomicState.WindowsAppOpened = false
        atomicState.WindowsAppClosed = true
        atomicState.messageSent = 0
    }
    else if(atomicState.messageSent == 0 && ContactsOpen && atomicState.WindowsAppOpened == true){ 

        message = "IMPORTANT MESSAGE $Actuators will not close again until you close them yourself!"
        log.info message
        send(message)
        state.messageSent = state.messageSent + 1
    }
    else if(ContactsOpen && atomicState.WindowsAppOpened == false){
        // log.debug "WINDOWS MANUALLY OPENED"
    }
    /*
def notAllClosed = contact.latestValue("contact").contains("open")
if(atomicState.ClosedByApp == true && notAllClosed){
// making sure... for now
Actuators?.off()
}
*/

}
def OkToOpen(){

    def humidity = HumidityMeasurement.latestValue("humidity") 
    def TooHumid = humidity > HumidityTolerance

    def CSPSet = atomicState.CSPSet
    def HSPSet = atomicState.HSPSet 

    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature") 
    def CurrTemp = Inside
    def Outside = OutsideSensor.currentValue("temperature") 
    def outsideTemp = Outside
    def WithinCriticalOffSet = Inside >= CriticalTemp + OffSet
    def notAllClosed = contact.latestValue("contact").contains("open")
    def closed = !notAllClosed
    def OutsideTempHighThres = OutsideTempHighThres
    def ExceptHighThreshold1 = ExceptHighThreshold1

    def ShouldCool = atomicState.ShouldCool || (outsideTemp >= OutsideTempHighThres && CurrTemp >= CSPSet)
    def ShouldHeat = (outsideTemp <= OutsideTempLowThres || CurrTemp <= HSPSet) && !ShouldCool
    if(ShouldCool && ShouldHeat) {
        ShouldCool = false
    }
    if(!ShouldCool && !ShouldHeat && CurrTemp >= CSPSet) {
        ShouldCool = true
    }        

    // log.debug "ShouldCool = $ShouldCool"
    // log.debug "ShouldHeat = $ShouldHeat"


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

    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4
    }


    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid

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
closed?($closed)
OutSideWithinMargin?($OutSideWithinMargin)
Inside is WithinCriticalOffSet?($WithinCriticalOffSet) 
ShouldCool?($ShouldCool)    
TooHumid?($TooHumid)
Humidity is: $humidity
OkToOpen?($result)
"""

    return result


}
def StopActuators(){
    def OpenInFull = state.OpenInFull
    if(!OpenInFull){
        Actuators?.stop()
    }
}
private schedules() { 

    def scheduledTimeA = 1
    def scheduledTimeB = 5

    //schedule("0 0/$scheduledTimeA * * * ?", TemperaturesModes)
    //log.debug "TemperaturesModes scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    // log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        //// log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }


}

private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
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


