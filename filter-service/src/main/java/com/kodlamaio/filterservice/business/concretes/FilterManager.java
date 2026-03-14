package com.kodlamaio.filterservice.business.concretes;

import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.filterservice.business.abstracts.FilterService;
import com.kodlamaio.filterservice.business.dto.responses.GetAllFiltersResponse;
import com.kodlamaio.filterservice.business.dto.responses.GetFilterResponse;
import com.kodlamaio.filterservice.entities.Filter;
import com.kodlamaio.filterservice.repository.FilterRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
//@RequiredArgsConstructor -> final yazılanlar static ile eşitlenir, final olmayanlar...
public class FilterManager implements FilterService
{
    private final FilterRepository repository;
    private ModelMapperService mapper;

    @Override
    public PageResponse<GetAllFiltersResponse> getAll(Pageable pageable)
    {
        var filters = repository.findAll(pageable)
                .map(filter -> mapper.forResponse().map(filter, GetAllFiltersResponse.class));

        return PageResponse.from(filters);
    }

    @Override
    public GetFilterResponse getById(UUID id)
    {
        var filter = repository.findById(id).orElseThrow(() -> new BusinessException("FILTER_NOT_EXISTS"));
        var response = mapper.forResponse().map(filter, GetFilterResponse.class);

        return response;
    }

    @Override
    public void add(Filter filter)
    {
        if (filter.getCarId() != null) {
            filter.setId(filter.getCarId());
        }
        else if (filter.getId() == null) {
            filter.setId(UUID.randomUUID());
        }
        repository.save(filter);
    }

    @Override
    public void delete(UUID id)
    {
        repository.deleteById(id);
    }


    @Override
    public void deleteAllByBrandId(UUID brandId)
    {
        repository.deleteAllByBrandId(brandId);
    }

    @Override
    public void deleteByCarId(UUID carId)
    {
        repository.deleteByCarId(carId);
    }

    @Override
    public void deleteAllByModelId(UUID modelId)
    {
        repository.deleteAllByModelId(modelId);
    }
    @Override
    public Filter getByCarId(UUID carId)
    {
        Filter filter = repository.findByCarId(carId);
        if (filter == null) {
            throw new BusinessException("FILTER_NOT_EXISTS");
        }
        return filter;
    }
}
