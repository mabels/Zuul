/*
 * This is ugly and only for testing
 * I need to cleanup the api
 */
var http = require('http')
var fs = require('fs')
var csv = require('csv')

var CouchClient = require('./couch-client');

var config = JSON.parse(fs.readFileSync("config.json", "utf-8"))
console.log("CONFIG:", config)

var attendants = CouchClient(config.couchdb);
attendants.request('PUT', '/attendants', function(err, result) {
  csv().from.path(config['csv']["file"], config["csv"]).on('record', function(row,index){ 
    if (index == 0) {
      return
    }
    if (!row[3]) {
      return
    }
    attendants.get(row[3], function(err, data) { 
      data = data || { _id : row[3] }
      data['ticket']={
       "lastName":row[2],
       "firstName":row[1],
       "email":row[0],
       "displayIdentifier":row[3],
       "imported":new Date()}
      attendants.save(data, function(err) {
        if (err) {
          console.log("can to write:", data._id, err)
        }
        console.log("TICKET-SAVE:"+data._id)
      })
    })
  })
})
