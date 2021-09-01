package me.plytki.virtualrealty.utils;

import org.apache.commons.lang.Validate;
import org.diorite.cfg.system.Template;
import org.diorite.cfg.system.TemplateCreator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConfigUtil {

    public static <T> T loadConfig(File file, Class<T> implementationFile) {
        Template<T> template = TemplateCreator.getTemplate(implementationFile);
        Constructor<T> implementationFileConstructor = (Constructor<T>) Reflections.getConstructor(implementationFile);
        T config;
        if (!file.exists()) {
            try {
                try {
                    config = template.fillDefaults(implementationFileConstructor.newInstance());
                } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException("Couldn't get access to " + implementationFile.getName() + "  constructor", e);
                }
                Validate.isTrue(file.createNewFile(), "Couldn't create " + file.getAbsolutePath() + " config file");
            } catch (final IOException e) {
                throw new RuntimeException("IO exception when creating config file: " + file.getAbsolutePath(), e);
            }
        } else {
            try {
                try {
                    config = template.load(file);
                    if (config == null) {
                        config = template.fillDefaults(implementationFileConstructor.newInstance());
                    }
                } catch (final IOException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException("IO exception when loading config file: " + file.getAbsolutePath(), e);
                }
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Couldn't get access to " + implementationFile.getName() + "  constructor", e);
            }
        }
        try {
            template.dump(file, config, false);
        } catch (final IOException e) {
            throw new RuntimeException("Can't dump configuration file!", e);
        }
        return config;
    }

}
