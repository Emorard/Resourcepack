(defproject Resourcepack "0.1.0-SNAPSHOT"
  :description "Resourcepack for legacy(>=1.12.2) and modern(1.12.2<) users."
  :url "https://github.com/Emorard/Resourcepack"
  :license {:name "The MIT License (MIT)"
            :url "https://github.com/Emorard/Resourcepack/LICENSE"}
  :repositories [["spigotmc" "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"]
                 ["papermc" "https://papermc.io/repo/repository/maven-public/"]
                 ["snapshots" "https://oss.sonatype.org/content/repositories/snapshots"]
                 ["viaversion-repo" "https://repo.viaversion.com"]]
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :profiles {:provided {:dependencies [[io.papermc.paper/paper-api "1.17.1-R0.1-SNAPSHOT" :scope "runtime"]
                                       [com.viaversion/viaversion-api "4.0.0" :scope "provided"]
                                       [io.netty/netty-all "4.1.86.Final"]]}}
  :aot :all
  :repl-options {:init-ns com.pokkedoll.resourcepack})
