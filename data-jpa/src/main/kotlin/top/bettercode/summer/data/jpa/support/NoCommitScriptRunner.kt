package top.bettercode.summer.data.jpa.support

import java.io.BufferedReader
import java.io.PrintWriter
import java.io.Reader
import java.sql.*
import java.util.regex.Pattern

class NoCommitScriptRunner(private val connection: Connection) {
    private var stopOnError = false
    private var throwWarning = false
    private var sendFullScript = false
    private var removeCRs = false
    private var escapeProcessing = true
    private var logWriter: PrintWriter? = PrintWriter(System.out)
    private var errorLogWriter: PrintWriter? = PrintWriter(System.out)
    private var delimiter = DEFAULT_DELIMITER
    private var fullLineDelimiter = false
    fun setStopOnError(stopOnError: Boolean) {
        this.stopOnError = stopOnError
    }

    fun setThrowWarning(throwWarning: Boolean) {
        this.throwWarning = throwWarning
    }

    fun setSendFullScript(sendFullScript: Boolean) {
        this.sendFullScript = sendFullScript
    }

    fun setRemoveCRs(removeCRs: Boolean) {
        this.removeCRs = removeCRs
    }

    fun setEscapeProcessing(escapeProcessing: Boolean) {
        this.escapeProcessing = escapeProcessing
    }

    fun setLogWriter(logWriter: PrintWriter?) {
        this.logWriter = logWriter
    }

    fun setErrorLogWriter(errorLogWriter: PrintWriter?) {
        this.errorLogWriter = errorLogWriter
    }

    fun setDelimiter(delimiter: String) {
        this.delimiter = delimiter
    }

    fun setFullLineDelimiter(fullLineDelimiter: Boolean) {
        this.fullLineDelimiter = fullLineDelimiter
    }

    fun runScript(reader: Reader) {
        if (sendFullScript) {
            executeFullScript(reader)
        } else {
            executeLineByLine(reader)
        }
    }

    private fun executeFullScript(reader: Reader) {
        val script = StringBuilder()
        try {
            val lineReader = BufferedReader(reader)
            var line: String?
            while (lineReader.readLine().also { line = it } != null) {
                script.append(line)
                script.append(LINE_SEPARATOR)
            }
            val command = script.toString()
            println(command)
            executeStatement(command)
        } catch (e: Exception) {
            val message = "Error executing: $script.  Cause: $e"
            printlnError(message)
            throw RuntimeException(message, e)
        }
    }

    private fun executeLineByLine(reader: Reader) {
        val command = StringBuilder()
        try {
            val lineReader = BufferedReader(reader)
            var line: String
            while (lineReader.readLine().also { line = it } != null) {
                handleLine(command, line)
            }
            checkForMissingLineTerminator(command)
        } catch (e: Exception) {
            val message = "Error executing: $command.  Cause: $e"
            printlnError(message)
            throw RuntimeException(message, e)
        }
    }

    private fun checkForMissingLineTerminator(command: StringBuilder?) {
        if (command != null && command.toString().trim { it <= ' ' }.isNotEmpty()) {
            throw RuntimeException(
                    "Line missing end-of-line terminator ($delimiter) => $command")
        }
    }

    @Throws(SQLException::class)
    private fun handleLine(command: StringBuilder, line: String) {
        val trimmedLine = line.trim { it <= ' ' }
        if (lineIsComment(trimmedLine)) {
            val matcher = DELIMITER_PATTERN.matcher(trimmedLine)
            if (matcher.find()) {
                delimiter = matcher.group(5)
            }
            println(trimmedLine)
        } else if (commandReadyToExecute(trimmedLine)) {
            command.append(line, 0, line.lastIndexOf(delimiter))
            command.append(LINE_SEPARATOR)
            println(command)
            executeStatement(command.toString())
            command.setLength(0)
        } else if (trimmedLine.isNotEmpty()) {
            command.append(line)
            command.append(LINE_SEPARATOR)
        }
    }

    private fun lineIsComment(trimmedLine: String): Boolean {
        return trimmedLine.startsWith("//") || trimmedLine.startsWith("--")
    }

    private fun commandReadyToExecute(trimmedLine: String): Boolean {
        // issue #561 remove anything after the delimiter
        return !fullLineDelimiter && trimmedLine.contains(delimiter) || fullLineDelimiter && (trimmedLine
                == delimiter)
    }

    @Throws(SQLException::class)
    fun executeStatement(command: String) {
        var hasResults = false
        val statement = connection.createStatement()
        statement.setEscapeProcessing(escapeProcessing)
        var sql = command
        if (removeCRs) {
            sql = sql.replace("\r\n".toRegex(), "\n")
        }
        if (stopOnError) {
            hasResults = statement.execute(sql)
            if (throwWarning) {
                // In Oracle, CRATE PROCEDURE, FUNCTION, etc. returns warning
                // instead of throwing exception if there is compilation error.
                val warning = statement.warnings
                if (warning != null) {
                    throw warning
                }
            }
        } else {
            try {
                hasResults = statement.execute(sql)
            } catch (e: SQLException) {
                val message = "Error executing: $command.  Cause: $e"
                printlnError(message)
            }
        }
        printResults(statement, hasResults)
        try {
            statement.close()
        } catch (e: Exception) {
            // Ignore to workaround a bug in some connection pools
        }
    }

    private fun printResults(statement: Statement, hasResults: Boolean) {
        try {
            if (hasResults) {
                val rs = statement.resultSet
                if (rs != null) {
                    val md = rs.metaData
                    val cols = md.columnCount
                    for (i in 0 until cols) {
                        val name = md.getColumnLabel(i + 1)
                        print(name + "\t")
                    }
                    println("")
                    while (rs.next()) {
                        for (i in 0 until cols) {
                            val value = rs.getString(i + 1)
                            print(value + "\t")
                        }
                        println("")
                    }
                }
            }
        } catch (e: SQLException) {
            printlnError("Error printing results: " + e.message)
        }
    }

    private fun print(o: Any) {
        if (logWriter != null) {
            logWriter!!.print(o)
            logWriter!!.flush()
        }
    }

    private fun println(o: Any) {
        if (logWriter != null) {
            logWriter!!.println(o)
            logWriter!!.flush()
        }
    }

    private fun printlnError(o: Any) {
        if (errorLogWriter != null) {
            errorLogWriter!!.println(o)
            errorLogWriter!!.flush()
        }
    }

    companion object {
        private val LINE_SEPARATOR = System.getProperty("line.separator", "\n")
        private const val DEFAULT_DELIMITER = ";"
        private val DELIMITER_PATTERN = Pattern
                .compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+(\\S+)", Pattern.CASE_INSENSITIVE)
    }
}
