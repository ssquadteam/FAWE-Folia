/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.block;

import static com.google.common.base.Preconditions.checkArgument;

import com.boydti.fawe.Fawe;
import com.boydti.fawe.command.SuggestInputParseException;
import com.boydti.fawe.util.MathMan;
import com.boydti.fawe.util.ReflectionUtils;
import com.boydti.fawe.util.StringMan;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SingleBlockTypeMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.registry.state.PropertyKey;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import it.unimi.dsi.fastutil.ints.IntCollections;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stores a list of common Block String IDs.
 */
public final class BlockTypes {

    @Nullable public static final BlockType ACACIA_BUTTON = get("minecraft:acacia_button");
    @Nullable public static final BlockType ACACIA_DOOR = get("minecraft:acacia_door");
    @Nullable public static final BlockType ACACIA_FENCE = get("minecraft:acacia_fence");
    @Nullable public static final BlockType ACACIA_FENCE_GATE = get("minecraft:acacia_fence_gate");
    @Nullable public static final BlockType ACACIA_LEAVES = get("minecraft:acacia_leaves");
    @Nullable public static final BlockType ACACIA_LOG = get("minecraft:acacia_log");
    @Nullable public static final BlockType ACACIA_PLANKS = get("minecraft:acacia_planks");
    @Nullable public static final BlockType ACACIA_PRESSURE_PLATE = get("minecraft:acacia_pressure_plate");
    @Nullable public static final BlockType ACACIA_SAPLING = get("minecraft:acacia_sapling");
    @Nullable public static final BlockType ACACIA_SLAB = get("minecraft:acacia_slab");
    @Nullable public static final BlockType ACACIA_STAIRS = get("minecraft:acacia_stairs");
    @Nullable public static final BlockType ACACIA_TRAPDOOR = get("minecraft:acacia_trapdoor");
    @Nullable public static final BlockType ACACIA_WOOD = get("minecraft:acacia_wood");
    @Nullable public static final BlockType ACTIVATOR_RAIL = get("minecraft:activator_rail");
    @Nullable public static final BlockType AIR = get("minecraft:air");
    @Nullable public static final BlockType ALLIUM = get("minecraft:allium");
    @Nullable public static final BlockType ANDESITE = get("minecraft:andesite");
    @Nullable public static final BlockType ANVIL = get("minecraft:anvil");
    @Nullable public static final BlockType ATTACHED_MELON_STEM = get("minecraft:attached_melon_stem");
    @Nullable public static final BlockType ATTACHED_PUMPKIN_STEM = get("minecraft:attached_pumpkin_stem");
    @Nullable public static final BlockType AZURE_BLUET = get("minecraft:azure_bluet");
    @Nullable public static final BlockType BARRIER = get("minecraft:barrier");
    @Nullable public static final BlockType BEACON = get("minecraft:beacon");
    @Nullable public static final BlockType BEDROCK = get("minecraft:bedrock");
    @Nullable public static final BlockType BEETROOTS = get("minecraft:beetroots");
    @Nullable public static final BlockType BIRCH_BUTTON = get("minecraft:birch_button");
    @Nullable public static final BlockType BIRCH_DOOR = get("minecraft:birch_door");
    @Nullable public static final BlockType BIRCH_FENCE = get("minecraft:birch_fence");
    @Nullable public static final BlockType BIRCH_FENCE_GATE = get("minecraft:birch_fence_gate");
    @Nullable public static final BlockType BIRCH_LEAVES = get("minecraft:birch_leaves");
    @Nullable public static final BlockType BIRCH_LOG = get("minecraft:birch_log");
    @Nullable public static final BlockType BIRCH_PLANKS = get("minecraft:birch_planks");
    @Nullable public static final BlockType BIRCH_PRESSURE_PLATE = get("minecraft:birch_pressure_plate");
    @Nullable public static final BlockType BIRCH_SAPLING = get("minecraft:birch_sapling");
    @Nullable public static final BlockType BIRCH_SLAB = get("minecraft:birch_slab");
    @Nullable public static final BlockType BIRCH_STAIRS = get("minecraft:birch_stairs");
    @Nullable public static final BlockType BIRCH_TRAPDOOR = get("minecraft:birch_trapdoor");
    @Nullable public static final BlockType BIRCH_WOOD = get("minecraft:birch_wood");
    @Nullable public static final BlockType BLACK_BANNER = get("minecraft:black_banner");
    @Nullable public static final BlockType BLACK_BED = get("minecraft:black_bed");
    @Nullable public static final BlockType BLACK_CARPET = get("minecraft:black_carpet");
    @Nullable public static final BlockType BLACK_CONCRETE = get("minecraft:black_concrete");
    @Nullable public static final BlockType BLACK_CONCRETE_POWDER = get("minecraft:black_concrete_powder");
    @Nullable public static final BlockType BLACK_GLAZED_TERRACOTTA = get("minecraft:black_glazed_terracotta");
    @Nullable public static final BlockType BLACK_SHULKER_BOX = get("minecraft:black_shulker_box");
    @Nullable public static final BlockType BLACK_STAINED_GLASS = get("minecraft:black_stained_glass");
    @Nullable public static final BlockType BLACK_STAINED_GLASS_PANE = get("minecraft:black_stained_glass_pane");
    @Nullable public static final BlockType BLACK_TERRACOTTA = get("minecraft:black_terracotta");
    @Nullable public static final BlockType BLACK_WALL_BANNER = get("minecraft:black_wall_banner");
    @Nullable public static final BlockType BLACK_WOOL = get("minecraft:black_wool");
    @Nullable public static final BlockType BLUE_BANNER = get("minecraft:blue_banner");
    @Nullable public static final BlockType BLUE_BED = get("minecraft:blue_bed");
    @Nullable public static final BlockType BLUE_CARPET = get("minecraft:blue_carpet");
    @Nullable public static final BlockType BLUE_CONCRETE = get("minecraft:blue_concrete");
    @Nullable public static final BlockType BLUE_CONCRETE_POWDER = get("minecraft:blue_concrete_powder");
    @Nullable public static final BlockType BLUE_GLAZED_TERRACOTTA = get("minecraft:blue_glazed_terracotta");
    @Nullable public static final BlockType BLUE_ICE = get("minecraft:blue_ice");
    @Nullable public static final BlockType BLUE_ORCHID = get("minecraft:blue_orchid");
    @Nullable public static final BlockType BLUE_SHULKER_BOX = get("minecraft:blue_shulker_box");
    @Nullable public static final BlockType BLUE_STAINED_GLASS = get("minecraft:blue_stained_glass");
    @Nullable public static final BlockType BLUE_STAINED_GLASS_PANE = get("minecraft:blue_stained_glass_pane");
    @Nullable public static final BlockType BLUE_TERRACOTTA = get("minecraft:blue_terracotta");
    @Nullable public static final BlockType BLUE_WALL_BANNER = get("minecraft:blue_wall_banner");
    @Nullable public static final BlockType BLUE_WOOL = get("minecraft:blue_wool");
    @Nullable public static final BlockType BONE_BLOCK = get("minecraft:bone_block");
    @Nullable public static final BlockType BOOKSHELF = get("minecraft:bookshelf");
    @Nullable public static final BlockType BRAIN_CORAL = get("minecraft:brain_coral");
    @Nullable public static final BlockType BRAIN_CORAL_BLOCK = get("minecraft:brain_coral_block");
    @Nullable public static final BlockType BRAIN_CORAL_FAN = get("minecraft:brain_coral_fan");
    @Nullable public static final BlockType BRAIN_CORAL_WALL_FAN = get("minecraft:brain_coral_wall_fan");
    @Nullable public static final BlockType BREWING_STAND = get("minecraft:brewing_stand");
    @Nullable public static final BlockType BRICK_SLAB = get("minecraft:brick_slab");
    @Nullable public static final BlockType BRICK_STAIRS = get("minecraft:brick_stairs");
    @Nullable public static final BlockType BRICKS = get("minecraft:bricks");
    @Nullable public static final BlockType BROWN_BANNER = get("minecraft:brown_banner");
    @Nullable public static final BlockType BROWN_BED = get("minecraft:brown_bed");
    @Nullable public static final BlockType BROWN_CARPET = get("minecraft:brown_carpet");
    @Nullable public static final BlockType BROWN_CONCRETE = get("minecraft:brown_concrete");
    @Nullable public static final BlockType BROWN_CONCRETE_POWDER = get("minecraft:brown_concrete_powder");
    @Nullable public static final BlockType BROWN_GLAZED_TERRACOTTA = get("minecraft:brown_glazed_terracotta");
    @Nullable public static final BlockType BROWN_MUSHROOM = get("minecraft:brown_mushroom");
    @Nullable public static final BlockType BROWN_MUSHROOM_BLOCK = get("minecraft:brown_mushroom_block");
    @Nullable public static final BlockType BROWN_SHULKER_BOX = get("minecraft:brown_shulker_box");
    @Nullable public static final BlockType BROWN_STAINED_GLASS = get("minecraft:brown_stained_glass");
    @Nullable public static final BlockType BROWN_STAINED_GLASS_PANE = get("minecraft:brown_stained_glass_pane");
    @Nullable public static final BlockType BROWN_TERRACOTTA = get("minecraft:brown_terracotta");
    @Nullable public static final BlockType BROWN_WALL_BANNER = get("minecraft:brown_wall_banner");
    @Nullable public static final BlockType BROWN_WOOL = get("minecraft:brown_wool");
    @Nullable public static final BlockType BUBBLE_COLUMN = get("minecraft:bubble_column");
    @Nullable public static final BlockType BUBBLE_CORAL = get("minecraft:bubble_coral");
    @Nullable public static final BlockType BUBBLE_CORAL_BLOCK = get("minecraft:bubble_coral_block");
    @Nullable public static final BlockType BUBBLE_CORAL_FAN = get("minecraft:bubble_coral_fan");
    @Nullable public static final BlockType BUBBLE_CORAL_WALL_FAN = get("minecraft:bubble_coral_wall_fan");
    @Nullable public static final BlockType CACTUS = get("minecraft:cactus");
    @Nullable public static final BlockType CAKE = get("minecraft:cake");
    @Nullable public static final BlockType CARROTS = get("minecraft:carrots");
    @Nullable public static final BlockType CARVED_PUMPKIN = get("minecraft:carved_pumpkin");
    @Nullable public static final BlockType CAULDRON = get("minecraft:cauldron");
    @Nullable public static final BlockType CAVE_AIR = get("minecraft:cave_air");
    @Nullable public static final BlockType CHAIN_COMMAND_BLOCK = get("minecraft:chain_command_block");
    @Nullable public static final BlockType CHEST = get("minecraft:chest");
    @Nullable public static final BlockType CHIPPED_ANVIL = get("minecraft:chipped_anvil");
    @Nullable public static final BlockType CHISELED_QUARTZ_BLOCK = get("minecraft:chiseled_quartz_block");
    @Nullable public static final BlockType CHISELED_RED_SANDSTONE = get("minecraft:chiseled_red_sandstone");
    @Nullable public static final BlockType CHISELED_SANDSTONE = get("minecraft:chiseled_sandstone");
    @Nullable public static final BlockType CHISELED_STONE_BRICKS = get("minecraft:chiseled_stone_bricks");
    @Nullable public static final BlockType CHORUS_FLOWER = get("minecraft:chorus_flower");
    @Nullable public static final BlockType CHORUS_PLANT = get("minecraft:chorus_plant");
    @Nullable public static final BlockType CLAY = get("minecraft:clay");
    @Nullable public static final BlockType COAL_BLOCK = get("minecraft:coal_block");
    @Nullable public static final BlockType COAL_ORE = get("minecraft:coal_ore");
    @Nullable public static final BlockType COARSE_DIRT = get("minecraft:coarse_dirt");
    @Nullable public static final BlockType COBBLESTONE = get("minecraft:cobblestone");
    @Nullable public static final BlockType COBBLESTONE_SLAB = get("minecraft:cobblestone_slab");
    @Nullable public static final BlockType COBBLESTONE_STAIRS = get("minecraft:cobblestone_stairs");
    @Nullable public static final BlockType COBBLESTONE_WALL = get("minecraft:cobblestone_wall");
    @Nullable public static final BlockType COBWEB = get("minecraft:cobweb");
    @Nullable public static final BlockType COCOA = get("minecraft:cocoa");
    @Nullable public static final BlockType COMMAND_BLOCK = get("minecraft:command_block");
    @Nullable public static final BlockType COMPARATOR = get("minecraft:comparator");
    @Nullable public static final BlockType CONDUIT = get("minecraft:conduit");
    @Nullable public static final BlockType CRACKED_STONE_BRICKS = get("minecraft:cracked_stone_bricks");
    @Nullable public static final BlockType CRAFTING_TABLE = get("minecraft:crafting_table");
    @Nullable public static final BlockType CREEPER_HEAD = get("minecraft:creeper_head");
    @Nullable public static final BlockType CREEPER_WALL_HEAD = get("minecraft:creeper_wall_head");
    @Nullable public static final BlockType CUT_RED_SANDSTONE = get("minecraft:cut_red_sandstone");
    @Nullable public static final BlockType CUT_SANDSTONE = get("minecraft:cut_sandstone");
    @Nullable public static final BlockType CYAN_BANNER = get("minecraft:cyan_banner");
    @Nullable public static final BlockType CYAN_BED = get("minecraft:cyan_bed");
    @Nullable public static final BlockType CYAN_CARPET = get("minecraft:cyan_carpet");
    @Nullable public static final BlockType CYAN_CONCRETE = get("minecraft:cyan_concrete");
    @Nullable public static final BlockType CYAN_CONCRETE_POWDER = get("minecraft:cyan_concrete_powder");
    @Nullable public static final BlockType CYAN_GLAZED_TERRACOTTA = get("minecraft:cyan_glazed_terracotta");
    @Nullable public static final BlockType CYAN_SHULKER_BOX = get("minecraft:cyan_shulker_box");
    @Nullable public static final BlockType CYAN_STAINED_GLASS = get("minecraft:cyan_stained_glass");
    @Nullable public static final BlockType CYAN_STAINED_GLASS_PANE = get("minecraft:cyan_stained_glass_pane");
    @Nullable public static final BlockType CYAN_TERRACOTTA = get("minecraft:cyan_terracotta");
    @Nullable public static final BlockType CYAN_WALL_BANNER = get("minecraft:cyan_wall_banner");
    @Nullable public static final BlockType CYAN_WOOL = get("minecraft:cyan_wool");
    @Nullable public static final BlockType DAMAGED_ANVIL = get("minecraft:damaged_anvil");
    @Nullable public static final BlockType DANDELION = get("minecraft:dandelion");
    @Nullable public static final BlockType DARK_OAK_BUTTON = get("minecraft:dark_oak_button");
    @Nullable public static final BlockType DARK_OAK_DOOR = get("minecraft:dark_oak_door");
    @Nullable public static final BlockType DARK_OAK_FENCE = get("minecraft:dark_oak_fence");
    @Nullable public static final BlockType DARK_OAK_FENCE_GATE = get("minecraft:dark_oak_fence_gate");
    @Nullable public static final BlockType DARK_OAK_LEAVES = get("minecraft:dark_oak_leaves");
    @Nullable public static final BlockType DARK_OAK_LOG = get("minecraft:dark_oak_log");
    @Nullable public static final BlockType DARK_OAK_PLANKS = get("minecraft:dark_oak_planks");
    @Nullable public static final BlockType DARK_OAK_PRESSURE_PLATE = get("minecraft:dark_oak_pressure_plate");
    @Nullable public static final BlockType DARK_OAK_SAPLING = get("minecraft:dark_oak_sapling");
    @Nullable public static final BlockType DARK_OAK_SLAB = get("minecraft:dark_oak_slab");
    @Nullable public static final BlockType DARK_OAK_STAIRS = get("minecraft:dark_oak_stairs");
    @Nullable public static final BlockType DARK_OAK_TRAPDOOR = get("minecraft:dark_oak_trapdoor");
    @Nullable public static final BlockType DARK_OAK_WOOD = get("minecraft:dark_oak_wood");
    @Nullable public static final BlockType DARK_PRISMARINE = get("minecraft:dark_prismarine");
    @Nullable public static final BlockType DARK_PRISMARINE_SLAB = get("minecraft:dark_prismarine_slab");
    @Nullable public static final BlockType DARK_PRISMARINE_STAIRS = get("minecraft:dark_prismarine_stairs");
    @Nullable public static final BlockType DAYLIGHT_DETECTOR = get("minecraft:daylight_detector");
    @Nullable public static final BlockType DEAD_BRAIN_CORAL = get("minecraft:dead_brain_coral");
    @Nullable public static final BlockType DEAD_BRAIN_CORAL_BLOCK = get("minecraft:dead_brain_coral_block");
    @Nullable public static final BlockType DEAD_BRAIN_CORAL_FAN = get("minecraft:dead_brain_coral_fan");
    @Nullable public static final BlockType DEAD_BRAIN_CORAL_WALL_FAN = get("minecraft:dead_brain_coral_wall_fan");
    @Nullable public static final BlockType DEAD_BUBBLE_CORAL = get("minecraft:dead_bubble_coral");
    @Nullable public static final BlockType DEAD_BUBBLE_CORAL_BLOCK = get("minecraft:dead_bubble_coral_block");
    @Nullable public static final BlockType DEAD_BUBBLE_CORAL_FAN = get("minecraft:dead_bubble_coral_fan");
    @Nullable public static final BlockType DEAD_BUBBLE_CORAL_WALL_FAN = get("minecraft:dead_bubble_coral_wall_fan");
    @Nullable public static final BlockType DEAD_BUSH = get("minecraft:dead_bush");
    @Nullable public static final BlockType DEAD_FIRE_CORAL = get("minecraft:dead_fire_coral");
    @Nullable public static final BlockType DEAD_FIRE_CORAL_BLOCK = get("minecraft:dead_fire_coral_block");
    @Nullable public static final BlockType DEAD_FIRE_CORAL_FAN = get("minecraft:dead_fire_coral_fan");
    @Nullable public static final BlockType DEAD_FIRE_CORAL_WALL_FAN = get("minecraft:dead_fire_coral_wall_fan");
    @Nullable public static final BlockType DEAD_HORN_CORAL = get("minecraft:dead_horn_coral");
    @Nullable public static final BlockType DEAD_HORN_CORAL_BLOCK = get("minecraft:dead_horn_coral_block");
    @Nullable public static final BlockType DEAD_HORN_CORAL_FAN = get("minecraft:dead_horn_coral_fan");
    @Nullable public static final BlockType DEAD_HORN_CORAL_WALL_FAN = get("minecraft:dead_horn_coral_wall_fan");
    @Nullable public static final BlockType DEAD_TUBE_CORAL = get("minecraft:dead_tube_coral");
    @Nullable public static final BlockType DEAD_TUBE_CORAL_BLOCK = get("minecraft:dead_tube_coral_block");
    @Nullable public static final BlockType DEAD_TUBE_CORAL_FAN = get("minecraft:dead_tube_coral_fan");
    @Nullable public static final BlockType DEAD_TUBE_CORAL_WALL_FAN = get("minecraft:dead_tube_coral_wall_fan");
    @Nullable public static final BlockType DETECTOR_RAIL = get("minecraft:detector_rail");
    @Nullable public static final BlockType DIAMOND_BLOCK = get("minecraft:diamond_block");
    @Nullable public static final BlockType DIAMOND_ORE = get("minecraft:diamond_ore");
    @Nullable public static final BlockType DIORITE = get("minecraft:diorite");
    @Nullable public static final BlockType DIRT = get("minecraft:dirt");
    @Nullable public static final BlockType DISPENSER = get("minecraft:dispenser");
    @Nullable public static final BlockType DRAGON_EGG = get("minecraft:dragon_egg");
    @Nullable public static final BlockType DRAGON_HEAD = get("minecraft:dragon_head");
    @Nullable public static final BlockType DRAGON_WALL_HEAD = get("minecraft:dragon_wall_head");
    @Nullable public static final BlockType DRIED_KELP_BLOCK = get("minecraft:dried_kelp_block");
    @Nullable public static final BlockType DROPPER = get("minecraft:dropper");
    @Nullable public static final BlockType EMERALD_BLOCK = get("minecraft:emerald_block");
    @Nullable public static final BlockType EMERALD_ORE = get("minecraft:emerald_ore");
    @Nullable public static final BlockType ENCHANTING_TABLE = get("minecraft:enchanting_table");
    @Nullable public static final BlockType END_GATEWAY = get("minecraft:end_gateway");
    @Nullable public static final BlockType END_PORTAL = get("minecraft:end_portal");
    @Nullable public static final BlockType END_PORTAL_FRAME = get("minecraft:end_portal_frame");
    @Nullable public static final BlockType END_ROD = get("minecraft:end_rod");
    @Nullable public static final BlockType END_STONE = get("minecraft:end_stone");
    @Nullable public static final BlockType END_STONE_BRICKS = get("minecraft:end_stone_bricks");
    @Nullable public static final BlockType ENDER_CHEST = get("minecraft:ender_chest");
    @Nullable public static final BlockType FARMLAND = get("minecraft:farmland");
    @Nullable public static final BlockType FERN = get("minecraft:fern");
    @Nullable public static final BlockType FIRE = get("minecraft:fire");
    @Nullable public static final BlockType FIRE_CORAL = get("minecraft:fire_coral");
    @Nullable public static final BlockType FIRE_CORAL_BLOCK = get("minecraft:fire_coral_block");
    @Nullable public static final BlockType FIRE_CORAL_FAN = get("minecraft:fire_coral_fan");
    @Nullable public static final BlockType FIRE_CORAL_WALL_FAN = get("minecraft:fire_coral_wall_fan");
    @Nullable public static final BlockType FLOWER_POT = get("minecraft:flower_pot");
    @Nullable public static final BlockType FROSTED_ICE = get("minecraft:frosted_ice");
    @Nullable public static final BlockType FURNACE = get("minecraft:furnace");
    @Nullable public static final BlockType GLASS = get("minecraft:glass");
    @Nullable public static final BlockType GLASS_PANE = get("minecraft:glass_pane");
    @Nullable public static final BlockType GLOWSTONE = get("minecraft:glowstone");
    @Nullable public static final BlockType GOLD_BLOCK = get("minecraft:gold_block");
    @Nullable public static final BlockType GOLD_ORE = get("minecraft:gold_ore");
    @Nullable public static final BlockType GRANITE = get("minecraft:granite");
    @Nullable public static final BlockType GRASS = get("minecraft:grass");
    @Nullable public static final BlockType GRASS_BLOCK = get("minecraft:grass_block");
    @Nullable public static final BlockType GRASS_PATH = get("minecraft:grass_path");
    @Nullable public static final BlockType GRAVEL = get("minecraft:gravel");
    @Nullable public static final BlockType GRAY_BANNER = get("minecraft:gray_banner");
    @Nullable public static final BlockType GRAY_BED = get("minecraft:gray_bed");
    @Nullable public static final BlockType GRAY_CARPET = get("minecraft:gray_carpet");
    @Nullable public static final BlockType GRAY_CONCRETE = get("minecraft:gray_concrete");
    @Nullable public static final BlockType GRAY_CONCRETE_POWDER = get("minecraft:gray_concrete_powder");
    @Nullable public static final BlockType GRAY_GLAZED_TERRACOTTA = get("minecraft:gray_glazed_terracotta");
    @Nullable public static final BlockType GRAY_SHULKER_BOX = get("minecraft:gray_shulker_box");
    @Nullable public static final BlockType GRAY_STAINED_GLASS = get("minecraft:gray_stained_glass");
    @Nullable public static final BlockType GRAY_STAINED_GLASS_PANE = get("minecraft:gray_stained_glass_pane");
    @Nullable public static final BlockType GRAY_TERRACOTTA = get("minecraft:gray_terracotta");
    @Nullable public static final BlockType GRAY_WALL_BANNER = get("minecraft:gray_wall_banner");
    @Nullable public static final BlockType GRAY_WOOL = get("minecraft:gray_wool");
    @Nullable public static final BlockType GREEN_BANNER = get("minecraft:green_banner");
    @Nullable public static final BlockType GREEN_BED = get("minecraft:green_bed");
    @Nullable public static final BlockType GREEN_CARPET = get("minecraft:green_carpet");
    @Nullable public static final BlockType GREEN_CONCRETE = get("minecraft:green_concrete");
    @Nullable public static final BlockType GREEN_CONCRETE_POWDER = get("minecraft:green_concrete_powder");
    @Nullable public static final BlockType GREEN_GLAZED_TERRACOTTA = get("minecraft:green_glazed_terracotta");
    @Nullable public static final BlockType GREEN_SHULKER_BOX = get("minecraft:green_shulker_box");
    @Nullable public static final BlockType GREEN_STAINED_GLASS = get("minecraft:green_stained_glass");
    @Nullable public static final BlockType GREEN_STAINED_GLASS_PANE = get("minecraft:green_stained_glass_pane");
    @Nullable public static final BlockType GREEN_TERRACOTTA = get("minecraft:green_terracotta");
    @Nullable public static final BlockType GREEN_WALL_BANNER = get("minecraft:green_wall_banner");
    @Nullable public static final BlockType GREEN_WOOL = get("minecraft:green_wool");
    @Nullable public static final BlockType HAY_BLOCK = get("minecraft:hay_block");
    @Nullable public static final BlockType HEAVY_WEIGHTED_PRESSURE_PLATE = get("minecraft:heavy_weighted_pressure_plate");
    @Nullable public static final BlockType HOPPER = get("minecraft:hopper");
    @Nullable public static final BlockType HORN_CORAL = get("minecraft:horn_coral");
    @Nullable public static final BlockType HORN_CORAL_BLOCK = get("minecraft:horn_coral_block");
    @Nullable public static final BlockType HORN_CORAL_FAN = get("minecraft:horn_coral_fan");
    @Nullable public static final BlockType HORN_CORAL_WALL_FAN = get("minecraft:horn_coral_wall_fan");
    @Nullable public static final BlockType ICE = get("minecraft:ice");
    @Nullable public static final BlockType INFESTED_CHISELED_STONE_BRICKS = get("minecraft:infested_chiseled_stone_bricks");
    @Nullable public static final BlockType INFESTED_COBBLESTONE = get("minecraft:infested_cobblestone");
    @Nullable public static final BlockType INFESTED_CRACKED_STONE_BRICKS = get("minecraft:infested_cracked_stone_bricks");
    @Nullable public static final BlockType INFESTED_MOSSY_STONE_BRICKS = get("minecraft:infested_mossy_stone_bricks");
    @Nullable public static final BlockType INFESTED_STONE = get("minecraft:infested_stone");
    @Nullable public static final BlockType INFESTED_STONE_BRICKS = get("minecraft:infested_stone_bricks");
    @Nullable public static final BlockType IRON_BARS = get("minecraft:iron_bars");
    @Nullable public static final BlockType IRON_BLOCK = get("minecraft:iron_block");
    @Nullable public static final BlockType IRON_DOOR = get("minecraft:iron_door");
    @Nullable public static final BlockType IRON_ORE = get("minecraft:iron_ore");
    @Nullable public static final BlockType IRON_TRAPDOOR = get("minecraft:iron_trapdoor");
    @Nullable public static final BlockType JACK_O_LANTERN = get("minecraft:jack_o_lantern");
    @Nullable public static final BlockType JUKEBOX = get("minecraft:jukebox");
    @Nullable public static final BlockType JUNGLE_BUTTON = get("minecraft:jungle_button");
    @Nullable public static final BlockType JUNGLE_DOOR = get("minecraft:jungle_door");
    @Nullable public static final BlockType JUNGLE_FENCE = get("minecraft:jungle_fence");
    @Nullable public static final BlockType JUNGLE_FENCE_GATE = get("minecraft:jungle_fence_gate");
    @Nullable public static final BlockType JUNGLE_LEAVES = get("minecraft:jungle_leaves");
    @Nullable public static final BlockType JUNGLE_LOG = get("minecraft:jungle_log");
    @Nullable public static final BlockType JUNGLE_PLANKS = get("minecraft:jungle_planks");
    @Nullable public static final BlockType JUNGLE_PRESSURE_PLATE = get("minecraft:jungle_pressure_plate");
    @Nullable public static final BlockType JUNGLE_SAPLING = get("minecraft:jungle_sapling");
    @Nullable public static final BlockType JUNGLE_SLAB = get("minecraft:jungle_slab");
    @Nullable public static final BlockType JUNGLE_STAIRS = get("minecraft:jungle_stairs");
    @Nullable public static final BlockType JUNGLE_TRAPDOOR = get("minecraft:jungle_trapdoor");
    @Nullable public static final BlockType JUNGLE_WOOD = get("minecraft:jungle_wood");
    @Nullable public static final BlockType KELP = get("minecraft:kelp");
    @Nullable public static final BlockType KELP_PLANT = get("minecraft:kelp_plant");
    @Nullable public static final BlockType LADDER = get("minecraft:ladder");
    @Nullable public static final BlockType LAPIS_BLOCK = get("minecraft:lapis_block");
    @Nullable public static final BlockType LAPIS_ORE = get("minecraft:lapis_ore");
    @Nullable public static final BlockType LARGE_FERN = get("minecraft:large_fern");
    @Nullable public static final BlockType LAVA = get("minecraft:lava");
    @Nullable public static final BlockType LEVER = get("minecraft:lever");
    @Nullable public static final BlockType LIGHT_BLUE_BANNER = get("minecraft:light_blue_banner");
    @Nullable public static final BlockType LIGHT_BLUE_BED = get("minecraft:light_blue_bed");
    @Nullable public static final BlockType LIGHT_BLUE_CARPET = get("minecraft:light_blue_carpet");
    @Nullable public static final BlockType LIGHT_BLUE_CONCRETE = get("minecraft:light_blue_concrete");
    @Nullable public static final BlockType LIGHT_BLUE_CONCRETE_POWDER = get("minecraft:light_blue_concrete_powder");
    @Nullable public static final BlockType LIGHT_BLUE_GLAZED_TERRACOTTA = get("minecraft:light_blue_glazed_terracotta");
    @Nullable public static final BlockType LIGHT_BLUE_SHULKER_BOX = get("minecraft:light_blue_shulker_box");
    @Nullable public static final BlockType LIGHT_BLUE_STAINED_GLASS = get("minecraft:light_blue_stained_glass");
    @Nullable public static final BlockType LIGHT_BLUE_STAINED_GLASS_PANE = get("minecraft:light_blue_stained_glass_pane");
    @Nullable public static final BlockType LIGHT_BLUE_TERRACOTTA = get("minecraft:light_blue_terracotta");
    @Nullable public static final BlockType LIGHT_BLUE_WALL_BANNER = get("minecraft:light_blue_wall_banner");
    @Nullable public static final BlockType LIGHT_BLUE_WOOL = get("minecraft:light_blue_wool");
    @Nullable public static final BlockType LIGHT_GRAY_BANNER = get("minecraft:light_gray_banner");
    @Nullable public static final BlockType LIGHT_GRAY_BED = get("minecraft:light_gray_bed");
    @Nullable public static final BlockType LIGHT_GRAY_CARPET = get("minecraft:light_gray_carpet");
    @Nullable public static final BlockType LIGHT_GRAY_CONCRETE = get("minecraft:light_gray_concrete");
    @Nullable public static final BlockType LIGHT_GRAY_CONCRETE_POWDER = get("minecraft:light_gray_concrete_powder");
    @Nullable public static final BlockType LIGHT_GRAY_GLAZED_TERRACOTTA = get("minecraft:light_gray_glazed_terracotta");
    @Nullable public static final BlockType LIGHT_GRAY_SHULKER_BOX = get("minecraft:light_gray_shulker_box");
    @Nullable public static final BlockType LIGHT_GRAY_STAINED_GLASS = get("minecraft:light_gray_stained_glass");
    @Nullable public static final BlockType LIGHT_GRAY_STAINED_GLASS_PANE = get("minecraft:light_gray_stained_glass_pane");
    @Nullable public static final BlockType LIGHT_GRAY_TERRACOTTA = get("minecraft:light_gray_terracotta");
    @Nullable public static final BlockType LIGHT_GRAY_WALL_BANNER = get("minecraft:light_gray_wall_banner");
    @Nullable public static final BlockType LIGHT_GRAY_WOOL = get("minecraft:light_gray_wool");
    @Nullable public static final BlockType LIGHT_WEIGHTED_PRESSURE_PLATE = get("minecraft:light_weighted_pressure_plate");
    @Nullable public static final BlockType LILAC = get("minecraft:lilac");
    @Nullable public static final BlockType LILY_PAD = get("minecraft:lily_pad");
    @Nullable public static final BlockType LIME_BANNER = get("minecraft:lime_banner");
    @Nullable public static final BlockType LIME_BED = get("minecraft:lime_bed");
    @Nullable public static final BlockType LIME_CARPET = get("minecraft:lime_carpet");
    @Nullable public static final BlockType LIME_CONCRETE = get("minecraft:lime_concrete");
    @Nullable public static final BlockType LIME_CONCRETE_POWDER = get("minecraft:lime_concrete_powder");
    @Nullable public static final BlockType LIME_GLAZED_TERRACOTTA = get("minecraft:lime_glazed_terracotta");
    @Nullable public static final BlockType LIME_SHULKER_BOX = get("minecraft:lime_shulker_box");
    @Nullable public static final BlockType LIME_STAINED_GLASS = get("minecraft:lime_stained_glass");
    @Nullable public static final BlockType LIME_STAINED_GLASS_PANE = get("minecraft:lime_stained_glass_pane");
    @Nullable public static final BlockType LIME_TERRACOTTA = get("minecraft:lime_terracotta");
    @Nullable public static final BlockType LIME_WALL_BANNER = get("minecraft:lime_wall_banner");
    @Nullable public static final BlockType LIME_WOOL = get("minecraft:lime_wool");
    @Nullable public static final BlockType MAGENTA_BANNER = get("minecraft:magenta_banner");
    @Nullable public static final BlockType MAGENTA_BED = get("minecraft:magenta_bed");
    @Nullable public static final BlockType MAGENTA_CARPET = get("minecraft:magenta_carpet");
    @Nullable public static final BlockType MAGENTA_CONCRETE = get("minecraft:magenta_concrete");
    @Nullable public static final BlockType MAGENTA_CONCRETE_POWDER = get("minecraft:magenta_concrete_powder");
    @Nullable public static final BlockType MAGENTA_GLAZED_TERRACOTTA = get("minecraft:magenta_glazed_terracotta");
    @Nullable public static final BlockType MAGENTA_SHULKER_BOX = get("minecraft:magenta_shulker_box");
    @Nullable public static final BlockType MAGENTA_STAINED_GLASS = get("minecraft:magenta_stained_glass");
    @Nullable public static final BlockType MAGENTA_STAINED_GLASS_PANE = get("minecraft:magenta_stained_glass_pane");
    @Nullable public static final BlockType MAGENTA_TERRACOTTA = get("minecraft:magenta_terracotta");
    @Nullable public static final BlockType MAGENTA_WALL_BANNER = get("minecraft:magenta_wall_banner");
    @Nullable public static final BlockType MAGENTA_WOOL = get("minecraft:magenta_wool");
    @Nullable public static final BlockType MAGMA_BLOCK = get("minecraft:magma_block");
    @Nullable public static final BlockType MELON = get("minecraft:melon");
    @Nullable public static final BlockType MELON_STEM = get("minecraft:melon_stem");
    @Nullable public static final BlockType MOSSY_COBBLESTONE = get("minecraft:mossy_cobblestone");
    @Nullable public static final BlockType MOSSY_COBBLESTONE_WALL = get("minecraft:mossy_cobblestone_wall");
    @Nullable public static final BlockType MOSSY_STONE_BRICKS = get("minecraft:mossy_stone_bricks");
    @Nullable public static final BlockType MOVING_PISTON = get("minecraft:moving_piston");
    @Nullable public static final BlockType MUSHROOM_STEM = get("minecraft:mushroom_stem");
    @Nullable public static final BlockType MYCELIUM = get("minecraft:mycelium");
    @Nullable public static final BlockType NETHER_BRICK_FENCE = get("minecraft:nether_brick_fence");
    @Nullable public static final BlockType NETHER_BRICK_SLAB = get("minecraft:nether_brick_slab");
    @Nullable public static final BlockType NETHER_BRICK_STAIRS = get("minecraft:nether_brick_stairs");
    @Nullable public static final BlockType NETHER_BRICKS = get("minecraft:nether_bricks");
    @Nullable public static final BlockType NETHER_PORTAL = get("minecraft:nether_portal");
    @Nullable public static final BlockType NETHER_QUARTZ_ORE = get("minecraft:nether_quartz_ore");
    @Nullable public static final BlockType NETHER_WART = get("minecraft:nether_wart");
    @Nullable public static final BlockType NETHER_WART_BLOCK = get("minecraft:nether_wart_block");
    @Nullable public static final BlockType NETHERRACK = get("minecraft:netherrack");
    @Nullable public static final BlockType NOTE_BLOCK = get("minecraft:note_block");
    @Nullable public static final BlockType OAK_BUTTON = get("minecraft:oak_button");
    @Nullable public static final BlockType OAK_DOOR = get("minecraft:oak_door");
    @Nullable public static final BlockType OAK_FENCE = get("minecraft:oak_fence");
    @Nullable public static final BlockType OAK_FENCE_GATE = get("minecraft:oak_fence_gate");
    @Nullable public static final BlockType OAK_LEAVES = get("minecraft:oak_leaves");
    @Nullable public static final BlockType OAK_LOG = get("minecraft:oak_log");
    @Nullable public static final BlockType OAK_PLANKS = get("minecraft:oak_planks");
    @Nullable public static final BlockType OAK_PRESSURE_PLATE = get("minecraft:oak_pressure_plate");
    @Nullable public static final BlockType OAK_SAPLING = get("minecraft:oak_sapling");
    @Nullable public static final BlockType OAK_SLAB = get("minecraft:oak_slab");
    @Nullable public static final BlockType OAK_STAIRS = get("minecraft:oak_stairs");
    @Nullable public static final BlockType OAK_TRAPDOOR = get("minecraft:oak_trapdoor");
    @Nullable public static final BlockType OAK_WOOD = get("minecraft:oak_wood");
    @Nullable public static final BlockType OBSERVER = get("minecraft:observer");
    @Nullable public static final BlockType OBSIDIAN = get("minecraft:obsidian");
    @Nullable public static final BlockType ORANGE_BANNER = get("minecraft:orange_banner");
    @Nullable public static final BlockType ORANGE_BED = get("minecraft:orange_bed");
    @Nullable public static final BlockType ORANGE_CARPET = get("minecraft:orange_carpet");
    @Nullable public static final BlockType ORANGE_CONCRETE = get("minecraft:orange_concrete");
    @Nullable public static final BlockType ORANGE_CONCRETE_POWDER = get("minecraft:orange_concrete_powder");
    @Nullable public static final BlockType ORANGE_GLAZED_TERRACOTTA = get("minecraft:orange_glazed_terracotta");
    @Nullable public static final BlockType ORANGE_SHULKER_BOX = get("minecraft:orange_shulker_box");
    @Nullable public static final BlockType ORANGE_STAINED_GLASS = get("minecraft:orange_stained_glass");
    @Nullable public static final BlockType ORANGE_STAINED_GLASS_PANE = get("minecraft:orange_stained_glass_pane");
    @Nullable public static final BlockType ORANGE_TERRACOTTA = get("minecraft:orange_terracotta");
    @Nullable public static final BlockType ORANGE_TULIP = get("minecraft:orange_tulip");
    @Nullable public static final BlockType ORANGE_WALL_BANNER = get("minecraft:orange_wall_banner");
    @Nullable public static final BlockType ORANGE_WOOL = get("minecraft:orange_wool");
    @Nullable public static final BlockType OXEYE_DAISY = get("minecraft:oxeye_daisy");
    @Nullable public static final BlockType PACKED_ICE = get("minecraft:packed_ice");
    @Nullable public static final BlockType PEONY = get("minecraft:peony");
    @Nullable public static final BlockType PETRIFIED_OAK_SLAB = get("minecraft:petrified_oak_slab");
    @Nullable public static final BlockType PINK_BANNER = get("minecraft:pink_banner");
    @Nullable public static final BlockType PINK_BED = get("minecraft:pink_bed");
    @Nullable public static final BlockType PINK_CARPET = get("minecraft:pink_carpet");
    @Nullable public static final BlockType PINK_CONCRETE = get("minecraft:pink_concrete");
    @Nullable public static final BlockType PINK_CONCRETE_POWDER = get("minecraft:pink_concrete_powder");
    @Nullable public static final BlockType PINK_GLAZED_TERRACOTTA = get("minecraft:pink_glazed_terracotta");
    @Nullable public static final BlockType PINK_SHULKER_BOX = get("minecraft:pink_shulker_box");
    @Nullable public static final BlockType PINK_STAINED_GLASS = get("minecraft:pink_stained_glass");
    @Nullable public static final BlockType PINK_STAINED_GLASS_PANE = get("minecraft:pink_stained_glass_pane");
    @Nullable public static final BlockType PINK_TERRACOTTA = get("minecraft:pink_terracotta");
    @Nullable public static final BlockType PINK_TULIP = get("minecraft:pink_tulip");
    @Nullable public static final BlockType PINK_WALL_BANNER = get("minecraft:pink_wall_banner");
    @Nullable public static final BlockType PINK_WOOL = get("minecraft:pink_wool");
    @Nullable public static final BlockType PISTON = get("minecraft:piston");
    @Nullable public static final BlockType PISTON_HEAD = get("minecraft:piston_head");
    @Nullable public static final BlockType PLAYER_HEAD = get("minecraft:player_head");
    @Nullable public static final BlockType PLAYER_WALL_HEAD = get("minecraft:player_wall_head");
    @Nullable public static final BlockType PODZOL = get("minecraft:podzol");
    @Nullable public static final BlockType POLISHED_ANDESITE = get("minecraft:polished_andesite");
    @Nullable public static final BlockType POLISHED_DIORITE = get("minecraft:polished_diorite");
    @Nullable public static final BlockType POLISHED_GRANITE = get("minecraft:polished_granite");
    @Nullable public static final BlockType POPPY = get("minecraft:poppy");
    @Nullable public static final BlockType POTATOES = get("minecraft:potatoes");
    @Nullable public static final BlockType POTTED_ACACIA_SAPLING = get("minecraft:potted_acacia_sapling");
    @Nullable public static final BlockType POTTED_ALLIUM = get("minecraft:potted_allium");
    @Nullable public static final BlockType POTTED_AZURE_BLUET = get("minecraft:potted_azure_bluet");
    @Nullable public static final BlockType POTTED_BIRCH_SAPLING = get("minecraft:potted_birch_sapling");
    @Nullable public static final BlockType POTTED_BLUE_ORCHID = get("minecraft:potted_blue_orchid");
    @Nullable public static final BlockType POTTED_BROWN_MUSHROOM = get("minecraft:potted_brown_mushroom");
    @Nullable public static final BlockType POTTED_CACTUS = get("minecraft:potted_cactus");
    @Nullable public static final BlockType POTTED_DANDELION = get("minecraft:potted_dandelion");
    @Nullable public static final BlockType POTTED_DARK_OAK_SAPLING = get("minecraft:potted_dark_oak_sapling");
    @Nullable public static final BlockType POTTED_DEAD_BUSH = get("minecraft:potted_dead_bush");
    @Nullable public static final BlockType POTTED_FERN = get("minecraft:potted_fern");
    @Nullable public static final BlockType POTTED_JUNGLE_SAPLING = get("minecraft:potted_jungle_sapling");
    @Nullable public static final BlockType POTTED_OAK_SAPLING = get("minecraft:potted_oak_sapling");
    @Nullable public static final BlockType POTTED_ORANGE_TULIP = get("minecraft:potted_orange_tulip");
    @Nullable public static final BlockType POTTED_OXEYE_DAISY = get("minecraft:potted_oxeye_daisy");
    @Nullable public static final BlockType POTTED_PINK_TULIP = get("minecraft:potted_pink_tulip");
    @Nullable public static final BlockType POTTED_POPPY = get("minecraft:potted_poppy");
    @Nullable public static final BlockType POTTED_RED_MUSHROOM = get("minecraft:potted_red_mushroom");
    @Nullable public static final BlockType POTTED_RED_TULIP = get("minecraft:potted_red_tulip");
    @Nullable public static final BlockType POTTED_SPRUCE_SAPLING = get("minecraft:potted_spruce_sapling");
    @Nullable public static final BlockType POTTED_WHITE_TULIP = get("minecraft:potted_white_tulip");
    @Nullable public static final BlockType POWERED_RAIL = get("minecraft:powered_rail");
    @Nullable public static final BlockType PRISMARINE = get("minecraft:prismarine");
    @Nullable public static final BlockType PRISMARINE_BRICK_SLAB = get("minecraft:prismarine_brick_slab");
    @Nullable public static final BlockType PRISMARINE_BRICK_STAIRS = get("minecraft:prismarine_brick_stairs");
    @Nullable public static final BlockType PRISMARINE_BRICKS = get("minecraft:prismarine_bricks");
    @Nullable public static final BlockType PRISMARINE_SLAB = get("minecraft:prismarine_slab");
    @Nullable public static final BlockType PRISMARINE_STAIRS = get("minecraft:prismarine_stairs");
    @Nullable public static final BlockType PUMPKIN = get("minecraft:pumpkin");
    @Nullable public static final BlockType PUMPKIN_STEM = get("minecraft:pumpkin_stem");
    @Nullable public static final BlockType PURPLE_BANNER = get("minecraft:purple_banner");
    @Nullable public static final BlockType PURPLE_BED = get("minecraft:purple_bed");
    @Nullable public static final BlockType PURPLE_CARPET = get("minecraft:purple_carpet");
    @Nullable public static final BlockType PURPLE_CONCRETE = get("minecraft:purple_concrete");
    @Nullable public static final BlockType PURPLE_CONCRETE_POWDER = get("minecraft:purple_concrete_powder");
    @Nullable public static final BlockType PURPLE_GLAZED_TERRACOTTA = get("minecraft:purple_glazed_terracotta");
    @Nullable public static final BlockType PURPLE_SHULKER_BOX = get("minecraft:purple_shulker_box");
    @Nullable public static final BlockType PURPLE_STAINED_GLASS = get("minecraft:purple_stained_glass");
    @Nullable public static final BlockType PURPLE_STAINED_GLASS_PANE = get("minecraft:purple_stained_glass_pane");
    @Nullable public static final BlockType PURPLE_TERRACOTTA = get("minecraft:purple_terracotta");
    @Nullable public static final BlockType PURPLE_WALL_BANNER = get("minecraft:purple_wall_banner");
    @Nullable public static final BlockType PURPLE_WOOL = get("minecraft:purple_wool");
    @Nullable public static final BlockType PURPUR_BLOCK = get("minecraft:purpur_block");
    @Nullable public static final BlockType PURPUR_PILLAR = get("minecraft:purpur_pillar");
    @Nullable public static final BlockType PURPUR_SLAB = get("minecraft:purpur_slab");
    @Nullable public static final BlockType PURPUR_STAIRS = get("minecraft:purpur_stairs");
    @Nullable public static final BlockType QUARTZ_BLOCK = get("minecraft:quartz_block");
    @Nullable public static final BlockType QUARTZ_PILLAR = get("minecraft:quartz_pillar");
    @Nullable public static final BlockType QUARTZ_SLAB = get("minecraft:quartz_slab");
    @Nullable public static final BlockType QUARTZ_STAIRS = get("minecraft:quartz_stairs");
    @Nullable public static final BlockType RAIL = get("minecraft:rail");
    @Nullable public static final BlockType RED_BANNER = get("minecraft:red_banner");
    @Nullable public static final BlockType RED_BED = get("minecraft:red_bed");
    @Nullable public static final BlockType RED_CARPET = get("minecraft:red_carpet");
    @Nullable public static final BlockType RED_CONCRETE = get("minecraft:red_concrete");
    @Nullable public static final BlockType RED_CONCRETE_POWDER = get("minecraft:red_concrete_powder");
    @Nullable public static final BlockType RED_GLAZED_TERRACOTTA = get("minecraft:red_glazed_terracotta");
    @Nullable public static final BlockType RED_MUSHROOM = get("minecraft:red_mushroom");
    @Nullable public static final BlockType RED_MUSHROOM_BLOCK = get("minecraft:red_mushroom_block");
    @Nullable public static final BlockType RED_NETHER_BRICKS = get("minecraft:red_nether_bricks");
    @Nullable public static final BlockType RED_SAND = get("minecraft:red_sand");
    @Nullable public static final BlockType RED_SANDSTONE = get("minecraft:red_sandstone");
    @Nullable public static final BlockType RED_SANDSTONE_SLAB = get("minecraft:red_sandstone_slab");
    @Nullable public static final BlockType RED_SANDSTONE_STAIRS = get("minecraft:red_sandstone_stairs");
    @Nullable public static final BlockType RED_SHULKER_BOX = get("minecraft:red_shulker_box");
    @Nullable public static final BlockType RED_STAINED_GLASS = get("minecraft:red_stained_glass");
    @Nullable public static final BlockType RED_STAINED_GLASS_PANE = get("minecraft:red_stained_glass_pane");
    @Nullable public static final BlockType RED_TERRACOTTA = get("minecraft:red_terracotta");
    @Nullable public static final BlockType RED_TULIP = get("minecraft:red_tulip");
    @Nullable public static final BlockType RED_WALL_BANNER = get("minecraft:red_wall_banner");
    @Nullable public static final BlockType RED_WOOL = get("minecraft:red_wool");
    @Nullable public static final BlockType REDSTONE_BLOCK = get("minecraft:redstone_block");
    @Nullable public static final BlockType REDSTONE_LAMP = get("minecraft:redstone_lamp");
    @Nullable public static final BlockType REDSTONE_ORE = get("minecraft:redstone_ore");
    @Nullable public static final BlockType REDSTONE_TORCH = get("minecraft:redstone_torch");
    @Nullable public static final BlockType REDSTONE_WALL_TORCH = get("minecraft:redstone_wall_torch");
    @Nullable public static final BlockType REDSTONE_WIRE = get("minecraft:redstone_wire");
    @Nullable public static final BlockType REPEATER = get("minecraft:repeater");
    @Nullable public static final BlockType REPEATING_COMMAND_BLOCK = get("minecraft:repeating_command_block");
    @Nullable public static final BlockType ROSE_BUSH = get("minecraft:rose_bush");
    @Nullable public static final BlockType SAND = get("minecraft:sand");
    @Nullable public static final BlockType SANDSTONE = get("minecraft:sandstone");
    @Nullable public static final BlockType SANDSTONE_SLAB = get("minecraft:sandstone_slab");
    @Nullable public static final BlockType SANDSTONE_STAIRS = get("minecraft:sandstone_stairs");
    @Nullable public static final BlockType SEA_LANTERN = get("minecraft:sea_lantern");
    @Nullable public static final BlockType SEA_PICKLE = get("minecraft:sea_pickle");
    @Nullable public static final BlockType SEAGRASS = get("minecraft:seagrass");
    @Nullable public static final BlockType SHULKER_BOX = get("minecraft:shulker_box");
    @Nullable public static final BlockType SIGN = get("minecraft:sign");
    @Nullable public static final BlockType SKELETON_SKULL = get("minecraft:skeleton_skull");
    @Nullable public static final BlockType SKELETON_WALL_SKULL = get("minecraft:skeleton_wall_skull");
    @Nullable public static final BlockType SLIME_BLOCK = get("minecraft:slime_block");
    @Nullable public static final BlockType SMOOTH_QUARTZ = get("minecraft:smooth_quartz");
    @Nullable public static final BlockType SMOOTH_RED_SANDSTONE = get("minecraft:smooth_red_sandstone");
    @Nullable public static final BlockType SMOOTH_SANDSTONE = get("minecraft:smooth_sandstone");
    @Nullable public static final BlockType SMOOTH_STONE = get("minecraft:smooth_stone");
    @Nullable public static final BlockType SNOW = get("minecraft:snow");
    @Nullable public static final BlockType SNOW_BLOCK = get("minecraft:snow_block");
    @Nullable public static final BlockType SOUL_SAND = get("minecraft:soul_sand");
    @Nullable public static final BlockType SPAWNER = get("minecraft:spawner");
    @Nullable public static final BlockType SPONGE = get("minecraft:sponge");
    @Nullable public static final BlockType SPRUCE_BUTTON = get("minecraft:spruce_button");
    @Nullable public static final BlockType SPRUCE_DOOR = get("minecraft:spruce_door");
    @Nullable public static final BlockType SPRUCE_FENCE = get("minecraft:spruce_fence");
    @Nullable public static final BlockType SPRUCE_FENCE_GATE = get("minecraft:spruce_fence_gate");
    @Nullable public static final BlockType SPRUCE_LEAVES = get("minecraft:spruce_leaves");
    @Nullable public static final BlockType SPRUCE_LOG = get("minecraft:spruce_log");
    @Nullable public static final BlockType SPRUCE_PLANKS = get("minecraft:spruce_planks");
    @Nullable public static final BlockType SPRUCE_PRESSURE_PLATE = get("minecraft:spruce_pressure_plate");
    @Nullable public static final BlockType SPRUCE_SAPLING = get("minecraft:spruce_sapling");
    @Nullable public static final BlockType SPRUCE_SLAB = get("minecraft:spruce_slab");
    @Nullable public static final BlockType SPRUCE_STAIRS = get("minecraft:spruce_stairs");
    @Nullable public static final BlockType SPRUCE_TRAPDOOR = get("minecraft:spruce_trapdoor");
    @Nullable public static final BlockType SPRUCE_WOOD = get("minecraft:spruce_wood");
    @Nullable public static final BlockType STICKY_PISTON = get("minecraft:sticky_piston");
    @Nullable public static final BlockType STONE = get("minecraft:stone");
    @Nullable public static final BlockType STONE_BRICK_SLAB = get("minecraft:stone_brick_slab");
    @Nullable public static final BlockType STONE_BRICK_STAIRS = get("minecraft:stone_brick_stairs");
    @Nullable public static final BlockType STONE_BRICKS = get("minecraft:stone_bricks");
    @Nullable public static final BlockType STONE_BUTTON = get("minecraft:stone_button");
    @Nullable public static final BlockType STONE_PRESSURE_PLATE = get("minecraft:stone_pressure_plate");
    @Nullable public static final BlockType STONE_SLAB = get("minecraft:stone_slab");
    @Nullable public static final BlockType STRIPPED_ACACIA_LOG = get("minecraft:stripped_acacia_log");
    @Nullable public static final BlockType STRIPPED_ACACIA_WOOD = get("minecraft:stripped_acacia_wood");
    @Nullable public static final BlockType STRIPPED_BIRCH_LOG = get("minecraft:stripped_birch_log");
    @Nullable public static final BlockType STRIPPED_BIRCH_WOOD = get("minecraft:stripped_birch_wood");
    @Nullable public static final BlockType STRIPPED_DARK_OAK_LOG = get("minecraft:stripped_dark_oak_log");
    @Nullable public static final BlockType STRIPPED_DARK_OAK_WOOD = get("minecraft:stripped_dark_oak_wood");
    @Nullable public static final BlockType STRIPPED_JUNGLE_LOG = get("minecraft:stripped_jungle_log");
    @Nullable public static final BlockType STRIPPED_JUNGLE_WOOD = get("minecraft:stripped_jungle_wood");
    @Nullable public static final BlockType STRIPPED_OAK_LOG = get("minecraft:stripped_oak_log");
    @Nullable public static final BlockType STRIPPED_OAK_WOOD = get("minecraft:stripped_oak_wood");
    @Nullable public static final BlockType STRIPPED_SPRUCE_LOG = get("minecraft:stripped_spruce_log");
    @Nullable public static final BlockType STRIPPED_SPRUCE_WOOD = get("minecraft:stripped_spruce_wood");
    @Nullable public static final BlockType STRUCTURE_BLOCK = get("minecraft:structure_block");
    @Nullable public static final BlockType STRUCTURE_VOID = get("minecraft:structure_void");
    @Nullable public static final BlockType SUGAR_CANE = get("minecraft:sugar_cane");
    @Nullable public static final BlockType SUNFLOWER = get("minecraft:sunflower");
    @Nullable public static final BlockType TALL_GRASS = get("minecraft:tall_grass");
    @Nullable public static final BlockType TALL_SEAGRASS = get("minecraft:tall_seagrass");
    @Nullable public static final BlockType TERRACOTTA = get("minecraft:terracotta");
    @Nullable public static final BlockType TNT = get("minecraft:tnt");
    @Nullable public static final BlockType TORCH = get("minecraft:torch");
    @Nullable public static final BlockType TRAPPED_CHEST = get("minecraft:trapped_chest");
    @Nullable public static final BlockType TRIPWIRE = get("minecraft:tripwire");
    @Nullable public static final BlockType TRIPWIRE_HOOK = get("minecraft:tripwire_hook");
    @Nullable public static final BlockType TUBE_CORAL = get("minecraft:tube_coral");
    @Nullable public static final BlockType TUBE_CORAL_BLOCK = get("minecraft:tube_coral_block");
    @Nullable public static final BlockType TUBE_CORAL_FAN = get("minecraft:tube_coral_fan");
    @Nullable public static final BlockType TUBE_CORAL_WALL_FAN = get("minecraft:tube_coral_wall_fan");
    @Nullable public static final BlockType TURTLE_EGG = get("minecraft:turtle_egg");
    @Nullable public static final BlockType VINE = get("minecraft:vine");
    @Nullable public static final BlockType VOID_AIR = get("minecraft:void_air");
    @Nullable public static final BlockType WALL_SIGN = get("minecraft:wall_sign");
    @Nullable public static final BlockType WALL_TORCH = get("minecraft:wall_torch");
    @Nullable public static final BlockType WATER = get("minecraft:water");
    @Nullable public static final BlockType WET_SPONGE = get("minecraft:wet_sponge");
    @Nullable public static final BlockType WHEAT = get("minecraft:wheat");
    @Nullable public static final BlockType WHITE_BANNER = get("minecraft:white_banner");
    @Nullable public static final BlockType WHITE_BED = get("minecraft:white_bed");
    @Nullable public static final BlockType WHITE_CARPET = get("minecraft:white_carpet");
    @Nullable public static final BlockType WHITE_CONCRETE = get("minecraft:white_concrete");
    @Nullable public static final BlockType WHITE_CONCRETE_POWDER = get("minecraft:white_concrete_powder");
    @Nullable public static final BlockType WHITE_GLAZED_TERRACOTTA = get("minecraft:white_glazed_terracotta");
    @Nullable public static final BlockType WHITE_SHULKER_BOX = get("minecraft:white_shulker_box");
    @Nullable public static final BlockType WHITE_STAINED_GLASS = get("minecraft:white_stained_glass");
    @Nullable public static final BlockType WHITE_STAINED_GLASS_PANE = get("minecraft:white_stained_glass_pane");
    @Nullable public static final BlockType WHITE_TERRACOTTA = get("minecraft:white_terracotta");
    @Nullable public static final BlockType WHITE_TULIP = get("minecraft:white_tulip");
    @Nullable public static final BlockType WHITE_WALL_BANNER = get("minecraft:white_wall_banner");
    @Nullable public static final BlockType WHITE_WOOL = get("minecraft:white_wool");
    @Nullable public static final BlockType WITHER_SKELETON_SKULL = get("minecraft:wither_skeleton_skull");
    @Nullable public static final BlockType WITHER_SKELETON_WALL_SKULL = get("minecraft:wither_skeleton_wall_skull");
    @Nullable public static final BlockType YELLOW_BANNER = get("minecraft:yellow_banner");
    @Nullable public static final BlockType YELLOW_BED = get("minecraft:yellow_bed");
    @Nullable public static final BlockType YELLOW_CARPET = get("minecraft:yellow_carpet");
    @Nullable public static final BlockType YELLOW_CONCRETE = get("minecraft:yellow_concrete");
    @Nullable public static final BlockType YELLOW_CONCRETE_POWDER = get("minecraft:yellow_concrete_powder");
    @Nullable public static final BlockType YELLOW_GLAZED_TERRACOTTA = get("minecraft:yellow_glazed_terracotta");
    @Nullable public static final BlockType YELLOW_SHULKER_BOX = get("minecraft:yellow_shulker_box");
    @Nullable public static final BlockType YELLOW_STAINED_GLASS = get("minecraft:yellow_stained_glass");
    @Nullable public static final BlockType YELLOW_STAINED_GLASS_PANE = get("minecraft:yellow_stained_glass_pane");
    @Nullable public static final BlockType YELLOW_TERRACOTTA = get("minecraft:yellow_terracotta");
    @Nullable public static final BlockType YELLOW_WALL_BANNER = get("minecraft:yellow_wall_banner");
    @Nullable public static final BlockType YELLOW_WOOL = get("minecraft:yellow_wool");
    @Nullable public static final BlockType ZOMBIE_HEAD = get("minecraft:zombie_head");
    @Nullable public static final BlockType ZOMBIE_WALL_HEAD = get("minecraft:zombie_wall_head");


    private static BlockType get(String id) {
    	return register(new BlockType(id));
    }

    private static BlockType get(String id, Function<BlockState, BlockState> values) {
    	return register(new BlockType(id, values));
    }

    public static BlockType get(BlockType type) {
    	if(sortedRegistry == null) {
    		sortedRegistry = new ArrayList<>();
    		stateList = new ArrayList<>();
    		$NAMESPACES = new LinkedHashSet<>();
            BIT_OFFSET = MathMan.log2nlz(WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().registerBlocks().size());
            BIT_MASK = ((1 << BIT_OFFSET) - 1);
    	}
    	if(!sortedRegistry.contains(type))sortedRegistry.add(type);
    	return internalRegister(type, sortedRegistry.indexOf(type));
    }

    private static ArrayList<BlockType> sortedRegistry;
    private static ArrayList<BlockState> stateList;
    public static BlockType[] values;
    public static BlockState[] states;
    private static Set<String> $NAMESPACES;
    @Deprecated public static int BIT_OFFSET; // Used internally
    @Deprecated public static int BIT_MASK; // Used internally

    private static BlockType internalRegister(BlockType blockType, final int internalId) {
        init(blockType, blockType.getId(), internalId, stateList);
        if(BlockType.REGISTRY.get(blockType.getId()) == null) BlockType.REGISTRY.register(blockType.getId(), blockType);
        $NAMESPACES.add(blockType.getNamespace());
        values = sortedRegistry.toArray(new BlockType[sortedRegistry.size()]);
        states = stateList.toArray(new BlockState[stateList.size()]);
        return blockType;
    }

    private static void init(BlockType type, String id, int internalId, ArrayList<BlockState> states) {
        try {
            type.setSettings(new Settings(type, id, internalId, states));
            states.addAll(type.updateStates());
            type.setStates(states);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /*
     -----------------------------------------------------
                    Settings
     -----------------------------------------------------
     */
    public final static class Settings {
        protected final int internalId;
        protected final ItemType itemType;
        protected BlockState defaultState;
        protected final AbstractProperty<?>[] propertiesMapArr;
        protected final AbstractProperty<?>[] propertiesArr;
        protected final List<AbstractProperty<?>> propertiesList;
        protected final Map<String, AbstractProperty<?>> propertiesMap;
        protected final Set<AbstractProperty<?>> propertiesSet;
        protected final BlockMaterial blockMaterial;
        protected final int permutations;
        protected int[] stateOrdinals;
        protected ArrayList<BlockState> localStates;

        Settings(BlockType type, String id, int internalId, List<BlockState> states) {
            this.internalId = internalId;

            int maxInternalStateId = 0;
            Map<String, ? extends Property<?>> properties = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getProperties(type);
            if (!properties.isEmpty()) {
                // Ensure the properties are registered
                int maxOrdinal = 0;
                for (String key : properties.keySet()) {
                    maxOrdinal = Math.max(PropertyKey.getOrCreate(key).ordinal(), maxOrdinal);
                }
                this.propertiesMapArr = new AbstractProperty[maxOrdinal + 1];
                int prop_arr_i = 0;
                this.propertiesArr = new AbstractProperty[properties.size()];
                HashMap<String, AbstractProperty<?>> propMap = new HashMap<>();

                int bitOffset = 0;
                for (Map.Entry<String, ? extends Property<?>> entry : properties.entrySet()) {
                    PropertyKey key = PropertyKey.getOrCreate(entry.getKey());
                    AbstractProperty<?> property = ((AbstractProperty<?>) entry.getValue()).withOffset(bitOffset);
                    this.propertiesMapArr[key.ordinal()] = property;
                    this.propertiesArr[prop_arr_i++] = property;
                    propMap.put(entry.getKey(), property);
                    bitOffset += property.getNumBits();

                    maxInternalStateId += (property.getValues().size() << bitOffset);
                }
                this.propertiesList = Arrays.asList(this.propertiesArr);
                this.propertiesMap = Collections.unmodifiableMap(propMap);
                this.propertiesSet = new LinkedHashSet<>(this.propertiesMap.values());
            } else {
                this.propertiesMapArr = new AbstractProperty[0];
                this.propertiesArr = this.propertiesMapArr;
                this.propertiesList = Collections.emptyList();
                this.propertiesMap = Collections.emptyMap();
                this.propertiesSet = Collections.emptySet();
            }
            this.permutations = maxInternalStateId;
            this.localStates = new ArrayList<>();

            this.blockMaterial = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getMaterial(type);
            this.itemType = ItemTypes.get(type);
            
            if (!propertiesList.isEmpty()) {
                this.stateOrdinals = generateStateOrdinals(internalId, states.size(), maxInternalStateId, propertiesList);
                for (int propId = 0; propId < this.stateOrdinals.length; propId++) {
                    int ordinal = this.stateOrdinals[propId];
                    if (ordinal != -1) {
                        int stateId = internalId + (propId << BlockTypes.BIT_OFFSET);
                        this.localStates.add(new BlockStateImpl(type, stateId, ordinal));
                    }
                }
                
              this.defaultState = this.localStates.get(this.stateOrdinals[internalId >> BlockTypes.BIT_OFFSET] - states.size());     
            } else {
                this.defaultState = new BlockStateImpl(id.contains("minecraft:__reserved__") ? new BlockType("minecraft:air") : type, internalId, states.size());
                this.localStates.add(this.defaultState);
            }
        }
    }
    
    private static int[] generateStateOrdinals(int internalId, int ordinal, int maxStateId, List<AbstractProperty<?>> props) {
        if (props.isEmpty()) return null;
        int[] result = new int[maxStateId + 1];
        Arrays.fill(result, -1);
        int[] state = new int[props.size()];
        int[] sizes = new int[props.size()];
        for (int i = 0; i < props.size(); i++) {
            sizes[i] = props.get(i).getValues().size();
        }
        int index = 0;
        outer:
        while (true) {
            // Create the state
            int stateId = internalId;
            for (int i = 0; i < state.length; i++) {
                stateId = props.get(i).modifyIndex(stateId, state[i]);
            }
            // Map it to the ordinal
            result[stateId >> BlockTypes.BIT_OFFSET] = ordinal++;
            // Increment the state
            while (++state[index] == sizes[index]) {
                state[index] = 0;
                index++;
                if (index == state.length) break outer;
            }
            index = 0;
        }
        return result;
    }

    public static BlockType parse(final String type) throws InputParseException {
        final String inputLower = type.toLowerCase();
        String input = inputLower;

        if (!input.split("\\[", 2)[0].contains(":")) input = "minecraft:" + input;
        BlockType result = BlockType.REGISTRY.get(input);
        if (result != null) return result;

        try {
            BlockStateHolder block = LegacyMapper.getInstance().getBlockFromLegacy(input);
            if (block != null) return block.getBlockType();
        } catch (NumberFormatException e) {
        } catch (IndexOutOfBoundsException e) {}

        throw new SuggestInputParseException("Does not match a valid block type: " + inputLower, inputLower, () -> Stream.of(BlockTypes.values)
            .filter(b -> b.getId().contains(inputLower))
            .map(e1 -> e1.getId())
            .collect(Collectors.toList())
        );
    }

    public static Set<String> getNameSpaces() {
        return $NAMESPACES;
    }

	public static final @Nullable BlockType get(final String id) {
	  return BlockType.REGISTRY.get(id.toLowerCase());
	}

	public static final @Nullable BlockType get(final CharSequence id) {
	  return BlockType.REGISTRY.get(id.toString().toLowerCase());
	}

    @Deprecated
    public static final BlockType get(final int ordinal) {
        return values[ordinal];
    }

    @Deprecated
    public static final BlockType getFromStateId(final int internalStateId) {
        return values[internalStateId & BIT_MASK];
    }

    @Deprecated
    public static final BlockType getFromStateOrdinal(final int internalStateOrdinal) {
        return states[internalStateOrdinal].getBlockType();
    }

    public static int size() {
        return values.length;
    }
    
}
