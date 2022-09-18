package com.cst438.controller;

import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StudentController {
    final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostMapping("/student")
    public StudentDTO addStudent(@RequestBody StudentDTO studentDTO) {
        System.out.println("/student called.");

        if (studentRepository.findByEmail(studentDTO.email) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Student with email '%s' already exists.", studentDTO.email));
        }
        if (studentRepository.findById(studentDTO.student_id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Student with id '%d' already exists.", studentDTO.student_id));
        }

        var student = new Student();
        student.setName(studentDTO.name);
        student.setEmail(studentDTO.email);
        student.setStatus(studentDTO.status);
        student.setStatusCode(studentDTO.statusCode);

        studentRepository.save(student);
        // Reflect the auto-increment in returned JSON
        studentDTO.student_id = student.getStudent_id();
        return studentDTO;
    }
}
