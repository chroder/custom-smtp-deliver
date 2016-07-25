package com.deskpro.service.sendmail

import java.net.InetAddress

case class SendServerConfig (bindAddress: InetAddress, smtpPort: Int, ctrlPort: Int, hostname: String = "cloud.deskpro.com")