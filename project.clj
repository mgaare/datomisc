(defproject datomisc "0.1.2-SNAPSHOT"
  :description "The Datomic utility functions I'm tired having to write."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; exclude me if you want to use pro
                 [com.datomic/datomic-free "0.9.5344"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}})
