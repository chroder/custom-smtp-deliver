This uses [MailsterSMTP](https://github.com/edeoliveira/MailsterSMTP) to create a SMTP server. A custom SMTP command was created so a client can make messages send through a specific target server.

The purpose was to use this in a SaaS platform where email could be sent through custom customer accounts. I needed a way to facilitate sending emails over SMTP (because that is what the backend software libraries "spoke"), but I needed to allow customers to enter their own SMTP accounts if they wanted to. But a lot of processing needs to happen on messages before they are ultimately sent out (e.g. logging, backups, stats, etc) so we couldn't just use the custom account right away.

So this was an experiment to see if we could wrap up all of the necessary logic in a "real" SMTP server so the client software could essentially queue messages over the SMTP protocol, which would mean we wouldn't need to change the software itself.

Ultimately, we ditched this as a bad idea and moved to a more traditional job queue approach, where an email job is registered in a Redis queue, and a job processor just took care of sending the message with whatever target server the job was configured to use.