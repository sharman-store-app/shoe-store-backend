package com.shoestore.backend.dto.session;

public record SessionRequestDto(
        String referrer,
        String countryCode
) {
}
