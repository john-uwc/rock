package uwc.carry;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author steven
 */
class Distributor implements Tunnel.Coordinator {

    @Override
    public void refresh(Collection<SoftReference<Membrane>> membranes) {
        for (SoftReference<Membrane> membrane : membranes)
            if (null == membrane.get()) membranes.remove(membrane);
        Collections.sort((List<SoftReference<Membrane>>)membranes, new Comparator<SoftReference<Membrane>>() {
            @Override
            public int compare(SoftReference<Membrane> o1, SoftReference<Membrane> o2) {
                return o1.get().compareTo(o2.get());
            }
        });
    }


    @Override
    public void schedule(Object obj, Collection<SoftReference<Membrane>> membranes) {
        for (SoftReference<Membrane> membrane : membranes) {
            if (null != membrane.get()) membrane.get().absorb(obj);
        }
    }
}
