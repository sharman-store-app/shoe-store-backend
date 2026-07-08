package com.shoestore.backend.mapper;

import com.shoestore.backend.config.MapperConfig;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.model.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);
}
