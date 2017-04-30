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

        section("how many thermostats do you want to control?") { 

            input(name: "HowMany", type: "number", range: "1..4", title: "set a value between 1 and 4", description: null, submitOnChange: true)

            if(HowMany >= 1) {
                input(name: "Thermostat_1", type: "capability.thermostat", title: "Thermostat 1 is $Thermostat_1", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_1", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_1){
                    input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
            }
            if(HowMany >= 2) {
                input(name: "Thermostat_2", type: "capability.thermostat", title: "Thermostat 2 is $Thermostat_2", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_2", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_2){
                    input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
            }
            if(HowMany >= 3) {
                input(name: "Thermostat_3", type: "capability.thermostat", title: "Thermostat 3 is $Thermostat_3", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_3", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_3){
                    input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
                input(name: "ExceptionSW", type : "bool", title: "Make an exception for this device when a switch is on | setting to be completed in the next page", 
                      defaut: false, submitOnChange: true)
            }
            if(HowMany == 4) {
                input(name: "Thermostat_4", type: "capability.thermostat", title: "Thermostat 4 is $Thermostat_4", required: false, multiple: false, description: null, submitOnChange: true)
            }
        }
        section(""){
            if(AltSensor1 || AltSensor_2 || AltSensor_3){
                input(name: "AltThermOffSet", type: "number", title: "OffSet value for your alternative sensors?", default: 0, required: false, description: "leave blank if none")
                input(name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Pick a sensor for Outside's temperature", required: true, multiple: false, description: null)
                paragraph "This sensor is rendered mandatory by selecting alternate sensor management (above) or windows actuators management (below). If you do not have an outside temp measurment device, you can allways create a SmartWeater virtual device"
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
                paragraph "If within margin then open. But not if inside's temp is lower then heat setting minus offset. Reference is $XtraTempSensor"
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

        section("Thermostats temperatures for $Home Mode"){
            if(HowMany >= 1) {
                input(name: "HSPH1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Home mode", required: true)
                input(name: "CSPH1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Home mode", required: true)

                if(ExceptionSW){              
                    input(name: "CtrlSwtH", type: "capability.switch", title: "Make an exception When this switch is on", required: false, submitOnChange: true)
                    if(CtrlSwtH){
                        input(name: "AddDegrees", type: "decimal", title: "Add this amount of degrees to $Thermostat_1 heat setting in $Home mode", required: true)
                        input(name: "SubDegrees", type: "decimal", title: "Substract this amount of degrees to $Thermostat_3 cooling setting in $Home mode", required: true)                     
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
    log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler
    init()
}
def updated() {
    atomicState.modeStartTime = now() 
    state.WindowsAppOpened = false // needed at first to avoid false turning on when updated or mode changed
    atomicState.ThermOff = false // allows units override in open windows modes
    log.debug "updated with settings = $settings $Modes"
    unsubscribe()
    unschedule()

    init()
}
def init() {

    log.debug "enter updated, state: $state"  

    subscribe(contact, "contact.open", contactHandlerOpen)
    subscribe(contact, "contact.closed", contactHandlerClosed)
    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    subscribe(Thermostat_1, "temperature", temperatureHandler)
    subscribe(Thermostat_1, "thermostatMode", ThermostatSwitchHandler)
    subscribe(Thermostat_1, "heatingSetpoint", setpointHandler)
    subscribe(Thermostat_1, "coolingSetpoint", setpointHandler)


    def swichtCapableCap = Thermostat_1.hasCapability("Switch")
    def swichtCapableLow = Thermostat_1.hasCapability("switch")
    if(swichtCapableLow){
        subscribe(Thermostat_1, "switch", ThermostatSwitchHandler)
        //log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
    } else if(swichtCapableCap){
        subscribe(Thermostat_1, "Switch", ThermostatSwitchHandler)
        //log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
    } else { log.debug "no switch capability for $Thermostat_1" }
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
        subscribe(OutsideSensor, "temperature", temperatureHandler)
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


    /*  */

    state.ThermostatOverriden = "none"
    atomicState.ThisIsManual = false
    atomicState.closed = true // windows management
    //atomicState.hasRun = 0 // must not be reset here

    atomicState.messageSent = 0

    atomicState.locationModeChange = true 
    runIn(60, resetLocationChangeVariable)
    runIn(10,TemperaturesModes)
    schedules()

}
def switchHandler(evt){
    log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        atomicState.exception = true
    } else {
        atomicState.exception = false
    }

    runIn(10, TemperaturesModes)

}
def setpointHandler(evt){

    log.debug "New $evt.name for $evt.device : $evt.value"
    log.debug "evt.displayName is $evt.displayName -------------------------------"
    state.ThermostatOverriden = evt.displayName

    if(atomicState.withinApp == false){
        //atomicState.SPValue = evt.value
        //atomicState.SPname = evt.name
        //atomicState.devicedisplayName = evt.displayName
        //atomicState.SPdevice = evt.device


        def CurrMode = location.currentMode

        //CurrMode = CurrMode[0]
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
        //log.info "device is ------------------- $KeyValueForThisTherm.value"
        def ThermNumber = KeyValueForThisTherm.value
        ThermNumber = KeyValueForThisTherm.value.toInteger()

        //log.info "ThermNumber is ------------------- $ThermNumber"

        //array heat
        def HSPH = ["0","$HSPH1", "$HSPH2", "$HSPH3", "$HSPH4"]
        def HSPN = ["0","$HSPN1", "$HSPN2", "$HSPN3", "$HSPN4"]
        def HSPA_ = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
        def HSPCust1 = ["0","$HSPCust1_T1", "$HSPCust1_T2", "$HSPCust1_T3", "$HSPCust1_T4"]
        def HSPCust2 = ["0","$HSPCust2_T1", "$HSPCust2_T2", "$HSPCust2_T3", "$HSPCust2_T4"]

        // declare an integer value for current mode
        def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
        def ModeIndexValue = MapofIndexValues.find{ it.key == "$CurrMode"}
        //log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
        ModeIndexValue = ModeIndexValue.value

        ModeIndexValue = ModeIndexValue.toInteger()
        //log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"

        //array cool
        def CSPH = ["0","$CSPH1", "$CSPH2", "$CSPH3", "$CSPH4"]
        def CSPN = ["0","$CSPN1", "$CSPN2", "$CSPN3", "$CSPN4"]
        def CSPA_ = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
        def CSPCust1 = ["0","$CSPCust1_T1", "$CSPCust1_T2", "$CSPCust1_T3", "$CSPCust1_T4"]
        def CSPCust2 = ["0","$CSPCust2_T1", "$CSPCust2_T2", "$CSPCust2_T3", "$CSPCust2_T4"]

        // now transpose corresponding values among tables
        // for heat values
        def ListHSPs = ["0", HSPH, HSPN, HSPA, HSPCust1, HSPCust2]
        def HSPModecheck = ListHSPs[ModeIndexValue]
        def RefHeat = HSPModecheck[ThermNumber]
        //do the same with cooling values
        def ListCSPs = ["0", CSPH, CSPN, CSPA, CSPCust1, CSPCust2]
        def CSPModecheck = ListCSPs[ModeIndexValue]
        def RefCool = CSPModecheck[ThermNumber]

        // for thermostat set to work based on alternate temp sensors, the Alternativesensor() loops will 
        // simply stop running after this new setting has been compared to settings() in the arrays above
        // by declaring the atomicState.AppMgnt_T_X variable as false.  

        if(evt.name == "heatingSetpoint"){              
            reference = RefHeat
            //reference = Math.round(Double.parseDouble(RefHeat))
            //reference = reference.toInteger()
            log.debug "RefHeat was: $RefHeat"
            log.debug "RefHeat is now converted to a reference as: $reference"

        }
        else  if(evt.name == "coolingSetpoint"){ 
            reference = RefCool
            //reference = Math.round(Double.parseDouble(RefCool))
            //reference = reference.toInteger()
            log.debug "RefCool was: $RefCool"
            log.debug "RefCool is now converted to a reference as: $reference"
        }

        def ThisIsModeChange = atomicState.locationModeChange
        def ExceptionState = atomicState.exception
        def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState
        log.debug "ExceptionSwitch?($thisIsExceptionTemp)"
        ///
        def Value = evt.value
        //def Value = Math.round(Double.parseDouble(evt.value))
        Value = Value.toInteger()
        log.debug "Evt value to Integer is : $Value"
        log.debug "Value is to be compared to : $reference"


        if(Value == reference || ThisIsModeChange || thisIsExceptionTemp){  
            log.debug "NO SETPOINT OVERRIDE"
            atomicState.ThisIsManual = false

        }
        else {       
            atomicState.ThisIsManual = true

            log.debug "MANUAL SETPOINT OVERRIDE for $evt.device"
        }

        //     
        if(evt.displayName == "${Thermostat_1}" && !thisIsExceptionTemp)
        {
            if(atomicState.ThisIsManual == true){
                atomicState.T1_AppMgt = false
                atomicState.T2_AppMgtSP = false
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
        log.debug "RefHeat is: $RefHeat"
        log.debug "RefCool is: $RefCool"
        log.debug "reference is: $reference"
        log.debug """SetPoint Change was Manual? ($atomicState.ThisIsManual) if false then should have $reference = $Value 
(unless location mode just changed or Exception Switch is on)"""
        atomicState.RefHeat = RefHeat
        atomicState.RefCool = RefCool
    }
    else{
        log.debug "Not evaluating SETPOINT OVERRIDE because the command came from within the app itself"
    }
    runIn(10, TemperaturesModes)
}

def ThermostatSwitchHandler(evt){

    log.debug "$evt.device set to $evt.value "

    log.debug "CHECKING COMMAND ORIGIN for $evt.device "

    if(atomicState.CRITICAL == false){
        def CurrMode = location.currentMode
        def LocatioModeChange = atomicState.locationModeChange
        log.debug "Location Mode Changed?($LocatioModeChange)"


        log.trace "Latest Thermostat ModeS : $atomicState.LatestThermostatMode_T1 | $atomicState.LatestThermostatMode_T2 | $atomicState.LatestThermostatMode_T3 | $atomicState.LatestThermostatMode_T4"
        def MapofShouldBe = ["$Thermostat_1": atomicState.LatestThermostatMode_T1, "$Thermostat_2": atomicState.LatestThermostatMode_T2, "$Thermostat_3": atomicState.LatestThermostatMode_T3, "$Thermostat_4": atomicState.LatestThermostatMode_T4]
        def WhichThermInvolved = MapofShouldBe.find{ it.key == "$evt.device"} 
        def Shouldbe = WhichThermInvolved.value
        log.debug "ShouldBe Values : ${WhichThermInvolved}"

        def ShouldBe = WhichThermInvolved.value.toString()

        // is the current SetPoint app managed or manually set?
        def IsSpAppManagedMap = ["${Thermostat_1}": atomicState.T1_AppMgtSP, "${Thermostat_2}" : atomicState.T2_AppMgtSP, "${Thermostat_3}" : atomicState.T3_AppMgtSP, "${Thermostat_4}" : atomicState.T4_AppMgtSP]
        def IsSpAppManaged = IsSpAppManagedMap.find{it.key == "$evt.device"}
        IsSpAppManaged = IsSpAppManaged.value
        log.debug "IsSpAppManaged?($IsSpAppManaged)"

        // is current state app managed or manually set?
        log.debug " for $evt.device shoudlbe: evt.value($evt.value) =? ShouldBe($ShouldBe)"
        def IdenticalShouldbe = evt.value == ShouldBe && IsSpAppManaged
        log.debug "IDENTICAL?($IdenticalShouldbe)"

        def ExceptionState = atomicState.exception
        def thisIsExceptionTemp = evt.displayName == "$Thermostat_1" && ExceptionState
        log.debug "ExceptionSwitch?($thisIsExceptionTemp)"




        //make sure that  : 
        // 1) the thermostat DID effectively had its operating mode changed
        // 2) this change was not triggered by a home location change 
        // 3) or by the TemperaturesModes() loop
        // 4) no setpoint exception

        def ThereWasChange = ShouldBe != evt.value && !LocatioModeChange && !ChangedByApp

        log.debug " Change($ThereWasChange)"

        if(evt.displayName == "${Thermostat_1}" ){
            if(IdenticalShouldbe || thisIsExceptionTemp ){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_1"
                atomicState.T1_AppMgt = true


            }
            else if(!IdenticalShouldbe && !thisIsExceptionTemp ){
                // command did not come from app so manual or set point is manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_1"
                atomicState.T1_AppMgt = false

            }       
        }
        else if(evt.displayName == "${Thermostat_2}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_2"
                atomicState.T2_AppMgt = true


            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_2"
                atomicState.T2_AppMgt = false

            }     
        } 
        else if(evt.displayName == "${Thermostat_3}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_3"
                atomicState.T3_AppMgt = true


            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_3"
                atomicState.T3_AppMgt = false

            }     
        } 
        else if(evt.displayName == "${Thermostat_4}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "END of MANUAL OVERRIDE for $Thermostat_4"
                atomicState.T4_AppMgt = true


            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_4"
                atomicState.T4_AppMgt = false

            }     
        }


    }
    else { log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" }
    runIn(10, TemperaturesModes)
}
def temperatureHandler(evt) { 

    //log.debug "The source of this event is: ${evt.source}"

    log.debug "current temperature value for $evt.device is $evt.value" 
    def currentTemp = XtraTempSensor.currentValue("temperature")
    log.debug "Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp"


    if(currentTemp <= CriticalTemp) {
        log.debug "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 

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
            log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                log.debug "CLOSING WINDOWS"
                Actuators?.off()
                // allow for user to reopen them if they want to. 
                state.windowswereopenandclosedalready = true // windows won't close again as long as temperature is still critical to allow for user's override
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
    TemperaturesModes()
}
def contactHandlerClosed(evt) {

    log.debug "$evt.device is $evt.value"

    if(contact.latestValue("contact").contains("open")){
        log.debug "Not all contacts are closed, doing nothing"
        atomicState.doorsAreOpen = true
    }
    else {             
        atomicState.doorsAreOpen = false

        runIn(10, TemperaturesModes)
        log.info "all contacts are closed, unscheduling previous TurnOffThermostats command, cancelling any override by updating all values: update()"
        unschedule(TurnOffThermostats) // in case were closed within time frame
        updated() // reset to all default settings
    } 

    alldoorsareclosed()
}
def contactHandlerOpen(evt) {
    atomicState.doorsAreOpen = true
    log.debug "$evt.device is now $evt.value" 
    log.debug "Turning off all thermostats in $TimeBeforeClosing seconds"
    runIn(TimeBeforeClosing, TurnOffThermostats)   

    if(Actuators){

        CheckWindows()
    }
}
def ChangedModeHandler(evt) {

    log.debug "mode changed to ${evt.value}"
    atomicState.hasRun = 0 /// should be 0 set to 1 for tests

    updated()

}
// main loop
def TemperaturesModes(){

    log.trace "MAIN LOOP RUNNING"
    def doorsOk = alldoorsareclosed()
    if(doorsOk){
        def CurrMode = location.currentMode
        def outsideTemp = OutsideSensor.currentTemperature
        log.debug "atomicState.ThisIsManual value is $atomicState.ThisIsManual"
        def CurrTemp1 = Thermostat_1?.currentTemperature
        def ThermState1 = Thermostat_1?.currentValue("thermostatMode") as String
        def CurrTemp2 = Thermostat_2?.currentTemperature
        def ThermState2 = Thermostat_2?.currentValue("thermostatMode") as String   
        def CurrTemp3 = Thermostat_3?.currentTemperature
        def ThermState3 = Thermostat_3?.currentValue("thermostatMode") as String 
        def CurrTemp4 = Thermostat_4?.currentTemperature
        def ThermState4 = Thermostat_4?.currentValue("thermostatMode") as String

        log.trace """T1 : $atomicState.T1_AppMgt, T2 : $atomicState.T2_AppMgt, T3 : $atomicState.T3_AppMgt, T4 : $atomicState.T4_AppMgt"""

        // which HSP? 
        log.debug "CurrMode is $CurrMode mode"
        //array heat
        def HSP1 = ["0","$HSPH1", "$HSPN1", "$HSPA1", "$HSPCust1_T1", "$HSPCust2_T1"]
        def HSP2 = ["0","$HSPH2", "$HSPN2", "$HSPA2", "$HSPCust1_T2", "$HSPCust2_T2"]
        def HSP3 = ["0","$HSPH3", "$HSPN3", "$HSPA3", "$HSPCust1_T3", "$HSPCust2_T3"]
        def HSP4 = ["0","$HSPH4", "$HSPN4", "$HSPA4", "$HSPCust1_T4", "$HSPCust2_T4"]
        //array cool
        def CSP1 = ["0","$CSPH1", "$CSPN1", "$CSPA1", "$CSPCust1_T1", "$CSPCust2_T1"]
        def CSP2 = ["0","$CSPH2", "$CSPN2", "$CSPA2", "$CSPCust1_T2", "$CSPCust2_T2"]
        def CSP3 = ["0","$CSPH3", "$CSPN3", "$CSPA3", "$CSPCust1_T3", "$CSPCust2_T3"]
        def CSP4 = ["0","$CSPH4", "$CSPN4", "$CSPA4", "$CSPCust1_T4", "$CSPCust2_T4"]
        // declare an integer value for current mode
        def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
        def ModeIndexValue = MapofIndexValues.find{ it.key == "$location.currentMode"}
        ModeIndexValue = ModeIndexValue.value.toInteger()
        //log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
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

        def SwitchesOn = CtrlSwtch?.latestValue("switch").contains("on")
        log.debug "SwitchesOn($SwitchesOn)"
        def HSP = ["0", "$HSP1", "$HSP2", "$HSP3", "$HSP4"]
        //log.debug "HSP LIST is $HSP"
        def HSPSet = 0
        def CSP = ["0", "$CSP1", "$CSP2", "$CSP3", "$CSP4"]
        //log.debug "CSP LIST is $CSP"
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
        def NullThermFind = Therm.findAll { val ->
            val == "null" ? true : false
        }
        def ThermsInvolved = Therm.size() - NullThermFind.size() - 1 
        // -1 because of index 0
        ThermsInvolved = ThermsInvolved.toInteger()
        //log.debug "Number of Thermostats Selected by User : $ThermsInvolved"
        def loopValue = 0
        //log.info "$settings"
        while(loopValue < ThermsInvolved){
            loopValue++
                state.loopValue == loopValue
            log.info "loop($loopValue)"
            CurrTemp = CurrTempList[loopValue]
            ThermState = ThermStateList[loopValue]
            ThermSet = Therm[loopValue]
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
            //log.debug "AltLoop.value (for $ThermSet) is $AltLoop.value"
            AltLoop = AltLoop.value == true
            def AppMgtList = ["0", atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
            def AppMgt = AppMgtList[loopValue]
            log.debug "AppMgt = $AppMgt"
            def ShouldCool = outsideTemp >= OutsideTempHighThres && CurrTemp >= CSPSet
            def ShouldHeat = outsideTemp <= OutsideTempLowThres || CurrTemp <= CSHSet
            if(ShouldCool && ShouldHeat) {
                ShouldCool = false
            }
            log.debug "ShouldCool = $ShouldCool"
            log.debug "ShouldHeat = $ShouldHeat"

            if(ExceptionSW){
                if(ThermSet == "$Thermostat_1"){
                    if(SwitchesOn)
                    HSPSet = HSPSet + AddDegrees
                    CSPSet = CSPSet + SubDegrees
                    AppMgt = true
                    log.debug "$ThermSet SetPoints ExceptionSW active and AppMgt set to $AppMgt (should read 'true') to avoid triggering override"                
                }
            }
            if(AppMgt){
                if(AltLoop){
                    log.debug "$ThermSet managed by $AltSensorToRun"
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
                    log.debug "loading $CurrMode settings for $ThermSet"     
                    atomicState.withinApp = true
                    ThermSetDevice.setHeatingSetpoint(HSPSet)
                    ThermSetDevice.setCoolingSetpoint(CSPSet)
                    if(CurrTemp > HSPSet && ThermState != "off" && !ShouldCool){
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
                }   
            }
            else {

                log.debug "${ThermSet} in OVERRIDE MODE, doing nothing -- AppMgt = $AppMgt"

                if(CurrTemp > HSPSet && ThermState != "off" && !ShouldCool){

                    log.debug "$ThermSet SHOULD BE OFF"      
                    LatestThermostatMode = "off"
                } 
                else if(CurrTemp >= CSPSet && ThermState != "cool" && ShouldCool){

                    log.debug "$ThermSet SHOULD BE set to cool"
                    LatestThermostatMode = "cool"
                }
                else if(CurrTemp < HSPSet && ThermState != "heat" && !ShouldCool){

                    log.debug "$ThermSet SHOULD BE set to Heat"
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
$Thermostat_1 should be $atomicState.LatestThermostatMode_T1,
$Thermostat_2 should be $atomicState.LatestThermostatMode_T2,
$Thermostat_3 should be $atomicState.LatestThermostatMode_T3,
$Thermostat_4 should be $atomicState.LatestThermostatMode_T4"""
            }
        }
        // end of while loop
    }
    else {
        log.debug "Some windows or doors are open, doing nothing"
        TurnOffThermostats()
    }
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
    def doorsOk = alldoorsareclosed()
    if(doorsOk){
        log.debug "Running Alternative Sensor Loop for $Thermostat_1"
        def SenTemp = Sensor_1.currentTemperature
        log.debug "Current Temperature at $Sensor_1 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = false 
        def NewCoolSet = false
        atomicState.ThermState = Thermostat_1.currentValue("thermostatMode") as String
        log.trace "atomicState.ThermState for $Thermostat_1 is $atomicState.ThermState"


        def IsOn = atomicState.ThermState in ["heat", "cool"] 

        log.debug "IsOn?($IsOn)"


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
                log.debug "$Thermostat_1 heating now set to $NewHeatSet"
            }
            else {
                log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_1.setCoolingSetpoint(NewCoolSet)            
                log.debug "$Thermostat_1 cooling now set to $NewCoolSet"
            }
            else {
                log.debug "CurCSP already OK"
            }
        } 
        else {
            log.debug "$Thermostat_1 in SP OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        log.debug "OVERRIDEN IS $Overriden"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        log.debug "ShouldCool = $ShouldCool"

        if(NoOverride){
            // no setpoint override, no on/off override
            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){

                // set proper mode
                if(SenTemp < DefaultSetHeat && !ShouldCool){

                    Thermostat_1.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T1 = "heat"
                    log.debug "$Thermostat_1 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool && ShouldCool){

                    Thermostat_1.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T1 = "cool"
                    log.debug "$Thermostat_1 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            } 
            else {
                //turning off this unit
                if(atomicState.ThermState == "off"){
                    log.debug "$Thermostat_1 stays off"
                    // Thermostat_1.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T1 = "off" // redundant
                    atomicState.T1_AppMgt = true
                }
                else {
                    log.debug "turning off $Thermostat_1" 
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
            log.debug "$Thermostat_1 in OVERRIDE MODE, doing nothing (alt loop) " 
        }    
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
        TurnOffThermostats()
    }
    // runIn(10, resetAppMgt)
}
def AlternativeSensor2(){

    def doorsOk = alldoorsareclosed()

    if(doorsOk){
        log.debug "Running Alternative Sensor Loop for $Thermostat_2"
        def SenTemp = Sensor_2.currentTemperature
        log.debug "Current Temperature at $Sensor_2 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        log.debug "Current Temperature OUTSIDE is ${OutsideTemp}F"
        def NewHeatSet = false 
        def NewCoolSet = false
        def CurrMode = location.currentMode
        atomicState.ThermState = Thermostat_2.currentValue("thermostatMode") // as String
        log.trace "atomicState.ThermState for $Thermostat_2 is $atomicState.ThermState"
        def IsOn = atomicState.ThermState in ["heat", "cool"]

        log.debug "IsOn?($IsOn)"

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

        log.debug "DefaultSetHeat is : $DefaultSetHeat"
        log.debug "DefaultSetCool is : $DefaultSetCool"
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
                log.debug "$Thermostat_2 heating now set to $NewHeatSet"
            }
            else {
                log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_2.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_2 cooling now set to $NewCoolSet"
            }
            else {
                log.debug "CurCSP already OK"
            }
        } 
        else {
            log.debug "$Thermostat_2 in SP OVERRIDE MODE (alt loop2 at 0)"
        }

        log.trace "OVERRIDES VALUES (Alt loop2 at 1):  atomicState.T2_AppMgt : $atomicState.T2_AppMgt"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        log.debug "ShouldCool = $ShouldCool"

        if(NoOverride){

            // no setpoint override, no on/off override
            log.debug "evaluating for AlternativeSensor2"
            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){

                // set proper mode

                if(SenTemp < DefaultSetHeat && !ShouldCool){

                    Thermostat_2.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T2 = "heat"              
                    log.debug "$Thermostat_2 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool  && ShouldCool){
                    Thermostat_2.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T2 = "cool"           
                    log.debug "$Thermostat_2 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            }
            //turning off this unit
            else {

                if(atomicState.ThermState == "off"){
                    log.debug "$Thermostat_2 stays off"
                    //Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T2 = "off" // redundant 
                    atomicState.T1_AppMgt = true
                }
                else {    
                    log.debug "turning off $Thermostat_2"
                    atomicState.T2_AppMgt = true
                    Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T2 = "off"
                }
            }
            atomicState.NewHeatSet2 = NewHeatSet
            log.debug "atomicState.NewHeatSet2 = DefaultSetHeat+$AltThermOffSet, that is: $DefaultSetHeat + $AltThermOffSet = $atomicState.NewHeatSet2"
            atomicState.NewCoolSet2 = NewCoolSet
            log.debug "atomicState.NewCoolSet2 = DefaultSetCool-$AltThermOffSet, that is: $DefaultSetCool - $AltThermOffSet = $atomicState.NewCoolSet2"
            //shouldBeT2()
        }
        else { 
            log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing (alt loop2 at 1)" 
        }
    }     
    else 
    { 
        log.debug "some doors are open, AlternativeSensor2 loop not running"
        TurnOffThermostats()
    }
    // runIn(10, resetAppMgt)
}
def AlternativeSensor3(){

    def doorsOk = alldoorsareclosed()
    if(doorsOk){
        log.debug "Running Alternative Sensor Loop for $Thermostat_3"
        def SenTemp = Sensor_3.currentTemperature
        log.debug "Current Temperature at $Sensor_3 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = false 
        def NewCoolSet = false
        def CurrMode = location.currentMode
        atomicState.ThermState = Thermostat_3.currentValue("thermostatMode") as String
        log.trace "atomicState.ThermState for $Thermostat_3 is $atomicState.ThermState"
        def IsOn = atomicState.ThermState in ["heat", "cool"]

        log.debug "IsOn?($IsOn)"

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
                log.debug "$Thermostat_3 heating now set to $NewHeatSet"
            }
            else {
                log.debug "CurHSP already OK"
            }
            if(CurCSP != NewCoolSet){
                atomicState.withinApp = true
                Thermostat_3.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_3 cooling now set to $NewCoolSet"
            }
            else {
                log.debug "CurCSP already OK"
            }
        } 
        else {
            log.debug "$Thermostat_3 in SP OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        log.debug "OVERRIDEN IS $Overriden"

        def ShouldCool = outsideTemp >= OutsideTempHighThres || CurrTemp >= CSPSet
        log.debug "ShouldCool = $ShouldCool"


        if(NoOverride){

            if(SenTemp < DefaultSetHeat || SenTemp > DefaultSetCool){
                // set proper mode
                if(SenTemp < DefaultSetHeat && !ShouldCool){
                    Thermostat_3.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode_T3 = "heat"
                    log.debug "$Thermostat_3 set to Heat"
                    atomicState.T2_AppMgt = true
                }
                else if(SenTemp > DefaultSetCool  && ShouldCool){
                    Thermostat_3.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode_T3 = "cool"
                    log.debug "$Thermostat_3 set to Cool"
                    atomicState.T2_AppMgt = true
                }
            } 
            else {
                //turning off this unit

                if(atomicState.ThermState == "off"){
                    log.debug "$Thermostat_3 stays off"
                    // Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T3 = "off" // redundant
                    atomicState.T1_AppMgt = true
                }
                else {
                    atomicState.T3_AppMgt = true
                    log.debug "turning off $Thermostat_3"      
                    Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode_T3 = "off"            
                }
            }
            atomicState.NewHeatSet3 = NewHeatSet
            atomicState.NewCoolSet3 = NewCoolSet
            //shouldBeT3()
        }
        else { 
            log.debug "$Thermostat_3 in OVERRIDE MODE, doing nothing" 
        }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
        TurnOffThermostats()
    }
    //runIn(10, resetAppMgt)
}
def alldoorsareclosed(){

    log.debug "atomicState.doorsAreOpen value is: $atomicState.doorsAreOpen"
    if(atomicState.doorsAreOpen == true){
        false
    }
    else {
        true
    }
}
def TurnOffThermostats() {

    if(atomicState.ThermOff == false){
        if(atomicState.CRITICAL == false){
            log.debug "Turning off thermostats" 
            Thermostat_1.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_1  turned off"
            // atomicState.T1_AppMgt = true
            if(Thermostat_2){      
                Thermostat_2.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                log.debug "$Thermostat_2 turned off"
                //     atomicState.T2_AppMgt = true
            }
            if(Thermostat_3){
                Thermostat_3.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                log.debug "$Thermostat_3 turned off"
                //     atomicState.T3_AppMgt = true
            }
            if(Thermostat_4){
                if(Thermostat_4.currentValue != atomicState.therm_4_CurrMode){
                    Thermostat_4.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off"
                    log.debug "$Thermostat_4 turned off"
                    //        atomicState.T4_AppMgt = true
                }
            }
        }
        else { 
            log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 
        }
        atomicState.ThermOff = true
    }
    log.debug "Thermostats already turned off, doing nothing"
}

def resetLocationChangeVariable(){
    atomicState.locationModeChange = false
}
def resetOverride(){
    log.debug "OVERRIDE RESET"
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
        log.debug "polling $CtrlSwtch"
    }
    else if(CtrlSwtchRefresh){
        CtrlSwtch?.refresh()
        log.debug "refreshing $CtrlSwtch"
    }
    else { log.debug "$CtrlSwtch does not support either poll() nor refresh() commands"
         }

    if(Thermostat_1){
        def poll = Thermostat_1.hasCommand("poll")
        def refresh = Thermostat_1.hasCommand("refresh")
        if(poll){
            Thermostat_1.poll()
            log.debug "polling Thermostat_1"
        }
        else if(refresh){
            Thermostat_1.refresh()
            log.debug "refreshing Thermostat_1"
        }
        else { log.debug "Thermostat_1 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_2){
        def poll = Thermostat_2.hasCommand("poll")
        def refresh = Thermostat_2.hasCommand("refresh")
        if(poll){
            Thermostat_2.poll()
            log.debug "polling Thermostat_2"
        }
        else if(refresh){
            Thermostat_2.refresh()
            log.debug "refreshing Thermostat_2"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_3){
        def poll = Thermostat_3.hasCommand("poll")
        def refresh = Thermostat_3.hasCommand("refresh")
        if(poll){
            Thermostat_3.poll()
            log.debug "polling Thermostat_3"
        }
        else if(refresh){
            Thermostat_3.refresh()
            log.debug "refreshing Thermostat_3"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_4){
        def poll = Thermostat_4.hasCommand("poll")
        def refresh = Thermostat_4.hasCommand("refresh")
        if(poll){
            Thermostat_4.poll()
            log.debug "polling Thermostat_4"
        }
        else if(refresh){
            Thermostat_4.refresh()
            log.debug "refreshing Thermostat_4"
        }
        else { log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
             }
    }
    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            log.debug "polling OutsideSensor"
        }
        else if(refresh){
            OutsideSensor.refresh()
            log.debug "refreshing OutsideSensor"
        }
        else { log.debug "OutsideSensor does not support either poll() nor refresh() commands"
             }
    }
}
def CheckWindows(){

    def CurrentContacts = contact.latestValue("contact")
    def ClosedContacts = CurrentContacts.findAll { AllcontactsClosed ->
        AllcontactsClosed == "closed" ? true : false
    }
    log.debug "${ClosedContacts.size()} windows/doors out of ${contact.size()} are closed"
    def ContactsOpen = contact.latestValue("contact").contains("open")

    log.debug "Contacts Opened?($ContactsOpen)"
    atomicState.doorsAreOpen = ContactsOpen

    log.debug "Checking windows"
    def Inside = XtraTempSensor.currentValue("temperature")
    def Outside = OutsideSensor.currentValue("temperature")
    def closed = atomicState.closed == true
    // find current setpoints references

    def OkToOpen = OkToOpen() 
    log.debug "OkToOpen?($OkToOpen)"
    log.debug "OffSet?($OffSet)"
    log.debug "atomicState.hasRun($atomicState.hasRun)"

    def message = ""

    if(atomicState.hasRun == 0) {
        state.WindowsAppOpened = false
    }

    log.debug "state.WindowsAppOpened($state.WindowsAppOpened)"
    log.debug "state.WindowsAppClosed($state.WindowsAppClosed)"
    log.debug "atomicState.messageSent($atomicState.messageSent)"
    log.debug "atomicState.hasRun($atomicState.hasRun)"

    if(OkToOpen){

        if(!ContactsOpen && atomicState.hasRun == 0)  {

            Actuators?.on()
            log.debug "opening windows"

            message = "Conditions permitting, I'm opening $Actuators. Operation time is $OperatingTime seconds"
            log.info message 
            send(message)
            state.WindowsAppOpened = true // resets unless hasrun >= 1
            atomicState.hasRun = atomicState.hasRun + 1
            atomicState.messageSent = atomicState.messageSent + 1

            if(OperatingTime){
                log.debug "stop in $OperatingTime seconds"
                runIn(OperatingTime, StopActuators)              
            }
        }
        else { log.debug "Windows have already been opened by this app, doing nothing" }
    }
    else if (!OkToOpen && state.WindowsAppOpened == true) {

        Actuators?.off()
        log.debug "closing windows"
        message = "It's too cold, I'm closing $Actuators."
        log.info message 
        send(message)
        atomicState.closed = true
        atomicState.hasRun = 0 // this is an integer beware of not replacing with bool

        state.WindowsAppOpened = false
        state.WindowsAppClosed = true
        atomicState.messageSent = 0
    }
    else if(atomicState.messageSent == 0 && ContactsOpen && state.WindowsAppOpened == true){ 

        message = "IMPORTANT MESSAGE $Actuators will not close again until you close them yourself!"
        log.info message
        send(message)
        state.messageSent = state.messageSent + 1
    }
    else if(ContactsOpen && state.WindowsAppOpened == false){
    log.debug "WINDOWS MANUALLY OPENED"
    }

}
def OkToOpen(){

    def CSPSet = atomicState.CSPSet
    def HSPSet = atomicState.HSPSet 
     
    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature") 
    def CurrTemp = Inside
    def Outside = OutsideSensor.currentValue("temperature") 
    def outsideTemp = OutsideSensor.currentTemperature
    def WithinCriticalOffSet = Inside >= CriticalTemp + OffSet
    def closed = atomicState.closed
    //def OutsideTempHighThres = OutsideTempHighThres

    def ShouldCool = outsideTemp >= OutsideTempHighThres && CurrTemp >= CSPSet
    def ShouldHeat = outsideTemp <= OutsideTempLowThres || CurrTemp <= CSHSet
    if(ShouldCool && ShouldHeat) {
        ShouldCool = false
    }
    log.debug "ShouldCool = $ShouldCool"
    log.debug "ShouldHeat = $ShouldHeat"

    if(ExceptACMode1 && CurrMode in ExceptACMode1){
        def NewOutsideTempHighThres = OutsideTempHighThres - ExceptHighThreshold1
        log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold1" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    else if(ExceptACMode2 && CurrMode in ExceptACMode2){
        OutsideTempHighThres = OutsideTempHighThres - ExceptHighThreshold2
        log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold2" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }

    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres


    log.info "Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
    log.debug "closed?($closed)"
    log.debug "OutSideWithinMargin?($OutSideWithinMargin)"
    log.debug "Inside is WithinCriticalOffSet?($WithinCriticalOffSet)"    

    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool

    return result

}
def StopActuators(){
    Actuators?.stop()
}
private schedules() { 

    def scheduledTimeA = 1
    def scheduledTimeB = 5

    /*schedule("0 0/$scheduledTimeA * * * ?", TemperaturesModes)
log.debug "TemperaturesModes scheduled to run every $scheduledTimeA minutes"*/

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        //log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }


}

private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    log.debug msg
}

// trash

private trash(){
    def onSwitches = CtrlSwtch.findAll { switchVal ->
        switchVal == "on" ? true : false
    }
    log.debug "${onSwitches.size()} out of ${CtrlSwtch.size()} switches are on"
    def SwitchesOn = onSwitches.size() != 0

}

