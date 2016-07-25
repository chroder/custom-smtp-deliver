package com.deskpro.service.sendmail.handler

import org.mailster.smtp.api.MessageListener
import org.mailster.smtp.api.handler.SessionContext
import java.io.{File, InputStream}
import org.apache.commons.io.FileUtils

class SaveFileListener(private val saveBasePath: String) extends MessageListener {

  def deliver(ctx: SessionContext, from: String, recipient: String, data: InputStream) = {
    val dpid     = ctx.getAttribute("DP_ID").asInstanceOf[String]
    val dirPath  = saveBasePath + "/" + dpid.substring(0, 8)
    val fileName = dpid.substring(9)
    val file = new File(dirPath + "/" + fileName)

    FileUtils.forceMkdir(file.getParentFile)
  }

  def accept(ctx: SessionContext, from: String, recipient: String) = {
    true
  }
}
