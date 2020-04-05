'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class TimeSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'TIME'
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {TimeSetting} Time Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * The maximum inclusive value the time can be set to (only the time will be used out of the date time).
	 *
	 * @param {String} value DateTime
	 * @returns {TimeSetting} Time Setting instance
	 */
	max(value) {
		this._max = value
		return this
	}

	/**
	 * The minimum inclusive value the time can be set to (only the time will be used out of the date time).
	 *
	 * @param {String} value DateTime
	 * @returns {TimeSetting} Time Setting instance
	 */
	min(value) {
		this._min = value
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
