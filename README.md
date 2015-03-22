# datomisc

A bunch of Datomic utility functions that I'm tired of writing in
projects that use Datomic.

## Installation

Leiningen atom:

    [datomisc "0.1.1"]

If your project uses Datomic pro, then do instead:

    [datomisc "0.1.1" :exclusions [com.datomic/datomic-free]]

## Usage

```clj
(require '[datomisc :as dm]
         '[datomic.api :as d])

(def d-uri "datomic:mem://dm")
(d/create-database d-uri)
(def conn (d/connect d-uri))
(def db (d/db conn))

;; Some useful predicates

;; 3+-vectors and datoms are statements
(dm/statement? [12345 :attr "value"]) ; true
(dm/statement? (first (d/datoms db :eavt))) ; true

;; handy to have around
(dm/entity? (d/entity db :db.part/db)) ; true
(dm/entity? {:db/id 12345 :justa "map")) ; false

(dm/attribute? (d/attribute db :db/cardinality)) ; true
(dm/attribute? (d/entity db :db/cardinality)) ; false
(dm/attribute? {:db/id 12345 :justa "map")) ; false

(dm/entity-or-map? {:hello "world"}) ; true
(dm/entity-or-map (d/entity db :db.part/db)) ; true

(dm/to-statements (d/entity db :db/cardinality))
; ([41
;   :db/doc
;   "Property of an attribute. Two possible values: :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes. Defaults to :db.cardinality/one."]
;  [41 :db/cardinality :db.cardinality/one]
;  [41 :db/valueType :db.type/ref]
;  [41 :db/ident :db/cardinality])

(dm/to-entity-maps (dm/to-statements (d/entity db :db/cardinality)))
; ({:db/id 41,
;   :db/ident #{:db/cardinality},
;   :db/valueType #{:db.type/ref},
;   :db/cardinality #{:db.cardinality/one},
;   :db/doc
;   #{"Property of an attribute. Two possible values: :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes. Defaults to :db.cardinality/one."}})
```

## Todo

- Simple ensure-schema functionality

## License

Copyright © 2015 Michael Gaare

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
