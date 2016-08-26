package tel.schich.yearreducation

import java.time.{LocalDate, ZoneId}
import java.time.LocalDate._
import java.time.temporal.ChronoUnit._
import java.util.TimeZone

import com.ning.http.client.Response
import play.api.libs.json.Json.parse
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

trait BlockerSource {
    def retrieveBlockers(year: Int): Future[Seq[Blocker]]
}

object BlockerSource {

    val DefaultZoneId = ZoneId.of(TimeZone.getDefault.getID)

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
}