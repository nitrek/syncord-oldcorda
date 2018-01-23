package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.linearHeadsOfType
import net.corda.core.utilities.ProgressTracker
import net.corda.iou.state.IOUState
import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction

/**
 * This is the flow which opens all the transactions from the vault to read the vote.
 * Does a sum total and displays the final voting result.
 */


@StartableByRPC
@InitiatingFlow
class TotalPosition(val state: IOUState) : FlowLogic<Map<String,Int>>() {

    override val progressTracker: ProgressTracker = TotalPosition.tracker()

    companion object {
        object FETCH : ProgressTracker.Step("Obtaining Transactions from vault")
        object READ : ProgressTracker.Step("Building and verifying transaction.")


        fun tracker() = ProgressTracker(FETCH, READ)
    }

    @Suspendable
    override fun call(): Map<String, Int> {


        // Stage 1. Retrieve all IOU's from the vault.
        progressTracker.currentStep = TotalPosition.Companion.FETCH
        val iouStates = serviceHub.vaultService.linearHeadsOfType<IOUState>()

        //Get the total Holding for each Trade

        val toalposition = mutableMapOf<String, Int>()
        val numbers: IntArray = intArrayOf()
        val arr = intArrayOf()

        //To initialize the transactrion
        val sumHKIV01: Int
        val sumDBKS01: Int
        val sumDBKS02: Int
        val sumLUKT01: Int
        progressTracker.currentStep = TotalPosition.Companion.READ
        for (i in iouStates) {
            val iouststeref = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")
            val fundid = iouststeref.state.data.fundId.trim()
            val transactionamount = iouststeref.state.data.transactionAmount
            val investorname = state.participants.map { it.owningKey }

            //To get the sum
            /// val numbers: IntArray = intArrayOf()
            if (fundid=="HKIV01"){

                val HKIV01 = intArrayOf(transactionamount)

            }

            if (fundid=="DBKS01"){

                val DBKS01 = intArrayOf(transactionamount)
            }
            if (fundid=="DBKS02"){

                val DBKS02 = intArrayOf(transactionamount)

            }
            if (fundid=="LUKT01"){

                val LUKT01 = intArrayOf(transactionamount)

            }


        }

/*        //Question::Option, Count
      val votes = mutableMapOf<String, Int>()
        progressTracker.currentStep = CountFlow.Companion.READ
        for (iouState in iouStates) {
            val iouStateAndRef = iouStates[iouState.key] ?: throw IllegalArgumentException("Could not map IOU's")
           val option = iouStateAndRef.state.data.options.trim()
            val question = iouStateAndRef.state.data.question.trim()
            if(!option.contains(',')) {
                if (votes[question + "::" + option] == null) {
                    votes.put(question + "::" + option, 1)  //Initialaize the map if the optionis coming for the first time
                } else {
                    val prevCount: Int = votes[question + "::" + option] ?: throw IllegalArgumentException("Illegal vote exception in CountFlow.kt Line:62")
                    votes.put(question + "::" + option, prevCount + 1)
                }
                val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity
            }
        }
        return votes*/
        return  toalposition
    }
}