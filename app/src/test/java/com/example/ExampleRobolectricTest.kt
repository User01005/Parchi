package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.parser.VoiceBillParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PARCHI", appName)
  }

  @Test
  fun `parse customer and multiple hindi items`() {
    val input = "Customer Ramesh phone 9876543210 10 kilo atta 356 aur 5 kilo cheeni 200"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals("Ramesh", result.customerInfo.name)
    assertEquals("9876543210", result.customerInfo.phone)
    assertEquals(2, result.items.size)

    assertEquals("Atta", result.items[0].itemName)
    assertEquals("10 kg", result.items[0].weightOrQuantity)
    assertEquals(356.0, result.items[0].price)

    assertEquals("Sugar", result.items[1].itemName)
    assertEquals("5 kg", result.items[1].weightOrQuantity)
    assertEquals(200.0, result.items[1].price)
  }

  @Test
  fun `parse hindi price phrases and fractions`() {
    val input = "50 rupaye ka dahi aur aadha kilo jeera 120"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals(2, result.items.size)
    assertEquals("Curd", result.items[0].itemName)
    assertEquals(50.0, result.items[0].price)

    assertEquals("Jeera", result.items[1].itemName)
    assertEquals("500 g", result.items[1].weightOrQuantity)
    assertEquals(120.0, result.items[1].price)
  }

  @Test
  fun `parse devanagari hindi input`() {
    val input = "१० किलो आटा ३५६ और ५ किलो चीनी २००"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals(2, result.items.size)
    assertEquals("10 kg", result.items[0].weightOrQuantity)
    assertEquals(356.0, result.items[0].price)

    assertEquals("5 kg", result.items[1].weightOrQuantity)
    assertEquals(200.0, result.items[1].price)
  }

  @Test
  fun `parse user exact example with hesitation and bilingual items`() {
    val input = "Aata 10 kg, Cheeni 10 kg, two piece, two piece, two brush"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals(3, result.items.size)
    assertEquals("Atta", result.items[0].itemName)
    assertEquals("10 kg", result.items[0].weightOrQuantity)

    assertEquals("Sugar", result.items[1].itemName)
    assertEquals("10 kg", result.items[1].weightOrQuantity)

    assertEquals("Brush", result.items[2].itemName)
    assertEquals("2 pcs", result.items[2].weightOrQuantity)
  }

  @Test
  fun `parse two brush into brush with 2 pcs`() {
    val input = "two brush"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals(1, result.items.size)
    assertEquals("Brush", result.items[0].itemName)
    assertEquals("2 pcs", result.items[0].weightOrQuantity)
  }

  @Test
  fun `filter out thinking and filler words completely`() {
    val input = "okay Aata 10 kg, wait ek second, hmm two brush, bas itna hi"
    val result = VoiceBillParser.parseTranscript(input)

    assertEquals(2, result.items.size)
    assertEquals("Atta", result.items[0].itemName)
    assertEquals("10 kg", result.items[0].weightOrQuantity)

    assertEquals("Brush", result.items[1].itemName)
    assertEquals("2 pcs", result.items[1].weightOrQuantity)
  }
}

