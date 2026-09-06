package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TransferEntity
import com.example.model.CurrencyCatalog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ZeroFeeGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionReceiptDialog(
  transfer: TransferEntity?,
  onDismiss: () -> Unit
) {
  if (transfer == null) return

  val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(transfer.timestampMillis))

  val senderCurr = CurrencyCatalog.findByCode(transfer.senderCurrency)
  val recipCurr = CurrencyCatalog.findByCode(transfer.recipientCurrency)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("transaction_receipt_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ZeroPay Official Receipt",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp).testTag("close_receipt_dialog")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Transfer Summary
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${recipCurr.symbol}${String.format("%,.2f", transfer.recipientAmount)}",
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Sent ${transfer.senderCurrency} ${String.format("%.2f", transfer.senderAmount)} to ${transfer.recipientName}",
              fontSize = 12.sp,
              color = SlateLight,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Zero-fee pill
            Surface(
              shape = RoundedCornerShape(20),
              color = MintLight,
              border = androidx.compose.foundation.BorderStroke(1.dp, MintAccent)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ElectricBolt,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "0% Markup • Zero Fee Applied",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delivery Tracker Steps
        Text(
          text = "Transfer Status & Network Tracker",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        TrackerStep(
          title = "Transfer Initiated",
          subtitle = formattedDate,
          isCompleted = true,
          isLast = false
        )
        TrackerStep(
          title = "0-Fee Currency Exchange",
          subtitle = "Mid-market rate 1 ${transfer.senderCurrency} = ${String.format("%.4f", transfer.exchangeRate)} ${transfer.recipientCurrency}",
          isCompleted = true,
          isLast = false
        )
        TrackerStep(
          title = "Cross-Border Settlement",
          subtitle = "Settled via Instant Zero-Fee Global Ledger",
          isCompleted = true,
          isLast = false
        )
        TrackerStep(
          title = if (transfer.status == "Delivered") "Delivered to Recipient" else "In Transit (Instant Clearing)",
          subtitle = "${transfer.recipientName} (${transfer.deliveryMethod})",
          isCompleted = transfer.status == "Delivered",
          isLast = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown Table
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            ReceiptRow(label = "Reference Code", value = transfer.referenceCode, isMonospace = true)
            ReceiptRow(label = "Delivery Method", value = transfer.deliveryMethod)
            ReceiptRow(label = "Recipient Country", value = transfer.recipientCountry)
            ReceiptRow(label = "Transfer Fee", value = "$0.00 (FREE)", isHighlight = true)
            ReceiptRow(label = "FX Margin", value = "0.0% (Zero Markup)", isHighlight = true)
            ReceiptRow(
              label = "Estimated Bank Savings",
              value = "+$${String.format("%.2f", transfer.bankSavings)} saved",
              isSavings = true
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("receipt_done_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun TrackerStep(
  title: String,
  subtitle: String,
  isCompleted: Boolean,
  isLast: Boolean
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(CircleShape)
          .background(if (isCompleted) EmeraldPrimary else SlateLight),
        contentAlignment = Alignment.Center
      ) {
        if (isCompleted) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
        }
      }
      if (!isLast) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(if (isCompleted) EmeraldPrimary.copy(alpha = 0.5f) else SlateBorder)
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 10.dp)) {
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = SlateLight
      )
    }
  }
}

@Composable
fun ReceiptRow(
  label: String,
  value: String,
  isMonospace: Boolean = false,
  isHighlight: Boolean = false,
  isSavings: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 11.sp, color = SlateLight)
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = if (isHighlight || isSavings) FontWeight.Bold else FontWeight.Medium,
      fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
      color = when {
        isSavings -> ZeroFeeGold
        isHighlight -> EmeraldPrimary
        else -> MaterialTheme.colorScheme.onSurface
      }
    )
  }
}
