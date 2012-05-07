/*
 * This is ugly and only for testing
 * I need to cleanup the api
 */
var http = require('http')
var fs = require('fs')

var CouchClient = require('./couch-client');

var config = JSON.parse(fs.readFileSync("config.json", "utf-8"))
console.log("CONFIG:", config)

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
  //console.log("buildUrl:", url)
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
//console.log("sync:", data)
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
//console.log("******:/api/event/find", params)
  this.call("GET", "/api/event/find", params, completed)
}

Amiando.prototype.ticketFind = function(params, completed) {
//console.log("******:/api/ticket/find", params)
  this.call("GET", "/api/ticket/find", params, completed);
}
Amiando.prototype.event = function(eventId, completed) {
//console.log("******:/api/event/", eventId)
  this.call("GET", "/api/event/"+ eventId, {}, completed);
}

Amiando.prototype.ticket = function(ticketId, completed) {
//console.log("******:/api/ticket/", ticketId)
  this.call("GET", "/api/ticket/"+ ticketId, {}, completed);
}

Amiando.prototype.allPayments = function(eventId, completed) {
//console.log("******:/api/ticket/", eventId)
  this.call("GET", "/api/event/"+ eventId + "/payments", {}, completed);
}

Amiando.prototype.allTickets = function(payementId, completed) {
//console.log("******:/api/payment/", payementId)
  this.call("GET", "/api/payment/"+ payementId + "/tickets", {}, completed);
}

var amiando = new Amiando();

var job = function () {
	var attendants = CouchClient(config.couchdb);
	attendants.request('PUT', '/attendants', function(err, result) {

		amiando.eventFind({ title: config.eventTitle }, function(eventFind) {
		console.log("eventFind:", eventFind)
			amiando.event(eventFind.ids[0], function(event) {
		console.log("event:", event)
				amiando.allPayments(event.event.id, function(payments) {
		console.log("allPayments:", payments)
					var readPayment = function(id, completed) {  
						if (!id) {
							return completed() 
						}
						amiando.allTickets(id, function(tickets) { 
							var readTicket = function(ticketId, completed) {
								if (!ticketId) {
									return completed() 
								}
								attendants.get(ticketId, function(err, data) { 
									if (err) { 
										amiando.ticket(ticketId, function(ticket) {
											ticket._id = ticket.ticket.displayIdentifier
											attendants.save(ticket, function(err) {
												if (err) {
													console.log("can to write:", id, err)
												}
												readTicket(tickets.tickets.pop(), completed)
											})
										})
									} else {
										readTicket(tickets.tickets.pop(), completed)
									}
								})
							}
							console.log("allTickets:", tickets);
							if (tickets && tickets.tickets) {
								readTicket(tickets.tickets.pop(), function() {
									readPayment(payments.payments.pop(), completed)
								})
							} else {
								readPayment(payments.payments.pop(), completed)
							}
							return
						})
					}
					readPayment(payments.payments.pop(), function() { 
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
	})
}

var timeouter = function() { 
	job();
	setTimeout(timeouter, 3600000);
}
timeouter();
