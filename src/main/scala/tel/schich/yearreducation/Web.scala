package tel.schich.yearreducation

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import play.api.libs.json.{Json, Reads}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object Web {

    private implicit val backend = AsyncHttpClientFutureBackend()

    def getString(url: String): Future[Option[String]] = {

        quick.sttp.get(uri"$url").send() map { r =>
            r.body.toOption
        }
    }

    def getJson[T](url: String)(implicit reads: Reads[T]): Future[Option[T]] = {
        getString(url) map {
            _.map(s => Json.parse(s).as)
        }
    }

}
