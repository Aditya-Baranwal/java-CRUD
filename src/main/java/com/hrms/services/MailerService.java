package com.hrms.services;

import com.hrms.dto.MailDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class MailerService {

     @Autowired
     JavaMailSender javaMailSender;

    public void sendMail(MailDataDto mailDetails) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject(mailDetails.getSubject());
            mailMessage.setTo(mailDetails.getTo());
            mailMessage.setFrom(mailDetails.getFrom());
            mailMessage.setText(mailDetails.getText());
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            throw e;
        }
    }

}
