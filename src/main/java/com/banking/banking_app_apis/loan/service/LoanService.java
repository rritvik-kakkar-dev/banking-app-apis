package com.banking.banking_app_apis.loan.service;

import com.banking.banking_app_apis.loan.dto.*;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;

public interface LoanService {

    LoanDetailResponse createLoan(CreateLoanRequest request, User currentUser);

    LoanDetailResponse getLoan(Long id, User currentUser);

    LoanScheduleDetailsResponse getLoanSchedule(Long id, User currentUser);

    Page<LoanSummaryResponse> getLoans(User currentUser, int page, int limit, String sortBy, String sortOrder);

    PayEmiResponse payEmi(Long id, PayEmiRequest request, User currentUser);
}
