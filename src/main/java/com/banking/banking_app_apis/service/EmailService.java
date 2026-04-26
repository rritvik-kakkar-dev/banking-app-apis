package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.dto.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);

    void sendEMailWithAttachment(EmailDetails emailDetails);
}
