# Hue Advanced (Connect)

## Features

- Groups
- Transition Time
- [Circadian Daylight] (https://github.com/claytonjn/SmartThingsPublic/tree/Circadian-Daylight) Integration
- Effects (colorloop)
- Colormode attribute
- Reachable attribute
- Alert
- **Full** Hue API Implementation
- Bridge Refresh
- Hue White Ambiance support
- Proper Color Temperature
- (Optional) **Update Notifications!**

## Installation

**Unfortunately, due to platform limitations, you _must_ remove all Hue bulbs and the Hue (Connect) service manager before attempting to use Hue Advanced (Connect)**

Although it appears to be more work to install through GitHub, this method allows for much easier updating of the SmartApp and Device Handlers in the future.

### With GitHub Integration

1. In the SmartThings IDE, under *My SmartApps* select *Settings*, then select *Add new repository*. Fill out the boxes as follows:

> **Owner:** claytonjn

> **Name:** SmartThingsPublic

> **Branch:** Hue-Advanced

1. (cont.) Select *Save*.
2. Select *Update from Repo*, then select *SmartThingsPublic (Hue-Advanced)*. Under *New (only in GitHub)* select *smartapps/claytonjn/hue-advanced-connect.src*, then select *Publish*, then select *Execute Update*.
3. In the SmartThings IDE, under *My Device Handlers* select *Update from Repo*, then select *SmartThingsPublic (Hue-Advanced)*. Under *New (only in GitHub)* select all of the devices, then select *Publish*, then select *Execute Update*.
4. In the SmartThings mobile app, under *Marketplace* select *SmartApps*, then select *My Apps*, then select *Hue Advanced (Connect)*. Follow the installation steps to add your Hue devices and preferences.

### Without GitHub Integration

1. In the SmartThings IDE, under *My SmartApps* select *New SmartApp*, then select *From Code*. Copy the entire contents of [hue-advanced-connect.groovy] (https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Hue-Advanced-Development/smartapps/claytonjn/hue-advanced-connect.src/hue-advanced-connect.groovy) and paste into the text box, then select *Create*.
2. In the SmartThings IDE, under *My Device Handlers* select *Create New Device Handler*, then select *From Code*. Copy the entire (RAW) contents of each .groovy file located under [/devicetypes/claytonjn/] (https://github.com/claytonjn/SmartThingsPublic/tree/Hue-Advanced-Development/devicetypes/claytonjn) and paste into the text box, then select *Create*. Repeat for each Device Handler.
3. In the SmartThings mobile app, under *Marketplace* select *SmartApps*, then select *My Apps*, then select *Hue Advanced (Connect)*. Follow the installation steps to add your Hue devices and preferences.
