package romelo333.notenoughwands.items;


import net.minecraft.ChatFormat;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import romelo333.notenoughwands.Config;
import romelo333.notenoughwands.Configuration;
import romelo333.notenoughwands.varia.Tools;

import java.util.List;

public class PotionWand extends GenericWand {
    private boolean allowPassive = true;
    private boolean allowHostile = true;
    private float difficultyMult = 0.0f;
    private float diffcultyAdd = 1.0f;

    public PotionWand() {
        super(100);
        setup("potion_wand").xpUsage(10).loot(3);
    }

    @Override
    public void initConfig(Configuration cfg) {
        super.initConfig(cfg, 200, 100000, 100, 200000, 50, 500000);
        allowPassive =  cfg.get(Config.CATEGORY_WANDS, getConfigPrefix() + "_allowPassive", allowPassive, "Allow freeze passive mobs").getBoolean();
        allowHostile =  cfg.get(Config.CATEGORY_WANDS, getConfigPrefix() + "_allowHostile", allowHostile, "Allow freeze hostile mobs").getBoolean();
        difficultyMult = (float) cfg.get(Config.CATEGORY_WANDS, getConfigPrefix() + "_difficultyMult", difficultyMult, "Multiply the HP of a mob with this number to get the difficulty scale that affects XP/RF usage (a final result of 1.0 means that the default XP/RF is used)").getDouble();
        diffcultyAdd = (float) cfg.get(Config.CATEGORY_WANDS, getConfigPrefix() + "_diffcultyAdd", diffcultyAdd, "Add this to the HP * difficultyMult to get the final difficulty scale that affects XP/RF usage (a final result of 1.0 means that the default XP/RF is used)").getDouble();
    }

    private String getEffectName(StatusEffectInstance potioneffect){
        String s1 = I18n.translate(potioneffect.getTranslationKey()).trim();
        if (potioneffect.getAmplifier() > 0) {
            s1 = s1 + " " + I18n.translate("potion.potency." + potioneffect.getAmplifier()).trim();
        }
        if (potioneffect.getDuration() > 20) {
            s1 = s1 + " (" + StatusEffectUtil.durationToString(potioneffect, potioneffect.getDuration()) + ")";
        }
        return s1;
    }

    @Override
    public void buildTooltip(ItemStack stack, World player, List<Component> list, TooltipContext b) {
        super.buildTooltip(stack, player, list, b);
        list.add(new TextComponent("Left click on creature to apply effect"));
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound==null){
            list.add(new TextComponent(ChatFormat.YELLOW+"No effects. Combine with potion"));
            list.add(new TextComponent(ChatFormat.YELLOW+"in crafting table to add effect"));
            return;
        }
        ListTag effects = (ListTag) tagCompound.getTag("effects");
        if (effects == null || effects.size()==0){
            list.add(new TextComponent(ChatFormat.YELLOW+"No effects. Combine with potion"));
            list.add(new TextComponent(ChatFormat.YELLOW+"in crafting table to add effect"));
            return;
        }
        list.add(new TextComponent(ChatFormat.YELLOW+"Combine with empty bottle"));
        list.add(new TextComponent(ChatFormat.YELLOW+"to clear effects"));
        int mode = getMode(stack);
        for (int i=0;i<effects.size();i++) {
            CompoundTag effecttag = effects.getCompoundTag(i);
            StatusEffectInstance effect = StatusEffectInstance.deserialize(effecttag);
            if (i==mode){
                list.add(new TextComponent("    + " + ChatFormat.GREEN + getEffectName(effect)));
            } else {
                list.add(new TextComponent("    " + ChatFormat.GRAY+getEffectName(effect)));
            }
        }
    }

    @Override
    public void toggleMode(PlayerEntity player, ItemStack stack) {
        int mode = getMode(stack);
        mode++;
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound==null){
            return;
        }
        ListTag effects = (ListTag) tagCompound.getTag("effects");
        if (effects == null || effects.size()==0){
            return;
        }
        if (mode >= effects.size()) {
            mode = 0;
        }
        CompoundTag effecttag = effects.getCompoundTag(mode);
        StatusEffectInstance effect = StatusEffectInstance.deserialize(effecttag);
        Tools.notify(player, "Switched to " + getEffectName(effect) + " mode");
        Tools.getTagCompound(stack).putInt("mode", mode);
    }

    private int getMode(ItemStack stack) {
        return Tools.getTagCompound(stack).getInt("mode");
    }


    private void addeffect(LivingEntity entity, ItemStack wand, PlayerEntity player){
        CompoundTag tagCompound = wand.getTag();
        if (tagCompound==null){
            Tools.error(player, "There are no effects in this wand!");
            return;
        }
        ListTag effects = (ListTag) tagCompound.getTag("effects");
        if (effects == null || effects.size()==0){
            Tools.error(player, "There are no effects in this wand!");
            return;
        }
        CompoundTag effecttag = effects.getCompoundTag(getMode(wand));
        StatusEffectInstance effect = StatusEffectInstance.deserialize(effecttag);
        entity.addPotionEffect(effect);
    }

    @Override
    public boolean interactWithEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (!player.getEntityWorld().isClient) {
            if (entity != null) {
                if ((!allowHostile) && entity instanceof Monster) {
                    Tools.error(player, "It is not possible to add effects to hostile mobs with this wand!");
                    return true;
                }
                if ((!allowPassive) && !(entity instanceof Monster)) {
                    Tools.error(player, "It is not possible to add effects to passive mobs with this wand!");
                    return true;
                }

                float difficultyScale = entity.getHealthMaximum() * difficultyMult + diffcultyAdd;
                if (!checkUsage(stack, player, difficultyScale)) {
                    return true;
                }

                addeffect(entity, stack, player);
                registerUsage(stack, player, difficultyScale);
            } else {
                Tools.error(player, "Please select a living entity!");
            }
        }
        return true;
    }
}