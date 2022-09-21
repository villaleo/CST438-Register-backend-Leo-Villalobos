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
     * This operation is idempotent.
     * @param student_id The id of the student to be updated.
     * @param statusCode The student to be updated.
     * @param message The message for the reason of a status.
     * @throws ResponseStatusException If the student does not exist.
     */
    @PutMapping("/student/{student_id}")
    public void updateStatus(@PathVariable int student_id, @RequestParam("status") int statusCode, @RequestParam("msg") String message) {
        System.out.printf("/student/%d?status=%d&msg=\"%s\" called.", student_id, statusCode, message);

        var student = studentRepository.findById(student_id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Student with id '%d' not found.", student_id)));

        // Update the student with the new status code and message
        student.setStatusCode(statusCode);
        student.setStatus(message == null || message.isEmpty() ? null : message);
        studentRepository.save(student);
    }
}
