package org.gpc4j.ncaaf;

import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.documents.session.OrderingType;
import org.gpc4j.ncaaf.ravendb.dto.Poll;
import org.gpc4j.ncaaf.ravendb.dto.TeamWeekRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Lyle T Harris
 */
public class PollProvider {

  @Inject
  IDocumentStore docStore;

  final static private Logger LOG
      = LoggerFactory.getLogger(PollProvider.class);

  public Stream<Poll> getPolls(int year) {
    List<Poll> polls;

    try (IDocumentSession session = docStore.openSession()) {
      polls = session.query(Poll.class)
          .whereEquals("year", String.valueOf(year))
          .addOrder("week", false, OrderingType.LONG)
          .addOrder("rank", false, OrderingType.LONG)
          .toList();
    }

    return polls.stream();
  }

  public Stream<Poll> getPolls2(int year) {

    LOG.debug("Year: " + year);

    List<Poll> polls = new ArrayList<>();
    List<TeamWeekRank> weeks;

    try (IDocumentSession session = docStore.openSession()) {

      weeks = session.query(TeamWeekRank.class)
          .whereEquals("year", String.valueOf(year))
          .addOrder("year", false, OrderingType.LONG)
          .addOrder("week", false, OrderingType.LONG)
          .addOrder("rank", false, OrderingType.LONG)
          .toList();
    }

    int max = 20;

    for (int i = 0; i < max; i++) {

      final String week = String.valueOf(i + 1);

      Poll poll = new Poll();
      poll.setPoll("AP");
      poll.setTeams(new LinkedList<>());
      poll.setYear(String.valueOf(year));
      poll.setWeek(i);
      polls.add(poll);

      weeks.stream()
          .filter(w -> w.getWeek().equals(week))
          .forEach(w -> poll.getTeams().add(w.getTeam()));
    }

    return polls.stream();
  }

}
