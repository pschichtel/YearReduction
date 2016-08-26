package tel.schich.yearreducation.sources

import java.nio.charset.Charset
import java.time.Instant.ofEpochMilli
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import biweekly.Biweekly
import biweekly.io.TimezoneInfo
import biweekly.property.DateOrDateTimeProperty
import tel.schich.yearreducation.{Blocker, BlockerSource}
import dispatch._
import tel.schich.yearreducation.BlockerSource.{as => _, _}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ICalSource(icalUrl: String, timeZoneId: ZoneId = DefaultZoneId) extends BlockerSource {
    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] =
        ICalSource.retrieve(icalUrl, timeZoneId)
}

object ICalSource {
    val GermanCodePage: Charset = Charset.forName("ISO-8859-1")
    val Utf8: Charset = Charset.forName("UTF-8")

    private def toLocalDate(zoneId: ZoneId, prop: DateOrDateTimeProperty): LocalDate =
        ofEpochMilli(prop.getValue.getTime).atZone(zoneId).toLocalDate

    def retrieve(link: String, zoneId: ZoneId): Future[Seq[Blocker]] = {
        toScala(Http(url(link) OK as.String)) map {source =>

            val ical = Biweekly.parse(new String(source.getBytes(GermanCodePage), Utf8)).first()
            val timeZoneInfo: TimezoneInfo = ical.getTimezoneInfo
            val timeZone = Option(timeZoneInfo.getTimezones.asScala.headOption.getOrElse(timeZoneInfo.getDefaultTimezone))
                .map(z => ZoneId.of(z.getTimeZone.getID))
                .getOrElse(zoneId)
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
