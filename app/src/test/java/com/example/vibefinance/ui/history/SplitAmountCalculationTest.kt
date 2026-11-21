package com.example.vibefinance.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class SplitAmountCalculationTest {

    @Test
    fun exactSplitDistributesRemainderCentsToFirstParticipants() {
        val result = calculateExactSplitShares(
            baseAmount = 10.00,
            tipPercent = 0,
            participantIds = listOf("self", "a", "b")
        )
        assertEquals(10.00, result.totalAmount, 0.0001)
        assertEquals(3.34, result.yourShare, 0.0001)
        assertEquals(6.66, result.owedToYou, 0.0001)
        assertEquals(3.34, result.sharesByParticipantId["self"]!!, 0.0001)
        assertEquals(3.33, result.sharesByParticipantId["a"]!!, 0.0001)
        assertEquals(3.33, result.sharesByParticipantId["b"]!!, 0.0001)
        val sum = result.sharesByParticipantId.values.sum()
        assertEquals(result.totalAmount, sum, 0.0001)
    }

    @Test
    fun splitWithTipComputesExactTotalAndShares() {
        val result = calculateExactSplitShares(
            baseAmount = 100.00,
            tipPercent = 10,
            participantIds = listOf("self", "friend1", "friend2", "friend3")
        )
        assertEquals(110.00, result.totalAmount, 0.0001)
        assertEquals(27.50, result.yourShare, 0.0001)
        assertEquals(82.50, result.owedToYou, 0.0001)
        assertEquals(110.00, result.sharesByParticipantId.values.sum(), 0.0001)
    }
}
