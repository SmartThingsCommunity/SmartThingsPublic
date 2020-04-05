'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class ColorSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'COLOR'
	}

	/**
	 * Configure your image source
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {ColorSetting} Color Setting instance
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
