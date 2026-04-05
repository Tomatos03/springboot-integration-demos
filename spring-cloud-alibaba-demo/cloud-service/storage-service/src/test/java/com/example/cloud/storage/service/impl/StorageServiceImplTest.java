package com.example.cloud.storage.service.impl;

import com.example.cloud.storage.dto.DeductStorageRequest;
import com.example.cloud.storage.entity.StorageDO;
import com.example.cloud.storage.mapper.StorageMapper;
import com.example.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageMapper storageMapper;

    @InjectMocks
    private StorageServiceImpl storageService;

    @Test
    void deductShouldUpdateStockWhenSufficient() {
        DeductStorageRequest request = new DeductStorageRequest("C1001", 2);
        StorageDO storage = new StorageDO();
        storage.setCommodityCode("C1001");
        storage.setResidue(10);

        when(storageMapper.selectByCommodityCode("C1001")).thenReturn(storage);
        when(storageMapper.deduct("C1001", 2)).thenReturn(1);

        storageService.deduct(request);

        verify(storageMapper).deduct("C1001", 2);
    }

    @Test
    void deductShouldThrowWhenStockInsufficient() {
        DeductStorageRequest request = new DeductStorageRequest("C1001", 2);
        StorageDO storage = new StorageDO();
        storage.setCommodityCode("C1001");
        storage.setResidue(1);

        when(storageMapper.selectByCommodityCode("C1001")).thenReturn(storage);

        assertThrows(BusinessException.class, () -> storageService.deduct(request));
        verify(storageMapper, never()).deduct("C1001", 2);
    }
}
