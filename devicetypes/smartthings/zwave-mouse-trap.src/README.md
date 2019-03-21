# Z-Wave Mouse Trap

Local Execution

* [Capabilities](#capabilities)
* [Health](#device-health)
* [Troubleshooting](#Troubleshooting)

## Capabilities

* **Sensor** - detects sensor events
* **Battery** - defines that the device has a battery
* **Configuration** - _configure()_ command called when device is installed or device preferences updated
* **Health Check** - indicates ability to get device health notifications
* **Refresh** - _refresh()_ command for status updates
* **Pest Control** - indicates ability to get mouse trap notifications

## Device Health

Z-Wave Mouse Trap is a Z-Wave sleepy device and checks in every 12 hours.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2 * 12 * 60 * 60 + 2 * 60) sek. = 1442 mins.

* __1442min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.