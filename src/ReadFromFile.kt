import java.io.File
import kotlin.system.exitProcess

class ReadFromFile {


    fun initializeActors(fileSelection:Int = 1): ArrayList<Actor> {
        val file = when(fileSelection){
            1 -> File("src/resources/input.txt")
            2 -> File("src/resources/input2.txt")
            3 -> File("src/resources/input3.txt")
            4 -> File("src/resources/input4.txt")
            5 -> File("src/resources/input5.txt")
            else -> File("src/resources/input.txt")
        }

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

                    val actorIndex = index - (actorCount + 1)
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
                    val string:String = s.subSequence(0,5).toString()
                    val numbers = string.replace(" ","").split(",")

                    val firstActor: Int = numbers[0].toInt() - 1
                    val secondActor: Int = numbers[1].toInt() - 1

                    actors[firstActor].outConnectionsToken.add(Pair(secondActor,removeString(s)))
                    actors[secondActor].inputConnectionsToken.add(Pair(firstActor,removeString(s)))
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