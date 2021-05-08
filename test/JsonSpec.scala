import models.{BlockCoding, CLang, Student}
import play.api.libs.json.{JsValue, Json}

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Using

/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

class JsonSpec extends MyPlaySpec {
  "A student" should "convert from object to json" in {
    import play.api.libs.json._
    val sampleStudent = Student("sample-id", "김동희", CLang)
    val json = Json.toJson(sampleStudent)
    println(json)
  }
  it should "convert from json to object" in {
    val sampleStudent = Student("sample-id", "김동희", CLang)
    val jsonString: JsValue = Json.parse(
      """{"id":"sample-id","name":"김동희","group":{"_type":"models.CLang"}}"""
    )
    Json.fromJson[Student](jsonString).map{ student =>
      student shouldBe sampleStudent
    }
  }
  it should "be saved as a file from json" in {
    val jsonString: JsValue = Json.parse(
      """{"id":"sample-id1","name":"김동희","group":{"_type":"models.CLang"}}"""
    )
/*    val file = new File("student.json")
      Using(new PrintWriter(file)){ pw =>
        pw.write(jsonString.toString())
        pw.close()
      }*/
    reflect.io.File("student1.json").writeAll(Json.stringify(jsonString))
  }
  it should "be JsValue from a file" in {

    val file = new File("student.json")
    val jsonString = Source.fromFile(file).getLines().mkString
    val json = Json.parse(jsonString)

    val expectedJson: JsValue = Json.parse(
      """{"id":"sample-id","name":"김동희","group":{"_type":"models.CLang"}}"""
    )
    json shouldBe expectedJson
  }

  "Students" should "convert from object to json" in {
    val students = List(
      Student("sample-id1", "김동희", CLang),
      Student("sample-id2", "홍길동", BlockCoding),
        Student("sample-id3", "김영희", CLang)
    )
    val json = Json.toJson(students)
    println(json)
  }
  it should "convert from json block_coding_class to object block_coding_class" in {
    val students = List(
      Student("sample-id1", "김동희", CLang),
      Student("sample-id2", "홍길동", BlockCoding),
      Student("sample-id3", "김영희", CLang)
    )
    val json = Json.toJson(students)
    val convertedStudents = Json.parse(Json.stringify(json)).as[List[Student]]

    println(convertedStudents)

    convertedStudents shouldBe students
  }
}
