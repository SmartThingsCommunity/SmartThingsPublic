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
 * This device handler implements solar position sensors as well as
 * time intervals over day and night events.
 *
 * Requires "SunCalc Contact Sensor" DH for child sensors
 *
 * The parent sensors are:
 *     Contact Sensor:      sunrise <= sun <= sunset (sun is up)
 *     Motion Sensor:       dawn <= sun <= dusk
 *     Acceleration Sensor: night <= sun <= nightEnd
 *     Infrared Level:      dayNigthRatio
 *
 * Child Contact sensors are:
 *     Sun Position (contact opens in the position)
 *         SunNorth, SunSouth, SunEast, SunWest
 *         please refer to settings angleFromNorth and angleOfIncidence
 *         for ideal configuration for your home for these sensors
 *
 *     Sun Time Interval (contact opens in the interval)
 *          SunEarlyMorning:   sunrise <= sun <= midmorning
 *          SunLateMorning:    midmorning <= sun <= noon
 *          SunEarlyAfternoon: noon <= sun <= midafternoon
 *          SunLateAfternoon:  midafternoon <= sun <= sunset
 *          SunEarlyNight:     sunset <= sun <= nadir (solar midnight)
 *          SunLateNight:      nadir <= sun <= sunrise
 *
 * Note: Please read remarks about attributes near theirs definitions.
 */
metadata {
    definition (
       name: "SunCalc",
       description: "Create Sun sensors",
       version: "3.86 (2021-02-25)",
       namespace: "baranauskas",
       author: "Jose Augusto Baranauskas",
       runLocally: true,
       minHubCoreVersion: '000.021.00001'
    ) {
        capability "Sensor"
        capability "Refresh"

        // sunrise <= sun <= sunset (sun is up)
        capability "Contact Sensor"

        // dawn <= sun <= dusk
        capability "Motion Sensor"

        // night <= sun <= nightEnd
        capability "Acceleration Sensor"

        // dayNigthRatio
        capability "Infrared Level"

        // Sun Position
        // altitude (elevation): in the range [-90, +90] degrees
        attribute "altitude",               "number"

        // azimuth: in the range [-180, +180] degress
        //          positive values are clockwise from north to south
        //          negative values are counterclockwise from north to south
        attribute "azimuth",                "number"

        // Home angles
        // angleFromNorth is the angle (in degrees) of the home from real North
        //                [0,+90] towards East
        //                [0,-90] towards West
        // Use non-zero values only if you want to make some adjustments to facilitate
        // your orientation towards your home with cardinal points
        // Otherwise, use zero for compass values (no correction)
        // Copy from the respective settings value
        attribute "angleFromNorth",         "number"

        // angleOfIncidence is the angle (in degrees) from which the incidence
        // of the sun is considered at each cardinal region
        // For example, assume that the sun is moving towards the west face of your home.
        // If you use 45 degrees then only when the sun reaches 45 degrees
        // or more on the west face will the respective sensor indicate the incidence
        // of sun on that face of your home.
        // Copy from the respective settings value
        attribute "angleOfIncidence",       "number"

        // azimuthCompass: in the range [0, +360] degress (compass convention values)
        //        clockwise from north
        // azimuthCompass  considers azimuth adjustment using angleFromNorth
        // azimuthCompass  simplified formula is (180 - azimuth) + angleFromNorth
        //        negative angles are converted to positive ones using
        //        compass arithmetic
        attribute "azimuthCompass",         "number"

        // booleans indicating sun is up and inciding on each home side
        attribute "sun_up",                 "string"
        // Each sensor of the four cardinal points takes into account the angleOfIncidence
        attribute "sun_north",              "string"
        attribute "sun_west",               "string"
        attribute "sun_south",              "string"
        attribute "sun_east",               "string"

        // Datetime from last sun position update
        attribute "lastUpdatedSunPosition", "string"

        // Sun datetimes
        attribute "nightEnd",               "string"
        attribute "nauticalDawn",           "string"
        attribute "dawn",                   "string"
        attribute "sunrise",                "string"
        attribute "sunriseEnd",             "string"
        attribute "goldenHourEnd",          "string"
        attribute "midmorning",             "string"
        attribute "noon",                   "string"
        attribute "midafternoon",           "string"
        attribute "goldenHour",             "string"
        attribute "sunsetStart",            "string"
        attribute "sunset",                 "string"
        attribute "dusk",                   "string"
        attribute "nauticalDusk",           "string"
        attribute "night",                  "string"
        attribute "nadir",                  "string"

        // Lengths
        attribute "daytime",                "string"
        attribute "nighttime",              "string"

        // Ratio daytime / nighttime
        attribute "dayNigthRatio",          "number"

        // Datetime from last sun times update
        attribute "lastUpdatedSunTimes",    "string"

        // location
        attribute "latitude",               "number"
        attribute "longitude",              "number"
        attribute "timeZoneOffset",         "string"
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

    // Schedules
    runEvery1Minute( refreshSunPosition )
    int ss = 1 + 58 * Math.random()
    def cronString = "${ss} 0 0 1/1 * ? *"
    log.debug "cron schedule: ${cronString}"
    schedule( cronString, refreshSunTimes )
}

def refresh() {
    log.debug "refresh()"

    refreshSunTimes()
    refreshSunPosition()
    refreshLocation()
}

def getSensorNames() {
  return ["SunNorth", "SunWest", "SunSouth", "SunEast",
          "SunEarlyMorning", "SunLateMorning",
          "SunEarlyAfternoon", "SunLateAfternoon",
          "SunEarlyNight", "SunLateNight"
         ]
}

def initialize() {
    // create child
    // first check if child device is already installed
    def childDevices = getChildDevices()
    log.info "Child found ${childDevices.size()}: ${childDevices}"
    def names = getSensorNames()
    if ( childDevices.size() != names.size() ) {
      def child
      def added = []
      log.info "Creating Child Sensors"
      try {
        names.each { name ->
            child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${name}" }
            if( ! child ) {
              child = addChildDevice("SunCalc Contact Sensor",
                      "${device.deviceNetworkId}-${name}",
                      device.hub.id,
                      [completedSetup: true, label: "${name}",
                       isComponent: false,   componentLabel: "${name}"
                      ]
              )
              added << child
            }
        }
      }
      catch(Exception e) {
        log.error "Error creating child device: ${e}"
      }
      log.info "Child created: ${added}"
    }
    else {
      log.info "Child already exists"
    }
}

// Do not use/define getSettings() since it seems to be ST reserved
def getValidatedSettings() {
  return  ["angleFromNorth":   (settings.angleFromNorth   ?: 0),
           "angleOfIncidence": (settings.angleOfIncidence ?: 45)
          ]
}

def refreshSunPosition() {
    log.debug "refreshSunPosition()"

    Map attr = [:]
    def date = new Date()
    attr.lastUpdatedSunPosition = formatDate( new Date(date.getTime()) )
    def lat  = location.latitude
    def lng  = location.longitude
    def p    = getPosition( date, lat, lng )
    attr << p
    log.info "SunPosition: ${p}"

    // update sun position from home
    def s = getValidatedSettings()
    attr << s
    log.info "Angles = ${s}"

    def afn       = s.angleFromNorth
    def aoi       = s.angleOfIncidence
    def azc       = compassArithmetic( 180 - p.azimuth + afn )

    def t = getCurrentTimes( attr )
    //def sun_up    = (p.altitude >= -0.833)
    def sun_up        = (t.sunrise <= t.now && t.now <= t.sunset)
    def dawnDusk      = (t.dawn    <= t.now && t.now <= t.dusk)
    def nightNightEnd = (t.night   <= t.now || t.now <= t.nightEnd)

    def sun_north = sun_up && ((270.0 + aoi) <= azc || azc < ( 90.0 - aoi))
    def sun_west  = sun_up && ((  0.0 + aoi) <= azc && azc < (180.0 - aoi))
    def sun_south = sun_up && (( 90.0 + aoi) <= azc && azc < (270.0 - aoi))
    def sun_east  = sun_up && ((180.0 + aoi) <= azc && azc < (360.0 - aoi))
    attr << [
         azimuthCompass: azc,
         sun_up: sun_up,
         sun_north: sun_north,
         sun_west: sun_west,
         sun_south: sun_south,
         sun_east: sun_east
    ]

/*    Map parentSensors = [
        contact:      boolContact( attr.sun_up ),
        motion:       boolMotion( p.altitude >= -6 ),
        acceleration: boolAcceleration( p.altitude >= -18 )
    ]
*/
    Map parentSensors = [
        contact:      boolContact( attr.sun_up ),
        motion:       boolMotion( p.altitude >= -6 ),
        acceleration: boolAcceleration( p.altitude >= -18 )
    ]

    //log.debug "attr[${attr.size()}]: ${attr}"
    sendEvents( attr )
    sendEvents( parentSensors )
    def childSensors = childSensorValues( attr )
    sendEventsChild( childSensors )
}

def boolContact( boolean b )       { return ( b ? "open" : "closed") }
def boolMotion( boolean b )        { return ( b ? "active" : "inactive") }
def boolAcceleration( boolean b )  { return ( b ? "active" : "inactive") }
def sendEvents( Map e ) {
    e.each { key, value ->
      sendEvent( name: key, value: value )
    }
}
// get "HH:mm" from datetime "yyyy-MM-dd HH:mm:ss-0300" as string
def getTimeFromDate( String date ){
    return date.substring( 11, 16 )
}

def getCurrentTimes( Map attr ) {
  // Datetime as string
  def t = [
      nightEnd:     device.currentValue("nightEnd"),
      dawn:         device.currentValue("dawn"),
      sunrise:      device.currentValue("sunrise"),
      midmorning:   device.currentValue("midmorning"),
      noon:         device.currentValue("noon"),
      midafternoon: device.currentValue("midafternoon"),
      sunset:       device.currentValue("sunset"),
      dusk:         device.currentValue("dusk"),
      night:        device.currentValue("night"),
      nadir:        device.currentValue("nadir"),
      now:          attr.lastUpdatedSunPosition
  ]
  t.each { key, value ->
    t[ key ] = getTimeFromDate( value )
  }
  return t
}

def childSensorValues( Map attr ) {
  // Datetime as string
  def t = [
      sunrise:       device.currentValue("sunrise"),
      midmorning:    device.currentValue("midmorning"),
      noon:          device.currentValue("noon"),
      midafternoon:  device.currentValue("midafternoon"),
      sunset:        device.currentValue("sunset"),
      nadir:         device.currentValue("nadir"),
      now:           attr.lastUpdatedSunPosition
  ]
  t.each { key, value ->
    t[ key ] = getTimeFromDate( value )
  }
  //log.debug "Times as string: ${t}"
  def childSensors = [
      SunNorth:          attr.sun_north,
      SunWest:           attr.sun_west,
      SunSouth:          attr.sun_south,
      SunEast:           attr.sun_east,
      SunEarlyMorning:   (t.sunrise      <= t.now && t.now <= t.midmorning),
      SunLateMorning:    (t.midmorning   <= t.now && t.now <= t.noon),
      SunEarlyAfternoon: (t.noon         <= t.now && t.now <= t.midafternoon),
      SunLateAfternoon:  (t.midafternoon <= t.now && t.now <= t.sunset),
      SunEarlyNight:     (t.sunset       <= t.now || t.now <= t.nadir),
      SunLateNight:      (t.nadir        <= t.now && t.now <= t.sunrise)
  ]
  return childSensors
}

def sendEventsChild( Map sensors ) {
  def childDevices = getChildDevices()
  def child, contactValue
  def contacts = [:]
  sensors.each { name, value ->
    child = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${name}" }
    if( child ) {
      contactValue = boolContact( value )
      contacts << [ "${child}":  contactValue ]
      // sensors
      child.sendEvent( name: "contact", value: contactValue )
    }
  }
  log.debug "sendEvent to child: ${contacts}"
}

def refreshSunTimes() {
    log.debug "refreshSunTimes()"

    def date = new Date()
    // Remove timezone from date (but keep time local)
    def today = new Date( date.getTime() ).format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    today = new Date().parse("yyyy-MM-dd HH:mm:ss", today)

    def todayMidnight = new Date( today.getTime() ).clearTime()
    def todayNoon     = new Date( todayMidnight.getTime() + 12*60*60*1000 )
    //log.debug "date: ${date}, today: ${today}, todayMidnight: ${todayMidnight}, todayNoon: ${todayNoon}"

    def lat  = location.latitude
    def lng  = location.longitude

    def t    = getTimes( todayNoon, lat, lng )
    log.debug "SunTimes: ${t}"
    def tf = formatDates( t )
    sendEvents( tf )

    def l = getLenghts( t )
    log.debug "Lengths = ${l}"
    // Do not include timezone in lengths
    def lf = formatDates( l, false )
    sendEvents( lf )

    def r = getRatios( l )
    sendEvents( r )
    log.debug "Ratios = ${r}"

    // Parent sensors
    sendEvent(name: "infraredLevel", value: (100.0 * r.dayNigthRatio) )
    //attr
    sendEvent(name: "lastUpdatedSunTimes",
              value: formatDate( new Date(date.getTime() ) )
    )
}

def getLenghts( t ) {
  def tr = t.sunrise.getTime()
  def ts = t.sunset.getTime()
  def tm = new Date( ts ).clearTime().getTime()
  def result = ["daytime"      : new Date( tm + (ts - tr) ),
                "nighttime"    : new Date( tm + (24*60*60*1000+1000) - (ts - tr) )
  ]
  return result
}

def getRatios( l ) {
  def dt = l.daytime.getTime()
  def midnight = new Date( dt ).clearTime().getTime()
  def dayNigthRatio = (dt - midnight) / (24*60*60*1000)
  def result = ["dayNigthRatio": dayNigthRatio
  ]
  return result
}

def refreshLocation() {
    log.debug "refreshLocation()"
    def loc = [
        latitude:       location.latitude,
        longitude:      location.longitude,
        timeZoneOffset: new Date().format("Z", location.timeZone )
    ]
    sendEvents( loc )
}

def formatDate( date, boolean tz = true ) {
  return tz ? date.format("yyyy-MM-dd HH:mm:ssZ", location.timeZone )
            : date.format("yyyy-MM-dd HH:mm:ssZ" )
}

def formatDates ( dates, boolean tz = true ) {
  def result = [:]
  dates.each { key, value ->
        result[key] = formatDate( value, tz )
  }
  return result
}
//-----------------------------------------------------------------------
// Computes compass arithmetic (in degrees)
double compassArithmetic( double expression ) {
  // Make -360 < compass < 360
  double compass = remainder( expression, 360 )

  // Next, correct negative angles to positive ones
  compass = (compass < 0) ? (compass + 360) : (compass)
  return compass
}
//-----------------------------------------------------------------------
// Computes remainder using doubles a and b (a % b)
double remainder( double a, double b ) {
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
double PI()       { return Math.PI }
double rad()      { return Math.PI / 180.0 }
double sin(x)     { return Math.sin(x) }
double asin(x)    { return Math.asin(x) }
double cos(x)     { return Math.cos(x) }
double acos(x)    { return Math.acos(x) }
double tan(x)     { return Math.tan(x) }
double atan(y, x) { return Math.atan2(y, x) }

// date/time constants and conversions
def dayMs() { return 1000 * 60 * 60 * 24 }
def J1970() { return 2440588 }
def J2000() { return 2451545 }

def toJulian(date) { return date.getTime() / dayMs() - 0.5 + J1970() }
def fromJulian(j)  { return new Date( (long) ((j + 0.5 - J1970()) * dayMs()) ) }
def toDays(date)   { return toJulian(date) - J2000() }

// general calculations for position
double e() { rad() * 23.4397 } // obliquity of the Earth

double rightAscension(l, b) { return atan(sin(l) * cos(e()) - tan(b) * sin(e()), cos(l)); }
double declination(l, b)    { return asin(sin(b) * cos(e()) + cos(b) * sin(e()) * sin(l)); }

double azimuth(H, phi, dec)  { return atan(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi)); }
double altitude(H, phi, dec) { return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H)); }

double siderealTime(d, lw) { return rad() * (280.16 + 360.9856235 * d) - lw; }

double astroRefraction(h) {
    if (h < 0) // the following formula works for positive altitudes only.
        h = 0  // if h = -0.08901179 a div/0 would occur.

    // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
    // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
    return 0.0002967 / Math.tan(h + 0.00312536 / (h + 0.08901179));
}

// general sun calculations
def solarMeanAnomaly(d) { return rad() * (357.5291 + 0.98560028 * d) }

def eclipticLongitude( M ) {
    def C = rad() * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M)) // equation of center
    def P = rad() * 102.9372 // perihelion of the Earth
    return M + C + P + PI()
}

def sunCoords( d ) {
    def M = solarMeanAnomaly(d)
    def L = eclipticLongitude(M)
    return [
        dec: declination(L, 0),
        ra:  rightAscension(L, 0)
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
double J0() { return 0.0009 }

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
    result << ["midmorning"   : fromJulian( (jr + jn) / 2 ),
               "midafternoon" : fromJulian( (jn + js) / 2 )
              ]
    return result
}
