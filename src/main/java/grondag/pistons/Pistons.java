/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.pistons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Pistons implements ModInitializer {
    public static final String MODID = "pistons-hd";

    public static final Logger LOG = LogManager.getLogger("pistons-hd");
    
    private static ItemGroup itemGroup;

    @Override
    public void onInitialize() {
        Configurator.init();
        itemGroup = FabricItemGroupBuilder.create(id("item_group"))
//          .icon(() -> new ItemStack(Registry.ITEM.get(stackId)))
//          .appendItems(Xb::stackAppender)
                .build();
        PistonBlocks.init();
    }
    
    public static String idString(String path) {
        return MODID + ":" + path;
    }
    
    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
    
    public static Block register(String idString, Block block) {
        Identifier id = id(idString);
        return Registry.BLOCK.add(id, block);
    }
    
    public static Item registerItem(String idString, Block block) {
        Identifier id = id(idString);
        return Registry.ITEM.add(id, new BlockItem(block, new Item.Settings().maxCount(64).group(itemGroup)));
//        items.add(Registry.ITEM.add(id, new BlockItem(block, new Item.Settings().maxCount(64).group(itemGroup))));
    }
}
