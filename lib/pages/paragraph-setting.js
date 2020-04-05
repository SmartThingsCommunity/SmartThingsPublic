'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class ParagraphSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'PARAGRAPH'
	}

	text(value) {
		super.name(value)
		return this
	}

	description(value) {
		super.description(value)
		return this
	}

	/**
	 * Set an image icon
	 *
	 * @param {String} source HTTPS url or Base64-encoded data URI. Max length 2048 characters.
	 * @returns {ParagraphSetting} Paragraph Setting instance
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
