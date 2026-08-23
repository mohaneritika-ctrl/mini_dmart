package com.dmart.service;

import com.dmart.dto.request.CreateReturnRequestDto;
import com.dmart.dto.request.UpdateReturnStatusDto;
import com.dmart.dto.response.ReturnRequestResponseDto;
import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;

import java.util.List;

public interface ReturnRequestService {

    ReturnRequestResponseDto createRequest(CreateReturnRequestDto dto);

    List<ReturnRequestResponseDto> getMyRequests();

    ReturnRequestResponseDto getMyRequestById(Long id);

    ReturnRequestResponseDto cancelMyRequest(Long id);

    List<ReturnRequestResponseDto> getAllRequests(ReturnStatus status, ReturnType type);

    ReturnRequestResponseDto getRequestById(Long id);

    ReturnRequestResponseDto updateRequestStatus(Long id, UpdateReturnStatusDto dto);
}