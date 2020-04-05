'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class SoundSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'SOUND'
	}

	/**
	 * @typedef GroupedSoundOption
	 * @prop {String} name The display name of this group of sound options.  Max length 128 characters.
	 * @prop {Array.<SoundOption>} options The sound enum options.
	 */

	/**
	 * @typedef SoundOption
	 * @prop {String} id The unique ID for this option. Max length 128 characters.
	 * @prop {String} name The display name for this option. Max length 128 characters.
	 * @prop {String} sound The sound url. Either .mp3 or .wav. Max length 2048 characters.
	 */

	/**
	 * Indicates if this enum setting can have multiple values.
	 *
	 * @param {Boolean} value Multiple value
	 * @default false
	 * @returns {SoundSetting} Sound Setting instance
	 */
	multiple(value) {
		this._multiple = value
		return this
	}

	/**
	 * Display the enum options as groups.
	 *
	 * @param {Array.<GroupedSoundOption>} groups Array of grouped sound options
	 * @returns {SoundSetting} Sound Setting instance
	 *
	 * @example
	 ```javascript
const groups = [{
        name: 'First Group',
        options: [{
            id: 'sound-001',
            name: 'Sound 1',
            sound: 'https://example.com/sound-001.mp3'
        }]
    },
    {
        name: 'Second Group',
        options: [{
            id: 'sound-002',
            name: 'Sound 2',
            sound: 'https://example.com/sound-002.mp3'
        }]
    }
]
section.soundSetting('id').groupedOptions(groups)
	 ```
	 */
	groupedOptions(groups) {
		this._groupedOptions = groups
		return this
	}

	/**
	 * The sound options.
	 *
	 * @param {SoundOption | Array.<SoundOption>} options Single or array of sound options
	 * @returns {SoundSetting} Sound Setting instance
	 * @example
	 *
	 ```javascript
const options = [{
        id: 'sound-001',
		name: 'Sound 1',
		sound: 'https://example.com/sound-001.mp3'
    },
    {
        id: 'sound-002',
		name: 'Sound 2',
		sound: 'https://example.com/sound-002.mp3'
    }
]
section.soundSetting('id').options(options)
	 ```
	 */
	options(value) {
		this._options = value
		return this
	}

	/**
	 * Indicates if this input should refresh configs after a change in value.
	 *
	 * @param {Boolean} value Submit on change value
	 * @default false
	 * @returns {SoundSetting} Sound Setting instance
	 */
	submitOnChange(value) {
		this._submitOnChange = value
		return this
	}

	/**
	 * Style of the setting.
	 *
	 * @param {('COMPLETE','ERROR','DEFAULT','DROPDOWN')} value
	 * @returns {SoundSetting} Sound Setting instance
	 */
	style(value) {
		this._style = value
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._multiple) {
			result.multiple = this._multiple
		}

		if (this._groupedOptions) {
			result.groupedOptions = this._groupedOptions
		}

		if (this._options) {
			result.options = this._options
		}

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
