/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package models

import play.api.libs.json.{Json, OFormat}


sealed trait Group
case object CLang extends Group
case object BlockCoding extends Group
object Group{
  implicit val clangFormat = Json.format[CLang.type]
  implicit val blockCodingFormat = Json.format[BlockCoding.type]
  implicit val groupFormat: OFormat[Group] = Json.format[Group]
}
