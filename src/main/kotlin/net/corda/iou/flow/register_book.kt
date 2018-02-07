package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
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
class Register() : FlowLogic<String>() {

    override val progressTracker: ProgressTracker = TotalPosition.tracker()

    companion object {
        object FETCH : ProgressTracker.Step("Obtaining Transactions from vault")
        object READ : ProgressTracker.Step("Building and verifying transaction.")


        fun tracker() = ProgressTracker(FETCH, READ)
    }

    @Suspendable

    override fun call(): String {


        // Stage 1. Retrieve all IOU's from the vault.
        progressTracker.currentStep = TotalPosition.Companion.FETCH
        val iouStates = serviceHub.vaultService.linearHeadsOfType<IOUState>()
        progressTracker.currentStep = TotalPosition.Companion.READ



        //define an empty array to hold all the investores in our system
        var bucket: ArrayList<String> = ArrayList()
        var investor_name : String ="hey";






        //Start the for loop to get the total Units assigned to each Investor
        for (i in iouStates) {
                            val iouststeref = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")



                            //Variables to store the fundID
                            val fundid = iouststeref.state.data.fundId

                             //Variables to store the lender identity
                             val transactiontype = iouststeref.state.data.txType

                             //Variables to store the  units
                             val units = iouststeref.state.data.units

                              //variable to store the totalamount
                              val transactionamount = iouststeref.state.data.transactionAmount


                             //Variables to store the lender identity
                            val investor = iouststeref.state.data.lender.toString()

                            bucket.add(investor)

                            }

            for (i in bucket){

                            investor_name = i


                             }





        //var a = bucket.distinct()
        //return  a.toString()
        return investor_name

    }


}
