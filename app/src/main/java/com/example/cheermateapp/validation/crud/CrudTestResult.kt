package com.example.cheermateapp.validation.crud

/**
 * Result of a single CRUD operation test
 * 
 * @property daoName Name of the DAO being tested
 * @property operation CRUD operation being tested
 * @property methodName Name of the method being tested
 * @property success Whether the operation succeeded
 * @property errorMessage Error message if operation failed
 * @property executionTimeMs Time taken to execute the operation in milliseconds
 * @property severity Severity of any issues found
 * @property recommendation Suggested improvement or fix
 */
data class CrudTestResult(
    val daoName: String,
    val operation: CrudOperation,
    val methodName: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0,
    val severity: IssueSeverity? = null,
    val recommendation: String? = null
)
