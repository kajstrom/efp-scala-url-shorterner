package controllers

import javax.inject._

import com.redis.RedisClient
import play.api._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import play.api.data.Forms._

import scala.util.Random

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val urlForm = Form(
    mapping(
      "url" -> text
    )(URLData.apply)(URLData.unapply)
  )

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(urlForm))
  }

  def add = Action { implicit request =>
    val errorFunction = { formWithErrors: Form[URLData] =>
      Redirect(routes.HomeController.index()).flashing("info" -> "URL was not added! Try again")
    }

    val successFunction = { data: URLData =>
      val key = Random.alphanumeric.take(10).mkString
      println(key)

      val r = new RedisClient("localhost", 6379)
      r.hmset("todo", Map(key -> data.url))
      r.disconnect

      Redirect(routes.HomeController.index()).flashing("info" -> "URL added!")
    }

    val formValidationResult = urlForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}

case class URLData(url: String)