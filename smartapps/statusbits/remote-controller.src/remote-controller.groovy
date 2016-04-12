/**
 *  Remote Controller.
 *
 *  This SmartApp allows using remote controls (e.g. Aeon Labs Minimote) to
 *  execute routines, change modes and set the Smart Home Monitor mode.
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright © 2015 Statusbits.com. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain a
 *  copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  --------------------------------------------------------------------------
 *
 *  Version 1.0.2 (10/04/2015)
 */

import groovy.json.JsonSlurper

definition(
    name: "Remote Controller",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Use remote controls to execute routines, change modes " +
        "and set the Smart Home Monitor mode.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/unknown/zwave/remote-controller@2x.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/unknown/zwave/remote-controller@2x.png",
    oauth: false
)

preferences {
    page(name:"pageSetup")
    page(name:"pageAddButton")
    page(name:"pageEditButton")
    page(name:"pageEditLight")
}

// Show "Setup Menu" page
private def pageSetup() {
    LOG("pageSetup()")

    def buttons = getButtons()

    def hrefAbout = [
        url:        "http://statusbits.github.io/smartthings/",
        style:      "embedded",
        title:      "More information...",
        description:"http://statusbits.github.io/smartthings/",
        required:   false
    ]

    def inputRemotes = [
        name:       "remotes",
        type:       "capability.button",
        title:      "Which remote controls?",
        multiple:   true,
        required:   false
    ]

    def inputDimmers = [
        name        : "dimmers",
        type        : "capability.switchLevel",
        title       : "Select Dimmers",
        multiple:   true,
        required:   false
    ]

    def inputSwitches = [
        name        : "switches",
        type        : "capability.switch",
        title       : "Select Switches",
        multiple:   true,
        required:   false
    ]

    def inputHues = [
        name        : "color",
        type        : "capability.colorControl",
        title       : "Select Color Bulbs",
        multiple:   true,
        required:   false
    ]

    def inputTemp = [
        name        : "temp",
        type        : "capability.colorControl",
        title       : "Select Color Temperature",
        multiple:   true,
        required:   false
    ]

    def pageProperties = [
        name:       "pageSetup",
        //title:      "Setup",
        nextPage:   null,
        install:    true,
        uninstall:  state.installed ?: false
    ]

    return dynamicPage(pageProperties) {
        section("About", hideable:true, hidden:true) {
            paragraph about()
            href hrefAbout
        }

        section("Setup Menu") {
            input inputRemotes
            input inputDimmers
            input inputSwitches
            input inputHues
            input inputTemp
            href "pageAddButton", title:"Add button", description:"Tap to open"
            buttons.each() { button ->
                href "pageEditButton", params:[button:button], title:"Configure button ${button}",
                    description:"Tap to open"
            }
        }

        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "Add Button" setup page.
private def pageAddButton() {
    LOG("pageAddButton()")

    def textHelp =
        "Enter button number between 1 and 99."

    def pageProperties = [
        name:       "pageAddButton",
        title:      "Add Button",
        nextPage:   "pageEditButton",
    ]

    return dynamicPage(pageProperties) {
        section() {
            paragraph textHelp
            input "cfg_button", "number", title:"Which button?"
        }
    }
}

// Show "Configure Button" setup page.
private def pageEditButton(params) {
    LOG("pageEditButton(${params})")

    def button = params.button?.toInteger()
    if (!button) {
        button = settings.cfg_button?.toInteger()
    }

    if (!button || button < 1 || button > 99) {
        log.error "Invalid button number '${button}'"
        return pageSetup()
    }

    def textHelp =
        "You can configure buttons to execute routines, change modes and " +
        "set the Smart Home Monitor mode.\n\n" +
        "Some remote controls, for example Aeon Labs Minimote, can " +
        "recognize whether the button was pushed momentarily or held down. " +
        "You can configure Remote Controller to perform different actions " +
        "depending on the type of button action."

    def routines = getRoutineNames()
    def modes = getModeNames()
    def alarmModes = ["Away", "Stay", "Off"]
    def buttonModes = ["pushed", "held"]

    def pageProperties = [
        name:       "pageEditButton",
        title:      "Configure Button ${button}",
        nextPage:   "pageSetup",
    ]

    return dynamicPage(pageProperties) {
        section() {
            paragraph textHelp
        }

        for (mode in buttonModes) {
            section("'${mode.capitalize()}' Button Actions") {
                input "btn_${button}_${mode}_routine", "enum", title:"Execute routine",
                        options:routines, required:false
                input "btn_${button}_${mode}_mode", "enum", title:"Change the mode to",
                        options:modes, required:false
                input "btn_${button}_${mode}_alarm", "enum", title:"Set Smart Home Monitor to",
                        options:alarmModes, required:false

                settings.dimmers?.each() {
                    def buttonValue = "btn_${button}_${mode}_${it.id}"
                    def description = "Tap to edit. toggle:"+settings[buttonValue+'_toggle']+" level:"+settings[buttonValue+'_level']
                    href 'pageEditLight', params:[button:button,mode:mode,name:it.displayName,id:it.id,type:'dimmer'], title:it.displayName, description:description
                }
                settings.switches?.each() {
                    def buttonValue = "btn_${button}_${mode}_${it.id}"
                    def description = "Tap to edit. toggle:"+settings[buttonValue+'_toggle']+" level:"+settings[buttonValue+'_level']
                    href 'pageEditLight', params:[button:button,mode:mode,name:it.displayName,id:it.id,type:'switch'], title:it.displayName, description:description
                }
                settings.color?.each() {
                    def buttonValue = "btn_${button}_${mode}_${it.id}"
                    def description = "Tap to edit. toggle:"+settings[buttonValue+'_toggle']+" level:"+settings[buttonValue+'_level']+' hue:'+settings[buttonValue+'_hue']+' sat:'+settings[buttonValue+'_sat']
                    href 'pageEditLight', params:[button:button,mode:mode,name:it.displayName,id:it.id,type:'color'], title:it.displayName, description:description
                }
                settings.temp?.each() {
                    def buttonValue = "btn_${button}_${mode}_${it.id}"
                    def description = "Tap to edit. toggle:"+settings[buttonValue+'_toggle']+" level:"+settings[buttonValue+'_level']+' temp:'+settings[buttonValue+'_temp']
                    href 'pageEditLight', params:[button:button,mode:mode,name:it.displayName,id:it.id,type:'temperature'], title:it.displayName, description:description
                }
            }
        }
    }
}

private def pageEditLight(params) {
    LOG("pageEditLight()")
    def button = params.button.toInteger()
    LOG("bulb: ${button} ${params.mode} ${params.name} ${params.id} ${params.type}")

    def textHelp = "Configure bulb ${params.type}"

    def pageProperties = [
        name:       "pageEditLight",
        title:      "Configure ${params.name}",
        nextPage:   "pageSetup",
    ]


    return dynamicPage(pageProperties) {
        section() {
            paragraph textHelp
        }

        section("Light settings") {

            if (params.type == 'switch') {
                input "btn_${button}_${params.mode}_${params.id}_level", "enum", title:"${params.name} on/off",
                    metadata:[values: ["on", "off"]], required:false
            } else {
                input "btn_${button}_${params.mode}_${params.id}_level", "number", title:"${params.name} level", required:false
            }
            if (params.type == 'color') {
                input "btn_${button}_${params.mode}_${params.id}_hue", "number", title:"${params.name} hue", required:false
                input "btn_${button}_${params.mode}_${params.id}_sat", "number", title:"${params.name} saturation", required:false
            }
            if (params.type == 'temperature') {
                input "btn_${button}_${params.mode}_${params.id}_temp", "number", title:"${params.name} temperature", required:false
            }
            input "btn_${button}_${params.mode}_${params.id}_toggle", "enum", title:"${params.name} toggle",
                metadata:[values: ["on", "off"]], required:false

            href 'pageEditButton', params:[button:button], title:'Go Back', description:'Back to button edit page'
        }
    }
}

def installed() {
    state.installed = true
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def onButtonEvent(evt) {
    LOG("onButtonEvent(${evt.value})")

    if (!evt.data) {
        return
    }

    def slurper = new JsonSlurper()
    def data = slurper.parseText(evt.data)
    def button = data?.buttonNumber?.toInteger()
    if (!button) {
        log.error "cannot parse button data: ${data}"
        return
    }

    LOG("Button '${button}' was ${evt.value}.")

    def items = settings.findAll { it.key.startsWith("btn_${button}_${evt.value}") }

    if (!items) {
        LOG("Button '${button}' is not configured")
        return
    }

    def allSwitches = []
    if (settings.dimmers)
        allSwitches.addAll(settings.dimmers)
    if (settings.switches)
        allSwitches.addAll(settings.switches)
    if (settings.color)
        allSwitches.addAll(settings.color)
    if (settings.temp)
        allSwitches.addAll(settings.temp)

    items.each() { item ->
        if (item.key.endsWith('_routine')) {
            executeRoutine(item.value)
        } else if (item.key.endsWith('_mode')) {
            setMode(item.value)
        } else if (item.key.endsWith('_alarm')) {
            setAlarmMode(item.value)
        } else if (item.key.endsWith('_level')) {
            def dev = allSwitches.find { it.id == item.key.minus("btn_${button}_${evt.value}_").minus('_level') }
            if (dev != null) {
                def toggle = settings[item.key.minus('_level')+'_toggle']
                LOG("Device ${dev.displayName} found, dump: ${dev.currentSwitch} toggle: ${toggle}")
                if (toggle == 'on' && dev.currentSwitch == 'on') {
                    LOG("Toggling '${dev.displayName}' off")
                    dev.off()
                } else if (item.value == 'on') {
                    LOG("Turning '${dev.displayName}' on")
                    dev.on()
                } else if (item.value == 'off') {
                    LOG("Turning '${dev.displayName}' off")
                    dev.off()
                } else {
                    def value = item.value.toInteger()
                    def hue = settings[item.key.minus('_level')+'_hue']
                    def sat = settings[item.key.minus('_level')+'_sat']
                    def temp = settings[item.key.minus('_level')+'_temp']

                    if (value > 99) value = 99
             
                    if (hue != null) {
                        if (sat == null) {
                          sat=100
                        }
                        LOG("Setting '${dev.displayName}' hue/sat/level to ${hue}/${sat}/${value}")
                        def newValue = [hue: hue.toInteger(), saturation: sat.toInteger(), level: value as Integer ?: 100]
                        dev.setColor(newValue)
                    } else if (temp != null) { 
                        LOG("Setting '${dev.displayName}' temp/level to ${temp}/${value}")
                        dev.setColorTemperature(temp.toInteger())
                        dev.setLevel(value)
                    } else {
                        LOG("Setting '${dev.displayName}' level to ${value}")
                        dev.setLevel(value)
                    }
                }
            }
        }
    }
}

private def initialize() {
    log.info "Remote. Version ${version()}. ${copyright()}"
    LOG("initialize with ${settings}")

    if (settings.remotes) {
        subscribe(settings.remotes, "button", onButtonEvent)
    }

    LOG("state: ${state}")
}

private def getButtons() {
    LOG("getButtons()")

    def buttons = []
    (1..99).each() { btn ->
        def button = settings.find { it.key.startsWith("btn_${btn}_") }

        if (button) {
            buttons << btn.toInteger()
        }
    }

    if (buttons.size() > 1) {
        buttons = buttons.sort()
    }

    //log.debug "buttons: ${buttons}"
    return buttons
}

private def getRoutineNames() {
    def routines = location.helloHome?.getPhrases().collect() { it.label }
    return routines.sort()
}

private def getModeNames() {
    def modes = location.modes?.collect() { it.name }
    return modes.sort()
}

private def executeRoutine(name) {
    log.trace "Executing Routine \'${name}\'"
    location.helloHome.execute(name)
}

private def setMode(name) {
    log.trace "Setting location mode to \'${name}\'"
    setLocationMode(name)
}

private def setAlarmMode(name) {
    log.trace "Setting alarm system mode to \'${name}\'"

    def event = [
        name:           "alarmSystemStatus",
        value:          name,
        isStateChange:  true,
        displayed:      true,
        description:    "alarm system status is ${name}",
    ]

    sendLocationEvent(event)
}

private def version() {
    return "1.0.2"
}

private def copyright() {
    return "Copyright © 2015 Statusbits.com"
}

private def about() {
    def text =
        "This SmartApp allows using remote controls (e.g. Aeon Labs " +
        "Minimote) to execute routines, change modes and set the Smart " +
        "Home Monitor mode.\n\n" +
        "Version ${version()}\n${copyright()}\n\n" +
        "You can contribute to the development of this app by making a " +
        "PayPal donation to geko@statusbits.com. We appreciate your support."
}

private def LOG(message) {
    log.trace message
}
