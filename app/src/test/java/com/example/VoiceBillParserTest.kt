package com.example

import com.example.parser.VoiceBillParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceBillParserTest {

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
    fun parseMultipleItems_chainedWithAnd() {
        val transcript = "10 kg atta 356 and 5 kg sugar 280 rupees and 2 kg moong dal"
        val result = VoiceBillParser.parseTranscript(transcript, startingSerial = 1)

        assertEquals(3, result.items.size)

        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)
        assertEquals(356.0, result.items[0].price)

        assertEquals("Sugar", result.items[1].itemName)
        assertEquals("5 kg", result.items[1].weightOrQuantity)
        assertEquals(280.0, result.items[1].price)

        assertEquals("Moong Dal", result.items[2].itemName)
        assertEquals("2 kg", result.items[2].weightOrQuantity)
        assertNull(result.items[2].price)
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
