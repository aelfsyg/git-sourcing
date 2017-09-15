(ns git-sourcing.core
  (:require [chime :refer [chime-at]]
            [clj-jgit.porcelain :as jgit]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]))

(defn get-repo
  "Either returns the existing repo, or clones the remote repo."
  ([local-repo]
   (jgit/load-repo local-repo))
  ([remote-repo local-repo]
   (try (get-repo local-repo)
        (catch FileNotFoundException fne
          (:repo (jgit/git-clone-full remote-repo local-repo))))))

(def default-opts
  nil
  {:period (-> 10 t/seconds)})

(defn init
  "Synchronises a local and remote repository."
  ([remote-repo local-repo]
   (init remote-repo local-repo {}))
  ([remote-repo local-repo options]
   (let [repo (get-repo remote-repo local-repo)
         callback (fn [_] (jgit/git-pull repo))
         {:keys [period]} (merge default-opts options)]
     (chime-at (periodic-seq (t/now) period)
               callback
               {:error-handler nil}))))
