package tel.schich.yearreducation.sources

import java.time.DayOfWeek.{SATURDAY, SUNDAY}
import java.time.temporal.ChronoUnit.DAYS

import tel.schich.yearreducation.BlockerSource.daysOfYear
import tel.schich.yearreducation.{Blocker, BlockerSource}

import scala.concurrent.Future
import scala.concurrent.Future.successful

object Weekends extends BlockerSource {

    val WeekendDays = Set(SATURDAY, SUNDAY)

    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] =
        successful(daysOfYear(year)
            .filter(d => WeekendDays.contains(d.getDayOfWeek))
            .map(date => Blocker("Weekend", date, date.plus(1, DAYS))).toList)
}
