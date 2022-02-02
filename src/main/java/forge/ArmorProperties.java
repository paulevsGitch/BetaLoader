package forge;

public class ArmorProperties {
    public boolean allowRegularComputation;
    public int damageRemove;
    
    public ArmorProperties() {}
    
    public ArmorProperties(final int damageRemove, final boolean allowRegularCompuation) {
        this.allowRegularComputation = allowRegularCompuation;
        this.damageRemove = damageRemove;
    }
}
