package cofh.redstonearsenal.util;

import cofh.core.compat.curios.CuriosProxy;
import cofh.redstonearsenal.capability.IFluxShieldedItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

public class FluxShieldingHelper {

    public static final String TAG_FLUX_SHIELD = "FluxShield";

    public static ItemStack findShieldedItem(LivingEntity entity) {

        Predicate<ItemStack> isShieldedItem = i -> i.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.currCharges(entity) > 0).orElse(false);

        // HELD
        ItemStack mainHand = entity.getMainHandItem();
        if (isShieldedItem.test(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = entity.getOffhandItem();
        if (isShieldedItem.test(offHand)) {
            return offHand;
        }
        //ARMOR
        for (ItemStack piece : entity.getArmorSlots()) {
            if (isShieldedItem.test(piece)) {
                return piece;
            }
        }
        //CURIOS
        final ItemStack[] retStack = {ItemStack.EMPTY};
        CuriosProxy.getAllWorn(entity).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); ++i) {
                ItemStack slot = c.getStackInSlot(i);
                if (isShieldedItem.test(slot)) {
                    retStack[0] = slot;
                    return;
                }
            }
        });
        return retStack[0];
    }

    public static int[] countCharges(LivingEntity entity) {

        if (entity == null) {
            return new int[]{0, 0};
        }
        final int[] counter = {0, 0};
        Consumer<ItemStack> count = i -> {
            counter[0] += getCurrCharges(entity, i);
            counter[1] += getMaxCharges(entity, i);
        };

        // HELD & ARMOR
        for (ItemStack item : entity.getArmorSlots()) {
            count.accept(item);
        }
        // CURIOS
        CuriosProxy.getAllWorn(entity).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); ++i) {
                count.accept(c.getStackInSlot(i));
            }
        });
        return counter;
    }

    public static boolean hasFluxShieldCharge(LivingEntity entity) {

        return !findShieldedItem(entity).isEmpty();
    }

    public static boolean useFluxShieldCharge(LivingEntity entity) {

        return useFluxShieldCharge(entity, findShieldedItem(entity));
    }

    public static boolean useFluxShieldCharge(LivingEntity entity, ItemStack stack) {

        if (stack.isEmpty()) {
            return false;
        }
        LazyOptional<IFluxShieldedItem> cap = stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY);
        if (cap.map(c -> c.useCharge(entity)).orElse(false)) {
            if (entity instanceof ServerPlayerEntity) {
                cap.ifPresent(c -> c.scheduleUpdate((ServerPlayerEntity) entity));
            }
            onUseFluxShieldCharge(entity);
            return true;
        }
        return false;
    }

    public static int getCurrCharges(LivingEntity entity, ItemStack stack) {

        return stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.currCharges(entity)).orElse(0);
    }

    public static int getMaxCharges(LivingEntity entity, ItemStack stack) {

        return stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.maxCharges(entity)).orElse(0);
    }

    public static boolean equalCharges(LivingEntity entity, ItemStack a, ItemStack b) {

        return getCurrCharges(entity, a) == getCurrCharges(entity, b) && getMaxCharges(entity, a) == getMaxCharges(entity, b);
    }

    protected static void onUseFluxShieldCharge(LivingEntity entity) {

        SoundCategory category = SoundCategory.NEUTRAL;
        if (entity instanceof PlayerEntity) {
            category = SoundCategory.PLAYERS;
        } else if (entity instanceof MonsterEntity) {
            category = SoundCategory.HOSTILE;
        }
        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, category, 1.0F, 1.0F); //TODO: sound event

        AxisAlignedBB bounds = entity.getBoundingBox();
        Vector3d pos = bounds.getCenter();
        if (!entity.level.isClientSide()) {
            ((ServerWorld) entity.level).sendParticles(RedstoneParticleData.REDSTONE, pos.x(), pos.y(), pos.z(), 20, bounds.getXsize() * 0.5 + 0.2, bounds.getYsize() * 0.5 + 0.2, bounds.getZsize() * 0.5 + 0.2, 0);
        }
    }

}
