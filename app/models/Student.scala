/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package models

import play.api.libs.json.{Json, OFormat}

case class Student(id: String, name: String, group: Group)

object Student {
  implicit val StudentFormat: OFormat[Student] = Json.format[Student]
  def apply(id: String, name: String, group: Group): Student = new Student(id, name, group)
  def apply(id: String, name: String, group: String): Student = new Student(id, name, group match {
    case "블럭코딩" => BlockCoding
    case "C언어" => CLang
  })

  def unapply(arg: Student): Option[(String, String, String)] = Some(arg.id, arg.name, arg.group match{
    case BlockCoding => "블럭코딩"
    case CLang => "C언어"
  })
}
