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

(defn to-entity-maps
  "Takes a collection of statements, and returns collection of entity maps."
  [xs]
  (->> (reduce
        (fn [em s]
          (let [[e a v] ((juxt e a v) s)]
            (update-in em [e a]
                       (fn [x]
                         (if x
                           (conj x v)
                           #{v})))))
        {}
        xs)
       (map (fn [[k v]]
              (assoc v :db/id k)))))
