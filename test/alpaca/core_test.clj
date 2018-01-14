(ns alpaca.core-test
  (:require [clojure.test :refer :all]
            [alpaca.core :refer :all]
            [clojure.core.async :as async :refer [<! >! go >!! <!! chan timeout mult tap close!]]))

(deftest a-test
  (testing "Basic core async"
    (let [c (chan)]
      (go (>! c "Hello World!"))
      (is (= "Hello World!" (<!! c))))))


(deftest one-input-two-outputs-test
  (testing "One input to two outputs"
    (let [topic (chan)
          topic-mult (mult topic)
          output-a (chan)
          output-b (chan)]

      ;; subscribe to the topic
      (tap topic-mult output-a)
      (tap topic-mult output-b)

      (go (>! topic "Hello World!"))
      (is (= "Hello World!" (<!! output-a)))
      (is (= "Hello World!" (<!! output-b))))))


;; alts to get value or timeout
(deftest result-or-timeout-test
  (testing "result"
    (is (= :result (let [out (chan)]
                     (go (<! (timeout 40))
                         (>! out :result))
                     (if-let [res (first (async/alts!! [out (timeout 80)]))]
                       res
                       :timed-out)))))

  (testing "timed-out"
    (is (= :timed-out (let [out (chan)]
                        (go (<! (timeout 40))
                            (>! out :result))
                        (if-let [res (first (async/alts!! [out (timeout 20)]))]
                          res
                          :timed-out))))))

(deftest aggregate-results-test
  (testing "Get results from both channels"
    (let [c (chan)
          c2 (chan)
          mc (async/merge [c c2])
          _ (go (>! c {:a "Hello"}))
          _ (go (>! c2 {:b "World"}))
          result (reduce (fn [results _]
                           (let [m (<!! mc)]
                             (merge results m))) {}  (range 2))]
      (is (= {:a "Hello"
              :b "World"}
             result)))))
