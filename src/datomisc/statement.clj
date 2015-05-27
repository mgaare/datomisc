(ns datomisc.statement
  (:require [datomic.api :as d]
            [datomisc :as dm]))

;; A function like this should perhaps be in clojure.core
(defn map-entry?
  "Returns true if x is a MapEntry or vector with length 2, false otherwise."
  [x]
  (or (instance? java.util.Map$Entry x)
      (and (vector? x)
           (= 2 (count x)))))

(defn remove-command
  "Given a statement, removes :db/add or :db/remove if present."
  [stmt]
  (if (and (sequential? stmt)
           (#{:db/add :db/remove} (first stmt)))
    (rest stmt)
    stmt))

(defn remove-commands
  "Given a collection of transaction statements, removes :db/add or :db/remove
  if present."
  [stmts]
  (map remove-command stmts))

(defn prepend-command
  "Takes a command (one of :db/add or :db/remove) and a datomic transaction
   statement, and returns the statement with the command added."
  [cmd stmt]
  (cons cmd stmt))

(defn prepend-commands
  "Takes a command (one of :db/add or :db/remove) and a collection of datomic
   transaction statements, and returns the statements with the command added."
  [cmd stmts]
  (map (partial prepend-command cmd) stmts))

(defn attribute-exists?
  "Accepts a Datomic db and an attribute keyword, and returns true if the
  attribute is defined in the db."
  [db attr]
  (dm/attribute? (d/attribute db attr)))

(defn uses-existing-attribute?
  "Accepts a Datomic db and a statement either in the form of a
   datomisc.Statement, a MapEntry, or a 2-entry vector."
  [db stmt]
  {:pre [(or (dm/statement? stmt) (map-entry? stmt))]}
  (cond (dm/statement? stmt)
        (attribute-exists? db (dm/a (remove-command stmt)))
        :else
        (let [a (first stmt)]
          (or (= :db/id a)
              (attribute-exists? db a)))))

(defn remove-missing-attributes
  "Accepts a Datomic db and a collection of statements, and removes any
   statements that use attributes that don't exist in the db."
  [db stmts]
  (filter (partial uses-existing-attribute? db) stmts))
