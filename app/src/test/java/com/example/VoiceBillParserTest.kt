package com.example

import com.example.parser.VoiceBillParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceBillParserTest {

    @Test
    fun parseUserExactScenario_withCommasAndAnd() {
        val transcript = "10 kg atta, 5 kg rajma, 2 kg maida, and 3 kg sugar"
        val result = VoiceBillParser.parseTranscript(transcript, startingSerial = 1)

        assertEquals(4, result.items.size)

        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)

        assertEquals("Rajma", result.items[1].itemName)
        assertEquals("5 kg", result.items[1].weightOrQuantity)

        assertEquals("Maida", result.items[2].itemName)
        assertEquals("2 kg", result.items[2].weightOrQuantity)

        assertEquals("Sugar", result.items[3].itemName)
        assertEquals("3 kg", result.items[3].weightOrQuantity)
    }

    @Test
    fun parseUserExactScenario_withoutCommas_consecutiveSpeech() {
        val transcript = "10 kg atta 5 kg rajma 2 kg maida and 3 kg sugar"
        val result = VoiceBillParser.parseTranscript(transcript, startingSerial = 1)

        assertEquals(4, result.items.size)

        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)

        assertEquals("Rajma", result.items[1].itemName)
        assertEquals("5 kg", result.items[1].weightOrQuantity)

        assertEquals("Maida", result.items[2].itemName)
        assertEquals("2 kg", result.items[2].weightOrQuantity)

        assertEquals("Sugar", result.items[3].itemName)
        assertEquals("3 kg", result.items[3].weightOrQuantity)
    }

    @Test
    fun parsePhoneticallyMisheardSpeech_reconstructsIntendedKiranaItems() {
        // Test phonetic mishearings from STT engines:
        // "aa" -> Atta, "tama" -> Rajma, "ida" -> Maida, "sugar core" -> Sugar (stripping "core")
        val transcript = "10 kg aa 5 kg tama 2 kg ida 3 kg sugar core"
        val result = VoiceBillParser.parseTranscript(transcript, startingSerial = 1)

        assertEquals(4, result.items.size)

        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)

        assertEquals("Rajma", result.items[1].itemName)
        assertEquals("5 kg", result.items[1].weightOrQuantity)

        assertEquals("Maida", result.items[2].itemName)
        assertEquals("2 kg", result.items[2].weightOrQuantity)

        assertEquals("Sugar", result.items[3].itemName)
        assertEquals("3 kg", result.items[3].weightOrQuantity)
    }

    @Test
    fun parseConsecutiveItems_trailingQuantities() {
        val transcript = "atta 10 kg rajma 5 kg maida 2 kg sugar 3 kg"
        val result = VoiceBillParser.parseTranscript(transcript, startingSerial = 1)

        assertEquals(4, result.items.size)

        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)

        assertEquals("Rajma", result.items[1].itemName)
        assertEquals("5 kg", result.items[1].weightOrQuantity)

        assertEquals("Maida", result.items[2].itemName)
        assertEquals("2 kg", result.items[2].weightOrQuantity)

        assertEquals("Sugar", result.items[3].itemName)
        assertEquals("3 kg", result.items[3].weightOrQuantity)
    }

    @Test
    fun parseSingleItem_withPrice() {
        val item = VoiceBillParser.parseSingleItemClause("10 kg atta 356", 1)
        assertNotNull(item)
        assertEquals(1, item?.serialNumber)
        assertEquals("Atta", item?.itemName)
        assertEquals("10 kg", item?.weightOrQuantity)
        assertEquals(356.0, item?.price)
    }

    @Test
    fun parseSingleItem_withRupeesKeyword() {
        val item = VoiceBillParser.parseSingleItemClause("5 kg sugar 280 rupees", 2)
        assertNotNull(item)
        assertEquals(2, item?.serialNumber)
        assertEquals("Sugar", item?.itemName)
        assertEquals("5 kg", item?.weightOrQuantity)
        assertEquals(280.0, item?.price)
    }

    @Test
    fun parseSingleItem_withoutPrice_leavesPriceNull() {
        val item = VoiceBillParser.parseSingleItemClause("2 kg moong dal", 3)
        assertNotNull(item)
        assertEquals(3, item?.serialNumber)
        assertEquals("Moong Dal", item?.itemName)
        assertEquals("2 kg", item?.weightOrQuantity)
        assertNull(item?.price)
    }

    @Test
    fun parseCustomerDetails_extractsMetadata() {
        val transcript = "customer name Ramesh Sharma mobile 9876543210 house 42B"
        val result = VoiceBillParser.parseTranscript(transcript)

        assertEquals("Ramesh Sharma", result.customerInfo.name)
        assertEquals("9876543210", result.customerInfo.phone)
        assertEquals("42B", result.customerInfo.houseNo)
    }

    @Test
    fun parseVariousUnits_litresAndPackets() {
        val oil = VoiceBillParser.parseSingleItemClause("1 litre mustard oil 145", 1)
        assertNotNull(oil)
        assertEquals("Mustard Oil", oil?.itemName)
        assertEquals("1 L", oil?.weightOrQuantity)
        assertEquals(145.0, oil?.price)

        val maggi = VoiceBillParser.parseSingleItemClause("3 packets maggi 42", 2)
        assertNotNull(maggi)
        assertEquals("Maggi", maggi?.itemName)
        assertEquals("3 pkts", maggi?.weightOrQuantity)
        assertEquals(42.0, maggi?.price)
    }
}
