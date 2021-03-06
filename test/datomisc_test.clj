(ns datomisc-test
  (:require [clojure.test :refer :all]
            [datomisc.test-utils :refer :all]
            [datomic.api :as d])
  (:use datomisc))

(use-fixtures :once with-test-db)

(deftest flat-entity-map-test
  (let [in {:db/id 12345
            :one-val "hi"
            :many-val #{"there" "guys"}}
        test-output (to-statements in)]
    (is (= #{[12345 :one-val "hi"]
             [12345 :many-val "there"]
             [12345 :many-val "guys"]}
           (set test-output)))
    (is (= (count test-output) 3))))

(deftest nested-entity-map-test
  (let [in {:db/id 12345
            :one-ref {:db/id 54321
                      :one-val "hi"}
            :many-ref #{{:db/id 6789
                          :many-val #{"there" "guys"}}}}
        test-output (to-statements in)]
    (is (= #{[12345 :one-ref 54321]
             [54321 :one-val "hi"]
             [12345 :many-ref 6789]
             [6789 :many-val "there"]
             [6789 :many-val "guys"]}
           (set test-output)))
    (is (= (count test-output) 5))))

(deftest entity-map-cycle-test
  (let [in {:db/id 1
            :one-ref {:db/id 2
                      :one-val "hi"
                      :many-ref #{{:db/id 3
                                   :one-ref {:db/id 1}}
                                  {:db/id 1}}}}
        test-output (to-statements in)]
    (is (= #{[1 :one-ref 2]
             [2 :one-val "hi"]
             [2 :many-ref 3]
             [2 :many-ref 1]
             [3 :one-ref 1]}
           (set test-output)))
    (is (= (count test-output) 5))))

(deftest recursion-limit-test
  (let [in {:db/id 0
            :one-ref {:db/id 1
                      :one-ref
                      {:db/id 2
                       :one-ref {:db/id 3}}}}
        test-output (to-statements in {:max-depth 1})]
    (is (= #{[0 :one-ref 1]
             [1 :one-ref 2]})
        (set test-output))
    (is (= (count test-output) 2))))

(deftest statement?-test
  (is (statement? [:e :a :v]))
  (is (statement? (first (d/datoms *db* :eavt))))
  (is (not (statement? [:e :a])))
  (is (not (statement? (d/entity *db* :db.part/db)))))

(deftest attribute?-test
  (is (attribute? (d/attribute *db* :db/index)))
  (is (not (attribute? {:db/id 123 :db/ident :db/index}))))

(deftest entity-or-map?-test
  (is (entity-or-map? {:a 1}))
  (is (entity-or-map? (d/entity *db* :db.part/db)))
  (is (not (entity-or-map? [:e :a :v]))))

(deftest db-id?-test
  (is (db-id? (d/tempid :db.part/user)))
  (is (not (db-id? {:db/id 123}))))
