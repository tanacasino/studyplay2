package services

import javax.inject.Inject

import scala.concurrent.ExecutionContext

import scalaz.{ -\/, \/, \/- }
import scalaz.{ Monad, EitherT }
import slick.dbio.DBIO

import repositories.TaskRepository
import models._

class TaskService @Inject() (taskRepo: TaskRepository)(implicit ec: ExecutionContext) {

  class DBIOMonad(implicit ec: ExecutionContext) extends Monad[DBIO] {

    def point[A](a: => A): DBIO[A] = DBIO.successful(a)

    def bind[A, B](fa: DBIO[A])(f: A => DBIO[B]): DBIO[B] = fa flatMap f

  }

  implicit def dbioInstance(implicit ec: ExecutionContext): Monad[DBIO] = new DBIOMonad

  def updateTask(taskId: Long, name: String, owner: User, versionNo: Long)(implicit user: User): EitherT[DBIO, Error, Task] = {
    for {
      _ <- EitherT(taskRepo.findTaskById(taskId))
      updated <- EitherT(taskRepo.update(taskId, name, owner, versionNo))
    } yield updated
  }

}

