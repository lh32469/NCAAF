package org.gpc4j.ncaaf;

import org.gpc4j.ncaaf.jaxb.Team;


/**
 * Extend functionality of basic JAXB POJO.
 *
 * @author Lyle T Harris
 */
public class XTeam extends Team {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Team) {
            Team other = (Team) obj;
            return other.getName().equals(this.name);
        }
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        return this.name.hashCode();
    }


}
