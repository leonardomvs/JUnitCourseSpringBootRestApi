package io.github.leonardomvs.springmvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.leonardomvs.springmvc.models.CollegeStudent;
import io.github.leonardomvs.springmvc.repository.MathGradesDao;
import io.github.leonardomvs.springmvc.repository.StudentDao;
import io.github.leonardomvs.springmvc.service.StudentAndGradeService;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class GradeBookControllerTest {

	private static MockHttpServletRequest request;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Mock
	StudentAndGradeService studentCreateServiceMock;

	 @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    private CollegeStudent student;
	
    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;
    
    public static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
    
    @BeforeAll
    public static void setup() {
    	request = new MockHttpServletRequest();    	
    	request.setParameter("firstname", "Chad");
    	request.setParameter("lastname", "Darby");
    	request.setParameter("emailAddress", "chad.darby@luv2code_school.com");
    }
    
    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }
    
    @Test
    void getStudentsHttpRequest() throws Exception {
    	
    	student.setFirstname("Chad");
    	student.setLastname("Darby");
    	student.setEmailAddress("chad.darby@luv2code_school.com");
    	entityManager.persist(student);
    	entityManager.flush();
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$", hasSize(2)));
    	
    }
    
    @Test
    void createStudentHttpRequest() throws Exception {
    	
    	String emailAddress = "chad.darby@luv2code_school.com";
    	
    	student.setFirstname("Chad");
    	student.setLastname("Darby");
    	student.setEmailAddress(emailAddress);
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/")
    			.contentType(APPLICATION_JSON_UTF8)
    			.content(objectMapper.writeValueAsString(student)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
    	
    	CollegeStudent verifyStudent = studentDao.findByEmailAddress(emailAddress);
    	assertNotNull(verifyStudent, "Student should be valid.");
    	
    }
    
    @Test
    void deleteStudentHttpRequest() throws Exception {
    	
    	int studentId = 1;
    	
    	assertTrue(studentDao.findById(studentId).isPresent());
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", studentId))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$", hasSize(0)));
    	
    	assertFalse(studentDao.findById(studentId).isPresent());
    	
    }
    
    @Test
    void deleteStudentHttpRequestErrorPage() throws Exception {
    	
    	int studentId = 0;
    	
    	assertFalse(studentDao.findById(studentId).isPresent());
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", studentId))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404)))
			.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
    
    @Test
    void studentInformationHttpRequest() throws Exception {
    	
    	int studentId = 1;
    	
    	Optional<CollegeStudent> student = studentDao.findById(studentId);
    	
    	assertTrue(student.isPresent());
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", studentId))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(studentId)))
			.andExpect(jsonPath("$.firstname", is("Eric")))
			.andExpect(jsonPath("$.lastname", is("Roby")))
			.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com")));
    	
    }
    
    @Test
    void studentInformationHttpRequestEmptyResponse() throws Exception {
    	
    	int studentId = 0;
    	
    	Optional<CollegeStudent> student = studentDao.findById(studentId);
    	
    	assertFalse(student.isPresent());
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", studentId))
    		.andExpect(status().is4xxClientError())
    		.andExpect(jsonPath("$.status", is(404)))
    		.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
    
    @Test
    void createAValidGradeHttpRequest() throws Exception {
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "85.00")
    			.param("gradeType", "math")
    			.param("studentId", "1"))    			
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_UTF8))
					.andExpect(jsonPath("$.id", is(1)))
					.andExpect(jsonPath("$.firstname", is("Eric")))
					.andExpect(jsonPath("$.lastname", is("Roby")))
					.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com")))
    				.andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(2)));

    }
    
    @Test
    void createAValidGradeHttpRequestStudentDoesNotExistEmptyResponse() throws Exception {
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "85.00")
    			.param("gradeType", "math")
    			.param("studentId", "0"))    			
    				.andExpect(status().is4xxClientError())
    				.andExpect(jsonPath("$.status", is(404)))
					.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
    
    @Test
    void createANonValidGradeHttpRequestGradeTypeDoesNotExistEmptyReponse() throws Exception {
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "85.00")
    			.param("gradeType", "literature")
    			.param("studentId", "1"))    			
    				.andExpect(status().is4xxClientError())
    				.andExpect(jsonPath("$.status", is(404)))
					.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
    
    @Test
    void deleteAValidGradeHttpRequest() throws Exception {
    	
    	int gradeId = 1;
    	String gradeType = "math";
    	
    	assertTrue(mathGradeDao.findById(gradeId).isPresent());
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", gradeId, gradeType))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.firstname", is("Eric")))
				.andExpect(jsonPath("$.lastname", is("Roby")))
				.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com")))
				.andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(0)));
    	    	
    }
    
    @Test
    void deleteAValidGradeHttpRequestStudentIdDoesNotExistEmptyResponse() throws Exception {
    	
    	int gradeId = 2;
    	String gradeType = "history";
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", gradeId, gradeType))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404)))
			.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
    
    @Test
    void deleteANonValidGradeHttpRequest() throws Exception {
    	
    	int gradeId = 1;
    	String gradeType = "literature";
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", gradeId, gradeType))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404)))
			.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	
    }
            
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
    
}
