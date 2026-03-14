package com.kodlamaio.maintenanceservice.business.abstracts;

import com.kodlamaio.maintenanceservice.business.dto.requests.create.CreateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.requests.update.UpdateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.responses.create.CreateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetAllMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.update.UpdateMaintenanceResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService
{
    default List<GetAllMaintenanceResponse> getAll() {
        return getAll(Pageable.unpaged()).getContent();
    }
    PageResponse<GetAllMaintenanceResponse> getAll(Pageable pageable);
    GetMaintenanceResponse getById(UUID id);
    GetMaintenanceResponse returnCarFromMaintenance(UUID carId);
    CreateMaintenanceResponse add(CreateMaintenanceRequest request) throws InterruptedException;
    UpdateMaintenanceResponse update(UUID id, UpdateMaintenanceRequest request);
    void delete(UUID id);
}
