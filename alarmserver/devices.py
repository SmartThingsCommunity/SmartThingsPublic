#!/usr/bin/python2.7

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


MAXZONES=128
MAXPARTITIONS=16

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
        self.ALARMCODE = self.read_config_var('envisalink', 'alarmcode', 1111, 'str')
        self.EVENTTIMEAGO = self.read_config_var('alarmserver', 'eventtimeago', True, 'bool')
        self.LOGFILE = self.read_config_var('alarmserver', 'logfile', '', 'str')
        self.CALLBACKURL_BASE = self.read_config_var('alarmserver', 'callbackurl_base', '', 'str')
        self.CALLBACKURL_APP_ID = self.read_config_var('alarmserver', 'callbackurl_app_id', '', 'str')
        self.CALLBACKURL_ACCESS_TOKEN = self.read_config_var('alarmserver', 'callbackurl_access_token', '', 'str')
        self.CALLBACKURL_EVENT_CODES = self.read_config_var('alarmserver', 'callbackurl_event_codes', '', 'str')
        global LOGTOFILE
        if self.LOGFILE == '':
            LOGTOFILE = False
        else:
            LOGTOFILE = True

        self.PARTITIONNAMES={}
        self.PARTITIONS={}
        for i in range(1, MAXPARTITIONS+1):
            self.PARTITIONNAMES[i]=self.read_config_var('partition'+str(i), 'name', False, 'str', True)
            stay=self.read_config_var('partition'+str(i), 'stay', False, 'str', True)
            away=self.read_config_var('partition'+str(i), 'away', False, 'str', True)
            if stay!=False or away!=False:
                self.PARTITIONS[i] = {}
                if away!=False:
                    self.PARTITIONS[i]['away']=away
                if stay!=False:
                    self.PARTITIONS[i]['stay']=stay

        self.ZONES={}
        self.ZONENAMES={}
        for i in range(1, MAXZONES+1):
            self.ZONENAMES[i]=self.read_config_var('zone'+str(i), 'name', False, 'str', True)
            type = self.read_config_var('zone'+str(i), 'type', False, 'str', True)
            if(self.ZONENAMES[i]!=False and type!=False):
                self.ZONES[i] = {}
                self.ZONES[i]['name'] = self.ZONENAMES[i]
                self.ZONES[i]['type'] = type

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

class DeviceSetup():
    def __init__(self, config, delete):
        self._config = config

        # prepare partition and zone json for device creation
        if delete:
            partitionjson = json.dumps({})
            zonejson = json.dumps({})
        else:
            partitionjson = json.dumps(config.PARTITIONS)
            zonejson = json.dumps(config.ZONES)

        headers = {'content-type': 'application/json'}

        # create zone devices
        myURL = config.CALLBACKURL_BASE + '/' + config.CALLBACKURL_APP_ID + '/installzones' + '?access_token=' + config.CALLBACKURL_ACCESS_TOKEN
        print('myURL: %s' % myURL)
        r = requests.post(myURL, data=zonejson, headers=headers)
        print('zonejson: %s\nStatus: %s\nMessage: %s' % (zonejson, r.status_code, r.text))

        # create partition devices
        myURL = config.CALLBACKURL_BASE + '/' + config.CALLBACKURL_APP_ID + '/installpartitions' + '?access_token=' + config.CALLBACKURL_ACCESS_TOKEN
        print('myURL: %s' % myURL)
        r = requests.post(myURL, data=partitionjson, headers=headers)
        print('partitionjson: %s\nStatus: %s\nMessage: %s' % (partitionjson, r.status_code, r.text))



def usage():
    print 'Usage: '+sys.argv[0]+' -c <configfile>'


def main(argv):
    try:
      opts, args = getopt.getopt(argv, "hdc:", ["help", "config="])
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
        elif opt in ("-d", "--delete"):
            global delete
            delete = True


if __name__=="__main__":
    conffile='alarmserver.cfg'
    delete=False
    main(sys.argv[1:])
    print('Using configuration file %s' % conffile)
    config = AlarmServerConfig(conffile)
    DeviceSetup(config, delete)
