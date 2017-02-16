definition(
    name: " Multi Thermostat Manager 7.0",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description:  "Manage up to 4 thermostats, in parallel with these options: contact sensors, temperature sensor and modes",
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
                paragraph "This sensor is rendered mandatory by selecting alternate sensor management (above). If you do not have an outside temp measurment device, you can allways create a SmartWeater virtual device"
            }

            input(name: "contact", type:"capability.contactSensor", title: "select windows / contacts", multiple: true, required: true)

            input(name: "TimeBeforeClosing", type: "number", title: "after this amount of time in seconds", required: false, description: "default is 60 seconds", default: 60, uninstall: true, install: true)
            input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "set a safety value, default is 65", defaut: 65)
            input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true)
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
            input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: true, required: false)

        }
        section("Other Modes"){
            input(name: "Night", type : "mode", title: "Select Night mode(s)", multiple: true, required: true)
            input(name: "Away", type : "mode", title: "Select away mode(s)", multiple: true, required: true)
        }
        section("MoreModes"){ 
            input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
            if(Moremodes){
                input(name: "CustomMode1", type : "mode", title: "Select modes", multiple: true, required: true)
                input(name: "CustomMode2", type : "mode", title: "Select modes", multiple: true, required: false, submitOnChange: true)
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
    unsubscribe()
    unschedule()

    init()
}
def init() {

    log.debug "enter updated, state: $state"  
    log.debug "updated with settings = $settings $Modes"


    pollThermostats()
    log.debug "polling thermostats"


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
        log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
    } else if(swichtCapableCap){
        subscribe(Thermostat_1, "Switch", ThermostatSwitchHandler)
        log.debug "$Thermostat_1 has switch capability, subscribing to ThermostatSwitchHandler events"
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
            log.trace "$Thermostat_2 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else if(swichtCapableCap){
            subscribe(Thermostat_2, "Switch", ThermostatSwitchHandler)
            log.trace "$Thermostat_2 has switch capability, subscribing to ThermostatSwitchHandler events"
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
            log.trace "$Thermostat_3 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else if(swichtCapableCap){
            subscribe(Thermostat_3, "Switch", ThermostatSwitchHandler)
            log.trace "$Thermostat_3 has switch capability, subscribing to ThermostatSwitchHandler events"
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
            log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else  if(swichtCapableCap){
            subscribe(Thermostat_4, "Switch", ThermostatSwitchHandler)
            log.trace "$Thermostat_4 has switch capability, subscribing to ThermostatSwitchHandler events"
        } else { log.trace "no switch capability for $Thermostat_4" }
    }

    if(AltSensor_1 || AltSensor_2 || AltSensor_3){
        subscribe(OutsideSensor, "temperature", temperatureHandler)
    }

    if(AltSensor_1){
        subscribe(Sensor_1, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_1"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        subscribe(Sensor_2, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_2"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        subscribe(Sensor_3, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_3"
        AlternativeSensor3()
    }


    // these schedules are for test purpose only
    def scheduledTime = 1
    if(AltSensor_1){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor1)
        log.debug "AlternativeSensor1 scheduled to run every $scheduledTime minutes"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor2)
        log.debug "AlternativeSensor2 scheduled to run every $scheduledTime minutes"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor3)
        log.debug "AlternativeSensor3 scheduled to run every $scheduledTime minutes"
        AlternativeSensor3()
    }

    schedule("0 0/$scheduledTime * * * ?", TemperaturesModes)
    log.debug "TemperaturesModes scheduled to run every $scheduledTime minutes"

    state.doorsAreOpen = 0
    state.Override = 0
    state.T1_AppMgt = 1
    state.T2_AppMgt = 1
    state.T3_AppMgt = 1
    state.T4_AppMgt = 1

    TemperaturesModes()
}

def appHandler(evt){
    log.debug "app event ${evt.name}:${evt.value} received"
}
def setpointHandler(evt){
    log.debug "New $evt.name for $evt.device : $evt.value"

    state.SPValue = evt.value
    state.SPname = evt.name
    state.SPdevice = evt.device

    def CurrMode = location.currentMode
    def HomeMode = null

    def ThisIsManual = false 
    def reference = null
    def termRef = null
    def EventDevice = null 
    def AltSENSOR = false
    def number = 0
    def RefHeat = null
    def RefCool = null
    def device = state.SPdevice as String

    if(device == "${Thermostat_1}"){
        log.debug "evt.device 1 is $evt.device"
        number = 1
        if(AltSensor_1){
            AltSENSOR = true
            log.debug "AltSENSOR is $AltSENSOR"
            AlternativeSensor1()
        }
    }
    else if(device == "${Thermostat_2}"){
        log.debug "evt.device 2 is $evt.device"
        number = 2
        if(AltSensor_2){
            AltSENSOR = true
            log.debug "AltSENSOR is $AltSENSOR"
            AlternativeSensor2()
        }
    } 
    else if(device == "${Thermostat_3}"){
        log.debug "evt.device 3 is $evt.device"
        number = 3
        if(AltSensor_3){
            AltSENSOR = true
            log.debug "AltSENSOR is $AltSENSOR"
            AlternativeSensor1()
        }
    } 
    else if(device == "${Thermostat_4}"){
        log.debug "evt.device 4 is $evt.device"
        AltSENSOR = false // always false because this option doesn't exist for this thermostat number
        number = 4
    }
    log.debug "number is $number"

    def NewHeatSet = ["0", "$state.NewHeatSet1", "$state.NewHeatSet2", "$state.NewHeatSet3"]
    //def NewHeatSet = ["0", "10", "20", "30", "40"]
    def NewCoolSet = ["0", "$state.NewCoolSet1", "$state.NewCoolSet2", "$state.NewCoolSet3"]

    //must be deleted later for test only
    log.debug "NewHeatSet values are $NewHeatSet"

    //array heat
    def HSPH = ["0","$HSPH1", "$HSPH2", "$HSPH3", "$HSPH4"]
    def HSPN = ["0","$HSPN1", "$HSPN2", "$HSPN3", "$HSPN4"]
    def HSPA_ = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
    def HSPCust1 = ["0","$HSPCust1_T1", "$HSPCust1_T2", "$HSPCust1_T3", "$HSPCust1_T4"]
    def HSPCust2 = ["0","$HSPCust2_T1", "$HSPCust2_T2", "$HSPCust2_T3", "$HSPCust2_T4"]
    //array cool
    def CSPH = ["0","$CSPH1", "$CSPH2", "$CSPH3", "$CSPH4"]
    def CSPN = ["0","$CSPN1", "$CSPN2", "$CSPN3", "$CSPN4"]
    def CSPA_ = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
    def CSPCust1 = ["0","$CSPCust1_T1", "$CSPCust1_T2", "$CSPCust1_T3", "$CSPCust1_T4"]
    def CSPCust2 = ["0","$CSPCust2_T1", "$CSPCust2_T2", "$CSPCust2_T3", "$CSPCust2_T4"]

    def HSP = 0
    def CSP = 0

    if(CurrMode in Home){
        HSP = HSPH[number] 
        CSP = CSPH[number] 
        log.debug "HSP is $HSP"
        log.debug "CSP is $CSP"
        if(AltSENSOR){
            RefHeat = NewHeatSet[number] 
            RefCool = NewCoolSet[number] 
        }
        else {
            RefHeat = HSP 
            RefCool = CSP 
        }
    }
    else if(CurrMode in Night){
        HSP = HSPN[number] 
        CSP = CSPN[number] 
        log.debug "HSP is $HSP"
        log.debug "CSP is $CSP"
        if(AltSENSOR){
            RefHeat = NewHeatSet[number] 
            RefCool = NewCoolSet[number] 
        }
        else {
            RefHeat = HSP 
            RefCool = CSP 
        }
    } 
    else if(CurrMode in Away){
        HSP = HSPA_[number] 
        CSP = CSPA[number] 
        log.debug "HSP is $HSP"
        log.debug "CSP is $CSP"
        if(AltSENSOR){
            RefHeat = NewHeatSet[number] 
            RefCool = NewCoolSet[number] 
        }
        else {
            RefHeat = HSP 
            RefCool = CSP 
        }
    }
    else if(CurrMode in CustomMode1){
        HSP = HSPCust1[number] 
        CSP = CSPCust1[number] 
        log.debug "HSP is $HSP"
        log.debug "CSP is $CSP"
        if(AltSENSOR){
            RefHeat = NewHeatSet[number] 
            RefCool = NewCoolSet[number] 
        }
        else {
            RefHeat = HSP 
            RefCool = CSP 
        }
    }
    else if(CurrMode in CustomMode2){
        HSP = HSPCust2[number] 
        CSP = CSPCust2[number] 
        log.debug "HSP is $HSP"
        log.debug "CSP is $CSP"
        if(AltSENSOR){
            RefHeat = NewHeatSet[number] 
            RefCool = NewCoolSet[number]
            log.debug "NewHeatSet is $RefHeat"
            log.debug "NewCoolSet is $RefCool"
        }
        else {
            RefHeat = HSP 
            RefCool = CSP 
            log.debug "NewHeatSet is $RefHeat"
            log.debug "NewCoolSet is $RefCool"
        }
    }

    if(state.SPname == "heatingSetpoint"){              
        reference = Math.round(Double.parseDouble(RefHeat))
        reference = RefHeat.toInteger()
        log.debug "RefHeat was: $RefHeat"
        log.debug "RefHeat is now converted to a reference as: $reference"

    }
    else  if(state.SPname == "coolingSetpoint"){ 
        reference = Math.round(Double.parseDouble(CoolHeat))
        reference = reference.toInteger()
        log.debug "CoolHeat was: $CoolHeat"
        log.debug "CoolHeat is now converted to a reference as: $reference"
    }

    /// 
    def Value = Math.round(Double.parseDouble(state.SPValue))
    Value = Value.toInteger()
    log.debug "Evt value to Integer is : $Value"

    if(Value == reference){
        log.debug "NO OVERRIDE"
        state.ThisIsManual = false
    }
    else {
        log.debug "new Set Point for $device is MANUAL ---------- OVERRIDE MODE ACTIVATED"
        state.ThisIsManual = true
    }


    log.debug "RefCool is: $RefCool"
    log.debug "reference is: $reference"
    log.debug "SetPoint Change was Manual? ($state.ThisIsManual) if false then should have $reference = $Value"
    state.number = number

}

def ThermostatSwitchHandler(evt){
    log.debug "evt.value at ThermostatSwitchHandler is $evt.value"
    log.debug "evt.device is $evt.device"
    def device = evt.device as String

    def OnValues = ["on", "auto", "cool", "heat"]
    def OnValue = null
    if(evt.value in OnValues){
        OnValue = true
    }
    else {
        OnValue = false
    }
    pause(500)
    log.debug "Turned ON?($OnValue)"	
    log.trace "BEFORE LOOP state.T1_AppMgt = $state.T1_AppMgt || state.T2_AppMgt = $state.T2_AppMgt || state.T3_AppMgt = $state.T3_AppMgt || state.T4_AppMgt = $state.T4_AppMgt"
    log.trace "state.override = $state.override"
    pause(500)

    if(device == "${Thermostat_1}"){
        if(!OnValue && state.T1_AppMgt == 0){
            // command did not come from app so manual override is on
            log.debug "MANUAL OVERRIDE for $Thermostat_1"
            state.Override = 1
        }
        else if(OnValue && state.Override == 1 && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_1"
            state.T1_AppMgt = 1
            state.Override = 0
            state.ThisIsManual == false // this will reset all values to settings'
        }     
    }
    else if(device == "${Thermostat_2}"){
        if(!OnValue && state.T2_AppMgt == 0){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_2"
            state.Override = 1
        }
        else if (OnValue && state.Override == 1 && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_2"
            state.T2_AppMgt = 1
            state.Override = 0
        }
    } 
    else if(device == "${Thermostat_3}"){
        if(!OnValue && state.T3_AppMgt == 0){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_3"
            state.Override = 1
        }
        else if (OnValue && state.Override == 0 && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_3"
            state.T3_AppMgt = 1
            state.Override = 0
        }
    } 
    else if(device == "${Thermostat_4}"){
        if(!OnValue && state.T4_AppMgt == 1){
            // command did not come from app
            log.debug "MANUAL OVERRIDE for $Thermostat_4"
            state.Override = 1
        }
        else if (OnValue && state.Override == 0 && state.ThisIsManual == false){
            // manual override deactivated
            log.debug "END of MANUAL OVERRIDE for $Thermostat_4"
            state.T4_AppMgt = 1
            state.Override = 0
        }
    }
    log.trace "AFTER LOOP : state.T1_AppMgt = $state.T1_AppMgt || state.T2_AppMgt = $state.T2_AppMgt || state.T3_AppMgt = $state.T3_AppMgt || state.T4_AppMgt = $state.T4_AppMgt"
}
def temperatureHandler(evt) { 

    log.debug "The source of this event is: ${evt.source}"

    log.debug "current temperature value for $evt.device is $evt.value" 
    def currentTemp = XtraTempSensor.currentValue("temperature")
    log.debug "Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp"

    if(AltSensor_1 && state.T1_AppMgt == 1){
        AlternativeSensor1()
    }
    if(AltSensor_2 && state.T1_AppMgt == 1){
        AlternativeSensor2()
    }
    if(AltSensor_3 && state.T1_AppMgt == 1){
        AlternativeSensor3()
    }

    if(currentTemp < CriticalTemp) {
        log.debug "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 
        Thermostat_1.setThermostatMode("heat")
        if(Themorstat_2){Thermostat_2.setThermostatMode("heat")}
        if(Themorstat_3){Thermostat_3.setThermostatMode("heat")}
        if(Themorstat_4){Thermostat_4.setThermostatMode("heat")}
    } 
    else 
    { 
        log.debug "CriticalTemp OK"
    } 
    TemperaturesModes()
}
def AlternativeSensor1(){
    def doorsOk = alldoorsareclosed()
    if(doorsOk){
        log.debug "Running Alternative Sensor Loop for $Thermostat_1"
        def SenTemp = Sensor_1.currentTemperature
        log.debug "Current Temperature at $Sensor_1 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = 0 
        def NewCoolSet = 0
        state.ThermState = Thermostat_1.currentValue("thermostatMode") as String

        def IsOn = null 
        if(state.ThermState in ["heat", "cool"]){
            IsOn = true
        } 
        else {
            IsOn = false
        }
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
        if((!IsOn && state.override == 0) || (IsOn && state.T1_AppMgt == 0)){
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // incresease current thermostat heat setting to force run 

                log.trace "$Thermostat_1: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_1.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_1 heating now set to $NewHeatSet"
                Thermostat_1.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_1 cooling now set to $NewCoolSet"

                // set proper mode
                if(SenTemp < DefaultSetHeat){
                    state.T1_AppMgt = 0
                    Thermostat_1.setThermostatMode("heat")
                    log.debug "$Thermostat_1 set to Heat"
                }
                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
                    state.T1_AppMgt = 0
                    Thermostat_1.setThermostatMode("cool")
                    log.debug "$Thermostat_1 set to Cool"
                }
            } 
            else {
                //turning off this unit
                if(state.ThermState == "off"){
                    log.debug "$Thermostat_1 stays off"
                    Thermostat_1.setThermostatMode("off") // redundancy to prevent failures to stop
                }
                else {
                    log.debug "turning off $Thermostat_1" 
                    state.T1_AppMgt = 1
                    Thermostat_1.setThermostatMode("off")     
                }
            }
            state.NewHeatSet1 = NewHeatSet
            state.NewCoolSet1 = NewCoolSet // references used by heatingSetpointHandler()
        }
        else { log.debug "$Thermostat_1 already properly set (state.T1_AppMgt = $state.T1_AppMgt) or in OVERRIDE MODE (state.override = $state.override), doing nothing" }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
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
        def NewHeatSet = 0 
        def NewCoolSet = 0
        def CurrMode = location.currentMode
        state.ThermState = Thermostat_2.currentValue("thermostatMode") // as String
        log.trace "state.ThermState for $Thermostat_2 is $state.ThermState"
        def IsOn = null 
        if(state.ThermState in ["heat", "cool"]){
            IsOn = true
        } 
        else {
            IsOn = false
        }
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
        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        if((!IsOn && state.override == 0) || (IsOn && state.T2_AppMgt == 0)){
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // set current thermostat settings to force operation 

                log.trace "$Thermostat_2: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_2.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_2 heating now set to $NewHeatSet"
                Thermostat_2.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_2 cooling now set to $NewCoolSet"

                // set proper mode

                if(SenTemp < DefaultSetHeat){
                    state.T2_AppMgt = 0
                    Thermostat_2.setThermostatMode("heat")              
                    log.debug "$Thermostat_2 set to Heat"
                }

                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
                    state.T2_AppMgt = 0
                    Thermostat_2.setThermostatMode("cool")           
                    log.debug "$Thermostat_2 set to Cool"
                }

                else {
                    //turning off this unit
                    if(state.ThermState == "off"){
                        log.debug "$Thermostat_2 stays off"

                        Thermostat_2.setThermostatMode("off") // redundency due to fucked up syncs sometimes
                    }
                    else {    
                        log.debug "turning off $Thermostat_2"
                        state.T2_AppMgt = 1
                        Thermostat_2.setThermostatMode("off")
                    }
                }
                state.NewHeatSet2 = NewHeatSet
                log.debug "state.NewHeatSet2 = DefaultSetHeat+5, that is: $DefaultSetHeat + 5 = $state.NewHeatSet2"
                state.NewCoolSet2 = NewCoolSet
                log.debug "state.NewCoolSet2 = DefaultSetCool-5, that is: $DefaultSetCool - 5 = $state.NewCoolSet2"
            }
        }
        else { log.debug "$Thermostat_2 already properly set (state.T2_AppMgt = $state.T2_AppMgt) or in OVERRIDE MODE (state.override = $state.override), doing nothing" }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
    }
}
def AlternativeSensor3(){

    def doorsOk = alldoorsareclosed()
    if(doorsOk){
        log.debug "Running Alternative Sensor Loop for $Thermostat_3"
        def SenTemp = Sensor_3.currentTemperature
        log.debug "Current Temperature at $Sensor_3 is ${SenTemp}F"
        def OutsideTemp = OutsideSensor.currentTemperature
        def NewHeatSet = 0 
        def NewCoolSet = 0
        def CurrMode = location.currentMode
        state.ThermState = Thermostat_1.currentValue("thermostatMode") as String
        def IsOn = null 
        if(state.ThermState in ["heat", "cool"]){
            IsOn = true
        } 
        else {
            IsOn = false
        }
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

        if((!IsOn && state.override == 0) || (IsOn && state.T3_AppMgt == 0)){

            //(IsOn && state.T3_AppMgt == 0) if on but only due to previous app's command, then run
            //(!IsOn && state.override == 0) if off but not due to override, then run
            if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
                // incresease current thermostat heat setting to force run 

                log.trace "$Thermostat_3: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
                Thermostat_3.setHeatingSetpoint(NewHeatSet)
                log.debug "$Thermostat_3 heating now set to $NewHeatSet"
                Thermostat_3.setCoolingSetpoint(NewCoolSet)
                log.debug "$Thermostat_3 cooling now set to $NewCoolSet"

                // set proper mode
                if(SenTemp < DefaultSetHeat){
                    state.T3_AppMgt = 0
                    Thermostat_3.setThermostatMode("heat")
                    log.debug "$Thermostat_3 set to Heat"
                }
                else if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
                    state.T3_AppMgt = 0
                    Thermostat_3.setThermostatMode("cool")
                    log.debug "$Thermostat_3 set to Cool"
                }
            } 
            else {
                //turning off this unit
                
                if(state.ThermState == "off"){
                    log.debug "$Thermostat_3 stays off"
                    Thermostat_3.setThermostatMode("off") // redundency due to fucked up syncs sometimes
                }
                else {
                    state.T3_AppMgt = 1
                    log.debug "turning off $Thermostat_3"      
                    Thermostat_3.setThermostatMode("off")            
                }
            }
            state.NewHeatSet3 = NewHeatSet
            state.NewCoolSet3 = NewCoolSet
        }
        else { log.debug "$Thermostat_3 already properly set (state.T1_AppMgt = $state.T1_AppMgt) or in OVERRIDE MODE (state.override = $state.override), doing nothing" }
    }
    else { 
        log.debug "some doors are open, AlternativeSensor1 loop not running"
    }
}

def contactHandlerClosed(evt) {

    log.debug "$evt.device is $evt.value"

    if(contact.latestValue("contact").contains("open")){
        log.debug "Not all contacts are closed, doing nothing"
    }
    else {             
        state.doorsAreOpen = 0
        runIn(10, TemperaturesModes)
        log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
        unschedule(TurnOffThermostats)
    } 
    alldoorsareclosed()
}

def contactHandlerOpen(evt) {
    state.doorsAreOpen = 1
    log.debug "$evt.device is now $evt.value" 
    log.debug "Turning off all thermostats in $TimeBeforeClosing seconds"
    runIn(TimeBeforeClosing, TurnOffThermostats)   
}
def ChangedModeHandler(evt) {

    state.modeStartTime = now() 

    log.debug "mode changed to ${evt.value}"
    state.T1_AppMgt = 1
    state.override = 0
    state.ThisIsManual = false

    //array heat
    def HSPH = ["0","$HSPH1", "$HSPH2", "$HSPH3", "$HSPH4"]
    def HSPN = ["0","$HSPN1", "$HSPN2", "$HSPN3", "$HSPN4"]
    def HSPA_ = ["0","$HSPA", "$HSPA", "$HSPA", "$HSPA"]
    def HSPCust1 = ["0","$HSPCust1_T1", "$HSPCust1_T2", "$HSPCust1_T3", "$HSPCust1_T4"]
    def HSPCust2 = ["0","$HSPCust2_T1", "$HSPCust2_T2", "$HSPCust2_T3", "$HSPCust2_T4"]
    //array cool
    def CSPH = ["0","$CSPH1", "$CSPH2", "$CSPH3", "$CSPH4"]
    def CSPN = ["0","$CSPN1", "$CSPN2", "$CSPN3", "$CSPN4"]
    def CSPA_ = ["0","$CSPA", "$CSPA", "$CSPA", "$CSPA"]
    def CSPCust1 = ["0","$CSPCust1_T1", "$CSPCust1_T2", "$CSPCust1_T3", "$CSPCust1_T4"]
    def CSPCust2 = ["0","$CSPCust2_T1", "$CSPCust2_T2", "$CSPCust2_T3", "$CSPCust2_T4"]

    log.trace "HSPH: $HSPH, HSPN: $HSPN, HSPA: $HSPA, HSPCust1: $HSPCust1, HSPCust2: $HSPCust2"

    log.trace "CSPH: $CSPH, CSPN: $CSPN, CSPA_: $CSPA_, CSPCust1: $CSPCust1, CSPCust2: $CSPCust2"

    TemperaturesModes()
}
def alldoorsareclosed(){

    log.debug "state.doorsAreOpen value is: $state.doorsAreOpen"
    if(state.doorsAreOpen == 1){
        false
    }
    else {
        true
    }
}

def TemperaturesModes(){
    log.trace "state.T1_AppMgt = $state.T1_AppMgt, state.T2_AppMgt = $state.T2_AppMgt, state.T3_AppMgt = $state.T3_AppMgt, state.T4_AppMgt = $state.T4_AppMgt"

    def doorsOk = alldoorsareclosed()
    if(doorsOk){
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
            if(Thermostat_1 && state.T1_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 1){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = 1
                    if(!AltSensor_1){
                        log.debug "loading $Home settings for $Thermostat_1"        
                        Thermostat_1.setHeatingSetpoint(HSPH1)
                        Thermostat_1.setCoolingSetpoint(CSPH1)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPH1 && state.ThermState1 != "off"){
                            state.T1_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPH1 && state.ThermState1 != "cool"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp1 < HSPH1 && state.ThermState1 != "heat"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to Heat"
                            Thermostat_1.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_2 && state.T2_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 2){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = 1
                    if(!AltSensor_2){
                        log.debug "loading $Home settings for $Thermostat_2"          
                        Thermostat_2.setHeatingSetpoint(HSPH2)
                        Thermostat_2.setCoolingSetpoint(CSPH2)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPH2 && state.ThermState2 != "off"){
                            state.T2_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPH2 && state.ThermState2 != "cool"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp2 < HSPH2 && state.ThermState2 != "heat"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_3 && state.T3_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 3){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = 1
                    if(!AltSensor_3){
                        log.debug "loading $Home settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPH3)
                        Thermostat_3.setCoolingSetpoint(CSPH3)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPH3 && state.ThermState3 != "off"){
                            state.T3_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPH3 && state.ThermState3 != "cool"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp3 < HSPH3 && state.ThermState3 != "heat"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_4 && state.T4_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 4){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = 1
                    log.debug "loading $Home settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPH4)
                    Thermostat_4.setCoolingSetpoint(CSPH4)   

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPH4 && state.ThermState4 != "off"){
                        state.T4_AppMgt = 1
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off")
                    } 
                    else if(outsideTemp >= CSPH4 && state.ThermState4 != "cool"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool")
                    }
                    else if(state.CurrTemp4 < HSPH4 && state.ThermState4 != "heat"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat")

                    }
                }
            }
        }
        else if(CurrMode in Night){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1 && state.T1_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 1){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = 1
                    if(!AltSensor_1){
                        log.debug "loading $Night settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPN1)
                        Thermostat_1.setCoolingSetpoint(CSPN1)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPN1 && state.ThermState1 != "off"){
                            state.T1_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPN1 && state.ThermState1 != "cool"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp1 < HSPN1 && state.ThermState1 != "heat"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_2 && state.T2_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 2){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = 1
                    if(!AltSensor_2){
                        log.debug "loading $Night settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPN2)
                        Thermostat_2.setCoolingSetpoint(CSPN2) 

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPN2 && state.ThermState2 != "off"){
                            state.T2_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPN2 && state.ThermState2 != "cool"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp2 < HSPN2 && state.ThermState2 != "heat"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_3 && state.T3_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 3){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = 1
                    if(!AltSensor_3){
                        log.debug "loading $Night settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPN3)
                        Thermostat_3.setCoolingSetpoint(CSPN3)  


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPN3 && state.ThermState3 != "off"){
                            state.T3_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPN3 && state.ThermState3 != "cool"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp3 < HSPN3 && state.ThermState3 != "heat"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_4 && state.T4_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 4){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = 1
                    log.debug "loading $Night0 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPN4)
                    Thermostat_4.setCoolingSetpoint(CSPN4)    

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPN4 && state.ThermState4 != "off"){
                        state.T4_AppMgt = 1
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off")
                    } 
                    else if(outsideTemp >= CSPN4 && state.ThermState4 != "cool"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool")
                    }
                    else if(state.CurrTemp4 < HSPN4 && state.ThermState4 != "heat"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat")
                    }           
                }
            }
        }
        else if(CurrMode in Away){
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1 && state.T1_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 1){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = 1
                    if(!AltSensor_1){
                        log.debug "loading $Away settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPA1)
                        Thermostat_1.setCoolingSetpoint(CSPA1)

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPA1 && state.ThermState1 != "off"){
                            state.T1_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPA1 && state.ThermState1 != "cool"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp1 < HSPA1 && state.ThermState1 != "heat"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_2 && state.T2_AppMgt == 1){
                if(!AltSensor_2){
                    if(state.ThisIsManual == true && state.number == 2){
                        log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                    }
                    else {
                        state.AppChangedToT2 = 1
                        log.debug "loading $Away settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPA2)
                        Thermostat_2.setCoolingSetpoint(CSPA2)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPA2 && state.ThermState2 != "off"){
                            state.T2_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPA2 && state.ThermState2 != "cool"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp2 < HSPA2 && state.ThermState2 != "heat"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_3 && state.T3_AppMgt == 1){
                if(!AltSensor_3){
                    if(state.ThisIsManual == true && state.number == 3){
                        log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                    }
                    else {
                        state.AppChangedToT3 = 1
                        log.debug "loading $Away settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPA3)
                        Thermostat_3.setCoolingSetpoint(CSPA3)   

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPA3 && state.ThermState3 != "off"){
                            state.T3_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPA3 && state.ThermState3 != "cool"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp3 < HSPA3 && state.ThermState3 != "heat"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_4 && state.T4_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 4){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = 1
                    log.debug "loading $Away settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPA4)
                    Thermostat_4.setCoolingSetpoint(CSPA4)   

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPA4 && state.ThermState4 != "off"){
                        state.T4_AppMgt = 1
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off")
                    } 
                    else if(outsideTemp >= CSPA4 && state.ThermState4 != "cool"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool")
                    }
                    else if(state.CurrTemp4 < HSPA4 && state.ThermState4 != "heat"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat")
                    }           
                }
            }
        }
        else if(CurrMode in CustomMode1){
            log.debug "CustomMode1"
            log.debug "location is in $CurrMode mode, applying settings accordingly" 
            if(Thermostat_1 && state.T1_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 1){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = 1
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode1 settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPCust1_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust1_T1) 


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPCust1_T1 && state.ThermState1 != "off"){
                            state.T1_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust1_T1 && state.ThermState1 != "cool"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp1 < HSPCust1_T1 && state.ThermState1 != "heat"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_2 && state.T2_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 2){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = 1
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode1 settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPCust1_T2)
                        Thermostat_2.setCoolingSetpoint(CSPCust1_T2)  

                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPCust1_T2 && state.ThermState2 != "off"){
                            state.T2_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust1_T2 && state.ThermState2 != "cool"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp2 < HSPCust1_T2 && state.ThermState2 != "heat"){
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat")
                        }
                    }      
                }
            }
            if(Thermostat_3 && state.T3_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 3){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = 1
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode1 settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPCust1_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust1_T3)   


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPCust1_T3 && state.ThermState3 != "off"){
                            state.T3_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust1_T3 && state.ThermState3 != "cool"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_3.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp3 < HSPCust1_T3 && state.ThermState3 != "heat"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_3.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_4 && state.T4_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 4){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = 1
                    log.debug "loading $CustomMode1 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPCust1_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust1_T4)     

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPCust1_T4 && state.ThermState4 != "off"){
                        state.T4_AppMgt = 1
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off")
                    } 
                    else if(outsideTemp >= CSPCust1_T4 && state.ThermState4 != "cool"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to cool"
                        Thermostat_4.setThermostatMode("cool")
                    }
                    else if(state.CurrTemp4 < HSPCust1_T4 && state.ThermState4 != "heat"){
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        log.debug "$Thermostat_1 set to heat"
                        Thermostat_4.setThermostatMode("heat")
                    }   
                }
            }
        }
        else if(CustomMode2 && CurrMode in CustomMode2){
            log.debug "CustomMode2"
            if(Thermostat_1 && state.T1_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 1){
                    log.debug "${Thermostat_1}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT1 = 1
                    if(!AltSensor_1){
                        log.debug "loading $CustomMode2 settings for $Thermostat_1"
                        Thermostat_1.setHeatingSetpoint(HSPCust2_T1)
                        Thermostat_1.setCoolingSetpoint(CSPCust2_T1)    


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp1 > HSPCust2_T1 && state.ThermState1 != "off"){
                            state.T1_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_1"
                            Thermostat_1.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust2_T1 && state.ThermState1 != "cool"){
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_1 to cool"
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_1.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp1 < HSPCust2_T1 && state.ThermState1 != "heat"){
                            log.debug "Setting $Thermostat_1 to heat"
                            state.T1_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_1.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_2 && state.T2_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 2){
                    log.debug "${Thermostat_2}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT2 = 1
                    if(!AltSensor_2){
                        log.debug "loading $CustomMode2 settings for $Thermostat_2"
                        Thermostat_2.setHeatingSetpoint(HSPCust2_T1)
                        Thermostat_2.setCoolingSetpoint(CSPCust2_T2)


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp2 > HSPCust2_T1 && state.ThermState2 != "off"){
                            state.T2_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_2"
                            Thermostat_2.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust2_T2 && state.ThermState2 != "cool"){
                            log.debug "Setting $Thermostat_2 to cool"
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to cool"
                            Thermostat_2.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp2 < HSPCust2_T1 && state.ThermState2 != "heat"){
                            log.debug "Setting $Thermostat_2 to heat"
                            state.T2_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "$Thermostat_1 set to heat"
                            Thermostat_2.setThermostatMode("heat")
                        }
                    }      
                }
            }
            if(Thermostat_3 && state.T3_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 3){
                    log.debug "${Thermostat_3}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT3 = 1
                    if(!AltSensor_3){
                        log.debug "loading $CustomMode2 settings for $Thermostat_3"
                        Thermostat_3.setHeatingSetpoint(HSPCust2_T3)
                        Thermostat_3.setCoolingSetpoint(CSPCust2_T3) 


                        // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                        if(state.CurrTemp3 > HSPCust2_T3 && state.ThermState3 != "off"){
                            state.T3_AppMgt = 1
                            log.debug "Turning OFF $Thermostat_3"
                            Thermostat_3.setThermostatMode("off")
                        } 
                        else if(outsideTemp >= CSPCust2_T3 && state.ThermState3 != "cool"){
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            log.debug "Setting $Thermostat_3 to cool"
                            Thermostat_3.setThermostatMode("cool")
                        }
                        else if(state.CurrTemp3 < HSPCust2_T3 && state.ThermState3 != "heat"){
                            log.debug "Setting $Thermostat_3 to heat"
                            state.T3_AppMgt = 0 // so if turned off it'll be by user
                            Thermostat_3.setThermostatMode("heat")
                        }
                    }
                }
            }
            if(Thermostat_4 && state.T4_AppMgt == 1){
                if(state.ThisIsManual == true && state.number == 4){
                    log.debug "${Thermostat_4}'s SetPoint changed by user's OVERRIDE, doing nothing"
                }
                else {
                    state.AppChangedToT4 = 1
                    log.debug "loading $CustomMode2 settings for $Thermostat_4"
                    Thermostat_4.setHeatingSetpoint(HSPCust2_T4)
                    Thermostat_4.setCoolingSetpoint(CSPCust2_T4)  

                    // no AltSensor 4 
                    if(state.CurrTemp4 > HSPCust2_T4 && state.ThermState4 != "off"){
                        state.T4_AppMgt = 1
                        log.debug "Turning OFF $Thermostat_4"
                        Thermostat_4.setThermostatMode("off")
                    } 
                    else if(outsideTemp >= CSPCust2_T4 && state.ThermState4 != "cool"){
                        log.debug "Setting $Thermostat_3 to cool"
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("cool")
                    }
                    else if(state.CurrTemp4 < HSPCust2_T4 && state.ThermState4 != "heat"){
                        log.debug "Setting $ThermState4 to heat"
                        state.T4_AppMgt = 0 // so if turned off it'll be by user
                        Thermostat_4.setThermostatMode("heat")
                    }   
                }
            }
        }
    }
    else {
        log.debug "Some windows or doors are open, doing nothing"
    }
}

def TurnOffThermostats() {
    log.debug "Turning off thermostats" 
    Thermostat_1.setThermostatMode("off")
    log.debug "$Thermostat_1  turned off"
    if(Thermostat_2){      
        Thermostat_2.setThermostatMode("off")
        log.debug "$Thermostat_2 turned off"
    }
    if(Thermostat_3){
        Thermostat_3.setThermostatMode("off")
        log.debug "$Thermostat_3 turned off"
    }
    if(Thermostat_4){
        if(Thermostat_4.currentValue != state.therm_4_CurrMode){
            Thermostat_4.setThermostatMode("off")
            log.debug "$Thermostat_4 turned off"
        }
    }
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
            log.debug "polling Thermostat_2"
        }
        else if(Thermostat_3.hasCommand("refresh")){
            Thermostat_4.refresh()
            log.debug "refreshing Thermostat_2"
        }
        else { log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
             }
    }
}


