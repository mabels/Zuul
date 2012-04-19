/*
 * This is ugly and only for testing
 * I need to cleanup the api
 */
var http = require('http')
var fs = require('fs')

var config = JSON.parse(fs.readFileSync("config.json", "utf-8"))
console.log("CONFIG:", config)

var tickets = {}
var readTickets = function(completed) {
  var cnt = 0;
  fs.readdir(process.argv[2], function(error, dirs) {
//console.log("dirs:", process.argv[2], arguments)
    var readTicket = function(fname, completed) {
      if (!fname)  {
        completed(cnt);
        return;
      }
      if (fname.substr(-".ticket.json".length) == ".ticket.json") {
        ++cnt;
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
  console.log("readcompleted:", cnt)
  setTimeout(readTickets, 10000)
})

var elements = [
  "lastName",
  "displayIdentifier",
  "paymentId",
  "id",
  "email",
  "firstName",
  "identifier"
]
var reDisplayIdentifier = new RegExp(".*\\/\\(\\d{4}\\)(\\d{4}\\)(\\d{4}\\)$")
var srv = http.createServer(function(req, res) {
  var result = reDisplayIdentifier.exec(req.url)
  if (result) {
    var my = result.join('-')
    console.log("FOUND", my)
   
    for(var i in tickets) {
      var elem = ticket[elements[j]]
      if (elem.displayIdentifier == my) {
        res.writeHead(200, {'Content-Type': 'application/json'})
        res.end(JSON.stringify(elem))
        return;
      }
    }
    res.writeHead(404, {'Content-Type': 'application/json'})
    res.end(JSON.stringify({ error: "not found", value: my}))
    return;
  }
  res.writeHead(200, {'Content-Type': 'application/json'})
  var found = []
  for(var i in tickets) {
    var ticket = tickets[i].ticket
    for(var j = 0; j < elements.length; ++j) {
      var elem = ticket[elements[j]]
      if (!elem) { continue; }
    }
    if (found.length >= 20) { break }
  }
  res.end(JSON.stringify(found))
})
srv.listen(config.admin.port, config.admin.bind) 

