// package net.corda.iou.flow

// import co.paralleluniverse.fibers.Suspendable
// import net.corda.core.contracts.Command
// import net.corda.core.contracts.TransactionType
// import net.corda.core.flows.FlowLogic
// import net.corda.core.flows.InitiatedBy
// import net.corda.core.flows.InitiatingFlow
// import net.corda.core.flows.StartableByRPC
// import net.corda.core.identity.Party
// import net.corda.core.node.services.linearHeadsOfType
// import net.corda.core.transactions.SignedTransaction
// import net.corda.core.utilities.ProgressTracker
// import net.corda.flows.CollectSignaturesFlow
// import net.corda.flows.FinalityFlow
// import net.corda.flows.SignTransactionFlow
// import net.corda.iou.contract.IOUContract
// import net.corda.iou.state.IOUState
// import net.corda.iou.state.Wallet

// //

// //For alloting of transaction

// object UpdateNavFlow {
//     @StartableByRPC
//     @InitiatingFlow
//     class Initiator(val fundId: String,val navValue:String) : FlowLogic<String>() {

//         override val progressTracker: ProgressTracker = Initiator.tracker()

//         companion object {
//             object PREPARATION : ProgressTracker.Step("Obtaining IOU from vault")
//             object BUILDING : ProgressTracker.Step("Building and verifying transaction.")
//             object SIGNING : ProgressTracker.Step("signing transaction.")
//             object COLLECTING : ProgressTracker.Step("Collecting counterparty signature.") {
//                 override fun childProgressTracker() = CollectSignaturesFlow.tracker()
//             }

//             object FINALISING : ProgressTracker.Step("Finalising transaction") {
//                 override fun childProgressTracker() = FinalityFlow.tracker()
//             }

//             fun tracker() = ProgressTracker(PREPARATION, BUILDING, SIGNING, COLLECTING, FINALISING)
//         }

//         @Suspendable
//         override fun call(): String {
//             val me = serviceHub.myInfo.legalIdentity

//             // Step 1. Retrieve the IOU state from the vault.
//             progressTracker.currentStep = PREPARATION
//             val iouStates = serviceHub.vaultService.linearHeadsOfType<IOUState>()
//             //iouStates.filter {  }
//             val fundIdParsed = fundId.split(",")
//             val navValueParsed = navValue.split(",")

//             for (i in iouStates) {

//                 val navStates = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")
//                 val navData = navStates.state.data
//                 val txStatus = navData.txnStatus
//                 //APPROVED (for tx kyc is done)
//                 if (txStatus == "APPROVED") {

//                 val counterparty = navStates.state.data.lender
//                 progressTracker.currentStep = BUILDING
//                 val notary = navStates.state.notary
//                 val builder = TransactionType.General.Builder(notary)
//                 val settleCommand = Command(IOUContract.Commands.Settle(),  me.owningKey)
//                 builder.addCommand(settleCommand)
//                 builder.addInputState(navStates)
                
            
//                 val index = fundIdParsed.indexOf(navData.fundId)
//                  val type = navData.txType

//                 if(type == "SUBSCRIPTION")
//                 {
//                 val updatednavData: IOUState = navData.updateNav(navValueParsed.get(index).toFloat())
//                 val units: Float = updatednavData.transactionAmount / navValueParsed.get(index).toFloat();
//                 val unitsState: IOUState = updatednavData.updateUnits(units)
// /*                val finalState: IOUState = unitsState.updateTransactionStatus("ALLOTED")
//                 //System.out.print(kycStatus.toString()+" hjjkh");
//                 builder.addOutputState(finalState)*/
//                     val updateAmount:IOUState = unitsState.updateAmount(unitsState.transactionAmount.toFloat())
//                     val finalState:IOUState = updateAmount.updateTransactionStatus("SETTLED")


//                     //System.out.print(kycStatus.toString()+" hjjkh");
//                     builder.addOutputState(finalState)

//                     //to update the wallet for the particular investor

//                     val wallets = serviceHub.vaultService.linearHeadsOfType<Wallet>()
//                     for (i in wallets) {
//                         val wallet = wallets[i.key] ?: throw IllegalArgumentException("Could not map Wallet")
//                         val walletData = wallet.state.data
//                         val wCurrency = walletData.currency
//                         /*require(walletData.availableBalance > state.transactionAmount) { "Insufficient Balance in Wallet" }*/
//                         val currency = "GBP"
//                         if (currency == wCurrency && walletData.investor == finalState.lender ) {

//                             val counterparty = wallet.state.data.cashCCP
//                             val notary1 = wallet.state.notary
//                             val builder1 = TransactionType.General.Builder(notary1)
//                             val me = serviceHub.myInfo.legalIdentity
//                             val settleCommand = Command(IOUContract.Commands.Settle(),me.owningKey)
//                             builder1.addCommand(settleCommand)
//                             builder1.addInputState(wallet)
//                             val finalState2: Wallet =  wallet.state.data.removeblockMoney(finalState.transactionAmount.toFloat());

//                             //System.out.print(kycStatus.toString()+" hjjkh");
//                             //
//                             builder1.addOutputState(finalState2)
// //                    // Step 8. Verify and sign the transaction.
//                             builder1.toWireTransaction().toLedgerTransaction(serviceHub).verify()
//                             val ptx1 = serviceHub.signInitialTransaction(builder1)
// //
// //                    // Step 9. Get counterparty signature.
//                             val stx1 = subFlow(CollectSignaturesFlow(ptx1))
// //                    //stxG = stx
// //                    // Step 10. Finalize the transaction.
// //
//                             subFlow(FinalityFlow(stx1)).single()
// //
// //                    return "done";
//                         }}
//                 }
//             else{
//                  val updatednavData: IOUState = navData.updateNav(navValueParsed.get(index).toFloat())

//                 val transactionamount = navData.units * navValueParsed.get(index).toFloat();
//                 val xyz :IOUState = updatednavData.transactionamountupdate(transactionamount.toInt())
//                     val yzx :IOUState = xyz.updateAmount(transactionamount);
//                 val finalState:IOUState = yzx.updateTransactionStatus("SETTLED")
//                 builder.addOutputState(finalState)
//                     val wallets = serviceHub.vaultService.linearHeadsOfType<Wallet>()
//                     for (i in wallets) {
//                         val wallet = wallets[i.key] ?: throw IllegalArgumentException("Could not map Wallet")
//                         val walletData = wallet.state.data
//                         val wCurrency = walletData.currency
//                         /*require(walletData.availableBalance > state.transactionAmount) { "Insufficient Balance in Wallet" }*/
//                         val currency = "GBP"
//                         if (currency == wCurrency) {

//                             val counterparty = wallet.state.data.cashCCP
//                             val notary1 = wallet.state.notary
//                             val builder1 = TransactionType.General.Builder(notary1)
//                             val me = serviceHub.myInfo.legalIdentity
//                             val settleCommand = Command(IOUContract.Commands.Settle(),me.owningKey)
//                             builder1.addCommand(settleCommand)
//                             builder1.addInputState(wallet)
//                             val finalState2: Wallet =  wallet.state.data.addMoney(finalState.transactionAmount.toFloat());

//                             //System.out.print(kycStatus.toString()+" hjjkh");
//                             //
//                             builder1.addOutputState(finalState2)
// //                    // Step 8. Verify and sign the transaction.
//                             builder1.toWireTransaction().toLedgerTransaction(serviceHub).verify()
//                             val ptx1 = serviceHub.signInitialTransaction(builder1)
// //
// //                    // Step 9. Get counterparty signature.
//                             val stx1 = subFlow(CollectSignaturesFlow(ptx1))
// //                    //stxG = stx
// //                    // Step 10. Finalize the transaction.
// //
//                             subFlow(FinalityFlow(stx1)).single()
// //
// //                    return "done";
//                         }}
//             }

                

//                 // Step 8. Verify and sign the transaction.
//                 builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
//                 progressTracker.currentStep = SIGNING
//                 val ptx = serviceHub.signInitialTransaction(builder)

//                 // Step 9. Get counterparty signature.
//                 progressTracker.currentStep = COLLECTING
//                 val stx = subFlow(CollectSignaturesFlow(ptx, COLLECTING.childProgressTracker()))
//                 //stxG = stx
//                 // Step 10. Finalize the transaction.
//                 progressTracker.currentStep = FINALISING

//                 subFlow(FinalityFlow(stx, FINALISING.childProgressTracker())).single()
//                 }
//                 }
//                 return "done";
//             }
//         }

//     @InitiatedBy(UpdateNavFlow.Initiator::class)
//     class Responder(val otherParty: Party) : FlowLogic<String>() {
//         @Suspendable
//         override fun call(): String {
//             val flow = object : SignTransactionFlow(otherParty) {
//                 @Suspendable
//                 override fun checkTransaction(stx: SignedTransaction) {
//                     // TODO: Add some checking.
//                 }
//             }

//             val stx = subFlow(flow)
//             waitForLedgerCommit(stx.id)
//             return "done"
//         }
//     }
// }
