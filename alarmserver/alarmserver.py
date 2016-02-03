#!/usr/bin/python2.7
## Alarm Server
## Supporting Envisalink 2DS/3
## Written by donnyk+envisalink@gmail.com
## Lightly improved by leaberry@gmail.com
##
## This code is under the terms of the GPL v3 license.


import asyncore, asynchat
import ConfigParser
import datetime
import os, socket, string, sys, httplib, urllib, urlparse, ssl
import StringIO, mimetools
import json
import hashlib
import time
import getopt
import requests
import urllib2

from envisalinkdefs import evl_ResponseTypes
from envisalinkdefs import evl_Defaults
from envisalinkdefs import evl_ArmModes

LOGTOFILE = False

class CodeError(Exception): pass

ALARMSTATE={'version' : 0.1}
MAXPARTITIONS=16
MAXZONES=128
MAXALARMUSERS=47
CONNECTEDCLIENTS={}

def dict_merge(a, b):
    c = a.copy()
    c.update(b)
    return c

def getMessageType(code):
    return evl_ResponseTypes[code]

def alarmserver_logger(message, type = 0, level = 0):
    if LOGTOFILE:
        outfile.write(str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))+' '+message+'\n')
        outfile.flush()
    else:
        print (str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))+' '+message)
    

def to_chars(string):
    chars = []
    for char in string:
        chars.append(ord(char))
    return chars

def get_checksum(code, data):
    return ("%02X" % sum(to_chars(code)+to_chars(data)))[-2:]

#currently supports pushover notifications, more to be added
#including email, text, etc.
#to be fixed!
def send_notification(config, message):
    if config.PUSHOVER_ENABLE == True:
        conn = httplib.HTTPSConnection("api.pushover.net:443")
        conn.request("POST", "/1/messages.json",
            urllib.urlencode({
            "token": "qo0nwMNdX56KJl0Avd4NHE2onO4Xff",
            "user": config.PUSHOVER_USERTOKEN,
            "message": str(message),
            }), { "Content-type": "application/x-www-form-urlencoded" })

class AlarmServerConfig():
    def __init__(self, configfile):

        self._config = ConfigParser.ConfigParser()
        self._config.read(configfile)

        self.LOGURLREQUESTS = self.read_config_var('alarmserver', 'logurlrequests', True, 'bool')
        self.HTTPSPORT = self.read_config_var('alarmserver', 'httpsport', 8111, 'int')
        self.CERTFILE = self.read_config_var('alarmserver', 'certfile', 'server.crt', 'str')
        self.KEYFILE = self.read_config_var('alarmserver', 'keyfile', 'server.key', 'str')
        self.MAXEVENTS = self.read_config_var('alarmserver', 'maxevents', 10, 'int')
        self.MAXALLEVENTS = self.read_config_var('alarmserver', 'maxallevents', 100, 'int')
        self.ENVISALINKHOST = self.read_config_var('envisalink', 'host', 'envisalink', 'str')
        self.ENVISALINKPORT = self.read_config_var('envisalink', 'port', 4025, 'int')
        self.ENVISALINKPASS = self.read_config_var('envisalink', 'pass', 'user', 'str')
        self.ENABLEPROXY = self.read_config_var('envisalink', 'enableproxy', True, 'bool')
        self.ENVISALINKPROXYPORT = self.read_config_var('envisalink', 'proxyport', self.ENVISALINKPORT, 'int')
        self.ENVISALINKPROXYPASS = self.read_config_var('envisalink', 'proxypass', self.ENVISALINKPASS, 'str')
        self.PUSHOVER_ENABLE = self.read_config_var('pushover', 'enable', False, 'bool')
        self.PUSHOVER_USERTOKEN = self.read_config_var('pushover', 'enable', False, 'bool')
        self.ALARMCODE = self.read_config_var('envisalink', 'alarmcode', 1111, 'int')
        self.EVENTTIMEAGO = self.read_config_var('alarmserver', 'eventtimeago', True, 'bool')
        self.LOGFILE = self.read_config_var('alarmserver', 'logfile', '', 'str')
        self.APPTOKEN = self.read_config_var('alarmserver', 'apptoken', '', 'str')
        self.ACCESSTOKEN = self.read_config_var('alarmserver', 'accesstoken', '', 'str')
        self.ALARMTOKEN = self.read_config_var('alarmserver', 'alarmtoken', '', 'str')
        self.STURL = self.read_config_var('alarmserver', 'smartthingsurl', '', 'str')

        global LOGTOFILE
        if self.LOGFILE == '':
            LOGTOFILE = False
        else:
            LOGTOFILE = True

        self.ALARMSTAY={}
        self.ALARMAWAY={}
        self.ALARMOFF={}
        self.ALARMEXITDELAY={}
        self.ALARMENTRYDELAY={}
        self.ALARMNOTREADY={}
        self.ALARMREADY={}
        self.ALARMACTIVE={}
        for i in range(1, 99):
            token=self.read_config_var('alarm'+str(i), 'token', False, 'str', True)
            type=self.read_config_var('alarm'+str(i), 'type', 'switches', 'str', True)
            away=self.read_config_var('alarm'+str(i), 'away', False, 'str', True)
            stay=self.read_config_var('alarm'+str(i), 'stay', False, 'str', True)
            off=self.read_config_var('alarm'+str(i), 'off', False, 'str', True)
            exitdelay=self.read_config_var('alarm'+str(i), 'exitdelay', False, 'str', True)
            entrydelay=self.read_config_var('alarm'+str(i), 'entrydelay', False, 'str', True)
            notready=self.read_config_var('alarm'+str(i), 'notready', False, 'str', True)
            ready=self.read_config_var('alarm'+str(i), 'ready', False, 'str', True)
            alarm=self.read_config_var('alarm'+str(i), 'alarm', False, 'str', True)
            if token:
                if stay:
                    self.ALARMSTAY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+stay+'?access_token='+self.ACCESSTOKEN
                if away:
                    self.ALARMAWAY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+away+'?access_token='+self.ACCESSTOKEN
                if off:
                    self.ALARMOFF[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+off+'?access_token='+self.ACCESSTOKEN
                if exitdelay:
                    self.ALARMEXITDELAY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+exitdelay+'?access_token='+self.ACCESSTOKEN
                if entrydelay:
                    self.ALARMENTRYDELAY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+entrydelay+'?access_token='+self.ACCESSTOKEN
                if notready:
                    self.ALARMNOTREADY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+notready+'?access_token='+self.ACCESSTOKEN
                if ready:
                    self.ALARMREADY[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+ready+'?access_token='+self.ACCESSTOKEN
                if alarm:
                    self.ALARMACTIVE[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+token+'/'+alarm+'?access_token='+self.ACCESSTOKEN

        self.ZONEOPEN={}
        self.ZONECLOSE={}
        for i in range(1, MAXZONES+1):
            zonetype = self.read_config_var('zone'+str(i), 'type', False, 'str', True)
            if zonetype == 'contact':
              type = 'contactsensors'
              open = 'open'
              close = 'closed'
            elif zonetype == 'motion':
              type = 'motionsensors'
              open = 'active'
              close = 'inactive'

            if zonetype:
              self.ZONEOPEN[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+self.read_config_var('zone'+str(i), 'token', False, 'str', True)+'/'+open+'?access_token='+self.ACCESSTOKEN
              self.ZONECLOSE[i]=self.STURL+self.APPTOKEN+'/'+type+'/'+self.read_config_var('zone'+str(i), 'token', False, 'str', True)+'/'+close+'?access_token='+self.ACCESSTOKEN
            else:
              self.ZONEOPEN[i]=False
              self.ZONECLOSE[i]=False
 
        self.PARTITIONNAMES={}
        for i in range(1, MAXPARTITIONS+1):
            self.PARTITIONNAMES[i]=self.read_config_var('alarmserver', 'partition'+str(i), False, 'str', True)

        self.ZONENAMES={}
        for i in range(1, MAXZONES+1):
            self.ZONENAMES[i]=self.read_config_var('zone'+str(i), 'description', False, 'str', True)

        self.ALARMUSERNAMES={}
        for i in range(1, MAXALARMUSERS+1):
            self.ALARMUSERNAMES[i]=self.read_config_var('alarmserver', 'user'+str(i), False, 'str', True)

        if self.PUSHOVER_USERTOKEN == False and self.PUSHOVER_ENABLE == True: self.PUSHOVER_ENABLE = False

    def defaulting(self, section, variable, default, quiet = False):
        if quiet == False:
            print('Config option '+ str(variable) + ' not set in ['+str(section)+'] defaulting to: \''+str(default)+'\'')

    def read_config_var(self, section, variable, default, type = 'str', quiet = False):
        try:
            if type == 'str':
                return self._config.get(section,variable)
            elif type == 'bool':
                return self._config.getboolean(section,variable)
            elif type == 'int':
                return int(self._config.get(section,variable))
        except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
            self.defaulting(section, variable, default, quiet)
            return default

class HTTPChannel(asynchat.async_chat):
    def __init__(self, server, sock, addr):
        asynchat.async_chat.__init__(self, sock)
        self.server = server
        self.set_terminator("\r\n\r\n")
        self.header = None
        self.data = ""
        self.shutdown = 0

    def collect_incoming_data(self, data):
        self.data = self.data + data
        if len(self.data) > 16384:
        # limit the header size to prevent attacks
            self.shutdown = 1

    def found_terminator(self):
        if not self.header:
            # parse http header
            fp = StringIO.StringIO(self.data)
            request = string.split(fp.readline(), None, 2)
            if len(request) != 3:
                # badly formed request; just shut down
                self.shutdown = 1
            else:
                # parse message header
                self.header = mimetools.Message(fp)
                self.set_terminator("\r\n")
                self.server.handle_request(
                    self, request[0], request[1], self.header
                    )
                self.close_when_done()
            self.data = ""
        else:
            pass # ignore body data, for now

    def pushstatus(self, status, explanation="OK"):
        self.push("HTTP/1.0 %d %s\r\n" % (status, explanation))

    def pushok(self, content):
        self.pushstatus(200, "OK")
        self.push('Content-type: application/json\r\n')
        self.push('Expires: Sat, 26 Jul 1997 05:00:00 GMT\r\n')
        self.push('Last-Modified: '+ datetime.datetime.now().strftime("%d/%m/%Y %H:%M:%S")+' GMT\r\n')
        self.push('Cache-Control: no-store, no-cache, must-revalidate\r\n' ) 
        self.push('Cache-Control: post-check=0, pre-check=0\r\n') 
        self.push('Pragma: no-cache\r\n' )
        self.push('\r\n')
        self.push(content)

    def pushfile(self, file):
        self.pushstatus(200, "OK")
        extension = os.path.splitext(file)[1]
        if extension == ".html":
            self.push("Content-type: text/html\r\n")
        elif extension == ".js":
            self.push("Content-type: text/javascript\r\n")
        elif extension == ".png":
            self.push("Content-type: image/png\r\n")
        elif extension == ".css":
            self.push("Content-type: text/css\r\n")
        self.push("\r\n")
        self.push_with_producer(push_FileProducer(sys.path[0] + os.sep + 'ext' + os.sep + file))

class EnvisalinkClient(asynchat.async_chat):
    def __init__(self, config):
        # Call parent class's __init__ method
        asynchat.async_chat.__init__(self)

        # Define some private instance variables
        self._buffer = []

        # Are we logged in?
        self._loggedin = False

        # Set our terminator to \n
        self.set_terminator("\r\n")

        # Set config
        self._config = config

        # Reconnect delay
        self._retrydelay = 10

        self.do_connect()

    def do_connect(self, reconnect = False):
        # Create the socket and connect to the server
        if reconnect == True:
            alarmserver_logger('Connection failed, retrying in '+str(self._retrydelay)+ ' seconds')
            for i in range(0, self._retrydelay):
                time.sleep(1)

        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)

        self.connect((self._config.ENVISALINKHOST, self._config.ENVISALINKPORT))

    def collect_incoming_data(self, data):
        # Append incoming data to the buffer
        self._buffer.append(data)

    def found_terminator(self):
        line = "".join(self._buffer)
        self.handle_line(line)
        self._buffer = []

    def handle_connect(self):
        alarmserver_logger("Connected to %s:%i" % (self._config.ENVISALINKHOST, self._config.ENVISALINKPORT))

    def handle_close(self):
        self._loggedin = False
        self.close()
        alarmserver_logger("Disconnected from %s:%i" % (self._config.ENVISALINKHOST, self._config.ENVISALINKPORT))
        self.do_connect(True)

    def handle_eerror(self):
        self._loggedin = False
        self.close()
        alarmserver_logger("Error, disconnected from %s:%i" % (self._config.ENVISALINKHOST, self._config.ENVISALINKPORT))
        self.do_connect(True)

    def send_command(self, code, data, checksum = True):
        if checksum == True:
            to_send = code+data+get_checksum(code,data)+'\r\n'
        else:
            to_send = code+data+'\r\n'

        alarmserver_logger('TX > '+to_send[:-1])
        self.push(to_send)

    def handle_line(self, input):
        if input != '':
            for client in CONNECTEDCLIENTS:
                CONNECTEDCLIENTS[client].send_command(input, False)

            code=int(input[:3])
            parameters=input[3:][:-2]
            event = getMessageType(int(code))
            message = self.format_event(event, parameters)
            alarmserver_logger('RX < ' +str(code)+' - '+message)

            try:
                handler = "handle_%s" % evl_ResponseTypes[code]['handler']
            except KeyError:
                #call general event handler
                self.handle_event(code, parameters, event, message)
                return

            try:
                func = getattr(self, handler)
            except AttributeError:
                raise CodeError("Handler function doesn't exist")

            func(code, parameters, event, message)

    def format_event(self, event, parameters):
        if 'type' in event:
            if event['type'] in ('partition', 'zone'):
                if event['type'] == 'partition':
                    # If parameters includes extra digits then this next line would fail
                    # without looking at just the first digit which is the partition number
                    if int(parameters[0]) in self._config.PARTITIONNAMES:
                        if self._config.PARTITIONNAMES[int(parameters[0])]!=False:
                            # After partition number can be either a usercode
                            # or for event 652 a type of arm mode (single digit)
                            # Usercode is always 4 digits padded with zeros
                            if len(str(parameters)) == 5:
                                # We have a usercode
                                try:
                                    usercode = int(parameters[1:5])
                                except:
                                    usercode = 0
                                if int(usercode) in self._config.ALARMUSERNAMES:
                                    if self._config.ALARMUSERNAMES[int(usercode)]!=False:
                                        alarmusername = self._config.ALARMUSERNAMES[int(usercode)]
                                    else:
                                        # Didn't find a username, use the code instead
                                        alarmusername = usercode
                                    return event['name'].format(str(self._config.PARTITIONNAMES[int(parameters[0])]), str(alarmusername))
                            elif len(parameters) == 2:
                                # We have an arm mode instead, get it's friendly name
                                armmode = evl_ArmModes[int(parameters[1])]
                                return event['name'].format(str(self._config.PARTITIONNAMES[int(parameters[0])]), str(armmode))
                            else:
                                return event['name'].format(str(self._config.PARTITIONNAMES[int(parameters)]))
                elif event['type'] == 'zone':
                    if int(parameters) in self._config.ZONENAMES:
                        if self._config.ZONENAMES[int(parameters)]!=False:
                            return event['name'].format(str(self._config.ZONENAMES[int(parameters)]))

        return event['name'].format(str(parameters))

    #envisalink event handlers, some events are unhandeled.
    def handle_login(self, code, parameters, event, message):
        if parameters == '3':
            self._loggedin = True
            self.send_command('005', self._config.ENVISALINKPASS)
        if parameters == '1':
            self.send_command('001', '')
        if parameters == '0':
            alarmserver_logger('Incorrect envisalink password')
            sys.exit(0)

    def handle_event(self, code, parameters, event, message):
        if 'type' in event:
            if not event['type'] in ALARMSTATE: ALARMSTATE[event['type']]={'lastevents' : []}

            if event['type'] in ('partition', 'zone'):
                if event['type'] == 'zone':
                    if int(parameters) in self._config.ZONENAMES:
                        if not int(parameters) in ALARMSTATE[event['type']]:
                          ALARMSTATE[event['type']][int(parameters)] = {'name' : self._config.ZONENAMES[int(parameters)]}
                    else:
                        if not int(parameters) in ALARMSTATE[event['type']]: ALARMSTATE[event['type']][int(parameters)] = {}
                elif event['type'] == 'partition':
                    if int(parameters) in self._config.PARTITIONNAMES:
                        if not int(parameters) in ALARMSTATE[event['type']]: ALARMSTATE[event['type']][int(parameters)] = {'name' : self._config.PARTITIONNAMES[int(parameters)]}
                    else:
                        if not int(parameters) in ALARMSTATE[event['type']]: ALARMSTATE[event['type']][int(parameters)] = {}
            else:
                if not int(parameters) in ALARMSTATE[event['type']]: ALARMSTATE[event['type']][int(parameters)] = {}

            if not 'lastevents' in ALARMSTATE[event['type']][int(parameters)]: ALARMSTATE[event['type']][int(parameters)]['lastevents'] = []
            if not 'status' in ALARMSTATE[event['type']][int(parameters)]:
                if not 'type' in event:
                    ALARMSTATE[event['type']][int(parameters)]['status'] = {}
                else:
                    ALARMSTATE[event['type']][int(parameters)]['status'] = evl_Defaults[event['type']]

            if 'status' in event:
                ALARMSTATE[event['type']][int(parameters)]['status']=dict_merge(ALARMSTATE[event['type']][int(parameters)]['status'], event['status'])

            if len(ALARMSTATE[event['type']][int(parameters)]['lastevents']) > self._config.MAXEVENTS:
                ALARMSTATE[event['type']][int(parameters)]['lastevents'].pop(0)
            ALARMSTATE[event['type']][int(parameters)]['lastevents'].append({'datetime' : str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")), 'message' : message})

            if len(ALARMSTATE[event['type']]['lastevents']) > self._config.MAXALLEVENTS:
                ALARMSTATE[event['type']]['lastevents'].pop(0)
            ALARMSTATE[event['type']]['lastevents'].append({'datetime' : str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")), 'message' : message})
        self.callbackurl_event(code, parameters, event, message)

    def handle_zone(self, code, parameters, event, message):
        self.handle_event(code, parameters[1:], event, message)

    def handle_partition(self, code, parameters, event, message):
        self.handle_event(code, parameters[0], event, message)

    def callbackurl_event(self, code, parameters, event, message):
        alarmserver_logger('Did a callback!')
        if parameters.isdigit()==True:
                if self._config.ZONEOPEN[int(parameters)]!=False:
                        alarmserver_logger(str(code))
                        if str(code) == "609":
                                try:
                                        response = urllib2.urlopen(str(self._config.ZONEOPEN[int(parameters)]))
                                        html = response.read()
                                        alarmserver_logger(str(self._config.ZONEOPEN[int(parameters)]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")    
                        elif str(code) == "610":
                                try:
                                        response = urllib2.urlopen(str(self._config.ZONECLOSE[int(parameters)]))
                                        html = response.read()
                                        alarmserver_logger(str(self._config.ZONECLOSE[int(parameters)]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Alarm Ready
                        elif str(code) == "650":
                                try:
                                        for i in self._config.ALARMREADY:
                                            response = urllib2.urlopen(str(self._config.ALARMREADY[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMREADY[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Alarm Not Ready
                        elif str(code) == "651":
                                try:
                                        for i in self._config.ALARMNOTREADY:
                                            response = urllib2.urlopen(str(self._config.ALARMNOTREADY[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMNOTREADY[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Alarm Active (Alarm Tripped)
                        elif str(code) == "654":
                                try:
                                        for i in self._config.ALARMACTIVE:
                                            response = urllib2.urlopen(str(self._config.ALARMACTIVE[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMACTIVE[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Alarm Disarm
                        elif str(code) == "655":
                                try:
                                        for i in self._config.ALARMOFF:
                                            response = urllib2.urlopen(str(self._config.ALARMOFF[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMOFF[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Exit Delay
                        elif str(code) == "656":
                                try:
                                        for i in self._config.ALARMEXITDELAY:
                                            response = urllib2.urlopen(str(self._config.ALARMEXITDELAY[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMEXITDELAY[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")
                        #Entry Delay
                        elif str(code) == "657":
                                try:
                                        for i in self._config.ALARMENTRYDELAY:
                                            response = urllib2.urlopen(str(self._config.ALARMENTRYDELAY[i]))
                                            html = response.read()
                                            alarmserver_logger(str(self._config.ALARMENTRYDELAY[i]))
                                except:
                                        alarmserver_logger("Failed to connect to SmartThings")

                        #Alarm Armed
                        elif str(code) == "652":
                                if message.endswith("Away"):
                                        # Turn on away
                                        try:
                                                for i in self._config.ALARMAWAY:
                                                    response = urllib2.urlopen(str(self._config.ALARMAWAY[i]))
                                                    html = response.read()
                                                    alarmserver_logger(str(self._config.ALARMAWAY[i]))
                                        except:
                                                alarmserver_logger("Failed to connect to SmartThings")
                                elif message.endswith("Stay"):
                                        # Turn on stay
                                        try:
                                                for i in self._config.ALARMSTAY:
                                                    response = urllib2.urlopen(str(self._config.ALARMSTAY[i]))
                                                    html = response.read()
                                                    alarmserver_logger(str(self._config.ALARMSTAY[i]))
                                        except:
                                                alarmserver_logger("Failed to connect to SmartThings")


class push_FileProducer:
    # a producer which reads data from a file object

    def __init__(self, file):
        self.file = open(file, "rb")

    def more(self):
        if self.file:
            data = self.file.read(2048)
            if data:
                return data
            self.file = None
        return ""

class AlarmServer(asyncore.dispatcher):
    def __init__(self, config):
        # Call parent class's __init__ method
        asyncore.dispatcher.__init__(self)

        # Create Envisalink client object
        self._envisalinkclient = EnvisalinkClient(config)

        #Store config
        self._config = config

        # Create socket and listen on it
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.bind(("", config.HTTPSPORT))
        self.listen(5)

    def handle_accept(self):
        # Accept the connection
        conn, addr = self.accept()
        if (config.LOGURLREQUESTS):
            alarmserver_logger('Incoming web connection from %s' % repr(addr))

        try:
            HTTPChannel(self, conn, addr)
            # HTTPChannel(self, ssl.wrap_socket(conn, server_side=True, certfile=config.CERTFILE, keyfile=config.KEYFILE, ssl_version=ssl.PROTOCOL_TLSv1), addr)
        except ssl.SSLError:
            return

    def handle_request(self, channel, method, request, header):
        if (config.LOGURLREQUESTS):
            alarmserver_logger('Web request: '+str(method)+' '+str(request))

        query = urlparse.urlparse(request)
        query_array = urlparse.parse_qs(query.query, True)

        if query.path == '/':
            channel.pushfile('index.html');
        elif query.path == '/api':
            channel.pushok(json.dumps(ALARMSTATE))
        elif query.path == '/api/alarm/arm':
            channel.pushok(json.dumps({'response' : 'Request to arm received'}))
            self._envisalinkclient.send_command('030', '1')
        elif query.path == '/api/alarm/stayarm':
            channel.pushok(json.dumps({'response' : 'Request to arm in stay received'}))
            self._envisalinkclient.send_command('031', '1')
        elif query.path == '/api/alarm/armwithcode':
            channel.pushok(json.dumps({'response' : 'Request to arm with code received'}))
            self._envisalinkclient.send_command('033', '1' + str(query_array['alarmcode'][0]))
        elif query.path == '/api/pgm':
            channel.pushok(json.dumps({'response' : 'Request to trigger PGM'}))
            #self._envisalinkclient.send_command('020', '1' + str(query_array['pgmnum'][0]))
            self._envisalinkclient.send_command('071', '1' + "*7" + str(query_array['pgmnum'][0]))
            time.sleep(1)
            self._envisalinkclient.send_command('071', '1' + str(query_array['alarmcode'][0]))
        elif query.path == '/api/alarm/disarm':
            channel.pushok(json.dumps({'response' : 'Request to disarm received'}))
            if 'alarmcode' in query_array:
                self._envisalinkclient.send_command('040', '1' + str(query_array['alarmcode'][0]))
            else:
                self._envisalinkclient.send_command('040', '1' + str(self._config.ALARMCODE))
        elif query.path == '/api/refresh':
            channel.pushok(json.dumps({'response' : 'Request to refresh data received'}))
            self._envisalinkclient.send_command('001', '')
        elif query.path == '/api/config/eventtimeago':
            channel.pushok(json.dumps({'eventtimeago' : str(self._config.EVENTTIMEAGO)}))
        elif query.path == '/img/glyphicons-halflings.png':
            channel.pushfile('glyphicons-halflings.png')
        elif query.path == '/img/glyphicons-halflings-white.png':
            channel.pushfile('glyphicons-halflings-white.png')
        elif query.path == '/favicon.ico':
            channel.pushfile('favicon.ico')
        else:
            if len(query.path.split('/')) == 2:
                try:
                    with open(sys.path[0] + os.sep + 'ext' + os.sep + query.path.split('/')[1]) as f:
                        f.close()
                        channel.pushfile(query.path.split('/')[1])
                except IOError as e:
                    print "I/O error({0}): {1}".format(e.errno, e.strerror)
                    channel.pushstatus(404, "Not found")
                    channel.push("Content-type: text/html\r\n")
                    channel.push("File not found")
                    channel.push("\r\n")
            else:
                if (config.LOGURLREQUESTS):
                                                                        alarmserver_logger("Invalid file requested")

                channel.pushstatus(404, "Not found")
                channel.push("Content-type: text/html\r\n")
                channel.push("\r\n")

class ProxyChannel(asynchat.async_chat):
    def __init__(self, server, proxypass, sock, addr):
        asynchat.async_chat.__init__(self, sock)
        self.server = server
        self.set_terminator("\r\n")
        self._buffer = []
        self._server = server
        self._clientMD5 = hashlib.md5(str(addr)).hexdigest()
        self._straddr = str(addr)
        self._proxypass = proxypass
        self._authenticated = False

        self.send_command('5053')

    def collect_incoming_data(self, data):
        # Append incoming data to the buffer
        self._buffer.append(data)

    def found_terminator(self):
        line = "".join(self._buffer)
        self._buffer = []
        self.handle_line(line)

    def handle_line(self, line):
        alarmserver_logger('PROXY REQ < '+line)
        if self._authenticated == True:
            self._server._envisalinkclient.send_command(line, '', False)
        else:
            self.send_command('500005')
            expectedstring = '005' + self._proxypass + get_checksum('005', self._proxypass)
            if line == ('005' + self._proxypass + get_checksum('005', self._proxypass)):
                alarmserver_logger('Proxy User Authenticated')
                CONNECTEDCLIENTS[self._straddr]=self
                self._authenticated = True
                self.send_command('5051')
            else:
                alarmserver_logger('Proxy User Authentication failed')
                self.send_command('5050')
                self.close()

    def send_command(self, data, checksum = True):
        if checksum == True:
            to_send = data+get_checksum(data, '')+'\r\n'
        else:
            to_send = data+'\r\n'

        self.push(to_send)

    def handle_close(self):
        alarmserver_logger('Proxy connection from %s closed' % self._straddr)
        if self._straddr in CONNECTEDCLIENTS: del CONNECTEDCLIENTS[self._straddr]
        self.close()

    def handle_error(self):
        alarmserver_logger('Proxy connection from %s errored' % self._straddr)
        if self._straddr in CONNECTEDCLIENTS: del CONNECTEDCLIENTS[self._straddr]
        self.close()

class EnvisalinkProxy(asyncore.dispatcher):
    def __init__(self, config, server):
        self._config = config
        if self._config.ENABLEPROXY == False:
            return

        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.set_reuse_addr()
        alarmserver_logger('Envisalink Proxy Started')

        self.bind(("", self._config.ENVISALINKPROXYPORT))
        self.listen(5)

    def handle_accept(self):
        pair = self.accept()
        if pair is None:
            pass
        else:
            sock, addr = pair
            alarmserver_logger('Incoming proxy connection from %s' % repr(addr))
            handler = ProxyChannel(server, self._config.ENVISALINKPROXYPASS, sock, addr)

def usage():
    print 'Usage: '+sys.argv[0]+' -c <configfile>'

def main(argv):
    try:
      opts, args = getopt.getopt(argv, "hc:", ["help", "config="])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ("-c", "--config"):
            global conffile
            conffile = arg


if __name__=="__main__":
    conffile='alarmserver.cfg'
    main(sys.argv[1:])
    print('Using configuration file %s' % conffile)
    config = AlarmServerConfig(conffile)
    if LOGTOFILE:
        outfile=open(config.LOGFILE,'a')
        print ('Writing logfile to %s' % config.LOGFILE)

    alarmserver_logger('Alarm Server Starting')
    alarmserver_logger('Currently Supporting Envisalink 2DS/3 only')
    alarmserver_logger('Tested on a DSC-1616 + EVL-3')
    alarmserver_logger('and on a DSC-1832 + EVL-2DS')
    alarmserver_logger('and on a DSC-1864 v4.6 + EVL-3')

    server = AlarmServer(config)
    proxy = EnvisalinkProxy(config, server)

    try:
        while True:
            asyncore.loop(timeout=2, count=1)
            # insert scheduling code here.
    except KeyboardInterrupt:
        print "Crtl+C pressed. Shutting down."
        alarmserver_logger('Shutting down from Ctrl+C')
        if LOGTOFILE:
            outfile.close()
        
        server.shutdown(socket.SHUT_RDWR) 
        server.close() 
        sys.exit()
