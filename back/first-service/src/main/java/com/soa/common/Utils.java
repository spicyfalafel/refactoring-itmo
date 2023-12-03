package com.soa.common;

import com.soa.exception.ErrorDescriptions;

public class Utils {
    public static void validateLimitOffset(Long limit, Long offset) {
        if (limit != null) {
            if (limit <= 0) {
                throw ErrorDescriptions.INCORRECT_LIMIT.exception();
            }
        }

        if (offset != null) {
            if (offset < 0) {
                throw ErrorDescriptions.INCORRECT_OFFSET.exception();
            }
        }
    }
}
