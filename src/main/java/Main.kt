import kotlin.jvm.JvmStatic
import com.raylib.Raylib
import com.raylib.Jaylib.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.thread


const val WIDTH = 1920
const val HEIGHT = 1080

const val FRAME_TIME_NANOS: Long = 16000000

const val RBL = 10

class Vector2(val x: Int, val y: Int) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

enum class Direction(val x: Int, val y: Int) {
    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0)
}

class Snake(
        val segments: PersistentList<Segment> = persistentListOf(Segment(Vector2(0, 0), Vector2(0, 0))),
        val direction: Direction = Direction.UP,
        //val position: Vector2 = Vector2(0, 0)
) {
}

class Segment(val start: Vector2, val end: Vector2) {

    override fun toString(): String {
        return "Segment ($start, $end}"
    }
}

//class Point(val x: Int, val y: Int)

class State(val snakes: PersistentMap<Int, Snake> = persistentMapOf()) {
}

class Input(val playerInputs: HashMap<Int, Direction> = HashMap())

object Main {
    val game = Game()

    @JvmStatic
    fun main(args: Array<String>) {
        game.run()
    }
}

class Game {

    var zoom = 1.0


    @Volatile
    var currentState = State(persistentMapOf(Pair(0, Snake())))
    var oldState = currentState
    val inputArray = HashMap<Long, Input>()


    fun run() {
        Raylib.InitWindow(WIDTH, HEIGHT, "Demo")
        Raylib.SetTargetFPS(60)

        val updateThread = thread { update() }

        while (!Raylib.WindowShouldClose()) {
            draw(currentState)
        }
        Raylib.CloseWindow()
        updateThread.stop()
    }

    fun update() {
        var frame: Long = 0
        val startTime = System.nanoTime()
        //var timer = System.nanoTime()
        while (true) {
            val inputDirection = when {
                IsKeyDown(KEY_UP) -> Direction.UP
                IsKeyDown(KEY_DOWN) -> Direction.DOWN
                IsKeyDown(KEY_LEFT) -> Direction.LEFT
                IsKeyDown(KEY_RIGHT) -> Direction.RIGHT
                else -> null
            }

            var input = Input()

            //var inputs = persistentMapOf<Int, Direction>()
            if (inputDirection != null) {
                input.playerInputs.put(0, inputDirection)
            }
            inputArray.put(frame-50, input)

            // if(frame>100) {
            oldState = nextState(inputArray.getOrDefault(frame-101 , Input()), oldState)
            var tempState = oldState
            for (i in frame - 100 .. frame) {
                //println(i)
                tempState = nextState(inputArray.getOrDefault(i, Input()), tempState)
            }

            currentState = tempState //nextState(inputArray.getOrDefault(frame, Input()), state)

            // }
            frame++
            val timer = startTime + frame * FRAME_TIME_NANOS

            while (System.nanoTime() < timer) {

            }
        }
    }


    fun nextState(inputs: Input, state: State): State {
        var newSnakes = state.snakes

        for ((id, snake) in state.snakes) {
            var segments = snake.segments

            val input = inputs.playerInputs.get(id)

            if (input != null && snake.direction != input) {
                val position = segments.last().end
                segments = segments.add(Segment(position, position))
            }

            val direction = input ?: snake.direction
            val oldStart = segments.last().start
            val newPosition = Vector2(segments.last().end.x + direction.x, segments.last().end.y + direction.y)
            val newLastSegment = Segment(oldStart, newPosition)
            segments = segments.remove(segments.last()).add(newLastSegment)


            val newSnake = Snake(segments, direction)
            newSnakes = newSnakes.put(id, newSnake)
        }
        return State(newSnakes)

    }

    val zoomX = 1f
    val zoomY = -1f

//    fun convertX(x: Int): Int{
//        return (x / zoomX) + WIDTH/2
//    }
//
//    fun convertY(y: Int): Int{
//        return (y / zoomY) + HEIGHT/2
//    }

    fun convertRL(p: Vector2): Raylib.Vector2 {

        return Raylib.Vector2().x((p.x / zoomX) + WIDTH / 2).y((p.y / zoomY) + HEIGHT / 2)
    }

    private fun draw(state: State) {
        BeginDrawing()
        ClearBackground(BLACK)

        for (snake in state.snakes.values) {
            for (segment in snake.segments) {
                DrawLineEx(convertRL(segment.start), convertRL(segment.end), 1f, WHITE)
            }
        }


        DrawFPS(20, 20)
        EndDrawing()
    }
}