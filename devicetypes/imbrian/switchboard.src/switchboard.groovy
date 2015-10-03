/**
 * Copyright (c) 2014 brian@bevey.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/**
 * Switchboard Interface
 *
 * Author: brian@bevey.org and markewest@gmail.com
 * Date: 2014-5-6
 *
 * Used in conjunction with Switchboard, this allows you to control properly
 * configured devices from SmartThings.
 */

metadata {
  definition (name: "Switchboard", namespace: "imbrian", author: "Brian J.") {
    capability "Sensor"

    command "sam_poweroff"
    command "sam_up"
    command "sam_down"
    command "sam_left"
    command "sam_enter"
    command "sam_right"
    command "sam_chup"
    command "sam_chdown"
    command "sam_volup"
    command "sam_voldown"
    command "sam_mute"
    command "sam_smarthub"
    command "sam_return"
    command "sam_link"

    command "ps3_up"
    command "ps3_down"
    command "ps3_left"
    command "ps3_right"
    command "ps3_poweron"
    command "ps3_ps"
    command "ps3_select"
    command "ps3_start"
    command "ps3_triangle"
    command "ps3_square"
    command "ps3_circle"
    command "ps3_cross"
  }

  tiles {
    // SAMSUNG
    standardTile("sam_poweroff", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_poweroff", label: "Power Off", action: "sam_poweroff", icon: ""
    }

    standardTile("sam_up",       "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_up", label: "up", action: "sam_up", icon: ""
    }

    standardTile("sam_smarthub", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_smarthub", label: "SmartHub", action: "sam_smarthub", icon: ""
    }

    standardTile("sam_left",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_left", label: "left", action: "sam_left", icon: ""
    }

    standardTile("sam_enter",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_enter", label: "Enter", action: "sam_enter", icon: ""
    }

    standardTile("sam_right",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_right", label: "right", action: "sam_right", icon: ""
    }

    standardTile("sam_chdown",   "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_chdown", label: "Ch Down", action: "sam_chdown", icon: ""
    }

    standardTile("sam_down",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_down", label: "down", action: "sam_down", icon: ""
    }

    standardTile("sam_chup",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_chup", label: "Ch Up", action: "sam_chup", icon: ""
    }

    standardTile("sam_mute",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_mute", label: "Mute", action: "sam_mute", icon: ""
    }

    standardTile("sam_voldown",  "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_voldown", label: "Vol Down", action: "sam_voldown", icon: ""
    }

    standardTile("sam_volup",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_volup", label: "Vol Up", action: "sam_volup", icon: ""
    }

    standardTile("sam_return",   "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_return", label: "Return", action: "sam_return", icon: ""
    }

    standardTile("sam_link",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "sam_link", label: "Link", action: "sam_link", icon: ""
    }

    // PS3
    standardTile("ps3_poweron",  "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_poweron", label: "Power On", action: "ps3_poweron", icon: ""
    }

    standardTile("ps3_up",       "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_up", label: "up", action: "ps3_up", icon: ""
    }

    standardTile("ps3_ps",       "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_ps", label: "PS", action: "ps3_ps", icon: ""
    }

    standardTile("ps3_left",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_left", label: "left", action: "ps3_left", icon: ""
    }

    standardTile("ps3_cross",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_cross", label: "Cross", action: "ps3_cross", icon: ""
    }

    standardTile("ps3_right",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_right", label: "right", action: "ps3_right", icon: ""
    }

    standardTile("ps3_select",   "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_select", label: "Select", action: "ps3_select", icon: ""
    }

    standardTile("ps3_down",     "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_down", label: "down", action: "ps3_down", icon: ""
    }

    standardTile("ps3_start",    "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_start", label: "Start", action: "ps3_start", icon: ""
    }

    standardTile("ps3_triangle", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_triangle", label: "Triangle", action: "ps3_triangle", icon: ""
    }

    standardTile("ps3_circle",   "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_circle", label: "Circle", action: "ps3_circle", icon: ""
    }

    standardTile("ps3_square",   "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "ps3_square", label: "Square", action: "ps3_square", icon: ""
    }

    standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "pause", label: "", action: "pause", icon: ""
    }

    main "sam_poweroff"

    details(["sam_poweroff", "sam_up", "sam_smarthub", "sam_left", "sam_enter", "sam_right", "sam_chdown", "sam_down", "sam_chup", "sam_mute", "sam_voldown", "sam_volup", "sam_return", "sam_link", "blank", "ps3_poweron", "ps3_up", "ps3_ps", "ps3_left", "ps3_cross", "ps3_right", "ps3_select", "ps3_down", "ps3_start", "ps3_triangle", "ps3_circle", "ps3_square"])
  }
}

// SAMSUNG
def sam_powerof() {
  api("samsung", "POWEROFF")
}

def sam_up() {
  api("samsung", "UP")
}

def sam_smarthub() {
  api("samsung", "CONTENTS")
}

def sam_left() {
  api("samsung", "LEFT")
}

def sam_enter() {
  api("samsung", "ENTER")
}

def sam_right() {
  api("samsung", "RIGHT")
}

def sam_chdown() {
  api("samsung", "CHDOWN")
}

def sam_down() {
  api("samsung", "DOWN")
}

def sam_chup() {
  api("samsung", "CHUP")
}

def sam_mute() {
  api("samsung", "MUTE")
}

def sam_voldown() {
  api("samsung", "VOLDOWN")
}

def sam_volup() {
  api("samsung", "VOLUP")
}

def sam_return() {
  api("samsung", "RETURN")
}

def sam_link() {
  api("samsung", "HDMI4,DOWN,ENTER")
}

// PS3
def ps3_poweron() {
  api("ps3", "PowerOn")
}

def ps3_up() {
  api("ps3", "Up")
}

def ps3_ps() {
  api("ps3", "PS")
}

def ps3_left() {
  api("ps3", "Left")
}

def ps3_cross() {
  api("ps3", "Cross")
}

def ps3_right() {
  api("ps3", "Right")
}

def ps3_select() {
  api("ps3", "Select")
}

def ps3_down() {
  api("ps3", "Down")
}

def ps3_start() {
  api("ps3", "Start")
}

def ps3_triangle() {
  api("ps3", "Triangle")
}

def ps3_circle() {
  api("ps3", "Circle")
}

def ps3_square() {
  api("ps3", "Square")
}

private api(device, command) {
  log.debug("Executing ${command}")

  def uri = "/?${device}=${command}"

  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: [HOST:getHostAddress()]
  )

  hubAction
}

//helper methods
private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
  def parts = device.deviceNetworkId.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
}
