class Compute {

    fun computeLatency(actors: ArrayList<Actor>) {
        val startArray = arrayListOf<Int>(0)

        var index: Int
        var latency = 0

        var throughput: Int? = null

        while (startArray.isNotEmpty()) {

            // get the last actor from the list and calculate the line
            index = startArray.last()
            startArray.removeAt(startArray.size - 1)

            if (checkInput(actors[index])) {
                // fire the actor
                println("Actor number $index is Fired!")

                latency += actors[index].latency!!

                consumeTokens(actors[index])

                actors[index].outConnectionsToken.forEach { data ->
                    // this condition is because of loops
                    // if the index was bigger than the actor (data.first), it's a loop (SO don't add it)
                    if (data.first > index)
                        startArray.add(data.first)

                    println("${actors[index].outputRate!!} Tokens produces for actor ${data.first}")
                    // fill the next actor inputs
                    fillInputs(actors[data.first], index)

                }

                // TODO("Implement throughput in another function")
//                // If we reach the end actor
//                if (actors[index].outConnections.isNullOrEmpty()) {
//                    // throughput is calculated when
//                    if (throughput == null) {
//                        throughput = latency
////                    println("Throughput: $throughput")
//                    }
//                }


            } else {
                println("Actor number $index be cannot Fired!")
            }
        }
        if (latency != 0)
            println("Latency: $latency")
        else {
            println("Latency cannot be calculated!")
            println("Because this structure does not support this kind of SDF")
            println("Note: The first actor MUST be fired at time 0")
        }
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
    // in fact steps is a time slice
    fun computeThroughput(actors: ArrayList<Actor>,steps:Int){
        var time = 0
        while (time <= steps) {
            var biggestTime:Int = 0

            // check all the actors that can be fired and fire them
            actors.forEachIndexed { index, actor ->
                // TODO("Every input is firing at the same time, because of the same time checking !")
                val c = checkInput(actor)
                if (c){
                    consumeTokens(actor)


                    // fill the inputs of the next actors
                    for (vectors in actor.outConnectionsToken){
                        fillInputs(actors[vectors.first], index)
                    }
                    println("  Actor $index fired at the time: $time ")
                    // Get the biggest time of the fired actors
                    if (biggestTime < actor.latency!!)
                        biggestTime = actor.latency!!


                    // if the last actor was fired print a message
//                    println("actor $index was fired at the time of $time")
                    if ( index+1 == actors.size )
                        println("index : $index   output came at clock ${time + biggestTime}\n")
                }
            }

            // if any of actors was fired, our time would be added to the biggest time of the fired actor
            // else if no actors was fired just increase the time with one
            if (biggestTime != 0)
                time += biggestTime
            else
                time++
        }
    }
}