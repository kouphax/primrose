(ns primrose.core
  "Core functions for primrose - A set of basic utility functions for working
   with collections of futures and selecting 1, some or all their results.

   There is currently no timeouts or error handling in place (and there really
   should be)"
  (:refer-clojure :exclude [first]))

(declare countdown tick stop)

(defn- countdown [start on-zero]
  (let [counter (atom start)]
    (add-watch counter nil
      #(when (<= %4 0)
         (on-zero)
         (stop counter)))))

(defn- tick [counter]
  (swap! counter dec))

(defn- stop [counter]
  (remove-watch counter nil))

(defn select-one
  "Selects a single return value from one of the passed futures based on the
   given predicate.  If all futures complete without matching the predicate
   the returned promised will be satisifed with `nil`

   No timeout operations are supported internally so if no future returns then
   neither will this method."
  [predicate & futures]
  (let [promise   (promise)
        count     (count futures)
        countdown (countdown count #(deliver promise nil))]
    (doseq [f futures]
      (future
        (let [result @f]
          (when (predicate result)
            (deliver promise result)
            (stop countdown))
          (tick countdown))))
    promise))

(defn select-many
  "Selects 0..N return values from the passed futures based on the given
   predicate.  If all futures complete without matching the predicate an
   empty seq is returned.

   No timeout operations are supported internally so if a future fails to
   return then neither will this method"
  [predicate & futures]
  (let [promise   (promise)
        count     (count futures)
        data      (atom [])
        countdown (countdown count #(deliver promise @data))]
    (doseq [f futures]
      (future
        (let [result @f]
          (when (predicate result)
            (swap! data concat [@f]))
          (tick countdown))))
    promise))

(defn first
  "Return the first future to be realised.

   A convenience method for `(select-one (fn [_] true) ...)` to avoid having to
   write the boiler plate predicate"
  [& futures]
  (apply select-one (cons (fn [_] true) futures)))

(defn all
  "Return all the futures when they are fully realised.

   A convenience method for `(select-many (fn [_] true) ...)` to avoid having to
   write the boiler plate predicate"
  [& futures]
  (apply select-many (cons (fn [_] true) futures)))

