package gregtech.api.util;

import gregtech.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class InventoryUtilsTest {

    /**
     * Required. Without this all item-related operations will fail because registries haven't been initialized.
     */
    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void simulateItemStackMerge_succeeds_for_inserting_single_stack_into_empty_one_slot_inventory() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 64);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(feathers),
                        handler
                );

        MatcherAssert.assertThat("Merging one full stack into a single empty slot failed.", result, is(true));
    }

    @Test
    public void simulateItemStackMerge_succeeds_for_inserting_two_half_stacks_into_empty_one_slot_inventory() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 32);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Arrays.asList(feathers, feathers),
                        handler
                );

        MatcherAssert.assertThat("Merging two half-stacks into an empty inventory with one slot failed.", result, is(true));
    }

    @Test
    public void simulateItemStackMerge_succeeds_for_inserting_one_half_stack_into_inventory_with_one_half_stack() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 32);

        handler.insertItem(0, feathers, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(feathers),
                        handler
                );

        MatcherAssert.assertThat("Merging half a stack into an inventory with one slot containing half a stack of the same item failed.", result, is(true));
    }

    @Test
    public void simulateItemStackMerge_succeeds_for_inserting_one_half_stack_into_inventory_with_two_three_quarter_stacks() {
        IItemHandler handler = new ItemStackHandler(2);
        ItemStack feathers_32 = new ItemStack(Items.FEATHER, 32);
        ItemStack feathers_48 = new ItemStack(Items.FEATHER, 48);

        handler.insertItem(0, feathers_48, false);
        handler.insertItem(1, feathers_48, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(feathers_32),
                        handler
                );

        MatcherAssert.assertThat("Merging half a stack into an inventory with two three-quarter stacks of the same item failed.", result, is(true));
    }

    @Test
    public void simulateItemStackMerge_succeeds_for_inserting_one_half_stack_into_inventory_with_one_three_quarter_stack_and_one_empty_slot() {
        IItemHandler handler = new ItemStackHandler(2);
        ItemStack feathers_32 = new ItemStack(Items.FEATHER, 32);
        ItemStack feathers_48 = new ItemStack(Items.FEATHER, 48);

        handler.insertItem(0, feathers_48, false);
        handler.insertItem(1, feathers_48, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(feathers_32),
                        handler
                );

        MatcherAssert.assertThat("Merging half a stack into an inventory with one three-quarter stack of the same item and one empty slot failed.", result, is(true));
    }

    @Test
    public void simulateItemStackMerge_fails_to_insert_items_into_a_full_inventory_with_no_common_items() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 32);
        ItemStack arrows = new ItemStack(Items.ARROW, 1);

        handler.insertItem(0, feathers, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(arrows),
                        handler
                );

        MatcherAssert.assertThat("Unexpectedly succeeded at merging an arrow into an inventory full of feathers.", result, is(false));
    }

    @Test
    public void simulateItemStackMerge_fails_to_insert_items_into_a_full_inventory_with_common_items() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack inv_feathers = new ItemStack(Items.FEATHER, 64);
        ItemStack more_feathers = new ItemStack(Items.FEATHER, 1);

        handler.insertItem(0, inv_feathers, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(more_feathers),
                        handler
                );

        MatcherAssert.assertThat("Unexpectedly succeeded at merging feathers into an inventory full of feathers.", result, is(false));
    }

    @Test
    public void simulateItemStackMerge_respects_different_NBT_tags_as_different_items() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 1);
        ItemStack special_feathers = new ItemStack(Items.FEATHER, 1);
        special_feathers.setTagInfo("Foo", new NBTTagString("Test"));

        handler.insertItem(0, feathers, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(special_feathers),
                        handler
                );

        MatcherAssert.assertThat("Unexpectedly succeeded at merging feathers with NBT tags into a stack of plain feathers.", result, is(false));
    }

    @Test
    public void simulateItemStackMerge_respects_different_damage_values_as_different_items() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack feathers = new ItemStack(Items.FEATHER, 1);
        ItemStack special_feathers = new ItemStack(Items.FEATHER, 1);
        special_feathers.setItemDamage(1);

        handler.insertItem(0, feathers, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(special_feathers),
                        handler
                );

        MatcherAssert.assertThat("Unexpectedly succeeded at merging damaged feathers into a stack of plain feathers.", result, is(false));
    }

    @Test
    public void simulateItemStackMerge_respects_unstackable_but_otherwise_identical_items() {
        IItemHandler handler = new ItemStackHandler(1);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        ItemStack anotherPickaxe = new ItemStack(Items.IRON_PICKAXE, 1);

        MatcherAssert.assertThat(pickaxe.isStackable(), is(false));

        handler.insertItem(0, pickaxe, false);

        boolean result =
                InventoryUtils.simulateItemStackMerge(
                        Collections.singletonList(anotherPickaxe),
                        handler
                );

        MatcherAssert.assertThat("Unexpectedly succeeded at merging a pickaxe into another one.", result, is(false));
    }

    @Test
    public void normalizeItemStack_returns_empty_list_for_single_empty_stack() {
        List<ItemStack> result = InventoryUtils.normalizeItemStack(ItemStack.EMPTY);
        MatcherAssert.assertThat("Unexpectedly got results when normalizing an empty ItemStack", result.isEmpty(), is(true));
    }

    @Test
    public void normalizeItemStack_returns_single_element_list_for_a_single_already_normal_stack() {
        ItemStack stack = new ItemStack(Items.ENDER_PEARL, 16);
        List<ItemStack> result = InventoryUtils.normalizeItemStack(stack);

        MatcherAssert.assertThat("Unexpectedly got no results when normalizing an already normal ItemStack", result.isEmpty(), is(false));
        MatcherAssert.assertThat("Unexpectedly got wrong number of resulting stacks when normalizing an already normal ItemStack", result.size(), is(1));
        MatcherAssert.assertThat("ItemStack was modified when it didn't need to be", ItemStack.areItemStacksEqual(stack, result.get(0)), is(true));
    }

    @Test
    public void normalizeItemStack_returns_normalized_stacks_for_an_abnormal_stack() {
        ItemStack stack = new ItemStack(Items.ENDER_PEARL, 45);
        List<ItemStack> result = InventoryUtils.normalizeItemStack(stack);

        MatcherAssert.assertThat("Unexpectedly got no results when normalizing an abnormal stack", result.isEmpty(), is(false));
        MatcherAssert.assertThat("Unexpectedly got wrong number of resulting stacks when normalizing an abnormal ItemStack", result.size(), is(3));

        ItemStack expectedFull = new ItemStack(Items.ENDER_PEARL, 16);
        ItemStack expectedPartial = new ItemStack(Items.ENDER_PEARL, 13);
        MatcherAssert.assertThat("First item stack does not match expected full stack", ItemStack.areItemStacksEqual(expectedFull, result.get(0)), is(true));
        MatcherAssert.assertThat("Second item stack does not match expected full stack", ItemStack.areItemStacksEqual(expectedFull, result.get(1)), is(true));
        MatcherAssert.assertThat("Third item stack does not match expected partial stack", ItemStack.areItemStacksEqual(expectedPartial, result.get(2)), is(true));
    }

    @Test
    public void apportionStack_throws_AssertionError_when_supplied_stack_is_empty() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            InventoryUtils.apportionStack(ItemStack.EMPTY, 64);
        });
        MatcherAssert.assertThat(exception.getMessage(), is("Cannot apportion an empty stack."));
    }

    @Test
    public void apportionStack_throws_AssertionError_when_maxCount_is_zero() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            InventoryUtils.apportionStack(new ItemStack(Items.ARROW, 1), 0);
        });
        MatcherAssert.assertThat(exception.getMessage(), is("Count must be non-zero and positive."));
    }

    @Test
    public void apportionStack_throws_AssertionError_when_maxCount_is_negative() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            InventoryUtils.apportionStack(new ItemStack(Items.ARROW, 1), -1);
        });
        MatcherAssert.assertThat(exception.getMessage(), is("Count must be non-zero and positive."));
    }

    @Test
    public void apportionStack_splits_evenly_divisible_stack() {
        ItemStack oversized = new ItemStack(Items.ENDER_PEARL, 64);
        ItemStack normal = new ItemStack(Items.ENDER_PEARL, 16);

        List<ItemStack> result = InventoryUtils.apportionStack(oversized, 16);

        MatcherAssert.assertThat(result.isEmpty(), is(false));
        MatcherAssert.assertThat(result.size(), is(4));
        for (ItemStack stack : result) {
            MatcherAssert.assertThat(ItemStack.areItemStacksEqual(stack, normal), is(true));
        }
    }

    @Test
    public void apportionStack_splits_unevenly_divisible_stack_with_remainder_at_end() {
        ItemStack oversized = new ItemStack(Items.ENDER_PEARL, 45);
        ItemStack normal = new ItemStack(Items.ENDER_PEARL, 16);
        ItemStack remainder = new ItemStack(Items.ENDER_PEARL, 13);

        List<ItemStack> result = InventoryUtils.apportionStack(oversized, 16);

        MatcherAssert.assertThat(result.isEmpty(), is(false));
        MatcherAssert.assertThat(result.size(), is(3));

        MatcherAssert.assertThat(ItemStack.areItemStacksEqual(result.get(0), normal), is(true));
        MatcherAssert.assertThat(ItemStack.areItemStacksEqual(result.get(1), normal), is(true));
        MatcherAssert.assertThat(ItemStack.areItemStacksEqual(result.get(2), remainder), is(true));
    }

    @Test
    public void deepCopy_retains_empty_stacks_when_requested() {
        IItemHandler inventory = new ItemStackHandler(2);

        inventory.insertItem(1, new ItemStack(Items.FEATHER, 1), false);

        MatcherAssert.assertThat(inventory.getStackInSlot(0).isEmpty(), is(true));
        MatcherAssert.assertThat(inventory.getStackInSlot(1).isEmpty(), is(false));

        List<ItemStack> result = InventoryUtils.deepCopy(inventory, true);

        MatcherAssert.assertThat(result.isEmpty(), is(false));
        MatcherAssert.assertThat(result.size(), is(2));
        MatcherAssert.assertThat(result.get(0).isEmpty(), is(true));
    }

    @Test
    public void deepCopy_discards_empty_stacks_when_requested() {
        IItemHandler inventory = new ItemStackHandler(2);
        ItemStack feather = new ItemStack(Items.FEATHER, 1);
        inventory.insertItem(1, feather, false);

        MatcherAssert.assertThat(inventory.getStackInSlot(0).isEmpty(), is(true));
        MatcherAssert.assertThat(inventory.getStackInSlot(1).isEmpty(), is(false));

        List<ItemStack> result = InventoryUtils.deepCopy(inventory, false);

        MatcherAssert.assertThat(result.isEmpty(), is(false));
        MatcherAssert.assertThat(result.size(), is(1));
        MatcherAssert.assertThat(ItemStack.areItemStacksEqual(feather, result.get(0)), is(true));
    }
}
