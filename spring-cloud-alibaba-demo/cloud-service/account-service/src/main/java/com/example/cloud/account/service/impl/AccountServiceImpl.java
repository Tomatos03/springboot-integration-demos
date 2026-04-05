package com.example.cloud.account.service.impl;

import com.example.cloud.account.dto.AccountResponse;
import com.example.cloud.account.dto.DeductAccountRequest;
import com.example.cloud.account.entity.AccountDO;
import com.example.cloud.account.mapper.AccountMapper;
import com.example.cloud.account.service.AccountService;
import com.example.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(DeductAccountRequest request) {
        AccountDO account = accountMapper.selectByUserId(request.getUserId());
        if (account == null) {
            throw new BusinessException("account not found: " + request.getUserId());
        }
        if (account.getResidue().compareTo(request.getMoney()) < 0) {
            throw new BusinessException("insufficient account balance");
        }
        int rows = accountMapper.deduct(request.getUserId(), request.getMoney());
        if (rows != 1) {
            throw new BusinessException("account deduct failed");
        }
    }

    @Override
    public List<AccountResponse> list() {
        return accountMapper.selectList(null)
                .stream()
                .map(it -> new AccountResponse(it.getUserId(), it.getTotal(), it.getUsed(), it.getResidue()))
                .toList();
    }
}
