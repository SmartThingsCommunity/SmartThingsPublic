'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class LinkSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'LINK'
	}

	/**
	 * The page to navigate to.
	 *
	 * @param {String} source Max length 2048 characters.
	 * @returns {LinkSetting} Link Setting instance
	 */
	url(value) {
		this._url = value
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {LinkSetting} Link Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	/**
	 * Style of the setting
	 *
	 * @param {('COMPLETE', 'ERROR', 'DEFAULT', 'BUTTON')} value
	 * @returns {LinkSetting} Link Setting instance
	 */
	style(value) {
		this._style = value
		return this
	}

	toJson() {
		const result = super.toJson()
		delete result.required
		if (this._url) {
			result.url = this._url
		}

		if (this._image) {
			result.image = this._image
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
