(ns datomisc.test-utils
  (:require [datomic.api :as d]))
(def ^:dynamic *db*)

(def datomic-uri
  "datomic:mem://test")

(defn connect
  ([]
   (connect datomic-uri))
  ([uri]
   (d/create-database uri)
   (d/connect uri)))

(def test-schema
  [{:db/id (d/tempid :db.part/db)
    :db/ident :name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :one-ref
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :many-ref
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :one-val
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :many-val
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}])

(def test-data
  (let [[eid1 eid2 eid3 eid4 eid5 eid6] (repeatedly #(d/tempid :db.part/user))]
    [{:db/id eid1
      :name "start"
      :one-ref {:db/id eid2
                :many-val #{"good" "day" "sir"}
                :one-ref {:db/id eid3
                             :one-val "three"
                             :one-ref eid1}}
      :one-val "hello"
      :many-ref #{eid4 eid5 eid6}
      :many-val #{"on" "my" "way"}}
     {:db/id eid4
      :one-val "Hi there"
      :many-val #{"my" "name" "is" "Dave"}}
     {:db/id eid5
      :one-val "Dinosaurs"
      :many-ref #{eid6}}
     {:db/id eid6
      :one-val "to the sixes"
      :many-val #{"Sinatra"}}]))

(defn with-test-db
  [f]
  (let [conn (connect)]
    (binding [*db* (d/db conn)]
      (f))))
