'use strict';

var should = require('should');
var clipboard = require("../index.js");

function copy_and_paste(content, done) {
    clipboard.copy(content, function(error_when_copy) {
        should.not.exist(error_when_copy);
        
        clipboard.paste(function (error_when_paste, p) {
            should.not.exist(error_when_paste);
            should.exist(p);
            p.should.equal(content);
            done();
        });
    });
}

describe('copy and paste', function () {
  it('should work correctly with ascii chars (<128)', function (done) {
    
    copy_and_paste("123456789abcdefghijklmnopqrstuvwxyz+-=&_[]<^=>=/{:})-{(`)}", done);
  });
  
  it('should work correctly with cp437 chars (<256)', function (done) {
    
    copy_and_paste("ÉæÆôöòûùÿÖÜ¢£¥₧ƒ", done);
  });
  
  it('should work correctly with unicode chars (<2^16)', function (done) {
    
    copy_and_paste("ĀāĂăĄąĆćĈĉĊċČčĎ ፰፱፲፳፴፵፶፷፸፹፺፻፼", done);
  });
  
  it('should work correctly for "±"', function (done) {
    
    copy_and_paste("±", done);
  });
});