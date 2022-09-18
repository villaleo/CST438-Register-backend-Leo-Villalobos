package com.cst438.domain;

public class StudentDTO {
    public int student_id;
    public String name;
    public String email;
    public int statusCode;
    public String status;

    public StudentDTO() {
        this.student_id = 0;
        this.name = null;
        this.email = null;
        this.statusCode = 0;
        this.status = null;
    }

    public StudentDTO(String name, String email) {
        this.student_id = 0;
        this.name = name;
        this.email = email;
        this.statusCode = 0;
        this.status = null;
    }

    @Override
    public String toString() {
        return String.format(
            "StudentDTO [student_id=%d, name=%s, email=%s, statusCode=%d, status=%s]",
            student_id, name, email, statusCode, status
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof StudentDTO dto) {
            if (dto.name == null || dto.email == null || dto.status == null) return false;
            return (
                this.student_id == dto.student_id &&
                this.name.equals(dto.name) &&
                this.email.equals(dto.email) &&
                this.status.equals(dto.status)
            );
        }
        return false;
    }
}
