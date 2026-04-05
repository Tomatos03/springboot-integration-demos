package com.example.cloud.account.service;

import com.example.cloud.account.dto.AccountResponse;
import com.example.cloud.account.dto.DeductAccountRequest;

import java.util.List;

public interface AccountService {

    void deduct(DeductAccountRequest request);

    List<AccountResponse> list();
}
