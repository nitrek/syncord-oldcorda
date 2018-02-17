package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TransactionType
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.linearHeadsOfType
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.flows.CollectSignaturesFlow
import net.corda.flows.FinalityFlow
import net.corda.flows.SignTransactionFlow
import net.corda.iou.contract.IOUContract
import net.corda.iou.state.IOUState
import net.corda.iou.state.IOUState_NAV
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

//

//For alloting of transaction

object UpdateNavFlow {
    @StartableByRPC
    @InitiatingFlow
    class Initiator(val fundId: String,val navValue:String) : FlowLogic<String>() {

        override val progressTracker: ProgressTracker = Initiator.tracker()

        companion object {
            object PREPARATION : ProgressTracker.Step("Obtaining IOU from vault")
            object BUILDING : ProgressTracker.Step("Building and verifying transaction.")
            object SIGNING : ProgressTracker.Step("signing transaction.")
            object COLLECTING : ProgressTracker.Step("Collecting counterparty signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING : ProgressTracker.Step("Finalising transaction") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(PREPARATION, BUILDING, SIGNING, COLLECTING, FINALISING)
        }

        @Suspendable
        override fun call(): String {
            val me = serviceHub.myInfo.legalIdentity

            // Step 1. Retrieve the IOU state from the vault.
            progressTracker.currentStep = PREPARATION
            val iouStates = serviceHub.vaultService.linearHeadsOfType<IOUState>()
            //iouStates.filter {  }
            val fundIdParsed = fundId.split(",")
            val navValueParsed = navValue.split(",")

            for (i in iouStates) {

                val navStates = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")
                val navData = navStates.state.data
                val txStatus = navData.txnStatus
                //APPROVED (for tx kyc is done)
                if (txStatus == "APPROVED") {

                val counterparty = navStates.state.data.lender
                progressTracker.currentStep = BUILDING
                val notary = navStates.state.notary
                val builder = TransactionType.General.Builder(notary)
                val settleCommand = Command(IOUContract.Commands.Settle(), listOf(counterparty.owningKey, me.owningKey))
                builder.addCommand(settleCommand)
                builder.addInputState(navStates)
                
            
                val index = fundIdParsed.indexOf(navData.fundId)

                // Step 7. Only add an output IOU state of the IOU has not been fully settled
                val updatednavData: IOUState = navData.updateNav(navValueParsed.get(index).toFloat())
                val units: Float = updatednavData.transactionAmount / navValueParsed.get(index).toFloat();
                val unitsState: IOUState = updatednavData.updateUnits(units)
                val finalState: IOUState = unitsState.updateTransactionStatus("ALLOTED")
                //System.out.print(kycStatus.toString()+" hjjkh");
                builder.addOutputState(finalState)


                

                // Step 8. Verify and sign the transaction.
                builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
                progressTracker.currentStep = SIGNING
                val ptx = serviceHub.signInitialTransaction(builder)

                // Step 9. Get counterparty signature.
                progressTracker.currentStep = COLLECTING
                val stx = subFlow(CollectSignaturesFlow(ptx, COLLECTING.childProgressTracker()))
                //stxG = stx
                // Step 10. Finalize the transaction.
                progressTracker.currentStep = FINALISING

                subFlow(FinalityFlow(stx, FINALISING.childProgressTracker())).single()
                }
                }
                return "done";
            }
        }

    @InitiatedBy(UpdateNavFlow.Initiator::class)
    class Responder(val otherParty: Party) : FlowLogic<String>() {
        @Suspendable
        override fun call(): String {
            val flow = object : SignTransactionFlow(otherParty) {
                @Suspendable
                override fun checkTransaction(stx: SignedTransaction) {
                    // TODO: Add some checking.
                }
            }

            val stx = subFlow(flow)
            waitForLedgerCommit(stx.id)
            return "done"
        }
    }
}
