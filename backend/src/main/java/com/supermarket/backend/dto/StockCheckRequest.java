package com.supermarket.backend.dto;

import com.supermarket.backend.entity.StockCheckRecord;
import lombok.Data;

import java.util.List;

@Data
public class StockCheckRequest {
    private String description;
    private List<StockCheckRecord> items;
}
