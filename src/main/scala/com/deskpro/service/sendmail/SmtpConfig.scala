package com.deskpro.service.sendmail

case class SmtpConfig(host: String, port: Int, user: Option[String], password: Option[String], secureMode: Option[String])