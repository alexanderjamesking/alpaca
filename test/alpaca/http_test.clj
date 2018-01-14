(ns alpaca.http-test
  (:require  [clojure.test :refer [deftest testing is use-fixtures]]
             [clojure.data.json :as json]
             [org.httpkit.client :as http])
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

(deftest hello-wiremock-test
  (stub {:request {:method "GET"
                   :url "/"}
         :response {:status 200
                    :fixedDelayMilliseconds 100
                    :body "Hello World!"
                    :headers {:Content-Type "text/plain"}}})
  (is (= "Hello World!" (-> (http/get wiremock-url) deref :body))))
