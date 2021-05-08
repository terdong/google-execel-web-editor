import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model.{BatchUpdateSpreadsheetRequest, BatchUpdateValuesRequest, BooleanCondition, CellData, DataValidationRule, ExtendedValue, GridRange, RepeatCellRequest, Request, RowData, ValueRange}
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import org.scalatestplus.play.PlaySpec

import java.io.{File, InputStream, InputStreamReader}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util
import java.util.Collections
import scala.collection.mutable
import scala.io.Source
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Using

/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

class SheetSpec extends PlaySpec {
  val APPLICATION_NAME = "Google Sheet Editor"
  val TOKENS_DIRECTORY_PATH = "tokens"
  val CREDENTIALS_FILE_PATH = "credentials.json"
  val JSON_FACTORY = GsonFactory.getDefaultInstance
  val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS)
  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
  val spreadsheetId = "1Eq56PDU55ogPVI472BRt4KRAHNrZH41I-yku-VXxDCU"
  val spreadsheetId2 = "1TJmZAHHECG6POFGrnZTtrizRlLFypqvJw3C3EHfqQZA"
  val spreadsheetId3 = "1r0ynCV_ogPRs_vkIUHUra764WgubcNKYWWBL_2dox10"
  val range = "시트1!A2:G"

  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport) = { // Load client secrets.
    Using(getClass.getResourceAsStream(CREDENTIALS_FILE_PATH)) { in =>
      val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
      val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build
      val receiver = new LocalServerReceiver.Builder().setPort(12344).build
      new AuthorizationCodeInstalledApp(flow, receiver).authorize("sejong3@jamcoding.co.kr")
    }.toOption
  }

  val sheetsOption = getCredentials(HTTP_TRANSPORT).map { credentials =>

    val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
      .setApplicationName(APPLICATION_NAME).build()
    service.spreadsheets()
  }

  "A credentials file" must {
    "be get from conf directory" in {
      val is: InputStream = getClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
      println(Source.fromInputStream(is).mkString)
      // resource.getFile  mustBe CREDENTIALS_FILE_PATH
    }
  }

  "A sheet" must {
    "print all content" in {
      sheetsOption.map { sheets =>
        val request = sheets.values().get(spreadsheetId2, "수업!B6:L")
        val response = request.execute()
        println(response.getValues.asScala)
      }
    }
    "print last content" in {
      sheetsOption.map { sheets =>
        val request = sheets.values().get(spreadsheetId2, "수업!B6:L")
        val response = request.execute()
        val lastList = response.getValues.asScala.last
        println(response.getValues.asScala.last)
        val customDateTimeFormat = DateTimeFormatter.ofPattern("yyMMdd")
        val lastDate = LocalDate.parse(lastList.get(0).toString, customDateTimeFormat)
        println(s"next date = ${lastDate.plusWeeks(1)}")
      }
    }

    "print hyper link" in {
      sheetsOption.map { sheets =>
        val request: Sheets#Spreadsheets#Values#Get = sheets.values().get(spreadsheetId, "시트1!A2")
        //request.setFields("sheets/data/rowData/values/hyperlink")
        val response = request.execute()
        val values: util.List[util.List[AnyRef]] = response.getValues
        values.asScala.map(_.asScala).foreach { list: mutable.Seq[AnyRef] =>
          println(list)
        }
      }
    }

    "write a data at google sheet" in {
      sheetsOption.map { sheets =>
        val body = new ValueRange().setValues(util.Arrays.asList(
          util.Arrays.asList(
            "test1", "test2", 3, 4, 5
          )
        ))
        sheets.values().append(spreadsheetId3, "시트1!A2", body).setValueInputOption("RAW").execute()
      }
    }
    "write a data on the last line of google sheet" in {
      sheetsOption.map { sheets =>
        val response = sheets.values().get(spreadsheetId3, "시트1!A1:E").execute()
        println(response.getValues)
        //response.getValues.size() mustBe 3

        /*val cell = new CellData().setUserEnteredValue(new ExtendedValue().setBoolValue(true))
        val cellList = util.Arrays.asList(cell)
        val rowList = util.Arrays.asList(new RowData().setValues(cellList))*/

        val body = new ValueRange().setValues(util.Arrays.asList(
          util.Arrays.asList(
            "test1", "test2", 5, 6, false
          )
        ))
        val lastLine = response.getValues.size() + 1
        sheets.values().append(spreadsheetId3, s"시트1!A$lastLine", body).setValueInputOption("USER_ENTERED").execute()
      }
    }
    "add a checkbox with batch update" in {
      sheetsOption.map { sheets =>
        val response = sheets.values().get(spreadsheetId3, "시트1!A1:E").execute()
        val lastLine = response.getValues.size() + 1

        val request = new Request().setRepeatCell(new RepeatCellRequest()
          .setCell(new CellData() // set Cell
            .setDataValidation(new DataValidationRule().setCondition(new BooleanCondition().setType("BOOLEAN"))))
          .setRange(new GridRange() // Set Range
            .setSheetId(0) // sheet number see URL
            .setStartRowIndex(1) // starting row index
            .setEndRowIndex(10) // ending row index
            .setStartColumnIndex(5) // starting column Index
            .setEndColumnIndex(7)) // ending column index
          .setFields("DataValidation"))


        val requests = util.Arrays.asList(request)
        val batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests)

        sheets.batchUpdate(spreadsheetId3, batchUpdateRequest).execute()

        val body = util.Arrays.asList(new ValueRange().setValues(util.Arrays.asList(
          util.Arrays.asList(
            "=Sum(1+2)", new BooleanCondition().setType("BOOLEAN")
          )
        )).setRange("시트1!H1:I2"))

        val requestBody = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(body)
        //sheets.values().batchUpdate(spreadsheetId3, requestBody).execute()


        //sheets.values().append(spreadsheetId3, s"시트1!A$lastLine")
        //sheets.values().get(spreadsheetId3, "시트1!A1:E")
      }
    }
    "print all contents from spreadsheetId3" in {
      sheetsOption.map { sheets =>
        val request = sheets.values().get(spreadsheetId3, "시트1!B1:C3")
        val response = request.execute()
        println(response.getValues.asScala)
      }
    }
  }
}
