(ns cljagents.prob5
  "Utilize `core.logic` to implement a simple wandering walk for the bot."
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic.arithmetic :as ar])
  (:use [cljagents.agent]
        [clojure.core.logic]
        [clojure.core.async :only [<!!]]))

;; In general terms, core.logic acts as the 'brains' for the bot,
;; deciding where to go next based on input from

(def goal (atom {}))
(def has-gold (atom false))
(def direction (atom 0))

(declare next-move take-action)

(defn assignment
  "Given a pair of coordinates, scour the map until the item is found, and
  then return to the provided coordinates"
  [[x y] in out]
  (reset! goal {:x x :y y})
  (spawn-bot in out)
  (move-for in 0.5 :heading 0)
  (loop [action (first (run 1 [q] (next-move (<!! out) q)))]
    (println ";;; Action result:" action)
    (if (= (:action action) :stop)
      (println "Completed.")
      (do
        (take-action in action)
        (recur (first (run 1 [q] (next-move (<!! out) q))))))))


;;; Goal functions
(defn find-gold [msgs action]
  (let [gold (filter (fn [msg]
                       (some #(= "quagent_item_gold" %) (:data msg)))
                     msgs)
        msg (if (nil? gold)
              (first msgs)
              (first gold))]
    (fresh [a dist dir]
      (== a "quagent_item_gold")
      (conda
       [(membero a (:data msg))
        (== dist (if (> (count (:data msg)) 1)
                   (read-string (nth (:data msg) 2))))
        (== dir (if (> (count (:data msg)) 1)
                  (read-string (nth (:data msg) 3))))
        (== action {:action :move-by :data {:dist  dist :dir dir}})]
       [(== action {:action :location})]))))

(defn where-to? [msg action]
  (fresh [cx cy x y dist dir done-walking]
    (== cx (read-string (nth (:data msg) 0)))
    (== cy (read-string (nth (:data msg) 1)))
    (matche [@has-gold]
            [[true] (conda
                     [(project [cx cy]
                               (ar/<= (euclidean-distance
                                       [(:x @goal) (:y @goal)]
                                       [cx cy])
                                      150))
                      (== x (:x @goal))
                      (== y (:y @goal))
                      (== action {:action :move-to :data {:x x :y y}})]
                     [(== action {:action :move-for})])]
            [[false] (== action {:action :move-for})])))

(defn at-goal [msg action]
  (fresh [done-walking]
    (== done-walking @has-gold)
    (conde
     [(== done-walking true)
      (== action {:action :stop})]
     [(== done-walking false)
      (== action {:action :pick-up})])))

(defn check-blocking [msg action]
  (fresh [a]
    (== a "blocked")
    (conda
     [(membero a (:data msg))
      (== action {:action :turn-around})]
     [(== action {:action :radar})])))

(defn next-move
  "The brain of the bot. Given a response from the engine,
  determine the next move."
  [{:keys [op msgs] :as response} action]
  (matche [op]
          [[:ra] (find-gold (:msgs response) action)]
          [[:lc] (where-to? (first (filter #(= :rs (:type %))
                                           (:msgs response))) action)]
          [[:mt] (at-goal (first (:msgs response)) action)]
          [[:mf] (check-blocking (first (:msgs response)) action)]
          [[:pu] (== action {:action :location})]
          [[:mb] (== action {:action :pick-up})]))


;;; Process the result from the brain
(defmulti take-action
  "Based on the response from the 'brain' of the bot, take
  some action in the engine."
  (fn [c response] (:action response)))

(defmethod take-action :pick-up
  [c response]
  (reset! has-gold true)
  (pick-up c))

(defmethod take-action :move-for
  [c {:keys [action] :as response}]
  (swap! direction #(mod (+ % (- (rand-int 40) 20)) 360))
  (move-for c 0.25 :heading @direction))

(defmethod take-action :move-by
  [c {:keys [action data] :as response}]
  (move-by c (:dist data) :heading (:dir data)))

(defmethod take-action :move-to
  [c {:keys [action data] :as response}]
  (move-to c (:x data) (:y data)))

(defmethod take-action :location
  [c response]
  (location? c))

(defmethod take-action :radar
  [c response]
  (radar c 100))

(defmethod take-action :turn-around
  [c response]
  (swap! direction #(mod (+ % (+ 105 (rand-int 150))) 360))
  (location? c))

(defmethod take-action :default
  [c response]
  (println ";; Ignored response:" response))
