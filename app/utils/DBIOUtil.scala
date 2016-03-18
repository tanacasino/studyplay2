package utils

import scala.concurrent.ExecutionContext

import scalaz.{ -\/, \/, \/- }
import scalaz.{ Monad, EitherT }
import slick.dbio.DBIO

object DBIOUtil {

  private class DBIOMonad(implicit ec: ExecutionContext) extends Monad[DBIO] {

    def point[A](a: => A): DBIO[A] = DBIO.successful(a)

    def bind[A, B](fa: DBIO[A])(f: A => DBIO[B]): DBIO[B] = fa flatMap f

  }

  implicit def dbioInstance(implicit ec: ExecutionContext): Monad[DBIO] = new DBIOMonad

}
