package com.example.cloud.account.service.impl;

import com.example.common.dto.DeductAccountRequest;
import com.example.cloud.account.entity.AccountDO;
import com.example.cloud.account.mapper.AccountMapper;
import com.example.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void deductShouldUpdateBalanceWhenSufficient() {
        DeductAccountRequest request = new DeductAccountRequest("u1001", new BigDecimal("100.00"));
        AccountDO account = new AccountDO();
        account.setUserId("u1001");
        account.setResidue(new BigDecimal("500.00"));

        when(accountMapper.selectByUserId("u1001")).thenReturn(account);
        when(accountMapper.deduct("u1001", new BigDecimal("100.00"))).thenReturn(1);

        accountService.deduct(request);

        verify(accountMapper).deduct("u1001", new BigDecimal("100.00"));
    }

    @Test
    void deductShouldThrowWhenResidueInsufficient() {
        DeductAccountRequest request = new DeductAccountRequest("u1001", new BigDecimal("100.00"));
        AccountDO account = new AccountDO();
        account.setUserId("u1001");
        account.setResidue(new BigDecimal("50.00"));

        when(accountMapper.selectByUserId("u1001")).thenReturn(account);

        assertThrows(BusinessException.class, () -> accountService.deduct(request));
        verify(accountMapper, never()).deduct("u1001", new BigDecimal("100.00"));
    }
}
