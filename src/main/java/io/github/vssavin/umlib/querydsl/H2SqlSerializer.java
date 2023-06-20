package io.github.vssavin.umlib.querydsl;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.SerializerBase;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathType;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author vssavin on 19.06.2023
 */
public class H2SqlSerializer extends SQLSerializer {
    private static final Logger logger = LoggerFactory.getLogger(H2SqlSerializer.class);

    public H2SqlSerializer(Configuration conf) {
        super(conf);
    }

    public H2SqlSerializer(Configuration conf, boolean dml) {
        super(conf, dml);
    }

    @Override
    protected void serializeForUpdate(QueryMetadata metadata, RelationalPath<?> entity,
                                      Map<Path<?>, Expression<?>> updates) {
        this.entity = entity;

        serialize(QueryFlag.Position.START, metadata.getFlags());

        if (!serialize(QueryFlag.Position.START_OVERRIDE, metadata.getFlags())) {
            append(templates.getUpdate());
        }
        serialize(QueryFlag.Position.AFTER_SELECT, metadata.getFlags());

        boolean originalDmlWithSchema = dmlWithSchema;
        dmlWithSchema = true;
        handle(entity);
        dmlWithSchema = originalDmlWithSchema;
        append("\n");
        append(templates.getSet());
        boolean first = true;
        skipParent = true;
        for (final Map.Entry<Path<?>,Expression<?>> update : updates.entrySet()) {
            if (!first) {
                append(COMMA);
            }
            handle(update.getKey());
            append(" = ");
            if (!useLiterals && update.getValue() instanceof Constant<?>) {
                constantPaths.add(update.getKey());
            }

            String value = "";
            if (update.getValue() instanceof Path ) {
                if (((Path<?>) update.getValue()).getMetadata().getPathType() == PathType.VARIABLE ||
                        ((Path<?>) update.getValue()).getMetadata().getPathType() == PathType.PROPERTY) {

                    value = "'" + ((Path<?>) update.getValue()).getMetadata().getName() + "'";
                }
            }

            handle(update.getValue());

            modifyBuilder(value);

            first = false;
        }
        skipParent = false;

        if (metadata.getWhere() != null) {
            serializeForWhere(metadata);
        }
    }

    private void modifyBuilder(String value) {
        if (!value.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            try {
                Class<?> c = SerializerBase.class;
                Field builderField = c.getDeclaredField("builder");
                builderField.setAccessible(true);
                builder = (StringBuilder) builderField.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("Access to field 'builder' error! ", e);
            }
            if (builder.charAt(builder.length() - 1) == '"') {
                builder.replace(builder.length() - value.length(), builder.length(), value);
            } else {
                builder.replace(builder.length() - value.length() + 2, builder.length(), value);
            }
        }
    }
}
