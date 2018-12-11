See: https://community.smartthings.com/t/mimo2-full-functionallity/90591/2

The published B-side devicehandler from Fortrezz wasn't working. This code is a bit sloppy, but not more than the original. Whatever the case, I get the contact/relay and voltage readings now. It needs my B-side DH and modified SmartApp (which publishes the right data to the B-side/sig2 DH as a child of the main DH/sig1).

Switch your B-side device to us this DH. Then select the A-side/sig1 from the SmartApp. This sets up the parent/child relationship and allows the second device to see the data that the first device is processing.

