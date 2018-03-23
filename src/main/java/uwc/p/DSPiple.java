package uwc.p;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by steven on 08/05/2017.
 */
class DSPiple<T extends Element> implements TypeAdapterFactory {
    private static Gson sChannel = new Gson();
    private Gson mPort = null;
    protected Class<T> type = null;

    @Override
    public <tT> TypeAdapter<tT> create(final Gson gson, final TypeToken<tT> tType) {
        if (!Collection.class.isAssignableFrom(tType.getRawType())) {
            return sChannel.getAdapter(tType);
        }

        return new TypeAdapter<tT>() {
            @Override
            public void write(JsonWriter out, tT value) throws IOException {
                Collection<T> collection = (Collection<T>) value;
                out.beginArray();
                for (T element : collection)
                    out.value(gson.toJson(element.getValue(), type));
                out.endArray();
                out.flush();
            }

            @Override
            public tT read(JsonReader in) throws IOException {
                Collection<T> collection = new ArrayList<T>();
                in.beginArray();
                while (in.hasNext())
                    collection.add(gson.fromJson(in.nextString(), type));
                in.endArray();
                return (tT) collection;
            }
        };
    }

    private Gson port() {
        if (null == mPort)
            mPort = new GsonBuilder().registerTypeAdapterFactory(this).create();
        return mPort;
    }

    protected DSPiple(Class<T> type) {
        this.type = type;
    }

    protected Collection<T> deserialize(String tag, Collection<T> def) {
        String json = Source.Holder.obtain().getString(tag);
        Collection<T> ret = port().fromJson(json, new TypeToken<Collection<T>>() {}.getType());
        if (null == ret) ret = def;
        return ret;
    }

    protected void serialize(Collection<T> collection, String tag) {
        String json = port().toJson(collection);
        Source.Holder.obtain().putString(tag, json);
    }
}
