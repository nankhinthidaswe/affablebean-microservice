package com.example.affablebeanui.model;

import java.time.LocalDate;


public record CartItem(
        Integer id,
        String name,
        double price,
        String description,
        Integer quantity,
        LocalDate lastUpdate
) {
}