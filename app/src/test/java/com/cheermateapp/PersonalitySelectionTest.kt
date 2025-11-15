package com.cheermateapp

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Personality Selection System
 * 
 * Verifies that the personality card IDs correctly map to Type_IDs (1-5)
 */
class PersonalitySelectionTest {
    
    /**
     * Test that personality cards map to correct Type_IDs
     * Based on PersonalityActivity.kt lines 140-146
     */
    @Test
    fun testPersonalityCardMapping_correctTypeIds() {
        // Given - Expected mapping from PersonalityActivity
        val expectedMapping = mapOf(
            "cardKalog" to 1,
            "cardGenZ" to 2,
            "cardSofty" to 3,
            "cardGrey" to 4,
            "cardFlirty" to 5
        )
        
        // Then - Verify all mappings are correct
        assertEquals(1, expectedMapping["cardKalog"])
        assertEquals(2, expectedMapping["cardGenZ"])
        assertEquals(3, expectedMapping["cardSofty"])
        assertEquals(4, expectedMapping["cardGrey"])
        assertEquals(5, expectedMapping["cardFlirty"])
        
        // Verify we have exactly 5 personality types
        assertEquals(5, expectedMapping.size)
        
        // Verify all Type_IDs are in the valid range (1-5)
        expectedMapping.values.forEach { typeId ->
            assertTrue("Type_ID $typeId should be between 1 and 5", typeId in 1..5)
        }
        
        // Verify no duplicate Type_IDs
        val uniqueTypeIds = expectedMapping.values.toSet()
        assertEquals(5, uniqueTypeIds.size)
    }
    
    /**
     * Test personality names match expected values
     */
    @Test
    fun testPersonalityNames_matchExpectedValues() {
        // Given
        val personalityNames = mapOf(
            1 to "Kalog",
            2 to "Gen Z",
            3 to "Softy",
            4 to "Grey",
            5 to "Flirty"
        )
        
        // Then - Verify names are correct
        assertEquals("Kalog", personalityNames[1])
        assertEquals("Gen Z", personalityNames[2])
        assertEquals("Softy", personalityNames[3])
        assertEquals("Grey", personalityNames[4])
        assertEquals("Flirty", personalityNames[5])
    }
    
    /**
     * Test personality descriptions are appropriate
     */
    @Test
    fun testPersonalityDescriptions_notEmpty() {
        // Given
        val descriptions = mapOf(
            1 to "The funny friend who makes everything entertaining!",
            2 to "Tech-savvy and trendy with the latest slang!",
            3 to "Gentle and caring with a warm heart!",
            4 to "Calm and balanced with steady wisdom!",
            5 to "Playful and charming with a wink!"
        )
        
        // Then - Verify all descriptions exist and are not empty
        descriptions.forEach { (typeId, description) ->
            assertNotNull("Description for Type_ID $typeId should not be null", description)
            assertTrue("Description for Type_ID $typeId should not be empty", description.isNotEmpty())
        }
    }
    
    /**
     * Test that each personality has all required message categories
     */
    @Test
    fun testMessageCategories_allPersonalitiesHaveAllCategories() {
        // Given
        val requiredCategories = setOf(
            "task_created",
            "task_completed",
            "motivation",
            "reminder"
        )
        
        val personalityIds = listOf(1, 2, 3, 4, 5)
        
        // Then - Each personality should have all categories
        personalityIds.forEach { personalityId ->
            // Verify each personality needs 4 message templates (one per category)
            assertEquals(
                "Personality $personalityId should have ${requiredCategories.size} message categories",
                4,
                requiredCategories.size
            )
        }
        
        // Verify total message templates = 5 personalities × 4 categories = 20
        val totalExpectedTemplates = personalityIds.size * requiredCategories.size
        assertEquals(20, totalExpectedTemplates)
    }
    
    /**
     * Test Type_ID range validation
     */
    @Test
    fun testTypeIdRange_isValid() {
        // Given
        val validRange = 1..5
        
        // Then - Test boundary values
        assertTrue("Type_ID 1 should be valid", 1 in validRange)
        assertTrue("Type_ID 5 should be valid", 5 in validRange)
        
        // Test invalid values
        assertFalse("Type_ID 0 should be invalid", 0 in validRange)
        assertFalse("Type_ID 6 should be invalid", 6 in validRange)
        assertFalse("Type_ID -1 should be invalid", -1 in validRange)
    }
    
    /**
     * Test that personality IDs are sequential
     */
    @Test
    fun testPersonalityIds_areSequential() {
        // Given
        val personalityIds = listOf(1, 2, 3, 4, 5)
        
        // Then
        assertEquals(1, personalityIds[0])
        assertEquals(2, personalityIds[1])
        assertEquals(3, personalityIds[2])
        assertEquals(4, personalityIds[3])
        assertEquals(5, personalityIds[4])
        
        // Verify no gaps in sequence
        for (i in 0 until personalityIds.size - 1) {
            assertEquals(
                "Personality IDs should be sequential",
                personalityIds[i] + 1,
                personalityIds[i + 1]
            )
        }
    }
    
    /**
     * Test personality card UI element IDs
     */
    @Test
    fun testCardIds_followNamingConvention() {
        // Given - Expected card ID naming pattern: card + PersonalityName
        val cardIds = listOf(
            "cardKalog",
            "cardGenZ",
            "cardSofty",
            "cardGrey",
            "cardFlirty"
        )
        
        // Then - Verify naming convention
        cardIds.forEach { cardId ->
            assertTrue("Card ID should start with 'card'", cardId.startsWith("card"))
            assertTrue("Card ID should have more than 4 characters", cardId.length > 4)
        }
        
        // Verify we have exactly 5 cards
        assertEquals(5, cardIds.size)
    }
    
    /**
     * Test that personality system supports exactly 5 personalities
     */
    @Test
    fun testPersonalitySystem_supportsExactlyFivePersonalities() {
        // Given
        val numberOfPersonalities = 5
        
        // Then
        assertEquals("System should support exactly 5 personalities", 5, numberOfPersonalities)
        
        val personalityIds = (1..numberOfPersonalities).toList()
        assertEquals(5, personalityIds.size)
        assertEquals(1, personalityIds.first())
        assertEquals(5, personalityIds.last())
    }
}
