import java.io.File

class ReadFromFile {


    fun initializeActors(): ArrayList<Actor> {
        val file = File("src/resources/input.txt")
        val actors = ArrayList<Actor>()

        var actorsRelationShips: Int? = null
        var actorCount: Int = -1
        file.readLines().forEachIndexed { index, s ->
            when (index) {
                // the first line is to get the actors count
                0 -> {
                    // get the actors count
                    actorCount = removeString(s)
                    // calculate total lines for actors relationships
                    actorsRelationShips = calculateActorRelationShip(actorCount)
                }
                // get the input rates
                // the input rates would start from actorCount+1
                in 1..actorCount -> {
                    // get the input rates
                    val inputRate = removeString(s)
                    actors.add(Actor(inputRate))
                }
                // get the output rates
                // the output rates would start from actorCount+1
                in actorCount + 1..actorCount * 2 -> {

                    val actorIndex = index - (actorsRelationShips!! + 1)
                    val outputRate = removeString(s)
                    actors[actorIndex].outputRate = outputRate

                }
                // get the latencies
                // the latencies would start from actorCount*2+1
                in (actorCount * 2 + 1)..(actorCount * 2 + actorCount) -> {
                    val actorIndex = index - (actorCount * 2 + 1)
                    actors[actorIndex].latency = removeString(s)
                }
                // else is adding the relationships
                else -> {
                    // we would read the actors minus one
                    // because the first actor number is zero !
                    val firstActor: Int = Character.getNumericValue(s[0]) - 1
                    val secondActor: Int = Character.getNumericValue(s[2]) - 1


                    actors[firstActor].outConnections.add(secondActor)
                    actors[firstActor].outputTokens.add(removeString(s))
                    actors[secondActor].inputConnections.add(firstActor)
                    actors[secondActor].inputTokens.add(removeString(s))

                }
            }
        }
        return actors
    }

    private fun calculateActorRelationShip(n: Int): Int {
        val math = Math()
        val fact1 = math.factorial(n)
        val fact2 = math.factorial(n - 2)
        return fact1 / (2 * fact2)
    }

    private fun removeString(s: String): Int = s.split(':')[1].replace(" ", "").toInt()
}