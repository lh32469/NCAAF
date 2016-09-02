/*
http://docs.groovy-lang.org/latest/html/api/groovy/util/slurpersupport/package-summary.html
*/

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
def slurper = new XmlSlurper(tagsoupParser)
def html = slurper.parse("http://sports.yahoo.com/ncaa/football/polls?poll=1")

table = html.'**'.findAll {
  it.'@id' == 'ysprankings-results-table'
}

//println table
//println "table size: " + table.size()
//println "table getAt: " + table.getAt(0)

//println "GetAt size: " + table.getAt(0).size()
//println "GetAt text: " + table.getAt(0).getAt(0)
//println "GetAt children: " + table.getAt(0).getAt(0).children()

def substitute = [:]
substitute["St\\."] = "State"
substitute["N\\."]  = "North"

table.getAt(0).getAt(0).children().each {
  print it.children()[0].text()
  team = it.children()[1].text()
  substitute.each{ k, v -> 
    team = team.replaceAll(k,v)
  }
  // Remove ranking
  team = team.replaceAll(/\(\d+\)/,"")
  println "  " + team
}
