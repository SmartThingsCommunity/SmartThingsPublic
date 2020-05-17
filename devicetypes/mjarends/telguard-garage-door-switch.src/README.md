Telguard GDC1 Device Type
==========================
Device type for controlling Telguard GDC1 garage door switch. The device type provides Switch, Door Control, Garage Door Control and Contact Sensor capabilities to the Telguard GDC1 within SmartThings.

## Installation

### Install Using GitHub Integration
Follow these steps (all within the SmartThings IDE):
- Click on the `My Device Types` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `mjarends`
  - Name: `SmartThingsPublic`
  - Branch: `master`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `telguard-garage-door-switch.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 devices and created 1 new devices, 1 published`"
- Verify that the two devices show up in the list and are marked with Status `Published`

## Use

The device type displays the current status of the garage door:

 * Open
 * Opening
 * Closed
 * Closing

It can also be used in SmartApps Smart Lights rules because it also shows up as a switch.

## Thanks

Thanks goes to:
  * johnconstantelo for sharing his MIMO lite code which helped me work through the issues with my device type. 
  * StrykerSKS for the well written installation instructions which I coped into my own README.md
