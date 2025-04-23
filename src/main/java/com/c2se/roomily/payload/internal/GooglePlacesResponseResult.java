package com.c2se.roomily.payload.internal;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GooglePlacesResponseResult {
    private String status;
    private JsonNode results;
}
