import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

class FutureSpec extends MyPlaySpec {

  def randomInt(from: Int, to: Int): Int = {
    Thread.sleep(3000)
    (from + (to - from + 1) * Math.random()).toInt
  }


  "future seq" should "process 100 futures in sequential" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val futures: Seq[Future[Int]] = List.fill(100) { Future { randomInt(100, 200) } }
    futures.map{ f: Future[Int] =>
      println(s"${f.toString} start")
      val number = Await.result(f, Duration.Inf)
      println(s"${f.toString} result ${number}")
    }
  }

  "concurrency future seq" should "process 100 futures in parallel" in {
    implicit val ec = ExecutionContext
      .fromExecutor(Executors.newCachedThreadPool())

    val futures: Seq[Future[Int]] = List.fill(100) { Future { randomInt(100, 200) } }
    val f: Future[Seq[Int]] = Future.sequence(futures)
    val numbers = Await.result(f, Duration.Inf)
    println(numbers)
  }
}
