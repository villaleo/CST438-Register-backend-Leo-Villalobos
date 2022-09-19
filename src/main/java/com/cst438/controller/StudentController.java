package com.cst438.controller;

import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StudentController {
    final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * This method is called by the registration service to create a new student.
     * @param studentDTO The student to be created. The student_id field is ignored.
     * @return The student that was created.
     * @throws ResponseStatusException If the student already exists.
     */
    @PostMapping("/student")
    @Transactional
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

    /**
     * This method is called by the registration service to update the status and status code of an existing student.
     * The student_id field is required. This operation is idempotent.
     * @param student_id The id of the student to be updated.
     * @param studentDTO The student to be updated.
     * @return The student that was updated.
     * @throws ResponseStatusException If the student does not exist.
     */
    @PutMapping("/student/status/{student_id}")
    public StudentDTO updateStatus(@PathVariable int student_id, @RequestBody StudentDTO studentDTO) {
        System.out.println("/student/" + student_id + " called.");

        var student = studentRepository.findById(student_id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Student with id '%d' not found.", student_id)));

        // Update the student with the new status code and status
        student.setStatusCode(studentDTO.statusCode);
        student.setStatus(studentDTO.status);
        studentRepository.save(student);

        // Match the StudentDTO fields to the Student fields
        studentDTO.student_id = student.getStudent_id();
        studentDTO.name = student.getName();
        studentDTO.email = student.getEmail();
        return studentDTO;
    }
}
