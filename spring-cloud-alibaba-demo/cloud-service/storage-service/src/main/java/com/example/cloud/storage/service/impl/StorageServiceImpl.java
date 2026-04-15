package com.example.cloud.storage.service.impl;

import com.example.cloud.storage.dto.StorageResponse;
import com.example.cloud.storage.entity.StorageDO;
import com.example.cloud.storage.mapper.StorageMapper;
import com.example.cloud.storage.service.StorageService;
import com.example.common.dto.DeductStorageRequest;
import com.example.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageMapper storageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(DeductStorageRequest request) {
        StorageDO storage = storageMapper.selectByCommodityCode(request.getCommodityCode());
        if (storage == null) {
            throw new BusinessException("storage not found: " + request.getCommodityCode());
        }
        if (storage.getResidue() < request.getCount()) {
            throw new BusinessException("insufficient storage stock");
        }
        int rows = storageMapper.deduct(request.getCommodityCode(), request.getCount());
        if (rows != 1) {
            throw new BusinessException("storage deduct failed");
        }
    }

    @Override
    public List<StorageResponse> list() {
        return storageMapper.selectList(null)
                .stream()
                .map(it -> new StorageResponse(it.getCommodityCode(), it.getTotal(), it.getUsed(), it.getResidue()))
                .toList();
    }
}
