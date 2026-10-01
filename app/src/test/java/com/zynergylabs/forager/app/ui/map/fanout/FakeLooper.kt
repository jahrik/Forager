package com.zynergylabs.forager.app.ui.map.fanout

/**
 * The main looper as the camera-move classifier meets it: tasks run in the order they were posted, and a task posted while the queue is being drained
 * goes to the back. [runAll] drains it, as the real looper would between two of the app's calls.
 */
class FakeLooper {
    private val queue = ArrayDeque<() -> Unit>()

    fun post(task: () -> Unit) {
        queue.addLast(task)
    }

    /** One turn: the task at the front runs, and what it posts goes to the back. */
    fun runNext() {
        queue.removeFirst().invoke()
    }

    fun runAll() {
        while (queue.isNotEmpty()) queue.removeFirst().invoke()
    }
}
