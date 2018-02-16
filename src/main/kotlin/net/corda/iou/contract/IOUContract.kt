package net.corda.iou.contract

import net.corda.contracts.asset.Cash
import net.corda.contracts.asset.sumCash
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TransactionForContract.InOutGroup
import net.corda.core.crypto.SecureHash
import net.corda.iou.state.IOUState

/**
 * The IOUContract can handle three transaction types involving [IOUState]s:
 * - Issuance. Issuing a new [IOUState] on the ledger, which is a bilateral agreement between two parties.
 * - Transfer. Re-assigning the lender/beneficiary.
 * - Settle. Fully or partially settling the [IOUState] using the Corda [Cash] contract.
 */
class IOUContract : Contract {
    /**
     * Legal prose hash. This is just a dummy string for the time being.
     */
    override val legalContractReference: SecureHash = SecureHash.zeroHash
    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class Transfer : TypeOnlyCommandData(), Commands
        class Settle : TypeOnlyCommandData(), Commands
        class NAV : TypeOnlyCommandData(), Commands
        class NAVPublish:TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [IOUContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: TransactionForContract): Unit {
        val command = tx.commands.requireSingleCommand<IOUContract.Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an IOU." using (tx.outputs.size == 1)
                val iou = tx.outputs.single() as IOUState
                //"A newly issued IOU must have a positive amount." using (iou.amount > Amount(0, iou.amount.token))
                "Both lender and borrower together only may sign IOU issue transaction." using
                        (command.signers.toSet() == iou.participants.map { it.owningKey }.toSet())
                "Transaction Amount should be greater than 500." using (iou.transactionAmount >= 500)
            }
            is Commands.Transfer ->  {
//                "An IOU transfer transaction should only consume one input state." using (tx.inputs.size == 1)
//                "An IOU transfer transaction should only create one output state." using (tx.outputs.size == 1)
//                val input = tx.inputs.single() as IOUState
//                val output = tx.outputs.single() as IOUState
//                "Only the lender property may change." using (input == output.withNewLender(input.lender))
//                "The lender property must change in a transfer." using (input.lender != output.lender)
//                "The borrower, old lender and new lender only must sign an IOU transfer transaction" using
//                        (command.signers.toSet() == (input.participants.map { it.owningKey }.toSet() `union`
//                                output.participants.map { it.owningKey }.toSet()))
            }
            is Commands.Settle -> {

            }
            is Commands.NAV -> {

            }
        }
    }
}
