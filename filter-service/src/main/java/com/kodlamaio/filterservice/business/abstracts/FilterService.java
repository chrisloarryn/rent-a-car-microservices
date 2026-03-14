package com.kodlamaio.filterservice.business.abstracts;

import com.kodlamaio.filterservice.business.dto.responses.GetAllFiltersResponse;
import com.kodlamaio.filterservice.business.dto.responses.GetFilterResponse;
import com.kodlamaio.filterservice.entities.Filter;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FilterService
{
    default List<GetAllFiltersResponse> getAll() {
        return getAll(Pageable.unpaged()).getContent();
    }
    PageResponse<GetAllFiltersResponse> getAll(Pageable pageable);
    GetFilterResponse getById(UUID id);

    //Ağıdakiler dış dünyaya açık değil
    void add(Filter filter);
    void delete(UUID id);
    void deleteAllByBrandId(UUID brandId);
    void deleteAllByModelId(UUID modelId);
    void deleteByCarId(UUID carId);
    Filter getByCarId(UUID carId);
}
