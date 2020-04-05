var iconv = require("iconv-lite");
var path = require("path");

var vbsPath = path.join(__dirname, ".\\fallbacks\\paste.vbs");

var paste = { command: "cscript", args: [ "/Nologo", vbsPath ] };
paste.full_command = [ paste.command, paste.args[0], '"'+vbsPath+'"' ].join(" ");

exports.copy = { command: "clip", args: [] };
exports.paste = paste;

exports.encode = function(str) { return iconv.encode(str, "utf16le"); };
exports.decode = function(chunks) {
	if(!Array.isArray(chunks)) { chunks = [ chunks ]; }

	var b64 = iconv.decode(Buffer.concat(chunks), "cp437");
	b64 = b64.substr(0, b64.length - 2); // Chops off extra "\r\n"
    
    // remove bom and decode
    var result = new Buffer(b64, "base64").slice(3).toString("utf-8");
    return result;
};
