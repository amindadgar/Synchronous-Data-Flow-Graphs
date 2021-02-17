import java.io.File
import java.lang.StringBuilder
import kotlin.collections.ArrayList

class Compute {

    val outputLog: StringBuilder = StringBuilder()

    init {
        // free the output File
        File("output.txt").writeText("")
    }

    /** we have implemented two type of functions for calculation
     *  function computeExactLatency and computeExactThroughput is just showing the throughput and latency value
     *  function computeAll is for showing the results in interval
     *
     *  two functions computeExactLatency and computeExactThroughput Must be maintained! ( INCORRECT )
     */
    private fun computeExactLatency(actors: ArrayList<Actor>): Int {
        var totalLatency: Int = 0
        actors.forEach { totalLatency += it.latency!! }

        var loop: Boolean = false
        var totalLoopTokens: Int = 0
        actors.forEachIndexed { index, actor ->
            for (conn in actor.outConnectionsToken) {
                totalLoopTokens += conn.second
                if (conn.first < index && index == actors.size - 1) {
                    loop = true
                    outputLog.append("Loop from $index, ${conn.first}\n")
                }
            }
        }

        val returnLatency: Int
        returnLatency = if (totalLoopTokens > 0) {
            outputLog.append("Total latency (Having a loop): $totalLatency\n")
            totalLatency / totalLoopTokens
        } else {
            outputLog.append("Total latency: $totalLatency\n")
            totalLatency
        }
        return returnLatency
    }

    private fun computeExactThroughput(actors: ArrayList<Actor>): Int {
        val totalLatency = arrayListOf<Int>(0)

        // sum every actor's latency before and after token
        var index = 0
        actors.forEachIndexed { i, actor ->
            totalLatency[index] += actor.latency!!
            var haveToken = false
            for (conn in actor.outConnectionsToken) {
                if (conn.second > 0) {
                    haveToken = true
                    outputLog.append("${conn.second} tokens in for ${i + 1} found\n")
                }
            }
            // if we had token, get the token and save it
            if (haveToken) {
                // the goal of the line below is to make a space in array for new value
                totalLatency.add(++index, 0)
            }
        }

        var biggerLatency = 0
        totalLatency.forEach {
            outputLog.append("Part Graph Throughput $it\n")
            if (biggerLatency < it)
                biggerLatency = it
        }
        outputLog.append("Whole Graph Throughput: $biggerLatency\n")
        return biggerLatency

    }


    // Check the input tokens
    // Weather the actor can fire or not
    private fun checkInput(actor: Actor): Boolean {
        var boolean: Boolean = true

        // check the input tokens
        for (tokens in actor.inputConnectionsToken) {
            // if the input tokens was less than input rate, so it can't fire
            if (tokens.second < actor.inputRate!!)
                boolean = false
        }
        return boolean
    }

    // Consume the tokens in graph
    private fun consumeTokens(actor: Actor): Boolean {
        var i = 0
        while (i < actor.inputConnectionsToken.size) {
            // reassign the variable to consume tokens
            actor.inputConnectionsToken[i] = Pair(
                actor.inputConnectionsToken[i].first,
                actor.inputConnectionsToken[i].second - actor.inputRate!!
            )
            // add the i, due to consume the tokens from other vectors
            i++
        }
        return true
    }

    // add the tokens on the vector of graph
    // the first input is the next actor
    // the second input is the index of actor in use
    // the third element is the output rate of the actor in use
    private fun fillInputs(actor: Actor, index: Int, tokenIncrease: Int): Boolean {
        val result = actor.inputConnectionsToken.find { it.first == index }

        return if (result != null) {
            val dataIndex = actor.inputConnectionsToken.indexOf(result)
            actor.inputConnectionsToken[dataIndex] = Pair(result.first, result.second + tokenIncrease)
            true
        } else
            false
    }

    // steps is changeable due to user input
    // in fact steps is a time slice (Or clock)
    fun computeAll(actors: ArrayList<Actor>, steps: Int) {
        var time = 0
        val startTime = System.nanoTime()
        val outputTimes = arrayListOf<Int>()
        var firstTimeToken: Int? = null
        while (time <= steps) {
            actors.forEachIndexed { index, actor ->

                // if the actor is busy ( processing input token )
                if (actor.processFinishTime > 0) {
                    // decrease the processing timer
                    actor.processFinishTime--

                    // If the timer came to zero, then fire the actor and
                    // Put tokens on next graph vectors ( output vectors )
                    if (actor.processFinishTime == 0) {
                        // fill the inputs of the next actors
                        for (vectors in actor.outConnectionsToken) {
                            fillInputs(actors[vectors.first], index, actor.outputRate!!)
                        }

                        // if it was the last actor so its the output token
                        if (index + 1 == actors.size) {
                            if (firstTimeToken == null)
                                firstTimeToken = time
                            outputTimes.add(time)
                            println("$time ns: output came")
                            outputLog.append("$time ns: output came\n")
                        }

                        // Again if we could fire , set the processFinishTime ( timer )
                        if (checkInput(actor)) {
                            actor.processFinishTime = actor.latency!!
                            consumeTokens(actor)
                        }
                    }
                } else {
                    // else if the actor was not busy
                    // check if it can fire
                    if (checkInput(actor)) {
                        actor.processFinishTime = actor.latency!!
                        consumeTokens(actor)
                    }
                }
            }
            time++
        }
        outputLog.append("-------------------------------------\n")

//        outputLog.append("Compute With method One:\n")
//        var throughput = computeExactThroughput(actors)
//        val latency = computeExactLatency(actors)
//        outputLog.append("-------------------------------------")
//        outputLog.append("Compute with method two (using time steps shown at the top)\n")

        // if any tokens was in graph the variable firsTimeToken ( Latency variable ) is obsolete
        // because the the first time a token out is seen is not our input token
        // else if there was no token in the graph
        // so the first token out is our latency ( out token is our input token )
        val graphTokens = checkVectorTokens(actors)
        if (graphTokens != 0)
            outputLog.append("Latency: unknown\n")
        else
            outputLog.append("Latency: $firstTimeToken\n")

        var throughput = 0
        if (outputTimes.size > 1)
            throughput = outputTimes[outputTimes.size - 1] - outputTimes[outputTimes.size - 2]
        outputLog.append("Throughput: $throughput\n")

        val algorithmTime = System.nanoTime() - startTime

        writeToFile(outputLog.toString())
        println("algorithm time: ${algorithmTime / 9} ns")
    }

    // check the token count that is in graph
    private fun checkVectorTokens(actors: ArrayList<Actor>): Int {
        var totalTokensInGraph: Int = 0
        actors.forEach { actor ->
            actor.outConnectionsToken.forEach { actorOutConnection ->
                totalTokensInGraph += actorOutConnection.second
            }
        }
        return totalTokensInGraph
    }

    private fun writeToFile(text: Any) {
        val output = File("output.txt")
        print(text.toString())
        output.appendText(text.toString() + "\n")
    }


}