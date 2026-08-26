package com.gtohjs.machine

import com.gtolib.utils.RegistriesUtils
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.HashMap
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

/** Loads and validates the canonical GTOHJS multiblock pattern resources. */
object HyperdimensionalPatternResources {
    private val cache: MutableMap<String, List<Array<String>>> = HashMap()
    private val expectedDimensions: Map<String, IntArray> = mapOf(
        "hyperdimensional_forge" to intArrayOf(15, 43, 15),
        "hyperdimensional_steam_furnace" to intArrayOf(15, 43, 15),
        "hyperdimensional_smelter" to intArrayOf(49, 34, 39),
        "hyperdimensional_chemical_factory" to intArrayOf(49, 34, 39),
        "advanced_alchemy_cauldron" to intArrayOf(5, 3, 5),
        "large_petal_apothecary" to intArrayOf(5, 3, 5),
        "universal_steam_factory" to intArrayOf(5, 5, 5)
    )

    @JvmStatic
    @Synchronized
    fun load(name: String): List<Array<String>> {
        cache[name]?.let { return it }

        val path = "/data/gtohjs/structures/$name.pattern"
        try {
            val stream: InputStream = HyperdimensionalPatternResources::class.java.getResourceAsStream(path)
                ?: throw IllegalStateException("Missing GTOHJS multiblock pattern resource: $path")
            stream.use { input ->
                val text = String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .replace('\r', '\n')
                val aisles = mutableListOf<Array<String>>()
                val rows = mutableListOf<String>()
                var rowWidth = -1
                var aisleHeight = -1

                fun finishAisle() {
                    if (rows.isEmpty()) return
                    if (aisleHeight < 0) {
                        aisleHeight = rows.size
                    } else if (rows.size != aisleHeight) {
                        throw IllegalStateException(
                            "Inconsistent aisle height in $path: ${rows.size}; expected $aisleHeight"
                        )
                    }
                    aisles += rows.toTypedArray()
                    rows.clear()
                }

                for (line in text.split('\n')) {
                    if (line.isEmpty()) {
                        finishAisle()
                        continue
                    }
                    if (rowWidth < 0) {
                        rowWidth = line.length
                    } else if (line.length != rowWidth) {
                        throw IllegalStateException(
                            "Inconsistent row width in $path: ${line.length}; expected $rowWidth"
                        )
                    }
                    rows += line
                }
                finishAisle()

                if (rowWidth <= 0 || aisleHeight <= 0 || aisles.isEmpty()) {
                    throw IllegalStateException("Empty multiblock pattern resource: $path")
                }
                expectedDimensions[name]?.let { expected ->
                    if (rowWidth != expected[0] || aisleHeight != expected[1] || aisles.size != expected[2]) {
                        throw IllegalStateException(
                            "Invalid dimensions in $path: ${rowWidth}x${aisleHeight}x${aisles.size}; " +
                                "expected ${expected[0]}x${expected[1]}x${expected[2]}"
                        )
                    }
                }

                val immutable = Collections.unmodifiableList(aisles.map { it.copyOf() })
                cache[name] = immutable
                return immutable
            }
        } catch (error: IOException) {
            throw IllegalStateException("Could not read GTOHJS multiblock pattern resource: $path", error)
        }
    }

    @JvmStatic
    fun apply(pattern: FactoryBlockPattern, name: String): FactoryBlockPattern {
        for (aisle in load(name)) {
            pattern.aisle(*aisle)
        }
        return pattern
    }

    @JvmStatic
    fun countSymbol(name: String, symbol: Char): Int {
        var count = 0
        for (aisle in load(name)) {
            for (row in aisle) {
                for (character in row) {
                    if (character == symbol) count++
                }
            }
        }
        return count
    }

    @JvmStatic
    fun block(id: String): Block {
        val resolvedBlock = RegistriesUtils.getBlock(id)
        val expected = ResourceLocation.tryParse(id)
        val actual = resolvedBlock?.let { ForgeRegistries.BLOCKS.getKey(it) }
        if (expected == null || expected != actual) {
            throw IllegalStateException(
                "Missing or mismatched registered block for GTOHJS pattern: $id; resolved=$actual"
            )
        }
        return resolvedBlock
    }
}
