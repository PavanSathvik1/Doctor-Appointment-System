package com.example.demo.controller;

import com.example.demo.dto.AppointmentResponseDto;
import com.example.demo.dto.CreateAppointmentRequestDto;
import com.example.demo.dto.PatientResponseDto;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.PatientService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @RequestBody CreateAppointmentRequestDto createAppointmentRequestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createNewAppointment(createAppointmentRequestDto));
    }

    @GetMapping("/profile")
    public ResponseEntity<PatientResponseDto> getPatientProfile() {

        Long patientId = 2L;

        return ResponseEntity.ok(
                patientService.getPatientById(patientId));
    }
}