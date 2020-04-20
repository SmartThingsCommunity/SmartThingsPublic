/* Virtual Contact Sensor for alarm panel zones */



metadata {

    definition (name: "virtual contact sensor", namespace: "alarmdecoder", author: "scott@nutech.com") {

        capability "Contact Sensor"

        capability "Refresh"

    }



    // tile definitions

    tiles {

        standardTile("sensor", "device.contact", width: 2, height: 2, canChangeIcon: true) {

            state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"

            state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffffff"

        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {

            state "default", label:'Refresh', action:"device.refresh", icon: "st.secondary.refresh-icon"

        }



        main "sensor"

        details(["sensor", "refresh"])

    }

}