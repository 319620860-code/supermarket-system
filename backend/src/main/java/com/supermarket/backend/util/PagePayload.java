package com.supermarket.backend.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一分页 JSON 结构，与前端 {@code unwrapPagePayload} 约定一致。
 */
public final class PagePayload {

    private PagePayload() {}

    public static <T> Map<String, Object> of(List<T> records, int total, int current, int pageSize) {
        Map<String, Object> m = new HashMap<>();
        m.put("records", records);
        m.put("total", total);
        m.put("current", current);
        m.put("pageSize", pageSize);
        int pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
        m.put("pages", pages);
        return m;
    }
}
