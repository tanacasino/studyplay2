package services

import javax.inject.Inject

import scala.concurrent.ExecutionContext

import scalaz.{ -\/, \/, \/- }
import scalaz.{ Monad, EitherT }
import slick.dbio.DBIO

import repositories.TaskRepository
import models._
import utils.DBIOUtil._

class TaskService @Inject() (taskRepo: TaskRepository)(implicit ec: ExecutionContext) {

  def updateTask(taskId: Long, name: String, owner: User, versionNo: Long)(implicit user: User): EitherT[DBIO, Error, Task] = {
    for {
      _ <- EitherT(taskRepo.findTaskById(taskId))
      updated <- EitherT(taskRepo.update(taskId, name, owner, versionNo))
    } yield updated
  }

}

