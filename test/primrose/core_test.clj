(ns primrose.core-test
  (:require [midje.sweet :refer :all]
            [primrose.core :as primrose]))

(defn- sleep [time value]
  (future
    (Thread/sleep time)
    value))

(fact "select-one will return the first future matching the predicate"
  @(primrose/select-one
     (fn [_] true)
     (sleep 0  1)
     (sleep 10 2)
     (sleep 10 3)) => 1
  @(primrose/select-one
     (fn [v] (= v 2))
     (sleep 0  1)
     (sleep 10 2)
     (sleep 0  3)) => 2)

(fact "select-one will return nil when none match"
  @(primrose/select-one
     (fn [_] false)
     (sleep 0 1)
     (sleep 0 2)
     (sleep 0 3)) => nil)

(fact "select-one has a convenience method for getting the first returned future"
  @(primrose/first
     (sleep 0  1)
     (sleep 10 2)
     (sleep 10 3)) => @(primrose/select-one
                          (fn [_] true)
                          (sleep 0  1)
                          (sleep 10 2)
                          (sleep 10 3)))

(fact "select-many will return the results of all futures that match the predicate"
  @(primrose/select-many
     (fn [_] true)
     (sleep 0 1)
     (sleep 0 2)
     (sleep 0 3)) => (just [1 2 3] :in-any-order)
   @(primrose/select-many
     (fn [v] (= 0 (mod v 2)))
     (sleep 0 1)
     (sleep 0 2)
     (sleep 0 4)) => (just [2 4] :in-any-order))

(fact "select-many will return an empty list if none match"
  @(primrose/select-many
     (fn [_] false)
     (sleep 0 1)
     (sleep 0 2)
     (sleep 0 3)) => [])

(fact "select-many has a convenience method for returning all futures"
  @(primrose/all
     (sleep 0 1)
     (sleep 0 2)
     (sleep 0 3)) => (just @(primrose/select-many
                        (fn [_] true)
                        (sleep 0 1)
                        (sleep 0 2)
                        (sleep 0 3)) :in-any-order))

