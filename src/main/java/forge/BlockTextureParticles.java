package forge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ParticleBase;

public class BlockTextureParticles {
    public String texture;
    public List<ParticleBase> effects;
    
    public BlockTextureParticles() {
        this.effects = new ArrayList<>();
    }
}
