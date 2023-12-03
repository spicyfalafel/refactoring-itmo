package com.soa.model.tickets;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TypesDto {

    public List<Map<String, String>> types;
}
