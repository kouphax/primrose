(ns primrose.core
  (:refer-clojure :exclude [first]))

(defprotocol ^:private Countdown
  (start   [me value action])
  (tick    [me])
  (stop    [me])
  (current [me]))

(deftype ^:private Countdowner [counter-atom]
  Countdown
  (start [me value action]
    (stop me)
    (reset! counter-atom value)
    (add-watch counter-atom nil
               #(when (< %4 1)
                  (action)
                  (stop me)))
    me)
  (tick [me]
    (swap! counter-atom dec))
  (stop [me]
    (remove-watch counter-atom nil)
    me)
  (current [me] @counter-atom))

(defn- countdown [value action]
  (let [counter (Countdowner. (atom 0))]
    (start counter value action)
    counter))

(defn first-of
  "Similar to `any` except return is based on a predicate"
  [predicate & futures]
  (let [promise (promise)
        count   (count futures)
        countdown (countdown count #(deliver promise false))]
    (doseq [f futures]
      (future
        (let [result @f]
          (when (predicate result)
            (deliver promise result)
            (stop countdown))
          (tick countdown))))
    promise))

(defn first
  "Delivers the promise when any future returns.  Will deref futures"
  [& futures]
  (apply first-of (cons (fn [_] true) futures)))

(defn all
  "Delivers the promise when all futures return. Will deref futures"
  [& futures]
  (let [promise   (promise)
        data      (atom [])
        count     (count futures)
        countdown (countdown count #(deliver promise @data))]
    (doseq [f futures]
      (future
        (swap! data concat [@f])
        (tick countdown)))
    promise))
