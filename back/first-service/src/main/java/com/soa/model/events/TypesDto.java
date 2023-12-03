package com.soa.model.events;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TypesDto {
    public List<Map<String, String>> types;
}
