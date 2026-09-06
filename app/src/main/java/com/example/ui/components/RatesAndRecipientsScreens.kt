package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ZeroFeeGold

@Composable
fun RatesCompareView(
  onSendWithCurrency: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var testAmount by remember { mutableStateOf("500") }

  val amount = testAmount.toDoubleOrNull() ?: 500.0

  val filteredCurrencies = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      CurrencyCatalog.supportedCurrencies
    } else {
      CurrencyCatalog.supportedCurrencies.filter {
        it.code.contains(searchQuery, ignoreCase = true) ||
          it.name.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Zero-Fee Rates & Comparison",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Real mid-market exchange rates updated in real-time. No hidden markups, zero transfer fees.",
        fontSize = 12.sp,
        color = SlateLight
      )
    }

    // Comparison highlight card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("fee_comparison_table_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MintAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Fee Comparison on $${String.format("%.0f", amount)} Transfer",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Surface(shape = RoundedCornerShape(50), color = MintAccent) {
              Text(
                text = "100% Free",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Table Header
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Provider", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1.2f))
            Text(text = "Fee", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(0.8f))
            Text(text = "FX Margin", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
            Text(text = "Total Cost", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
          }

          Spacer(modifier = Modifier.height(6.dp))

          // ZeroPay row (Highlighted)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "⚡ ZeroPay", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MintAccent, modifier = Modifier.weight(1.2f))
              Text(text = "$0.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.8f))
              Text(text = "0.0%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
              Text(text = "$0.00 (FREE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZeroFeeGold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Traditional Bank row
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Traditional Banks", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1.2f))
            Text(text = "$25.00", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(0.8f))
            Text(text = "+3.8%", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            Text(text = "~$44.00", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f), textAlign = TextAlign.End, textDecoration = TextDecoration.LineThrough)
          }

          // Legacy Wire Services row
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Wire Agencies", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1.2f))
            Text(text = "$15.00", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(0.8f))
            Text(text = "+2.5%", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            Text(text = "~$27.50", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f), textAlign = TextAlign.End, textDecoration = TextDecoration.LineThrough)
          }
        }
      }
    }

    // Search bar for currencies
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SlateLight)
        },
        placeholder = { Text("Search currency or country...") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_currencies_input"),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          unfocusedBorderColor = SlateBorder
        ),
        singleLine = true
      )
    }

    // List of Live Currencies & Mid-Market Rates
    items(filteredCurrencies) { curr ->
      if (curr.code != "USD") {
        val rateFromUsd = 1.0 / curr.rateToUsd
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rate_item_${curr.code}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = curr.flagEmoji, fontSize = 28.sp)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "${curr.code} - ${curr.name}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Mid-market: 1 USD = ${String.format("%.4f", rateFromUsd)} ${curr.code}",
                  fontSize = 11.sp,
                  color = SlateLight
                )
              }
            }

            Button(
              onClick = { onSendWithCurrency(curr.code) },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("send_button_${curr.code}")
            ) {
              Text(text = "Send 0-Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun RecipientsView(
  recipients: List<RecipientEntity>,
  onRecipientClick: (RecipientEntity) -> Unit,
  onAddRecipientClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filtered = remember(searchQuery, recipients) {
    if (searchQuery.isBlank()) recipients
    else recipients.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true) ||
        it.currencyCode.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Saved Recipients",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Instant 0-fee global beneficiary accounts",
          fontSize = 12.sp,
          color = SlateLight
        )
      }

      Button(
        onClick = onAddRecipientClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("add_recipient_top_button")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SlateLight)
      },
      placeholder = { Text("Search recipients by name, country...") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search_recipients_input"),
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmeraldPrimary,
        unfocusedBorderColor = SlateBorder
      ),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filtered, key = { it.id }) { recipient ->
        val currency = CurrencyCatalog.findByCode(recipient.currencyCode)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecipientClick(recipient) }
            .testTag("recipient_list_item_${recipient.name}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(
                    try {
                      Color(android.graphics.Color.parseColor(recipient.avatarColorHex))
                    } catch (e: Exception) {
                      EmeraldPrimary
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = recipient.name.take(1).uppercase(),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = recipient.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = currency.flagEmoji, fontSize = 14.sp)
                }
                Text(
                  text = "${recipient.country} • ${recipient.bankOrProvider}",
                  fontSize = 11.sp,
                  color = SlateLight
                )
                Text(
                  text = "${recipient.accountNumber} (${recipient.currencyCode})",
                  fontSize = 11.sp,
                  color = SlateLight
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MintLight,
              modifier = Modifier.clickable { onRecipientClick(recipient) }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = "Send",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Send",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
