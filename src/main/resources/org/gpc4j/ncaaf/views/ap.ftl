<html>

    <head>
        <title>${title}</title>
    </head>

    <body>
        <svg width="3500" height="2050">
            <#list weeks as week>
                <text x="${week.XPos?c}" 
                      y="25" fill="black">Week ${week.number} [${week.volatility}]
                    <title role="tooltip">[volatility score] Indication of the amount of position changes from prior week
                    </title>
                </text>
                
                <#assign place = 1>
                <#list week.teams as team>
                    <#assign record = getRecord(team) />
                    <image x="${team.CX?c}" 
                           y="${team.CY?c}"
                           width="70"
                           height="70"
                           xlink:href="${team.image}" >
                        <title role="tooltip">${team.name} (${place}) ${record}</title>
                    </image>
                    <#assign place = place + 1>
                </#list>
                      
                <#if !(week_has_next)>
                    <!-- Next week's opponent -->
                    <text x="${(week.XPos + 175)?c}" 
                        y="25" fill="black">Next Opponent
                    </text>
                    <#list week.teams as team>
                        <#if (team.next)??>
                            <#assign record = getRecord(team.next) />                            
                            <#if (team.nextGame)??>
                                <#assign nextDate = team.nextGame.date />
                            <#else>
                                <#assign nextDate = "" />
                            </#if>
                            <image x="${(team.CX + 200)?c}" 
                                  y="${team.CY?c}"
                                  width="70"
                                  height="70"
                                  xlink:href="${team.next.image}" >
                                <title role="tooltip">${team.next.name} ${record} ${nextDate}
                                    </title>
                            </image>
                        </#if>
                    </#list>
                </#if>
                    
            </#list>    
        </svg>
    </body>

</html>
