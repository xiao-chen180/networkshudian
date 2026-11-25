package com.example.bookstore.mapper;

import com.example.bookstore.dto.AddressDTO;
import com.example.bookstore.dto.AddressRequest;
import com.example.bookstore.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequest request) {
        Address address = new Address();
        address.setName(request.name());
        address.setPhone(request.phone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetail(request.detail());
        address.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        return address;
    }

    public AddressDTO toDTO(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getName(),
                address.getPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetail(),
                address.getIsDefault()
        );
    }
}