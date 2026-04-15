package com.example.cloud.storage.service;

import com.example.cloud.storage.dto.StorageResponse;
import com.example.common.dto.DeductStorageRequest;

import java.util.List;

public interface StorageService {

    void deduct(DeductStorageRequest request);

    List<StorageResponse> list();
}
