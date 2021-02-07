fun main() {

    val initialize = ReadFromFile()
    // get the actors
    val actors: ArrayList<Actor> = initialize.initializeActors()
    compute(actors)


}

fun compute(actors: ArrayList<Actor>) {
    val startArray = arrayListOf<Int>(0)

    // Apply this to start the SDF work
    actors[0].inputTokens.add(actors[0].inputRate!!)
    var index: Int
    var latency = 0

    var throughput:Int? =null

    while (startArray.isNotEmpty()) {

        // get the last actor from the list and calculate the line
        index = startArray.last()
        startArray.removeAt(startArray.size - 1)



        if (checkInput(actors[index])) {
            // fire the actor

            latency += actors[index].latency!!
            println(actors[index].latency!!)

            consumeTokens(actors[index])
            startArray.addAll(actors[index].outConnections)

            for (i in actors[index].outConnections){
                // put the input tokens on the actors
                // TODO: Check this to fix the input problem
                actors[i].inputTokens.add(index, actors[index].outputRate!! )
            }

            // If we reach the end actor
            if (actors[index].outConnections.isNullOrEmpty()) {
                // throughput is calculated when
                // TODO : Wrong throughput calculation method
                if (throughput == null) {
                    throughput = latency
//                    println("Throughput: $throughput")
                }
            }


        } else {
            println("The Graph is unstable!")
        }
    }
    println("Latency: $latency")
}

// Check the input tokens
// Weather the actor can fire or not
fun checkInput(actor: Actor):Boolean{
    var boolean:Boolean = true
    for (tokens in actor.inputTokens){
        if (tokens < actor.inputRate!!)
            boolean = false
    }
    return boolean
}

// Consume the tokens in graph
fun consumeTokens(actor: Actor):Boolean{
    var i = 0
    while (i < actor.inputTokens.size){
        actor.inputTokens[i] -= actor.inputRate!!
        i++
    }
    return true
}