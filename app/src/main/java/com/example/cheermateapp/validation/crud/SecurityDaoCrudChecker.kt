package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.SecurityDao
import com.example.cheermateapp.data.model.SecurityQuestion
import com.example.cheermateapp.data.model.UserSecurityAnswer

/**
 * CRUD validator for SecurityDao
 */
class SecurityDaoCrudChecker(private val securityDao: SecurityDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "SecurityDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testAddQuestion())
        results.add(testInsertQuestion())
        results.add(testUpdateQuestion())
        results.add(testDeleteQuestion())
        results.add(testSaveAnswer())
        results.add(testInsertAnswer())
        results.add(testGetAllQuestions())
        results.add(testAnswers())
        results.add(testGetUserAnswers())
        results.add(testInsertAllQuestions())
        results.add(testGetQuestionCount())
        
        return results
    }
    
    private suspend fun testAddQuestion() = executeTest(CrudOperation.CREATE, "addQuestion") {
        val test = createTestQuestion()
        val insertedId = securityDao.addQuestion(test)
        assert(insertedId > 0) { "AddQuestion should return positive ID" }
        
        // Cleanup
        securityDao.deleteQuestion(test)
    }
    
    private suspend fun testInsertQuestion() = executeTest(CrudOperation.CREATE, "insertQuestion") {
        val test = createTestQuestion(questionId = 99998)
        val insertedId = securityDao.insertQuestion(test)
        assert(insertedId > 0) { "InsertQuestion should return positive ID" }
        
        // Cleanup
        securityDao.deleteQuestion(test)
    }
    
    private suspend fun testUpdateQuestion() = executeTest(CrudOperation.UPDATE, "updateQuestion") {
        val test = createTestQuestion()
        securityDao.insertQuestion(test)
        
        val updated = test.copy(QuestionText = "Updated Question?")
        securityDao.updateQuestion(updated)
        
        // Cleanup
        securityDao.deleteQuestion(updated)
    }
    
    private suspend fun testDeleteQuestion() = executeTest(CrudOperation.DELETE, "deleteQuestion") {
        val test = createTestQuestion()
        securityDao.insertQuestion(test)
        securityDao.deleteQuestion(test)
    }
    
    private suspend fun testSaveAnswer() = executeTest(CrudOperation.CREATE, "saveAnswer") {
        val test = createTestAnswer()
        securityDao.saveAnswer(test)
    }
    
    private suspend fun testInsertAnswer() = executeTest(CrudOperation.CREATE, "insertAnswer") {
        val test = createTestAnswer(answerId = 99998)
        val insertedId = securityDao.insertAnswer(test)
        assert(insertedId > 0) { "InsertAnswer should return positive ID" }
    }
    
    private suspend fun testGetAllQuestions() = executeTest(CrudOperation.READ, "getAllQuestions") {
        securityDao.getAllQuestions()
    }
    
    private suspend fun testAnswers() = executeTest(CrudOperation.QUERY, "answers") {
        securityDao.answers(9999)
    }
    
    private suspend fun testGetUserAnswers() = executeTest(CrudOperation.QUERY, "getUserAnswers") {
        securityDao.getUserAnswers(9999)
    }
    
    private suspend fun testInsertAllQuestions() = executeTest(CrudOperation.BULK_OPERATION, "insertAllQuestions") {
        val questions = listOf(
            createTestQuestion(questionId = 99990),
            createTestQuestion(questionId = 99991)
        )
        securityDao.insertAllQuestions(questions)
        
        // Cleanup
        questions.forEach { securityDao.deleteQuestion(it) }
    }
    
    private suspend fun testGetQuestionCount() = executeTest(CrudOperation.COUNT, "getQuestionCount") {
        val count = securityDao.getQuestionCount()
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private fun createTestQuestion(questionId: Int = 99999): SecurityQuestion {
        return SecurityQuestion(
            Question_ID = questionId,
            QuestionText = "Test Question?"
        )
    }
    
    private fun createTestAnswer(answerId: Int = 99999): UserSecurityAnswer {
        return UserSecurityAnswer(
            Answer_ID = answerId,
            User_ID = 9999,
            Question_ID = 99999,
            AnswerHash = "test_hash"
        )
    }
}
