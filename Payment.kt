package in.vikas.hisabkitab

data class Payment(
    val id: String,
    val label: String,
    val amount: Double,
    val date: Long,
    val details: String,
    val direction: String
)
