package tel.schich.yearreducation.sources

import java.nio.charset.Charset
import java.time.Instant.ofEpochMilli
import java.time.{LocalDate, ZoneId}

import biweekly.Biweekly
import biweekly.io.TimezoneInfo
import biweekly.property.DateOrDateTimeProperty
import tel.schich.yearreducation.{Blocker, BlockerSource, Web}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class ICalSource(icalUrl: String, timeZoneId: ZoneId = BlockerSource.DefaultZoneId) extends BlockerSource {
    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] =
        ICalSource.retrieve(icalUrl, timeZoneId)
}

object ICalSource {
    val GermanCodePage: Charset = Charset.forName("ISO-8859-1")

    private def toLocalDate(zoneId: ZoneId, prop: DateOrDateTimeProperty): LocalDate =
        ofEpochMilli(prop.getValue.getTime).atZone(zoneId).toLocalDate

    def retrieve(link: String, zoneId: ZoneId): Future[Seq[Blocker]] = {

        Web.getString(link) map {
            case Some(ics) =>
                val ical = Biweekly.parse(ics).first()
                val timeZoneInfo: TimezoneInfo = ical.getTimezoneInfo
                val timeZone = Option(timeZoneInfo.getTimezones.asScala.headOption.getOrElse(timeZoneInfo.getDefaultTimezone))
                    .map(z => ZoneId.of(z.getTimeZone.getID))
                    .getOrElse(zoneId)
                val dateInZone = toLocalDate(timeZone, _: DateOrDateTimeProperty)

                for (event <- ical.getEvents.asScala.toSeq) yield {
                    val start = dateInZone(event.getDateStart)
                    val end = dateInZone(event.getDateEnd)
                    Blocker(event.getSummary.getValue, start, end)
                }
            case None =>
                Nil
        }
    }
}
