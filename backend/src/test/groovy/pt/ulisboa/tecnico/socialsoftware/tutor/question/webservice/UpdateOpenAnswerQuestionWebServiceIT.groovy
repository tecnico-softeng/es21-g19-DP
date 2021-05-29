package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservices

import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    def teacher
    def student
    def response
    def question
    def questionDetails
    def questionDetailsDto
    def mapper

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
        mapper = new ObjectMapper()
        createExternalCourseAndExecution();

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)

        student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(student)
        userRepository.save(student)

        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDetailsDto = new OpenAnswerQuestionDto()
        questionDetailsDto.setCorrectAnswer(OPEN_QUESTION_1_ANSWER)
        questionDetailsDto.setExpression(OPEN_QUESTION_1_EXPRESSION)
        questionDto.setQuestionDetailsDto(questionDetailsDto)

        questionService.createQuestion(externalCourse.getId(), questionDto)
        question = questionRepository.findAll().get(0)

        questionDetails = question.getQuestionDetails()

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
    }

    def "teacher updates open answer question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        def questionDetailsDto = new OpenAnswerQuestionDto()
        questionDetailsDto.setCorrectAnswer(OPEN_QUESTION_2_ANSWER)
        questionDetailsDto.setExpression(OPEN_QUESTION_2_EXPRESSION)
        questionDto.setQuestionDetailsDto(questionDetailsDto)

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200

        def question = response.data
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        question.status == Question.Status.AVAILABLE.name()
        question.questionDetailsDto != null
        question.questionDetailsDto.correctAnswer == OPEN_QUESTION_2_ANSWER
        question.questionDetailsDto.expression == OPEN_QUESTION_2_EXPRESSION

    }

    def "student updates open answer question"() {
        given: 'a student login'
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: 'a questionDto'
        QuestionDto questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(questionDetailsDto)
        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_QUESTION_1_ANSWER)
        questionDto.getQuestionDetailsDto().setExpression(OPEN_QUESTION_1_EXPRESSION)

        when:
        response = restClient.post(
                path: '/courses/' + externalCourseExecution.getId()+ '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

    }

    def cleanup() {
        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())

        courseRepository.deleteById(externalCourse.getId())
        questionRepository.deleteAll()
    }

}