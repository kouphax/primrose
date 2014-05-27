(ns primrose.core-test
  (:require [midje.sweet :refer :all]
            [primrose.core :as primrose]))

(defn- sleep [time value]
  (future
    (Thread/sleep time)
    value))

(fact "any can be used to short circuit a set of futures when the first one returns"
  @(primrose/first (sleep 100 1) (sleep 200 2)) => 1
  @(primrose/first (sleep 200 1) (sleep 100 2)) => 2)



