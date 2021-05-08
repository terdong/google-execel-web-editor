import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.play.WsScalaTestClient

/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

abstract class MyPlaySpec extends AnyFlatSpec with Matchers with OptionValues with WsScalaTestClient {

}
