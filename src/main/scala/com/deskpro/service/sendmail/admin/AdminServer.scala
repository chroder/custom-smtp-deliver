package com.deskpro.service.sendmail.admin

import java.net.{InetAddress, InetSocketAddress}
import java.nio.charset.Charset

import com.deskpro.service.sendmail.SendServer
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.{IoSession, IdleStatus}
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.textline.TextLineCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketAcceptor

/**
 * Created by chroder on 12/07/2014.
 */
class AdminServer (port: Int, val sendServer: SendServer) {
  val acceptor  = new NioSocketAcceptor()
  var isStarted = false

  def start() = {
    if (isStarted) throw new RuntimeException("Server already started")
    isStarted = true

    acceptor.getFilterChain().addLast("logger", new LoggingFilter());
    acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
    acceptor.setHandler(new AdminServerHandler(sendServer))
    acceptor.getSessionConfig().setReadBufferSize(2048)
    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10)
    acceptor.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port))
  }

  def shutdown() = {
    if (!isStarted) throw new RuntimeException("Server not started")
    acceptor.unbind()
    isStarted = false
  }
}

class AdminServerHandler(val sendServer: SendServer) extends IoHandlerAdapter {
  override def messageReceived(session: IoSession, message: scala.Any): Unit = {
    val command = message.toString.trim.toLowerCase

    command match {
      case "quit"       => session.close(true)
      case "shutdown"   => sendServer.shutdown()
      case "status"     => session.write("RUNNING")
      case "stats"      => session.write(sendServer.messageStats(10))
      case "stats_30m"  => session.write(sendServer.messageStats(30))
      case "stats_60m"  => session.write(sendServer.messageStats(60))
      case "stats_2h"   => session.write(sendServer.messageStats(120))
      case "stats_4h"   => session.write(sendServer.messageStats(240))
      case "stats_6h"   => session.write(sendServer.messageStats(360))
      case "stats_12h"  => session.write(sendServer.messageStats(720))
      case "stats_24h"  => session.write(sendServer.messageStats(1440))
      case _            => session.write("Unknown command: " + command)
    }
  }

  override def exceptionCaught(session: IoSession, cause: Throwable): Unit = {
    cause.printStackTrace();
  }

  override def sessionIdle(session: IoSession, status: IdleStatus): Unit = {
    session.close(false)
  }
}