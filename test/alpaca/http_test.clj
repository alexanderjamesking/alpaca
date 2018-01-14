(ns alpaca.http-test
  (:require  [clojure.test :refer [deftest testing is use-fixtures]]
             [clojure.data.json :as json]
             [org.httpkit.client :as http]
             [clojure.core.async :as async :refer [<! >! go >!! <!! chan timeout mult tap close!]])
  (:import com.github.tomakehurst.wiremock.WireMockServer))

(def wiremock-url "http://localhost:8080")

(def wiremock-server (new WireMockServer))

(use-fixtures :once
  (fn [test-suite]
    (.start wiremock-server)
    (test-suite)
    (.stop wiremock-server)))

(use-fixtures :each
  (fn [test-to-run]
    (.resetAll wiremock-server)
    (test-to-run)))

(defn stub [options]
  @(http/post (str wiremock-url "/__admin/mappings")
              {:body (json/write-str options)}))

(defn stub-text
  ([url text]
   (stub-text url text 0))
  ([url text delay-ms]
   (stub {:request {:method "GET"
                    :url url}
          :response {:status 200
                     :fixedDelayMilliseconds delay-ms
                     :body text
                     :headers {:Content-Type "text/plain"}}})))

(deftest hello-wiremock-test
  (stub-text "/" "Hello World!")
  (is (= "Hello World!" (-> (http/get wiremock-url) deref :body))))

(deftest return-quickest-of-two-endpoints
  (stub-text "/a" "Response from A!" 200)
  (stub-text "/b" "Response from B!" 400)

  (let [a (chan)
        b (chan)]
    (http/get (str wiremock-url "/a") (fn [res]
                                        (go (>! a (:body res)))))
    (http/get (str wiremock-url "/b") (fn [res]
                                        (go (>! b (:body res)))))

    (is (= "Response from A!" (first (async/alts!! [a b]))))
    ;; to ensure we consume the result of b to end the test cleanly
    (is (= "Response from B!" (<!! b)))))
