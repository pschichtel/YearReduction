package tel.schich.yearreducation

import java.time.LocalDate
import java.time.LocalDate._
import java.time.temporal.ChronoUnit._

import dispatch.Http

import scala.concurrent.Future
import scala.concurrent.Future.sequence
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

object Reduce {

    def daysFrom(start: LocalDate): Stream[LocalDate] = start #:: daysFrom(start).map(_.plus(1, DAYS))

    def daysOfYear(year: Int): Stream[LocalDate] = daysFrom(ofYearDay(year, 1)).takeWhile(_.getYear == year)

    def annotate(year: Int, blockerSources: Seq[BlockerSource], includePreviousYear: Boolean = false): Future[Seq[(LocalDate, Seq[Blocker])]] = {

        val thisYearsBlockers = blockerSources.map(_.retrieveBlockers(year))
        val lastYearsBlockers = if (includePreviousYear) {
            blockerSources.map(_.retrieveBlockers(year - 1).recover({case _ => Nil}))
        } else Nil

        val annotatedYear: Future[Seq[(LocalDate, Seq[Blocker])]] = sequence(lastYearsBlockers ++ thisYearsBlockers).map(_.flatten).map {blockers =>
            val daysOfThisYear = daysOfYear(year).toList
            daysOfThisYear.map {day => (day, findBlockers(blockers, day))}
        }

        annotatedYear.onComplete {t =>
            t match {
                case Failure(e) =>
                    println(e)
                case _ =>
            }
            Http.shutdown()
        }

        annotatedYear
    }

    private def findBlockers(blockers: Seq[Blocker], date: LocalDate): Seq[Blocker] = {
        blockers.filter(_.isWithin(date))
    }

}
