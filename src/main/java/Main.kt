import com.raylib.Jaylib.*
import kotlin.jvm.JvmStatic
import com.raylib.Raylib
import com.raylib.Raylib.*
import kotlinx.collections.immutable.*

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
    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0), NONE (0,0), SPAWN(0,0)
}

class Snake(x: Int = 0, y: Int = 0,
        val segments: PersistentList<Segment> = persistentListOf(Segment(Vector2(x, y), Vector2(x, y))),
        val direction: Direction = Direction.UP,
        val color: Raylib.Color = RED
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
    var updateTime: Long = 0

    @Volatile
    var currentState = State(persistentMapOf())
    var oldState = currentState
    val inputArray = HashMap<Long, Input>()

    init {
        val snakes = HashMap<Int, Snake>()
        for (i in 1..1000) {
            val color = ColorFromHSV(i.toFloat()*3, 1f, 1f)
            val snake = Snake(x=i*10, y=i*10, color = color, direction = Direction.RIGHT)
            snakes.put(i, snake)
            //println("Snake $i, color ${snake.color.r()} ${snake.color.g()} ${snake.color.b()}")
        }


        currentState = State(snakes.toImmutableMap().toPersistentMap())
        oldState = currentState
    }


    fun run() {
        Raylib.InitWindow(WIDTH, HEIGHT, "Demo")
        Raylib.SetTargetFPS(60)



        val updateThread = thread {
            val t = System.nanoTime()
            update()
            val d = System.nanoTime()-t
            println(d)
        }

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
            val frameStartTime = System.nanoTime()
            val inputDirection = when {
                IsKeyDown(KEY_UP) -> Direction.UP
                IsKeyDown(KEY_DOWN) -> Direction.DOWN
                IsKeyDown(KEY_LEFT) -> Direction.LEFT
                IsKeyDown(KEY_RIGHT) -> Direction.RIGHT
                IsKeyDown(KEY_SPACE) -> Direction.SPAWN
                else -> null
            }
            if (inputDirection != null) {
                val input = Input()

                //var inputs = persistentMapOf<Int, Direction>()
                //
                input.playerInputs.put(0, inputDirection)
                //}
                inputArray.put(frame - 50, input)
            }

            // if(frame>100) {
            oldState = nextState(inputArray.getOrDefault(frame-101 , Input()), oldState)
            var tempState = oldState
            for (i in frame - 100 .. frame) {
                //println(i)
                tempState = nextState(inputArray.getOrDefault(i, Input()), tempState)
            }

            currentState = tempState //nextState(inputArray.getOrDefault(frame, Input()), state)

            // }

            val frameTimeTaken = System.nanoTime() - frameStartTime
            updateTime = frameTimeTaken/1000000

            frame++
            val timer = startTime + frame * FRAME_TIME_NANOS

            while (System.nanoTime() < timer) {
                //Thread.sleep(1)
            }


        }
    }


    fun nextState(inputs: Input, state: State): State {
        var newSnakes = state.snakes

//        if(newSnakes.size==0){
//            newSnakes = newSnakes.put(0, Snake())
//        }

        for ((id, input) in inputs.playerInputs){
            if(input == Direction.SPAWN){
                newSnakes = newSnakes.put(id, Snake())
            }
        }

        for ((id, snake) in newSnakes) {
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


            val newSnake = Snake(0, 0, segments, direction, snake.color)
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

    fun convertRL(p: Vector2, rlv: Raylib.Vector2): Raylib.Vector2 {

        return rlv.x((p.x / zoomX) + WIDTH / 2).y((p.y / zoomY) + HEIGHT / 2)
    }


    private fun draw(state: State) {
        BeginDrawing()
        ClearBackground(BLACK)

        for (snake in state.snakes.values) {
            //println("${snake.color.r()} ${snake.color.g()} ${snake.color.b()}")
            for (segment in snake.segments) {
                val rlv = Raylib.Vector2()
                val rlv2 = Raylib.Vector2()
                rlv.deallocate(false)
                rlv2.deallocate(false)

                DrawLineEx(convertRL(segment.start, rlv), convertRL(segment.end, rlv2), 22f, WHITE)
                DrawLineEx(convertRL(segment.start, rlv), convertRL(segment.end, rlv2), 20f, snake.color)
                DrawCircle(convertRL(segment.start, rlv).x().toInt(), convertRL(segment.start, rlv2).y().toInt(), 13f, WHITE)
                DrawCircle(convertRL(segment.start, rlv).x().toInt(), convertRL(segment.start, rlv2).y().toInt(), 12f, snake.color)

                DrawCircle(convertRL(segment.end, rlv).x().toInt(), convertRL(segment.end, rlv2).y().toInt(), 13f, WHITE)
                DrawCircle(convertRL(segment.end, rlv).x().toInt(), convertRL(segment.end, rlv2).y().toInt(), 12f, snake.color)

                rlv.deallocate()
                rlv2.deallocate()
            }
        }



        Raylib.DrawText(updateTime.toString(), 100, 20, 50, WHITE)
        DrawFPS(20, 20)
        EndDrawing()
    }
}