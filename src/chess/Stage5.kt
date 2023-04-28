package chess

import kotlin.math.abs
import kotlin.system.exitProcess


fun main() {
    PawnOnlyChess()
}

class PawnOnlyChess {
    val colorWhite = "white"
    val colorBlack = "black"
    val empty = " "
    var firstName = ""
    var secondName = ""
    val lines = MutableList(8) { "+---" }
    var board = MutableList(8) { MutableList(8) { empty } }
    val whitePawns = MutableList(8) { Pawn(colorWhite, it, 6) }
    val blackPawns = MutableList(8) { Pawn(colorBlack, it, 1) }

    init {
        board = MutableList(8) { i ->
            MutableList(8) {
                when (i) {
                    1 -> colorBlack.first().uppercase()
                    6 -> colorWhite.first().uppercase()
                    else -> empty
                }
            }
        }
        println("Pawns-Only Chess")
        println("First Player's name:")
        firstName = readln()
        println("Second Player's name:")
        secondName = readln()
        var turn = firstName
        board.currentBoardState()
        println()
        for (it in whitePawns) {
            board[it.col][it.row] = it.color.first().uppercase()
        }
        for (it in blackPawns) {
            board[it.col][it.row] = it.color.first().uppercase()
        }

        do {
            println("$turn's turn:")
            val playerInput = readln()
            if (playerInput.equals("exit", true)) {
                break
            }
            val color = if (turn == firstName) colorWhite else colorBlack
            val oppositeColor = when (color) {
                colorWhite -> colorBlack
                else -> colorWhite
            }
            if (!playerInput.valid()) {
                println("Invalid Input")
            } else {
                val input0 = playerInput[0].index()
                val input1 = 8 - playerInput[1].digitToInt()
                val input2 = playerInput[2].index()
                val input3 = 8 - playerInput[3].digitToInt()
                val winRank = if (color == colorWhite) 0 else 7
                val enPassantRow = if (color == colorWhite) 3 else 4
                if (board[input1][input0] != color.first().uppercase()) {
                    println("No $color pawn at ${playerInput.substring(0..1)}")
                } else {
                    if (board.validMove(input0, input1, input2, input3, color)) {

                        if (PawnAt(input2, input3).color == oppositeColor) PawnAt(input2, input3).captured = true

                        if (input1 == enPassantRow && PawnAt(input2, input1).color == oppositeColor && PawnAt(
                                input2,
                                input1
                            ).firstMove
                        ) {
                            PawnAt(input2, input1).captured = true
                            board[input1][input2] = empty
                        }

                        board[input1][input0] = empty
                        board[input3][input2] = color.first().uppercase()
                        PawnAt(input0, input1).row = input2
                        PawnAt(input2, input1).col = input3
                        board.currentBoardState()
                        println("\n")

                        if (input3 == winRank) {
                            println("${if (color == colorWhite) "White" else "Black"} Wins!")
                            break
                        }

                        if (board.whiteWon()) {
                            println("White Wins!")
                            break
                        }
                        if (board.blackWon()) {
                            println("Black Wins!")
                            break
                        }

                        if (stalemate()) {
                            println("Stalemate!")
                            break
                        }

                        if (color == colorWhite) {
                            for (it in blackPawns) {
                                if (it.col != 1) it.firstMove = false
                            }
                        } else {
                            for (it in whitePawns) {
                                if (it.col != 6) it.firstMove = false
                            }
                        }

                        turn = if (turn == firstName) secondName else firstName
                    } else println("Invalid Input")
                }
            }
        } while (true)
        println("Bye!")
        exitProcess(0)
    }

    fun MutableList<MutableList<String>>.currentBoardState() {
        val end = "    a   b   c   d   e   f   g   h\n"
        println("  ${lines.joinToString("")}+")
        this.forEachIndexed { index, i ->
            print("${8 - index} ")
            i.forEach { print("| $it ") }
            println(
                "|\n" +
                        "  ${lines.joinToString("")}+"
            )
        }
        print(end)

    }

    fun Char.index(): Int {
        return when (this) {
            'a' -> 0
            'b' -> 1
            'c' -> 2
            'd' -> 3
            'e' -> 4
            'f' -> 5
            'g' -> 6
            'h' -> 7
            else -> this.code - 97
        }

    }

    fun String.valid(): Boolean = "[a-h][1-8][a-h][1-8]".toRegex().matches(this)
    class Pawn(
        val color: String,
        var row: Int,
        var col: Int,
        var captured: Boolean = false,
        var firstMove: Boolean = true
    )

    fun PawnAt(r: Int, c: Int): Pawn {
        for (it in whitePawns) {
            if (it.row == r && it.col == c) return it
        }
        for (it in blackPawns) {
            if (it.row == r && it.col == c) return it
        }
        return Pawn("null", -1, -1)
    }

    fun MutableList<MutableList<String>>.validMove(
        input0: Int,
        input1: Int,
        input2: Int,
        input3: Int,
        color: String
    ): Boolean {
        val oppositeColor = when (color) {
            colorWhite -> colorBlack
            else -> colorWhite
        }

        val r1 = if (color == colorWhite) input1 else input3
        val r2 = if (color == colorWhite) input3 else input1
        val enPassantRow = if (color == colorWhite) 3 else 4

        if (input0 == input2) {
            return when {
                this[input3][input2] == oppositeColor[0].uppercase() -> false
                else -> r1 - r2 == 1 || PawnAt(input0, input1).firstMove && r1 - r2 == 2
            }

        } else {
            when {
                r1 - r2 != 1 || abs(input0 - input2) != 1 -> return false
                PawnAt(input2, input3).color == oppositeColor -> return true
                input1 == enPassantRow && PawnAt(input2, input1).color == oppositeColor && PawnAt(
                    input2,
                    input1
                ).firstMove -> return true
            }

        }

        return false
    }

    fun stalemate(): Boolean {
        var whiteCanMove = false
        var blackCanMove = false

        for (it in whitePawns) {
            when {
                !it.captured && (board.validMove(it.row, it.col, it.row, it.col - 1, it.color) || it.row != 0 &&
                        board.validMove(it.row, it.col, it.row - 1, it.col - 1, it.color) ||
                        it.row != 7 && board.validMove(
                    it.row,
                    it.col,
                    it.row + 1,
                    it.col - 1,
                    it.color
                )) -> whiteCanMove = true
            }
        }

        for (it in blackPawns) {
            when {
                !it.captured && (board.validMove(it.row, it.col, it.row, it.col + 1, it.color) ||
                        it.row != 0 && board.validMove(it.row, it.col, it.row - 1, it.col + 1, it.color) ||
                        it.row != 7 && board.validMove(
                    it.row,
                    it.col,
                    it.row + 1,
                    it.col + 1,
                    it.color
                )) -> blackCanMove = true
            }
        }

        return !whiteCanMove || !blackCanMove
    }

    fun countPawns(sym: String, board: MutableList<MutableList<String>>): Int {
        var count = 0
        board.forEach { row ->
            row.forEach { col ->
                when {
                    col.uppercase() == sym.uppercase() -> count++
                }
            }
        }
        return count
    }

    fun MutableList<MutableList<String>>.blackWon(): Boolean {
        return when {
            countPawns(colorWhite.first().uppercase(), this) == 0 -> true
            colorBlack.first().uppercase() in this[0].map { it.uppercase() } -> true
            else -> {
                this.forEach { row ->
                    row.forEach { cell ->
                        if (cell.uppercase() == colorWhite.first().uppercase()) return false
                    }
                }
                true
            }
        }
    }

    fun MutableList<MutableList<String>>.whiteWon(): Boolean {
        when {
            countPawns(colorBlack.first().uppercase(), this) == 0 -> return true
            colorWhite.first().uppercase() in this[this.lastIndex].map { it.uppercase() } -> return true
            else -> {
                this.forEach { row ->
                    row.forEach { cell ->
                        if (cell.uppercase() == colorBlack.first().uppercase()) return false
                    }
                }
                return true
            }
        }
    }


}

