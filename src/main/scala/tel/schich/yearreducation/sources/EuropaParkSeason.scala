package tel.schich.yearreducation.sources

import java.time.LocalDate._
import java.time.Month.{DECEMBER, JANUARY, MARCH, NOVEMBER}

import tel.schich.yearreducation.{Blocker, BlockerSource}

import scala.concurrent.Future
import scala.concurrent.Future.successful

object EuropaParkSeason extends BlockerSource {
    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] = successful(Seq(
        Blocker("Not open yet", of(year, JANUARY, 1), of(year, MARCH, 19)),
        Blocker("Already closed", of(year, NOVEMBER, 6), of(year, DECEMBER, 31))
    ))
}
