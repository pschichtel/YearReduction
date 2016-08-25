package tel.schich.yearreducation.sources

import java.time.Instant.ofEpochSecond
import java.time.ZoneId

import dispatch.{Http, Req, url}
import play.api.libs.json.Json.reads
import tel.schich.yearreducation.BlockerSource.{as, toScala}
import tel.schich.yearreducation.{Blocker, BlockerSource}

import scala.concurrent.Future.sequence
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by phillip on 25.08.16.
  */
object SmartnoobApi extends BlockerSource {

    val AllStates = Seq("BW", "BY", "BE", "BB", "HB", "HH", "HE", "MV", "NI", "NW", "RP", "SL", "SN", "ST", "SH", "TH")
    val GermanTimeZone = ZoneId.of("Europe/Berlin")
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
        val schoolHolidays = AllStates.map(s => germanSchoolHolidaysFor(s, year)) :+ allGermanHolidaysFor(year)

        sequence(schoolHolidays).map { responses =>

            responses.collect({case Some(r) => r}).flatMap {response =>
                response.daten.map {data =>
                    Blocker(data.title,
                        ofEpochSecond(data.beginn).atZone(GermanTimeZone).toLocalDate,
                        ofEpochSecond(data.ende).atZone(GermanTimeZone).toLocalDate)
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
