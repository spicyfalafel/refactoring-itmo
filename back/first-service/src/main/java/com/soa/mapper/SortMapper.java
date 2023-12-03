package com.soa.mapper;

import com.soa.exception.ErrorDescriptions;
import com.soa.repository.SortCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortMapper {

    public static List<SortCriteria> map(String sort){
        List<SortCriteria> sc = new ArrayList<>();
        if (sort != null) {
            try {
                var listSorts = Arrays.asList(sort.split(","));
                for (String oneSort : listSorts) {
                    var descSort = oneSort.charAt(0) == '-';
                    var key = "";
                    if (descSort) {
                        key = oneSort.substring(1);
                    } else {
                        key = oneSort;
                    }
                    sc.add(new SortCriteria(key, !descSort));
                }
            } catch (Exception e) {
                throw ErrorDescriptions.INCORRECT_SORT.exception();
            }
        }
        return sc;
    }

}
