// package net.corda.iou.flow

// import co.paralleluniverse.fibers.Suspendable
// import net.corda.core.flows.FlowLogic
// import net.corda.core.flows.InitiatingFlow
// import net.corda.core.flows.StartableByRPC
// import net.corda.core.node.services.linearHeadsOfType
// import net.corda.core.utilities.ProgressTracker
// import net.corda.iou.state.IOUState
// import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction

// /**
//  * This is the flow which opens all the transactions from the vault to read the vote.
//  * Does a sum total and displays the final voting result.
//  */


// @StartableByRPC
// @InitiatingFlow
// class nav() : FlowLogic<String>() {

//     override val progressTracker: ProgressTracker = TotalPosition.tracker()

//     companion object {
//         object FETCH : ProgressTracker.Step("Obtaining Transactions from vault")
//         object READ : ProgressTracker.Step("Building and verifying transaction.")


//         fun tracker() = ProgressTracker(FETCH, READ)
//     }

//     @Suspendable
//     override fun call(): String {


//         var a = "10"
//         var b ="5"
//         var c= "15"
//         var d ="20"

//         var nav = a+","+b+","+c+","+d


//         return  nav
//     }
// }