fun main() {

    val initialize = ReadFromFile()
    // get the actors
    // 3 means input3.txt file
    val actors: ArrayList<Actor> = initialize.initializeActors(5)
    val compute = Compute()

    compute.computeAll(actors,500000)




}

