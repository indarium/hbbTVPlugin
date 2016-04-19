package controllers.util

import controllers.webjazz.WebjazzController._
import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-04-19
  */
object ControllerUtil {

  /**
    * @return http 400, {"status": false}
    */
  def KO = {
    BadRequest(Json.obj("status" -> false))
      .withHeaders(CONTENT_TYPE -> "application/json")
  }

  /**
    * @return http 400, {"status": "unsuccessful"}
    */
  def Unsuccessful400 = {
    BadRequest(Json.obj("status" -> "unsuccessful"))
      .withHeaders(CONTENT_TYPE -> "application/json")
  }

  /**
    * @return http 404, {"status": "unsuccessful"}
    */
  def Unsuccessful404 = {
    NotFound(Json.obj("status" -> "unsuccessful"))
      .withHeaders(CONTENT_TYPE -> "application/json")
  }

  /**
    * @return http 500, {"status": "unsuccessful"}
    */
  def Unsuccessful500 = {
    InternalServerError(Json.obj("status" -> "unsuccessful"))
      .withHeaders(CONTENT_TYPE -> "application/json")
  }

  /**
    * @return http 200, {"status": "OK"}
    */
  def statusOK = {
    Ok(Json.obj("status" -> "OK"))
      .withHeaders(CONTENT_TYPE -> "application/json")
  }

}
