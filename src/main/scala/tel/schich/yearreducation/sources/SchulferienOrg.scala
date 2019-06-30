package tel.schich.yearreducation.sources

import java.time.ZoneId

import tel.schich.yearreducation.BlockerSource._
import tel.schich.yearreducation.sources.SchulferienOrg.mkLink
import tel.schich.yearreducation.{Blocker, BlockerSource}

import scala.concurrent.Future
import scala.concurrent.Future.sequence
import scala.concurrent.ExecutionContext.Implicits.global

class SchulferienOrg(locations: Map[String, Seq[String]], timeZoneId: ZoneId = DefaultZoneId) extends BlockerSource{

    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] = {

        val urls = locations.toSeq.flatMap {case (country, parts) =>
            parts.map(p => mkLink(country, "ferien_" + p, year)) :+ mkLink(country, "feiertage", year)
        }

        sequence(urls.map(url => ICalSource.retrieve(url, timeZoneId))).map(_.flatten)
    }
}

object SchulferienOrg {
    def mkLink(land: String, file: String, year: Int) = s"https://www.schulferien.org/media/ical/$land/${file}_$year.ics"
}
