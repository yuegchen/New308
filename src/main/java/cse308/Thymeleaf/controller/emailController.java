package cse308.Thymeleaf.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class emailController {
    @Autowired
    private JavaMailSender MailSender;
//    @RequestMapping(path = "/email/trigger", method = RequestMethod.POST)
    public String triggerEmail(String email, String token) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("Please type in the following token to verify your email here: http://localhost:8080/verify"
        		+ "\n"
        	+ "\n" + "your token is: " + token);
        message.setTo(email);
        message.setSubject("Verify Registration Email");
        message.setFrom("orangej1997@gmail.com");
        try {
            MailSender.send(message);
            return "{\"message\": \"OK\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"message\": \"Error\"}";
        }
    }

}