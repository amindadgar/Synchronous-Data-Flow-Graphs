/**
 * This is the actor class
 * Note: we would split the graph into actors with this values below
 *
 * @param inputRate is the firing rate of input
 * @param outputRate is the firing rate of output
 * @param latency is the actor latency ( or the time to process the token)
 * @param outConnections is showing the connections or lines to the out actors
 *      Note: out connection is an array of char ( ex: our actor is connected to B and C actors )
 * @param inputConnectionsToken is showing every vectors token, ex: (1,0) means
 * on the vector from actor one we have 0 tokens
 *
 */
data class Actor(
    var inputRate: Int?
    , var outputRate: Int? = null
    , var latency: Int? = null
    , var outConnectionsToken:  ArrayList<Pair<Int,Int>> = arrayListOf()
    , var inputConnectionsToken: ArrayList<Pair<Int,Int>> = arrayListOf()
//    , var outputTokens:  ArrayList<Int> = arrayListOf()
)