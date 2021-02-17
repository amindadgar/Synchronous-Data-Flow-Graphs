fun main() {

    val initialize = ReadFromFile()
    // get the actors
    // 3 means input3.txt file
    val actors: ArrayList<Actor> = initialize.initializeActors(2)
    val compute = Compute()
    print("Enter number of time steps (clock): ")
    val steps = readLine()
    compute.computeAll(actors,steps!!.toInt())




}

