definition(
    name: " Multi Thermostat Manager 7.0",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description:  "Manage up to 4 thermostats, in parallel with these options: contact sensors, third party temperature sensor, modes, off override and temperature set point override",
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
            href "settings", title: "Choose your thermostats", description: ""
            href "Modes", title: "Set different temperatures depending on location mode", description: ""
        }
    }
}
def settings() {

    def pageName = "settings"

    def pageProperties = [
        name:       "settings",
        title:      "Main Settings",
        nextPage:   "pageSetup",
        install: true,
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
                input(name: "OutsideTempLowThres", type: "number", title: "Outside temperature above which I open windows", required: true)
                input(name: "OutsideTempHighThres", type: "number", title: "Outside temperature above which I keep windows closed", required: true)
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
        title:      "Select Modes and Temperatures",
        nextPage:   "pageSetup",
        install: true,
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
def installed() {	 
    log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler
    init()
}
def updated() {

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

    if(AltSensor_1 || AltSensor_2 || AltSensor_3){
        subscribe(OutsideSensor, "temperature", temperatureHandler)
    }
    atomicState.doorsAreOpen = false

    // these values will be reset to false later, for now they need to remain true so app doesn't go into override
    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true
   
    /*  */

    atomicState.override = false
    atomicState.ThisIsManual = false
    atomicState.closed = true // windows management
    atomicState.hasRun = 0 // windows management

    atomicState.locationModeChange = true 
    runIn(30, resetLocationChangeVariable)

    schedules()

}
def appHandler(evt){
    log.debug "app event ${evt.name}:${evt.value} received"
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
        log.info "device is ------------------- $KeyValueForThisTherm.value"
        def ThermNumber = KeyValueForThisTherm.value
        ThermNumber = KeyValueForThisTherm.value.toInteger()

        log.info "ThermNumber is ------------------- $ThermNumber"

        //array heat
        def HSPH = ["0","$HSPH1", "$HSPH2", "$HSPH3", "$HSPH4"]
        def HSPN = ["0","$HSPN1", "$HSPN2", "$HSPN3", "$HSPN4"]
        def HSPA_ = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
        def HSPCust1 = ["0","$HSPCust1_T1", "$HSPCust1_T2", "$HSPCust1_T3", "$HSPCust1_T4"]
        def HSPCust2 = ["0","$HSPCust2_T1", "$HSPCust2_T2", "$HSPCust2_T3", "$HSPCust2_T4"]

        // declare an integer value for current mode
        def MapofIndexValues = [0: "0", "$Home": "1", "$Night": "2", "$Away": "3", "$CustomMode1": "4", "$CustomMode2": "5" ]   
        def ModeIndexValue = MapofIndexValues.find{ it.key == "$CurrMode"}
        log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"
        ModeIndexValue = ModeIndexValue.value

        ModeIndexValue = ModeIndexValue.toInteger()
        log.info "-----------------------------------------------------------------------------------------ModeIndexValue = $ModeIndexValue"

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
            reference = Math.round(Double.parseDouble(RefHeat))
            reference = reference.toInteger()
            log.debug "RefHeat was: $RefHeat"
            log.debug "RefHeat is now converted to a reference as: $reference"

        }
        else  if(evt.name == "coolingSetpoint"){ 
            reference = Math.round(Double.parseDouble(RefCool))
            reference = reference.toInteger()
            log.debug "RefCool was: $RefCool"
            log.debug "RefCool is now converted to a reference as: $reference"
        }

        /// 
        def Value = Math.round(Double.parseDouble(evt.value))
        Value = Value.toInteger()
        log.debug "Evt value to Integer is : $Value"
        log.debug "reference Value is to be compared to is: $reference"

        if(Value == reference){  
            log.debug "NO SETPOINT OVERRIDE"
            atomicState.ThisIsManual = false
            atomicState.override = false
            // we don't change atomicState.override value here, it'll be done in the CheckCmdOrigin() loop     
            // we don't change _AppMgtSetPoint either, it'll change only on mode change or update
        }
        else {       
            atomicState.ThisIsManual = true
            atomicState.override = true
            log.debug "new Set Point for $evt.device is MANUAL ---------- OVERRIDE MODE ACTIVATED"
        }

        //     
        if(evt.displayName == "${Thermostat_1}")
        {
            if(atomicState.ThisIsManual == true){
                atomicState.T1_AppMgt = false
                log.info "atomicState.T1_AppMgt set to $atomicState.T1_AppMgt"
            }
            else {
                atomicState.T1_AppMgt = true
                log.info "atomicState.T1_AppMgt set to $atomicState.T1_AppMgt"
            }

        }
        else if(evt.displayName == "${Thermostat_2}")
        {
            if(atomicState.ThisIsManual == true){
                atomicState.T2_AppMgt = false
                log.info "atomicState.T2_AppMgt set to $atomicState.T2_AppMgt"
            }
            else {
                atomicState.T2_AppMgt = true
                log.info "atomicState.T2_AppMgt set to $atomicState.T2_AppMgt"
            }
        }
        else if(evt.displayName == "${Thermostat_3}")
        {
            if(atomicState.ThisIsManual == true){
                atomicState.T3_AppMgt = false
                log.info "atomicState.T3_AppMgt set to $atomicState.T3_AppMgt"
            }
            else {
                atomicState.T3_AppMgt = true
                log.info "atomicState.T3_AppMgt set to $atomicState.T3_AppMgt"
            }
        }
        else if(evt.displayName == "${Thermostat_4}")
        {
            if(atomicState.ThisIsManual == true){
                atomicState.T4_AppMgt = false
                log.info "atomicState.T4_AppMgt set to $atomicState.T4_AppMgt"
            }
            else {
                atomicState.T4_AppMgt = true
                log.info "atomicState.T4_AppMgt set to $atomicState.T4_AppMgt"
            }

        }   

        log.debug "RefHeat is: $RefHeat"
        log.debug "RefCool is: $RefCool"
        log.debug "reference is: $reference"
        log.debug "SetPoint Change was Manual? ($atomicState.ThisIsManual) if false then should have $reference = $Value"
        atomicState.RefHeat = RefHeat
        atomicState.RefCool = RefCool

    }
    else{
        log.debug "Not evaluating SETPOINT OVERRIDE because the command came from within the app itself"
    }
}
def ThermostatSwitchHandler(evt){

    log.debug "$evt.device set to $evt.value "

    def device = state.EvtDevice 
    def event = state.EvtValue
    device = device as String

    log.debug "CHECKING COMMAND ORIGIN for $evt.device "

    if(atomicState.CRITICAL == false){

       
        def CurrMode = location.currentMode
        def LocatioModeChange = atomicState.locationModeChange
        log.debug "Location Mode Changed?($LocatioModeChange)"

        // these values are reset to default FALSE every 30 seconds, so if any is true it means the command came from the within the app and, therefore, was not manual. 
        def ChangedByAppMAP = [atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
        //, atomicState.T1_AppMgt, atomicState.T2_AppMgt, atomicState.T3_AppMgt, atomicState.T4_AppMgt]
        def ChangedByAppFind = ChangedByAppMAP.findAll { val ->
            val == true ? true : false
        }
        log.debug "ChangedByApp : : : ${ChangedByAppFind.size()} out of ${ChangedByAppMAP.size()} values are true"

        def ChangedByApp = ChangedByAppFind.size() == ChangedByAppMAP.size()
        log.info "Changed By App? ($ChangedByApp)"

        log.trace "Latest Thermostat ModeS : $atomicState.LatestThermostatMode_T1 | $atomicState.LatestThermostatMode_T2 | $atomicState.LatestThermostatMode_T3 | $atomicState.LatestThermostatMode_T4"
        def MapofShouldBe = ["$Thermostat_1": atomicState.LatestThermostatMode_T1, "$Thermostat_2": atomicState.LatestThermostatMode_T2, "$Thermostat_3": atomicState.LatestThermostatMode_T3, "$Thermostat_4": atomicState.LatestThermostatMode_T4]
        def WhichThermInvolved = MapofShouldBe.find{ it.key == "$evt.device"} 
        def Shouldbe = WhichThermInvolved.value
        log.debug "ShouldBe Values for device[WhichThermInvolved], that is ${WhichThermInvolved}"

        //retrieve object name
        def Thermostats = [Thermostat_1, Thermostat_2, Thermostat_3, Thermostat_4]
        def deviceName = Thermostats.find{"$evt.device"} 

        // now compare 

        //def CurTMd =  deviceName.currentValue("thermostatMode").toString()
        def ShouldBe = WhichThermInvolved.value.toString()

        log.debug "Current Mode Value of $evt.device = $CurTMd"
        log.debug "  $evt.value =? $ShouldBe"
        def IdenticalShouldbe = evt.value == ShouldBe
        log.debug "IDENTICAL?($IdenticalShouldbe)"

        //make sure that  : 
        // 1) the thermostat DID effectively had its operating mode changed
        // 2) this change was not triggered by a home location change 
        // 3) or by the TemperaturesModes() loop
        //
        def ThereWasChange = ShouldBe != evt.value && !LocatioModeChange && !ChangedByApp

        log.debug " Change($ThereWasChange) "

        def override = false

        if(ThereWasChange){
            // this is only for alternativeloop() eval 

            state.ThermostatOverriden = evt.device
            log.info "Latest mode for state.ThermostatOverriden($state.ThermostatOverriden) was $Shouldbe and it just switched to $evt.value --------------------"
        }
        else {
            state.ThermostatOverriden = "none"
            // atomicState.override = false
        }

	// for temperaturesModes loop

        if(evt.displayName == "${Thermostat_1}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_1"
                atomicState.T1_AppMgt = true
                override = false

            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_1"
                atomicState.T3_AppMgt = false
                override = true
            }       
        }
        else if(evt.displayName == "${Thermostat_2}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_2"
                atomicState.T2_AppMgt = true
                override = false

            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_2"
                atomicState.T3_AppMgt = false
                override = true
            }     
        } 
        else if(evt.displayName == "${Thermostat_3}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "NO MANUAL OVERRIDE for $Thermostat_3"
                atomicState.T3_AppMgt = true
                override = false

            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_3"
                atomicState.T3_AppMgt = false
                override = true
            }     
        } 
        else if(evt.displayName == "${Thermostat_4}"){
            if(IdenticalShouldbe){
                // manual override deactivated
                log.debug "END of MANUAL OVERRIDE for $Thermostat_4"
                atomicState.T4_AppMgt = true
                override = false

            }
            else if(!IdenticalShouldbe){
                // command did not come from app so manual override is on
                log.debug "MANUAL OVERRIDE for $Thermostat_4"
                atomicState.T3_AppMgt = false
                override = true
            }     
        }
        atomicState.override = override
        log.info "atomicState.override ====== $atomicState.override "

    }
    else { log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" }

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
        state.WindowsAppManaged = true

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
    atomicState.modeStartTime = now() 

    // these values will be reset to false later, for now they need to remain true so app doesn't go into override
    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true
    /*  */

    log.debug "mode changed to ${evt.value}"

    atomicState.override = false
    atomicState.ThisIsManual = false

    state.ThermostatOverriden = "none"
    atomicState.locationModeChange = true
    runIn(60, resetLocationChangeVariable)

    runIn(30, resetOverride)
}
// main loop
def TemperaturesModes(){

    log.trace "MAIN LOOP RUNNING"
    //updated()/ whatch OUT! infinite loop because TemperaturesModes is run from schedules()!!!!!!!!!!!!!!!!
    //pause(10000)
    pollThermostats()

    log.trace "atomicState.T1_AppMgt = $atomicState.T1_AppMgt, atomicState.T2_AppMgt = $atomicState.T2_AppMgt, atomicState.T3_AppMgt = $atomicState.T3_AppMgt, atomicState.T4_AppMgt = $atomicState.T4_AppMgt"

    def doorsOk = alldoorsareclosed()

    if(doorsOk){

        log.trace """atomicState.T1_AppMgt : $atomicState.T1_AppMgt, atomicState.T2_AppMgt : $atomicState.T2_AppMgt, 
atomicState.T3_AppMgt : $atomicState.T3_AppMgt, atomicState.T4_AppMgt : $atomicState.T4_AppMgt"""
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
        

        log.trace "$Thermostat_1 : $ThermState1, $Thermostat_2 : $ThermState2, $Thermostat_3 : $ThermState3, $Thermostat_4 : $ThermState4"

        log.trace "CURRENT TEMPS : CurrTemp1 : $CurrTemp1, CurrTemp2 : $CurrTemp2, CurrTemp3 : $CurrTemp3, CurrTemp4 : $CurrTemp4, OUTSIDE: $outsideTemp"

        if(CurrMode in Home){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.T1_AppMgt == false){
                    log.debug "${Thermostat_1} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_1){
                        log.debug "loading $Home settings for $Thermostat_1"     
                        atomicState.withinApp = true
                        Thermostat_1.setHeatingSetpoint(HSPH1)
                        Thermostat_1.setCoolingSetpoint(CSPH1)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp1 > HSPH1 && ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T1 = "off"
                        } 
                        else if(outsideTemp >= CSPH1 && ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T1 = "cool"
                        }
                        else if(CurrTemp1 < HSPH1 && ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to Heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T1 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT1()
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.T2_AppMgt == false){
                    log.debug "${Thermostat_2} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_2){
                        log.debug "loading $Home settings for $Thermostat_2"    
                        atomicState.withinApp = true
                        Thermostat_2.setHeatingSetpoint(HSPH2)
                        Thermostat_2.setCoolingSetpoint(CSPH2)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp2 > HSPH2 && ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T2 = "off"
                        } 
                        else if(outsideTemp >= CSPH2 && ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T2 = "cool"
                        }
                        else if(CurrTemp2 < HSPH2 && ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T2 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT2()
                    }

                }
            }
            if(Thermostat_3){
                log.debug "atomicState.T3_AppMgt = $atomicState.T3_AppMgt"
                if(atomicState.T3_AppMgt == false){
                    log.debug "${Thermostat_3} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_3){
                        log.debug "loading $Home settings for $Thermostat_3"
                        atomicState.withinApp = true
                        Thermostat_3.setHeatingSetpoint(HSPH3)
                        Thermostat_3.setCoolingSetpoint(CSPH3)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp3 > HSPH3 && ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T3 = "off"
                        } 
                        else if(outsideTemp >= CSPH3 && ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T3 = "cool"
                        }
                        else if(CurrTemp3 < HSPH3 && ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T3 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT3()
                    }

                }
            }
            if(Thermostat_4){
                if(atomicState.T4_AppMgt == false){
                    log.debug "${Thermostat_4} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    log.debug "loading $Home settings for $Thermostat_4"
                    atomicState.withinApp = true
                    Thermostat_4.setHeatingSetpoint(HSPH4)
                    Thermostat_4.setCoolingSetpoint(CSPH4)   

                    // no AltSensor 4 
                    if(CurrTemp4 > HSPH4 && ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode_T4 = "off"
                    } 
                    else if(outsideTemp >= CSPH4 && ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode_T4 = "cool"
                    }
                    else if(CurrTemp4 < HSPH4 && ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T4 = "heat"

                    }
                    //recording set values for further reference in override
                    //shouldBeT4()
                }

            }
        }
        else if(CurrMode in Night){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.T1_AppMgt == false){
                    log.debug "${Thermostat_1} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_1){
                        log.debug "loading $Night settings for $Thermostat_1"
                        atomicState.withinApp = true
                        Thermostat_1.setHeatingSetpoint(HSPN1)
                        Thermostat_1.setCoolingSetpoint(CSPN1)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp1 > HSPN1 && ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T1 = "off"
                        } 
                        else if(outsideTemp >= CSPN1 && ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T1 = "cool"
                        }
                        else if(CurrTemp1 < HSPN1 && ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T1 = "heat"
                        }
                        //recording set values for further reference in override
                        //  shouldBeT1()
                    }

                }
            }
            if(Thermostat_2){
                if(atomicState.T2_AppMgt == false){
                    log.debug "${Thermostat_2} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_2){
                        log.debug "loading $Night settings for $Thermostat_2"
                        atomicState.withinApp = true
                        Thermostat_2.setHeatingSetpoint(HSPN2)
                        Thermostat_2.setCoolingSetpoint(CSPN2) 

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp2 > HSPN2 && ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T2 = "off"
                        } 
                        else if(outsideTemp >= CSPN2 && ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T2 = "cool"
                        }
                        else if(CurrTemp2 < HSPN2 && ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T2 = "heat"
                        }

                        //recording set values for further reference in override
                        // shouldBeT2()
                    }
                }
            }
            if(Thermostat_3){
                log.info "atomicState.ThisIsManual is $atomicState.ThisIsManual"
                if(atomicState.T3_AppMgt == false){
                    log.debug "${Thermostat_3} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_3){
                        log.debug "loading $Night settings for $Thermostat_3"
                        atomicState.withinApp = true
                        Thermostat_3.setHeatingSetpoint(HSPN3)
                        Thermostat_3.setCoolingSetpoint(CSPN3)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp3 > HSPN3 && ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T3 = "off"
                        } 
                        else if(outsideTemp >= CSPN3 && ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T3 = "cool"
                        }
                        else if(CurrTemp3 < HSPN3 && ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T3 = "heat"
                        }
                        //recording set values for further reference in override
                        //  shouldBeT3()
                    }

                }
            }
            if(Thermostat_4){
                if(atomicState.T4_AppMgt == false){
                    log.debug "${Thermostat_4} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    log.debug "loading $Night0 settings for $Thermostat_4"
                    atomicState.withinApp = true
                    Thermostat_4.setHeatingSetpoint(HSPN4)
                    Thermostat_4.setCoolingSetpoint(CSPN4)    

                    // no AltSensor 4 
                    if(CurrTemp4 > HSPN4 && ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode_T4 = "off"
                    } 
                    else if(outsideTemp >= CSPN4 && ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode_T4 = "cool"
                    }
                    else if(CurrTemp4 < HSPN4 && ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T4 = "heat"
                    }

                    //recording set values for further reference in override
                    //shouldBeT4()
                }
            }
        }
        else if(CurrMode in Away){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.T1_AppMgt == false){
                    log.debug "${Thermostat_1} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_1){
                        log.debug "loading $Away settings for $Thermostat_1"
                        atomicState.withinApp = true
                        Thermostat_1.setHeatingSetpoint(HSPA)
                        Thermostat_1.setCoolingSetpoint(CSPA)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp1 > HSPA1 && ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T1 = "off"
                        } 
                        else if(outsideTemp >= CSPA1 && ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T1 = "cool"
                        }
                        else if(CurrTemp1 < HSPA1 && ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T1 = "heat"
                        }

                        //recording set values for further reference in override
                        //shouldBeT1()
                    }
                }
            }
            if(Thermostat_2){

                if(atomicState.T2_AppMgt == false){
                    log.debug "${Thermostat_2} in OVERRIDE MODE, doing nothing"
                }
                else {

                    
                    if(!AltSensor_2){
                        log.debug "loading $Away settings for $Thermostat_2"
                        atomicState.withinApp = true
                        Thermostat_2.setHeatingSetpoint(HSPA)
                        Thermostat_2.setCoolingSetpoint(CSPA)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp2 > HSPA2 && ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T2 = "off"
                        } 
                        else if(outsideTemp >= CSPA2 && ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T2 = "cool"
                        }
                        else if(CurrTemp2 < HSPA2 && ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T2 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT2()
                    }
                }
            }
            if(Thermostat_3){

                if(atomicState.T3_AppMgt == false){
                    log.debug "${Thermostat_3} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_3){
                        log.debug "loading $Away settings for $Thermostat_3"
                        atomicState.withinApp = true
                        Thermostat_3.setHeatingSetpoint(HSPA)
                        Thermostat_3.setCoolingSetpoint(CSPA)   

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp3 > HSPA3 && ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T3 = "off"
                        } 
                        else if(outsideTemp >= CSPA3 && ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T3 = "cool"
                        }
                        else if(CurrTemp3 < HSPA3 && ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T3 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT3()
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.T4_AppMgt == false){
                    log.debug "${Thermostat_4} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    log.debug "loading $Away settings for $Thermostat_4"
                    atomicState.withinApp = true
                    Thermostat_4.setHeatingSetpoint(HSPA)
                    Thermostat_4.setCoolingSetpoint(CSPA)   

                    // no AltSensor 4 
                    if(CurrTemp4 > HSPA4 && ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode_T4 = "off"
                    } 
                    else if(outsideTemp >= CSPA4 && ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode_T4 = "cool"
                    }
                    else if(CurrTemp4 < HSPA4 && ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T4 = "heat"
                    }
                    //recording set values for further reference in override
                    //shouldBeT4()
                }
            }
        }
        else if(CurrMode in CustomMode1){
            log.debug "CustomMode1"
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.T1_AppMgt == false){
                    log.debug "${Thermostat_1} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode1 settings for $Thermostat_1"
                        atomicState.withinApp = true
                        Thermostat_1.setHeatingSetpoint(HSPCust1_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust1_T1) 


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp1 > HSPCust1_T1 && ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T1 = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T1 && ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T1 = "cool"
                        }
                        else if(CurrTemp1 < HSPCust1_T1 && ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T1 = "heat"
                        }
                        //recording set values for further reference in override
                        //  shouldBeT1()
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.T2_AppMgt == false){
                    log.debug "${Thermostat_2} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode1 settings for $Thermostat_2"
                        atomicState.withinApp = true
                        Thermostat_2.setHeatingSetpoint(HSPCust1_T2)
                        Thermostat_2.setCoolingSetpoint(CSPCust1_T2)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp2 > HSPCust1_T2 && ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T2 = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T2 && ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T2 = "cool"
                        }
                        else if(CurrTemp2 < HSPCust1_T2 && ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T2 = "heat"
                        }
                        //recording set values for further reference in override
                        //shouldBeT2()
                    }      
                }
            }
            if(Thermostat_3){
                log.info "atomicState.T3_AppMgt = $atomicState.T3_AppMgt"                
                if(atomicState.T3_AppMgt == false){
                    log.debug "${Thermostat_3} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode1 settings for $Thermostat_3"
                        atomicState.withinApp = true
                        Thermostat_3.setHeatingSetpoint(HSPCust1_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust1_T3)   


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp3 > HSPCust1_T3 && ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T3 = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T3 && ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T3 = "cool"
                        }
                        else if(CurrTemp3 < HSPCust1_T3 && ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T3 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT3()
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.T4_AppMgt == false){
                    log.debug "${Thermostat_4} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    log.debug "loading $CustomMode1 settings for $Thermostat_4"
                    atomicState.withinApp = true
                    Thermostat_4.setHeatingSetpoint(HSPCust1_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust1_T4)     

                    // no AltSensor 4 
                    if(CurrTemp4 > HSPCust1_T4 && ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode_T4 = "off"
                    } 
                    else if(outsideTemp >= CSPCust1_T4 && ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode_T4 = "cool"
                    }
                    else if(CurrTemp4 < HSPCust1_T4 && ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T4 = "heat"
                    }   
                    //recording set values for further reference in override
                    //shouldBeT4()
                }
            }
        }
        else if(CustomMode2 && CurrMode in CustomMode2){
            log.debug "CustomMode2"
            if(Thermostat_1){
                if(atomicState.T1_AppMgt == false){
                    log.debug "${Thermostat_1} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode2 settings for $Thermostat_1"
                        atomicState.withinApp = true
                        Thermostat_1.setHeatingSetpoint(HSPCust2_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust2_T1)    


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp1 > HSPCust2_T1 && ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T1 = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T1 && ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_1 to cool"
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T1 = "cool"
                        }
                        else if(CurrTemp1 < HSPCust2_T1 && ThermState1 != "heat"){
                            log.debug "Setting $Thermostat_1 to heat"
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T1 = "heat"
                        }
                        //recording set values for further reference in override
                        // shouldBeT1()
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.T2_AppMgt == false){
                    log.debug "${Thermostat_2} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode2 settings for $Thermostat_2"
                        atomicState.withinApp = true
                        Thermostat_2.setHeatingSetpoint(HSPCust2_T2)
                        Thermostat_2.setCoolingSetpoint(CSPCust2_T2)


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp2 > HSPCust2_T2 && ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T2 = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T2 && ThermState2 != "cool"){
                            log.debug "Setting $Thermostat_2 to cool"
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T2 = "cool"
                        }
                        else if(CurrTemp2 < HSPCust2_T2 && ThermState2 != "heat"){
                            log.debug "Setting $Thermostat_2 to heat"
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T2 = "heat"
                        }
                        //recording set values for further reference in override
                        //shouldBeT2()
                    }      
                }
            }
            if(Thermostat_3){
                log.info "atomicState.T3_AppMgt = $atomicState.T3_AppMgt" 
                if(atomicState.T3_AppMgt == false){
                    log.debug "${Thermostat_3} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode2 settings for $Thermostat_3"
                        atomicState.withinApp = true
                        Thermostat_3.setHeatingSetpoint(HSPCust2_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust2_T3) 

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(CurrTemp3 > HSPCust2_T3 && ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode_T3 = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T3 && ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_3 to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode_T3 = "cool"
                        }
                        else if(CurrTemp3 < HSPCust2_T3 && ThermState3 != "heat"){
                            log.debug "Setting $Thermostat_3 to heat"
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode_T3 = "heat"
                        }
                        //recording set values for further reference in override
                        //shouldBeT3()
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.T4_AppMgt == false){
                    log.debug "${Thermostat_4} in OVERRIDE MODE, doing nothing"
                }
                else {
                    
                    log.debug "loading $CustomMode2 settings for $Thermostat_4"
                    atomicState.withinApp = true
                    Thermostat_4.setHeatingSetpoint(HSPCust2_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust2_T4)  

                    // no AltSensor 4 
                    if(CurrTemp4 > HSPCust2_T4 && ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode_T4 = "off"
                    } 
                    else if(outsideTemp >= CSPCust2_T4 && ThermState4 != "cool"){
                        log.debug "Setting $Thermostat_3 to cool"
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode_T4 = "cool"
                    }
                    else if(CurrTemp4 < HSPCust2_T4 && ThermState4 != "heat"){
                        log.debug "Setting $ThermState4 to heat"
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T4 = "heat"
                    }   
                    //recording set values for further reference in override
                    //shouldBeT4()
                }
            }
        }
    }
    else {
        log.debug "Some windows or doors are open, doing nothing"
        TurnOffThermostats()
    }
    //log.info "reseting override's triggers for future reference"
    //runIn(30, resetAppMgt)

		atomicState.withinApp = false // this value is reset to false so if there's a manual setpoint override it'll be detected as such

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

        if(atomicState.T1_AppMgt == true){
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
            log.debug "$Thermostat_1 in SETPOINT OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        log.debug "OVERRIDEN IS $Overriden"

        if(atomicState.T1_AppMgt == true){
            if(aOverriden != "$Thermostat_1"){
                // no setpoint override, no on/off override
                if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){

                    // set proper mode
                    if(SenTemp < DefaultSetHeat){

                        Thermostat_1.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T1 = "heat"
                        log.debug "$Thermostat_1 set to Heat"
                        atomicState.T2_AppMgt = true
                    }
                    else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

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
            else { log.debug "$Thermostat_1 in OVERRIDE MODE " }
        }
        else {log.debug "$Thermostat_1 in SETPOINT OVERRIDE MODE, doing nothing" }
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
        log.debug "atomicState.override is $atomicState.override"

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

        if(atomicState.T2_AppMgt == true){
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
            log.debug "$Thermostat_2 in SETPOINT OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        log.debug "OVERRIDEN IS $Overriden"

        if(atomicState.T2_AppMgt == true){
            if(Overriden != "$Thermostat_2"){
                // no setpoint override, no on/off override
                log.debug "evaluating for AlternativeSensor2"
                if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){

                    // set proper mode

                    if(SenTemp < DefaultSetHeat){

                        Thermostat_2.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T2 = "heat"              
                        log.debug "$Thermostat_2 set to Heat"
                        atomicState.T2_AppMgt = true
                    }

                    else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

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
            else { log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing" }
        }
        else {log.debug "$Thermostat_2 in SETPOINT OVERRIDE MODE, doing nothing" }
    }
    else { 
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

        if(atomicState.T3_AppMgt == true){
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
            log.debug "$Thermostat_3 in SETPOINT OVERRIDE MODE"
        }

        def Overriden = state.ThermostatOverriden as String
        log.debug "OVERRIDEN IS $Overriden"

        if(atomicState.T3_AppMgt == true){
            if(Overriden != "$Thermostat_3"){

                //(IsOn && atomicState.T3_AppMgt == false) if on but only due to previous app's command, then run
                //(!IsOn && atomicState.override == false) if off but not due to override, then run
                if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){


                    // set proper mode
                    if(SenTemp < DefaultSetHeat){

                        Thermostat_3.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode_T3 = "heat"
                        log.debug "$Thermostat_3 set to Heat"
                        atomicState.T2_AppMgt = true
                    }
                    else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

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
            else { log.debug "$Thermostat_3 in OVERRIDE MODE, doing nothing" }
        }
        else {log.debug "$Thermostat_3 in SETPOINT OVERRIDE MODE, doing nothing" }
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

    if(atomicState.CRITICAL == false){
        log.debug "Turning off thermostats" 
        Thermostat_1.setThermostatMode("off") 
        atomicState.LatestThermostatMode = "off"
        log.debug "$Thermostat_1  turned off"
        atomicState.T1_AppMgt = true
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
            if(Thermostat_4.currentValue != atomicState.therm_4_CurrMode){
                Thermostat_4.setThermostatMode("off") 
                atomicState.LatestThermostatMode = "off"
                log.debug "$Thermostat_4 turned off"
                atomicState.T4_AppMgt = true
            }
        }
    }
    else { log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" }
}
def CheckCmdOrigin(){

}
def resetLocationChangeVariable(){
    atomicState.locationModeChange = false
}
def resetOverride(){
    atomicState.override = false
    log.debug "OVERRIDE RESET to FALSE"
}
def resetAppMgt(){
    // presets before deciding if override = true or not
    /*atomicState.T1_AppMgt = false
atomicState.T2_AppMgt = false
atomicState.T3_AppMgt = false
atomicState.T4_AppMgt = false


    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true
    
    */

    state.ThermostatOverriden = "none"
    if( atomicState.hasRun < 1)
    { 
        state.WindowsAppManaged = false 
        log.debug "state.WindowsAppManaged reset to FALSE"
    }

    // the idea is that if any of these values is true or 1 then the change has been made by the app. Otherwise, it was a manual override

    log.trace """VALUES RESET : atomicState.T1_AppMgt = $atomicState.T1_AppMgt || atomicState.T2_AppMgt = $atomicState.T2_AppMgt || 
atomicState.T3_AppMgt = $atomicState.T3_AppMgt || atomicState.T4_AppMgt = $atomicState.T4_AppMgt, state.WindowsAppManaged = $state.WindowsAppManaged"""


}
def pollThermostats(){
    if(Thermostat_1){
        if(Thermostat_1.hasCommand("poll")){
            Thermostat_1.poll()
            log.debug "polling Thermostat_1"
        }
        else if(Thermostat_1.hasCommand("refresh")){
            Thermostat_1.refresh()
            log.debug "refreshing Thermostat_1"
        }
        else { log.debug "Thermostat_1 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_2){
        if(Thermostat_2.hasCommand("poll")){
            Thermostat_2.poll()
            log.debug "polling Thermostat_2"
        }
        else if(Thermostat_2.hasCommand("refresh")){
            Thermostat_2.refresh()
            log.debug "refreshing Thermostat_2"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_3){
        if(Thermostat_3.hasCommand("poll")){
            Thermostat_3.poll()
            log.debug "polling Thermostat_3"
        }
        else if(Thermostat_3.hasCommand("refresh")){
            Thermostat_3.refresh()
            log.debug "refreshing Thermostat_3"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_4){
        if(Thermostat_4.hasCommand("poll")){
            Thermostat_4.poll()
            log.debug "polling Thermostat_4"
        }
        else if(Thermostat_3.hasCommand("refresh")){
            Thermostat_4.refresh()
            log.debug "refreshing Thermostat_4"
        }
        else { log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
             }
    }
    if(OutsideSensor){
        if(OutsideSensor.hasCommand("poll")){
            OutsideSensor.poll()
            log.debug "polling OutsideSensor"
        }
        else if(OutsideSensor.hasCommand("refresh")){
            OutsideSensor.refresh()
            log.debug "refreshing OutsideSensor"
        }
        else { log.debug "OutsideSensor does not support either poll() nor refresh() commands"
             }
    }
}
def CheckWindows(){
    log.debug "Checking windows"
    def Inside = XtraTempSensor.currentValue("temperature")
    def Outside = OutsideSensor.currentValue("temperature")
    def closed = atomicState.closed == true
    // find current setpoints references

    def OkToOpen = OkToOpen() 
    log.debug "OkToOpen?($OkToOpen)"
    def OffSet = OffSet.toInteger() 
    log.debug "OffSet?($OffSet)"
    log.debug "atomicState.hasRun($atomicState.hasRun)"
    log.debug "atomicState.WindowsAppManaged($atomicState.WindowsAppManaged)"

    // atomicState.hasRun = 0
    def message = ""

    log.debug "Inside temperature is above Heat Setting. "
    if(OkToOpen){

        if(atomicState.hasRun < 1)  {

            Actuators?.on()
            log.debug "opening windows"

            message = "Conditions permitting, I'm opening $Actuators. Operation time is $OperatingTime seconds"
            log.info message 
            send(message)
			state.WindowsAppManaged = true // resets periodically unless hasrun >= 1
            state.hasRun = state.hasRun + 1
             state.messageSent = true
            
            if(OperatingTime){
                log.debug "stop in $OperatingTime seconds"
                runIn(OperatingTime, StopActuators)              
            }
        }
        else { log.debug "Windows open already run, doing nothing" }
    }
    else if (!OkToOpen && state.WindowsAppManaged == true) {

        log.debug "state.WindowsAppManaged RESET to $state.WindowsAppManaged"
        Actuators?.off()
        log.debug "closing windows"
        message = "It's too cold, I'm closing $Actuators. Operation time is $OperatingTime seconds"
        log.info message 
        send(message)
        atomicState.closed = true
        atomicState.hasRun = 0 // this is an integer beware of not replacing with bool
        state.WindowsAppManaged = false
    }
    else if(state.WindowsAppManaged != true && state.messageSent == false){ 

        message = "IMPORTANT MESSAGE $Actuators will not close again until you close them yourself!"
        log.info message
        send(message)
        state.messageSent = true
    }

}
def OkToOpen(){
    def Inside = XtraTempSensor.currentValue("temperature") 
    def Outside = OutsideSensor.currentValue("temperature") 
    def WithinOffSet = Inside >= CriticalTemp + OffSet

    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres

    log.info "Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
    log.debug "closed?($closed)"
    log.debug "OutSideWithinMargin?($OutSideWithinMargin)"
    log.debug "WithinOffSet?($WithinOffSet)"
    log.debug "atomicState.closed?($atomicState.closed)"
    log.debug "state.WindowsAppManaged?($state.WindowsAppManaged)"

    def result = OutSideWithinMargin && WithinOffSet

    return result

}
def StopActuators(){
    Actuators?.stop()
}
private schedules() { 

    def scheduledTimeA = 1
    def scheduledTimeB = 1

    if(AltSensor_1){
        subscribe(Sensor_1, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_1"
        schedule("0 0/$scheduledTimeB * * * ?", AlternativeSensor1)
        //log.debug "AlternativeSensor1 scheduled to run every $scheduledTimeB minutes"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        subscribe(Sensor_2, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_2"
        schedule("0 0/$scheduledTimeB * * * ?", AlternativeSensor2)
        //log.debug "AlternativeSensor2 scheduled to run every $scheduledTimeB minutes"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        subscribe(Sensor_3, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_3"
        schedule("0 0/$scheduledTimeB * * * ?", AlternativeSensor3)
        //log.debug "AlternativeSensor3 scheduled to run every $scheduledTimeB minutes"
        AlternativeSensor3()
    }

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        //log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }

    schedule("0 0/$scheduledTimeB * * * ?", TemperaturesModes)
    log.debug "TemperaturesModes scheduled to run every $scheduledTimeB minutes"
    TemperaturesModes()
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





