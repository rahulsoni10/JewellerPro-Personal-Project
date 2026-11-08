package com.jeweldesk.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.AppData
import com.jeweldesk.app.data.ChargeUnit
import com.jeweldesk.app.data.GOLD_PURITIES
import com.jeweldesk.app.data.HSN_CODES
import com.jeweldesk.app.data.InvoiceType
import com.jeweldesk.app.data.Kind
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.Product
import com.jeweldesk.app.data.SILVER_PURITIES
import com.jeweldesk.app.data.SellingCharges
import com.jeweldesk.app.data.StockType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.ui.DNum
import com.jeweldesk.app.ui.DropField
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.GoldSquareButton
import com.jeweldesk.app.ui.InlineDrop
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.OutlineGoldButton
import com.jeweldesk.app.ui.PathImage
import com.jeweldesk.app.ui.RadioCard
import com.jeweldesk.app.ui.SearchBar
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.SquareIconButton
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.saveImage
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** Products that make sense to pick for a given invoice kind. */
fun pickable(d: AppData, kind: Kind): List<Product> = d.products.filter { p ->
    when (kind) {
        Kind.SALE, Kind.PURCHASE_RETURN -> if (p.type == StockType.BULK) p.grossWt > 0 else p.inStock
        Kind.SALE_RETURN -> p.type == StockType.BULK || !p.inStock
        Kind.PURCHASE -> p.type == StockType.BULK || !p.inStock
        Kind.PURCHASE_METAL -> false
    }
}

fun findByCode(list: List<Product>, code: String): Product? {
    val c = code.trim()
    if (c.isEmpty()) return null
    return list.firstOrNull { it.code.equals(c, true) || it.id == c || it.huids.any { h -> h.equals(c, true) } }
}

@Composable
fun ProductPicker(type: InvoiceType, onDismiss: () -> Unit, onPick: (List<Product>) -> Unit) {
    val d = appData()
    var q by remember { mutableStateOf("") }
    var metal by remember { mutableStateOf(if (d.profile?.dealsGold == false) Metal.SILVER else Metal.GOLD) }
    var stock by remember { mutableStateOf(StockType.BULK) }
    val selected = remember { mutableStateListOf<String>() }
    var scanning by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    val all = pickable(d, type.kind)
    val list = if (q.isNotBlank()) all.filter { it.name.contains(q, true) || it.code.contains(q, true) || it.huids.any { h -> h.contains(q, true) } }
    else all.filter { it.metal == metal && it.type == stock }

    JDialog(
        "Select Products", Icons.Outlined.Diamond, onDismiss,
        footer = {
            Text("${selected.size} selected", color = jc.text)
            GoldButton("Add Selected", enabled = selected.isNotEmpty()) { onPick(selected.mapNotNull { d.product(it) }) }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchBar(q, { q = it }, "Search all products, PCS ID or HUID", Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            GoldSquareButton(Icons.Default.QrCodeScanner, 52.dp) { scanning = true }
            Spacer(Modifier.width(8.dp))
            GoldSquareButton(Icons.Default.Add, 52.dp) { adding = true }
        }
        SegTabs(listOf("Gold", "Silver"), if (metal == Metal.GOLD) 0 else 1) { metal = if (it == 0) Metal.GOLD else Metal.SILVER }
        SegTabs(listOf("Bulk Stock", "Individual Stock"), stock.ordinal) { stock = StockType.entries[it] }
        if (list.isEmpty()) Text("No products found", color = jc.sub)
        list.forEach { p ->
            val sel = p.id in selected
            JCard(bg = if (sel) jc.cream else jc.surface, onClick = { if (sel) selected.remove(p.id) else selected.add(p.id) }) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(sel, { if (it) selected.add(p.id) else selected.remove(p.id) }, colors = CheckboxDefaults.colors(checkedColor = Gold.Main, checkmarkColor = Color.Black))
                    if (p.imagePath.isNotBlank()) PathImage(p.imagePath, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Medium, color = jc.text)
                        Text(
                            "${p.code} • ${p.purity.ifBlank { p.metal.label }}" + (if (p.huids.isNotEmpty()) " • HUID ${p.huids.joinToString()}" else ""),
                            color = jc.sub, fontSize = 12.sp,
                        )
                    }
                    Text(if (p.type == StockType.BULK) p.grossWt.wt() else p.netWt.wt(), color = Gold.Text, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    if (scanning) ScanDialog(all, selected.size, { scanning = false }) { p -> if (p.id !in selected) selected.add(p.id) }
    if (adding) ProductDialog(null, metal, forPurchase = type.kind == Kind.PURCHASE, onDismiss = { adding = false }) { p ->
        adding = false
        if (p.id !in selected) selected.add(p.id)
    }
}

@Composable
fun ScanDialog(candidates: List<Product>, count: Int, onDismiss: () -> Unit, onFound: (Product) -> Unit) {
    val ctx = LocalContext.current
    var code by remember { mutableStateOf("") }
    var flash by remember { mutableStateOf(false) }
    fun lookup(c: String) {
        val p = findByCode(candidates, c)
        if (p == null) ctx.toast("No matching product for \"$c\"") else { onFound(p); ctx.toast("Added ${p.name}"); code = "" }
    }
    val launcher = rememberLauncherForActivityResult(ScanContract()) { r -> r.contents?.let { lookup(it) } }
    JDialog("Scan or enter code", Icons.Default.QrCodeScanner, onDismiss, headerActions = {
        SquareIconButton(if (flash) Icons.Default.Check else Icons.Default.CameraAlt, 44.dp) { flash = !flash }
    }) {
        Box(
            Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).clickable {
                launcher.launch(
                    ScanOptions().setPrompt("Scan a tag or label").setBeepEnabled(true).setOrientationLocked(false).setTorchEnabled(flash),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(140.dp).border(3.dp, Gold.Main, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                    Text("Tap to scan", color = Color.White, fontSize = 13.sp)
                    Text(if (flash) "Flash on" else "Flash off", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            JField(code, { code = it }, "Product / piece code", Modifier.weight(1f), Icons.Default.Tag)
            Spacer(Modifier.width(8.dp))
            SquareIconButton(Icons.Default.Search) { lookup(code) }
        }
        Text("Scan a tag or label, or enter its code", color = jc.sub, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        GoldButton("$count selected · Done", leading = Icons.Default.Check, icon = null) { onDismiss() }
    }
}

private val TUNCH_GOLD = listOf("99.9", "99.5", "91.6", "84", "75", "58.5", "37.5")
private val TUNCH_SILVER = listOf("99.9", "92.5", "90", "80", "70", "60")

fun tunchForPurity(p: String): String = when (p.uppercase()) {
    "24K" -> "99.9"; "23K" -> "95.8"; "22K" -> "91.6"; "20K" -> "83.3"; "18K" -> "75"; "14K" -> "58.5"; "9K" -> "37.5"
    "999" -> "99.9"; "925" -> "92.5"; "900" -> "90"; "800" -> "80"; "700" -> "70"; else -> ""
}

@Composable
fun ProductDialog(existing: Product?, defaultMetal: Metal, forPurchase: Boolean = false, defaultCategory: String = "", onDismiss: () -> Unit, onSaved: (Product) -> Unit) {
    val ctx = LocalContext.current
    val d = appData()
    val ps = d.productSettings
    var type by remember { mutableStateOf(existing?.type ?: if (ps.bulk) StockType.BULK else StockType.INDIVIDUAL) }
    var metal by remember { mutableStateOf(existing?.metal ?: defaultMetal) }
    var catId by remember { mutableStateOf(existing?.categoryId ?: defaultCategory) }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var tunch by remember { mutableStateOf(existing?.tunch ?: "") }
    var purity by remember { mutableStateOf(existing?.purity ?: "") }
    var hsn by remember { mutableStateOf(existing?.hsn ?: "7113") }
    var size by remember { mutableStateOf(existing?.size ?: "") }
    var pcs by remember { mutableStateOf((existing?.pcs ?: 1).toDouble()) }
    var gross by remember { mutableStateOf(existing?.grossWt ?: 0.0) }
    var less by remember { mutableStateOf(existing?.lessWt ?: 0.0) }
    var diamond by remember { mutableStateOf(existing?.diamondWt ?: 0.0) }
    var wtUnit by remember { mutableStateOf("g") }
    val huids = remember { mutableStateListOf<String>().apply { addAll(existing?.huids ?: emptyList()) } }
    var image by remember { mutableStateOf(existing?.imagePath ?: "") }
    var charges by remember { mutableStateOf(existing?.charges ?: SellingCharges(makingUnit = d.estInvoice.makingUnit, hallmarkUnit = d.estInvoice.hallmarkUnit)) }
    var showCharges by remember { mutableStateOf(existing != null && existing.charges != SellingCharges()) }
    var addCat by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { saveImage(ctx, it)?.let { p -> image = p } } }
    val cats = d.categories.filter { it.metal == metal }
    val bulk = type == StockType.BULK
    val pcsInt = pcs.toInt().coerceAtLeast(1)

    JDialog(if (existing == null) "Add Product" else "Edit Product", Icons.Outlined.Diamond, onDismiss, headerActions = {
        SquareIconButton(Icons.Default.Info, 44.dp) { info = !info }
    }) {
        if (info) Text(
            "Bulk Stock: items sold by weight from a common pool (e.g. chains, payal). Sales reduce the total weight.\n" +
                "Individual Stock: tagged single pieces with their own weight and HUID. Selling a piece marks it as sold.",
            color = jc.sub, fontSize = 13.sp,
        )
        if (existing == null && ps.bulk && ps.individual) SegTabs(listOf("Bulk Stock", "Individual Stock"), type.ordinal) { type = StockType.entries[it] }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RadioCard("Gold", metal == Metal.GOLD, Modifier.weight(1f)) { metal = Metal.GOLD; catId = "" }
            RadioCard("Silver", metal == Metal.SILVER, Modifier.weight(1f)) { metal = Metal.SILVER; catId = "" }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DropField("Category Name", d.category(catId)?.name ?: if (cats.isEmpty()) "Categories will appear here" else "", cats, { it.name }, { catId = it.id }, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    GoldSquareButton(Icons.Default.Add, 52.dp) { addCat = true }
                }
                JField(name, { name = it }, "Product Name")
            }
            if (ps.image) Box(
                Modifier.size(width = 84.dp, height = 124.dp).clip(RoundedCornerShape(14.dp)).background(jc.cream)
                    .border(1.5.dp, Gold.Main, RoundedCornerShape(14.dp))
                    .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center,
            ) {
                if (image.isBlank()) Icon(Icons.Default.Image, null, tint = Gold.Main, modifier = Modifier.size(34.dp))
                else PathImage(image, Modifier.size(width = 84.dp, height = 124.dp))
            }
        }
        val purities = if (metal == Metal.GOLD) GOLD_PURITIES else SILVER_PURITIES
        val tunches = if (metal == Metal.GOLD) TUNCH_GOLD else TUNCH_SILVER
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (ps.purity) JField(purity, { purity = it.uppercase() }, "Purity", Modifier.weight(1f), trailing = {
                InlineDrop("", purities, { it }) { purity = it; if (tunch.isBlank()) tunch = tunchForPurity(it) }
            })
            if (ps.tunch) JField(tunch, { tunch = com.jeweldesk.app.ui.cleanNum(it) }, "Tunch", Modifier.weight(1f), trailing = {
                InlineDrop("", tunches, { it }) { tunch = it }
            })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (ps.hsn) DropField("HSN Code", hsn, HSN_CODES, { it }, { hsn = it }, Modifier.weight(1f))
            if (ps.pcs) DNum(pcs, { pcs = it }, "PCS", Modifier.weight(1f), integer = true)
        }
        if (bulk) {
            if (ps.openingWeight && !forPurchase && existing == null) DNum(gross, { gross = it }, "Total / Opening Weight", trailing = {
                InlineDrop(wtUnit, listOf("g", "mg", "kg"), { it }) { wtUnit = it }
            })
            if (existing != null) Text("Current stock: ${existing.grossWt.wt()} (adjust from the product list)", color = jc.sub, fontSize = 13.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DNum(gross, { gross = it }, "Gross Weight", Modifier.weight(1f), suffix = "g")
                DNum(less, { less = it }, "Less Weight", Modifier.weight(1f), suffix = "g")
            }
            NetBar((gross - less).coerceAtLeast(0.0))
            DNum(diamond, { diamond = it }, "Diamond / Stone Wt", suffix = "ct")
        }
        if (ps.size) JField(size, { size = it }, "Size")
        if (!bulk && ps.huid) {
            for (i in 0 until pcsInt) JField(huids.getOrElse(i) { "" }, { v ->
                while (huids.size <= i) huids.add("")
                huids[i] = v.uppercase().filter { c -> c.isLetterOrDigit() }.take(6)
            }, "PCS ${i + 1} HUID")
        }
        if (ps.sellingCharges) {
            if (!showCharges) OutlineGoldButton("Add Selling Charges", icon = Icons.Default.Add) { showCharges = true }
            else {
                Text("Selling Charges", fontWeight = FontWeight.Medium, color = jc.text)
                DNum(charges.making, { charges = charges.copy(making = it) }, "Making Charges", decimals = 2, trailing = {
                    InlineDrop(charges.makingUnit.label, ChargeUnit.entries, { it.label }) { charges = charges.copy(makingUnit = it) }
                })
                DNum(charges.wastagePct, { charges = charges.copy(wastagePct = it) }, "Wastage", suffix = "%", decimals = 2)
                DNum(charges.hallmark, { charges = charges.copy(hallmark = it) }, "Hallmark Charges", decimals = 2, trailing = {
                    InlineDrop(charges.hallmarkUnit.label, ChargeUnit.entries, { it.label }) { charges = charges.copy(hallmarkUnit = it) }
                })
                DNum(charges.stone, { charges = charges.copy(stone = it) }, "Diamond / Stone Charge (₹)", decimals = 2)
            }
        }
        GoldButton("Save Product") {
            if (name.isBlank()) { ctx.toast("Enter product name"); return@GoldButton }
            if (!bulk && gross <= 0) { ctx.toast("Enter gross weight"); return@GoldButton }
            if (less > gross) { ctx.toast("Less weight cannot exceed gross weight"); return@GoldButton }
            val hu = huids.take(pcsInt).filter { it.isNotBlank() }
            if (hu.any { it.length != 6 }) { ctx.toast("HUID must be 6 characters"); return@GoldButton }
            val clash = Store.value.products.any { p -> p.id != existing?.id && p.huids.any { it in hu } }
            if (clash) { ctx.toast("HUID already used by another product"); return@GoldButton }
            val grams = when (wtUnit) { "mg" -> gross / 1000; "kg" -> gross * 1000; else -> gross }
            val cat = d.category(catId)
            val p = Product(
                id = existing?.id ?: newId(),
                code = existing?.code ?: Ops.productCode(Store.value, metal, cat?.name ?: name),
                metal = metal, type = type, categoryId = catId, name = name.trim(), tunch = tunch, purity = purity, hsn = hsn, size = size,
                pcs = pcsInt, grossWt = if (bulk) (existing?.grossWt ?: if (forPurchase) 0.0 else grams) else gross,
                lessWt = if (bulk) 0.0 else less, diamondWt = if (bulk) 0.0 else diamond, huids = hu, imagePath = image,
                charges = if (showCharges) charges else SellingCharges(),
                inStock = existing?.inStock ?: !(forPurchase && !bulk), createdAt = existing?.createdAt ?: now(),
            )
            Ops.saveProduct(p)
            onSaved(p)
        }
    }
    if (addCat) CategoryDialog(metal, { addCat = false }) { catId = it; addCat = false }
}

@Composable
fun CategoryDialog(metal: Metal, onDismiss: () -> Unit, onAdded: (String) -> Unit) {
    var n by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    JDialog("Add ${metal.label} Category", Icons.Default.Category, onDismiss) {
        JField(n, { n = it }, "Category Name", icon = Icons.Default.Category)
        Text("e.g. Ring, Chain, Bangle, Payal", color = jc.sub, fontSize = 13.sp)
        GoldButton("Add Category") {
            if (n.isBlank()) { ctx.toast("Enter a name"); return@GoldButton }
            onAdded(Ops.addCategory(n, metal).id)
        }
    }
}
