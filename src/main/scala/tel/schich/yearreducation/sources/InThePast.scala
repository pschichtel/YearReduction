package tel.schich.yearreducation.sources

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import tel.schich.yearreducation.{Blocker, BlockerSource, Reduce}

import scala.concurrent.Future

object InThePast extends BlockerSource {
    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] = {
        val today = LocalDate.now(BlockerSource.DefaultZoneId)
        Future.successful(Reduce.daysOfYear(year)
            .filter(d => d.isBefore(today))
            .map(date => Blocker("In The Past", date, date.plus(1, DAYS))).toList)
    }
}
