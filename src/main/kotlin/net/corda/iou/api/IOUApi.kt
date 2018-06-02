package net.corda.iou.api

import net.corda.client.rpc.notUsed
import net.corda.contracts.asset.Cash
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.getOrThrow
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.random63BitValue
import net.corda.core.serialization.makeNoWhitelistClassResolver
import net.corda.iou.flow.*
import net.corda.iou.state.Order
import net.corda.iou.state.Order_NAV
import net.corda.iou.state.Wallet
import org.apache.commons.math.random.RandomData
import org.bouncycastle.asn1.x500.X500Name
import org.jetbrains.exposed.sql.Date
import org.jetbrains.exposed.sql.dateParam
import rx.Observable
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val SERVICE_NODE_NAMES = listOf(
        X500Name("CN=Controller,O=R3,L=London,C=UK"),
        X500Name("CN=NetworkMapService,O=R3,L=London,C=UK"))

/**
 * This API is accessible from /api/iou. The endpoint paths specified below are relative to it.
 * We've defined a bunch of endpoints to deal with IOUs, cash and the various operations you can perform with them.
 */
@Path("iou")
class IOUApi(val services: CordaRPCOps) {
    private val myLegalName = services.nodeIdentity().legalIdentity.name

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<X500Name>> {
        val (nodeInfo, nodeUpdates) = services.networkMapUpdates()
        nodeUpdates.notUsed()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentity.name }
                .filter { it != myLegalName && it !in SERVICE_NODE_NAMES })
    }

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GET
    @Path("ious")
    @Produces(MediaType.APPLICATION_JSON)
    // Filter by state type: IOU.
    fun getIOUs(): List<StateAndRef<ContractState>> {
        return services.vaultAndUpdates().justSnapshot.filter { it.state.data is Order }
    }
    @GET
    @Path("getWallet")
    @Produces(MediaType.APPLICATION_JSON)
            // Filter by state type: IOU.
    fun getWallet(): List<StateAndRef<ContractState>> {
        return services.vaultAndUpdates().justSnapshot.filter { it.state.data is Wallet }
    }

    @GET
    @Path("issueWallet")
    fun issueWallet(
            @QueryParam(value = "availableBalance") availableBalance: String
    ): Response {

        val tAgent1= "CN=TA,O=NodeA";
        val tAgent = services.partyFromName(tAgent1) ?: throw IllegalArgumentException("Unknown party name.")
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val ccy= "GBP" ;//Need to pull this based on fund ID
        val investor1 = myLegalName;
        val investor = services.partyFromX500Name(investor1) ?: throw IllegalArgumentException("Unknown party name.")
        val me = services.nodeIdentity().legalIdentity
        val state = Wallet(tAgent,investor,availableBalance.toFloat(),0.0f,formatted,ccy)
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IssueWalletFlow.Initiator::class.java, state, tAgent)
            val result = flowHandle.use { it.returnValue.getOrThrow() }
            // Return the response.
            Response.Status.CREATED to "Wallet issues to  ${investor1} with balance ${availableBalance}"
        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
            var message ="some error"
            if(e.message!=null)
                message = e.message.toString()
            Response.Status.CREATED to message.substring(56)
        }

        return Response.status(status).entity(message).build()
    }
    @GET
    @Path("issue-iou")
    fun issueObligation(@QueryParam(value = "issueSize") issueSize: Int,
                        @QueryParam(value = "party") party: String,
                        @QueryParam(value = "issueName") issueName: String): Response {
        val observer = "CN=Observer,O=NodeB";
        val observerNode = services.partyFromName(fManager1) ?: throw IllegalArgumentException("Unknown party name.")
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val issuestatus = "DRAFT"
        val currency = "USD"
        //val issueAmount = Amount(issueSize.toLong() * 100, Currency.getInstance(currency))
        val coBanker = party;
/*val issueSize: Int,
                      val leadBanker: Party,
                      val coBanker: Party,
                      val observer: Party,
                      val issueName:String,
                      val status:String,
                      val transactiondate:String, */
        val coBankerNode = services.partyFromX500Name(coBanker) ?: throw IllegalArgumentException("Unknown party name.")
        val me = services.nodeIdentity().legalIdentity
        val state = Issue(me,coBankerNode,observerNode,issueName,issuestatus,formatted)
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IOUIssueFlow.Initiator::class.java, state, tAgent,ccy)
            val result = flowHandle.use { it.returnValue.getOrThrow() }
            // Return the response.
            Response.Status.CREATED to "Trade with id ${result.id} Created Successfully"
        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
            var message ="some error"
            if(e.message!=null)
                message = e.message.toString()
            Response.Status.CREATED to message.substring(56)
        }

        return Response.status(status).entity(message).build()
    }

    // Helper method to get just the snapshot portion of an RPC call which also returns an Observable of updates. It's
    // important to unsubscribe from this Observable if we're not going to use it as otherwise we leak resources on the server.
    private val <A> Pair<A, Observable<*>>.justSnapshot: A get() {
        second.notUsed()
        return first
    }


}