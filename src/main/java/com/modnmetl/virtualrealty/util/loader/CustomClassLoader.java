package com.modnmetl.virtualrealty.util.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomClassLoader extends URLClassLoader {

    public CustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

}
