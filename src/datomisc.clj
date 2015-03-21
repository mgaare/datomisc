(ns datomisc
  (:require [datomic.api]))

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

(defn attribute?
  "Return true is x is a datomic.Attribute"
  [x]
  (instance? datomic.Attribute x))

(defn entity-or-map?
  "Return true if x is an entity or map."
  [x]
  (or (entity? x)
      (map? x)))

;; Little helper function

(defn to-map
  "Coerces arg to map."
  [x]
  (if (map? x)
    x
    (into {} x)))

;; cases:
;; - receives an entitymap/map
;; - receives a single kv with entity ref
;; - receives single kv with value
;; - receives k with multiple v

;; map
;;
;; - check against max-depth, map over kvs with to-statements* and id
;;   set, add id to processed-ids, increment depth

;; single kv with entity ref
;; - generate single statement, and then recur with non-id version

;; multiple v
;; - map across vs, dispatching value vs map

(defn- to-statements*
  ([processed-ids max-depth cur-depth m]
   {:pre [(:db/id m)]}
   (when (or (not max-depth)
             (<= cur-depth max-depth))
     (let [id (:db/id m)]
       (when-not (contains? @processed-ids id)
         (swap! processed-ids conj id)
         (to-statements* processed-ids max-depth (inc cur-depth) id
                         (-> m to-map (dissoc :db/id)))))))
  ([processed-ids max-depth cur-depth cur-id xs]
   (lazy-seq
    (when (seq xs)
      (let [[k v] (first xs)
            nxt (fn [r] (to-statements* processed-ids max-depth cur-depth cur-id r))
            make-statement (fn [k v]
                             (if (entity-or-map? v)
                               (cons [cur-id k (:db/id v)]
                                     (to-statements* processed-ids max-depth cur-depth v))
                               [[cur-id k v]]))]
        (if (set? v)
          (concat
           (mapcat (partial make-statement k)
                   v)
           (nxt (rest xs)))
          (concat (make-statement k v) (nxt (rest xs)))))))))

(defn to-statements
  "Takes a Datomic entity or map with :db/id key, and returns lazy seq of
   [e a v] statements.
   Opts is a map with keys:
    - :max-depth - limits recursion depth (default no limit)"
  ([m]
   (to-statements m nil))
  ([m {:keys [max-depth] :as opts}]
   {:pre [(entity-or-map? m)
          (:db/id m)]}
   (to-statements* (atom #{}) max-depth 0 m)))

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
