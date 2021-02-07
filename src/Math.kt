
class Math {
    fun factorial(n: Int):Int{
        return if (n >= 2)
            n*factorial(n-1)
        else
            1
    }
}