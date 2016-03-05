package auth

import javax.inject.Inject

import play.api.Environment

import services.UserService

case class AuthComponent @Inject() (
  env: Environment,
  userService: UserService
)

