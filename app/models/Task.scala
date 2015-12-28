package models

import java.time.LocalDateTime

case class Task(
  taskId: Long,
  name: String,
  owner: User,
  createdBy: User,
  createAt: LocalDateTime,
  versionNo: Long
)

