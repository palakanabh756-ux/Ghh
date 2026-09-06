package com.example.model

data class Currency(
  val code: String,
  val name: String,
  val symbol: String,
  val flagEmoji: String,
  val rateToUsd: Double // Mid-market exchange rate: 1 unit of this currency = X USD
)

object CurrencyCatalog {
  val supportedCurrencies = listOf(
    Currency("USD", "US Dollar", "$", "🇺🇸", 1.0),
    Currency("EUR", "Euro", "€", "🇪🇺", 1.085),
    Currency("GBP", "British Pound", "£", "🇬🇧", 1.282),
    Currency("INR", "Indian Rupee", "₹", "🇮🇳", 0.012),
    Currency("JPY", "Japanese Yen", "¥", "🇯🇵", 0.0067),
    Currency("CAD", "Canadian Dollar", "C$", "🇨🇦", 0.738),
    Currency("AUD", "Australian Dollar", "A$", "🇦🇺", 0.655),
    Currency("MXN", "Mexican Peso", "Mex$", "🇲🇽", 0.051),
    Currency("SGD", "Singapore Dollar", "S$", "🇸🇬", 0.752),
    Currency("BRL", "Brazilian Real", "R$", "🇧🇷", 0.180),
    Currency("PHP", "Philippine Peso", "₱", "🇵🇭", 0.0175)
  )

  fun findByCode(code: String): Currency {
    return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
      ?: supportedCurrencies[0]
  }

  // Calculate mid-market rate between any two currencies: from -> to
  fun calculateExchangeRate(fromCode: String, toCode: String): Double {
    val from = findByCode(fromCode)
    val to = findByCode(toCode)
    if (to.rateToUsd == 0.0) return 1.0
    return from.rateToUsd / to.rateToUsd
  }

  // Calculate traditional bank fee (e.g. $25 wire + 3.8% FX markup) vs ZeroPay ($0 fee)
  fun calculateBankSavings(amountInFromCurrency: Double, fromCode: String): Double {
    val from = findByCode(fromCode)
    val amountInUsd = amountInFromCurrency * from.rateToUsd
    val typicalWireFeeUsd = 25.00
    val typicalFxMarkupUsd = amountInUsd * 0.038
    val totalBankCostUsd = typicalWireFeeUsd + typicalFxMarkupUsd
    // Return in sender currency
    return if (from.rateToUsd > 0) totalBankCostUsd / from.rateToUsd else 35.0
  }
}
