package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionType
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
import net.corda.iou.state.Wallet
import java.util.*

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
object IOUIssueFlow {

    @InitiatingFlow
    @StartableByRPC

    //Rohan:-Class is defoned as  class <class name> (<Variable you want to declare>):<Type if you want any specific flow>
    class Initiator(val state: IOUState, val otherParty: Party,val currency: String) : FlowLogic<SignedTransaction>() {


        //Rohan:-Object declaration inside a class i
        companion object {
            object BUILDING : ProgressTracker.Step("Building and verifying transaction.")
            object SIGNING : ProgressTracker.Step("signing transaction.")
            object COLLECTING : ProgressTracker.Step("Collecting counterparty signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING : ProgressTracker.Step("Finalising transaction") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(BUILDING, SIGNING, COLLECTING, FINALISING)
        }

        override val progressTracker: ProgressTracker = Initiator.tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = BUILDING
            // Step 1. Get a reference to the notary service on our network and our key pair.


            val wallets = serviceHub.vaultService.linearHeadsOfType<Wallet>()
            for (i in wallets) {
                 val wallet = wallets[i.key] ?: throw IllegalArgumentException("Could not map Wallet")
                val walletData = wallet.state.data
                val wCurrency = walletData.currency
                require(walletData.availableBalance > state.transactionAmount) { "Insufficient Balance in Wallet" }
                if (currency == wCurrency) {

                    val counterparty = wallet.state.data.cashCCP
                   progressTracker.currentStep = BUILDING
                   val notary1 = wallet.state.notary
                   val builder1 = TransactionType.General.Builder(notary1)
                    val me = serviceHub.myInfo.legalIdentity
                   val settleCommand = Command(IOUContract.Commands.Settle(),me.owningKey)
                   builder1.addCommand(settleCommand)
                   builder1.addInputState(wallet)
                   val finalState:Wallet =  wallet.state.data.blockMoneyAvl(state.transactionAmount.toFloat());
                    val finalState2 =  finalState.blockMoneyBlock(state.transactionAmount.toFloat());
                    //System.out.print(kycStatus.toString()+" hjjkh");
                    //
                    builder1.addOutputState(finalState2)
//                    // Step 8. Verify and sign the transaction.
                    builder1.toWireTransaction().toLedgerTransaction(serviceHub).verify()
                    progressTracker.currentStep = SIGNING
                    val ptx1 = serviceHub.signInitialTransaction(builder1)
//
//                    // Step 9. Get counterparty signature.
                    progressTracker.currentStep = COLLECTING
                    val stx1 = subFlow(CollectSignaturesFlow(ptx1, COLLECTING.childProgressTracker()))
//                    //stxG = stx
//                    // Step 10. Finalize the transaction.
                    progressTracker.currentStep = FINALISING
//
                    subFlow(FinalityFlow(stx1, FINALISING.childProgressTracker())).single()
//
//                    return "done";
                }}
                val notary2 = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity
                // Step 2. Create a new issue command.
                // Remember that a command is a CommandData object and a list of CompositeKeys
                val issueCommand = Command(IOUContract.Commands.Issue(), state.participants.map { it.owningKey })
                // Step 3. Create a new TransactionBuilder object.
                val builder = TransactionType.General.Builder(notary2)
                // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
                builder.withItems(state, issueCommand)
                // Step 5. Verify and sign it with our KeyPair.
                builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
                progressTracker.currentStep = SIGNING
                val ptx = serviceHub.signInitialTransaction(builder)

                // Step 6. Collect the other party's signature using the SignTransactionFlow.
                progressTracker.currentStep = COLLECTING
                val stx = subFlow(CollectSignaturesFlow(ptx, COLLECTING.childProgressTracker()))

                // Step 7. Assuming no exceptions, we can now finalise the transaction.
                progressTracker.currentStep = FINALISING
                return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker())).single()
            }
        }


    @InitiatingFlow
    @InitiatedBy(IOUIssueFlow.Initiator::class)
    class Responder(val otherParty: Party) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val flow = object : SignTransactionFlow(otherParty) {
                @Suspendable
                override fun checkTransaction(stx: SignedTransaction) {
                    // TODO: Add some checking.
                }
            }

            val stx = subFlow(flow)

            return waitForLedgerCommit(stx.id)
        }
    }

}