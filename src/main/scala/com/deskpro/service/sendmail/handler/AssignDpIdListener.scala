package com.deskpro.service.sendmail.handler

import org.mailster.smtp.api.MessageListener
import org.mailster.smtp.api.handler.SessionContext
import java.io.InputStream
import java.util.{Date}
import java.text.SimpleDateFormat
import org.apache.commons.lang.RandomStringUtils
import org.slf4j.{LoggerFactory, Logger}

class AssignDpIdListener extends MessageListener {

  private final val LOG: Logger = LoggerFactory.getLogger(classOf[AssignDpIdListener])

  def deliver(ctx: SessionContext, from: String, recipient: String, data: InputStream) = {
    val sb = new StringBuilder(new SimpleDateFormat("yyyyMMdd-hhmmss-SSS").format(new Date))
    sb.append(RandomStringUtils.randomAlphabetic(30).toUpperCase)

    val dpId = sb.toString
    ctx.setAttribute("DP_ID", dpId)
    LOG.debug("Assign DP_ID = " + dpId)
  }

  def accept(ctx: SessionContext, from: String, recipient: String) = {
    true
  }
}
