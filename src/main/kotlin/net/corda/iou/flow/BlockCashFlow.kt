//package net.corda.iou.flow
//
//import co.paralleluniverse.fibers.Suspendable
//import net.corda.core.contracts.Command
//import net.corda.core.contracts.TransactionType
//import net.corda.core.flows.FlowLogic
//import net.corda.core.flows.InitiatedBy
//import net.corda.core.flows.InitiatingFlow
//import net.corda.core.flows.StartableByRPC
//import net.corda.core.identity.Party
//import net.corda.core.node.services.linearHeadsOfType
//import net.corda.core.transactions.SignedTransaction
//import net.corda.core.utilities.ProgressTracker
//import net.corda.flows.CollectSignaturesFlow
//import net.corda.flows.FinalityFlow
//import net.corda.flows.SignTransactionFlow
//import net.corda.iou.contract.IOUContract
//import net.corda.iou.state.IOUState
//import net.corda.iou.state.Wallet
//import java.util.*
//
//object BlockCashFlow {
//    @StartableByRPC
//    @InitiatingFlow
//    class Initiator(val cash: String,val currency:String) : FlowLogic<String>() {
//
//        override val progressTracker: ProgressTracker = Initiator.tracker()
//
//        companion object {
//            object PREPARATION : ProgressTracker.Step("Obtaining IOU from vault")
//            object BUILDING : ProgressTracker.Step("Building and verifying transaction.")
//            object SIGNING : ProgressTracker.Step("signing transaction.")
//            object COLLECTING : ProgressTracker.Step("Collecting counterparty signature.") {
//                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
//            }
//
//            object FINALISING : ProgressTracker.Step("Finalising transaction") {
//                override fun childProgressTracker() = FinalityFlow.tracker()
//            }
//
//            fun tracker() = ProgressTracker(PREPARATION, BUILDING, SIGNING, COLLECTING, FINALISING)
//        }
//
//        @Suspendable
//        override fun call(): String {
//            val me = serviceHub.myInfo.legalIdentity
//
//            // Step 1. Retrieve the IOU state from the vault.
//            progressTracker.currentStep = PREPARATION
//            val wallets = serviceHub.vaultService.linearHeadsOfType<Wallet>()
//            for (i in wallets) {
//
//                val wallet = wallets[i.key] ?: throw IllegalArgumentException("Could not map Wallet")
//                val walletData = wallet.state.data
//                val wCurrency = walletData.currency
//
//                if (currency == wCurrency) {
//
//                    val counterparty = wallet.state.data.cashCCP
//                    progressTracker.currentStep = BUILDING
//                    val notary = wallet.state.notary
//                    val builder = TransactionType.General.Builder(notary)
//                    val settleCommand = Command(IOUContract.Commands.Settle(), listOf(counterparty.owningKey, me.owningKey))
//                    builder.addCommand(settleCommand)
//                    builder.addInputState(wallet)
//
//                    //System.out.print(kycStatus.toString()+" hjjkh");
//                    builder.addOutputState(finalState)
//                    // Step 8. Verify and sign the transaction.
//                    builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
//                    progressTracker.currentStep = SIGNING
//                    val ptx = serviceHub.signInitialTransaction(builder)
//
//                    // Step 9. Get counterparty signature.
//                    progressTracker.currentStep = COLLECTING
//                    val stx = subFlow(CollectSignaturesFlow(ptx, COLLECTING.childProgressTracker()))
//                    //stxG = stx
//                    // Step 10. Finalize the transaction.
//                    progressTracker.currentStep = FINALISING
//
//                    subFlow(FinalityFlow(stx, FINALISING.childProgressTracker())).single()
//
//                    return "done";
//                }
//            }
//            return "Wallet No found"
//        }
//
//    @InitiatedBy(BlockCashFlow.Initiator::class)
//    class Responder(val otherParty: Party) : FlowLogic<String>() {
//        @Suspendable
//        override fun call(): String {
//            val flow = object : SignTransactionFlow(otherParty) {
//                @Suspendable
//                override fun checkTransaction(stx: SignedTransaction) {
//                    // TODO: Add some checking.
//                }
//            }
//
//            val stx = subFlow(flow)
//            waitForLedgerCommit(stx.id)
//            return "done"
//        }
//    }
//}