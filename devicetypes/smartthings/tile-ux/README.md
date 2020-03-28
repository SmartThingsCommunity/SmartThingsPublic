# Device Tiles Examples and Reference

This package contains examples of Device tiles, organized by tile type.

## Purpose

Each Device Handler shows example usages of a specific tile, and is meant to represent the variety of permutations that a tile can be configured.

The various tiles can be used by QA to test tiles on all supported mobile devices, and by developers as a reference implementation.

## Installation

1. Self-publish the Device Handlers in this package.
2. Self-publish the Device Tile Controller SmartApp. The SmartApp can be found [here](https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/smartapps/smartthings/tile-ux/device-tile-controller.src/device-tile-controller.groovy).
3. Install the SmartApp from the Marketplace, under "My Apps".
4. Select the simulated devices you want to install and press "Done".

The simulated devices can then be found in the "Things" view of "My Home" in the mobile app.
You may wish to create a new room for these simulated devices for easy access.

## Usage

Each simulated device can be interacted with like other devices.
You can use the mobile app to interact with the tiles to see how they look and behave.

## Troubleshooting

If you get an error when installing the simulated devices using the controller SmartApp, ensure that you have published all the Device Handlers for yourself.
Also check live logging to see if there is a specific tile that is causing installation issues.

## FAQ

*Question: A tile isn't behaving as expected. What should I do?*

QA should create a JIRA ticket for any issues or inconsistencies of tiles across devices.

Developers may file a support ticket, and reference the specific tile and issue observed.

*Question: I'd like to contribute an example tile usage that would be helpful for testing and reference purposes. Can I do that?*

We recommend that you open an issue in the SmartThingsPublic repository describing the example tile and usage.
That way we can discuss with you the proposed change, and then if appropriate you can create a PR associated to the issue.
