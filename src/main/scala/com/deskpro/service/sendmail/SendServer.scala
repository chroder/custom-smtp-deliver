package com.deskpro.service.sendmail

import java.io.InputStream
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import com.deskpro.service.sendmail.SendServerConfig
import org.apache.commons.lang.RandomStringUtils
import org.mailster.smtp.api.handler.SessionContext
import scala.collection.mutable
import scala.collection.mutable.{StringBuilder}
import com.deskpro.service.sendmail.admin.AdminServer
import com.deskpro.service.sendmail.command.DpUseServerCommand
import org.mailster.smtp.SMTPServer
import com.deskpro.service.sendmail.handler.AssignDpIdListener
import org.mailster.smtp.api.MessageListener

object SendServer {
  def main(args: Array[String]): Unit = {
    val serverConfig = new SendServerConfig(
      bindAddress = InetAddress.getByName("0.0.0.0"),
      smtpPort = 25000,
      ctrlPort = 25001
    )

    val server = new SendServer(serverConfig)
    server.start()
  }
}

case class SendServerStat(from: String, when: Long)

class RecordStatListener(val sendServer: SendServer) extends MessageListener {
  def deliver(ctx: SessionContext, from: String, recipient: String, data: InputStream) = {
    sendServer.recordMessage(new SendServerStat(from, System.currentTimeMillis()))
  }

  def accept(ctx: SessionContext, from: String, recipient: String) = {
    true
  }
}

class SendServer(serverConfig: SendServerConfig) {

  val smtpServer  = createSmtpServer(serverConfig)
  val adminServer = createAdminServer(serverConfig)
  var lastCleanup = System.currentTimeMillis()
  var messageList = new mutable.MutableList[SendServerStat]

  def start(): Unit = {
    println("Starting SMTP Server...")
    smtpServer.start()
    println("Done")

    println("Starting control server...")
    adminServer.start()
    println("Done...")
  }

  def shutdown(): Unit = {
    smtpServer.shutdown()
    adminServer.shutdown()
    System.exit(0)
  }

  def messageStats(mins: Int): String = {
    val minTime = System.currentTimeMillis() - (mins * 60 * 1000)
    val matching = messageList.filter(_.when > minTime)
    s"Messages sent: ${matching.length}\n\n"
  }

  def recordMessage(stat: SendServerStat): Unit = {
    this.synchronized {

      if (lastCleanup < (System.currentTimeMillis() - 3600000)) {
        lastCleanup = System.currentTimeMillis()
        val minTime = System.currentTimeMillis() - 86400000
        messageList = messageList.filter(_.when > minTime)
      }

      messageList += stat
    }
  }

  private def createAdminServer(serverConfig: SendServerConfig): AdminServer = {
    val server = new AdminServer(serverConfig.ctrlPort, this)
    return server
  }

  private def createSmtpServer(serverConfig: SendServerConfig): SMTPServer = {
    val listeners = new util.ArrayList[MessageListener](1)

    val server = new SMTPServer(listeners)
    server.setBindAddress(serverConfig.bindAddress)
    server.getConfig.setHostName(serverConfig.hostname)
    server.getConfig.setConnectionTimeout(10000)
    server.setPort(serverConfig.smtpPort)
    server.getCommandHandler.addCommand(new DpUseServerCommand)
    server.getDeliveryHandlerFactory.addListener(new AssignDpIdListener())
    server.getDeliveryHandlerFactory.addListener(new RecordStatListener(this))

    return server
  }
}