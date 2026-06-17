package com.globalstock.webapi.manager;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.config.MarketAppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 邮件发送管理器。
 *
 * <p>职责说明：封装激活邮件、重置密码邮件和邮件调试开关。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class EmailManager {

    private static final Logger log = LoggerFactory.getLogger(EmailManager.class);

    private final JavaMailSender mailSender;
    private final MarketAppProperties marketAppProperties;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${market.email.debug:false}")
    private boolean emailDebug;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private Integer mailPort;

    public EmailManager(JavaMailSender mailSender, MarketAppProperties marketAppProperties) {
        this.mailSender = mailSender;
        this.marketAppProperties = marketAppProperties;
    }

    /**
     * 发送账号激活邮件。
     *
     * @param email 邮箱
     * @param code 激活码
     */
    public void sendActivationEmail(String email, String code) {
        String link = buildLink("/activate", email, "code", code);
        String text = """
                您好，

                请点击以下链接激活您的 Global Stock 账号（24 小时内有效）：
                %s

                如非本人操作，请忽略此邮件。
                """.formatted(link);
        send(email, "激活您的 Global Stock 账号", text);
    }

    /**
     * 发送重置密码邮件。
     *
     * @param email 邮箱
     * @param token 重置令牌
     */
    public void sendResetPasswordEmail(String email, String token) {
        String link = buildLink("/reset-password", email, "token", token);
        String text = """
                您好，

                请点击以下链接重置您的 Global Stock 密码（15 分钟内有效）：
                %s

                如非本人操作，请忽略此邮件。
                """.formatted(link);
        send(email, "重置您的 Global Stock 密码", text);
    }

    /**
     * 异步发送账号锁定提醒。
     *
     * @param email 邮箱
     */
    @Async
    public void sendAccountLockedEmailAsync(String email) {
        if (emailDebug) {
            log.info("[DEBUG] Skipped locked-account email to {}", email);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("账号异常登录提醒");
            message.setText("""
                    您好，

                    您的账号因连续多次登录失败已被临时锁定，请稍后再试。
                    如非本人操作，请尽快联系管理员。
                    """);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("Failed to send locked-account email to {}", email, exception);
        }
    }

    private void send(String to, String subject, String text) {
        if (emailDebug) {
            log.info("[DEBUG] Skipped email to {}, subject: {}\n{}", to, subject, text);
            return;
        }
        try {
            log.info("Sending email to {}, subject: {}, host={}, port={}, debug={}",
                    to, subject, mailHost, mailPort, emailDebug);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Sent email to {}, subject: {}", to, subject);
        } catch (MailException exception) {
            log.error("Failed to send email to {}", to, exception);
            throw new BusinessException("邮件服务暂不可用，请稍后重试或联系管理员");
        }
    }

    private String buildLink(String path, String email, String paramName, String paramValue) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedValue = URLEncoder.encode(paramValue, StandardCharsets.UTF_8);
        return marketAppProperties.getBaseUrl() + path
                + "?email=" + encodedEmail
                + "&" + paramName + "=" + encodedValue;
    }
}
