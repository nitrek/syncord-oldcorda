package net.corda.iou.flow

import co.paralleluniverse.fibers.Suspendable
import com.sun.xml.internal.fastinfoset.util.StringArray
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.linearHeadsOfType
import net.corda.core.utilities.ProgressTracker
import net.corda.iou.state.IOUState
import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction
import org.json.simple.JSONObject
import java.util.ArrayList
import javax.json.JsonObject
import javax.swing.text.html.parser.Parser

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
        var investor_name: String = "hey";


        //Define that variable that we are going to be using from Incvestor1

        var sumHKIV01iv1 = 0.0f
        var sumDBKS01iv1 = 0.0f
        var sumDBKS02iv1 = 0.0f
        var sumLUKT01iv1 = 0.0f

        var sumHKIV01unitsiv1 = 0.0f
        var sumDBKS01unitsiv1 = 0.0f
        var sumDBKS02unitsiv1 = 0.0f
        var sumLUKT01unitsiv1 = 0.0f


        //Define that variable that we are going to be using from Investor 2

        var sumHKIV01iv2 = 0.0f
        var sumDBKS01iv2 = 0.0f
        var sumDBKS02iv2 = 0.0f
        var sumLUKT01iv2 = 0.0f

        var sumHKIV01unitsiv2 = 0.0f
        var sumDBKS01unitsiv2 = 0.0f
        var sumDBKS02unitsiv2 = 0.0f
        var sumLUKT01unitsiv2 = 0.0f



        //Varaible to get the total holding

        var totalHKIV01 = 0.0f
        var totalDBKS01 = 0.0f
        var totalDBKS02 = 0.0f
        var totalLUKT01 = 0.0f


        // Variable to get the percentage holding for investor1

        var percentageHKIV01iv1 = 0.0f
        var percentageDBKS01iv1 = 0.0f
        var percentageDBKS02iv1 = 0.0f
        var percentageLUKT01iv1 = 0.0f


        // Variable to get the percentage holding for investor1

        var percentageHKIV01iv2 = 0.0f
        var percentageDBKS01iv2 = 0.0f
        var percentageDBKS02iv2 = 0.0f
        var percentageLUKT01iv2 = 0.0f



        // Varibake to store the jason output of each investor

        // var investor1array:  Array<String >
        // var investor1array = JSONObject()

        var investor1array = ""

        //val listOfStates = ArrayList<String>()




        //var investor2array: Array<String>

        var investor2array = arrayOf<String>()

        // var finaloutputarray :Array<String>

        var finaloutputarray = arrayOf<String>()


        //Start the for loop to get the total Units assigned to each Investor
/*        val investors : Array<String>;//array
        val outJson = [];*/
        for (i in iouStates) {
            val iouststeref = iouStates[i.key] ?: throw IllegalArgumentException("Could not map IOU's")


            //variable to store the lender name

            val lender = iouststeref.state.data.lender.toString()
            //Variables to store the fundID
            val fundid = iouststeref.state.data.fundId

            //Variables to store the lender identity
            val transactiontype = iouststeref.state.data.txType

            //Variables to store the  units
            val units = iouststeref.state.data.units

            //variable to store the totalamount
            val transactionamount = iouststeref.state.data.transactionAmount

            //variable to get the transaction status

            val transactionstatus = iouststeref.state.data.txnStatus

/*        inve = "02,01,03"
            if (lender in investors){
                val i = investors.indexOf(lender)
                outJson[i].append(json)
            }
            else{
                investors.append(lender);
                outJson[i].append(json)
            }*/
            if (lender == "CN=UH00001,O=NodeC") {


                if (fundid == "HKIV01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {

                        //sum of transaction amount
                        sumHKIV01iv1 = sumHKIV01iv1 + transactionamount

                        //sum of units
                        sumHKIV01unitsiv1 = sumHKIV01unitsiv1 +units

                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {

                                //substraction of transaction amount
                                sumHKIV01iv1 = sumHKIV01iv1 - transactionamount


                                //Substaraction of units
                                sumHKIV01unitsiv1 = sumHKIV01unitsiv1 -units


                            } else {
                                // blockedHKIV01 = blockedHKIV01 + transactionamount
                            }
                        }
                    }
                }


                //For DBKS01 fundID
                if (fundid == "DBKS01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumDBKS01.add(transactionamount)
                        sumDBKS01iv1 = sumDBKS01iv1 + transactionamount

                        sumDBKS01unitsiv1 = sumDBKS01unitsiv1 +units
                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {
                                sumDBKS01iv1 = sumDBKS01iv1 - transactionamount

                                sumDBKS01unitsiv1 = sumDBKS01unitsiv1 - units
                            } else {

                                //blockedDBKS01 = blockedDBKS01 + transactionamount
                            }

                        }


                    }
                }

                //For DBKS02 fundID
                if (fundid == "DBKS02") {
                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumDBKS02.add(transactionamount)
                        sumDBKS02iv1 = sumDBKS02iv1 + transactionamount

                        sumDBKS02unitsiv1 = sumDBKS02unitsiv1 + units


                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {
                                sumDBKS02iv1 = sumDBKS02iv1 - transactionamount

                                sumDBKS02unitsiv1 = sumDBKS02unitsiv1 - units
                            } else {

                                // blockedDBKS02 = blockedDBKS02 + transactionamount
                            }

                        }

                    }
                }
                //For LUKT01 fundID
                if (fundid == "LUKT01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumLUKT01.add(transactionamount)
                        sumLUKT01iv1 = sumLUKT01iv1 + transactionamount

                        sumLUKT01unitsiv1 = sumLUKT01unitsiv1 + units

                    } else {
                        if (transactiontype == "REDEMPTION") {

                            if (transactionstatus == "Settled") {
                                sumLUKT01iv1 = sumLUKT01iv1 - transactionamount

                                sumLUKT01unitsiv1 = sumLUKT01unitsiv1- units
                            } else {

                                //  blockedLUKT01 = blockedLUKT01 + transactionamount
                            }
                        }


                    }

                }

            }

            else {

                if (fundid == "HKIV01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {

                        // sumHKIV01.add(transactionamount)
                        sumHKIV01iv2 = sumHKIV01iv2 + transactionamount

                        sumHKIV01unitsiv2 = sumHKIV01unitsiv2 + units

                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {
                                sumHKIV01iv2 = sumHKIV01iv2 - transactionamount

                                sumHKIV01unitsiv2 -= units
                            } else {
                                // blockedHKIV01 = blockedHKIV01 + transactionamount
                            }
                        }
                    }
                }


                //For DBKS01 fundID
                if (fundid == "DBKS01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumDBKS01.add(transactionamount)
                        sumDBKS01iv2 = sumDBKS01iv2 + transactionamount

                        sumDBKS01unitsiv2 += units

                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {
                                sumDBKS01iv2 = sumDBKS01iv2 - transactionamount

                                sumDBKS01unitsiv2 -=units
                            } else {

                                //blockedDBKS01 = blockedDBKS01 + transactionamount
                            }

                        }


                    }
                }

                //For DBKS02 fundID
                if (fundid == "DBKS02") {
                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumDBKS02.add(transactionamount)
                        sumDBKS02iv2 = sumDBKS02iv2 + transactionamount

                        sumDBKS02unitsiv2 += units
                    } else {
                        if (transactiontype == "REDEMPTION") {
                            if (transactionstatus == "Settled") {
                                sumDBKS02iv2 = sumDBKS02iv2 - transactionamount

                                sumDBKS02unitsiv2 -= units
                            } else {

                                // blockedDBKS02 = blockedDBKS02 + transactionamount
                            }

                        }

                    }
                }
                //For LUKT01 fundID
                if (fundid == "LUKT01") {

                    if (transactiontype == "SUBSCRIPTION" && transactionstatus == "Settled") {
                        // sumLUKT01.add(transactionamount)
                        sumLUKT01iv2 = sumLUKT01iv2 + transactionamount



                        sumLUKT01unitsiv2 += sumLUKT01unitsiv2

                    } else {
                        if (transactiontype == "REDEMPTION") {

                            if (transactionstatus == "Settled") {
                                sumLUKT01iv2 = sumLUKT01iv2 - transactionamount

                                sumLUKT01unitsiv2 -= sumLUKT01unitsiv2
                            } else {

                                //  blockedLUKT01 = blockedLUKT01 + transactionamount
                            }
                        }


                    }

                }

            }
        }

        //Total sum of each fund to get the percentage value
        totalHKIV01 = sumHKIV01iv1+sumHKIV01iv2
        totalDBKS01 = sumDBKS01iv1 +sumDBKS01iv2
        totalDBKS02 = sumDBKS02iv1 + sumDBKS02iv2
        totalLUKT01 = sumLUKT01iv1+ sumLUKT01iv2


        // TO get the percentage holding for the first investor 2

        percentageHKIV01iv1 =  (sumHKIV01iv1 / totalHKIV01) * 100
        percentageDBKS01iv1 =  (sumDBKS01iv1 / totalDBKS01) * 100
        percentageDBKS02iv1 =  (sumDBKS02iv1 / totalDBKS02) * 100
        percentageLUKT01iv1 =  (sumLUKT01iv1 / totalLUKT01) * 100

        // To get the percentage holdoing of investor 2

        percentageHKIV01iv2 = (sumHKIV01iv2 / totalHKIV01) * 100
        percentageDBKS01iv2 = (sumDBKS01iv2 / totalDBKS01) * 100
        percentageDBKS02iv2 = (sumDBKS02iv2 / totalDBKS02) * 100
        percentageLUKT01iv2 = (sumLUKT01iv2 / totalLUKT01) * 100




        // Json to return when function is called

        //TO get the investor1 holding

        var investor1HKIV01 = "CN=UH00001,O=NodeC" + "," + "HKIV01" +","+ sumHKIV01iv1 +","+sumHKIV01unitsiv1+"," +percentageHKIV01iv1

        var investor1DBKS01 = "CN=UH00001,O=NodeC" + "," + "DBKS01" +","+ sumDBKS01iv1 +","+sumDBKS01unitsiv1+"," +percentageDBKS01iv1

        var investor1DBKS02 = "CN=UH00001,O=NodeC" + "," + "DBKS02" +","+ sumDBKS02iv1 +","+sumDBKS02unitsiv1+"," +percentageDBKS02iv1

        var investor1LUKT01 = "CN=UH00001,O=NodeC" + "," + "LUKT01" +","+ sumLUKT01iv1 +","+sumLUKT01unitsiv1+"," +percentageLUKT01iv1


        //TO get the investor2 holding

        var investor2HKIV01 = "CN=UH00002,O=NodeD" + "," + "HKIV01" +","+ sumHKIV01iv2 +","+sumHKIV01unitsiv2+"," +percentageHKIV01iv2

        var investor2DBKS01 = "CN=UH00002,O=NodeD" + "," + "DBKS01" +","+ sumDBKS01iv2 +","+sumDBKS01unitsiv2+"," +percentageDBKS01iv2

        var investor2DBKS02 ="CN=UH00002,O=NodeD" + "," + "DBKS02" +","+ sumDBKS02iv2 +","+sumDBKS02unitsiv2+"," +percentageDBKS02iv2

        var investor2LUKT01 = "CN=UH00002,O=NodeD" + "," + "LUKT01" +","+ sumLUKT01iv2 +","+sumLUKT01unitsiv2+"," +percentageLUKT01iv2

        val investor1arrayintermediat1 = investor1HKIV01 + "," +investor1DBKS01 +"," +investor1DBKS02 + ","+investor1LUKT01

        val investor1arrayintermediat2 = investor2HKIV01 + "," +investor2DBKS01 +"," +investor2DBKS02 + ","+investor2LUKT01

        investor1array = investor1arrayintermediat1 + "," +  investor1arrayintermediat2

        return investor1array
    }

}
