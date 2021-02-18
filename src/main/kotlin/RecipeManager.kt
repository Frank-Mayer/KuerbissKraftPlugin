package main

import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.Plugin

class RecipeManager(private val plugin: Plugin) {
    private val allowedRecipes = hashSetOf<String>()

    init {
        addAncientDebrisCook()
        addBlackstoneBlastFurnaceRecipe()
    }

    /**
     * @param id: PlayerId + Recipe Key
     */
    fun isUnlocked(id: String): Boolean {
        return allowedRecipes.contains(id)
    }

    /**
     * @param id: PlayerId + Recipe Key
     */
    fun unlock(id: String) {
        allowedRecipes.add(id)
    }

    private fun addAncientDebrisCook() {
        val blastingRecipe =
            BlastingRecipe(
                NamespacedKey(plugin, "BlackstoneToAncientDebris"),
                ItemStack(Material.ANCIENT_DEBRIS),
                Material.POLISHED_BLACKSTONE,
                4.0f,
                1024
            )
        getServer().addRecipe(blastingRecipe)
    }

    private fun addBlackstoneBlastFurnaceRecipe() {
        val shapedRecipe =
            ShapedRecipe(NamespacedKey(plugin, "BlackstoneBlastFurnace"), ItemStack(Material.BLAST_FURNACE))
        shapedRecipe.shape("iii", "ifi", "bbb")
        shapedRecipe.setIngredient('i', Material.IRON_INGOT)
        shapedRecipe.setIngredient('f', Material.FURNACE)
        shapedRecipe.setIngredient('b', Material.POLISHED_BLACKSTONE)
        getServer().addRecipe(shapedRecipe)
    }

}
