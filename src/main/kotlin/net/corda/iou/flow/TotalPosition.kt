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
class TotalPosition() : FlowLogic<String>() {

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

        //Get the total Holding for each Trade

        var sumHKIV01 = 0.0f
        var sumDBKS01 = 0.0f
        var sumDBKS02 = 0.0f
        var sumLUKT01 = 0.0f

        //Blocked holding at the time of REDEMPTION

        var blockedHKIV01 = 0.0f
        var blockedDBKS01 = 0.0f
        var blockedDBKS02 = 0.0f
        var blockedLUKT01 = 0.0f



        progressTracker.currentStep = TotalPosition.Companion.READ

        //Start the for loop to get the total Units assigned to each Investor
        for (i in iouStates) {
            val iouststeref = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")
            val fundid = iouststeref.state.data.fundId.trim()
            val transactionamount = iouststeref.state.data.units
            val transactiontype = iouststeref.state.data.txType
            val transactionstatus = iouststeref.state.data.txnStatus
            //val investorname = state.participants.map { it.owningKey }

            //To get the sum
            /// val numbers: IntArray = intArrayOf()

            //For HKIV01 fundID
            if (fundid == "HKIV01") {

                if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {

                    // sumHKIV01.add(transactionamount)
                    sumHKIV01 = sumHKIV01 + transactionamount

                } else {
                    if (transactiontype == "REDEMPTION") {
                        if (transactionstatus == "Settled") {
                            sumHKIV01 = sumHKIV01 - transactionamount
                        } else {
                            blockedHKIV01 = blockedHKIV01 + transactionamount
                        }
                    }
                }
            }


            //For DBKS01 fundID
            if (fundid == "DBKS01") {

                if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                    // sumDBKS01.add(transactionamount)
                    sumDBKS01 = sumDBKS01 + transactionamount
                } else {
                    if (transactiontype == "REDEMPTION") {
                        if (transactionstatus == "Settled") {
                            sumDBKS01 = sumDBKS01 - transactionamount
                        } else {

                            blockedDBKS01 = blockedDBKS01 + transactionamount
                        }

                    }


                }
            }

            //For DBKS02 fundID
            if (fundid == "DBKS02") {
                if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                    // sumDBKS02.add(transactionamount)
                    sumDBKS02 = sumDBKS02 + transactionamount
                } else {
                    if (transactiontype == "REDEMPTION") {
                        if (transactionstatus == "Settled") {
                            sumDBKS02 = sumDBKS02 - transactionamount
                        } else {

                            blockedDBKS02 = blockedDBKS02 + transactionamount
                        }

                    }

                }
            }
            //For LUKT01 fundID
            if (fundid == "LUKT01") {

                if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                    // sumLUKT01.add(transactionamount)
                    sumLUKT01 = sumLUKT01 + transactionamount

                } else {
                    if (transactiontype == "REDEMPTION") {

                        if (transactionstatus == "Settled") {
                            sumLUKT01 = sumLUKT01 - transactionamount
                        } else {

                            blockedLUKT01 = blockedLUKT01 + transactionamount
                        }
                    }


                }


            }
        }

        //Variable for getting the current holding //Initiating the  variable
        var CurrentHKIV01 = 0.0f
        var CurrentDBKS01 = 0.0f
        var CurrentDBKS02 = 0.0f
        var CurrentLUKT01 = 0.0f

        // Finding the actual holding
        CurrentHKIV01=sumHKIV01-blockedHKIV01
        CurrentDBKS01=sumDBKS01-blockedDBKS01
        CurrentDBKS02=sumDBKS02-blockedDBKS02
        CurrentLUKT01=sumLUKT01-blockedLUKT01

        //Crete an comma seperated string to be returned when this KT file is called by API
        var output = sumHKIV01.toString()+","+sumDBKS01+","+sumDBKS02+","+sumLUKT01+","+blockedHKIV01+","+blockedDBKS01+","+blockedDBKS02+","+blockedLUKT01+","+CurrentHKIV01+","+CurrentDBKS01+","+CurrentDBKS02+","+CurrentLUKT01


        return  output
    }
}