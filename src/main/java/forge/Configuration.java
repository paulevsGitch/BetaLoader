package forge;

import java.util.Arrays;
import net.minecraft.block.BlockBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

public class Configuration {
    public static final int GENERAL_PROPERTY = 0;
    public static final int BLOCK_PROPERTY = 1;
    public static final int ITEM_PROPERTY = 2;
    public TreeMap<String, Property> blockProperties;
    public TreeMap<String, Property> itemProperties;
    public TreeMap<String, Property> generalProperties;
    private boolean[] configBlocks;
    File file;
    
    public Configuration(final File file) {
        this.configBlocks = null;
        this.blockProperties = new TreeMap<>();
        this.itemProperties = new TreeMap<>();
        this.generalProperties = new TreeMap<>();
        this.file = file;
    }
    
    public Property getOrCreateBlockIdProperty(final String key, final int defaultId) {
        if (this.configBlocks == null) {
            this.configBlocks = new boolean[BlockBase.BY_ID.length];
            Arrays.fill(this.configBlocks, false);
        }
        if (this.blockProperties.containsKey(key)) {
            final Property property = this.getOrCreateIntProperty(key, 1, defaultId);
            this.configBlocks[Integer.parseInt(property.value)] = true;
            return property;
        }
        final Property property = new Property();
        this.blockProperties.put(key, property);
        property.name = key;
        if (BlockBase.BY_ID[defaultId] == null && !this.configBlocks[defaultId]) {
            property.value = Integer.toString(defaultId);
            this.configBlocks[defaultId] = true;
            return property;
        }
        for (int j = BlockBase.BY_ID.length - 1; j >= 0; --j) {
            if (BlockBase.BY_ID[j] == null && !this.configBlocks[j]) {
                property.value = Integer.toString(j);
                this.configBlocks[j] = true;
                return property;
            }
        }
        throw new RuntimeException("No more block ids available for " + key);
    }
    
    public Property getOrCreateIntProperty(final String key, final int kind, final int defaultValue) {
        final Property prop = this.getOrCreateProperty(key, kind, Integer.toString(defaultValue));
        try {
            Integer.parseInt(prop.value);
            return prop;
        }
        catch (NumberFormatException e) {
            prop.value = Integer.toString(defaultValue);
            return prop;
        }
    }
    
    public Property getOrCreateBooleanProperty(final String key, final int kind, final boolean defaultValue) {
        final Property prop = this.getOrCreateProperty(key, kind, Boolean.toString(defaultValue));
        if ("true".equalsIgnoreCase(prop.value) || "false".equalsIgnoreCase(prop.value)) {
            return prop;
        }
        prop.value = Boolean.toString(defaultValue);
        return prop;
    }
    
    public Property getOrCreateProperty(final String key, final int kind, final String defaultValue) {
        TreeMap<String, Property> source = null;
        switch (kind) {
            case GENERAL_PROPERTY: {
                source = this.generalProperties;
                break;
            }
            case BLOCK_PROPERTY: {
                source = this.blockProperties;
                break;
            }
            case ITEM_PROPERTY: {
                source = this.itemProperties;
                break;
            }
        }
        if (source.containsKey(key)) {
            return source.get(key);
        }
        if (defaultValue != null) {
            final Property property = new Property();
            source.put(key, property);
            property.name = key;
            property.value = defaultValue;
            return property;
        }
        return null;
    }
    
    public void load() {
        try {
            if (this.file.getParentFile() != null) {
                this.file.getParentFile().mkdirs();
            }
            if (!this.file.exists() && !this.file.createNewFile()) {
                return;
            }
            if (this.file.canRead()) {
                final FileInputStream fileinputstream = new FileInputStream(this.file);
                final BufferedReader buffer = new BufferedReader(new InputStreamReader(fileinputstream, "8859_1"));
                TreeMap<String, Property> currentMap = null;
                while (true) {
                    final String line = buffer.readLine();
                    if (line == null) {
                        break;
                    }
                    int nameStart = -1;
                    int nameEnd = -1;
                    boolean skip = false;
                    for (int i = 0; i < line.length() && !skip; ++i) {
                        if (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '.') {
                            if (nameStart == -1) {
                                nameStart = i;
                            }
                            nameEnd = i;
                        }
                        else if (!Character.isWhitespace(line.charAt(i))) {
                            switch (line.charAt(i)) {
                                case '#': {
                                    skip = true;
                                    break;
                                }
                                case '{': {
                                    final String scopeName = line.substring(nameStart, nameEnd + 1);
                                    if (scopeName.equals("general")) {
                                        currentMap = this.generalProperties;
                                        break;
                                    }
                                    if (scopeName.equals("block")) {
                                        currentMap = this.blockProperties;
                                        break;
                                    }
                                    if (scopeName.equals("item")) {
                                        currentMap = this.itemProperties;
                                        break;
                                    }
                                    throw new RuntimeException("unknown section " + scopeName);
                                }
                                case '}': {
                                    currentMap = null;
                                    break;
                                }
                                case '=': {
                                    final String propertyName = line.substring(nameStart, nameEnd + 1);
                                    if (currentMap == null) {
                                        throw new RuntimeException("property " + propertyName + " has no scope");
                                    }
                                    final Property prop = new Property();
                                    prop.name = propertyName;
                                    prop.value = line.substring(i + 1);
                                    i = line.length();
                                    currentMap.put(propertyName, prop);
                                    break;
                                }
                                default: {
                                    throw new RuntimeException("unknown character " + line.charAt(i));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            if (this.file.getParentFile() != null) {
                this.file.getParentFile().mkdirs();
            }
            if (!this.file.exists() && !this.file.createNewFile()) {
                return;
            }
            if (this.file.canWrite()) {
                final FileOutputStream fileoutputstream = new FileOutputStream(this.file);
                final BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fileoutputstream, "8859_1"));
                buffer.write("# Configuration file\r\n");
                buffer.write("# Generated on " + DateFormat.getInstance().format(new Date()) + "\r\n");
                buffer.write("\r\n");
                buffer.write("###########\r\n");
                buffer.write("# General #\r\n");
                buffer.write("###########\r\n\r\n");
                buffer.write("general {\r\n");
                this.writeProperties(buffer, this.generalProperties.values());
                buffer.write("}\r\n\r\n");
                buffer.write("#########\r\n");
                buffer.write("# Block #\r\n");
                buffer.write("#########\r\n\r\n");
                buffer.write("block {\r\n");
                this.writeProperties(buffer, this.blockProperties.values());
                buffer.write("}\r\n\r\n");
                buffer.write("########\r\n");
                buffer.write("# Item #\r\n");
                buffer.write("########\r\n\r\n");
                buffer.write("item {\r\n");
                this.writeProperties(buffer, this.itemProperties.values());
                buffer.write("}\r\n\r\n");
                buffer.close();
                fileoutputstream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeProperties(final BufferedWriter buffer, final Collection<Property> props) throws IOException {
        for (final Property property : props) {
            if (property.comment != null) {
                buffer.write("   # " + property.comment + "\r\n");
            }
            buffer.write("   " + property.name + "=" + property.value);
            buffer.write("\r\n");
        }
    }
}
