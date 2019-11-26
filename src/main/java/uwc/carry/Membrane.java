package uwc.carry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author steven
 */
abstract class Membrane implements Comparable<Membrane> {
    protected final String TAG = getClass().getSimpleName();

    /**
     * the summary of membrane
     *
     * @author steven
     */
    public static class Brief {
        public static final class Builder {
            public final Brief create() {
                return brief;
            }

            public Builder setPriority(int priority) {
                brief.mPriority = priority;
                return this;
            }

            public Builder setAbsorbRange(Class<?> range[]) {
                brief.mAbsorbRange.clear();
                brief.mAbsorbRange.addAll(Arrays.asList(range));
                return this;
            }

            private final Brief brief = new Brief();
        }

        private HashSet<Class<?>> mAbsorbRange = new HashSet<>();
        private int mPriority = Integer.MAX_VALUE;

        private Brief() {
        }
    }


    private Brief mBrief = new Brief.Builder().create();

    protected Membrane(Brief brief) {
        if (null != brief) this.mBrief = brief;
    }

    /**
     * named absorb. it is invoked by the tunnel's coordinator
     * it is in order to handle object by the membrane
     *
     * @param obj
     */
    final void absorb(final Object obj) {
        if (trap(obj)) permeate(obj);
    }

    /**
     * handle the object
     *
     * @param obj
     */
    protected abstract void permeate(Object obj);

    /**
     * decide to whether the object can be handled
     *
     * @param obj
     */
    protected boolean trap(Object obj) {
        boolean may = mBrief.mAbsorbRange.isEmpty();
        Iterator<Class<?>> it = mBrief.mAbsorbRange.iterator();
        while (it.hasNext() && !(may = it.next().isInstance(obj))) ;
        return may;
    }


    @Override
    public int compareTo(Membrane o) {
        return o.mBrief.mPriority <= mBrief.mPriority ? 1 : -1;
    }
}
