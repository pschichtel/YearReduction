package tel.schich.yearreducation.sources

import java.nio.charset.Charset
import java.time.Instant.ofEpochMilli
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.io.TimezoneInfo
import biweekly.property.DateOrDateTimeProperty
import tel.schich.yearreducation.{Blocker, BlockerSource}
import dispatch._
import tel.schich.yearreducation.BlockerSource.toScala

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ICalSource(icalUrl: String) extends BlockerSource {
    val GermanTimezone: ZoneId = ZoneId.of("Europe/Berlin")
    val GermanCodePage: Charset = Charset.forName("ISO-8859-15")
    val Utf8: Charset = Charset.forName("UTF-8")

    private def toLocalDate(zoneId: ZoneId, prop: DateOrDateTimeProperty): LocalDate =
        ofEpochMilli(prop.getValue.getTime).atZone(zoneId).toLocalDate

    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] = {
        toScala(Http(url(icalUrl) OK as.String)) map {source =>

            val ical = Biweekly.parse(new String(source.getBytes(GermanCodePage), Utf8)).first()
            val timeZoneInfo: TimezoneInfo = ical.getTimezoneInfo
            val timeZone = Option(timeZoneInfo.getTimezones.asScala.headOption.getOrElse(timeZoneInfo.getDefaultTimezone))
                .map(z => ZoneId.of(z.getTimeZone.getID))
                .getOrElse(GermanTimezone)
            val dateInZone = toLocalDate(timeZone, _: DateOrDateTimeProperty)

            for (event <- ical.getEvents.asScala) yield {
                val start = dateInZone(event.getDateStart)
                val end = dateInZone(event.getDateEnd)
                Blocker(event.getSummary.getValue, start, end)
            }
        } recover {
            case e =>
                e.printStackTrace(System.err)
                Seq.empty
        }
    }
}
