/**
 *  Fibaro Wall Plug US child
 */
metadata {
    definition (name: "Fibaro Wall Plug USB", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Configuration"
        capability "Health Check"

        command "reset"
        command "refresh"
    }

    tiles (scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: 'USB', action: "", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/wallPlugUS/plugusb_on.png", backgroundColor: "#00a0dc"
            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("multiStatus", label:'${currentValue}')
            }
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'${currentValue}\nW', action:"refresh"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "energy", label:'${currentValue}\nkWh', action:"refresh"
        }
        valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "reset", label:'reset\nkWh', action:"reset"
        }
    }

    preferences {
        input ( name: "logging", title: "Logging", type: "boolean", required: false )
        input ( type: "paragraph", element: "paragraph", title: null, description: "This is a child device. If you're looking for parameters to set you'll find them in main component of this device." )
    }
}

def reset() {
    parent.childReset()
}

def refresh() {
    parent.childRefresh()
}