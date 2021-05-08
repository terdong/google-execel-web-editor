import org.scalatestplus.play.PlaySpec

import scala.collection.mutable

/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

class StackSpec extends PlaySpec{
  "A Stack" should {
    "pop values in last-in-first-out order" in {
      val stack = new mutable.Stack[Int]
      stack.push(1)
      stack.push(2)
      stack.pop() mustBe 2
      stack.pop() mustBe 1
    }
    "throw NoSuchElementException if an empty stack is popped" in {
      val emptyStack = new mutable.Stack[Int]
      a[NoSuchElementException] must be thrownBy {
        emptyStack.pop()
      }
    }
  }
}
