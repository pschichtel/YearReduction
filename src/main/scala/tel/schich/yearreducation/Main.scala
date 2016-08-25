package tel.schich.yearreducation

import java.time.{Instant, LocalDate}

import dispatch.Http
import tel.schich.yearreducation.BlockerSource.daysOfYear
import tel.schich.yearreducation.sources.{EuropaParkSeason, ICalSource, SmartnoobApi, Weekends}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn
import scala.util.Failure

/**
  * Created by phillip on 25.08.16.
  */
object Main extends App {


    def schulferienorg(land: String, file: String, year: Int) = s"http://www.schulferien.org/media/ical/$land/${file}_$year.ics"

    val year =
        try {
            if (args.length >= 1) args(0).toInt
            else StdIn.readLine("Year: ").toInt
        } catch {
            case _: NumberFormatException =>
                Instant.now().atZone(SmartnoobApi.GermanTimeZone).getYear
        }

    println(s"Using year $year!")

    val blockerSources = Seq(
        SmartnoobApi,
        EuropaParkSeason,
        Weekends,
        new ICalSource(schulferienorg("frankreich", "ferien_strassburg", year)),
        new ICalSource(schulferienorg("frankreich", "feiertage", year)),
        new ICalSource(schulferienorg("schweiz", "feiertage", year)),
        new ICalSource(schulferienorg("schweiz", "ferien_basel-stadt_alle-schulen", year))
    )

    def findBlockers(blockers: Seq[Blocker])(date: LocalDate): List[String] = {
        blockers.foldLeft(List.empty[String]) {(names, b) =>
            if (b.isWithin(date)) b.name :: names
            else names
        }
    }

    Future.sequence(blockerSources.map(_.retrieveBlockers(year))).map { blockers =>
        val blockedBy: (LocalDate) => List[String] = findBlockers(blockers.flatten)

        daysOfYear(year) map { d => (d, blockedBy(d)) } /*filter(_._2.isEmpty)*/ foreach println
    }.onComplete {t =>
        t match {
            case Failure(e) =>
                println(e)
            case _ =>
        }
        Http.shutdown()
    }

}
