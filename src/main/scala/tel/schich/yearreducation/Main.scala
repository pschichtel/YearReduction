package tel.schich.yearreducation

import java.time.Instant.now
import java.time.ZoneId

import play.api.libs.json.Json.parse
import tel.schich.yearreducation.sources._

import scala.concurrent.Await
import scala.io.Source.fromFile
import scala.io.StdIn
import scala.concurrent.duration.Duration

object Main extends App {

    val timeZone = ZoneId.of("Europe/Berlin")

    val year =
        try {
            if (args.length >= 1) args(0).toInt
            else StdIn.readLine("Year: ").toInt
        } catch {
            case _: NumberFormatException =>
                now().atZone(timeZone).getYear
        }

    println(s"Using year $year!")

    val blockerSources = Seq(
        EuropaParkSeason,
        Weekends,
        new SchulferienOrg(parse(fromFile("locations.json").mkString).as[Map[String, Seq[String]]], timeZone)
    )

    val days = Await.result(Reduce.annotate(year, blockerSources, includePreviousYear = true), Duration.Inf)

    days foreach {case (day, blockers) =>
        val blockerNames =
            if (blockers.isEmpty) "Free"
            else {
                val names = blockers.map(_.name).take(4)
                val delta = blockers.length - names.length
                if (delta > 0) {
                    names.mkString(", ") + s", +$delta"
                } else names.mkString(", ")
            }
        println(s"$day: $blockerNames")
    }

}
