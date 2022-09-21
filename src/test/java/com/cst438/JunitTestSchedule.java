package com.cst438;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import com.cst438.controller.StudentController;
import com.cst438.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cst438.controller.ScheduleController;
import com.cst438.service.GradebookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;

/* 
 * Example of using Junit with Mockito for mock objects.
 * The database repositories are mocked with test data.
 *  
 * Mockmvc is used to test a simulated REST call to the RestController
 * The http response and repository is verified.
 * 
 * Note: This tests uses Junit 5.
 * ContextConfiguration identifies the controller class to be tested
 * addFilters=false turns off security.  (I could not get security to work in test environment.)
 * WebMvcTest is needed for test environment to create Repository classes.
 */
@ContextConfiguration(classes = { ScheduleController.class, StudentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestSchedule {
	static final String URL = "http://localhost:8080";
	public static final int TEST_COURSE_ID = 40442;
	public static final int TEST_STUDENT_ID = 1;
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME  = "test";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";

	@MockBean
	CourseRepository courseRepository;
	@MockBean
	StudentRepository studentRepository;
	@MockBean
	EnrollmentRepository enrollmentRepository;
	@MockBean
	GradebookService gradebookService;

	@Autowired
	private MockMvc mvc;

	@Test
	public void addCourse()  throws Exception {
		MockHttpServletResponse response;
		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);	

		// sets the start and end date of the course
		Calendar c = Calendar.getInstance();
		c.set(2021,  8,  16);
		course.setStart(new java.sql.Date( c.getTimeInMillis() ));
		c.set(2021,  12, 16);
		course.setEnd(new java.sql.Date( c.getTimeInMillis() ));
		
		Student student = new Student();
		student.setEmail(TEST_STUDENT_EMAIL);
		student.setName(TEST_STUDENT_NAME);
		student.setStatusCode(0);
		student.setStudent_id(1);
		
		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		enrollment.setEnrollment_id(1);
		enrollment.setSemester(TEST_SEMESTER);
		enrollment.setStudent(student);
		enrollment.setYear(TEST_YEAR);
		
		List<Enrollment> enrollments = new java.util.ArrayList<>();
		enrollments.add(enrollment);
		
		// given  -- stubs for database repositories that return test data
	    given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
	    given(studentRepository.findByEmail(TEST_STUDENT_EMAIL)).willReturn(student);
	    given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);
	    given(enrollmentRepository.findStudentSchedule(TEST_STUDENT_EMAIL, TEST_YEAR, TEST_SEMESTER)).willReturn(enrollments);
	  
	    // create the DTO (data transfer object) for the course to add.  primary key course_id is 0.
		ScheduleDTO.CourseDTO courseDTO = new ScheduleDTO.CourseDTO();
		courseDTO.course_id = TEST_COURSE_ID;
		
		// then do an http post request with body of courseDTO as JSON
		response = mvc.perform(MockMvcRequestBuilders
			.post("/schedule")
			.content(asJsonString(courseDTO))
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();
		
		// verify that return status = OK (value 200) 
		assertEquals(200, response.getStatus());
		
		// verify that returned data has non-zero primary key
		ScheduleDTO.CourseDTO result = fromJsonString(response.getContentAsString(), ScheduleDTO.CourseDTO.class);
		assertNotEquals( 0  , result.id);
		
		// verify that repository save method was called.
		verify(enrollmentRepository).save(any(Enrollment.class));
		
		// do http GET for student schedule 
		response = mvc.perform(MockMvcRequestBuilders
			.get("/schedule?year=" + TEST_YEAR + "&semester=" + TEST_SEMESTER)
		    .accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();
		
		// verify that return status = OK (value 200) 
		assertEquals(200, response.getStatus());
		
		// verify that returned data contains the added course 
		ScheduleDTO scheduleDTO = fromJsonString(response.getContentAsString(), ScheduleDTO.class);
		boolean found = false;		
		for (ScheduleDTO.CourseDTO sc : scheduleDTO.courses) {
			if (sc.course_id == TEST_COURSE_ID) {
				found = true;
			}
		}
		assertEquals(true, found, "Added course not in updated schedule.");
		
		// verify that repository find method was called.
		verify(enrollmentRepository, times(1)).findStudentSchedule(TEST_STUDENT_EMAIL, TEST_YEAR, TEST_SEMESTER);
	}
	
	@Test
	public void dropCourse()  throws Exception {
		MockHttpServletResponse response;
		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);	
		
		Calendar c = Calendar.getInstance();
		c.set(2021,  8,  16);
		course.setStart(new java.sql.Date( c.getTimeInMillis() ));  // course start date 8-16-2021
		c.set(2021,  12, 16);
		course.setEnd(new java.sql.Date( c.getTimeInMillis() ));
		
		Student student = new Student();
		student.setEmail(TEST_STUDENT_EMAIL);
		student.setName(TEST_STUDENT_NAME);
		student.setStatusCode(0);
		student.setStudent_id(1);
		
		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		enrollment.setEnrollment_id(1);
		enrollment.setSemester(TEST_SEMESTER);
		enrollment.setStudent(student);
		enrollment.setYear(TEST_YEAR);
	
		// given  -- stubs for database repositories that return test data
	    given(enrollmentRepository.findById(1)).willReturn(Optional.of(enrollment));
	    // note:  it is not necessary to create a mock for enrollmentRepository.delete.
	    //   Because it is a method that has a void return type, Mockito will mock it automatically.
	  
		// then 
		response = mvc.perform(MockMvcRequestBuilders
			.delete("/schedule/1"))
			.andReturn().getResponse();
		
		// verify that return status = OK (value 200) 
		assertEquals(200, response.getStatus());
	
		// verify that repository delete method was called.
		verify(enrollmentRepository).delete(any(Enrollment.class));
	}

	@Test
	public void testAddStudent() throws Exception {
		MockHttpServletResponse response;

		var studentDTO = new StudentDTO(TEST_STUDENT_NAME, TEST_STUDENT_EMAIL);
		studentDTO.student_id = TEST_STUDENT_ID;

		var student = new Student();
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setEmail(TEST_STUDENT_EMAIL);
		student.setStatusCode(0);

		given(studentRepository.findByEmail(TEST_STUDENT_EMAIL)).willReturn(null);
		given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.empty());
		given(studentRepository.save(any(Student.class))).willReturn(student);

		response = mvc.perform(MockMvcRequestBuilders
			.post("/student")
			.characterEncoding("utf-8")
			.content(asJsonString(studentDTO))
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
		.andReturn().getResponse();

		assertEquals(200, response.getStatus());
		StudentDTO result = fromJsonString(response.getContentAsString(), StudentDTO.class);
		verify(studentRepository).save(any(Student.class));
	}

	@Test
	public void testPlaceHold() throws Exception {
		MockHttpServletResponse response;

		var student = new Student();
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setEmail(TEST_STUDENT_EMAIL);

		given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(student));
		given(studentRepository.save(any(Student.class))).willReturn(student);

		var holdReason = "Too many books overdue";
		response = mvc.perform(MockMvcRequestBuilders
						.put(String.format("/student/%d?status=%d&msg=%s", TEST_STUDENT_ID, StudentDTO.HOLD, holdReason))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		assertEquals(200, response.getStatus());
		verify(studentRepository).save(any(Student.class));
	}

	@Test
	public void testRemoveHold() throws Exception {
		MockHttpServletResponse response;

		var student = new Student();
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setEmail(TEST_STUDENT_EMAIL);
		student.setStatusCode(StudentDTO.HOLD);
		student.setStatus("Too many books overdue");

		given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(student));
		given(studentRepository.save(any(Student.class))).willReturn(student);

		response = mvc.perform(MockMvcRequestBuilders
						.put(String.format("/student/%d?status=%d&msg=%s", TEST_STUDENT_ID, StudentDTO.NO_HOLDS, ""))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		assertEquals(200, response.getStatus());
		verify(studentRepository).save(any(Student.class));
	}

	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T  fromJsonString(String str, Class<T> valueType ) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
