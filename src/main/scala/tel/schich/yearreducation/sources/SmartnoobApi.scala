package tel.schich.yearreducation.sources

import java.time.Instant.ofEpochSecond
import java.time.ZoneId
import java.util.TimeZone

import dispatch.{Http, Req, url}
import play.api.libs.json.Json.reads
import tel.schich.yearreducation.BlockerSource.{DefaultZoneId, as, toScala}
import tel.schich.yearreducation.{Blocker, BlockerSource}

import scala.concurrent.Future.sequence
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by phillip on 25.08.16.
  */
class SmartnoobApi(states: Seq[String] = SmartnoobApi.AllStates, timeZoneId: ZoneId = DefaultZoneId) extends BlockerSource {

    val SmartnoobBase = "http://api.smartnoob.de/ferien/v1"

    object Holydays {
        implicit val read = reads[Holydays]
    }

    object HolidaysResponse {
        implicit val read = reads[HolidaysResponse]
    }

    case class HolidaysResponse(error: Int, jahr: String, daten: Seq[Holydays])
    case class Holydays(title: String, beginn: Long, ende: Long)


    override def retrieveBlockers(year: Int): Future[Seq[Blocker]] = {
        val schoolHolidays = states.map(s => germanSchoolHolidaysFor(s, year)) :+ allGermanHolidaysFor(year)

        sequence(schoolHolidays).map { responses =>

            responses.collect({case Some(r) => r}).flatMap {response =>
                response.daten.map {data =>
                    Blocker(data.title,
                        ofEpochSecond(data.beginn).atZone(timeZoneId).toLocalDate,
                        ofEpochSecond(data.ende).atZone(timeZoneId).toLocalDate)
                }
            }

        }
    }

    private def responseFor(req: Req): Future[Option[HolidaysResponse]] =
        toScala(Http(req OK as[HolidaysResponse]))

    private def germanSchoolHolidaysFor(state: String, year: Int) =
        responseFor(url(s"$SmartnoobBase/ferien/?bundesland=$state&jahr=$year"))

    private def allGermanHolidaysFor(year: Int) =
        responseFor(url(s"$SmartnoobBase/feiertage/?bundesland=DE&jahr=$year"))
}

object SmartnoobApi {
    val AllStates = Seq("BW", "BY", "BE", "BB", "HB", "HH", "HE", "MV", "NI", "NW", "RP", "SL", "SN", "ST", "SH", "TH")
}