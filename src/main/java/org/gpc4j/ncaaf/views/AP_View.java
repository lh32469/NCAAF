package org.gpc4j.ncaaf.views;

import io.dropwizard.views.View;
import java.util.List;
import org.gpc4j.ncaaf.jaxb.Week;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
public class AP_View extends View {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP_View.class);

    private List<Week> weeks;

    private String title;


    public AP_View() {
        super("ap.ftl");
    }


    public List<Week> getWeeks() {
        LOG.debug(weeks.size() + "");
        return weeks;
    }


    public void setWeeks(List<Week> weeks) {
        this.weeks = weeks;
    }


    /*
     <#list weeks as week>
     <h3>Wk${week.number}</h3>
     <#list week.teams as team>
     <p/>${team.name}
     </#list>
       
     </#list>
    
     <#assign x = week.xPos>
     <text x="${x?c}" 
     y="25" fill="black">Wk${week.number} [${week.volatility}]
     <title role="tooltip">[volatility score] Indication of the amount of position changes from prior week
     </title>
     </text>
     */
    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


}
