package org.gpc4j.ncaaf.ravendb.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Lyle T Harris (lyle.harris@cofense.com)
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ranking", propOrder = {
  "poll",
  "year",
  "week",
  "teams"
})
public class Poll implements Serializable {

  private static final long serialVersionUID = 1L;

  @XmlElement(required = true)
  protected String poll;
  @XmlElement(required = true)
  protected String year;
  protected int week;
  @XmlElement(required = true, name = "team")
  protected List<String> teams;

}
