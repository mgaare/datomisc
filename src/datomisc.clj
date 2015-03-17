(ns datomisc
  (:require [datomic.api :as d]))

(defprotocol Statement
  (e [x])
  (a [x])
  (v [x])
  (t [x]))

(extend-protocol Statement
  datomic.Datom
  (e [d]
    (.e d))
  (a [d]
    (.a d))
  (v [d]
    (.v d))
  (t [d]
    (.tx d))

  clojure.lang.Sequential
  (e [s]
    (nth s 0 nil))
  (a [s]
    (nth s 1 nil))
  (v [s]
    (nth s 2 nil))
  (t [s]
    (nth s 3 nil)))

(def statement?
  "Return true if x is a statement"
  (every-pred (partial satisfies? Statement)
              e a v))

(defn entity?
  "Return true if x is a datomic.query.EntityMap"
  [x]
  (instance? datomic.query.EntityMap x))

(defn entity-or-map?
  "Return true if x is an entity or map."
  [x]
  (or (entity? x)
      (map? x)))

(defn- to-statements*
  [id kvs]
  (lazy-seq
   (when (seq kvs)
     (let [[k v] (first kvs)]
       (if (coll? v)
         (let [ss (if (entity-or-map? v)
                    (to-statements v)
                    (map (partial vector id k) v))]
           (concat ss (rest kvs)))
         (cons (vector id k v) (to-statements* id (rest kvs))))))))

;; key problem I haven't solved yet: nested entities
;; - limit recursion depth?
;; - how to handle cycles? pass around a set of ids that have already been added?


(defn to-statements
  "Takes a Datomic entity or map with :db/id key, and returns lazy seq of
  [e a v] statements."
  [m]
  {:pre [(or (entity? m) (map? m))
         (:db/id m)]}
  (let [id (:db/id m)]
    (->> (dissoc m :db/id)
         (to-statements* id))))

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
