'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class PhoneSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'PHONE'
		this._description = 'Tap to set'
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {PhoneSetting} Phone Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._image) {
			result.image = this._image
		}

		return result
	}
}
