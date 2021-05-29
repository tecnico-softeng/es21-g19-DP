package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*



@DataJpaTest
class UpdateOpenAnswerQuestionTest extends SpockTest {
    def question

    def setup() {
        createExternalCourseAndExecution()

        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)
        question.setImage(image)
        def questionDetails = new OpenAnswerQuestion()
        questionDetails.setCorrectAnswer(OPEN_QUESTION_1_ANSWER)
        questionDetails.setExpression(OPEN_QUESTION_1_EXPRESSION)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)
    }


    def "update an open answer question with an expression"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())

        and: "changed correct answer"
        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_QUESTION_2_ANSWER)

        and: "changed expression"
        questionDto.getQuestionDetailsDto().setExpression(OPEN_QUESTION_2_EXPRESSION)
        questionRepository.save(question)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getImage() != null
        result.getCourse() == externalCourse
        and: 'a correct answer and expression are changed'
        def resultQuestion = (OpenAnswerQuestion) result.getQuestionDetails()
        resultQuestion.getCorrectAnswer() == OPEN_QUESTION_2_ANSWER
        resultQuestion.getExpression().toString() == OPEN_QUESTION_2_EXPRESSION
    }

    def "update open answer changed expression to blank"() {
        given: "a changed expression"
        def questionDto = new QuestionDto(question)
        questionDto.getQuestionDetailsDto().setExpression("")
        questionRepository.save(question)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        def result = questionRepository.findAll().get(0)

        and: 'a correct answer and expression are changed'
        def resultQuestion = (OpenAnswerQuestion) result.getQuestionDetails()
        resultQuestion.getExpression().toString() == ""
    }

    @Unroll("invalid arguments: #correctAnswer | #expression || #errorMessage")
    def "invalid input values"() {
        given: "a changed expression"
        def questionDto = new QuestionDto(question)
        questionDto.getQuestionDetailsDto().setCorrectAnswer(correctAnswer)
        questionDto.getQuestionDetailsDto().setExpression(expression)
        questionRepository.save(question)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        correctAnswer           | expression                           || errorMessage
        null                    | OPEN_QUESTION_1_EXPRESSION           || NO_CORRECT_ANSWER
        ""                      | OPEN_QUESTION_1_EXPRESSION           || NO_CORRECT_ANSWER
        "         "             | OPEN_QUESTION_1_EXPRESSION           || NO_CORRECT_ANSWER
        OPEN_QUESTION_1_ANSWER  | OPEN_QUESTION_1_MISMATCH_EXPRESSION  || EXPRESSION_NEEDS_TO_MATCH_ANSWER
        OPEN_QUESTION_1_ANSWER  | "["                                  || INVALID_EXPRESSION
        OPEN_QUESTION_1_ANSWER  | "          "                         || EXPRESSION_WITH_SPACES_ONLY
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
