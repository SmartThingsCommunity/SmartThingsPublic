/*
*   Communtity App Installer
*   Copyright 2018, 2019, 2020 Anthony Santilli, Corey Lista
*
// /**********************************************************************************************************************************************/
import java.security.MessageDigest

definition(
    name			: "ST-Community-Installer",
    namespace		: "tonesto7",
    author			: "tonesto7",
    description		: "The Community SmartApp/Devices Installer",
    category		: "My Apps",
    singleInstance	: true,
    iconUrl			: "${getAppImg("app_logo.png")}",
    iconX2Url		: "${getAppImg("app_logo.png")}",
    iconX3Url		: "${getAppImg("app_logo.png")}")
{
    appSetting "devMode"
}
/**********************************************************************************************************************************************/
private releaseVer() { return "1.1.0311a" }
private appVerDate() { "3-11-2020" }
/**********************************************************************************************************************************************/
preferences {
    page name: "startPage"
    page name: "mainPage"
}

mappings {
    path("/installStart") { action: [GET: "installStartHtml"] }
}

def startPage() {
    if(!atomicState?.accessToken) { getAccessToken() }
	if(!atomicState?.accessToken) {
		return dynamicPage(name: "startPage", title: "Status Page", nextPage: "", install: false, uninstall: true) {
			section ("Status Page:") {
				def title = ""
                def desc = ""
				if(!atomicState?.accessToken) { title="OAUTH Error"; desc = "OAuth is not Enabled for ${app?.label} application.  Please click remove and review the installation directions again"; }
				else { title="Unknown Error"; desc = "Application Status has not received any messages to display";	}
				log.warn "Status Message: $desc"
				paragraph title: "$title", "$desc", required: true, state: null
			}
		}
	}
    else { return mainPage() }
}

def mainPage() {
    dynamicPage (name: "mainPage", title: "", install: true, uninstall: true) {
        section("") { image getAppImg("welcome.png") }
        section("Login Options:") {
            if(!settings?.authAcctType) {
                paragraph title: "This helps to determine the login server you are sent to!", ""
            }
            input "authAcctType", "enum", title: "IDE Login Account Type", multiple: false, required: true, submitOnChange: true, options: ["samsung":"Samsung", "st":"SmartThings"], image: getAppImg("${settings?.authAcctType}_icon.png")
        }
        def hideBrowDesc = (atomicState?.isInstalled == true && ["embedded", "external"].contains(settings?.browserType))
        section("Browser Type Description:", hideable: hideBrowDesc, hidden: hideBrowDesc) {
            def embstr = "It's the most secure as the session is wiped everytime you close the view. So it will require logging in everytime you leave the view and isn't always friendly with Password managers (iOS)"
            paragraph title: "Embedded (Recommended)", embstr
            def extstr = "Will open the page outside the SmartThings app in your default browser. It will maintain your SmartThings until you logout. It will not force you to login everytime you leave the page and should be compatible with most Password managers. You can bookmark the Page for quick access."
            paragraph title: "External", extstr
        }
        section("Browser Option:") {
            input "browserType", "enum", title: "Browser Type", required: true, defaultValue: "embedded", submitOnChange: true, options: ["embedded":"Embedded", "external":"Mobile Browser"], image: ""
        }
        section("") {
            if(settings?.browserType) {
                // href "", title: "Installer Home", url: getLoginUrl(), style: (settings?.browserType == "external" ? "external" : "embedded"), required: false, description: "Tap Here to load the Installer Web App", image: getAppImg("go_img.png")
                href "", title: "Installer Home", url: getLoginUrl(), style: (settings?.browserType == "external" ? "external" : "embedded"), required: false, description: "Tap Here to launch the Installer Web App and Signin to the IDE", image: getAppImg("go_img.png")
            } else {
                paragraph title: "Browser Type Missing", "Please Select a browser type to proceed", required: true, state: null
            }
        }
    }
}

def baseUrl(path) {
    return "https://community-installer-34dac.firebaseapp.com${path}"
}

def getLoginUrl() {
    def r = URLEncoder.encode(getAppEndpointUrl("installStart"))
    def theURL = "https://account.smartthings.com/login?redirect=${r}"
    if(settings?.authAcctType == "samsung") { theURL = "https://account.smartthings.com/login/samsungaccount?redirect=${r}" }
    return theURL
}

def installStartHtml() {
    def randVerStr = "?=${now()}"
    def html = """
        <html lang="en">
            <head>
                <meta name="robots" content="noindex">
                <link rel="stylesheet" type="text/css" href="${baseUrl('/content/css/main_mdb.min.css')}" />
                <link rel="stylesheet" type="text/css" href="${baseUrl('/content/css/main_web.min.css')}" />
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
                <script type="text/javascript">
                    const serverUrl     = '${apiServerUrl('')}';
                    const homeUrl       = '${getAppEndpointUrl('installStart')}';
                    const loginUrl      = '${getLoginUrl()}'
                    const baseAppUrl    = '${baseUrl('')}';
                    const appVersion    = '${releaseVer()}';
                    const appVerDate    = '${appVerDate()}';
                    const hashedUuid    = '${generateLocationHash()}';
                    const devMode       = '${isDev()}';
                </script>
            </head>
            <body>
                <div id="bodyDiv"></div>
                ${getScript()}
            </body>
        </html>"""
    render contentType: "text/html", data: html
}

def isDev() { (appSettings?.devMode == true) }
def getScript() {
	def randVerStr = "?=${now()}"
    return isDev() ? """<script type="text/javascript" src="${baseUrl('/content/js/ignore_me.js')}"></script>""" : """<script type="text/javascript" src="${baseUrl('/content/js/awesome_file.js')}${randVerStr}"></script>"""
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    atomicState?.isInstalled = true
    initialize()
}

def updated() {
    log.trace ("${app?.getLabel()} | Now Running Updated() Method")
    if(!atomicState?.isInstalled) { atomicState?.isInstalled = true }
    initialize()
}

def initialize() {
    if (!atomicState?.accessToken) {
        log.debug "Access token not defined. Attempting to refresh. Ensure OAuth is enabled in the SmartThings IDE."
        getAccessToken()
    }
}

def uninstalled() {
	revokeAccessToken()
    log.warn("${app?.getLabel()} has been Uninstalled...")
}

def generateLocationHash() {
    def s = location?.getId()
    MessageDigest digest = MessageDigest.getInstance("MD5")
    digest.update(s.bytes);
    new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

def getAccessToken() {
    try {
        if(!atomicState?.accessToken) {
            log.error "SmartThings Access Token Not Found... Creating a New One!!!"
            atomicState?.accessToken = createAccessToken()
        } else { return true }
    }
    catch (ex) {
        log.error "Error: OAuth is not Enabled for ${app?.label}!.  Please click remove and Enable Oauth under the SmartApp App Settings in the IDE"
        return false
    }
}

def gitBranch()         { return "master" }
def getAppImg(file)	    { return "https://raw.githubusercontent.com/tonesto7/st-community-installer/${gitBranch()}/images/$file" }
def getAppVideo(file)	{ return "https://raw.githubusercontent.com/tonesto7/st-community-installer/${gitBranch()}/videos/$file" }
def getAppEndpointUrl(subPath)	{ return "${apiServerUrl("/api/smartapps/installations/${app.id}${subPath ? "/${subPath}" : ""}?access_token=${atomicState.accessToken}")}" }