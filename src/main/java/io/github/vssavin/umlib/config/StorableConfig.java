package io.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author vssavin on 17.12.21
 */
public class StorableConfig {
    @IgnoreField private static final Logger LOG = LoggerFactory.getLogger(StorableConfig.class);
    @IgnoreField private String configFile = "" ;
    @IgnoreField private String namePrefix = "";

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public void store() {
        store(getProperties());
    }

    private void store(Map<String, String> propertiesMap) {
        SortedProperties props = new SortedProperties();
        if (propertiesMap != null) {

            try(FileReader reader = new FileReader(configFile)) {
                props.load(reader);
            } catch (IOException e) {
                LOG.error("Reading properties error!", e);
            }

            props.putAll(propertiesMap);

            try(FileWriter writer = new FileWriter(configFile)) {
                props.store(writer, null);
            } catch (IOException e) {
                LOG.error("Writing properties error!", e);
            }


        }
    }

    private Map<String, String> getProperties() {
        Map<String, String> fieldsMap = new TreeMap<>();
        try {
            for(Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getDeclaredAnnotation(IgnoreField.class) == null) {
                    fieldsMap.put(namePrefix + "." + field.getName(), String.valueOf(field.get(this)));
                }
            }
        } catch (IllegalAccessException e) {
            LOG.error("Field processing error!", e);
        } catch (Exception e) {
            LOG.error("Getting properties error!", e);
        }

        return fieldsMap;
    }

    private static class SortedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        @Override
        public Set<Object> keySet() {
            return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {

            Set<Map.Entry<Object, Object>> set1 = super.entrySet();
            Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<>(set1.size());

            Iterator<Map.Entry<Object, Object>> iterator =
                    set1.stream().sorted(Comparator.comparing(o -> o.getKey().toString())).iterator();

            while (iterator.hasNext())
                set2.add(iterator.next());

            return set2;
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    }



}
