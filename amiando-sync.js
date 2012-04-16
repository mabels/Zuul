/*
 * This is ugly and only for testing
 * I need to cleanup the api
 */
var http = require('http')
var fs = require('fs')

var config = JSON.stringify(fs.readFileSync("config.json", "utf-8"))

var Amiando = function() { 
  this.apiKey = config.apiKey
  this.params = {
    apikey: this.apiKey,
    version: 1,
    format: "json"
  }
}
Amiando.prototype.buildUrl = function(uri, opt) {
  var params = []
  for(var i in this.params) {
    opt[i] = opt[i] || this.params[i]
  }
  for (var i in opt) { 
    params.push(i+"="+escape(opt[i]))
  }
  var url =  uri + "?" + params.join("&")
  console.log("buildUrl:", url)
  return url;
}
Amiando.prototype.call = function(method, uri, opt, completed) {
  var options = {
      host: this.host || "www.amiando.com",
      port: ~~this.port || 80,
      path: this.buildUrl(uri, opt),
      method: method
  }
  var req = http.request(options, function(res) {
    if (res.statusCode == 200) {
      res.setEncoding('utf8')
      var chunks = []
      res.on('error', function (e) {
        completed(null, e)
      })
      res.on('data', function (chunk) {
        chunks.push(chunk)
      })
      res.on("end", function() {
          completed(JSON.parse(chunks.join('')))
      })
    } else {
      console.log("Amiando.prototype.call:statusCode:", res.statusCode)
      completed(null, res)
    }
  }) 
  req.on('error', function(e) {
    console.log('problem with request: ' + e.message);
    completed(null, e)
  });
  method == 'POST' && req.write(JSON.stringify(opt))
  req.end()
}

Amiando.prototype.resync = function(version, completed) {
  this.call("POST", "/api/sync/resync", { version: version }, completed)
}

Amiando.prototype.sync = function(number, completed) {
  if (typeof number != "number") { number = 1 } 
  var self = this
  this.call("GET", "/api/sync/"+number, {}, function(data) {
console.log("sync:", data)
      completed(data)
/*
    if (data.events.length == 0) {
      setTimeout(function() {
        self.sync(data.nextId, completed)
      }, 30000)
    } else {
      self.sync(data.nextId, completed)
    }
*/
  })
}

Amiando.prototype.eventFind = function(params, completed) {
console.log("******:/api/event/find", params)
  this.call("GET", "/api/event/find", params, completed)
}

Amiando.prototype.ticketFind = function(params, completed) {
console.log("******:/api/ticket/find", params)
  this.call("GET", "/api/ticket/find", params, completed);
}
Amiando.prototype.event = function(eventId, completed) {
console.log("******:/api/event/", eventId)
  this.call("GET", "/api/event/"+ eventId, {}, completed);
}

Amiando.prototype.ticket = function(ticketId, completed) {
console.log("******:/api/ticket/", ticketId)
  this.call("GET", "/api/ticket/"+ ticketId, {}, completed);
}

var amiando = new Amiando();

amiando.eventFind({ title: config.eventTitle }, function(eventFind) {
console.log("eventFind:", eventFind)
  amiando.event(eventFind.ids[0], function(event) {
console.log("event:", event)
    amiando.ticketFind({eventId: event.event.id}, function(tickets) {
console.log("ticketFind:", tickets)
      var readTicket = function(id, completed) {  
        if (!id) {
          return completed() 
        }
        fs.readFile(id+".ticket.json", "utf-8", function(err, data) { 
          if (err) { 
            amiando.ticket(id, function(data) { 
console.log("DONE")
              if (!data || !data.ticket)  {
                console.log("ticket:", data);
                readTicket(tickets.ids.pop(), completed)
                return
              }
              fs.writeFile(id+".ticket.json", JSON.stringify(data), "utf-8", function(err) {
                if (err) {
                  console.log("can to write:", id, err)
                }
                readTicket(tickets.ids.pop(), completed)
              })
            })
          } else {
            readTicket(tickets.ids.pop(), completed)
          }
        })
      }
      readTicket(tickets.ids.pop(), function() { 
console.log("READ all tickets")
      })
    })
  })

/*
var syncer = function(next) { 
  amiando.resync(1, function(result) { 
console.log("resyncer:", result)
    amiando.sync(result.nextId, function(result) {
console.log("sync:", result)
      })
    })
  })
}
  */
})

//syncer();
