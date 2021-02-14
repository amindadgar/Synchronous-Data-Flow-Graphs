import java.io.File
import kotlin.collections.ArrayList

class Compute {

    init {
        // free the output File
        File("output.txt").writeText("")
    }

    /** we have implemented two type of functions for calculation
     *  the first functions, computeLatency and computeThroughput are for calculations with log and time steps
     *  the seconds, computeLatency2 and computeThroughput2 are using another method to calculate the request
     */
    fun computeLatency(actors: ArrayList<Actor>) {
        val startArray = arrayListOf<Int>(0)

        var index: Int
        var latency = 0

        while (startArray.isNotEmpty()) {

            // get the last actor from the list and calculate the line
            index = startArray.last()
            startArray.removeAt(startArray.size - 1)

            if (checkInput(actors[index])) {
                // fire the actor
                writeToFile("Actor number $index is Fired!")

                latency += actors[index].latency!!

                consumeTokens(actors[index])

                actors[index].outConnectionsToken.forEach { data ->
                    // this condition is because of loops
                    // if the index was bigger than the actor (data.first), it's a loop (SO don't add it)
                    if (data.first > index)
                        startArray.add(data.first)

                    writeToFile("${actors[index].outputRate!!} Tokens produces for actor ${data.first}")
                    // fill the next actor inputs
                    fillInputs(actors[data.first], index)

                }

            } else {
                writeToFile("Actor number $index be cannot Fired!")
            }
        }
        if (latency != 0)
            writeToFile("Latency: $latency")
        else {
            writeToFile("Latency cannot be calculated!")
            writeToFile("Because this structure does not support this kind of SDF")
            writeToFile("Note: The first actor MUST be fired at time 0")
        }
    }

    fun computeLatency2(actors: ArrayList<Actor>) {
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
        if (loop)
            writeToFile("Total latency (Having a loop): ${totalLatency / totalLoopTokens}")
        else
            writeToFile("Total latency: $totalLatency")
    }

    fun computeThroughput2(actors: ArrayList<Actor>) {
        val totalLatency = arrayListOf<Int>(0)

        val totalLoopTokens = arrayListOf<Pair<Int, Int>>()
        // sum every actor's latency before and after token
        var index = 0
        actors.forEachIndexed { i, actor ->
            totalLatency[index] += actor.latency!!
            var haveToken = false
            for (conn in actor.outConnectionsToken) {
                if (conn.second > 0) {
                    haveToken = true
                    writeToFile("$i token found")
                }
            }
            // if we had token, get the token and save it
            if (haveToken) {
                totalLatency.add(++index, 0)
            }
        }

        var biggerLatency = 0
        totalLatency.forEach {
            writeToFile(it)
            if (biggerLatency < it)
                biggerLatency = it
        }
        writeToFile("Throughput: $biggerLatency")


    }


    // Check the input tokens
    // Weather the actor can fire or not
    private fun checkInput(actor: Actor): Boolean {
        var boolean: Boolean = true
        for (tokens in actor.inputConnectionsToken) {
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
    fun computeThroughput(actors: ArrayList<Actor>, steps: Int) {
        var time = 0
        // the second copy is for changing the actors list states (tokens) after a clock (Or a time step)


        while (time <= steps) {
            // deep copy the actors
            val actorsC2 = actors.map { actor ->
                val outC = actor.outConnectionsToken.map { it.copy() }
                val inC = actor.inputConnectionsToken.map { it.copy() }
                actor.copy(
                    actor.inputRate, actor.outputRate, actor.latency,
                    outC as ArrayList<Pair<Int, Int>>
                    , inC as ArrayList<Pair<Int, Int>>
                )
            }

            var biggestTime: Int = 0
            // check all the actors that can be fired and fire them
            actorsC2.forEachIndexed { index, actor ->
                val c = checkInput(actor)
                if (c) {
                    consumeTokens(actors[index])
                    writeToFile("Actor ${index + 1} is fired!")

                    // fill the inputs of the next actors
                    for (vectors in actor.outConnectionsToken) {
                        fillInputs(actors[vectors.first], index)
                    }
                    // Get the biggest time of the fired actors
                    if (biggestTime < actor.latency!!)
                        biggestTime = actor.latency!!


                    writeToFile("Actor ${index + 1} Fired, at time: ${actor.latency!! + time}")


                    // if the last actor was fired print a message
                    if (index + 1 == actors.size) {
                        writeToFile("output came at clock ${actor.latency!! + time}")

                    }
                } else {
                    writeToFile("Actor ${index + 1} cannot be fired!")
                }
            }

            // if any of actors was fired, our time would be added to the biggest time of the fired actor
            // else if no actors was fired just increase the time with one
            if (biggestTime != 0)
                time += biggestTime
        }
    }

    private fun writeToFile(text: Any) {
        val output = File("output.txt")
        println(text.toString())
        output.appendText(text.toString() + "\n")


    }
}