package com.example.Auth.config;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioClient {
    @Value("${spring.twilio.accountSid}")
    private String accountSid;

    @Value("${spring.twilio.authToken}")
    private String authToken;

    @Value("${spring.twilio.fromPhoneNumber}")
    private String fromPhoneNumber;

    public void sendSMS(String to, String message) {
        Twilio.init(accountSid, authToken);
        Message.creator(new PhoneNumber(to), new PhoneNumber(fromPhoneNumber), message).create();
    }
}
