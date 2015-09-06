package models

sealed trait Role

case object Normal extends Role

case object Admin extends Role

