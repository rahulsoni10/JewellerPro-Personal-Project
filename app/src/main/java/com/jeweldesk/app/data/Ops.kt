package com.jeweldesk.app.data

import kotlin.math.max

object Ops {

    fun nextNumber(d: AppData, key: String): Int = (d.counters[key] ?: 0) + 1

    private fun bump(d: AppData, key: String, used: Int): AppData =
        d.copy(counters = d.counters + (key to max(d.counters[key] ?: 0, used)))

    // ----- invoices -----

    /** Applies (sign = +1) or reverses (sign = -1) the stock side-effects of an invoice. */
    private fun applyEffects(d: AppData, inv: Invoice, sign: Int): AppData {
        var products = d.products
        var gstWeight = d.gstWeight
        val dir = inv.type.stockSign * sign
        if (inv.type.kind != Kind.PURCHASE_METAL) {
            inv.items.forEach { item ->
                if (item.productId.isNotBlank()) {
                    products = products.map { p ->
                        if (p.id != item.productId) p
                        else if (p.type == StockType.BULK) p.copy(
                            grossWt = max(0.0, p.grossWt + dir * item.grossWt),
                            pcs = max(0, p.pcs + dir * item.pcs),
                        )
                        else p.copy(inStock = dir > 0)
                    }
                }
                if (inv.type.isGst) {
                    val cur = gstWeight[item.metal] ?: 0.0
                    gstWeight = gstWeight + (item.metal to max(0.0, cur + dir * item.netWt))
                }
            }
        }
        var metals = d.metalStock
        inv.metals.forEach { m ->
            val exists = metals.any { it.name == m.stockName }
            if (!exists) metals = metals + MetalStock(m.stockName, m.metal)
            metals = metals.map { s ->
                if (s.name != m.stockName) s
                else if (inv.type.kind == Kind.PURCHASE_METAL) s.copy(direct = s.direct + sign * m.fineWt)
                else s.copy(indirect = s.indirect + sign * m.fineWt)
            }
        }
        return d.copy(products = products, gstWeight = gstWeight, metalStock = metals)
    }

    fun saveInvoice(inv: Invoice) {
        Store.update { d0 ->
            val old = d0.invoices.firstOrNull { it.id == inv.id }
            var d = if (old != null) applyEffects(d0, old, -1) else d0
            d = applyEffects(d, inv, +1)
            d = d.copy(invoices = if (old != null) d.invoices.map { if (it.id == inv.id) inv else it } else d.invoices + inv)
            bump(d, inv.type.name, inv.number)
        }
    }

    fun deleteInvoice(id: String) {
        Store.update { d0 ->
            val old = d0.invoices.firstOrNull { it.id == id } ?: return@update d0
            val d = applyEffects(d0, old, -1)
            d.copy(invoices = d.invoices.filterNot { it.id == id })
        }
    }

    // ----- products -----

    fun productCode(d: AppData, metal: Metal, categoryName: String): String {
        val prefix = (if (metal == Metal.GOLD) "G" else "S") + (categoryName.firstOrNull()?.uppercaseChar() ?: 'X')
        val n = 100 + nextNumber(d, "product")
        return "$prefix-$n"
    }

    fun saveProduct(p: Product) {
        Store.update { d ->
            val exists = d.products.any { it.id == p.id }
            val nd = d.copy(products = if (exists) d.products.map { if (it.id == p.id) p else it } else d.products + p)
            if (exists) nd else bump(nd, "product", nextNumber(d, "product"))
        }
    }

    fun deleteProduct(id: String) = Store.update { d -> d.copy(products = d.products.filterNot { it.id == id }) }

    fun addCategory(name: String, metal: Metal): Category {
        val existing = Store.value.categories.firstOrNull { it.name.equals(name.trim(), true) && it.metal == metal }
        if (existing != null) return existing
        val c = Category(newId(), name.trim(), metal)
        Store.update { it.copy(categories = it.categories + c) }
        return c
    }

    fun deleteCategory(id: String) = Store.update { d ->
        d.copy(categories = d.categories.filterNot { it.id == id }, products = d.products.map { if (it.categoryId == id) it.copy(categoryId = "") else it })
    }

    fun adjustMetal(name: String, delta: Double) = Store.update { d ->
        d.copy(metalStock = d.metalStock.map { if (it.name == name) it.copy(direct = it.direct + delta) else it })
    }

    fun setGstWeight(metal: Metal, value: Double) = Store.update { d -> d.copy(gstWeight = d.gstWeight + (metal to value)) }

    // ----- parties -----

    fun saveParty(p: Party) = Store.update { d ->
        val exists = d.parties.any { it.id == p.id }
        d.copy(parties = if (exists) d.parties.map { if (it.id == p.id) p else it } else d.parties + p)
    }

    fun deleteParty(id: String) = Store.update { d ->
        d.copy(parties = d.parties.filterNot { it.id == id }, khata = d.khata.filterNot { it.partyId == id })
    }

    // ----- khata / expenses / cash -----

    fun addKhata(e: KhataEntry) = Store.update { it.copy(khata = it.khata + e) }
    fun deleteKhata(id: String) = Store.update { d -> d.copy(khata = d.khata.filterNot { it.id == id }) }

    fun saveExpense(e: Expense) = Store.update { d ->
        val exists = d.expenses.any { it.id == e.id }
        d.copy(expenses = if (exists) d.expenses.map { if (it.id == e.id) e else it } else d.expenses + e)
    }

    fun deleteExpense(id: String) = Store.update { d -> d.copy(expenses = d.expenses.filterNot { it.id == id }) }

    fun addCashEntry(c: CashEntry) = Store.update { it.copy(cashEntries = it.cashEntries + c) }
    fun deleteCashEntry(id: String) = Store.update { d -> d.copy(cashEntries = d.cashEntries.filterNot { it.id == id }) }

    // ----- girvi -----

    fun saveGirvi(g: Girvi) = Store.update { d ->
        val exists = d.girvis.any { it.id == g.id }
        val nd = d.copy(girvis = if (exists) d.girvis.map { if (it.id == g.id) g else it } else d.girvis + g)
        bump(nd, "girvi", g.number)
    }

    fun deleteGirvi(id: String) = Store.update { d -> d.copy(girvis = d.girvis.filterNot { it.id == id }) }

    // ----- account -----

    fun logout() = Store.update { it.copy(loggedIn = false) }

    fun deleteAccount() = Store.replaceAll(AppData())
}
