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
data class Issue(     val leadBanker: Party,
                      val coBanker: Party,
                      val observer: Party,
                      val issueSize: Int,
                      val issueName:String,
                      val status:String,
                      val transactiondate:String,
                      val paid: Int = 0,
                      override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> get() = listOf(leadBanker, coBanker,observer)
 override fun isRelevant(ourKeys: Set<PublicKey>): Boolean {
        return ourKeys.intersect(participants.flatMap {
            it.owningKey.keys
        }).isNotEmpty()
    }

    fun pay(amountToPay: Int) = copy(paid = paid + amountToPay)
    fun withNewLender(newLender: AbstractParty) = copy(leadBanker = newLender)
    fun withoutLender() = copy(leadBanker = NullKeys.NULL_PARTY)

    override fun toString(): String {
        val lenderString = (lender as? Party)?.name?.organisation ?: lender.owningKey.toBase58String()
        val borrowerString = (borrower as? Party)?.name?.organisation ?: borrower.owningKey.toBase58String()
        return "Obligation($linearId): $borrowerString owes $lenderString $issueSize and has paid $paid so far."
    }
      override val contract get() = IOUContract()
}
