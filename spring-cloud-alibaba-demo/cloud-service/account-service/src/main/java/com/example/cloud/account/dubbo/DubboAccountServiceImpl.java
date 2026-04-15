package com.example.cloud.account.dubbo;

import com.example.cloud.account.mapper.AccountMapper;
import com.example.common.dto.DeductAccountRequest;
import com.example.common.dubbo.DubboAccountService;
import com.example.common.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class DubboAccountServiceImpl implements DubboAccountService {
    private final AccountMapper accountMapper;

    @Override
    public ResultVO<Void> deduct(DeductAccountRequest request) {
        accountMapper.deduct(request.getUserId(), request.getMoney());
        return ResultVO.success();
    }
}