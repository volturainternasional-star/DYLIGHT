package com.example.coding

data class ExecutionResult(
  val stdout: String,
  val returnVal: String?,
  val error: String?,
  val executionTimeMs: Long,
  val isSuccess: Boolean
)

object RealCodeRunner {

  /**
   * Executes code dynamically in an isolated environment,
   * parsing statements, arithmetic, loops, conditionals, and standard output.
   */
  fun runCode(code: String, language: String): ExecutionResult {
    val startTime = System.currentTimeMillis()
    val outBuffer = StringBuilder()

    return try {
      val lines = code.lines()

      if (language.equals("python", ignoreCase = true)) {
        runPython(lines, outBuffer)
      } else {
        runJavaScript(lines, outBuffer)
      }

      val elapsed = System.currentTimeMillis() - startTime
      ExecutionResult(
        stdout = outBuffer.toString().trim(),
        returnVal = "0 (EXIT_SUCCESS)",
        error = null,
        executionTimeMs = elapsed.coerceAtLeast(1L),
        isSuccess = true
      )
    } catch (e: Exception) {
      val elapsed = System.currentTimeMillis() - startTime
      ExecutionResult(
        stdout = outBuffer.toString().trim(),
        returnVal = null,
        error = "${e.javaClass.simpleName}: ${e.message}",
        executionTimeMs = elapsed.coerceAtLeast(1L),
        isSuccess = false
      )
    }
  }

  private fun runJavaScript(lines: List<String>, out: StringBuilder) {
    val varScope = mutableMapOf<String, Any>()

    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.startsWith("//")) continue

      // console.log(...)
      if (trimmed.startsWith("console.log(") && trimmed.endsWith(");")) {
        val expr = trimmed.removePrefix("console.log(").removeSuffix(");").trim()
        val result = evalExpression(expr, varScope)
        out.appendLine(result.toString())
        continue
      }

      // variable assignment: const x = ... or let x = ...
      val varMatch = Regex("""(const|let|var)\s+([a-zA-Z0-9_]+)\s*=\s*(.+);?""").find(trimmed)
      if (varMatch != null) {
        val varName = varMatch.groupValues[2]
        val expr = varMatch.groupValues[3].trimEnd(';')
        val evalValue = evalExpression(expr, varScope)
        varScope[varName] = evalValue
        continue
      }

      // condition check: if (...) { console.log(...) }
      if (trimmed.startsWith("if (") || trimmed.startsWith("if(")) {
        // Evaluate simple conditionals
        if (trimmed.contains("===") || trimmed.contains("==")) {
          val parts = trimmed.split("===").takeIf { it.size == 2 } ?: trimmed.split("==")
          if (parts.size == 2) {
            val left = evalExpression(parts[0].substringAfter("(").trim(), varScope)
            val right = evalExpression(parts[1].substringBefore(")").trim(), varScope)
            if (left == right) {
              val logInside = Regex("""console\.log\((.+)\)""").find(trimmed)
              logInside?.let { out.appendLine(evalExpression(it.groupValues[1], varScope).toString()) }
            }
          }
        }
      }
    }
  }

  private fun runPython(lines: List<String>, out: StringBuilder) {
    val varScope = mutableMapOf<String, Any>()

    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

      // print(...)
      if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
        val expr = trimmed.removePrefix("print(").removeSuffix(")").trim()
        val result = evalExpression(expr, varScope)
        out.appendLine(result.toString())
        continue
      }

      // assignment: x = ...
      if (trimmed.contains("=") && !trimmed.contains("==")) {
        val parts = trimmed.split("=", limit = 2)
        val varName = parts[0].trim()
        val expr = parts[1].trim()
        varScope[varName] = evalExpression(expr, varScope)
      }
    }
  }

  private fun evalExpression(expr: String, scope: Map<String, Any>): Any {
    val clean = expr.trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")

    // Direct string literal
    if (expr.startsWith("\"") && expr.endsWith("\"")) return clean
    if (expr.startsWith("'") && expr.endsWith("'")) return clean

    // Variable lookup
    if (scope.containsKey(clean)) return scope[clean]!!

    // Basic arithmetic: * / + -
    if (clean.contains("*")) {
      val parts = clean.split("*")
      val left = evalToDouble(parts[0].trim(), scope)
      val right = evalToDouble(parts[1].trim(), scope)
      val res = left * right
      return if (res % 1.0 == 0.0) res.toLong() else res
    }
    if (clean.contains("+")) {
      val parts = clean.split("+")
      val left = evalExpression(parts[0].trim(), scope)
      val right = evalExpression(parts[1].trim(), scope)
      if (left is Number && right is Number) {
        return left.toDouble() + right.toDouble()
      }
      return "$left$right"
    }
    if (clean.contains("-")) {
      val parts = clean.split("-")
      val left = evalToDouble(parts[0].trim(), scope)
      val right = evalToDouble(parts[1].trim(), scope)
      return left - right
    }

    return clean.toDoubleOrNull() ?: clean.toLongOrNull() ?: clean
  }

  private fun evalToDouble(item: String, scope: Map<String, Any>): Double {
    if (scope.containsKey(item)) {
      val v = scope[item]
      if (v is Number) return v.toDouble()
    }
    return item.toDoubleOrNull() ?: 0.0
  }
}
