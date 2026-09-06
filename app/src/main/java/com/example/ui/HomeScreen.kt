package com.example.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransferEntity
import com.example.model.CurrencyCatalog
import com.example.ui.components.AddRecipientDialog
import com.example.ui.components.ConvertCurrencyDialog
import com.example.ui.components.MultiCurrencyWalletsRow
import com.example.ui.components.QuickRecipientsRow
import com.example.ui.components.RatesCompareView
import com.example.ui.components.RecipientsView
import com.example.ui.components.SendMoneySheet
import com.example.ui.components.TopUpDialog
import com.example.ui.components.TotalBalanceAndSavingsCard
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.ZeroFeePill
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ZeroFeeGold
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: PaymentViewModel,
  modifier: Modifier = Modifier
) {
  val wallets by viewModel.wallets.collectAsState()
  val recipients by viewModel.recipients.collectAsState()
  val transfers by viewModel.transfers.collectAsState()
  val totalUsd by viewModel.totalBalanceInUsd.collectAsState()
  val totalSaved by viewModel.totalSavedFees.collectAsState()

  val activeTab by viewModel.activeTab.collectAsState()
  val transactionFilter by viewModel.transactionFilter.collectAsState()
  val sendUiState by viewModel.sendUiState.collectAsState()
  val convertUiState by viewModel.convertUiState.collectAsState()
  val topUpUiState by viewModel.topUpUiState.collectAsState()
  val viewingTransfer by viewModel.selectedTransferForReceipt.collectAsState()

  var isAddRecipientDialogOpen by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collectLatest { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("main_navigation_bar")
      ) {
        NavigationBarItem(
          icon = { Icon(Icons.Default.Send, contentDescription = "Transfers") },
          label = { Text("Transfers") },
          selected = activeTab == 0,
          onClick = { viewModel.setActiveTab(0) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldDark,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MintLight
          ),
          modifier = Modifier.testTag("nav_tab_transfers")
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.People, contentDescription = "Recipients") },
          label = { Text("Recipients") },
          selected = activeTab == 1,
          onClick = { viewModel.setActiveTab(1) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldDark,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MintLight
          ),
          modifier = Modifier.testTag("nav_tab_recipients")
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.CompareArrows, contentDescription = "Rates") },
          label = { Text("0-Fee Rates") },
          selected = activeTab == 2,
          onClick = { viewModel.setActiveTab(2) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldDark,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MintLight
          ),
          modifier = Modifier.testTag("nav_tab_rates")
        )
      }
    },
    floatingActionButton = {
      if (activeTab == 0) {
        FloatingActionButton(
          onClick = { viewModel.openSendSheet() },
          containerColor = EmeraldPrimary,
          contentColor = Color.White,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("main_fab_send_money")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Money", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Top App Bar
      TopFintechHeader()

      // Tab contents
      when (activeTab) {
        0 -> HomeDashboardView(
          totalUsd = totalUsd,
          totalSaved = totalSaved,
          wallets = wallets,
          recipients = recipients,
          transfers = transfers,
          filter = transactionFilter,
          onFilterChange = { viewModel.setTransactionFilter(it) },
          onSendClick = { viewModel.openSendSheet() },
          onAddMoneyClick = { viewModel.openTopUpSheet() },
          onConvertClick = { viewModel.openConvertSheet() },
          onWalletClick = { wallet -> viewModel.openConvertSheet(wallet.currencyCode) },
          onAddCurrencyClick = { viewModel.openTopUpSheet() },
          onRecipientClick = { recipient -> viewModel.openSendSheet(prefillRecipient = recipient) },
          onAddRecipientClick = { isAddRecipientDialogOpen = true },
          onTransferClick = { transfer -> viewModel.viewTransferReceipt(transfer) }
        )

        1 -> RecipientsView(
          recipients = recipients,
          onRecipientClick = { recipient -> viewModel.openSendSheet(prefillRecipient = recipient) },
          onAddRecipientClick = { isAddRecipientDialogOpen = true }
        )

        2 -> RatesCompareView(
          onSendWithCurrency = { currencyCode ->
            viewModel.openSendSheet(prefillCurrency = "USD")
            viewModel.updateSendToCurrency(currencyCode)
          }
        )
      }
    }

    // Modal Sheets and Dialogs
    SendMoneySheet(
      uiState = sendUiState,
      recipients = recipients,
      onClose = { viewModel.closeSendSheet() },
      onAmountChange = { viewModel.updateSendAmount(it) },
      onFromCurrencyChange = { viewModel.updateSendFromCurrency(it) },
      onToCurrencyChange = { viewModel.updateSendToCurrency(it) },
      onSwapCurrencies = { viewModel.swapCurrencies() },
      onSelectRecipient = { viewModel.selectRecipient(it) },
      onUpdateCustomRecipient = { name, country, acct, bank ->
        viewModel.updateCustomRecipientDetails(name, country, acct, bank)
      },
      onUpdateDeliveryMethod = { viewModel.updateDeliveryMethod(it) },
      onExecuteTransfer = { viewModel.executeTransfer() },
      onViewReceipt = { transfer -> viewModel.viewTransferReceipt(transfer) }
    )

    ConvertCurrencyDialog(
      uiState = convertUiState,
      onDismiss = { viewModel.closeConvertSheet() },
      onAmountChange = { viewModel.updateConvertAmount(it) },
      onCurrenciesChange = { from, to -> viewModel.updateConvertCurrencies(from, to) },
      onConfirmConvert = { viewModel.executeConversion() }
    )

    TopUpDialog(
      uiState = topUpUiState,
      onDismiss = { viewModel.closeTopUpSheet() },
      onAmountChange = { viewModel.updateTopUpAmount(it) },
      onCurrencyChange = { viewModel.updateTopUpCurrency(it) },
      onConfirmTopUp = { viewModel.executeTopUp() }
    )

    AddRecipientDialog(
      isOpen = isAddRecipientDialogOpen,
      onDismiss = { isAddRecipientDialogOpen = false },
      onAddRecipient = { name, country, currency, acct, bank ->
        viewModel.addNewRecipient(name, country, currency, acct, bank)
      }
    )

    TransactionReceiptDialog(
      transfer = viewingTransfer,
      onDismiss = { viewModel.closeReceipt() }
    )
  }
}

@Composable
fun TopFintechHeader() {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth().statusBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(EmeraldPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "345 Pay",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Zero Fee Global Money",
            fontSize = 10.sp,
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      ZeroFeePill()
    }
  }
}

@Composable
fun HomeDashboardView(
  totalUsd: Double,
  totalSaved: Double,
  wallets: List<com.example.data.WalletEntity>,
  recipients: List<com.example.data.RecipientEntity>,
  transfers: List<TransferEntity>,
  filter: String,
  onFilterChange: (String) -> Unit,
  onSendClick: () -> Unit,
  onAddMoneyClick: () -> Unit,
  onConvertClick: () -> Unit,
  onWalletClick: (com.example.data.WalletEntity) -> Unit,
  onAddCurrencyClick: () -> Unit,
  onRecipientClick: (com.example.data.RecipientEntity) -> Unit,
  onAddRecipientClick: () -> Unit,
  onTransferClick: (TransferEntity) -> Unit
) {
  val filteredTransfers = remember(filter, transfers) {
    when (filter) {
      "Sent" -> transfers.filter { !it.recipientName.startsWith("Self") }
      "Exchanges" -> transfers.filter { it.recipientName.startsWith("Self") }
      else -> transfers
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Total Balance & Savings Card
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        TotalBalanceAndSavingsCard(
          totalUsd = totalUsd,
          totalSaved = totalSaved,
          onSendClick = onSendClick,
          onAddMoneyClick = onAddMoneyClick,
          onConvertClick = onConvertClick
        )
      }
    }

    // Multi-currency Wallets Carousel
    item {
      MultiCurrencyWalletsRow(
        wallets = wallets,
        onWalletClick = onWalletClick,
        onAddCurrencyClick = onAddCurrencyClick
      )
    }

    // Quick Recipients Horizontal Row
    item {
      QuickRecipientsRow(
        recipients = recipients,
        onRecipientClick = onRecipientClick,
        onAddRecipientClick = onAddRecipientClick
      )
    }

    // Transactions Header & Filters
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Transfers",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          // Filter pills
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("All", "Sent", "Exchanges").forEach { f ->
              val isSelected = filter == f
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .clickable { onFilterChange(f) }
                  .testTag("filter_tab_$f")
              ) {
                Text(
                  text = f,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    }

    // Transaction list items
    if (filteredTransfers.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = null,
              tint = SlateLight,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No transfers yet in this filter",
              fontSize = 13.sp,
              color = SlateLight
            )
          }
        }
      }
    } else {
      items(filteredTransfers, key = { it.id }) { transfer ->
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
          TransferHistoryItem(
            transfer = transfer,
            onClick = { onTransferClick(transfer) }
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp)) // Padding for FAB & nav bar
    }
  }
}

@Composable
fun TransferHistoryItem(
  transfer: TransferEntity,
  onClick: () -> Unit
) {
  val isExchange = transfer.recipientName.startsWith("Self")
  val senderCurrency = CurrencyCatalog.findByCode(transfer.senderCurrency)
  val recipCurrency = CurrencyCatalog.findByCode(transfer.recipientCurrency)
  val dateFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(transfer.timestampMillis))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("transfer_item_${transfer.referenceCode}"),
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
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isExchange) MintLight else Color(0xFFEFF6FF)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isExchange) Icons.Default.SwapHoriz else Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = if (isExchange) EmeraldPrimary else Color(0xFF2563EB),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = transfer.recipientName,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = recipCurrency.flagEmoji, fontSize = 12.sp)
          }

          Text(
            text = "$dateStr • ${transfer.deliveryMethod}",
            fontSize = 11.sp,
            color = SlateLight
          )

          // Zero fee badge
          Text(
            text = "Fee: $0.00 • Saved $${String.format("%.2f", transfer.bankSavings)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = SuccessGreen
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "-${senderCurrency.symbol}${String.format("%.2f", transfer.senderAmount)}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "+${recipCurrency.symbol}${String.format("%,.2f", transfer.recipientAmount)}",
          fontSize = 12.sp,
          color = SlateLight
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (transfer.status == "Delivered") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ) {
          Text(
            text = transfer.status,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (transfer.status == "Delivered") Color(0xFF2E7D32) else Color(0xFFE65100),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}
