package com.example.bookstore.controller;

import com.example.bookstore.dto.AddressDTO;
import com.example.bookstore.dto.AddressRequest;
import com.example.bookstore.mapper.AddressMapper;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;
    private final CurrentUser currentUser;

    public AddressController(AddressService addressService, AddressMapper addressMapper,
                             CurrentUser currentUser) {
        this.addressService = addressService;
        this.addressMapper = addressMapper;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId).stream()
                .map(addressMapper::toDTO)
                .toList();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(addressMapper.toDTO(addressService.getAddressById(id, userId)));
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(addressMapper.toDTO(
                addressService.createAddress(addressMapper.toEntity(request), userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id,
                                                    @Valid @RequestBody AddressRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(addressMapper.toDTO(
                addressService.updateAddress(id, addressMapper.toEntity(request), userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<AddressDTO> setDefaultAddress(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(addressMapper.toDTO(addressService.setDefaultAddress(id, userId)));
    }
}