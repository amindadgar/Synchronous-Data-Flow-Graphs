fun main() {

    val initialize = ReadFromFile()
    // get the actors
    // 3 means input3.txt file
    val actors: ArrayList<Actor> = initialize.initializeActors(3)
    val compute = Compute()

    compute.computeThroughput(actors,25)



}

