package stone.mae2.client.render.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;

/**
 * Copied from AE2
 */
class FaultyCardBakedModel implements BakedModel {
    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    private final BakedModel baseModel;

    private final TextureAtlasSprite texture;

    private final AEColor[] colorCode;

    private final Cache<CacheKey, FaultyCardBakedModel> modelCache;

    private final ImmutableList<BakedQuad> generalQuads;

    FaultyCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture) {
        this(baseModel, texture, DEFAULT_COLOR_CODE, createCache());
    }

    private FaultyCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture, AEColor[] hash,
            Cache<CacheKey, FaultyCardBakedModel> modelCache) {
        this.baseModel = baseModel;
        this.texture = texture;
        this.colorCode = hash;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<CacheKey, FaultyCardBakedModel> createCache() {
        return CacheBuilder.newBuilder().maximumSize(100).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {

        List<BakedQuad> quads = this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, null);

        if (side != null) {
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size() + this.generalQuads.size());
        result.addAll(quads);
        result.addAll(this.generalQuads);
        return result;
    }

    private List<BakedQuad> buildGeneralQuads() {
        CubeBuilder builder = new CubeBuilder();

        builder.setTexture(this.texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                final AEColor color = this.colorCode[x + y * 4];

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(7 + x, 8 + 1 - y, 7.5f, 7 + x + 1, 8 + 1 - y + 1, 8.5f);
            }
        }

        return builder.getOutput();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel level,
                    LivingEntity entity, int seed) {
                try {
                    if (stack.getItem() instanceof IMemoryCard memoryCard) {
                        final AEColor[] colors = memoryCard.getColorCode(stack);

                        return FaultyCardBakedModel.this.modelCache.get(new CacheKey(colors),
                                () -> new FaultyCardBakedModel(FaultyCardBakedModel.this.baseModel,
                                        FaultyCardBakedModel.this.texture, colors,
                                        FaultyCardBakedModel.this.modelCache));
                    }
                } catch (ExecutionException e) {
                    AELog.error(e);
                }

                return FaultyCardBakedModel.this;
            }
        };
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }

    private static class CacheKey {
        private final AEColor[] key;

        CacheKey(AEColor[] key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.key);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return Arrays.equals(this.key, other.key);
        }
    }
}
