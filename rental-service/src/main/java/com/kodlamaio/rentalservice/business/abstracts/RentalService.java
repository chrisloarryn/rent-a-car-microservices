package com.kodlamaio.rentalservice.business.abstracts;

import com.kodlamaio.rentalservice.business.dto.requests.CreateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.requests.UpdateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.responses.CreateRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetAllRentalsResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.UpdateRentalResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RentalService
{
    default List<GetAllRentalsResponse> getAll() {
        return getAll(Pageable.unpaged()).getContent();
    }
    PageResponse<GetAllRentalsResponse> getAll(Pageable pageable);
    GetRentalResponse getById(UUID id);
    CreateRentalResponse add(CreateRentalRequest request) throws InterruptedException;
    UpdateRentalResponse update(UUID id, UpdateRentalRequest request);
    void delete(UUID id);
}
