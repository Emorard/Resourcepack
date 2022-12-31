(ns com.pokkedoll.resourcepack
  (:import
    (com.viaversion.viaversion.api Via ViaAPI)
    (net.kyori.adventure.text Component)
    (org.bukkit Bukkit)
    (org.bukkit.command CommandSender ConsoleCommandSender)
    (org.bukkit.entity Player)
    (org.bukkit.event.player PlayerJoinEvent)
    (org.bukkit.scheduler BukkitRunnable)))

(gen-class
  :name com.pokkedoll.resourcepack.Main
  :extends com.pokkedoll.resourcepack.ClojurePlugin
  :main false
  :prefix "main-"
  :constructors {[] []})

(gen-class
  :name com.pokkedoll.resourcepack.Cmd
  :implements [org.bukkit.command.CommandExecutor]
  :main false
  :prefix "cmd-"
  :methods [[setResourcepack [org.bukkit.command.CommandSender String String] void]]
  :init "init"
  :state "state"
  :constructors {[com.pokkedoll.resourcepack.Main] []})

(gen-class
  :name com.pokkedoll.resourcepack.Lis
  :implements [org.bukkit.event.Listener]
  :main false
  :prefix "lis-"
  :init "init"
  :state "state"
  :constructors {[com.pokkedoll.resourcepack.Main] []}
  :methods [[^{org.bukkit.event.EventHandler true} onJoin [org.bukkit.event.player.PlayerJoinEvent] void]])

(defn setField
  [this key value]
  (swap! (.state this) into {key value}))

(defn getField [this key] (@(.state this) key))

; プロトコルバージョン
(defn legacy? [version] (if (<= version 404) "legacy" "modern"))

(defn main-onEnable
  [this]
  (-> this (.getCommand "resourcepack") (.setExecutor (com.pokkedoll.resourcepack.Cmd. this)))
  (-> (Bukkit/getPluginManager) (.registerEvents (com.pokkedoll.resourcepack.Lis. this) this)))

(defn main-decideResourcepackType
  "Return 'modern' or 'legacy' from player's version. if ViaAPI is nil, return 'modern'."
  [this ^Player player]
  (if-let [via ^ViaAPI (Via/getAPI)]
    (-> via
        (.getPlayerVersion (.getUniqueId player))
        (legacy?)))
  "modern")

(defn main-setResourcepack
  "Set resourcepack to player. Resourcepack must be 'modean' or 'legacy'"
  [^com.pokkedoll.resourcepack.Main this ^Player player ^String resourcepack-type]
  (if (or (= resourcepack-type "modern") (= resourcepack-type "legacy"))
    (if-let [url (-> this (.getConfig) (.getString resourcepack-type nil))]
      (do (-> player (.setResourcePack url))
          (-> player (.sendMessage (str "リソースパックを送信しています。"))))
      (-> player (.sendMessage (str "リソースパックのURLを取得できませんでした。"))))
    (-> player (.sendMessage (str "不正なリソースパックタイプです。")))))

(def msg-how2use
  (-> (Component/text "使い方: ")
      (.append (Component/newline))
      (.append (Component/text "* /resourcepack <action> <value>"))
      (.append (Component/newline))
      (.append (Component/text "例:"))
      (.append (Component/newline))
      (.append (Component/text "* /resourcepack use: リソースパックを適用"))
      (.append (Component/newline))
      (.append (Component/text "* /resourcepack set-modern <URL>: 1.13以上のクライアントのリソースパックを<URL>に指定"))
      (.append (Component/newline))
      (.append (Component/text "* /resourcepack set-legacy <URL>: 1.12.2以下のクライアントのリソースパックを<URL>に指定"))
      (.append (Component/newline))
      (.append (Component/text "TIPS: 1.11以下のクライアントは接続未確認かつサポート対象外です。"))))

(defn cmd-init [^com.pokkedoll.resourcepack.Main plugin]
  [[] (atom {:plugin plugin})])

(defn- cmd-setResourcepack
  "Update resourcepack URL."
  [^com.pokkedoll.resourcepack.Cmd this ^CommandSender sender ^String resourcepack-type ^String url]
  (if-let [_url url]
    (let [plugin (getField this :plugin)]
      (if (or (instance? ConsoleCommandSender sender)
              (and (instance? Player sender) (-> sender (.hasPermission "resourcepack.set"))))
        (do
          (-> plugin (.getConfig) (.set resourcepack-type _url))
          (-> plugin (.saveConfig))
          (-> plugin (.reloadConfig))
          (-> sender (.sendMessage "リソースパック情報をアップデートしました！")))
        (-> sender (.sendMessage "権限を所持していません")))
      )
    (-> sender (.sendMessage "URLが指定されていません"))))

(defn- cmd-applyResourcepack
  "Apply resourcepack."
  [this sender ^String resourepack-type]
  (let [plugin ^com.pokkedoll.resourcepack.Main (getField this :plugin)
        resourepack-type* (if (nil? resourepack-type) (main-decideResourcepackType plugin sender) (.toLowerCase resourepack-type))]
    (main-setResourcepack plugin sender resourepack-type*)))

(defn f
  "Parse 1st arguments. return 'default' if arguments is empty."
  [args] (if-let [a1 (first args)] a1 "default"))

(defn cmd-onCommand
  [this ^CommandSender sender _ _ args]
  (case (f args) "use" (when (instance? Player sender) (cmd-applyResourcepack this sender (second args)))
                 "set-modern" (cmd-setResourcepack this sender "modern" (second args))
                 "set-legacy" (cmd-setResourcepack this sender "legacy" (second args))
                 (-> sender (.sendMessage msg-how2use)))
  true)

(defn lis-init [^com.pokkedoll.resourcepack.Main plugin]
  [[] (atom {:plugin plugin})])

(defn lis-onJoin
  [this ^PlayerJoinEvent e]
  (let [plugin (getField this :plugin) player (.getPlayer e) resourcepack-type (main-decideResourcepackType plugin player)]
    (-> (proxy [BukkitRunnable] []
          (run [] (main-setResourcepack plugin player resourcepack-type)))
        (.runTaskLater plugin 20))))