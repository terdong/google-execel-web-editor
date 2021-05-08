/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package controllers

import models.{BlockCoding, CLang, Evaluation, EvaluationForm, Student}
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Editor

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton
class GoogleSheetController @Inject()(cc: ControllerComponents, editor: Editor) extends AbstractController(cc) with Logging with play.api.i18n.I18nSupport {
  implicit val ec = ExecutionContext.global

  val studentForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "name" -> nonEmptyText,
      "group" -> nonEmptyText
    )(Student.apply)(Student.unapply)
  )

  //  case class Evaluation(id:String, attendance:Boolean, grade:String, isAssignment:Boolean, evaluation:String, topic:Option[String] = None)

  val evaluationForm = Form(
    mapping(
      "topic" -> nonEmptyText,
      "body" -> nonEmptyText,
      "closing" -> nonEmptyText,
      "isAssignment" -> boolean,
      "evaluations" -> seq(
        mapping(
          "id" -> nonEmptyText,
          "attendance" -> boolean,
          "grade" -> nonEmptyText,
          "evaluation" -> nonEmptyText,
          "topic" -> default(text, ""),
          "isAssignment" -> default(boolean, false)
        )(Evaluation.apply)(Evaluation.unapply)
      )
    )(EvaluationForm.apply)(EvaluationForm.unapply)
  )

  def list = Action {
    Ok(views.html.list())
  }

  def insertStudent = Action { implicit request =>
    val student = studentForm.bindFromRequest().get
    editor.insertStudent(student)
    val result = student.group match {
      case BlockCoding => routes.GoogleSheetController.blockCodingClass
      case CLang => routes.GoogleSheetController.cLangClass
    }
    Redirect(result)
  }

  def insertEvaluation = Action { implicit request =>

    evaluationForm.bindFromRequest().fold(
      formWithErrors => {
        logger.debug(formWithErrors.errors.mkString("\n"))
        BadRequest(views.html.block_coding_class(editor.getStudentsInfo().filter(_.group == BlockCoding), formWithErrors))
      },
      evaluationForm => {
        logger.debug(evaluationForm.toString)

        val evaluationList: Seq[Evaluation] = evaluationForm.evaluations.map{ evaluation =>
          evaluation.copy(
            topic = evaluationForm.topic,
            evaluation = s"${evaluationForm.body}\n\n${evaluation.evaluation}\n\n${evaluationForm.closing}",
            isAssignment = evaluationForm.isAssignment
          )
        }

        editor.insertEvaluation(evaluationList).map{ result =>
          logger.info(result.mkString("\n"))
        }

        Redirect(routes.GoogleSheetController.blockCodingClass)
      }
    )

    /*val evaluations = evaluationForm.bindFromRequest().get
    logger.debug(evaluations.toString())
    Redirect(routes.GoogleSheetController.blockCodingClass)*/
  }

  def blockCodingClass = Action { implicit request =>
    Ok(views.html.block_coding_class(editor.getStudentsInfo().filter(_.group == BlockCoding), evaluationForm))
  }

  def cLangClass = Action {
    Ok(views.html.c_lang_class())
  }
  /*  def writeEvaluation = Action {

      val spreadsheetId = "1Eq56PDU55ogPVI472BRt4KRAHNrZH41I-yku-VXxDCU"
      val range = "시트1!A2:G"
  /*    getCredentials(HTTP_TRANSPORT).map { credentials =>
        val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
          .setApplicationName(APPLICATION_NAME).build()
        val response: ValueRange = service.spreadsheets().values()
          .get(spreadsheetId, range)
          .execute()

        val values= response.getValues.asScala
        values.foreach{ row: util.List[AnyRef] =>
          println(row.asScala.map{_.toString})
        }
      }*/
      Ok(views.html.write_evaluation())
    }*/
}
