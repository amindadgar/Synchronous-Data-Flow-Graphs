import java.io.File
import kotlin.collections.ArrayList

class Compute {

    init {
        // free the output File
        File("output.txt").writeText("")
    }

    /** we have implemented two type of functions for calculation
     *  function computeExactLatency and computeExactThroughput is just showing the throughput and latency value
     *  function computeAll is for showing the results in interval
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
                    writeToFile("Loop from $index, ${conn.first}")
                }
            }
        }

        val returnLatency: Int
        returnLatency = if (totalLoopTokens > 0) {
            writeToFile("Total latency (Having a loop): ${totalLatency / totalLoopTokens}")
            totalLatency / totalLoopTokens
        } else {
            writeToFile("Total latency: $totalLatency")
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
                    writeToFile("${conn.second} tokens in for ${i + 1} found")
                }
            }
            // if we had token, get the token and save it
            if (haveToken) {
                totalLatency.add(++index, 0)
            }
        }

        var biggerLatency = 0
        totalLatency.forEach {
            writeToFile("Part Graph Throughput $it")
            if (biggerLatency < it)
                biggerLatency = it
        }
        writeToFile("Whole Graph Throughput: $biggerLatency")
        return biggerLatency

    }


    // Check the input tokens
    // Weather the actor can fire or not
    private fun checkInput(actor: Actor): Boolean {
        var boolean: Boolean = true
        // first check the actor if it is in processing state or not
        if (actor.processFinishTime > 0)
            actor.processFinishTime--

        if (actor.processFinishTime < 1) {
            // then the actor is not processing, check if it is having tokens to fire or not
            for (tokens in actor.inputConnectionsToken) {
                if (tokens.second < actor.inputRate!!)
                    boolean = false
            }
        } else {
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
    private fun fillInputs(actor: Actor, index: Int, tokenIncrease: Int = actor.outputRate!!): Boolean {
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

        val outputTimes = arrayListOf<Int>()
        var firstTimeToken: Int? = null
        while (time <= steps) {
            // deep copy the actors
            // the second copy is for changing the actors list states (tokens) after a clock (Or a time step)
            val actorsC2 = actors.map { actor ->
                val outC = actor.outConnectionsToken.map { it.copy() }
                val inC = actor.inputConnectionsToken.map { it.copy() }
                actor.copy(
                    actor.inputRate, actor.outputRate, actor.latency,
                    outC as ArrayList<Pair<Int, Int>>
                    , inC as ArrayList<Pair<Int, Int>>
                )
            }

            // check all the actors that can be fired and fire them
            actorsC2.forEachIndexed { index, actor ->
                // check if we can fire an actor
                // Note: if the actor processFinishTime is more than zero this will output false
                var c = checkInput(actor)

                // we will call checkInput function on actors array too
                // because we want changes of processFinishTime
                c = checkInput(actors[index])

                // if we can fire the actor ( c == true )
                // and if our actor process was finished!
                // Note: processFinishTime is -1 when we start the flow
                // the zero equal condition is for checking weather the actor was fired or not!
                if (c && actor.processFinishTime == 0) {
                    writeToFile("$time ns: Actor ${index + 1} Fired token! And it is going to processing state")

                    // fill the inputs of the next actors
                    for (vectors in actor.outConnectionsToken) {
                        fillInputs(actors[vectors.first], index)
                    }

                    // if the last actor was fired print a message
                    if (index + 1 == actors.size) {
                        // when first time a token is out then it is our Latency
                        if (firstTimeToken == null)
                            firstTimeToken = time

                        writeToFile("$time ns: output came")
                        outputTimes.add(time)
                    }
                    actors[index].processFinishTime = actors[index].latency!!
                    consumeTokens(actors[index])

                } else if (c) {
                    // here the processFinishTime was -1
                    // means processing state is starting ( The actor is busy and processing tokens )
                    // after we process tokens our processFinishTime would be zero
                    writeToFile("$time ns: Actor ${index + 1} is processing!")

                    // the actor is fired
                    // so add the processFinishTime to it
                    actors[index].processFinishTime = actors[index].latency!!
                    consumeTokens(actors[index])
                } else if (actors[index].processFinishTime == 0) {
                    // here our processing state is finish and we would create output tokens
                    // The difference from above conditions is we don't have enough input to fire again
                    // so we would make processFinishTime -1 to wait if again token come

                    for (vectors in actor.outConnectionsToken) {
                        fillInputs(actors[vectors.first], index)
                    }

                    // if the last actor was fired print a message
                    if (index + 1 == actors.size) {
                        // when first time a token is out then it is our Latency
                        if (firstTimeToken == null)
                            firstTimeToken = time
                        writeToFile("$time ns: output came")
                        outputTimes.add(time)
                    }
                    actors[index].processFinishTime = -1
                }
            }
            time++
        }
        writeToFile("-------------------------------------")
        writeToFile("Compute With method One:\n")
        var throughput = computeExactThroughput(actors)
        val latency = computeExactLatency(actors)
        writeToFile("-------------------------------------")
        writeToFile("Compute with method two (using time steps shown at the top)\n")

        // if any tokens was in graph the variable firsTimeToken ( Latency variable ) is obsolete
        // because the the first time a token out is seen is not our input token
        // else if there was no token in the graph
        // so the first token out is our latency ( out token is our input token )
        val graphTokens = checkVectorTokens(actors)
        if (graphTokens != 0)
            writeToFile("Latency: $latency")
        else
            writeToFile("Latency: $firstTimeToken")

        if (outputTimes.size > 1)
            throughput = outputTimes[outputTimes.size - 1] - outputTimes[outputTimes.size - 2]
        writeToFile("Throughput: $throughput")
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
        println(text.toString())
        output.appendText(text.toString() + "\n")
    }
}