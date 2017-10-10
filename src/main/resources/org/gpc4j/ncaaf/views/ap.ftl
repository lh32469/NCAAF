<html>

    <head>
        <title>${title}</title>
    </head>

    <body>
        <script type="text/javascript">
         
            function MakeTransparent2(evt) {
                evt.target.setAttributeNS(null,"opacity","0.5");
            }

            function MakeOpaque2(evt) {
                evt.target.setAttributeNS(null,"opacity","1");
            }
           
        </script>
        
        <script>
            (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
            (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
            })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

            ga('create', 'UA-10033624-3', 'auto');
            ga('send', 'pageview');
        </script>
        
        <svg width="4500" height="2050">

            <#list paths as path>
                <path d="${path.d}" stroke="${path.stroke}"  stroke-width="${path.strokeWidth}" fill="none" />
            </#list>

            <#list weeks as week>
                <#if week.number = 13>
                    <text x="${(week.XPos-10)?c}" y="25" fill="black">Conf Championships
                    </text>
                <#elseIf week.number = 14>
                    <text x="${week.XPos?c}" y="25" fill="black">Bowl Games
                    </text>
                <#elseIf week.number = 15>
                    <text x="${(week.XPos-10)?c}" y="25" fill="black">Final Results
                    </text>
                <#else>
                    <text x="${week.XPos?c}" 
                          y="25" fill="black">Week ${week.number} [${week.volatility}]
                        <title role="tooltip">[volatility score] Indication of the amount of position changes from prior week
                        </title>
                    </text>
                </#if>
                
                <#assign place = 1>
                <#list week.teams as team>
                    <#assign record = getRecord(week.number,team) />
                    <#assign opponent = getOpponent(week.number,team) />
                    <#assign result = getResult(week.number,team) />
                    <#assign resultAsText = getResultAsText(week.number,team) />
                    <image x="${team.CX?c}" 
                           y="${team.CY?c}"
                           width="70"
                           height="70"
                           onmouseover="MakeTransparent2(evt)"
                           onmouseout="MakeOpaque2(evt)"
                           xlink:href="${team.image}" >
                        <title role="tooltip">${team.name} (${place}) ${record}</title>
                    </image>
                    <#if week.number < 14 && newGamesPosted(week.number) >
                        <image x="${(team.CX+100)?c}"
                               y="${(team.CY+20)?c}"
                               width="40"
                               height="40"
                               onclick="alert('${resultAsText}')"
                               xlink:href="${opponent.image}" >
                            <title role="tooltip">${result}</title>
                        </image>
                    <#elseIf week.number == 14 >
                        <image x="${(team.CX+100)?c}"
                               y="${(team.CY+20)?c}"
                               width="40"
                               height="40"
                               onclick="alert('${resultAsText}')"
                               xlink:href="${opponent.image}" >
                            <title role="tooltip">${result}</title>
                        </image>
                    </#if>
                    <#assign place = place + 1>
                </#list>
                    
            </#list>
        </svg>
    </body>

</html>
