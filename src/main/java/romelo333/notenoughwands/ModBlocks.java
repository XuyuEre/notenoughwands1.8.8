package romelo333.notenoughwands;


import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import romelo333.notenoughwands.blocks.LightBlock;
import romelo333.notenoughwands.blocks.LightTE;

public class ModBlocks {
    public static LightBlock lightBlock;

    public static BlockEntityType<LightTE> LIGHT;

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        BlockEntityType<T> blockEntityType = builder.method_11034(null);
        Registry.register(Registry.BLOCK_ENTITY, NotEnoughWands.MODID + ":" + name, blockEntityType);
        return blockEntityType;
    }

    public static void init() {
        LIGHT = register("light", BlockEntityType.Builder.create(LightTE::new));

    }

}
