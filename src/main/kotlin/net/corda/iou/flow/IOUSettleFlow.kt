package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
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
import net.corda.iou.state.Wallet
import java.util.*
//

//For alloting of transaction

object IOUSettleFlow {
    @StartableByRPC
    @InitiatingFlow
    class Initiator(val linearId: UniqueIdentifier,val navValue:Float) : FlowLogic<SignedTransaction>() {

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
        override fun call(): SignedTransaction {
            val me = serviceHub.myInfo.legalIdentity

            // Step 1. Retrieve the IOU state from the vault.
            progressTracker.currentStep = PREPARATION
            val iouStates = serviceHub.vaultService.linearHeadsOfType<IOUState>()
            val iouToSettle = iouStates[linearId] ?: throw IllegalArgumentException("IOUState with linearId $linearId not found.")
            val counterparty = iouToSettle.state.data.lender
            //Step 2. Check the party running this flow is the borrower.
            require(iouToSettle.state.data.borrower == me) { "NAV can be added by Transfer Agent" }
            // Step 3. Create a transaction builder.
            progressTracker.currentStep = BUILDING
            val notary = iouToSettle.state.notary
            val builder = TransactionType.General.Builder(notary)


            val settleCommand = Command(IOUContract.Commands.Settle(), listOf(counterparty.owningKey, me.owningKey))

            builder.addCommand(settleCommand)
            builder.addInputState(iouToSettle)
            val type = iouToSettle.state.data.txType


            if (type =="SUBSCRIPTION"){



                // Step 7. Only add an output IOU state of the IOU has not been fully settled
                val votedIOU: IOUState = iouToSettle.state.data.updateNav(navValue)

                val units:Float = iouToSettle.state.data.transactionAmount / navValue;
                val unitsState:IOUState = votedIOU.updateUnits(units)
                val updateAmount:IOUState = votedIOU.updateAmount(unitsState.transactionAmount.toFloat())


                val finalState:IOUState = updateAmount.updateTransactionStatus("SETTLED")


                //System.out.print(kycStatus.toString()+" hjjkh");
                builder.addOutputState(finalState)

                //to update the wallet for the particular investor

                val wallets = serviceHub.vaultService.linearHeadsOfType<Wallet>()
                for (i in wallets) {
                    val wallet = wallets[i.key] ?: throw IllegalArgumentException("Could not map Wallet")
                    val walletData = wallet.state.data
                    val wCurrency = walletData.currency
                    /*require(walletData.availableBalance > state.transactionAmount) { "Insufficient Balance in Wallet" }*/
                    val currency = "GBP"
                    if (currency == wCurrency) {

                        val counterparty = wallet.state.data.cashCCP
                        progressTracker.currentStep = BUILDING
                        val notary1 = wallet.state.notary
                        val builder1 = TransactionType.General.Builder(notary1)
                        val me = serviceHub.myInfo.legalIdentity
                        val settleCommand = Command(IOUContract.Commands.Settle(),me.owningKey)
                        builder1.addCommand(settleCommand)
                        builder1.addInputState(wallet)
                        val finalState2:Wallet =  wallet.state.data.removeblockMoney(finalState.transactionAmount.toFloat());

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
//
                    }}


            }

            else {
                val votedIOU: IOUState = iouToSettle.state.data.updateNav(navValue)

                val transactionamount = iouToSettle.state.data.units * navValue
                val xyz :IOUState = votedIOU.transactionamountupdate(transactionamount.toInt())
                val finalState:IOUState = xyz.updateTransactionStatus("ALLOTED")
                builder.addOutputState(finalState)
            }

            // Step 8. Verify and sign the transaction.
            builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
            progressTracker.currentStep = SIGNING
            val ptx = serviceHub.signInitialTransaction(builder)

            // Step 9. Get counterparty signature.
            progressTracker.currentStep = COLLECTING
            val stx = subFlow(CollectSignaturesFlow(ptx, COLLECTING.childProgressTracker()))

            // Step 10. Finalize the transaction.
            progressTracker.currentStep = FINALISING
            return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker())).single()
        }
    }

    @InitiatedBy(IOUSettleFlow.Initiator::class)
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
