package uwc.event;

/**
 * Created by steven on 13/06/2017.
 */
public interface Receptor {
    public <T extends Ligand> void absorb(T ligand);
}
