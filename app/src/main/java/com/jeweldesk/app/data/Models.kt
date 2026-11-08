package com.jeweldesk.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class Metal(val label: String) { GOLD("Gold"), SILVER("Silver") }

@Serializable
enum class StockType(val label: String) { BULK("Bulk Stock"), INDIVIDUAL("Individual Stock") }

@Serializable
enum class PartyType(val label: String) { CUSTOMER("Customer"), SUPPLIER("Supplier") }

@Serializable
enum class Book(val label: String) { GST("GST"), ESTIMATE("Estimate") }

@Serializable
enum class ChargeUnit(val label: String) {
    PER_GRAM("per gram"), PERCENT("percentage"), FIXED("fixed price"), PER_PIECE("per piece")
}

@Serializable
enum class RateUnit(val label: String, val grams: Double) {
    PER_GRAM("per gram", 1.0), PER_10_GRAM("per 10 gram", 10.0), PER_KG("per kg", 1000.0)
}

enum class Kind { SALE, SALE_RETURN, PURCHASE, PURCHASE_METAL, PURCHASE_RETURN }

@Serializable
enum class InvoiceType(val title: String, val short: String, val book: Book, val kind: Kind) {
    GST_SALE("GST Sale Invoice", "GST Sale", Book.GST, Kind.SALE),
    EST_SALE("Estimate Sale", "Estimate Sale", Book.ESTIMATE, Kind.SALE),
    GST_SALE_RETURN("GST Sale Return", "GST Sale Return", Book.GST, Kind.SALE_RETURN),
    GST_PURCHASE("GST Purchase Invoice", "GST Purchase", Book.GST, Kind.PURCHASE),
    GST_PURCHASE_METAL("GST Purchase Metal", "GST Purchase Metal", Book.GST, Kind.PURCHASE_METAL),
    EST_PURCHASE("Estimate Purchase Invoice", "Estimate Purchase", Book.ESTIMATE, Kind.PURCHASE),
    EST_PURCHASE_METAL("Estimate Purchase Metal", "Estimate Purchase Metal", Book.ESTIMATE, Kind.PURCHASE_METAL),
    GST_PURCHASE_RETURN("GST Purchase Return", "GST Purchase Return", Book.GST, Kind.PURCHASE_RETURN);

    val isGst get() = book == Book.GST
    val isSaleSide get() = kind == Kind.SALE || kind == Kind.SALE_RETURN
    val partyType get() = if (isSaleSide) PartyType.CUSTOMER else PartyType.SUPPLIER

    /** +1 = money comes in to the shop, -1 = money goes out. */
    val cashSign: Int
        get() = when (kind) {
            Kind.SALE, Kind.PURCHASE_RETURN -> 1
            Kind.SALE_RETURN, Kind.PURCHASE, Kind.PURCHASE_METAL -> -1
        }

    /** +1 = stock increases, -1 = stock decreases. */
    val stockSign: Int
        get() = when (kind) {
            Kind.SALE, Kind.PURCHASE_RETURN -> -1
            Kind.SALE_RETURN, Kind.PURCHASE, Kind.PURCHASE_METAL -> 1
        }
}

val PAYMENT_MODES = listOf("Cash", "UPI", "Card", "Bank Transfer", "Cheque")
fun isCashMode(mode: String) = mode.equals("Cash", ignoreCase = true)

@Serializable
data class Profile(
    val mobile: String = "",
    val ownerName: String = "",
    val businessName: String = "",
    val gstin: String = "",
    val address: String = "",
    val pincode: String = "",
    val city: String = "",
    val state: String = "",
    val businessType: String = "Retailer (B2C)",
    val dealsGold: Boolean = true,
    val dealsSilver: Boolean = true,
    val email: String = "",
    val pan: String = "",
)

/** All rates are stored per gram. */
@Serializable
data class Rates(
    val gold24: Double = 0.0,
    val gold22: Double = 0.0,
    val gold18: Double = 0.0,
    val silver999: Double = 0.0,
    val silver925: Double = 0.0,
    val updatedAt: Long = 0,
)

@Serializable
data class Category(val id: String, val name: String, val metal: Metal)

@Serializable
data class SellingCharges(
    val making: Double = 0.0,
    val makingUnit: ChargeUnit = ChargeUnit.PER_GRAM,
    val wastagePct: Double = 0.0,
    val hallmark: Double = 0.0,
    val hallmarkUnit: ChargeUnit = ChargeUnit.PER_PIECE,
    val stone: Double = 0.0,
)

@Serializable
data class Product(
    val id: String,
    val code: String,
    val metal: Metal,
    val type: StockType,
    val categoryId: String = "",
    val name: String,
    val tunch: String = "",
    val purity: String = "",
    val hsn: String = "7113",
    val size: String = "",
    val pcs: Int = 1,
    /** For bulk stock this is the current stock weight. */
    val grossWt: Double = 0.0,
    val lessWt: Double = 0.0,
    val diamondWt: Double = 0.0,
    val huids: List<String> = emptyList(),
    val imagePath: String = "",
    val charges: SellingCharges = SellingCharges(),
    val inStock: Boolean = true,
    val createdAt: Long = 0,
) {
    val netWt: Double get() = (grossWt - lessWt).coerceAtLeast(0.0)
}

@Serializable
data class MetalStock(val name: String, val metal: Metal, val direct: Double = 0.0, val indirect: Double = 0.0)

val DEFAULT_METALS = listOf(
    MetalStock("Fine Silver", Metal.SILVER),
    MetalStock("Old Silver", Metal.SILVER),
    MetalStock("Hatti", Metal.GOLD),
    MetalStock("Old Gold", Metal.GOLD),
    MetalStock("Rawa", Metal.GOLD),
)

@Serializable
data class Party(
    val id: String,
    val type: PartyType,
    val name: String,
    val mobile: String = "",
    val email: String = "",
    val gstin: String = "",
    val pan: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    /** Positive = party owes the shop (customer) / shop owes party (supplier). */
    val openingDue: Double = 0.0,
    val createdAt: Long = 0,
)

@Serializable
data class InvoiceItem(
    val productId: String = "",
    val name: String = "",
    val code: String = "",
    val metal: Metal = Metal.GOLD,
    val stockType: StockType = StockType.INDIVIDUAL,
    val purity: String = "",
    val tunch: String = "",
    val hsn: String = "7113",
    val huid: String = "",
    val size: String = "",
    val description: String = "",
    val pcs: Int = 1,
    val grossWt: Double = 0.0,
    val lessWt: Double = 0.0,
    /** Per gram. */
    val rate: Double = 0.0,
    val making: Double = 0.0,
    val makingUnit: ChargeUnit = ChargeUnit.PER_GRAM,
    val wastagePct: Double = 0.0,
    val hallmark: Double = 0.0,
    val hallmarkUnit: ChargeUnit = ChargeUnit.PER_PIECE,
    val stone: Double = 0.0,
    val diamondWt: Double = 0.0,
    /** Wholesale only: labour per gram of net weight. */
    val labour: Double = 0.0,
    val packagingWt: Double = 0.0,
) {
    val netWt: Double get() = (grossWt - lessWt - packagingWt).coerceAtLeast(0.0)
}

@Serializable
data class MetalItem(
    val stockName: String = "Old Gold",
    val metal: Metal = Metal.GOLD,
    val givenWt: Double = 0.0,
    val tunch: Double = 100.0,
    /** Per gram of fine metal. */
    val rate: Double = 0.0,
    val description: String = "",
) {
    val fineWt: Double get() = givenWt * tunch / 100.0
    val amount: Double get() = fineWt * rate
}

@Serializable
data class Payment(val amount: Double, val mode: String = "Cash", val date: Long = 0)

@Serializable
data class Invoice(
    val id: String,
    val type: InvoiceType,
    val number: Int,
    val prefix: String = "",
    val date: Long,
    val partyId: String = "",
    val partyName: String = "",
    val partyMobile: String = "",
    val partyAddress: String = "",
    val partyGstin: String = "",
    val placeOfSupply: String = "",
    val interState: Boolean = false,
    val items: List<InvoiceItem> = emptyList(),
    val metals: List<MetalItem> = emptyList(),
    val description: String = "",
    val discount: Double = 0.0,
    val discountPercent: Boolean = false,
    val roundOff: Double = 0.0,
    val gstBeforeOldMetal: Boolean = true,
    val gstRate: Double = 3.0,
    val duesIncluded: Double = 0.0,
    val advanceApplied: Double = 0.0,
    val payments: List<Payment> = emptyList(),
    val createdAt: Long = 0,
) {
    val displayNo: String get() = "$prefix$number"
}

@Serializable
enum class KhataType(val customerLabel: String, val supplierLabel: String, val dueSign: Int, val advSign: Int, val cashSign: Int) {
    DUE_ADD("Udhar Given", "Amount Payable", 1, 0, 0),
    DUE_SETTLE("Payment Received", "Payment Made", -1, 0, 1),
    ADVANCE_IN("Advance Received", "Advance Paid", 0, 1, 1),
    ADVANCE_OUT("Advance Refunded", "Advance Returned", 0, -1, -1),
}

@Serializable
data class KhataEntry(
    val id: String,
    val partyId: String,
    val date: Long,
    val type: KhataType,
    val amount: Double,
    val mode: String = "Cash",
    val book: Book = Book.ESTIMATE,
    val note: String = "",
)

@Serializable
data class Expense(
    val id: String,
    val date: Long,
    val category: String,
    val amount: Double,
    val mode: String = "Cash",
    val book: Book = Book.ESTIMATE,
    val note: String = "",
)

val EXPENSE_CATEGORIES = listOf(
    "Rent", "Salary", "Electricity", "Tea / Snacks", "Transport", "Repair", "Hallmarking",
    "Refining", "Karigar Labour", "Marketing", "Office Supplies", "Other",
)

@Serializable
data class CashEntry(
    val id: String,
    val date: Long,
    val book: Book,
    val cash: Boolean,
    val amount: Double,
    val note: String = "",
)

@Serializable
enum class InterestType(val label: String) { SIMPLE("Simple"), COMPOUND("Compound") }

@Serializable
enum class CalcMethod(val label: String) {
    MONTHLY("Monthly"),
    DAILY("Daily"),
    ONE_MONTH_PLUS_DAILY("1 Month + Daily"),
    FIFTEEN_DAYS_PLUS_DAILY("15 Days + Daily"),
}

@Serializable
data class GirviItem(
    val name: String = "",
    val metal: Metal = Metal.GOLD,
    val pcs: Int = 1,
    val grossWt: Double = 0.0,
    val lessWt: Double = 0.0,
    val description: String = "",
) {
    val netWt: Double get() = (grossWt - lessWt).coerceAtLeast(0.0)
}

@Serializable
data class GirviPayment(val date: Long, val amount: Double, val mode: String = "Cash", val note: String = "")

@Serializable
data class GirviTransfer(
    val toName: String,
    val date: Long,
    val amount: Double,
    val rate: Double,
    val note: String = "",
)

@Serializable
data class Girvi(
    val id: String,
    val number: Int,
    val date: Long,
    val partyId: String = "",
    val customerName: String,
    val mobile: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val items: List<GirviItem> = emptyList(),
    val description: String = "",
    val approxValue: Double = 0.0,
    val principal: Double,
    /** % per month. */
    val rate: Double,
    val interestType: InterestType = InterestType.SIMPLE,
    val method: CalcMethod = CalcMethod.ONE_MONTH_PLUS_DAILY,
    val mode: String = "Cash",
    val book: Book = Book.ESTIMATE,
    val payments: List<GirviPayment> = emptyList(),
    val released: Boolean = false,
    val releasedAt: Long = 0,
    val transfer: GirviTransfer? = null,
)

@Serializable
data class InvoiceSettings(
    val themeName: String = "Stylish",
    val themeColor: Long = 0xFFC8161D,
    val showStockWeights: Boolean = true,
    // header details (estimate)
    val showBusinessName: Boolean = true,
    val showLogoHeader: Boolean = true,
    val showAddress: Boolean = true,
    val showSignatureHeader: Boolean = true,
    // columns
    val colPurity: Boolean = true,
    val colGrossLess: Boolean = true,
    val colPcs: Boolean = true,
    val colSize: Boolean = false,
    val colDescription: Boolean = false,
    val colHuid: Boolean = true,
    // show on invoice
    val showEstimateTitle: Boolean = true,
    val showHallmarkLogo: Boolean = true,
    val showInvoiceDescription: Boolean = false,
    val showPaymentMode: Boolean = true,
    val showGoldRate: Boolean = false,
    val showSilverRate: Boolean = false,
    val showCustomerSignature: Boolean = true,
    // charges
    val makingCharge: Boolean = true,
    val wastageCharge: Boolean = false,
    val hallmarkCharge: Boolean = true,
    val stoneCharge: Boolean = true,
    val metalRateUnit: RateUnit = RateUnit.PER_GRAM,
    val makingUnit: ChargeUnit = ChargeUnit.PER_GRAM,
    val hallmarkUnit: ChargeUnit = ChargeUnit.PER_PIECE,
    val prefix: String = "",
    // logo / signature
    val logoEnabled: Boolean = true,
    val logoIsImage: Boolean = false,
    val companyInitial: String = "",
    val logoPath: String = "",
    val signatureEnabled: Boolean = false,
    val signaturePath: String = "",
    val tagline: String = "",
    val bisNo: String = "",
    val invoicePhone: String = "",
    val showEmail: Boolean = false,
    // bank
    val showBank: Boolean = true,
    val accountNumber: String = "",
    val bankName: String = "",
    val bankBranch: String = "",
    val ifsc: String = "",
    val upiId: String = "",
    // collect metal
    val collectOldMetal: Boolean = true,
    val showGivenWt: Boolean = true,
    val showNetWt: Boolean = true,
    val showTunch: Boolean = true,
    val showRate: Boolean = true,
    val gstBeforeOldMetal: Boolean = true,
    val declaration: String = "We declare that this invoice shows the actual price of the goods described and all particulars are true and correct.",
    val terms1: String = "Payment should be made by A/c payee only.",
    val terms2: String = "All disputes are subjected to local jurisdiction only.",
    val terms3: String = "E. & O.E.",
)

@Serializable
data class WholesaleSettings(
    val logo: Boolean = true,
    val businessName: Boolean = true,
    val contact: Boolean = true,
    val address: Boolean = true,
    val labour: Boolean = true,
    val making: Boolean = true,
    val packagingWt: Boolean = true,
    val hallmark: Boolean = true,
    val grossLess: Boolean = true,
    val pcs: Boolean = true,
    val size: Boolean = true,
    val stone: Boolean = true,
    val productDescription: Boolean = true,
    val invoiceDescription: Boolean = true,
)

@Serializable
data class ProductSettings(
    val bulk: Boolean = true,
    val individual: Boolean = true,
    val image: Boolean = true,
    val tunch: Boolean = true,
    val purity: Boolean = true,
    val hsn: Boolean = true,
    val size: Boolean = true,
    val pcs: Boolean = true,
    val openingWeight: Boolean = true,
    val sellingCharges: Boolean = true,
    val huid: Boolean = true,
)

@Serializable
data class GirviSettings(
    val pincode: Boolean = true,
    val city: Boolean = true,
    val state: Boolean = true,
    val pcs: Boolean = true,
    val grossWt: Boolean = true,
    val lessWt: Boolean = true,
    val productDescription: Boolean = true,
    val girviDescription: Boolean = true,
    val approxValue: Boolean = true,
    val activeTransfer: Boolean = true,
    val slipRate: Boolean = true,
    val slipType: Boolean = true,
    val slipMethod: Boolean = true,
    val defaultType: InterestType = InterestType.SIMPLE,
    val defaultRate: Double = 0.0,
    val defaultMethod: CalcMethod = CalcMethod.ONE_MONTH_PLUS_DAILY,
    val declaration: String = "",
    val terms1: String = "",
    val terms2: String = "",
    val terms3: String = "",
)

@Serializable
data class TagField(val key: String, val size: Int = 0)

@Serializable
data class TagLayout(
    val lengthMm: Double = 100.0,
    val breadthMm: Double = 14.6,
    val tailLengthMm: Double = 36.0,
    val tailBreadthMm: Double = 3.0,
    val tailPosition: String = "Left",
    val leftCols: Int = 2,
    val rightCols: Int = 2,
    /** Four columns: Section1-Left, Section1-Right, Section2-Left, Section2-Right. */
    val columns: List<List<TagField>> = listOf(
        listOf(TagField("productName"), TagField("productCode"), TagField("grossWt"), TagField("netWt")),
        listOf(TagField("qr")),
        listOf(TagField("purity"), TagField("huid"), TagField("category")),
        listOf(TagField("firmName"), TagField("size"), TagField("diamondWt")),
    ),
)

val DEFAULT_LABEL = TagLayout(
    lengthMm = 50.0, breadthMm = 25.0, tailLengthMm = 0.0, tailBreadthMm = 0.0, tailPosition = "None",
    leftCols = 1, rightCols = 1,
    columns = listOf(
        listOf(TagField("productName"), TagField("productCode"), TagField("grossWt"), TagField("netWt"), TagField("purity")),
        emptyList(),
        listOf(TagField("qr")),
        emptyList(),
    ),
)

@Serializable
data class Addons(val khata: Boolean = true, val expense: Boolean = true, val girvi: Boolean = true)

@Serializable
data class AppData(
    val loggedIn: Boolean = false,
    val profile: Profile? = null,
    val darkMode: Boolean = false,
    val firstMoveShown: Boolean = false,
    val rates: Rates = Rates(),
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val metalStock: List<MetalStock> = DEFAULT_METALS,
    /** Manual + GST-invoice driven weight that is accounted in GST books, per metal. */
    val gstWeight: Map<Metal, Double> = emptyMap(),
    val parties: List<Party> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val khata: List<KhataEntry> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val cashEntries: List<CashEntry> = emptyList(),
    val girvis: List<Girvi> = emptyList(),
    val counters: Map<String, Int> = emptyMap(),
    val gstInvoice: InvoiceSettings = InvoiceSettings(),
    val estInvoice: InvoiceSettings = InvoiceSettings(hallmarkUnit = ChargeUnit.PER_PIECE, signatureEnabled = true),
    val wholesale: WholesaleSettings = WholesaleSettings(),
    val productSettings: ProductSettings = ProductSettings(),
    val girviSettings: GirviSettings = GirviSettings(),
    val tag: TagLayout = TagLayout(),
    val label: TagLayout = DEFAULT_LABEL,
    val gstPasswordHash: String = "",
    val addons: Addons = Addons(),
    val language: String = "English",
) {
    fun invoiceSettings(book: Book) = if (book == Book.GST) gstInvoice else estInvoice
    fun party(id: String) = parties.firstOrNull { it.id == id }
    fun product(id: String) = products.firstOrNull { it.id == id }
    fun category(id: String) = categories.firstOrNull { it.id == id }
}

val INDIAN_STATES = listOf(
    "Andaman and Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chandigarh",
    "Chhattisgarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Goa", "Gujarat", "Haryana",
    "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Ladakh", "Lakshadweep",
    "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry",
    "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand",
    "West Bengal",
)

val BUSINESS_TYPES = listOf("Retailer (B2C)", "Wholesaler (B2B)", "Retailer + Wholesaler", "Manufacturer")
val GOLD_PURITIES = listOf("24K", "23K", "22K", "20K", "18K", "14K", "9K")
val SILVER_PURITIES = listOf("999", "925", "900", "800", "700")
val HSN_CODES = listOf("7113", "7114", "7108", "7106", "7117")
