/**
 *  Copyright 2020 Jose Augusto Baranauskas
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * Notes:
 *  elevation ranges in [-90, +90] degrees
 *    azimuth ranges in [-180, +180] degress,
 *            positive values clockwise from north to south,
 *            negative values counterclockwise from north to south
 *    front follows compass convention, i.e. [0, 360] degrees clockwise from north
 *          front = 180 + azimuth
*/
metadata {
    definition (
       name: "SunCalc3",
//       version: "3.0",
       namespace: "baranauskas",
       author: "Jose Augusto Baranauskas",
       runLocally: true,
       minHubCoreVersion: '000.021.00001'
    ) {
        capability "Sensor"
        capability "Contact Sensor"   // sunrise <= sun <= sunset (sun is up)
        capability "Motion Sensor"    // dawn <= sun <= dusk
        capability "Refresh"

        // Sun Position
        attribute "altitude",   "number"
        attribute "azimuth",    "number"
        attribute "front",      "number"

        attribute "sun_up",     "string"
        attribute "sun_north",  "string"
        attribute "sun_west",   "string"
        attribute "sun_south",  "string"
        attribute "sun_east",   "string"
        attribute "lastUpdatedSunPosition", "string"

        // Sun Times
        attribute "nightEnd", "string"
        attribute "nauticalDawn", "string"
        attribute "dawn", "string"
        attribute "sunrise", "string"
        attribute "sunriseEnd", "string"
        attribute "goldenHourEnd", "string"
        attribute "midmorning", "string"
        attribute "noon", "string"
        attribute "midafternoon", "string"
        attribute "goldenHour", "string"
        attribute "sunsetStart", "string"
        attribute "sunset", "string"
        attribute "dusk", "string"
        attribute "nauticalDusk", "string"
        attribute "night", "string"
        attribute "nadir", "string"
        attribute "lastUpdatedSunTimes", "string"
        // Lengths
        attribute "daytime", "string"
        attribute "nighttime", "string"
        // Ratio
        attribute "dayNigthRatio", "string"

        // location
        attribute "latitude", "number"
        attribute "longitude", "number"
        attribute "timeZoneOffset", "string"
    }

    preferences{
        input("angleFromNorth", "number",
              title:"Home angle from north",
              description: "Angle (in degrees) of the home from real North, [0,+90] towards East, [0,-90] towards West. Defaults to zero (no correction).",
              range: "-90.0..+90.0",
              required: true,
              displayDuringSetup: true
        )
        input("angleOfIncidence", "number",
              title:"Sun angle of incidence",
              description: "Angle (in degrees) from which the incidence of the sun is considered at each cardinal region. Defaults to 45.",
              range: "0.0..45.0",
              required: true,
              displayDuringSetup: true
        )
    }

}

def parse(String description) {
    log.debug "parse(${description})"
}

def installed() {
    log.debug "installed() with ${settings}"
    updated()
}

def uninstalled() {
    log.debug "uninstalled()"
    unschedule()
}

def updated() {
    log.debug "updated() with ${settings}"

    unschedule()
    initialize()
    refresh()
    runEvery1Minute( refreshSunPosition )
    int ss = 60 * Math.random()
    def cronString = "${ss} 1 0 1/1 * ? *"
    log.debug "cron schedule: ${cronString}"
    schedule(cronString, refreshSunTimes )
}

def getSensorNames() {
  return ["SunNorth", "SunWest", "SunSouth", "SunEast"]
}

Map getSettings() {
  def afn = settings.angleFromNorth   ?: 0
  def ai  = settings.angleOfIncidence ?: 45
  return ["afn": afn, "aoi": aoi]
}

def initialize() {
    // create child
    // first check if child device is already installed
    def childDevices = getChildDevices()
    log.debug "Total of ${childDevices.size()}: ${childDevices}"
    //DeviceWrapper addChildDevice(String typeName, String deviceNetworkId, hubId, Map properties)
    if ( childDevices.size() == 0 ) {
      log.trace "Creating Child Sensors"
      def names = getSensorNames()
      try {
        names.each { name ->
            addChildDevice("Empty Contact Sensor",
                           "${device.deviceNetworkId}-${name}",
                           device.hub.id,
                           [completedSetup: true,
                            label: "${name}",
                            isComponent: false,
                            componentLabel: "${name}"
                           ]
            )
        }
      } catch(Exception e) {
        log.debug "Error: ${e}"
      }
      log.trace "Child created"
    }
    else {
      log.trace "Child already exists"
    }
}

def refresh() {
    log.debug "refresh()"

    refreshSunPosition()
    refreshSunTimes()
    refreshLocation()
}

def refreshSunPosition() {
    log.debug "refreshSunPosition()"

    def date = new Date()
    def lat  = location.latitude
    def lng  = location.longitude
    def p    = getPosition( date, lat, lng )
    log.debug "SunPosition: ${p}"

    def s = getSettings()
    def front = remainder( (360 + 180 - p.azimuth + s.afn), 360 )
    def sun_up    = (p.altitude > -0.833)
    def sun_north = sun_up && ((360.0 - s.aoi) <= front || front <  s.aoi)
    def sun_west  = sun_up && (s.aoi <= front && front < (180.0 - s.aoi))
    def sun_south = sun_up && ((180.0 - s.aoi) <= front && front < (270.0 - s.aoi))
    def sun_east  = sun_up && ((270.0 - s.aoi) <= front && front < (360.0 - s.aoi))

    sendEvent(name: "altitude",       value: p.altitude )
    sendEvent(name: "azimuth",        value: p.azimuth )
    sendEvent(name: "front",          value: front )
    sendEvent(name: "sun_north",      value: sun_north )
    sendEvent(name: "sun_west",       value: sun_west )
    sendEvent(name: "sun_south",      value: sun_south )
    sendEvent(name: "sun_east",       value: sun_east )
    sendEvent(name: "sun_up",         value: sun_up )
    sendEvent(name: "contact",        value: (sun_up ? "open" : "closed") )
    sendEvent(name: "motion",         value: (p.altitude > -6 ? "active" : "inactive") )
    sendEvent(name: "lastUpdatedSunPosition", value: formatDate( date ) )

    def sunSensors = [
        "SunNorth": (sun_north  ? "open" : "closed"),
        "SunWest":  (sun_west   ? "open" : "closed"),
        "SunSouth": (sun_south  ? "open" : "closed"),
        "SunEast":  (sun_east   ? "open" : "closed")
    ]
    def childDevices = getChildDevices()
    def child
    sunSensors.each { name, value ->
      child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${name}" }
      if( child ) {
        log.debug "sendEvent ${value} to device ${child}"
        child.sendEvent( "name": "contact", "value": value )
      }
    }
}

def refreshSunTimes() {
    log.debug "refreshSunTimes()"

    def date = new Date()
    def lat  = location.latitude
    def lng  = location.longitude
    def t    = getTimes( date, lat, lng )
    log.debug "SunTimes: ${t}"

    t = formatDates( t )
    t.each { key, value ->
      sendEvent(name: key,           value: value  )
    }

/*    sendEvent(name: "sunrise",        value: t.sunrise )
    sendEvent(name: "sunset",         value: t.sunset )
    sendEvent(name: "sunriseEnd",     value: t.sunriseEnd )
    sendEvent(name: "sunsetStart",    value: t.sunsetStart )
    sendEvent(name: "dawn",           value: t.dawn )
    sendEvent(name: "dusk",           value: t.dusk )
    sendEvent(name: "nauticalDawn",   value: t.nauticalDawn )
    sendEvent(name: "nauticalDusk",   value: t.nauticalDusk )
    sendEvent(name: "night",          value: t.night )
    sendEvent(name: "nightEnd",       value: t.nightEnd )
    sendEvent(name: "goldenHourEnd",  value: t.goldenHourEnd )
    sendEvent(name: "goldenHour",     value: t.goldenHour )
    sendEvent(name: "noon",           value: t.noon )
    sendEvent(name: "nadir",          value: t.nadir )
    sendEvent(name: "midmorning",     value: t.midmorning )
    sendEvent(name: "midafternoon",   value: t.midafternoon )
    sendEvent(name: "daytime",        value: t.daytime )
    sendEvent(name: "nighttime",      value: t.nighttime )
*/
    sendEvent(name: "lastUpdatedSunTimes", value: formatDate( date ) )
}

def refreshLocation() {
    log.debug "refreshLocation()"

    sendEvent(name: "latitude", value: location.latitude )
    sendEvent(name: "longitude", value: location.longitude )
    sendEvent(name: "timeZoneOffset", value: location.timeZone )
}


def formatDate( date ) {
  return date.format("yyyy-MM-dd HH:mm:ssZ", location.timeZone )
}

def formatDates ( dates ) {
  def result = [:]
  dates.each { key, value ->
      result[key] = formatDate( value )
  }
  return result
}

// Computes remainder using doubles a and b (a % b)
def remainder( double a, double b ) {
  // Handling negative values
  long sign = (a < 0) ? -1 : 1
  a = Math.abs( a )
  b = Math.abs( b )

  long   q = a / b
  double r = a - q * b
  return ( sign * r )
}
//--------------------------------------------------------------------------------
// SunCalc
//--------------------------------------------------------------------------------
// sun calculations are based on http://aa.quae.nl/en/reken/zonpositie.html formulas

// shortcuts for easier to read formulas
def PI()       { return Math.PI }
def rad()      { return Math.PI / 180 }
def sin(x)     { return Math.sin(x) }
def asin(x)    { return Math.asin(x) }
def cos(x)     { return Math.cos(x) }
def acos(x)    { return Math.acos(x) }
def tan(x)     { return Math.tan(x) }
def atan(y, x) { return Math.atan2(y, x) }

// date/time constants and conversions
//dayMs = 1000 * 60 * 60 * 24
//J1970 = 2440588
//J2000 = 2451545
def dayMs() { return 1000 * 60 * 60 * 24 }
def J1970() { 2440588 }
def J2000() { 2451545 }

//def toJulian(date) { return date.valueOf() / dayMs() - 0.5 + J1970(); }
def toJulian(date) { return date.getTime() / dayMs() - 0.5 + J1970() }
//def fromJulian(j)  { return new Date((j + 0.5 - J1970()) * dayMs()) }
def fromJulian(j)  { return new Date( (long) ((j + 0.5 - J1970()) * dayMs()) ) }
def toDays(date)   { return toJulian(date) - J2000() }

// general calculations for position
//e = rad() * 23.4397; // obliquity of the Earth
def e() { rad() * 23.4397 } // obliquity of the Earth

def rightAscension(l, b) { return atan(sin(l) * cos(e()) - tan(b) * sin(e()), cos(l)); }
def declination(l, b)    { return asin(sin(b) * cos(e()) + cos(b) * sin(e()) * sin(l)); }

def azimuth(H, phi, dec)  { return atan(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi)); }
def altitude(H, phi, dec) { return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H)); }

def siderealTime(d, lw) { return rad() * (280.16 + 360.9856235 * d) - lw; }

def astroRefraction(h) {
    if (h < 0) // the following formula works for positive altitudes only.
        h = 0; // if h = -0.08901179 a div/0 would occur.

    // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
    // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
    return 0.0002967 / Math.tan(h + 0.00312536 / (h + 0.08901179));
}

// general sun calculations
def solarMeanAnomaly(d) { return rad() * (357.5291 + 0.98560028 * d) }

def eclipticLongitude(M) {
    def C = rad() * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M)) // equation of center
    def P = rad() * 102.9372 // perihelion of the Earth
    return M + C + P + PI()
}

def sunCoords(d) {
    def M = solarMeanAnomaly(d)
    def L = eclipticLongitude(M)
    return [
        dec: declination(L, 0),
        ra: rightAscension(L, 0)
    ]
}

// calculates sun position for a given date and latitude/longitude
def getPosition(date, lat, lng) {

    def lw  = rad() * -lng
    def phi = rad() * lat
    def d   = toDays(date)
    def c   = sunCoords(d)
    def H   = siderealTime(d, lw) - c.ra
    def result = [
        azimuth:  azimuth(H, phi, c.dec) / rad(),
        altitude: altitude(H, phi, c.dec) / rad()
    ]
    return result
}
//-----------------------------------------------------------------------
// sun times configuration (angle, morning name, evening name)
def times() {
  return [
    [-0.833, 'sunrise',       'sunset'      ],
    [  -0.3, 'sunriseEnd',    'sunsetStart' ],
    [    -6, 'dawn',          'dusk'        ],
    [   -12, 'nauticalDawn',  'nauticalDusk'],
    [   -18, 'nightEnd',      'night'       ],
    [     6, 'goldenHourEnd', 'goldenHour'  ]
  ]
}

// adds a custom time to the times config
def addTime(angle, riseName, setName) {
    return times().push( [angle, riseName, setName] )
};

// calculations for sun times
//def J0 = 0.0009;
def J0() { return 0.0009 }

def julianCycle(d, lw) { return Math.round(d - J0() - lw / (2 * PI())) }

def approxTransit(Ht, lw, n) { return J0() + (Ht + lw) / (2 * PI()) + n }
def solarTransitJ(ds, M, L)  { return J2000() + ds + 0.0053 * sin(M) - 0.0069 * sin(2 * L) }

def hourAngle(h, phi, d) { return acos((sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d))) }
def observerAngle(height) { return -2.076 * Math.sqrt(height) / 60 }

// returns set time for the given sun altitude
def getSetJ(h, lw, phi, dec, n, M, L) {
    def w = hourAngle(h, phi, dec)
    def a = approxTransit(w, lw, n)
    return solarTransitJ(a, M, L)
}

// calculates sun times for a given date, latitude/longitude, and, optionally,
// the observer height (in meters) relative to the horizon
def getTimes(date, lat, lng, height=0) {
    height = height ?: 0
    def lw = rad() * -lng
    def phi = rad() * lat

    def dh = observerAngle(height)

    def d = toDays(date)
    def n = julianCycle(d, lw)
    def ds = approxTransit(0, lw, n)

    def M = solarMeanAnomaly(ds)
    def L = eclipticLongitude(M)
    def dec = declination(L, 0)

    def Jnoon = solarTransitJ(ds, M, L)

    def    i, len, time, h0, Jset, Jrise

    def result = [
        "noon" : fromJulian(Jnoon),
        "nadir": fromJulian(Jnoon - 0.5)
    ]
    def times = times()
    len = times.size()
    for (i = 0; i < len; i += 1) {
        time = times[i];
        h0 = (time[0] + dh) * rad();

        Jset = getSetJ(h0, lw, phi, dec, n, M, L);
        Jrise = Jnoon - (Jset - Jnoon);

        result[time[1]] = fromJulian(Jrise);
        result[time[2]] = fromJulian(Jset);
    }
    def jr = toJulian( result.sunrise )
    def js = toJulian( result.sunset )
    def jn = toJulian( result.noon )
    def jm = new Date( result.sunset.getTime() ).clearTime()
    jm = toJulian( jm )
    result << ["midmorning"   : fromJulian( (jr + jn) / 2 ),
               "midafternoon" : fromJulian( (jn + js) / 2 ),
               "daytime"    : fromJulian( jm + (js - jr) ),
               "nighttime"  : fromJulian( jm + 1.000011574074074 - (js - jr) )
              ]
    return result
}
