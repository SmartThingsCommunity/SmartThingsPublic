'use strict'
const i18n = require('i18n')

const Section = require('./section')

module.exports = class Page {
	constructor(id, language) {
		this._id = id
		this._name = this.i18nKey('name')
		this._nextPageId = null
		this._previousPageId = null
		this._sections = []
		if (language) {
			this.headers = {'accept-language': language}
			i18n.init(this)
		}

		this._settingIds = []
		this._defaultRequired = false
	}

	/**
	 * @callback SectionCallback
	 * @param section { import("./section") } Chainable section instance
	 */

	/**
	 * Define sections for user defined settings. Chain as many as needed.
	 *
	 * https://smartthings.developer.samsung.com/docs/how-to/design-pages-smartapp.html#Page-Components
	 * @param {String} name Name your section for use in i18n localization keys
	 * @param {SectionCallback} closure { import("./section") } Chainable section instance
	 * @returns {Page} Page instance
	 */
	async section(name, closure) {
		let sec
		let callable = closure
		if (typeof name === 'string') {
			sec = new Section(this, name)
		} else {
			sec = new Section(this)
			callable = name
		}

		this._sections.push(sec)
		if (callable) {
			await callable(sec)
		}

		return this
	}

	/**
	 * Indicates if this is the last page in the configuration process. If left unset,
	 * `complete` will equal `true` when **no next page** and **no previous pages**
	 * are defined.
	 *
	 * @param {Boolean} complete Set to true to signify that this page will complete configuration
	 * @returns {Page} Page instance
	 */
	complete(complete) {
		this._complete = complete
		return this
	}

	/**
	 * Name of the page to be configured.
	 *
	 * @param {String} name
	 * @returns {Page} Page instance
	 */
	name(name) {
		this._name = name
		return this
	}

	/**
	 * Change how the page is presented.
	 *
	 * @param {('NORMAL'|'SPLASH')} style Page style
	 * @returns {Page} Page instance
	 */
	style(style) {
		this._style = style
		return this
	}

	/**
	 * The text for the next button. Only applies if style is `SPLASH`.
	 *
	 * @param {String} text
	 */
	nextText(text) {
		this._nextText = text
		return this
	}

	/**
	 * A developer defined page ID for the next page in the configuration process. Must be URL safe characters.
	 *
	 * @param {String} id
	 * @returns {Page} Page instance
	 */
	nextPageId(id) {
		this._nextPageId = id
		return this
	}

	/**
	 * A developer defined page ID for the previous page in the configuration process. Must be URL safe characters.
	 *
	 * @param {String} id
	 * @returns {Page} Page instance
	 */
	previousPageId(id) {
		this._previousPageId = id
		return this
	}

	/**
	 * Automatically applies the `required: true` field to all section settings
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {Page} Page instance
	 */
	defaultRequired(value) {
		this._defaultRequired = value
		return this
	}

	i18nKey(property) {
		return `pages.${this._id}.${property}`
	}

	translate(...args) {
		if (this.headers) {
			return this.__(...args)
		}

		return args[0]
	}

	toJson() {
		const result = {
			name: this.translate(this._name),
			complete: this._complete === undefined ? (!this._nextPageId && !this._previousPageId) : this._complete,
			pageId: this._id,
			nextPageId: this._nextPageId,
			previousPageId: this._previousPageId,
			sections: []
		}
		if (this._style) {
			result.style = this._style
		}

		if (this._nextText) {
			result.nextText = this._nextText
		}

		for (const section of this._sections) {
			result.sections.push(section.toJson())
		}

		return result
	}
}
