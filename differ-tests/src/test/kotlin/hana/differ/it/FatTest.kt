package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FatTest {

    private fun fat(vararg changed: Pair<Int, Int>): Fat {
        val values = IntArray(70)
        for ((i, n) in changed) values[i] = n
        return Fat(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15], values[16], values[17], values[18], values[19], values[20], values[21], values[22], values[23], values[24], values[25], values[26], values[27], values[28], values[29], values[30], values[31], values[32], values[33], values[34], values[35], values[36], values[37], values[38], values[39], values[40], values[41], values[42], values[43], values[44], values[45], values[46], values[47], values[48], values[49], values[50], values[51], values[52], values[53], values[54], values[55], values[56], values[57], values[58], values[59], values[60], values[61], values[62], values[63], values[64], values[65], values[66], values[67], values[68], values[69])
    }

    @Test
    fun moreThanSixtyFourSlots() {
        val diff = FatDiffer.diff(fat(), fat(0 to 1, 69 to 7))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasF00)
        assertTrue(diff.hasF69)
        assertFalse(diff.hasF01)
        assertFalse(diff.hasF63)
        assertFalse(diff.hasF64)
    }
}
