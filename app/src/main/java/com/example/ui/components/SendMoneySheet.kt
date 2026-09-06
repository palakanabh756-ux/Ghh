package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecipientEntity
import com.example.model.CurrencyCatalog
import com.example.ui.SendUiState
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ZeroFeeGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneySheet(
  uiState: SendUiState,
  recipients: List<RecipientEntity>,
  onClose: () -> Unit,
  onAmountChange: (String) -> Unit,
  onFromCurrencyChange: (String) -> Unit,
  onToCurrencyChange: (String) -> Unit,
  onSwapCurrencies: () -> Unit,
  onSelectRecipient: (RecipientEntity) -> Unit,
  onUpdateCustomRecipient: (String, String, String, String) -> Unit,
  onUpdateDeliveryMethod: (String) -> Unit,
  onExecuteTransfer: () -> Unit,
  onViewReceipt: (com.example.data.TransferEntity) -> Unit
) {
  if (!uiState.isOpen) return

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val senderCurrency = CurrencyCatalog.findByCode(uiState.fromCurrency)
  val receiverCurrency = CurrencyCatalog.findByCode(uiState.toCurrency)
  val exchangeRate = remember(uiState.fromCurrency, uiState.toCurrency) {
    CurrencyCatalog.calculateExchangeRate(uiState.fromCurrency, uiState.toCurrency)
  }

  val amountDouble = uiState.amountInput.toDoubleOrNull() ?: 0.0
  val calculatedReceiverAmount = amountDouble * exchangeRate
  val estimatedSavings = remember(amountDouble, uiState.fromCurrency) {
    CurrencyCatalog.calculateBankSavings(amountDouble, uiState.fromCurrency)
  }

  ModalBottomSheet(
    onDismissRequest = onClose,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier
      .testTag("send_money_bottom_sheet")
      .fillMaxHeight(0.94f)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Send Internationally",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Zero transfer fees • Real mid-market rate",
            fontSize = 12.sp,
            color = MintAccent,
            fontWeight = FontWeight.Medium
          )
        }
        IconButton(onClick = onClose, modifier = Modifier.testTag("close_send_sheet")) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // If transfer just completed, show high-speed success state
      if (uiState.completedTransfer != null) {
        TransferSuccessView(
          transfer = uiState.completedTransfer,
          onViewReceipt = { onViewReceipt(uiState.completedTransfer) },
          onDone = onClose
        )
      } else {
        // Active Transfer Flow
        // Sender Amount Card
        CurrencyInputBlock(
          label = "You send exactly",
          amount = uiState.amountInput,
          currencyCode = uiState.fromCurrency,
          flag = senderCurrency.flagEmoji,
          onAmountChange = onAmountChange,
          onCurrencyChange = onFromCurrencyChange,
          testTagPrefix = "send_from"
        )

        // Mid-market Rate Bar with Swap button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = ZeroFeeGold,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "1 ${uiState.fromCurrency} = ${String.format("%.4f", exchangeRate)} ${uiState.toCurrency}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(8.dp))
              IconButton(
                onClick = onSwapCurrencies,
                modifier = Modifier.size(24.dp).testTag("swap_currencies_button")
              ) {
                Icon(
                  imageVector = Icons.Default.SwapVert,
                  contentDescription = "Swap",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        // Recipient Gets Amount Card
        CurrencyOutputBlock(
          label = "Recipient receives (Guaranteed)",
          calculatedAmount = calculatedReceiverAmount,
          currencyCode = uiState.toCurrency,
          flag = receiverCurrency.flagEmoji,
          symbol = receiverCurrency.symbol,
          onCurrencyChange = onToCurrencyChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick amount chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("50", "150", "300", "500", "1000").forEach { preset ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (uiState.amountInput == preset) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { onAmountChange(preset) }
                .testTag("amount_preset_$preset")
            ) {
              Text(
                text = "$$preset",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (uiState.amountInput == preset) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transparent Zero-Fee Breakdown Box
        ZeroFeeBreakdownCard(
          senderCurrency = uiState.fromCurrency,
          estimatedSavings = estimatedSavings
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Recipient Selection / Input
        Text(
          text = "Recipient Details",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (recipients.isNotEmpty()) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(recipients) { r ->
              val isSelected = uiState.recipient?.id == r.id
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) EmeraldPrimary else SlateBorder
                ),
                modifier = Modifier
                  .clickable { onSelectRecipient(r) }
                  .testTag("sheet_recipient_${r.name}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = r.name.take(1),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = r.name,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${r.country} (${r.currencyCode})",
                      fontSize = 10.sp,
                      color = SlateLight
                    )
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Custom recipient fields if no preset selected or modifying
        OutlinedTextField(
          value = uiState.recipientName,
          onValueChange = {
            onUpdateCustomRecipient(it, uiState.recipientCountry, uiState.recipientAccount, uiState.recipientBank)
          },
          label = { Text("Recipient Full Name") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recipient_name_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = SlateBorder
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = uiState.recipientCountry,
            onValueChange = {
              onUpdateCustomRecipient(uiState.recipientName, it, uiState.recipientAccount, uiState.recipientBank)
            },
            label = { Text("Country") },
            modifier = Modifier
              .weight(1f)
              .testTag("recipient_country_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = SlateBorder
            ),
            singleLine = true
          )

          OutlinedTextField(
            value = uiState.recipientAccount,
            onValueChange = {
              onUpdateCustomRecipient(uiState.recipientName, uiState.recipientCountry, it, uiState.recipientBank)
            },
            label = { Text("IBAN / Account #") },
            modifier = Modifier
              .weight(1.2f)
              .testTag("recipient_account_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = SlateBorder
            ),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delivery Method
        Text(
          text = "Delivery Method",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Instant Bank Wire", "Mobile Wallet", "Direct Card").forEach { method ->
            val isSelected = uiState.deliveryMethod == method
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { onUpdateDeliveryMethod(method) }
                .testTag("delivery_method_$method")
            ) {
              Text(
                text = method,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Send Button
        Button(
          onClick = onExecuteTransfer,
          enabled = !uiState.isSending && amountDouble > 0 && uiState.recipientName.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("confirm_send_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            disabledContainerColor = EmeraldPrimary.copy(alpha = 0.4f)
          )
        ) {
          if (uiState.isSending) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Processing 0-Fee Transfer...", fontWeight = FontWeight.Bold)
          } else {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Send $${String.format("%.2f", amountDouble)} (0 Fee)",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun CurrencyInputBlock(
  label: String,
  amount: String,
  currencyCode: String,
  flag: String,
  onAmountChange: (String) -> Unit,
  onCurrencyChange: (String) -> Unit,
  testTagPrefix: String
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = label, fontSize = 11.sp, color = SlateLight, fontWeight = FontWeight.Medium)
        OutlinedTextField(
          value = amount,
          onValueChange = onAmountChange,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_input"),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
          ),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }

      // Currency selector dropdown button
      Box {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
          modifier = Modifier
            .clickable { menuExpanded = true }
            .testTag("${testTagPrefix}_currency_dropdown")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currencyCode,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          CurrencyCatalog.supportedCurrencies.forEach { curr ->
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = curr.flagEmoji, fontSize = 16.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = "${curr.code} - ${curr.name}", fontSize = 13.sp)
                }
              },
              onClick = {
                onCurrencyChange(curr.code)
                menuExpanded = false
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun CurrencyOutputBlock(
  label: String,
  calculatedAmount: Double,
  currencyCode: String,
  flag: String,
  symbol: String,
  onCurrencyChange: (String) -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = label, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "$symbol${String.format("%,.2f", calculatedAmount)}",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("calculated_recipient_amount")
        )
      }

      Box {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
          modifier = Modifier
            .clickable { menuExpanded = true }
            .testTag("send_to_currency_dropdown")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currencyCode,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          CurrencyCatalog.supportedCurrencies.forEach { curr ->
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = curr.flagEmoji, fontSize = 16.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = "${curr.code} - ${curr.name}", fontSize = 13.sp)
                }
              },
              onClick = {
                onCurrencyChange(curr.code)
                menuExpanded = false
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ZeroFeeBreakdownCard(
  senderCurrency: String,
  estimatedSavings: Double
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MintLight,
    border = androidx.compose.foundation.BorderStroke(1.dp, MintAccent.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth().testTag("zero_fee_breakdown_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Zero-Fee Guarantee",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark
          )
        }
        Text(
          text = "⚡ Instant Delivery",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = EmeraldPrimary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Transfer fee", fontSize = 12.sp, color = SlateDark)
        Text(
          text = "$0.00 (FREE)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Exchange rate markup", fontSize = 12.sp, color = SlateDark)
        Text(
          text = "0.0% (Real mid-market)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Traditional bank cost",
          fontSize = 12.sp,
          color = SlateLight,
          textDecoration = TextDecoration.LineThrough
        )
        Text(
          text = "~$$senderCurrency ${String.format("%.2f", estimatedSavings)}",
          fontSize = 12.sp,
          color = SlateLight,
          textDecoration = TextDecoration.LineThrough
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "You save on this transfer:",
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "$${String.format("%.2f", estimatedSavings)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ZeroFeeGold
          )
        }
      }
    }
  }
}

@Composable
fun TransferSuccessView(
  transfer: com.example.data.TransferEntity,
  onViewReceipt: () -> Unit,
  onDone: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(EmeraldPrimary),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(44.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Transfer Sent Successfully!",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Delivered to ${transfer.recipientName} (${transfer.recipientCountry}) with $0 fees",
      fontSize = 13.sp,
      color = SlateLight,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Amount Sent", fontSize = 12.sp, color = SlateLight)
          Text(
            text = "${transfer.senderCurrency} ${String.format("%.2f", transfer.senderAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Recipient Received", fontSize = 12.sp, color = SlateLight)
          Text(
            text = "${transfer.recipientCurrency} ${String.format("%,.2f", transfer.recipientAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Transfer Fee", fontSize = 12.sp, color = SlateLight)
          Text(text = "$0.00 (Zero)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Reference Code", fontSize = 12.sp, color = SlateLight)
          Text(text = transfer.referenceCode, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onViewReceipt,
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("view_receipt_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
    ) {
      Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("View Digital Receipt", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
      onClick = onDone,
      modifier = Modifier.fillMaxWidth().testTag("transfer_done_button")
    ) {
      Text("Back to Dashboard", fontWeight = FontWeight.SemiBold, color = SlateDark)
    }
  }
}
