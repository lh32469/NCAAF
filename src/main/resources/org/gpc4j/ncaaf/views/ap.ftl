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
                    <#assign opponent = getOpponent(week.number,team) />
                    <#assign result = getResult(week.number,team) />
                    <image x="${team.CX?c}" 
                           y="${team.CY?c}"
                           width="70"
                           height="70"
                           xlink:href="${team.image}" >
                        <title role="tooltip">${team.name} (${place}) ${record}</title>
                    </image>
                    <image x="${(team.CX+100)?c}" 
                           y="${(team.CY+20)?c}"
                           width="40"
                           height="40"
                           xlink:href="${opponent.image}" >
                        <title role="tooltip">${result}</title>
                    </image>
                    <#assign place = place + 1>
                </#list>
                    
            </#list>
                    
            <#list paths as path>
                <path d="${path.d}" stroke="${path.stroke}" fill="none" />
            </#list>

        </svg>
    </body>

</html>
