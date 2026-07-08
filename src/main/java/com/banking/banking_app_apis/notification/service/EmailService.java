package com.banking.banking_app_apis.notification.service;

import com.banking.banking_app_apis.notification.dto.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);

    void sendEMailWithAttachment(EmailDetails emailDetails);
}
