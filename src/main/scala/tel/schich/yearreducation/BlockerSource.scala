package tel.schich.yearreducation

import java.time.{LocalDate, ZoneId}
import java.util.TimeZone

import scala.concurrent.{Future, Promise}

trait BlockerSource {
    def retrieveBlockers(year: Int): Future[Seq[Blocker]]
}

object BlockerSource {
    val DefaultZoneId: ZoneId = ZoneId.of(TimeZone.getDefault.getID)
}