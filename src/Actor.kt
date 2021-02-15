/**
 * This is the actor class
 * Note: we would split the graph into actors with this values below
 *
 * @param inputRate is the firing rate of input
 * @param outputRate is the firing rate of output
 * @param latency is the actor latency ( or the time to process the token)
 * @param inputConnectionsToken is showing every vectors token, ex: (1,0) means
 * on the vector from actor one we have 0 tokens
 * @param outConnectionsToken is similar to inputConnectionsToken, except for outputs
 * @param processFinishTime is the time that the process will be finished,
 *          it is zero at the instantiating time because the actor is not processing
 */
data class Actor(
    var inputRate: Int?
    , var outputRate: Int? = null
    , var latency: Int? = null
    , var outConnectionsToken:  ArrayList<Pair<Int,Int>> = arrayListOf()
    , var inputConnectionsToken: ArrayList<Pair<Int,Int>> = arrayListOf()
    , var processFinishTime: Int = -1
)