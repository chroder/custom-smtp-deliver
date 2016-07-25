package com.deskpro.service.sendmail

case class SmtpFile(dpid: String, from: String, recipients: List[String], useServer: Option[SmtpConfig])
