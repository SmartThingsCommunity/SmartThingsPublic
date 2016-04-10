/**
 *  Dim and Dimmer.
 *
 *  Create lighting scenes by setting dimmers to different levels depending on
 *  the routine. For more information, please visit
 *  <https://github.com/statusbits/smartthings/tree/master/DimAndDimmer>.
 *
 *  Version 1.0.0 (2014-12-21)
 *
 *  The latest version of this file can be found at:
 *  <https://github.com/statusbits/smartthings/tree/master/DimAndDimmer/DimAndDimmer.groovy>
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

definition(
    name: "Dim and Dimmer Routine",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Create lighting scenes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true
)

preferences {
    page name:"pageSetup"
    page name:"pageAbout"
    page name:"pageConfigure"
    page name:"pageEditHues"
}

// Show "Setup Menu" page
private def pageSetup() {
    TRACE("pageSetup()")

    if (state.installed == null) {
        // First run - initialize state
        state.installed = false
        return pageAbout()
    }

    def routines = getRoutineNames()

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
        name        : "hues",
        type        : "capability.colorControl",
        title       : "Select Color Bulbs",
        multiple:   true,
        required:   false
    ]

    def pageProperties = [
        name        : "pageSetup",
        title       : "Setup Menu",
        nextPage    : "pageConfigure",
        install     : false,
        uninstall   : state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            input "routines", "enum", title:"Select Routines", options:routines, required:true, multiple:true
            input inputDimmers
            input inputSwitches
            input inputHues
            href "pageAbout", title:"About", description:"Tap to open"
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "About" page
private def pageAbout() {
    TRACE("pageAbout()")

    def textAbout =
        "This smart app allows you to create lighting scenes by setting " +
        "dimmers to different levels depending on the routine."

    def pageProperties = [
        name        : "pageAbout",
        title       : "About",
        nextPage    : "pageSetup",
        install     : false,
        uninstall   : state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textAbout
            paragraph "${textVersion()}\n${textCopyright()}"
        }
        section("License") {
            paragraph textLicense()
        }
    }
}

private def getRoutineNames() {
    def routines = location.helloHome?.getPhrases().collect() { it.label }
    return routines.sort()
}

// Show "Configure Dimmers and Switches" setup page
private def pageConfigure() {
    TRACE("pageConfigure()")

    def textAbout =
        "Set desired dimming levels for each routine. Dimming values " +
        "are between 0 (off) and 99 (full brightness). If left blank, the " +
        "dimming level will not change when switching to this mode."

    def pageProperties = [
        name        : "pageConfigure",
        title       : "Configure Dimmers and Switches",
        nextPage    : null,
        install     : true,
        uninstall   : state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textAbout
        }

        settings.routines?.each() {
            def name = it as String
            section("${name} Routine", hideable:true, hidden:false) {
                name = name.tr(' !+', '___')
                settings.dimmers?.each() {
                    input "${it.id}_${name}", "number", title:it.displayName, required:false
                }
                settings.switches?.each() {
                    input "${it.id}_${name}", "enum", title:it.displayName,
                        metadata:[values: ["on", "off"]], required:false
                }
                settings.hues?.each() {
                    def description = "Tap to edit. level:" + settings[it.id+'_'+name]+' hue:'+settings[it.id+'_'+name+'_hue']+' sat:'+settings[it.id+'_'+name+'_sat']
                    href "pageEditHues", params:[routine:name,name:it.displayName,id:it.id], title:it.displayName, description:description
                }
            }
        }
    }
}

private def pageEditHues(params) {
    TRACE("pageEditHues()")
    TRACE("hues: ${params.routine} ${params.name} ${params.id}")

    def textHelp = "Configure color bulb hue/saturation/level"

    def pageProperties = [
        name:       "pageEditHues",
        title:      "Configure ${params.name}",
        nextPage:   "pageConfigure",
    ]


    return dynamicPage(pageProperties) {
        section() {
            paragraph textHelp
        }

        section("Bulb settings") {
            input "${params.id}_${params.routine}", "number", title:"${params.name} level", required:false
            input "${params.id}_${params.routine}_hue", "number", title:"${params.name} hue", required:false
            input "${params.id}_${params.routine}_sat", "number", title:"${params.name} saturation", required:false
        }
    }
}

def installed() {
    TRACE("installed()")

    state.installed = true
    initialize()
}

def updated() {
    TRACE("updated()")

    unsubscribe()
    initialize()
}

// Handle location event.
def onLocation(evt) {
    TRACE("onLocation(${evt.value})")

    String routine = evt.displayName

    def allSwitches = []
    if (settings.dimmers)
        allSwitches.addAll(settings.dimmers)
    if (settings.switches)
        allSwitches.addAll(settings.switches)
    if (settings.hues)
        allSwitches.addAll(settings.hues)

    allSwitches.each() {
        def name = "${it.id}_${routine.tr(' !+', '___')}"
        TRACE("name: ${name}")
        def value = settings[name]
        def hue = settings[name+'_hue']
        def sat = settings[name+'_sat']

        TRACE("value: ${value}")
        if (value != null) {
            if (value == 'on') {
                TRACE("Turning '${it.displayName}' on")
                it.on()
            } else if (value == 'off') {
                TRACE("Turning '${it.displayName}' off")
                it.off()
            } else {
                value = value.toInteger()
                if (value > 99) value = 99

                if (hue != null) {
                    if (sat == null) {
                      sat='100'
                    }
                    TRACE("Setting '${it.displayName}' hue/sat/level to ${hue}/${sat}/${value}")
                    def newValue = [hue: hue, saturation: sat, level: value as Integer ?: 100]
                    it.setColor(newValue)
                } else {
                    TRACE("Setting '${it.displayName}' level to ${value}")
                    it.setLevel(value)
                }
            }
        }
    }
}

private def initialize() {
    log.trace "${app.name}. ${textVersion()}. ${textCopyright()}"

    subscribe(location, "routineExecuted", onLocation)
    STATE()
}

private def textVersion() {
    def text = "Version 1.0.0"
}

private def textCopyright() {
    def text = "Copyright (c) 2014 Statusbits.com"
}

private def textLicense() {
    def text =
        "This program is free software: you can redistribute it and/or " +
        "modify it under the terms of the GNU General Public License as " +
        "published by the Free Software Foundation, either version 3 of " +
        "the License, or (at your option) any later version.\n\n" +
        "This program is distributed in the hope that it will be useful, " +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
        "General Public License for more details.\n\n" +
        "You should have received a copy of the GNU General Public License " +
        "along with this program. If not, see <http://www.gnu.org/licenses/>."
}

private def TRACE(message) {
    log.debug message
}

private def STATE() {
    log.trace "settings: ${settings}"
    log.trace "state: ${state}"
}
