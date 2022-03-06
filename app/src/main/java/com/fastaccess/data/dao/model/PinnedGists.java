// Generated file do not edit, generated by io.requery.processor.EntityProcessor
package com.fastaccess.data.dao.model;

import io.requery.Persistable;
import io.requery.meta.AttributeBuilder;
import io.requery.meta.NumericAttribute;
import io.requery.meta.QueryAttribute;
import io.requery.meta.StringAttribute;
import io.requery.meta.Type;
import io.requery.meta.TypeBuilder;
import io.requery.proxy.EntityProxy;
import io.requery.proxy.IntProperty;
import io.requery.proxy.LongProperty;
import io.requery.proxy.Property;
import io.requery.proxy.PropertyState;
import io.requery.util.function.Function;
import io.requery.util.function.Supplier;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import javax.annotation.Generated;

@Generated("io.requery.processor.EntityProcessor")
public class PinnedGists extends AbstractPinnedGists implements Persistable {
    public static final NumericAttribute<PinnedGists, Integer> ENTRY_COUNT = 
    new AttributeBuilder<PinnedGists, Integer>("entryCount", int.class)
    .setProperty(new IntProperty<PinnedGists>() {
        @Override
        public Integer get(PinnedGists entity) {
            return entity.entryCount;
        }

        @Override
        public void set(PinnedGists entity, Integer value) {
            if(value != null) {
                entity.entryCount = value;
            }
        }

        @Override
        public int getInt(PinnedGists entity) {
            return entity.entryCount;
        }

        @Override
        public void setInt(PinnedGists entity, int value) {
            entity.entryCount = value;
        }
    })
    .setPropertyName("entryCount")
    .setPropertyState(new Property<PinnedGists, PropertyState>() {
        @Override
        public PropertyState get(PinnedGists entity) {
            return entity.$entryCount_state;
        }

        @Override
        public void set(PinnedGists entity, PropertyState value) {
            entity.$entryCount_state = value;
        }
    })
    .setGenerated(false)
    .setReadOnly(false)
    .setLazy(false)
    .setNullable(true)
    .setUnique(false)
    .buildNumeric();

    public static final StringAttribute<PinnedGists, String> LOGIN = 
    new AttributeBuilder<PinnedGists, String>("login", String.class)
    .setProperty(new Property<PinnedGists, String>() {
        @Override
        public String get(PinnedGists entity) {
            return entity.login;
        }

        @Override
        public void set(PinnedGists entity, String value) {
            entity.login = value;
        }
    })
    .setPropertyName("login")
    .setPropertyState(new Property<PinnedGists, PropertyState>() {
        @Override
        public PropertyState get(PinnedGists entity) {
            return entity.$login_state;
        }

        @Override
        public void set(PinnedGists entity, PropertyState value) {
            entity.$login_state = value;
        }
    })
    .setGenerated(false)
    .setReadOnly(false)
    .setLazy(false)
    .setNullable(true)
    .setUnique(false)
    .buildString();

    public static final QueryAttribute<PinnedGists, Gist> GIST = 
    new AttributeBuilder<PinnedGists, Gist>("gist", Gist.class)
    .setProperty(new Property<PinnedGists, Gist>() {
        @Override
        public Gist get(PinnedGists entity) {
            return entity.gist;
        }

        @Override
        public void set(PinnedGists entity, Gist value) {
            entity.gist = value;
        }
    })
    .setPropertyName("gist")
    .setPropertyState(new Property<PinnedGists, PropertyState>() {
        @Override
        public PropertyState get(PinnedGists entity) {
            return entity.$gist_state;
        }

        @Override
        public void set(PinnedGists entity, PropertyState value) {
            entity.$gist_state = value;
        }
    })
    .setGenerated(false)
    .setReadOnly(false)
    .setLazy(false)
    .setNullable(true)
    .setUnique(false)
    .setConverter(new com.fastaccess.data.dao.converters.GistConverter())
    .build();

    public static final NumericAttribute<PinnedGists, Long> GIST_ID = 
    new AttributeBuilder<PinnedGists, Long>("gistId", long.class)
    .setProperty(new LongProperty<PinnedGists>() {
        @Override
        public Long get(PinnedGists entity) {
            return entity.gistId;
        }

        @Override
        public void set(PinnedGists entity, Long value) {
            if(value != null) {
                entity.gistId = value;
            }
        }

        @Override
        public long getLong(PinnedGists entity) {
            return entity.gistId;
        }

        @Override
        public void setLong(PinnedGists entity, long value) {
            entity.gistId = value;
        }
    })
    .setPropertyName("gistId")
    .setPropertyState(new Property<PinnedGists, PropertyState>() {
        @Override
        public PropertyState get(PinnedGists entity) {
            return entity.$gistId_state;
        }

        @Override
        public void set(PinnedGists entity, PropertyState value) {
            entity.$gistId_state = value;
        }
    })
    .setGenerated(false)
    .setReadOnly(false)
    .setLazy(false)
    .setNullable(true)
    .setUnique(false)
    .buildNumeric();

    public static final NumericAttribute<PinnedGists, Long> ID = 
    new AttributeBuilder<PinnedGists, Long>("id", long.class)
    .setProperty(new LongProperty<PinnedGists>() {
        @Override
        public Long get(PinnedGists entity) {
            return entity.id;
        }

        @Override
        public void set(PinnedGists entity, Long value) {
            entity.id = value;
        }

        @Override
        public long getLong(PinnedGists entity) {
            return entity.id;
        }

        @Override
        public void setLong(PinnedGists entity, long value) {
            entity.id = value;
        }
    })
    .setPropertyName("id")
    .setPropertyState(new Property<PinnedGists, PropertyState>() {
        @Override
        public PropertyState get(PinnedGists entity) {
            return entity.$id_state;
        }

        @Override
        public void set(PinnedGists entity, PropertyState value) {
            entity.$id_state = value;
        }
    })
    .setKey(true)
    .setGenerated(true)
    .setReadOnly(true)
    .setLazy(false)
    .setNullable(false)
    .setUnique(false)
    .buildNumeric();

    public static final Type<PinnedGists> $TYPE = new TypeBuilder<PinnedGists>(PinnedGists.class, "PinnedGists")
    .setBaseType(AbstractPinnedGists.class)
    .setCacheable(true)
    .setImmutable(false)
    .setReadOnly(false)
    .setStateless(false)
    .setView(false)
    .setFactory(new Supplier<PinnedGists>() {
        @Override
        public PinnedGists get() {
            return new PinnedGists();
        }
    })
    .setProxyProvider(new Function<PinnedGists, EntityProxy<PinnedGists>>() {
        @Override
        public EntityProxy<PinnedGists> apply(PinnedGists entity) {
            return entity.$proxy;
        }
    })
    .addAttribute(GIST)
    .addAttribute(GIST_ID)
    .addAttribute(ENTRY_COUNT)
    .addAttribute(LOGIN)
    .addAttribute(ID)
    .build();

    private PropertyState $entryCount_state;

    private PropertyState $login_state;

    private PropertyState $gist_state;

    private PropertyState $gistId_state;

    private PropertyState $id_state;

    private final transient EntityProxy<PinnedGists> $proxy = new EntityProxy<PinnedGists>(this, $TYPE);

    public PinnedGists() {
    }

    public int getEntryCount() {
        return $proxy.get(ENTRY_COUNT);
    }

    public void setEntryCount(int entryCount) {
        $proxy.set(ENTRY_COUNT, entryCount);
    }

    public String getLogin() {
        return $proxy.get(LOGIN);
    }

    public void setLogin(String login) {
        $proxy.set(LOGIN, login);
    }

    public Gist getGist() {
        return $proxy.get(GIST);
    }

    public void setGist(Gist gist) {
        $proxy.set(GIST, gist);
    }

    public long getGistId() {
        return $proxy.get(GIST_ID);
    }

    public void setGistId(long gistId) {
        $proxy.set(GIST_ID, gistId);
    }

    public long getId() {
        return $proxy.get(ID);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PinnedGists && ((PinnedGists)obj).$proxy.equals(this.$proxy);
    }

    @Override
    public int hashCode() {
        return $proxy.hashCode();
    }

    @Override
    public String toString() {
        return $proxy.toString();
    }
}
