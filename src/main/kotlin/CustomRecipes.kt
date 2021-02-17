package main

import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ItemStack

class CustomRecipes(private val nsKey: NamespacedKey) {
    init {
        addAncientDebrisCook()
        addBlackstoneBlastFurnaceRecipe()
    }

    private fun addAncientDebrisCook() {
        val ad = ItemStack(Material.ANCIENT_DEBRIS)
        val adr = BlastingRecipe(nsKey, ad, Material.POLISHED_BLACKSTONE, 4.0f, 1024)
        getServer().addRecipe(adr)
    }

    private fun addBlackstoneBlastFurnaceRecipe() {
        val bf = ItemStack(Material.BLAST_FURNACE)
        val bfr = ShapedRecipe(nsKey, bf)
        bfr.shape("iii", "ifi", "bbb")
        bfr.setIngredient('i', Material.IRON_INGOT)
        bfr.setIngredient('f', Material.FURNACE)
        bfr.setIngredient('b', Material.POLISHED_BLACKSTONE)
        getServer().addRecipe(bfr)
    }
}
