package com.example.bookstore.service;

import com.example.bookstore.entity.Address;
import com.example.bookstore.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId);
    }

    public Address getAddressById(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
    }

    @Transactional
    public Address createAddress(Address address, Long userId) {
        address.setUserId(userId);
        
        if (address.getIsDefault() == null) {
            address.setIsDefault(false);
        }
        
        if (address.getIsDefault()) {
            clearDefault(userId);
        } else {
            List<Address> existingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId);
            if (existingAddresses.isEmpty()) {
                address.setIsDefault(true);
            }
        }
        
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long id, Address address, Long userId) {
        Address existingAddress = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        existingAddress.setName(address.getName());
        existingAddress.setPhone(address.getPhone());
        existingAddress.setProvince(address.getProvince());
        existingAddress.setCity(address.getCity());
        existingAddress.setDistrict(address.getDistrict());
        existingAddress.setDetail(address.getDetail());
        
        if (address.getIsDefault() != null && address.getIsDefault() && !existingAddress.getIsDefault()) {
            clearDefault(userId);
            existingAddress.setIsDefault(true);
        }
        
        return addressRepository.save(existingAddress);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        addressRepository.delete(address);
        
        List<Address> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId);
        if (!remainingAddresses.isEmpty() && remainingAddresses.stream().noneMatch(Address::getIsDefault)) {
            remainingAddresses.get(0).setIsDefault(true);
            addressRepository.save(remainingAddresses.get(0));
        }
    }

    @Transactional
    public Address setDefaultAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getIsDefault()) {
            clearDefault(userId);
            address.setIsDefault(true);
            addressRepository.save(address);
        }
        
        return address;
    }

    private void clearDefault(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId);
        for (Address addr : addresses) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
                break;
            }
        }
    }
}