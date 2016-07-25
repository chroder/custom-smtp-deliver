package com.deskpro.service.sendmail.command

import org.mailster.smtp.util.Base64
import com.deskpro.service.sendmail.SmtpConfig
import net.liftweb.json.{DefaultFormats, JsonParser}
import scala.util.{Failure, Success, Try}
import org.mailster.smtp.core.commands.AbstractCommand
import org.apache.mina.core.session.IoSession
import org.mailster.smtp.core.SMTPContext
import org.slf4j.{LoggerFactory, Logger}

object DpUseServerCommand {
  val DPUSESRV_ATTRIBUTE = "DpUseServerCommand.ctx"
}

class DpUseServerCommand extends AbstractCommand("DPUSESRV", "Specify which server to use") {

  private final val LOG: Logger = LoggerFactory.getLogger(classOf[DpUseServerCommand])

  override def execute(rawCommandString: String, ioSession: IoSession, ctx: SMTPContext): Unit = {
    parseCommandString(rawCommandString) match {
      case Success(smtpConfig) => {
        ioSession.setAttribute(DpUseServerCommand.DPUSESRV_ATTRIBUTE, smtpConfig)
        sendResponse(ioSession, "250 Ok")
      }
      case Failure(ex) => {
        sendResponse(ioSession, s"501 Invalid server params: [${ex.getClass.getSimpleName}] ${ex.getMessage}")
        LOG.debug(s"DPUSESRV error (#${ioSession.getId})", ex)
      }
    }
  }


  /**
   * Parses the raw command string to get a SmtpConfig object.
   *
   * @param rawCommandString The raw command string
   * @return SmtpConfig object wrapped in a Try
   */
  private def parseCommandString(rawCommandString: String): Try[SmtpConfig] = {
    val commandString = rawCommandString.substring(9).trim
    for {
      jsonString <- getJsonString(commandString)
      smtpConfig <- parseJsonString(jsonString)
    } yield smtpConfig
  }


  /**
   * Gets the JSON string from the raw command string the client passed us.
   * This could be JSON already, or it might be base64 encoded.
   *
   * @param commandString JSON or base64 encoded JSON
   * @return JSON string
   */
  private def getJsonString(commandString: String): Try[String] = {
    if (commandString.startsWith("{")) {
      LOG.debug("Raw JSON string detected, do not need to base64-decode")
      Try(commandString)
    } else {
      Try({
        LOG.debug("base64-decode string")
        val jsonBytes = Base64.decode(commandString)
        if (jsonBytes == null || jsonBytes.length == 0) {
          LOG.debug("Error decoding JSON string, or string is empty")
          throw new IllegalArgumentException("Bad base64-encoded string")
        }
        new String(jsonBytes, "UTF-8")
      })
    }
  }


  /**
   * Parses a JSON string into SmtpConfig object
   *
   * @param jsonString The JSON string to parse
   * @return The SmtpConfig object wrapped in a Try
   */
  private def parseJsonString(jsonString: String): Try[SmtpConfig] = {
    implicit val formats = DefaultFormats
    Try(JsonParser.parse(jsonString).extract[SmtpConfig])
  }
}