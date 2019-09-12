package grondag.pistons;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class PistonBlocks {

    public static final Block MOVING_DOUBLE_PISTON = Pistons.register("moving_double_piston", new DoublePistonExtensionBlock(FabricBlockSettings.of(Material.PISTON).strength(-1.0F, 0).dynamicBounds().dropsNothing().build()));
    public static final Block DOUBLE_PISTON = Pistons.register("double_piston", new DoublePistonBlock(false, FabricBlockSettings.of(Material.PISTON).strength(0.5F, 0.5F).build()));
    public static final Block STICKY_DOUBLE_PISTON = Pistons.register("sticky_double_piston", new DoublePistonBlock(true, FabricBlockSettings.of(Material.PISTON).strength(0.5F, 0.5F).build()));
    public static final Block DOUBLE_PISTON_HEAD = Pistons.register("double_piston_head", new DoublePistonHeadBlock(FabricBlockSettings.of(Material.PISTON).strength(0.5F, 0.5F).dropsNothing().build()));
    public static final Item DOUBLE_PISTON_ITEM = Pistons.registerItem("double_piston", DOUBLE_PISTON);
    public static final Item STICKY_DOUBLE_PISTON_ITEM = Pistons.registerItem("sticky_double_piston", STICKY_DOUBLE_PISTON);
    
    public static final BlockEntityType<DoublePistonBlockEntity> DOUBLE_PISTON_TYPE = BlockEntityType.Builder.create(DoublePistonBlockEntity::new, MOVING_DOUBLE_PISTON).build(null);

    public static void init() {
        Registry.register(Registry.BLOCK_ENTITY, Pistons.id("double_piston"), DOUBLE_PISTON_TYPE);
    }
}
