package com.example

import com.example.data.GameSecRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testModulesLoaded() {
    val modules = GameSecRepository.modules
    assertEquals(10, modules.size)
    assertEquals("Client dan Server", modules[0].title)
    assertEquals("Berpikir Kritis & Verifikasi", modules[9].title)
    assertTrue(modules[0].content.isNotBlank())
  }

  @Test
  fun testStaticModuleList() {
    val staticModules = GameSecRepository.staticModuleList
    assertEquals(10, staticModules.size)
    assertEquals("Client dan Server", staticModules[0].title)
    assertTrue(staticModules[0].content.contains("Client adalah perangkat"))
    // Modul 2 has steps
    assertEquals("Memory & Runtime", staticModules[1].title)
    val mod2Steps = staticModules[1].steps
    assertTrue(mod2Steps != null && mod2Steps.isNotEmpty())
  }

  @Test
  fun testQuizQuestionsValid() {
    val quiz = GameSecRepository.quizQuestions
    assertTrue(quiz.isNotEmpty())
    quiz.forEach { q ->
      assertTrue(q.options.isNotEmpty())
      assertTrue(q.correctIndex in q.options.indices)
      assertTrue(q.explanation.isNotBlank())
    }
  }

  @Test
  fun testTerminalCommands() {
    val helpOutput = GameSecRepository.executeTerminalCommand("help")
    assertTrue(helpOutput.contains("architecture"))
    assertTrue(helpOutput.contains("validate movement"))
    assertTrue(helpOutput.contains("validate cooldown"))
    assertTrue(helpOutput.contains("rate limit demo"))

    val archOutput = GameSecRepository.executeTerminalCommand("architecture")
    assertTrue(archOutput.contains("Authoritative truth"))

    val movementOutput = GameSecRepository.executeTerminalCommand("validate movement")
    assertTrue(movementOutput.contains("MELAMPAUI BATAS"))

    val cooldownOutput = GameSecRepository.executeTerminalCommand("validate cooldown")
    assertTrue(cooldownOutput.contains("COOLDOWN VIOLATION"))

    val rateLimitOutput = GameSecRepository.executeTerminalCommand("rate limit demo")
    assertTrue(rateLimitOutput.contains("429 TOO_MANY_REQUESTS"))

    val invalidOutput = GameSecRepository.executeTerminalCommand("unknown_cmd")
    assertTrue(invalidOutput.contains("tidak dikenal"))
  }

  @Test
  fun testForensicCasesAndCodingLoaded() {
    assertEquals(5, GameSecRepository.sampleInvestigationCases.size)
    assertEquals(3, GameSecRepository.sampleClaims.size)
    assertEquals(6, GameSecRepository.codingExercises.size)
    assertEquals(3, GameSecRepository.cryptoAssets.size)
    assertEquals(3, GameSecRepository.stockAssets.size)
    assertEquals(4, GameSecRepository.initialToolConfigs.size)
  }
}
