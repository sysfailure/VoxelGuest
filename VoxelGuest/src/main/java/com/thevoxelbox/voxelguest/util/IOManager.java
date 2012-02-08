package com.thevoxelbox.voxelguest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.thevoxelbox.voxelguest.VoxelGuest;

public class IOManager {

    private static String directory = "plugins/VoxelGuest/data";

    public IOManager(String dir) {
        directory = dir;
    }

    public static Map<String, Object> load(String target) {
        return load(target, "");
    }

    public static Map<String, Object> load(String target, String destination) {
        // Note: Destination is appended to plugins/VoxelGuest/data
        // For example, when destination is "/channels",
        // the target directory will be "plugins/VoxelGuest/data/channels/"

        Map<String, Object> map = new HashMap<String, Object>();
        map.clear();

        File f = new File(directory + destination + "/" + target + ".properties");
        FileInputStream fi = null;

        if (f.exists()) {
            try {
                Properties props = new Properties();
                fi = new FileInputStream(f);

                props.load(fi);

                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String key = entry.getKey().toString();

                    try {
                        Integer i = Integer.parseInt(entry.getValue().toString());
                        map.put(key, i);
                    } catch (NumberFormatException ex) {
                        if (entry.getValue().toString().equals(Boolean.TRUE.toString()) || entry.getValue().toString().equals(Boolean.FALSE.toString())) {
                            Boolean bool = Boolean.parseBoolean(entry.getValue().toString());
                            map.put(key, bool);
                            continue;
                        }

                        if (entry.getValue().toString().toLowerCase().startsWith("%list={") && entry.getValue().toString().toLowerCase().endsWith("}%")) {
                            map.put(key, parseList(entry.getValue().toString().toLowerCase().replaceFirst("%list={", "").replace("}%", "")));
                            continue;
                        }

                        map.put(key, entry.getValue().toString());
                        continue;
                    }
                }
            } catch (FileNotFoundException ex) {
                VoxelGuest.log("File not found: " + f.getAbsolutePath(), 2);
            } catch (IOException ex) {
                VoxelGuest.log("Incorrectly loaded properties from " + f.getAbsolutePath(), 2);
            } finally {
                try {
                    if (fi != null) {
                        fi.close();
                    }
                } catch (IOException ex) {
                    VoxelGuest.log("##### -- FATAL ERROR -- ##### Failed to store data to " + f.getAbsolutePath(), 2);
                    ex.printStackTrace();
                }
            }
        }

        return map;
    }

    public static void save(String target, Map<String, Object> data) {
        save(target, data, "");
    }

    public static void save(String target, Map<String, Object> data, String destination) {
        // Note: Destination is appended to plugins/VoxelGuest/data
        // For example, when destination is "/channels",
        // the target directory will be "plugins/VoxelGuest/data/channels/"

        File f = new File(directory + destination + "/" + target + ".properties");
        FileOutputStream fo = null;

        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            Properties props = new Properties();
            fo = new FileOutputStream(f);

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();

                // Write out Object[]
                if (entry.getValue() instanceof Object[]) {
                    Object[] obj = (Object[]) entry.getValue();
                    props.setProperty(key, writeList(obj));
                    continue;
                }

                props.setProperty(key, entry.getValue().toString());
            }

            props.store(fo, "Last storage time: " + (new Date()).toString());
        } catch (IOException ex) {
            VoxelGuest.log("Could not create file " + f.getAbsolutePath(), 2);
        } finally {
            try {
                if (fo != null) {
                    fo.close();
                }
            } catch (IOException ex) {
                VoxelGuest.log("##### -- FATAL ERROR -- ##### Failed to store data to " + f.getAbsolutePath(), 2);
                ex.printStackTrace();
            }
        }
    }

    private static final Object[] parseList(String input) {
        input = input.trim();
        String[] split = input.split(",");
        Object[] obj = new Object[split.length];

        for (int i = 0; i < obj.length; i++) {
            try {
                Integer _i = Integer.parseInt(split[i]);
                obj[i] = _i;

            } catch (NumberFormatException ex) {
                if (split[i].equals(Boolean.TRUE.toString()) || split[i].equals(Boolean.FALSE.toString())) {
                    Boolean bool = Boolean.parseBoolean(split[i]);
                    obj[i] = bool;
                    continue;
                }

                if (split[i].toLowerCase().startsWith("%list={")) {
                    VoxelGuest.log("I can't handle nested lists! Try an alternative storage method", 2);
                    break;
                }

                obj[i] = split[i];
                continue;
            }
        }

        return obj;
    }

    private static String writeList(Object[] obj) {
        String out = "%list={";

        for (int i = 0; i < obj.length; i++) {
            String toWrite = "";

            if (obj[i] instanceof Object[]) {
                toWrite = writeList((Object[]) obj[i]);
            } else {
                toWrite = obj[i].toString();
            }

            if (i == (obj.length - 1)) {
                out = out + toWrite + "}%";
            } else {
                out = out + toWrite + ",";
            }
        }

        return out;
    }
}