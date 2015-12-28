package repositories

import java.time.LocalDateTime

import scalaz.{ -\/, \/, \/- }
import slick.dbio.DBIO

import models._

class TaskRepository {

  // on mem 適当実装. TODO RDB/Slick
  var database = Map.empty[Long, Task]

  def create(name: String, owner: User)(implicit user: User): DBIO[Error \/ Task] = {
    val task = Task(1L, name, owner, user, LocalDateTime.now, 1L)
    database += (task.taskId -> task)

    DBIO.successful(
      \/-(task)
    )
  }

  def findTaskById(taskId: Long)(implicit user: User): DBIO[Error \/ Task] = {
    database.get(taskId) match {
      case Some(task) =>
        DBIO.successful(\/-(task))
      case None =>
        DBIO.successful(-\/(ResourceNotFound(s"Task not found. taskId=$taskId")))
    }
  }

  def update(taskId: Long, name: String, owner: User, versionNo: Long)(implicit user: User): DBIO[Error \/ Task] = {
    database.get(taskId) match {
      case Some(task) =>
        if (task.versionNo == versionNo) {
          val newTask = task.copy(name = name, owner = owner, versionNo = task.versionNo + 1L)
          database += (taskId -> newTask)
          DBIO.successful(
            \/-(newTask)
          )
        } else {
          DBIO.successful(
            -\/(ResourceConflict(msg = "Task was updated by another thread."))
          )
        }
      case None =>
        DBIO.successful(
          -\/(ResourceNotFound(msg = s"Task not found. taskId=$taskId"))
        )
    }
  }

}
