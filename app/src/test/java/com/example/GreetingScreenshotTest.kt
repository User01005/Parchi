package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.BillItem
import com.example.ui.components.BillItemRow
import com.example.ui.components.CustomerInfoSection
import com.example.ui.components.ReceiptPaperCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ReceiptPaperCard(modifier = Modifier.padding(16.dp)) {
          CustomerInfoSection(
            storeName = "Manmohan Di Hatti",
            billNumber = "1042",
            formattedDateTime = "21 Sep 2026, 01:34 PM",
            customerName = "Ramesh Sharma",
            customerPhone = "9876543210",
            customerHouseNo = "42B",
            onEditCustomerClick = {}
          )
          BillItemRow(
            item = BillItem(serialNumber = 1, itemName = "Atta", weightOrQuantity = "10 kg", price = 356.0, isVerified = true),
            onToggleVerify = {},
            onEditItem = {},
            onDeleteItem = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

