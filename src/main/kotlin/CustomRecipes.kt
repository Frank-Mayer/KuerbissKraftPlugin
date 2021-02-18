package main

import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.Plugin

class CustomRecipes(private val plugin: Plugin) {
    init {
        addAncientDebrisCook()
        addBlackstoneBlastFurnaceRecipe()
        addNetheriteHelmetRecipe()
        addNetheriteChestplateRecipe()
        addNetheriteLeggingsRecipe()
        addNetheriteBootsRecipe()
        addNetheriteSwordRecipe()
        addShinyNetheriteSwordRecipe()
        addNetheriteAxeRecipe()
        addShinyNetheriteAxeRecipe()
        addNetheritePickaxeRecipe()
        addShinyNetheritePickaxeRecipe()
        addNetheriteHoeRecipe()
        addShinyNetheriteHoeRecipe()
        addNetheriteShovelRecipe()
        addShinyNetheriteShovelRecipe()
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

    private fun addNetheriteHelmetRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteHelmetCraft"), ItemStack(Material.NETHERITE_HELMET))
        shapedRecipe.shape("iii", "i i", "   ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteChestplateRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteChestplateCraft"), ItemStack(Material.NETHERITE_CHESTPLATE))
        shapedRecipe.shape("i i", "iii", "iii")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteLeggingsRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteLeggingsCraft"), ItemStack(Material.NETHERITE_LEGGINGS))
        shapedRecipe.shape("iii", "i i", "i i")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteBootsRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteBootsCraft"), ItemStack(Material.NETHERITE_BOOTS))
        shapedRecipe.shape("   ", "i i", "i i")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteSwordRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteSwordCraft"), ItemStack(Material.NETHERITE_SWORD))
        shapedRecipe.shape(" i ", " i ", " s ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('s', Material.STICK)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addShinyNetheriteSwordRecipe() {
        val item = ItemStack(Material.NETHERITE_SWORD)
        item.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 2)
        item.addEnchantment(Enchantment.FIRE_ASPECT, 1)
        item.addEnchantment(Enchantment.DURABILITY, 3)
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "ShinyNetheriteHelmetCraft"), item)
        shapedRecipe.shape(" i ", " i ", " r ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('r', Material.BLAZE_ROD)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheritePickaxeRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheritePickaxeCraft"), ItemStack(Material.NETHERITE_PICKAXE))
        shapedRecipe.shape("iii", " s ", " s ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('s', Material.STICK)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addShinyNetheritePickaxeRecipe() {
        val item = ItemStack(Material.NETHERITE_PICKAXE)
        item.addEnchantment(Enchantment.DURABILITY, 3)
        item.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1)
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "ShinyNetheritePickaxeCraft"), item)
        shapedRecipe.shape("iii", " r ", " r ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('r', Material.BLAZE_ROD)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteShovelRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteShovelCraft"), ItemStack(Material.NETHERITE_SHOVEL))
        shapedRecipe.shape(" i ", " s ", " s ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('s', Material.STICK)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addShinyNetheriteShovelRecipe() {
        val item = ItemStack(Material.NETHERITE_SHOVEL)
        item.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1)
        item.addEnchantment(Enchantment.DURABILITY, 2)
        item.addEnchantment(Enchantment.DIG_SPEED, 2)
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "ShinyNetheriteShovelCraft"), item)
        shapedRecipe.shape(" i ", " r ", " r ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('r', Material.BLAZE_ROD)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteAxeRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteAxeCraft"), ItemStack(Material.NETHERITE_AXE))
        shapedRecipe.shape("ii ", "is ", " s ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('s', Material.STICK)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addShinyNetheriteAxeRecipe() {
        val item = ItemStack(Material.NETHERITE_AXE)
        item.addEnchantment(Enchantment.DURABILITY, 2)
        item.addEnchantment(Enchantment.DIG_SPEED, 1)
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "ShinyNetheriteAxeCraft"), item)
        shapedRecipe.shape("ii ", "ir ", " r ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('r', Material.BLAZE_ROD)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addNetheriteHoeRecipe() {
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "NetheriteHoeCraft"), ItemStack(Material.NETHERITE_HOE))
        shapedRecipe.shape("ii ", " s ", " s ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('s', Material.STICK)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }

    private fun addShinyNetheriteHoeRecipe() {
        val item = ItemStack(Material.NETHERITE_HOE)
        item.addEnchantment(Enchantment.DURABILITY, 1)
        val shapedRecipe = ShapedRecipe(NamespacedKey(plugin, "ShinyNetheriteHoeCraft"), item)
        shapedRecipe.shape("ii ", " r ", " r ")
        shapedRecipe.setIngredient('i', Material.NETHERITE_INGOT)
        shapedRecipe.setIngredient('r', Material.BLAZE_ROD)
        shapedRecipe.setIngredient(' ', Material.AIR)
        getServer().addRecipe(shapedRecipe)
    }
}
