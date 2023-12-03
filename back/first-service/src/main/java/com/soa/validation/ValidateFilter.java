package com.soa.validation;

import com.soa.repository.FilterCriteria;

import java.util.List;

public interface ValidateFilter {
    public List<FilterCriteria> map(String[] filter);
}
