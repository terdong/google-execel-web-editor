/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package services

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model.{AppendValuesResponse, ValueRange}
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import models.{Evaluation, Student}
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Environment}

import java.io.{File, InputStreamReader}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util
import java.util.Collections
import javax.inject._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Try, Using}

trait Editor {
  def insertStudent(student: Student): Unit

  def updateStudent(student: Student): Unit

  def deleteStudent(student: Student): Unit

  def insertEvaluation(evaluations: Seq[Evaluation]): Future[Seq[AppendValuesResponse]]

  def getStudentsInfo(): Seq[Student]
}

@Singleton
class ExcelEditor @Inject()(env: Environment, config: Configuration) extends Editor {
  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
  val APPLICATION_NAME = "Google Sheet Editor"
  val TOKENS_DIRECTORY_PATH = "tokens"
  val CREDENTIALS_FILE_PATH = "credentials.json"
  val REGISTERED_STUDENTS_JSON_FILE_NAME = "students.json"
  val JSON_FACTORY = GsonFactory.getDefaultInstance
  val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)
  val credentialOption = getCredentials(HTTP_TRANSPORT)
  val savedStudentsFilePathOption = config.getOptional[String]("save.path").map(path => s"$path/$REGISTERED_STUDENTS_JSON_FILE_NAME")
  lazy val students: mutable.Set[Student] = savedStudentsFilePathOption.flatMap { filePath =>
    Using(Source.fromFile(new File(filePath))) { source =>
      val jsonString = source.getLines().mkString
      Json.parse(jsonString).as[List[Student]]
    }.toOption
  }.getOrElse(List.empty).to(collection.mutable.Set)

  lazy val sheetsOption = credentialOption.map { credentials =>
    val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
      .setApplicationName(APPLICATION_NAME).build()
    service.spreadsheets()
  }

  val rangeForGetting = "수업!B6:L"
  val rangeFormatForInserting = "수업!B%d"

  val customDateTimeFormat = DateTimeFormatter.ofPattern("yyMMdd")

  override def insertStudent(student: Student): Unit = {
    students += student
    saveStudentsAsFile()
  }

  override def updateStudent(student: Student): Unit = {
    students.find(_.id == student.id).foreach(students.remove(_))
    students.update(student, true)
    saveStudentsAsFile()
  }

  override def deleteStudent(student: Student): Unit = {
    students.remove(student)
  }

  override def insertEvaluation(evaluations: Seq[Evaluation]): Future[Seq[AppendValuesResponse]] = {
    sheetsOption.map { sheets =>
      evaluations.map(e => Future {
        val sheetId = e.id
        val response = sheets.values().get(sheetId, rangeForGetting).execute()
        val values = response.getValues.asScala
        val lastValue = values.last
        val today = LocalDate.now().format(customDateTimeFormat)
        val nextWeekDay = lastValue.get(1).toString
        val lecturer = "김동희"
        val studentName = lastValue.get(3).toString
        val attendance = e.attendance
        val topic = e.topic
        val grade = e.grade
        val episodeCount = lastValue.get(7).toString.toInt + 1
        val assignment = e.isAssignment match {
          case true => "o"
          case false => "x"
        }

        val evaluation = e.evaluation

        val body = new ValueRange().setValues(util.Arrays.asList(
          util.Arrays.asList(
            today.toString,
            nextWeekDay,
            lecturer,
            studentName,
            attendance,
            topic,
            grade,
            episodeCount,
            "",
            assignment,
            evaluation
          )
        ))
        val lastLine = values.size + 1 + 5
        sheets.values().append(sheetId, rangeFormatForInserting.format(lastLine), body).setValueInputOption("RAW").execute()
      })
    }.map(Future.sequence(_)).getOrElse(Future.successful(Seq.empty))
  }

  override def getStudentsInfo(): Seq[Student] = students.toSeq

  private def saveStudentsAsFile() = {
    val json: JsValue = Json.toJson(students)
    savedStudentsFilePathOption.foreach {
      reflect.io.File(_).writeAll(Json.stringify(json))
    }
  }

  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport) = { // Load client secrets.
    env.resourceAsStream(CREDENTIALS_FILE_PATH).map { in =>
      val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
      val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build
      val receiver = new LocalServerReceiver.Builder().setPort(12344).build
      new AuthorizationCodeInstalledApp(flow, receiver).authorize("sejong3@jamcoding.co.kr")
    }
  }
}
