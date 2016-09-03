/*
http://docs.groovy-lang.org/latest/html/api/groovy/util/slurpersupport/package-summary.html
*/

import redis.clients.jedis.*

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
@Grab(group='redis.clients', module='jedis', version='2.6.1')
def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
def slurper = new XmlSlurper(tagsoupParser)
def html = slurper.parse("http://sports.yahoo.com/ncaa/football/polls?poll=1")

table = html.'**'.findAll {
  it.'@id' == 'ysprankings-results-table'
}

def substitute = [:]
substitute["St\\."] = "State"
substitute["N\\."]  = "North"
substitute["S\\."]  = "South"
substitute["E\\."]  = "East"
substitute["W\\."]  = "West"

Jedis jedis = new Jedis("macmini.local",6388)
jedis.auth("welcome1")
jedis.select(10)

// Key to save rankings under
key = System.getProperty("KEY","sample.wk")

table.getAt(0).getAt(0).children().each {
  rank = it.children()[0].text()
    if(!"Rank".equals(rank)) {
    team = it.children()[1].text()
    substitute.each{ k, v -> 
      team = team.replaceAll(k,v)
    }
    // Remove ranking
    team = team.replaceAll(/\(\d+\)/,"")
    println rank + "\t" + team
    jedis.rpush(key,team)
  }
}
