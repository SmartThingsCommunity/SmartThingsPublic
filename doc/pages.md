# SmartThings SDK

## Configuration Pages Reference

### Page

**page (id, definition)**

```javascript
app.page('pageOne', (ctx, page, configData) => {
    page.section('Heading', section => {
    	...
    })
})
```
Creates a new configuration page. All pages must have an ID, which is used to reference the page in link and next/previous
fields.

**page (id, definition)**

```javascript
app.defaultPage((ctx, page, configData) => {
    if(configData.pageId === 'pageTwo') {
    	...
    } else {
    	...
    }
})
```
Defines a handler that is called if the page ID doesn't match any of the declared pages.

#### Page Properties

##### _name (text)_
Specify the name appearing at the top of this page. If omitted and i18n localization
has been configured then this text is taken from the appropriate
locale file.

##### _complete ( true | false )_
If true then the _Done_ button appears on the page, allowing the app
to be installed or updated.

##### _nextPageId (id)_
ID of the page to display when the _Next_ button is pressed.

##### _previousPageId (id)_
ID of the page to display when the _Back_ button is pressed.

### Section

**section([title], definition)**

```javascript
app.page.('pageOne', (page) => {
    page.section('Select sensors', (section) => {
        section.hideable(true)
        ...
    })
})
```
Add a section to the page. All inputs are contained within a section. Sections optionally can have a title

#### Section Properties

##### _hideable (value)_

Section contents can be displayed or hidden on the page. Hideable sections should have titles for clarity.

##### _hidden(value)_

The initial state of the section is hidden.

### Settings
```javascript
app.page.('pageOne', (page) => {
    page.section('Select sensors', (section) => {
        section.deviceSetting('motionSensors')
            .capability('motionSensor')
            .multiple(true)
    })
})
```


#### Common Setting Properties

The following properties can be set for any type of setting

##### _name (text)_
##### _description (text)_
##### _disabled (true|false)_
##### _required (true|false)_
##### _defaultValue (value)_
##### _translateDefaultValue (true|false)_

#### booleanSetting (id)
##### _image (url)_
##### _submitOnChange (true|false)_


#### decimalSetting (id)
##### _image (url)_
##### _min (value)_
##### _max (value)_
##### _postMessage (text)_

#### deviceSetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _submitOnChange (true|false)_
##### _preselect (true|false)_
##### _capability (string)_
##### _apabilities (list)_
##### _excludeCapability (string)_
##### _excludeCapabilities (list)_
##### _capability (string)_
##### _permissions (string | list)_

#### emailSetting (id)
##### _image (url)_

#### enumSetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _translateOptions (true|false)_
##### _groupedOptions (list)_
##### _options (list)_
##### _submitOnChange (true|false)_
##### _style ('COMPLETE'|'ERROR'|'DROPDOWN'|DEFAULT')_

#### imageSetting (id)
##### _image (url)_

#### imagesSetting (id)
##### _images (list)_

#### linkSetting (id)
##### _url (url)_
##### _image (url)_
##### _style ('COMPLETE'|'ERROR'|'BUTTON'|DEFAULT')_

#### modeSetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _submitOnChange (true|false)_
##### _style ('COMPLETE'|'ERROR'|DEFAULT')_

#### numberSetting (id)
##### _image (url)_
##### _min (value)_
##### _max (value)_
##### _postMessage (text)_

#### oauthSetting (id)
##### _urlTemplate (url)_
##### _style ('COMPLETE'|'ERROR'|DEFAULT')_

#### paragraphSetting (id)
##### _image (url)_
##### _text (text)_

#### passwordSetting (id)
##### _image (url)_
##### _minLength (value)_
##### _maxLength (value)_

#### phoneSetting (id)
##### _image (url)_

#### sceneSetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _submitOnChange (true|false)_
##### _style ('COMPLETE'|'ERROR'|DEFAULT')_

#### securitySetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _submitOnChange (true|false)_
##### _style ('COMPLETE'|'ERROR'|'DROPDOWN'|DEFAULT')_

#### soundSetting (id)
##### _multiple (true|false)_
##### _closeOnSelection (true|false)_
##### _groupedOptions (list)_
##### _options (list)_
##### _submitOnChange (true|false)_
##### _style ('COMPLETE'|'ERROR'|'DROPDOWN'|DEFAULT')_

#### textSetting (id)
##### _image (url)_
##### _minLength (value)_
##### _maxLength (value)_
##### _postMessage (text)_

#### timeSetting (id)
##### _image (url)_

#### videoSetting (id)
##### _image (url)_
##### _video (url)_
