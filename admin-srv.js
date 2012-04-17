/*
 * This is ugly and only for testing
 * I need to cleanup the api
 */
var http = require('http')
var fs = require('fs')

var tickets = {}
var readTickets = function(completed) {
  fs.readdir(process.argv[1], function(dirs) {
    var readTicket = function(fname, completed) {
      if (fname.substr(-".ticket.json".length) == ".ticket.json") {
        var ticketId = fname.substr(0, fname.length-".ticket.json".length)
        if (!tickets[ticketId]) {
          fs.readFile(fname, "utf-8", function(err, data) { 
            tickets[ticketId] = JSON.parse(data)
          })
        } else {
          readTicket(dirs.pop(), completed)
        }
      } else {
        readTicket(dirs.pop(), completed)
      }
    }
    readTicket(dirs.pop(), completed)
  })
}

readTickets(function() {
  setTimeout(readTickets, 10000)
})

var elements = [
  "lastName",
  "displayIdentifier",
  "paymentId"
  "id",
  "email",
  "firstName"
  "identifier"
]
var srv = http.createServer(function(req, res) {
  res.writeHead(200, {'Content-Type': 'application/json'})
  var found = []
  for(var i in tickets) {
    var ticket = tickets[i].ticket
    for(var j = 0; j < elements.length; ++j) {
      var elem = ticket[elements[j]
      if (!elem) { continue; }

      var elems = elem.split(/\s+/) 
      for (var k = 0; k < elems.length; ++k) {
      }
    }
    if (found.length >= 20) { break }
  }
  res.end(JSON.stringify(found))
})
