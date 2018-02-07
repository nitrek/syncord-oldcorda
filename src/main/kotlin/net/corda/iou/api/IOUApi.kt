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
import net.corda.iou.state.IOUState
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
        return services.vaultAndUpdates().justSnapshot.filter { it.state.data is IOUState }
    }

    @GET
    @Path("kyc")
    fun updateKYC(@QueryParam(value = "id") id: String,
                  @QueryParam(value = "kycstatus") kycstatus: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
          val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IOUTransferFlow.Initiator::class.java, linearId,kycstatus)
            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to "KYC updated for id $id."
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }

    @GET
    @Path("issue-iou")
    fun issueIOU(
                 @QueryParam(value = "fundId") fundId: String,
                 @QueryParam(value = "txType") txType: String,
                 @QueryParam(value = "txId") txId: Int,
                 @QueryParam(value = "transactionAmount") transactionAmount: Int
               ): Response {

        val tAgent1= "CN=TA,O=NodeA";
        val fManager1 = "CN=FM,O=NodeB";
        val tAgent = services.partyFromName(tAgent1) ?: throw IllegalArgumentException("Unknown party name.")
        val fManager = services.partyFromName(fManager1) ?: throw IllegalArgumentException("Unknown party name.")
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val txnId = 1009;
        val investorId = "UH00001";
        val nav=  0.0f;
        val units= 0.0f;
        val kycValid= "";
        val txStat= "PEND"
        val ccy= "GBP" ;//Need to pull this based on fund ID
        val amtPaid= 0.0f;

        val investor1 = myLegalName;

        val investor = services.partyFromX500Name(investor1) ?: throw IllegalArgumentException("Unknown party name.")
        val me = services.nodeIdentity().legalIdentity
        val state = IOUState(fundId,txType,transactionAmount,tAgent,fManager,txnId,investorId,nav,units,kycValid,txStat,ccy,amtPaid,investor,formatted)
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IOUIssueFlow.Initiator::class.java, state, tAgent)
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


    // Redumtion API

    @GET
    @Path("redumption-iou")
    fun issueIOU(
            @QueryParam(value = "fundId") fundId: String,
            @QueryParam(value = "Unit") units: Float

    ): Response {

        val tAgent1= "CN=TA,O=NodeA";
        val fManager1 = "CN=FM,O=NodeB";
        val tAgent = services.partyFromName(tAgent1) ?: throw IllegalArgumentException("Unknown party name.")
        val fManager = services.partyFromName(fManager1) ?: throw IllegalArgumentException("Unknown party name.")
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
       // val txType="Redemption"
        val txType="REDUMPTION"
        val transactionAmount=0
        val txnId = 1009;
        val investorId = "UH00001";
        val nav=  0.0f;
        val kycValid= "";
        val txStat= "PEND"
        val ccy= "GBP" ;//Need to pull this based on fund ID
        val amtPaid= 0.0f;
        val investor1 = myLegalName;
        val investor = services.partyFromX500Name(investor1) ?: throw IllegalArgumentException("Unknown party name.")


        // Get party objects for myself and the counterparty.
        val me = services.nodeIdentity().legalIdentity

        // Create a new IOU state using the parameters given.
        val state = IOUState(fundId,txType,transactionAmount,tAgent,fManager,txnId,investorId,nav,units,kycValid,txStat,ccy,amtPaid,investor,formatted)
        //Function to  check for the total amount available
        var toalunits=0.0f
        fun total_holding(): Response {
            val (status, message) = try {
                val flowHandle = services.startTrackedFlowDynamic(TotalPosition::class.java)
                var totalholding=flowHandle.use { flowHandle.returnValue.getOrThrow() }
                //var Identify_fund =""
                if (fundId=="HKIV01"){

                    toalunits=totalholding.split(",")[0].toFloat()-totalholding.split(",")[4].toFloat()
                }
                if (fundId=="DBKS01"){

                    toalunits=totalholding.split(",")[1].toFloat()-totalholding.split(",")[5].toFloat()
                }
                if (fundId=="DBKS02"){

                    toalunits=totalholding.split(",")[2].toFloat()-totalholding.split(",")[6].toFloat()
                }
                if (fundId=="LUKT01"){

                    toalunits=totalholding.split(",")[3].toFloat()-totalholding.split(",")[7].toFloat()
                }


                Response.Status.CREATED to toalunits
            } catch (e: Exception) {
                Response.Status.BAD_REQUEST to e.message
            }

            return Response.status(status).entity(message).build()
        }

        //Print the input holding the needs to be checked
        // Start the IOUIssueFlow. We block and wait for the flow to return.
        val (status, message) = try {
            total_holding()
            val flowHandle = services.startTrackedFlowDynamic(redemption.Initiator::class.java, state, tAgent,toalunits.toFloat())
            val result = flowHandle.use { it.returnValue.getOrThrow() }
            // Return the response.
            //Response.Status.CREATED to "Trade with id ${result.id} Created Successfully"

            Response.Status.CREATED to "Your request for redeeming fund `$fundId` created successfully"

        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
           Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }

    /**
     * tranfers an IOU specified by [linearId] to a new party.

    @GET
    @Path("transfer-iou")
    fun transferIOU(@QueryParam(value = "id") id: String,
                    @QueryParam(value = "party") party: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val newLender = services.partyFromName(party) ?: throw IllegalArgumentException("Unknown party name.")

        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IOUTransferFlow.Initiator::class.java, linearId, newLender)
            // We don't care about the signed tx returned by the flow, only that it finishes successfully
            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to "IOU $id transferred to $party."
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }
*/
    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.*/

    @GET
    @Path("settle-iou")
    fun settleIOU(@QueryParam(value = "id") id: String,
                  @QueryParam(value = "amount") amount: Float): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val settleAmount = amount
        //var f="1,2,3,4";
        System.out.print(settleAmount)
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(IOUSettleFlow.Initiator::class.java, linearId, settleAmount)
            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to "$amount paid off on IOU id $id."
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }

    /**
     * Helper end-point to issue some cash to ourselves.
*/
    @GET
    @Path("amountpaid")
    fun amountpaid(@QueryParam(value = "id") id: String,
            @QueryParam(value = "amount") amount: Float): Response {
        val issueAmount = amount
        val linearId = UniqueIdentifier.fromString(id)
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(AmountPaid.Initiator::class.java, linearId, issueAmount)
            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to "$amount paid off on IOU id $id."
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }

    @GET
    @Path("total_holding")
    fun total_holding(): Response {
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(TotalPosition::class.java)
            var totalholding=flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to totalholding
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }

        return Response.status(status).entity(message).build()
    }

    @GET
    @Path("register_book")
    fun register_book(): Response {
        val (status, message) = try {
            val flowHandle = services.startTrackedFlowDynamic(Register::class.java)
            var totalholding=flowHandle.use { flowHandle.returnValue.getOrThrow() }
            Response.Status.CREATED to totalholding
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
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