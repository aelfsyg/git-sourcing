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

(defn init
  "Synchronises a local and remote repository."
  [remote-repo local-repo]
  (let [repo (get-repo remote-repo local-repo)
        callback (jgit/git-pull repo)]
    (chime-at (periodic-seq (t/now) (-> 10 t/seconds))
              callback
              {:error-handler nil})))
