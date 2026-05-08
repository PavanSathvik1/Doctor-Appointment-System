package com.example.demo.controller;

import com.example.demo.dto.DoctorResponseDto;
import com.example.demo.service.DoctorService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class HospitalController {

    private final DoctorService doctorService;

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponseDto>> getAllDoctors() {

        return ResponseEntity.ok(
                doctorService.getAllDoctors());
    }
}
