(ns user
  (:require [clojure.pprint :refer (pprint)]
            [clojure.tools.namespace.repl :refer :all]
            [datomic.api :as d]
            [datomisc :refer :all]))

(def d-uri "datomic:mem://test")

(defn connect
  []
  (d/create-database d-uri)
  (d/connect d-uri))

;; features we want
; map <-> statements
; ensure schema
; entity? predicate
; add :db/add
