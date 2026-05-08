package com.example.demo.controller;

import com.example.demo.dto.AppointmentResponseDto;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.User;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.service.AppointmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAllAppointmentsOfDoctor() {

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return ResponseEntity.ok(
                appointmentService.getAllAppointmentsOfDoctor(doctor.getId()));
    }
}