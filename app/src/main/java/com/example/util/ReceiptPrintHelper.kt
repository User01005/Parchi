package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.model.BillItem

object ReceiptPrintHelper {

    fun printReceipt(
        context: Context,
        storeName: String,
        billNumber: String,
        dateTime: String,
        customerName: String,
        customerPhone: String,
        customerHouseNo: String,
        items: List<BillItem>,
        totalAmount: Double
    ) {
        val htmlContent = generateReceiptHtml(
            storeName = storeName,
            billNumber = billNumber,
            dateTime = dateTime,
            customerName = customerName,
            customerPhone = customerPhone,
            customerHouseNo = customerHouseNo,
            items = items,
            totalAmount = totalAmount
        )

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Receipt_$billNumber")
                val printAttributes = PrintAttributes.Builder()
                    .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A5)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager?.print("Grocery_Bill_$billNumber", printAdapter, printAttributes)
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun generateReceiptHtml(
        storeName: String,
        billNumber: String,
        dateTime: String,
        customerName: String,
        customerPhone: String,
        customerHouseNo: String,
        items: List<BillItem>,
        totalAmount: Double
    ): String {
        val itemsRows = items.joinToString("\n") { item ->
            val priceStr = item.price?.let { "₹${if (it % 1.0 == 0.0) it.toInt() else String.format("%.2f", it)}" } ?: "-"
            val checkmark = if (item.isVerified) "&#10003; " else ""
            """
            <tr>
                <td style="text-align: center; padding: 4px 2px;">${item.serialNumber}</td>
                <td style="padding: 4px 4px; font-weight: bold;">$checkmark${escapeHtml(item.itemName)}</td>
                <td style="text-align: center; padding: 4px 4px;">${escapeHtml(item.weightOrQuantity)}</td>
                <td style="text-align: right; padding: 4px 4px;">$priceStr</td>
            </tr>
            """.trimIndent()
        }

        val hasUnpricedItems = items.any { item -> item.price == null }
        val totalNote = if (hasUnpricedItems) "*(Some items pending price)" else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <style>
                body {
                    font-family: 'Courier New', Courier, monospace, sans-serif;
                    font-size: 13px;
                    line-height: 1.3;
                    color: #000;
                    margin: 0;
                    padding: 16px;
                    background: #fff;
                }
                .receipt-container {
                    max-width: 380px;
                    margin: 0 auto;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px dashed #000;
                    padding-bottom: 8px;
                    margin-bottom: 8px;
                }
                .store-title {
                    font-size: 18px;
                    font-weight: bold;
                    letter-spacing: 1px;
                }
                .sub-title {
                    font-size: 11px;
                    margin-top: 2px;
                }
                .meta-section {
                    margin-bottom: 8px;
                    border-bottom: 1px dashed #000;
                    padding-bottom: 6px;
                }
                .meta-row {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 2px;
                    font-size: 12px;
                }
                .customer-field {
                    font-size: 12px;
                    margin-bottom: 2px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 6px;
                    font-size: 12px;
                }
                th {
                    border-bottom: 1px solid #000;
                    border-top: 1px solid #000;
                    padding: 4px 2px;
                    text-align: left;
                }
                .total-section {
                    border-top: 2px dashed #000;
                    border-bottom: 2px dashed #000;
                    margin-top: 10px;
                    padding: 8px 4px;
                    font-size: 15px;
                    font-weight: bold;
                    display: flex;
                    justify-content: space-between;
                }
                .footer {
                    text-align: center;
                    margin-top: 14px;
                    font-size: 11px;
                }
            </style>
        </head>
        <body>
            <div class="receipt-container">
                <div class="header">
                    <div class="store-title">${escapeHtml(storeName)}</div>
                    <div class="sub-title">GROCERY PURCHASE BILL / RECEIPT</div>
                </div>

                <div class="meta-section">
                    <div class="meta-row">
                        <span><strong>Bill No:</strong> #$billNumber</span>
                        <span>$dateTime</span>
                    </div>
                    <div class="customer-field">
                        <strong>Customer:</strong> ${if (customerName.isNotBlank()) escapeHtml(customerName) else "____________________"}
                    </div>
                    <div class="customer-field">
                        <strong>Mobile No:</strong> ${if (customerPhone.isNotBlank()) escapeHtml(customerPhone) else "____________________"}
                    </div>
                    <div class="customer-field">
                        <strong>House / Flat:</strong> ${if (customerHouseNo.isNotBlank()) escapeHtml(customerHouseNo) else "____________________"}
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="width: 12%; text-align: center;">S.No</th>
                            <th style="width: 48%;">Item</th>
                            <th style="width: 22%; text-align: center;">Qty/Wt</th>
                            <th style="width: 18%; text-align: right;">Price</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsRows
                    </tbody>
                </table>

                <div class="total-section">
                    <span>GRAND TOTAL</span>
                    <span>₹${if (totalAmount % 1.0 == 0.0) totalAmount.toInt() else String.format("%.2f", totalAmount)}</span>
                </div>
                ${if (totalNote.isNotBlank()) "<div style='font-size: 10px; text-align: right; margin-top: 2px;'>$totalNote</div>" else ""}

                <div class="footer">
                    <div>* Verified Grocery List *</div>
                    <div>Thank you! Visit Again!</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
