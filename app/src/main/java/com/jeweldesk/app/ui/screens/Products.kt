package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.MetalStock
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.Product
import com.jeweldesk.app.data.StockType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.gstStockWeight
import com.jeweldesk.app.data.stockProducts
import com.jeweldesk.app.data.totalStockWeight
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.data.wtShort
import com.jeweldesk.app.pdf.Pdf
import com.jeweldesk.app.ui.ConfirmDialog
import com.jeweldesk.app.ui.DNum
import com.jeweldesk.app.ui.EmptyState
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.GoldFab
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.PathImage
import com.jeweldesk.app.ui.SearchBar
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.SquareIconButton
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast

@Composable
fun ProductsTab() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val gate = rememberGstGate()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var q by rememberSaveable { mutableStateOf("") }
    var add by remember { mutableStateOf(false) }
    var view by remember { mutableStateOf(false) }
    var gstEdit by remember { mutableStateOf(false) }
    var adjustMetal by remember { mutableStateOf<MetalStock?>(null) }
    var newMetal by remember { mutableStateOf(false) }
    val metal = if (tab == 1) Metal.SILVER else Metal.GOLD

    Page(
        header = {
            HeaderCard("Products", "Gold, silver & metals", Icons.Outlined.Diamond, actions = {
                SquareIconButton(Icons.Default.Refresh) { ctx.toast("Up to date") }
            })
        },
        navPadding = false,
        fab = if (tab < 2) ({ GoldFab("Add Product", Icons.Default.Add) { add = true } }) else null,
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SegTabs(listOf("Gold", "Silver", "Metals"), tab) { tab = it; q = "" } }
            if (tab < 2) {
                item {
                    JCard {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row { Text("Total Wt. ", color = Gold.Green, fontWeight = FontWeight.Medium); Text(d.totalStockWeight(metal).wtShort(), fontWeight = FontWeight.Bold, color = jc.text) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("GST Total Wt. ", color = Gold.Red, fontWeight = FontWeight.Medium)
                                    Text(d.gstStockWeight(metal).wtShort(), fontWeight = FontWeight.Bold, color = jc.text)
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier.size(30.dp).clip(CircleShape).background(jc.tile).border(1.dp, Gold.Main, CircleShape).clickable { gate { gstEdit = true } },
                                        contentAlignment = Alignment.Center,
                                    ) { Icon(Icons.Outlined.Edit, null, tint = Gold.Text, modifier = Modifier.size(16.dp)) }
                                }
                            }
                            Row(Modifier.clickable { view = true }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, null, tint = Gold.Text)
                                Spacer(Modifier.width(4.dp))
                                Text("View", color = Gold.Text, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                item { SearchBar(q, { q = it }, "Search Category") }
                val prods = d.products.filter { it.metal == metal }
                val cats = d.categories.filter { it.metal == metal && it.name.contains(q, true) }
                val rows = cats.map { Triple(it.id, it.name, prods.filter { p -> p.categoryId == it.id }) } +
                    (prods.filter { p -> p.categoryId.isBlank() || d.category(p.categoryId) == null }.takeIf { it.isNotEmpty() && "uncategorised".contains(q, true) }
                        ?.let { listOf(Triple("none", "Uncategorised", it)) } ?: emptyList())
                if (rows.isEmpty()) item { EmptyState(Icons.Default.Layers, "No product categories found") }
                items(rows, key = { it.first }) { (id, name, list) ->
                    val stock = list.filter { it.inStock && (it.type == StockType.INDIVIDUAL || it.grossWt > 0) }
                    JCard(onClick = { nav.navigate("prodcat/${metal.name}/$id") }) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(jc.tile), contentAlignment = Alignment.Center) {
                                Text(name.take(1).uppercase(), color = Gold.Text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold, color = jc.text)
                                Text("${stock.size} items in stock • ${list.size} total", color = jc.sub, fontSize = 13.sp)
                            }
                            Text(stock.sumOf { it.netWt }.wtShort(), color = Gold.Text, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                item { SearchBar(q, { q = it }, "Search Metal") }
                listOf(Metal.SILVER, Metal.GOLD).forEach { m ->
                    val ms = d.metalStock.filter { it.metal == m && it.name.contains(q, true) }
                    if (ms.isNotEmpty()) {
                        item { Text(m.label, color = Gold.Text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                        items(ms, key = { "${m.name}_${it.name}" }) { s ->
                            JCard(onClick = { adjustMetal = s }) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(s.name, fontSize = 17.sp, color = jc.text, fontWeight = FontWeight.Medium)
                                    Row { Text("Indirect Weight: ", color = Gold.Red); Text(s.indirect.wtShort(), color = jc.text) }
                                    Row { Text("Direct Weight: ", color = Gold.Text); Text(s.direct.wtShort(), color = jc.text) }
                                }
                            }
                        }
                    }
                }
                item { Text("+ Add metal type", color = Gold.Text, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { newMetal = true }.padding(8.dp)) }
            }
        }
    }

    if (add) ProductDialog(null, metal, onDismiss = { add = false }) { add = false; ctx.toast("Product saved") }
    if (view) JDialog("${metal.label} Stock", Icons.Default.Visibility, { view = false }) {
        val stock = d.stockProducts(metal)
        ValueRow("Total items", stock.size.toString(), jc.text)
        ValueRow("Gross weight", stock.sumOf { if (it.type == StockType.BULK) it.grossWt else it.grossWt }.wt())
        ValueRow("Net weight", stock.sumOf { it.netWt }.wt())
        ValueRow("GST weight", d.gstStockWeight(metal).wt())
        stock.groupBy { d.category(it.categoryId)?.name ?: "Uncategorised" }.forEach { (cat, list) ->
            ValueRow("$cat (${list.size})", list.sumOf { it.netWt }.wt(), jc.sub)
        }
    }
    if (gstEdit) {
        var v by remember { mutableStateOf(d.gstStockWeight(metal)) }
        JDialog("GST ${metal.label} Weight", Icons.Default.Scale, { gstEdit = false }) {
            Text("Weight accounted in your GST books. GST invoices update this automatically.", color = jc.sub, fontSize = 13.sp)
            DNum(v, { v = it }, "GST Total Weight", suffix = "g")
            GoldButton("Save") { Ops.setGstWeight(metal, v); gstEdit = false }
        }
    }
    adjustMetal?.let { s ->
        var delta by remember { mutableStateOf(0.0) }
        JDialog(s.name, Icons.Default.Scale, { adjustMetal = null }) {
            ValueRow("Direct Weight", s.direct.wt())
            ValueRow("Indirect Weight", s.indirect.wt())
            DNum(delta, { delta = it }, "Weight", suffix = "g")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("Add", Modifier.weight(1f), icon = null) { if (delta > 0) { Ops.adjustMetal(s.name, delta); adjustMetal = null } }
                GoldButton("Remove", Modifier.weight(1f), icon = null) { if (delta > 0) { Ops.adjustMetal(s.name, -delta); adjustMetal = null } }
            }
        }
    }
    if (newMetal) {
        var name by remember { mutableStateOf("") }
        var m by remember { mutableStateOf(Metal.GOLD) }
        JDialog("Add Metal Type", Icons.Default.Scale, { newMetal = false }) {
            SegTabs(listOf("Gold", "Silver"), m.ordinal) { m = Metal.entries[it] }
            JField(name, { name = it }, "Name (e.g. Fine Gold)")
            GoldButton("Add") {
                if (name.isBlank() || d.metalStock.any { it.name.equals(name.trim(), true) }) { ctx.toast("Enter a new unique name"); return@GoldButton }
                Store.update { it.copy(metalStock = it.metalStock + MetalStock(name.trim(), m)) }
                newMetal = false
            }
        }
    }
}

@Composable
fun CategoryProductsScreen(metal: Metal, catId: String) {
    val ctx = LocalContext.current
    val d = appData()
    val cat = d.category(catId)
    var q by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var add by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<Product?>(null) }
    var del by remember { mutableStateOf<Product?>(null) }
    var adjust by remember { mutableStateOf<Product?>(null) }
    var delCat by remember { mutableStateOf(false) }
    val list = d.products.filter { it.metal == metal && (if (catId == "none") it.categoryId.isBlank() || d.category(it.categoryId) == null else it.categoryId == catId) }
        .filter { q.isBlank() || it.name.contains(q, true) || it.code.contains(q, true) || it.huids.any { h -> h.contains(q, true) } }
        .filter {
            when (filter) {
                1 -> it.type == StockType.BULK
                2 -> it.type == StockType.INDIVIDUAL && it.inStock
                3 -> it.type == StockType.INDIVIDUAL && !it.inStock
                else -> true
            }
        }.sortedByDescending { it.createdAt }
    Page(
        header = {
            HeaderCard(cat?.name ?: "Uncategorised", "${metal.label} products", back = true, actions = {
                if (cat != null) SquareIconButton(Icons.Default.Delete, tint = Gold.Red) { delCat = true }
                SquareIconButton(Icons.Default.Print) {
                    val stock = list.filter { it.inStock }
                    if (stock.isEmpty()) ctx.toast("No products to print") else Pdf.print(ctx, Pdf.tags(ctx, d, stock, false), "Tags")
                }
            })
        },
        fab = { GoldFab("Add Product", Icons.Default.Add) { add = true } },
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SearchBar(q, { q = it }, "Search name, code or HUID") }
            item { SegTabs(listOf("All", "Bulk", "In Stock", "Sold"), filter, height = 40.dp) { filter = it } }
            if (list.isEmpty()) item { EmptyState(Icons.Outlined.Diamond, "No products found") }
            items(list, key = { it.id }) { p ->
                var menu by remember { mutableStateOf(false) }
                JCard(onClick = { edit = p }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.imagePath.isNotBlank()) PathImage(p.imagePath, Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)))
                        else Box(Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(jc.tile), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Diamond, null, tint = Gold.Main)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.SemiBold, color = jc.text)
                            Text("${p.code} • ${p.type.label}" + (if (p.purity.isNotBlank()) " • ${p.purity}" else ""), color = jc.sub, fontSize = 12.sp)
                            if (p.type == StockType.BULK) Text("Stock: ${p.grossWt.wt()} • ${p.pcs} pcs", color = Gold.Text, fontSize = 13.sp)
                            else {
                                Text("GW ${p.grossWt.wt()} • NW ${p.netWt.wt()}", color = Gold.Text, fontSize = 13.sp)
                                if (p.huids.isNotEmpty()) Text("HUID ${p.huids.joinToString()}", color = jc.sub, fontSize = 12.sp)
                            }
                            if (p.type == StockType.INDIVIDUAL && !p.inStock) Text("Sold", color = Gold.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box {
                            Icon(Icons.Default.MoreVert, null, tint = jc.text, modifier = Modifier.clip(CircleShape).clickable { menu = true }.padding(6.dp))
                            DropdownMenu(menu, { menu = false }) {
                                DropdownMenuItem({ Text("Edit") }, { menu = false; edit = p }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                                DropdownMenuItem({ Text("Print Tag") }, { menu = false; Pdf.print(ctx, Pdf.tags(ctx, d, listOf(p), false), "Tag ${p.code}") }, leadingIcon = { Icon(Icons.Default.Print, null) })
                                DropdownMenuItem({ Text("Print Label") }, { menu = false; Pdf.print(ctx, Pdf.tags(ctx, d, listOf(p), true), "Label ${p.code}") }, leadingIcon = { Icon(Icons.Default.Print, null) })
                                if (p.type == StockType.BULK) DropdownMenuItem({ Text("Adjust Stock") }, { menu = false; adjust = p }, leadingIcon = { Icon(Icons.Default.Scale, null) })
                                DropdownMenuItem({ Text("Delete", color = Gold.Red) }, { menu = false; del = p }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Gold.Red) })
                            }
                        }
                    }
                }
            }
        }
    }
    if (add) ProductDialog(null, metal, defaultCategory = if (catId == "none") "" else catId, onDismiss = { add = false }) { add = false }
    edit?.let { p -> ProductDialog(p, p.metal, onDismiss = { edit = null }) { edit = null } }
    del?.let { p ->
        val used = d.invoices.any { inv -> inv.items.any { it.productId == p.id } }
        ConfirmDialog("Delete ${p.name}?", if (used) "This product is used in invoices. Those invoices keep their copy of the item." else "This cannot be undone.", onDismiss = { del = null }) {
            Ops.deleteProduct(p.id)
        }
    }
    adjust?.let { p ->
        var delta by remember { mutableStateOf(0.0) }
        var pcs by remember { mutableStateOf(0.0) }
        JDialog("Adjust ${p.name}", Icons.Default.Scale, { adjust = null }) {
            ValueRow("Current stock", p.grossWt.wt())
            DNum(delta, { delta = it }, "Weight", suffix = "g")
            DNum(pcs, { pcs = it }, "PCS", integer = true)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("Add", Modifier.weight(1f), icon = null) {
                    Ops.saveProduct(p.copy(grossWt = p.grossWt + delta, pcs = p.pcs + pcs.toInt())); adjust = null
                }
                GoldButton("Remove", Modifier.weight(1f), icon = null) {
                    Ops.saveProduct(p.copy(grossWt = (p.grossWt - delta).coerceAtLeast(0.0), pcs = (p.pcs - pcs.toInt()).coerceAtLeast(0))); adjust = null
                }
            }
        }
    }
    if (delCat && cat != null) ConfirmDialog("Delete category ${cat.name}?", "Products in it will become uncategorised.", onDismiss = { delCat = false }) {
        Ops.deleteCategory(cat.id)
    }
}
