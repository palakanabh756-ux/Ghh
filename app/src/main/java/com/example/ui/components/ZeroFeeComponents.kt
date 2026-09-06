package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecipientEntity
import com.example.data.WalletEntity
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
fun ZeroFeePill(
  modifier: Modifier = Modifier,
  text: String = "0% FX Markup • $0 Transfer Fees"
) {
  Surface(
    modifier = modifier.testTag("zero_fee_pill"),
    shape = RoundedCornerShape(50),
    color = Color(0xFF00382E),
    border = androidx.compose.foundation.BorderStroke(1.dp, MintAccent.copy(alpha = 0.5f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Icon(
        imageVector = Icons.Default.ElectricBolt,
        contentDescription = null,
        tint = ZeroFeeGold,
        modifier = Modifier.size(14.dp)
      )
      Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MintLight
      )
    }
  }
}

@Composable
fun TotalBalanceAndSavingsCard(
  totalUsd: Double,
  totalSaved: Double,
  onSendClick: () -> Unit,
  onAddMoneyClick: () -> Unit,
  onConvertClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("total_balance_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFF004D40),
              Color(0xFF00695C),
              Color(0xFF004D40)
            )
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Total Balance (All Wallets)",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.75f),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "$${String.format("%,.2f", totalUsd)}",
              fontSize = 32.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.testTag("total_balance_amount")
            )
          }

          // Zero fee guarantee badge
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.12f),
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalAlignment = Alignment.End
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = ZeroFeeGold,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Bank Fees Saved",
                  fontSize = 10.sp,
                  color = Color.White.copy(alpha = 0.8f),
                  fontWeight = FontWeight.Medium
                )
              }
              Text(
                text = "$${String.format("%.2f", totalSaved)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ZeroFeeGold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick action buttons inside card
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            isPrimary = true,
            onClick = onSendClick,
            modifier = Modifier.weight(1f).testTag("action_send_money")
          )
          ActionButton(
            icon = Icons.Default.Add,
            label = "Add Money",
            isPrimary = false,
            onClick = onAddMoneyClick,
            modifier = Modifier.weight(1f).testTag("action_add_money")
          )
          ActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Convert",
            isPrimary = false,
            onClick = onConvertClick,
            modifier = Modifier.weight(1f).testTag("action_convert_money")
          )
        }
      }
    }
  }
}

@Composable
fun ActionButton(
  icon: ImageVector,
  label: String,
  isPrimary: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .height(46.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() },
    color = if (isPrimary) MintAccent else Color.White.copy(alpha = 0.15f),
    shape = RoundedCornerShape(14.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isPrimary) SlateDark else Color.White,
        modifier = Modifier.size(17.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isPrimary) SlateDark else Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun MultiCurrencyWalletsRow(
  wallets: List<WalletEntity>,
  onWalletClick: (WalletEntity) -> Unit,
  onAddCurrencyClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Your Global Wallets",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Multi-Currency",
        fontSize = 12.sp,
        color = MintAccent,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(wallets, key = { it.currencyCode }) { wallet ->
        val currency = CurrencyCatalog.findByCode(wallet.currencyCode)
        WalletCard(
          wallet = wallet,
          flagEmoji = currency.flagEmoji,
          symbol = currency.symbol,
          onClick = { onWalletClick(wallet) }
        )
      }

      item {
        Surface(
          modifier = Modifier
            .width(130.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onAddCurrencyClick() }
            .testTag("add_currency_wallet_button"),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Currency",
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "+ Currency",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
            Text(
              text = "0% fee to open",
              fontSize = 10.sp,
              color = SlateLight
            )
          }
        }
      }
    }
  }
}

@Composable
fun WalletCard(
  wallet: WalletEntity,
  flagEmoji: String,
  symbol: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(155.dp)
      .height(115.dp)
      .clickable { onClick() }
      .testTag("wallet_card_${wallet.currencyCode}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = flagEmoji, fontSize = 22.sp)
        Text(
          text = wallet.currencyCode,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = SlateLight
        )
      }

      Column {
        Text(
          text = "$symbol${String.format("%,.2f", wallet.balance)}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "Zero Fee Ready",
          fontSize = 10.sp,
          color = SuccessGreen,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun QuickRecipientsRow(
  recipients: List<RecipientEntity>,
  onRecipientClick: (RecipientEntity) -> Unit,
  onAddRecipientClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Quick Send Recipients",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Instant 0-Fee",
        fontSize = 12.sp,
        color = EmeraldPrimary,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clickable { onAddRecipientClick() }
            .testTag("add_recipient_avatar_button")
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Contact",
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Add New",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
          )
        }
      }

      items(recipients, key = { it.id }) { recipient ->
        val currency = CurrencyCatalog.findByCode(recipient.currencyCode)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clickable { onRecipientClick(recipient) }
            .testTag("recipient_avatar_${recipient.name}")
        ) {
          Box(
            modifier = Modifier.size(52.dp)
          ) {
            Box(
              modifier = Modifier
                .size(52.dp)
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

            Text(
              text = currency.flagEmoji,
              fontSize = 14.sp,
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.White, CircleShape)
                .padding(1.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = recipient.name.split(" ").firstOrNull() ?: recipient.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}
