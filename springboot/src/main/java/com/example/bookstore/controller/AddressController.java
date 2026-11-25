package com.example.bookstore.controller;

import com.example.bookstore.entity.Address;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AddressController(AddressService addressService, JwtUtil jwtUtil) {
        this.addressService = addressService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Address address = addressService.getAddressById(id, userId);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        Address createdAddress = addressService.createAddress(address, userId);
        return ResponseEntity.ok(createdAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Address updatedAddress = addressService.updateAddress(id, address, userId);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<Address> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Address address = addressService.setDefaultAddress(id, userId);
        return ResponseEntity.ok(address);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.replace("Bearer ", "");
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}