package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CurrencyCatalog
import com.example.ui.ConvertUiState
import com.example.ui.TopUpUiState
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateLight

@Composable
fun ConvertCurrencyDialog(
  uiState: ConvertUiState,
  onDismiss: () -> Unit,
  onAmountChange: (String) -> Unit,
  onCurrenciesChange: (String, String) -> Unit,
  onConfirmConvert: () -> Unit
) {
  if (!uiState.isOpen) return

  val fromCurr = CurrencyCatalog.findByCode(uiState.fromCurrency)
  val toCurr = CurrencyCatalog.findByCode(uiState.toCurrency)
  val rate = remember(uiState.fromCurrency, uiState.toCurrency) {
    CurrencyCatalog.calculateExchangeRate(uiState.fromCurrency, uiState.toCurrency)
  }
  val amount = uiState.amountInput.toDoubleOrNull() ?: 0.0
  val convertedAmount = amount * rate

  var fromMenuExpanded by remember { mutableStateOf(false) }
  var toMenuExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("convert_currency_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Convert Balance",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Instant 0% markup • $0 exchange fees",
              fontSize = 11.sp,
              color = EmeraldPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // From currency
        Text(text = "From your wallet", fontSize = 11.sp, color = SlateLight)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = onAmountChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("convert_amount_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = SlateBorder
            )
          )

          Box {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { fromMenuExpanded = true }
                .padding(4.dp)
            ) {
              Text(
                text = "${fromCurr.flagEmoji} ${fromCurr.code}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
              )
            }
            DropdownMenu(expanded = fromMenuExpanded, onDismissRequest = { fromMenuExpanded = false }) {
              CurrencyCatalog.supportedCurrencies.forEach { c ->
                DropdownMenuItem(
                  text = { Text("${c.flagEmoji} ${c.code} - ${c.name}") },
                  onClick = {
                    onCurrenciesChange(c.code, uiState.toCurrency)
                    fromMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Swap icon & rate
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "1 ${uiState.fromCurrency} = ${String.format("%.4f", rate)} ${uiState.toCurrency}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlateLight
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // To currency
        Text(text = "You receive in wallet", fontSize = 11.sp, color = SlateLight)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Text(
                text = "${toCurr.symbol}${String.format("%,.2f", convertedAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Box {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { toMenuExpanded = true }
                .padding(4.dp)
            ) {
              Text(
                text = "${toCurr.flagEmoji} ${toCurr.code}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
              )
            }
            DropdownMenu(expanded = toMenuExpanded, onDismissRequest = { toMenuExpanded = false }) {
              CurrencyCatalog.supportedCurrencies.forEach { c ->
                DropdownMenuItem(
                  text = { Text("${c.flagEmoji} ${c.code} - ${c.name}") },
                  onClick = {
                    onCurrenciesChange(uiState.fromCurrency, c.code)
                    toMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Zero Fee badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MintLight,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Fee: $0.00 • Instant settlement in your balance",
              fontSize = 11.sp,
              color = EmeraldDark,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onConfirmConvert,
          enabled = !uiState.isConverting && amount > 0,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("confirm_convert_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          if (uiState.isConverting) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
          } else {
            Text("Convert Now ($0 Fee)", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun TopUpDialog(
  uiState: TopUpUiState,
  onDismiss: () -> Unit,
  onAmountChange: (String) -> Unit,
  onCurrencyChange: (String) -> Unit,
  onConfirmTopUp: () -> Unit
) {
  if (!uiState.isOpen) return

  var menuExpanded by remember { mutableStateOf(false) }
  val curr = CurrencyCatalog.findByCode(uiState.currencyCode)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("top_up_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Add Money to Balance",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "0% deposit fees on all methods",
              fontSize = 11.sp,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Medium
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Amount to deposit", fontSize = 11.sp, color = SlateLight)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = onAmountChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("top_up_amount_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = SlateBorder
            )
          )

          Box {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { menuExpanded = true }
                .padding(4.dp)
            ) {
              Text(
                text = "${curr.flagEmoji} ${curr.code}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
              )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
              CurrencyCatalog.supportedCurrencies.forEach { c ->
                DropdownMenuItem(
                  text = { Text("${c.flagEmoji} ${c.code} - ${c.name}") },
                  onClick = {
                    onCurrencyChange(c.code)
                    menuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick amount buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("100", "250", "500", "1000").forEach { preset ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (uiState.amountInput == preset) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { onAmountChange(preset) }
            ) {
              Text(
                text = "+$preset",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (uiState.amountInput == preset) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment method
        Text(text = "Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = EmeraldPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Instant Debit Card (Visa/Mastercard)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              Text(text = "Fee: $0.00 • Instant availability", fontSize = 10.sp, color = EmeraldPrimary)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onConfirmTopUp,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("confirm_top_up_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Add Money Now ($0 Fee)", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun AddRecipientDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onAddRecipient: (name: String, country: String, currency: String, account: String, bank: String) -> Unit
) {
  if (!isOpen) return

  var name by remember { mutableStateOf("") }
  var country by remember { mutableStateOf("India") }
  var currency by remember { mutableStateOf("INR") }
  var account by remember { mutableStateOf("") }
  var bank by remember { mutableStateOf("") }
  var menuExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("add_recipient_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Add Global Recipient",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("add_recipient_name_field"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.weight(1f).testTag("add_recipient_country_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
          )

          Box(modifier = Modifier.weight(0.8f)) {
            OutlinedTextField(
              value = currency,
              onValueChange = {},
              readOnly = true,
              label = { Text("Currency") },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { menuExpanded = true }
                .testTag("add_recipient_currency_field"),
              shape = RoundedCornerShape(12.dp)
            )
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
              CurrencyCatalog.supportedCurrencies.forEach { c ->
                DropdownMenuItem(
                  text = { Text("${c.flagEmoji} ${c.code}") },
                  onClick = {
                    currency = c.code
                    menuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = account,
          onValueChange = { account = it },
          label = { Text("IBAN / Account #") },
          modifier = Modifier.fillMaxWidth().testTag("add_recipient_account_field"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = bank,
          onValueChange = { bank = it },
          label = { Text("Bank / Provider Name") },
          modifier = Modifier.fillMaxWidth().testTag("add_recipient_bank_field"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onAddRecipient(name, country, currency, account.ifBlank { "Direct Wire" }, bank.ifBlank { "Bank" })
              onDismiss()
            }
          },
          enabled = name.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_recipient_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Save Recipient", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
