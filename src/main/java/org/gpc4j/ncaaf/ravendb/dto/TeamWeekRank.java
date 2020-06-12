package org.gpc4j.ncaaf.ravendb.dto;

import lombok.Data;

/**
 *
 * @author Lyle T Harris
 */
@Data
public class TeamWeekRank {

  String id;
  String team;
  int rank;
  String year;
  String week;
  String date;

  public void setId() {
    id = "AP." + this.year + "." + this.week + "." + this.rank;
  }
}
