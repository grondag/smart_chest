/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.facility.init;

import static grondag.facility.Facility.REG;

import java.util.function.Predicate;

import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;

import net.fabricmc.fabric.api.block.FabricBlockSettings;

import grondag.facility.storage.PortableStore;
import grondag.facility.storage.bulk.PortableTankItem;
import grondag.facility.storage.bulk.TankBlock;
import grondag.facility.storage.bulk.TankBlockEntity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.storage.bulk.SimpleTank;
import grondag.fluidity.wip.api.transport.CarrierConnector;
import grondag.xm.api.block.XmBlockRegistry;
import grondag.xm.api.block.XmProperties;
import grondag.xm.api.connect.species.SpeciesProperty;
import grondag.xm.api.modelstate.primitive.PrimitiveStateFunction;
import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.simple.CubeWithFace;
import grondag.xm.api.texture.XmTextures;

@SuppressWarnings("unchecked")
public enum TankBlocks {
	;

	public static final Predicate<Article> FILTER_TYPE = d -> d.type() == ArticleType.FLUID;

	public static final TankBlock TANK = REG.blockNoItem("tank", new TankBlock(FabricBlockSettings.of(Material.METAL).strength(1, 1).build(), TankBlocks::tankBe, false));
	public static final BlockEntityType<TankBlockEntity> TANK_BLOCK_ENTITY_TYPE = REG.blockEntityType("tank", TankBlocks::tankBe, TANK);
	private static TankBlockEntity tankBe() {
		return new TankBlockEntity(TANK_BLOCK_ENTITY_TYPE, () -> new SimpleTank(Fraction.of(32)).filter(FILTER_TYPE), "TANK ");
	}

	public static final PortableTankItem PORTABLE_TANK_ITEM = REG.item("tank", new PortableTankItem(TANK, REG.itemSettings().maxCount(1).maxDamage(32768)));

	static {
		PORTABLE_TANK_ITEM.appendBlocks(Item.BLOCK_ITEMS, PORTABLE_TANK_ITEM);

		CarrierConnector.CARRIER_CONNECTOR_COMPONENT.addProvider(TANK);
		Store.STORAGE_COMPONENT.registerProvider(ctx -> ((TankBlockEntity) ctx.blockEntity()).getEffectiveStorage(), TANK);
		Store.INTERNAL_STORAGE_COMPONENT.registerProvider(ctx -> ((TankBlockEntity) ctx.blockEntity()).getInternalStorage(), TANK);
		Store.STORAGE_COMPONENT.registerProvider(ctx -> new PortableStore(new SimpleTank(Fraction.of(32)), ctx), PORTABLE_TANK_ITEM);

		final XmPaint basePaint = XmPaint.finder()
				.textureDepth(2)
				.texture(0, XmTextures.TILE_NOISE_SUBTLE)
				.textureColor(0, 0xFF404045)
				.texture(1, XmTextures.BORDER_WEATHERED_LINE)
				.blendMode(1, PaintBlendMode.TRANSLUCENT)
				.textureColor(1, 0xA0000000)
				.find();

		XmBlockRegistry.addBlockStates(TANK, bs -> PrimitiveStateFunction.builder()
				.withJoin(TankBlock.JOIN_TEST)
				.withUpdate(SpeciesProperty.SPECIES_MODIFIER)
				.withUpdate(XmProperties.FACE_MODIFIER)
				.withDefaultState(XmProperties.FACE_MODIFIER.mutate(SpeciesProperty.SPECIES_MODIFIER.mutate(
						CubeWithFace.INSTANCE.newState()
						.paintAll(basePaint), bs), bs))
				.build());
	}
}
