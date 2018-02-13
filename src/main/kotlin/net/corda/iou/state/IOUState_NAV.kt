package net.corda.iou.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.keys
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.iou.contract.IOUContract
import java.security.PublicKey
import java.security.Timestamp
import java.time.LocalDateTime
import java.util.*

/**
 * The IOU State object, with the following properties:
 * - [amount] The amount owed by the [borrower] to the [lender]
 * - [lender] The lending party.
 * - [borrower] The borrowing party.
 * - [contract] Holds a reference to the [IOUContract]
 * - [paid] Records how much of the [amount] has been paid.
 * - [linearId] A unique id shared by all LinearState states representing the same agreement throughout history within
 *   the vaults of all parties. Verify methods should check that one input and one output share the id in a transaction,
 *   except at issuance/termination.
 */

/*  var tDate = new Date('December 07, 2017 23:15:30');
  var day = tDate.getDate();*/

data class IOUState_NAV(
        val fundId: String,
        val borrower: Party,   //Transfer Agent
        val fundManager: Party,  //FundManager
        val nav: Float,
        val lender: Party,     //Me the node that  doing the transaction.
        val date :String,

        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override fun isRelevant(ourKeys: Set<PublicKey>): Boolean {
        return ourKeys.intersect(participants.flatMap {
            it.owningKey.keys
        }).isNotEmpty()
    }


    override val participants: List<AbstractParty> get() = listOf(lender, borrower, fundManager)

    override val contract get() = IOUContract()

}