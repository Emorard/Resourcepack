package com.pokkedoll.resourcepack;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class ClojurePlugin extends JavaPlugin {

  static {
    Thread.currentThread().setContextClassLoader(ClojurePlugin.class.getClassLoader());
  }

}
