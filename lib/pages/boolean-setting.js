'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class BooleanSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'BOOLEAN'
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {BooleanSetting} Boolean Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * Indicates if this input should refresh configs after a change in value.
	 *
	 * @param {String} value
	 * @default false
	 * @returns {BooleanSetting} Boolean Setting instance
	 */
	submitOnChange(value) {
		this._submitOnChange = value
		return this
	}

	toJson() {
		const result = super.toJson()
		if (result.defaultValue === undefined && result.required) {
			result.defaultValue = 'false'
		}

		if (this._image) {
			result.image = this._image
		}

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		return result
	}
}
