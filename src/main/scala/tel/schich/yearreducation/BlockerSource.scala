package tel.schich.yearreducation

import java.time.LocalDate
import java.time.LocalDate._
import java.time.temporal.ChronoUnit._

import com.ning.http.client.Response
import play.api.libs.json.Json.parse
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

trait BlockerSource {
    def retrieveBlockers(year: Int): Future[Seq[Blocker]]
}

object BlockerSource {

    def as[A](response: Response)(implicit reads: Reads[A]): Option[A] = {
        (dispatch.as.String andThen parse andThen reads.reads)(response) match {
            case JsSuccess(o, _) =>
                Some(o)
            case e: JsError =>
                println(e)
                None
        }
    }

    def toScala[T](dispatchFuture: dispatch.Future[T]): Future[T] = {
        val promise = Promise[T]()
        dispatchFuture.onComplete(promise.complete)
        promise.future
    }

    def daysFrom(start: LocalDate): Stream[LocalDate] = start #:: daysFrom(start).map(_.plus(1, DAYS))
    def daysOfYear(year: Int): Stream[LocalDate] = daysFrom(ofYearDay(year, 1)).takeWhile(_.getYear == year)
}