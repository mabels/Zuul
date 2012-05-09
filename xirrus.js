
var https = require('https')
var fs = require('fs');


var jsdom = require('jsdom')


/*
curl  -D /dev/stdout --insecure 'https://172.20.95.21/cgi-bin/ViewPage.cgi?fname=pjxLogin&args=admin&user=admin&args=PWD&password=PWD'
curl  -D /dev/stdout --insecure  -H 'Cookie: CGISESSID=52abb7005e66f4481bfd9e0918a32c01; path=/; XIRRUS_OPTIONS=OPT_AUTO_CLOSE&1; path=/; expires=Fri, 08-Jun-2012 10:28:18 GMT' 'https://172.20.95.21/cgi-bin/ViewPage.cgi?fname=pjxContent&args=stations'
*/

var config = JSON.parse(fs.readFileSync("config.json", "utf-8"));
//console.log("CONFIG:", config)

var password = config.xirruspw;

var xirrus = function(ip) {
  if (!ip) { 
    return;
  } else {
    xirrus(config.xirrus.pop());
  }
  var login = {
        host: ip,
        port: 443,
        path: "/cgi-bin/ViewPage.cgi?fname=pjxLogin&args=admin&user=admin&args="+escape(password)+'&password='+escape(password),
        method: 'GET',
        headers: {}
    }

  var req = https.request(login, function(res) {
      if (res.statusCode == 200) {
        res.setEncoding('utf8')
        var chunks = []
        res.on('error', function (e) {
          console.error("FEHLER:", e);
        })
        res.on('data', function (chunk) {
          chunks.push(chunk)
        });
        res.on("end", function() {
            //console.log(res.headers); 
            data=login
            data.path="/cgi-bin/ViewPage.cgi?fname=pjxContent&args=stations"
            data.headers['cookie'] = res.headers['set-cookie'].join('; ')
            https.request(data, function(res) {
              res.setEncoding('utf8')
              var chunks = []
              res.on('error', function (e) {
                console.error("FEHLER:", e);
              })
              res.on('data', function (chunk) {
                chunks.push(chunk)
              });
              res.on("end", function() {
              /*
  50:ea:d6:37:3e:9a',
      '',
      '10.24.7.84',
      '',
      '',
      'Apple',
      '',
      '',
      'next12',
      '',
      '223',
      '2',
      'None',
      'none',
      'none',
      '.11bgn',
      'iap2',
               */
                jsdom.env({
                            html: chunks.join(''),
                            scripts: ['http://code.jquery.com/jquery-1.7.1.min.js']
                          }, function(err, window) {
                              var $ = window.jQuery;
                              rows = []
                              $('#TableEntries tr.assoc').each(function() {
                                var $entry = $(this)
                                row = []
                                $entry.find('th').each(function() {
                                  row.push($.trim($(this).text()))
                                })
                                $entry.find('td').each(function() {
                                  row.push($.trim($(this).text()))
                                })
                                rows.push(row)
                              })
                              for(var i in rows) {
                                console.log(ip, rows[i][0],rows[i][16],rows[i][2])
                              }
                          })
                var logout = data;
                logout.path = "/cgi-bin/ViewPage.cgi?page=logout"
                https.request(logout, function(res) {
                }).end();
              })
            }).end()
        })
      } else {
        console.error("geht nicht", res);
      }
  })
  req.end();
}

xirrus(config.xirrus.pop());


