(defproject primrose "0.1.0"
  :description "A set of utility methods for working with collections of futures"
  :url "https://github.com/kouphax/primrose"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles { :dev { :plugins [[lein-midje "3.0.0"]]
                     :dependencies [[midje "1.6.3"]] } })
