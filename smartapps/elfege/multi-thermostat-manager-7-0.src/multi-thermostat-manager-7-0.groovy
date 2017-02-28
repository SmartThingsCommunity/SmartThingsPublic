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
        title:      "Select your thermostats",
        nextPage:   "pageSetup"
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

            if(AltSensor1 || AltSensor_2 || AltSensor_3){
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
        nextPage:   "pageSetup"
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


    def scheduledTime = 1

    if(AltSensor_1){
        subscribe(Sensor_1, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_1"
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor1)
        //log.debug "AlternativeSensor1 scheduled to run every $scheduledTime minutes"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        subscribe(Sensor_2, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_2"
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor2)
        //log.debug "AlternativeSensor2 scheduled to run every $scheduledTime minutes"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        subscribe(Sensor_3, "temperature", temperatureHandler)
        //log.debug "Subscription for alternative Sensor for $Sensor_3"
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor3)
        //log.debug "AlternativeSensor3 scheduled to run every $scheduledTime minutes"
        AlternativeSensor3()
    }

    if(Actuators){
        schedule("0 0/$scheduledTime * * * ?", CheckWindows)
        //log.debug "CheckWindows scheduled to run every $scheduledTime minutes"
        CheckWindows()
    }


    atomicState.doorsAreOpen = false
    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true
    atomicState.override = false
    state.ThisIsManual = false


    atomicState.AppMgnt_T_1 = true
    atomicState.AppMgnt_T_2 = true
    atomicState.AppMgnt_T_3 = true
    atomicState.AppMgnt_T_4 = true
    /*  */

    atomicState.closed = true // windows management
    atomicState.hasRun = 0 // windows management

    schedule("0 0/$scheduledTime * * * ?", TemperaturesModes)
    log.debug "TemperaturesModes scheduled to run every $scheduledTime minutes"


}

def appHandler(evt){
    log.debug "app event ${evt.name}:${evt.value} received"
}
def setpointHandler(evt){


    log.debug "New $evt.name for $evt.device : $evt.value"
    log.debug "evt.displayName is $evt.displayName -------------------------------"

    state.SPValue = evt.value
    state.SPname = evt.name
    state.devicedisplayName = evt.displayName
    state.SPdevice = evt.device

    //findSetPointReferences()

    def CurrMode = location.currentMode
     
    //CurrMode = CurrMode[0]
    def HomeMode = null
    def ThisIsManual = false 
    def reference = null
    def termRef = null
    def EventDevice = null 
    def AltSENSOR = false    
    def device = state.SPdevice 

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

    if(state.SPname == "heatingSetpoint"){              
        reference = Math.round(Double.parseDouble(RefHeat))
        reference = reference.toInteger()
        log.debug "RefHeat was: $RefHeat"
        log.debug "RefHeat is now converted to a reference as: $reference"

    }
    else  if(state.SPname == "coolingSetpoint"){ 
        reference = Math.round(Double.parseDouble(RefCool))
        reference = reference.toInteger()
        log.debug "RefCool was: $RefCool"
        log.debug "RefCool is now converted to a reference as: $reference"
    }

    /// 
    def Value = Math.round(Double.parseDouble(state.SPValue))
    Value = Value.toInteger()
    log.debug "Evt value to Integer is : $Value"


    if(Value == reference){  
        log.debug "NO OVERRIDE"
        state.ThisIsManual = false
        // we don't change atomicState.override value here, it'll be done in the TemperaturesModes() loop     
        // we don't change _AppMgtSetPoint either, it'll change only on mode change or update
    }
    else {       
        state.ThisIsManual = true
        atomicState.override = true
        if(device == "${Thermostat_1}")
        {
            atomicState.AppMgnt_T_1 = false
            log.info "atomicState.AppMgnt_T_1 set to $atomicState.AppMgnt_T_1"
        }
        else if(device == "${Thermostat_2}")
        {
            atomicState.AppMgnt_T_2 = false
            log.info "atomicState.AppMgnt_T_2 set to $atomicState.AppMgnt_T_2"
        }
        else if(device == "${Thermostat_3}")
        {
            atomicState.AppMgnt_T_3 = false
            log.info "atomicState.AppMgnt_T_3 set to $atomicState.AppMgnt_T_3"
        }
        else if(device == "${Thermostat_4}")
        {
            atomicState.AppMgnt_T_4 = false
            log.info "atomicState.AppMgnt_T_4 set to $atomicState.AppMgnt_T_4"
        }
        log.debug "new Set Point for $device is MANUAL ---------- OVERRIDE MODE ACTIVATED"
        /// test             
    }   

    log.debug "RefHeat is: $RefHeat"
    log.debug "RefCool is: $RefCool"
    log.debug "reference is: $reference"
    log.debug "SetPoint Change was Manual? ($state.ThisIsManual) if false then should have $reference = $Value"
    atomicState.RefHeat = RefHeat
    atomicState.RefCool = RefCool

}
def ThermostatSwitchHandler(evt){
    log.debug "evt.value at ThermostatSwitchHandler is $evt.value"
    log.debug "evt.device is $evt.device"

    state.EvtValue = evt.value
    state.EvtDevice = evt.device

    log.trace "BEFORE CheckCmdOrigin atomicState.T1_AppMgt = $atomicState.T1_AppMgt || atomicState.T2_AppMgt = $atomicState.T2_AppMgt || atomicState.T3_AppMgt = $atomicState.T3_AppMgt || atomicState.T4_AppMgt = $atomicState.T4_AppMgt"
    log.trace "atomicState.override = $atomicState.override"

    CheckCmdOrigin()
}
def temperatureHandler(evt) { 

    //log.debug "The source of this event is: ${evt.source}"

    log.debug "current temperature value for $evt.device is $evt.value" 
    def currentTemp = XtraTempSensor.currentValue("temperature")
    log.debug "Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp"

    if(AltSensor_1 && atomicState.T1_AppMgt == true){
        AlternativeSensor1()
    }
    if(AltSensor_2 && atomicState.T1_AppMgt == true){
        AlternativeSensor2()
    }
    if(AltSensor_3 && atomicState.T1_AppMgt == true){
        AlternativeSensor3()
    }

    if(currentTemp < CriticalTemp) {
        log.debug "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 
        Thermostat_1.setThermostatMode("heat") 
        atomicState.LatestThermostatMode = "heat"
        if(Themorstat_2){Thermostat_2.setThermostatMode("heat") 
                         atomicState.LatestThermostatMode = "heat"}
        if(Themorstat_3){Thermostat_3.setThermostatMode("heat") 
                         atomicState.LatestThermostatMode = "heat"}
        if(Themorstat_4){Thermostat_4.setThermostatMode("heat") 
                         atomicState.LatestThermostatMode = "heat"}
    } 
    else 
    { 
        log.debug "CriticalTemp OK"
    } 
    TemperaturesModes()
}
def contactHandlerClosed(evt) {

    log.debug "$evt.device is $evt.value"

    if(contact.latestValue("contact").contains("open")){
        log.debug "Not all contacts are closed, doing nothing"
    }
    else {             

        runIn(10, TemperaturesModes)
        log.info "all contacts are closed, unscheduling previous TurnOffThermostats command, cancelling any override by updating all values: update()"
        unschedule(TurnOffThermostats)
        updated()
    } 

    alldoorsareclosed()
}
def contactHandlerOpen(evt) {
    atomicState.doorsAreOpen = true
    log.debug "$evt.device is now $evt.value" 
    log.debug "Turning off all thermostats in $TimeBeforeClosing seconds"
    runIn(TimeBeforeClosing, TurnOffThermostats)   
}
def ChangedModeHandler(evt) {
    state.modeStartTime = now() 
    // these values are to be set here and in mode changes only


    // these values are to be set here and in mode changes only
    atomicState.AppMgnt_T_1 = true
    atomicState.AppMgnt_T_2 = true
    atomicState.AppMgnt_T_3 = true
    atomicState.AppMgnt_T_4 = true
    /*  */

    log.debug "mode changed to ${evt.value}"
    atomicState.T1_AppMgt = true
    atomicState.T2_AppMgt = true
    atomicState.T3_AppMgt = true
    atomicState.T4_AppMgt = true
    atomicState.override = false
    state.ThisIsManual = false

    TemperaturesModes()
}
// main loop
def TemperaturesModes(){
    pollThermostats()
    log.trace "atomicState.T1_AppMgt = $atomicState.T1_AppMgt, atomicState.T2_AppMgt = $atomicState.T2_AppMgt, atomicState.T3_AppMgt = $atomicState.T3_AppMgt, atomicState.T4_AppMgt = $atomicState.T4_AppMgt"

    def doorsOk = alldoorsareclosed()
    if(doorsOk){

        log.trace """atomicState.AppMgnt_T_1 : $atomicState.AppMgnt_T_1, atomicState.AppMgnt_T_2 : $atomicState.AppMgnt_T_2, 
atomicState.AppMgnt_T_3 : $atomicState.AppMgnt_T_3, atomicState.AppMgnt_T_4 : $atomicState.AppMgnt_T_4"""
        def CurrMode = location.currentMode
        def outsideTemp = OutsideSensor.currentTemperature

        log.debug "state.ThisIsManual value is $state.ThisIsManual"

        if(Thermostat_1){
            state.CurrTemp1 = Thermostat_1.currentTemperature
            state.ThermState1 = Thermostat_1.currentValue("thermostatMode") as String
        }
        if(Thermostat_2){
            state.CurrTemp2 = Thermostat_2.currentTemperature
            state.ThermState2 = Thermostat_2.currentValue("thermostatMode") as String
        }
        if(Thermostat_3){
            state.CurrTemp3 = Thermostat_3.currentTemperature
            state.ThermState3 = Thermostat_3.currentValue("thermostatMode") as String
        }
        if(Thermostat_4){
            state.CurrTemp4 = Thermostat_4.currentTemperature
            state.ThermState4 = Thermostat_4.currentValue("thermostatMode") as String
        }

        log.trace "$Thermostat_1 : $state.ThermState1, $Thermostat_2 : $state.ThermState2, $Thermostat_3 : $state.ThermState3, $Thermostat_4 : $state.ThermState4"

        log.trace "CURRENT TEMPS : state.CurrTemp1 : $state.CurrTemp1, state.CurrTemp2 : $state.CurrTemp2, state.CurrTemp3 : $state.CurrTemp3, state.CurrTemp4 : $state.CurrTemp4, OUTSIDE: $outsideTemp"

        if(CurrMode in Home){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.AppMgnt_T_1 == false && atomicState.override == true){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = true
                    if(!AltSensor_1){
                        log.debug "loading $Home settings for $Thermostat_1"        
                        Thermostat_1.setHeatingSetpoint(HSPH1)
                        Thermostat_1.setCoolingSetpoint(CSPH1)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPH1 && state.ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPH1 && state.ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp1 < HSPH1 && state.ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to Heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = true
                    if(!AltSensor_2){
                        log.debug "loading $Home settings for $Thermostat_2"          
                        Thermostat_2.setHeatingSetpoint(HSPH2)
                        Thermostat_2.setCoolingSetpoint(CSPH2)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPH2 && state.ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPH2 && state.ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp2 < HSPH2 && state.ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_3){
                log.debug "atomicState.AppMgnt_T_3 = $atomicState.AppMgnt_T_3"
                if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = true
                    if(!AltSensor_3){
                        log.debug "loading $Home settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPH3)
                        Thermostat_3.setCoolingSetpoint(CSPH3)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPH3 && state.ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPH3 && state.ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp3 < HSPH3 && state.ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.AppMgnt_T_4 == false && atomicState.override == true){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = true
                    log.debug "loading $Home settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPH4)
                    Thermostat_4.setCoolingSetpoint(CSPH4)   

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPH4 && state.ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode = "off"
                    } 
                    else if(outsideTemp >= CSPH4 && state.ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode = "cool"
                    }
                    else if(state.CurrTemp4 < HSPH4 && state.ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode = "heat"

                    }
                }
            }
        }
        else if(CurrMode in Night){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.AppMgnt_T_1 == false && atomicState.override == true){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = true
                    if(!AltSensor_1){
                        log.debug "loading $Night settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPN1)
                        Thermostat_1.setCoolingSetpoint(CSPN1)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPN1 && state.ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPN1 && state.ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp1 < HSPN1 && state.ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = true
                    if(!AltSensor_2){
                        log.debug "loading $Night settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPN2)
                        Thermostat_2.setCoolingSetpoint(CSPN2) 

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPN2 && state.ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPN2 && state.ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp2 < HSPN2 && state.ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_3){
                log.info "state.ThisIsManual is $state.ThisIsManual"
                if(atomicState.AppMgnt_T_3 == false && atomicState.override == true){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = true
                    if(!AltSensor_3){
                        log.debug "loading $Night settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPN3)
                        Thermostat_3.setCoolingSetpoint(CSPN3)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPN3 && state.ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPN3 && state.ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp3 < HSPN3 && state.ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.AppMgnt_T_4 == false && atomicState.override == true){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = true
                    log.debug "loading $Night0 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPN4)
                    Thermostat_4.setCoolingSetpoint(CSPN4)    

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPN4 && state.ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode = "off"
                    } 
                    else if(outsideTemp >= CSPN4 && state.ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode = "cool"
                    }
                    else if(state.CurrTemp4 < HSPN4 && state.ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode = "heat"
                    }           
                }
            }
        }
        else if(CurrMode in Away){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.AppMgnt_T_1 == false && atomicState.override == true){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = true
                    if(!AltSensor_1){
                        log.debug "loading $Away settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPA1)
                        Thermostat_1.setCoolingSetpoint(CSPA1)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPA1 && state.ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPA1 && state.ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp1 < HSPA1 && state.ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_2){
                if(!AltSensor_2){
                    if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                        log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                    }
                    else {
                        state.AppChangedToT2 = true
                        log.debug "loading $Away settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPA2)
                        Thermostat_2.setCoolingSetpoint(CSPA2)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPA2 && state.ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPA2 && state.ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp2 < HSPA2 && state.ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_3){
                if(!AltSensor_3){
                    if(atomicState.AppMgnt_T_3 == false && atomicState.override == true){
                        log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                    }
                    else {
                        state.AppChangedToT3 = true
                        log.debug "loading $Away settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPA3)
                        Thermostat_3.setCoolingSetpoint(CSPA3)   

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPA3 && state.ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPA3 && state.ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp3 < HSPA3 && state.ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.AppMgnt_T_4 == false && atomicState.override == true){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = true
                    log.debug "loading $Away settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPA4)
                    Thermostat_4.setCoolingSetpoint(CSPA4)   

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPA4 && state.ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode = "off"
                    } 
                    else if(outsideTemp >= CSPA4 && state.ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode = "cool"
                    }
                    else if(state.CurrTemp4 < HSPA4 && state.ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode = "heat"
                    }           
                }
            }
        }
        else if(CurrMode in CustomMode1){
            log.debug "CustomMode1"
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1){
                if(atomicState.AppMgnt_T_1 == false && atomicState.override == true){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = true
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode1 settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPCust1_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust1_T1) 


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPCust1_T1 && state.ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T1 && state.ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp1 < HSPCust1_T1 && state.ThermState1 != "heat"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = true
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode1 settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPCust1_T2)
                        Thermostat_2.setCoolingSetpoint(CSPCust1_T2)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPCust1_T2 && state.ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T2 && state.ThermState2 != "cool"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp2 < HSPCust1_T2 && state.ThermState2 != "heat"){
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }      
                }
            }
            if(Thermostat_3){
                log.info "atomicState.AppMgnt_T_3 = $atomicState.AppMgnt_T_3"                
                if(atomicState.AppMgnt_T_3 == false && atomicState.override == true){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = true
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode1 settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPCust1_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust1_T3)   


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPCust1_T3 && state.ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust1_T3 && state.ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp3 < HSPCust1_T3 && state.ThermState3 != "heat"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.AppMgnt_T_4 == false && atomicState.override == true){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = true
                    log.debug "loading $CustomMode1 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPCust1_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust1_T4)     

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPCust1_T4 && state.ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode = "off"
                    } 
                    else if(outsideTemp >= CSPCust1_T4 && state.ThermState4 != "cool"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode = "cool"
                    }
                    else if(state.CurrTemp4 < HSPCust1_T4 && state.ThermState4 != "heat"){
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode = "heat"
                    }   
                }
            }
        }
        else if(CustomMode2 && CurrMode in CustomMode2){
            log.debug "CustomMode2"
            if(Thermostat_1){
                if(atomicState.AppMgnt_T_1 == false && atomicState.override == true){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = true
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode2 settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPCust2_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust2_T1)    


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPCust2_T1 && state.ThermState1 != "off"){
                            atomicState.T1_AppMgt = true
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T1 && state.ThermState1 != "cool"){
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_1 to cool"
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp1 < HSPCust2_T1 && state.ThermState1 != "heat"){
                            log.debug "Setting $Thermostat_1 to heat"
                            atomicState.T1_AppMgt = true // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_2){
                if(atomicState.AppMgnt_T_2 == false && atomicState.override == true){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = true
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode2 settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPCust2_T1)
                        Thermostat_2.setCoolingSetpoint(CSPCust2_T2)


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPCust2_T1 && state.ThermState2 != "off"){
                            atomicState.T2_AppMgt = true
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T2 && state.ThermState2 != "cool"){
                            log.debug "Setting $Thermostat_2 to cool"
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp2 < HSPCust2_T1 && state.ThermState2 != "heat"){
                            log.debug "Setting $Thermostat_2 to heat"
                            atomicState.T2_AppMgt = false // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }      
                }
            }
            if(Thermostat_3){
                log.info "atomicState.AppMgnt_T_3 = $atomicState.AppMgnt_T_3" 
                if(atomicState.AppMgnt_T_3 == false && atomicState.override == true){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = true
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode2 settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPCust2_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust2_T3) 

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPCust2_T3 && state.ThermState3 != "off"){
                            atomicState.T3_AppMgt = true
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off") 
                            atomicState.LatestThermostatMode = "off"
                        } 
                        else if(outsideTemp >= CSPCust2_T3 && state.ThermState3 != "cool"){
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_3 to cool"
                            Thermostat_3.setThermostatMode("cool") 
                            atomicState.LatestThermostatMode = "cool"
                        }
                        else if(state.CurrTemp3 < HSPCust2_T3 && state.ThermState3 != "heat"){
                            log.debug "Setting $Thermostat_3 to heat"
                            atomicState.T3_AppMgt = false // so if turned off it'll be by user
                            Thermostat_3.setThermostatMode("heat") 
                            atomicState.LatestThermostatMode = "heat"
                        }
                    }
                }
            }
            if(Thermostat_4){
                if(atomicState.AppMgnt_T_4 == false && atomicState.override == true){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = true
                    log.debug "loading $CustomMode2 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPCust2_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust2_T4)  

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPCust2_T4 && state.ThermState4 != "off"){
                        atomicState.T4_AppMgt = true
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off") 
                        atomicState.LatestThermostatMode = "off"
                    } 
                    else if(outsideTemp >= CSPCust2_T4 && state.ThermState4 != "cool"){
                        log.debug "Setting $Thermostat_3 to cool"
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool") 
                        atomicState.LatestThermostatMode = "cool"
                    }
                    else if(state.CurrTemp4 < HSPCust2_T4 && state.ThermState4 != "heat"){
                        log.debug "Setting $ThermState4 to heat"
                        atomicState.T4_AppMgt = false // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat") 
                        atomicState.LatestThermostatMode = "heat"
                    }   
                }
            }
        }
    }
    else {
        log.debug "Some windows or doors are open, doing nothing"
    }
    log.info "reseting override's triggers for future reference"
    runIn(10, resetOverride)
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
        state.ThermState = Thermostat_1.currentValue("thermostatMode") as String
        log.trace "state.ThermState for $Thermostat_1 is $state.ThermState"


        def IsOn = state.ThermState in ["heat", "cool"] 

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

        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        if(atomicState.override == false){
            // no setpoint override, no on/off override
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // incresease current thermostat heat setting to force run 

                log.trace "$Thermostat_1: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_1.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_1 heating now set to $NewHeatSet"
                Thermostat_1.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_1 cooling now set to $NewCoolSet"

                // set proper mode
                if(SenTemp < DefaultSetHeat){

                    Thermostat_1.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode = "heat"
                    log.debug "$Thermostat_1 set to Heat"
                    atomicState.T2_AppMgt = false
                }
                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

                    Thermostat_1.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode = "cool"
                    log.debug "$Thermostat_1 set to Cool"
                    atomicState.T2_AppMgt = false
                }
            } 
            else {
                //turning off this unit
                if(state.ThermState == "off"){
                    log.debug "$Thermostat_1 stays off"
                    // Thermostat_1.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off" // redundant
                }
                else {
                    log.debug "turning off $Thermostat_1" 
                    atomicState.T1_AppMgt = true
                    Thermostat_1.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off"     
                }
            }
            state.NewHeatSet1 = NewHeatSet
            state.NewCoolSet1 = NewCoolSet // references used by heatingSetpointHandler()
        }
        else { log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing : atomicState.T2_AppMgt = $atomicState.T2_AppMgt, atomicState.override = $atomicState.override, atomicState.AppMgnt_T_1 = $atomicState.AppMgnt_T_1 " }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
        TurnOffThermostats()
    }
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
        state.ThermState = Thermostat_2.currentValue("thermostatMode") // as String
        log.trace "state.ThermState for $Thermostat_2 is $state.ThermState"
        def IsOn = state.ThermState in ["heat", "cool"]

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
        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        if(atomicState.override == false){
            // no setpoint override, no on/off override
            log.debug "evaluating for AlternativeSensor2"
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // set current thermostat settings to force operation 

                log.trace " NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_2.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_2 heating now set to $NewHeatSet"
                Thermostat_2.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_2 cooling now set to $NewCoolSet"

                // set proper mode

                if(SenTemp < DefaultSetHeat){

                    Thermostat_2.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode = "heat"              
                    log.debug "$Thermostat_2 set to Heat"
                    atomicState.T2_AppMgt = false
                }

                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

                    Thermostat_2.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode = "cool"           
                    log.debug "$Thermostat_2 set to Cool"
                    atomicState.T2_AppMgt = false
                }
            }
            //turning off this unit
            else {

                if(state.ThermState == "off"){
                    log.debug "$Thermostat_2 stays off"
                    //Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off" // redundant 
                }
                else {    
                    log.debug "turning off $Thermostat_2"
                    atomicState.T2_AppMgt = true
                    Thermostat_2.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off"
                }
            }
            state.NewHeatSet2 = NewHeatSet
            log.debug "state.NewHeatSet2 = DefaultSetHeat+5, that is: $DefaultSetHeat + 5 = $state.NewHeatSet2"
            state.NewCoolSet2 = NewCoolSet
            log.debug "state.NewCoolSet2 = DefaultSetCool-5, that is: $DefaultSetCool - 5 = $state.NewCoolSet2"
        }
        else { log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing : atomicState.T2_AppMgt = $atomicState.T2_AppMgt, atomicState.override = $atomicState.override, atomicState.AppMgnt_T_2 = $atomicState.AppMgnt_T_2" }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor2 loop not running"
        TurnOffThermostats()
    }
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
        state.ThermState = Thermostat_3.currentValue("thermostatMode") as String
        log.trace "state.ThermState for $Thermostat_3 is $state.ThermState"
        def IsOn = state.ThermState in ["heat", "cool"]

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

        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        if(atomicState.override == false){

            //(IsOn && atomicState.T3_AppMgt == false) if on but only due to previous app's command, then run
            //(!IsOn && atomicState.override == false) if off but not due to override, then run
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // incresease current thermostat heat setting to force run 

                log.trace "$Thermostat_3: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_3.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_3 heating now set to $NewHeatSet"
                Thermostat_3.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_3 cooling now set to $NewCoolSet"

                // set proper mode
                if(SenTemp < DefaultSetHeat){

                    Thermostat_3.setThermostatMode("heat") 
                    atomicState.LatestThermostatMode = "heat"
                    log.debug "$Thermostat_3 set to Heat"
                    tomicState.T2_AppMgt = false
                }
                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){

                    Thermostat_3.setThermostatMode("cool") 
                    atomicState.LatestThermostatMode = "cool"
                    log.debug "$Thermostat_3 set to Cool"
                    atomicState.T2_AppMgt = false
                }
            } 
            else {
                //turning off this unit

                if(state.ThermState == "off"){
                    log.debug "$Thermostat_3 stays off"
                    // Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off" // redundant
                }
                else {
                    atomicState.T3_AppMgt = true
                    log.debug "turning off $Thermostat_3"      
                    Thermostat_3.setThermostatMode("off") 
                    atomicState.LatestThermostatMode = "off"            
                }
            }
            state.NewHeatSet3 = NewHeatSet
            state.NewCoolSet3 = NewCoolSet
        }
        else { log.debug "$Thermostat_2 in OVERRIDE MODE, doing nothing : atomicState.T2_AppMgt = $atomicState.T2_AppMgt, atomicState.override = $atomicState.override, atomicState.AppMgnt_T_3 = $atomicState.AppMgnt_T_3" }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
        TurnOffThermostats()
    }
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
        if(Thermostat_4.currentValue != state.therm_4_CurrMode){
            Thermostat_4.setThermostatMode("off") 
            atomicState.LatestThermostatMode = "off"
            log.debug "$Thermostat_4 turned off"
            atomicState.T4_AppMgt = true
        }
    }
}
def CheckCmdOrigin(){

    def device = state.EvtDevice
    def event = state.EvtValue

    def latestMode = atomicState.LatestThermostatMode    
    def currentMode = device.currentValue("thermostatMode")

    latestMode.toString()
    currentMode.toString()
    def ThereWasChange = latestMode != currentMode

    log.info "Latest mode for $device was $latestMode and it just switched to $currentMode --------------------"

    log.debug " Change($ThereWasChange) "
    log.info "latest override event regards $device"


    def thisIsOverride = null
    log.info "Override?($thisIsOverride) and currently atomicState.override = $atomicState.override"
    device = device as String

    if(device == "${Thermostat_1}"){

        if(ThereWasChange && atomicState.override == true){
            thisIsOverride = false // unit was set back to previous app's management setting
        }
        else if(ThereWasChange && atomicState.T2_AppMgt == false){
            thisIsOverride = true
        }

        if(thisIsOverride){
            // command did not come from app so manual override is on
            log.debug "MANUAL OVERRIDE for $Thermostat_1"
            atomicState.override = true
        }     
        else if(ThereWasChange && atomicState.override == true && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_1"
            atomicState.override = false
            state.ThisIsManual == false // this will reset all values to settings'
        }     
    }
    else if(device == "${Thermostat_2}"){

        if(ThereWasChange && atomicState.override == true){
            thisIsOverride = false
        }
        else if(ThereWasChange && atomicState.T2_AppMgt == false && atomicState.override == false){
            thisIsOverride = true
        }

        if(thisIsOverride){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_2"
            atomicState.override = true
        }
        else if(ThereWasChange && atomicState.override == true && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_2"
            atomicState.override = false
        }
    } 
    else if(device == "${Thermostat_3}"){

        if(ThereWasChange && atomicState.override == true){
            thisIsOverride = false
        }
        else if(ThereWasChange && atomicState.T2_AppMgt == false){
            thisIsOverride = true
        }
        if(thisIsOverride){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_3"
            atomicState.override = true
        }
        else if(ThereWasChange && atomicState.override == true && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_3"
            atomicState.override = false
        }
    } 
    else if(device == "${Thermostat_4}"){
        thisIsOverride = false
        if(ThereWasChange && atomicState.override == true){
            thisIsOverride = false
        }
        else if(ThereWasChange && atomicState.T2_AppMgt == false){
            thisIsOverride = true
        }

        if(thisIsOverride){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_4"
            atomicState.override = true
        }
        else if(ThereWasChange && atomicState.override == true && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_4"
            atomicState.override = false
        }
    }

    log.info "Override?($thisIsOverride)"

    atomicState.LatestThermostatMode = event // if was manual now this is the new latest value so change after override can be detected
    log.debug "atomicState.LatestThermostatMode is now $atomicState.LatestThermostatMode (event = $event)"
}
def resetOverride(){
    // presets before deciding if override = true or not
    atomicState.T1_AppMgt = false
    atomicState.T2_AppMgt = false
    atomicState.T3_AppMgt = false
    atomicState.T4_AppMgt = false

    atomicState.AppMgnt_T_1 = false
    atomicState.AppMgnt_T_2 = false
    atomicState.AppMgnt_T_3 = false
    atomicState.AppMgnt_T_4 = false

    if( atomicState.hasRun < 1)
    { 
        atomicState.WindowsAppManaged = false 
        log.debug "atomicState.WindowsAppManaged reset to FALSE"
    }

    // the idea is that if any of these values is true or 1 then the change has been made by the app. Otherwise, it was a manual override

    log.info """VALUES RESET : atomicState.T1_AppMgt = $atomicState.T1_AppMgt || atomicState.T2_AppMgt = $atomicState.T2_AppMgt || 
atomicState.T3_AppMgt = $atomicState.T3_AppMgt || atomicState.T4_AppMgt = $atomicState.T4_AppMgt, atomicState.WindowsAppManaged = $atomicState.WindowsAppManaged"""


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

    // atomicState.hasRun = 0

    log.debug "Inside temperature is above Heat Setting"
    if(OkToOpen){
        if(state.hasRun < 1)  {
            if(atomicState.ClosedByApp == 1){ // do not reopen if value is 0

                Actuators?.on()
                log.debug "opening windows"
                if(OperatingTime){
                    log.debug "stop in $OperatingTime seconds"
                    runIn(OperatingTime, StopActuators)
                }
                atomicState.hasRun = state.hasRun + 1
                atomicState.WindowsAppManaged = true // reset periodically unless hasrun > 1
            }
        }
        else { log.debug "Windows open already run, doing nothing" }
    }
    else if (!OkToOpen && atomicState.WindowsAppManaged == true) {
        Actuators?.off()
        log.debug "closing windows"
        atomicState.closed = true
        atomicState.hasRun = 0 // this is an integer beware of not replacing with bool
        atomicState.WindowsAppManaged = true
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
    log.debug "atomicState.WindowsAppManaged?($atomicState.WindowsAppManaged)"

    def result = OutSideWithinMargin && WithinOffSet

    return result

}

def StopActuators(){
    Actuators?.stop()
}



