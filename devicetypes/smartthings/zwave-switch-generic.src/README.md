# Z-wave Switch Generic

Cloud Execution

Works with: 

* [Leviton Appliance Module (DZPA1-1LW)](https://www.smartthings.com/works-with-smartthings/outlets/leviton-appliance-module)
* [GE Plug-In Outdoor Smart Switch (GE 12720) (Z-Wave)](https://www.smartthings.com/works-with-smartthings/outlets/ge-plug-in-outdoor-smart-switch)
* [Leviton Outlet (DZR15-1LZ)](https://www.smartthings.com/works-with-smartthings/outlets/leviton-outlet)
* [Leviton Switch (DZS15-1LZ)](https://www.smartthings.com/works-with-smartthings/switches-and-dimmers/leviton-switch)
* [Leviton 15A Switch (VRS15-1LZ)](https://www.smartthings.com/works-with-smartthings/lighting-and-switches/leviton-15a-switch)
* [Enerwave Duplex Receptacle (ZW15R)](https://www.smartthings.com/works-with-smartthings/outlets/enerwave-duplex-receptacle)
* [Enerwave On/Off Switch (ZW15S)](https://www.smartthings.com/works-with-smartthings/lighting-and-switches/enerwave-onoff-switch)

## Table of contents

* [Capabilities](#capabilities)
* [Health](#device-health)

## Capabilities

* **Actuator** - represents that a Device has commands
* **Health Check** - indicates ability to get device health notifications
* **Switch** - can detect state (possible values: on/off)
* **Polling** - represents that poll() can be implemented for the device
* **Refresh** - _refresh()_ command for status updates
* **Sensor** - detects sensor events

## Device Health

Leviton Appliance Module (DZPA1-1LW), GE Plug-In Outdoor Smart Switch (GE 12720), Leviton Outlet (DZR15-1LZ), Leviton Switch (DZS15-1LZ) (Z-Wave), Leviton Switch, Enerwave Duplex Receptacle (ZW15R) and Enerwave On/Off Switch (ZW15S) are polled by the hub.
As of hubCore version 0.14.38 the hub sends up reports every 15 minutes regardless of whether the state changed.
Device-Watch allows 2 check-in misses from device plus some lag time. So Check-in interval = (2*15 + 2)mins = 32 mins.
Not to mention after going OFFLINE when the device is plugged back in, it might take a considerable amount of time for
the device to appear as ONLINE again. This is because if this listening device does not respond to two poll requests in a row,
it is not polled for 5 minutes by the hub. This can delay up the process of being marked ONLINE by quite some time.

* __32min__ checkInterval

## Troubleshooting

If the device doesn't pair when trying from the SmartThings mobile app, it is possible that the device is out of range.
Pairing needs to be tried again by placing the device closer to the hub.
Instructions related to pairing, resetting and removing the device from SmartThings can be found in the following link:
* [Leviton Appliance Module (DZPA1-1LW) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)
* [GE Plug-In Outdoor Smart Switch (GE 12720) (Z-Wave) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/200903080-GE-Plug-In-Outdoor-Smart-Switch-GE-12720-Z-Wave-)
* [Leviton Outlet (DZR15-1LZ) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)
* [Leviton Switch (DZS15-1LZ) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)
* [Leviton 15A Switch (VRS15-1LZ) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/206171053-How-to-connect-Leviton-Z-Wave-devices)
* [Enerwave Duplex Receptacle (ZW15R) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204854176-How-to-connect-Enerwave-switches-and-dimmers)
* [Enerwave On/Off Switch (ZW15S) Troubleshooting Tips](https://support.smartthings.com/hc/en-us/articles/204854176-How-to-connect-Enerwave-switches-and-dimmers)