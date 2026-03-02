package com.ra.batshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email thông thường
     * @param to: Email người nhận (lấy từ database)
     * @param subject: Tiêu đề thư
     * @param content: Nội dung thư (chứa mã OTP)
     */
    public void sendEmail(String to, String subject, String content) {

        SimpleMailMessage message = new SimpleMailMessage();

        // Thiết lập người nhận là biến 'to' truyền vào từ Controller
        message.setTo(to);

        // Tiêu đề email
        message.setSubject(subject);

        // Nội dung email
        message.setText(content);

        // Gửi đi bằng tài khoản cấu hình trong application.properties
        mailSender.send(message);
    }
}